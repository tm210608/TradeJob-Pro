# EVIDENCE — SPEC-008: Tests unitarios del módulo :login

**Fecha:** 2026-11-12
**Rama:** `feature/spec-008-login-tests`

## 1. Build

```
node .devin/verify.cjs
BUILD: OK (app + APK de pruebas)
TESTS home: OK, ejecutados
TESTS login: OK, resultado reutilizado
```

Log local: `.devin/android/artifacts/verify.log`.

## 2. Tests

`:login:testDebugUnitTest` — **22 tests, 0 fallos, 0 errores, 0 omitidos**
(antes: `NO-SOURCE`).

| Suite | Tests | Cubre |
|---|---|---|
| `LoginViewModelTest` | 8 ✓ | `loginEnable` (email válido/inválido/vacío, password 6 vs >6), éxito con y sin `userId`, error con mensaje, `clearEvent` |
| `RegisterViewModelTest` | 9 ✓ | `isValid` (campos, contraseñas distintas, nombre vacío, email inválido, password 6), `register()` inválido sin llamar al use case, éxito/error, `clearEvent` |
| `LoginUseCaseTest` | 2 ✓ | Loading→Success con mensaje+userId, Loading→Error |
| `RegisterUserUseCaseTest` | 3 ✓ | Éxito Firebase→local con `updateFirebaseUid`, error Firebase sin tocar local, fallo local tras éxito Firebase |

`:home:testDebugUnitTest` — 17/17 verdes (ejecutados por verify.cjs).

## 3. Refactor aplicado (único cambio en producción)

- `EmailValidator` de `:libs:common` expone `companion object isValid(email)`
  con la regex ya existente — misma fuente para app y tests.
- `LoginViewModel` y `RegisterViewModel` sustituyen
  `Patterns.EMAIL_ADDRESS` (no usable en tests JVM) por
  `EmailValidator.isValid`. Cero cambio visible.

## 4. Dispositivo

`emulator-5560` (AVD `TradeJob_Test`):

```
adb -s emulator-5560 install -r app-debug.apk → Success
am start -W → Status: ok, LaunchState: COLD, TotalTime: 6282 ms
```

Captura `.devin/android/artifacts/spec-008-login.png` (local, ignorada por
Git): pantalla de login renderiza idéntica — logo, título, campos
Email/Contraseña, ENTRAR desactivado con formulario vacío (consistente con
los tests de `loginEnable`).

## 5. Criterios de aceptación — estado

- [x] AC-01 `loginEnable` solo con email válido + password >6 (tests 1-4 de
      LoginViewModelTest; misma regex en app y test)
- [x] AC-02 éxito → `Event.Success` + `saveUserId` + `scheduleSync`; error →
      `Event.Error` con mensaje (tests 5-7)
- [x] AC-03 `isValid` del registro y `register()` inválido sin llamar al use
      case (tests de RegisterViewModelTest)
- [x] AC-04 éxito/error de `register()` → `RegisterEvent` correcto
- [x] AC-05 `LoginUseCase` propaga Loading→Success/Error del repositorio
- [x] AC-06 `RegisterUserUseCase`: vínculo local+uid, error Firebase sin
      tocar local, fallo local tras éxito
- [x] AC-07 `:login:testDebugUnitTest` 22 verdes; `:home` 17/17 verdes

## 6. Diff

```
M  libs/common/.../validation/Validator.kt      (EmailValidator.isValid companion)
M  login/.../ui/LoginViewModel.kt               (Patterns → EmailValidator.isValid)
M  login/.../ui/RegisterViewModel.kt            (idem)
M  login/build.gradle.kts                       (+mockk, +coroutines-test en test)
A  login/src/test/.../ui/LoginViewModelTest.kt        (8 tests)
A  login/src/test/.../ui/RegisterViewModelTest.kt     (9 tests)
A  login/src/test/.../domain/LoginUseCaseTest.kt      (2 tests)
A  login/src/test/.../domain/RegisterUserUseCaseTest.kt (3 tests)
```

## No cubierto (honesto)

- `LoginRepositoryImpl` / `FirebaseAuthDataSource` / `UserDataSourceImpl`:
  se mockean; su lógica real requiere Firebase/Room (las pruebas aisladas de
  `.devin/firebase` cubren sync; repo de login queda para spec futura o
  `journey-login`).
- La inconsistencia de longitud de contraseña (registro ≥6, login >6) queda
  documentada como hallazgo: los tests reflejan el comportamiento actual,
  no lo corrigen.
