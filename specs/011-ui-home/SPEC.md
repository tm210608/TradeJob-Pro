# SPEC-011: Dashboard cálido — "Tu taller, de un vistazo"

**Estado:** verificada (v2 "parte de trabajo" aprobada por el usuario el 13-11; v1 rechazada y reemplazada)
**Rama:** `feature/spec-011-ui-home`
**Fecha:** 2026-11-13

## Qué construimos

Rediseño del dashboard (`HomeScreen`) como **"parte de trabajo del taller"**:
la pantalla deja de ser una rejilla de tarjetas genéricas y se convierte en
el parte de la jornada del autónomo. Dirección aprobada por el usuario el
13-11 tras rechazar la v1 ("se ve vacío / los colores no dicen nada"):

- Fondo **blanco hueso** (`BoneLight`) en light y petróleo cálido en dark, a
  nivel de pantalla — sin cambiar el `colorScheme` global.
- **Cabecera de parte**: fecha real en eyebrow ("VIERNES 13 DE NOVIEMBRE") y
  saludo con nombre ("Buenas tardes, Carlos") reutilizando `greetingForHour`
  (trasladado a `:libs:common`).
- **Una sola tarjeta-parte** ("Parte de la semana", con rango lun–dom):
  línea destacada "N trabajos esta semana", líneas ✓ lima para trabajo
  terminado / factura cobrada, divisor troquelado, pie con totales
  (pendientes/en curso/terminados) y `MitoSignatureDivider` al final —
  la firma *cierra el parte*, no decora.
- **Trabajos recientes como líneas del parte** (filas compactas
  título + estado), no tarjetas grandes. `JobCard` no se toca; deja de
  usarse solo en esta pantalla.
- **Estado vacío**: tarjeta "Tu taller empieza aquí" con CTA a
  `client_form/0` cuando no hay clientes ni trabajos.
- La pantalla migra a `colorScheme` y tokens de `theme/Color.kt` (cero
  colores hardcodeados nuevos).

### Contexto / por qué

Tras SPEC-010 el login ya se siente "de taller"; al entrar, el dashboard
sigue siendo un panel gris corporativo que rompe la experiencia. Es la
pantalla que el autónomo abre cada mañana: debe saludarle por su nombre,
decirle cómo va la semana y celebrar lo cobrado.

## Criterios de aceptación

- [x] AC-01: Dashboard light muestra fondo hueso, fecha real, saludo por
      hora con el nombre del usuario y `MitoSignatureDivider` cerrando el
      parte; captura en dispositivo.
- [x] AC-02: Dashboard dark muestra superficie petróleo cálida con contraste
      correcto; captura en dispositivo.
- [x] AC-03: Contador semanal "N trabajos esta semana" cuenta trabajos con
      `(scheduledAt ?: createdAt)` dentro de la semana en curso (lunes–domingo,
      hora local); totales de pendientes, en curso y terminados
      (`COMPLETED` + `INVOICED`) visibles debajo.
- [x] AC-04: Las líneas ✓ lima del parte aparecen solo si
      `completedThisWeek > 0` o `paidInvoicesThisWeek > 0` (trabajos con
      `completedAt` esta semana; facturas `PAID` con `paidAt` esta semana)
      y usan plurales correctos; ocultas en caso contrario.
- [x] AC-05: Con 0 clientes y 0 trabajos se muestra la invitación con CTA a
      `client_form/0` y se ocultan contadores, trabajos recientes y acciones
      rápidas; captura.
- [x] AC-06: `greetingForHour` vive en `:libs:common` con sus strings; el
      login muestra el mismo saludo que antes (sin regresión, captura).
- [x] AC-07: Strings nuevos en `strings.xml` (`:home`, y `:libs:common` si
      aplica), plurales para cantidades; cero strings hardcodeados nuevos.
- [x] AC-08: `node .devin/verify.cjs` OK; tests nuevos del cómputo semanal
      verdes; suites `:home` y `:login` en verde.
- [x] AC-09: Status bar y nav bar coherentes con el nuevo fondo (hueso light,
      petróleo dark) vía `SystemBarIconStyle`, como en login.
- [x] AC-10: Navegación sin cambios funcionales: menú (clientes, perfil,
      facturas, logout), acciones rápidas y trabajos recientes navegan a las
      mismas rutas que hoy.

## Fuera de alcance

