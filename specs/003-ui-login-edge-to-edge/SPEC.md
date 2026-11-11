# SPEC-003: Login a pantalla completa (edge-to-edge)

**Estado:** en curso
**Rama:** `feature/spec-003-e2e-login`
**Fecha:** 2026-11-02

## Qué construimos

El color petróleo de marca ocupa TODA la pantalla del login: dibuja detrás de la
status bar y de la navigation bar del sistema. Ahora el fondo se corta en ambas
franjas (se ve blanco del sistema).

## Criterios de aceptación

- [x] `enableEdgeToEdge()` en `MainActivity` antes de `setContent`
- [x] `windowSoftInputMode="adjustResize"` en el manifest (login tiene TextFields)
- [x] Fondo petróleo a sangre en `LoginScreen` (bg sin inset; contenido protegido
      con `safeDrawingPadding` + `imePadding` antes del scroll, sin doble padding)
- [x] Iconos de status/nav bar legibles sobre petróleo (oscuro) → iconos claros
      en login y registro; restaurado al salir
- [x] Build compila + tests verdes + captura en dispositivo físico (Xiaomi
      `rodin`) con el petróleo llegando a los bordes

## Fuera de alcance

- Resto de pantallas: edge-to-edge queda activo globalmente pero sus insets se
  tratan en cada spec ui-* (el usuario lo autoriza explícitamente)
- `isNavigationBarContrastEnforced` para bottom bars (sin bottom bar aún)

## Hallazgos

- `themes.xml` tiene `statusBarColor` morado legacy (lo sobreescribe
  edge-to-edge; se limpia aquí por ser 1 línea de deuda de marca)
- MIUI/HyperOS dibuja la nav bar **opaca** (gris claro con tema `.Light`)
  ignorando la transparencia de edge-to-edge. Resuelto aquí: `SystemBarIconStyle`
  acepta `navigationBarColor` opcional y pinta la nav bar de petróleo en
  login/registro (`isNavigationBarContrastEnforced = false` aplicado por
  necesidad). El resto de pantallas hereda el problema: queda en manos de cada
  spec ui-* o de pasar `Theme.TradeJobPro` a `DayNight`/colorScheme M3.
