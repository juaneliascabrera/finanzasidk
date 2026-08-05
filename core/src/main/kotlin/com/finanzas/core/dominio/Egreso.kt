package com.finanzas.core.dominio

import java.time.LocalDate

class Egreso(
    override val id: String,
    override val cuentaId: String,
    override val fecha: LocalDate,
    override val monto: Dinero,
    val categoria: Categoria?
) : Movimiento {
    init {
        require(id.isNotBlank()) { "El id del egreso no puede estar vacio" }
        require(monto.importe >= Dinero.cero(monto.moneda).importe) {
            "El monto de un egreso no puede ser negativo"
        }
    }

    override fun equals(otro: Any?): Boolean {
        return otro is Egreso && id == otro.id
    }

    override fun hashCode(): Int = id.hashCode()

    override fun toString(): String {
        return "Egreso(id=$id, cuentaId=$cuentaId, fecha=$fecha, monto=$monto, categoria=$categoria)"
    }
}
