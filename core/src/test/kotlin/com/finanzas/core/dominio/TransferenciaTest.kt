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
    fun no_puede_registrar_otra_transaccion_con_el_id_de_una_pata() {
        val origen = cuentaBrubank()
        val destino = cuentaEfectivo()
        val transferencia = Transferencia(
            id = "transferencia-1",
            fecha = LocalDate.of(2026, 8, 5),
            cuentaOrigen = origen,
            cuentaDestino = destino,
            monto = Dinero.ars("25.00")
        )
        val ingreso = Ingreso(
            id = "transferencia-1:entrada",
            cuentaId = destino.id,
            fecha = transferencia.fecha,
            monto = transferencia.monto
        )

        assertFailsWith<IllegalArgumentException> {
            destino.registrar(ingreso)
        }
    }

    @Test
    fun un_aporte_de_inversion_resta_en_la_operativa_y_suma_en_la_inversion() {
        val origen = cuentaBrubank()
        val destino = cuentaInversion()

        val aporte = Transferencia(
            id = "aporte-1",
            cuentaOrigen = origen,
            cuentaDestino = destino,
            fecha = LocalDate.of(2026, 8, 10),
            monto = Dinero.ars("25.00"),
            tipo = TipoTransferencia.APORTE_INVERSION
        )

        assertEquals(TipoTransferencia.APORTE_INVERSION, aporte.tipo)
        assertEquals(Dinero.ars("75.00"), origen.saldo())
        assertEquals(Dinero.ars("175.00"), destino.saldo())
    }

    @Test
    fun un_rescate_de_inversion_resta_en_la_inversion_y_suma_en_la_operativa() {
        val origen = cuentaInversion()
        val destino = cuentaEfectivo()

        val rescate = Transferencia(
            id = "rescate-1",
            cuentaOrigen = origen,
            cuentaDestino = destino,
            fecha = LocalDate.of(2026, 8, 10),
            monto = Dinero.ars("25.00"),
            tipo = TipoTransferencia.RESCATE_INVERSION
        )

        assertEquals(TipoTransferencia.RESCATE_INVERSION, rescate.tipo)
        assertEquals(Dinero.ars("125.00"), origen.saldo())
        assertEquals(Dinero.ars("75.00"), destino.saldo())
    }

    @Test
    fun un_aporte_requiere_una_cuenta_de_inversion_como_destino() {
        assertFailsWith<IllegalArgumentException> {
            Transferencia(
                id = "aporte-1",
                cuentaOrigen = cuentaBrubank(),
                cuentaDestino = cuentaEfectivo(),
                fecha = LocalDate.of(2026, 8, 10),
                monto = Dinero.ars("25.00"),
                tipo = TipoTransferencia.APORTE_INVERSION
            )
        }
    }

    @Test
    fun un_rescate_requiere_una_cuenta_de_inversion_como_origen() {
        assertFailsWith<IllegalArgumentException> {
            Transferencia(
                id = "rescate-1",
                cuentaOrigen = cuentaBrubank(),
                cuentaDestino = cuentaEfectivo(),
                fecha = LocalDate.of(2026, 8, 10),
                monto = Dinero.ars("25.00"),
                tipo = TipoTransferencia.RESCATE_INVERSION
            )
        }
    }

    @Test
    fun no_deja_la_transferencia_a_medio_registrar_si_el_id_de_una_pata_ya_existe() {
        val origen = cuentaBrubank()
        val destino = cuentaEfectivo()
        destino.registrar(
            Ingreso(
                id = "transferencia-1:entrada",
                cuentaId = destino.id,
                fecha = LocalDate.of(2026, 8, 1),
                monto = Dinero.ars("10.00")
            )
        )

        assertFailsWith<IllegalArgumentException> {
            Transferencia(
                id = "transferencia-1",
                fecha = LocalDate.of(2026, 8, 5),
                cuentaOrigen = origen,
                cuentaDestino = destino,
                monto = Dinero.ars("25.00")
            )
        }

        assertEquals(emptyList(), origen.transacciones())
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
    fun no_registra_una_pata_de_transferencia_en_una_cuenta_de_inversion() {
        val inversion = cuentaInversion()
        val transferencia = Transferencia(
            id = "transferencia-1",
            fecha = LocalDate.of(2026, 8, 5),
            cuentaOrigen = cuentaBrubank(),
            cuentaDestino = cuentaEfectivo(),
            monto = Dinero.ars("25.00")
        )
        val pata = TransferIngreso(
            id = "pata-inversion",
            transferencia = transferencia,
            cuentaId = inversion.id,
            fecha = transferencia.fecha,
            monto = transferencia.monto
        )

        assertFailsWith<IllegalArgumentException> {
            inversion.registrar(pata)
        }
    }

    @Test
    fun no_registra_una_pata_en_una_cuenta_operativa_que_no_corresponde() {
        val otraCuenta = Cuenta(
            id = "otra-cuenta",
            nombre = "Otra cuenta",
            moneda = Moneda.ARS,
            tipo = TipoCuenta.OPERATIVA,
            saldoInicial = Dinero.ars("80.00")
        )
        val transferencia = Transferencia(
            id = "transferencia-1",
            fecha = LocalDate.of(2026, 8, 5),
            cuentaOrigen = cuentaBrubank(),
            cuentaDestino = cuentaEfectivo(),
            monto = Dinero.ars("25.00")
        )
        val pata = TransferIngreso(
            id = "pata-otra-cuenta",
            transferencia = transferencia,
            cuentaId = otraCuenta.id,
            fecha = transferencia.fecha,
            monto = transferencia.monto
        )

        assertFailsWith<IllegalArgumentException> {
            otraCuenta.registrar(pata)
        }
    }

    @Test
    fun no_registra_una_pata_con_un_monto_distinto_al_de_la_transferencia() {
        val destino = cuentaEfectivo()
        val transferencia = Transferencia(
            id = "transferencia-1",
            fecha = LocalDate.of(2026, 8, 5),
            cuentaOrigen = cuentaBrubank(),
            cuentaDestino = destino,
            monto = Dinero.ars("25.00")
        )
        val pata = TransferIngreso(
            id = "pata-monto-invalido",
            transferencia = transferencia,
            cuentaId = destino.id,
            fecha = transferencia.fecha,
            monto = Dinero.ars("30.00")
        )

        assertFailsWith<IllegalArgumentException> {
            destino.registrar(pata)
        }
    }

    @Test
    fun no_registra_una_pata_con_una_fecha_distinta_a_la_de_la_transferencia() {
        val destino = cuentaEfectivo()
        val transferencia = Transferencia(
            id = "transferencia-1",
            fecha = LocalDate.of(2026, 8, 5),
            cuentaOrigen = cuentaBrubank(),
            cuentaDestino = destino,
            monto = Dinero.ars("25.00")
        )
        val pata = TransferIngreso(
            id = "pata-fecha-invalida",
            transferencia = transferencia,
            cuentaId = destino.id,
            fecha = LocalDate.of(2026, 8, 6),
            monto = transferencia.monto
        )

        assertFailsWith<IllegalArgumentException> {
            destino.registrar(pata)
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
