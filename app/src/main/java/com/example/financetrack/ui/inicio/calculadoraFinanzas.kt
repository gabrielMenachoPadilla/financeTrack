package com.example.financetrack.ui.inicio

import com.example.financetrack.data.model.Transaction

object calculadoraFinanzas {
    fun calcularSaldoDeTransacciones(transacciones: List<Transaction>): Double {
        val ingresos = transacciones.filter { it.type == "INGRESO" }.sumOf { it.amount }
        val gastos = transacciones.filter { it.type == "GASTO" }.sumOf { it.amount }
        return ingresos - gastos
    }
}