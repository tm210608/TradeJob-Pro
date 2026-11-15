# EVIDENCE — SPEC-015: Facturas cálidas

**Fecha:** 2026-11-15
**Rama:** `feature/spec-015-ui-invoices`
**Dispositivo:** Xiaomi POCO X7 Pro (`rodin`), Android 16 —
serial `adb-MVVSE6A6S4IVQ8MJ-YvIufY._adb-tls-connect._tcp`.
Verificación en el móvil físico del usuario (autorizada en la sesión
anterior); instalación con `adb install -r` conservando datos. Cuenta
demo `demo@taller.test` reutilizada; todos los datos de prueba
(cliente, presupuestos, facturas) eliminados al terminar.

## Build

- `gradlew.bat :app:assembleDebug` → **BUILD SUCCESSFUL**
- `node .devin/verify.cjs` → build + tests verdes; log en
  `.devin/android/artifacts/verify.log`

## Unit tests

- `gradlew.bat :home:testDebugUnitTest :login:testDebugUnitTest` → verde
- `InvoiceFormattersTest` (nuevo): **6 tests, 0 fallos, 0 errores**
  (fecha con/sin valor, importe entero, decimales/miles, cero, nulo).

## Flujo en dispositivo (light)

1. Presupuesto de prueba aceptado → "Convertir en factura" →
   `F-1794772064` (45 € + IVA 21 % = 54,45 €).
2. Lista "Facturas": fondo hueso, TopAppBar transparente, filtros
   Todas/Pendiente/Pagada/Vencida → AC-01.
3. `InvoiceCard`: número `F-1794772064`, chip "Pendiente" ámbar,
   **"Cliente Spec15"**, línea "15 nov · 54,45 €" y botón
   "Exportar PDF" → AC-02, AC-03 (pendiente).
4. Detalle: TopAppBar con `F-1794772064`, chips Pendiente/Pagada/
   Vencida/Anulada envueltos en `FlowRow`, "Fecha emision: 15 nov",
   totales "Subtotal: 45 € / IVA: 9,45 € / Total: 54,45 €" → AC-06.
5. "Marcar como pagada" → chip "Pagada" (lima) y línea
   "Pagado: 54,45 € · Pendiente: 0 €" → AC-03 (pagada), AC-04, AC-08.
6. "Exportar PDF" → snackbar de éxito → AC-08.
7. "Eliminar factura" → hoja `ConfirmMitoButtonSheet` "Eliminar factura /
   ¿Estas seguro de que deseas eliminar esta factura?": Cancelar deja
   todo intacto; Eliminar → lista "Sin facturas aun" → AC-07, AC-05
   (estado vacío).

## Flujo en dispositivo (dark)

- Segunda factura de prueba sobre gradiente petróleo: tarjeta con
  cliente/estado/fecha/total legibles y detalle oscuro verificado;
  hoja de borrado también capturada en dark → AC-01, AC-02, AC-06,
  AC-07, AC-09.

## Capturas (`shots/`)

| Archivo | Qué muestra |
|---|---|
| `spec15-invoicelist-light.png` | Lista light: filtros + tarjeta con cliente |
| `spec15-invoicedetail-light.png` | Detalle light: número en barra, chips, totales € |
| `spec15-invoicedelete-sheet.png` | Hoja de confirmación de borrado |
| `spec15-invoicecard-dark.png` | Tarjeta dark con cliente, fecha y total |
| `spec15-invoicedetail-dark.png` | Detalle dark |

## Strings

- Nuevas en `home/src/main/res/values/strings.xml`; espejo en
  `app/src/main/res/values/strings.xml`. Literales de la pantalla y
  eventos del `InvoiceFormViewModel` pasados a recursos → AC-10.

## Notas honestas

- AC-03: `PENDIENTE` (ámbar) y `PAGADA` (lima) ejercitados con datos
  reales; `VENCIDA` y `ANULADA` solo visualizados en el selector de
  chips (mismo mapa compartido, no se crearon facturas en esos
  estados).
- AC-04: la línea pagado/pendiente se verificó tras pago completo
  ("Pagado: 54,45 € · Pendiente: 0 €"); no existe cobro parcial
  editable en el modelo actual (fuera de alcance).
- AC-05: estado vacío verificado ("Sin facturas aun"); los filtros se
  ven en capturas pero no se ejercitó un filtro concreto (solo una
  factura activa a la vez).
- Limpieza: facturas y cliente de prueba eliminados del dispositivo;
  modo claro restaurado. El teléfono quedó sin datos de prueba.
