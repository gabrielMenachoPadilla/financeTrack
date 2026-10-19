package com.example.financetrack.ui.inicio

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.financetrack.R
import com.example.financetrack.data.local.FinanceTrackDbHelper
import com.example.financetrack.data.model.CategoriaDesglose
import com.example.financetrack.data.model.Transaction
import com.example.financetrack.data.repository.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class InicioViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = TransactionRepository(FinanceTrackDbHelper(application))

    private val _saldoDisponible = MutableStateFlow(0.0)
    val saldoDisponible: StateFlow<Double> = _saldoDisponible.asStateFlow()

    private val _totalIngresos = MutableStateFlow(0.0)
    val totalIngresos: StateFlow<Double> = _totalIngresos.asStateFlow()

    private val _totalGastos = MutableStateFlow(0.0)
    val totalGastos: StateFlow<Double> = _totalGastos.asStateFlow()

    // Datos para el gráfico
    private val _datosGrafico = MutableStateFlow<List<CategoriaDesglose>>(emptyList())
    val datosGrafico: StateFlow<List<CategoriaDesglose>> = _datosGrafico.asStateFlow()

    // Transacciones recientes
    private val _transaccionesRecientes = MutableStateFlow<List<Transaction>>(emptyList())
    val transaccionesRecientes: StateFlow<List<Transaction>> = _transaccionesRecientes.asStateFlow()

    fun cargarResumen() {
        val todasLasTransacciones = repository.getAllTransactions()
        val categoriasMap = repository.getAllCategories().associateBy { it.nombre }

        val ingresos = todasLasTransacciones.filter { it.type == "INGRESO" }.sumOf { it.amount }
        val gastos = todasLasTransacciones.filter { it.type == "GASTO" }.sumOf { it.amount }

        _totalIngresos.value = ingresos
        _totalGastos.value = gastos
        _saldoDisponible.value = calculadoraFinanzas.calcularSaldoDeTransacciones(todasLasTransacciones)

        // 1. Preparar datos para el Gráfico Circular (Ingresos vs Gastos)
        val listaDesglose = mutableListOf<CategoriaDesglose>()

        if (ingresos > 0) {
            listaDesglose.add(CategoriaDesglose("Ingresos", ingresos, 0))
        }
        if (gastos > 0) {
            listaDesglose.add(CategoriaDesglose("Gastos", gastos, 0))
        }
        _datosGrafico.value = listaDesglose

        // 2. Transacciones recientes con sus colores
        val recientes = todasLasTransacciones
            .sortedWith(compareByDescending<Transaction> { it.date }.thenByDescending { it.hora })
            .take(5)
            .map { t ->
                val cat = categoriasMap[t.category]
                t.copy(
                    colorCategory = cat?.color ?: "#888888",
                    iconCategory = cat?.icono ?: R.drawable.ic_dinero
                )
            }
        _transaccionesRecientes.value = recientes
    }
}
