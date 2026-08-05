package com.finanzas.core.dominio

import java.time.LocalDate
import java.time.YearMonth
import kotlin.test.Test
import kotlin.test.assertEquals

class PresupuestoTest {
    @Test
    fun calcula_el_restante_de_un_presupuesto_con_un_egreso_del_mismo_mes() {
        val categoria = Categoria(id = "comida", nombre = "Comida")
        val presupuesto = Presupuesto(
            categoria = categoria,
            mes = YearMonth.of(2026, 8),
            limite = Dinero.ars("300000.00")
        )
        val egreso = Egreso(
            fecha = LocalDate.of(2026, 8, 5),
            monto = Dinero.ars("50000.00"),
            categoria = categoria
        )

        val restante = presupuesto.restante(listOf(egreso))

        assertEquals(Dinero.ars("250000.00"), restante)
    }
}
