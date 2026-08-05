package com.finanzas.core.dominio

import java.time.LocalDate
import java.time.YearMonth
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

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

    @Test
    fun un_presupuesto_sin_egresos_conserva_su_limite() {
        val presupuesto = presupuestoDeComida("300000.00")

        assertEquals(Dinero.ars("300000.00"), presupuesto.restante(emptyList()))
    }

    @Test
    fun suma_varios_egresos_de_la_misma_categoria_y_mes() {
        val categoria = Categoria(id = "comida", nombre = "Comida")
        val presupuesto = presupuestoDeComida("300000.00")
        val egresos = listOf(
            egreso("50000.00", LocalDate.of(2026, 8, 5), categoria),
            egreso("25000.00", LocalDate.of(2026, 8, 20), categoria)
        )

        assertEquals(Dinero.ars("225000.00"), presupuesto.restante(egresos))
    }

    @Test
    fun ignora_egresos_de_otra_categoria() {
        val presupuesto = presupuestoDeComida("300000.00")
        val egreso = egreso(
            monto = "50000.00",
            fecha = LocalDate.of(2026, 8, 5),
            categoria = Categoria(id = "transporte", nombre = "Transporte")
        )

        assertEquals(Dinero.ars("300000.00"), presupuesto.restante(listOf(egreso)))
    }

    @Test
    fun ignora_egresos_de_otro_mes() {
        val presupuesto = presupuestoDeComida("300000.00")
        val egreso = egreso(
            monto = "50000.00",
            fecha = LocalDate.of(2026, 7, 31),
            categoria = Categoria(id = "comida", nombre = "Comida")
        )

        assertEquals(Dinero.ars("300000.00"), presupuesto.restante(listOf(egreso)))
    }

    @Test
    fun ignora_egresos_sin_categoria() {
        val presupuesto = presupuestoDeComida("300000.00")
        val egreso = egreso(
            monto = "50000.00",
            fecha = LocalDate.of(2026, 8, 5),
            categoria = null
        )

        assertEquals(Dinero.ars("300000.00"), presupuesto.restante(listOf(egreso)))
    }

    @Test
    fun ignora_egresos_de_otra_moneda() {
        val presupuesto = presupuestoDeComida("300000.00")
        val egreso = Egreso(
            fecha = LocalDate.of(2026, 8, 5),
            monto = Dinero.usd("50.00"),
            categoria = Categoria(id = "comida", nombre = "Comida")
        )

        assertEquals(Dinero.ars("300000.00"), presupuesto.restante(listOf(egreso)))
    }

    @Test
    fun restante_es_cero_cuando_el_gasto_alcanza_el_limite() {
        val presupuesto = presupuestoDeComida("300000.00")
        val egreso = egreso("300000.00", LocalDate.of(2026, 8, 5))

        assertEquals(Dinero.ars("0.00"), presupuesto.restante(listOf(egreso)))
    }

    @Test
    fun restante_puede_ser_negativo_cuando_se_supera_el_limite() {
        val presupuesto = presupuestoDeComida("300000.00")
        val egreso = egreso("350000.00", LocalDate.of(2026, 8, 5))

        assertEquals(Dinero.ars("-50000.00"), presupuesto.restante(listOf(egreso)))
    }

    @Test
    fun incluye_egresos_del_primer_y_ultimo_dia_del_mes() {
        val presupuesto = presupuestoDeComida("300000.00")
        val egresos = listOf(
            egreso("50000.00", LocalDate.of(2026, 8, 1)),
            egreso("25000.00", LocalDate.of(2026, 8, 31))
        )

        assertEquals(Dinero.ars("225000.00"), presupuesto.restante(egresos))
    }

    @Test
    fun ignora_egresos_inmediatamente_fuera_del_mes() {
        val presupuesto = presupuestoDeComida("300000.00")
        val egresos = listOf(
            egreso("50000.00", LocalDate.of(2026, 7, 31)),
            egreso("25000.00", LocalDate.of(2026, 9, 1))
        )

        assertEquals(Dinero.ars("300000.00"), presupuesto.restante(egresos))
    }

    @Test
    fun conserva_los_centavos_en_el_calculo() {
        val presupuesto = presupuestoDeComida("300.00")
        val egreso = egreso("100.41", LocalDate.of(2026, 8, 5))

        assertEquals(Dinero.ars("199.59"), presupuesto.restante(listOf(egreso)))
    }

    @Test
    fun categorias_con_el_mismo_id_representan_la_misma_categoria() {
        val presupuesto = Presupuesto(
            categoria = Categoria(id = "comida", nombre = "Comida"),
            mes = YearMonth.of(2026, 8),
            limite = Dinero.ars("300000.00")
        )
        val egreso = egreso(
            monto = "50000.00",
            fecha = LocalDate.of(2026, 8, 5),
            categoria = Categoria(id = "comida", nombre = "Alimentacion")
        )

        assertEquals(Dinero.ars("250000.00"), presupuesto.restante(listOf(egreso)))
    }

    @Test
    fun no_permite_un_limite_negativo() {
        assertFailsWith<IllegalArgumentException> {
            presupuestoDeComida("-1.00")
        }
    }

    private fun presupuestoDeComida(limite: String): Presupuesto {
        return Presupuesto(
            categoria = Categoria(id = "comida", nombre = "Comida"),
            mes = YearMonth.of(2026, 8),
            limite = Dinero.ars(limite)
        )
    }

    private fun egreso(
        monto: String,
        fecha: LocalDate,
        categoria: Categoria? = Categoria(id = "comida", nombre = "Comida")
    ): Egreso {
        return Egreso(
            fecha = fecha,
            monto = Dinero.ars(monto),
            categoria = categoria
        )
    }
}
