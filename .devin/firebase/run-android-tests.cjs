const { spawnSync } = require('node:child_process');
const fs = require('node:fs');
const path = require('node:path');

const serial = process.env.ANDROID_SERIAL || 'emulator-5554';
if (!/^emulator-\d+$/.test(serial)) {
    throw new Error('These tests may only run on an Android emulator, not a physical device.');
}
const root = path.resolve(__dirname, '../..');
const sdk = process.env.ANDROID_HOME || path.join(process.env.LOCALAPPDATA, 'Android/Sdk');
const adb = path.join(sdk, 'platform-tools/adb.exe');
function run(command, args, capture = false) {
    const result = spawnSync(command, args, {
        cwd: root,
        encoding: 'utf8',
        stdio: capture ? 'pipe' : 'inherit',
        timeout: 600_000,
    });
    if (result.error) throw result.error;
    if (result.status !== 0) {
        throw new Error(`${command} failed (${result.status}): ${result.stderr || ''}`);
    }
    return result.stdout || '';
}
const devices = run(adb, ['devices'], true);
if (!devices.split(/\r?\n/).some(line => line.trim() === `${serial}\tdevice`)) {
    throw new Error(`Start Android emulator ${serial} before running these tests.`);
}
const forwardedPorts = [];
try {
    run(process.env.ComSpec || 'cmd.exe', ['/d', '/c', 'gradlew.bat :libs:database:assembleDebugAndroidTest --console=plain']);
    for (const port of [9099, 8080, 9199]) {
        run(adb, ['-s', serial, 'reverse', '--no-rebind', `tcp:${port}`, `tcp:${port}`]);
        forwardedPorts.push(port);
    }
    run(adb, ['-s', serial, 'install', '-r', 'libs/database/build/outputs/apk/androidTest/debug/database-debug-androidTest.apk']);
    const result = run(adb, ['-s', serial, 'shell', 'am', 'instrument', '-w', '-e', 'firebaseEmulators', 'true', '-e', 'class', 'com.tradejob.pro.database.sync.SyncWorkerEmulatorTest', 'com.tradejob.pro.database.test/androidx.test.runner.AndroidJUnitRunner'], true);
    process.stdout.write(result);
    fs.mkdirSync(path.join(__dirname, 'artifacts'), { recursive: true });
    fs.writeFileSync(path.join(__dirname, 'artifacts/android-sync-test.log'), result);
    if (!/OK \(2 tests\)/.test(result)) {
        throw new Error('Local Firebase integration tests failed. See artifacts/android-sync-test.log.');
    }
} finally {
    for (const port of forwardedPorts) {
        spawnSync(adb, ['-s', serial, 'reverse', '--remove', `tcp:${port}`], { stdio: 'inherit' });
    }
}
