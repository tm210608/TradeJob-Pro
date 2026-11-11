# EVIDENCE — SPEC-003: Login edge-to-edge

**Fecha:** 2026-11-11
**Rama:** `feature/spec-003-e2e-login`
**Dispositivo:** emulador `sdk_gphone64_x86_64` (API actual, 1080x2400)
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

## Checklist de criterios

- [x] `enableEdgeToEdge()` en `MainActivity` antes de `setContent`
- [x] `windowSoftInputMode="adjustResize"` en el manifest
- [x] Fondo petróleo a sangre en `LoginScreen`/`NewUserScreen`
      (`safeDrawingPadding` + `imePadding` antes del scroll, sin doble padding:
      el `Scaffold` ya no aplica `paddingValues` al contenido)
- [x] Iconos claros sobre petróleo vía `SystemBarIconStyle(darkIcons = false)`,
      restaurados al salir (`DisposableEffect.onDispose`)
- [x] Build compila + tests verdes + captura con petróleo en los bordes

## Pendiente

- La spec pedía "dispositivo físico"; la evidencia es de **emulador**. Si hay un
dispositivo físico disponible, repetir captura antes de mergear a `main`.
- Verificar visualmente que los iconos de status/nav bar se ven claros (código
  implementado; la captura no permite confirmarlo al 100% sin inspección manual).
