package com.example.financetrack.ui.perfil

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.financetrack.ui.login.LoginActivity
import com.example.financetrack.ui.theme.CardBackground
import com.example.financetrack.ui.theme.DividerColor
import com.example.financetrack.ui.theme.GoldPrimary
import com.example.financetrack.ui.theme.GreenIncomeDark
import com.example.financetrack.ui.theme.RedExpense
import com.example.financetrack.ui.theme.TextMuted
import com.example.financetrack.ui.theme.TextPrimary

@Composable
fun PerfilScreen(
    modifier: Modifier = Modifier,
    viewModel: PerfilViewModel = viewModel(),
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // Mostrar mensajes (toasts) y navegar al cerrar sesión
    LaunchedEffect(state.mensaje) {
        state.mensaje?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.consumirMensaje()
        }
    }
    LaunchedEffect(state.sesionCerrada) {
        if (state.sesionCerrada) {
            val intent = Intent(context, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            context.startActivity(intent)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
    ) {
        // Encabezado
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 28.dp, bottom = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                imageVector = Icons.Filled.Person,
                contentDescription = null,
                tint = GoldPrimary,
                modifier = Modifier.size(90.dp),
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Mi Perfil",
                color = TextPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(4.dp))
            Text(text = state.correo, color = TextMuted, fontSize = 14.sp)
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardBackground),
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                AccionPerfil(
                    icono = Icons.Filled.CloudUpload,
                    tintIcono = GreenIncomeDark,
                    texto = if (state.respaldando) "Subiendo a la nube..." else "Respaldar datos en la nube",
                    habilitado = !state.respaldando,
                    onClick = { viewModel.respaldar() },
                )
                HorizontalDivider(color = DividerColor, modifier = Modifier.padding(horizontal = 16.dp))
                AccionPerfil(
                    icono = Icons.Filled.CloudDownload,
                    tintIcono = GoldPrimary,
                    texto = if (state.sincronizando) "Descargando..." else "Sincronizar desde la nube",
                    habilitado = !state.sincronizando,
                    onClick = { viewModel.sincronizar() },
                )
                HorizontalDivider(color = DividerColor, modifier = Modifier.padding(horizontal = 16.dp))
                AccionPerfil(
                    icono = Icons.AutoMirrored.Filled.Logout,
                    tintIcono = RedExpense,
                    texto = "Cerrar Sesión",
                    colorTexto = RedExpense,
                    habilitado = true,
                    onClick = { viewModel.cerrarSesion() },
                )
            }
        }
    }
}

@Composable
private fun AccionPerfil(
    icono: ImageVector,
    tintIcono: Color,
    texto: String,
    habilitado: Boolean,
    colorTexto: Color = TextPrimary,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .then(if (habilitado) Modifier.clickable { onClick() } else Modifier)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icono, contentDescription = null, tint = tintIcono, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(16.dp))
        Text(
            text = texto,
            color = colorTexto,
            fontSize = 16.sp,
            fontWeight = if (colorTexto == RedExpense) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.weight(1f),
        )
        Icon(
            Icons.Filled.ChevronRight,
            contentDescription = null,
            tint = Color(0xFF444444),
            modifier = Modifier.size(20.dp),
        )
    }
}
