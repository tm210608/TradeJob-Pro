# EVIDENCE — SPEC-012: Trabajos cálidos

**Fecha:** 2026-11-13 | **Rama:** `feature/spec-012-ui-jobs` | **Dispositivo:** `emulator-5560` (AVD propio `TradeJob_Test`, Android 36.1, ventana + GPU host)

## Build y tests

- `node .devin/verify.cjs` → `BUILD: OK (app + APK de pruebas)`, `TESTS home: OK`, `TESTS login: OK`. Log: `.devin/android/artifacts/verify.log`.
- Unit tests `:home`: 32 verdes — 7 nuevos en `JobFormattersTest`
  (`formatJobDate`, `formatJobAmount` con prioridad final/budget y formato
  es-ES, `isUrgent` HIGH/URGENT). `JobListViewModelTest` existente ajustado
  con stub de `getClientById` tras añadir `clientName`.
- Unit tests `:login`: verdes.

## Verificación en dispositivo

La ruta `job_list` no recibe navegación: la lista real es la sección
"Trabajos / Averías" de `ClientDetailScreen` (embebe `JobCard`), por eso
las capturas cubren esa pantalla. Cuenta de prueba "Carlos Prueba";
datos semilla previos en Room (cliente + 4 trabajos + factura); cliente
"Cliente Vacio" creado por UI para el estado vacío.

| Captura (local, en `.devin/android/artifacts/`) | AC | Resultado |
|---|---|---|
| `spec-012-jobs-dark.png` | AC-02/03/04/05 | Dark: "Reparar caldera — En curso · **Urgente**" (ámbar, prioridad Alta), "Sin fecha · 300 €"; "Completado" lima, "Pendiente" neutro |
| `spec-012-jobs-light.png` | AC-01/03/04/05 | Light: mismas tarjetas `BoneCard` con borde; fechas "10 nov · 890 €", "11 nov · 120 €" |
| `spec-012-form-light.png` | AC-07 | Ficha light: hueso, "Editar Trabajo" + sección "Detalles", campos y dropdowns intactos, "Sin fotos aun" |
| `spec-012-empty.png` | AC-06 | Cliente sin trabajos: icono + "No hay trabajos registrados" + botón "Nuevo Trabajo" |
| `spec-012-cta.png` | AC-06/10 | "Nuevo Trabajo" abre `job_form/{clientId}/0` (formulario nuevo trabajo) |

## Criterios no cubiertos / limitaciones honestas

- `JobListScreen` (`job_list/{clientId}`) quedó rediseñada (TopAppBar con
  nombre de cliente vía `clientName`, estado vacío con CTA, piel) pero la
  ruta **no recibe navegación** — verificado en código; su aspecto no se
  capturó en dispositivo al no ser alcanzable.
- Los badges "Antes"/"Después"/"Progreso" migraron a colores de tema en
  código (`PhotoItem`); los trabajos semilla no tienen fotos, sin captura
  visual del badge. El caso `PROGRESS` antes caía en "Después" (bug leve)
  y ahora tiene texto propio.
- La pantalla de detalle de cliente conserva su cabecera/tarjeta de datos
  con estilo anterior (malva): fuera de alcance → `ui-clients`.
- En `ClientDetailScreen` el botón PDF de la tarjeta está sin implementar
  (`onExportPdf` vacío) — preexistente.
- Interacción por taps de coordenadas; un tutorial de stylus de Gboard y
  la pila de back restaurada provocaron un desvío a otra app instalada en
  el AVD (`com.aistudio.gymtracker` — preinstalada, no relacionada); sin
  consecuencias: se relanzó la app y se verificó el flujo real.
