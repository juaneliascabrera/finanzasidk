# Aplicación Android

El módulo `app` contiene la interfaz Compose y conecta `core` con
`persistence`. No contiene reglas financieras: las validaciones siguen viviendo
en el dominio.

## Pantallas

La navegación inicial usa cuatro pestañas persistentes en la barra inferior:

- **Inicio:** saldo por moneda, cuentas, presupuesto del mes y acciones rápidas.
- **Movimientos:** historial ordenado por fecha de ingresos, egresos, ajustes y
  patas de transferencias.
- **Inversiones:** valor de las cuentas de inversión y accesos a aportes,
  rescates y ajustes.
- **Presupuestos:** límites mensuales y progreso de gasto.

## Acciones

Desde la interfaz se pueden crear:

- cuentas operativas e inversión;
- categorías;
- presupuestos;
- ingresos;
- egresos categorizados o sin categoría;
- transferencias normales;
- aportes y rescates;
- ajustes de valuación.

Los formularios validan localmente fechas, importes, cuentas compatibles y
monedas antes de invocar el ViewModel. El dominio vuelve a validar la operación
antes de persistirla.

## Arquitectura

La pantalla obtiene un `FinanzasViewModel` con el repositorio creado por
`FinanzasApplication`. El ViewModel expone un `StateFlow<FinanzasUiState>` y
ejecuta las operaciones suspendidas fuera del hilo de UI.

No se agregó Hilt ni otro contenedor de dependencias: hay un solo repositorio y
un solo punto de composición. La factory manual del ViewModel mantiene esa
decisión explícita y fácil de cambiar si el proyecto crece.

## Tema

El tema usa Material 3, colores dinámicos en Android 12 o superior y una paleta
de respaldo para versiones anteriores. Los colores principales están
centralizados en `ui/theme/Theme.kt` para que una futura personalización visual
no requiera buscar valores por todas las pantallas.

## Testing

`FinanzasSmokeTest` verifica que la actividad principal muestre la navegación
inicial. Para ejecutarlo se necesita Android SDK y un emulador o dispositivo:

```bash
./gradlew :app:assembleDebug
./gradlew :app:connectedDebugAndroidTest
```

El entorno actual no tiene Android SDK, por eso el smoke test está escrito pero
no ejecutado aquí.

## Decisiones pendientes

- Extraer textos a recursos localizables cuando se definan idiomas adicionales.
- Agregar selección de fecha con DatePicker si el ingreso manual de `AAAA-MM-DD`
  resulta demasiado lento.
- Agregar edición y archivado desde la interfaz cuando esas operaciones tengan
  un flujo de negocio definido.
- Agregar estados de carga por acción si la persistencia deja de ser local.
