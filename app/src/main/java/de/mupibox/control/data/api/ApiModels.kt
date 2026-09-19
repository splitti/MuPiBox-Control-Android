package de.mupibox.control.data.api

import com.google.gson.annotations.SerializedName

data class PlayerStatus(
    val state: String = "stopped",
    val backend: String = "",
    @SerializedName("folder_id") val folderId: String = "",
    val folder: String = "",
    val cover: String? = null,
    val queue: List<TrackDto> = emptyList(),
    val index: Int = -1,
    val position: Double = 0.0,
    val duration: Double = 0.0,
    val volume: Int = 0,
    @SerializedName("max_volume") val maxVolume: Int = 100,
    val error: String? = null,
) {
    val currentTrack: TrackDto? get() = queue.getOrNull(index)
    val isPlaying: Boolean get() = state == "playing"
    val isPaused: Boolean get() = state == "paused"
}

data class TrackDto(
    val id: String = "",
    val title: String = "",
    val provider: String? = null,
    @SerializedName("resume_policy") val resumePolicy: String? = null,
)

data class SystemStatus(
    val online: Boolean = false,
    val wifi: WiFiStatus = WiFiStatus(),
    val battery: BatteryStatus = BatteryStatus(),
)

data class WiFiStatus(
    val connected: Boolean = false,
    @SerializedName("interface") val interfaceName: String? = null,
    @SerializedName("signal_dbm") val signalDbm: Int? = null,
    @SerializedName("quality_percent") val qualityPercent: Int? = null,
)

data class BatteryStatus(
    val available: Boolean = false,
    val percent: Int? = null,
    val charging: Boolean = false,
)

data class InfoResponse(
    val version: String = "",
    val simulation: Boolean = false,
    val backend: String = "",
    val theme: String? = null,
    @SerializedName("settings_persistent") val settingsPersistent: Boolean = false,
)

data class SpotifyStatus(
    val connected: Boolean = false,
    val playing: Boolean = false,
    val paused: Boolean = false,
    val buffering: Boolean = false,
    val volume: Int = 0,
    @SerializedName("volume_steps") val volumeSteps: Int = 100,
    val track: SpotifyTrack? = null,
)

data class SpotifyTrack(
    val name: String = "",
    val artists: List<String> = emptyList(),
    val album: String = "",
    val cover: String? = null,
    @SerializedName("position_ms") val positionMs: Long = 0,
    @SerializedName("duration_ms") val durationMs: Long = 0,
)

data class HealthResponse(
    val status: String = "",
    val version: String = "",
)

data class BluetoothDeviceDto(
    val address: String = "",
    val name: String = "",
    val paired: Boolean = false,
    val trusted: Boolean = false,
    val connected: Boolean = false,
)

data class BluetoothResponse(
    val enabled: Boolean = true,
    val devices: List<BluetoothDeviceDto> = emptyList(),
)

data class ApiError(val code: Int, override val message: String) : Exception(message)
