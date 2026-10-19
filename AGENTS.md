# AGENTS.md — FinanceTrack

Guía del proyecto para agentes de IA (y para quien llegue nuevo). Léela completa antes de tocar código.

---

## 1. Qué es el proyecto

**FinanceTrack** es una app Android nativa para que estudiantes universitarios registren y analicen sus
finanzas personales: ingresos/gastos, categorías, gráficos y respaldo en la nube.

- **Proyecto académico** (Proyecto Integrador II – Ingeniería de Sistemas).
- App **solo Android**, en **Kotlin + Jetpack Compose**.
- Persistencia **local en SQLite** + **Firebase** (Auth y Firestore para respaldo/sincronización).

> El `README.md` menciona Room, ML Kit y un asistente NLP. **Nada de eso está implementado** todavía.
> El README está desactualizado; la fuente de verdad es este documento y el código.

---

## 2. Stack y versiones

| Elemento | Valor |
|---|---|
| Lenguaje | Kotlin 2.2.10 |
| UI | Jetpack Compose (BOM `2026.09.00`), Material 3 |
| Navegación | Navigation Compose `2.10.2` |
| Lifecycle | `2.11.0` (`viewmodel-compose`, `runtime-compose`) |
| Build | Android Gradle Plugin `9.4.1`, Gradle wrapper |
| `minSdk / targetSdk / compileSdk` | `24 / 36 / 37` |
| Java/Kotlin target | Java 11 |
| Firebase | BOM `32.8.0` (`firebase-auth`, `firebase-firestore`), plugin `google-services 4.4.1` |
| Tests | JUnit 4 |
| Módulos | Solo `:app` (`rootProject.name = "financeTrack"`) |

Archivos de configuración clave:
- `app/build.gradle.kts` — dependencias y config Android.
- `gradle/libs.versions.toml` — version catalog (usar `libs.*`, no versiones fijas).
- `app/google-services.json` — config de Firebase (está versionado).
- `.env` y `.aider*` están en `.gitignore`. **Nunca subir `.env`.**

---

## 3. Comandos (Windows / PowerShell, desde la raíz)

```powershell
# Compilar
.\gradlew.bat :app:compileDebugKotlin

# Tests unitarios (JVM, rápidos)  ->  corre esto siempre antes de terminar un cambio
.\gradlew.bat :app:testDebugUnitTest

# APK de debug
.\gradlew.bat :app:assembleDebug

# Instalar en dispositivo/emulador conectado (conserva datos con -r)
.\gradlew.bat :app:installDebug
# o:
& "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" install -r app\build\outputs\apk\debug\app-debug.apk
```

`adb` **no está en el `PATH`**; usar la ruta completa:
`$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe`.

```powershell
# Ver el log de crashes (lo más útil para depurar en dispositivo)
& $adb logcat -d -b crash
# Log completo filtrado por la app
& $adb logcat -d "*:E" | Select-String "financetrack|AndroidRuntime"
```

---

## 4. Arquitectura

**MVVM + Repository**, en capas. Flujo unidireccional de estado:

```
UI (Compose)  ──observa──►  ViewModel (StateFlow)  ──►  Repository  ──►  SQLite (FinanceTrackDbHelper)
                                                          └──────────►  Firestore (Perfil: respaldo/sync)
```

Reglas de la arquitectura:
- La **UI no accede a la base de datos**. Solo habla con el ViewModel.
- El **ViewModel** expone estado de **solo lectura** con `StateFlow` (`MutableStateFlow` privado + `asStateFlow()`).
- La **UI** consume con `collectAsStateWithLifecycle()` (import `androidx.lifecycle.compose`).
- El **Repository** es la única puerta a la persistencia.
- **No hay inyección de dependencias**: cada `AndroidViewModel` crea su propio `FinanceTrackDbHelper` +
  `TransactionRepository`. Es deuda técnica conocida (ver §9).

### Single-activity

- `LoginActivity` es la **launcher** (exported). Si ya hay sesión de Firebase, salta directo a `MainActivity`.
- `RegisterActivity` para crear cuenta.
- `MainActivity` (exported=false) monta `FinanceTrackApp()` (Compose) con el `NavHost` y la barra inferior.

### Navegación

`ui/navigation/FinanceTrackApp.kt` define el enum `Destino` y el `NavHost`:

