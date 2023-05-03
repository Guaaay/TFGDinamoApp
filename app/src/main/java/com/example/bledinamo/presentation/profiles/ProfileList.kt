package com.example.bledinamo.presentation.profiles

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun ProfileList(
    navController: NavController,
    viewModel: ProfilesListViewModel = hiltViewModel()
) {
    Scaffold(topBar = { TopAppBar(
        title = { Text(text = "Perfiles") },
        contentColor = MaterialTheme.colors.onPrimary,
        backgroundColor = MaterialTheme.colors.primary,
        actions = {
            IconButton(onClick = {
                navController.navigate("profiles_screen/profile_form")
            }) {
                Icon(Icons.Default.Add, "Abrir un formulario para añadir un nuevo perfil")
            }
        }


        ) }){
        viewModel.getProfiles()
        if(!viewModel.loadingProfiles){
            val profiles = viewModel.allProfiles
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(4.dp),

                ) {
                items(profiles!!){ profile ->
                    profileMenuItem(profileName = profile.profile.name,navController)
                }
            }

        }
        else{
            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(5.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
                Text(
                    text = "Cargando perfiles"
                )
            }

        }
    }


}


@Composable
fun profileMenuItem(profileName : String, navController: NavController){

    Card(
        modifier = Modifier
            .padding(10.dp)
            .fillMaxWidth()
            .wrapContentHeight()
            .clickable {
                navController.navigate("profiles_screen/$profileName")
            },
        shape = MaterialTheme.shapes.medium,
        elevation = 5.dp,
        backgroundColor = MaterialTheme.colors.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
            ) {
            Icon(Icons.Default.Person, contentDescription = Icons.Default.Person.name,
                modifier = Modifier,)
            Column(Modifier.padding(8.dp)) {
                Text(
                    text = profileName,
                    style = MaterialTheme.typography.h4,
                    color = MaterialTheme.colors.onSurface,
                )

            }
        }

    }
}