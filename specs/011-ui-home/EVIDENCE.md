# EVIDENCE — SPEC-011: Dashboard cálido

**Fecha:** 2026-11-13 | **Rama:** `feature/spec-011-ui-home` | **Dispositivo:** `emulator-5560` (AVD propio `TradeJob_Test`, Android 36.1, headless)

## Build y tests

- `node .devin/verify.cjs` → `BUILD: OK (app + APK de pruebas)`, `TESTS home: OK`, `TESTS login: OK`. Ejecutado dos veces; la última sobre el estado final (tras el fix de `home_stat_completed` en `:app`). Log: `.devin/android/artifacts/verify.log`.
- Unit tests `:home`: 25 verdes — 8 nuevos en `GetHomeStatsUseCaseTest` (semana con reloj fijo, `scheduledAt` vs `createdAt`, `completedAt`, factura `PAID` con `paidAt`, `INVOICED` como terminado, vacío, rango lunes–lunes, propagación de error).
- Unit tests `:login`: 27 verdes — `GreetingTest` (3) ahora importa `greetingForHour` desde `:libs:common` y sigue verde.

## Verificación en dispositivo

Cuenta de prueba "Carlos Prueba" registrada por UI; datos semilla insertados
directamente en `trade_job_database` (1 cliente, 4 trabajos con distintos
estados/fechas, 1 factura PAID esta semana) para ejercer contadores y
celebración. La app siempre abre en login; se entró por UI en cada captura.

| Captura (local, en `.devin/android/artifacts/`) | AC | Resultado |
|---|---|---|
| `spec-011-landing.png`, `spec-011-login-light.png` | AC-06 | Login dark/light con "BUENAS TARDES" + firma: el saludo desde `:libs:common` no rompe nada |
| `spec-011-home-dark-empty.png` | AC-05 | Cuenta sin datos: "Tu taller empieza aquí" + CTA "Crear mi primer cliente"; sin contadores ni acciones |
| `spec-011-home-dark-full2.png` | AC-02/03/04 | Dark petróleo: "3 trabajos esta semana", chips 1/1/2 Terminados (incluye INVOICED), franja lima "1 trabajo terminado · 1 factura cobrada esta semana" |
| `spec-011-home-light-full.png` | AC-01/03/04 | Light hueso: mismo contenido, "Terminados" correcto tras fix |
| `spec-011-home-light-bottom.png` | AC-09 | Acciones rápidas y footer; nav bar acorde al fondo |
| `spec-011-home-menu.png` | AC-10 | Menú con Mis Clientes / Mi Perfil / Facturas / Cerrar sesión |
| `spec-011-nav-clients.png` | AC-10 | Navegación menú → `client_list` (cliente semilla visible, datos Room reales) |
| `spec-011-nav-client-form.png` | AC-10/05 | Acción rápida → `client_form/0` (misma ruta del CTA del estado vacío) |

## Iteración de diseño (v2, 13-11) — diseño final aprobado

La v1 (saludo + número grande + chips + tarjetas) fue rechazada por el
usuario: "se ve vacío / los colores no dicen nada". Se eligió la dirección
"parte de trabajo del taller": fecha real de eyebrow, una sola tarjeta-parte
con borde (título "PARTE DE LA SEMANA" + rango lun–dom, número de trabajos,
líneas ✓ lima, divisor troquelado, pie de totales y `MitoSignatureDivider`
firmando el cierre), y los trabajos recientes como líneas del parte con
punto de estado. Las capturas `spec-011-*` de la tabla anterior documentan
la v1: siguen valiendo como evidencia de datos, navegación y estado vacío
(esas mecánicas no cambiaron), pero el diseño definitivo es el de estas
capturas, verificadas con el usuario en el emulador visible (`TradeJob_Test`
con ventana y GPU host):

| Captura | AC | Resultado |
|---|---|---|
| `home_v2_light.png` | AC-01/03/04 | Light hueso: "VIERNES 13 DE NOVIEMBRE" + "Buenas tardes, Carlos Prueba"; parte con borde "3 trabajos esta semana", ✓ lima (1 terminado + 1 factura), troquelado, pie "1 pendiente · 1 en curso · 2 terminados", firma cerrando |
| `home_v2_dark.png` | AC-02/09 | Dark petróleo: mismo parte, número y ✓ en lima, firma lima, contraste correcto |

Diseño aprobado por el usuario tras verlo en el emulador. Build del estado
final (post-cambio `Locale`): `gradlew :app:assembleDebug` OK.

## Criterios no cubiertos / limitaciones honestas

- Los datos del panel se sembraron por SQLite (insert directo en Room), no por
  flujos de UI: verifica el cómputo y la UI, no el camino de creación.
- El botón del estado vacío compone `client_form/0` (revisión de código) y la
  ruta se verificó pulsando la acción rápida equivalente; no se pulsó el botón
  del estado vacío en sí, y su captura es solo en dark.
- ANR de "System UI" del emulador durante la sesión (conocido desde SPEC-007);
  se pulsó "Wait" y el flujo continuó. `uiautomator dump` muere en este
  emulador; la interacción se hizo con taps por coordenadas.
- `JobCard` (trabajos recientes) conserva su paleta malva previa — fuera de
  alcance, queda para `ui-jobs`.
