package bachelor.vaegtregistreringaffysiskbelastning.GitRepository;

import android.annotation.TargetApi;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import java.util.List;
import java.util.UUID;

// Adapted from Source: http://developer.android.com/guide/topics/connectivity/bluetooth-le.html 12-04-16
// Which is an example from the official android API site.

public class BluetoothService extends Service {


// Tag for the info console.
    private final static String TAG = BluetoothService.class.getSimpleName();

    private static final String WEIGHT_SERVICE = "f00d";

    private static final String FRONT_SENSOR = "beef";
    private static final String BACK_SENSOR = "ceef";
    private static final String LEFT_SENSOR = "deef";
    private static final String RIGHT_SENSOR = "feef";

    // BLE-button broadcast
    private final static String BLE_BROADCAST = "com.vaegtregistreringaffysiskbelasting.BLEScan";

    private static String nextInRow = "";

    private BluetoothGattService vaegtService = null; // The service with the UUID segment in SERVICE
    private static final long SCAN_TIME = 20000; //In milliseconds
    private Handler mHandler; // the handler that will stop the scan after SCAN_TIME ms
    private BluetoothAdapter mBluetoothAdapter; //Where we start Bluetooth LEscans from.
    private boolean mScanning; // If we are mid-scan or not.
    private BluetoothGatt mBluetoothGatt;
    private BluetoothManager bluetoothManager;

    // Used for the broadcasts
    private int[] recentSensorValues = new int[5];

    final Handler h = new Handler();

    private static boolean gattConnected;

    // For broadcasting Intents:
    public final static String ACTION_DATA_AVAILABLE =
            "com.vaegtregistreringaffysiskbelastning.bluetooth.le.ACTION_DATA_AVAILABLE";
    // This is for broadcasting with extra data attached.
    public final static String EXTRA_DATA = "com.vaegtregistreringaffysiskbelastning.bluetooth.le.EXTRA_DATA";

