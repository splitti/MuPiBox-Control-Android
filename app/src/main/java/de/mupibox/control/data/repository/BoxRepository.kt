package de.mupibox.control.data.repository

import de.mupibox.control.data.api.HealthResponse
import de.mupibox.control.data.api.LocalEndpointValidator
import de.mupibox.control.data.api.MuPiBoxApi
import de.mupibox.control.data.local.BoxPersistence
import de.mupibox.control.model.BoxEndpoint
import kotlinx.coroutines.flow.Flow

class BoxRepository(
    private val store: BoxPersistence,
    private val api: MuPiBoxApi,
) {
    val savedBoxes: Flow<List<BoxEndpoint>> = store.boxes

    /**
     * Validates [box], requires a passing health probe, then saves it. Serves both add and
     * update: [BoxPersistence.upsert] replaces any existing entry with a matching id rather than
     * appending, so calling this with an existing box's id (see [BoxesViewModel.update]) edits it
     * in place instead of creating a duplicate.
     */
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
