package com.example.bledinamo.presentation

import android.annotation.SuppressLint
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.bledinamo.presentation.bottomNav.DinamoBottomNavigation
import com.example.bledinamo.presentation.profiles.CreateProfileForm
import com.example.bledinamo.presentation.profiles.ProfileList
import com.example.bledinamo.presentation.profiles.ProfileScreen

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun Navigation(
    onBluetoothStateChanged:() -> Unit
) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            DinamoBottomNavigation(navController)
        },

    ) {
        NavHost(navController = navController, startDestination = BottomNavScreen.Home.route) {
            composable(BottomNavScreen.Home.route) {
                StartScreen(navController = navController)
            }
            composable(Screen.GripGraph.route) {
                GripGraph(
                    onBluetoothStateChanged
                )
            }
            composable(BottomNavScreen.Games.route) {
                GamesScreen(
                    navController = navController
                )
            }
            composable(BottomNavScreen.Profiles.route) {
                ProfileList(
                    navController = navController
                )
            }
            composable(
                Screen.ProfileScreen.route,
                arguments = listOf(navArgument("profileName") { type = NavType.StringType })
            ){ backStackEntry ->
                ProfileScreen(navController, backStackEntry.arguments?.getString("profileName"))
            }
            composable(
                Screen.CreateProfileForm.route
            ){ CreateProfileForm(navController = navController)
            }

        }
    }
}

sealed class Screen(val route:String){
    object GripGraph:Screen("grip_graph")

    object ProfileScreen:Screen("profiles_screen/{profileName}")
    object CreateProfileForm:Screen(route = "profiles_screen/profile_form")
}
sealed class BottomNavScreen(var title:String, val icon: ImageVector, var route:String){
    object Home : BottomNavScreen("Home", Icons.Filled.Home,"start_screen")
    object Games: BottomNavScreen("Juegos", Icons.Filled.PlayArrow,"games_screen")
    object Profiles: BottomNavScreen("Perfiles", Icons.Filled.Person,"profiles_screen")

}