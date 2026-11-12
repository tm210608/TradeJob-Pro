# Estado de TradeJob Pro

Fecha: 2026-11-12 | Rama: feature/spec-008-login-tests | Base: main publicado
Merge autorizado: SPEC-007 + SDMD integrados en main junto a commits remotos
(iconos/build config/temas); conflicto de iconos resuelto conservando los
.webp de marca. Verificacion post-merge: build + 17 tests OK.
CI: faltaba gradlew unix -> generado wrapper completo + JDK 21 + eol=lf.
Android CI run #9 OK en 5m28s (runs #6-8 fallaban por chmod gradlew).

## Hecho
- SDMD configurado en .devin/skills/; estado aquí, historial/roadmap en PROGRESS.md.
- App y APK de pruebas compilan; node .devin/verify.cjs termina con código 0.
- Continuación: 17 tests home ejecutados con --rerun, 0 fallos/errores/omitidos; login NO-SOURCE.
- Selector: 13 tests Node verdes en la verificación; prioridad USB/Wi-Fi y AVD exclusivo.
- Firebase aislado: 2/2 tests correctos en la fase previa de esta sesión, no repetidos ahora.
- WorkManagerInitializationTest: OK (1 test) tras precompilar APK app y APK tests en el emulador.
- Login observado: am start -W Status: ok; captura tras Esperar de System UI sin aviso.

## Incidencias de esta continuación
- Varios intentos instrumentados dieron Process crashed por ANR al arrancar.
- Trazas: Android descomprimiendo/verificando DEX, elevada presión de CPU; no llegó al test.
- GPU host/4 CPU/4096 MB no bastó; cmd package compile -m speed -f de ambos APK dio Success.
- No se ha validado arranque frío sin precompilar. También apareció ANR de System UI.
- Logs y capturas en .devin/android/artifacts/, ignorados por Git; detalles en PROGRESS.md.

## Siguiente paso
- SPEC-008 login-tests VERIFICADA en rama feature/spec-008-login-tests:
  22 tests nuevos verdes, verify.cjs OK, login idéntico en emulador.
  SPEC+EVIDENCE en specs/008-login-tests/. Cambios sin commit.
- Pendiente: commit/merge/push solo con autorización.
- Cola: db-migration-tests, ui-*, sync-completa, i18n-domain,
  password-length-mismatch (hallazgo SPEC-008), journey-login, cleanup-deps.

## Decisiones y dispositivo
- Móvil USB/Wi-Fi autorizado prioritario; varias conexiones requieren ANDROID_SERIAL.
- Sin móvil usar solo TradeJob_Test; emulator-5560 quedó arrancado sin ventana,
  con GPU host, 4 CPU y 4096 MB (opciones de esta ejecución, no cambios al selector).
- emulator-5556 es de SocialChef: intacto. Sin móvil físico ni datos borrados.
- No cambiar producción, desinstalar, commit, merge ni push sin petición.
- Firebase aislado nunca en móvil/proyecto real; planificador y round trip HTTP local pendientes.
- Recargar sesión si una skill homónima se resuelve desde otro proyecto.
