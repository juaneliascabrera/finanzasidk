package com.finanzas.persistence

import com.finanzas.core.dominio.AjusteValuacion
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

class FinanzasRepository(private val database: FinanzasDatabase) {
    private val dao = database.finanzasDao()

    suspend fun guardarCuenta(cuenta: Cuenta) {
        dao.insertarCuenta(cuenta.aEntity())
    }

    suspend fun actualizarCuenta(cuenta: Cuenta) {
        val actual = dao.obtenerCuenta(cuenta.id)
            ?: error("La cuenta no existe")
        if (dao.contarOperacionesDeCuenta(cuenta.id) > 0) {
            require(cuenta.moneda.name == actual.moneda) {
                "No se puede cambiar la moneda de una cuenta con operaciones"
            }
            require(cuenta.tipo.name == actual.tipo) {
                "No se puede cambiar el tipo de una cuenta con operaciones"
            }
            require(cuenta.saldoInicial.importe.toPlainString() == actual.saldoInicial) {
                "No se puede cambiar el saldo inicial de una cuenta con operaciones"
            }
        }
        dao.actualizarCuenta(cuenta.aEntity())
    }

    suspend fun eliminarCuenta(id: String) {
        val cuenta = dao.obtenerCuenta(id) ?: error("La cuenta no existe")
        require(dao.contarOperacionesDeCuenta(id) == 0L) {
            "No se puede eliminar una cuenta con operaciones"
        }
        dao.eliminarCuenta(cuenta)
    }

    suspend fun guardarCategoria(categoria: Categoria) {
        dao.insertarCategoria(categoria.aEntity())
    }

    suspend fun actualizarCategoria(categoria: Categoria) {
        require(dao.obtenerCategoria(categoria.id) != null) {
            "La categoria no existe"
        }
        dao.actualizarCategoria(categoria.aEntity())
    }

    suspend fun eliminarCategoria(id: String) {
        val categoria = dao.obtenerCategoria(id) ?: error("La categoria no existe")
        require(dao.contarPresupuestosDeCategoria(id) == 0L) {
            "No se puede eliminar una categoria con presupuestos"
        }
        require(dao.contarOperacionesDeCategoria(id) == 0L) {
            "No se puede eliminar una categoria usada por operaciones"
        }
        dao.eliminarCategoria(categoria)
    }

    suspend fun guardarPresupuesto(presupuesto: Presupuesto) {
        require(dao.obtenerCategoria(presupuesto.categoria.id) != null) {
            "La categoria del presupuesto no existe"
        }
        dao.insertarPresupuesto(presupuesto.aEntity())
    }

    suspend fun actualizarPresupuesto(presupuesto: Presupuesto) {
        require(dao.obtenerPresupuesto(presupuesto.id) != null) {
            "El presupuesto no existe"
        }
        require(dao.obtenerCategoria(presupuesto.categoria.id) != null) {
            "La categoria del presupuesto no existe"
        }
        dao.actualizarPresupuesto(presupuesto.aEntity())
    }

    suspend fun eliminarPresupuesto(id: String) {
        val presupuesto = dao.obtenerPresupuesto(id) ?: error("El presupuesto no existe")
        dao.eliminarPresupuesto(presupuesto)
    }

    suspend fun registrarIngreso(ingreso: Ingreso) {
        val cuenta = reconstruirCuentas()[ingreso.cuentaId]
            ?: error("La cuenta del ingreso no existe")
        cuenta.registrar(ingreso)
        dao.insertarOperacion(ingreso.aEntity())
    }

    suspend fun registrarEgreso(egreso: Egreso) {
        val cuenta = reconstruirCuentas()[egreso.cuentaId]
            ?: error("La cuenta del egreso no existe")
        val categoria = egreso.categoria
        require(categoria == null || dao.obtenerCategoria(categoria.id) != null) {
            "La categoria del egreso no existe"
        }
        cuenta.registrar(egreso)
        dao.insertarOperacion(egreso.aEntity())
    }

    suspend fun registrarAjusteValuacion(
        id: String,
        cuentaId: String,
        fecha: LocalDate,
        valorNuevo: Dinero
    ): AjusteValuacion {
        val cuenta = reconstruirCuentas()[cuentaId]
            ?: error("La cuenta del ajuste no existe")
        val ajuste = cuenta.registrarAjusteValuacion(id, fecha, valorNuevo)
        dao.insertarOperacion(ajuste.aEntity())
        return ajuste
    }

