# EVIDENCE — SPEC-009: Longitud mínima de contraseña unificada

**Fecha:** 2026-11-12
**Rama:** `feature/spec-009-password-min-length`

## 1. Build

```
node .devin/verify.cjs
BUILD: OK (app + APK de pruebas)
TESTS home: OK
TESTS login: OK
```

## 2. Tests

`:login:testDebugUnitTest` — **24 tests, 0 fallos** (los 22 de SPEC-008 +
los 2 casos límite nuevos):

| Suite | Tests |
|---|---|
| `LoginViewModelTest` | 9 ✓ (incluye "123456" → activa y "12345" → no) |
| `RegisterViewModelTest` | 10 ✓ (incluye "12345" → `isValid` falso) |
| `LoginUseCaseTest` | 2 ✓ |
| `RegisterUserUseCaseTest` | 3 ✓ |

`:home:testDebugUnitTest` — 17/17 verdes.

## 3. Dispositivo

`emulator-5560` (AVD `TradeJob_Test`): `install -r` Success, `am start -W`
ok, `TotalTime: 9222 ms`. Captura local `spec-009-login.png`: login idéntico
(sin cambio visible — el cambio solo afecta a cuándo se activa ENTRAR).

## 4. Criterios de aceptación — estado

- [x] AC-01: password de exactamente 6 + email válido → `loginEnable` true
- [x] AC-02: registro sigue exigiendo ≥6; 5 inválido en ambos lados
- [x] AC-03: casos límite "123456"/"12345" cubiertos en ambos ViewModels
- [x] AC-04: `:login:testDebugUnitTest` y `:home:testDebugUnitTest` verdes

## 5. Diff

```
M  login/.../ui/LoginViewModel.kt        (isValidPassword: >6 → >=6)
M  login/src/test/.../LoginViewModelTest.kt    (test actualizado + caso 5 chars)
M  login/src/test/.../RegisterViewModelTest.kt (comentario + caso 5 chars)
```

Efecto para el usuario: quien se registró con contraseña de 6 caracteres ya
puede iniciar sesión (antes el botón ENTRAR nunca se activaba).
