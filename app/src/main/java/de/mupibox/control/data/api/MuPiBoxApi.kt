package de.mupibox.control.data.api

import de.mupibox.control.model.BoxEndpoint

/** Wire-level MuPiBox API used by repositories. Kept as an interface so JVM tests need no HTTP. */
interface MuPiBoxApi {
    suspend fun health(box: BoxEndpoint): HealthResponse
    suspend fun playerStatus(box: BoxEndpoint): PlayerStatus
    suspend fun systemStatus(box: BoxEndpoint): SystemStatus
    suspend fun info(box: BoxEndpoint): InfoResponse
    suspend fun spotifyStatus(box: BoxEndpoint): SpotifyStatus
    suspend fun bluetoothStatus(box: BoxEndpoint): BluetoothResponse
    suspend fun playerCommand(box: BoxEndpoint, action: String, value: Number? = null)
    suspend fun spotifyCommand(box: BoxEndpoint, action: String, value: Number? = null): SpotifyStatus
    suspend fun speak(box: BoxEndpoint, text: String)
}
