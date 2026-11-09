package br.com.cacapalavras.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import br.com.cacapalavras.domain.PlayerProgress
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import java.io.IOException

private val Context.progressStore: DataStore<Preferences> by preferencesDataStore(name = "progress")

/** Persistência local do progresso (FR-006, Constituição IV). */
class ProgressRepository(context: Context) {
    private val store = context.applicationContext.progressStore
    private val key = stringPreferencesKey("progress_json")
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    val progress: Flow<PlayerProgress> = store.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { decode(it) }

    private fun decode(prefs: Preferences): PlayerProgress =
        prefs[key]?.let { runCatching { json.decodeFromString<PlayerProgress>(it) }.getOrNull() } ?: PlayerProgress()

    /** Aplica [transform] de forma atômica e devolve o novo estado. */
    suspend fun update(transform: (PlayerProgress) -> PlayerProgress): PlayerProgress {
        var out = PlayerProgress()
        store.edit { prefs ->
            out = transform(decode(prefs))
            prefs[key] = json.encodeToString(PlayerProgress.serializer(), out)
        }
        return out
    }
}
