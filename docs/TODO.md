# TODO del modelo de dominio

Documento que recoge diferencias e inconsistencias del modelo Kotlin respecto de
`ejemplo/solucion.st` y del alcance actual definido en `MODELO-DOMINIO.md`.

Para cada caso pendiente se propone un test que hoy **debería fallar**, para
usarlo como semilla del ciclo rojo-verde-refactor de TDD.

---

## 1. Navegación hermana de las patas de una transferencia

Diferencia respecto de `solucion.st`.

En Smalltalk cada pata guarda `associatedTransfer` (una referencia al objeto
`Transfer` padre) y eso permite navegar a la pata hermana:

```smalltalk
transferDeposit associatedWithdraw   "-> del depósito a la extracción"
transferWithdraw  associatedDeposit  "-> de la extracción al depósito"
```

En la primera versión Kotlin las patas guardaban solo `transferenciaId: String`,
un id, sin ningún mecanismo para llegar al padre ni a la pata hermana.

### Estado: RESUELTO (refactor)

Las patas ahora guardan `transferencia: Transferencia` (referencia al padre) y
exponen `associatedDeposit` / `associatedWithdraw`. La limitación de Kotlin de
no poder referenciar `this` en un property initializer se resuelve construyendo
las patas dentro del `init` con `lateinit var`.

Test que cubre el comportamiento (pasante):

```kotlin
@Test
fun la_salida_conoce_la_entrada_asociada_y_viceversa() {
    val transferencia = Transferencia(
        id = "transferencia-1",
        fecha = LocalDate.of(2026, 8, 5),
        cuentaOrigen = cuentaBrubank(),
        cuentaDestino = cuentaEfectivo(),
        monto = Dinero.ars("25.00")
    )
    assertSame(transferencia.transferIngreso, transferencia.transferEgreso.associatedDeposit)
    assertSame(transferencia.transferEgreso, transferencia.transferIngreso.associatedWithdraw)
}
```

---

## 2. Guarda `OPERATIVA` inconsistente en las patas de transferencia

`Ingreso` y `Egreso` sobrescriben `validarRegistroEn` para rechazar cuentas de
inversión, pero `TransferIngreso` / `TransferEgreso` **no** lo hacen: heredan la
versión base de `Transaccion` que solo valida `cuentaId` y moneda.

El hueco queda tapado hoy por `Transferencia` (permite ambas cuentas operativas
solo), pero la API pública `Cuenta.registrar(pataDeTransferencia)` registra una
pata en una cuenta de inversión sin error.

> Nota: cuando se implementen aportes y rescates (que sí tocan cuentas de
> inversión), la corrección no debería ser "agregar `OPERATIVA` a las patas"
> a secas, sino modelar esas operaciones aparte para que la guarda quede en
> cada tipo de operación según corresponda.

### Estado: PENDIENTE

Test que hoy falla (semilla TDD):

```kotlin
@Test
fun rechaza_una_pata_de_transferencia_en_una_cuenta_de_inversion() {
    val inversion = cuentaInversion()
    val origen = cuentaBrubank()
    val destino = cuentaEfectivo()
    val transferencia = Transferencia(
        id = "transferencia-1",
        fecha = LocalDate.of(2026, 8, 5),
        cuentaOrigen = origen,
        cuentaDestino = destino,
        monto = Dinero.ars("25.00")
    )
    val pata = TransferIngreso(
        id = "pata-inversion",
        transferencia = transferencia,
        cuentaId = inversion.id,
        fecha = LocalDate.of(2026, 8, 5),
        monto = Dinero.ars("25.00")
    )
    assertFailsWith<IllegalArgumentException> {
        inversion.registrar(pata)
    }
}
```

Hoy `inversion.registrar(pata)` no lanza ninguna excepción, por lo que el
`assertFailsWith` falla (la pata se registra sola en la cuenta de inversión).

---

## 3. Ids de patas derivados por concatenación

Las patas se identifican con `"$id:salida"` y `"$id:entrada"`. Eso expone dos
problemas dentro del alcance actual:

- **Colisión de ids con transacciones ingresadas por el usuario**: si el usuario
  crea un `Ingreso` con id `"t1:entrada"`, coincide con el id derivado de la
  pata de una transferencia con id `"t1".
- **`equals` tipo a ciego**: `Transaccion.equals` compara solo `id` ignorando el
  tipo, así que un `Ingreso` y un `TransferEgreso` con el mismo id se consideran
  el mismo objeto.

### Estado: PENDIENTE

Test que hoy falla:

```kotlin
@Test
fun la_entrada_de_una_transferencia_no_es_igual_a_un_ingreso_con_el_mismo_id() {
    val transferencia = Transferencia(
        id = "transferencia-1",
        fecha = LocalDate.of(2026, 8, 5),
        cuentaOrigen = cuentaBrubank(),
        cuentaDestino = cuentaEfectivo(),
        monto = Dinero.ars("25.00")
    )
    val ingreso = Ingreso(
        id = "transferencia-1:entrada",
        cuentaId = "brubank",
        fecha = LocalDate.of(2026, 8, 5),
        monto = Dinero.ars("25.00")
    )
    assertNotEquals(transferencia.transferIngreso, ingreso)
}
```

Hoy ambos tienen id `"transferencia-1:entrada"` y `equals` por id los considera
iguales, por lo que el `assertNotEquals` falla.

Idea a evaluar (no urgente): derive ids por solo el id de la transferencia, o que
`equals` de `Transaccion` incluya el tipo además del id.

---

## 4. Una cuenta permite registrar dos veces la misma transacción

`Cuenta.registrar` no rechaza registrar dos veces la misma transacción ni otra
con el mismo `id`. Registrar dos veces un mismo `Ingreso` duplica el efecto en
el saldo.

Nota: `solucion.st` también lo permite (`register:` agrega sin validar), así que
es un comportamiento heredado del ejemplo. Se deja anotado como decisión.

### Estado: PENDIENTE (baja prioridad)

Test que hoy falla:

```kotlin
@Test
fun no_se_puede_registrar_dos_veces_la_misma_transaccion() {
    val cuenta = cuentaBrubank()
    val ingreso = Ingreso(
        id = "ingreso-1",
        cuentaId = "brubank",
        fecha = LocalDate.of(2026, 8, 1),
        monto = Dinero.ars("100.00")
    )
    cuenta.registrar(ingreso)
    assertFailsWith<IllegalArgumentException> {
        cuenta.registrar(ingreso)
    }
}
```

Hoy el segundo `registrar` no lanza una excepción y el saldo queda duplicado.

---

Última actualización: agosto 2026.