package de.mupibox.control.data.repository

import de.mupibox.control.data.api.HealthResponse
import de.mupibox.control.data.api.LocalEndpointValidator
import de.mupibox.control.data.api.MuPiBoxApi
import de.mupibox.control.data.local.BoxStore
import de.mupibox.control.model.BoxEndpoint
import kotlinx.coroutines.flow.Flow

class BoxRepository(
    private val store: BoxStore,
    private val api: MuPiBoxApi,
) {
    val savedBoxes: Flow<List<BoxEndpoint>> = store.boxes

    suspend fun add(box: BoxEndpoint): HealthResponse {
        val normalized = LocalEndpointValidator.validate(box).getOrThrow()
        val health = api.health(normalized)
        require(health.status == "ok") { "Host antwortet, ist aber keine erreichbare MuPiBox." }
        store.upsert(normalized)
        return health
    }

    suspend fun remove(id: String) = store.remove(id)
    suspend fun probe(box: BoxEndpoint) = api.health(box)
}
