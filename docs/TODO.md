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

## 2. Guarda `OPERATIVA` y rol de cuenta en las patas de transferencia

`Ingreso` y `Egreso` sobrescriben `validarRegistroEn` para rechazar cuentas de
inversión. Las patas de transferencia también deben verificar que la cuenta sea
operativa y que corresponda al rol de la pata.

La validación del constructor de `Transferencia` no alcanza, porque la API
pública `Cuenta.registrar(pataDeTransferencia)` también permite registrar patas
creadas manualmente.

> Nota: cuando se implementen aportes y rescates (que sí tocan cuentas de
> inversión), la corrección no debería ser "agregar `OPERATIVA` a las patas"
> a secas, sino modelar esas operaciones aparte para que la guarda quede en
> cada tipo de operación según corresponda.

### Estado: RESUELTO

Los tests que guiaron la corrección verifican que:

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

También se verifica que una pata no pueda registrarse en una cuenta operativa
distinta de la cuenta origen o destino correspondiente.

La implementación valida el tipo de cuenta y la correspondencia entre el rol de
la pata y la cuenta de la transferencia. Esto no bloquea futuros aportes y
rescates, que deberán tener clases de operación diferentes con sus propias
reglas.

---

## 3. Unicidad local de ids de transacciones

Las patas se identifican con `"$id:salida"` y `"$id:entrada"`. La regla del
dominio es que dentro de una misma cuenta no puede existir más de una
transacción con el mismo id, sin importar el tipo concreto:

- Un `Ingreso` y otro `Ingreso` no pueden repetir id.
- Un `Ingreso` y un `Egreso` tampoco pueden repetir id.
- Una transacción manual no puede usar el id de una pata ya registrada.

La unicidad es local a cada cuenta. La misma cadena podría existir en cuentas
distintas, aunque la capa de aplicación podrá imponer una política más amplia
si más adelante fuera necesario.

### Estado: RESUELTO

`Cuenta.registrar` rechaza cualquier transacción cuyo id ya exista en esa cuenta,
sin depender de la clase concreta ni de `equals`.

Tests que cubren el comportamiento:

```kotlin
@Test
fun no_puede_registrar_dos_transacciones_de_distinto_tipo_con_el_mismo_id() {
    cuenta.registrar(ingreso)
    assertFailsWith<IllegalArgumentException> {
        cuenta.registrar(egreso)
    }
}
```

También se cubre la colisión entre una transacción manual y el id de una pata.

Se mantiene `Transaccion.equals` por id: bajo esta regla, dos transacciones con
el mismo id representan la misma identidad lógica. La protección se aplica al
registrar dentro de la cuenta.

---

## 4. Una cuenta permite registrar dos veces la misma transacción

Registrar dos veces un mismo `Ingreso` duplicaba el efecto en el saldo.

Nota: `solucion.st` también lo permite (`register:` agrega sin validar), así que
es un comportamiento heredado del ejemplo. Se deja anotado como decisión.

### Estado: RESUELTO

Test que cubre el comportamiento:

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

El segundo `registrar` ahora lanza `IllegalArgumentException` y no modifica la
lista ni el saldo de la cuenta.

---

## 5. Construcción manual de patas inconsistentes

Las patas de transferencia son construidas automáticamente por `Transferencia`,
pero sus constructores siguen siendo accesibles. Una pata creada manualmente no
debe poder declarar otra cuenta, monto o fecha que los de su transferencia padre.

### Estado: RESUELTO

Al registrar una pata se valida:

- que la cuenta sea operativa;
- que sea la cuenta origen o destino correcta según el rol;
- que el monto coincida con el de la transferencia;
- que la fecha coincida con la de la transferencia.

Los tests cubren patas con cuenta incorrecta, cuenta de inversión, monto
incorrecto y fecha incorrecta.

---

## 6. Registro parcialmente exitoso de una transferencia

Una transferencia registra dos patas en dos cuentas distintas. Si la segunda
pata era rechazada, la primera podía quedar registrada, dejando las cuentas en
un estado que no representaba ninguna transferencia válida.

### Estado: RESUELTO

La transferencia valida ambas patas antes de mutar cualquiera de las cuentas.
El test correspondiente verifica que, si el id de la pata de entrada ya existe,
la cuenta origen no conserve una salida huérfana.

---

## 7. Variantes de transferencia

La transferencia normal, los aportes y los rescates comparten la misma
estructura de operación con dos patas, pero tienen distintas reglas para los
tipos de cuenta.

### Estado: RESUELTO

`Transferencia` ahora compone un `TipoTransferencia` sellado con las variantes
`NORMAL`, `APORTE_INVERSION` y `RESCATE_INVERSION`. Las reglas de cada variante
se validan a través de ese tipo, sin condicionales dispersos en la transferencia.

---

## 8. Ajustes de valuación

Una cuenta de inversión puede cambiar de valor sin que exista un movimiento de
dinero entre cuentas. El usuario informa el valor actual y el dominio calcula la
variación respecto del saldo anterior.

### Estado: RESUELTO

`AjusteValuacion` es una `Transaccion` que conserva `valorAnterior` y
`valorNuevo`. Su monto es la diferencia firmada entre ambos valores. Solo se
registra en cuentas de inversión y exige que el valor anterior coincida con el
saldo actual de la cuenta. No es un ingreso, egreso ni transferencia y por eso
no afecta presupuestos. `Cuenta.registrarAjusteValuacion` recibe el valor nuevo,
obtiene el anterior desde el saldo actual y registra el ajuste histórico.

---

Última actualización: agosto 2026.
