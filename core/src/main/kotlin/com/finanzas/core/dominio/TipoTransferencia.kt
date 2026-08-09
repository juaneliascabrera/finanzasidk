package com.finanzas.core.dominio

sealed class TipoTransferencia(
    private val tipoOrigen: TipoCuenta,
    private val tipoDestino: TipoCuenta
) {
    fun validarCuentas(origen: Cuenta, destino: Cuenta) {
        require(origen.tipo == tipoOrigen) {
            "La cuenta origen no es valida para este tipo de transferencia"
        }
        require(destino.tipo == tipoDestino) {
            "La cuenta destino no es valida para este tipo de transferencia"
        }
    }

    fun validarOrigen(cuenta: Cuenta) {
        require(cuenta.tipo == tipoOrigen) {
            "La cuenta no es valida como origen para este tipo de transferencia"
        }
    }

    fun validarDestino(cuenta: Cuenta) {
        require(cuenta.tipo == tipoDestino) {
            "La cuenta no es valida como destino para este tipo de transferencia"
        }
    }

    object NORMAL : TipoTransferencia(TipoCuenta.OPERATIVA, TipoCuenta.OPERATIVA)

    object APORTE_INVERSION : TipoTransferencia(TipoCuenta.OPERATIVA, TipoCuenta.INVERSION)

    object RESCATE_INVERSION : TipoTransferencia(TipoCuenta.INVERSION, TipoCuenta.OPERATIVA)
}
