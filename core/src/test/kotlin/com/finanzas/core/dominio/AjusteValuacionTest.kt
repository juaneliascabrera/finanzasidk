package com.finanzas.core.dominio

import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class AjusteValuacionTest {
    @Test
    fun aumenta_el_valor_de_una_cuenta_de_inversion() {
        val cuenta = cuentaInversion()
        val ajuste = cuenta.registrarAjusteValuacion(
            id = "ajuste-1",
            fecha = LocalDate.of(2026, 8, 15),
            valorNuevo = Dinero.ars("160.00")
        )

        assertEquals(Dinero.ars("150.00"), ajuste.valorAnterior)
        assertEquals(Dinero.ars("10.00"), ajuste.monto)
        assertEquals(Dinero.ars("160.00"), cuenta.saldo())
    }

    @Test
    fun disminuye_el_valor_de_una_cuenta_de_inversion() {
        val cuenta = cuentaInversion()
        val ajuste = cuenta.registrarAjusteValuacion(
            id = "ajuste-1",
            fecha = LocalDate.of(2026, 8, 15),
            valorNuevo = Dinero.ars("140.00")
        )

        assertEquals(Dinero.ars("150.00"), ajuste.valorAnterior)
        assertEquals(Dinero.ars("-10.00"), ajuste.monto)
        assertEquals(Dinero.ars("140.00"), cuenta.saldo())
    }

    @Test
    fun solo_se_puede_registrar_en_una_cuenta_de_inversion() {
        val cuenta = cuentaBrubank()
        val ajuste = AjusteValuacion(
            id = "ajuste-1",
            cuentaId = cuenta.id,
            fecha = LocalDate.of(2026, 8, 15),
            valorAnterior = Dinero.ars("100.00"),
            valorNuevo = Dinero.ars("110.00")
        )

        assertFailsWith<IllegalArgumentException> {
            cuenta.registrar(ajuste)
        }
    }

    @Test
    fun rechaza_un_valor_anterior_distinto_del_saldo_actual() {
        val cuenta = cuentaInversion()
        val ajuste = AjusteValuacion(
            id = "ajuste-1",
            cuentaId = cuenta.id,
            fecha = LocalDate.of(2026, 8, 15),
            valorAnterior = Dinero.ars("149.99"),
            valorNuevo = Dinero.ars("160.00")
        )

        assertFailsWith<IllegalArgumentException> {
            cuenta.registrar(ajuste)
        }
    }

    @Test
    fun rechaza_valores_negativos() {
        assertFailsWith<IllegalArgumentException> {
            AjusteValuacion(
                id = "ajuste-1",
                cuentaId = "fci",
                fecha = LocalDate.of(2026, 8, 15),
                valorAnterior = Dinero.ars("150.00"),
                valorNuevo = Dinero.ars("-1.00")
            )
        }
    }

    @Test
    fun rechaza_valores_de_distinta_moneda() {
        assertFailsWith<IllegalArgumentException> {
            AjusteValuacion(
                id = "ajuste-1",
                cuentaId = "fci",
                fecha = LocalDate.of(2026, 8, 15),
                valorAnterior = Dinero.ars("150.00"),
                valorNuevo = Dinero.usd("160.00")
            )
        }
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
