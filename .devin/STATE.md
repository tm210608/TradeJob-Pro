# Estado de TradeJob Pro

Fecha: 2026-11-12 | Rama: feature/sdmd-project-setup | Base: 71821c1
Commit de cierre: consultar git log -1; guardado local, sin merge ni push.

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
- Arranque frío VERIFICADO el 12-11 en emulator-5560: test OK sin precompilar,
  app COLD en 6,1 s, login limpio (spec-007-login-coldboot.png). EVIDENCE y
  SPEC actualizados; cambios sin commit.
- Planificador VERIFICADO: SyncWorker ENQUEUED en JobScheduler real (red, ~1h,
  backoff 1m); run forzado llega al proceso y se difiere correctamente.
- Pendiente: commit de la actualización (si se autoriza). Solo queda sin
  verificar el round trip HTTP de Storage local (limitación SDK) y sync real horaria.
- No integrar ramas ni publicar sin autorización.

## Decisiones y dispositivo
- Móvil USB/Wi-Fi autorizado prioritario; varias conexiones requieren ANDROID_SERIAL.
- Sin móvil usar solo TradeJob_Test; emulator-5560 quedó arrancado sin ventana,
  con GPU host, 4 CPU y 4096 MB (opciones de esta ejecución, no cambios al selector).
- emulator-5556 es de SocialChef: intacto. Sin móvil físico ni datos borrados.
- No cambiar producción, desinstalar, commit, merge ni push sin petición.
- Firebase aislado nunca en móvil/proyecto real; planificador y round trip HTTP local pendientes.
- Recargar sesión si una skill homónima se resuelve desde otro proyecto.
