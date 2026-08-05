package com.finanzas.core.dominio

import java.time.YearMonth

data class Presupuesto(
    val categoria: Categoria,
    val mes: YearMonth,
    val limite: Dinero
) {
    fun restante(egresos: List<Egreso>): Dinero {
        val gastado = egresos
            .asSequence()
            .filter { it.fecha.let(YearMonth::from) == mes }
            .filter { it.categoria == categoria }
            .filter { it.monto.moneda == limite.moneda }
            .map { it.monto }
            .fold(Dinero.cero(limite.moneda), Dinero::plus)

        return limite - gastado
    }
}
