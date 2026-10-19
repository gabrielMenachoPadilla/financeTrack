package com.example.financetrack.data.repository

import com.example.financetrack.data.local.FinanceTrackDbHelper
import com.example.financetrack.R
import com.example.financetrack.data.model.Category
import com.example.financetrack.data.model.Transaction
import java.text.SimpleDateFormat
import java.util.Locale

class TransactionRepository(private val dbHelper: FinanceTrackDbHelper) {

    // --- OPERACIONES DE TRANSACCIONES ---

    fun getAllTransactions(): List<Transaction> {
        val transactions = mutableListOf<Transaction>()
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM transactions", null)

        // Buscamos los índices por NOMBRE, no por posición: así toleramos
        // bases de datos creadas con esquemas antiguos (sin columna "hora").
        val idxId = cursor.getColumnIndex("id")
        val idxAmount = cursor.getColumnIndex("amount")
        val idxType = cursor.getColumnIndex("type")
        val idxCategory = cursor.getColumnIndex("category")
        val idxDate = cursor.getColumnIndex("date")
        val idxHora = cursor.getColumnIndex("hora")
        val idxDescription = cursor.getColumnIndex("description")

        while (cursor.moveToNext()) {
            transactions.add(
                Transaction(
                    id = if (idxId >= 0) cursor.getInt(idxId) else 0,
                    amount = if (idxAmount >= 0) cursor.getDouble(idxAmount) else 0.0,
                    type = if (idxType >= 0) cursor.getString(idxType) ?: "GASTO" else "GASTO",
                    category = if (idxCategory >= 0) cursor.getString(idxCategory) ?: "" else "",
                    date = normalizarFecha(if (idxDate >= 0) cursor.getString(idxDate) else null),
                    // Registros antiguos pueden tener hora o descripción nulas.
                    hora = if (idxHora >= 0) cursor.getString(idxHora) ?: "00:00" else "00:00",
                    description = if (idxDescription >= 0) cursor.getString(idxDescription) ?: "" else "",
                )
            )
        }
        cursor.close()
        return transactions
    }

    fun insertTransaction(transaction: Transaction): Boolean {
        val db = dbHelper.writableDatabase
        val values = android.content.ContentValues().apply {
            put("amount", transaction.amount)
            put("type", transaction.type)
            put("category", transaction.category)
            put("date", transaction.date)
            put("hora", transaction.hora) // Guardamos la hora
            put("description", transaction.description)
        }
        val result = db.insert("transactions", null, values)
        return result != -1L
    }

    fun updateTransaction(transaction: Transaction) {
        val db = dbHelper.writableDatabase
        val values = android.content.ContentValues().apply {
            put("amount", transaction.amount)
            put("type", transaction.type)
            put("category", transaction.category)
            put("date", transaction.date)
            put("hora", transaction.hora) // Actualizamos la hora también
            put("description", transaction.description)
        }
        db.update("transactions", values, "id = ?", arrayOf(transaction.id.toString()))
    }

    fun deleteTransaction(id: Int) {
        val db = dbHelper.writableDatabase
        db.delete("transactions", "id = ?", arrayOf(id.toString()))
    }


    // --- OPERACIONES DE SINCRONIZACIÓN ---

