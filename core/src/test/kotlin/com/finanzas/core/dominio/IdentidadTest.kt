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
    fun transferencias_con_el_mismo_id_representan_la_misma_transferencia() {
        val primera = Transferencia(
            id = "transferencia-1",
            cuentaOrigen = cuenta("origen-1"),
            cuentaDestino = cuenta("destino-1"),
            fecha = LocalDate.of(2026, 8, 1),
            monto = Dinero.ars("100.00")
        )
        val segunda = Transferencia(
            id = "transferencia-1",
            cuentaOrigen = cuenta("origen-2"),
            cuentaDestino = cuenta("destino-2"),
            fecha = LocalDate.of(2026, 8, 2),
            monto = Dinero.ars("200.00")
        )

        assertEquals(primera, segunda)
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

    @Test
    fun rechaza_categorias_sin_nombre() {
        assertFailsWith<IllegalArgumentException> {
            Categoria(id = "comida", nombre = "   ")
        }
    }

    private fun cuenta(id: String): Cuenta {
        return Cuenta(
            id = id,
            nombre = id,
            moneda = Moneda.ARS,
            tipo = TipoCuenta.OPERATIVA,
            saldoInicial = Dinero.ars("100.00")
        )
    }
}
