package bachelor.vaegtregistreringaffysiskbelastning.GitRepository;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ActionProvider;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.w3c.dom.Text;

import bachelor.vaegtregistreringaffysiskbelastning.R;

public class MainActivity extends ActionBarActivity {

    // This is the broadcast from BluetoothService.
    public final static String ACTION_DATA_AVAILABLE =
            "com.vaegtregistreringaffysiskbelastning.bluetooth.le.ACTION_DATA_AVAILABLE";
    // This is the integer array in the broadcast from BluetoothService.
    public final static String EXTRA_DATA = "com.vaegtregistreringaffysiskbelastning.bluetooth.le.EXTRA_DATA";


    private BroadcastReceiver vaegtReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if(intent.getIntArrayExtra(EXTRA_DATA) != null){

                int total = 0;
                int counter = 0;
                for (int i : intent.getIntArrayExtra(EXTRA_DATA)) {

                    total+=i;
                    updateText(i, counter);
                    counter++;
                }
                updateText(total,5);

            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        resetText();
        // start bluetooth service
        startService(new Intent(this, BluetoothService.class));
        // Start AlarmIndicationService
        startService(new Intent(this, AlarmIndicationService.class));

         Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getBaseContext(), InstillingerActivity.class);
                startActivity(intent);
            }
        });

        IntentFilter filter = new IntentFilter(ACTION_DATA_AVAILABLE);
        this.registerReceiver(vaegtReceiver, filter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    // Reset all the sensor values (setting them to 0)
    private void resetText(){
        int i = 1;
        while (i <= 4){
            updateText(0,i);
            i++;
        }
    }

    private void updateText(int value, int sensor){

        TextView backSensor = (TextView) findViewById(R.id.textViewBackSensor);
        TextView frontSensor =(TextView) findViewById(R.id.textViewFrontSensor);
        TextView leftSensor = (TextView) findViewById(R.id.textViewLeftSensor);
        TextView rightSensor = (TextView) findViewById(R.id.textViewRightSensor);


        // They go clockwise, starting with 12 o clock being the front, 3 equals right, 6 is back and 9 is left. Hence the order!

        // Deliberately chosen not to use an ENUM, due to the TWICE as large memory consumption. It's officially recommended not to use it in Android!
        switch (sensor) {
            case 1:
                frontSensor.setText(String.valueOf(value));
                break;
            case 2:
                rightSensor.setText(String.valueOf(value));
                break;
            case 3:
                backSensor.setText(String.valueOf(value));
                break;
            case 4:
                leftSensor.setText(String.valueOf(value));
                break;
        }

        try{ // Try to add all the values together and insert it into the "total" field

            TextView totalSensors = (TextView) findViewById(R.id.textViewTotal);

            // Add values together.
            int total = Integer.parseInt(backSensor.getText().toString()) +
                    Integer.parseInt(frontSensor.getText().toString()) +
                    Integer.parseInt(leftSensor.getText().toString()) +
                    Integer.parseInt(rightSensor.getText().toString());

            //Set the text in the "total" field.
            totalSensors.setText(String.valueOf(total));

        }catch (NumberFormatException nfe){
            Log.e("MainActivity", "updateText ERROR caught: NUMBER FORMAT EXCEPTION. CENTER TEXT NOT UPDATED!");
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(getBaseContext(), InstillingerActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
