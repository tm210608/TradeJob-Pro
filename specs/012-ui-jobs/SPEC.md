# SPEC-012: Trabajos cálidos — "Los partes del cliente"

**Estado:** verificada
**Rama:** `feature/spec-012-ui-jobs`
**Fecha:** 2026-11-13

## Qué construimos

Piel de taller (SPEC-010/011) para el flujo de trabajos: la lista de
trabajos del cliente y la ficha de crear/editar
(`job_form/{clientId}/{jobId}`, incluida su sección de fotos).

Nota de implementación (hallazgo): la ruta `job_list/{clientId}` no recibe
navegación — la lista real que ve el usuario es la sección "Trabajos /
Averías" de `ClientDetailScreen`, que embebe `JobCard` y sus propios
filtros. `JobListScreen` queda actualizada igualmente (piel + CTA) pero
hoy es código muerto navegable: candidata a borrar o fusionar en una spec
de limpieza. Los cambios de `ClientDetailScreen` se limitan a sus strings
del bloque de trabajos; su diseño general pertenece a `ui-clients`.

**Lista de trabajos:**

- Fondo hueso (`BoneLight`) en light / petróleo cálido en dark, a nivel de
  pantalla; TopAppBar transparente con el **nombre del cliente** de título
  (hoy muestra "Resumen de actividad" heredado — `clientRepository` ya está
  inyectado en el ViewModel).
- Tarjeta de trabajo (`JobCard`, `BoneCard`/`surface` con borde sutil como
  el parte): título, estado, descripción, **fecha programada** formateada
  ("13 nov" o "Sin fecha") e **importe** en euros (`finalAmount` si existe,
  si no `budgetAmount`). Acciones PDF/compartir/eliminar sin cambios.
- **Colores de estado coherentes con el parte**: pendiente = neutro
  (`onSurfaceVariant`), en curso = `primary`, terminado = `tertiary`,
  facturado = `secondary`. Hoy "Pendiente" usa `error` (rojo de error:
  un trabajo pendiente no es un fallo).
- **Prioridad solo en urgentes**: marca discreta ámbar (punto + etiqueta
  "Urgente") cuando `priority` es `HIGH` o `URGENT`; nada en el resto.
  La lima se reserva a lo positivo.
- Estado vacío: icono + texto + `PrimaryButton` "Crear primer trabajo" →
  `job_form/{clientId}/0` (coherente con el estado vacío del dashboard).
- FilterChips por estado se conservan (solo migrados al `colorScheme`).

**Ficha (form):**

- Misma piel: fondo, TopAppBar transparente, campos ya `Mito*` sin cambios
  funcionales (validación, guardado, borrado fotos, firma digital).
- Badges de foto "Antes"/"Después" con colores de tema (petróleo / lima)
  en vez de `Color.Red`/`Color.Green` puros.
- Visor de foto a pantalla completa sin cambios (negro está bien).

**Limpieza incluida:** strings hardcodeados del flujo pasan a `strings.xml`
("Todos", "Eliminar trabajo…", "No hay trabajos…", "Antes"/"Después",
"Compartir PDF", "PDF"/"Compartir"/"Eliminar" contentDescriptions).

### Contexto / por qué

El login y el dashboard ya se sienten de taller; al entrar en los trabajos
de un cliente se vuelve al Material genérico con "Pendiente" en rojo.
Además la ficha guarda fecha, importe y prioridad que la lista oculta: un
técnico abre la lista buscando "¿cuándo toca?" y "¿cuánto es?".

## Criterios de aceptación

- [x] AC-01: Tarjetas `BoneCard` con borde en light sobre la pantalla de
      detalle de cliente (el fondo general de esa pantalla es de
      `ui-clients`); `JobListScreen` preparada con TopAppBar de cliente
      pero la ruta no recibe navegación (ver hallazgo). Captura.
- [x] AC-02: Tarjetas en superficie cálida con contraste correcto en
      dark; captura.
