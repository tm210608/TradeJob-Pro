# STATE — TradeJob Pro
Fecha: 2026-11-13 | Rama: feature/spec-012-ui-jobs | Base: main cb35655

## Hecho hoy
- SPEC-011 mergeada y publicada (main cb35655): dashboard "parte de la
  semana" aprobado por el usuario.
- SPEC-012 (trabajos calidos) implementada y verificada en emulador.
  SIN COMMIT — pendiente visto bueno para merge/push.
- JobCard redisenada: borde, BoneCard, estados tipo parte (pendiente
  neutro, curso petrol, terminado lima, facturado cyan), "fecha · importe"
  (finalAmount ?? budgetAmount), marca ambar Urgente solo HIGH/URGENT.
- HALLAZGO: ruta job_list/{clientId} no recibe navegacion — la lista real
  es la seccion de ClientDetailScreen. JobListScreen actualizada pero es
  codigo muerto; spec de limpieza en cola.
- Ficha trabajo con piel + badges foto Antes/Despues/Progreso con
  colorScheme (PROGRESS mostraba mal "Despues" — corregido).
- Strings del flujo a strings.xml; 32 tests home (7 nuevos), verify.cjs OK.
- Idea usuario registrada en cola: voice-parte (dictar el parte por voz).

## Siguiente paso propuesto
- Merge/push de SPEC-012 si el usuario lo pide tras verla en emulador.
- Cola: ui-clients (detalle con malva viejo), ui-quotes, ui-invoices,
  voice-parte, cleanup-job-list-route, db-migration-tests, sync-completa.

## Decisiones/deuda
- Emulador TradeJob_Test con ventana; app de gimnasio ajena instalada en
  el AVD (com.aistudio.gymtracker) — no tocar, tampoco desinstalar.
- emulator-5556 (SocialChef) y 5558 (Samsung ajeno): no tocar.
- Taps por coordenadas; tutoriales de Gboard/stylus pueden interceptar.
- feature/spec-010 y spec-011 branches mergeadas, borrables.

## Retomar
Lee AGENTS.md y .devin/STATE.md y continua.