    private BroadcastReceiver BLEScanReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "BluetoothLEScanRequest Received: " + intent.toString());

            scanLeDevice();
        }
    };

    // Initializes Bluetooth manager.

    @Override
    public void onCreate() {

        bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        // Register receiver for getting updates on bluetooth devices
        IntentFilter filter = new IntentFilter(BLE_BROADCAST);
        this.registerReceiver(BLEScanReceiver, filter);
        scanLeDevice();
    }

    private void scanLeDevice() {
        // Stops scanning after a pre-defined scan period.
        // the use of this method over the
        // API 21 startScan(List,ScanSettings,ScanCallback)
        // is deliberate. api 21 is still new, and most phones won't run it.
        mHandler = new Handler();

    if(!mScanning) {
        mBluetoothAdapter.startLeScan(mLeScanCallback);
        mScanning = true;
        Log.d(TAG, "scanningLeDevice: ");
        Toast.makeText(getBaseContext(),"BLE scanning...",Toast.LENGTH_SHORT).show();
    }
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(mScanning!= false) {
                    mScanning = false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    Toast.makeText(getBaseContext(), "BLE scan ended", Toast.LENGTH_SHORT).show();
                }
            }
        }, SCAN_TIME);
    }

    // The handler where the found objects are called back to.
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, int rssi,
                                     byte[] scanRecord) {
                    //Toast.makeText(getBaseContext(),""+device+rssi+"",Toast.LENGTH_LONG).show();

                    Intent intent=new Intent(getApplicationContext(),BluetoothService.class);
                    intent.setAction("com.vaegtregistreringaffysiskbelasting.BroadcastNewDevice");
                    intent.putExtra("device", device);
                    intent.putExtra("rssi", rssi);
                    sendBroadcast(intent);
                    Log.d(TAG, "onLeScan: " + device.getAddress() + " name: " + device.getName());

                    //Connect to Footsensor
                    if(device.getName() != null) {
                        if (device.getName().equals("Footsensor") && !gattConnected) {
                            gattConnected = true; //TODO
                            mBluetoothAdapter.stopLeScan(mLeScanCallback);
                            if(!gattConnected) mBluetoothGatt = device.connectGatt(BluetoothService.this, false, mGattCallback);
                            Toast.makeText(getBaseContext(),"BLE scan ended",Toast.LENGTH_SHORT).show();
                            mScanning = false;
                        }
                    }
                }
            };
    private final BluetoothGattCallback mGattCallback =
            new BluetoothGattCallback() {

                public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                    if (newState == BluetoothProfile.STATE_CONNECTED) {
                        gattConnected = true;
                        Log.i(TAG, "Connected to the GATT server.");
                        // List the services in the console
                        Log.i(TAG, "Attempting to start service discovery:" +
                                mBluetoothGatt.discoverServices());
                    }
                    if (newState == BluetoothProfile.STATE_DISCONNECTED){
                        gattConnected = false;
                        Log.i(TAG, "Disconnected from GATT server.");
                    }
                }
                @Override
                // A new bluetooth GATT service has been discovered
                public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        Log.d(TAG, "onServicesDiscovered: " + gatt.getServices().size());

                        // Loop through the services, find and save WEIGHT_SERVICE to vaegtService
                        for (BluetoothGattService gattserv : gatt.getServices()) {

                            List<BluetoothGattCharacteristic> gattCharacteristics =
                                    gattserv.getCharacteristics();

                            Log.d(TAG, "onServicesDiscovered, Service: " + gattserv.getUuid() + " gattCharacteristics length: " + gattCharacteristics.size());
                            if (gattserv.getUuid().toString().contains(WEIGHT_SERVICE)) {
                                Log.d(TAG, "onServicesDiscovered: Contains WEIGHT_SERVICES");

                                //Set the vaegtService for reference in the descriptorWrite
                                vaegtService = gattserv;

                                // Loop through the characteristics of WEIGHT_SERVICE
                                for (BluetoothGattCharacteristic gattCha : vaegtService.getCharacteristics()) {
                                    //Log.d(TAG, "onServicesDiscovered, service, FORCE CONNECT deef, descriptor  size: " + vaegtService.getCharacteristic(UUID.fromString("0000deef-1212-efde-1523-785fef13d123")).getDescriptors().size());
                                    Log.d(TAG, "onServicesDiscovered, Service, Characteristics: " + gattCha.getUuid() + " Desc: " + gattCha.getDescriptor(gattCha.getUuid()));

                                    // If the characteristic contains the FRONT uuid, set the notification.
                                    if (gattCha.getUuid().toString().contains(FRONT_SENSOR)) {
                                        nextInRow = BACK_SENSOR; // Set the next UUID to be BACK_SENSOR
                                        //Log.d(TAG, "onServicesDiscovered, Service, Characteristics reading...: " + gatt.readCharacteristic(gattCha));
                                        mBluetoothGatt.setCharacteristicNotification(gattCha, true);

                                        // Iterate through all the descriptors in the characteristic. We need to set ENABLE_NOTIFICATION_VALUE which is 0x2902:
                                        for (BluetoothGattDescriptor descriptor : gattCha.getDescriptors()) {
                                            Log.d(TAG, "onServicesDiscovered: Characteristics, Descriptor: " + descriptor.getUuid());
                                            // If the descriptor is the place to write ENABLE_NOTIFICATION_VALUE
                                            if (descriptor.getUuid().toString().contains("2902")) {
                                                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                                                Log.d(TAG, "onServicesDiscovered: Descriptor written: " + mBluetoothGatt.writeDescriptor(descriptor));
                                            }
                                        }
                                    }
                                }

                            }
                        }
                    }
                    else Log.d(TAG, "onServicesDiscovered: " + status);
                }
                @Override
                public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status){

                    Log.d(TAG, "onDescriptorWrite triggered! this time: " +nextInRow);
                        // If the characteristic contains the FRONT uuid, set the notification.
                    if(nextInRow.length() != 0) {
                        for (BluetoothGattCharacteristic gattCha : vaegtService.getCharacteristics()) {
                            if (gattCha.getUuid().toString().contains(nextInRow)) { // If nextInRow contains a string and the characteristic matches it.
                                mBluetoothGatt.setCharacteristicNotification(gattCha, true); // Enable "we want to get notifications" locally.
                                // Set the next sensor to setup
                                switch (nextInRow) {
                                    case BACK_SENSOR:
                                        nextInRow = LEFT_SENSOR;

                                        break;
                                    case LEFT_SENSOR:
                                        nextInRow = RIGHT_SENSOR;
                                        break;
                                    case RIGHT_SENSOR:
                                            nextInRow = "";
                                        break;
                                }
                                Log.d(TAG, "onDescriptorWrite: next in row: " + nextInRow);
                                    // Iterate through all the descriptors in the characteristic. We need to set ENABLE_NOTIFICATION_VALUE which is 0x2902:
                                    for (BluetoothGattDescriptor descriptorTemp : gattCha.getDescriptors()) {
                                        Log.d(TAG, "onDescriptorWrite: Characteristics, Descriptor: " + descriptorTemp.getUuid());
                                        // If the descriptor is the place to write ENABLE_NOTIFICATION_VALUE
                                        if (descriptorTemp.getUuid().toString().contains("2902")) {
                                            descriptorTemp.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);

                                            final BluetoothGattDescriptor d = descriptorTemp;

                                            h.postDelayed(new Runnable() {
                                                @Override
                                                public void run() {
                                                    if (mScanning != false) {
                                                        Log.d(TAG, "onDescriptorWrite: Descriptor written: " + mBluetoothGatt.writeDescriptor(d));
                                                    }
                                                }
                                            }, 100);

                                        }
                                    }
                            }
                        }
                    }
                }

                @Override
                // Characteristics from a GATT service has been read
                public void onCharacteristicRead(BluetoothGatt gatt,
                                                 BluetoothGattCharacteristic characteristic,
                                                 int status) {
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
                        Log.d(TAG, "onServicesDiscovered, Service," + characteristic.getUuid() + " Val: " + characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT32,0) );
                    }
                }

                // When notfied of change
                @Override
                public void onCharacteristicChanged (BluetoothGatt gatt, BluetoothGattCharacteristic characteristic){
                    Log.d(TAG, "onCharacteristicChanged(" + characteristic.getUuid() +"): " + characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT32,0));
                }

            };

    // Broadcasts an update over the Intent API. This is for detection by activities and other services.
    private void broadcastUpdate(final String action, final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);

        // Reads the value of the characteristic and formats it
        byte[] data = characteristic.getValue();
        if(data != null && data.length > 0) {// basically, if there's data at all
            Log.d(TAG, "broadcastUpdate: " + data);

            String characteristicUUID = characteristic.getUuid().toString();

            try {
                if (characteristicUUID.contains(FRONT_SENSOR)) {
                    recentSensorValues[1] = Integer.parseInt(data.toString());
                }
                if (characteristicUUID.contains(RIGHT_SENSOR)) {
                    recentSensorValues[2] = Integer.parseInt(data.toString());
                }
                if (characteristicUUID.contains(BACK_SENSOR)) {
                    recentSensorValues[3] = Integer.parseInt(data.toString());
                }
                if (characteristicUUID.contains(LEFT_SENSOR)) {
                    recentSensorValues[4] = Integer.parseInt(data.toString());
                }
            }catch (Exception e){
                Log.e(TAG, "broadcastUpdate ERROR: Failed to cast data to int");
            }
            intent.putExtra(EXTRA_DATA, recentSensorValues);
            sendBroadcast(intent);
        } else Log.d(TAG, "broadcastUpdate: " + "No data");
    }

    @Override
    public void onDestroy(){
        Log.d(TAG, "Bluetooth Service Destroyed!");
        unregisterReceiver(BLEScanReceiver);
        super.onDestroy();
    }

    // Binder given to clients
    public final IBinder bluetoothBinder = new LocalBinder();

    public class LocalBinder extends Binder {
        public BluetoothService getService(){
            return BluetoothService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // Returns the communication channel to the service.
        return bluetoothBinder;
    }

}