package de.mupibox.control.ui.boxes

import de.mupibox.control.data.repository.BoxRepository
import de.mupibox.control.data.repository.BoxRepositoryTest
import de.mupibox.control.model.BoxEndpoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BoxesViewModelTest {
    @Before fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test fun addSavesNewBoxAndInvokesCallback() = runTest {
        val store = BoxRepositoryTest.FakeBoxPersistence()
        val vm = BoxesViewModel(BoxRepository(store, BoxRepositoryTest.FakeApi()))
        var added = false

        vm.add("Kinderzimmer", "192.168.2.114", 8090) { added = true }

        assertTrue(added)
        assertEquals(1, store.state.value.size)
        assertNull(vm.uiState.value.error)
    }

    @Test fun updateChangesNameHostAndPortInPlace() = runTest {
        val store = BoxRepositoryTest.FakeBoxPersistence()
        val original = BoxEndpoint(id = "fixed-id", name = "Alt", host = "192.168.2.114", port = 8090)
        store.state.value = listOf(original)
        val vm = BoxesViewModel(BoxRepository(store, BoxRepositoryTest.FakeApi()))
        var updated = false

        vm.update("fixed-id", "Neu", "192.168.2.200", 9000) { updated = true }

        assertTrue(updated)
        assertEquals("Editing must not create a second entry", 1, store.state.value.size)
        val saved = store.state.value.single()
        assertEquals("fixed-id", saved.id)
        assertEquals("Neu", saved.name)
        assertEquals("192.168.2.200", saved.host)
        assertEquals(9000, saved.port)
    }

    @Test fun updateWithOnlyPortChangedDoesNotDuplicate() = runTest {
        val store = BoxRepositoryTest.FakeBoxPersistence()
        val original = BoxEndpoint(id = "fixed-id", name = "Kinderzimmer", host = "192.168.2.114", port = 8090)
        store.state.value = listOf(original)
        val vm = BoxesViewModel(BoxRepository(store, BoxRepositoryTest.FakeApi()))

        vm.update("fixed-id", "Kinderzimmer", "192.168.2.114", 9090) {}

        assertEquals(1, store.state.value.size)
        assertEquals(9090, store.state.value.single().port)
    }

    @Test fun updateRejectsPublicHostAndKeepsOriginal() = runTest {
        val store = BoxRepositoryTest.FakeBoxPersistence()
        val original = BoxEndpoint(id = "fixed-id", name = "Kinderzimmer", host = "192.168.2.114", port = 8090)
        store.state.value = listOf(original)
        val vm = BoxesViewModel(BoxRepository(store, BoxRepositoryTest.FakeApi()))
        var updated = false

        vm.update("fixed-id", "Kinderzimmer", "8.8.8.8", 8090) { updated = true }

        assertTrue("Callback must not fire on a rejected update", !updated)
        assertEquals(original, store.state.value.single())
        assertNotNull(vm.uiState.value.error)
    }

    @Test fun updateRejectsInvalidPortAndKeepsOriginal() = runTest {
        val store = BoxRepositoryTest.FakeBoxPersistence()
        val original = BoxEndpoint(id = "fixed-id", name = "Kinderzimmer", host = "192.168.2.114", port = 8090)
        store.state.value = listOf(original)
        val vm = BoxesViewModel(BoxRepository(store, BoxRepositoryTest.FakeApi()))

        vm.update("fixed-id", "Kinderzimmer", "192.168.2.114", 0) {}

        assertEquals(original, store.state.value.single())
        assertNotNull(vm.uiState.value.error)
    }

    @Test fun cancellingAnEditNeverTouchesSavedData() = runTest {
        // Simulates the user opening the edit dialog (pre-filled from the existing box, see
        // BoxesScreen's BoxDialog) and dismissing without pressing "Speichern" - i.e. update() is
        // simply never called. There is nothing to cancel in the ViewModel/repository because the
        // dialog never called through to them; this pins that invariant down explicitly.
        val store = BoxRepositoryTest.FakeBoxPersistence()
        val original = BoxEndpoint(id = "fixed-id", name = "Kinderzimmer", host = "192.168.2.114", port = 8090)
        store.state.value = listOf(original)
        BoxesViewModel(BoxRepository(store, BoxRepositoryTest.FakeApi()))

        assertEquals(listOf(original), store.state.value)
    }
}
