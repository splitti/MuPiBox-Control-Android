package de.mupibox.control.data.api

import de.mupibox.control.model.BoxEndpoint

object LocalEndpointValidator {
    fun validate(endpoint: BoxEndpoint): Result<BoxEndpoint> = runCatching {
        val host = endpoint.host.trim().removePrefix("[").removeSuffix("]")
        require(host.isNotBlank()) { "Host darf nicht leer sein." }
        require(endpoint.port in 1..65535) { "Ungültiger Port." }
        require(isLanHost(host)) {
            "Nur lokale MuPiBox-Ziele sind erlaubt (private IP, .local, .home.arpa oder lokaler Hostname)."
        }
        endpoint.copy(host = host)
    }

    internal fun isLanHost(host: String): Boolean {
        val h = host.lowercase()
        if (h == "localhost" || h.endsWith(".local") || h.endsWith(".home.arpa")) return true
        if (!h.contains('.') && !h.contains(':')) return true // local DNS single-label name
        if (isPrivateIpv4(h)) return true
        if (h.contains(':') && (h == "::1" || h.startsWith("fe80:") || h.startsWith("fc") || h.startsWith("fd"))) return true
        return false
    }

    private fun isPrivateIpv4(host: String): Boolean {
        val parts = host.split('.')
        if (parts.size != 4) return false
        val p = parts.map { it.toIntOrNull() ?: return false }
        if (p.any { it !in 0..255 }) return false
        return when {
            p[0] == 10 -> true
            p[0] == 127 -> true
            p[0] == 169 && p[1] == 254 -> true
            p[0] == 172 && p[1] in 16..31 -> true
            p[0] == 192 && p[1] == 168 -> true
            else -> false
        }
    }
}
