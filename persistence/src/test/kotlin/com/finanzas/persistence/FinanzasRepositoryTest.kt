package com.finanzas.persistence

import com.finanzas.core.dominio.Categoria
import com.finanzas.core.dominio.Cuenta
import com.finanzas.core.dominio.Dinero
import com.finanzas.core.dominio.Egreso
import com.finanzas.core.dominio.Ingreso
import com.finanzas.core.dominio.Moneda
import com.finanzas.core.dominio.Presupuesto
import com.finanzas.core.dominio.TipoCuenta
import com.finanzas.core.dominio.TipoTransferencia
import com.finanzas.core.dominio.Transferencia
import java.time.LocalDate
import java.time.YearMonth
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlinx.coroutines.test.runTest

class FinanzasRepositoryTest {
    @Test
    fun guarda_y_recupera_una_cuenta() = runTest {
        conRepositorio {
            guardarCuenta(cuentaBrubank())

            val recuperada = obtenerCuenta("brubank")

            assertNotNull(recuperada)
            assertEquals("Brubank", recuperada.nombre)
            assertEquals(Moneda.ARS, recuperada.moneda)
            assertEquals(Dinero.ars("100.00"), recuperada.saldo())
        }
    }

    @Test
    fun persiste_y_recupera_datos_desde_un_archivo() = runTest {
        val archivo = Files.createTempFile("finanzas-", ".db")
        Files.deleteIfExists(archivo)
        try {
            val primeraBase = FinanzasDatabaseFactory.enArchivo(archivo.toString())
            FinanzasRepository(primeraBase).run {
                guardarCuenta(cuentaBrubank())
                cerrar()
            }

            val segundaBase = FinanzasDatabaseFactory.enArchivo(archivo.toString())
            FinanzasRepository(segundaBase).run {
                assertEquals(Dinero.ars("100.00"), obtenerCuenta("brubank")!!.saldo())
                cerrar()
            }
        } finally {
            Files.deleteIfExists(archivo)
        }
    }

    @Test
    fun guarda_categoria_y_presupuesto() = runTest {
        conRepositorio {
            val categoria = Categoria("comida", "Comida")
            guardarCategoria(categoria)
            guardarPresupuesto(
                Presupuesto(
                    id = "presupuesto-1",
                    categoria = categoria,
                    mes = YearMonth.of(2026, 8),
                    limite = Dinero.ars("300.00")
                )
            )

            val presupuestos = obtenerPresupuestos()

            assertEquals(1, presupuestos.size)
            assertEquals("comida", presupuestos.single().categoria.id)
            assertEquals(Dinero.ars("300.00"), presupuestos.single().limite)
        }
    }

    @Test
    fun actualiza_entidades_y_elimina_un_presupuesto() = runTest {
        conRepositorio {
            val categoria = Categoria("comida", "Comida")
            guardarCuenta(cuentaBrubank())
            guardarCategoria(categoria)
            guardarPresupuesto(
                Presupuesto(
                    id = "presupuesto-1",
                    categoria = categoria,
                    mes = YearMonth.of(2026, 8),
                    limite = Dinero.ars("300.00")
                )
            )

            actualizarCuenta(
                Cuenta(
                    id = "brubank",
                    nombre = "Brubank principal",
                    moneda = Moneda.ARS,
                    tipo = TipoCuenta.OPERATIVA,
                    saldoInicial = Dinero.ars("100.00")
                )
            )
            actualizarCategoria(Categoria("comida", "Alimentos"))
            actualizarPresupuesto(
                Presupuesto(
                    id = "presupuesto-1",
                    categoria = Categoria("comida", "Alimentos"),
                    mes = YearMonth.of(2026, 8),
                    limite = Dinero.ars("350.00")
                )
            )

            assertEquals("Brubank principal", obtenerCuenta("brubank")!!.nombre)
            assertEquals("Alimentos", obtenerCategorias().single().nombre)
            assertEquals(Dinero.ars("350.00"), obtenerPresupuestos().single().limite)

            eliminarPresupuesto("presupuesto-1")

            assertEquals(emptyList(), obtenerPresupuestos())
        }
    }

