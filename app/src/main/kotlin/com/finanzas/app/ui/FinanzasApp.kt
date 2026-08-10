package com.finanzas.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.finanzas.app.FinanzasApplication
import com.finanzas.core.dominio.AjusteValuacion
import com.finanzas.core.dominio.Categoria
import com.finanzas.core.dominio.Cuenta
import com.finanzas.core.dominio.Dinero
import com.finanzas.core.dominio.Egreso
import com.finanzas.core.dominio.Ingreso
import com.finanzas.core.dominio.Moneda
import com.finanzas.core.dominio.Presupuesto
import com.finanzas.core.dominio.TipoCuenta
import com.finanzas.core.dominio.TipoTransferencia
import com.finanzas.core.dominio.Transaccion
import java.time.LocalDate
import java.time.YearMonth
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.Locale

private enum class AppTab(val title: String) {
    INICIO("Inicio"),
    MOVIMIENTOS("Movimientos"),
    INVERSIONES("Inversiones"),
    PRESUPUESTOS("Presupuestos")
}

private enum class DialogKind {
    NONE,
    MOVIMIENTO,
    TRANSFERENCIA,
    AJUSTE,
    CUENTA,
    CATEGORIA,
    PRESUPUESTO
}

private val transferTypes = listOf(
    TipoTransferencia.NORMAL,
    TipoTransferencia.APORTE_INVERSION,
    TipoTransferencia.RESCATE_INVERSION
)

@Composable
fun FinanzasApp() {
    val application = androidx.compose.ui.platform.LocalContext.current.applicationContext as FinanzasApplication
    val viewModel: FinanzasViewModel = viewModel(factory = FinanzasViewModel.factory(application.repository))
    val state by viewModel.state.collectAsStateWithLifecycle()
    var selectedTab by rememberSaveable { mutableStateOf(AppTab.INICIO.name) }
    var dialog by rememberSaveable { mutableStateOf(DialogKind.NONE.name) }
    var movimientoIngreso by rememberSaveable { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.error) {
        state.error?.let { snackbarHostState.showSnackbar(it) }
    }

    val tab = AppTab.valueOf(selectedTab)
    Scaffold(
        modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar {
                AppTab.entries.forEach { item ->
                    NavigationBarItem(
                        selected = tab == item,
                        onClick = { selectedTab = item.name },
                        icon = {
                            Icon(
                                imageVector = when (item) {
                                    AppTab.INICIO -> Icons.Outlined.Assessment
                                    AppTab.MOVIMIENTOS -> Icons.Outlined.Payments
                                    AppTab.INVERSIONES -> Icons.Outlined.Savings
                                    AppTab.PRESUPUESTOS -> Icons.Outlined.MoreHoriz
                                },
                                contentDescription = item.title
                            )
                        },
                        label = { Text(item.title) }
                    )
                }
            }
        },
        floatingActionButton = {
            if (state.cuentas.isNotEmpty() && tab != AppTab.INVERSIONES) {
                FloatingActionButton(onClick = { dialog = DialogKind.MOVIMIENTO.name }) {
                    Icon(Icons.Outlined.Add, contentDescription = "Nuevo movimiento")
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                state.cargando -> LoadingScreen()
                state.cuentas.isEmpty() -> EmptyAccountsScreen { dialog = DialogKind.CUENTA.name }
                tab == AppTab.INICIO -> InicioScreen(
                    state = state,
                    onGasto = { movimientoIngreso = false; dialog = DialogKind.MOVIMIENTO.name },
                    onIngreso = { movimientoIngreso = true; dialog = DialogKind.MOVIMIENTO.name },
                    onTransferencia = { dialog = DialogKind.TRANSFERENCIA.name },
                    onCuenta = { dialog = DialogKind.CUENTA.name },
                    onCategoria = { dialog = DialogKind.CATEGORIA.name }
                )
                tab == AppTab.MOVIMIENTOS -> MovimientosScreen(state)
                tab == AppTab.INVERSIONES -> InversionesScreen(
                    state = state,
                    onTransferencia = { dialog = DialogKind.TRANSFERENCIA.name },
                    onAjuste = { dialog = DialogKind.AJUSTE.name }
                )
                else -> PresupuestosScreen(state) { dialog = DialogKind.PRESUPUESTO.name }
            }
        }
    }

    when (DialogKind.valueOf(dialog)) {
        DialogKind.MOVIMIENTO -> MovimientoDialog(state, viewModel, movimientoIngreso) { dialog = DialogKind.NONE }
        DialogKind.TRANSFERENCIA -> TransferenciaDialog(state, viewModel) { dialog = DialogKind.NONE }
        DialogKind.AJUSTE -> AjusteDialog(state, viewModel) { dialog = DialogKind.NONE }
        DialogKind.CUENTA -> CuentaDialog(viewModel) { dialog = DialogKind.NONE }
        DialogKind.CATEGORIA -> CategoriaDialog(viewModel) { dialog = DialogKind.NONE }
        DialogKind.PRESUPUESTO -> PresupuestoDialog(state, viewModel) { dialog = DialogKind.NONE }
        DialogKind.NONE -> Unit
    }
}

