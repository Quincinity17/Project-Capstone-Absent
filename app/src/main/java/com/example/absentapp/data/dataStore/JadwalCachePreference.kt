package com.example.absentapp.data.dataStore

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.absentapp.data.model.Jadwal
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import com.example.absentapp.data.dataStore.helper.jadwalDataStore

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

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getJamMasukForToday(): String {
        val hari = LocalDate.now().dayOfWeek.name.lowercase()
        val key = stringPreferencesKey("jam_masuk_$hari")
        val prefs = context.jadwalDataStore.data.first()
        return prefs[key] ?: "07:30"
    }


    suspend fun clearJadwal() {
        context.jadwalDataStore.edit { it.clear() }
    }
}
