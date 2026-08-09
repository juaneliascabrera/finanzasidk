package com.finanzas.core.dominio

import java.time.LocalDate

class Cuenta(
    val id: String,
    val nombre: String,
    val moneda: Moneda,
    val tipo: TipoCuenta,
    val saldoInicial: Dinero
) {
    init {
        require(id.isNotBlank()) { "El id de la cuenta no puede estar vacio" }
        require(saldoInicial.moneda == moneda) {
            "El saldo inicial debe usar la moneda de la cuenta"
        }
        require(saldoInicial.importe >= Dinero.cero(moneda).importe) {
            "El saldo inicial no puede ser negativo"
        }
    }

    private val transacciones = mutableListOf<Transaccion>()

    fun registrar(transaccion: Transaccion) {
        validarPuedeRegistrar(transaccion)
        transacciones.add(transaccion)
    }

    fun registrarAjusteValuacion(
        id: String,
        fecha: LocalDate,
        valorNuevo: Dinero
    ): AjusteValuacion {
        val ajuste = AjusteValuacion(
            id = id,
            cuentaId = this.id,
            fecha = fecha,
            valorAnterior = saldo(),
            valorNuevo = valorNuevo
        )
        registrar(ajuste)
        return ajuste
    }

    internal fun validarPuedeRegistrar(transaccion: Transaccion) {
        transaccion.validarRegistroEn(this)
        require(transacciones.none { it.id == transaccion.id }) {
            "La cuenta ya tiene una transaccion con ese id"
        }
    }

    fun transacciones(): List<Transaccion> = transacciones.toList()

    fun saldo(): Dinero {
        return transacciones.fold(saldoInicial) { saldo, transaccion ->
            transaccion.afectarSaldo(saldo)
        }
    }

    override fun equals(otra: Any?): Boolean {
        return otra is Cuenta && id == otra.id
    }

    override fun hashCode(): Int = id.hashCode()

    override fun toString(): String {
        return "Cuenta(id=$id, nombre=$nombre, moneda=$moneda, tipo=$tipo, saldoInicial=$saldoInicial)"
    }
}
