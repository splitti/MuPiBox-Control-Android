package de.mupibox.control

import android.content.Context
import de.mupibox.control.data.api.MuPiBoxClient
import de.mupibox.control.data.discovery.NsdBoxDiscovery
import de.mupibox.control.data.local.BoxStore
import de.mupibox.control.data.repository.BoxRepository
import de.mupibox.control.data.repository.ControlRepository

class AppContainer(context: Context) {
    private val api = MuPiBoxClient()
    private val boxStore = BoxStore(context)

    val boxRepository = BoxRepository(boxStore, api)
    val controlRepository = ControlRepository(api)
    val discovery = NsdBoxDiscovery(context)
}
