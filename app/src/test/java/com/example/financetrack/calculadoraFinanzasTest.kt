package com.example.financetrack

import com.example.financetrack.data.model.Transaction
import com.example.financetrack.ui.inicio.calculadoraFinanzas // Importación correcta
import org.junit.Assert.assertEquals
import org.junit.Test

class calcularFinanzasTest {

    @Test
    fun calcularSaldoDeTransacciones_retornaElBalanceCorrecto() {
        // ARRANGE
        val listaFicticia = listOf(
            Transaction(id = 1, amount = 1000.0, type = "INGRESO", category = "Salario",
                date = "2026-10-01", hora = "10:00", description = ""),
            Transaction(id = 2, amount = 500.0, type = "INGRESO", category = "Bono",
                date = "2026-10-02", hora = "11:00", description = ""),
            Transaction(id = 3, amount = 300.0, type = "GASTO", category = "Comida",
                date = "2026-10-03", hora = "12:00", description = "")
        )
        val saldoEsperado = 1200.0

        // ACT
        val saldoReal = calculadoraFinanzas.calcularSaldoDeTransacciones(listaFicticia)

        // ASSERT
        assertEquals(saldoEsperado, saldoReal, 0.0)
    }
}