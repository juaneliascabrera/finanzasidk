# Visión del proyecto

## Propósito

Construir una aplicación **móvil, open-source, offline-first y extremadamente simple** para que una persona joven en Argentina pueda registrar sus ingresos y egresos, planificar su mes y saber cuánto dinero realmente puede gastar sin sentir que está haciendo contabilidad.

El problema principal a resolver no es la falta de funcionalidades: es la **fricción**.

Las apps de finanzas personales actuales son lentas, sobrecargadas, con anuncios o con tantas opciones que anotar un café se convierte en un trámite. Esta app busca lo contrario: **el registro más rápido posible del gasto más común**, con la previsibilidad justa para tomar mejores decisiones al principio de cada mes.

## Usuario principal

- Hombre, 22 años, Argentina.
- Sueldo fijo en pesos argentinos (ARS).
- Paga principalmente con débito (BruBank, MODO, Google Wallet).
- No usa tarjeta de crédito.
- Inversiones conservadoras: FCI y dólares (dólar bancarizado / legal).
- Quiere anotar gastos rápido, sin depender de bancos ni APIs externas.
- Valor: control, previsibilidad, simplicidad, privacidad.

## Nociones e intuiciones clave

1. **Anotar un gasto debe tardar menos de 10 segundos.** Si no, no se usa.
2. **El foco está en el mes corriente.** La app se abre con la información de este mes.
3. **No es una planilla contable.** No hay doble partida, no hay asientos contables, no hay vocabulario raro.
4. **Las cuentas de inversión son especiales.** No se gasta desde ellas. Solo se ingresa capital y se actualiza manualmente su saldo.
5. **Los presupuestos son mensuales y configurables.** El usuario define el límite de cada categoría al inicio del mes. La app solo avisa cuando se acerca o se pasa.
6. **La privacidad es offline-first.** Los datos son del usuario, en su dispositivo, sin depender de servidores externos.
7. **La app no toca el dinero real.** Es un registro. No se conecta a bancos ni se realizan transferencias.
8. **El diseño es mobile-first, limpio y sin distracciones.** Nada de anuncios, nada de notificaciones innecesarias, nada de pantallas que pidan datos irrelevantes.
9. **La moneda principal es el peso argentino, pero se respeta el dólar.** Las cuentas de inversión en dólares se registran como tales, y se puede actualizar su saldo manualmente.
10. **El MVP es solo para una persona.** No se construye multi-usuario en esta etapa. Si en el futuro se suma, no debe complicar la experiencia actual.

## Constraints

- **Mobile-first:** la experiencia se diseña primero para celular.
- **Offline-first:** funciona sin internet. No requiere backend propio.
- **Open-source:** código público, licencia clara.
- **Ligera:** poco uso de batería, poco almacenamiento, arranque rápido.
- **Sin APIs externas en el MVP:** no conexión a bancos, ni brokers, ni cotizaciones automáticas.
- **Sin multi-usuario en el MVP.**
- **Sin tarjeta de crédito en el MVP.** Solo débito y efectivo.
- **Fácil de mantener:** stack que permita una sola persona seguirlo sin dolor.
- **Registro manual intencional:** aunque en el futuro se pueda importar o recordar, el MVP premia la anotación consciente y rápida.

## Criterios para futuras decisiones de diseño

Antes de agregar cualquier funcionalidad nueva, se debe preguntar:

- ¿Hace más rápido registrar un gasto? Si no, está en duda.
- ¿Aumenta la claridad sobre cuánto puedo gastar este mes? Si no, es secundario.
- ¿Agrega fricción a la pantalla principal? Si sí, es sospechosa.
- ¿Es necesario para un usuario argentino con sueldo en pesos? Si no, posible post-MVP.
- ¿Se puede desactivar o ignorar? Si no, es peligrosa.

## Nombre temporal

**Finanzas** (hasta definir un nombre definitivo).

---

Última actualización: agosto 2026.