    @Test
    fun no_permite_cambiar_la_base_financiera_de_una_cuenta_con_operaciones() = runTest {
        conRepositorio {
            val cuenta = cuentaBrubank()
            guardarCuenta(cuenta)
            registrarIngreso(
                Ingreso(
                    id = "ingreso-1",
                    cuentaId = cuenta.id,
                    fecha = LocalDate.of(2026, 8, 1),
                    monto = Dinero.ars("10.00")
                )
            )

            assertFailsWith<IllegalArgumentException> {
                actualizarCuenta(
                    Cuenta(
                        id = cuenta.id,
                        nombre = "Brubank",
                        moneda = Moneda.USD,
                        tipo = TipoCuenta.OPERATIVA,
                        saldoInicial = Dinero.usd("100.00")
                    )
                )
            }

            assertFailsWith<IllegalArgumentException> {
                actualizarCuenta(
                    Cuenta(
                        id = cuenta.id,
                        nombre = "Brubank",
                        moneda = Moneda.ARS,
                        tipo = TipoCuenta.OPERATIVA,
                        saldoInicial = Dinero.ars("101.00")
                    )
                )
            }
        }
    }

    @Test
    fun permite_eliminar_cuentas_y_categorias_sin_referencias() = runTest {
        conRepositorio {
            guardarCuenta(cuentaBrubank())
            guardarCategoria(Categoria("comida", "Comida"))

            eliminarCuenta("brubank")
            eliminarCategoria("comida")

            assertEquals(emptyList(), obtenerCuentas())
            assertEquals(emptyList(), obtenerCategorias())
        }
    }

    @Test
    fun no_elimina_una_cuenta_con_operaciones_ni_una_categoria_referenciada() = runTest {
        conRepositorio {
            val cuenta = cuentaBrubank()
            val categoria = Categoria("comida", "Comida")
            guardarCuenta(cuenta)
            guardarCategoria(categoria)
            guardarPresupuesto(
                Presupuesto(
                    id = "presupuesto-1",
                    categoria = categoria,
                    mes = YearMonth.of(2026, 8),
                    limite = Dinero.ars("300.00")
                )
            )

            assertFailsWith<IllegalArgumentException> {
                eliminarCategoria(categoria.id)
            }

            registrarIngreso(
                Ingreso(
                    id = "ingreso-1",
                    cuentaId = cuenta.id,
                    fecha = LocalDate.of(2026, 8, 1),
                    monto = Dinero.ars("10.00")
                )
            )

            assertFailsWith<IllegalArgumentException> {
                eliminarCuenta(cuenta.id)
            }
        }
    }

    @Test
    fun guarda_y_reconstruye_ingresos_egresos_y_su_saldo() = runTest {
        conRepositorio {
            val cuenta = cuentaBrubank()
            val categoria = Categoria("comida", "Comida")
            guardarCuenta(cuenta)
            guardarCategoria(categoria)
            registrarIngreso(
                Ingreso(
                    id = "ingreso-1",
                    cuentaId = cuenta.id,
                    fecha = LocalDate.of(2026, 8, 1),
                    monto = Dinero.ars("50.00")
                )
            )
            registrarEgreso(
                Egreso(
                    id = "egreso-1",
                    cuentaId = cuenta.id,
                    fecha = LocalDate.of(2026, 8, 2),
                    monto = Dinero.ars("20.00"),
                    categoria = categoria,
                    descripcion = "Café con amigos"
                )
            )

            val recuperada = obtenerCuenta(cuenta.id)

            assertNotNull(recuperada)
            assertEquals(Dinero.ars("130.00"), recuperada.saldo())
            assertEquals(2, recuperada.transacciones().size)
            assertEquals("Café con amigos", (recuperada.transacciones()[1] as Egreso).descripcion)
        }
    }

    @Test
    fun guarda_y_reconstruye_una_transferencia_sin_persistir_las_patas_por_separado() = runTest {
        conRepositorio {
            val origen = cuentaBrubank()
            val destino = cuentaEfectivo()
            guardarCuenta(origen)
            guardarCuenta(destino)
            val transferencia = Transferencia(
                id = "transferencia-1",
                cuentaOrigen = origen,
                cuentaDestino = destino,
                fecha = LocalDate.of(2026, 8, 5),
                monto = Dinero.ars("25.00")
            )

            registrarTransferencia(transferencia)

            val origenRecuperado = obtenerCuenta(origen.id)
            val destinoRecuperado = obtenerCuenta(destino.id)

            assertNotNull(origenRecuperado)
            assertNotNull(destinoRecuperado)
            assertEquals(Dinero.ars("75.00"), origenRecuperado.saldo())
            assertEquals(Dinero.ars("75.00"), destinoRecuperado.saldo())
            assertEquals(1, origenRecuperado.transacciones().size)
            assertEquals(1, destinoRecuperado.transacciones().size)
        }
    }

