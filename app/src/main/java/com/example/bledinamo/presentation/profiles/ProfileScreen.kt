package com.example.bledinamo.presentation.profiles

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.bledinamo.persistence.entities.ProfileWithMeasurements
import javax.annotation.WillCloseWhenClosed


@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun ProfileScreen(navController: NavController,
                  profileName : String?,
                  viewModel: ProfilesViewModel = hiltViewModel(),
){
    if(profileName == null){
        Column(verticalArrangement = Arrangement.Center,horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "HA OCURRIDO UN TERRIBLE ERROR")
        }

    }
    else{
        Scaffold(topBar = { TopAppBar(
            title = { Text(text = profileName!!) },
            contentColor = MaterialTheme.colors.onPrimary,
            backgroundColor = MaterialTheme.colors.primary,
            actions = {
                IconButton(onClick = {/* Do Something*/ }) {
                    Icon(Icons.Default.Delete, null)
                }
            }


        ) }) {
            viewModel.getProfile(profileName)
        }
    }


}