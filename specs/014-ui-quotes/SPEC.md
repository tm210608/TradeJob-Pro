# SPEC-014: Presupuestos cálidos — el presupuesto como parte

**Estado:** completada (verificada en dispositivo físico)
**Rama:** `feature/spec-014-ui-quotes`
**Fecha:** 2026-11-15

## Qué construimos

Piel de taller para el flujo de presupuestos (lista + formulario),
siguiendo la dirección minimalista aprobada en las specs anteriores,
y reparación de dos huecos funcionales decididos por el usuario:
el **estado del presupuesto se puede cambiar por primera vez** (chips,
como en facturas) y se puede **borrar un presupuesto** con confirmación.

**Lista de presupuestos (`quote_list/{clientId}`):**

- Fondo hueso en light / gradiente petróleo en dark (como
  `ClientDetailScreen`), TopAppBar transparente con el **nombre del
  cliente** de título (decisión del usuario, igual que en la ficha).
- `QuoteCard` rediseñada en `BoneCard` con borde sutil (como `JobCard`):
  número del presupuesto en petróleo, chip de estado con los colores del
  parte, **fecha de creación** ("13 nov") y **total formateado**
  ("1.250,50 €" — hoy muestra "Total: 1250.5" sin símbolo).
- Colores del estado coherentes con el parte: `DRAFT` neutro (outline),
  `SENT` petróleo, `ACCEPTED` lima (tertiary), `REJECTED` rojo de error
  (es el único cierre negativo real).
- Estado vacío conserva icono + texto y añade CTA "Nuevo presupuesto"
  que navega a `quote_form/{clientId}/0` (como el vacío de trabajos).
- FAB petróleo para crear.

**Formulario de presupuesto (`quote_form/{clientId}/{quoteId}`):**

- Misma piel: fondo hueso/petróleo, TopAppBar transparente.
- **Selector de estado nuevo**: sección "Estado" con `FilterChips`
  (Borrador / Enviado / Aceptado / Rechazado), mismo patrón que
  `InvoiceFormScreen`. El estado elegido se persiste al guardar.
- **Borrar presupuesto**: en modo edición, icono de papelera en la
  TopAppBar + confirmación `MitoBottomSheet`/`ConfirmMitoButtonSheet`
  (mismo patrón que el borrado de trabajos en `ClientDetailScreen`).
  Al confirmar, vuelve a la lista y el presupuesto ya no aparece.
- "Añadir partida" pasa de un "+" suelto a un botón secundario con
  icono y texto. Partidas como `BoneCard` con borde y borrado por línea
  como hoy.
- Totales agrupados en una tarjeta-parte `BoneCard`: "Subtotal:
  1.000 €", "IVA (21%): 210 €" y "Total: 1.210 €" destacado —
  todo formateado es-ES con símbolo de euro.
- Firma, exportar PDF y "Convertir en factura" conservan su
  funcionalidad actual; los botones pasan al estilo de marca. Con el
  selector de estado, el flujo completo borrador → aceptado → factura
  es alcanzable por primera vez.

### Contexto / por qué

Es la siguiente pantalla de la serie "piel de taller" (login → dashboard
→ trabajos → clientes). Al explorar el código se descubrió que
`QuoteFormViewModel.onStatusChanged` existe pero **ninguna pantalla lo
llama**: el estado nace `DRAFT` y nunca cambia, así que el botón
"Convertir en factura" (solo visible si `ACCEPTED`) es código
inalcanzable. Tampoco hay forma de borrar presupuestos aunque
`delete()` existe en el ViewModel. El usuario decidió incluir ambas
reparaciones en esta spec porque son pequeñas y desbloquean el flujo
real del producto (presupuesto enviado → aceptado → factura).

## Criterios de aceptación

- [x] AC-01: La lista muestra la piel de taller: fondo hueso (light) /
      gradiente petróleo (dark), TopAppBar transparente con el nombre
      del cliente. Verificable con captura.
- [x] AC-02: Cada `QuoteCard` muestra número, estado con color del
      parte, fecha "13 nov" (`createdAt`) y total "890 €" formateado
      es-ES. Verificable con captura sobre datos demo.
- [x] AC-03: Colores de estado: `DRAFT` outline, `SENT` petróleo,
      `ACCEPTED` lima, `REJECTED` error. DRAFT y ACCEPTED ejercitados
      en dispositivo; SENT y REJECTED verificados en el selector del
      formulario (ver EVIDENCE).
- [x] AC-04: Cliente sin presupuestos: estado vacío con CTA "Nuevo
      presupuesto" que navega a `quote_form/{clientId}/0`.
- [x] AC-05: El formulario muestra la piel: partidas en `BoneCard`,
      botón "Añadir partida" con icono + texto, totales en tarjeta
      formateados ("Subtotal: 1.000 €", "Total: 1.210 €").
- [x] AC-06: El selector de estado (chips) persiste el estado elegido:
      marcar "Aceptado", guardar y la tarjeta de la lista muestra
      "Aceptado". Verificado en dispositivo.
