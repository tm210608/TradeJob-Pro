# AGENTS.md — TradeJob Pro

Guía para agentes (y humanos) que trabajan en este repo bajo **SDMD (Spec-Driven Mobile Development)**.

## Empezar una sesión

1. Lee `.devin/STATE.md` antes de explorar: estado breve y siguiente paso.
2. Consulta `PROGRESS.md` para el historial/roadmap y la SPEC activa en `specs/`.
3. Flujo: `/spec` (preguntas y aprobación) → `/implement-spec` → `/verify` → `/handoff`.
   Skills del proyecto en `.devin/skills/`; configuración nueva siempre en `.devin/`.
   En sesiones con varios proyectos comprueba la ruta de la skill: debe pertenecer a
   TradeJob-Pro, no a TravelDiaryBis/SocialChef. Si la resolución conserva una skill ajena,
   lee el SKILL.md local y abre una sesión nueva para recargar las skills.
4. Al terminar, actualiza `.devin/STATE.md` en menos de 40 líneas. No declares cerrado
   lo que siga pendiente de pruebas, dispositivo o evidencia. No commit/merge/push sin petición.

## Qué es esta app

Gestión de campo para autónomos técnicos (España/LATAM): clientes, trabajos con evidencia
fotográfica antes/después, presupuestos, facturas con firma digital y PDF, sincronización
opcional con Firebase. Offline-first por defecto.

## Arquitectura

Multi-módulo Gradle. Clean Architecture + MVVM. Compose, Hilt, Room, WorkManager, Firebase
(Auth, Firestore, Storage, Crashlytics, Analytics).

| Módulo | Responsabilidad |
|---|---|
| `:app` | Application, MainActivity, wiring de módulos |
| `:login` | Auth (Firebase + fallback local Room/BCrypt), registro |
| `:home` | Features: dashboard, clients, jobs+photos, quotes, invoices, profile |
| `:libs:database` | Room (entities, DAOs, TradeJobDatabase), SyncWorker |
| `:libs:core` | `Screen` interface, SessionManager |
| `:libs:common` | `Result`, validation framework, AnalyticsHelper, Verifactu (stub) |
| `:libs:components` | Design system `Mito*` (TextField, Buttons, SignaturePad, recursos) |
| `:libs:navigation` | `AppNavigation` — NavHost genérico sobre `List<Screen>` |

### Reglas de navegación

- Cada destino es una clase que implementa `Screen` (route + arguments + Content).
- Las rutas con parámetros usan placeholders: `"job_form/{clientId}/{jobId}"`.
- Los ViewModels leen args vía `SavedStateHandle`.

## Convenciones

- **Result**: una sola clase `com.tradejob.pro.common.usecase.Result` (Success/Error/Loading).
- **Validación**: `libs:common` validation framework (`RequiredValidator`, `NumberValidator`,
  `ValidationResult`). No inventar validadores ad hoc.
- **Strings de UI**: siempre en `strings.xml` del módulo (i18n activo, ES base).
- **DI**: Hilt. Módulos `@InstallIn(SingletonComponent::class)` en cada feature (`di/`).
- **Ramas**: `feature/<nombre>` por cada cambio. Nada grande directo a `main`.
- **Componentes UI**: reutilizar `Mito*` de `:libs:components` antes de crear nuevos.

## Comandos

Verificador SDMD (Windows, Node.js y Java 21):

- `node .devin/verify.cjs`: compila app/APK de pruebas y verifica tests; salida breve.
- `node .devin/verify.cjs --device`: lo anterior + instala conservando datos y abre
  en móvil USB/Wi-Fi; sin móvil crea/arranca el AVD exclusivo `TradeJob_Test`.
- `node .devin/verify.cjs --sync`: pruebas Firebase aisladas, siempre en emulador.
- `node .devin/android/device.cjs`: selecciona móvil o prepara el emulador, sin instalar.
- `node .devin/android/device.cjs --emulator-only`: prepara solo el emulador.
- `node --test .devin/android/device.test.cjs`: tests de selección segura, sin dispositivos.
- Logs/AVD locales ignorados por Git en `.devin/android/artifacts/` y `.devin/android/avd/`.
- `ANDROID_HOME` permite elegir SDK; no leer `local.properties` ni archivos de secretos.
- Usa imagen local Android 36.1 Google Play x86_64; si falta, pide autorización para descargar.
- Con varios móviles/conexiones se exige `ANDROID_SERIAL`; nunca escoger el primero a ciegas.
  Todos los comandos ADB dirigidos a un dispositivo deben llevar `-s <serial>`.
