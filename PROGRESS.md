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

### SPEC-007 — Corrección de arranque de sincronización (11 noviembre 2026)

- Rama: `feature/spec-007-sync-worker-init` → **mergeada a `main`** el 12-11
  (junto a `feature/sdmd-project-setup`) y publicada en `origin/main`.
- Causa reproducida en emulador: WorkManager usaba `DefaultWorkerFactory`, aunque
  `TradeJobApplication` ya proporcionaba `HiltWorkerFactory` y `SyncWorker` ya tenía
  `@HiltWorker`. El inicializador automático ignoraba la configuración de la app.
- Corrección: eliminar únicamente `WorkManagerInitializer` del manifiesto fusionado;
  conservar AndroidX Startup y sus otros inicializadores.
- Regresión: `WorkManagerInitializationTest` falla antes del cambio y pasa después
  al comprobar la identidad de la factoría realmente usada por WorkManager.
- Verificación: `:app:assembleDebug`, `:app:assembleDebugAndroidTest` y 17 tests de
  `:home:testDebugUnitTest` correctos. Prueba instrumentada correcta en `emulator-5554`
  sin conexión; no se han probado subidas a Firebase ni modificado el móvil físico.
  `:login:testDebugUnitTest` devuelve `NO-SOURCE` (no hay tests unitarios).
- Apertura manual: `am start -W` agotó su espera; después se confirmó `MainActivity`
  en estado `Resumed`, sin errores en los tags `AndroidRuntime`/`WM-WorkerFactory`
  consultados. App detenida antes de restaurar Wi-Fi y datos del emulador.
- Pruebas locales añadidas en `:libs:database`, ejecutadas en un APK independiente
  con Room en memoria y Firebase `demo-tradejob-sync` (Auth, Firestore, Storage).
  Ninguna cuenta, base local ni proyecto real de la app se usa para estas pruebas.
- `npm --prefix .devin/firebase run test:sync` verificado: arranca los tres servicios,
  construye e instala el APK de tests solo en emulador, pasa 2/2 tests y apaga Firebase.
  Log local: `.devin/firebase/artifacts/android-sync-test.log` (ignorado por Git).
- Verificado: subida de cliente/trabajo/foto, flags locales y documentos remotos,
  bytes del archivo, recuperación de foto con referencia `gs://`, y cliente pendiente
  si no hay sesión. Se invoca `SyncWorker.doWork()` con DAOs temporales: no es una
  prueba del planificador periódico real; la factoría de la app se verifica aparte.
- Limitación observada: el SDK rechaza `getReferenceFromUrl()` con la URL HTTP de
  Storage local. El test comprueba la URL generada al subir y sustituye únicamente
  la referencia del fixture por `gs://` al mismo objeto antes de probar recuperación.
  El round trip directo con esa URL HTTP local NO está validado ni corregido.
- Hallazgo fuera de alcance: una recuperación fallida se registra en logs pero
  `SyncWorker` devuelve éxito; no se ha cambiado ese comportamiento en esta rama.
- Herramienta local fijada: `firebase-tools@15.34.2`; `npm audit` informa 14 avisos
  moderados transitivos, sin altos/críticos. No se aplican downgrades ni overrides.
  Servicios limitados a loopback; no se relajan las reglas de ningún proyecto real.
- Cierre documental completado el 12-11: `specs/007-sync-worker-init/SPEC.md` y
  `EVIDENCE.md` creados. Rama sin merge ni push.
- Revisión del 12 de noviembre de 2026 sobre `71821c1`: comando
  `gradlew.bat :app:assembleDebug :app:assembleDebugAndroidTest :home:testDebugUnitTest :login:testDebugUnitTest`
  correcto (`BUILD SUCCESSFUL`, 242 tareas: 37 ejecutadas, 205 actualizadas).
  Tests de home `UP-TO-DATE`: no se han vuelto a ejecutar; login `NO-SOURCE`.
  El log local conservado de Firebase confirma `OK (2 tests)` de la sesión anterior,
  no una ejecución nueva. No se ha repetido la instrumentación ni instalado la app.
