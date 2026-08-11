# Comandos del proyecto

## Ejecutar tests JVM

Desde la raíz del repositorio:

```bash
docker volume create finanzas-gradle-home >/dev/null

docker run --rm \
  -e GRADLE_USER_HOME=/home/gradle/.gradle \
  -v "$PWD":/app \
  -v finanzas-gradle-home:/home/gradle/.gradle \
  -w /app \
  gradle:8.12.1-jdk21 \
  ./gradlew :core:test :persistence:test --no-daemon --console=plain
```

Este comando:

- Ejecuta los tests JVM de `core` y `persistence`.
- Ejecuta siempre los tests, aunque Gradle los considere `UP-TO-DATE`.
- Muestra explícitamente los tests `PASSED`, `FAILED` y `SKIPPED`.
- Usa Docker para evitar instalar Java o Gradle en el sistema host.
- Usa la imagen `gradle:8.12.1-jdk21`, que fija las versiones de Gradle y Java.
- Conserva las descargas en el volumen `finanzas-gradle-home` para acelerar ejecuciones posteriores.
- Elimina el contenedor al terminar, pero no elimina el volumen de caché.

El reporte HTML queda disponible en:

```text
core/build/reports/tests/test/index.html
```

## `UP-TO-DATE` versus ejecución real

Las tareas de compilación pueden mostrar:

```text
:core:test UP-TO-DATE
```

Eso significa que reutilizaron un resultado anterior porque no detectaron cambios. Es correcto y permite que el build sea rápido. Las tareas `test` están configuradas aparte para ejecutarse siempre, por lo que deberían mostrar los casos `PASSED` en cada invocación.

## Validar Android

Estos comandos requieren Android SDK configurado:

```bash
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest
./gradlew :app:connectedDebugAndroidTest
```

El último requiere un emulador o dispositivo conectado.

## Build portable con GitHub Actions

El workflow `.github/workflows/android.yml` se ejecuta automáticamente en cada
push a `main` y puede lanzarse manualmente desde GitHub con **Actions** →
**Android** → **Run workflow**.

El workflow:

- configura Java, Gradle y Android SDK;
- ejecuta los tests de `core` y `persistence`;
- compila `app-debug.apk`;
- publica el artifact `finanzas-debug-apk` durante 14 días.

Desde el celular, se puede abrir la ejecución terminada en GitHub, descargar el
artifact y extraer `app-debug.apk` para instalarlo.

## Comando local JVM

Cuando exista un entorno con Java configurado, el equivalente será:

```bash
./gradlew :core:test :persistence:test
```

---

Última actualización: agosto 2026.
