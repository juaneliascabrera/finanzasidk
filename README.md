# Finanzas

Aplicación móvil de finanzas personales, offline-first y enfocada en registrar
ingresos, egresos, presupuestos e inversiones con la menor fricción posible.

El proyecto todavía está en la etapa previa a la UI. Actualmente existen los
módulos `core` y `persistence`, escritos para la JVM y sin dependencias de una
interfaz Android.

## Estado actual

El proyecto contiene y prueba:

- Dinero con `BigDecimal`, monedas ARS y USD, y hasta dos decimales.
- Categorías identificadas por id.
- Presupuestos mensuales por categoría.
- Cuentas operativas y de inversión.
- Ingresos y egresos.
- Transferencias normales entre cuentas operativas.
- Aportes desde cuentas operativas hacia cuentas de inversión.
- Rescates desde cuentas de inversión hacia cuentas operativas.
- Ajustes de valuación de inversiones.
- Unicidad local de ids de transacciones dentro de cada cuenta.
- Validaciones de moneda, tipo de cuenta, cuentas involucradas y montos.
- Registro atómico de las dos patas de una transferencia.

El módulo `persistence` agrega persistencia local con Room y SQLite bundled, sin UI.

Todavía no están implementados:

- Módulo Android `app`.
- Interfaz visual.
- Sincronización con bancos, billeteras o brokers.

## Estructura

```text
core/
  src/main/kotlin/com/finanzas/core/dominio/
    AjusteValuacion.kt
    Categoria.kt
    Cuenta.kt
    Dinero.kt
    Egreso.kt
    Ingreso.kt
    Moneda.kt
    Presupuesto.kt
    TipoCuenta.kt
    TipoTransferencia.kt
    Transaccion.kt
    TransferTransaction.kt
    Transferencia.kt
  src/test/kotlin/com/finanzas/core/dominio/

persistence/
  src/main/kotlin/com/finanzas/persistence/
  src/test/kotlin/com/finanzas/persistence/
  schemas/
```

El modelo de dominio no depende de Android, Room ni una interfaz visual. Esto
permite probar la lógica financiera sin emulador ni dispositivo.

## Operaciones principales

### Ingreso y egreso

Los ingresos y egresos se construyen y se registran en una cuenta:

```kotlin
val ingreso = Ingreso(
    id = "ingreso-1",
    cuentaId = cuenta.id,
    fecha = fecha,
    monto = Dinero.ars("100000.00")
)

cuenta.registrar(ingreso)
```

Un egreso puede tener una categoría opcional. Las cuentas de inversión no
aceptan ingresos ni egresos directos.

### Transferencias

`Transferencia` es una operación compuesta por dos patas y un
`TipoTransferencia`:

```kotlin
Transferencia(
    id = "transferencia-1",
    cuentaOrigen = brubank,
    cuentaDestino = efectivo,
    fecha = fecha,
    monto = Dinero.ars("25000.00"),
    tipo = TipoTransferencia.NORMAL
)
```

Los tipos disponibles son:

- `NORMAL`: operativa hacia operativa.
- `APORTE_INVERSION`: operativa hacia inversión.
- `RESCATE_INVERSION`: inversión hacia operativa.

La construcción valida las dos patas antes de modificar las cuentas. Si una no
puede registrarse, ninguna queda registrada.

### Ajuste de valuación

El usuario informa el nuevo valor y la cuenta calcula el valor anterior desde su
saldo actual:

```kotlin
val ajuste = cuentaInversion.registrarAjusteValuacion(
    id = "ajuste-1",
    fecha = fecha,
    valorNuevo = Dinero.ars("160000.00")
)
```

El ajuste conserva el valor anterior y el nuevo, y registra la diferencia como
una variación positiva o negativa. No representa dinero movido entre cuentas y
no afecta presupuestos.

## Reglas de dinero

- No se convierten monedas automáticamente.
- No se suman ni restan importes de monedas distintas.
- Los importes admiten como máximo dos decimales.
- Se usa `BigDecimal`, no `Double`.
- Los ajustes de valuación pueden tener una variación negativa, aunque sus
  valores anterior y nuevo no pueden ser negativos.

## Ejecutar los tests

Con un entorno local de Java y Gradle:

```bash
./gradlew :core:test
```

Desde Docker, el comando recomendado para ejecutar todos los tests es:

```bash
docker volume create finanzas-gradle-home >/dev/null

docker run --rm \
  -e GRADLE_USER_HOME=/home/gradle/.gradle \
  -v "$PWD":/app \
  -v finanzas-gradle-home:/home/gradle/.gradle \
  -w /app \
  gradle:8.12.1-jdk21 \
  ./gradlew test --no-daemon --console=plain
```

La documentación ampliada de testing está en `docs/TESTING.md` y la versión
canónica del comando está en `docs/COMANDOS.md`.

La arquitectura y el esquema de almacenamiento están documentados en
`docs/PERSISTENCIA.md`.

## Decisiones de diseño

- `core` se mantiene independiente de Android y de la persistencia.
- `persistence` contiene Room, SQLite y los mappers; no se mezclan con el dominio.
- Las operaciones internas comparten la entidad `Transferencia` y delegan las
  reglas específicas en `TipoTransferencia`.
- Las patas mantienen una referencia al objeto padre en memoria; la capa de
  persistencia podrá reconstruir esas relaciones usando ids.
- Los ids de transacciones son únicos dentro de cada cuenta.
- No se agrega inyección de dependencias ni una arquitectura de presentación
  antes de que exista una necesidad concreta.

## Próximos pasos

1. Agregar migraciones y operaciones de actualización/eliminación persistente.
2. Revisar el límite de rescates respecto del saldo disponible.
3. Crear el módulo Android `app`.
4. Construir la pantalla principal de registro rápido de egresos.

La documentación funcional completa está en `docs/MODELO-DOMINIO.md`.
