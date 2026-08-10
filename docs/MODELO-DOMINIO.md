# Modelo de dominio

Este documento define los conceptos y reglas financieras del módulo `core`. No describe pantallas ni detalles de Android.

El objetivo es que la lógica del sistema pueda probarse sin Android, sin base de datos y sin una interfaz visual.

## Principios

- El saldo de una cuenta se obtiene de su saldo inicial y sus movimientos.
- Un ingreso, un egreso, una transferencia y un ajuste de valuación son operaciones diferentes.
- Mover dinero entre cuentas no es gastar dinero.
- Aportar dinero a una inversión no es un ingreso.
- El rendimiento de una inversión no es un ingreso disponible para gastar.
- Las operaciones entre cuentas deben respetar la moneda.
- Los importes admiten hasta dos decimales.
- Las categorías son personalizables.
- Las fechas pasadas están permitidas.

## Identidad de las entidades

Las entidades persistibles tienen un `id` textual, no derivado de su nombre:

- `Cuenta`.
- `Categoria`.
- `Ingreso`.
- `Egreso`.
- `Presupuesto`.

El `id` identifica la entidad aunque cambien sus otros datos. Los nombres son etiquetas editables y no forman parte de la identidad.

`Dinero` no tiene `id` porque es un objeto de valor: su identidad está determinada por su importe y su moneda.

Los identificadores se reciben desde afuera del dominio por ahora. La generación de IDs y su persistencia se definirán al diseñar la capa de datos.

## Moneda y dinero

El MVP contempla dos monedas:

- `ARS`: pesos argentinos.
- `USD`: dólares estadounidenses.

No se convierten monedas automáticamente. Dos importes solo pueden sumarse o compararse si tienen la misma moneda.

Los importes se representan con `BigDecimal`, con una escala máxima de dos decimales. No se usa `Double` para dinero porque sus errores de representación binaria pueden producir resultados incorrectos.

Ejemplos válidos:

```text
100.00 ARS
100.41 ARS
150.00 USD
```

## Cuenta

Una cuenta representa un lugar donde existe dinero registrado en la aplicación.

### Datos

- Identificador.
- Nombre personalizable.
- Moneda.
- Tipo: operativa o inversión.
- Saldo inicial.

El saldo inicial no es un ingreso: representa cuánto dinero ya existía en la cuenta cuando comenzó a registrarse en la aplicación.

### Cuenta operativa

Es una cuenta desde la que se puede gastar o recibir dinero.

Ejemplos:

- Cuenta bancaria de Brubank.
- Billetera asociada a MODO o Google Wallet.
- Efectivo.

Una cuenta operativa puede participar en:

- Ingresos.
- Egresos.
- Transferencias normales.
- Aportes de inversión.
- Rescates de inversión.

### Cuenta de inversión

Es una cuenta que representa dinero invertido, como un FCI o dólares destinados a ahorro/inversión.

Una cuenta de inversión puede participar en:

- Aportes de inversión.
- Rescates de inversión.
- Ajustes de valuación.

Una cuenta de inversión **no permite egresos**. Un gasto nunca puede salir directamente de una cuenta de inversión.

Un rescate de inversión sí está permitido: es una transferencia desde la cuenta de inversión hacia una cuenta operativa de la misma moneda. Luego, el gasto se registra desde la cuenta operativa.

El monto rescatado no puede superar el saldo actual de la cuenta de inversión.

## Operaciones

Todas las operaciones tienen, como mínimo:

- Identificador.
- Fecha (`LocalDate`).
- Importe o importes correspondientes.
- Cuenta o cuentas involucradas.

### Ingreso

Representa dinero que entra al sistema desde afuera.

Ejemplo:

```text
Sueldo +1.572.000 ARS en Brubank
```

Un ingreso no puede registrarse directamente en una cuenta de inversión en el MVP. Para aumentar una inversión se utiliza un aporte.

### Egreso

Representa dinero gastado.

Ejemplo:

```text
Comida -25.000 ARS desde Brubank
```

Un egreso:

- Sale de una cuenta operativa.
- Tiene un importe no negativo y una dirección de egreso.
- Puede tener una categoría.
- Puede tener descripción opcional.
- Puede tener una fecha pasada.

Durante el MVP se permite registrar temporalmente un egreso sin categoría. El sistema deberá alertar posteriormente si esos egresos permanecen sin categorizar durante demasiado tiempo. La duración y el diseño de esa alerta quedan para una decisión posterior.

### Transferencia normal

Representa un movimiento entre dos cuentas operativas de la misma moneda.

Ejemplo:

```text
Brubank -50.000 ARS
Efectivo +50.000 ARS
```

Una transferencia normal:

- No es ingreso.
- No es egreso.
- No afecta presupuestos.
- Requiere cuenta operativa de origen y cuenta operativa de destino.
- Requiere que ambas cuentas tengan la misma moneda.

### Aporte de inversión

Representa dinero que pasa de una cuenta operativa a una cuenta de inversión.

Ejemplo:

```text
Brubank       -150.000 ARS
FCI           +150.000 ARS
```

Un aporte de inversión:

- No es ingreso.
- No es egreso.
- No afecta presupuestos.
- Requiere cuenta operativa de origen.
- Requiere cuenta de inversión de destino.
- Requiere la misma moneda en ambas cuentas.

