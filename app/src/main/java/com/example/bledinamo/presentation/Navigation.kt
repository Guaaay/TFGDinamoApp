package com.example.bledinamo.presentation

import android.annotation.SuppressLint
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.bledinamo.presentation.bottomNav.DinamoBottomNavigation

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun Navigation(
    onBluetoothStateChanged:() -> Unit
) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            DinamoBottomNavigation(navController)
        }
    ) {
        NavHost(navController = navController, startDestination = Screen.StartScreen.route) {
            composable(BottomNavScreen.Home.screen_route) {
                StartScreen(navController = navController)
            }
            composable(Screen.GripGraph.route) {
                GripGraph(
                    onBluetoothStateChanged
                )
            }
            composable(BottomNavScreen.Games.screen_route) {
                GamesScreen(
                    navController = navController
                )
            }
            composable(BottomNavScreen.Profiles.screen_route) {
                ProfileList(
                    navController = navController
                )
            }
        }
    }
}

sealed class Screen(val route:String){
    object StartScreen:Screen("start_screen")

    object GripGraph:Screen("grip_graph")
}
sealed class BottomNavScreen(var title:String, val icon: ImageVector, var screen_route:String){
    object Home : BottomNavScreen("Home", Icons.Filled.Home,"start_screen")
    object Games: BottomNavScreen("Juegos", Icons.Filled.PlayArrow,"games_screen")
    object Profiles: BottomNavScreen("Perfiles", Icons.Filled.Person,"profiles_screen")

}