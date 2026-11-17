# STATE — TradeJob Pro
Fecha: 2026-11-17 | Rama: feature/spec-016-numeracion-legible | Base: main

## Hecho hoy
- SPEC-016 implementada y verificada: numeración legible.
  Presupuestos nuevos `P-AAAA-NNNN`, facturas `F-AAAA-NNNN`
  (contadores independientes que se reinician por año).
  Documentos viejos (P-<epoch>, semilla "2026-001") intactos.
- Nuevos: DocumentNumber.kt (helper puro) + DocumentNumberStore.kt
  (SharedPreferences: último correlativo emitido por prefijo-año).
  Queries LIKE en QuoteDao/InvoiceDao + Next*NumberUseCase.
- Bug hallado en dispositivo: solo con máximo existente, borrar el
  último reutilizaba el número → corregido combinando con el store
  (next = max(existente, emitido) + 1). Sin migración Room.
- verify.cjs verde (build OK, 11 DocumentNumberTest nuevos).
- Verificado en TradeJob_Test: P-2026-0001..0004, F-2026-0001/0002,
  PDF Factura_F-2026-0002_*.pdf, borrado sin reutilizar, limpieza.
- Evidencia: specs/016-numeracion-legible/EVIDENCE.md + 5 shots/.

## Siguiente paso propuesto
- Mergear SPEC-016 a main (pendiente aprobación del usuario).
- Cola: sync-completa, voice-parte, cleanup-job-list-route,
  db-migration-tests, vencimientos automáticos.

## Notas honestas
- Reinicio anual no ejercitado en dispositivo (cubierto por tests).
- Concurrencia simultánea no garantizada (mono-usuario local).

## Decisiones/deuda
- Strings :home ↔ :app duplicadas (sin resolver).
- Tras PDF la app vuelve a la lista (preexistente).
- Contador en SharedPreferences: si se borran datos de la app sin
  borrar la BD (imposible en la práctica) podría repetir sufijos;
  max() con existentes lo cubre.

## Retomar
Lee AGENTS.md y .devin/STATE.md y continua.
