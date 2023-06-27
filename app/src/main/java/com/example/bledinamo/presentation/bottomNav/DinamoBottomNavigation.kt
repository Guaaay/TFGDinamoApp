package com.example.bledinamo.presentation.bottomNav

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.bledinamo.presentation.BottomNavScreen

@Composable
fun DinamoBottomNavigation(navController: NavController) {
    val items = listOf(
        BottomNavScreen.Home,
        BottomNavScreen.Games,
        BottomNavScreen.Profiles,
    )
    BottomNavigation(
        backgroundColor = MaterialTheme.colors.primary,
        contentColor = MaterialTheme.colors.secondary
    ) {
        val navBackStackEntry = navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry.value?.destination?.route
        Log.d("NAVBAR", currentRoute.toString())
        items.forEach { item ->
            if (currentRoute != null) {
                BottomNavigationItem(
                    icon = { Icon(item.icon,item.icon.name)} ,
                    label = { Text(text = item.title,
                        fontSize = 9.sp) },
                    selectedContentColor = MaterialTheme.colors.onPrimary,
                    unselectedContentColor = MaterialTheme.colors.onPrimary.copy(0.6f),
                    alwaysShowLabel = true,
                    selected = currentRoute.contains(item.route),
                    onClick = {
                        navController.navigate(item.route) {

                            navController.graph.startDestinationRoute?.let { screen_route ->
                                popUpTo(screen_route) {
                                    saveState = true
                                }
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    }
}
