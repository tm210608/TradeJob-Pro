# Specs — Spec-Driven Mobile Development

Cada feature, fix o refactor relevante vive en `specs/NNN-<nombre-corto>/`:

```
specs/
  templates/SPEC.md          → plantilla base (copiar al crear una spec)
  001-estabilidad/
    SPEC.md                  → qué construimos + criterios + fuera de alcance + plan de tests
    EVIDENCE.md              → prueba de que está hecho: build, tests, device, screenshots
```

## Ciclo

```
PETICIÓN → PREGUNTAS → SPEC → AGENTE → BUILD → TESTS → DEVICE → EVIDENCIA
```

1. **Especificar**: copiar `templates/SPEC.md`, rellenar con criterios *testeables*.
2. **Branch**: `feature/spec-NNN-<nombre>`. Un spec = una rama = un diff acotado.
3. **Implementar**: el agente/humano trabaja solo lo que la spec dice.
4. **Verificar**: build + tests + ejecución real en dispositivo.
5. **Evidenciar**: `EVIDENCE.md` con logs y screenshots. Sin evidencia no hay "done".

## Numeración

`NNN` correlativo (001, 002…). Prefijos opcionales en el nombre para series:
`ui-XXX` para el rediseño M3 Expressive (p. ej. `specs/010-ui-home/`).

## Reglas

- Criterios de aceptación: cosas que un test o una captura pueden demostrar. Nada de
  "se ve bien" o "funciona mejor".
- "Fuera de alcance" es obligatorio: define qué NO se toca en esta spec.
- Si la spec crece, se parte. Specs pequeñas > specs épicas.