    suspend fun registrarTransferencia(transferencia: Transferencia) {
        val cuentas = reconstruirCuentas()
        val origen = cuentas[transferencia.cuentaOrigen.id]
            ?: error("La cuenta origen de la transferencia no existe")
        val destino = cuentas[transferencia.cuentaDestino.id]
            ?: error("La cuenta destino de la transferencia no existe")
        val transferenciaValidada = Transferencia(
            id = transferencia.id,
            cuentaOrigen = origen,
            cuentaDestino = destino,
            fecha = transferencia.fecha,
            monto = transferencia.monto,
            tipo = transferencia.tipo
        )
        dao.insertarOperacion(transferenciaValidada.aEntity())
    }

    suspend fun obtenerCuentas(): List<Cuenta> = reconstruirCuentas().values.toList()

    suspend fun obtenerCuenta(id: String): Cuenta? = reconstruirCuentas()[id]

    suspend fun obtenerCategorias(): List<Categoria> = dao.obtenerCategorias().map { it.aDominio() }

    suspend fun obtenerPresupuestos(): List<Presupuesto> {
        val categorias = dao.obtenerCategorias().associateBy { it.id }
        return dao.obtenerPresupuestos().map { entity ->
            val categoria = categorias[entity.categoriaId]
                ?: error("La categoria del presupuesto no existe")
            entity.aDominio(categoria.aDominio())
        }
    }

    fun cerrar() {
        database.close()
    }

    private suspend fun reconstruirCuentas(): LinkedHashMap<String, Cuenta> {
        val cuentas = LinkedHashMap(
            dao.obtenerCuentas().associate { it.id to it.aDominio() }
        )
        val categorias = dao.obtenerCategorias().associateBy { it.id }

        dao.obtenerOperaciones().forEach { operacion ->
            when (operacion.tipo) {
                TipoOperacion.INGRESO -> {
                    val cuenta = cuentas.requireCuenta(operacion.cuentaId)
                    cuenta.registrar(operacion.aIngreso())
                }

                TipoOperacion.EGRESO -> {
                    val cuenta = cuentas.requireCuenta(operacion.cuentaId)
                    val categoria = operacion.categoriaId?.let { id ->
                        categorias[id]?.aDominio()
                            ?: error("La categoria del egreso no existe")
                    }
                    cuenta.registrar(operacion.aEgreso(categoria))
                }

                TipoOperacion.AJUSTE_VALUACION -> {
                    val cuenta = cuentas.requireCuenta(operacion.cuentaId)
                    cuenta.registrar(operacion.aAjusteValuacion())
                }

                TipoOperacion.TRANSFERENCIA_NORMAL,
                TipoOperacion.APORTE_INVERSION,
                TipoOperacion.RESCATE_INVERSION -> {
                    val origen = cuentas.requireCuenta(operacion.cuentaOrigenId)
                    val destino = cuentas.requireCuenta(operacion.cuentaDestinoId)
                    Transferencia(
                        id = operacion.id,
                        cuentaOrigen = origen,
                        cuentaDestino = destino,
                        fecha = LocalDate.parse(operacion.fecha),
                        monto = operacion.aDinero(),
                        tipo = operacion.aTipoTransferencia()
                    )
                }

                else -> error("Tipo de operacion desconocido: ${operacion.tipo}")
            }
        }
        return cuentas
    }
}

private object TipoOperacion {
    const val INGRESO = "INGRESO"
    const val EGRESO = "EGRESO"
    const val AJUSTE_VALUACION = "AJUSTE_VALUACION"
    const val TRANSFERENCIA_NORMAL = "TRANSFERENCIA_NORMAL"
    const val APORTE_INVERSION = "APORTE_INVERSION"
    const val RESCATE_INVERSION = "RESCATE_INVERSION"
}

private fun Cuenta.aEntity(): CuentaEntity {
    return CuentaEntity(id, nombre, moneda.name, tipo.name, saldoInicial.importe.toPlainString())
}

private fun Categoria.aEntity(): CategoriaEntity = CategoriaEntity(id, nombre)

private fun Presupuesto.aEntity(): PresupuestoEntity {
    return PresupuestoEntity(
        id,
        categoria.id,
        mes.toString(),
        limite.importe.toPlainString(),
        limite.moneda.name
    )
}

private fun Ingreso.aEntity(): OperacionEntity {
    return OperacionEntity(
        id = id,
        tipo = TipoOperacion.INGRESO,
        fecha = fecha.toString(),
        cuentaId = cuentaId,
        cuentaOrigenId = null,
        cuentaDestinoId = null,
        monto = monto.importe.toPlainString(),
        moneda = monto.moneda.name,
        categoriaId = null,
        descripcion = null,
        valorAnterior = null,
        valorNuevo = null
    )
}

