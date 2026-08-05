# Comandos del proyecto

## Ejecutar todos los tests

Desde la raíz del repositorio:

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

Este comando:

- Ejecuta los tests de todos los módulos del proyecto.
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

Eso significa que reutilizaron un resultado anterior porque no detectaron cambios. Es correcto y permite que el build sea rápido. La tarea `test` está configurada aparte para ejecutarse siempre, por lo que debería mostrar los casos `PASSED` en cada invocación.

## Comando local futuro

Cuando exista un entorno con Java configurado, el equivalente será:

```bash
./gradlew test
```

---

Última actualización: agosto 2026.
