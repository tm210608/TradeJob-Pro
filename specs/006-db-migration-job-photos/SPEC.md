# SPEC-006: Hotfix — migración Room `job_photos` (crash al actualizar)

**Estado:** verificada (emulador + BD real del Xiaomi; físico pendiente libre)
**Rama:** `feature/spec-006-migration-job-photos`
**Fecha:** 2026-11-11
**Prioridad:** alta — la app crashea al hacer login en dispositivos con BD
de versión anterior (detectado en el Xiaomi físico del usuario).

## Qué construimos

Arreglar el crash `Migration didn't properly handle: job_photos` que impide
abrir la app en dispositivos con una base de datos antigua. Migración
`MIGRATION_6_7` que reconstruye `job_photos` con el esquema exacto esperado
conservando las fotos existentes, y bump de versión 6 → 7.

### Contexto / por qué

Detectado durante la evidencia de SPEC-005 (ver su sección Hallazgos). El
Xiaomi tiene `user_version = 3` y su `job_photos` carece de `isSynced` y
`remoteUrl` (columnas añadidas a la entidad sin migración que las cubra en
este camino: `2→3` añade `remoteUrl` pero ya corríó; ninguna añade
`isSynced`). Al actualizar 3→6 ninguna migración toca `job_photos` → la
validación `TableInfo` de Room lanza `IllegalStateException` y la app muere
en el primer acceso a BD (login). El emulador no lo reprodujo porque su BD
se creó nueva. Impacto real: **todo usuario existente que actualice la app
puede perder el acceso** (crash en cada arranque).

Diagnóstico sobre la BD real extraída del dispositivo: `clients`, `jobs`,
`users`, `quotes`, `quote_items`, `invoices` quedan correctos tras la cadena
de migraciones; solo `job_photos` desajusta.

## Criterios de aceptación

- [x] `version = 7` en `TradeJobDatabase` + `MIGRATION_6_7` registrada en
      `DatabaseModule`
- [x] La migración produce EXACTAMENTE el `TableInfo` esperado (columnas,
      FK a `jobs` con `ON DELETE CASCADE`, índice `index_job_photos_jobId`)
      partiendo de cualquier variante antigua: usa `PRAGMA table_info` para
      copiar solo columnas existentes (idempotente y segura para BD v1–v6)
- [x] Conserva las filas de `job_photos` (`isSynced=0`, `remoteUrl=NULL`
      para datos previos) y funciona si la tabla no existe (BD muy vieja)
- [x] BD v3 real (extraída del Xiaomi): la app abre, login posible,
      navegación a `:home` sin crash — verificado en emulador con copia
      byte a byte de su BD (el físico estaba ocupado); capturas en
      `EVIDENCE.md`
- [x] Build compila + tests verdes

## Fuera de alcance

- Otras tablas: verificado sobre la BD real que solo `job_photos` desajusta
- Suite de tests de migración Room (`MigrationTestHelper` necesita los
  schemas históricos y solo existe `6.json`) → spec futura `db-migration-tests`
- `fallbackToDestructiveMigration` como política: NO se propone (borra datos
  del usuario en silencio)
- Completar EVIDENCE de SPEC-005 (se retoma tras este fix)

## Diseño / notas técnicas

- Módulos afectados: `:libs:database` (`TradeJobDatabase.kt`,
  `DatabaseModule.kt`)
- Estrategia: rebuild seguro, no ALTERs a ciegas:
  1. `PRAGMA table_info(job_photos)` → columnas existentes
  2. `CREATE TABLE job_photos_new (...)` con el esquema v6 exacto (sin
     `DEFAULT` en columnas — el esperado es `undefined`)
  3. `INSERT SELECT` con literales por defecto para columnas ausentes
  4. `DROP TABLE job_photos` + `RENAME` + `CREATE INDEX`
- Si `job_photos` no existe en sqlite_master: solo crear la tabla nueva.
- Cubre también el camino v2→… (donde `2→3` sí añadió `remoteUrl`):
  `PRAGMA` detecta lo presente y no rompe nada.

## Mobile checklist

- [x] Datos del usuario preservados (no se borra nada)
- [x] Edge cases: tabla ausente, columnas parcialmente presentes, BD vacía
- [x] Offline: la migración corre local, sin red

## Plan de tests

- [x] `gradlew.bat :app:assembleDebug` compila
- [x] `gradlew.bat :home:testDebugUnitTest :login:testDebugUnitTest` verdes
- [x] BD v3 real del físico en emulador: app abre tras `install -r`,
      login OK, dashboard visible con datos preservados
- [x] `PRAGMA table_info(job_photos)` post-migración muestra las 8 columnas
      y `user_version = 7`
- [x] Screenshots en `EVIDENCE.md`

## Hallazgos fuera de scope

- Solo existe `schemas/6.json` exportado — las versiones históricas no se
  guardaron, lo que impide tests de migración formales. Recomendado: spec
  `db-migration-tests` (exportSchema ya activo; las v7+ quedarán guardadas).
- `SyncWorker` no puede instanciarse por WorkManager
  (`NoSuchMethodException SyncWorker.<init>(Context, WorkerParameters)` —
  logcat del emulador). Falta `HiltWorkerFactory`/`@HiltWorker`: el sync
  nunca ha corrido realmente. Candidato a spec `sync-worker-hilt`.
- ANR en primer arranque del emulador frío (init Firebase lento) — no es
  un crash; observar.
- Pendiente opcional: repetir `install -r` + login en el Xiaomi físico
  cuando quede libre de otros agentes (la verificación se hizo con copia
  byte a byte de su BD real en emulador).
