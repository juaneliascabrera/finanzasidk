package com.finanzas.core.dominio

import java.time.LocalDate

class Transferencia(
    val cuentaOrigen: Cuenta,
    val cuentaDestino: Cuenta,
    val fecha: LocalDate,
    val monto: Dinero,
    val id: String
) {
    lateinit var transferEgreso: TransferEgreso
        private set

    lateinit var transferIngreso: TransferIngreso
        private set

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

        transferEgreso = TransferEgreso(
            id = "$id:salida",
            transferencia = this,
            cuentaId = cuentaOrigen.id,
            fecha = fecha,
            monto = monto
        )
        transferIngreso = TransferIngreso(
            id = "$id:entrada",
            transferencia = this,
            cuentaId = cuentaDestino.id,
            fecha = fecha,
            monto = monto
        )

        cuentaOrigen.registrar(transferEgreso)
        cuentaDestino.registrar(transferIngreso)
    }

    override fun toString(): String {
        return "Transferencia(id=$id, fecha=$fecha, origen=${cuentaOrigen.id}, destino=${cuentaDestino.id}, monto=$monto)"
    }
}
