# Persistencia

La persistencia vive en el módulo `persistence` y no forma parte de `core`.
Este módulo no contiene UI ni depende de Android: usa Room sobre la JVM y el
driver SQLite bundled.

## Responsabilidades

`persistence` se ocupa de:

- convertir objetos del dominio a filas de SQLite;
- reconstruir cuentas y operaciones en el orden correcto;
- guardar categorías y presupuestos;
- guardar ingresos, egresos y ajustes de valuación;
- guardar transferencias normales, aportes y rescates;
- validar las operaciones contra el estado persistido antes de insertarlas.
- actualizar cuentas, categorías y presupuestos;
- eliminar presupuestos;
- eliminar cuentas y categorías solo cuando no tengan referencias.

La capa de datos expone `FinanzasRepository`, cuyas operaciones son `suspend`
porque Room para JVM/KMP exige acceso asíncrono a sus DAOs.

## Fuente persistente

Las transferencias se guardan como una sola operación padre. Sus patas no se
persisten como filas independientes: se reconstruyen cuando se carga la
transferencia en el dominio.

Esto evita duplicar datos y mantiene la relación entre transferencia y patas
consistente.

Las operaciones persistidas son:

- `INGRESO`;
- `EGRESO`;
- `AJUSTE_VALUACION`;
- `TRANSFERENCIA_NORMAL`;
- `APORTE_INVERSION`;
- `RESCATE_INVERSION`.

Cada operación tiene un `ordenRegistro` generado por SQLite. La reconstrucción
usa ese orden, no la fecha de la operación, porque el dominio permite fechas
pasadas y los ajustes de valuación dependen del saldo que existía al momento de
registrarse.

## Esquema

La base contiene cuatro tablas:

- `cuentas`: datos persistibles de una cuenta.
- `categorias`: categorías personalizables.
- `presupuestos`: límites mensuales asociados a categorías.
- `operaciones`: operaciones del dominio y sus datos específicos.

Los importes se almacenan como texto decimal y la moneda como texto. No se usa
`Double` en ningún punto del almacenamiento.

El esquema generado por Room se conserva en `persistence/schemas` para poder
comparar cambios futuros y preparar migraciones.

## Uso

La base en memoria se usa en tests:

```kotlin
val database = FinanzasDatabaseFactory.enMemoria()
val repository = FinanzasRepository(database)
```

Para una base en archivo:

```kotlin
val database = FinanzasDatabaseFactory.enArchivo("finanzas.db")
```

Ejemplo de registro:

```kotlin
repository.guardarCuenta(cuenta)
repository.registrarIngreso(ingreso)
```

Para ajustes, el repositorio recibe el valor nuevo y el dominio calcula el
valor anterior desde el saldo reconstruido:

```kotlin
repository.registrarAjusteValuacion(
    id = "ajuste-1",
    cuentaId = "fci",
    fecha = fecha,
    valorNuevo = Dinero.ars("160000.00")
)
```

## Tests

Los tests de `FinanzasRepository` usan una base Room en memoria y cubren:

- guardado y recuperación de cuentas;
- categorías y presupuestos;
- ingresos y egresos;
- transferencias normales;
- aportes y rescates;
- ajustes de valuación;
- orden de registro;
- rechazo de transferencias con efectos parciales.

Se ejecutan junto con el resto del proyecto mediante:

```bash
bash lastcommand.txt
```

## Alcance pendiente

- No hay migraciones todavía: el esquema actual es versión 1.
- No hay archivado: el dominio todavía no modela el estado archivado.
- La unicidad de ids de transacciones sigue siendo local a cada cuenta.
- La UI Android existe en `app`, pero todavía requiere validación en emulador.
