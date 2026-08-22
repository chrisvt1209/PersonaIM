package dev.compose.messenger.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class PreferencesManager(private val context: Context) {
    private val tokenKey = stringPreferencesKey("auth_token")
    private val seasonKey = stringPreferencesKey("current_season")

    val authToken: Flow<String?> = context.dataStore.data.map { it[tokenKey] }
    val currentSeason: Flow<String?> = context.dataStore.data.map { it[seasonKey] }

    suspend fun saveAuthToken(token: String) {
        context.dataStore.edit { it[tokenKey] = token }
    }

    suspend fun saveSeason(season: String) {
        context.dataStore.edit { it[seasonKey] = season }
    }

    suspend fun clear() {
        context.dataStore.edit { it.clear() }
    }
}
