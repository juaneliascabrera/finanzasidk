package com.finanzas.core.dominio

import java.time.LocalDate

data class Ingreso(
    override val cuentaId: String,
    override val fecha: LocalDate,
    override val monto: Dinero
) : Movimiento {
    init {
        require(monto.importe >= Dinero.cero(monto.moneda).importe) {
            "El monto de un ingreso no puede ser negativo"
        }
    }
}