- Solo aparece conectado `emulator-5556`, identificado en el estado de SocialChef
  como su emulador de pruebas. No se usa sin confirmar disponibilidad; no se toca
  ningún móvil físico ni se modifica la configuración de los otros proyectos.
- Comparación SDMD realizada en lectura: SocialChef y TravelDiaryBis tienen
  `docs/STATE.md`, flujo de trabajo, skills en `.devin/skills/` y `scripts/verify.sh`.
  Propuesta pendiente de aprobación: resumen breve de sesión y skills adaptadas,
  conservando `specs/` y el requisito de dispositivo de TradeJob. No copiar scripts
  sin adaptar módulos, selección explícita de dispositivo y comprobación del flujo.

### Revalidación de SPEC-007 (12 noviembre 2026, continuación)

- `:home:testDebugUnitTest --rerun` ejecutó de nuevo los 17 tests: 0 errores,
  0 fallos y 0 omitidos en los cinco XML de resultados. Login sigue `NO-SOURCE`.
- Gradle informó `BUILD SUCCESSFUL` (242 tareas, 1 ejecutada, 241 actualizadas)
  al verificar también app y APK de pruebas. Log local:
  `.devin/android/artifacts/spec-007-verification.log`. La envoltura PowerShell
  devolvió código 1; una ejecución independiente de `node .devin/verify.cjs`
  terminó con código 0 y confirmó la compilación. Esa segunda ejecución reutilizó tests.
- AVD propio reiniciado sin ventana tras cerrarse un arranque anterior. Varios intentos
  instrumentados devolvieron `Process crashed`: Android registró ANR de arranque.
  Las trazas del hilo principal mostraron descompresión/verificación DEX y elevada
  presión de CPU, antes de alcanzar la prueba. No se cambió código de producción.
- Reintentar con GPU host, 4 CPU y 4096 MB no bastó por sí solo. Preparar app y
  APK de tests con `cmd package compile -m speed -f` devolvió `Success` en ambos.
- Después, `WorkManagerInitializationTest` pasó: `OK (1 test)` en `emulator-5560`,
  comprobando la identidad de la factoría usada realmente por WorkManager.
  Log: `.devin/android/artifacts/spec-007-workmanager-precompiled.log`.
  Los logs previos fallidos se conservan; no equivalen a tests aprobados.
- Apertura posterior con `am start -W`: `Status: ok`. Primera captura mostró
  aviso de ANR de System UI; tras pulsar Esperar se comprobó visualmente el login
  sin aviso. Capturas locales `spec-007-login.png` y `spec-007-login-after-wait.png`
  en `.devin/android/artifacts/`. Esto no demuestra un flujo completo de sincronización.
- Los 2 tests Firebase aislados correctos de esta sesión constan en el apartado de
  configuración; no se repiten en esta continuación. Sin móvil físico, datos borrados,
  modificación de SocialChef, commits, merge ni push.
- Cierre formal realizado después de esta revalidación: SPEC/EVIDENCE en
  `specs/007-sync-worker-init/` (commit `c85615b`). El arranque frío sin
  precompilar se verificó después: test `OK` y app COLD en 6,1 s en
  `emulator-5560`. El planificador periódico se verificó después: `SyncWorker`
  ENQUEUED en el JobScheduler real con constraint de red y ventana ~1 h; el run
  forzado llega al proceso y WorkManager lo difiere correctamente. El round
  trip HTTP de Storage también quedó verificado: el test reescribe la URL del
  emulador al formato `firebasestorage.googleapis.com` y `SyncWorker` la
  parsea y descarga byte a byte (2/2 tests). Ya no quedan verificaciones
  pendientes de SPEC-007 salvo la sincronización real contra Firebase
  producción, que requiere proyecto real y no se ha hecho.

