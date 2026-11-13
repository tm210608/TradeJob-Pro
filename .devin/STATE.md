# STATE — TradeJob Pro
Fecha: 2026-11-13 | Rama: main | Ultimo merge: SPEC-011 (b8c350a)

## Hecho hoy
- SPEC-011 (dashboard "parte de la semana") implementada, verificada en
  emulador, aprobada por el usuario y mergeada a main con push.
- Diseno final v2 "parte de la semana" (v1 rechazada por el usuario):
  fecha real, saludo+nombre, tarjeta-parte con borde (rango lun-dom,
  N trabajos, lineas lima terminado/cobrada, troquelado, pie de totales,
  firma cerrando el parte). Trabajos recientes = lineas con punto de
  estado; JobCard intacto fuera del dashboard.
- Regla semanal scheduledAt ?: createdAt lun-dom; INVOICED cuenta
  como terminado; greetingForHour en :libs:common compartido con login.
- 25 tests home (8 nuevos), 27 login, verify.cjs OK sobre estado final.
- Capturas home_v2_*.png (diseno final) + spec-011-*.png (v1, evidencia
  de datos/nav/estado vacio). Cuenta prueba Carlos Prueba + semilla Room.
- Hallazgos: :app duplica strings home_* (corregido home_stat_completed,
  resto = deuda); app siempre arranca en login (preexistente).

## Siguiente paso propuesto
- Cola: ui-jobs (JobCard malva pendiente), ui-clients, db-migration-tests,
  sync-completa, i18n-domain, cleanup-strings-app (duplicados :app).

## Decisiones/deuda
- Emulador TradeJob_Test corre con ventana (host GPU), muestra v2 light.
- emulator-5556 (SocialChef) y 5558 (Samsung ajeno): no tocar.
- uiautomator dump muere en este emulador; taps por coordenadas.
- feature/spec-010-ui-login-calido sigue pudiendo borrarse (mergeada).

## Retomar
Lee AGENTS.md y .devin/STATE.md y continua.