    @Test
    fun guarda_aporte_y_rescate_con_sus_tipos() = runTest {
        conRepositorio {
            val operativa = cuentaBrubank()
            val inversion = cuentaInversion()
            guardarCuenta(operativa)
            guardarCuenta(inversion)

            registrarTransferencia(
                Transferencia(
                    id = "aporte-1",
                    cuentaOrigen = operativa,
                    cuentaDestino = inversion,
                    fecha = LocalDate.of(2026, 8, 5),
                    monto = Dinero.ars("25.00"),
                    tipo = TipoTransferencia.APORTE_INVERSION
                )
            )
            registrarTransferencia(
                Transferencia(
                    id = "rescate-1",
                    cuentaOrigen = inversion,
                    cuentaDestino = operativa,
                    fecha = LocalDate.of(2026, 8, 6),
                    monto = Dinero.ars("10.00"),
                    tipo = TipoTransferencia.RESCATE_INVERSION
                )
            )

            assertEquals(Dinero.ars("85.00"), obtenerCuenta(operativa.id)!!.saldo())
            assertEquals(Dinero.ars("165.00"), obtenerCuenta(inversion.id)!!.saldo())
        }
    }

    @Test
    fun guarda_y_reconstruye_un_ajuste_de_valuacion() = runTest {
        conRepositorio {
            val cuenta = cuentaInversion()
            guardarCuenta(cuenta)

            val ajuste = registrarAjusteValuacion(
                id = "ajuste-1",
                cuentaId = cuenta.id,
                fecha = LocalDate.of(2026, 8, 15),
                valorNuevo = Dinero.ars("160.00")
            )

            val recuperada = obtenerCuenta(cuenta.id)

            assertEquals(Dinero.ars("150.00"), ajuste.valorAnterior)
            assertNotNull(recuperada)
            assertEquals(Dinero.ars("160.00"), recuperada.saldo())
            assertEquals(1, recuperada.transacciones().size)
        }
    }

    @Test
    fun reconstruye_operaciones_en_orden_de_registro_y_no_en_orden_de_fecha() = runTest {
        conRepositorio {
            val cuenta = cuentaInversion()
            guardarCuenta(cuenta)
            registrarAjusteValuacion(
                id = "ajuste-1",
                cuentaId = cuenta.id,
                fecha = LocalDate.of(2026, 8, 20),
                valorNuevo = Dinero.ars("160.00")
            )
            registrarAjusteValuacion(
                id = "ajuste-2",
                cuentaId = cuenta.id,
                fecha = LocalDate.of(2026, 8, 1),
                valorNuevo = Dinero.ars("155.00")
            )

            val recuperada = obtenerCuenta(cuenta.id)

            assertNotNull(recuperada)
            assertEquals(Dinero.ars("155.00"), recuperada.saldo())
            assertEquals(listOf("ajuste-1", "ajuste-2"), recuperada.transacciones().map { it.id })
        }
    }

    @Test
    fun una_transferencia_rechazada_no_se_persiste_ni_deja_efectos_parciales() = runTest {
        conRepositorio {
            val origen = cuentaBrubank()
            val destino = cuentaEfectivo()
            guardarCuenta(origen)
            guardarCuenta(destino)
            registrarIngreso(
                Ingreso(
                    id = "transferencia-1:entrada",
                    cuentaId = destino.id,
                    fecha = LocalDate.of(2026, 8, 1),
                    monto = Dinero.ars("10.00")
                )
            )

            assertFailsWith<IllegalArgumentException> {
                registrarTransferencia(
                    Transferencia(
                        id = "transferencia-1",
                        cuentaOrigen = origen,
                        cuentaDestino = destino,
                        fecha = LocalDate.of(2026, 8, 5),
                        monto = Dinero.ars("25.00")
                    )
                )
            }

            assertEquals(Dinero.ars("100.00"), obtenerCuenta(origen.id)!!.saldo())
            assertEquals(Dinero.ars("60.00"), obtenerCuenta(destino.id)!!.saldo())
        }
    }

    private suspend fun conRepositorio(block: suspend FinanzasRepository.() -> Unit) {
        val database = FinanzasDatabaseFactory.enMemoria()
        try {
            FinanzasRepository(database).block()
        } finally {
            database.close()
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
