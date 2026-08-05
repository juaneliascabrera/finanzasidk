package com.finanzas.core.dominio

import java.time.YearMonth

class Presupuesto(
    val id: String,
    val categoria: Categoria,
    val mes: YearMonth,
    val limite: Dinero
) {
    init {
        require(id.isNotBlank()) { "El id del presupuesto no puede estar vacio" }
        require(limite.importe >= Dinero.cero(limite.moneda).importe) {
            "El limite de un presupuesto no puede ser negativo"
        }
    }

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

    override fun equals(otro: Any?): Boolean {
        return otro is Presupuesto && id == otro.id
    }

    override fun hashCode(): Int = id.hashCode()

    override fun toString(): String {
        return "Presupuesto(id=$id, categoria=$categoria, mes=$mes, limite=$limite)"
    }
}
