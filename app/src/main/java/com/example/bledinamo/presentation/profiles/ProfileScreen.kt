package com.example.bledinamo.presentation.profiles

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import java.time.LocalDateTime


@RequiresApi(Build.VERSION_CODES.O)
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
                    title = { Text(text = profileName) },
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
                            .fillMaxWidth()
                            ,
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

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ProfileContent (navController: NavController,
                    viewModel: ProfileScreenViewModel,)
{
    val profile = viewModel.profileResult!!
    Column(modifier =
    Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState()),
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
                Spacer(modifier = Modifier.padding(vertical = 15.dp))
                Text(
                    text = "Nombre",
                    style = MaterialTheme.typography.h6,
                    color = MaterialTheme.colors.onSurface,
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = profile.profile.name,
                        style = MaterialTheme.typography.h5,
                        color = MaterialTheme.colors.onSurface,
                    )
                }
                Spacer(modifier = Modifier.padding(vertical = 15.dp))
            }
        }
        Row(modifier = Modifier
            .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ){
            CardAchteristic("Edad: " + profile.profile.age.toString())
            CardAchteristic("Sexo: " + profile.profile.sex)
        }
        CardAchteristic(text = "Record en gobos: " + profile.profile.gatoHighScore.toString())
        DescriptionCard(profile.profile.description)
        CardAchteristic("Medidas:")
        Button(onClick = {
            viewModel.setCurrentProfile(profile.profile.name)
            navController.navigate("grip_graph")
        }){
            Text("Realizar una nueva medida")
        }

        if(profile.measurements.isNotEmpty()){
            MeasuresGraph(measureList = profile.measurements)
            profile.measurements.asReversed().forEach{measurement ->
                MeasurementCard(measurement = measurement.measurement, date = measurement.dateTaken)
            }
            Spacer(modifier = Modifier.padding(vertical = 30.dp))
        }
    }


}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MeasurementCard(measurement: Float, date : LocalDateTime){
    Card(
        modifier = Modifier
            .padding(8.dp)
            .wrapContentSize(),
        elevation = 4.dp,
    ){
        Column(
            modifier = Modifier
                .padding(10.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            var cero = ""
            if(date.minute < 10)
                cero = "0"
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Fuerza: ${"%.2f".format(measurement)}Kg  Fecha: ${date.dayOfMonth}/${date.monthValue}/${date.year} a las ${date.hour}:$cero${date.minute}",
                    style = MaterialTheme.typography.body1,
                    color = MaterialTheme.colors.onSurface,
                )
            }
        }
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
                    style = MaterialTheme.typography.h6,
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
                style = MaterialTheme.typography.h6,
                color = MaterialTheme.colors.onSurface,
            )
            if(text.length != 0){
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = text,
                        style = MaterialTheme.typography.body1,
                        color = MaterialTheme.colors.onSurface,
                    )
                }
            }
            else{
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Sin observaciones",
                        style = MaterialTheme.typography.body1,
                        color = MaterialTheme.colors.onSurface,
                    )
                }
            }

        }
    }
}