# Guías para el agente de desarrollo

Este documento contiene las reglas que debe seguir el agente de código cuando trabaje en este proyecto.

## 1. No asumir conocimiento previo del usuario

El usuario tiene formación en Ciencias de la Computación (BSc.) y experiencia desarrollando aplicaciones de escritorio, pero no necesariamente conoce el stack de Android, las bibliotecas de Kotlin, los patrones de arquitectura móvil ni las herramientas del ecosistema.

- Antes de nombrar una tecnología, biblioteca o patrón, explicar qué es en una oración.
- Antes de proponer una dependencia, explicar qué problema resuelve y por qué es necesaria.
- No usar buzzwords como justificación: "usamos MVVM porque es una buena práctica" no es suficiente.
- Preferir ejemplos concretos y analogías con tecnologías que el usuario ya conozca.

## 2. Justificar cada decisión de diseño

Cada elección tecnológica o arquitectónica debe poder responder:

- ¿Qué problema concreto resuelve?
- ¿Qué alternativas existen?
- ¿Por qué se descartaron las alternativas?
- ¿Qué costo agrega esta elección?

Si no se puede responder eso, la elección no está justificada.

## 3. No introducir complejidad anticipada

- No agregar capas de arquitectura que el MVP no necesita.
- No agregar bibliotecas que solo se "podrían llegar a usar".
- Si una funcionalidad se puede resolver con el lenguaje/base estándar, resolverla así.
- Ejemplo: para un MVP simple, no se propone inyección de dependencias sin explicar por qué es indispensable y sin demostrar que el código sin ella se vuelve difícil de mantener.

## 4. Involucrar al usuario en decisiones importantes

- No elegir un stack complejo sin presentar opciones claras.
- No implementar un patrón de arquitectura sin avisar y justificar.
- Si una decisión tiene trade-offs importantes, presentarlos en una tabla simple.

## 5. Documentar mientras se construye

- Cada issue debe ser autocontenido: qué se hace, por qué, y cómo se valida.
- Cada cambio de arquitectura importante debe actualizar la documentación correspondiente en `docs/`.
- Si se agrega una dependencia, agregar una nota en `docs/DEPENDENCIAS.md` (o similar) explicando su propósito.

## 6. Preferir el lenguaje del usuario

- La documentación y los mensajes de commit deben estar en español.
- Los nombres de variables y funciones en el código pueden estar en inglés, pero no mezclar español e inglés en la misma API.

## 7. Respetar la visión

- Antes de implementar cualquier funcionalidad, revisar `VISION.md`.
- Si la funcionalidad contradice algún constraint (simplicidad, offline-first, mobile-first), no implementarla sin renegociar primero.

## 8. No asumir el entorno de desarrollo del usuario

- Antes de proponer herramientas que requieren instalación local (Android Studio, emuladores, SDKs), verificar si el usuario tiene acceso a su PC y puede instalarlas.
- Si el usuario está trabajando desde SSH o desde una PC ajena, proponer alternativas que no dependan de su entorno local.
- Preferir flujos que puedan validarse en la nube (CI/CD) cuando el entorno local no está disponible.
- No dar por sentado que el usuario puede ejecutar una app en un emulador o dispositivo en este momento.

## 9. No ejecutar comandos largos o de impacto sin aprobación previa

- Antes de correr un comando que pueda tardar más de unos pocos segundos, descargar imágenes, modificar el sistema o generar archivos no previstos, el agente debe explicar qué va a hacer y esperar la aprobación del usuario.
- El usuario debe poder decidir si ejecuta el comando él mismo o si lo autoriza al agente.
- Esto incluye: instalaciones, builds largos, descargas de imágenes Docker, generación de wrappers, migraciones, etc.

## 8. Ejemplo de lo que se espera

En vez de decir:

> "Vamos a usar Hilt para inyección de dependencias."

Decir:

> "Para este MVP, no necesitamos Hilt. El proyecto es una sola persona, con pocas clases y un solo punto de entrada. Si más adelante crece y tenemos múltiples repositorios, viewmodels y servicios que se instancian en muchos lugares, entonces sí podría valer la pena evaluar una inyección de dependencias para evitar pasar objetos a mano. Pero no es necesario ahora."

---

Última actualización: agosto 2026.
