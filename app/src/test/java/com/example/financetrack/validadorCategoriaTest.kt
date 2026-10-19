package com.example.financetrack

import com.example.financetrack.ui.categorias.validadorCategoria
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidadorCategoriaTest {

    @Test
    fun esNombreCategoriaValido_nombreCorrecto_retornaVerdadero() {
        // ARRANGE
        val nombreValido = "Transporte"

        // ACT
        val resultado = validadorCategoria.esNombreCategoriaValido(nombreValido)

        // ASSERT
        assertTrue(resultado)
    }

    @Test
    fun esNombreCategoriaValido_soloEspaciosEnBlanco_retornaFalso() {
        // ARRANGE solo espacios
        val nombreInvalido = "     "

        // ACT
        val resultado = validadorCategoria.esNombreCategoriaValido(nombreInvalido)

        // ASSERT
        assertFalse(resultado)
    }

    @Test
    fun esNombreCategoriaValido_nombreMuyCorto_retornaFalso() {
        // ARRANGE Menos de 3 caracteres
        val nombreCorto = "Lz"

        // ACT
        val resultado = validadorCategoria.esNombreCategoriaValido(nombreCorto)

        // ASSERT
        assertFalse(resultado)
    }
}