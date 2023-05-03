package com.example.bledinamo.presentation.profiles

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.bledinamo.persistence.entities.ProfileWithMeasurements


@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun ProfileScreen(navController: NavController,
                  profileName : String?,
                  viewModel: ProfileScreenViewModel = hiltViewModel(),
){
    if(profileName == null){
        Column(verticalArrangement = Arrangement.Center,horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "HA OCURRIDO UN TERRIBLE ERROR")
        }

    }
    else{
            if(viewModel.deleted){
                navController.popBackStack("profiles_screen",inclusive = false)
            }
            else{
                Scaffold(topBar = { TopAppBar(
                    title = { Text(text = profileName!!) },
                    contentColor = MaterialTheme.colors.onPrimary,
                    backgroundColor = MaterialTheme.colors.primary,
                    actions = {
                        IconButton(onClick = {
                            viewModel.showDialog()
                        }) {
                            Icon(Icons.Default.Delete, null)
                        }
                    }


                ) }) {
                viewModel.getProfile(profileName)
                if(viewModel.loadingProfile){
                    Column(
                        modifier = Modifier
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(5.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Text(
                            text = "Cargando perfil..."
                        )

                    }
                }
                else{
                    if(viewModel.showDialog){
                        AlertDialog(
                            onDismissRequest = { viewModel.hideDialog() },
                            confirmButton = {
                                TextButton(onClick = {
                                    viewModel.deleteProfile(viewModel.profileResult!!.profile)

                                })
                                { Text(text = "Borrar Perfil") }
                            },
                            dismissButton = {
                                TextButton(onClick = {viewModel.hideDialog()})
                                { Text(text = "Cancelar") }
                            },
                            title = { Text(text = "Borrar perfil") },
                            text = { Text(text = "¿Seguro que quieres borrar el perfil?") }
                        )
                    }
                    else{
                        ProfileContent(navController = navController, viewModel = viewModel)
                    }
                }
            }

        }
    }
}

@Composable
fun ProfileContent (navController: NavController,
                    viewModel: ProfileScreenViewModel,)
{
    val profile = viewModel.profileResult!!
    Column(modifier =
        Modifier.
            fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        Spacer(modifier = Modifier.padding(5.dp))
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(5.dp)
                .wrapContentSize(),
            elevation = 4.dp

        ){
            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(modifier = Modifier.padding(vertical = 20.dp))
                Text(
                    text = "Nombre",
                    style = MaterialTheme.typography.h5,
                    color = MaterialTheme.colors.onSurface,
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = profile.profile.name,
                        style = MaterialTheme.typography.h4,
                        color = MaterialTheme.colors.onSurface,
                    )
                }
                Spacer(modifier = Modifier.padding(vertical = 20.dp))
            }
        }
        Row(modifier = Modifier
            .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ){
            CardAchteristic("Edad: " + profile.profile.age.toString())
            CardAchteristic("Sexo: " + profile.profile.sex)
        }
        DescriptionCard(profile.profile.description)


    }


}

@Composable
fun CardAchteristic(text : String){
    Card(
        modifier = Modifier
            .padding(8.dp)
            .wrapContentSize(),
        elevation = 4.dp

    ){
        Column(
            modifier = Modifier
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.h5,
                    color = MaterialTheme.colors.onSurface,
                )
            }
        }
    }
}

@Composable
fun DescriptionCard(text : String){
    Card(
        modifier = Modifier
            .padding(8.dp)
            .wrapContentSize(),
        elevation = 4.dp

    ){
        Column(
            modifier = Modifier
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Observaciones:",
                style = MaterialTheme.typography.h5,
                color = MaterialTheme.colors.onSurface,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.body1,
                    color = MaterialTheme.colors.onSurface,
                )
            }
        }
    }
}