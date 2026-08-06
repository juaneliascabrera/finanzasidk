package com.finanzas.core.dominio

import java.time.LocalDate

abstract class TransferTransaction(
    id: String,
    fecha: LocalDate,
    override val cuentaId: String,
    override val monto: Dinero,
    val transferencia: Transferencia
) : Transaccion(id, fecha, monto)

class TransferIngreso(
    id: String,
    transferencia: Transferencia,
    cuentaId: String,
    fecha: LocalDate,
    monto: Dinero
) : TransferTransaction(id, fecha, cuentaId, monto, transferencia) {
    val associatedWithdraw: TransferEgreso get() = transferencia.transferEgreso

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

    override fun afectarSaldo(saldo: Dinero): Dinero = saldo - monto
}
