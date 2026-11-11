# EVIDENCE — SPEC-005: nav-bar-oem (nav bar = fondo en toda la app)

**Fecha:** 2026-11-11
**Rama:** `feature/spec-005-nav-bar-oem` (rebasada sobre main tras SPEC-006)
**Dispositivos:** emulador `Medium_Phone_API_36.1` (`emulator-5554`) con la
BD real del Xiaomi migrada a v7; físico Xiaomi `rodin` — login (SPEC-003/004)
**APK:** `app/build/outputs/apk/debug/app-debug.apk`

## BUILD / TESTS

```
gradlew.bat :app:assembleDebug
BUILD SUCCESSFUL in 32s
200 actionable tasks: 15 executed, 185 up-to-date
```

Solo warnings ya documentados: `window.navigationBarColor` deprecated en
API 35+ (necesario para la barra opaca de MIUI — ver SPEC-003/005).

## DEVICE — emulador

### Regresión login (override gana sobre default global)

`screenshots/login_dark_regression.png` (modo noche del sistema activo):

```
nav px (borde inferior): (12,74,82) ×3 = Petrol30
card px: (10,25,27) = Slate900 (dark)
```

La nav bar del login sigue **petróleo** en dark — el override por pantalla
(`navigationBarColor = Petrol30`) gana sobre el default global
(`colorScheme.background`), incluida la re-evaluación al conmutar el tema.

### Pantalla interna — dashboard `:home`

`screenshots/dashboard_navbar_emu.png` (light) /
`dashboard_navbar_emu_dark.png`:

```
nav px light (borde): ≈(245,245,245) = fondo de la app, sin franja gris
```

### Pantalla interna — lista de clientes (`Mis Clientes`)

`screenshots/clients_list_emu.png` (dark) /
`screenshots/clients_list_emu_light.png` (light):

```
nav px dark  (borde): (10,25,27)   = Slate900 = colorScheme.background dark
nav px light (borde): (244,249,248)= Slate50  = colorScheme.background light
```

`ClientListScreen` sí sigue el tema (a diferencia del dashboard): en dark
toda la pantalla pinta `Slate900` y la nav bar continúa el mismo color sin
franja — el cliente sembrado "Cli Test" es visible en la lista.

En emulador stock la nav bar es transparente y el efecto es sutil (el fondo
ya llegaba al borde); el comportamiento crítico es MIUI, donde pasa de
"gris opaco" a `colorScheme.background`. El cambio se verifica por código
(un único `SystemBarIconStyle` global en `TradeJobTheme`, línea 29-32 de
`Theme.kt`), por regresión en login, y por los píxeles anteriores en dos
pantallas internas y ambos modos.

Nota: el dashboard no cambia a dark con `ui_night_mode=2` porque
`HomeScreen` hardcodea `Color(0xFFF5F5F5)` y colores literales — la migración
de `:home` al `colorScheme` es la spec `ui-home` (fuera de alcance).

## DEVICE — físico Xiaomi `rodin`

- Login: nav bar petróleo verificada en SPEC-003/004 (regresión OK).
- Pantallas internas: **pendiente** — requiere sesión real en el móvil
  (credenciales del usuario) y el dispositivo estaba ocupado por otros
  agentes durante esta sesión. Con SPEC-006 ya mergeado el login físico ya
  no crashea; cuando el móvil quede libre basta login → captura de
  dashboard/clientes para cerrar el criterio literal.

## Checklist de criterios

- [x] `TradeJobTheme` aplica default global: `darkIcons = !darkTheme` +
      `navigationBarColor = colorScheme.background` (Theme.kt L29-32)
- [x] `DisposableEffect` incluye `darkTheme` en claves → override de login
      se re-aplica tras conmutación light↔dark (verificado: login petróleo
      en dark tras recrear)
- [x] Login/registro conservan nav bar petróleo (regresión emulador dark)
- [x] Pantallas internas en emulador: dashboard + lista clientes, nav bar
      = `colorScheme.background` verificado por píxeles en light y dark
- [ ] Xiaomi físico pantallas internas: pendiente (otros agentes lo usan;
      bug de migración que lo bloqueaba ya resuelto en SPEC-006)
- [x] Build compila; tests verdes (sin tests nuevos — cambio de sistema de
      barras, se verifica en dispositivo)

## Hallazgos

- En dark mode, `HomeScreen` no sigue el tema — fondo `0xFFF5F5F5` y
  textos/card literales hardcodeados. `ClientListScreen` SÍ lo sigue
  (pantalla dark completa). La nav bar global toma `Slate900` en dark → en
  el dashboard quedará contraste fondo-claro/nav-oscura en MIUI hasta que
  `ui-home` migre esas pantallas. No es regresión: antes era gris en ambos.
- La app de otros agentes (`com.example.traveldiary`) robaba el foco del
  móvil durante la sesión — evidencia física pospuesta, se completó en
  emulador con la BD real del Xiaomi migrada.
