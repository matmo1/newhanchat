package com.newhanchat.v1.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

// Initialize DataStore
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_prefs")

@Singleton
class AuthPreferences @Inject constructor(@ApplicationContext private val context: Context) {

    // Define keys for the cache
    private val TOKEN_KEY = stringPreferencesKey("jwt_token")
    private val USER_ID_KEY = stringPreferencesKey("user_id")

    // Expose cached data as reactive Flows
    val authToken: Flow<String?> = context.dataStore.data.map { prefs -> prefs[TOKEN_KEY] }
    val userId: Flow<String?> = context.dataStore.data.map { prefs -> prefs[USER_ID_KEY] }

    // Inside your AuthPreferences.kt or AuthRepository.kt
    val userIdFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USER_ID_KEY] // Returns the saved ID, or null if logged out
    }

    // Save to Cache
    suspend fun saveCredentials(token: String, userId: String) {
        context.dataStore.edit { prefs ->
            prefs[TOKEN_KEY] = token
            prefs[USER_ID_KEY] = userId
        }
    }

    // Clear Cache (Logout)
    suspend fun clearCredentials() {
        context.dataStore.edit { prefs -> prefs.clear() }
    }
}