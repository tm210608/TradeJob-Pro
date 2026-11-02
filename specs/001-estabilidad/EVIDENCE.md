# EVIDENCE — SPEC-001: Endurecimiento de estabilidad

**Fecha:** 2026-11-02
**Rama:** `feature/spec-001-estabilidad`

## 1. Build

```
gradlew.bat :app:assembleDebug :home:testDebugUnitTest :login:testDebugUnitTest
BUILD SUCCESSFUL in 6m 14s
```

Incluye la compilación tras eliminar el módulo `:libs:network` del `settings.gradle.kts`:
ningún módulo dependía de él aparte de `:login` (dependencia retirada).

## 2. Tests

`home/build/test-results/testDebugUnitTest/` — 17 tests, 0 failures, 0 errors:

| Suite | Tests |
|---|---|
| SaveClientUseCaseTest | 2 ✓ |
| SaveJobUseCaseTest | 3 ✓ |
| JobFormViewModelTest | 6 ✓ (incluye el test corregido) |
| JobListViewModelTest | 2 ✓ |
| ProfileViewModelTest | 4 ✓ |

`:login:testDebugUnitTest` → NO-SOURCE (no hay unit tests en el módulo; solo
`androidTest`). Detectado para spec futura: el módulo login no tiene cobertura unit.

## 3. Dispositivo

Intento inicial en dispositivo físico (Xiaomi 2412DPC0AG por ADB inalámbrico):
la conexión se cayó mid-install (`device offline`). Evidencia tomada en
`emulator-5554` (API reciente, boot completado).

```
adb -s emulator-5554 install -r app/build/outputs/apk/debug/app-debug.apk
→ Performing Streamed Install / Success

adb -s emulator-5554 shell monkey -p com.tradejob.pro.app -c ...LAUNCHER 1
→ Events injected: 1
```

### Screenshots

- `evidence/01-app-launched.png` — app lanzada post-refactor: pantalla de login
  renderiza completa (logo, título, card de login, campos Email/Contraseña,
  botón ENTRAR en estado disabled, enlace de registro). Sin crash, sin ANR.
- `evidence/02-login-form-filled.png` — intento de input sintético por
  coordenadas: los taps no aterrizaron en los TextField (limitación conocida de
  `adb shell input` sobre Compose). Se deja constancia: la interacción real del
  formulario se cubrirá con UI tests/journeys en spec futura.

## 4. Diff

```
M  home/.../HomeViewModel.kt          (getUserId() != -1L → != null)
M  home/.../ProfileViewModel.kt       (idem)
M  home/.../JobFormViewModelTest.kt   (assert acorde al validation framework)
M  libs/core/.../SessionManager.kt    (getUserId(): Long?)
D  libs/network/**                    (módulo muerto: dummy_login + Retrofit dummyapi.com)
D  login/.../LoginRemoteDataSource.kt + Impl.kt  (código muerto, sin invocadores)
M  login/.../LoginModule.kt           (binding muerto eliminado)
M  login/.../{LoginRepositoryImpl,UserDataSourceImpl,RegisterViewModel,RegisterUserUseCase,UserDataSource,LoginRepository}.kt (imports → login.domain)
A  login/domain/{User,LoginResponse}.kt (modelos reubicados desde libs:network)
M  login/build.gradle.kts             (sin :libs:network)
M  settings.gradle.kts                (sin :libs:network)
M  gradle/libs.versions.toml          (sin retrofit/converter-gson)
A  AGENTS.md, specs/README.md, specs/templates/SPEC.md, specs/001-estabilidad/*
```

## 5. Criterios de aceptación — estado

- [x] `JobFormViewModelTest` verde (17/17)
- [x] `:libs:network` eliminado; `User`/`LoginResponse` en `login.domain`
- [x] `LoginRemoteDataSource`/`Impl` + binding eliminados
- [x] `SessionManager.getUserId(): Long?`; sentinel `-1L` eliminado
- [x] `assembleDebug` compila
- [x] Unit tests 100% verdes
- [x] APK instalado y app lanzada en emulador con evidencia

## Pendientes detectados (fuera de scope, ya en SPEC.md)

- Sync de quotes/invoices a Firestore
- i18n de strings en ViewModels
- Cobertura unitaria nula en `:login`
- Journey e2e del flujo de login con UI tests (no taps sintéticos)
