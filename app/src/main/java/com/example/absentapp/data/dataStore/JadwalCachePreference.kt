package com.example.absentapp.data.dataStore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.absentapp.data.model.Jadwal
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.jadwalDataStore by preferencesDataStore(name = "jadwal_cache")

class JadwalCachePreference(private val context: Context) {

    companion object {
        private val HARI_KEY = stringPreferencesKey("jadwal_hari")
        private val JAM_MASUK_KEY = stringPreferencesKey("jadwal_jam_masuk")
        private val JAM_KELUAR_KEY = stringPreferencesKey("jadwal_jam_keluar")
    }

    val cachedJadwal: Flow<Jadwal?> = context.jadwalDataStore.data
        .map { preferences ->
            val hari = preferences[HARI_KEY]
            val masuk = preferences[JAM_MASUK_KEY]
            val keluar = preferences[JAM_KELUAR_KEY]
            if (hari != null && masuk != null && keluar != null) {
                Jadwal(hari, masuk, keluar)
            } else null
        }

    suspend fun saveJadwal(jadwal: Jadwal) {
        context.jadwalDataStore.edit { preferences ->
            preferences[HARI_KEY] = jadwal.hari
            preferences[JAM_MASUK_KEY] = jadwal.jamMasuk
            preferences[JAM_KELUAR_KEY] = jadwal.jamKeluar
        }
    }

    suspend fun clearJadwal() {
        context.jadwalDataStore.edit { it.clear() }
    }
}
