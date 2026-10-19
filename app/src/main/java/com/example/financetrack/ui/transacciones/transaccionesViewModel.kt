package com.example.financetrack.ui.transacciones

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.financetrack.R
import com.example.financetrack.data.local.FinanceTrackDbHelper
import com.example.financetrack.data.model.Category
import com.example.financetrack.data.model.CategoriaDesglose
import com.example.financetrack.data.model.Transaction
import com.example.financetrack.data.repository.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class TransaccionesViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = TransactionRepository(FinanceTrackDbHelper(application))

    private val _transaccionesFiltradas = MutableStateFlow<List<Transaction>>(emptyList())
    val transaccionesFiltradas: StateFlow<List<Transaction>> = _transaccionesFiltradas.asStateFlow()

    private val _datosGrafico = MutableStateFlow<List<CategoriaDesglose>>(emptyList())
    val datosGrafico: StateFlow<List<CategoriaDesglose>> = _datosGrafico.asStateFlow()

    // Memoria para saber qué estamos viendo y poder recargar la misma pantalla
    private var filtroEsGastoActual = true
    private var filtroPeriodoActual = "Mes"

    // Variables para controlar la fecha actual que estamos viendo
    val fechaBase: Calendar = Calendar.getInstance()
    private val _textoFecha = MutableStateFlow("")
    val textoFecha: StateFlow<String> = _textoFecha.asStateFlow()

    private val formatosFecha = listOf(
        "yyyy-MM-dd",
        "dd/MM/yyyy",
        "d/M/yyyy",
        "dd-MM-yyyy",
        "d-M-yyyy",
        "yyyy/MM/dd",
    )

    /** Interpreta la fecha de una transacción probando varios formatos. Devuelve null si no se puede. */
    private fun parseFecha(fecha: String): Calendar? {
        for (formato in formatosFecha) {
            try {
                val date = SimpleDateFormat(formato, Locale.getDefault())
                    .apply { isLenient = false }
                    .parse(fecha)
                if (date != null) return Calendar.getInstance().apply { time = date }
            } catch (_: Exception) {
                // Se prueba con el siguiente formato.
            }
        }
        return null
    }

    // Navegar en el tiempo
    fun cambiarFecha(offset: Int, periodo: String, esGasto: Boolean) {
        when (periodo) {
            "Día" -> fechaBase.add(Calendar.DAY_OF_YEAR, offset)
            "Semana" -> fechaBase.add(Calendar.WEEK_OF_YEAR, offset)
            "Mes" -> fechaBase.add(Calendar.MONTH, offset)
            "Año" -> fechaBase.add(Calendar.YEAR, offset)
        }
        filtrarDatos(esGasto, periodo)
    }

    // Elegir fecha exacta desde el calendario
    fun setFechaExacta(year: Int, month: Int, day: Int, periodo: String, esGasto: Boolean) {
        fechaBase.set(year, month, day)
        filtrarDatos(esGasto, periodo)
    }

    private fun actualizarTextoFecha(periodo: String) {
        val formato = when (periodo) {
            "Día" -> SimpleDateFormat("dd 'de' MMMM, yyyy", Locale.forLanguageTag("es-ES"))
            "Semana" -> SimpleDateFormat("'Semana del' dd MMM", Locale.forLanguageTag("es-ES"))
            "Mes" -> SimpleDateFormat("MMMM yyyy", Locale.forLanguageTag("es-ES"))
            "Año" -> SimpleDateFormat("yyyy", Locale.forLanguageTag("es-ES"))
            else -> SimpleDateFormat("MMMM yyyy", Locale.forLanguageTag("es-ES"))
        }
        _textoFecha.value = formato.format(fechaBase.time).replaceFirstChar { it.uppercase() }
    }

    fun filtrarDatos(esGasto: Boolean, periodo: String) {
        // 1. Guardamos en la memoria lo que el usuario está viendo ahora mismo
        filtroEsGastoActual = esGasto
        filtroPeriodoActual = periodo

        // 2. Actualizamos el texto de la fecha en la interfaz
        actualizarTextoFecha(periodo)

        val todas = repository.getAllTransactions()

        // Traemos el diccionario de categorías
        val categoriasMap = repository.getAllCategories().associateBy { it.nombre }

        val tipoBusqueda = if (esGasto) "GASTO" else "INGRESO"
        val filtradasPorTipo = todas.filter { it.type == tipoBusqueda }

        val filtradasPorTiempo = filtradasPorTipo.filter { transaccion ->
            val calTransaccion = parseFecha(transaccion.date) ?: return@filter false

            when (periodo) {
                "Día" -> calTransaccion.get(java.util.Calendar.YEAR) == fechaBase.get(java.util.Calendar.YEAR) && calTransaccion.get(java.util.Calendar.DAY_OF_YEAR) == fechaBase.get(java.util.Calendar.DAY_OF_YEAR)
                "Semana" -> calTransaccion.get(java.util.Calendar.YEAR) == fechaBase.get(java.util.Calendar.YEAR) && calTransaccion.get(java.util.Calendar.WEEK_OF_YEAR) == fechaBase.get(java.util.Calendar.WEEK_OF_YEAR)
                "Mes" -> calTransaccion.get(java.util.Calendar.YEAR) == fechaBase.get(java.util.Calendar.YEAR) && calTransaccion.get(java.util.Calendar.MONTH) == fechaBase.get(java.util.Calendar.MONTH)
                "Año" -> calTransaccion.get(java.util.Calendar.YEAR) == fechaBase.get(java.util.Calendar.YEAR)
                else -> true
            }
        }.map { t ->
            // Inyectamos el color e ícono
            val cat = categoriasMap[t.category]
            t.copy(
                colorCategory = cat?.color ?: "#888888",
                iconCategory = cat?.icono ?: R.drawable.ic_dinero
            )
        }.sortedWith(compareByDescending<Transaction> { it.date }.thenByDescending { it.hora })

        _transaccionesFiltradas.value = filtradasPorTiempo

        val agrupadas = filtradasPorTiempo.groupBy { it.category }
        val totalGeneral = filtradasPorTiempo.sumOf { it.amount }

        val desglose = agrupadas.map { (cat, lista) ->
            val totalCat = lista.sumOf { it.amount }
            val porcentaje = if (totalGeneral > 0) (totalCat / totalGeneral * 100).toInt() else 0

            // NUEVO: Buscamos el color en el diccionario de categorías
            val colorCategoria = categoriasMap[cat]?.color ?: "#888888"

            com.example.financetrack.data.model.CategoriaDesglose(cat, totalCat, porcentaje, colorCategoria)
        }.sortedByDescending { it.monto }

        _datosGrafico.value = desglose
    }

    fun obtenerCategorias(tipo: String): List<Category> = repository.getAllCategories().filter { it.tipo == tipo }

    fun agregarTransaccion(monto: Double, tipo: String, categoria: String, fecha: String, descripcion: String) {
        // Capturamos la hora exacta en formato 24h
        val horaActual = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date())

        val nueva = Transaction(
            amount = monto,
            type = tipo,
            category = categoria,
            date = fecha,
            hora = horaActual, // Inyectamos la hora
            description = descripcion
        )
        repository.insertTransaction(nueva)

        // Recargar la lista automáticamente
        filtrarDatos(filtroEsGastoActual, filtroPeriodoActual)
    }

    fun editarTransaccion(id: Int, monto: Double, tipo: String, categoria: String, fecha: String, descripcion: String) {
        val horaActual = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date())

        val actualizada = Transaction(
            id = id,
            amount = monto,
            type = tipo,
            category = categoria,
            date = fecha,
            hora = horaActual, // Actualizamos la hora para que el cambio suba arriba
            description = descripcion
        )
        repository.updateTransaction(actualizada)

        // Recargar la lista usando la memoria
        filtrarDatos(filtroEsGastoActual, filtroPeriodoActual)
    }

    fun eliminarTransaccion(id: Int, esGasto: Boolean) {
        repository.deleteTransaction(id)

        // Recargar la lista usando la memoria
        filtrarDatos(filtroEsGastoActual, filtroPeriodoActual)
    }
}