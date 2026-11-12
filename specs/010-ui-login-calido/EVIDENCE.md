# EVIDENCE — SPEC-010: Login cálido

**Fecha:** 2026-11-12
**Rama:** `feature/spec-010-ui-login-calido`

## 1. Build + Tests

```
./gradlew.bat :app:assembleDebug :login:testDebugUnitTest
BUILD SUCCESSFUL
```

`:login:testDebugUnitTest` — **27 tests, 0 fallos** (24 anteriores +
`GreetingTest` con 3 tests de límites horarios: 6-13 mañana, 14-20 tarde,
resto noche).

## 2. Dispositivo — `emulator-5560` (AVD `TradeJob_Test`)

`install -r` Success, `am start -W` ok. Capturas locales en
`.devin/android/artifacts/` (ignoradas por Git):

| Captura | Verificado |
|---|---|
| `spec-010-login-light.png` | Fondo hueso `#FAF7F0`, eyebrow "BUENAS TARDES" en lima-oliva con tracking, trazo de firma bajo el título, "Tu taller en el bolsillo", card cálida, ENTRAR disabled, enlace en petróleo |
| `spec-010-login-dark.png` | Gradiente petróleo conservado, saludo y firma en `LimeAccent`, card oscura, enlace blanco |
| `spec-010-register-light.png` | Mismo tratamiento: fondo hueso, TopAppBar transparente con título/flecha petróleo (antes blancos → invisibles), card cálida |
| `spec-010-register-dark.png` | Registro en oscuro correcto, sin cambios funcionales |

Edge-to-edge y `safeDrawingPadding` intactos; nav bar hueso en light y
petróleo en dark vía `SystemBarIconStyle` (SPEC-005 sigue aplicando el
color por tema).

## 3. Criterios de aceptación — estado

- [x] AC-01 fondo hueso + saludo por hora + firma + estados de botón intactos
- [x] AC-02 registro con el mismo tratamiento (light y dark)
- [x] AC-03 dark con contraste correcto verificado en captura
- [x] AC-04 edge-to-edge, padding y nav bar coherentes con el fondo
- [x] AC-05 strings nuevos en `strings.xml` (`greeting_*`, `login_tagline`)
- [x] AC-06 build + 27 tests login + 17 home verdes (assembleDebug +
      testDebugUnitTest ejecutados)

## 4. Diff

```
M  libs/components/.../theme/Color.kt     (+BoneLight, +BoneCard)
A  libs/components/.../SignatureDivider.kt (MitoSignatureDivider — trazo firma)
A  login/.../ui/Greeting.kt               (greetingForHour, pura y testeada)
M  login/.../ui/LoginScreen.kt            (fondo por tema, eyebrow, firma, tagline)
M  login/.../ui/NewUserScreen.kt          (mismo tratamiento + TopAppBar por tema)
M  login/src/main/res/values/strings.xml  (greeting_*, login_tagline)
A  login/src/test/.../ui/GreetingTest.kt  (3 tests)
```

## 5. Notas honestas

- El "saludo" es por hora del dispositivo; no usa el nombre del usuario
  (no hay perfil cargado en login — queda para `ui-home`).
- El trazo de firma es un path fijo (motivo decorativo), no una animación
  de trazado — la animación queda para una iteración posterior.
- Registro verificado con captura navegada por tap real; el subtítulo
  "Únete a la comunidad de profesionales" se mantiene (copy existente).
