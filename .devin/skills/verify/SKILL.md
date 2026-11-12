---
name: verify
description: Verificar TradeJob Pro con build, tests, dispositivo seleccionado y evidencia honesta por criterio.
---
# /verify <feature>

1. Lee AGENTS.md, .devin/STATE.md y la SPEC correspondiente en specs/.
2. Ejecuta node .devin/verify.cjs. Resume resultados; el log completo queda en .devin/android/artifacts/verify.log. UP-TO-DATE no significa tests ejecutados de nuevo y NO-SOURCE no significa tests verdes.
3. Para comprobar en dispositivo explica antes la instalación y usa node .devin/verify.cjs --device. Prioriza móvil autorizado USB/Wi-Fi; sin móvil crea/arranca solo TradeJob_Test. Con varios móviles pide elegir mediante ANDROID_SERIAL. Nunca borres datos ni desinstales para resolver conflictos de firma.
4. Ejecuta el flujo real de la SPEC, inspecciona capturas y comprueba criterios. Instalar/abrir no equivale a verificar un flujo. Usa ADB siempre con -s y el serial elegido.
5. Marca [x] solo criterios realmente verificados, [~] parciales con limitación y [ ] pendientes. Conserva evidencia en la spec según las reglas del proyecto. Firebase aislado: solo emulador, nunca móvil ni proyecto real.
6. Si falla, informa causa y alcance; no relajes controles ni marques done. Build + tests + flujo en dispositivo + evidencia son obligatorios.