### Configuración SDMD de sesiones y dispositivo propio (12 noviembre 2026)

- Rama de configuración: `feature/sdmd-project-setup`, creada desde `71821c1`;
  sin commit nuevo, merge ni push. Conserva el trabajo previo de SPEC-007.
- Estado breve en `.devin/STATE.md`; skills `spec`, `implement-spec`, `verify` y
  `handoff` en `.devin/skills/`. No se trasladan las specs existentes de `specs/`.
- Verificador `node .devin/verify.cjs` probado: compila app y APK de tests;
  home reutilizado (`UP-TO-DATE`), login sin tests (`NO-SOURCE`). Sin dependencias nuevas.
- Selector con 13 tests Node correctos: prioridad móvil USB/Wi-Fi, selección explícita
  con varias conexiones y rechazo de móviles offline/no autorizados. Sin prueba física.
- AVD exclusivo `TradeJob_Test` creado con la imagen Android 36.1 local. Datos y logs
  ignorados por Git bajo `.devin/android/`. No se copia información de otros AVD.
- Arranque confirmado en `emulator-5560`, reutilización del AVD verificada, instalación
  conservadora y apertura de TradeJob correctas; `MainActivity` en estado Resumed.
  Primeros intentos terminaron sin completar; reintento con renderizado por software
  correcto, sin afirmar causa raíz. No se modifica el emulador de SocialChef (`5556`).
- `node .devin/verify.cjs --sync` ejecutado en `emulator-5560`: 2/2 tests nuevos
  correctos con Firebase aislado; log `.devin/firebase/artifacts/android-sync-test.log`.
  Se mantienen las limitaciones de cobertura descritas en SPEC-007 arriba.
- Cierre de sesión autorizado por el usuario: configuración y registros guardados en
  commit local de `feature/sdmd-project-setup` (consultar `git log -1`), sin merge ni push.
  Los logs, capturas y datos del AVD permanecen locales, ignorados por Git.
- Cierre documental SPEC-007 realizado: `specs/007-sync-worker-init/` con
  SPEC y EVIDENCE honestos. Pendiente: verificación del flujo completo
  (arranque frío, planificador, round trip HTTP Storage). Recargar sesión si
  se resuelve una skill homónima de otro proyecto en lugar de la local.

### SPEC-008 — Tests unitarios de :login (12 noviembre 2026)

- Rama: `feature/spec-008-login-tests` (sin merge).
- 22 tests nuevos verdes (antes `NO-SOURCE`): LoginViewModel (8),
  RegisterViewModel (9), LoginUseCase (2), RegisterUserUseCase (3).
  Convenciones de `:home` (JUnit4 + mockk + coroutines-test).
- Refactor mínimo aprobado: `EmailValidator.isValid` (companion en
  `:libs:common`, regex ya existente) sustituye `Patterns.EMAIL_ADDRESS`
  en ambos ViewModels — misma regex en app y tests, cero cambio visible.
- Verificación: `verify.cjs` OK; app lanzada COLD en `emulator-5560`, login
  idéntico (captura local `spec-008-login.png`).
- Hallazgo documentado (no corregido): registro admite password de 6
  caracteres pero el login exige >6 — un usuario podría registrarse con una
  contraseña que el formulario de login no le dejaría escribir. Spec futura.
- SPEC/EVIDENCE en `specs/008-login-tests/`. Sin merge ni push.

### SPEC-009 — Mínimo de contraseña unificado (12 noviembre 2026)

- Rama: `feature/spec-009-password-min-length` (sin merge).
- Bug real corregido: registro aceptaba 6 caracteres pero login exigía >6 —
  un usuario registrado con 6 no podía entrar. `isValidPassword`: `>6`→`>=6`,
  alineado con Firebase (mínimo 6). Rescata cuentas ya creadas.
- 24 tests de login verdes (casos límite "123456"/"12345" en ambos lados);
  verify.cjs OK; login idéntico en emulador.

