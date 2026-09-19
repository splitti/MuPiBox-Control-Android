package de.mupibox.control.data.discovery

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.net.wifi.WifiManager
import de.mupibox.control.model.BoxEndpoint
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Future-ready discovery for a MuPiBox DNS-SD service.
 *
 * MuPiBox-NG does NOT advertise this service at the time this scaffold was created.
 * Manual host/IP entry therefore remains the reliable MVP path. This class can stay in
 * place until the box starts advertising `_mupibox._tcp.`.
 */
class NsdBoxDiscovery(context: Context) {
    private val appContext = context.applicationContext
    private val nsd = appContext.getSystemService(NsdManager::class.java)
    private val wifi = appContext.getSystemService(WifiManager::class.java)

    fun discover(): Flow<BoxEndpoint> = callbackFlow {
        val lock = wifi.createMulticastLock("mupibox-control-nsd").apply {
            setReferenceCounted(false)
            acquire()
        }

        val listener = object : NsdManager.DiscoveryListener {
            override fun onDiscoveryStarted(serviceType: String) = Unit
            override fun onDiscoveryStopped(serviceType: String) = Unit
            override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) = close()
            override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) = Unit

            override fun onServiceFound(serviceInfo: NsdServiceInfo) {
                if (!serviceInfo.serviceType.startsWith(SERVICE_TYPE.removeSuffix("."))) return
                @Suppress("DEPRECATION")
                nsd.resolveService(serviceInfo, object : NsdManager.ResolveListener {
                    override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) = Unit

                    @Suppress("DEPRECATION")
                    override fun onServiceResolved(resolved: NsdServiceInfo) {
                        val host = resolved.host?.hostAddress ?: return
                        trySend(
                            BoxEndpoint(
                                id = "nsd:${resolved.serviceName}:$host:${resolved.port}",
                                name = resolved.serviceName.ifBlank { "MuPiBox" },
                                host = host,
                                port = resolved.port.takeIf { it > 0 } ?: DEFAULT_PORT,
                            )
                        )
                    }
                })
            }

            override fun onServiceLost(serviceInfo: NsdServiceInfo) = Unit
        }

        nsd.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, listener)
        awaitClose {
            runCatching { nsd.stopServiceDiscovery(listener) }
            if (lock.isHeld) lock.release()
        }
    }

    companion object {
        const val SERVICE_TYPE = "_mupibox._tcp."
        const val DEFAULT_PORT = 8090
    }
}
