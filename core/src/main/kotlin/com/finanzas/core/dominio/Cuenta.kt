package com.finanzas.core.dominio

data class Cuenta(
    val id: String,
    val nombre: String,
    val moneda: Moneda,
    val tipo: TipoCuenta,
    val saldoInicial: Dinero
) {
    init {
        require(saldoInicial.moneda == moneda) {
            "El saldo inicial debe usar la moneda de la cuenta"
        }
        require(saldoInicial.importe >= Dinero.cero(moneda).importe) {
            "El saldo inicial no puede ser negativo"
        }
    }

    fun saldo(movimientos: List<Movimiento>): Dinero {
        return movimientos
            .asSequence()
            .filter { it.cuentaId == id }
            .onEach { movimiento ->
                require(movimiento.monto.moneda == moneda) {
                    "El movimiento debe usar la moneda de la cuenta"
                }
            }
            .fold(saldoInicial) { saldo, movimiento ->
                when (movimiento) {
                    is Ingreso -> saldo + movimiento.monto
                    is Egreso -> saldo - movimiento.monto
                }
            }
    }
}
