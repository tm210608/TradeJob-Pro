# EVIDENCE — SPEC-004: ui-login (colorScheme + Expressive)

**Fecha:** 2026-11-11
**Rama:** `feature/spec-004-ui-login`
**Dispositivo:** emulador `sdk_gphone64_x86_64` (API 36.1, 1080x2400)
**APK:** `app/build/outputs/apk/debug/app-debug.apk`

## BUILD

```
./gradlew.bat :app:assembleDebug
BUILD SUCCESSFUL in 28s
200 actionable tasks: 17 executed, 183 up-to-date
```

Sin errores ni warnings nuevos.

## TESTS

```
./gradlew.bat :home:testDebugUnitTest :login:testDebugUnitTest
BUILD SUCCESSFUL in 13s
106 actionable tasks: 4 executed, 102 up-to-date
```

17/17 tests verdes. `:login` sigue sin tests (spec `login-tests` en cola).

## DEVICE — emulador, light theme

`cmd uimode night no` → capturas `evidence-login-light.png` y
`evidence-register-light.png` (esta carpeta).

- Gradiente petróleo a sangre (status + nav bar cubiertas), iconos claros.
- Card `surface` clara; `Iniciar Sesión` y el icono del logo en
  `colorScheme.primary` (Petrol30 — mismo color que antes, ahora por rol).
- `ENTRAR`/`REGISTRARME` deshabilitados con los defaults M3
  (`onSurface` + alphas), sustituyendo los grises ad-hoc.
- Registro: TopAppBar transparente — el gradiente continúa bajo la barra.

## DEVICE — emulador, dark theme

`cmd uimode night yes` → capturas `evidence-login-dark.png` y
`evidence-register-dark.png`.

- Gradiente petróleo fijo (marca), card pasa a `surface` dark (Slate900).
- Título e icono del logo en `colorScheme.primary` dark = Petrol80
  (verde-azulado claro, legible sobre surface oscura).
- Bordes de campo visibles: píxeles del borde del campo Email ≈
  `(206,217,218)` sobre card ≈ `(10,25,27)` — antes heredaban Petrol30
  del shim y quedaban casi invisibles sobre surface oscura.
- Nav bar sigue petróleo (`navigationBarColor = Petrol30`, no
  `colorScheme.primary` que en dark sería Petrol80).

## Pendiente — dispositivo físico

El Xiaomi `rodin` se desconectó a mitad de sesión; repetir capturas
light/dark al reconectarlo. El cambio es de theming (no OEM-dependiente):
el comportamiento MIUI de la nav bar ya se resolvió en SPEC-003 y aquí se
mantiene el patrón (`navigationBarColor` explícito).

## Checklist de criterios

- [x] Cero imports de `components.resources.*` en `:login` (grep: 0 matches
      en `login/src/main`)
- [x] `MitoTextField`+familia y `PrimaryButton`/`SecondaryButton` sin shim:
      `colorScheme.primary`/`onPrimary`, disabled = defaults M3
- [x] Gradiente a sangre en ambos temas con tokens `Petrol20`→`Petrol30`
- [x] `navigationBarColor = Petrol30` (no `colorScheme.primary`)
- [x] TopAppBar de registro `containerColor = Color.Transparent`
- [x] `LoadingIndicator` M3 Expressive en overlays de ambas pantallas
      (verificado por compilación e inspección; el estado isLoading
      requiere credenciales reales para dispararse)
- [x] Build compila + tests verdes
- [x] Capturas en emulador light + dark; físico pendiente (ver arriba)

## Notas

- Impacto cruzado aceptado en SPEC: `MitoTextField`/`PrimaryButton` migrados
  en `:libs:components` — `:home` hereda foco/botón correctos en dark; en
  light el color es idéntico al shim que sustituye.
