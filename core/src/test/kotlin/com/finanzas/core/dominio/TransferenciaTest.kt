package com.finanzas.core.dominio

import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class TransferenciaTest {
    @Test
    fun reduce_el_saldo_de_la_cuenta_origen() {
        val transferencia = transferenciaValida()

        val saldoResultante = transferencia.afectarSaldo(
            cuentaId = "brubank",
            saldo = Dinero.ars("100.00")
        )

        assertEquals(Dinero.ars("75.00"), saldoResultante)
    }

    @Test
    fun aumenta_el_saldo_de_la_cuenta_destino() {
        val transferencia = transferenciaValida()

        val saldoResultante = transferencia.afectarSaldo(
            cuentaId = "efectivo",
            saldo = Dinero.ars("50.00")
        )

        assertEquals(Dinero.ars("75.00"), saldoResultante)
    }

    @Test
    fun no_afecta_una_cuenta_ajena() {
        val transferencia = transferenciaValida()

        val saldoResultante = transferencia.afectarSaldo(
            cuentaId = "otra-cuenta",
            saldo = Dinero.ars("80.00")
        )

        assertEquals(Dinero.ars("80.00"), saldoResultante)
    }

    @Test
    fun requiere_cuentas_operativas() {
        val cuentaInversion = Cuenta(
            id = "fci",
            nombre = "FCI",
            moneda = Moneda.ARS,
            tipo = TipoCuenta.INVERSION,
            saldoInicial = Dinero.ars("100.00")
        )

        assertFailsWith<IllegalArgumentException> {
            Transferencia(
                id = "transferencia-1",
                fecha = LocalDate.of(2026, 8, 5),
                cuentaOrigen = cuentaBrubank(),
                cuentaDestino = cuentaInversion,
                monto = Dinero.ars("25.00")
            )
        }
    }

    @Test
    fun requiere_monedas_iguales_en_las_cuentas() {
        val cuentaDolares = Cuenta(
            id = "dolares",
            nombre = "Dolares",
            moneda = Moneda.USD,
            tipo = TipoCuenta.OPERATIVA,
            saldoInicial = Dinero.usd("100.00")
        )

        assertFailsWith<IllegalArgumentException> {
            Transferencia(
                id = "transferencia-1",
                fecha = LocalDate.of(2026, 8, 5),
                cuentaOrigen = cuentaBrubank(),
                cuentaDestino = cuentaDolares,
                monto = Dinero.ars("25.00")
            )
        }
    }

    @Test
    fun no_permite_transferirse_a_la_misma_cuenta() {
        val cuenta = cuentaBrubank()

        assertFailsWith<IllegalArgumentException> {
            Transferencia(
                id = "transferencia-1",
                fecha = LocalDate.of(2026, 8, 5),
                cuentaOrigen = cuenta,
                cuentaDestino = cuenta,
                monto = Dinero.ars("25.00")
            )
        }
    }

    @Test
    fun no_permite_monto_cero() {
        assertFailsWith<IllegalArgumentException> {
            Transferencia(
                id = "transferencia-1",
                fecha = LocalDate.of(2026, 8, 5),
                cuentaOrigen = cuentaBrubank(),
                cuentaDestino = cuentaEfectivo(),
                monto = Dinero.ars("0.00")
            )
        }
    }

    @Test
    fun no_permite_monto_negativo() {
        assertFailsWith<IllegalArgumentException> {
            Transferencia(
                id = "transferencia-1",
                fecha = LocalDate.of(2026, 8, 5),
                cuentaOrigen = cuentaBrubank(),
                cuentaDestino = cuentaEfectivo(),
                monto = Dinero.ars("-25.00")
            )
        }
    }

    @Test
    fun conserva_sus_datos_identificatorios() {
        val fecha = LocalDate.of(2026, 8, 5)
        val transferencia = Transferencia(
            id = "transferencia-1",
            fecha = fecha,
            cuentaOrigen = cuentaBrubank(),
            cuentaDestino = cuentaEfectivo(),
            monto = Dinero.ars("25.00")
        )

        assertEquals("transferencia-1", transferencia.id)
        assertEquals(fecha, transferencia.fecha)
        assertEquals("brubank", transferencia.cuentaOrigen.id)
        assertEquals("efectivo", transferencia.cuentaDestino.id)
        assertEquals(Dinero.ars("25.00"), transferencia.monto)
    }

    @Test
    fun rechaza_un_saldo_de_otra_moneda() {
        val transferencia = transferenciaValida()

        assertFailsWith<IllegalArgumentException> {
            transferencia.afectarSaldo(
                cuentaId = "brubank",
                saldo = Dinero.usd("100.00")
            )
        }
    }

    private fun transferenciaValida(): Transferencia {
        return Transferencia(
            id = "transferencia-1",
            fecha = LocalDate.of(2026, 8, 5),
            cuentaOrigen = cuentaBrubank(),
            cuentaDestino = cuentaEfectivo(),
            monto = Dinero.ars("25.00")
        )
    }

    private fun cuentaBrubank(): Cuenta {
        return Cuenta(
            id = "brubank",
            nombre = "Brubank",
            moneda = Moneda.ARS,
            tipo = TipoCuenta.OPERATIVA,
            saldoInicial = Dinero.ars("100.00")
        )
    }

    private fun cuentaEfectivo(): Cuenta {
        return Cuenta(
            id = "efectivo",
            nombre = "Efectivo",
            moneda = Moneda.ARS,
            tipo = TipoCuenta.OPERATIVA,
            saldoInicial = Dinero.ars("50.00")
        )
    }
}
