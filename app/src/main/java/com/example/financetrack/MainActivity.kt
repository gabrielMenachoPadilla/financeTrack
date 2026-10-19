package com.example.financetrack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.financetrack.ui.navigation.FinanceTrackApp
import com.example.financetrack.ui.theme.FinanceTrackTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FinanceTrackTheme {
                FinanceTrackApp()
            }
        }
    }
}
