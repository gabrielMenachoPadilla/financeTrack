package com.example.financetrack

import com.example.financetrack.ui.inicio.validacionTransaccion
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TransactionValidatorTest {

    @Test
    fun isMontoValido_conMontoPositivo_retornaTrue() {
        // ARRANGE
        val montoCorrecto = 150.50
        // ACT
        val resultado = validacionTransaccion.isMontoValido(montoCorrecto)
        // ASSERT
        assertTrue(resultado)
    }

    @Test
    fun isMontoValido_conMontoNegativo_retornaFalse() {
        // ARRANGE
        val montoIncorrecto = -50.0
        // ACT
        val resultado = validacionTransaccion.isMontoValido(montoIncorrecto)
        // ASSERT
        assertFalse(resultado)
    }

    @Test
    fun isMontoValido_conMontoCero_retornaFalse() {
        // ARRANGE (Valor límite)
        val montoCero = 0.0
        // ACT
        val resultado = validacionTransaccion.isMontoValido(montoCero)
        // ASSERT
        assertFalse(resultado)
    }
}