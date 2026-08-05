package com.finanzas.core.dominio

import java.time.LocalDate

data class Egreso(
    val fecha: LocalDate,
    val monto: Dinero,
    val categoria: Categoria?
) {
    init {
        require(monto.importe >= Dinero.cero(monto.moneda).importe) {
            "El monto de un egreso no puede ser negativo"
        }
    }
}
