package de.mupibox.control.ui.control

import de.mupibox.control.data.api.ApiError
import de.mupibox.control.data.api.BluetoothResponse
import de.mupibox.control.data.api.HealthResponse
import de.mupibox.control.data.api.InfoResponse
import de.mupibox.control.data.api.MuPiBoxApi
import de.mupibox.control.data.api.PlayerStatus
import de.mupibox.control.data.api.SpotifyStatus
import de.mupibox.control.data.api.SystemStatus
import de.mupibox.control.data.repository.ControlRepository
import de.mupibox.control.model.BoxEndpoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Uses [StandardTestDispatcher] (not Unconfined) deliberately: [BoxControlViewModel.startPolling]
 * runs an unbounded `while (true) { ... ; delay(1_000) }` loop, so tests that touch polling use
 * [runCurrent]/[advanceTimeBy] (bounded) and always call `stopPolling()` before returning -
 * calling [advanceUntilIdle] while polling is still active would advance virtual time forever and
 * hang the test, since there is always more scheduled work.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class BoxControlViewModelTest {
    @Before fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @After fun tearDown() {
        Dispatchers.resetMain()
    }

    private val box = BoxEndpoint(name = "Kinderzimmer", host = "192.168.2.114")

    // MARK: Offline

    @Test fun offlineBoxSignalsDisconnectedAndErrorWithoutLosingBoxIdentity() = runTest {
        val api = FakeApi(playerThrows = true)
        val vm = BoxControlViewModel(box, ControlRepository(api))

        vm.startPolling()
        runCurrent()
        vm.stopPolling()

        val state = vm.uiState.value
        assertEquals("Box identity must survive a failed refresh", box, state.box)
        assertEquals(false, state.connected)
        assertNotNull(state.error)
        assertFalse(state.loading)
    }

    @Test fun successfulRefreshMarksConnectedAndClearsError() = runTest {
        val api = FakeApi()
        val vm = BoxControlViewModel(box, ControlRepository(api))

        vm.startPolling()
        runCurrent()
        vm.stopPolling()

        val state = vm.uiState.value
        assertEquals(true, state.connected)
        assertNull(state.error)
    }

    // MARK: Bluetooth

    @Test fun bluetoothIsNeverRequestedAutomaticallyWhilePolling() = runTest {
        val api = FakeApi()
        val vm = BoxControlViewModel(box, ControlRepository(api))

        vm.startPolling()
        advanceTimeBy(6_000) // several player (~1s) and system (~5s) poll ticks
        runCurrent()
        vm.stopPolling()

        assertEquals(0, api.bluetoothCalls)
    }

    @Test fun manualBluetoothRefreshSetsLoadingThenClearsItOnSuccess() = runTest {
        val api = FakeApi(bluetoothDelayMs = 5_000) // the real endpoint scans for ~8s
        val vm = BoxControlViewModel(box, ControlRepository(api))

        vm.refreshBluetooth()
        runCurrent() // runs up to the in-flight scan's delay()
        assertTrue("Loading must be visible while the scan is in flight", vm.uiState.value.actionBusy)

        advanceUntilIdle() // no polling active in this test, safe to drain fully
        assertFalse(vm.uiState.value.actionBusy)
        assertNotNull(vm.uiState.value.bluetoothDevices)
        assertNull(vm.uiState.value.error)
    }

    @Test fun manualBluetoothRefreshClearsLoadingOnFailure() = runTest {
        val api = FakeApi(bluetoothError = ApiError(503, "scan failed"))
        val vm = BoxControlViewModel(box, ControlRepository(api))

        vm.refreshBluetooth()
        runCurrent()

        assertFalse(vm.uiState.value.actionBusy)
        assertNotNull(vm.uiState.value.error)
    }

    @Test fun bluetoothAuthErrorGetsItsOwnReadableState() = runTest {
        val api = FakeApi(bluetoothError = ApiError(401, "unauthorized"))
        val vm = BoxControlViewModel(box, ControlRepository(api))

        vm.refreshBluetooth()
        runCurrent()

        assertEquals("Anmeldung erforderlich.", vm.uiState.value.error)
    }

    @Test fun bluetoothDisabledErrorGetsItsOwnReadableState() = runTest {
        val api = FakeApi(bluetoothError = ApiError(409, "disabled"))
        val vm = BoxControlViewModel(box, ControlRepository(api))

        vm.refreshBluetooth()
        runCurrent()

        assertEquals("Bluetooth ist deaktiviert.", vm.uiState.value.error)
    }

    // MARK: TTS

    @Test fun speakSetsBusyDuringTheRequestAndClearsItOnSuccess() = runTest {
        val api = FakeApi(speakDelayMs = 1_000)
        val vm = BoxControlViewModel(box, ControlRepository(api))

        vm.speak("Hallo")
        runCurrent()
        assertTrue(vm.uiState.value.actionBusy)

        advanceUntilIdle()
        assertFalse(vm.uiState.value.actionBusy)
        assertNull(vm.uiState.value.error)
    }

    @Test fun slowTtsRequestIsNotTreatedAsAFailureBeforeItCompletes() = runTest {
        val api = FakeApi(speakDelayMs = 20_000) // a Piper cache miss can take close to 20s
        val vm = BoxControlViewModel(box, ControlRepository(api))

        vm.speak("Hallo")
        advanceTimeBy(19_000)
        runCurrent()
        assertTrue("Still within the expected slow-TTS window", vm.uiState.value.actionBusy)
        assertNull("Must not fail early just because it's slow", vm.uiState.value.error)

        advanceUntilIdle()
        assertFalse(vm.uiState.value.actionBusy)
        assertNull(vm.uiState.value.error)
    }

    @Test fun speakFailureClearsBusyAndSurfacesAnError() = runTest {
        val api = FakeApi(speakThrows = true)
        val vm = BoxControlViewModel(box, ControlRepository(api))

        vm.speak("Hallo")
        runCurrent()

        assertFalse(vm.uiState.value.actionBusy)
        assertNotNull(vm.uiState.value.error)
    }

    @Test fun speakFailureNeverMirrorsTheSpokenTextIntoTheErrorState() = runTest {
        val api = FakeApi(speakThrows = true)
        val vm = BoxControlViewModel(box, ControlRepository(api))

        vm.speak("Geheimer Text 12345")
        runCurrent()

        assertFalse(vm.uiState.value.error.orEmpty().contains("Geheimer Text"))
    }

    private class FakeApi(
        private val playerThrows: Boolean = false,
        private val bluetoothError: ApiError? = null,
        private val bluetoothDelayMs: Long = 0,
        private val speakThrows: Boolean = false,
        private val speakDelayMs: Long = 0,
    ) : MuPiBoxApi {
        var bluetoothCalls = 0

        override suspend fun health(box: BoxEndpoint) = HealthResponse(status = "ok")

        override suspend fun playerStatus(box: BoxEndpoint): PlayerStatus {
            if (playerThrows) throw ApiError(503, "Box nicht erreichbar")
            return PlayerStatus()
        }

        override suspend fun systemStatus(box: BoxEndpoint) = SystemStatus()
        override suspend fun info(box: BoxEndpoint) = InfoResponse()
        override suspend fun spotifyStatus(box: BoxEndpoint) = SpotifyStatus()

        override suspend fun bluetoothStatus(box: BoxEndpoint): BluetoothResponse {
            bluetoothCalls++
            if (bluetoothDelayMs > 0) delay(bluetoothDelayMs)
            bluetoothError?.let { throw it }
            return BluetoothResponse(devices = emptyList())
        }

        override suspend fun playerCommand(box: BoxEndpoint, action: String, value: Number?) = Unit
        override suspend fun spotifyCommand(box: BoxEndpoint, action: String, value: Number?) = SpotifyStatus()

        override suspend fun speak(box: BoxEndpoint, text: String) {
            if (speakDelayMs > 0) delay(speakDelayMs)
            if (speakThrows) throw ApiError(500, "TTS fehlgeschlagen")
        }
    }
}
