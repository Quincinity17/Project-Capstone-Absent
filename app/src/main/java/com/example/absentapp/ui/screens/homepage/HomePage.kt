package com.example.absentapp.ui.screens.homepage

import android.app.Activity
import android.os.Build
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.absentapp.R
import com.example.absentapp.auth.AuthViewModel
import com.example.absentapp.data.dataStore.JadwalCachePreference
import com.example.absentapp.data.model.Jadwal
import com.example.absentapp.location.LocationViewModel
import com.example.absentapp.ui.components.ConfirmationBottomSheet
import com.example.absentapp.ui.screens.homepage.components.AbsenGlassCard
import com.example.absentapp.ui.screens.homepage.components.AbsenStatusBanner
import com.example.absentapp.ui.screens.homepage.components.JadwalCard
import com.example.absentapp.ui.theme.LocalAppColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomePage(
    fromBottomBar: Boolean,

    authViewModel: AuthViewModel,
    locationViewModel: LocationViewModel,
    navController: NavController
) {

    val context = LocalContext.current
    val currentEmail = authViewModel.getCurrentUserEmail()

<<<<<<< HEAD

=======
>>>>>>> 6517416 (Finishing Iterasi 1)
    val activity = context as? Activity
    val jadwalPrefs = remember { JadwalCachePreference(context) }
    val coroutineScope = rememberCoroutineScope()

<<<<<<< HEAD

=======
>>>>>>> 6517416 (Finishing Iterasi 1)
    val distance by locationViewModel.currentDistance.collectAsState()
    val isLoadingLokasi by locationViewModel.isFetchingLocation.collectAsState()
    val distanceLimit by locationViewModel.distanceLimit.collectAsState()

    val absenTime by authViewModel.absenTime.collectAsState()
    val isUpdating by authViewModel.isUpdating.collectAsState()

    var jadwalHariIni by remember { mutableStateOf<Jadwal?>(null) }
    var jadwalBesok by remember { mutableStateOf<Jadwal?>(null) }

<<<<<<< HEAD



=======
>>>>>>> 6517416 (Finishing Iterasi 1)

    var showExitDialog by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val appColors = LocalAppColors.current

    val focusRequester = remember { FocusRequester() }

    BackHandler {
        showExitDialog = true
    }
    if (showExitDialog) {
        ConfirmationBottomSheet(
            title = "Keluar Aplikasi",
            description = "Apakah Anda yakin ingin keluar?",
            iconResId = R.drawable.ilt_exit,
            sheetState = sheetState,
            onDismiss = { showExitDialog = false },
            onFirstButton = { activity?.finish() },
            onSecondButton = { showExitDialog = false },
            firstText = "Ya, saya yakin",
            secondText = "Tidak"
        )
    }


// Talkback: Narik focus dari bottombar ke component di homepage
    LaunchedEffect(fromBottomBar) {
        if (fromBottomBar) {
            snapshotFlow { true }.first()
            delay(100)
            focusRequester.requestFocus()
        }
    }

// Fetch jadwal
    LaunchedEffect(Unit) {
        authViewModel.getAllJadwal { fetched ->
            fetched.let {
                coroutineScope.launch {
                    jadwalPrefs.saveAllWeeklySchedules(it)

                    jadwalHariIni = jadwalPrefs.getSchedulesToday()
                    jadwalBesok = jadwalPrefs.getSchedulesTommorow()

        //                    Log.d("SATEPADANG", "Jadwal Hari Ini: Masuk = ${jadwalHariIni?.jamMasuk}, Pulang = ${jadwalHariIni?.jamKeluar}")
        //                    Log.d("SATEPADANG", "Jadwal Besok: Masuk = ${jadwalBesok?.jamMasuk}, Pulang = ${jadwalBesok?.jamKeluar}")
                }
            }
        }
    }



    val jamMasukHariIni = jadwalHariIni?.jamMasuk
    val jamKeluarHariIni = jadwalHariIni?.jamKeluar

<<<<<<< HEAD
    val jamMasukBesok = jadwalBesok?.jamKeluar
=======
    val jamMasukBesok = jadwalBesok?.jamMasuk
>>>>>>> 6517416 (Finishing Iterasi 1)


    val now = Calendar.getInstance()
    val startOfDay = (now.clone() as Calendar).apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val endOfDay = (now.clone() as Calendar).apply {
        set(Calendar.HOUR_OF_DAY, 23)
        set(Calendar.MINUTE, 59)
        set(Calendar.SECOND, 59)
        set(Calendar.MILLISECOND, 999)
    }

    //Ambil list absen hari ini
    val filteredAbsen = absenTime
        .filter { it.name == currentEmail }
        .filter {
            val time = it.timestamp?.toDate()
            time != null && time.after(startOfDay.time) && time.before(endOfDay.time)
        }
        .sortedBy { it.timestamp?.toDate() }

    //Check apakah ada absen masuk dan keluar hari ini
    val hasCheckedIn = filteredAbsen.any { it.type == "masuk" }
    val hasCheckedOut = filteredAbsen.any { it.type == "keluar" }



    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(appColors.primaryBackground)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.bg_gradient),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxHeight(0.8f)
                    .fillMaxWidth()
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 16.dp, top = 32.dp)
                    .focusRequester(focusRequester)
                    .focusable()
                    .clearAndSetSemantics {
                        contentDescription = "Selamat datang di halaman beranda"
                    }
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_profile),
                    contentDescription = "Ikon profil",
                    modifier = Modifier.size(20.dp),
                    tint = Color.Black
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Selamat datang",
<<<<<<< HEAD
                    color = appColors.primaryText,
