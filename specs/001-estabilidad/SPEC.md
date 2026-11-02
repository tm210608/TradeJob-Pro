# SPEC-001: Endurecimiento de estabilidad post-auditoría

**Estado:** verificada
**Rama:** `feature/spec-001-estabilidad`
**Fecha:** 2026-11-02

## Qué construimos

Verificación del estado real del repo tras el informe de mayo (`INFORME_REVISION.md`),
arreglo del único test roto y eliminación del código muerto confirmado.

### Contexto / por qué

El informe de mayo listaba 8 errores de compilación y 8 bugs críticos. Auditoría manual
de noviembre confirma que casi todo ya se resolvió (navegación con args, confirmaciones
de borrado, `isLoading`, Firebase Auth + BCrypt, validación de email, `Resources.kt`
eliminado). Quedan: 1 test roto, un módulo entero de código muerto y un sentinel `-1L`
frágil. Es el piloto del ciclo SDMD: spec pequeña, diff acotado, evidencia en device.

## Criterios de aceptación

- [x] `JobFormViewModelTest` verde: el test de "save con título vacío" afirma el
      comportamiento actual (evento Error + `titleError` en `ValidationResult.Error`)
- [x] El módulo `:libs:network` (dummy_login + Retrofit a `dummyapi.com`) eliminado
      del build; `User` y `LoginResponse` viven en `login.domain`
- [x] `LoginRemoteDataSource`/`Impl` y su binding en `LoginModule` eliminados
      (nadie los invocaba: el login va por Firebase + Room)
- [x] `SessionManager.getUserId()` devuelve `Long?`; se elimina el sentinel `-1L`
      en `HomeViewModel` y `ProfileViewModel`
- [x] `gradlew.bat :app:assembleDebug` compila
- [x] `gradlew.bat :home:testDebugUnitTest :login:testDebugUnitTest` 100% verde
- [x] APK instalado y app lanzada en dispositivo físico (evidencia en `EVIDENCE.md`)

## Fuera de alcance

- Sincronización de quotes/invoices a Firestore (gap confirmado → spec futura)
- Strings hardcodeados en ViewModels (i18n de la capa domain → spec futura)
- Verifactu, rediseño M3 Expressive, tests de migración Room
- Cualquier cambio de comportamiento visible para el usuario

## Diseño / notas técnicas

- `User` se mueve a `com.tradejob.pro.login.domain` (donde pertenece; un modelo de
  dominio no debe vivir en un módulo de red). Usuarios actuales: `RegisterUserUseCase`,
  `UserDataSource(Impl)`, `RegisterViewModel`.
- `LoginResponse` se mueve a `com.tradejob.pro.login.domain`. Usuarios: `LoginResult`,
  `LoginRepositoryImpl`.
- `settings.gradle.kts`: quitar `:libs:network`. `login/build.gradle.kts`: quitar
  `implementation(project(":libs:network"))`. `libs.versions.toml`: quitar
  `retrofit`, `converter-gson` (nadie más los usa). Borrar `libs/network/`.
- `SessionManager.getUserId(): Long?` → call sites usan `userId != null`.

## Mobile checklist

- [x] Offline: el fallback a Room en login se mantiene intacto
- [x] Edge cases: `getUserId()` sin sesión → `null`, no `-1`

## Plan de tests

- [x] `gradlew.bat :app:assembleDebug` compila tras borrar el módulo
- [x] `JobFormViewModelTest` actualizado y verde junto al resto de la suite
- [x] `adb install -r` + `am start` en device; screenshot de login en `EVIDENCE.md`

## Hallazgos fuera de scope

- `SyncWorker` solo sincroniza clients/jobs/photos: **quotes e invoices nunca suben
  a Firestore** → spec futura `sync-completa`.
- `LoginRepositoryImpl` tiene un `else -> LoginResult(Result.Loading, null)`
  defensivo inalcanzable; se deja (harmless, exhaustividad de `when`).
- Mensajes de error hardcodeados en español dentro de ViewModels (`home` y `login`)
  → spec futura de i18n en capa domain (patrón `UiText` o strings vía context).
- Dependencias `retrofit`/`converter-gson` quedarán en catálogo solo si se usa
  `:libs:network` en el futuro; de momento se eliminan (git guarda el historial).
