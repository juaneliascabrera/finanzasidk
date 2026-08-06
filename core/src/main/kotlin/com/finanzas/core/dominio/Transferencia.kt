package com.finanzas.core.dominio

import java.time.LocalDate

class Transferencia(
    val id: String,
    val fecha: LocalDate,
    val cuentaOrigen: Cuenta,
    val cuentaDestino: Cuenta,
    val monto: Dinero
) {
    init {
        require(id.isNotBlank()) { "El id de la transferencia no puede estar vacio" }
        require(cuentaOrigen.id != cuentaDestino.id) {
            "El origen y el destino deben ser cuentas distintas"
        }
        require(cuentaOrigen.tipo == TipoCuenta.OPERATIVA) {
            "La cuenta origen debe ser operativa"
        }
        require(cuentaDestino.tipo == TipoCuenta.OPERATIVA) {
            "La cuenta destino debe ser operativa"
        }
        require(cuentaOrigen.moneda == cuentaDestino.moneda) {
            "Las cuentas deben usar la misma moneda"
        }
        require(monto.moneda == cuentaOrigen.moneda) {
            "El monto debe usar la moneda de las cuentas"
        }
        require(monto.importe > Dinero.cero(monto.moneda).importe) {
            "El monto debe ser positivo"
        }
    }

    fun afectarSaldo(cuentaId: String, saldo: Dinero): Dinero {
        require(saldo.moneda == monto.moneda) {
            "El saldo debe usar la moneda de la transferencia"
        }

        return when (cuentaId) {
            cuentaOrigen.id -> saldo - monto
            cuentaDestino.id -> saldo + monto
            else -> saldo
        }
    }

    override fun equals(otra: Any?): Boolean {
        return otra is Transferencia && id == otra.id
    }

    override fun hashCode(): Int = id.hashCode()

    override fun toString(): String {
        return "Transferencia(id=$id, fecha=$fecha, origen=${cuentaOrigen.id}, destino=${cuentaDestino.id}, monto=$monto)"
    }
}
