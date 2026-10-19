package com.example.financetrack.ui.login

object ValidadorAutenticacion {

    fun esCorreoValido(correo: String): Boolean {
        if (correo.isBlank()) return false
        val regexCorreo = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$".toRegex()
        return regexCorreo.matches(correo)
    }

    fun contrasenasCoinciden(contrasena: String, confirmacion: String): Boolean {
        if (contrasena.isEmpty() || confirmacion.isEmpty()) return false
        return contrasena == confirmacion
    }
}