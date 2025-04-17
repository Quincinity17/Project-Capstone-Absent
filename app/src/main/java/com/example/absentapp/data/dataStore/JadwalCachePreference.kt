package com.example.absentapp.data.dataStore

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.absentapp.data.dataStore.helper.jadwalDataStore
import com.example.absentapp.data.model.Jadwal
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDate

/**
 * Kelas untuk menyimpan dan mengambil data jadwal secara lokal menggunakan DataStore.
 */
class JadwalCachePreference(private val context: Context) {

    companion object {
        private val HARI_KEY = stringPreferencesKey("jadwal_hari")
        private val JAM_MASUK_KEY = stringPreferencesKey("jadwal_jam_masuk")
        private val JAM_KELUAR_KEY = stringPreferencesKey("jadwal_jam_keluar")
    }

    /**
     * Flow untuk mendapatkan jadwal yang telah disimpan sebelumnya.
     * Mengembalikan null jika salah satu data (hari, jam masuk, atau keluar) tidak ditemukan.
     */
    val cachedJadwal: Flow<Jadwal?> = context.jadwalDataStore.data.map { preferences ->
        val hari = preferences[HARI_KEY]
        val masuk = preferences[JAM_MASUK_KEY]
        val keluar = preferences[JAM_KELUAR_KEY]
        if (hari != null && masuk != null && keluar != null) {
            Jadwal(hari, masuk, keluar)
        } else null
    }

    /**
     * Menyimpan data jadwal ke DataStore.
     * @param jadwal Objek Jadwal yang berisi hari, jam masuk, dan jam keluar.
     */
    suspend fun saveJadwal(jadwal: Jadwal) {
        context.jadwalDataStore.edit { preferences ->
            preferences[HARI_KEY] = jadwal.hari
            preferences[JAM_MASUK_KEY] = jadwal.jamMasuk
            preferences[JAM_KELUAR_KEY] = jadwal.jamKeluar
        }
    }

    /**
     * Mengambil jam masuk berdasarkan hari saat ini.
     * Key-nya dibentuk dengan format "jam_masuk_{hari}" (contoh: jam_masuk_senin).
     */
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getJamMasukForToday(): String {
        val hari = LocalDate.now().dayOfWeek.name.lowercase()
        val key = stringPreferencesKey("jam_masuk_$hari")
        val prefs = context.jadwalDataStore.data.first()
        return prefs[key] ?: "null"
    }

    /**
     * Menghapus semua data jadwal dari DataStore.
     */
    suspend fun clearJadwal() {
        context.jadwalDataStore.edit { it.clear() }
    }
}