private fun Egreso.aEntity(): OperacionEntity {
    return OperacionEntity(
        id = id,
        tipo = TipoOperacion.EGRESO,
        fecha = fecha.toString(),
        cuentaId = cuentaId,
        cuentaOrigenId = null,
        cuentaDestinoId = null,
        monto = monto.importe.toPlainString(),
        moneda = monto.moneda.name,
        categoriaId = categoria?.id,
        descripcion = descripcion,
        valorAnterior = null,
        valorNuevo = null
    )
}

private fun AjusteValuacion.aEntity(): OperacionEntity {
    return OperacionEntity(
        id = id,
        tipo = TipoOperacion.AJUSTE_VALUACION,
        fecha = fecha.toString(),
        cuentaId = cuentaId,
        cuentaOrigenId = null,
        cuentaDestinoId = null,
        monto = monto.importe.toPlainString(),
        moneda = monto.moneda.name,
        categoriaId = null,
        descripcion = null,
        valorAnterior = valorAnterior.importe.toPlainString(),
        valorNuevo = valorNuevo.importe.toPlainString()
    )
}

private fun Transferencia.aEntity(): OperacionEntity {
    val tipo = when (tipo) {
        TipoTransferencia.NORMAL -> TipoOperacion.TRANSFERENCIA_NORMAL
        TipoTransferencia.APORTE_INVERSION -> TipoOperacion.APORTE_INVERSION
        TipoTransferencia.RESCATE_INVERSION -> TipoOperacion.RESCATE_INVERSION
    }
    return OperacionEntity(
        id = id,
        tipo = tipo,
        fecha = fecha.toString(),
        cuentaId = null,
        cuentaOrigenId = cuentaOrigen.id,
        cuentaDestinoId = cuentaDestino.id,
        monto = monto.importe.toPlainString(),
        moneda = monto.moneda.name,
        categoriaId = null,
        descripcion = null,
        valorAnterior = null,
        valorNuevo = null
    )
}

private fun CuentaEntity.aDominio(): Cuenta {
    return Cuenta(
        id = id,
        nombre = nombre,
        moneda = Moneda.valueOf(moneda),
        tipo = TipoCuenta.valueOf(tipo),
        saldoInicial = Dinero.de(saldoInicial, Moneda.valueOf(moneda))
    )
}

private fun CategoriaEntity.aDominio(): Categoria = Categoria(id, nombre)

private fun PresupuestoEntity.aDominio(categoria: Categoria): Presupuesto {
    return Presupuesto(
        id = id,
        categoria = categoria,
        mes = YearMonth.parse(mes),
        limite = Dinero.de(limite, Moneda.valueOf(moneda))
    )
}

private fun OperacionEntity.aDinero(): Dinero {
    return Dinero.de(monto ?: error("La operacion no tiene monto"), Moneda.valueOf(moneda!!))
}

private fun OperacionEntity.aIngreso(): Ingreso {
    return Ingreso(
        id = id,
        cuentaId = cuentaId ?: error("El ingreso no tiene cuenta"),
        fecha = LocalDate.parse(fecha),
        monto = aDinero()
    )
}

private fun OperacionEntity.aEgreso(categoria: Categoria?): Egreso {
    return Egreso(
        id = id,
        cuentaId = cuentaId ?: error("El egreso no tiene cuenta"),
        fecha = LocalDate.parse(fecha),
        monto = aDinero(),
        categoria = categoria,
        descripcion = descripcion
    )
}

private fun OperacionEntity.aAjusteValuacion(): AjusteValuacion {
    val monedaDominio = Moneda.valueOf(moneda!!)
    return AjusteValuacion(
        id = id,
        cuentaId = cuentaId ?: error("El ajuste no tiene cuenta"),
        fecha = LocalDate.parse(fecha),
        valorAnterior = Dinero.de(valorAnterior ?: error("El ajuste no tiene valor anterior"), monedaDominio),
        valorNuevo = Dinero.de(valorNuevo ?: error("El ajuste no tiene valor nuevo"), monedaDominio)
    )
}

private fun OperacionEntity.aTipoTransferencia(): TipoTransferencia {
    return when (tipo) {
        TipoOperacion.TRANSFERENCIA_NORMAL -> TipoTransferencia.NORMAL
        TipoOperacion.APORTE_INVERSION -> TipoTransferencia.APORTE_INVERSION
        TipoOperacion.RESCATE_INVERSION -> TipoTransferencia.RESCATE_INVERSION
        else -> error("La operacion no es una transferencia: $tipo")
    }
}

private fun <V> Map<String, V>.requireCuenta(id: String?): V {
    return this[id ?: error("La operacion no tiene cuenta")] ?: error("La cuenta no existe")
}
