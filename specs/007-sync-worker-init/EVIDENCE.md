# EVIDENCE — SPEC-007: Corrección de arranque de sincronización

**Fecha:** 2026-11-11 (revalidada 2026-11-12)
**Rama:** `feature/spec-007-sync-worker-init`
**Commit:** `71821c1 fix(sync): respetar la factoria Hilt al iniciar WorkManager`

Los logs y capturas son archivos locales ignorados por Git en
`.devin/android/artifacts/` y `.devin/firebase/artifacts/`.

## 1. Build

```
gradlew.bat :app:assembleDebug :app:assembleDebugAndroidTest \
            :home:testDebugUnitTest :login:testDebugUnitTest
BUILD SUCCESSFUL — 242 tareas (37 ejecutadas / 205 up-to-date el 11-11;
1 ejecutada / 241 up-to-date en la revalidación del 12-11)
```

`node .devin/verify.cjs` terminó con código 0 en ejecución independiente.
Log: `.devin/android/artifacts/spec-007-verification.log`.

## 2. Tests

### Unitarios

- `:home:testDebugUnitTest` — 17/17 verdes; re-ejecutados con `--rerun` el
  12-11: 0 errores, 0 fallos, 0 omitidos en los cinco XML de resultados.
- `:login:testDebugUnitTest` → `NO-SOURCE` (el módulo no tiene unit tests;
  ya anotado como spec candidata `login-tests`).

### Instrumentados — factoría de WorkManager

`WorkManagerInitializationTest` comprueba que la factoría usada por
WorkManager es la misma instancia que `TradeJobApplication.getWorkerFactory()`
(`HiltWorkerFactory`).

- **Antes del cambio:** falla (WorkManager usaba `DefaultWorkerFactory`).
- **Después:** `OK (1 test)` en `emulator-5560` (AVD `TradeJob_Test`).
  Log: `.devin/android/artifacts/spec-007-workmanager-precompiled.log`.

**Incidencia honesta:** los primeros intentos devolvieron `Process crashed`
por ANR de arranque — Android descomprimía/verificaba DEX bajo alta presión
de CPU antes de llegar al test. GPU host + 4 CPU + 4096 MB no bastó por sí
solo. Se precompilaron ambos APKs con
`cmd package compile -m speed -f <paquete>` (`Success` en
`com.tradejob.pro.app` y `com.tradejob.pro.app.test`) y entonces el test
pasó. Los logs fallidos se conservan (`spec-007-workmanager-*.log`) y **no
equivalen a tests aprobados**. El arranque frío sin precompilar **no está
verificado**.

### Firebase aislado — `SyncWorkerEmulatorTest`

`npm --prefix .devin/firebase run test:sync` en `emulator-5560` con emuladores
`demo-tradejob-sync` (Auth/Firestore/Storage en 127.0.0.1), Room en memoria y
APK de tests propio. Ninguna cuenta, base ni proyecto real de la app.

- **2/2 tests correctos.** Log: `.devin/firebase/artifacts/android-sync-test.log`.
- Verificado: subida de cliente/trabajo/foto, flags `isSynced`, documentos
  remotos, bytes del archivo, recuperación con referencia `gs://`, y cliente
  pendiente si no hay sesión.
- Limitación: el SDK rechaza `getReferenceFromUrl()` con la URL HTTP del
  Storage local; el test valida la URL al subir y usa `gs://` al mismo objeto
  para recuperar. **El round trip HTTP local no está validado.**
- Cobertura: se invoca `SyncWorker.doWork()` con DAOs temporales; **no es una
  prueba del planificador periódico real**.

## 3. Dispositivo

- AVD exclusivo `TradeJob_Test` (`emulator-5560`), arrancado sin ventana.
- `adb -s emulator-5560 install -r app-debug.apk` → Success (instalación
  conservadora, sin borrar datos).
- `am start -W` → `Status: ok`; `MainActivity` en `Resumed`.
- Sin errores en tags `AndroidRuntime` / `WM-WorkerFactory`.
- Capturas en `.devin/android/artifacts/`:
  - `spec-007-login.png` — primera captura con aviso de ANR de System UI
    (del sistema, no de la app).
  - `spec-007-login-after-wait.png` — login renderizado sin aviso tras pulsar
    Esperar.
- No se ha usado ningún móvil físico ni el emulador `emulator-5556`
  (pertenece a SocialChef).

## 4. Diff

```
M  app/src/main/AndroidManifest.xml              (WorkManagerInitializer removido del merge)
A  app/.../WorkManagerInitializationTest.java    (regresión de factoría)
M  gradle/libs.versions.toml                     (test-runner, work-testing)
M  libs/database/build.gradle.kts                (androidTest + manifiesto/res de tests)
A  libs/database/.../SyncWorkerEmulatorTest.kt   (2 tests de sync aislada)
A  .devin/firebase/**                            (runner, firebase.json, rules, lock)
M  AGENTS.md, PROGRESS.md                        (documentación)
```

## 5. Criterios de aceptación — estado

- [x] `WorkManagerInitializer` eliminado; AndroidX Startup conservado
- [x] `WorkManagerInitializationTest`: rojo antes, verde después (en emulador
      con APKs precompilados)
- [x] Builds `assembleDebug` + `assembleDebugAndroidTest` correctos
- [x] 17/17 tests unitarios de `:home` verdes (`--rerun`)
- [x] 2/2 tests de sincronización con Firebase aislado
- [x] App lanzada y login visible en emulador

## No verificado (honesto)

- Arranque frío de la app/tests sin precompilación DEX en el emulador.
- Planificador periódico real de WorkManager end-to-end.
- Round trip con la URL HTTP del Storage local (limitación del SDK).
- Comportamiento en dispositivo físico.
- Merge a `main` / push: pendiente de autorización.