- [x] AC-03: Cada tarjeta muestra fecha programada formateada (o "Sin
      fecha") e importe (`finalAmount` → si no, `budgetAmount`, en euros);
      verificado con "Sin fecha · 300 €" y "10 nov · 890 €".
- [x] AC-04: Marca ámbar "Urgente" solo con `HIGH`/`URGENT` (verificado
      con prioridad Alta); ausente en `LOW`/`MEDIUM`.
- [x] AC-05: Estados con colores del parte (pendiente neutro, en curso
      petrol, terminado lima, facturado cyan); "Pendiente" ya no es rojo.
- [x] AC-06: Cliente sin trabajos muestra icono + "No hay trabajos
      registrados" + botón "Nuevo Trabajo" que navega a
      `job_form/{clientId}/0`; captura y pulsación verificadas.
- [x] AC-07: Ficha con la misma piel (hueso/petróleo, "Detalles");
      badges "Antes"/"Después"/"Progreso" con colores de tema (código;
      sin fotos sembradas para captura visual). Galería/visor intactos.
- [x] AC-08: Cero strings hardcodeados nuevos; textos del flujo en
      `strings.xml` (incluidos los de la sección de trabajos de
      `ClientDetailScreen`).
- [x] AC-09: `node .devin/verify.cjs` OK; 7 tests nuevos de formato
      (`JobFormattersTest`) verdes; suites `:home` (32) y `:login` verdes.
- [x] AC-10: Comportamiento sin cambios: filtros, eliminar con
      confirmación, compartir texto, navegación a la ficha.

## Fuera de alcance

- Rediseño de la galería de fotos (carousel), firma digital, PDF.
- Cambios en filtros, ordenación o búsqueda de trabajos.
- Pantalla de detalle separada (la ficha es hoy lista+edición).
- clients/quotes/invoices/profile (specs `ui-*` propias).
- Sincronización Firebase; cambios en entidades/Room.

## Diseño / notas técnicas

- Módulos: solo `:home` (`JobListScreen`, `JobListViewModel`,
  `JobFormScreen`, `JobFormViewModel` si toca, strings, tests nuevos).
- `JobListViewModel` expone `clientName: StateFlow<String>` cargado con
  `clientRepository.getClientById(clientId)` (ya inyectado para el PDF).
- Formateadores puros testeables en JVM: `formatJobDate(Long?)`,
  `formatJobAmount(Double?, Double?)`, `isUrgent(JobPriority)` — funciones
  top-level en `ui/jobs` (o `domain` si se prefieren compartibles), con
  `java.util.Calendar`/`SimpleDateFormat` es-ES, sin `java.time`.
- Patrón de fondo/barra idéntico al de `HomeScreen` v2: `SystemBarIconStyle`,
  gradiente `Petrol20→Petrol30` en dark, `BoneLight` en light, TopAppBar
  transparente.
- Iconos Material existentes (`Icons.Default.*`); ninguna dependencia nueva.

## Hallazgos fuera de scope

- **`job_list/{clientId}` no recibe navegación**: la lista real vive en la
  sección "Trabajos / Averías" de `ClientDetailScreen`. `JobListScreen`
  quedó actualizada (piel, `clientName`, estado vacío con CTA) pero hoy
  es código muerto navegable — candidata a borrado/fusión en
  `ui-clients` o una spec de limpieza de rutas.
- En `ClientDetailScreen` el botón PDF de `JobCard` llega con `onExportPdf`
  vacío ("no implementado aquí") — preexistente, no tocado.
- El header/tarjeta de datos del cliente sigue con estilo anterior —
  pertenece a `ui-clients`.
- Idea del usuario (13-11): rellenar el parte por **voz o texto** al
  gusto del trabajador. Registrada como spec futura `voice-parte` en
  `PROGRESS.md`; Android `SpeechRecognizer` lo permite sin dependencias
  nuevas.

## Mobile checklist

- [ ] Estados UI: loading (indicador M3), error (snackbar como hoy), empty
      (icono + texto + CTA).
- [ ] Navegación: mismas rutas/args; back stack sin cambios.
- [ ] Lifecycle: `StateFlow`/`collectAsState`; filtros sobreviven rotación.
- [ ] Permisos: ninguno nuevo (fotos mantienen su flujo actual).
- [ ] Offline: Room es la fuente; sin red funciona igual.
- [ ] Edge cases: sin fecha, sin importe, sin descripción, cliente borrado,
      prioridad nula/desconocida → `MEDIUM`.
- [ ] i18n: strings en `strings.xml`, plurales si aplica.

## Plan de tests

- [ ] `gradlew.bat :app:assembleDebug` compila.
- [ ] Unit tests nuevos: `formatJobDate` (fecha hoy/otro mes/null),
      `formatJobAmount` (final vs budget, null, céntimos), `isUrgent`
      (HIGH/URGENT true, resto false); client name en ViewModel si es
      mockeable sin esfuerzo extra.
- [ ] `gradlew.bat :home:testDebugUnitTest :login:testDebugUnitTest` verdes.
- [ ] Flujo en dispositivo: dashboard → Mis Clientes → cliente → lista
      light/dark → ficha → sección de fotos; lista vacía con CTA.
- [ ] Screenshots en `EVIDENCE.md`: lista light/dark, tarjeta con fecha +
      importe + urgente, vacío, ficha.
