package com.example.financetrack.ui.categorias

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.financetrack.R
import com.example.financetrack.data.model.Category
import com.example.financetrack.ui.components.parseColorHex
import com.example.financetrack.ui.components.painterIconoCategoria
import com.example.financetrack.ui.theme.CardBackground
import com.example.financetrack.ui.theme.GoldPrimary
import com.example.financetrack.ui.theme.TextMuted
import com.example.financetrack.ui.theme.TextPrimary
import com.example.financetrack.ui.theme.TextSecondary
import com.example.financetrack.ui.transacciones.SheetDragHandle

private val COLORES_HEX = listOf(
    "#00BCD4", "#2196F3", "#F44336", "#4CAF50",
    "#E91E63", "#9C27B0", "#FBC02D", "#795548",
)

private val ICONOS_RES = listOf(
    R.drawable.ic_casa, R.drawable.ic_rayo, R.drawable.ic_agua,
    R.drawable.ic_banco, R.drawable.ic_dinero, R.drawable.ic_ahorro,
    R.drawable.ic_medicina, R.drawable.ic_corazon, R.drawable.ic_pesas,
    R.drawable.ic_pelicula, R.drawable.ic_juegos, R.drawable.ic_colegio,
    R.drawable.ic_maleta,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriasScreen(
    modifier: Modifier = Modifier,
    viewModel: CategoriasViewModel = viewModel(),
) {
    var esGasto by remember { mutableStateOf(true) }
    var categoriaAEditar by remember { mutableStateOf<Category?>(null) }
    var mostrarFormulario by remember { mutableStateOf(false) }
    var categoriaAEliminar by remember { mutableStateOf<Category?>(null) }

    val categorias by viewModel.categoriasVisibles.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.cargarCategorias(true)
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            PrimaryTabRow(
                selectedTabIndex = if (esGasto) 0 else 1,
                containerColor = CardBackground,
                contentColor = TextPrimary,
            ) {
                Tab(
                    selected = esGasto,
                    onClick = {
                        esGasto = true
                        viewModel.cargarCategorias(true)
                    },
                    text = { Text("GASTOS") },
                )
                Tab(
                    selected = !esGasto,
                    onClick = {
                        esGasto = false
                        viewModel.cargarCategorias(false)
                    },
                    text = { Text("INGRESOS") },
                )
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = 8.dp, bottom = 120.dp),
            ) {
                items(categorias) { categoria ->
                    CategoriaGridItem(
                        categoria = categoria,
                        onClick = { categoriaAEliminar = categoria },
                    )
                }
            }
        }

        FloatingActionButton(
            onClick = {
                categoriaAEditar = null
                mostrarFormulario = true
            },
            containerColor = GoldPrimary,
            contentColor = Color(0xFF121212),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 24.dp, bottom = 24.dp),
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Agregar categoría")
        }
    }

    if (mostrarFormulario) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { mostrarFormulario = false },
            sheetState = sheetState,
            containerColor = CardBackground,
            dragHandle = { SheetDragHandle() },
        ) {
            CategoriaFormSheet(
                categoriaExistente = categoriaAEditar,
                onGuardar = { nombre, color, icono ->
                    val tipo = if (esGasto) "GASTO" else "INGRESO"
                    val existente = categoriaAEditar
                    if (existente == null) {
                        viewModel.agregarCategoria(nombre, tipo, color, icono)
                    } else {
                        viewModel.editarCategoria(existente.id, nombre, tipo, color, icono)
                    }
                    mostrarFormulario = false
                },
                onDismiss = { mostrarFormulario = false },
            )
        }
    }

    categoriaAEliminar?.let { categoria ->
        AlertDialog(
            onDismissRequest = { categoriaAEliminar = null },
            containerColor = CardBackground,
            title = { Text(categoria.nombre, color = TextPrimary) },
            text = { Text("¿Qué deseas hacer con esta categoría?", color = TextSecondary) },
            confirmButton = {
                TextButton(onClick = {
                    categoriaAEliminar = null
                    categoriaAEditar = categoria
                    mostrarFormulario = true
                }) { Text("Editar", color = GoldPrimary) }
            },
            dismissButton = {
                TextButton(onClick = {
                    categoriaAEliminar = null
                    viewModel.eliminarCategoria(categoria.id, esGasto)
                }) { Text("Eliminar", color = Color(0xFFF44336)) }
            },
        )
    }
}

@Composable
private fun CategoriaGridItem(
    categoria: Category,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(parseColorHex(categoria.color)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterIconoCategoria(categoria.icono),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(32.dp),
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = categoria.nombre,
            color = TextPrimary,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun CategoriaFormSheet(
    categoriaExistente: Category?,
    onGuardar: (nombre: String, colorHex: String, iconoRes: Int) -> Unit,
    onDismiss: () -> Unit,
) {
    var nombre by remember { mutableStateOf(categoriaExistente?.nombre ?: "") }
    var colorSeleccionado by remember { mutableStateOf(categoriaExistente?.color ?: COLORES_HEX.first()) }
    var iconoSeleccionado by remember { mutableStateOf(categoriaExistente?.icono ?: ICONOS_RES.first()) }
    var errorNombre by remember { mutableStateOf(false) }

    val esEdicion = categoriaExistente != null

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 24.dp),
    ) {
        Text(
            text = if (esEdicion) "Editar Categoría" else "Crear Categoría",
            color = TextPrimary,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 20.dp),
        )

        OutlinedTextField(
            value = nombre,
            onValueChange = {
                nombre = it
                errorNombre = false
            },
            label = { Text("Nombre de la categoría") },
            singleLine = true,
            isError = errorNombre,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                cursorColor = GoldPrimary,
                focusedBorderColor = GoldPrimary,
                unfocusedBorderColor = Color(0xFF333333),
                focusedLabelColor = GoldPrimary,
                unfocusedLabelColor = TextMuted,
            ),
            modifier = Modifier.fillMaxWidth(),
        )
        if (errorNombre) {
            Text(
                text = "El nombre debe tener al menos 3 caracteres",
                color = Color(0xFFF44336),
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp, start = 4.dp),
            )
        }

        Spacer(Modifier.height(20.dp))

        Text("Símbolo", color = TextMuted, fontSize = 14.sp)
        Spacer(Modifier.height(8.dp))

        ICONOS_RES.chunked(5).forEach { fila ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                fila.forEach { resId ->
                    val seleccionado = resId == iconoSeleccionado
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(if (seleccionado) Color(0x44FFFFFF) else Color.Transparent)
                            .clickable { iconoSeleccionado = resId },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            painter = painterIconoCategoria(resId),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(28.dp),
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Text("Color", color = TextMuted, fontSize = 14.sp)
        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            COLORES_HEX.forEach { hex ->
                val seleccionado = hex == colorSeleccionado
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(parseColorHex(hex))
                        .then(
                            if (seleccionado) Modifier.border(3.dp, Color.White, CircleShape)
                            else Modifier
                        )
                        .clickable { colorSeleccionado = hex },
                )
            }
        }

        Spacer(Modifier.height(28.dp))

        Button(
            onClick = {
                if (validadorCategoria.esNombreCategoriaValido(nombre)) {
                    onGuardar(nombre.trim(), colorSeleccionado, iconoSeleccionado)
                } else {
                    errorNombre = true
                }
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = GoldPrimary,
                contentColor = Color(0xFF121212),
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
        ) {
            Text(
                text = if (esEdicion) "Actualizar" else "Guardar Categoría",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}
