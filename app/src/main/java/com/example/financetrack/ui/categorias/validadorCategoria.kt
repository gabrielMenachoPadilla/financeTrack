package com.example.financetrack.ui.categorias


object validadorCategoria {
    fun esNombreCategoriaValido(nombre: String): Boolean {
        val nombreLimpio = nombre.trim()
        return nombreLimpio.isNotEmpty() && nombreLimpio.length >= 3
    }
}