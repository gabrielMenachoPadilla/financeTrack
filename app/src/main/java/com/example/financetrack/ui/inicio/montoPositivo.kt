package com.example.financetrack.ui.inicio


object validacionTransaccion {

    fun isMontoValido(monto: Double): Boolean {
        return monto > 0.0
    }
}