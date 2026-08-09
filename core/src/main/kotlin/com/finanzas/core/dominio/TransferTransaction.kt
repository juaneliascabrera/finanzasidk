package com.finanzas.core.dominio

import java.time.LocalDate

abstract class TransferTransaction(
    id: String,
    fecha: LocalDate,
    override val cuentaId: String,
    override val monto: Dinero,
    val transferencia: Transferencia
) : Transaccion(id, fecha, monto) {
    override fun validarRegistroEn(cuenta: Cuenta) {
        super.validarRegistroEn(cuenta)
        require(fecha == transferencia.fecha) {
            "La pata debe tener la fecha de la transferencia"
        }
        require(monto == transferencia.monto) {
            "La pata debe tener el monto de la transferencia"
        }
    }
}

class TransferIngreso(
    id: String,
    transferencia: Transferencia,
    cuentaId: String,
    fecha: LocalDate,
    monto: Dinero
) : TransferTransaction(id, fecha, cuentaId, monto, transferencia) {
    val associatedWithdraw: TransferEgreso get() = transferencia.transferEgreso

    override fun validarRegistroEn(cuenta: Cuenta) {
        super.validarRegistroEn(cuenta)
        transferencia.tipo.validarDestino(cuenta)
        require(cuenta.id == transferencia.cuentaDestino.id) {
            "La entrada debe registrarse en la cuenta destino de la transferencia"
        }
    }

    override fun afectarSaldo(saldo: Dinero): Dinero = saldo + monto
}

class TransferEgreso(
    id: String,
    transferencia: Transferencia,
    cuentaId: String,
    fecha: LocalDate,
    monto: Dinero
) : TransferTransaction(id, fecha, cuentaId, monto, transferencia) {
    val associatedDeposit: TransferIngreso get() = transferencia.transferIngreso

    override fun validarRegistroEn(cuenta: Cuenta) {
        super.validarRegistroEn(cuenta)
        transferencia.tipo.validarOrigen(cuenta)
        require(cuenta.id == transferencia.cuentaOrigen.id) {
            "La salida debe registrarse en la cuenta origen de la transferencia"
        }
    }

    override fun afectarSaldo(saldo: Dinero): Dinero = saldo - monto
}
