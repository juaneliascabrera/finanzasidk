## Contexto

Ya tenemos definidos `VISION.md`, `VERSIONADO.md`, `TESTING.md` y `AGENTS.md`. El usuario aprobó un enfoque de proyecto separado en dos módulos:
- Un módulo `core` con Kotlin puro para la lógica de negocio, modelos y tests.
- Un módulo `app` Android para la interfaz visual, que se desarrollará más adelante.

Este issue cubre la creación del módulo `core` y su modelo de dominio inicial.

## Problema

No existe aún una representación del dominio del problema (cuentas, transacciones, categorías, presupuestos). Sin un modelo claro y testeado, cualquier pantalla que se construya después estará sobre una base inestable.

## Solución propuesta

Crear un módulo Gradle `core` con Kotlin puro (sin dependencias de Android) que contenga:

1. **Entidades de dominio:**
   - `Cuenta`: representa una billetera, cuenta bancaria o cuenta de inversión. Tiene nombre, moneda y tipo (normal o inversión).
   - `Transaccion`: abstracción común de ingresos, egresos y operaciones internas, identificada por `id`.
   - `Ingreso`: representa dinero externo que entra a una cuenta operativa.
   - `Egreso`: representa un gasto asociado a una cuenta y categoría opcional.
   - `Categoría`: representa una categoría de gasto o ingreso (comida, transporte, etc.).
   - `Presupuesto`: representa un límite mensual para una categoría, en una moneda determinada.

2. **Lógica de negocio:**
   - Calcular el saldo de una cuenta a partir de sus transacciones.
   - Calcular el total de gastos e ingresos de un período.
   - Calcular cuánto queda de un presupuesto según los gastos de su categoría.
   - Restricción: las cuentas de inversión no permiten registrar ingresos ni egresos directos.

3. **Tests unitarios:**
   - Cobertura de los cálculos principales.
   - Tests de comportamiento de cuentas de inversión.
   - Uso de fakes manuales o datos de prueba directos, sin inyección de dependencias.

## Alternativas consideradas

- **Poner todo dentro del módulo `app` de Android:** descartado porque obligaría a tener configurado el SDK de Android para poder compilar y testear la lógica pura. Separar el `core` permite testear con solo JDK y Gradle.
- **Usar una biblioteca de persistencia en el core:** descartado. La persistencia es responsabilidad de la capa de datos, no del dominio. El `core` solo define entidades y lógica.

## Criterios de aceptación

- [x] Existe un módulo Gradle `core` con Kotlin puro.
- [x] Compila correctamente usando el Gradle Wrapper dentro de Docker.
- [x] Los tests unitarios corren con JUnit 5.
- [x] El modelo incluye `Cuenta`, `Transaccion`, `Ingreso`, `Egreso`, `Categoría` y `Presupuesto`.
- [x] Existe lógica testeada para saldo de cuenta y restante de presupuesto.
- [x] Las cuentas de inversión rechazan ingresos y egresos directos.
- [x] Existe una transferencia normal entre cuentas operativas de la misma moneda.
- [x] No hay dependencias de Android en el módulo `core`.
- [x] Está implementada la jerarquía común `Transaccion`.
- [x] Están implementados aportes y rescates de inversión.
- [x] Están implementados los ajustes de valuación.
- [x] Existe documentación de uso del repositorio equivalente a un README.
- [x] El `README.md` del proyecto tiene instrucciones mínimas para ejecutar los tests.

## Impacto en VISION.md

Respeta los constraints de simplicidad, offline-first y mobile-first. El modelo se mantiene agnóstico a la interfaz, lo que permite iterar rápido sobre la lógica antes de construir la app visual.

## Notas

- Se usará JUnit 5 (Jupiter) para tests unitarios.
- Se usará Gradle con Kotlin DSL para la configuración del proyecto.
- No se agregará inyección de dependencias ni arquitectura MVVM en este issue: son responsabilidades de la capa de presentación (`app`).
