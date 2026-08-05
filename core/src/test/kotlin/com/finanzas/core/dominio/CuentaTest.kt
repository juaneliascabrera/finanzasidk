package com.finanzas.core.dominio

import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class CuentaTest {
    @Test
    fun conserva_los_datos_de_una_cuenta_operativa() {
        val cuenta = Cuenta(
            id = "brubank",
            nombre = "Brubank",
            moneda = Moneda.ARS,
            tipo = TipoCuenta.OPERATIVA,
            saldoInicial = Dinero.ars("100000.00")
        )

        assertEquals("brubank", cuenta.id)
        assertEquals("Brubank", cuenta.nombre)
        assertEquals(Moneda.ARS, cuenta.moneda)
        assertEquals(TipoCuenta.OPERATIVA, cuenta.tipo)
        assertEquals(Dinero.ars("100000.00"), cuenta.saldoInicial)
    }

    @Test
    fun calcula_saldo_sumando_ingresos_y_resta_egresos() {
        val cuenta = cuentaBrubank()
        val movimientos = listOf<Movimiento>(
            Ingreso(
                cuentaId = "brubank",
                fecha = LocalDate.of(2026, 8, 1),
                monto = Dinero.ars("50000.00")
            ),
            Egreso(
                cuentaId = "brubank",
                fecha = LocalDate.of(2026, 8, 5),
                monto = Dinero.ars("25000.00"),
                categoria = null
            )
        )

        assertEquals(Dinero.ars("125000.00"), cuenta.saldo(movimientos))
    }

    @Test
    fun ignora_movimientos_de_otras_cuentas() {
        val cuenta = cuentaBrubank()
        val movimientos = listOf<Movimiento>(
            Ingreso(
                cuentaId = "efectivo",
                fecha = LocalDate.of(2026, 8, 1),
                monto = Dinero.ars("50000.00")
            )
        )

        assertEquals(Dinero.ars("100000.00"), cuenta.saldo(movimientos))
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
    fun rechaza_movimiento_de_otra_moneda() {
        val cuenta = cuentaBrubank()
        val movimiento = Ingreso(
            cuentaId = "brubank",
            fecha = LocalDate.of(2026, 8, 1),
            monto = Dinero.usd("50.00")
        )

        assertFailsWith<IllegalArgumentException> {
            cuenta.saldo(listOf(movimiento))
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
        assertEquals(Dinero.ars("150000.00"), cuenta.saldoInicial)
    }

    @Test
    fun rechaza_egreso_directo_en_cuenta_de_inversion() {
        val cuenta = Cuenta(
            id = "fci",
            nombre = "FCI Conservador",
            moneda = Moneda.ARS,
            tipo = TipoCuenta.INVERSION,
            saldoInicial = Dinero.ars("150000.00")
        )
        val egreso = Egreso(
            cuentaId = "fci",
            fecha = LocalDate.of(2026, 8, 5),
            monto = Dinero.ars("50000.00"),
            categoria = null
        )

        assertFailsWith<IllegalArgumentException> {
            cuenta.saldo(listOf(egreso))
        }
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
}
