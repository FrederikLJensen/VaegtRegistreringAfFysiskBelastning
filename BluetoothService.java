package bachelor.vaegtregistreringaffysiskbelastning.GitRepository;

import android.annotation.TargetApi;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.widget.Toast;

// Source: http://developer.android.com/guide/topics/connectivity/bluetooth-le.html#find
public class BluetoothService extends Service {

    private static final long SCAN_TIME = 10000; //In milliseconds
    private Handler mHandler; // the handler that will stop the scan after SCAN_TIME ms
    private BluetoothAdapter mBluetoothAdapter; //Where we start Bluetooth LEscans from.
    private boolean mScanning; // If we are mid-scan or not.


    // Initializes Bluetooth manager.
    final BluetoothManager bluetoothManager =
            (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);

     public BluetoothService() {

        mBluetoothAdapter = bluetoothManager.getAdapter();

    }

    private void scanLeDevice() {
        // Stops scanning after a pre-defined scan period.
        // the use of this method over the
        // API 21 startScan(List,ScanSettings,ScanCallback)
        // is deliberate. api 21 is still new, and most phones won't run it.
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mScanning = false;
                mBluetoothAdapter.startLeScan(mLeScanCallback);
            }
        }, SCAN_TIME);
    }

    // The handler where the found objects are called back to.
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, int rssi,
                                     byte[] scanRecord) {
                    Toast.makeText(getBaseContext(),""+device+rssi+"",Toast.LENGTH_LONG);
                }
            };







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
