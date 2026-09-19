package de.mupibox.control.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import de.mupibox.control.model.BoxEndpoint
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.boxDataStore by preferencesDataStore(name = "boxes")

/**
 * Persistence boundary for saved boxes, so [de.mupibox.control.data.repository.BoxRepository]
 * (and its tests) don't need a real Android [Context]/DataStore - a JVM test can use a small fake
 * implementing this interface instead.
 */
interface BoxPersistence {
    val boxes: Flow<List<BoxEndpoint>>
    suspend fun upsert(box: BoxEndpoint)
    suspend fun remove(id: String)
}

class BoxStore(
    private val context: Context,
    private val gson: Gson = Gson(),
) : BoxPersistence {
    private val boxesKey = stringPreferencesKey("saved_boxes_json")
    private val listType = object : TypeToken<List<BoxEndpoint>>() {}.type

    override val boxes: Flow<List<BoxEndpoint>> = context.boxDataStore.data.map { prefs ->
        prefs[boxesKey]?.let { json ->
            runCatching { gson.fromJson<List<BoxEndpoint>>(json, listType) }.getOrDefault(emptyList())
        } ?: emptyList()
    }

    override suspend fun upsert(box: BoxEndpoint) {
        context.boxDataStore.edit { prefs ->
            val current = prefs[boxesKey]?.let { json ->
                runCatching { gson.fromJson<List<BoxEndpoint>>(json, listType) }.getOrDefault(emptyList())
            } ?: emptyList()
            val next = current.filterNot { it.id == box.id } + box
            prefs[boxesKey] = gson.toJson(next)
        }
    }

    override suspend fun remove(id: String) {
        context.boxDataStore.edit { prefs ->
            val current = prefs[boxesKey]?.let { json ->
                runCatching { gson.fromJson<List<BoxEndpoint>>(json, listType) }.getOrDefault(emptyList())
            } ?: emptyList()
            prefs[boxesKey] = gson.toJson(current.filterNot { it.id == id })
        }
    }
}
