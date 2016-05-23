package bachelor.vaegtregistreringaffysiskbelastning.GitRepository;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.Vibrator;
import android.util.Log;

public class AlarmIndicationService extends Service {

    //TODO test this.
    private SharedPreferences sharedPref;

    private final static String TAG = BluetoothService.class.getSimpleName();//Tag for logging

    public final static String ACTION_DATA_AVAILABLE =
            "com.vaegtregistreringaffysiskbelastning.bluetooth.le.ACTION_DATA_AVAILABLE";
    // Extra data for the bluetooth broadcast
    public final static String EXTRA_DATA = "com.vaegtregistreringaffysiskbelastning.bluetooth.le.EXTRA_DATA";

    private BroadcastReceiver vaegtReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            int threshold = sharedPref.getInt("vaegt",0);

            if(intent.getIntArrayExtra(EXTRA_DATA) != null){

                int total = 0;
                for(int i : intent.getIntArrayExtra(EXTRA_DATA)){
                    total+=i;
                }
                if(total >= threshold){
                    alarm();
                }

            }

        }
    };

    //Broadcast receiver for BT data
    @Override
    public void onCreate() {

        sharedPref = getSharedPreferences("vaegtRegistreringPrefs", Context.MODE_PRIVATE);
        //int threshold = 50;

        IntentFilter filter = new IntentFilter(ACTION_DATA_AVAILABLE);
        this.registerReceiver(vaegtReceiver, filter);
    }

    private void alarm(){
        // If the alarm is enabled

        // Send alarm broadcast. For changing colours in the MainActivity, possibly light alarm.
        Intent intent = new Intent("alarm");
        sendBroadcast(intent);

        if(sharedPref.getBoolean("switchAlarm",true)) {
            //Vibrate
            if (sharedPref.getBoolean("switchVibration", true)) {
                Vibrator v = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
                // Vibrate for 500 milliseconds
                v.vibrate(200);
            }
            //Light
            if (sharedPref.getBoolean("switchLight", true)) {

                //Source: http://stackoverflow.com/questions/14436103/how-to-make-screen-flashing-blinking-from-background-service-on-android-device 27/04-16
                //Source2: http://stackoverflow.com/questions/6068803/how-to-turn-on-camera-flash-light-programmatically-in-android
                //

                //TODO test lights
                PowerManager powerMan = (PowerManager) getSystemService(Context.POWER_SERVICE);
                final PowerManager.WakeLock wakeLock = powerMan.newWakeLock(
                        PowerManager.SCREEN_DIM_WAKE_LOCK |
                                PowerManager.ACQUIRE_CAUSES_WAKEUP, "wakelockTag");
                wakeLock.acquire();
                wakeLock.release();
                if(getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
                    new Thread() {
                        public void run() {
                            boolean screenOn = false;
                            for (int i = 0; i < 3; i++) {
                                Camera cam = Camera.open();
                                Camera.Parameters p = cam.getParameters();
                                p.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                                cam.setParameters(p);
                                cam.startPreview();
                                try {
                                    sleep(500);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                cam.stopPreview();
                                cam.release();
                            }
                        }
                    }.run();
                }
            }
            //Sound
            if (sharedPref.getBoolean("switchSound", true)) {
                //TODO test the sound
                Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
                r.play();
            }
        }
    }


    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "Alarm indication Service Destroyed!");
        unregisterReceiver(vaegtReceiver);
        super.onDestroy();
    }
}
