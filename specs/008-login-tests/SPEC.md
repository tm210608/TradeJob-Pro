# SPEC-008: Tests unitarios del módulo :login

**Estado:** verificada
**Rama:** `feature/spec-008-login-tests`
**Fecha:** 2026-11-12

## Qué construimos

Suite de tests unitarios para `:login` — hoy el módulo no tiene ninguno
(`:login:testDebugUnitTest` → `NO-SOURCE`). Cubre los ViewModels de login y
registro y los use cases de dominio, siguiendo las convenciones de `:home`
(JUnit4 + mockk + coroutines-test + `UnconfinedTestDispatcher`).

### Contexto / por qué

El acceso a la app es el código más delicado y solo se prueba a mano. Un
error en validación o en el flujo de login puede impedir entrar a la app sin
que nadie lo note hasta que un usuario lo sufra.

Decisión aprobada: la validación de email se extrae de
`android.util.Patterns` (no usable en tests JVM) a una función pura en
`:libs:common`, reutilizando el `EmailValidator`/regex del framework de
validación existente.

## Criterios de aceptación

- [x] AC-01: `LoginViewModel.onLoginChanged` activa `loginEnable` solo con
      email válido y contraseña de más de 6 caracteres; la misma regex decide
      en producción y en el test (una sola fuente en `:libs:common`)
- [x] AC-02: `LoginViewModel.login` con `Result.Success` emite
      `Event.Success`, guarda `userId` en `SessionManager` y llama a
      `syncManager.scheduleSync()`; con `Result.Error` emite `Event.Error`
      con el mensaje de la excepción
- [x] AC-03: `RegisterViewModel` marca `isValid` solo cuando nombre no vacío,
      email válido, contraseña ≥6 y coinciden ambas contraseñas; `register()`
      inválido emite `Error` sin llamar al use case
- [x] AC-04: `RegisterViewModel.register` con éxito emite
      `RegisterEvent.Success`; con error del use case emite `Error`
- [x] AC-05: `LoginUseCase` propaga Success/Error del `LoginRepository` y
      emite `Loading` primero
- [x] AC-06: `RegisterUserUseCase`: éxito Firebase → registra local, vincula
      `firebaseUid` y emite `Success(localId)`; error Firebase → `Error` sin
      tocar `UserDataSource`
- [x] AC-07: `gradlew.bat :login:testDebugUnitTest` ejecuta los nuevos tests
      en verde (ya no `NO-SOURCE`) y los 17 de `:home` siguen verdes

## Fuera de alcance

- Cambios de comportamiento visibles (mensajes, navegación, UI)
- Tests de `LoginRepositoryImpl`/`FirebaseAuthDataSource` (requieren Firebase;
  cubiertos por las pruebas aisladas de `.devin/firebase`)
- `LoginScreenTest` de androidTest existente y journeys e2e (spec `journey-login`)
- i18n de strings hardcodeados en ViewModels (spec `i18n-domain`)
- Cualquier cambio en producción que no sea la extracción de la validación
  de email

## Diseño / notas técnicas

- Módulos afectados: `:login` (tests nuevos + 2 ViewModels tocados para usar
  el validador compartido) y `:libs:common` (función/companion de validación
  de email reutilizable, p. ej. `EmailValidator.isValid(email)` o `object`
  expuesto; regex ya existente en `Validator.kt`).
- `login/build.gradle.kts`: añadir `testImplementation(libs.mockk)` y
  `testImplementation(libs.kotlinx.coroutines.test)` — ya están en catálogo.
- Mocks: `LoginUseCase`, `SessionManager`, `SyncManager`, `AnalyticsHelper`,
  `LoginRepository`, `FirebaseAuthDataSource`, `UserDataSource` (todos
  interfaces/clases mockables con mockk).
- `SheetValue` (material3) usado en `Status.sheetValue`: los tests de login
  no lo tocan; si molesta en JVM se evalúa en implementación (es una clase
  de Compose, no de Android framework).
- Ojo: `RegisterViewModel` usa `password.length >= 6` y `LoginViewModel` usa
  `> 6` — inconsistente preexistente; los tests documentan el comportamiento
  ACTUAL, sin corregirlo (anotar como hallazgo).

## Mobile checklist

- [x] Edge cases: contraseñas distintas en registro, email vacío/inválido,
      `userId == null` en login (no debe llamar a `saveUserId`)

## Plan de tests

- [ ] `gradlew.bat :app:assembleDebug` compila
- [ ] `gradlew.bat :login:testDebugUnitTest` — nuevos tests verdes
- [ ] `gradlew.bat :home:testDebugUnitTest` — 17/17 siguen verdes
- [ ] `node .devin/verify.cjs` OK
- [ ] App lanzada en emulador/dispositivo: login renderiza igual (refactor
      sin cambio visible); captura en EVIDENCE

## Hallazgos fuera de scope

- `LoginViewModel` requiere contraseña >6 y `RegisterViewModel` ≥6: con 6
  caracteres el registro admite una contraseña que el login rechazaría.
  Posible bug real → documentar, no corregir en esta spec.
