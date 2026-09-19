package de.mupibox.control.data.api

import okhttp3.Dns
import java.net.Inet6Address
import java.net.InetAddress
import java.net.UnknownHostException

/**
 * Defense in depth for the intentionally cleartext LAN client.
 *
 * LocalEndpointValidator constrains the textual host entered by the user. This DNS layer also
 * constrains the resolved addresses, so a local-looking hostname cannot silently resolve to a
 * public Internet address (for example through a search domain or DNS rebinding).
 */
object LanOnlyDns : Dns {
    override fun lookup(hostname: String): List<InetAddress> {
        val resolved = Dns.SYSTEM.lookup(hostname)
        val local = resolved.filter(::isLanAddress)
        if (local.isEmpty()) {
            throw UnknownHostException("$hostname resolves outside the local network")
        }
        return local
    }

    internal fun isLanAddress(address: InetAddress): Boolean {
        if (address.isLoopbackAddress || address.isLinkLocalAddress || address.isSiteLocalAddress) {
            return true
        }
        if (address is Inet6Address) {
            val first = address.address.firstOrNull()?.toInt()?.and(0xff) ?: return false
            // fc00::/7 Unique Local Address space.
            if (first == 0xfc || first == 0xfd) return true
        }
        return false
    }
}
