package com.finanzas.core.dominio

import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame

class TransferenciaTest {
    @Test
    fun registra_una_salida_en_la_cuenta_origen_y_una_entrada_en_destino() {
        val origen = cuentaBrubank()
        val destino = cuentaEfectivo()

        val transferencia = Transferencia(
            id = "transferencia-1",
            fecha = LocalDate.of(2026, 8, 5),
            cuentaOrigen = origen,
            cuentaDestino = destino,
            monto = Dinero.ars("25.00")
        )

        assertEquals(1, origen.transacciones().size)
        assertEquals(1, destino.transacciones().size)
        assertEquals(transferencia.transferEgreso, origen.transacciones().single())
        assertEquals(transferencia.transferIngreso, destino.transacciones().single())
    }

    @Test
    fun la_salida_reduce_el_saldo_de_la_cuenta_origen() {
        val origen = cuentaBrubank()
        val destino = cuentaEfectivo()

        Transferencia(
            id = "transferencia-1",
            fecha = LocalDate.of(2026, 8, 5),
            cuentaOrigen = origen,
            cuentaDestino = destino,
            monto = Dinero.ars("25.00")
        )

        assertEquals(Dinero.ars("75.00"), origen.saldo())
    }

    @Test
    fun la_entrada_aumenta_el_saldo_de_la_cuenta_destino() {
        val origen = cuentaBrubank()
        val destino = cuentaEfectivo()

        Transferencia(
            id = "transferencia-1",
            fecha = LocalDate.of(2026, 8, 5),
            cuentaOrigen = origen,
            cuentaDestino = destino,
            monto = Dinero.ars("25.00")
        )

        assertEquals(Dinero.ars("75.00"), destino.saldo())
    }

    @Test
    fun las_dos_transacciones_estan_asociadas_a_la_misma_transferencia() {
        val transferencia = Transferencia(
            id = "transferencia-1",
            fecha = LocalDate.of(2026, 8, 5),
            cuentaOrigen = cuentaBrubank(),
            cuentaDestino = cuentaEfectivo(),
            monto = Dinero.ars("25.00")
        )

        assertSame(transferencia, transferencia.transferEgreso.transferencia)
        assertSame(transferencia, transferencia.transferIngreso.transferencia)
    }

    @Test
    fun la_salida_conoce_la_entrada_asociada_y_viceversa() {
        val transferencia = Transferencia(
            id = "transferencia-1",
            fecha = LocalDate.of(2026, 8, 5),
            cuentaOrigen = cuentaBrubank(),
            cuentaDestino = cuentaEfectivo(),
            monto = Dinero.ars("25.00")
        )

        assertSame(transferencia.transferIngreso, transferencia.transferEgreso.associatedDeposit)
        assertSame(transferencia.transferEgreso, transferencia.transferIngreso.associatedWithdraw)
    }

    @Test
    fun no_afecta_una_cuenta_ajena() {
        val origen = cuentaBrubank()
        val destino = cuentaEfectivo()
        val otra = Cuenta(
            id = "otra-cuenta",
            nombre = "Otra cuenta",
            moneda = Moneda.ARS,
            tipo = TipoCuenta.OPERATIVA,
            saldoInicial = Dinero.ars("80.00")
        )

        Transferencia(
            id = "transferencia-1",
            fecha = LocalDate.of(2026, 8, 5),
            cuentaOrigen = origen,
            cuentaDestino = destino,
            monto = Dinero.ars("25.00")
        )

        assertEquals(emptyList(), otra.transacciones())
        assertEquals(Dinero.ars("80.00"), otra.saldo())
    }

    @Test
    fun requiere_cuentas_operativas() {
        val cuentaInversion = cuentaInversion()

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

    private fun cuentaInversion(): Cuenta {
        return Cuenta(
            id = "fci",
            nombre = "FCI Conservador",
            moneda = Moneda.ARS,
            tipo = TipoCuenta.INVERSION,
            saldoInicial = Dinero.ars("150.00")
        )
    }
}