- Explica y pide permiso antes de instalar en el móvil salvo autorización explícita vigente.
  Nunca desinstales ni borres datos para resolver fallos. Abrir la app no verifica el flujo.
- No uses emuladores ajenos automáticamente. No cambies ni detengas los de otros proyectos.
- No pegues logs completos: resume y consulta el archivo de log ante fallos.
- Si una instrumentación falla al arrancar con ANR, revisa logs y trazas antes de
  atribuirlo al código. En TradeJob_Test se observó Android verificando/descomprimiendo
  DEX antes del test. Precompilar app y APK de tests con
  `adb -s <serial> shell cmd package compile -m speed -f <paquete>` permitió ejecutar
  `WorkManagerInitializationTest` (paquetes `com.tradejob.pro.app` y
  `com.tradejob.pro.app.test`). Esto prepara el emulador: no valida el arranque frío
  sin precompilar ni permite ignorar ANR o dar una instrumentación fallida por verde.

Comandos Gradle originales:

```bash
# Build
gradlew.bat :app:assembleDebug

# Unit tests
gradlew.bat :home:testDebugUnitTest :login:testDebugUnitTest

# Dispositivo
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n com.tradejob.pro.app/.MainActivity
```

### Pruebas locales de sincronización

- Herramientas aisladas en `.devin/firebase/`; Node.js y Java 21 deben estar en `PATH`.
- Dependencias: `npm --prefix .devin/firebase ci` (requiere autorización para descargar).
- Con un emulador Android arrancado: `npm --prefix .devin/firebase run test:sync`.
- Usa `demo-tradejob-sync`, sin login de Firebase, servicios ligados a `127.0.0.1`,
  APK de tests de `:libs:database` y Room en memoria. No usa la app ni datos reales.
- `ANDROID_SERIAL` permite elegir emulador; el ejecutor rechaza dispositivos físicos.
- El ejecutor crea y retira sus propios puentes ADB; guarda el resultado en
  `.devin/firebase/artifacts/android-sync-test.log` (ignorado por Git).
- Los tests requieren `-e firebaseEmulators true`; sin esa opción se omiten.
- El SDK rechaza `getReferenceFromUrl()` con la URL HTTP devuelta por Storage local.
  La prueba verifica esa URL durante la subida y usa una referencia `gs://` al mismo
  objeto para comprobar la recuperación. No equivale a validar ese round trip HTTP.

## Definición de "done" (SDMD)

Una feature/fix NO está terminada hasta que:

1. **SPEC** — existe `specs/NNN-<nombre>/SPEC.md` con criterios de aceptación testeables.
2. **BUILD** — `gradlew.bat :app:assembleDebug` compila sin errores.
3. **TESTS** — unit tests verdes + tests nuevos para lo añadido (cuando aplique).
4. **DEVICE** — el flujo ejecutado en un dispositivo/emulador real.
5. **EVIDENCE** — `specs/NNN-<nombre>/EVIDENCE.md` con build log, tests y screenshots.

## Proceso SDMD

```
PETICIÓN → PREGUNTAS → SPEC → AGENTE → BUILD → TESTS → DEVICE → EVIDENCIA
```

- Lee la spec antes de tocar código. Respeta "Fuera de alcance".
- Diff acotado: solo los archivos que la spec requiere.
- Si descubres un bug fuera de scope, documéntalo en la spec (sección "Hallazgos")
  o crea una spec nueva — no lo arregles de tapadillo.

## Comunicación con el usuario

El usuario tiene **poco conocimiento técnico**. Al trabajar con él:

- Al explicar una spec (o cualquier decisión), usa lenguaje llano: qué verá el
  usuario en la app y por qué importa, antes que detalles de implementación.
- En preguntas/cuestionarios con opciones, incluye siempre una opción marcada
  como **recomendada** y di por qué en una frase.
- Todo paso que requiera su aprobación (aprobar una spec, mergear, instalar
  algo, conectar el dispositivo) se pide con una explicación clara de qué se
  va a hacer y qué cambia para él.

## Documentos de referencia

- `PROGRESS.md` — historial y roadmap (fuente de verdad de estado).
- `EQUIPO_DESARROLLO.md` — estudio de mercado y roles.
- `INFORME_REVISION.md` — auditoría de mayo 2026 (la mayoría ya resuelta; ver specs/001).
- `specs/README.md` — convenciones del sistema de specs.