@Composable
private fun LoadingScreen() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun EmptyAccountsScreen(onCreate: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(28.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Outlined.AccountBalance, null, Modifier.size(56.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(18.dp))
        Text("Empezá por agregar una cuenta", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text("Después vas a poder registrar gastos, ingresos y movimientos de inversión.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(24.dp))
        Button(onClick = onCreate) { Text("Agregar cuenta") }
    }
}

@Composable
private fun InicioScreen(
    state: FinanzasUiState,
    onGasto: () -> Unit,
    onIngreso: () -> Unit,
    onTransferencia: () -> Unit,
    onCuenta: () -> Unit,
    onCategoria: () -> Unit
) {
    val cuentasArs = state.cuentas.filter { it.moneda == Moneda.ARS }
    val cuentasUsd = state.cuentas.filter { it.moneda == Moneda.USD }
    val egresos = state.cuentas.flatMap { it.transacciones() }.filterIsInstance<Egreso>()
    val presupuesto = state.presupuestos.firstOrNull { it.mes == YearMonth.now() }
    val restante = presupuesto?.restante(egresos)
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(top = 22.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            Column {
                Text("Hola", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                Text("Tu resumen", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            }
        }
        item {
            BalanceCard("Disponible en ARS", formatMoney(saldoTotal(cuentasArs, Moneda.ARS)))
        }
        if (cuentasUsd.isNotEmpty()) {
            item { BalanceCard("Disponible en USD", formatMoney(saldoTotal(cuentasUsd, Moneda.USD)), accent = true) }
        }
        item {
            QuickActions(onGasto, onIngreso, onTransferencia, onCuenta, onCategoria)
        }
        item {
            SectionTitle("Tus cuentas")
        }
        items(state.cuentas, key = { it.id }) { cuenta -> AccountCard(cuenta) }
        if (presupuesto != null && restante != null) {
            item { BudgetCard(presupuesto, restante) }
        }
    }
}

@Composable
private fun BalanceCard(title: String, amount: String, accent: Boolean = false) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (accent) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.primaryContainer
        ),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(Modifier.fillMaxWidth().padding(22.dp)) {
            Text(title, color = MaterialTheme.colorScheme.onPrimaryContainer)
            Spacer(Modifier.height(8.dp))
            Text(amount, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
            Spacer(Modifier.height(10.dp))
            Text("Saldo registrado", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = .7f))
        }
    }
}