### SPEC-010 — Login cálido "El taller te da los buenos días" (12 noviembre 2026)

- Rama: `feature/spec-010-ui-login-calido` (sin merge).
- El login dejaba de lado el gradiente petróleo total en light: ahora fondo
  hueso `#FAF7F0` (petróleo solo en acentos); en dark se conserva petróleo.
- Saludo por hora (mañana/tarde/noche) en lima, tagline "Tu taller en el
  bolsillo" y `MitoSignatureDivider` (trazo de firma de marca) bajo el título.
- Registro con el mismo tratamiento; TopAppBar adaptada por tema.
- 27 tests login (incluye `GreetingTest`), 17 home, verify.cjs OK.
- Capturas light/dark de login y registro en `emulator-5560`.

### SPEC-011 — Dashboard cálido "El parte de la semana" (13 noviembre 2026)

- Rama: `feature/spec-011-ui-home` (sin merge).
- **Iteración de diseño**: v1 (saludo + número grande + chips + tarjetas)
  rechazada por el usuario; v2 aprobada — el dashboard se convierte en el
  **parte de trabajo de la semana**: fecha real, saludo con nombre, una sola
  tarjeta-parte con borde (rango lun–dom, "N trabajos esta semana", líneas
  ✓ lima de terminado/cobrado, divisor troquelado, pie de totales y la
  firma `MitoSignatureDivider` cerrando el parte). Trabajos recientes como
  líneas del parte con punto de estado (JobCard intacto, solo fuera del
  dashboard).
- Regla semanal: `scheduledAt ?: createdAt` en lun–dom local; "terminados"
  cuenta `COMPLETED` + `INVOICED`.
- Estado vacío nuevo: "Tu taller empieza aquí" + CTA a `client_form/0` cuando
  no hay clientes ni trabajos.
- `greetingForHour` + strings `greeting_*` trasladados de `:login` a
  `:libs:common`; login sin regresión visual.
- Migrada la pantalla a `colorScheme`/tokens (sin colores hardcodeados ni
  imports del shim de colores viejo en `HomeScreen`).
- Corrección incluida: "terminados" cuenta `COMPLETED` + `INVOICED` (los
  facturados ya no desaparecen del total). Fix de runtime: `:app` duplicaba
  `home_stat_completed` y pisaba el nuevo texto — alineado.
- Tests: 25 verdes en `:home` (8 nuevos de cómputo semanal con reloj fijo),
  27 en `:login`. `verify.cjs` OK dos veces.
- Verificado en `TradeJob_Test` (emulator-5560): login light/dark, dashboard
  vacío dark, dashboard con datos light/dark, celebración, menú, navegación
  a clientes y a formulario de cliente. Evidencia en `specs/011-ui-home/`.

### SPEC-012 — Trabajos cálidos "Los partes del cliente" (13 noviembre 2026)

- Rama: `feature/spec-012-ui-jobs` (sin merge).
- `JobCard` rediseñada (usada en `ClientDetailScreen` y `JobListScreen`):
  borde sutil, `BoneCard`/`surface`, estados con colores del parte
  (pendiente neutro — ya no rojo de error —, curso petrol, terminado lima,
  facturado cyan), **fecha programada + importe** ("10 nov · 890 €",
  `finalAmount` ?? `budgetAmount`) y **marca ámbar "Urgente"** solo en
  `HIGH`/`URGENT`.
- `JobListScreen` (piel completa + título con nombre de cliente + estado
  vacío con CTA) quedó lista, pero **`job_list` es código muerto**: nada
  navega a esa ruta; la lista real es la sección de `ClientDetailScreen`.
  Hallazgo registrado en la spec — candidata a fusión/borrado.
- Ficha de trabajo con la piel (hueso/petróleo, "Detalles"), badges de
  foto "Antes"/"Después"/"Progreso" con colores de tema (el caso PROGRESS
  mostraba mal "Después" — corregido).
