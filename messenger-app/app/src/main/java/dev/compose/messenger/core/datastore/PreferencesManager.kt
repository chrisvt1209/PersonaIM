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
    private val currentUserIdKey = stringPreferencesKey("current_user_id")
    private val seasonKey = stringPreferencesKey("current_season")
    private val backgroundColorKey = stringPreferencesKey("current_background_color")

    val authToken: Flow<String?> = context.dataStore.data.map { it[tokenKey] }
    val currentUserId: Flow<Long?> = context.dataStore.data.map { it[currentUserIdKey]?.toLongOrNull() }
    val currentSeason: Flow<String?> = context.dataStore.data.map { it[seasonKey] }
    val currentBackgroundColor: Flow<String?> = context.dataStore.data.map { it[backgroundColorKey] }

    suspend fun saveAuthToken(token: String) {
        context.dataStore.edit { it[tokenKey] = token }
    }

    suspend fun saveCurrentUserId(userId: Long) {
        context.dataStore.edit { it[currentUserIdKey] = userId.toString() }
    }

    suspend fun saveSeason(season: String) {
        context.dataStore.edit { it[seasonKey] = season }
    }

    suspend fun saveBackgroundColor(backgroundColor: String) {
        context.dataStore.edit { it[backgroundColorKey] = backgroundColor }
    }

    suspend fun clear() {
        context.dataStore.edit { it.clear() }
    }
}
