# SPEC-NNN: <título corto>

**Estado:** borrador | en curso | verificada
**Rama:** `feature/spec-NNN-<nombre>`
**Fecha:**

## Qué construimos

<Una o dos frases: la petición tal como llegó, con los huecos resueltos.>

### Contexto / por qué

<Motivación: bug, feature, deuda, negocio.>

## Criterios de aceptación

Cada criterio debe ser verificable por un test, un build o una captura en dispositivo.

- [ ] <criterio testeable 1>
- [ ] <criterio testeable 2>

## Fuera de alcance

- <cosa que NO se toca en esta spec>

## Diseño / notas técnicas

- Módulos afectados:
- Componentes `Mito*` a reutilizar:
- Decisiones:

## Mobile checklist (marca lo que aplique)

- [ ] Estados UI: loading / error / empty definidos
- [ ] Navegación: rutas y argumentos correctos, back stack coherente
- [ ] Lifecycle: sobrevive rotación / proceso muerto
- [ ] Permisos: <cuáles>
- [ ] Offline: comportamiento sin red definido
- [ ] Edge cases: <lista>
- [ ] i18n: strings en `strings.xml`

## Plan de tests

- [ ] `gradlew.bat :app:assembleDebug` compila
- [ ] Unit tests nuevos/actualizados: <cuáles>
- [ ] `gradlew.bat :home:testDebugUnitTest :login:testDebugUnitTest` verdes
- [ ] Flujo en dispositivo: <pasos a ejecutar>
- [ ] Screenshots en `EVIDENCE.md`

## Hallazgos fuera de scope

<Bugs o deuda descubiertos durante el trabajo que NO pertenecen a esta spec.
Crear spec nueva o anotarlos en PROGRESS.md.>
