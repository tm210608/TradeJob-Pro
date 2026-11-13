# STATE — TradeJob Pro
Fecha: 2026-11-13 | Rama: main | Base: main 5f55959

## Hecho hoy
- SPEC-013 mergeada y publicada (merge 5f55959): clientes con piel de
  taller. Detalle: nombre en TopAppBar, botones Llamar/Email/Ruta
  (intents reales verificados: Dialer, Maps, Gmail), resumen por estado,
  tarjeta Ultimo trabajo, lista embebida conservada (decision usuario).
  ClientListScreen con BoneCard/petroleo. 4 tests nuevos, verify.cjs OK,
  evidencia en specs/013-ui-clients/ con shots/.
- Exploracion Stitch en .devin/android/artifacts/stitch/: base,
  registro, tablon, minimal (direccion aprobada = minimal/zen).
- TradeJob_Test2 (emulator-5562) con GPU host y datos demo: 6 clientes,
  12 trabajos, fotos, cuenta carlos@prueba.dev / prueba123.

## Siguiente paso propuesto
- El usuario comentara "cosillas" para la siguiente spec.
- Cola: ui-quotes, ui-invoices, voice-parte (idea usuario),
  cleanup-job-list-route, db-migration-tests, sync-completa.
- Ideas Stitch para futuras specs: num. de parte, tecnico asignado,
  garantia, balance cliente, agrupar por estado.

## Decisiones/deuda
- Lista de trabajos embebida en client_detail; job_list/{clientId}
  sigue muerta -> spec cleanup-job-list-route.
- Strings nuevas en :home y espejo :app (duplicacion sin resolver).
- Emuladores ajenos (5554, 5556, 5558) y movil fisico: no tocar.
- Taps por coordenadas en resolucion real (1080x2400); captures en
  read escalan ~1.53x.

## Retomar
Lee AGENTS.md y .devin/STATE.md y continua.