- Strings del flujo a `strings.xml` (filtros, borrado, vacíos, badges,
  placeholders); `ClientDetailScreen` externalizados sus textos de la
  sección trabajos.
- Tests: 32 verdes en `:home` (7 nuevos `JobFormattersTest`; stub
  `getClientById` añadido a `JobListViewModelTest`), `:login` verdes,
  `verify.cjs` OK.
- Verificado en `TradeJob_Test` con ventana: tarjetas light/dark con
  urgente/fecha/importe, ficha, estado vacío con CTA a `job_form/0`.

### SPEC-013 — Clientes cálidos: ficha con contacto directo (13 noviembre 2026)

- Rama: `feature/spec-013-ui-clients` (sin merge).
- Dirección aprobada desde exploración Stitch (variante "zen",
  `stitch-minimal.png`): ficha limpia + contacto directo, sin mover
  la lista de trabajos (decisión del usuario: embebida).
- `ClientDetailScreen` rediseñada: fondo hueso/petróleo, TopAppBar con
  el **nombre del cliente**, tarjeta `BoneCard` con contacto y notas,
  **fila de botones Llamar/Email/Ruta** (intents `tel:`/`mailto:`/`geo:`
  — Gmail/Maps/Dialer abiertos en emulador; botón oculto si falta el
  dato), **línea resumen** "● 1 en curso · ● 1 pendiente · ● 2
  completados" (INVOICED cuenta terminado, como el dashboard) y tarjeta
  **"Último trabajo"** (createdAt mayor). Lista embebida + filtros
  conservados.
- `ClientListScreen` con la piel: fondo, tarjetas `BoneCard`, nombre
  petróleo, FAB petróleo, contentDescriptions a recursos.
- `ClientFormatters.kt` nuevo: `countJobsByStatus` + `lastJob` puros;
  `ClientDetailViewModel` expone `allJobs` sin filtrar.
- Strings nuevos en `:home` y espejo en `:app` (plurals ES incluidos).
- Tests: 4 nuevos `ClientFormattersTest`; `verify.cjs` verde.
- Verificado en `TradeJob_Test2` light+dark: intents de llamada/mapas/
  email reales, cliente sin datos (solo Llamar), estado vacío + CTA.
- Fix durante verificación: "Llamar" partía en dos líneas → `maxLines=1`.

### 🔭 Cola de specs detectadas
- `sync-worker-hilt`: arranque corregido en la rama de SPEC-007; cierre pendiente
  según el apartado anterior. Detectado en SPEC-006.
- `db-migration-tests`: suite `MigrationTestHelper` con los schemas exportados
  (desde v7 ya quedan guardados en `libs/database/schemas/`).
- `ui-home`, `ui-clients`, `ui-jobs`, `ui-quotes`, `ui-invoices`: rediseño por flujo
  con componentes Expressive (ButtonGroup, FABMenu, LoadingIndicator, Carousel fotos).
- `sync-completa`: quotes/invoices no se sincronizan a Firestore (solo clients/jobs/photos).
- `i18n-domain`: strings hardcodeados en ViewModels.
- `voice-parte`: rellenar la ficha de trabajo dictando por voz o escribiendo
  (el trabajador elige). Idea del usuario 13-11: `SpeechRecognizer` nativo
  permite parsear "cambio de enchufes en cocina, 90 euros, mañana a las 10"
  a título/importe/fecha sin dependencias nuevas.
- `cleanup-job-list-route`: `job_list/{clientId}` no recibe navegación
  (la lista real es la sección de `ClientDetailScreen`); `JobListScreen`
  es código muerto navegable — borrarla o hacerla la ruta oficial.
- `login-repo-tests`: `LoginRepositoryImpl`/`FirebaseAuthDataSource`/
  `UserDataSourceImpl` sin tests propios (quedaron mockeados en SPEC-008).
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
