# EVIDENCE — SPEC-013: Clientes cálidos

**Fecha:** 2026-11-13
**Rama:** `feature/spec-013-ui-clients`
**Dispositivo:** `TradeJob_Test2` (emulator-5562, API 36.1) — cuenta `carlos@prueba.dev`, datos semilla.

## Build y tests

- `gradlew.bat :app:assembleDebug` → OK
- `node .devin/verify.cjs` → BUILD: OK, TESTS home: OK, TESTS login: OK
- Tests nuevos: `ClientFormattersTest` (4 tests: conteos por estado,
  lista vacía, último trabajo por `createdAt`, vacío → null)

## Flujo verificado en emulador

| AC | Qué se comprobó | Resultado |
|----|-----------------|-----------|
| AC-01 | Botón Llamar → marcador `com.google.android.dialer` con "(633) 445-566" precargado; Ruta → `com.google.android.apps.maps` (MapsActivity); Email → `com.google.android.gm` (WelcomeTour de Gmail, emulator limpio) | ✓ `intent_llamar.png`, `intent_ruta.png`, `intent_email.png` |
| AC-02 | Carmen Ortiz sin email → solo Llamar+Ruta; Cliente Vacio sin email ni dirección → solo Llamar | ✓ `detail_carmen_light.png`, `detail_vacio_light.png` |
| AC-03 | Cliente Semilla: "● 1 en curso · ● 1 pendiente · ● 2 completados" (2 completados = COMPLETED + INVOICED) | ✓ `detail_semilla_light.png` |
| AC-04 | "Último trabajo" = Reparar caldera (createdAt mayor), con estado/fecha/importe | ✓ `detail_semilla_light.png` |
| AC-05 | Cliente Vacio: sin resumen ni tarjeta, "No hay trabajos registrados" + botón "Nuevo Trabajo" | ✓ `detail_vacio_light.png` |
| AC-06 | Lista embebida con FilterChips y JobCard SPEC-012 (PDF/compartir/borrar) intacta | ✓ `detail_semilla_light.png` |
| AC-07 | TopAppBar muestra el nombre del cliente; iconos presupuestos/editar presentes (editar abre `client_form`) | ✓ capturas |
| AC-08 | Light y dark | ✓ `detail_dark.png`, `list_dark.png` |
| AC-09 | Lista de clientes con piel: fondo hueso, BoneCard, nombre petróleo, FAB petróleo | ✓ `clients_light.png`, `list_dark.png` |
| AC-10 | Strings nuevos en `strings.xml` de `:home` y espejo en `:app` (incl. plurals ES) | ✓ código |

## Notas honestas

- El intent de Email abre el tour de bienvenida de Gmail porque el
  emulador está limpio; la entrega del `mailto:` está confirmada por
  `dumpsys` (actividad de Gmail al frente), no el cuerpo del correo.
- "0 en curso" se muestra cuando el contador es cero (legible, decidido
  así; la línea solo aparece si el cliente tiene trabajos).
- La ruta `job_list/{clientId}` sigue siendo código muerto por decisión
  del usuario (lista embebida) — deuda registrada en la spec.
- Fix durante verificación: el botón "Llamar" partía el texto en dos
  líneas; corregido con `maxLines=1` y padding reducido (ver capturas).

## Capturas

`shots/` — lista light/dark, detalle Semilla (3 botones + resumen +
último trabajo), detalle Carmen (sin email), detalle Vacio (solo Llamar,
estado vacío), intents de Llamar/Ruta/Email.
