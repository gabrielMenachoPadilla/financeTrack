package com.example.financetrack.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.financetrack.data.model.Transaction
import com.example.financetrack.ui.theme.CardBackground
import com.example.financetrack.ui.theme.GreenIncome
import com.example.financetrack.ui.theme.SurfaceVariantDark
import com.example.financetrack.ui.theme.TextPrimary
import com.example.financetrack.ui.theme.TextSecondary

/** Formatea un monto como "$ 1234.56". */
fun formatearMonto(valor: Double): String = String.format(java.util.Locale.US, "$ %.2f", valor)

/** Convierte un color hexadecimal ("#RRGGBB") a [Color], con respaldo seguro. */
fun parseColorHex(hex: String?, fallback: Color = SurfaceVariantDark): Color = try {
    if (hex.isNullOrBlank()) fallback else Color(android.graphics.Color.parseColor(hex))
} catch (e: Exception) {
    fallback
}

@Composable
fun TransaccionItem(
    transaction: Transaction,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 6.dp)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Círculo con el ícono de la categoría
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(parseColorHex(transaction.colorCategory)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterIconoCategoria(transaction.iconCategory),
                    contentDescription = null,
                    tint = TextPrimary,
                    modifier = Modifier.size(20.dp),
                )
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.category,
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                )
                Row {
                    Text(
                        text = "${transaction.date} • ${transaction.hora}",
                        color = TextSecondary,
                        fontSize = 12.sp,
                    )
                    if (!transaction.description.isNullOrEmpty()) {
                        Text(
                            text = " | ${transaction.description}",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }

            val esIngreso = transaction.type == "INGRESO"
            Text(
                text = (if (esIngreso) "+" else "-") + " " + formatearMonto(transaction.amount),
                color = if (esIngreso) GreenIncome else TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}
