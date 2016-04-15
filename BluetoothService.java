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
import android.util.Log;
import android.widget.Toast;

import java.util.UUID;

// Adapted from Source: http://developer.android.com/guide/topics/connectivity/bluetooth-le.html 12-04-16
// Which is an example from the official android API site.

public class BluetoothService extends Service {

// Tag for the info console.
    private final static String TAG = BluetoothService.class.getSimpleName();


    private static final long SCAN_TIME = 20000; //In milliseconds
    private Handler mHandler; // the handler that will stop the scan after SCAN_TIME ms
    private BluetoothAdapter mBluetoothAdapter; //Where we start Bluetooth LEscans from.
    private boolean mScanning; // If we are mid-scan or not. //TODO this
    private BluetoothGatt mBluetoothGatt;
    private BluetoothManager bluetoothManager;

    // For broadcasting Intents:
    public final static String ACTION_DATA_AVAILABLE =
            "com.vaegtregistreringaffysiskbelastning.bluetooth.le.ACTION_DATA_AVAILABLE";
    // This is for broadcasting with extra data attached.
    public final static String EXTRA_DATA = "com.vaegtregistreringaffysiskbelastning.bluetooth.le.EXTRA_DATA";



    // Initializes Bluetooth manager.

    // Constructer, not sure if we need it.
     public BluetoothService() {
    }

    @Override
    public void onCreate() {
        bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        // Register receiver for getting updates on bluetooth devices
        IntentFilter filter = new IntentFilter("com.vaegtregistreringaffysiskbelasting.BLEScan");
        this.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(TAG, "BluetoothLEScanRequest Received: " + intent.toString());
                //scanLeDevice();
            }
        }, filter);
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
                    intent.putExtra("rssi",rssi);
                    sendBroadcast(intent);
                    Log.d(TAG, "onLeScan: " + device.getAddress() + " name: " + device.getName());

                    // TODO TEMPORARY TEST, should be replaced with a broadcastReceiver to get which device to connect to from User
                    //Connect to Footsensor
                    if(device.getName() != null) {
                        if (device.getName().equals("Footsensor")) {
                            mBluetoothGatt = device.connectGatt(BluetoothService.this, false, mGattCallback);
                            mBluetoothAdapter.stopLeScan(mLeScanCallback);
                            Toast.makeText(getBaseContext(),"BLE scan ended",Toast.LENGTH_SHORT).show();
                            mScanning = false;
                        }
                    }
                }
            };
    private final BluetoothGattCallback mGattCallback =
            new BluetoothGattCallback() {
                @Override
                public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                    if (newState == BluetoothProfile.STATE_CONNECTED) {
                        Log.i(TAG, "Connected to the GATT server.");
                        // List the services in the console
                        Log.i(TAG, "Attempting to start service discovery:" +
                                mBluetoothGatt.discoverServices());
                    }
                    if (newState == BluetoothProfile.STATE_DISCONNECTED){
                        Log.i(TAG, "Disconnected from GATT server.");
                    }
                }
                @Override
                // A new bluetooth GATT service has been discovered
                public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        Log.d(TAG, "onServicesDiscovered: " + gatt.getServices().size());
                        // Loop through the services
                        for (BluetoothGattService gattserv : gatt.getServices()) {
                            Log.d(TAG, "onServicesDiscovered, Service: " + gattserv.getUuid() + "gattCharacteristics length: " + gattserv.getCharacteristics().size());
                            // Loop through the characteristics
                            for (BluetoothGattCharacteristic gattCha : gattserv.getCharacteristics()) {
                                Log.d(TAG, "onServicesDiscovered, Service, Characteristics: " + gattCha.getUuid() + " Desc: " + gattCha.getDescriptor(gattCha.getUuid()));
                                // If the characteristic contains "beef" (our UUID for it) then read it.
                                if(gattCha.getUuid().toString().contains("beef")) {
                                    Log.d(TAG, "onServicesDiscovered, Service, Characteristics reading...: " + gatt.readCharacteristic(gattCha));
                                    mBluetoothGatt.setCharacteristicNotification(gattCha, true);

                                    // Iterate through all the descriptors in the characteristic. We need to set ENABLE_NOTIFICATION_VALUE which is 0x2902:

                                    for( BluetoothGattDescriptor descriptor : gattCha.getDescriptors()){
                                        Log.d(TAG, "onServicesDiscovered: Characteristics, Descriptor: " + descriptor.getUuid());
                                        // If the descriptor is the place to write ENABLE_NOTIFICATION_VALUE
                                        if(descriptor.getUuid().toString().contains("2902")){
                                            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                                            mBluetoothGatt.writeDescriptor(descriptor);
                                        }
                                    }



                                }
                            }
                        } 
                        


                    }
                    else Log.d(TAG, "onServicesDiscovered: " + status);

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
                    Log.d(TAG, "onCharacteristicChanged(beef): " + characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT32,0));
                }

            };

    // Broadcasts an update over the Intent API. This is for detection by activities and other services.
    private void broadcastUpdate(final String action, final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);

        // Reads the value of the characteristic and formats it
        byte[] data = characteristic.getValue();
        if(data != null && data.length > 0) {// basically, if there's data at all
            Log.d(TAG, "broadcastUpdate: " + data);

            intent.putExtra(EXTRA_DATA, new String(data) + "\n"/* +
                    stringBuilder.toString()*/); //TODO more formatting here.
        } else Log.d(TAG, "broadcastUpdate: " + "No data");
        sendBroadcast(intent); // TODO, should we broadcast null and length 0 data? If not, move into IF statement
    }

    @Override
    public void onDestroy(){
        Log.d(TAG, "Bluetooth Service Destroyed!");

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
