package com.example.financetrack.ui.login

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class LoginUiState(
    val cargando: Boolean = false,
    val error: String? = null,
    val exito: Boolean = false,
)

class LoginViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state.asStateFlow()

    fun consumirError() = _state.update { it.copy(error = null) }

    fun login(email: String, password: String) {
        val correo = email.trim()
        val clave = password.trim()

        if (correo.isEmpty() || clave.isEmpty()) {
            _state.update { it.copy(error = "Por favor llena todos los campos") }
            return
        }
        if (!ValidadorAutenticacion.esCorreoValido(correo)) {
            _state.update { it.copy(error = "Ingresa un correo válido") }
            return
        }

        _state.update { it.copy(cargando = true, error = null) }
        auth.signInWithEmailAndPassword(correo, clave)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _state.update { it.copy(cargando = false, exito = true) }
                } else {
                    _state.update {
                        it.copy(
                            cargando = false,
                            error = task.exception?.message ?: "No se pudo iniciar sesión",
                        )
                    }
                }
            }
    }
}
