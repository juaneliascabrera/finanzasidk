package com.finanzas.core.dominio

import java.time.LocalDate

class AjusteValuacion(
    override val cuentaId: String,
    fecha: LocalDate,
    val valorAnterior: Dinero,
    val valorNuevo: Dinero,
    id: String
) : Transaccion(id, fecha, valorNuevo - valorAnterior) {
    init {
        require(valorAnterior.importe >= Dinero.cero(valorAnterior.moneda).importe) {
            "El valor anterior no puede ser negativo"
        }
        require(valorNuevo.importe >= Dinero.cero(valorNuevo.moneda).importe) {
            "El valor nuevo no puede ser negativo"
        }
    }

    override fun validarRegistroEn(cuenta: Cuenta) {
        super.validarRegistroEn(cuenta)
        require(cuenta.tipo == TipoCuenta.INVERSION) {
            "Un ajuste de valuacion requiere una cuenta de inversion"
        }
        require(cuenta.saldo() == valorAnterior) {
            "El valor anterior debe coincidir con el saldo actual de la cuenta"
        }
    }

    override fun afectarSaldo(saldo: Dinero): Dinero = saldo + monto
}
