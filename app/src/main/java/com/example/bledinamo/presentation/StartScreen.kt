package com.example.bledinamo.presentation

import android.annotation.SuppressLint
import android.graphics.drawable.Icon
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.bledinamo.presentation.bottomNav.DinamoBottomNavigation

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun StartScreen(
    navController: NavController,
    viewModel: GripViewModel = hiltViewModel()
){
    viewModel.getCurrentProfile()

    Column(modifier = Modifier
        .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(30.dp)){
        Spacer(modifier = Modifier.size(60.dp))
        if(viewModel.currentProfile == null){
            Text(
                text = "Todavía no has seleccionado ningún perfil.",
                style = MaterialTheme.typography.h5,
                color = MaterialTheme.colors.onSurface,
            )
        }else{
            Text(
                text = "¡Hola! Perfil Actual:",
                style = MaterialTheme.typography.h5,
                color = MaterialTheme.colors.onSurface,
            )
            Column(
                modifier = Modifier
                    .wrapContentSize()
                    .background(MaterialTheme.colors.surface)
                    .clickable{
                        navController.navigate("profiles_screen/${viewModel.currentProfile!!.profile.name}")
                    }
                    .border(
                        BorderStroke(
                            2.dp, MaterialTheme.colors.secondary
                        ),
                        RoundedCornerShape(10.dp)
                    ),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    modifier = Modifier.padding(10.dp),
                    text = viewModel.currentProfile!!.profile.name,
                    style = MaterialTheme.typography.h5,
                    fontWeight = FontWeight(800),
                    color = MaterialTheme.colors.onSurface,
                )

            }



            MenuButton(navController = navController, destinationRoute = Screen.GloboGame.route, text = "Minijuegos",Icons.Default.PlayArrow)
            MenuButton(navController = navController, destinationRoute = Screen.GripGraph.route, text = "Medir agarre",Icons.Default.Add)
            MenuButton(navController = navController, destinationRoute = BottomNavScreen.Profiles.route, text = "Perfiles",Icons.Default.Person)
        }
    }
}

@Composable
fun MenuButton(navController: NavController,destinationRoute: String, text : String, icon: ImageVector){
    Box(
        modifier = Modifier
            .size(width = 250.dp, height = 100.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colors.primary)
            .clickable {
                //Navigate
                navController.navigate(destinationRoute)
            },
        contentAlignment = Alignment.Center
    ){
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                icon, contentDescription = Icons.Default.Person.name,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colors.onPrimary,

            )
            Text(
                text = text,
                fontSize = 25.sp,
                style = MaterialTheme.typography.h5,
                fontWeight = FontWeight(600),
                color = MaterialTheme.colors.onPrimary
            )
        }

    }
}