package de.mupibox.control.data.repository

import de.mupibox.control.data.api.BluetoothResponse
import de.mupibox.control.data.api.HealthResponse
import de.mupibox.control.data.api.InfoResponse
import de.mupibox.control.data.api.MuPiBoxApi
import de.mupibox.control.data.api.PlayerStatus
import de.mupibox.control.data.api.SpotifyStatus
import de.mupibox.control.data.api.SpotifyTrack
import de.mupibox.control.data.api.SystemStatus
import de.mupibox.control.data.api.TrackDto
import de.mupibox.control.model.BoxEndpoint
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ControlRepositoryTest {
    private val box = BoxEndpoint(name = "Test", host = "192.168.2.114")

    @Test fun localVolumePercentUsesServerMaxVolume() = runTest {
        val api = FakeApi()
        val repo = ControlRepository(api)
        val player = PlayerStatus(volume = 10, maxVolume = 50)

        repo.setVolumePercent(box, player, SpotifyStatus(), 40)

        assertEquals(Command("local", "volume", 20), api.lastCommand)
    }

    @Test fun spotifyVolumePercentUsesSpotifySteps() = runTest {
        val api = FakeApi()
        val repo = ControlRepository(api)
        val player = PlayerStatus(state = "stopped")
        val spotify = SpotifyStatus(paused = true, volumeSteps = 1000, track = SpotifyTrack(name = "Song"))

        repo.setVolumePercent(box, player, spotify, 25)

        assertEquals(Command("spotify", "volume", 250), api.lastCommand)
    }

    @Test fun pausedLocalQueueTakesPrecedenceOverStaleSpotifyTrack() = runTest {
        val api = FakeApi()
        val repo = ControlRepository(api)
        val player = PlayerStatus(
            state = "paused",
            queue = listOf(TrackDto(id = "1", title = "Local")),
            index = 0,
        )
        val spotify = SpotifyStatus(paused = true, track = SpotifyTrack(name = "Old Spotify"))

        repo.playPause(box, player, spotify)

        assertEquals(Command("local", "play", null), api.lastCommand)
    }

    private data class Command(val source: String, val action: String, val value: Number?)

    private class FakeApi : MuPiBoxApi {
        var lastCommand: Command? = null

        override suspend fun health(box: BoxEndpoint) = HealthResponse(status = "ok")
        override suspend fun playerStatus(box: BoxEndpoint) = PlayerStatus()
        override suspend fun systemStatus(box: BoxEndpoint) = SystemStatus()
        override suspend fun info(box: BoxEndpoint) = InfoResponse()
        override suspend fun spotifyStatus(box: BoxEndpoint) = SpotifyStatus()
        override suspend fun bluetoothStatus(box: BoxEndpoint) = BluetoothResponse()
        override suspend fun playerCommand(box: BoxEndpoint, action: String, value: Number?) {
            lastCommand = Command("local", action, value)
        }
        override suspend fun spotifyCommand(box: BoxEndpoint, action: String, value: Number?): SpotifyStatus {
            lastCommand = Command("spotify", action, value)
            return SpotifyStatus()
        }
        override suspend fun speak(box: BoxEndpoint, text: String) = Unit
    }
}
