package com.example.financetrack

import com.example.financetrack.ui.login.ValidadorAutenticacion
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidadorAutenticacionTest {

    @Test
    fun esCorreoValido_conCorreoCorrecto_retornaVerdadero() {
        // ARRANGE
        val correoValido = "gabriel@gmail.com"
        // ACT
        val resultado = ValidadorAutenticacion.esCorreoValido(correoValido)
        // ASSERT
        assertTrue(resultado)
    }

    @Test
    fun esCorreoValido_sinArroba_retornaFalso() {
        // ARRANGE
        val correoInvalido = "gabrielgmail.com"
        // ACT
        val resultado = ValidadorAutenticacion.esCorreoValido(correoInvalido)
        // ASSERT
        assertFalse(resultado)
    }

    @Test
    fun esCorreoValido_textoVacio_retornaFalso() {
        // ARRANGE
        val correoVacio = ""
        // ACT
        val resultado = ValidadorAutenticacion.esCorreoValido(correoVacio)
        // ASSERT
        assertFalse(resultado)
    }
    @Test
    fun contrasenasCoinciden_textosIguales_retornaVerdadero() {
        // ARRANGE
        val contrasena = "secreto123"
        val confirmacion = "secreto123"
        // ACT
        val resultado = ValidadorAutenticacion.contrasenasCoinciden(contrasena, confirmacion)
        // ASSERT
        assertTrue(resultado)
    }

    @Test
    fun contrasenasCoinciden_textosDiferentes_retornaFalso() {
        // ARRANGE
        val contrasena = "secreto123"
        val confirmacion = "secreto124"
        // ACT
        val resultado = ValidadorAutenticacion.contrasenasCoinciden(contrasena, confirmacion)
        // ASSERT
        assertFalse(resultado)
    }
}
