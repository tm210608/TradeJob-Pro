# SPEC-004: ui-login — Login/Registro sobre colorScheme + toques Expressive

**Estado:** verificada
**Rama:** `feature/spec-004-ui-login`
**Fecha:** 2026-11-11

## Qué construimos

Migrar `LoginScreen` y `NewUserScreen` del shim `resources/Colors.kt` a
`MaterialTheme.colorScheme`, junto con los componentes `Mito*` que consumen
(`MitoTextField` y familia, `PrimaryButton`, `SecondaryButton`), para que el
flujo de autenticación respete light/dark theme. Toques M3 Expressive acotados
donde aportan (indicador de carga), sin re-maquetar el formulario.

### Contexto / por qué

SPEC-002 introdujo `TradeJobTheme` (petróleo+lima, light/dark fijo de marca) y
dejó las pantallas usando el shim de compatibilidad. Login/registro siguen
hardcodeados a `PrimaryColor`/`PrimaryDarkColor`: en dark theme los bordes de
los campos (petróleo oscuro sobre surface oscura) quedan casi invisibles y el
botón deshabilitado usa grises fuera de escala.

## Criterios de aceptación

- [x] Cero imports de `com.tradejob.pro.components.resources.*` en `:login`
      (verificable con grep sobre `login/src/main`)
- [x] `MitoTextField`/`Mito*Field`, `PrimaryButton` y `SecondaryButton` no
      referencian el shim de colores: usan `colorScheme` (`primary`,
      `onPrimary`, disabled = `onSurface` con alphas M3)
- [x] Gradiente petróleo a sangre se mantiene en ambos temas, referenciando
      tokens de `theme/Color.kt` (`Petrol20`→`Petrol30`), no el shim
- [x] `navigationBarColor` pasado a `SystemBarIconStyle` usa el token del
      extremo inferior del gradiente (`Petrol30`), no `colorScheme.primary`
      (en dark sería `Petrol80`, demasiado claro)
- [x] TopAppBar de registro transparente sobre el gradiente (título e iconos
      blancos), en vez de `PrimaryDarkColor` opaco
- [x] Loading overlay usa `LoadingIndicator` (M3 Expressive) en vez de
      `CircularProgressIndicator`, en ambas pantallas
- [x] Capturas en **light y dark** en emulador y físico (Xiaomi `rodin`):
      login y registro con card legible, bordes de campo visibles, CTA con
      contraste correcto
- [x] Build compila + tests verdes

## Fuera de alcance

- Re-maquetar el login (ButtonGroup/FABMenu/Carousel no aplican a un form de
  auth; justificado en Diseño)
- Strings hardcodeados en `LoginViewModel`/`RegisterViewModel` → spec `i18n-domain`
- Tests unitarios de `:login` → spec `login-tests`
- Otras pantallas (`ui-home`, `ui-clients`…) y migración de los `Mito*` que no
  consume login (p. ej. `MitoButtonSheet`, `SignaturePad`)
- Eliminar el shim `resources/Colors.kt`: sigue usándose en `:home` hasta sus
  specs ui-*
- `Dimensions.kt`/`Fonts.kt` del paquete `resources` (no dependen del tema)

## Diseño / notas técnicas

- Módulos afectados: `:login` (2 screens), `:libs:components` (`MitoTextField`,
  `MitoButtons`)
- Componentes `Mito*` a reutilizar: los mismos; solo cambia su fuente de color
- Decisiones:
  - **Alcance propuesto = colorScheme + Expressive acotado.** El login es un
    formulario de 3 inputs + CTA: no hay agrupación de acciones (ButtonGroup),
    ni menú FAB, ni contenido para Carousel. Lo Expressive que aporta aquí es
    `LoadingIndicator` (la onda de M3) y el motion del tema ya heredado.
  - **Gradiente = marca fija.** SPEC-002 fijó marca light/dark; el petróleo a
    sangre se mantiene en dark. Se referencia `theme/Color.kt` directamente
    (los tokens Petrol* son públicos) y no `colorScheme` porque el gradiente
    es un fondo de marca, no un rol semántico.
  - **Texto sobre gradiente:** blanco fijo (fondo siempre oscuro).
  - **Card:** `colorScheme.surface` (ya lo hace) — en dark pasa a `Slate900`
    automáticamente.
  - **Impacto cruzado aceptado:** migrar `MitoTextField`/`PrimaryButton` a
    `colorScheme.primary` cambia el foco/botones en `:home` solo en dark
    (light: `primary` = `Petrol30` = mismo color que hoy). Es la dirección
    correcta y adelanta trabajo a las specs ui-*.
  - **`SecondaryButton`** (contentColor → `colorScheme.primary`): no se usa
    sobre el gradiente en login (el enlace de registro es `TextButton` blanco
    fijo, se mantiene).

## Mobile checklist

- [x] Estados UI: loading (overlay + `LoadingIndicator`), error (snackbar),
      empty (N/A)
- [x] Navegación: rutas `login`/`register` sin cambios; back stack intacto
- [x] Lifecycle: sin cambios de estado; `SystemBarIconStyle` sigue en
      `DisposableEffect`
- [x] i18n: sin strings nuevos previstos; si aparece alguno → `strings.xml`

## Plan de tests

- [x] `gradlew.bat :app:assembleDebug` compila
- [x] `gradlew.bat :home:testDebugUnitTest :login:testDebugUnitTest` verdes
- [x] Flujo en emulador: login → registro → atrás, en light y dark
- [x] Flujo en dispositivo físico (Xiaomi `rodin`): login → registro → atrás,
      en light y dark
- [x] Screenshots light + dark en `EVIDENCE.md` con verificación de píxeles
      (bordes de campo visibles sobre surface en dark)

## Hallazgos fuera de scope

- (rellenar durante el trabajo)