| Ruta | Pantalla | ViewModel |
|---|---|---|
| `inicio` (start) | `InicioScreen` | `InicioViewModel` |
| `transacciones` | `TransaccionesScreen` | `TransaccionesViewModel` |
| `categorias` | `CategoriasScreen` | `CategoriasViewModel` |
| `perfil` | `PerfilScreen` | `PerfilViewModel` |

---

## 5. Mapa de directorios

```
app/src/main/java/com/example/financetrack/
├── MainActivity.kt                 # Activity Compose (app principal)
├── data/
│   ├── local/
│   │   └── financeTrackDbHelper.kt # SQLiteOpenHelper: esquema + migraciones (NO CRUD)
│   ├── model/
│   │   ├── transaction.kt          # Transaction (data class)
│   │   ├── category.kt             # Category (data class)
│   │   └── categoriaDesglose.kt    # CategoriaDesglose (para gráficos)
│   └── repository/
│       └── transactionRepository.kt# Único acceso a la BD (+ sync Firestore)
└── ui/
    ├── navigation/FinanceTrackApp.kt
    ├── components/
    │   ├── TransaccionItem.kt      # Fila de transacción + formatearMonto() + parseColorHex()
    │   ├── PieChart.kt             # Gráfico de dona 100% Compose (reemplaza MPAndroidChart)
    │   └── IconoUtils.kt           # painterIconoCategoria(): carga SEGURA de íconos
    ├── theme/
    │   ├── Color.kt                # Paleta (tema oscuro + dorado)
    │   └── Theme.kt                # FinanceTrackTheme (darkColorScheme)
    ├── inicio/        InicioScreen · inicioViewModel · calculadoraFinanzas · montoPositivo
    ├── transacciones/ TransaccionesScreen · TransaccionesSheets · transaccionesViewModel
    ├── categorias/    CategoriasScreen · categoriasViewModel · validadorCategoria
    ├── perfil/        PerfilScreen · PerfilViewModel
    └── login/         LoginActivity · LoginScreen · LoginViewModel
                       RegisterActivity · RegisterScreen · RegisterViewModel · validadorAutenticacion
```

Tests: `app/src/test/java/com/example/financetrack/` (JUnit4): `calculadoraFinanzasTest`,
`authValidacionTest`, `validacionTransaccionTest`, `validadorCategoriaTest`.

---

## 6. Modelo de datos y esquema SQLite

`FinanceTrackDbHelper` (base `FinanceTrack.db`). **La versión actual es `DATABASE_VERSION = 2`.**

```sql
transactions(
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  amount REAL,
  type TEXT,          -- "GASTO" | "INGRESO"
  category TEXT,      -- nombre de la categoría (coincide con categories.nombre)
  date TEXT,          -- "yyyy-MM-dd" (se normaliza al leer)
  hora TEXT,          -- "HH:mm"
  description TEXT
)

categories(
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  nombre TEXT,
  tipo TEXT,          -- "GASTO" | "INGRESO"
  color TEXT,         -- hexadecimal, ej. "#F44336"
  icono INTEGER       -- resource id de la app, ej. R.drawable.ic_dinero
)
```

Categorías por defecto (solo si la tabla está vacía): `Alimentación`, `Transporte` (GASTO) y `Salario` (INGRESO).

### Firestore (respaldo en la nube)

```
usuarios/{uid}/transacciones/{id}   -> { amount, type, category, date, hora, description }
usuarios/{uid}/categorias/{id}      -> { nombre, tipo, color, icono }
```

- **Respaldar** (`PerfilViewModel.respaldar`): sube local → Firestore.
- **Sincronizar** (`PerfilViewModel.sincronizar`): **borra lo local** y lo reemplaza por lo de Firestore.
  ⚠️ Si el respaldo en la nube está vacío, se pierden los datos locales.
- **Cerrar sesión**: borra toda la BD local.

---

## 7. Reglas críticas (¡leer antes de tocar datos o UI!)

Estas reglas existen porque ya provocaron bugs reales en producción. **No las rompas.**

1. **Migraciones NO destructivas.** Si cambias el esquema, **sube `DATABASE_VERSION`** e implementa
   `onUpgrade()` agregando/cambiando cosas **sin borrar la tabla `transactions`**.
   *Historial del bug:* en el MVP se añadió la columna `hora` y la tabla `categories` **sin subir la versión**,
   así que las instalaciones antiguas nunca migraron y las transacciones "desaparecían" (y crasheaban).

