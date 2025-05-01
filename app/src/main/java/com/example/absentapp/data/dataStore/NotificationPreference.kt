package com.example.absentapp.data.dataStore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import com.example.absentapp.data.dataStore.helper.dataStore

/**
 * Kelas ini menangani penyimpanan preferensi notifikasi (ON/OFF)
 * menggunakan Jetpack DataStore Preferences.
 */
class NotificationPreference(private val context: Context) {

    companion object {
        // Key untuk menyimpan status notifikasi ke dalam DataStore
        private val NOTIFICATION_ENABLED_KEY = booleanPreferencesKey("notification_enabled")
    }

    /**
     * Flow untuk memantau status notifikasi secara real-time.
     * Default: true (notifikasi aktif)
     */
    val isNotificationEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[NOTIFICATION_ENABLED_KEY] ?: true
        }

    /**
     * Menyimpan status notifikasi ke DataStore.
     * @param enabled true jika notifikasi diaktifkan, false jika dinonaktifkan.
     */
    suspend fun setNotificationEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[NOTIFICATION_ENABLED_KEY] = enabled
        }
    }
}
