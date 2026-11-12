# SPEC-010: Login cálido — "El taller te da los buenos días"

**Estado:** verificada
**Rama:** `feature/spec-010-ui-login-calido`
**Fecha:** 2026-11-12

## Qué construimos

Rediseño del login y registro para que la primera impresión sea cálida y con
personalidad de taller, manteniendo la marca petróleo+lima:

- Fondo **blanco hueso cálido** (no gris frío) en light; el petróleo pasa de
  fondo total a acento (título, iconos, botones). En dark, superficie cálida
  oscura (petróleo muy profundo con matiz cálido, no azul corporativo).
- **Saludo con voz** según la hora: "Buenos días" / "Buenas tardes" /
  "Buenas noches" como eyebrow sobre "TradeJob Pro", y subtítulo de taller:
  "A por el día." (o similar, strings en `strings.xml`).
- **Firma gráfica**: un trazo tipo firma (curva dibujada, estilo
  `SignaturePad` ya existente) como divisor sutil bajo el título — motivo
  único de la app.
- Lima solo para lo positivo: acento del saludo y detalles, no fondo.

### Contexto / por qué

La pantalla actual se siente fría y corporativa ("entrar a un banco"). Las
apps de campo que enganchan tratan al autónomo como compañero de jornada.
Objetivo: que abrir la app se sienta como entrar a tu taller, no a una web
de gestión.

## Criterios de aceptación

- [x] AC-01: Login light muestra fondo hueso, saludo según hora del día,
      trazo de firma y botón ENTRAR con los estados actuales (disabled/
      enabled) sin cambios funcionales
- [x] AC-02: Registro (`NewUserScreen`) recibe el mismo tratamiento de fondo
      y acentos
- [x] AC-03: Dark theme mantiene contraste correcto (petróleo oscuro cálido,
      no negro/azul grisáceo); light y dark verificados con capturas
- [x] AC-04: Edge-to-edge, `safeDrawingPadding` y nav bar de SPEC-003/005
      intactos (nav bar acorde al nuevo fondo)
- [x] AC-05: Todos los strings nuevos en `strings.xml` de `:login` (ES)
- [x] AC-06: `verify.cjs` OK; 41 tests unitarios siguen verdes

## Fuera de alcance

- Dashboard y resto de pantallas (specs `ui-home` etc. aparte)
- Animaciones/celebraciones (fase posterior)
- Cambios de tipografía de marca (spec `ui-typography`)
- Onboarding o tutorial

## Diseño / notas técnicas

- Tokens nuevos en `libs/components/theme/Color.kt` (hueso cálido ~#FAF7F2
  light, ~#14261F o similar cálido dark) siguiendo el patrón de SPEC-002/004;
  nada de hardcodear colores en pantallas.
- Saludo por hora: función pura pequeña (p. ej. `greetingForHour(h): @StringRes Int`)
  testeable en JVM si se sitúa en `:login` domain o `:libs:common`.
- Trazo de firma: `Canvas` Compose con `Path` cúbica, ~40 líneas, sin
  dependencias nuevas; respeta `colorScheme`.
- Componentes `Mito*` ya migrados (SPEC-004): solo cambian fondos/containers
  y textos superiores; botones y TextFields quedan igual.
- Revisar `SystemBarIconStyle`/`nav bar`: con fondo hueso la nav bar light
  pasa a hueso (SPEC-005 usa `colorScheme.background`, debería seguir
  solo).

## Plan de tests

- [ ] `node .devin/verify.cjs` OK (build + 41 tests)
- [ ] Test unitario del saludo por hora si la función queda en JVM
- [ ] Capturas en emulador y/o Xiaomi: login light y dark

## Hallazgos fuera de scope

- Ninguno previsto.
