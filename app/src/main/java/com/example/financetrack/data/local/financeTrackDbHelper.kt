package com.example.financetrack.data.local

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.financetrack.R

class FinanceTrackDbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        // v1: transactions (sin "hora") — apps del MVP
        // v2: agrega la columna "hora" y la tabla categories SIN borrar datos
        private const val DATABASE_VERSION = 2
        private const val DATABASE_NAME = "FinanceTrack.db"

        // Nombre de las tablas y columnas
        const val TABLE_TRANSACTIONS = "transactions"
        const val TABLE_CATEGORIES = "categories"
        const val COLUMN_ID = "id"
        const val COLUMN_AMOUNT = "amount"
        const val COLUMN_TYPE = "type"
        const val COLUMN_CATEGORY = "category"
        const val COLUMN_DATE = "date"
        const val COLUMN_HORA = "hora"
        const val COLUMN_DESCRIPTION = "description"

        private val CREATE_TRANSACTIONS = """
            CREATE TABLE IF NOT EXISTS $TABLE_TRANSACTIONS (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_AMOUNT REAL,
                $COLUMN_TYPE TEXT,
                $COLUMN_CATEGORY TEXT,
                $COLUMN_DATE TEXT,
                $COLUMN_HORA TEXT,
                $COLUMN_DESCRIPTION TEXT
            )
        """.trimIndent()

        private val CREATE_CATEGORIES = """
            CREATE TABLE IF NOT EXISTS $TABLE_CATEGORIES (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                nombre TEXT,
                tipo TEXT,
                color TEXT,
                icono INTEGER
            )
        """.trimIndent()
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(CREATE_TRANSACTIONS)
        db.execSQL(CREATE_CATEGORIES)
        insertarCategoriasPorDefecto(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Migración NO destructiva: se conservan las transacciones del usuario.
        if (oldVersion < 2) {
            // 1. Agrega "hora" si la tabla viene del esquema antiguo.
            if (!columnaExiste(db, TABLE_TRANSACTIONS, COLUMN_HORA)) {
                db.execSQL("ALTER TABLE $TABLE_TRANSACTIONS ADD COLUMN $COLUMN_HORA TEXT")
            }
            // 2. Crea la tabla de categorías si no existía.
            db.execSQL(CREATE_CATEGORIES)
            insertarCategoriasPorDefecto(db)
        }
    }

    private fun columnaExiste(db: SQLiteDatabase, tabla: String, columna: String): Boolean {
        db.rawQuery("PRAGMA table_info($tabla)", null).use { cursor ->
            val idxNombre = cursor.getColumnIndex("name")
            while (cursor.moveToNext()) {
                if (idxNombre >= 0 && cursor.getString(idxNombre) == columna) return true
            }
        }
        return false
    }

    private fun insertarCategoriasPorDefecto(db: SQLiteDatabase) {
        // Solo inserta las categorías por defecto si la tabla está vacía.
        db.rawQuery("SELECT COUNT(*) FROM $TABLE_CATEGORIES", null).use { cursor ->
            if (cursor.moveToFirst() && cursor.getInt(0) > 0) return
        }

        val iconAlimentacion = R.drawable.ic_dinero
        val iconTransporte = R.drawable.ic_tren
        val iconIngreso = R.drawable.ic_banco

        db.execSQL("INSERT INTO $TABLE_CATEGORIES (nombre, tipo, color, icono) VALUES ('Alimentación', 'GASTO', '#F44336', $iconAlimentacion)")
        db.execSQL("INSERT INTO $TABLE_CATEGORIES (nombre, tipo, color, icono) VALUES ('Transporte', 'GASTO', '#2196F3', $iconTransporte)")
        db.execSQL("INSERT INTO $TABLE_CATEGORIES (nombre, tipo, color, icono) VALUES ('Salario', 'INGRESO', '#4CAF50', $iconIngreso)")
    }
}
