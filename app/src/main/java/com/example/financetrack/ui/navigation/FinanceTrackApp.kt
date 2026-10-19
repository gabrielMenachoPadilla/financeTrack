package com.example.financetrack.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.financetrack.ui.categorias.CategoriasScreen
import com.example.financetrack.ui.inicio.InicioScreen
import com.example.financetrack.ui.perfil.PerfilScreen
import com.example.financetrack.ui.theme.CardBackground
import com.example.financetrack.ui.theme.DarkBackground
import com.example.financetrack.ui.theme.GoldPrimary
import com.example.financetrack.ui.theme.TextMuted
import com.example.financetrack.ui.transacciones.TransaccionesScreen

enum class Destino(val route: String, val label: String, val icon: ImageVector) {
    Inicio("inicio", "Inicio", Icons.Filled.AccountBalanceWallet),
    Transacciones("transacciones", "Transacciones", Icons.Filled.SwapVert),
    Categorias("categorias", "Categorías", Icons.Filled.Category),
    Perfil("perfil", "Perfil", Icons.Filled.Person),
}

@Composable
fun FinanceTrackApp() {
    val navController = rememberNavController()

    Scaffold(
        containerColor = DarkBackground,
        bottomBar = {
            NavigationBar(containerColor = CardBackground) {
                val backStackEntry by navController.currentBackStackEntryAsState()
                val rutaActual = backStackEntry?.destination?.route
                Destino.entries.forEach { destino ->
                    NavigationBarItem(
                        selected = rutaActual == destino.route,
                        onClick = {
                            if (rutaActual != destino.route) {
                                navController.navigate(destino.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = { Icon(destino.icon, contentDescription = destino.label) },
                        label = { Text(destino.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = DarkBackground,
                            selectedTextColor = GoldPrimary,
                            indicatorColor = GoldPrimary,
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted,
                        ),
                    )
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Destino.Inicio.route,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(Destino.Inicio.route) { InicioScreen() }
            composable(Destino.Transacciones.route) { TransaccionesScreen() }
            composable(Destino.Categorias.route) { CategoriasScreen() }
            composable(Destino.Perfil.route) { PerfilScreen() }
        }
    }
}
