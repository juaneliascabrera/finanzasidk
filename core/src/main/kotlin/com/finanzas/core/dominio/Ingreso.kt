package com.finanzas.core.dominio

import java.time.LocalDate

class Ingreso(
    override val id: String,
    override val cuentaId: String,
    override val fecha: LocalDate,
    override val monto: Dinero
) : Movimiento {
    init {
        require(id.isNotBlank()) { "El id del ingreso no puede estar vacio" }
        require(monto.importe >= Dinero.cero(monto.moneda).importe) {
            "El monto de un ingreso no puede ser negativo"
        }
    }

    override fun equals(otro: Any?): Boolean {
        return otro is Ingreso && id == otro.id
    }

    override fun hashCode(): Int = id.hashCode()

    override fun toString(): String {
        return "Ingreso(id=$id, cuentaId=$cuentaId, fecha=$fecha, monto=$monto)"
    }
}
