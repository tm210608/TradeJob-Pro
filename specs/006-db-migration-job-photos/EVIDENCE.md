# EVIDENCE — SPEC-006: migración Room `job_photos` (6→7)

**Fecha:** 2026-11-11
**Rama:** `feature/spec-006-migration-job-photos`
**Dispositivo:** emulador `Medium_Phone_API_36.1` (`emulator-5554`,
sdk_gphone64_x86_64, 1080x2400) con la **BD real v3 del Xiaomi `rodin`**
sembrada en sus datos (el físico estaba ocupado por otros agentes; la BD
usada es una copia byte a byte extraída de él con `run-as`).
**APK:** `app/build/outputs/apk/debug/app-debug.apk`

## BUILD

```
gradlew.bat :app:assembleDebug :home:testDebugUnitTest :login:testDebugUnitTest
BUILD SUCCESSFUL in 2m 13s
212 actionable tasks: 26 executed, 186 up-to-date
```

Un primer intento falló por lock de Windows sobre
`libs/components/.../classes.jar`; resuelto con `gradlew.bat --stop` +
reintento. Sin errores ni warnings nuevos.

## TESTS

`:home:testDebugUnitTest` verde (`:login` NO-SOURCE, como siempre — spec
`login-tests` en cola). No se añaden tests: `MigrationTestHelper` requiere
schemas históricos exportados y solo existía `6.json` (ver Hallazgos en
SPEC.md); desde v7 quedan exportados para la futura spec
`db-migration-tests`.

## Simulación offline sobre la BD real

Antes de tocar el dispositivo se simuló la cadena completa `3→4→5→6→7`
sobre una copia de la BD extraída del Xiaomi (`tjdb2.db`, `user_version=3`,
`job_photos` con 6 columnas — sin `isSynced`/`remoteUrl`):

```
job_photos existe: True
columnas antes: ['createdAt','description','id','jobId','photoPath','type']
users: OK
clients: OK
jobs: OK
job_photos: OK
quotes: OK
quote_items: OK
invoices: OK
job_photos fks: [jobs(jobId→id) ON UPDATE NO ACTION ON DELETE CASCADE]
indices: ['index_job_photos_jobId']
RESULTADO: TODO OK
```

Las 7 entidades resultantes coinciden con `schemas/.../7.json` en
columnas, `notNull`, FK e índices.

## DEVICE — emulador con BD v3 real

### Corrida 1: BD tal cual del móvil (todas las tablas a 0 filas)

1. `adb install -r` (sin desinstalar → conserva datos), push de la BD v3 a
   `databases/trade_job_database` vía `run-as`.
2. `am start` → app arranca sin crash (login visible; el primer arranque en
   emulador frío produjo un ANR de lentitud — descartado con "Wait", sin
   relación con la migración: la BD seguía en v3 porque Room aún no la
   había abierto).
3. Intento de login con credenciales falsas → dispara el primer acceso a
   Room → migración `3→4→5→6→7` completa **sin crash** y la app navega al
   dashboard (`screenshots/dashboard_after_migration_empty.png`).

Verificación post-migración (pull binario-seguro con `exec-out`):

```
user_version: 7
job_photos: [id, jobId, photoPath, description, type, createdAt, isSynced, remoteUrl]
fks: [jobs(jobId→id) CASCADE]
indices: ['index_job_photos_jobId']
```

### Corrida 2: BD sembrada con datos (preservación real)

Como la BD del móvil tenía 0 filas, se sembró en la copia: `clients`+1 →
`jobs`+1 → `job_photos`+1 (con el esquema viejo de 6 columnas). Se
repitió el ciclo force-stop → seed → arranque → login.

Resultado tras migrar:

```
user_version: 7
clients: 1 | jobs: 1 | job_photos: 1
photo: (1, 1, '/tmp/foto.jpg', 'BEFORE', isSynced=0, remoteUrl=None)
```

`screenshots/dashboard_after_migration_seeded.png`: el dashboard muestra
"1 Clientes", "1 Pendientes" y el trabajo "Job Test" en recientes — **los
datos sobrevivieron la migración y son visibles en UI**.

## Hallazgos fuera de scope (documentados en SPEC.md)

- `SyncWorker` no puede instanciarse por WorkManager:
  `NoSuchMethodException SyncWorker.<init>(Context, WorkerParameters)` —
  falta `HiltWorkerFactory`/anotación `@HiltWorker`. El worker nunca ha
  corrido; candidato a spec `sync-worker-hilt`.
- ANR en primer arranque del emulador frío (Firebase init lento) — no
  reproducible como crash; observar.
- Solo `6.json` estaba exportado; desde v7 los schemas quedan guardados
  (`7.json` commiteado).

## Checklist de criterios

- [x] `version = 7` + `MIGRATION_6_7` registrada en `DatabaseModule`
- [x] `TableInfo` exacto tras migrar (columnas, FK CASCADE, índice) —
      verificado sobre BD real
- [x] Conserva filas (`isSynced=0`, `remoteUrl=NULL` por defecto) —
      verificado con fila sembrada sobreviviendo
- [x] App abre y navega a `:home` sin crash con BD v3 real
- [x] Build compila + tests verdes
- [ ] Xiaomi físico con su propia BD: pendiente — el dispositivo está
      ocupado por otros agentes; se verificó sobre copia byte a byte de su
      BD en emulador. Repetir `install -r` + login en el físico cuando
      quede libre cierra el criterio literal.
