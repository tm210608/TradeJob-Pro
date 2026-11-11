# TradeJob Pro - Progreso y Pendientes

## ✅ Completado (02 Noviembre 2026) — Adopción SDMD

**Metodología:** El proyecto adopta **SDMD (Spec-Driven Mobile Development)**.
Cada cambio = `specs/NNN/SPEC.md` + rama `feature/spec-NNN` + build + tests +
ejecución en dispositivo + `EVIDENCE.md`. Ver `AGENTS.md` y `specs/README.md`.

- [x] **SPEC-001 Estabilidad** (`feature/spec-001-estabilidad`): test roto corregido,
      módulo muerto `:libs:network` eliminado, `User`/`LoginResponse` reubicados a
      `login.domain`, `SessionManager.getUserId(): Long?` (sin sentinel -1L).
      17/17 tests verdes, app verificada en emulador.

- [x] **SPEC-002 Base M3 Expressive** (`feature/spec-002-ui-base` → main): BOM 2026.09,
      material3 1.5.0-beta02, compileSdk 37/targetSdk 36, `TradeJobTheme` petróleo+lima
      ("Técnico premium", marca fija light/dark) con `MaterialExpressiveTheme` +
      `MotionScheme.expressive()`. Shim `resources/Colors.kt` → paleta de marca.
      17/17 tests verdes, evidencia en dispositivo físico.

- [x] **SPEC-003 Login edge-to-edge** (`feature/spec-003-e2e-login` → main):
      `enableEdgeToEdge()` + `adjustResize`, fondo petróleo a sangre bajo status
      y nav bar en Login/NewUser, contenido con `safeDrawingPadding` +
      `imePadding`. `SystemBarIconStyle(darkIcons, navigationBarColor)` en
      `:libs:components` — iconos claros por pantalla + nav bar petróleo en OEMs
      con barra opaca (MIUI/HyperOS). Evidencia en emulador y Xiaomi rodin.
      17/17 tests verdes.

- [x] **SPEC-004 ui-login** (`feature/spec-004-ui-login` → main): Login/Registro
      migrados a `colorScheme` (cero imports del shim en `:login`); gradiente
      petróleo fijo en light/dark vía tokens `theme/Color.kt`; `MitoTextField` y
      `Primary`/`SecondaryButton` migrados (foco/botón dark correctos en toda la
      app); TopAppBar de registro transparente; `LoadingIndicator` M3 Expressive.
      17/17 tests verdes, evidencia emulador + Xiaomi `rodin` en light y dark.

- [x] **SPEC-006 Hotfix migración `job_photos`** (`feature/spec-006-migration-job-photos`
      → main): la app crasheaba al abrir BD en dispositivos con `user_version`
      antigua (detectado en el Xiaomi, v3) — `job_photos` carecía de `isSynced`/
      `remoteUrl` y ninguna migración de la cadena las cubría. `MIGRATION_6_7`
      reconstruye la tabla con el esquema exacto esperado copiando solo columnas
      presentes (idempotente, cubre v1–v6), versión 6→7, `schemas/7.json`
      exportado. Verificado en emulador con copia byte a byte de la BD real del
      Xiaomi **y en el propio Xiaomi físico**: `user_version 3→7`, datos
      preservados (fila sembrada visible en dashboard), cero crash.
      Tests verdes.

- [x] **SPEC-005 nav-bar-oem** (`feature/spec-005-nav-bar-oem` → main):
      color de nav bar global en `TradeJobTheme` (`colorScheme.background`)
      para MIUI y OEMs con barra opaca; `DisposableEffect` re-aplica el
      override por pantalla al conmutar light↔dark; login/registro conservan
      petróleo. Verificado en emulador **y en el Xiaomi `rodin` físico**:
      dashboard + Mis Clientes con nav bar `Slate50`/`Slate900` en light y
      dark — el gris MIUI desaparece.

### 🔭 Cola de specs detectadas
- `sync-worker-hilt`: `SyncWorker` no puede instanciarse
  (`NoSuchMethodException` en WorkManager — falta `HiltWorkerFactory`/
  `@HiltWorker`); el sync nunca ha corrido realmente. Detectado en SPEC-006.
- `db-migration-tests`: suite `MigrationTestHelper` con los schemas exportados
  (desde v7 ya quedan guardados en `libs/database/schemas/`).
- `ui-home`, `ui-clients`, `ui-jobs`, `ui-quotes`, `ui-invoices`: rediseño por flujo
  con componentes Expressive (ButtonGroup, FABMenu, LoadingIndicator, Carousel fotos).
