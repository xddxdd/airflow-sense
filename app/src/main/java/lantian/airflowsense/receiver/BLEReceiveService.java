package lantian.airflowsense.receiver;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import lantian.airflowsense.Common;

public class BLEReceiveService extends Service {
    public static boolean RUNNING = false;
    private static boolean CONNECTED = false;
    BluetoothManager btManager;
    BluetoothAdapter btAdapter;
    BluetoothGatt btGatt;
    BluetoothLeScanner bleScanner;
    UUID bleServiceCharacteristic = convertFromInteger(0x3c22);
    UUID bleValueCharacteristic = convertFromInteger(0xb018);
    UUID clientConfigCharacteristic = convertFromInteger(0x2902);
    ScanCallback bleScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            BluetoothDevice device = result.getDevice();

//            Log.i("BLE Device Addr", device.getAddress());
            if (null == device.getName()) return;
            if (device.getName().startsWith("BloodPressureBP")) {
                Log.i("BLE Device", device.getName());
                bleScanner.stopScan(bleScanCallback);
                btGatt = device.connectGatt(getApplicationContext(), true, bleCallback);
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            if (errorCode == 1) return;
            Toast.makeText(BLEReceiveService.this, "BLE connect failed: " + errorCode, Toast.LENGTH_SHORT).show();
        }
    };
    private Handler handler = new Handler();
    BluetoothGattCallback bleCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            if (!RUNNING) {
                gatt.disconnect();
                return;
            }
            if (status != BluetoothGatt.GATT_SUCCESS) return;
            if (newState == BluetoothGatt.STATE_CONNECTED) {
                CONNECTED = true;
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent();
                        intent.setAction(Common.Action.BROADCAST_CONNECTION_STATUS_UPDATE);
                        intent.putExtra(Common.PacketParams.CONNECTIVITY, true);
                        intent.putExtra("name", "蓝牙");
                        sendBroadcast(intent);
                    }
                });
                gatt.discoverServices();
//                gatt.readCharacteristic(bleCharacteristic);
//                gatt.setCharacteristicNotification(bleCharacteristic, true);
            } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                CONNECTED = false;
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent();
                        intent.setAction(Common.Action.BROADCAST_CONNECTION_STATUS_UPDATE);
                        intent.putExtra(Common.PacketParams.CONNECTIVITY, false);
                        intent.putExtra("name", "蓝牙");
                        sendBroadcast(intent);
                    }
                });
                btGatt = null;
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            if (!RUNNING) {
                gatt.disconnect();
                return;
            }
            BluetoothGattService service = gatt.getService(bleServiceCharacteristic);
            if (null == service) return;
            BluetoothGattCharacteristic gattCharacteristic = service.getCharacteristic(bleValueCharacteristic);
            if (null == gattCharacteristic) return;
            gatt.setCharacteristicNotification(gattCharacteristic, true);

//            handler.post(new Runnable() {
//                @Override
//                public void run() {
//                    Toast.makeText(BLEReceiveService.this, gattCharacteristic.getUuid().toString(), Toast.LENGTH_SHORT).show();
//                }
//            });

            final BluetoothGattDescriptor descriptor = gattCharacteristic.getDescriptor(clientConfigCharacteristic);
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            if (!gatt.writeDescriptor(descriptor)) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(BLEReceiveService.this, "set notification fail", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor,
                                      final int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
            if (!RUNNING) {
                gatt.disconnect();
                return;
            }

            if (status != BluetoothGatt.GATT_SUCCESS) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(BLEReceiveService.this, "descriptor write fail " + status, Toast.LENGTH_SHORT).show();
                    }
                });
            }
            if (descriptor.getValue() == BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE) {
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
                if (!gatt.writeDescriptor(descriptor)) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(BLEReceiveService.this, "set indication fail", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            if (!RUNNING) {
                gatt.disconnect();
                return;
            }

//            handler.post(new Runnable() {
//                @Override
//                public void run() {
//                    Toast.makeText(BLEReceiveService.this, "changed " + characteristic.getUuid().toString(), Toast.LENGTH_SHORT).show();
//                }
//            });
            update(characteristic);
        }

        private void update(BluetoothGattCharacteristic characteristic) {
            final byte[] data = characteristic.getValue();

            handler.post(new Runnable() {
                @Override
                public void run() {
                    int value = ByteBuffer.wrap(data, 4, 2).getShort();
                    double new_value = ((double) value) / 219;
                    Intent intent = new Intent();
                    intent.setAction(Common.Action.BROADCAST_DATA_UPDATE);
                    intent.putExtra(Common.PacketParams.NEW_VALUE, new_value);
                    sendBroadcast(intent);
                }
            });
        }
    };

    public static UUID convertFromInteger(int i) {
        final long MSB = 0x0000000000001000L;
        final long LSB = 0x800000805f9b34fbL;
        long value = i & 0xFFFFFFFF;
        return new UUID(MSB | (value << 32), LSB);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(getClass().getSimpleName(), "start");

        CONNECTED = false; // Initialize the connection state to false

        btManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        if (btManager == null){
            Log.e(getClass().getSimpleName(), "Bluetooth Manager not found");
            Toast.makeText(this, "Bluetooth Manager not found", Toast.LENGTH_SHORT).show();
            return Service.START_NOT_STICKY;
        }

        btAdapter = btManager.getAdapter();
        if (btAdapter == null || !btAdapter.isEnabled()) {
            Log.e(getClass().getSimpleName(), "Bluetooth not enabled");
            Toast.makeText(this, "Bluetooth not enabled", Toast.LENGTH_SHORT).show();
            return Service.START_NOT_STICKY;
        }

        bleScanner = btAdapter.getBluetoothLeScanner();
        List<ScanFilter> bleFilter = new ArrayList<>();
        ScanSettings bleSettings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build();
        bleScanner.startScan(bleFilter, bleSettings, bleScanCallback);

        RUNNING = true;
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        RUNNING = false;
        CONNECTED = false;
        bleScanner.stopScan(bleScanCallback);
        handler.post(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent();
                intent.setAction(Common.Action.BROADCAST_CONNECTION_STATUS_UPDATE);
                intent.putExtra(Common.PacketParams.CONNECTIVITY, false);
                intent.putExtra("name", "蓝牙");
                sendBroadcast(intent);
            }
        });
        if (null != btGatt) {
            btGatt.disconnect();
        }
    }

    public static boolean isConnected(){
        return RUNNING && CONNECTED;
    }
}
