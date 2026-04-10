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

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_prefs")

@Singleton
class AuthPreferences @Inject constructor(@ApplicationContext private val context: Context) {

    private val TOKEN_KEY = stringPreferencesKey("jwt_token")
    private val USER_ID_KEY = stringPreferencesKey("user_id")

    // ✨ NEW: Key for the custom background image
    private val BACKGROUND_URI_KEY = stringPreferencesKey("background_uri")

    val authToken: Flow<String?> = context.dataStore.data.map { prefs -> prefs[TOKEN_KEY] }
    val userId: Flow<String?> = context.dataStore.data.map { prefs -> prefs[USER_ID_KEY] }

    // ✨ NEW: Flow to read the background URI
    val backgroundUri: Flow<String?> = context.dataStore.data.map { prefs -> prefs[BACKGROUND_URI_KEY] }

    val userIdFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USER_ID_KEY]
    }

    suspend fun saveCredentials(token: String, userId: String) {
        context.dataStore.edit { prefs ->
            prefs[TOKEN_KEY] = token
            prefs[USER_ID_KEY] = userId
        }
    }

    // ✨ NEW: Save the custom background URI
    suspend fun saveBackgroundUri(uri: String) {
        context.dataStore.edit { prefs ->
            prefs[BACKGROUND_URI_KEY] = uri
        }
    }

    suspend fun clearCredentials() {
        context.dataStore.edit { prefs ->
            prefs.remove(TOKEN_KEY)
            prefs.remove(USER_ID_KEY)
            // Note: We deliberately DO NOT remove the background URI here,
            // so the app stays pretty even when logged out!
        }
    }
}