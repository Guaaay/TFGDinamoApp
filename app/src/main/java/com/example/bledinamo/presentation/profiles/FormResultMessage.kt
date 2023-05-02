package com.example.bledinamo.presentation.profiles

sealed class FormResultMessage(val message : String) {
    object ExistingName : FormResultMessage("Ya existe un perfil con ese nombre")
    object IncompleteFields : FormResultMessage("Revisa que todos los campos estén completos.")
    object Loading : FormResultMessage("Cargando")
    object Success : FormResultMessage("success")
}
