# EVIDENCIA — SPEC-016 Números de documento legibles

**Fecha:** 2026-11-17
**Rama:** `feature/spec-016-numeracion-legible`
**Dispositivo:** emulador `TradeJob_Test` (`emulator-5560`, Android 36, 1080x2400)
**Cuenta:** `carlos@prueba.dev` (datos semilla del emulador)

## Build y tests

- `node .devin/verify.cjs` → **BUILD: OK** (app + APK de pruebas), `BUILD SUCCESSFUL in 1m 45s`.
- **TESTS home: OK, ejecutados** — `DocumentNumberTest`: 11 tests, 0 fallos.
- **TESTS login: OK, resultado reutilizado**.
- Log completo: `.devin/android/artifacts/verify.log`.

## Hallazgo durante la verificación (bug corregido)

La primera versión calculaba el correlativo solo sobre el **máximo existente**.
En dispositivo se comprobó que borrar el último documento sí reutilizaba su
número (salió `P-2026-0002` dos veces), incumpliendo AC-05. Se corrigió añadiendo
`DocumentNumberStore` (SharedPreferences, clave `<prefijo>-<año>`) que recuerda
el último correlativo emitido: `next = max(máximo existente, último emitido) + 1`.
Sin migración Room; si el store se perdiera, el máximo existente evita colisiones.

## Verificación en dispositivo

| # | Paso | Resultado | AC |
|---|---|---|---|
| 1 | Crear presupuesto en cliente Javier Ruiz | `P-2026-0001` | AC-01 |
| 2 | Crear segundo presupuesto | `P-2026-0002` (correlativo) | AC-02 |
| 3 | Convertir a factura | `F-2026-0001`, contador propio | AC-03 |
| 4 | Borrar `P-2026-0003` y crear otro | Salió `P-2026-0004` (no reutiliza) | AC-05 |
| 5 | Convertir `P-2026-0004` a factura | `F-2026-0002` (serie continúa) | AC-03/AC-05 |
| 6 | Lista Facturas con factura semilla `2026-001` | Conserva su número intacto | AC-04 |
| 7 | Detalle de factura | Título `F-2026-0002` | AC-06 |
| 8 | Exportar PDF | `Factura_F-2026-0002_1794909973066.pdf` | AC-06 |

Las tarjetas de lista muestran número + estado + fecha + total con el nuevo
formato (capturas 04/05/06). Los números viejos (`2026-001` y cualquier
`…-<epoch>`) no coinciden con el patrón y quedan intactos.

## Capturas (`shots/`)

- `04-lista-0003.png` — presupuestos `P-2026-0001/0002/0003` conviviendo.
- `05-lista-0004-tras-borrado.png` — `P-2026-0004` tras borrar `0003`: AC-05.
- `06-facturas-0002-y-antigua.png` — `F-2026-0002`, `F-2026-0001` y la
  factura semilla `2026-001` sin renumerar: AC-03/AC-04.
- `07-pdf-f2026-0002.png` — snackbar con el nombre del PDF generado: AC-06.
- `08-detalle-f2026-0002.png` — detalle con `F-2026-0002` en la barra: AC-06.

## Notas honestas

- El cambio de año (reinicio del contador) no se pudo ejercitar en
  dispositivo; está cubierto por tests unitarios (`nextSequence` usa el año
  como parte de la clave y del patrón LIKE).
- El primer documento del año en base vacía (`…-0001`) está cubierto por
  tests; en dispositivo ya existían documentos emitidos.
- Concurrencia de dos creaciones simultáneas no está garantizada (la app es
  mono-usuario local; riesgo bajo, anotado en la spec).
- Datos de prueba eliminados del emulador tras verificar (presupuestos
  `P-2026-0001/0002/0004` y facturas `F-2026-0001/0002`); la factura semilla
  `2026-001` y los clientes de demostración quedaron intactos.
