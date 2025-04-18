package com.example.absentapp.ui.screens.homepage

import android.app.Activity
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
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
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomePage(
    authViewModel: AuthViewModel,
    locationViewModel: LocationViewModel,
    navController: NavController
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val jadwalPrefs = remember { JadwalCachePreference(context) }
    val coroutineScope = rememberCoroutineScope()


    val absenTime by authViewModel.absenTime.collectAsState()
    val currentEmail = authViewModel.getCurrentUserEmail()
    val distance by locationViewModel.currentDistance.collectAsState()
    val isLoadingLokasi by locationViewModel.isFetchingLocation.collectAsState()
    val isUpdating by authViewModel.isUpdating.collectAsState()


    val todaySchedule = remember { mutableStateOf<Jadwal?>(null) }
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




    // Fetch jadwal dari Firestore
    LaunchedEffect(Unit) {
        authViewModel.getTodaySchedule { fetched ->
            fetched?.let {
                todaySchedule.value = it
                coroutineScope.launch {
                    jadwalPrefs.saveJadwal(it)
                }
            }
        }
    }

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

    val distanceLimit by locationViewModel.distanceLimit.collectAsState()

    val filteredAbsen = absenTime
        .filter { it.name == currentEmail }
        .filter {
            val time = it.timestamp?.toDate()
            time != null && time.after(startOfDay.time) && time.before(endOfDay.time)
        }
        .sortedBy { it.timestamp?.toDate() }

    val hasCheckout = remember(filteredAbsen, todaySchedule.value) {
        val thresholdPulang = todaySchedule.value?.jamMasuk?.let {
            runCatching {
                LocalTime.parse(it, DateTimeFormatter.ofPattern("HH:mm"))
                    .plusHours(3)
            }.getOrNull()
        }

        filteredAbsen.any { absen ->
            val absenTime = absen.timestamp?.toDate()?.toInstant()
                ?.atZone(java.time.ZoneId.systemDefault())?.toLocalTime()

            absenTime != null && thresholdPulang != null && absenTime.isAfter(thresholdPulang)
        }
    }

    val hasCheckedIn = filteredAbsen.isNotEmpty()
    val checkoutTime = todaySchedule.value?.jamKeluar ?: "--:--"
    val nextDayCheckInTime = todaySchedule.value?.jamMasuk ?: "--:--"

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

            AbsenGlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .align(Alignment.BottomCenter),
                currentTime = LocalDateTime.now(),
                enabled = distance <= distanceLimit,
                onClickAbsen = {
                    navController.navigate("camera")
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        AbsenStatusBanner(
            isAbsenHariIni = hasCheckedIn,
            isSedangMengambilLokasi = isLoadingLokasi,
            sudahAbsenPulang = hasCheckout,
            distance = distance,
            jadwalMasuk = todaySchedule.value?.jamMasuk ?: "--:--",
            jadwalPulang = checkoutTime,
            jamMasukBesok = nextDayCheckInTime,
            distanceLimit = distanceLimit,
            isUpdateData = isUpdating
        )

        Spacer(modifier = Modifier.height(16.dp))

        todaySchedule.value?.let {
            val scheduleAvailable = rememberUpdatedState(it)

            LaunchedEffect(scheduleAvailable) {
                focusRequester.requestFocus()
            }

            Text(
                text = "Jadwal Anda hari ini ya :)",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .focusRequester(focusRequester)
                    .clearAndSetSemantics {
                        contentDescription = "Jadwal Anda hari ini"
                    }


            )

            JadwalCard(
                icon = R.drawable.ic_check,
                label = "masuk",
                waktu = it.jamMasuk
            )

            JadwalCard(
                icon = R.drawable.ic_car,
                label = "pulang",
                waktu = it.jamKeluar
            )
        } ?: Text(
            "Jadwal hari ini tidak ditemukan",
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))
    }

    // Load cache
    LaunchedEffect(Unit) {
        jadwalPrefs.cachedJadwal.collect { cached ->
            if (cached != null) todaySchedule.value = cached
        }
    }
}