---
name: implement-spec
description: Implementar una SPEC aprobada de TradeJob Pro por capas con pruebas incrementales.
---
# /implement-spec <feature>

1. Lee AGENTS.md, .devin/STATE.md y la SPEC aprobada en specs/. Si falta aprobación, detente y pídela.
2. Comprueba git status. Usa una rama feature/<nombre>; no mezcles trabajo pendiente ni hagas cambios destructivos.
3. Sigue patrones vecinos: Room y repositorios, casos de uso, ViewModels, Hilt y finalmente Compose/navegación. Conserva offline-first y componentes Mito; strings en recursos.
4. Para correcciones añade primero una prueba que falle cuando sea viable; ejecuta node .devin/verify.cjs tras los cambios.
5. No marques criterios como verificados solo por implementar. No hagas commits, merge ni push sin petición del usuario. Bugs fuera de alcance quedan registrados, no se arreglan a escondidas.
