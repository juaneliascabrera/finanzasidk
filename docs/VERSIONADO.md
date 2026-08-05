# Versionado y flujo de trabajo

## Principio general

Este proyecto se desarrolla de forma **intencional y justificada**. No se escribe código por inercia, ni por "por si acaso". Cada cambio significativo debe poder responder:

- ¿Qué problema concreto soluciona?
- ¿Por qué esta solución y no otra?
- ¿Qué se espera lograr con este cambio?

## Ciclo de trabajo

### 1. Justificación previa (issue)

Antes de desarrollar cualquier funcionalidad no trivial, se debe crear un issue con una justificación explicativa y autocontenida. El issue debe incluir:

- **Contexto:** ¿dónde estamos y qué falta?
- **Problema:** ¿qué fricción o error resuelve?
- **Solución propuesta:** ¿qué se va a construir y cómo?
- **Alternativas consideradas:** ¿por qué se descartaron?
- **Criterios de aceptación:** ¿cómo sabemos que está terminado?
- **Impacto en VISION.md:** ¿esto respeta los constraints de simplicidad, mobile-first, offline-first?

El desarrollador no avanza hasta que el propietario del proyecto apruebe el issue.

### 2. Desarrollo

- Trabajar sobre la rama principal `main` es aceptable mientras el proyecto sea personal y pequeño.
- Si un issue requiere experimentación larga o puede romper el flujo, se puede crear una rama temporal con nombre descriptivo: `feat/nombre-corto-del-cambio`.
- El código debe ser mínimo para cumplir el objetivo. No se anticipan funcionalidades futuras.

### 3. Commits

- Los commits deben ser **continuos y con criterio**: no uno por cada línea, pero tampoco un mega-commit con 20 cambios mezclados.
- Cada commit debe representar un paso lógico y comprensible.
- Mensaje de commit en español, imperativo, descriptivo:

  ```
  feat: agrega pantalla de registro de gasto rápido
  fix: corrige cálculo de presupuesto restante
  docs: actualiza VISION.md con constraint de cuentas de inversión
  refactor: simplifica modelo de categorías
  ```

- Si un commit introduce un cambio de arquitectura importante, el mensaje debe explicar por qué.

### 4. Versionado semántico

Cuando el proyecto tenga una versión usable, se usará **SemVer**:

- `MAJOR`: cambios que rompen compatibilidad o rediseñan el modelo.
- `MINOR`: nuevas funcionalidades que no rompen nada.
- `PATCH`: correcciones y ajustes pequeños.

En el MVP, el versionado se maneja de forma manual con tags de Git:

```bash
git tag -a v0.1.0 -m "Primer MVP funcional: registro de gastos e ingresos"
```

## Qué no se hace sin justificación

- No se agregan dependencias nuevas sin explicar por qué no se puede resolver con lo que ya hay.
- No se agrega persistencia en la nube sin un issue aprobado.
- No se agrega autenticación de usuarios sin un issue aprobado.
- No se agregan reportes gráficos complejos sin un issue aprobado.
- No se agregan notificaciones automáticas sin un issue aprobado.

## Documentación

- Toda la documentación del proyecto vive en `docs/`.
- `VISION.md` es el documento vivo de referencia para decisiones de diseño.
- `VERSIONADO.md` es este archivo.
- Antes de cada cambio de arquitectura importante, se actualiza la documentación correspondiente.

---

Última actualización: agosto 2026.
