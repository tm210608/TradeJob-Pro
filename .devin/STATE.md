# STATE — TradeJob Pro
Fecha: 2026-11-15 | Rama: feature/spec-014-ui-quotes (sin merge)

## Hecho hoy
- SPEC-014 completada y verificada: presupuestos con piel de taller.
  Lista: nombre del cliente en TopAppBar, QuoteCard con numero + chip de
  estado + fecha + total "108,9 €", vacio con CTA. Formulario: chips de
  estado (Borrador/Enviado/Aceptado/Rechazado — por fin cambiable),
  papelera con confirmacion, "Anadir partida" boton, tarjeta de totales.
- Desbloqueado: aceptado -> "Convertir en factura" alcanzable (factura
  F-1794766705 verificada). PDF exportado OK.
- 7 tests nuevos QuoteFormattersTest; verify.cjs verde; log en
  .devin/android/artifacts/verify.log.
- Verificado en el Xiaomi fisico del usuario (autorizado), light+dark.
  Cuenta demo demo@taller.test; datos de prueba borrados al terminar.
- Fix en verificacion: chip "Rechazado" partido -> FlowRow.
- Evidencia: specs/014-ui-quotes/EVIDENCE.md + shots/.

## Siguiente paso propuesto
- Mergear feature/spec-014-ui-quotes si el usuario aprueba.
- Cola: ui-invoices, voice-parte (idea usuario), cleanup-job-list-route,
  db-migration-tests, sync-completa.

## Decisiones/deuda
- Strings nuevas en :home y espejo :app (duplicacion sin resolver).
- Tras PDF la app vuelve a la lista (preexistente, anotado en SPEC).
- Emuladores ajenos y movil fisico: pedir permiso antes de instalar.

## Retomar
Lee AGENTS.md y .devin/STATE.md y continua.
