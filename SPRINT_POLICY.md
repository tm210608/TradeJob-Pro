# TradeJob Pro — Política de Desarrollo y Seguridad

## Estructura de Ramas

```
main              → Producción (protegida, solo squash desde develop)
├── develop       → Integración diaria (protegida, base de ramas)
│   ├── fix/      → Bugfixes: fix/descripcion-breve
│   ├── feat/     → Features:  feat/descripcion-breve
│   ├── chore/    → Refactor/limpieza: chore/descripcion-breve
│   └── test/     → Tests: test/descripcion-breve
└── release/      → Release candidate (cuando toque)
```

| Tipo | Base | Prefijo | Ejemplo |
|---|---|---|---|
| Bug crítico | `develop` | `fix/` | `fix/clientDao-method-names` |
| Bug menor | `develop` | `fix/` | `fix/email-validation` |
| Feature | `develop` | `feat/` | `feat/firebase-auth` |
| Refactor | `develop` | `chore/` | `chore/remove-dead-code` |
| Tests | `develop` | `test/` | `test/login-viewmodel` |

**Regla de oro:** Nunca se hace commit directo a `main` ni `develop`.

## Commits

Formato: `<tipo>(<ámbito>): <verbo presente> <descripción>`

```
fix(login):   validate email on client form
feat(home):   add delete confirmation dialog
chore(db):    unify Result and Resources into Result
test(login):  add LoginViewModel unit tests
```

## Sistema de Seguridad (3 Capas)

### Capa 1 — Pre-Push Hook (Local)
Se ejecuta **antes de subir cualquier cambio a GitHub**. Si falla, el push se rechaza.

```bash
.githooks/pre-push  →  ./gradlew assembleDebug  →  ./gradlew test
```

### Capa 2 — CI (GitHub Actions, Automático)
Se ejecuta **en cada push a cualquier rama**. Mismas verificaciones que el hook local, pero en remoto.

### Capa 3 — Protección de Ramas (GitHub)
`main` y `develop` protegidas: no se puede hacer push directo, solo merge vía PR con CI verde.

---

## Flujo Diario

```bash
# 1. Partir de develop actualizado
git checkout develop && git pull

# 2. Crear rama de trabajo
git checkout -b fix/clientDao-method-names

# 3. Desarrollar y commitear
git commit -m "fix(db): rename getAll to getAllClients in ClientDao"

# 4. Subir — el pre-push hook verifica build + tests primero
git push -u origin fix/clientDao-method-names
#   ↑ Si build o tests fallan, el push se rechaza automáticamente

# 5. Abrir PR en GitHub contra develop
#    → CI corre de nuevo en la rama
#    → Solo se permite merge si CI pasa

# 6. Merge squash a develop, luego develop → main
```

## Protección de Ramas en GitHub (Configurar una vez)

```
Settings → Branches → Add rule → "main"
☑ Require status checks to pass before merging
  ☑ Android CI (assembleDebug, test)
☑ Require branches to be up to date
☑ Do not allow bypassing the above settings

Repetir para "develop" (misma configuración)
```

## Ciclo de Sprint (48h máx)

```
Día 1 — Planning (elegir 2-4 issues) → Desarrollo en ramas
Día 2 — CI verifica → PR → Squash merge a develop → PR a main
```

## Checklist de Merge a `main`

- [ ] CI verde en la rama de origen
- [ ] Compila en debug
- [ ] Tests unitarios pasan
- [ ] Sin warnings de lint nuevos
- [ ] No hay secretos/credenciales en el diff
