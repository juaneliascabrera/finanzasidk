package com.finanzas.core.dominio

import java.math.BigDecimal
import java.math.RoundingMode

class Dinero private constructor(
    val importe: BigDecimal,
    val moneda: Moneda
) {
    operator fun plus(otro: Dinero): Dinero {
        verificarMismaMoneda(otro)
        return crear(importe.add(otro.importe), moneda)
    }

    operator fun minus(otro: Dinero): Dinero {
        verificarMismaMoneda(otro)
        return crear(importe.subtract(otro.importe), moneda)
    }

    override fun equals(otro: Any?): Boolean {
        return otro is Dinero &&
            moneda == otro.moneda &&
            importe.compareTo(otro.importe) == 0
    }

    override fun hashCode(): Int {
        return 31 * moneda.hashCode() + importe.stripTrailingZeros().hashCode()
    }

    override fun toString(): String {
        return "Dinero(importe=$importe, moneda=$moneda)"
    }

    private fun verificarMismaMoneda(otro: Dinero) {
        require(moneda == otro.moneda) {
            "No se pueden operar importes de monedas distintas"
        }
    }

    companion object {
        fun ars(valor: String): Dinero = crear(BigDecimal(valor), Moneda.ARS)

        fun usd(valor: String): Dinero = crear(BigDecimal(valor), Moneda.USD)

        fun cero(moneda: Moneda): Dinero = crear(BigDecimal.ZERO, moneda)

        fun de(valor: String, moneda: Moneda): Dinero = crear(BigDecimal(valor), moneda)

        private fun crear(valor: BigDecimal, moneda: Moneda): Dinero {
            require(valor.scale() <= 2) {
                "El importe no puede tener mas de dos decimales"
            }

            return Dinero(valor.setScale(2, RoundingMode.UNNECESSARY), moneda)
        }
    }
}