@Composable
private fun QuickActions(onGasto: () -> Unit, onIngreso: () -> Unit, onTransferencia: () -> Unit, onCuenta: () -> Unit, onCategoria: () -> Unit) {
    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        QuickAction(Icons.Outlined.ArrowDownward, "Gasto", onGasto)
        QuickAction(Icons.Outlined.ArrowUpward, "Ingreso", onIngreso)
        QuickAction(Icons.Outlined.SwapHoriz, "Mover", onTransferencia)
        QuickAction(Icons.Outlined.AccountBalance, "Cuenta", onCuenta)
        QuickAction(Icons.Outlined.MoreHoriz, "Categoría", onCategoria)
    }
}

@Composable
private fun QuickAction(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(76.dp)) {
        IconButton(onClick = onClick, modifier = Modifier.size(52.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant)) {
            Icon(icon, label, tint = MaterialTheme.colorScheme.primary)
        }
        Text(label, style = MaterialTheme.typography.labelSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun AccountCard(cuenta: Cuenta) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .45f))) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(if (cuenta.tipo == TipoCuenta.INVERSION) Icons.Outlined.Savings else Icons.Outlined.AccountBalance, null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(cuenta.nombre, fontWeight = FontWeight.SemiBold)
                Text(if (cuenta.tipo == TipoCuenta.INVERSION) "Inversión" else "Operativa", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(formatMoney(cuenta.saldo()), fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun MovimientosScreen(state: FinanzasUiState) {
    val filas = state.cuentas.flatMap { cuenta -> cuenta.transacciones().map { cuenta to it } }.sortedByDescending { it.second.fecha }
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(top = 22.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Movimientos", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text("Todo lo registrado en tus cuentas", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (filas.isEmpty()) {
            item { Text("Todavía no hay movimientos.", modifier = Modifier.padding(top = 30.dp), color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
        items(filas, key = { "${it.first.id}-${it.second.id}" }) { (cuenta, transaccion) -> MovementRow(cuenta, transaccion) }
    }
}

@Composable
private fun MovementRow(cuenta: Cuenta, transaccion: Transaccion) {
    val isPositive = when (transaccion) {
        is Ingreso, is com.finanzas.core.dominio.TransferIngreso -> true
        else -> false
    }
    val title = when (transaccion) {
        is Ingreso -> "Ingreso"
        is Egreso -> transaccion.categoria?.nombre ?: "Egreso"
        is AjusteValuacion -> "Ajuste de valuación"
        is com.finanzas.core.dominio.TransferIngreso -> "Entrada por ${transaccion.transferencia.tipo.label()}"
        is com.finanzas.core.dominio.TransferEgreso -> "Salida por ${transaccion.transferencia.tipo.label()}"
        else -> "Movimiento"
    }
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .35f))) {
        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(if (isPositive) Icons.Outlined.ArrowUpward else Icons.Outlined.ArrowDownward, null, tint = if (isPositive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.SemiBold)
                Text("${cuenta.nombre} · ${transaccion.fecha}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text((if (isPositive) "+" else "-") + formatMoney(dineroAbsoluto(transaccion.monto)), fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun InversionesScreen(state: FinanzasUiState, onTransferencia: () -> Unit, onAjuste: () -> Unit) {
    val inversiones = state.cuentas.filter { it.tipo == TipoCuenta.INVERSION }
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 20.dp), contentPadding = androidx.compose.foundation.layout.PaddingValues(top = 22.dp, bottom = 100.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Text("Inversiones", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text("Capital y valuación, separados del dinero para gastar", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        items(inversiones, key = { it.id }) { cuenta ->
            Card(shape = RoundedCornerShape(22.dp)) {
                Column(Modifier.padding(18.dp)) {
                    Text(cuenta.nombre, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(formatMoney(cuenta.saldo()), style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = onTransferencia, modifier = Modifier.weight(1f)) { Text("Aportar / rescatar") }
                        Button(onClick = onAjuste, modifier = Modifier.weight(1f)) { Text("Actualizar valor") }
                    }
                }
            }
        }
        if (inversiones.isEmpty()) item { Text("No tenés cuentas de inversión todavía.", color = MaterialTheme.colorScheme.onSurfaceVariant) }
    }
}

@Composable
private fun PresupuestosScreen(state: FinanzasUiState, onNuevoPresupuesto: () -> Unit) {
    val egresos = state.cuentas.flatMap { it.transacciones() }.filterIsInstance<Egreso>()
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 20.dp), contentPadding = androidx.compose.foundation.layout.PaddingValues(top = 22.dp, bottom = 100.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Presupuestos", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Text("Cómo viene tu mes", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                TextButton(onClick = onNuevoPresupuesto) { Text("Nuevo") }
            }
        }
        items(state.presupuestos, key = { it.id }) { presupuesto -> BudgetCard(presupuesto, presupuesto.restante(egresos)) }
        if (state.presupuestos.isEmpty()) item { Text("Todavía no configuraste presupuestos.", color = MaterialTheme.colorScheme.onSurfaceVariant) }
    }
}

@Composable
private fun BudgetCard(presupuesto: Presupuesto, restante: Dinero) {
    val progress = if (presupuesto.limite.importe.signum() == 0) {
        0f
    } else {
        restante.importe
            .divide(presupuesto.limite.importe, 4, RoundingMode.HALF_UP)
            .coerceIn(BigDecimal.ZERO, BigDecimal.ONE)
            .toFloat()
    }
    Card {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(presupuesto.categoria.nombre, fontWeight = FontWeight.SemiBold)
                    Text(presupuesto.mes.toString(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text(formatMoney(restante), fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(12.dp))
            androidx.compose.material3.LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(5.dp))
            Text("de ${formatMoney(presupuesto.limite)} disponibles", style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
}

@Composable
private fun FormDialog(title: String, onDismiss: () -> Unit, content: @Composable () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { content() },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cerrar") } }
    )
}

@Composable
private fun MovimientoDialog(state: FinanzasUiState, viewModel: FinanzasViewModel, initialIngreso: Boolean, onDismiss: () -> Unit) {
    var ingreso by rememberSaveable(initialIngreso) { mutableStateOf(initialIngreso) }
    var accountId by rememberSaveable { mutableStateOf(state.cuentas.firstOrNull()?.id.orEmpty()) }
    var categoryId by rememberSaveable { mutableStateOf(state.categorias.firstOrNull()?.id.orEmpty()) }
    var importe by rememberSaveable { mutableStateOf("") }
    var fecha by rememberSaveable { mutableStateOf(LocalDate.now().toString()) }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    val cuenta = state.cuentas.firstOrNull { it.id == accountId }
    FormDialog(if (ingreso) "Nuevo ingreso" else "Nuevo gasto", onDismiss) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChipButton("Gasto", !ingreso) { ingreso = false }
                FilterChipButton("Ingreso", ingreso) { ingreso = true }
            }
            DropdownField("Cuenta", state.cuentas, accountId, { it.id }, { it.nombre }) { accountId = it }
            if (!ingreso) DropdownField("Categoría", state.categorias, categoryId, { it.id }, { it.nombre }, allowEmpty = true) { categoryId = it }
            OutlinedTextField(importe, { importe = it }, label = { Text("Importe") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth())
            OutlinedTextField(fecha, { fecha = it }, label = { Text("Fecha (AAAA-MM-DD)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            error?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
            Button(
                onClick = {
                    if (cuenta == null) {
                        error = "Elegí una cuenta"
                        return@Button
                    }
                    val amount = runCatching { Dinero.de(importe.trim().replace(',', '.'), cuenta.moneda) }.getOrNull()
                    if (amount == null || amount.importe <= BigDecimal.ZERO) {
                        error = "Ingresá un importe positivo"
                        return@Button
                    }
                    val date = runCatching { LocalDate.parse(fecha) }.getOrNull()
                    if (date == null) {
                        error = "La fecha debe tener formato AAAA-MM-DD"
                        return@Button
                    }
                    error = null
                    val done: (Boolean) -> Unit = { if (it) onDismiss() }
                    if (ingreso) viewModel.registrarIngreso(cuenta, importe, date, done)
                    else viewModel.registrarEgreso(cuenta, state.categorias.firstOrNull { it.id == categoryId }, importe, date, done)
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Guardar") }
        }
    }
}

@Composable
private fun TransferenciaDialog(state: FinanzasUiState, viewModel: FinanzasViewModel, onDismiss: () -> Unit) {
    var tipoKey by rememberSaveable { mutableStateOf(TipoTransferencia.NORMAL.key()) }
    var origenId by rememberSaveable { mutableStateOf(state.cuentas.firstOrNull()?.id.orEmpty()) }
    var destinoId by rememberSaveable { mutableStateOf(state.cuentas.getOrNull(1)?.id.orEmpty()) }
    var importe by rememberSaveable { mutableStateOf("") }
    var fecha by rememberSaveable { mutableStateOf(LocalDate.now().toString()) }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    val tipo = transferTypes.firstOrNull { it.key() == tipoKey } ?: TipoTransferencia.NORMAL
    val origenes = state.cuentas.filter { it.tipo == tipo.originType() }
    val destinos = state.cuentas.filter { it.tipo == tipo.destinationType() }
    val origen = origenes.firstOrNull { it.id == origenId } ?: origenes.firstOrNull()
    val destino = destinos.firstOrNull { it.id == destinoId } ?: destinos.firstOrNull()
    FormDialog("Mover dinero", onDismiss) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            DropdownField("Tipo", transferTypes, tipo.key(), { it.key() }, { it.label() }) { tipoKey = it }
            DropdownField("Origen", origenes, origen?.id.orEmpty(), { it.id }, { it.nombre }) { origenId = it }
            DropdownField("Destino", destinos, destino?.id.orEmpty(), { it.id }, { it.nombre }) { destinoId = it }
            OutlinedTextField(importe, { importe = it }, label = { Text("Importe") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth())
            OutlinedTextField(fecha, { fecha = it }, label = { Text("Fecha (AAAA-MM-DD)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            error?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
            Button(onClick = {
                if (origen == null || destino == null) {
                    error = "No hay cuentas compatibles con este tipo de movimiento"
                    return@Button
                }
                if (origen.id == destino.id) {
                    error = "Elegí dos cuentas diferentes"
                    return@Button
                }
                if (origen.moneda != destino.moneda) {
                    error = "Las cuentas deben usar la misma moneda"
                    return@Button
                }
                val amount = runCatching { Dinero.de(importe.trim().replace(',', '.'), origen.moneda) }.getOrNull()
                if (amount == null || amount.importe <= BigDecimal.ZERO) {
                    error = "Ingresá un importe positivo"
                    return@Button
                }
                val date = runCatching { LocalDate.parse(fecha) }.getOrNull()
                if (date == null) {
                    error = "La fecha debe tener formato AAAA-MM-DD"
                    return@Button
                }
                error = null
                viewModel.registrarTransferencia(tipo, origen, destino, importe, date) { if (it) onDismiss() }
            }, modifier = Modifier.fillMaxWidth()) { Text("Guardar") }
        }
    }
}

@Composable
private fun AjusteDialog(state: FinanzasUiState, viewModel: FinanzasViewModel, onDismiss: () -> Unit) {
    var accountId by rememberSaveable { mutableStateOf(state.cuentas.firstOrNull { it.tipo == TipoCuenta.INVERSION }?.id.orEmpty()) }
    var nuevoValor by rememberSaveable { mutableStateOf("") }
    var fecha by rememberSaveable { mutableStateOf(LocalDate.now().toString()) }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    val cuentas = state.cuentas.filter { it.tipo == TipoCuenta.INVERSION }
    val cuenta = cuentas.firstOrNull { it.id == accountId } ?: cuentas.firstOrNull()
    FormDialog("Actualizar valuación", onDismiss) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            DropdownField("Cuenta", cuentas, cuenta?.id.orEmpty(), { it.id }, { it.nombre }) { accountId = it }
            OutlinedTextField(nuevoValor, { nuevoValor = it }, label = { Text("Nuevo valor") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth())
            OutlinedTextField(fecha, { fecha = it }, label = { Text("Fecha (AAAA-MM-DD)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            error?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
            Button(onClick = {
                if (cuenta == null) {
                    error = "No hay cuentas de inversión"
                    return@Button
                }
                val amount = runCatching { Dinero.de(nuevoValor.trim().replace(',', '.'), cuenta.moneda) }.getOrNull()
                if (amount == null || amount.importe < BigDecimal.ZERO) {
                    error = "Ingresá un valor válido"
                    return@Button
                }
                val date = runCatching { LocalDate.parse(fecha) }.getOrNull()
                if (date == null) {
                    error = "La fecha debe tener formato AAAA-MM-DD"
                    return@Button
                }
                error = null
                viewModel.registrarAjuste(cuenta, nuevoValor, date) { if (it) onDismiss() }
            }, modifier = Modifier.fillMaxWidth()) { Text("Guardar") }
        }
    }
}

@Composable
private fun CuentaDialog(viewModel: FinanzasViewModel, onDismiss: () -> Unit) {
    var nombre by rememberSaveable { mutableStateOf("") }
    var monedaName by rememberSaveable { mutableStateOf(Moneda.ARS.name) }
    var tipoName by rememberSaveable { mutableStateOf(TipoCuenta.OPERATIVA.name) }
    var saldo by rememberSaveable { mutableStateOf("0.00") }
    FormDialog("Nueva cuenta", onDismiss) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(nombre, { nombre = it }, label = { Text("Nombre") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            DropdownField("Moneda", Moneda.entries.toList(), monedaName, { it.name }, { it.name }) { monedaName = it }
            DropdownField("Tipo", TipoCuenta.entries.toList(), tipoName, { it.name }, { if (it == TipoCuenta.INVERSION) "Inversión" else "Operativa" }) { tipoName = it }
            OutlinedTextField(saldo, { saldo = it }, label = { Text("Saldo inicial") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth())
            Button(onClick = {
                viewModel.crearCuenta(nombre, Moneda.valueOf(monedaName), TipoCuenta.valueOf(tipoName), saldo) { if (it) onDismiss() }
            }, modifier = Modifier.fillMaxWidth()) { Text("Crear cuenta") }
        }
    }
}

@Composable
private fun CategoriaDialog(viewModel: FinanzasViewModel, onDismiss: () -> Unit) {
    var nombre by rememberSaveable { mutableStateOf("") }
    FormDialog("Nueva categoría", onDismiss) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(nombre, { nombre = it }, label = { Text("Nombre") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            Button(onClick = { viewModel.crearCategoria(nombre) { if (it) onDismiss() } }, modifier = Modifier.fillMaxWidth()) { Text("Crear categoría") }
        }
    }
}

@Composable
private fun PresupuestoDialog(state: FinanzasUiState, viewModel: FinanzasViewModel, onDismiss: () -> Unit) {
    var categoryId by rememberSaveable { mutableStateOf(state.categorias.firstOrNull()?.id.orEmpty()) }
    var mes by rememberSaveable { mutableStateOf(YearMonth.now().toString()) }
    var limite by rememberSaveable { mutableStateOf("") }
    var monedaName by rememberSaveable { mutableStateOf(Moneda.ARS.name) }
    val categoria = state.categorias.firstOrNull { it.id == categoryId }
    FormDialog("Nuevo presupuesto", onDismiss) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (state.categorias.isEmpty()) {
                Text("Primero necesitás crear una categoría.", color = MaterialTheme.colorScheme.error)
            } else {
                DropdownField("Categoría", state.categorias, categoryId, { it.id }, { it.nombre }) { categoryId = it }
                OutlinedTextField(mes, { mes = it }, label = { Text("Mes (AAAA-MM)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                DropdownField("Moneda", Moneda.entries.toList(), monedaName, { it.name }, { it.name }) { monedaName = it }
                OutlinedTextField(limite, { limite = it }, label = { Text("Límite") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth())
                Button(onClick = {
                    val month = runCatching { YearMonth.parse(mes) }.getOrNull() ?: return@Button
                    if (categoria == null) return@Button
                    viewModel.crearPresupuesto(categoria, month, limite, Moneda.valueOf(monedaName)) { if (it) onDismiss() }
                }, modifier = Modifier.fillMaxWidth()) { Text("Crear presupuesto") }
            }
        }
    }
}

@Composable
private fun <T> DropdownField(title: String, values: List<T>, selectedKey: String, key: (T) -> String, label: (T) -> String, allowEmpty: Boolean = false, onSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val selected = values.firstOrNull { key(it) == selectedKey }
    Column {
        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
            Text("$title: ${selected?.let(label) ?: if (allowEmpty) "Sin categoría" else "Elegir"}", modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
            Icon(Icons.Outlined.ChevronRight, null)
        }
        androidx.compose.material3.DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            if (allowEmpty) {
                androidx.compose.material3.DropdownMenuItem(text = { Text("Sin categoría") }, onClick = { onSelected(""); expanded = false })
            }
            values.forEach { value ->
                androidx.compose.material3.DropdownMenuItem(text = { Text(label(value)) }, onClick = { onSelected(key(value)); expanded = false })
            }
        }
    }
}

@Composable
private fun FilterChipButton(text: String, selected: Boolean, onClick: () -> Unit) {
    if (selected) Button(onClick = onClick, modifier = Modifier.width(130.dp)) { Text(text) }
    else OutlinedButton(onClick = onClick, modifier = Modifier.width(130.dp)) { Text(text) }
}

private fun saldoTotal(cuentas: List<Cuenta>, moneda: Moneda): Dinero {
    return cuentas.fold(Dinero.cero(moneda)) { total, cuenta -> total + cuenta.saldo() }
}

private fun dineroAbsoluto(dinero: Dinero): Dinero {
    return Dinero.de(dinero.importe.abs().toPlainString(), dinero.moneda)
}

private fun formatMoney(value: Dinero): String {
    val formatter = NumberFormat.getNumberInstance(Locale("es", "AR")).apply {
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    }
    return "${formatter.format(value.importe)} ${value.moneda.name}"
}

private fun TipoTransferencia.label(): String = when (this) {
    TipoTransferencia.NORMAL -> "Transferencia"
    TipoTransferencia.APORTE_INVERSION -> "Aporte"
    TipoTransferencia.RESCATE_INVERSION -> "Rescate"
}

private fun TipoTransferencia.key(): String = when (this) {
    TipoTransferencia.NORMAL -> "normal"
    TipoTransferencia.APORTE_INVERSION -> "aporte"
    TipoTransferencia.RESCATE_INVERSION -> "rescate"
}

private fun TipoTransferencia.originType(): TipoCuenta = when (this) {
    TipoTransferencia.NORMAL, TipoTransferencia.APORTE_INVERSION -> TipoCuenta.OPERATIVA
    TipoTransferencia.RESCATE_INVERSION -> TipoCuenta.INVERSION
}

private fun TipoTransferencia.destinationType(): TipoCuenta = when (this) {
    TipoTransferencia.NORMAL, TipoTransferencia.RESCATE_INVERSION -> TipoCuenta.OPERATIVA
    TipoTransferencia.APORTE_INVERSION -> TipoCuenta.INVERSION
}
