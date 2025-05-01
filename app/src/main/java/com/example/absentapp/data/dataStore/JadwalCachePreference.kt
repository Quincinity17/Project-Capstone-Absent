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
import kotlinx.coroutines.flow.first
import java.time.LocalDate

/**
 * Kelas untuk menyimpan dan mengambil data jadwal secara lokal menggunakan DataStore.
 * Disimpan dalam bentuk key-value seperti: jam_masuk_senin, jam_keluar_senin, dst.
 */
class JadwalCachePreference(private val context: Context) {

    /**
     * Menyimpan seluruh jadwal (jam masuk & keluar) ke DataStore.
     * Key terbentuk berdasarkan nama hari (contoh: jam_masuk_senin).
     */
    suspend fun saveAllWeeklySchedules(jadwalList: List<Jadwal>) {
        context.jadwalDataStore.edit { prefs ->
            jadwalList.forEach { jadwal ->
                val hari = jadwal.hari.lowercase()
                val masukKey = stringPreferencesKey("jam_masuk_$hari")
                val keluarKey = stringPreferencesKey("jam_keluar_$hari")
                prefs[masukKey] = jadwal.jamMasuk
                prefs[keluarKey] = jadwal.jamKeluar

//                Log.d("NASIGORENG", "Simpan: hari=${jadwal.hari}, masuk=${jadwal.jamMasuk}, keluar=${jadwal.jamKeluar}")
            }
        }
    }

    /**
     * Mengambil jadwal (jam masuk & keluar) berdasarkan nama hari.
     * Return null jika salah satu data tidak ditemukan.
     */
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

    /**
     * Mengambil jadwal hari ini berdasarkan nama hari lokal (contoh: senin, selasa).
     * Hanya bisa digunakan di Android versi O ke atas karena pakai LocalDate.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getSchedulesToday(): Jadwal? {
        val hari = LocalDate.now().dayOfWeek.toIndonesian()
        return getSchedulesByDays(hari)
    }

    /**
     * Mengambil jadwal untuk hari esok (besok) dalam format lokal (contoh: selasa).
     */
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getSchedulesTommorow(): Jadwal? {
        val hari = LocalDate.now().plusDays(1).dayOfWeek.toIndonesian()
        return getSchedulesByDays(hari)
    }

    /**
     * Mengambil hanya jam masuk untuk hari ini.
     * Digunakan misalnya untuk memicu reminder atau alarm absensi.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getJamMasukForToday(): String {
        val jadwal = getSchedulesToday()
        val jamMasuk = jadwal?.jamMasuk ?: "null"
//        Log.d("PUTUAYU", "Hari: ${jadwal?.hari} | Jam Masuk: $jamMasuk")
        return jamMasuk
    }

    /**
     * Menghapus semua data jadwal yang tersimpan di DataStore.
     * Biasanya dipanggil saat logout atau refresh data dari server.
     */
    suspend fun clearJadwal() {
        context.jadwalDataStore.edit { it.clear() }
    }
}
