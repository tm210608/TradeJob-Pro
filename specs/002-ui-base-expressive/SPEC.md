# SPEC-002: Base Material 3 Expressive + TradeJobTheme "Técnico premium"

**Estado:** verificada
**Rama:** `feature/spec-002-ui-base`
**Fecha:** 2026-11-02

## Qué construimos

La cimiente del rediseño: upgrade de dependencias Compose a versiones 2026,
`material3:1.4.0` (APIs Expressive estables), `compileSdk/targetSdk 36`, y el
primer `TradeJobTheme` real — la app hoy usa el `MaterialTheme` por defecto
(`Colors.kt` existe pero nadie lo aplica).

### Contexto / por qué

El usuario quiere un cambio radical a Material 3 Expressive. Antes de rediseñar
pantallas (specs ui-* siguientes), se necesita: dependencias actuales, tema de
marca propio y tokens de diseño. Sin esto, cada spec de pantalla arrastraría
deuda de versiones.

### Dirección visual (decidida con el usuario)

**"Técnico premium"**: petróleo profundo + lima eléctrico. Sensación de SaaS
financiero moderno — encaja con presupuestos/facturas — y lejos del azul
Material genérico. **Marca propia fija**: light y dark con paleta de marca;
sin dynamic color.

- Petróleo: `#0B3E42` (primary light) / surfaces petrol-tinted
- Lima eléctrico: `#B8E62E`-family como acento quirúrgico (CTAs, estados OK)
- Dark: petrol casi-negro `#0A191B` + primary aclarado + lima pleno

## Criterios de aceptación

- [ ] BOM Compose actualizado a 2026.09.00; `material3` unificado a 1.5.0-beta02
      en todos los módulos (sin pins divergentes 1.2.1/1.3.1). Nota: 1.5.0-beta es
      la línea compilada contra compose 1.12 que exige el BOM; contiene todas las
      APIs Expressive. Al salir 1.5.0 estable se sube el ref una vez.
- [ ] `compileSdk = 37` en todos los módulos (compose 1.12 lo exige);
      `targetSdk = 36` en `:app` (comportamiento runtime conservador)
- [ ] `TradeJobTheme` en `:libs:components` con `MaterialExpressiveTheme`,
      `MotionScheme.expressive()`, colorSchemes light+dark de marca
- [ ] `MainActivity` y `LoginScreen` dejan de usar `MaterialTheme` a pelo
- [ ] `assembleDebug` compila tras el salto de versiones (fixes de APIs
      deprecadas incluidos en esta spec, limitados a lo necesario para compilar)
- [ ] Unit tests siguen verdes
- [ ] Evidencia visual en emulador: login renderiza con la nueva paleta
      (petróleo/lima visible, no azul por defecto)

## Fuera de alcance

- Rediseño de pantallas concretas (specs `ui-*` posteriores: home, clients,
  jobs, quotes, invoices, login)
- Componentes Expressive nuevos en pantallas (ButtonGroup, FABMenu,
  LoadingIndicator...) — llegan con cada spec de pantalla
- Tipografía con fuentes descargables (Google Fonts) — spec posterior dedicada
- Eliminación de `libs.material` (Views) y `appcompat` si resultan huérfanos
- Migración `kotlinOptions` → `compilerOptions` DSL (warning AGP 9)

## Diseño / notas técnicas

- Tema vive en `com.tradejob.pro.components.theme` (`Color.kt`, `Theme.kt`,
  `Type.kt` si aplica). `:libs:components` ya es dependencia de todas las UIs.
- `MaterialExpressiveTheme` requiere `@OptIn(ExperimentalMaterial3ExpressiveApi::class)`.
- Posibles roturas esperadas al saltar ui 1.6→1.9/m3 1.2→1.4: `Divider` →
  `HorizontalDivider`, iconos `ArrowBack`/etc. auto-mirrored, APIs de TopAppBar,
  `MenuAnchorType`, cambios en `SheetValue`/`ModalBottomSheet`, `CardDefaults`.
  Se arreglan solo hasta compilar; el pulido va en specs de pantalla.

## Mobile checklist

- [x] Dark mode: esquema dark de marca desde el día 1
- [x] Edge cases: previews de Compose siguen funcionando tras el cambio de tema

## Plan de tests

- [ ] `gradlew.bat :app:assembleDebug` compila
- [ ] `gradlew.bat :home:testDebugUnitTest :login:testDebugUnitTest` verdes
- [ ] Emulador: lanzar app → screenshot login con paleta nueva en `EVIDENCE.md`

## Hallazgos fuera de scope
