package com.finanzas.core.dominio

import java.time.LocalDate

sealed interface Movimiento {
    val id: String
    val cuentaId: String
    val fecha: LocalDate
    val monto: Dinero
}
