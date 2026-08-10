package com.finanzas.core.dominio

import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class EgresoTest {
    @Test
    fun conserva_la_descripcion_opcional() {
        val egreso = Egreso(
            id = "egreso-1",
            cuentaId = "brubank",
            fecha = LocalDate.of(2026, 8, 1),
            monto = Dinero.ars("25.00"),
            categoria = null,
            descripcion = "  Café con amigos  "
        )

        assertEquals("Café con amigos", egreso.descripcion)
    }

    @Test
    fun convierte_una_descripcion_vacia_en_null() {
        val egreso = Egreso(
            id = "egreso-1",
            cuentaId = "brubank",
            fecha = LocalDate.of(2026, 8, 1),
            monto = Dinero.ars("25.00"),
            categoria = null,
            descripcion = "   "
        )

        assertNull(egreso.descripcion)
    }
}