- `sync-completa`: quotes/invoices no se sincronizan a Firestore (solo clients/jobs/photos).
- `i18n-domain`: strings hardcodeados en ViewModels.
- `login-tests`: el módulo `:login` no tiene tests unitarios.
- `journey-login`: UI test e2e del flujo de autenticación.
- `cleanup-deps`: `libs.material` (Views)/`appcompat` probablemente huérfanos;
  `kotlinOptions` → `compilerOptions` DSL; icons-extended → material-symbols.
- `ui-typography`: fuentes de marca vía Google Fonts downloadable.

## ✅ Completado (02 Junio 2026)

### Arquitectura y Calidad (Fase 2)
- [x] **Estudio de Mercado y Equipo:** Creado `EQUIPO_DESARROLLO.md` con roadmap profesional.
- [x] **Refactorización Core:** `MitoTextField` rediseñado para ser flexible y profesional.
- [x] **UX - Galería Real:** Integración de **Coil** para visualización de fotos en `JobFormScreen`.
- [x] **QA - Unit Testing:** Tests unitarios implementados (`SaveClientUseCaseTest`, `SaveJobUseCaseTest`).
- [x] **CRUD Trabajos Completo:** Eliminación de trabajos con confirmación y filtros por estado.
- [x] **Identidad Visual:** Logo e iconos corregidos en toda la app.

## ✅ Completado (14 Junio 2026)

### Conectividad y Pulido (Fase 3 & 4)
- [x] **Sincronización:** Worker de sincronización programado en `TradeJobApplication`.
- [x] **Gestión de Trabajos:** Campos de presupuesto, importe final y fecha añadidos al flujo.
- [x] **Dashboard:** Pantalla de inicio mejorada con estadísticas reales y trabajos recientes.
- [x] **Shield (QA):** Tests de UI iniciados para el módulo de login.
- [x] **Exportación Avanzada:** Generación de PDF profesional para reportes de trabajo.

## ✅ Completado (26 Junio 2026)

### Micro-Features y Calidad
- [x] **QA - Unit Testing:** Implementados tests unitarios para `JobListViewModel` verificado con `UnconfinedTestDispatcher`.
- [x] **Refactorización Componentes:** `MitoTextField` actualizado con API de `errorText` para validaciones más limpias.
- [x] **Sincronización:** Subida y descarga automática de fotos desde Firebase Storage en `SyncWorker`.

## ✅ Completado (05 Julio 2026) - Sesión Actual

### Profesionalización y Preparación para Lanzamiento
- [x] **Internacionalización (i18n):** Extracción de todos los strings de la UI a `strings.xml` en los módulos `:home` y `:login`.
- [x] **QA - Unit Testing Pro:** Suite de tests unitarios completa para `JobFormViewModel` (6 tests) y `ProfileViewModel` (4 tests).
- [x] **Validación Avanzada:** Implementada validación en tiempo real para campos numéricos (presupuesto/importes) en `JobFormViewModel`.
- [x] **Seguridad y Optimización:** Configuración de R8/ProGuard activa en `:app` y creación de `proguard-rules.pro`.
- [x] **Estabilidad UI:** Corregidos errores de recursos, deprecaciones de iconos e importaciones en `HomeScreen.kt`.

## 🚀 Próximos Pasos (Estrategia de Micro-Features)

> [!IMPORTANT]
> **Nueva Política de Desarrollo:** Cada mejora debe realizarse en una rama de git independiente (`feature/nombre-mejora`), ser pequeña y autocontenida, e incluir (siempre que sea posible) sus respectivos tests. No se permiten cambios grandes directamente en `main` o `develop`.

### 🧪 QA & Testing (Prioridad Actual)
1. **Módulo :login:** Ampliar cobertura de tests de UI para `LoginScreen`.
2. **Shield:** Añadir tests de migración de base de datos Room.

### 🛠️ Refactorizaciones y Mejoras UI
1. **Architect (PENDIENTE):** Implementar el "Professional Validation Framework" en `:libs:common` para estandarizar errores en toda la app.
2. **Sync:** Implementar descarga de imágenes desde Cloud Storage si faltan localmente.

## 🚀 Flujo Actual de Navegación

```
LoginScreen → HomeScreen → [Menú] → ClientListScreen → ClientFormScreen
                ↓
         [Cerrar sesión] → LoginScreen
```

## 📝 Notas Técnicas

- Gradle 8.13 configurado
- Java 17 / Kotlin 1.9.24
- Compose BOM 2024.06.00
- Tema: MaterialComponents (Transicionando a Material3)
- Package: `com.tradejob.pro`

---
**Última compilación:** EXITOSA (05/07/2026)
**Estado:** Production-Ready Candidate
