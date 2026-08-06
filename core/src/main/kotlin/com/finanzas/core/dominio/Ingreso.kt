package com.finanzas.core.dominio

import java.time.LocalDate

class Ingreso(
    override val cuentaId: String,
    fecha: LocalDate,
    override val monto: Dinero,
    id: String
) : Transaccion(id, fecha, monto) {
    init {
        require(id.isNotBlank()) { "El id del ingreso no puede estar vacio" }
        require(monto.importe >= Dinero.cero(monto.moneda).importe) {
            "El monto de un ingreso no puede ser negativo"
        }
    }

    override fun validarRegistroEn(cuenta: Cuenta) {
        super.validarRegistroEn(cuenta)
        require(cuenta.tipo == TipoCuenta.OPERATIVA) {
            "Una cuenta de inversion no puede registrar ingresos directos"
        }
    }

    override fun afectarSaldo(saldo: Dinero): Dinero {
        require(saldo.moneda == monto.moneda) {
            "El saldo y el ingreso deben usar la misma moneda"
        }
        return saldo + monto
    }

    override fun toString(): String {
        return "Ingreso(id=$id, cuentaId=$cuentaId, fecha=$fecha, monto=$monto)"
    }
}
