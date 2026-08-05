package com.finanzas.core.dominio

import java.time.LocalDate
import java.time.YearMonth
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class IdentidadTest {
    @Test
    fun ingresos_con_el_mismo_id_representan_el_mismo_ingreso() {
        val primero = Ingreso(
            id = "ingreso-1",
            cuentaId = "brubank",
            fecha = LocalDate.of(2026, 8, 1),
            monto = Dinero.ars("100.00")
        )
        val segundo = Ingreso(
            id = "ingreso-1",
            cuentaId = "brubank",
            fecha = LocalDate.of(2026, 8, 15),
            monto = Dinero.ars("200.00")
        )

        assertEquals(primero, segundo)
    }

    @Test
    fun egresos_con_el_mismo_id_representan_el_mismo_egreso() {
        val primero = Egreso(
            id = "egreso-1",
            cuentaId = "brubank",
            fecha = LocalDate.of(2026, 8, 1),
            monto = Dinero.ars("100.00"),
            categoria = null
        )
        val segundo = Egreso(
            id = "egreso-1",
            cuentaId = "brubank",
            fecha = LocalDate.of(2026, 8, 15),
            monto = Dinero.ars("200.00"),
            categoria = null
        )

        assertEquals(primero, segundo)
    }

    @Test
    fun presupuestos_con_el_mismo_id_representan_el_mismo_presupuesto() {
        val primero = Presupuesto(
            id = "presupuesto-1",
            categoria = Categoria(id = "comida", nombre = "Comida"),
            mes = YearMonth.of(2026, 8),
            limite = Dinero.ars("100.00")
        )
        val segundo = Presupuesto(
            id = "presupuesto-1",
            categoria = Categoria(id = "transporte", nombre = "Transporte"),
            mes = YearMonth.of(2026, 9),
            limite = Dinero.ars("200.00")
        )

        assertEquals(primero, segundo)
    }

    @Test
    fun rechaza_categorias_sin_id() {
        assertFailsWith<IllegalArgumentException> {
            Categoria(id = "", nombre = "Comida")
        }
    }
}
