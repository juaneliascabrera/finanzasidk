package com.finanzas.core.dominio

import java.time.LocalDate

sealed class Transaccion(
    val id: String,
    val fecha: LocalDate,
    open val monto: Dinero
) {
    init {
        require(id.isNotBlank()) { "El id de la transaccion no puede estar vacio" }
    }

    abstract val cuentaId: String

    open fun validarRegistroEn(cuenta: Cuenta) {
        require(cuenta.id == cuentaId) {
            "La transaccion no corresponde a la cuenta"
        }
        require(cuenta.moneda == monto.moneda) {
            "La transaccion y la cuenta deben usar la misma moneda"
        }
    }

    abstract fun afectarSaldo(saldo: Dinero): Dinero

    override fun equals(otra: Any?): Boolean {
        return otra is Transaccion && id == otra.id
    }

    override fun hashCode(): Int = id.hashCode()
}
