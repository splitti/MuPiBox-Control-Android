package de.mupibox.control.data.repository

import de.mupibox.control.data.api.ApiError
import de.mupibox.control.data.api.BluetoothResponse
import de.mupibox.control.data.api.HealthResponse
import de.mupibox.control.data.api.InfoResponse
import de.mupibox.control.data.api.MuPiBoxApi
import de.mupibox.control.data.api.PlayerStatus
import de.mupibox.control.data.api.SpotifyStatus
import de.mupibox.control.data.api.SystemStatus
import de.mupibox.control.data.local.BoxPersistence
import de.mupibox.control.model.BoxEndpoint
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * [BoxRepository.add] serves both add and edit (see its own doc comment): these tests exercise
 * the "edit" side explicitly - preserving an existing id must update in place, never duplicate -
 * alongside the validation/health-probe guarantees shared by both.
 */
class BoxRepositoryTest {
    @Test fun addSavesNormalizedNewBox() = runTest {
        val store = FakeBoxPersistence()
        val repo = BoxRepository(store, FakeApi())

        repo.add(BoxEndpoint(name = "Kinderzimmer", host = "192.168.2.114", port = 8090))

        assertEquals(1, store.state.value.size)
        assertEquals("Kinderzimmer", store.state.value.single().name)
    }

    @Test fun editingNameHostAndPortUpdatesInPlace() = runTest {
        val store = FakeBoxPersistence()
        val repo = BoxRepository(store, FakeApi())
        val original = BoxEndpoint(id = "fixed-id", name = "Alt", host = "192.168.2.114", port = 8090)
        store.state.value = listOf(original)

        repo.add(original.copy(name = "Neu", host = "192.168.2.200", port = 9000))

        assertEquals("Editing must not create a second entry", 1, store.state.value.size)
        val updated = store.state.value.single()
        assertEquals("fixed-id", updated.id)
        assertEquals("Neu", updated.name)
        assertEquals("192.168.2.200", updated.host)
        assertEquals(9000, updated.port)
    }

    @Test fun editingOnlyThePortKeepsNameAndHost() = runTest {
        val store = FakeBoxPersistence()
        val repo = BoxRepository(store, FakeApi())
        val original = BoxEndpoint(id = "fixed-id", name = "Kinderzimmer", host = "192.168.2.114", port = 8090)
        store.state.value = listOf(original)

        repo.add(original.copy(port = 9090))

        val updated = store.state.value.single()
        assertEquals("Kinderzimmer", updated.name)
        assertEquals("192.168.2.114", updated.host)
        assertEquals(9090, updated.port)
    }

    @Test fun secondSavedBoxIsUnaffectedByEditingTheFirst() = runTest {
        val store = FakeBoxPersistence()
        val repo = BoxRepository(store, FakeApi())
        val first = BoxEndpoint(id = "a", name = "Kinderzimmer", host = "192.168.2.114", port = 8090)
        val second = BoxEndpoint(id = "b", name = "Kueche", host = "192.168.2.115", port = 8090)
        store.state.value = listOf(first, second)

        repo.add(first.copy(name = "Umbenannt"))

        assertEquals(2, store.state.value.size)
        assertEquals("Kueche", store.state.value.first { it.id == "b" }.name)
    }

    @Test fun rejectsPublicHostBeforeSavingOrProbing() = runTest {
        val store = FakeBoxPersistence()
        val api = FakeApi()
        val repo = BoxRepository(store, api)

        val result = runCatching { repo.add(BoxEndpoint(name = "Fremd", host = "8.8.8.8", port = 8090)) }

        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
        assertTrue(store.state.value.isEmpty())
        assertEquals(0, api.healthCalls)
    }

    @Test fun editRejectsChangingToAPublicHost() = runTest {
        val store = FakeBoxPersistence()
        val repo = BoxRepository(store, FakeApi())
        val original = BoxEndpoint(id = "fixed-id", name = "Kinderzimmer", host = "192.168.2.114", port = 8090)
        store.state.value = listOf(original)

        val result = runCatching { repo.add(original.copy(host = "8.8.8.8")) }

        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
        assertEquals("A rejected edit must leave the saved box untouched", original, store.state.value.single())
    }

    @Test fun rejectsInvalidPort() = runTest {
        val store = FakeBoxPersistence()
        val repo = BoxRepository(store, FakeApi())

        val result = runCatching { repo.add(BoxEndpoint(name = "Kinderzimmer", host = "192.168.2.114", port = 0)) }

        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
        assertTrue(store.state.value.isEmpty())
    }

    @Test fun doesNotSaveWhenHealthProbeFails() = runTest {
        val store = FakeBoxPersistence()
        val repo = BoxRepository(store, FakeApi(healthThrows = true))

        val result = runCatching { repo.add(BoxEndpoint(name = "Kinderzimmer", host = "192.168.2.114", port = 8090)) }

        assertTrue(result.exceptionOrNull() is ApiError)
        assertTrue(store.state.value.isEmpty())
    }

    @Test fun doesNotSaveWhenHealthStatusIsNotOk() = runTest {
        val store = FakeBoxPersistence()
        val repo = BoxRepository(store, FakeApi(healthStatus = "degraded"))

        val result = runCatching { repo.add(BoxEndpoint(name = "Kinderzimmer", host = "192.168.2.114", port = 8090)) }

        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
        assertTrue(store.state.value.isEmpty())
    }

    internal class FakeBoxPersistence : BoxPersistence {
        val state = MutableStateFlow<List<BoxEndpoint>>(emptyList())
        override val boxes: Flow<List<BoxEndpoint>> = state

        override suspend fun upsert(box: BoxEndpoint) {
            state.value = state.value.filterNot { it.id == box.id } + box
        }

        override suspend fun remove(id: String) {
            state.value = state.value.filterNot { it.id == id }
        }
    }

    internal class FakeApi(
        private val healthStatus: String = "ok",
        private val healthThrows: Boolean = false,
    ) : MuPiBoxApi {
        var healthCalls = 0

        override suspend fun health(box: BoxEndpoint): HealthResponse {
            healthCalls++
            if (healthThrows) throw ApiError(503, "unreachable")
            return HealthResponse(status = healthStatus)
        }
        override suspend fun playerStatus(box: BoxEndpoint) = PlayerStatus()
        override suspend fun systemStatus(box: BoxEndpoint) = SystemStatus()
        override suspend fun info(box: BoxEndpoint) = InfoResponse()
        override suspend fun spotifyStatus(box: BoxEndpoint) = SpotifyStatus()
        override suspend fun bluetoothStatus(box: BoxEndpoint) = BluetoothResponse()
        override suspend fun playerCommand(box: BoxEndpoint, action: String, value: Number?) = Unit
        override suspend fun spotifyCommand(box: BoxEndpoint, action: String, value: Number?) = SpotifyStatus()
        override suspend fun speak(box: BoxEndpoint, text: String) = Unit
    }
}
