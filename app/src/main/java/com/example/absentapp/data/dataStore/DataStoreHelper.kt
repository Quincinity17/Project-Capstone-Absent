package com.example.absentapp.data.dataStore.helper

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore

val Context.dataStore by preferencesDataStore(name = "settings")
val Context.jadwalDataStore by preferencesDataStore(name = "jadwal_cache")