    /**
     * Reemplaza todo el contenido local por el recibido (usado al restaurar desde Firestore).
     * Se ejecuta en una sola transacción para no dejar la base de datos a medias.
     */
    fun reemplazarTodo(transacciones: List<Transaction>, categorias: List<Category>) {
        val db = dbHelper.writableDatabase
        db.beginTransaction()
        try {
            db.delete("transactions", null, null)
            db.delete("categories", null, null)

            for (c in categorias) {
                val values = android.content.ContentValues().apply {
                    if (c.id > 0) put("id", c.id)
                    put("nombre", c.nombre)
                    put("tipo", c.tipo)
                    put("color", c.color)
                    put("icono", c.icono)
                }
                db.insert("categories", null, values)
            }

            for (t in transacciones) {
                val values = android.content.ContentValues().apply {
                    if (t.id > 0) put("id", t.id)
                    put("amount", t.amount)
                    put("type", t.type)
                    put("category", t.category)
                    put("date", t.date)
                    put("hora", t.hora)
                    put("description", t.description)
                }
                db.insert("transactions", null, values)
            }

            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    /** Elimina todos los datos locales (usado al cerrar sesión). */
    fun limpiarTodo() {
        val db = dbHelper.writableDatabase
        db.beginTransaction()
        try {
            db.delete("transactions", null, null)
            db.delete("categories", null, null)
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }


    // --- OPERACIONES DE CATEGORÍAS ---

    fun getAllCategories(): List<Category> {
        val categories = mutableListOf<Category>()
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM categories", null)

        val idxId = cursor.getColumnIndex("id")
        val idxNombre = cursor.getColumnIndex("nombre")
        val idxTipo = cursor.getColumnIndex("tipo")
        val idxColor = cursor.getColumnIndex("color")
        val idxIcono = cursor.getColumnIndex("icono")

        while (cursor.moveToNext()) {
            categories.add(
                Category(
                    id = if (idxId >= 0) cursor.getInt(idxId) else 0,
                    nombre = if (idxNombre >= 0) cursor.getString(idxNombre) ?: "" else "",
                    tipo = if (idxTipo >= 0) cursor.getString(idxTipo) ?: "GASTO" else "GASTO",
                    color = if (idxColor >= 0) cursor.getString(idxColor) ?: "#888888" else "#888888",
                    icono = if (idxIcono >= 0) iconoValido(cursor.getInt(idxIcono)) else R.drawable.ic_dinero,
                )
            )
        }
        cursor.close()
        return categories
    }

    fun insertCategory(category: Category) {
        val db = dbHelper.writableDatabase
        val values = android.content.ContentValues().apply {
            put("nombre", category.nombre)
            put("tipo", category.tipo)
            put("color", category.color)
            put("icono", category.icono)
        }
        db.insert("categories", null, values)
    }

    fun updateCategory(category: Category) {
        val db = dbHelper.writableDatabase
        val values = android.content.ContentValues().apply {
            put("nombre", category.nombre)
            put("tipo", category.tipo)
            put("color", category.color)
            put("icono", category.icono)
        }
        db.update("categories", values, "id = ?", arrayOf(category.id.toString()))
    }

    fun deleteCategory(id: Int) {
        val db = dbHelper.writableDatabase
        db.delete("categories", "id = ?", arrayOf(id.toString()))
    }


    // --- UTILIDADES ---

    // Formatos aceptados, incluyendo los que se podían escribir a mano en versiones antiguas.
    private val formatosFecha = listOf(
        "yyyy-MM-dd",
        "dd/MM/yyyy",
        "d/M/yyyy",
        "dd-MM-yyyy",
        "d-M-yyyy",
        "yyyy/MM/dd",
    )

    private val formatoIso = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    /**
     * Los datos antiguos guardaban íconos del sistema (`android.R.drawable.*`), que Compose
     * no puede renderizar. Solo aceptamos recursos propios de la app (package id 0x7f).
     */
    private fun iconoValido(icono: Int): Int =
        if (((icono ushr 24) and 0xff) == 0x7f) icono else R.drawable.ic_dinero

    /** Convierte fechas antiguas de texto libre al formato estándar "yyyy-MM-dd". */
    private fun normalizarFecha(fecha: String?): String {
        if (fecha.isNullOrBlank()) return ""
        if (Regex("\\d{4}-\\d{2}-\\d{2}").matches(fecha)) return fecha

        for (formato in formatosFecha) {
            try {
                val date = SimpleDateFormat(formato, Locale.getDefault())
                    .apply { isLenient = false }
                    .parse(fecha)
                if (date != null) return formatoIso.format(date)
            } catch (_: Exception) {
                // Se prueba con el siguiente formato.
            }
        }
        return fecha // No se pudo interpretar: se conserva tal cual.
    }
}
