package com.example.financetrack.ui.login

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class RegisterUiState(
    val cargando: Boolean = false,
    val error: String? = null,
    val exito: Boolean = false,
)

class RegisterViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _state = MutableStateFlow(RegisterUiState())
    val state: StateFlow<RegisterUiState> = _state.asStateFlow()

    fun consumirError() = _state.update { it.copy(error = null) }

    fun registrar(email: String, password: String, confirmPassword: String) {
        val correo = email.trim()
        val clave = password.trim()
        val confirmacion = confirmPassword.trim()

        if (correo.isEmpty() || clave.isEmpty() || confirmacion.isEmpty()) {
            _state.update { it.copy(error = "Por favor llena todos los campos") }
            return
        }
        if (!ValidadorAutenticacion.esCorreoValido(correo)) {
            _state.update { it.copy(error = "Ingresa un correo válido") }
            return
        }
        if (clave.length < 6) {
            _state.update { it.copy(error = "La contraseña debe tener al menos 6 caracteres") }
            return
        }
        if (!ValidadorAutenticacion.contrasenasCoinciden(clave, confirmacion)) {
            _state.update { it.copy(error = "Las contraseñas no coinciden") }
            return
        }

        _state.update { it.copy(cargando = true, error = null) }
        auth.createUserWithEmailAndPassword(correo, clave)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _state.update { it.copy(cargando = false, exito = true) }
                } else {
                    _state.update {
                        it.copy(
                            cargando = false,
                            error = task.exception?.message ?: "No se pudo crear la cuenta",
                        )
                    }
                }
            }
    }
}
