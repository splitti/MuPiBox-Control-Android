package de.mupibox.control.data.api

import com.google.gson.Gson
import de.mupibox.control.model.BoxEndpoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit

class MuPiBoxClient(
    private val http: OkHttpClient = OkHttpClient.Builder()
        .dns(LanOnlyDns)
        .connectTimeout(3, TimeUnit.SECONDS)
        .readTimeout(22, TimeUnit.SECONDS) // Piper cache miss may take close to 20 s on Pi hardware.
        .writeTimeout(5, TimeUnit.SECONDS)
        .callTimeout(25, TimeUnit.SECONDS)
        .build(),
    private val gson: Gson = Gson(),
) : MuPiBoxApi {
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    override suspend fun health(box: BoxEndpoint): HealthResponse = get(box, "/api/health")
    override suspend fun playerStatus(box: BoxEndpoint): PlayerStatus = get(box, "/api/status")
    override suspend fun systemStatus(box: BoxEndpoint): SystemStatus = get(box, "/api/system")
    override suspend fun info(box: BoxEndpoint): InfoResponse = get(box, "/api/info")
    override suspend fun spotifyStatus(box: BoxEndpoint): SpotifyStatus = get(box, "/api/spotify/status")

    override suspend fun bluetoothStatus(box: BoxEndpoint): BluetoothResponse =
        get(box, "/api/connectivity/bluetooth")

    override suspend fun playerCommand(box: BoxEndpoint, action: String, value: Number?) {
        val payload = mutableMapOf<String, Any>("action" to action)
        value?.let { payload["value"] = it }
        post<Unit>(box, "/api/command", payload)
    }

    override suspend fun spotifyCommand(box: BoxEndpoint, action: String, value: Number?): SpotifyStatus {
        val payload = mutableMapOf<String, Any>("action" to action)
        value?.let { payload["value"] = it }
        return post(box, "/api/spotify/command", payload)
    }

    override suspend fun speak(box: BoxEndpoint, text: String) {
        val trimmed = text.trim()
        require(trimmed.isNotEmpty()) { "Text darf nicht leer sein." }
        post<Unit>(
            box,
            "/api/speak",
            mapOf(
                "source_type" to "android-app",
                "source_ref" to "mupibox-control",
                "text" to trimmed,
            ),
        )
    }

    private suspend inline fun <reified T> get(box: BoxEndpoint, path: String): T = withContext(Dispatchers.IO) {
        val endpoint = LocalEndpointValidator.validate(box).getOrThrow()
        val request = Request.Builder().url(endpoint.baseUrl + path).get().build()
        execute(request)
    }

    private suspend inline fun <reified T> post(
        box: BoxEndpoint,
        path: String,
        payload: Any,
    ): T = withContext(Dispatchers.IO) {
        val endpoint = LocalEndpointValidator.validate(box).getOrThrow()
        val body = gson.toJson(payload).toRequestBody(jsonMediaType)
        val request = Request.Builder().url(endpoint.baseUrl + path).post(body).build()
        execute(request)
    }

    private inline fun <reified T> execute(request: Request): T {
        http.newCall(request).execute().use { response ->
            val raw = response.body.string()
            if (!response.isSuccessful) {
                val message = runCatching {
                    @Suppress("UNCHECKED_CAST")
                    (gson.fromJson(raw, Map::class.java)["error"] as? String)
                }.getOrNull() ?: "HTTP ${response.code}"
                throw ApiError(response.code, message)
            }
            if (T::class == Unit::class) {
                @Suppress("UNCHECKED_CAST")
                return Unit as T
            }
            if (raw.isBlank()) throw IOException("Leere Antwort von ${request.url}")
            return gson.fromJson(raw, T::class.java)
        }
    }
}
