# SPEC-007: Corrección de arranque de sincronización (WorkManager + Hilt)

**Estado:** verificada (con limitaciones documentadas)
**Rama:** `feature/spec-007-sync-worker-init`
**Fecha:** 2026-11-11 (revalidada 2026-11-12)

## Qué construimos

La app no iniciaba la sincronización: WorkManager se inicializaba con
`DefaultWorkerFactory` en lugar de la `HiltWorkerFactory` que
`TradeJobApplication` ya proporciona, así que `SyncWorker` (`@HiltWorker`)
nunca podía crearse. Corrección del arranque y verificación con datos
aislados en Firebase local.

### Contexto / por qué

AndroidX Startup registra `WorkManagerInitializer` automáticamente y crea un
WorkManager con la factoría por defecto, ignorando el `Configuration.Provider`
de la `Application`. La sincronización fallaba en silencio en dispositivo.

## Criterios de aceptación

- [x] `WorkManagerInitializer` eliminado del manifiesto fusionado; se
      conserva AndroidX Startup y el resto de inicializadores
- [x] WorkManager usa realmente la `HiltWorkerFactory` de la app:
      `WorkManagerInitializationTest` falla antes del cambio y pasa después
- [x] `gradlew.bat :app:assembleDebug :app:assembleDebugAndroidTest` compila
- [x] `gradlew.bat :home:testDebugUnitTest :login:testDebugUnitTest` verde
      (17/17 home; login `NO-SOURCE`)
- [x] Prueba instrumentada correcta en emulador
- [x] Sincronización verificada con Firebase **aislado** (`demo-tradejob-sync`,
      emuladores locales): subida de cliente/trabajo/foto, flags locales,
      documentos remotos, bytes del archivo y cliente pendiente sin sesión
- [x] App instalada y lanzada en emulador, `MainActivity` Resumed, login visible

## Fuera de alcance

- Planificador periódico real de WorkManager (no probado end-to-end)
- Round trip HTTP de Storage local (limitación del SDK, ver Hallazgos)
- `SyncWorker` devuelve éxito aunque una recuperación falle (hallazgo
  documentado, no corregido)
- Merge a `main`, push, cualquier cambio en dispositivo físico

## Diseño / notas técnicas

- `app/src/main/AndroidManifest.xml`: provider `InitializationProvider` con
  `tools:node="merge"` y `meta-data WorkManagerInitializer tools:node="remove"`.
- Nuevo `WorkManagerInitializationTest` (androidTest de `:app`): afirma que la
  factoría de `WorkManager.getInstance()` es la misma instancia que
  `TradeJobApplication.getWorkerFactory()`.
- Pruebas de sync aisladas: `SyncWorkerEmulatorTest` en `androidTest` de
  `:libs:database`, con Room en memoria y `SyncWorker.doWork()` invocado sobre
  DAOs temporales. Herramientas en `.devin/firebase/` (`firebase-tools@15.34.2`,
  `npm --prefix .devin/firebase run test:sync`).
- Catálogo: `androidx.test:runner` y `androidx.work:work-testing`.

## Mobile checklist

- [x] Offline: `SyncWorker` reintenta según política; cliente pendiente si no
      hay sesión (cubierto por el test emulador)
- [x] Edge cases: ANR de arranque en emulador lento documentado; precompilación
      `-m speed -f` usada solo para preparar el emulador de pruebas

## Plan de tests

- [x] Builds `assembleDebug` + `assembleDebugAndroidTest`
- [x] 17 tests `:home` (re-ejecutados con `--rerun` el 12-11, 0 fallos)
- [x] `WorkManagerInitializationTest` OK (1 test) en `emulator-5560`
- [x] 2/2 `SyncWorkerEmulatorTest` con Firebase aislado
- [x] `am start -W` Status: ok + capturas del login

## Hallazgos fuera de scope

- El SDK rechaza `getReferenceFromUrl()` con el host del emulador
  (`127.0.0.1`): el test reescribe la URL al formato de producción
  (`firebasestorage.googleapis.com`) y verifica el round trip completo
  URL → referencia → descarga (12-11). El host emulador en sí no se puede
  parsear con el SDK — limitación documentada, no del código de la app.
- Una recuperación fallida se registra en logs pero `SyncWorker` devuelve
  éxito → espec candidata (`sync-completa` / robustez de sync).
- `npm audit`: 14 avisos moderados transitivos de `firebase-tools`, sin
  altos/críticos; no se aplican downgrades ni overrides.
- Arranque frío: verificado el 12-11 sin precompilar en `emulator-5560`
  (ver EVIDENCE). Los ANR iniciales eran verificación DEX del AVD, no del
  código. Sigue sin probarse en dispositivo físico.
