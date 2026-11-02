# EVIDENCE — SPEC-002: Base Material 3 Expressive + TradeJobTheme

**Fecha:** 2026-11-02
**Rama:** `feature/spec-002-ui-base`

## 1. Build

```
gradlew.bat :app:assembleDebug
BUILD SUCCESSFUL
```

Stack resuelto tras el upgrade:
- `compileSdk 37` (compose 1.12 lo exige; android-37 instalado), `targetSdk 36`
- `ExposedDropdownMenuBox`: `menuAnchor()` → `menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)` (3 call sites)
- `ExposedDropdownMenu` (eliminado en m3 1.5) → `DropdownMenu` del scope del box
- `material-icons-extended`: retirada del BOM 2026 → pin explícita a 1.7.8

## 2. Tests

`home/build/test-results/testDebugUnitTest/` — 17 tests, 0 failures, 0 errors
(SaveClient×2, SaveJob×3, JobFormVM×6, JobListVM×2, ProfileVM×4).

## 3. Dispositivo

Dispositivo físico (Xiaomi 2412DPC0AG, ADB inalámbrico):

```
adb install -r app-debug.apk → Success
monkey LAUNCHER → app lanzada, sin crash
```

### Screenshots

- `evidence/01-login-light.png` — **light**: fondo petróleo profundo + card
  surface petrol-tinted + título petróleo. Adiós azul #2196F3.
- `evidence/02-login-dark.png` — **idéntica a light, a propósito**: el fondo de
  LoginScreen usa colores hardcodeados (shim `resources/Colors.kt`), no
  `colorScheme` → el dark mode aún no altera esta pantalla. Es la evidencia que
  justifica las specs `ui-*` (migrar pantallas a `colorScheme` de verdad).

## 4. Diff

```
M  gradle/libs.versions.toml      (BOM 2026.09, m3 1.5.0-beta02, activity 1.13,
                                    nav 2.10.2, pins duplicados fuera del BOM)
M  */build.gradle.kts ×8          (compileSdk 37; :app targetSdk 36)
M  app/build.gradle.kts           (+ :libs:components, material3 pin unificado)
M  app/.../MainActivity.kt        (MaterialTheme → TradeJobTheme)
A  libs/components/.../theme/{Color,Theme}.kt  (TradeJobTheme petrol+lima,
                                    MaterialExpressiveTheme + MotionScheme.expressive)
M  libs/components/.../resources/Colors.kt     (shim legacy → paleta de marca)
M  login/.../LoginScreen.kt       (preview → TradeJobTheme)
M  home/.../JobFormScreen.kt      (menuAnchor + DropdownMenu ×2)
M  home/.../InvoiceFormScreen.kt  (menuAnchor + DropdownMenu)
A  specs/002-ui-base-expressive/*
```

## 5. Criterios — estado

- [x] BOM 2026.09.00 + material3 1.5.0-beta02 unificado
- [x] compileSdk 37 todos los módulos; targetSdk 36 en :app
- [x] `TradeJobTheme` con `MaterialExpressiveTheme` + `MotionScheme.expressive()`, light+dark
- [x] `MainActivity`/preview ya no usan `MaterialTheme` a pelo
- [x] `assembleDebug` compila (fixes de APIs incluidos)
- [x] Unit tests verdes
- [x] Login renderiza con paleta de marca en dispositivo físico

## Hallazgos (fuera de scope → cola)

- `material-icons-extended` pin 1.7.8: migrar a `material-symbols` o iconos core en specs ui-*.
- `libs.material` (Views) y `appcompat` probablemente huérfanos en app Compose-only → spec cleanup.
- Pantallas con colores hardcodeados (`resources/Colors.kt` shim): migrar a `colorScheme` en ui-*.
- `kotlinOptions` → `compilerOptions` DSL (warnings AGP 9) → spec build-hygiene.
- Tipografía de marca (Google Fonts downloadable) → spec ui-typography.
