package com.example.absentapp.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.absentapp.R
import com.example.absentapp.ui.theme.LocalAppColors
import kotlinx.coroutines.launch

data class BottomNavigationItem(
    val route: String,
    val title: String,
    val selectedIcon: Painter,
    val unselectedIcon: Painter
)


/**
 * Bottom Navigation Bar yang muncul di halaman utama.
 * Menyediakan akses cepat ke halaman "Absen" dan "Profile".
 */
@Composable
fun BottomNavigationBar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val items = listOf(
        BottomNavigationItem("homePage", "Homepage", painterResource(R.drawable.ic_home_filled), painterResource(R.drawable.ic_home_unfilled)),
        BottomNavigationItem("riwayatPage", "History", painterResource(R.drawable.ic_history_filled), painterResource(R.drawable.ic_history_unfilled)),
        BottomNavigationItem("settingPage", "Setting", painterResource(R.drawable.ic_setting_filled), painterResource(R.drawable.ic_setting_unfilled)),
    )
    val appColors = LocalAppColors.current


    Box(modifier = Modifier
        .offset(y = 32.dp)
        .background(appColors.primaryBackground))
    {
        HorizontalDivider(thickness = 1.dp, color = appColors.primaryBackground)

        NavigationBar(tonalElevation = 0.dp, containerColor = appColors.primaryBackground) {
            items.forEach { item ->
                NavigationBarItem(
                    selected = currentRoute == item.route,
                    onClick = {
                        if (currentRoute != item.route) {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    },
                    icon = {
                        Icon(
                            painter = if (currentRoute == item.route) item.selectedIcon else item.unselectedIcon,
                            contentDescription = item.title,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = { Text(item.title) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = appColors.navbarSelected,
                        unselectedIconColor = appColors.navbarUnselected,
                        selectedTextColor = appColors.navbarSelected,
                        unselectedTextColor = appColors.navbarUnselected,
                        indicatorColor = Color.Transparent
                    ),
                    interactionSource = remember { MutableInteractionSource() },
                    modifier = Modifier.padding(0.dp)
                )
            }
        }
    }
}
