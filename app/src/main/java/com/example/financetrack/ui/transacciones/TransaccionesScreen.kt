package com.example.financetrack.ui.transacciones

import android.app.DatePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.financetrack.data.model.Transaction
import com.example.financetrack.ui.components.PieChart
import com.example.financetrack.ui.components.PieSlice
import com.example.financetrack.ui.components.TransaccionItem
import com.example.financetrack.ui.components.formatearMonto
import com.example.financetrack.ui.components.parseColorHex
import com.example.financetrack.ui.theme.CardBackground
import com.example.financetrack.ui.theme.GoldPrimary
import com.example.financetrack.ui.theme.TextMuted
import com.example.financetrack.ui.theme.TextPrimary
import com.example.financetrack.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private val PERIODOS = listOf("Día", "Semana", "Mes", "Año")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransaccionesScreen(
    modifier: Modifier = Modifier,
    viewModel: TransaccionesViewModel = viewModel(),
) {
    val context = LocalContext.current

    var esGasto by remember { mutableStateOf(true) }
    var periodo by remember { mutableStateOf("Mes") }
    var indiceSeleccionado by remember { mutableStateOf<Int?>(null) }

    var mostrarNueva by remember { mutableStateOf(false) }
    var mostrarOpcionesPara by remember { mutableStateOf<Transaction?>(null) }
    var mostrarEditarPara by remember { mutableStateOf<Transaction?>(null) }

    val desglose by viewModel.datosGrafico.collectAsStateWithLifecycle()
    val lista by viewModel.transaccionesFiltradas.collectAsStateWithLifecycle()
    val textoFecha by viewModel.textoFecha.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.filtrarDatos(esGasto, periodo)
    }

    val slices = desglose.map { dato ->
        PieSlice(
            label = dato.nombre,
            value = dato.monto.toFloat(),
            color = parseColorHex(dato.colorHex),
        )
    }
    val totalActual = desglose.sumOf { it.monto }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Pestañas Gastos / Ingresos
            val tabSeleccionada = if (esGasto) 0 else 1
            PrimaryTabRow(
                selectedTabIndex = tabSeleccionada,
                containerColor = CardBackground,
                contentColor = TextPrimary,
            ) {
                Tab(
                    selected = esGasto,
                    onClick = {
                        esGasto = true
                        indiceSeleccionado = null
                        viewModel.filtrarDatos(true, periodo)
                    },
                    text = { Text("GASTOS") },
                )
                Tab(
                    selected = !esGasto,
                    onClick = {
                        esGasto = false
                        indiceSeleccionado = null
                        viewModel.filtrarDatos(false, periodo)
                    },
                    text = { Text("INGRESOS") },
                )
            }

            // Filtro por periodo
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                PERIODOS.forEach { p ->
                    FilterChip(
                        selected = periodo == p,
                        onClick = {
                            periodo = p
                            indiceSeleccionado = null
                            viewModel.filtrarDatos(esGasto, p)
                        },
                        label = { Text(p) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GoldPrimary,
                            selectedLabelColor = Color(0xFF121212),
                            labelColor = TextSecondary,
                        ),
                    )
                }
            }

            // Navegador de fecha
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = {
                    viewModel.cambiarFecha(-1, periodo, esGasto)
                    indiceSeleccionado = null
                }) {
                    Icon(Icons.Filled.ChevronLeft, contentDescription = "Anterior", tint = TextPrimary)
                }
                Text(
                    text = textoFecha.ifBlank { "Cargando..." },
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .clickable {
                            val cal = viewModel.fechaBase
                            DatePickerDialog(
                                context,
                                { _, anio, mes, dia ->
                                    viewModel.setFechaExacta(anio, mes, dia, periodo, esGasto)
                                    indiceSeleccionado = null
                                },
                                cal.get(Calendar.YEAR),
                                cal.get(Calendar.MONTH),
                                cal.get(Calendar.DAY_OF_MONTH),
                            ).show()
                        }
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                )
                IconButton(onClick = {
                    viewModel.cambiarFecha(1, periodo, esGasto)
                    indiceSeleccionado = null
                }) {
                    Icon(Icons.Filled.ChevronRight, contentDescription = "Siguiente", tint = TextPrimary)
                }
            }

            // Gráfico circular
            if (slices.isNotEmpty()) {
                val centro = if (indiceSeleccionado != null) {
                    val s = desglose[indiceSeleccionado!!]
                    "${s.nombre}\n${formatearMonto(s.monto)}"
                } else {
                    "Total\n${formatearMonto(totalActual)}"
                }
                PieChart(
                    slices = slices,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .padding(8.dp),
                    centerText = centro,
                    selectedIndex = indiceSeleccionado,
                    onSliceClick = { indiceSeleccionado = it },
                )
            }

            // Lista / Estado vacío
            Box(modifier = Modifier.weight(1f)) {
                if (lista.isEmpty()) {
                    EstadoVacio()
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 8.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 120.dp),
                    ) {
                        items(lista) { transaccion ->
                            TransaccionItem(
                                transaction = transaccion,
                                onClick = { mostrarOpcionesPara = transaccion },
                            )
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { mostrarNueva = true },
            containerColor = GoldPrimary,
            contentColor = Color(0xFF121212),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 24.dp, bottom = 24.dp),
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Agregar transacción")
        }
    }

    // Bottom sheet: nueva transacción
    if (mostrarNueva) {
        val tipo = if (esGasto) "GASTO" else "INGRESO"
        val categorias = remember(tipo, mostrarNueva) {
            viewModel.obtenerCategorias(tipo).map { it.nombre }
        }
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { mostrarNueva = false },
            sheetState = sheetState,
            containerColor = CardBackground,
            dragHandle = { SheetDragHandle() },
        ) {
            TransaccionFormSheet(
                tipo = tipo,
                categorias = categorias,
                titulo = "Registrar $tipo",
                accionTexto = "Guardar",
                onGuardar = { monto, categoria, descripcion ->
                    val fecha = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        .format(viewModel.fechaBase.time)
                    viewModel.agregarTransaccion(monto, tipo, categoria, fecha, descripcion)
                    mostrarNueva = false
                },
                onDismiss = { mostrarNueva = false },
            )
        }
    }

    // Bottom sheet: opciones (editar / eliminar)
    mostrarOpcionesPara?.let { transaccion ->
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { mostrarOpcionesPara = null },
            sheetState = sheetState,
            containerColor = CardBackground,
            dragHandle = { SheetDragHandle() },
        ) {
            OpcionesTransaccionSheet(
                onEditar = {
                    mostrarOpcionesPara = null
                    mostrarEditarPara = transaccion
                },
                onEliminar = {
                    mostrarOpcionesPara = null
                    viewModel.eliminarTransaccion(transaccion.id, esGasto)
                    indiceSeleccionado = null
                },
            )
        }
    }

    // Bottom sheet: editar
    mostrarEditarPara?.let { transaccion ->
        val categorias = remember(transaccion.id) {
            viewModel.obtenerCategorias(transaccion.type).map { it.nombre }
        }
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { mostrarEditarPara = null },
            sheetState = sheetState,
            containerColor = CardBackground,
            dragHandle = { SheetDragHandle() },
        ) {
            TransaccionFormSheet(
                tipo = transaccion.type,
                categorias = categorias,
                titulo = "Editar Registro",
                accionTexto = "Actualizar",
                montoInicial = transaccion.amount.toString(),
                descripcionInicial = transaccion.description ?: "",
                categoriaInicial = transaccion.category,
                onGuardar = { monto, categoria, descripcion ->
                    viewModel.editarTransaccion(
                        transaccion.id,
                        monto,
                        transaccion.type,
                        categoria,
                        transaccion.date,
                        descripcion,
                    )
                    mostrarEditarPara = null
                },
                onDismiss = { mostrarEditarPara = null },
            )
        }
    }
}

@Composable
private fun EstadoVacio() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Filled.Inbox,
            contentDescription = null,
            tint = Color(0xFF333333),
            modifier = Modifier.size(80.dp),
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Sin movimientos",
            color = TextPrimary,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Toca el botón + para registrar tu primer ingreso o gasto en este periodo.",
            color = TextMuted,
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
        )
    }
}
