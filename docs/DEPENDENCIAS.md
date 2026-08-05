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
