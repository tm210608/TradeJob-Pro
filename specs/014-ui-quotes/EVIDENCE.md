# EVIDENCE — SPEC-014: Presupuestos cálidos

**Fecha:** 2026-11-15
**Rama:** `feature/spec-014-ui-quotes`
**Dispositivo:** Xiaomi POCO X7 Pro (`rodin`), Android 16 —
serial `adb-MVVSE6A6S4IVQ8MJ-YvIufY._adb-tls-connect._tcp`.
Verificación autorizada por el usuario en su móvil físico (no emulador);
instalación con `adb install -r` conservando datos. Cuenta demo
`demo@taller.test` creada en el dispositivo; todos los datos de prueba
(cliente, presupuestos, factura) eliminados al terminar.

## Build

- `gradlew.bat :app:assembleDebug` → **BUILD SUCCESSFUL**
- `node .devin/verify.cjs` → build + tests verdes; log en
  `.devin/android/artifacts/verify.log`

## Unit tests

- `gradlew.bat :home:testDebugUnitTest :login:testDebugUnitTest` → verde
- `QuoteFormattersTest` (nuevo): **7 tests, 0 fallos, 0 errores**
  (importe con/sin decimales, miles, fecha con/sin valor, casos borde).

## Flujo en dispositivo (light)

1. Lista vacía del cliente: nombre del cliente en la TopAppBar, fondo
   hueso, CTA "Crear primer presupuesto" y FAB petróleo → AC-01, AC-04.
2. Presupuesto creado con partida "Cambio de enchufes cocina" (1 × 90 €,
   IVA 21 %): tarjeta totales "Subtotal: 90 € / IVA (21 %): 18,9 € /
   Total: 108,9 €" → AC-05.
3. Tarjeta en lista: `P-1794766642` + chip Borrador + "15 nov" +
   "108,9 €" → AC-02.
4. Chips de estado: "Aceptado" seleccionado, guardado, tarjeta pasa a
   lima → AC-03, AC-06.
5. "Convertir en factura" visible por primera vez → snackbar "Factura
   creada desde presupuesto"; factura `F-1794766705` (Pendiente,
   108,90 €) comprobada en la lista de facturas → AC-07.
6. "Exportar PDF" → snackbar de éxito → AC-09.
7. Papelera → hoja "Eliminar presupuesto": Cancelar deja todo intacto;
   Eliminar → snackbar "Presupuesto eliminado" y lista vacía → AC-08.

## Flujo en dispositivo (dark)

- Lista vacía y formulario sobre gradiente petróleo; chips de estado
  legibles (se corrigió `Rechazado` partido en vertical con `FlowRow`
  durante la verificación). Segundo presupuesto de prueba
  ("Revision caldera", 72,6 €) mostrado en tarjeta oscura y borrado con
  confirmación → AC-01, AC-02, AC-05, AC-08, AC-10.

## Capturas (`shots/`)

| Archivo | Qué muestra |
|---|---|
| `spec14-quotes-empty-light.png` | Lista vacía light: nombre cliente + CTA |
| `spec14-quoteform-light.png` | Formulario light: chips de estado, partida |
| `spec14-quotecard-draft.png` | Tarjeta Borrador con fecha y total |
| `spec14-quotecard-accepted.png` | Tarjeta Aceptado (chip lima) |
| `spec14-invoice-converted.png` | Factura F-1794766705 creada desde presupuesto |
| `spec14-quotes-empty-dark.png` | Lista vacía dark |
| `spec14-quoteform-dark.png` | Formulario dark con chips |
| `spec14-quotecard-dark.png` | Tarjeta dark con total |

## Strings

- Nuevas en `home/src/main/res/values/strings.xml`; espejo en
  `app/src/main/res/values/strings.xml`. Sin literales hardcodeados → AC-11.

## Notas honestas

- AC-03: `DRAFT` y `ACCEPTED` ejercitados con datos reales; `SENT` y
  `REJECTED` verificados visualmente en el selector de chips del
  formulario (no se crearon presupuestos en esos estados).
- AC-09: PDF verificado. El pad de firma se conserva en pantalla pero no
  se dibujó una firma durante la prueba.
- Hallazgo corregido en caliente: chip "Rechazado" se partía en vertical
  en la fila de chips → `FlowRow` para salto de línea (build reinstalado
  y re-verificado).
- Comportamiento preexistente observado: tras el snackbar de PDF la app
  vuelve a la lista (fuera de alcance, anotado en SPEC).
