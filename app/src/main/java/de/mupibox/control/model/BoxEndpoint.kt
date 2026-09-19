package de.mupibox.control.model

import java.util.UUID

data class BoxEndpoint(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val host: String,
    val port: Int = 8090,
    val https: Boolean = false,
) {
    val baseUrl: String
        get() {
            val renderedHost = if (host.contains(':') && !host.startsWith("[")) "[$host]" else host
            return "${if (https) "https" else "http"}://$renderedHost:$port"
        }
}
