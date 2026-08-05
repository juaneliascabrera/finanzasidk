# Estrategia de testing

Este documento describe las formas de testear el proyecto para tener seguridad de que las funcionalidades críticas funcionan y de que no se rompen entre cambios.

## Principio

Testear no es un objetivo en sí mismo: es una herramienta para detectar errores tempranos en la lógica que más importa. Dado que el MVP es simple, la estrategia de testing también debe ser simple.

## Estado actual

El proyecto tiene un módulo `core` de Kotlin/JVM preparado para tests unitarios. Este módulo no depende de Android, Compose, Room ni de un dispositivo.

Para ejecutar sus tests:

```bash
./gradlew :core:test
```

Desde Docker, el comando recomendado para ejecutar todos los tests es:

La versión canónica y reutilizable de este comando está en [`COMANDOS.md`](COMANDOS.md).

```bash
docker volume create finanzas-gradle-home >/dev/null

docker run --rm \
  -e GRADLE_USER_HOME=/home/gradle/.gradle \
  -v "$PWD":/app \
  -v finanzas-gradle-home:/home/gradle/.gradle \
  -w /app \
  gradle:8.12.1-jdk21 \
  ./gradlew test --no-daemon --console=plain --rerun-tasks
```

El volumen `finanzas-gradle-home` conserva la distribución de Gradle y las dependencias descargadas entre ejecuciones. `--no-daemon` evita iniciar un proceso persistente dentro del contenedor. `test` ejecuta los tests de todos los módulos que tengan esa tarea. La tarea `test` está configurada para ejecutarse siempre, mientras que las tareas de compilación siguen usando la caché incremental.

Para ejecutar la validación completa del módulo:

```bash
./gradlew :core:check
```

La configuración de Gradle muestra explícitamente el resultado de cada test:

```text
PresupuestoTest > calcula_el_restante_de_un_presupuesto_con_un_egreso_del_mismo_mes PASSED
```

`BUILD SUCCESSFUL` indica que la tarea completa terminó sin fallos. La línea `PASSED` permite verificar además qué caso individual fue ejecutado y aprobado.

El primer test todavía no fue creado. La siguiente etapa es definir una regla de dominio y expresarla como test antes de implementar código productivo.

## 1. Tests unitarios (obligatorios desde el inicio)

**Qué testean:** lógica pura que no depende de Android ni de la base de datos.

Ejemplos:
- Calcular cuánto queda de un presupuesto después de un gasto.
- Calcular el total de gastos de un mes.
- Calcular el saldo de una cuenta después de ingresos y egresos.
- Validar que una categoría no tenga nombre vacío.

**Herramientas actuales:**
- **JUnit 5**: framework que ejecuta los tests y reporta si pasan o fallan.
- **Kotlin Test** con adaptador JUnit 5: permite usar aserciones idiomáticas de Kotlin sobre JUnit 5.

No agregamos Kotest por ahora: ofrece una sintaxis alternativa, pero no resuelve una necesidad actual y agregaría otra decisión y dependencia.

**Por qué no necesitamos inyección de dependencias:**
Para testear lógica pura no hace falta. Se crean instancias directamente con los datos de prueba. La inyección de dependencias se vuelve útil cuando una clase necesita colaboradores complejos que son difíciles de construir en un test. En este MVP, podemos evitar esa complejidad inicial usando **fakes manuales** o pasando dependencias simples por constructor.

## 2. Tests de integración para la base de datos (Room)

**Qué testean:** que las consultas de Room guardan y devuelven los datos correctos.

**Herramientas:**
- **Room in-memory database**: SQLite corre en memoria durante el test, sin tocar el almacenamiento real del dispositivo.
- **JUnit 4** con una regla de Android para inicializar Room.

**Ejemplo:**
- Insertar un gasto y verificar que se pueda recuperar por mes.
- Insertar una categoría y verificar que no se duplique.

## 3. Tests de UI con Jetpack Compose (opcional, post-MVP inicial)

**Qué testean:** que al tocar un botón se abre la pantalla correcta o que un texto aparece después de una acción.

**Herramientas:**
- **Compose Test**: biblioteca oficial para testear componentes de Jetpack Compose.
- **Espresso** (solo si hay pantallas no hechas en Compose).

**Por qué es opcional al inicio:**
Los tests de UI son más lentos y frágiles. En el MVP, la prioridad es tener tests unitarios sobre la lógica de negocio. Se agregan tests de UI cuando las pantallas principales se estabilizan.

## 4. Tests end-to-end (post-MVP)

**Qué testean:** flujos completos de usuario, desde abrir la app hasta registrar un gasto.

**Herramientas:**
- **UI Automator** o **Maestro**.

**Por qué no se usan en el MVP:**
Requieren un emulador o dispositivo real y son más lentos. Se agregan cuando el flujo central es estable.

## 5. Análisis estático de código

**Qué hace:** detecta errores de estilo, complejidad innecesaria o problemas potenciales sin ejecutar la app.

**Herramientas:**
- **ktlint**: formateo automático de código Kotlin.
- **detekt**: análisis de complejidad y posibles bugs.

**Por qué es útil:** mantiene el código legible y consistente, especialmente cuando el proyecto es open-source.

## 6. CI/CD con GitHub Actions

**Qué hace:** corre automáticamente los tests y el análisis estático cada vez que se hace push.

**Ventaja:** si no tenés acceso a tu PC o a Android Studio en este momento, igual podemos validar que el código compila y pasa los tests en un entorno remoto.

**Flujo básico:**
```
push → GitHub Actions → compila proyecto → corre tests unitarios → corre ktlint/detekt → reporta resultado
```

## 7. Smoke tests manuales

**Qué son:** pruebas rápidas que hacemos a mano en un emulador o dispositivo real.

**Ejemplo:**
- Abrir la app.
- Registrar un gasto de $10.000.
- Verificar que aparece en la lista.
- Verificar que el presupuesto se actualiza.

**Por qué siguen siendo importantes:** ningún test automático reemplaza el uso real de la app.

## Resumen de prioridades

| Prioridad | Tipo de test | Cuándo se implementa |
|-----------|--------------|----------------------|
| Alta | Tests unitarios | Desde el primer modelo |
| Media | Tests de integración con Room | Cuando haya base de datos |
| Baja | Tests de UI con Compose | Cuando las pantallas estén estables |
| Post-MVP | End-to-end, CI/CD completo | Después del MVP |

## Inyección de dependencias y testing

**Pregunta común:** ¿necesitamos inyección de dependencias para testear?

**Respuesta corta:** no para el MVP.

**Explicación:**
- Si una clase necesita un repositorio, podemos pasarle una **interfaz** e implementar una versión de mentira (**fake**) en el test.
- Si la clase es simple, podemos pasarle directamente una instancia de prueba por constructor.
- La inyección de dependencias (como Hilt o Koin) se vuelve útil cuando hay muchas clases que necesitan las mismas dependencias en muchos lugares. En el MVP no tenemos ese problema.

**Ejemplo de fake manual:**
```kotlin
interface GastoRepository {
    fun obtenerGastos(): List<Gasto>
}

class GastoRepositoryFake : GastoRepository {
    override fun obtenerGastos() = listOf(Gasto(10000.0, "Comida"))
}
```

---

Última actualización: agosto 2026.
