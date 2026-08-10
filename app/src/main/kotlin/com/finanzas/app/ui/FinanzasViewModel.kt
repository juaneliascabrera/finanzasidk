package com.finanzas.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.finanzas.core.dominio.Categoria
import com.finanzas.core.dominio.Cuenta
import com.finanzas.core.dominio.Dinero
import com.finanzas.core.dominio.Egreso
import com.finanzas.core.dominio.Ingreso
import com.finanzas.core.dominio.Moneda
import com.finanzas.core.dominio.Presupuesto
import com.finanzas.core.dominio.TipoCuenta
import com.finanzas.core.dominio.TipoTransferencia
import com.finanzas.core.dominio.Transferencia
import com.finanzas.persistence.FinanzasRepository
import java.time.LocalDate
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class FinanzasUiState(
    val cuentas: List<Cuenta> = emptyList(),
    val categorias: List<Categoria> = emptyList(),
    val presupuestos: List<Presupuesto> = emptyList(),
    val cargando: Boolean = true,
    val error: String? = null
)

class FinanzasViewModel(
    private val repository: FinanzasRepository
) : ViewModel() {
    private val _state = MutableStateFlow(FinanzasUiState())
    val state: StateFlow<FinanzasUiState> = _state.asStateFlow()

    init {
        refrescar()
    }

    fun refrescar() {
        viewModelScope.launch {
            _state.value = _state.value.copy(cargando = true, error = null)
            runCatching {
                Triple(
                    repository.obtenerCuentas(),
                    repository.obtenerCategorias(),
                    repository.obtenerPresupuestos()
                )
            }.onSuccess { (cuentas, categorias, presupuestos) ->
                _state.value = FinanzasUiState(cuentas, categorias, presupuestos, false)
            }.onFailure { error ->
                _state.value = _state.value.copy(cargando = false, error = error.message ?: "No se pudo cargar la información")
            }
        }
    }

    fun crearCuenta(nombre: String, moneda: Moneda, tipo: TipoCuenta, saldoInicial: String, terminado: (Boolean) -> Unit) {
        viewModelScope.launch {
            runCatching {
                val saldo = Dinero.de(saldoInicial.normalizarImporte(), moneda)
                repository.guardarCuenta(
                    Cuenta(
                        id = UUID.randomUUID().toString(),
                        nombre = nombre.trim(),
                        moneda = moneda,
                        tipo = tipo,
                        saldoInicial = saldo
                    )
                )
            }.onSuccess {
                refrescar()
                terminado(true)
            }.onFailure { error ->
                mostrarError(error)
                terminado(false)
            }
        }
    }

    fun crearCategoria(nombre: String, terminado: (Boolean) -> Unit) {
        viewModelScope.launch {
            runCatching {
                repository.guardarCategoria(
                    Categoria(UUID.randomUUID().toString(), nombre.trim())
                )
            }.onSuccess {
                refrescar()
                terminado(true)
            }.onFailure { error ->
                mostrarError(error)
                terminado(false)
            }
        }
    }

    fun registrarIngreso(cuenta: Cuenta, importe: String, fecha: LocalDate, terminado: (Boolean) -> Unit) {
        ejecutar(terminado) {
            repository.registrarIngreso(
                Ingreso(
                    id = UUID.randomUUID().toString(),
                    cuentaId = cuenta.id,
                    fecha = fecha,
                    monto = Dinero.de(importe.normalizarImporte(), cuenta.moneda)
                )
            )
        }
    }

    fun registrarEgreso(cuenta: Cuenta, categoria: Categoria?, importe: String, fecha: LocalDate, terminado: (Boolean) -> Unit) {
        ejecutar(terminado) {
            repository.registrarEgreso(
                Egreso(
                    id = UUID.randomUUID().toString(),
                    cuentaId = cuenta.id,
                    fecha = fecha,
                    monto = Dinero.de(importe.normalizarImporte(), cuenta.moneda),
                    categoria = categoria
                )
            )
        }
    }

    fun registrarTransferencia(
        tipo: TipoTransferencia,
        origen: Cuenta,
        destino: Cuenta,
        importe: String,
        fecha: LocalDate,
        terminado: (Boolean) -> Unit
    ) {
        ejecutar(terminado) {
            repository.registrarTransferencia(
                Transferencia(
                    id = UUID.randomUUID().toString(),
                    cuentaOrigen = origen,
                    cuentaDestino = destino,
                    fecha = fecha,
                    monto = Dinero.de(importe.normalizarImporte(), origen.moneda),
                    tipo = tipo
                )
            )
        }
    }

    fun registrarAjuste(cuenta: Cuenta, nuevoValor: String, fecha: LocalDate, terminado: (Boolean) -> Unit) {
        ejecutar(terminado) {
            repository.registrarAjusteValuacion(
                id = UUID.randomUUID().toString(),
                cuentaId = cuenta.id,
                fecha = fecha,
                valorNuevo = Dinero.de(nuevoValor.normalizarImporte(), cuenta.moneda)
            )
        }
    }

    private fun ejecutar(terminado: (Boolean) -> Unit, accion: suspend () -> Unit) {
        viewModelScope.launch {
            runCatching { accion() }
                .onSuccess {
                    refrescar()
                    terminado(true)
                }
                .onFailure { error ->
                    mostrarError(error)
                    terminado(false)
                }
        }
    }

    private fun mostrarError(error: Throwable) {
        _state.value = _state.value.copy(error = error.message ?: "No se pudo guardar la operación")
    }

    companion object {
        fun factory(repository: FinanzasRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return FinanzasViewModel(repository) as T
                }
            }
    }
}

private fun String.normalizarImporte(): String = trim().replace(',', '.')
