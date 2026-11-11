# SPEC-003: Login a pantalla completa (edge-to-edge)

**Estado:** en curso
**Rama:** `feature/spec-003-e2e-login`
**Fecha:** 2026-11-02

## Qué construimos

El color petróleo de marca ocupa TODA la pantalla del login: dibuja detrás de la
status bar y de la navigation bar del sistema. Ahora el fondo se corta en ambas
franjas (se ve blanco del sistema).

## Criterios de aceptación

- [ ] `enableEdgeToEdge()` en `MainActivity` antes de `setContent`
- [ ] `windowSoftInputMode="adjustResize"` en el manifest (login tiene TextFields)
- [ ] Fondo petróleo a sangre en `LoginScreen` (bg sin inset; contenido protegido
      con `safeDrawingPadding` + `imePadding` antes del scroll, sin doble padding)
- [ ] Iconos de status/nav bar legibles sobre petróleo (oscuro) → iconos claros
      en login y registro; restaurado al salir
- [ ] Build compila + tests verdes + captura en dispositivo físico con el
      petróleo llegando a los bordes

## Fuera de alcance

- Resto de pantallas: edge-to-edge queda activo globalmente pero sus insets se
  tratan en cada spec ui-* (el usuario lo autoriza explícitamente)
- `isNavigationBarContrastEnforced` para bottom bars (sin bottom bar aún)

## Hallazgos

- `themes.xml` tiene `statusBarColor` morado legacy (lo sobreescribe
  edge-to-edge; se limpia aquí por ser 1 línea de deuda de marca)
