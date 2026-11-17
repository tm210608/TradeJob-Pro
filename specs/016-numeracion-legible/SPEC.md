# SPEC-016: Números de documento legibles — P-2026-0015

**Estado:** implementada y verificada
**Rama:** `feature/spec-016-numeracion-legible`
**Fecha:** 2026-11-17

## Qué construimos

Presupuestos y facturas nuevos con número legible `P-AAAA-NNNN` /
`F-AAAA-NNNN` (prefijo + año + correlativo de 4 dígitos que se reinicia
cada año) en lugar de `P-<epoch>` / `F-<epoch>`. Los documentos
**existentes conservan su número actual** (decisión del usuario): solo
los nuevos llevan formato nuevo.

### Contexto / por qué

Deuda anotada en SPEC-014 y SPEC-015: los números se generan con
`System.currentTimeMillis()/1000` (`P-1794772064`), imposibles de leer,
dictar por teléfono o poner en un PDF profesional. El número aparece en
listas, detalles, barras de título y PDFs — mejora visible sin tocar UI.

## Criterios de aceptación

- [x] AC-01: Un presupuesto nuevo guardado hoy recibe número
      `P-2026-NNNN` (año actual, 4 dígitos con ceros a la izquierda).
- [x] AC-02: Dos presupuestos nuevos consecutivos reciben números
      consecutivos (`…-0001`, `…-0002` — o el siguiente al máximo
      existente ese año).
- [x] AC-03: Una factura creada desde presupuesto recibe `F-2026-NNNN`
      con su propio contador independiente del de presupuestos.
- [x] AC-04: Un presupuesto/factura existente (número viejo `…-<epoch>`
      o semilla `2026-001`) conserva su número intacto tras abrirlo y
      guardarlo.
- [x] AC-05: El correlativo no reutiliza números borrados: combina el
      máximo existente del año con el último correlativo emitido
      (`DocumentNumberStore`, SharedPreferences).
- [x] AC-06: El número nuevo aparece correctamente en tarjeta de lista,
      título del detalle y PDF exportado (verificación en dispositivo).

## Fuera de alcance

- Renumerar documentos existentes ni migración de base de datos.
- Series múltiples, prefijos configurables por el usuario ni formato
  editable.
- Cambios en pantallas: el número se muestra donde ya se muestra.
- Sincronización ni colisiones multi-dispositivo (la app es mono-usuario
  local hoy; `sync-completa` decidirá cómo numerar en la nube).

## Diseño / notas técnicas

- Módulos afectados: `:libs:database` (queries en `QuoteDao` y
  `InvoiceDao`), `:home` (generación en `QuoteFormViewModel` y
  `CreateInvoiceFromQuoteUseCase`, más repositorios).
- Nuevo helper puro `DocumentNumber` (p. ej. en `:home` domain o junto
  a los formatters): `format("P", year, seq)` → `P-2026-0015` y
  `parseSequence(number)` para extraer el sufijo — funciones puras,
  testeables sin Android.
- Correlativo: query `SELECT number FROM quotes WHERE number LIKE
  'P-2026-%'` → sufijo máximo del año. Igual para facturas con `F-`.
  Además `DocumentNumberStore` (SharedPreferences, clave `P-2026` /
  `F-2026`) recuerda el último correlativo emitido:
  `next = max(máximo existente, último emitido) + 1`. Así borrar el
  último documento tampoco reutiliza su número (AC-05) — en la primera
  versión solo con máximo existente sí se reutilizaba (hallazgo de la
  verificación en dispositivo, corregido).
- Sin migración Room ni cambios de esquema: la columna `number` ya es
  texto libre.
- Año: `Calendar.getInstance().get(Calendar.YEAR)` en el dispositivo.

## Mobile checklist

- [x] Estados UI: sin cambios (el número solo cambia de valor)
- [x] Navegación: intacta
- [x] Permisos: ninguno nuevo
- [x] Offline: 100 % local, contador calculado desde Room
- [x] Edge cases: primer documento del año (empieza en 0001), base con
      solo números viejos (empieza en 0001 igual), documento borrado (no
      reutiliza), año bisiesto/cambio de año (reinicia), coexistencia de
      números viejos y nuevos en la misma lista
- [x] i18n: sin textos nuevos de UI

## Plan de tests

- [x] `gradlew.bat :app:assembleDebug` compila
- [x] Unit tests nuevos: `DocumentNumberTest` (formato, parseo de
      sufijo, año embebido, número viejo sin match, no reutilización con
      `lastIssued`) — 11 tests
- [x] `gradlew.bat :home:testDebugUnitTest :login:testDebugUnitTest`
      verdes
- [x] `node .devin/verify.cjs` + dispositivo: crear dos presupuestos
      (números consecutivos), convertir a factura (`F-2026-…`),
      comprobar tarjeta/detalle/PDF, borrar el último y crear otro (no
      reutiliza)
- [x] Screenshots en `EVIDENCE.md`

## Hallazgos fuera de scope

- En la nube (`sync-completa`) habrá que decidir numeración si algún día
  hay multi-dispositivo; hoy el contador local es suficiente.
