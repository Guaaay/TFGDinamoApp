package com.example.bledinamo.presentation.profiles

import android.annotation.SuppressLint
import android.util.Log
import androidx.activity.OnBackPressedCallback
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.AddCircle
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.bledinamo.R

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun CreateProfileForm(navController: NavController, viewModel : ProfilesListViewModel = hiltViewModel()){

    Scaffold(topBar = {
        TopAppBar(
            title = { Text(text = "Crear un perfil") },
            contentColor = MaterialTheme.colors.onPrimary,
            backgroundColor = MaterialTheme.colors.primary,


            )
    }) {
        Card {
            Column(
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
            ) {
                
                val mainFormState by viewModel.formState.collectAsState()
                Spacer(modifier = Modifier.padding(vertical = 35.dp))
                Text(style = MaterialTheme.typography.h6,text = "Nombre*:")
                OutlinedTextField(
                    modifier = Modifier.padding(vertical = 8.dp),
                    value = mainFormState.currentName,
                    singleLine = true,
                    onValueChange = { viewModel.updateName(it) },
                    label = { Text(text = "Nombre del perfil") },
                    isError = mainFormState.currentNameErrors.isNotEmpty()
                )
                mainFormState.currentNameErrors.forEach {
                    Text(
                        modifier = Modifier.padding(vertical = 8.dp),
                        text = it,
                        color = MaterialTheme.colors.error
                    )
                }
                Spacer(modifier = Modifier.size(15.dp))
                Text(style = MaterialTheme.typography.h6,text = "Sexo*:")
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = mainFormState.male,
                        onCheckedChange = { viewModel.updateSex("M") })
                    Spacer(Modifier.width(5.dp))
                    Text("M")
                    Spacer(Modifier.width(5.dp))
                    Checkbox(
                        checked = mainFormState.female,
                        onCheckedChange = { viewModel.updateSex("F") })
                    Text("F")
                }
                Text(style = MaterialTheme.typography.h6,text = "Edad*:")
                OutlinedTextField(
                    modifier = Modifier.padding(vertical = 8.dp),
                    value = mainFormState.currentAgeText,
                    singleLine = true,
                    onValueChange = { viewModel.updateAge(it) },
                    label = { Text(text = "Edad") },
                    isError = mainFormState.currentAgeErrors.isNotEmpty()
                )
                mainFormState.currentAgeErrors.forEach {
                    Text(
                        modifier = Modifier.padding(vertical = 8.dp),
                        text = it,
                        color = MaterialTheme.colors.error
                    )
                }
                Text(style = MaterialTheme.typography.h6,text = "Observaciones:")
                OutlinedTextField(
                    modifier = Modifier.padding(vertical = 8.dp),
                    value = mainFormState.currentDescription,
                    onValueChange = { viewModel.updateDesc(it) },
                    label = { Text(text = "Observaciones") },
                )
                Spacer(Modifier.padding(15.dp))
                var error = false

                if(!viewModel.validatingForm && viewModel.allProfiles != null){
                    Button(onClick = {
                        viewModel.createProfile()
                        if(viewModel.formResult.message == "success"){
                            navController.popBackStack()
                        }
                        else{
                            Log.d("CreateProfileForm","error set to true")
                            error = true
                        }

                    }) {
                        Text(text = "CONFIRMAR")
                    }
                }
                else{
                    viewModel.getProfiles()
                    CircularProgressIndicator()
                }
                if(viewModel.formResult.message != "success" && viewModel.formResult.message != "Cargando"){
                    Text(
                        modifier = Modifier.padding(vertical = 8.dp),
                        text = viewModel.formResult.message,
                        color = MaterialTheme.colors.error
                    )
                }

                Text(style = MaterialTheme.typography.caption,color = MaterialTheme.colors.onSurface,text = "Los campos con * son obligatorios")
            }

        }
    }
}

