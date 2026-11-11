# AGENTS.md — TradeJob Pro

Guía para agentes (y humanos) que trabajan en este repo bajo **SDMD (Spec-Driven Mobile Development)**.

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

```bash
# Build
gradlew.bat :app:assembleDebug

# Unit tests
gradlew.bat :home:testDebugUnitTest :login:testDebugUnitTest

# Dispositivo
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n com.tradejob.pro.app/.MainActivity
```

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