- Rediseño del resto de pantallas (clients, jobs, quotes, invoices, profile).
- Cambios de navegación (bottom bar, FAB menu, gestos).
- Animaciones o confeti (la celebración es estática).
- `JobCard` no se modifica: en el dashboard se sustituye por líneas de
  parte (punto de estado + título + estado); `JobListScreen` sigue usándolo.
- Cambiar `colorScheme.background` global (Slate50 → hueso): se aplicará
  pantalla a pantalla cuando toque cada spec `ui-*`.
- Tipografía de marca (spec `ui-typography`), sincronización, esquema Room.
- Panel "agenda"/calendario semanal.

## Diseño / notas técnicas

- Módulos afectados: `:home` (HomeScreen, HomeViewModel, GetHomeStatsUseCase,
  tests, strings), `:libs:common` (traslado de `greetingForHour` + strings
  `greeting_*`), `:login` (solo import y borrado de lo trasladado).
- Componentes `Mito*` a reutilizar: `MitoSignatureDivider`
  (`:libs:components`). No se crean componentes compartidos nuevos.
- `GetHomeStatsUseCase` pasa a combinar `clients + jobs + invoices`
  (`InvoiceRepository.getAllInvoices()` ya existe) y añade a `HomeStats`:
  `jobsThisWeek`, `completedThisWeek`, `paidInvoicesThisWeek`.
- "Esta semana" = lunes 00:00 a domingo 23:59 en hora local. Cómputo en
  función pura con reloj inyectable (p. ej. `nowProvider: () -> Long` con
  valor por defecto `System.currentTimeMillis`) para tests JVM sin
  `java.time`/desugaring — `java.util.Calendar` basta.
- `greetingForHour` se mueve a `:libs:common` junto a sus strings (mismo
  patrón que `EmailValidator` en SPEC-008: código en common, test ejecutado
  desde `:login` o `:home`, que ya tienen infra de tests; `:libs:common` no).
- Decisión: "terminados" cuenta `COMPLETED` + `INVOICED` para no perder los
  trabajos ya facturados (hoy `INVOICED` desaparece de los contadores).
- Estado vacío: condición `totalClients == 0 && total trabajos == 0`.
- Tarjetas: `CardDefaults` con `BoneCard`/`surface`, esquinas y elevación
  actuales; iconos en `colorScheme.primary`/`tertiary`, sin colores sueltos.

## Mobile checklist

- [x] Estados UI: loading (indicador M3), error (texto `colorScheme.error`),
      empty (invitación) — los tres definidos.
- [x] Navegación: mismas rutas/args que hoy; back stack sin cambios.
- [x] Lifecycle: estado vía `StateFlow`/`collectAsState`, sobrevive rotación.
- [ ] Permisos: ninguno nuevo.
- [x] Offline: Room es la fuente; sin red funciona igual.
- [x] Edge cases: usuario sin nombre (fallback "Usuario"), semana sin datos
      (celebración oculta), `INVOICED` contado como terminado.
- [x] i18n: strings en `strings.xml`, plurales para cantidades.

## Plan de tests

- [ ] `gradlew.bat :app:assembleDebug` compila.
- [ ] Unit tests nuevos: `GetHomeStatsUseCase` con reloj fijo (trabajos
      dentro/fuera de semana, `scheduledAt` vs `createdAt`, factura `PAID`
      con `paidAt`, `INVOICED` como terminado, vacío).
- [ ] `gradlew.bat :home:testDebugUnitTest :login:testDebugUnitTest` verdes.
- [ ] Flujo en dispositivo: login → dashboard light/dark, con datos y con
      cuenta vacía (o datos borrados en emulador); verificar nav bar.
- [ ] Screenshots en `EVIDENCE.md`: light, dark, vacío, celebración, login
      sin regresión.

## Hallazgos fuera de scope

- `HomeStats.completedJobs` hoy ignora `INVOICED`; esta spec lo corrige como
  parte del cómputo de "terminados", pero los filtros de `JobListScreen`
  podrían tener el mismo sesgo — revisar en spec `ui-jobs`.
- Trabajos sin `scheduledAt` cuentan por `createdAt`: documentado, no un bug.
- `:app` duplica las strings `home_*` de `:home` y las pisa en runtime
  (detectado: `home_stat_completed` mostraba el texto viejo). Corregido el
  valor en `:app`; la duplicación completa es deuda — candidata a spec de
  limpieza de recursos.
- `JobCard` conserva su paleta previa (malva en light/dark) — fuera de
  alcance, pendiente en `ui-jobs`.
- La app arranca siempre en login aunque haya sesión en SharedPreferences:
  comportamiento preexistente, no tocado.
