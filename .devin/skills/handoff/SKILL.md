---
name: handoff
description: Guardar el estado breve de TradeJob Pro para retomar la siguiente sesión sin explorar de nuevo.
---
# /handoff

1. Lee git status y rama/commit actuales. Reescribe .devin/STATE.md en menos de 40 líneas: fecha, rama/commit, hecho, siguiente paso, decisiones, bloqueos y deuda.
2. Distingue pruebas nuevas de resultados reutilizados e históricos. No marques SPEC-007 cerrada si faltan SPEC/EVIDENCE o verificaciones.
3. Actualiza PROGRESS.md solo si cambió el progreso; no dupliques el historial en STATE.
4. Indica serial y nombre del AVD utilizado. No apagues dispositivos de otros proyectos ni cambies su configuración.
5. Resume pendientes, no hagas commit/merge/push sin permiso. Propón retomar con: Lee AGENTS.md y .devin/STATE.md y continúa.