- [x] AC-07: Un presupuesto `ACCEPTED` muestra "Convertir en factura";
      al pulsarlo se crea la factura (snackbar de éxito). Flujo
      alcanzable por primera vez — verificado en dispositivo
      (factura `F-1794766705` comprobada en la lista de facturas).
- [x] AC-08: En modo edición, la papelera de la TopAppBar abre la
      confirmación; al confirmar se vuelve a la lista y el presupuesto
      ya no aparece. Cancelar no borra nada.
- [x] AC-09: Firma y exportar PDF siguen funcionando: "Exportar PDF"
      genera el archivo y muestra snackbar de éxito (pad de firma
      visible, no ejercitado — ver EVIDENCE).
- [x] AC-10: Light y dark verificados en capturas.
- [x] AC-11: Textos nuevos en `strings.xml` de `:home` y espejo en
      `:app` (deuda conocida de duplicación); nada hardcodeado.

## Fuera de alcance

- Rediseño de facturas (`ui-invoices` es la siguiente en cola).
- Cambiar la numeración de presupuestos (sigue `P-<epoch>`; una
  numeración legible tipo `P-2026-014` sería spec propia).
- Campo `validUntil` (existe en la entidad, sin UI) ni vinculación
  `jobId` presupuesto↔trabajo.
- Enviar el presupuesto por WhatsApp/email directamente, ni navegar a
  la factura recién creada tras convertir (hoy solo hay snackbar).
- Sincronización de presupuestos a Firestore (cola `sync-completa`).
- Persistir la firma del presupuesto (hoy solo se usa para el PDF del
  momento; no se guarda en la entidad).
- Navegación general, bottom bar y resto de pantallas.

## Diseño / notas técnicas

- Módulos afectados: `:home` (`QuoteListScreen`, `QuoteFormScreen`,
  `QuoteListViewModel`, strings); espejo de strings en `:app`.
- `QuoteListViewModel` necesita el nombre del cliente: inyectar
  `ClientRepository`/`getClientById` (mismo repositorio que ya usa
  `QuoteFormViewModel`).
- Formateadores puros nuevos en `QuoteFormatters.kt` (paquete
  `ui/quotes`), espejo de `JobFormatters`: `formatQuoteAmount(total)`
  con `NumberFormat` es-ES + " €"; fecha con el mismo `"d MMM"` de
  `formatJobDate` (reutilizar la función si queda en ámbito compartido
  o duplicar el helper privado mínimo — decidir en implementación).
- `delete()` y `onStatusChanged()` ya existen en `QuoteFormViewModel`;
  esta spec solo los conecta a la UI. No tocar dominio ni entidades.
- Reutilizar: `BoneCard`/`BoneLight`, `Petrol20`/`Petrol30`,
  `FilterChip`, `MitoBottomSheet` + `ConfirmMitoButtonSheet`,
  `PrimaryButton`/`OutlinedButton`, tokens de `colorScheme`.
- El mapa de colores de estado vive junto a `QuoteStatus` para que
  lista y formulario usen el mismo.

## Mobile checklist

- [x] Estados UI: loading (spinner existente), empty (icono + CTA
      nuevo), error (snackbar existente)
- [x] Navegación: rutas `quote_list/{clientId}` y
      `quote_form/{clientId}/{quoteId}` intactas; back stack igual
- [x] Permisos: ninguno nuevo
- [x] Offline: todo local en Room; PDF local como hoy
- [x] Edge cases: cliente sin presupuestos; presupuesto sin partidas
      (validación existente impide guardar); estado `REJECTED` sin
      botón de conversión; totales con decimales
- [x] i18n: strings en `strings.xml` (`:home` + espejo `:app`)

## Plan de tests

- [x] `gradlew.bat :app:assembleDebug` compila
- [x] Unit tests nuevos: `QuoteFormattersTest` (importe con/sin
      decimales, fecha nula y con fecha) — 7 tests verdes
- [x] `gradlew.bat :home:testDebugUnitTest :login:testDebugUnitTest`
      verdes
- [x] `node .devin/verify.cjs` + verificación en el móvil físico del
      usuario (autorizada): lista del cliente, crear presupuesto,
      cambiar estado a Aceptado y ver la tarjeta, convertir a factura,
      exportar PDF, borrar con confirmación, light y dark
- [x] Screenshots en `EVIDENCE.md`

## Hallazgos fuera de scope

- `P-<epoch>` como número de presupuesto es poco legible — candidata a
  spec de numeración legible (`P-2026-014`, secuencial por usuario).
- `validUntil` existe en `QuoteEntity` sin UI — idea "válido hasta" de
  la exploración Stitch; spec futura si el usuario la quiere.
- Tras "Convertir en factura" solo hay snackbar: no navega a la factura
  ni marca el presupuesto como facturado. Anotar en `ui-invoices`.
- La firma no se persiste: solo se adjunta al PDF generado en el
  momento. Reabrir el presupuesto no recupera la firma.
- Presupuestos y facturas no se sincronizan a Firestore (ya en cola
  `sync-completa`).
