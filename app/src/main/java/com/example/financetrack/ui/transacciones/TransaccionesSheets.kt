package com.example.financetrack.ui.transacciones

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.KeyboardOptions
import com.example.financetrack.ui.inicio.validacionTransaccion
import com.example.financetrack.ui.theme.GoldPrimary
import com.example.financetrack.ui.theme.GreenIncomeDark
import com.example.financetrack.ui.theme.RedExpense
import com.example.financetrack.ui.theme.TextMuted
import com.example.financetrack.ui.theme.TextPrimary
import com.example.financetrack.ui.theme.TextSecondary

/** Indicador visual (la "píldora") en la parte superior de los bottom sheets. */
@Composable
fun SheetDragHandle(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .padding(top = 12.dp, bottom = 20.dp)
            .size(width = 40.dp, height = 4.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(Color(0xFF666666)),
    )
}

@Composable
fun OpcionesTransaccionSheet(
    onEditar: () -> Unit,
    onEliminar: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        SheetDragHandle()
        Text(
            text = "Opciones de Movimiento",
            color = TextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp),
        )

        OpcionFila(
            icono = { Icon(Icons.Filled.Edit, contentDescription = null, tint = TextPrimary) },
            texto = "Editar",
            colorTexto = TextPrimary,
            onClick = onEditar,
        )
        OpcionFila(
            icono = { Icon(Icons.Filled.Delete, contentDescription = null, tint = RedExpense) },
            texto = "Eliminar",
            colorTexto = RedExpense,
            onClick = onEliminar,
        )
    }
}

@Composable
private fun OpcionFila(
    icono: @Composable () -> Unit,
    texto: String,
    colorTexto: Color,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 16.dp, horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.size(24.dp), contentAlignment = Alignment.Center) { icono() }
        Spacer(Modifier.width(16.dp))
        Text(text = texto, color = colorTexto, fontSize = 16.sp)
    }
}

/**
 * Formulario reutilizable para registrar o editar una transacción.
 */
@Composable
fun TransaccionFormSheet(
    tipo: String,
    categorias: List<String>,
    titulo: String,
    accionTexto: String,
    montoInicial: String = "",
    descripcionInicial: String = "",
    categoriaInicial: String? = null,
    onGuardar: (monto: Double, categoria: String, descripcion: String) -> Unit,
    onDismiss: () -> Unit,
) {
    val esIngreso = tipo == "INGRESO"
    val colorAcento = if (esIngreso) GreenIncomeDark else GoldPrimary
    val colorTextoAccion = if (esIngreso) Color.White else Color(0xFF121212)

    val opciones = remember(categorias) {
        if (categorias.isNotEmpty()) categorias else listOf("General")
    }

    var monto by remember { mutableStateOf(montoInicial) }
    var descripcion by remember { mutableStateOf(descripcionInicial) }
    var categoria by remember {
        mutableStateOf(
            categoriaInicial?.takeIf { it in opciones } ?: opciones.first()
        )
    }
    var dropdownAbierto by remember { mutableStateOf(false) }
    var montoError by remember { mutableStateOf(false) }

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = colorAcento,
        unfocusedBorderColor = colorAcento,
        focusedTextColor = TextPrimary,
        unfocusedTextColor = TextPrimary,
        cursorColor = colorAcento,
        focusedLabelColor = colorAcento,
        unfocusedLabelColor = TextMuted,
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 24.dp),
    ) {
        Text(
            text = titulo,
            color = TextPrimary,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 20.dp),
        )

        OutlinedTextField(
            value = monto,
            onValueChange = {
                monto = it
                montoError = false
            },
            label = { Text("Monto (0.00)") },
            singleLine = true,
            isError = montoError,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            colors = textFieldColors,
            modifier = Modifier.fillMaxWidth(),
        )
        if (montoError) {
            Text(
                text = "Ingresa un monto mayor a 0",
                color = RedExpense,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp, start = 4.dp),
            )
        }

        Spacer(Modifier.height(16.dp))

        Text("Categoría", color = TextMuted, fontSize = 12.sp)
        Spacer(Modifier.height(4.dp))
        Box {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { dropdownAbierto = true }
                    .padding(vertical = 14.dp, horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(categoria, color = TextPrimary, fontSize = 16.sp)
                Icon(Icons.Filled.ArrowDropDown, contentDescription = null, tint = TextPrimary)
            }
            DropdownMenu(
                expanded = dropdownAbierto,
                onDismissRequest = { dropdownAbierto = false },
            ) {
                opciones.forEach { opcion ->
                    DropdownMenuItem(
                        text = { Text(opcion, color = TextPrimary) },
                        onClick = {
                            categoria = opcion
                            dropdownAbierto = false
                        },
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = descripcion,
            onValueChange = { descripcion = it },
            label = { Text("Descripción (opcional)") },
            singleLine = true,
            colors = textFieldColors,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = TextMuted)
            }
            Spacer(Modifier.width(8.dp))
            Button(
                onClick = {
                    val valor = monto.replace(",", ".").trim().toDoubleOrNull() ?: 0.0
                    if (validacionTransaccion.isMontoValido(valor)) {
                        onGuardar(valor, categoria, descripcion)
                    } else {
                        montoError = true
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorAcento,
                    contentColor = colorTextoAccion,
                ),
            ) {
                Text(accionTexto, fontWeight = FontWeight.Bold)
            }
        }
    }
}
