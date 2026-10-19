package com.example.financetrack.ui.login

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.financetrack.MainActivity
import com.example.financetrack.ui.theme.FinanceTrackTheme
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Si ya hay una sesión activa, entrar directo a la app
        if (FirebaseAuth.getInstance().currentUser != null) {
            irAMainActivity()
            return
        }

        setContent {
            FinanceTrackTheme {
                LoginScreen(
                    onIngresar = { irAMainActivity() },
                    onIrARegistro = {
                        startActivity(Intent(this, RegisterActivity::class.java))
                    },
                )
            }
        }
    }

    private fun irAMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
