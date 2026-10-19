package com.example.financetrack.ui.perfil

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.financetrack.data.local.FinanceTrackDbHelper
import com.example.financetrack.data.model.Category
import com.example.financetrack.data.model.Transaction
import com.example.financetrack.data.repository.TransactionRepository
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

data class PerfilUiState(
    val correo: String = "",
    val respaldando: Boolean = false,
    val sincronizando: Boolean = false,
    val mensaje: String? = null,
    val sesionCerrada: Boolean = false,
)

/** Espera el resultado de una [Task] de Firebase dentro de una corrutina. */
private suspend fun <T> Task<T>.awaitTask(): T = suspendCancellableCoroutine { cont ->
    addOnCompleteListener { task ->
        if (task.isSuccessful) {
            cont.resume(task.result)
        } else {
            cont.resumeWithException(task.exception ?: RuntimeException("Tarea fallida"))
        }
    }
}

class PerfilViewModel(application: Application) : AndroidViewModel(application) {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val repository = TransactionRepository(FinanceTrackDbHelper(application))
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val _uiState = MutableStateFlow(
        PerfilUiState(
            correo = auth.currentUser?.email ?: "Usuario no identificado"
        )
    )
    val uiState: StateFlow<PerfilUiState> = _uiState.asStateFlow()

    fun consumirMensaje() {
        _uiState.update { it.copy(mensaje = null) }
    }

    fun respaldar() {
        val user = auth.currentUser ?: run {
            _uiState.update { it.copy(mensaje = "No hay sesión activa") }
            return
        }
        if (_uiState.value.respaldando) return

        _uiState.update { it.copy(respaldando = true) }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val uid = user.uid
                val transacciones = repository.getAllTransactions()
                val categorias = repository.getAllCategories()

                for (t in transacciones) {
                    val mapa = hashMapOf(
                        "amount" to t.amount,
                        "type" to t.type,
                        "category" to t.category,
                        "date" to t.date,
                        "hora" to t.hora,
                        "description" to t.description,
                    )
                    firestore.collection("usuarios").document(uid)
                        .collection("transacciones").document(t.id.toString())
                        .set(mapa).awaitTask()
                }

                for (c in categorias) {
                    val mapa = hashMapOf(
                        "nombre" to c.nombre,
                        "tipo" to c.tipo,
                        "color" to c.color,
                        "icono" to c.icono,
                    )
                    firestore.collection("usuarios").document(uid)
                        .collection("categorias").document(c.id.toString())
                        .set(mapa).awaitTask()
                }

                withContext(Dispatchers.Main) {
                    _uiState.update {
                        it.copy(respaldando = false, mensaje = "¡Datos respaldados con éxito!")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _uiState.update {
                        it.copy(respaldando = false, mensaje = "Error: ${e.message}")
                    }
                }
            }
        }
    }

    fun sincronizar() {
        val user = auth.currentUser ?: run {
            _uiState.update { it.copy(mensaje = "No hay sesión activa") }
            return
        }
        if (_uiState.value.sincronizando) return

        _uiState.update { it.copy(sincronizando = true) }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val uid = user.uid
                val categoriasSnap = firestore.collection("usuarios").document(uid)
                    .collection("categorias").get().awaitTask()
                val transaccionesSnap = firestore.collection("usuarios").document(uid)
                    .collection("transacciones").get().awaitTask()

                val categorias = categoriasSnap.documents.map { doc ->
                    Category(
                        id = doc.id.toIntOrNull() ?: 0,
                        nombre = doc.getString("nombre").orEmpty(),
                        tipo = doc.getString("tipo") ?: "GASTO",
                        color = doc.getString("color") ?: "#888888",
                        icono = doc.getLong("icono")?.toInt() ?: 0,
                    )
                }

                val transacciones = transaccionesSnap.documents.map { doc ->
                    Transaction(
                        id = doc.id.toIntOrNull() ?: 0,
                        amount = doc.get("amount")?.toString()?.toDoubleOrNull() ?: 0.0,
                        type = doc.getString("type") ?: "GASTO",
                        category = doc.getString("category").orEmpty(),
                        date = doc.getString("date").orEmpty(),
                        hora = doc.getString("hora") ?: "00:00",
                        description = doc.getString("description").orEmpty(),
                    )
                }

                repository.reemplazarTodo(transacciones, categorias)

                withContext(Dispatchers.Main) {
                    _uiState.update {
                        it.copy(
                            sincronizando = false,
                            mensaje = "¡Sincronización completada! Datos restaurados.",
                        )
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _uiState.update {
                        it.copy(sincronizando = false, mensaje = "Error: ${e.message}")
                    }
                }
            }
        }
    }

    fun cerrarSesion() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.limpiarTodo()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            auth.signOut()
            withContext(Dispatchers.Main) {
                _uiState.update { it.copy(sesionCerrada = true) }
            }
        }
    }
}
