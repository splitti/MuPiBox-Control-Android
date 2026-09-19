package de.mupibox.control.model

import de.mupibox.control.data.api.PlayerStatus
import de.mupibox.control.data.api.SpotifyStatus
import de.mupibox.control.data.api.SpotifyTrack
import de.mupibox.control.data.api.TrackDto
import org.junit.Assert.assertEquals
import org.junit.Test

class PlaybackSourceSelectorTest {
    private val spotifyPaused = SpotifyStatus(paused = true, track = SpotifyTrack(name = "Spotify"))

    @Test fun localPausedSessionWinsOverStaleSpotifyPausedTrack() {
        val local = PlayerStatus(state = "paused", queue = listOf(TrackDto(title = "Local")), index = 0)
        assertEquals(PlaybackSource.LOCAL, PlaybackSourceSelector.active(local, spotifyPaused))
    }

    @Test fun activeSpotifyWinsWhenLocalIsStopped() {
        val local = PlayerStatus(state = "stopped", queue = listOf(TrackDto(title = "Old local")), index = 0)
        val spotify = SpotifyStatus(playing = true, track = SpotifyTrack(name = "Spotify"))
        assertEquals(PlaybackSource.SPOTIFY, PlaybackSourceSelector.active(local, spotify))
    }
}
