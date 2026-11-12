const fs = require('node:fs');
const path = require('node:path');
const net = require('node:net');
const { spawn, spawnSync } = require('node:child_process');

const avdName = 'TradeJob_Test';
const avdHome = path.join(__dirname, 'avd');
const artifacts = path.join(__dirname, 'artifacts');
const sleep = ms => new Promise(resolve => setTimeout(resolve, ms));

function parseDevices(text) {
    return text.split(/\r?\n/).map(line => line.trim().split(/\s+/))
        .filter(parts => parts.length > 1 && !['List', '*'].includes(parts[0]))
        .map(([serial, state]) => ({ serial, state }));
}

function chooseDevice(devices, requested) {
    if (requested) {
        const match = devices.find(device => device.serial === requested);
        if (!match || match.state !== 'device') throw new Error('ANDROID_SERIAL no está conectado/autorizado.');
        return match.serial;
    }
    const phones = devices.filter(device => !device.serial.startsWith('emulator-'));
    if (phones.some(device => device.state !== 'device')) {
        throw new Error('Hay un móvil sin conexión/autorización. Autoriza ADB o desconéctalo antes de continuar.');
    }
    if (phones.length > 1) throw new Error('Hay varias conexiones de móvil. Elige una con ANDROID_SERIAL.');
    return phones[0]?.serial || null;
}

function tools() {
    const sdk = process.env.ANDROID_HOME || process.env.ANDROID_SDK_ROOT ||
        path.join(process.env.LOCALAPPDATA || '', 'Android', 'Sdk');
    const adb = path.join(sdk, 'platform-tools', 'adb.exe');
    const emulator = path.join(sdk, 'emulator', 'emulator.exe');
    if (!fs.existsSync(adb)) throw new Error('No se encuentra adb.exe. Configura ANDROID_HOME con tu SDK Android.');
    return { sdk, adb, emulator };
}

function run(command, args) {
    const result = spawnSync(command, args, { encoding: 'utf8', timeout: 30_000, windowsHide: true });
    if (result.error) throw result.error;
    if (result.status !== 0) throw new Error(`${path.basename(command)}: ${result.stderr || result.stdout}`);
    return result.stdout.trim();
}

function createAvd(sdk) {
    const image = path.join(sdk, 'system-images', 'android-36.1', 'google_apis_playstore', 'x86_64');
    if (!fs.existsSync(path.join(image, 'system.img'))) {
        throw new Error('Falta la imagen Android 36.1 Google Play x86_64. Pide autorización antes de descargarla.');
    }
    const directory = path.join(avdHome, `${avdName}.avd`);
    fs.mkdirSync(directory, { recursive: true });
    const config = [
        `AvdId=${avdName}`, `avd.ini.displayname=${avdName}`, 'avd.ini.encoding=UTF-8',
        'PlayStore.enabled=true', 'abi.type=x86_64', 'hw.cpu.arch=x86_64',
        'hw.cpu.ncore=2', 'hw.ramSize=2048', 'hw.gpu.enabled=yes', 'hw.gpu.mode=auto',
        'hw.lcd.width=1080', 'hw.lcd.height=2400', 'hw.lcd.density=420',
        'hw.keyboard=yes', 'hw.mainKeys=no', 'hw.battery=yes', 'hw.gps=yes',
        'hw.camera.back=virtualscene', 'hw.camera.front=emulated',
        'disk.dataPartition.size=4G', 'vm.heapSize=256', 'showDeviceFrame=no',
        'image.sysdir.1=system-images/android-36.1/google_apis_playstore/x86_64/',
        'tag.id=google_apis_playstore', 'tag.display=Google Play', 'target=android-36.1',
        'fastboot.forceColdBoot=yes', 'fastboot.forceFastBoot=no',
    ].join('\n') + '\n';
    const configFile = path.join(directory, 'config.ini');
    const indexFile = path.join(avdHome, `${avdName}.ini`);
    if (!fs.existsSync(configFile)) fs.writeFileSync(configFile, config, { flag: 'wx' });
    if (!fs.existsSync(indexFile)) {
        fs.writeFileSync(indexFile, `avd.ini.encoding=UTF-8\npath=${directory}\ntarget=android-36.1\n`, { flag: 'wx' });
    }
}

