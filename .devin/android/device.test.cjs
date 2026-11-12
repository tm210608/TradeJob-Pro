const { test } = require('node:test');
const assert = require('node:assert/strict');
const { parseDevices, chooseDevice, parseAvdName } = require('./device.cjs');

test('interpreta nombre AVD con retornos de carro duplicados de ADB Windows', () => {
    assert.equal(parseAvdName('TradeJob_Test\r\r\nOK\r\r\n'), 'TradeJob_Test');
});

const emulator = { serial: 'emulator-5556', state: 'device' };
const usb = { serial: 'phone-usb', state: 'device' };
const wifi = { serial: '192.0.2.1:5555', state: 'device' };

test('interpreta ADB sin cabeceras ni mensajes del daemon', () => {
    assert.deepEqual(parseDevices('List of devices attached\r\n* daemon started successfully\r\nphone-usb\tdevice\r\nemulator-5556\toffline\r\n'),
        [usb, { serial: 'emulator-5556', state: 'offline' }]);
});
test('sin dispositivos solicita emulador propio', () => {
    assert.equal(chooseDevice([]), null);
});
test('no selecciona un emulador ajeno automáticamente', () => {
    assert.equal(chooseDevice([emulator]), null);
});
test('prioriza móvil USB', () => {
    assert.equal(chooseDevice([emulator, usb]), usb.serial);
});
test('prioriza móvil Wi-Fi', () => {
    assert.equal(chooseDevice([emulator, wifi]), wifi.serial);
});
test('dos conexiones requieren selección explícita', () => {
    assert.throws(() => chooseDevice([usb, wifi]), /varias conexiones/);
});
test('móvil no autorizado no se ignora', () => {
    assert.throws(() => chooseDevice([{ ...usb, state: 'unauthorized' }, emulator]), /autoriza/i);
});
test('móvil offline no se ignora', () => {
    assert.throws(() => chooseDevice([{ ...wifi, state: 'offline' }]), /sin conexión/);
});
test('ANDROID_SERIAL selecciona una conexión exacta', () => {
    assert.equal(chooseDevice([usb, wifi, emulator], wifi.serial), wifi.serial);
});
test('ANDROID_SERIAL inexistente falla sin cambiar a otro dispositivo', () => {
    assert.throws(() => chooseDevice([usb], 'otro'), /ANDROID_SERIAL/);
});
test('ANDROID_SERIAL offline falla', () => {
    assert.throws(() => chooseDevice([{ ...emulator, state: 'offline' }], emulator.serial), /ANDROID_SERIAL/);
});
test('permite elegir emulador explícitamente', () => {
    assert.equal(chooseDevice([usb, emulator], emulator.serial), emulator.serial);
});
