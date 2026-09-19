package de.mupibox.control.data.repository

import de.mupibox.control.data.api.BluetoothResponse
import de.mupibox.control.data.api.MuPiBoxApi
import de.mupibox.control.data.api.PlayerStatus
import de.mupibox.control.data.api.SpotifyStatus
import de.mupibox.control.data.api.SystemStatus
import de.mupibox.control.model.BoxEndpoint
import de.mupibox.control.model.PlaybackSource
import de.mupibox.control.model.PlaybackSourceSelector

class ControlRepository(private val api: MuPiBoxApi) {
    suspend fun player(box: BoxEndpoint): PlayerStatus = api.playerStatus(box)
    suspend fun system(box: BoxEndpoint): SystemStatus = api.systemStatus(box)
    suspend fun spotify(box: BoxEndpoint): SpotifyStatus = api.spotifyStatus(box)

    /** Expensive on current MuPiBox-NG: may scan for ~8 seconds and can require admin auth. */
    suspend fun bluetoothOnDemand(box: BoxEndpoint): BluetoothResponse = api.bluetoothStatus(box)

    suspend fun playPause(box: BoxEndpoint, player: PlayerStatus, spotify: SpotifyStatus) {
        if (PlaybackSourceSelector.active(player, spotify) == PlaybackSource.SPOTIFY) {
            api.spotifyCommand(box, if (spotify.playing) "pause" else "resume")
        } else {
            api.playerCommand(box, if (player.isPlaying) "pause" else "play")
        }
    }

    suspend fun previous(box: BoxEndpoint, player: PlayerStatus, spotify: SpotifyStatus) {
        if (PlaybackSourceSelector.active(player, spotify) == PlaybackSource.SPOTIFY) {
            api.spotifyCommand(box, "previous")
        } else {
            api.playerCommand(box, "previous")
        }
    }

    suspend fun next(box: BoxEndpoint, player: PlayerStatus, spotify: SpotifyStatus) {
        if (PlaybackSourceSelector.active(player, spotify) == PlaybackSource.SPOTIFY) {
            api.spotifyCommand(box, "next")
        } else {
            api.playerCommand(box, "next")
        }
    }

    suspend fun setVolumePercent(box: BoxEndpoint, player: PlayerStatus, spotify: SpotifyStatus, percent: Int) {
        val p = percent.coerceIn(0, 100)
        if (PlaybackSourceSelector.active(player, spotify) == PlaybackSource.SPOTIFY) {
            val steps = spotify.volumeSteps.coerceAtLeast(1)
            val raw = ((p / 100.0) * steps).toInt().coerceIn(0, steps)
            api.spotifyCommand(box, "volume", raw)
        } else {
            val max = player.maxVolume.coerceAtLeast(1)
            val raw = ((p / 100.0) * max).toInt().coerceIn(0, max)
            api.playerCommand(box, "volume", raw)
        }
    }

    suspend fun speak(box: BoxEndpoint, text: String) = api.speak(box, text)
}
