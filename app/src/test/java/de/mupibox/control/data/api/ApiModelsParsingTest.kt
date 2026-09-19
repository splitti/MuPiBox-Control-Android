package de.mupibox.control.data.api

import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ApiModelsParsingTest {
    private val gson = Gson()

    @Test fun parsesLocalPlayerFixture() {
        val status = gson.fromJson(fixture("player_status.json"), PlayerStatus::class.java)
        assertEquals("playing", status.state)
        assertEquals("folder-1", status.folderId)
        assertEquals("Titel 02", status.currentTrack?.title)
        assertEquals(25, status.volume)
        assertEquals(50, status.maxVolume)
    }

    @Test fun parsesSystemAndOptionalBattery() {
        val status = gson.fromJson(fixture("system_status.json"), SystemStatus::class.java)
        assertTrue(status.online)
        assertEquals("wlan0", status.wifi.interfaceName)
        assertEquals(72, status.wifi.qualityPercent)
        assertTrue(status.battery.available)
        assertEquals(83, status.battery.percent)

        val missing = gson.fromJson(fixture("system_status_no_battery.json"), SystemStatus::class.java)
        assertFalse(missing.battery.available)
        assertNull(missing.battery.percent)
    }

    @Test fun parsesSpotifyRawVolumeRange() {
        val status = gson.fromJson(fixture("spotify_status.json"), SpotifyStatus::class.java)
        assertTrue(status.playing)
        assertEquals(65535, status.volumeSteps)
        assertEquals("Song", status.track?.name)
        assertEquals(42000L, status.track?.positionMs)
    }

    private fun fixture(name: String): String =
        checkNotNull(javaClass.classLoader?.getResource("fixtures/${name.removePrefix("/")}")) { "Missing fixture $name" }
            .readText()
}
