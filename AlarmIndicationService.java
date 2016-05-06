package bachelor.vaegtregistreringaffysiskbelastning.GitRepository;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.Vibrator;

public class AlarmIndicationService extends Service {

    //TODO test this.
    private final SharedPreferences sharedPref = getSharedPreferences("vaegtRegistreringPrefs", Context.MODE_PRIVATE);

    //Broadcast receiver for BT data
    @Override
    public void onCreate() {

        //int threshold = 50;

        IntentFilter filter = new IntentFilter("com.vaegtregistreringaffysiskbelasting.BroadcastBTData");
        this.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {


                int threshold = sharedPref.getInt("vaegt",0);

                if(intent.getIntArrayExtra("Data") != null){

                    int total = 0;
                  for(int i : intent.getIntArrayExtra("Data")){
                      total+=i;
                  }
                    if(total >= threshold){
                        alarm();
                    }

                }

            }
        }, filter);
    }

    private void alarm(){
        // If the alarm is enabled
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

                //TODO test lights
                PowerManager powerMan = (PowerManager) getSystemService(Context.POWER_SERVICE);
                final PowerManager.WakeLock wakeLock = powerMan.newWakeLock(
                        PowerManager.SCREEN_DIM_WAKE_LOCK |
                                PowerManager.ACQUIRE_CAUSES_WAKEUP, "wakelockTag");

                new Thread() {
                    public void run() {
                        boolean screenOn = false;
                        for (int i = 0; i < 5; i++) {
                            try {
                                sleep(500);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            if (screenOn) {
                                wakeLock.acquire();
                            } else {
                                wakeLock.release();
                            }
                        }
                    }
                }.run();

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
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }


}
