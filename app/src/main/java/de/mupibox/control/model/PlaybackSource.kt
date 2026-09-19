package de.mupibox.control.model

import de.mupibox.control.data.api.PlayerStatus
import de.mupibox.control.data.api.SpotifyStatus

enum class PlaybackSource {
    LOCAL,
    SPOTIFY,
}

object PlaybackSourceSelector {
    fun active(player: PlayerStatus, spotify: SpotifyStatus): PlaybackSource {
        val localHasSession = player.queue.isNotEmpty() && (player.isPlaying || player.isPaused)
        val spotifyHasSession = spotify.track != null && (spotify.playing || spotify.paused)
        return if (spotifyHasSession && !localHasSession) PlaybackSource.SPOTIFY else PlaybackSource.LOCAL
    }
}
