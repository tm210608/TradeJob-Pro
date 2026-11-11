# EVIDENCE — SPEC-003: Login edge-to-edge

**Fecha:** 2026-11-11
**Rama:** `feature/spec-003-e2e-login`
**Dispositivos:** emulador `sdk_gphone64_x86_64` (1080x2400) y **físico Xiaomi `rodin`**
(modelo 2412DPC0AG, 1220x2712)
**APK:** `app/build/outputs/apk/debug/app-debug.apk`

## BUILD

```
./gradlew.bat :app:assembleDebug
BUILD SUCCESSFUL in 5m 8s
200 actionable tasks: 32 executed, 168 up-to-date
```

Sin errores. Único warning relevante (preexistente): `Icons.Filled.ArrowBack`
deprecated en `NewUserScreen.kt:53` → candidato a spec `cleanup-deps`.

## TESTS

```
./gradlew.bat :home:testDebugUnitTest :login:testDebugUnitTest
BUILD SUCCESSFUL in 19s
106 actionable tasks: 8 executed, 98 up-to-date
```

`:login` no tiene tests unitarios (spec `login-tests` en cola) → `NO-SOURCE`.

## DEVICE

```
adb install -r app-debug.apk  → Success
adb shell am start -n com.tradejob.pro.app/.MainActivity
adb shell screencap -p /sdcard/e2e_login.png
```

Captura: `evidence-login.png` (esta carpeta).

### Verificación de píxeles (bordes = petróleo, no blanco del sistema)

| Punto | RGB | Interpretación |
|---|---|---|
| (5,5) esquina sup-izq | (10,59,64) | petróleo oscuro bajo status bar |
| (540,5) centro sup | (10,59,64) | idem |
| (1075,5) sup-der | (10,59,64) | idem |
| (5,1200) media-izq | (11,66,73) | gradiente petróleo |
| (5,2395) inf-izq | (12,74,82) | gradiente petróleo bajo nav bar |
| (540,2395) centro inf | (12,74,82) | idem |
| (1075,2395) inf-der | (12,74,82) | idem |

El fondo ocupa status bar y navigation bar: edge-to-edge confirmado.

## DEVICE — dispositivo físico (Xiaomi `rodin` / HyperOS)

Primera captura en el físico reveló una **banda gris claro** (~150px) donde debía
estar la nav bar: MIUI/HyperOS dibuja la nav bar **opaca** ignorando la
transparencia de edge-to-edge (el tema `Theme.MaterialComponents.Light` deriva a
una nav bar clara).

**Fix:** `SystemBarIconStyle(darkIcons, navigationBarColor)` acepta ahora un
color opcional para la nav bar (petróleo `PrimaryColor` en login/registro) +
`isNavigationBarContrastEnforced = false`. Se restaura al salir. En Android
stock la nav bar ya es transparente (el fondo petróleo se ve detrás) y el seteo
es inocuo.

Segunda captura (`evidence-login-physical.png`, esta carpeta):

| Punto | RGB |
|---|---|
| (5,5) / (610,5) / (1215,5) — status bar | (10,59,64) petróleo |
| (5,2707) / (610,2707) / (1215,2707) — nav bar | (12,74,82) petróleo |

Petróleo de borde a borde confirmado en físico; iconos status bar claros y
legibles en la captura.

## Checklist de criterios

- [x] `enableEdgeToEdge()` en `MainActivity` antes de `setContent`
- [x] `windowSoftInputMode="adjustResize"` en el manifest
- [x] Fondo petróleo a sangre en `LoginScreen`/`NewUserScreen`
      (`safeDrawingPadding` + `imePadding` antes del scroll, sin doble padding:
      el `Scaffold` ya no aplica `paddingValues` al contenido)
- [x] Iconos claros sobre petróleo vía `SystemBarIconStyle(darkIcons = false)`,
      restaurados al salir (`DisposableEffect.onDispose`)
- [x] Build compila + tests verdes + captura **en dispositivo físico** con el
      petróleo llegando a los bordes

## Notas

- `window.navigationBarColor` está deprecated en API 35+ pero sigue siendo la
  única vía para colorear la nav bar opaca de MIUI. Warning de compilación
  aceptado y documentado.