### Rescate de inversión

Representa dinero que sale de una cuenta de inversión y vuelve a una cuenta operativa de la misma moneda.

Ejemplo:

```text
FCI           -50.000 ARS
Brubank       +50.000 ARS
```

Un rescate de inversión:

- No es ingreso.
- No es egreso.
- No afecta presupuestos.
- Requiere cuenta de inversión de origen.
- Requiere cuenta operativa de destino.
- Requiere la misma moneda en ambas cuentas.

### Ajuste de valuación

Representa un cambio manual en el valor registrado de una cuenta de inversión.

Ejemplo:

```text
Valor anterior: 150.000 ARS
Valor nuevo:    160.000 ARS
Variación:       10.000 ARS
```

El ajuste de valuación:

- Solo puede aplicarse a cuentas de inversión.
- Se registra como una operación histórica.
- No es ingreso.
- No es egreso.
- No afecta presupuestos.
- Puede aumentar o disminuir el valor de la inversión.
- Debe conservar el valor anterior y el nuevo valor, o suficiente información para reconstruirlos.

El usuario ingresa el valor actual; el dominio calcula la diferencia respecto del valor registrado anterior.

## Saldo de cuenta

El saldo depende del tipo de cuenta.

### Cuenta operativa

```text
saldo inicial
+ ingresos
+ transferencias normales recibidas
+ rescates de inversión
- egresos
- transferencias normales enviadas
- aportes de inversión
```

### Cuenta de inversión

```text
saldo inicial
+ aportes de inversión
+ ajustes de valuación positivos
- rescates de inversión
- ajustes de valuación negativos
```

Un ajuste de valuación nunca se interpreta como dinero disponible en una cuenta operativa.

## Categoría

Una categoría es una clasificación personalizable para egresos.

La identidad de una categoría es siempre su `id`. El nombre es una etiqueta descriptiva y puede cambiar. Dos categorías con el mismo `id` representan la misma categoría aunque tengan nombres diferentes.

Ejemplos iniciales posibles:

- Comida.
- Transporte.
- Telefonía.
- Gimnasio.
- Salud.
- Salidas.

La aplicación no debe imponer una lista fija de categorías. El usuario puede crear, editar y eventualmente archivar categorías. Editar el nombre no debe crear una categoría nueva ni separar sus presupuestos o egresos históricos.

La relación entre un egreso y su categoría es opcional durante el período temporal de tolerancia definido en el MVP. Los presupuestos, en cambio, siempre pertenecen a una categoría concreta.

## Presupuesto

Un presupuesto define cuánto se permite gastar en una categoría durante un mes.

### Datos

- Categoría.
- Mes (`YearMonth`).
- Límite.
- Moneda.
- Identificador.

El límite puede cambiarse mes a mes. No se presupone que el límite de un mes se copie automáticamente al siguiente.

### Restante

```text
restante = límite - egresos categorizados de la categoría durante el mes
```

Solo se consideran:

- Egresos.
- De la categoría correspondiente.
- De la misma moneda.
- Con fecha dentro del mes del presupuesto.

No se consideran ingresos, transferencias, aportes, rescates ni ajustes de valuación.

El restante puede ser negativo. Eso significa que el usuario superó el límite.

## Decisiones fuera del alcance actual

- Conversión entre ARS y USD.
- Cotizaciones automáticas.
- Comisiones e impuestos de inversiones.
- Intereses o rendimientos detallados por cuotapartes.
- Retiros parciales complejos de inversiones.
- Tarjetas de crédito.
- Cuotas como operación financiera completa.
- Sincronización con bancos, billeteras o brokers.
- Usuarios múltiples.

## Estado actual del core

Actualmente están implementados y testeados:

- `Dinero` y `Moneda`.
- `Categoria` identificada por `id`.
- `Presupuesto` y cálculo de restante.
- `Cuenta` operativa e inversión.
- `Ingreso` y `Egreso` identificados por `id`.
- Jerarquía común `Transaccion` para ingresos, egresos y patas de transferencia.
- Cálculo de saldo de cuentas.
- Restricción de que las cuentas de inversión no aceptan ingresos ni egresos directos.
- `Transferencia` compuesta con `TipoTransferencia` y dos patas asociadas.
- Transferencias normales entre cuentas operativas de la misma moneda.
- Aportes de inversión entre cuentas operativas y cuentas de inversión.
- Rescates de inversión entre cuentas de inversión y cuentas operativas.
- Ajustes de valuación sobre cuentas de inversión.
- Unicidad local de ids de transacciones dentro de cada cuenta.
- Persistencia local del dominio en el módulo `persistence`, separada de `core`.

Todavía no están implementados:

- Módulo Android `app` e interfaz visual.

## Primer comportamiento implementado con TDD

El primer test puede expresar esta regla:

> Dado un presupuesto de comida de `300.000 ARS` y un egreso categorizado de `50.000 ARS` en el mismo mes, el restante del presupuesto debe ser `250.000 ARS`.

Es una regla pequeña, observable y suficientemente independiente del resto del sistema para comenzar el ciclo rojo-verde-refactor.

---

Última actualización: agosto 2026.
