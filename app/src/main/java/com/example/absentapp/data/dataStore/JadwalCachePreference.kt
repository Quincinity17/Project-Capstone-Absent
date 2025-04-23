package com.example.absentapp.data.dataStore

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.absentapp.data.dataStore.helper.jadwalDataStore
import com.example.absentapp.data.model.Jadwal
import com.example.absentapp.utils.toIndonesian
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.util.Locale

/**
 * Kelas untuk menyimpan dan mengambil data jadwal secara lokal menggunakan DataStore.
 */
class JadwalCachePreference(private val context: Context) {


    suspend fun saveAllWeeklySchedules(jadwalList: List<Jadwal>) {
        context.jadwalDataStore.edit { prefs ->
            jadwalList.forEach { jadwal ->
                val hari = jadwal.hari.lowercase()
                val masukKey = stringPreferencesKey("jam_masuk_$hari")
                val keluarKey = stringPreferencesKey("jam_keluar_$hari")
                prefs[masukKey] = jadwal.jamMasuk
                prefs[keluarKey] = jadwal.jamKeluar

                Log.d("NASIGORENG", "Simpan: hari=${jadwal.hari}, masuk=${jadwal.jamMasuk}, keluar=${jadwal.jamKeluar}")

            }
        }
    }

    suspend fun getSchedulesByDays(hari: String): Jadwal? {
        val masukKey = stringPreferencesKey("jam_masuk_${hari.lowercase()}")
        val keluarKey = stringPreferencesKey("jam_keluar_${hari.lowercase()}")

        val prefs = context.jadwalDataStore.data.first()
        val masuk = prefs[masukKey]
        val keluar = prefs[keluarKey]

        return if (masuk != null && keluar != null) {
            Jadwal(hari, masuk, keluar)
        } else null
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getSchedulesToday(): Jadwal? {
        val hari = LocalDate.now().dayOfWeek.toIndonesian()
        return getSchedulesByDays(hari)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getSchedulesTommorow(): Jadwal? {
        val hari = LocalDate.now().plusDays(1).dayOfWeek.toIndonesian()
        return getSchedulesByDays(hari)
    }





    /**
     * Mengambil jam masuk berdasarkan hari saat ini.
     * Key-nya dibentuk dengan format "jam_masuk_{hari}" (contoh: jam_masuk_senin).
     */
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getJamMasukForToday(): String {
        val jadwal = getSchedulesToday()
        val jamMasuk = jadwal?.jamMasuk ?: "null"
        Log.d("PUTUAYU", "Hari: ${jadwal?.hari} | Jam Masuk: $jamMasuk")
        return jamMasuk
    }

    /**
     * Menghapus semua data jadwal dari DataStore.
     */
    suspend fun clearJadwal() {
        context.jadwalDataStore.edit { it.clear() }
    }


}
