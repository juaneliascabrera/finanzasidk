# Dependencias

Este archivo registra las dependencias externas del proyecto y el motivo de cada una.

## Dependencias actuales

### Kotlin JVM plugin

- **Qué es:** plugin de Gradle que permite compilar código Kotlin dirigido a la JVM.
- **Por qué existe:** el módulo `core` se ejecutará como Kotlin/JVM puro, sin depender del SDK de Android.
- **Alternativas consideradas:** Java puro, descartado porque el proyecto se desarrollará en Kotlin para integrarse posteriormente con Android.

### JUnit 5

- **Qué es:** framework para descubrir y ejecutar tests automatizados.
- **Por qué existe:** necesitamos una forma estándar de ejecutar el primer test y todos los siguientes desde Gradle.
- **Alternativas consideradas:** JUnit 4, más antiguo; Kotest, una alternativa válida pero innecesaria para el MVP.

### Kotlin Test con adaptador JUnit 5

- **Qué es:** biblioteca de aserciones de Kotlin conectada al motor de ejecución de JUnit 5.
- **Por qué existe:** permite escribir verificaciones con una API natural para Kotlin sin introducir un framework adicional.

### Room

- **Qué es:** biblioteca que genera una capa tipada sobre SQLite para guardar y consultar datos locales.
- **Por qué existe:** permite persistir el dominio offline sin mezclar SQL ni detalles de almacenamiento dentro de `core`.
- **Dónde se usa:** módulo `persistence`; `core` no depende de Room.

### SQLite Bundled

- **Qué es:** driver SQLite distribuido junto con la aplicación de persistencia.
- **Por qué existe:** permite ejecutar Room en la JVM y probar la base en memoria sin depender de Android ni de una instalación externa de SQLite.

### KSP

- **Qué es:** procesador de símbolos de Kotlin.
- **Por qué existe:** Room lo usa para generar la implementación de la base de datos y los DAOs durante la compilación.

### Kotlin Coroutines

- **Qué es:** biblioteca para ejecutar operaciones asíncronas y suspendibles.
- **Por qué existe:** Room para JVM/KMP requiere métodos `suspend` en los DAOs; también permite que la futura app no bloquee el hilo principal al acceder a SQLite.
