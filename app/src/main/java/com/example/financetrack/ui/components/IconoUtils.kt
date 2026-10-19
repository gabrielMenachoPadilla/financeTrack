package com.example.financetrack.ui.components

import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import com.example.financetrack.R

/**
 * Carga el ícono de una categoría de forma segura.
 *
 * No usamos `painterResource` porque algunos recursos (íconos antiguos del sistema
 * `android.R.drawable.*` o ciertos XML) hacen que Compose lance
 * "Only VectorDrawables and rasterized asset types are supported".
 *
 * En su lugar delegamos en el cargador nativo de Android (Drawable), que soporta
 * cualquier tipo, y lo convertimos a un [BitmapPainter] para poder dibujarlo en Compose.
 */
@Composable
fun painterIconoCategoria(iconId: Int?): Painter {
    val context = LocalContext.current
    val id = iconId ?: R.drawable.ic_dinero

    return remember(id, context) {
        val drawable = cargarDrawable(context, id)
            ?: cargarDrawable(context, R.drawable.ic_dinero)

        val bitmap: Bitmap = drawable?.toBitmap(width = 96, height = 96)
            ?: Bitmap.createBitmap(96, 96, Bitmap.Config.ARGB_8888)

        BitmapPainter(bitmap.asImageBitmap())
    }
}

private fun cargarDrawable(
    context: android.content.Context,
    id: Int,
): android.graphics.drawable.Drawable? = try {
    ResourcesCompat.getDrawable(context.resources, id, context.theme)
} catch (_: Exception) {
    null
}
