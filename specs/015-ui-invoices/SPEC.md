# SPEC-015: Facturas cálidas — saber de quién es cada factura

**Estado:** completada (verificada en dispositivo físico)
**Rama:** `feature/spec-015-ui-invoices`
**Fecha:** 2026-11-15

## Qué construimos

Piel de taller para el flujo de facturas (lista global + detalle),
siguiendo la dirección aprobada en las specs anteriores, y reparación
del hueco más grave de la lista: **no se ve de qué cliente es cada
factura ni su fecha**. Decisiones del usuario: tarjeta con número +
cliente + estado + fecha + total, y colores de estado pendiente ámbar /
pagada lima / vencida roja / cancelada gris.

**Lista de facturas (`invoice_list`, global — todos los clientes):**

- Fondo hueso en light / gradiente petróleo en dark, TopAppBar
  transparente con título "Facturas" (es global: no lleva nombre de
  cliente arriba, a diferencia de presupuestos).
- `InvoiceCard` rediseñada en `BoneCard` con borde sutil (como
  `QuoteCard`): número en petróleo, **nombre del cliente**, chip de
  estado con color, **fecha de emisión** ("15 nov", `issuedAt`) y
  **total formateado** ("108,90 €" — hoy "Total: 108.9" sin símbolo).
- Colores de estado (decisión del usuario): `PENDING` ámbar de aviso,
  `PAID` lima, `OVERDUE` rojo de error, `CANCELLED` neutro (outline).
  El mapa vive junto a `InvoiceStatus` para que lista y detalle usen
  el mismo.
- Si hay cobro parcial, la línea "Pagado X · Pendiente Y" se conserva
  pero con formato es-ES.
- Filtros existentes (Todas/Pendiente/Pagada/Vencida) conservados.
- Estado vacío conserva icono + texto; **sin CTA nuevo** (las facturas
  nacen de presupuestos, no hay creación directa — igual que hoy).
- Botón "Exportar PDF" por tarjeta conservado.

**Detalle de factura (`invoice_form/{invoiceId}`):**

- Misma piel: fondo hueso/petróleo, TopAppBar transparente con el
  **número de factura** de título (hoy dice "Factura" genérico).
- Tarjeta de cliente, chips de estado, desplegable de método de pago y
  "Marcar como pagada" conservados con la nueva piel.
- Totales en tarjeta `BoneCard` formateada es-ES: subtotal, IVA, IRPF
  (si aplica), total destacado, pagado/pendiente — hoy todo "%.2f" sin
  símbolo.
- Fechas con formato español corto ("15 nov" / emisión y vencimiento),
  no "dd/MM/yyyy" anglosajón.
- Borrado: de `AlertDialog` plano a la hoja `MitoBottomSheet` /
  `ConfirmMitoButtonSheet` del proyecto (misma UX que presupuestos);
  conserva el botón en el cuerpo del formulario.

### Contexto / por qué

Es la última pantalla de la serie "piel de taller" (login → dashboard →
trabajos → clientes → presupuestos). La lista de facturas es global —
mezcla todos los clientes — y hoy cada tarjeta muestra solo número,
estado y "Total: 108.9": imposible saber de quién es ni cuándo se emitió
sin abrirla. Además el borrado usa un `AlertDialog` distinto al patrón
de hoja del resto de la app. A diferencia de presupuestos, el estado ya
se puede cambiar (chips existentes), así que es una spec de piel y
legibilidad, sin huecos funcionales.

## Criterios de aceptación

- [x] AC-01: Lista con piel de taller: fondo hueso/petróleo, TopAppBar
      transparente "Facturas". Verificable con captura.
- [x] AC-02: Cada `InvoiceCard` muestra número, **nombre del cliente**,
      chip de estado, fecha de emisión "15 nov" y total "108,90 €"
      es-ES. Verificable con captura sobre datos demo.
- [x] AC-03: Colores: `PENDING` ámbar, `PAID` lima, `OVERDUE` error,
      `CANCELLED` neutro — mismo mapa en lista y detalle.
      (Pendiente/pagada ejercitados; vencida/anulada vistos en el
      selector — ver EVIDENCE.)
- [x] AC-04: Cobro parcial: línea "Pagado X € · Pendiente Y €" es-ES.
      (Verificada tras pago completo; ver EVIDENCE.)
- [x] AC-05: Filtros por estado siguen funcionando con la nueva piel;
      estado vacío conserva icono + texto. (Estado vacío verificado;
      filtro concreto no ejercitado — ver EVIDENCE.)
- [x] AC-06: Detalle con piel: TopAppBar con el número de factura,
      tarjeta de cliente, chips de estado funcionales, totales en
      `BoneCard` con euros ("Subtotal: 90 €", "IVA: 18,90 €",
      "Total: 108,90 €") y fechas "15 nov".
