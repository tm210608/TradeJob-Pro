# EVIDENCE — SPEC-006: migración Room `job_photos` (6→7)

**Fecha:** 2026-11-11
**Rama:** `feature/spec-006-migration-job-photos`
**Dispositivos:** emulador `Medium_Phone_API_36.1` (`emulator-5554`,
sdk_gphone64_x86_64, 1080x2400) con la **BD real v3 del Xiaomi `rodin`**
sembrada en sus datos, **y el propio Xiaomi físico** (2412DPC0AG,
1220x2712, HyperOS) con su BD original una vez liberado.
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

## DEVICE — físico Xiaomi `rodin` (verificación final, 20:37)

Con el móvil liberado se repitió la prueba real: `install -r` sobre la BD
v3 existente (sin desinstalar), arranque, login con credenciales de prueba
→ la app navegó al dashboard sin crash. Pull de la BD post-login:

```
user_version: 7            (era 3)
job_photos: [id, jobId, photoPath, description, type, createdAt, isSynced, remoteUrl]
fks: [jobs(jobId→id) ON DELETE CASCADE]
indices: ['index_job_photos_jobId']
```

`screenshots/dashboard_xiaomi_after_migration.png`: dashboard funcional en
el Xiaomi tras migrar su propia BD — `Mis Clientes` también accesible.
Nota de input: en MIUI el texto por `adb input text` disparaba el popup de
"Traductor de Google" y el foco se movía por `adjustResize`; la secuencia
fiable fue tap → `input text` → keyevents por carácter para password.

## Checklist de criterios

- [x] `version = 7` + `MIGRATION_6_7` registrada en `DatabaseModule`
- [x] `TableInfo` exacto tras migrar (columnas, FK CASCADE, índice) —
      verificado sobre BD real
- [x] Conserva filas (`isSynced=0`, `remoteUrl=NULL` por defecto) —
      verificado con fila sembrada sobreviviendo (emulador)
- [x] App abre y navega a `:home` sin crash con BD v3 real — **verificado
      en el Xiaomi físico con su propia BD: 3→7, dashboard operativo**
- [x] Build compila + tests verdes