function portAvailable(port) {
    return new Promise(resolve => {
        const server = net.createServer();
        server.once('error', () => resolve(false));
        server.listen(port, '127.0.0.1', () => server.close(() => resolve(true)));
    });
}

async function selectDevice({ emulatorOnly = false } = {}) {
    const { sdk, adb, emulator } = tools();
    const devices = parseDevices(run(adb, ['devices']));
    const requested = process.env.ANDROID_SERIAL;
    if (emulatorOnly && requested && !requested.startsWith('emulator-')) {
        throw new Error('Esta operación requiere emulador, no móvil.');
    }
    const phone = chooseDevice(emulatorOnly ? devices.filter(d => d.serial.startsWith('emulator-')) : devices, requested);
    if (phone) return { serial: phone, adb };
    for (const device of devices.filter(d => d.state === 'device' && d.serial.startsWith('emulator-'))) {
        const name = parseAvdName(run(adb, ['-s', device.serial, 'emu', 'avd', 'name']));
        if (name === avdName) return { serial: device.serial, adb };
    }
    if (!fs.existsSync(emulator)) throw new Error('No se encuentra emulator.exe en el SDK.');
    createAvd(sdk);
    let port;
    for (let candidate = 5560; candidate <= 5680; candidate += 2) {
        if (!devices.some(d => d.serial === `emulator-${candidate}`) &&
            await portAvailable(candidate) && await portAvailable(candidate + 1)) {
            port = candidate;
            break;
        }
    }
    if (!port) throw new Error('No hay puertos libres para TradeJob_Test.');
    fs.mkdirSync(artifacts, { recursive: true });
    const output = fs.openSync(path.join(artifacts, 'emulator.log'), 'a');
    const child = spawn(emulator, ['-avd', avdName, '-port', String(port), '-no-snapshot', '-no-boot-anim', '-gpu', 'software'], {
        env: { ...process.env, ANDROID_AVD_HOME: avdHome },
        detached: true, stdio: ['ignore', output, output], windowsHide: false,
    });
    fs.closeSync(output);
    await new Promise((resolve, reject) => {
        child.once('spawn', resolve);
        child.once('error', reject);
    });
    child.unref();
    const serial = `emulator-${port}`;
    const deadline = Date.now() + 240_000;
    while (Date.now() < deadline) {
        if (child.exitCode !== null) throw new Error(`El emulador ha terminado (código ${child.exitCode}). Consulta .devin/android/artifacts/emulator.log.`);
        const boot = spawnSync(adb, ['-s', serial, 'shell', 'getprop', 'sys.boot_completed'], {
            encoding: 'utf8', timeout: 5000, windowsHide: true,
        });
        if (boot.status === 0 && boot.stdout.trim() === '1') return { serial, adb };
        await sleep(2000);
    }
    throw new Error('El arranque no se ha confirmado. Consulta .devin/android/artifacts/emulator.log; no se borra ningún dato.');
}

function parseAvdName(text) {
    return text.trim().split(/[\r\n]+/)[0].trim();
}

module.exports = { parseDevices, chooseDevice, selectDevice, parseAvdName };
if (require.main === module) {
    const flags = process.argv.slice(2);
    if (flags.some(flag => flag !== '--emulator-only')) {
        console.error('Uso: node .devin/android/device.cjs [--emulator-only]');
        process.exitCode = 1;
    } else {
        selectDevice({ emulatorOnly: flags.includes('--emulator-only') })
            .then(({ serial }) => console.log(`DISPOSITIVO: ${serial}`))
            .catch(error => { console.error(error.message); process.exitCode = 1; });
    }
}
