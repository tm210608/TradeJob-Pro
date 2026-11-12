# SPEC-009: Longitud mínima de contraseña unificada (6 caracteres)

**Estado:** verificada
**Rama:** `feature/spec-009-password-min-length`
**Fecha:** 2026-11-12

## Qué construimos

Unificar la longitud mínima de contraseña a **6 o más caracteres** en login y
registro. Hoy el registro acepta 6 y el login exige más de 6: quien se
registre con exactamente 6 caracteres no podrá volver a entrar.

### Contexto / por qué

Bug real detectado en SPEC-008 (documentado como hallazgo). Firebase Auth
exige un mínimo de 6 caracteres, así que 6 es el mínimo natural y, sobre
todo, rescata a los usuarios que ya pudieran haberse registrado con 6.

## Criterios de aceptación

- [x] AC-01: `LoginViewModel.onLoginChanged` activa `loginEnable` con
      contraseña de exactamente 6 caracteres + email válido
- [x] AC-02: `RegisterViewModel` sigue exigiendo `isValid` solo con
      contraseña de ≥6 (sin regresión: 5 sigue siendo inválido en ambos)
- [x] AC-03: test de regresión actualizado/nuevo: password "123456" activa
      el login y es válida en registro; "12345" no lo es en ninguno
- [x] AC-04: `gradlew.bat :login:testDebugUnitTest :home:testDebugUnitTest`
      verde

## Fuera de alcance

- Cambiar el mínimo de Firebase (sigue siendo 6, igual que la app)
- Mensajes de error o copy nuevo de UI
- Reset/migración de cuentas existentes (no hace falta: 6 ya era válido
  en Firebase y en registro)

## Diseño / notas técnicas

- `LoginViewModel.isValidPassword`: `password.length > 6` → `>= 6`.
  Único cambio de producción.
- Actualizar el test de `LoginViewModelTest` que documenta el
  comportamiento anterior (`>6`) y añadir el caso límite "12345" → inválido.
- Actualizar comentarios de hallazgo en los tests de SPEC-008.

## Plan de tests

- [ ] `:login:testDebugUnitTest` verde con el caso límite cubierto
- [ ] `node .devin/verify.cjs` OK
- [ ] App en emulador: login renderiza igual

## Hallazgos fuera de scope

- Ninguno previsto.