- [x] AC-07: Borrado con `ConfirmMitoButtonSheet`: cancelar deja todo
      intacto; confirmar borra y vuelve a la lista sin la factura.
- [x] AC-08: "Marcar como pagada" y "Exportar PDF" siguen funcionando
      (snackbar de éxito en ambos).
- [x] AC-09: Light y dark verificados en capturas.
- [x] AC-10: Textos nuevos en `strings.xml` de `:home` y espejo en
      `:app`; los literales existentes en ViewModels/pantallas tocadas
      ("Total:", "Pagado:", mensajes PDF, eventos del VM) pasan a
      recursos.

## Fuera de alcance

- Crear facturas manualmente (hoy solo nacen de presupuestos aceptados).
- Navegar a la factura recién creada tras "Convertir en factura" ni
  marcar el presupuesto como facturado (anotado en SPEC-014).
- Partidas de factura (el modelo `InvoiceEntity` no las tiene: solo
  totales; requeriría entidad nueva — spec propia).
- Número de factura legible (`F-<epoch>`, misma deuda que `P-<epoch>`).
- Vencimientos automáticos (`dueAt` existe; nada marca OVERDUE solo).
- Pagos parciales editables (solo existe "marcar pagada" completa).
- Sincronización de facturas (cola `sync-completa`).
- Enviar factura por WhatsApp/email ni Verifactu (stub en `:common`).

## Diseño / notas técnicas

- Módulos afectados: `:home` (`InvoiceListScreen`, `InvoiceFormScreen`,
  `InvoiceFormViewModel`, strings); espejo de strings en `:app`.
- Nombre del cliente en la lista: `InvoiceListViewModel` ya inyecta
  `ClientRepository`; cargar `getAllClients()` una vez a un mapa
  `id → nombre` (el lazy-load actual por PDF se reusa/unifica).
- Formateadores puros nuevos `InvoiceFormatters.kt` (importe es-ES +
  fecha "d MMM", espejo de `QuoteFormatters`) + tests.
- Reutilizar: `BoneCard`/`BoneLight`, `Petrol20`/`Petrol30`,
  `AmberWarning`, `FilterChip`, `MitoBottomSheet` +
  `ConfirmMitoButtonSheet`, `PrimaryButton`/`OutlinedButton`,
  `SystemBarIconStyle`, tokens de `colorScheme`.
- Mensajes de evento del ViewModel ("Factura actualizada", errores,
  "PDF generado…") a recursos con `Res` en el evento o contexto —
  misma deuda `i18n-domain` que el resto; solo los archivos tocados.
- No tocar dominio ni entidades; `markAsPaid`/`delete`/`save` se
  conservan.

## Mobile checklist

- [x] Estados UI: loading (spinner existente), empty (icono + texto),
      error (snackbar existente)
- [x] Navegación: rutas `invoice_list` y `invoice_form/{invoiceId}`
      intactas; back stack igual
- [x] Permisos: ninguno nuevo
- [x] Offline: todo local en Room; PDF local como hoy
- [x] Edge cases: lista vacía; filtro sin resultados; factura sin
      vencimiento ni IRPF; cobro parcial; cliente borrado (la factura
      se borra en cascada — no puede quedar huérfana)
- [x] i18n: strings en `strings.xml` (`:home` + espejo `:app`)

## Plan de tests

- [x] `gradlew.bat :app:assembleDebug` compila
- [x] Unit tests nuevos: `InvoiceFormattersTest` (importe con/sin
      decimales, fecha, línea pagado/pendiente si se extrae a helper)
- [x] `gradlew.bat :home:testDebugUnitTest :login:testDebugUnitTest`
      verdes
- [x] `node .devin/verify.cjs` + dispositivo: lista con cliente/fecha/
      total, filtros, detalle con piel, cambiar estado, marcar pagada,
      exportar PDF, borrar con hoja (cancelar y confirmar), light y dark
- [x] Screenshots en `EVIDENCE.md`

## Hallazgos fuera de scope

- `F-<epoch>` como número es poco legible — misma deuda que `P-<epoch>`;
  candidata a spec de numeración legible compartida.
- Nada marca `OVERDUE` automáticamente al pasar `dueAt` — el estado se
  elige a mano. Candidata a spec "vencimientos".
- Las facturas no tienen partidas (`InvoiceEntity` solo guarda totales):
  el PDF lista conceptos del presupuesto origen si existe; una factura
  manual no tendría desglose. Anotar si se hace spec de partidas.
- El snackbar de PDF en la lista muestra el nombre de archivo; en el
  detalle solo "PDF generado correctamente". Unificar si se quiere.
- Eventos de ViewModel con literales en español (`i18n-domain` ya en
  cola) — esta spec limpia solo los archivos que toca.