2. **Leer columnas por NOMBRE, nunca por posición.** Usa `cursor.getColumnIndex("columna")` y valores por
   defecto (`hora ?: "00:00"`, `description ?: ""`). El esquema viejo no tenía `hora` y leía posiciones fuera de rango.

3. **Íconos de categoría:** renderiza SIEMPRE con `painterIconoCategoria()` de `ui/components/IconoUtils.kt`.
   **No uses `painterResource(id = icono)`** con IDs dinámicos ni `android.R.drawable.*`:
   Compose lanza `IllegalArgumentException: Only VectorDrawables and rasterized asset types are supported`
   y cierra la app. `IconoUtils` carga vía el `Drawable` nativo y lo convierte a `BitmapPainter`.

4. **Fechas:** el formato canónico es `yyyy-MM-dd`. Al leer, `TransactionRepository.normalizarFecha()`
   convierte formatos heredados (`dd/MM/yyyy`, etc.). El filtro de `TransaccionesViewModel.parseFecha()`
   tolera varios formatos y **no debe lanzar excepción** con fechas raras.

5. **Estado:** los ViewModels exponen `StateFlow`. La UI usa `collectAsStateWithLifecycle()`.
   No reintroducir `LiveData` / `observeAsState` (la dependencia `runtime-livedata` ya se eliminó).

---

## 8. Convenciones del código

- **Idioma mezclado**: código/estructura en inglés, dominio y textos de UI en español. Mantener el estilo del
  archivo que estés tocando.
- Algunos **objetos utilitarios** van en minúscula e infieren su uso por nombre:
  `calculadoraFinanzas`, `validadorCategoria`, `validacionTransaccion`, `ValidadorAutenticacion`.
- Cálculos de negocio puros se extraen para poder testearlos (ej. `calculadoraFinanzas.calcularSaldoDeTransacciones`).
- Colores e íconos se inyectan a las transacciones en los ViewModels haciendo `copy(colorCategory=…, iconCategory=…)`.
- Tema **oscuro** con acento dorado (`GoldPrimary`). Usar colores de `ui/theme/Color.kt`, no valores sueltos.

---

## 9. Deuda técnica y pendientes (roadmap)

| # | Tema | Detalle |
|---|---|---|
| 1 | README desactualizado | Dice Room / ML Kit / NLP, que no existen. |
| 2 | Sin inyección de dependencias | Cada ViewModel instancia su `FinanceTrackDbHelper`/`Repository`. Candidato: DI manual (AppContainer) y luego Hilt. |
| 3 | Datos no separados por usuario | Las tablas no tienen `userId`; dependen del respaldo/sync. Cerrar sesión borra todo lo local. Requiere migración de esquema. |
| 4 | Vista `Transacciones` filtra en memoria | `getAllTransactions()` trae todo y el filtrado por tipo/fecha se hace en Kotlin, no con `WHERE`. Aceptable con pocos datos. |
| 5 | Funciones IA no implementadas | ML Kit (OCR de comprobantes) y NLP no están. Si se migra a Room, hacerlo con migraciones no destructivas. |
| 6 | Filtro por defecto "Mes" | Es por diseño: para ver histórico hay que cambiar el período a "Año" o navegar con las flechas. No es un bug. |

---

## 10. Checklist antes de dar por terminado un cambio

1. `.\gradlew.bat :app:compileDebugKotlin` compila sin errores.
2. `.\gradlew.bat :app:testDebugUnitTest` pasa.
3. Si tocaste esquema/DB → seguiste las reglas de §7 (migración no destructiva, versión subida).
4. Si tocaste UI con íconos → usaste `painterIconoCategoria()`.
5. Si tocaste estado → sigues con `StateFlow` + `collectAsStateWithLifecycle()`.
6. Probaste en dispositivo/emulador y revisaste `adb logcat -d -b crash` (vacío).

---

## 11. Contexto de repositorio

- Repo: `https://github.com/gabrielMenachoPadilla/financeTrack` (antes `GabrielGndl/financeTrack`).
- Rama principal: `main`.
- El backend de nube (Firestore) requiere `app/google-services.json` y conexión a internet.
