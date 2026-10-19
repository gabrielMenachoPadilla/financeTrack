package com.example.financetrack.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val FinanceTrackColorScheme = darkColorScheme(
    primary = GoldPrimary,
    onPrimary = DarkBackground,
    secondary = YellowAccent,
    onSecondary = DarkBackground,
    background = DarkBackground,
    onBackground = TextPrimary,
    surface = CardBackground,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = TextPrimary,
    outline = DividerColor,
    error = RedExpense,
    onError = TextPrimary,
)

@Composable
fun FinanceTrackTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = FinanceTrackColorScheme,
        content = content
    )
}
