# STATE — TradeJob Pro
Fecha: 2026-11-13 | Rama: feature/spec-013-ui-clients | Base: main 7fa887f

## Hecho hoy
- SPEC-013 implementada y verificada (sin merge): clientes con piel de
  taller. Detalle: nombre en TopAppBar, tarjeta BoneCard, botones
  Llamar/Email/Ruta (intents reales verificados: Dialer, Maps, Gmail),
  resumen "N en curso/pendiente/completados", tarjeta Ultimo trabajo,
  lista embebida con filtros conservada (decision usuario).
- ClientListScreen con piel: BoneCard, petroleo, FAB de marca.
- ClientFormatters.kt (countJobsByStatus, lastJob) + 4 tests nuevos;
  verify.cjs verde. Evidencia: specs/013-ui-clients/ (shots/ incluido).
- Exploracion Stitch en .devin/android/artifacts/stitch/: base,
  registro, tablon, minimal (direccion aprobada = minimal/zen).
- TradeJob_Test2 (emulator-5562) arrancado de nuevo con GPU host;
  datos demo persisten (6 clientes, 12 trabajos, fotos, cuenta
  carlos@prueba.dev / prueba123).

## Siguiente paso propuesto
- Mostrar SPEC-013 al usuario y, si aprueba, commit+merge+push.
- Cola: ui-quotes, ui-invoices, voice-parte (idea usuario),
  cleanup-job-list-route, db-migration-tests, sync-completa.
- Ideas Stitch para futuras specs: num. de parte, tecnico asignado,
  garantia, balance cliente, agrupar por estado.

## Decisiones/deuda
- Lista de trabajos queda embebida en client_detail (usuario);
  job_list/{clientId} sigue siendo codigo muerto → spec de limpieza.
- Strings nuevas se escriben en :home Y su espejo :app (deuda:
  duplicacion completa sin resolver).
- emulator-5554 ajeno detectado hoy: no tocar. 5556/5558 igual.
- Taps por coordenadas en resolucion real (1080x2400); captures en
  read escalan ~1.53x.

## Retomar
Lee AGENTS.md y .devin/STATE.md y continua.
