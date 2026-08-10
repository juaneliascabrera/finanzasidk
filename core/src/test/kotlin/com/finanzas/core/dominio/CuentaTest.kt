package com.finanzas.core.dominio

import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class CuentaTest {
    @Test
    fun conserva_los_datos_de_una_cuenta_operativa() {
        val cuenta = cuentaBrubank()

        assertEquals("brubank", cuenta.id)
        assertEquals("Brubank", cuenta.nombre)
        assertEquals(Moneda.ARS, cuenta.moneda)
        assertEquals(TipoCuenta.OPERATIVA, cuenta.tipo)
        assertEquals(Dinero.ars("100000.00"), cuenta.saldoInicial)
    }

    @Test
    fun calcula_saldo_a_partir_de_las_transacciones_registradas() {
        val cuenta = cuentaBrubank()
        cuenta.registrar(
            Ingreso(
                id = "ingreso-brubank-1",
                cuentaId = "brubank",
                fecha = LocalDate.of(2026, 8, 1),
                monto = Dinero.ars("50000.00")
            )
        )
        cuenta.registrar(
            Egreso(
                id = "egreso-brubank-1",
                cuentaId = "brubank",
                fecha = LocalDate.of(2026, 8, 5),
                monto = Dinero.ars("25000.00"),
                categoria = null
            )
        )

        assertEquals(Dinero.ars("125000.00"), cuenta.saldo())
    }

    @Test
    fun conoce_las_transacciones_que_registro() {
        val cuenta = cuentaBrubank()
        val ingreso = Ingreso(
            id = "ingreso-brubank-1",
            cuentaId = "brubank",
            fecha = LocalDate.of(2026, 8, 1),
            monto = Dinero.ars("50000.00")
        )

        cuenta.registrar(ingreso)

        assertEquals(listOf(ingreso), cuenta.transacciones())
    }

    @Test
    fun no_puede_registrar_dos_veces_la_misma_transaccion() {
        val cuenta = cuentaBrubank()
        val ingreso = Ingreso(
            id = "transaccion-1",
            cuentaId = "brubank",
            fecha = LocalDate.of(2026, 8, 1),
            monto = Dinero.ars("100.00")
        )

        cuenta.registrar(ingreso)

        assertFailsWith<IllegalArgumentException> {
            cuenta.registrar(ingreso)
        }
    }

    @Test
    fun no_puede_registrar_dos_transacciones_de_distinto_tipo_con_el_mismo_id() {
        val cuenta = cuentaBrubank()
        val ingreso = Ingreso(
            id = "transaccion-1",
            cuentaId = "brubank",
            fecha = LocalDate.of(2026, 8, 1),
            monto = Dinero.ars("100.00")
        )
        val egreso = Egreso(
            id = "transaccion-1",
            cuentaId = "brubank",
            fecha = LocalDate.of(2026, 8, 2),
            monto = Dinero.ars("25.00"),
            categoria = null
        )

        cuenta.registrar(ingreso)

        assertFailsWith<IllegalArgumentException> {
            cuenta.registrar(egreso)
        }
    }

    @Test
    fun no_puede_registrar_una_transaccion_de_otra_cuenta() {
        val cuenta = cuentaBrubank()
        val ingreso = Ingreso(
            id = "ingreso-efectivo-1",
            cuentaId = "efectivo",
            fecha = LocalDate.of(2026, 8, 1),
            monto = Dinero.ars("50000.00")
        )

        assertFailsWith<IllegalArgumentException> {
            cuenta.registrar(ingreso)
        }
    }

    @Test
    fun rechaza_saldo_inicial_negativo() {
        assertFailsWith<IllegalArgumentException> {
            Cuenta(
                id = "brubank",
                nombre = "Brubank",
                moneda = Moneda.ARS,
                tipo = TipoCuenta.OPERATIVA,
                saldoInicial = Dinero.ars("-1.00")
            )
        }
    }

    @Test
    fun rechaza_saldo_inicial_de_otra_moneda() {
        assertFailsWith<IllegalArgumentException> {
            Cuenta(
                id = "brubank",
                nombre = "Brubank",
                moneda = Moneda.ARS,
                tipo = TipoCuenta.OPERATIVA,
                saldoInicial = Dinero.usd("100.00")
            )
        }
    }

    @Test
    fun rechaza_cuentas_sin_nombre() {
        assertFailsWith<IllegalArgumentException> {
            Cuenta(
                id = "brubank",
                nombre = "   ",
                moneda = Moneda.ARS,
                tipo = TipoCuenta.OPERATIVA,
                saldoInicial = Dinero.ars("100.00")
            )
        }
    }

    @Test
    fun rechaza_transaccion_de_otra_moneda() {
        val cuenta = cuentaBrubank()
        val ingreso = Ingreso(
            id = "ingreso-brubank-usd",
            cuentaId = "brubank",
            fecha = LocalDate.of(2026, 8, 1),
            monto = Dinero.usd("50.00")
        )

        assertFailsWith<IllegalArgumentException> {
            cuenta.registrar(ingreso)
        }
    }

    @Test
    fun puede_representar_una_cuenta_de_inversion() {
        val cuenta = Cuenta(
            id = "fci",
            nombre = "FCI Conservador",
            moneda = Moneda.ARS,
            tipo = TipoCuenta.INVERSION,
            saldoInicial = Dinero.ars("150000.00")
        )

        assertEquals(TipoCuenta.INVERSION, cuenta.tipo)
        assertEquals(Dinero.ars("150000.00"), cuenta.saldo())
    }

    @Test
    fun rechaza_egreso_directo_en_cuenta_de_inversion() {
        val cuenta = cuentaInversion()
        val egreso = Egreso(
            id = "egreso-fci-1",
            cuentaId = "fci",
            fecha = LocalDate.of(2026, 8, 5),
            monto = Dinero.ars("50000.00"),
            categoria = null
        )

        assertFailsWith<IllegalArgumentException> {
            cuenta.registrar(egreso)
        }
    }

    @Test
    fun rechaza_ingreso_directo_en_cuenta_de_inversion() {
        val cuenta = cuentaInversion()
        val ingreso = Ingreso(
            id = "ingreso-fci-1",
            cuentaId = "fci",
            fecha = LocalDate.of(2026, 8, 5),
            monto = Dinero.ars("50000.00")
        )

        assertFailsWith<IllegalArgumentException> {
            cuenta.registrar(ingreso)
        }
    }

    @Test
    fun cuentas_con_el_mismo_id_representan_la_misma_cuenta() {
        val primera = cuentaBrubank()
        val segunda = Cuenta(
            id = "brubank",
            nombre = "Cuenta principal",
            moneda = Moneda.ARS,
            tipo = TipoCuenta.OPERATIVA,
            saldoInicial = Dinero.ars("250000.00")
        )

        assertEquals(primera, segunda)
    }

    private fun cuentaBrubank(): Cuenta {
        return Cuenta(
            id = "brubank",
            nombre = "Brubank",
            moneda = Moneda.ARS,
            tipo = TipoCuenta.OPERATIVA,
            saldoInicial = Dinero.ars("100000.00")
        )
    }

    private fun cuentaInversion(): Cuenta {
        return Cuenta(
            id = "fci",
            nombre = "FCI Conservador",
            moneda = Moneda.ARS,
            tipo = TipoCuenta.INVERSION,
            saldoInicial = Dinero.ars("150000.00")
        )
    }
}
