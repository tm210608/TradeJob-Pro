# SPEC-013: Clientes cálidos — ficha con contacto directo

**Estado:** verificada
**Rama:** `feature/spec-013-ui-clients`
**Fecha:** 2026-11-13

## Qué construimos

Piel de taller para el flujo de clientes y contacto en un toque desde la
ficha, siguiendo la dirección minimalista aprobada en la exploración
Stitch (propuesta "zen"):

**Detalle de cliente (`client_detail/{clientId}`):**

- Ficha compacta: nombre en petróleo, persona de contacto (teléfono),
  email y dirección con iconos pequeños, y notas (si hay) discretas.
- **Fila protagonista de 3 botones de contacto directo**: `Llamar`
  (ACTION_DIAL `tel:`), `Email` (ACTION_SENDTO `mailto:`) y `Ruta`
  (ACTION_VIEW `geo:0,0?q=<dirección>`). Si un dato falta (email o
  dirección vacíos) ese botón no se muestra. Sin permisos nuevos.
- **Línea de resumen de trabajos**: una sola línea de texto plano con
  puntos de color, p. ej. "● 1 en curso · ● 1 pendiente · ● 2
  completados" (completados = COMPLETED + INVOICED, coherente con el
  dashboard). Si no hay trabajos no se muestra la línea.
- **Tarjeta "Último trabajo"**: una sola tarjeta destacada con el trabajo
  más reciente (`createdAt` mayor), con título, estado, fecha e importe
  (mismo formato que `JobCard`). Si el cliente no tiene trabajos, la
  tarjeta no aparece y la CTA de crear trabajo sigue visible.
- **Lista embebida como hoy**: la sección "Trabajos / Averías" se
  conserva embebida con FilterChips y las `JobCard` de SPEC-012 (el
  usuario decidió no mover la lista a `job_list`; la ruta muerta sigue
  documentada para la spec de limpieza).
- La pantalla completa migra a la piel: fondo `BoneLight`/petróleo,
  TopAppBar transparente con el **nombre del cliente** de título
  (hoy dice "Mis Clientes"), iconos de presupuestos y editar en barra.

**Lista de clientes (`client_list`):**

- Solo piel (sin tocar funcionalidad): tarjetas en `BoneCard`/borde
  sutil en vez del malva heredado, nombre en petróleo, "Ver trabajos"
  con el estilo del sistema (aprobado por el usuario).

### Contexto / por qué

El usuario valoró las propuestas Stitch pero las encontró recargadas:
ver todas las averías a la vez se hace pesado. La dirección aprobada
prioriza la ficha limpia y el contacto directo — en campo, llamar al
cliente o abrir la ruta GPS desde su ficha es lo más usado. La lista
completa de trabajos queda accesible en la misma pantalla, debajo.

Referencia visual: `.devin/android/artifacts/stitch/stitch-minimal.png`.

## Criterios de aceptación

- [x] AC-01: La ficha muestra los 3 botones Llamar/Email/Ruta; cada uno
      lanza el intent correspondiente (dialer con `tel:<phone>`, correo
      con `mailto:<email>`, mapa con la dirección). Verificable con
      captura + `adb logcat` o inspección del intent en emulador.
- [x] AC-02: Cliente sin email → no hay botón Email; sin dirección → no
      hay botón Ruta. Verificable con el cliente "Cliente Vacio" (sin
      email/dirección: solo botón Llamar).
- [x] AC-03: La línea de resumen refleja los conteos reales por estado
      del cliente (verificable con Cliente Semilla: 1 en curso,
      1 pendiente, 1 completado + 1 facturado → "2 completados").
- [x] AC-04: La tarjeta "Último trabajo" muestra el trabajo con
      `createdAt` mayor del cliente, con estado/fecha/importe.
- [x] AC-05: Cliente sin trabajos: sin línea de resumen ni tarjeta;
      estado vacío + CTA "Nuevo Trabajo" intacto y navega a `job_form`.
- [x] AC-06: La lista embebida de trabajos (filtros + JobCard SPEC-012,
      borrado, compartir) sigue funcionando igual.
- [x] AC-07: TopAppBar muestra el nombre del cliente; iconos de
      presupuestos y editar siguen navegando a sus rutas.
- [x] AC-08: Light y dark verificados en capturas.
- [x] AC-09: Lista de clientes con la piel de taller (tarjetas
      `BoneCard`, nombre petróleo, botón "Ver trabajos" en estilo de marca).
- [x] AC-10: Nuevos textos en `strings.xml`; nada hardcodeado.

## Fuera de alcance

- Rediseño del formulario de cliente (`client_form`), presupuestos,
  facturas, perfil.
- Resucitar/mover la lista a `job_list/{clientId}` (decidido: embebida).
- Numero de parte (#OT), técnico asignado, garantía, balance del cliente
  — ideas Stitch que requerirían cambios de modelo/dominio: specs propias.
- Cambios en navegación general ni bottom bar.

## Diseño / notas técnicas

- Módulos afectados: `:home` (ClientDetailScreen, ClientListScreen,
  strings, posiblemente ClientDetailViewModel para la tarjeta destacada —
  puede derivarse en UI desde `jobs` sin tocar dominio).
- Intents directos con `LocalContext` — sin dependencias nuevas ni
  permisos (DIAL no requiere CALL_PHONE).
- "Último trabajo" = `jobs.maxByOrNull { it.createdAt }` — pura y
  testeable si se extrae a función.
- Conteos del resumen: misma lógica que el dashboard (PENDING,
  IN_PROGRESS, COMPLETED+INVOICED).
- Reutilizar `MitoButton`/`OutlinedButton` y tokens `BoneLight`,
  `BoneCard`, petrol, `LimeAccent`, ámbar de SPEC-012.

## Mobile checklist

- [x] Estados UI: loading (spinner ya existe), empty (sin trabajos),
      sin datos de contacto (botones ocultos)
- [x] Navegación: rutas existentes intactas; intents externos seguros
      (ActivityNotFoundException → snackbar, p. ej. sin app de mapas)
- [x] Permisos: ninguno nuevo (DIAL/SENDTO/VIEW no los requieren)
- [x] Offline: todo local, intents del sistema
- [x] Edge cases: cliente sin email/dirección/notas; sin trabajos;
      notas largas
- [x] i18n: strings en `strings.xml`

## Plan de tests

- [ ] `gradlew.bat :app:assembleDebug` compila
- [ ] Unit tests nuevos: función de "último trabajo" y/o conteos del
      resumen si se extraen
- [ ] `gradlew.bat :home:testDebugUnitTest :login:testDebugUnitTest` verdes
- [ ] Flujo en emulador: ficha Cliente Semilla (botones + resumen +
      último trabajo + lista), Cliente Vacio (solo Llamar + vacío),
      intents de Llamar/Ruta capturados, light y dark
- [ ] Screenshots en `EVIDENCE.md`

## Hallazgos fuera de scope

- `job_list/{clientId}` sigue siendo ruta muerta (spec de limpieza en
  cola; el usuario decidió mantener la lista embebida).
- Ideas Stitch para specs futuras: número de parte, botones de contacto
  también en la lista de clientes, nota técnica destacada, técnico
  asignado, garantías, balance del cliente.
