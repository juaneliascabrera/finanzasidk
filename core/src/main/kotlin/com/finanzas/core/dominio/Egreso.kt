package com.finanzas.core.dominio

import java.time.LocalDate

class Egreso(
    override val cuentaId: String,
    fecha: LocalDate,
    override val monto: Dinero,
    id: String,
    val categoria: Categoria?
) : Transaccion(id, fecha, monto) {
    init {
        require(id.isNotBlank()) { "El id del egreso no puede estar vacio" }
        require(monto.importe >= Dinero.cero(monto.moneda).importe) {
            "El monto de un egreso no puede ser negativo"
        }
    }

    override fun validarRegistroEn(cuenta: Cuenta) {
        super.validarRegistroEn(cuenta)
        require(cuenta.tipo == TipoCuenta.OPERATIVA) {
            "Una cuenta de inversion no puede registrar egresos directos"
        }
    }

    override fun afectarSaldo(saldo: Dinero): Dinero {
        require(saldo.moneda == monto.moneda) {
            "El saldo y el egreso deben usar la misma moneda"
        }
        return saldo - monto
    }

    override fun toString(): String {
        return "Egreso(id=$id, cuentaId=$cuentaId, fecha=$fecha, monto=$monto, categoria=$categoria)"
    }
}
