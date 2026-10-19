package com.example.financetrack.ui.login

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.financetrack.ui.theme.DarkBackground
import com.example.financetrack.ui.theme.GoldPrimary
import com.example.financetrack.ui.theme.TextPrimary
import com.example.financetrack.ui.theme.TextSecondary
import com.example.financetrack.ui.theme.YellowAccent

@Composable
fun RegisterScreen(
    onRegistrado: () -> Unit,
    onIrALogin: () -> Unit,
    viewModel: RegisterViewModel = viewModel(),
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsStateWithLifecycle()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var verPassword by remember { mutableStateOf(false) }

    LaunchedEffect(state.error) {
        state.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.consumirError()
        }
    }
    LaunchedEffect(state.exito) {
        if (state.exito) onRegistrado()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(rememberScrollState())
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "Crear Cuenta",
            color = TextPrimary,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 40.dp),
        )

        AuthField(
            value = email,
            onValueChange = { email = it },
            label = "Correo electrónico",
            keyboardType = KeyboardType.Email,
        )
        Spacer(Modifier.height(16.dp))

        AuthField(
            value = password,
            onValueChange = { password = it },
            label = "Contraseña",
            keyboardType = KeyboardType.Password,
            esPassword = true,
            verPassword = verPassword,
            onTogglePassword = { verPassword = !verPassword },
        )
        Spacer(Modifier.height(16.dp))

        AuthField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = "Confirmar Contraseña",
            keyboardType = KeyboardType.Password,
            esPassword = true,
            verPassword = verPassword,
            onTogglePassword = { verPassword = !verPassword },
        )
        Spacer(Modifier.height(32.dp))

        if (state.cargando) {
            CircularProgressIndicator(color = YellowAccent, modifier = Modifier.size(36.dp))
            Spacer(Modifier.height(16.dp))
        }

        Button(
            onClick = { viewModel.registrar(email, password, confirmPassword) },
            enabled = !state.cargando,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = YellowAccent,
                contentColor = Color(0xFF121212),
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
        ) {
            Text(
                text = if (state.cargando) "Registrando..." else "Registrarse",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
            )
        }
        Spacer(Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("¿Ya tienes cuenta? ", color = TextSecondary)
            Text(
                text = "Inicia Sesión",
                color = GoldPrimary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clickable { onIrALogin() }
                    .padding(4.dp),
            )
        }
    }
}
