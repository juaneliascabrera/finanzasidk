package com.finanzas.core.dominio

class Cuenta(
    val id: String,
    val nombre: String,
    val moneda: Moneda,
    val tipo: TipoCuenta,
    val saldoInicial: Dinero
) {
    init {
        require(id.isNotBlank()) { "El id de la cuenta no puede estar vacio" }
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
                    is Ingreso -> {
                        require(tipo == TipoCuenta.OPERATIVA) {
                            "Una cuenta de inversion no puede registrar ingresos directos"
                        }
                        saldo + movimiento.monto
                    }
                    is Egreso -> {
                        require(tipo == TipoCuenta.OPERATIVA) {
                            "Una cuenta de inversion no puede registrar egresos directos"
                        }
                        saldo - movimiento.monto
                    }
                }
            }
    }

    override fun equals(otra: Any?): Boolean {
        return otra is Cuenta && id == otra.id
    }

    override fun hashCode(): Int = id.hashCode()

    override fun toString(): String {
        return "Cuenta(id=$id, nombre=$nombre, moneda=$moneda, tipo=$tipo, saldoInicial=$saldoInicial)"
    }
}