=======
                    color = Color.Black,
>>>>>>> 6517416 (Finishing Iterasi 1)
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
            }

<<<<<<< HEAD
=======
            Log.d("KACANGREBUS", "distance : $distance ; distanceLimit : $distanceLimit")

            Log.d("KACANGREBUS", "isupdating : $isUpdating ; distance : ${distance<=distanceLimit}")

>>>>>>> 6517416 (Finishing Iterasi 1)

            AbsenGlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .align(Alignment.BottomCenter)
                    .semantics { heading() }
                ,
                currentTime = LocalDateTime.now(),
                enabled = distance <= distanceLimit && !isUpdating,
                onClickAbsen = {
                    navController.navigate("camera")
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        AbsenStatusBanner(
            isAbsenHariIni = hasCheckedIn,
            isSedangMengambilLokasi = isLoadingLokasi,
            sudahAbsenPulang = hasCheckedOut,
            distance = distance,
            jadwalMasuk = jamMasukHariIni ?: "--:--",
            jadwalPulang = jamKeluarHariIni ?: "--:--",
            jamMasukBesok = jamMasukBesok ?: "--:--",
            distanceLimit = distanceLimit,
            isUpdateData = isUpdating
        )


        Spacer(modifier = Modifier.height(16.dp))

        jadwalHariIni?.let { jadwal ->
            Column {
                Text(
<<<<<<< HEAD
                    text = "Jadwal Anda hari ini ya :)",
=======
                    text = "Jadwal Anda hari ini",
>>>>>>> 6517416 (Finishing Iterasi 1)
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .focusable()
                        .semantics {
                            contentDescription = "Jadwal Anda hari ini"
                        }
                )

                JadwalCard(
                    icon = R.drawable.ic_check,
                    label = "masuk",
                    waktu = jadwal.jamMasuk
                )

                JadwalCard(
                    icon = R.drawable.ic_car,
                    label = "pulang",
                    waktu = jadwal.jamKeluar
                )
            }
        } ?: Text(
            text = "Jadwal hari ini tidak ditemukan",
            modifier = Modifier.padding(horizontal = 16.dp)
        )


        Spacer(modifier = Modifier.height(16.dp))
    }
}