package com.example.financetrack.ui.categorias

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.financetrack.data.local.FinanceTrackDbHelper
import com.example.financetrack.data.model.Category
import com.example.financetrack.data.repository.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CategoriasViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = TransactionRepository(FinanceTrackDbHelper(application))

    private val _categoriasVisibles = MutableStateFlow<List<Category>>(emptyList())
    val categoriasVisibles: StateFlow<List<Category>> = _categoriasVisibles.asStateFlow()

    fun cargarCategorias(esGasto: Boolean) {
        val tipo = if (esGasto) "GASTO" else "INGRESO"
        val lista = repository.getAllCategories().filter { it.tipo == tipo }
        _categoriasVisibles.value = lista
    }

    fun agregarCategoria(nombre: String, tipo: String, colorHex: String, iconResId: Int) {
        val nueva = Category(nombre = nombre, tipo = tipo, color = colorHex, icono = iconResId)
        repository.insertCategory(nueva)
        cargarCategorias(tipo == "GASTO")
    }

    fun editarCategoria(id: Int, nombre: String, tipo: String, colorHex: String, iconResId: Int) {
        val actualizada = Category(id = id, nombre = nombre, tipo = tipo, color = colorHex, icono = iconResId)
        repository.updateCategory(actualizada)
        cargarCategorias(tipo == "GASTO")
    }

    fun eliminarCategoria(id: Int, esGasto: Boolean) {
        repository.deleteCategory(id)
        cargarCategorias(esGasto)
    }
}