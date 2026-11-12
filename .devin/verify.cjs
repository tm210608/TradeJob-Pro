const fs = require('node:fs');
const path = require('node:path');
const { spawnSync } = require('node:child_process');
const { selectDevice } = require('./android/device.cjs');

const root = path.resolve(__dirname, '..');
const artifacts = path.join(__dirname, 'android', 'artifacts');
const log = path.join(artifacts, 'verify.log');

function execute(command, args, env = process.env) {
    const fd = fs.openSync(log, 'a');
    let result;
    try {
        result = spawnSync(command, args, { cwd: root, env, stdio: ['ignore', fd, fd], timeout: 900_000 });
    } finally {
        fs.closeSync(fd);
    }
    if (result.error) throw result.error;
    if (result.status !== 0) throw new Error(`${path.basename(command)} ha fallado (código ${result.status}).`);
}

async function main() {
    const flags = process.argv.slice(2);
    if (flags.includes('--help')) {
        console.log('Uso: node .devin/verify.cjs [--device] [--sync]');
        console.log('Sin opciones: build + tests. --device: instalar y abrir. --sync: Firebase aislado, solo emulador.');
        return;
    }
    if (flags.some(flag => !['--device', '--sync'].includes(flag))) throw new Error('Opción desconocida. Usa --help.');
    if (flags.includes('--sync') && flags.includes('--device')) throw new Error('Ejecuta --device y --sync por separado.');
    fs.mkdirSync(artifacts, { recursive: true });
    fs.writeFileSync(log, `TradeJob verify ${new Date().toISOString()}\n`);
    if (flags.includes('--sync')) {
        const { serial } = await selectDevice({ emulatorOnly: true });
        console.log(`FIREBASE AISLADO: ${serial}`);
        execute(process.env.ComSpec || 'cmd.exe', ['/d', '/c', 'npm --prefix .devin/firebase run test:sync'], {
            ...process.env, ANDROID_SERIAL: serial,
        });
        console.log('SYNC: OK (consulta el resultado detallado en el log)');
        console.log(`Log: ${path.relative(root, log)}`);
        return;
    }
    execute(process.execPath, ['--test', path.join(__dirname, 'android', 'device.test.cjs')]);
    console.log('HERRAMIENTAS: tests de selección correctos');
    console.log('Verificando compilación y tests de TradeJob...');
    execute(process.env.ComSpec || 'cmd.exe', ['/d', '/c',
        'gradlew.bat :app:assembleDebug :app:assembleDebugAndroidTest :home:testDebugUnitTest :login:testDebugUnitTest --console=plain']);
    const output = fs.readFileSync(log, 'utf8');
    console.log('BUILD: OK (app + APK de pruebas)');
    for (const module of ['home', 'login']) {
        const task = output.split(/\r?\n/).find(line => line.startsWith(`> Task :${module}:testDebugUnitTest`));
        console.log(`TESTS ${module}: ${task?.includes('NO-SOURCE') ? 'SIN TESTS' : task?.includes('UP-TO-DATE') ? 'OK, resultado reutilizado' : 'OK, ejecutados'}`);
    }
    if (flags.includes('--device')) {
        const { serial, adb } = await selectDevice();
        console.log(`DISPOSITIVO SELECCIONADO: ${serial}`);
        const env = { ...process.env, ANDROID_SERIAL: serial };
        execute(adb, ['-s', serial, 'install', '-r', path.join(root, 'app/build/outputs/apk/debug/app-debug.apk')], env);
        execute(adb, ['-s', serial, 'shell', 'am', 'start', '-W', '-n', 'com.tradejob.pro.app/.MainActivity'], env);
        const launch = fs.readFileSync(log, 'utf8');
        if (!/^Status: ok\s*$/m.test(launch) || /^Error:|^Failure /m.test(launch)) {
            throw new Error('La instalación/apertura no ha quedado confirmada; revisa el log.');
        }
        console.log('APP: instalada y apertura confirmada; falta comprobar el flujo de la SPEC.');
    }
    console.log(`Log: ${path.relative(root, log)}`);
}

main().catch(error => {
    console.error(`VERIFICACIÓN: FALLO — ${error.message}`);
    console.error(`Log (si se ha generado): ${log}`);
    process.exitCode = 1;
});
