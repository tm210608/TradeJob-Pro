# STATE — TradeJob Pro
Fecha: 2026-11-15 | Rama: main | Base: merge SPEC-015

## Hecho hoy
- SPEC-015 mergeada a main: facturas con piel de taller.
  Lista: InvoiceCard con numero + NOMBRE DEL CLIENTE (lista global,
  antes invisible) + chip estado + fecha "15 nov" + total "54,45 €";
  filtros conservados.
  Detalle: titulo = numero de factura, chips FlowRow, totales BoneCard
  es-ES, borrado con hoja ConfirmMitoButtonSheet (antes AlertDialog).
- Nuevos: InvoiceFormatters.kt, InvoiceStatus.kt (pendiente ambar /
  pagada lima / vencida roja / cancelada gris), 6 tests formatters.
- verify.cjs verde; verificado en Xiaomi fisico (light+dark): crear
  via presupuesto, marcar pagada, PDF, borrar con hoja (cancel+confirm).
- Evidencia: specs/015-ui-invoices/EVIDENCE.md + 5 shots/.
- Movil limpio: datos de prueba borrados, modo claro.

## Siguiente paso propuesto
- Cola: sync-completa, voice-parte, cleanup-job-list-route,
  db-migration-tests, numeracion legible (F-/P-<epoch>), vencimientos.
- Serie "piel de taller" completa: login, dashboard, trabajos,
  clientes, presupuestos, facturas.

## Notas honestas
- Filtros de facturas visibles pero no ejercitados (1 factura a la vez).
- Vencida/anulada solo vistas en selector. Detalles en EVIDENCE.md.

## Decisiones/deuda
- Strings nuevas en :home y espejo :app (duplicacion sin resolver).
- Tras PDF la app vuelve a la lista (preexistente).
- Emuladores ajenos y movil fisico: pedir permiso antes de instalar.

## Retomar
Lee AGENTS.md y .devin/STATE.md y continua.
