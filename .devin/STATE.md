# STATE — TradeJob Pro
Fecha: 2026-11-13 | Rama: main | Base: main 7794c97

## Hecho hoy
- SPEC-012 mergeada y publicada (merge 7794c97): piel de taller en
  lista/ficha de trabajos, aprobada por el usuario en emulador.
- JobCard con fecha·importe y marca ambar Urgente (HIGH/URGENT);
  estados tipo parte (pendiente neutro, curso petrol, lima, cyan).
- HALLAZGO: ruta job_list/{clientId} es codigo muerto — los trabajos
  se ven embebidos en ClientDetailScreen. Spec de limpieza en cola.
- Badges de foto Antes/Despues/Progreso con colorScheme (bug PROGRESS
  mostraba "Despues" corregido). 32 tests home, verify.cjs OK.
- Emulador nuevo TradeJob_Test2 (emulator-5562) creado limpio porque
  TradeJob_Test tenia apps ajenas abiertas. Datos de demo sembrados
  por SQLite en ambos: 6 clientes, 12 trabajos, 12 fotos (texturas
  generadas), 1 factura cobrada. Cuenta carlos@prueba.dev / prueba123.
- Idea usuario en cola: voice-parte (dictar el parte por voz).

## Siguiente paso propuesto
- El usuario va a proponer mejoras/novedades tras ver la app con datos.
- Cola: ui-clients (detalle aun malva viejo), ui-quotes, ui-invoices,
  voice-parte, cleanup-job-list-route, db-migration-tests, sync-completa.

## Decisiones/deuda
- TradeJob_Test (5560) y TradeJob_Test2 (5562) corriendo con ventana;
  app de gimnasio ajena instalada en TradeJob_Test — no tocar.
- emulator-5556 (SocialChef) y 5558 (Samsung ajeno): no tocar.
- ADB por -s <serial>; taps por coordenadas; tutoriales Gboard/stylus
  pueden interceptar texto.
- Branches feature/spec-010..012 mergeadas, borrables.

## Retomar
Lee AGENTS.md y .devin/STATE.md y continua.
