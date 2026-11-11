# SPEC-005: nav-bar-oem — barra de navegación coherente en toda la app

**Estado:** en curso
**Rama:** `feature/spec-005-nav-bar-oem`
**Fecha:** 2026-11-11

## Qué construimos

En el Xiaomi (MIUI/HyperOS), la barra inferior del sistema (los botones de
navegación) se dibuja **opaca en gris claro** en todas las pantallas salvo
login/registro, rompiendo la imagen de marca. Se pinta del color de fondo de
la app en TODAS las pantallas, de una sola vez y para siempre — no pantalla
a pantalla.

### Contexto / por qué

SPEC-003 resolvió esto solo para login/registro con el parámetro
`navigationBarColor` de `SystemBarIconStyle`. Las ~11 pantallas de `:home`
(dashboard, clientes, trabajos, presupuestos, facturas, perfil) siguen con la
franja gris. En Android stock la nav bar es transparente y no se nota; en
MIUI se ve un recorte feo. Aprovechamos para fijar también el color de los
iconos de las barras (claros/oscuros según tema) de forma determinista en
todos los fabricantes.

## Criterios de aceptación

- [ ] `TradeJobTheme` aplica por defecto: iconos de status/nav bar
      (oscuros en light, claros en dark) + `navigationBarColor =
      colorScheme.background` (verificable en código, 1 sitio)
- [ ] `SystemBarIconStyle` re-aplica su override tras un cambio de tema
      (el `DisposableEffect` incluye el modo claro/oscuro en sus claves)
      → login/registro mantienen la nav bar petróleo aunque el sistema
      cambie a dark mientras están visibles
- [ ] En el Xiaomi físico: la nav bar de al menos 2 pantallas de `:home`
      coincide con el fondo de la app (no gris MIUI), en light y dark
- [ ] Login/registro siguen mostrando nav bar petróleo (regresión)
- [ ] Build compila + tests verdes

## Fuera de alcance

- Migrar `Theme.TradeJobPro` (XML) a DayNight/M3 — deuda mayor, otra spec
- `statusBarColor` por pantalla: MIUI ya la dibuja transparente correctamente
- Rediseño visual de las pantallas de `:home` (specs `ui-*` futuras)
- Cambiar fondos de pantalla: la nav bar toma `colorScheme.background`,
  suficiente porque `MainActivity` ya dibuja un `Surface` de ese color

## Diseño / notas técnicas

- Módulos afectados: `:libs:components` (`theme/Theme.kt`) — **un solo archivo**
- Componentes `Mito*` a reutilizar: `SystemBarIconStyle` ya existe
- Decisiones:
  - **Global en `TradeJobTheme`, no por pantalla** (recomendado): una línea
    `SystemBarIconStyle(darkIcons = !darkTheme,
    navigationBarColor = colorScheme.background)` dentro del tema cubre las
    11 pantallas actuales y todas las futuras sin que nadie tenga que
    acordarse. Alternativa descartada: repetir la llamada en cada Screen
    (~11 archivos, fácil de olvidar).
  - **Override por pantalla intacto**: login/registro llaman a
    `SystemBarIconStyle` como hijos del tema → sus valores ganan mientras
    están visibles y se restaura el default al salir (ya funciona así).
  - **Robustez ante cambio de tema**: añadir `isSystemInDarkTheme()` a las
    claves del `DisposableEffect` de `SystemBarIconStyle`, para que el
    override de login se re-aplique después del default del tema cuando el
    sistema conmuta light↔dark con la pantalla abierta.
  - `window.navigationBarColor` está deprecated en API 35+ pero es la única
    vía para la nav bar opaca de MIUI (documentado en SPEC-003); en stock
    es inocuo porque el fondo ya llega al borde.

## Mobile checklist

- [x] Navegación: sin cambios de rutas
- [x] Lifecycle: el efecto se re-evalúa con el cambio de tema del sistema
- [x] Edge cases: cambio light↔dark con pantalla abierta; OEM stock
      (nav bar transparente) sin regresión

## Plan de tests

- [ ] `gradlew.bat :app:assembleDebug` compila
- [ ] `gradlew.bat :home:testDebugUnitTest :login:testDebugUnitTest` verdes
- [ ] Dispositivo físico Xiaomi: login (nav petróleo) → sesión iniciada →
      dashboard + una lista (p. ej. clientes) con nav bar del color del
      fondo, en light y dark. **Requiere sesión real en el dispositivo:**
      el usuario inicia sesión una vez (credenciales no se automatizan)
- [ ] Screenshots en `EVIDENCE.md`

## Hallazgos fuera de scope

- **Bug crítico detectado durante la evidencia:** la app crashea al hacer
  login en el Xiaomi físico con BD `user_version=3`:
  `IllegalStateException: Migration didn't properly handle: job_photos`.
  La tabla `job_photos` de ese dispositivo carece de `isSynced`/`remoteUrl`
  y ninguna migración de la cadena las añade → validación Room falla.
  Bloquea la evidencia de esta spec. Resuelto en SPEC-006.
