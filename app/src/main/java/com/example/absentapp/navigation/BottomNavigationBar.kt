package com.example.absentapp.navigation

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.absentapp.R

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

    val bottomNavItems = listOf(
        BottomNavigationItem(
            route = "absen",
            title = "Homepage",
            selectedIcon = painterResource(R.drawable.ic_home_filled),
            unselectedIcon = painterResource(R.drawable.ic_home_unfilled)
        ),
        BottomNavigationItem(
            route = "profile",
            title = "History",
            selectedIcon = painterResource(R.drawable.ic_history_filled),
            unselectedIcon = painterResource(R.drawable.ic_history_unfilled)
        ),
        BottomNavigationItem(
            route = "setting",
            title = "Setting",
            selectedIcon = painterResource(R.drawable.ic_setting_filled),
            unselectedIcon = painterResource(R.drawable.ic_setting_unfilled)
        )
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    androidx.compose.foundation.layout.Box (
        modifier = Modifier
            .offset(y = 32.dp),
    ){
        androidx.compose.material3.Divider(
            thickness = 1.dp,
            color = Color(0xFFE0E0E0),
        )
        NavigationBar(
            tonalElevation = 0.dp,

            containerColor = Color.White
        ) {
            bottomNavItems.forEach { item ->
                val selected = currentRoute == item.route

                NavigationBarItem(
                    selected = selected,
                    onClick = { navController.navigate(item.route) },
                    label = { Text(item.title) },
                    icon = {
                        Icon(
                            painter = if (selected) item.selectedIcon else item.unselectedIcon,
                            contentDescription = null,
                            modifier = Modifier
                                .size(24.dp)
                                .offset(y = 0.dp)
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = Color.Transparent,
                        selectedIconColor = Color(0xFF022D9B),
                        unselectedIconColor = Color.Gray,
                        selectedTextColor = Color(0xFF022D9B),
                        unselectedTextColor = Color.Gray
                    ),
                    interactionSource = remember { MutableInteractionSource() },
                    modifier = Modifier.padding(0.dp)
                )
            }
        }
    }

}
