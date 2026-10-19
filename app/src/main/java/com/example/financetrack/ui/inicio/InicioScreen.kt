package com.example.financetrack.ui.inicio

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.financetrack.ui.components.PieChart
import com.example.financetrack.ui.components.PieSlice
import com.example.financetrack.ui.components.TransaccionItem
import com.example.financetrack.ui.components.formatearMonto
import com.example.financetrack.ui.theme.CardBackground
import com.example.financetrack.ui.theme.GreenIncome
import com.example.financetrack.ui.theme.RedExpense
import com.example.financetrack.ui.theme.TextPrimary
import com.example.financetrack.ui.theme.TextSecondary

@Composable
fun InicioScreen(
    modifier: Modifier = Modifier,
    viewModel: InicioViewModel = viewModel(),
    onIrATransacciones: () -> Unit = {},
) {
    val saldo by viewModel.saldoDisponible.collectAsStateWithLifecycle()
    val ingresos by viewModel.totalIngresos.collectAsStateWithLifecycle()
    val gastos by viewModel.totalGastos.collectAsStateWithLifecycle()
    val recientes by viewModel.transaccionesRecientes.collectAsStateWithLifecycle()
    val datosGrafico by viewModel.datosGrafico.collectAsStateWithLifecycle()

    // Recargar cada vez que el usuario vuelve a esta pantalla (equivalente al onResume anterior)
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.cargarResumen()
    }

    val slices = datosGrafico.map { dato ->
        PieSlice(
            label = dato.nombre,
            value = dato.monto.toFloat(),
            color = if (dato.nombre == "Ingresos") GreenIncome else RedExpense,
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        Text(
            text = "Mi Billetera",
            color = TextPrimary,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp),
        )

        // Saldo disponible
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardBackground),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("Saldo Disponible", color = TextSecondary, fontSize = 14.sp)
                Spacer(Modifier.height(8.dp))
                Text(
                    text = formatearMonto(saldo),
                    color = TextPrimary,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        // Ingresos / Gastos
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
        ) {
            StatCard(
                titulo = "Ingresos",
                valor = ingresos,
                colorTitulo = GreenIncome,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
            )
            StatCard(
                titulo = "Gastos",
                valor = gastos,
                colorTitulo = RedExpense,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp),
            )
        }

        Text(
            text = "Ingresos vs Gastos",
            color = TextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp),
        )

        if (slices.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp),
                contentAlignment = Alignment.Center,
            ) {
                PieChart(
                    slices = slices,
                    modifier = Modifier.fillMaxSize(),
                    centerText = "Balance",
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                slices.forEachIndexed { index, slice ->
                    if (index > 0) Spacer(Modifier.width(16.dp))
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(slice.color),
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(slice.label, color = TextSecondary, fontSize = 12.sp)
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Text(
            text = "Actividad Reciente",
            color = TextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp),
        )

        if (recientes.isEmpty()) {
            Text(
                text = "Aún no tienes movimientos registrados.",
                color = TextSecondary,
                fontSize = 14.sp,
            )
        } else {
            recientes.forEach { transaccion ->
                TransaccionItem(
                    transaction = transaccion,
                    onClick = onIrATransacciones,
                )
            }
        }
    }
}

@Composable
private fun StatCard(
    titulo: String,
    valor: Double,
    colorTitulo: Color,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = titulo,
                color = colorTitulo,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = formatearMonto(valor),
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
            )
        }
    }
}
