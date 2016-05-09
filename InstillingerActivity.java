package bachelor.vaegtregistreringaffysiskbelastning.GitRepository;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import bachelor.vaegtregistreringaffysiskbelastning.R;

public class InstillingerActivity extends AppCompatActivity {

    private final static String TAG = InstillingerActivity.class.getSimpleName();
    private SharedPreferences sharedPref;

    private ListView mBluetoothList;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> bluetoothDevices;
    private TextView t;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instillinger);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        sharedPref = getSharedPreferences("vaegtRegistreringPrefs", Context.MODE_PRIVATE);

// Setup the button
        Button button = (Button) findViewById(R.id.buttonBack);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getBaseContext(), MainActivity.class);
                startActivity(intent);
            }
        });

        t = (TextView) findViewById(R.id.textViewWeight); // get the textview to adjust our weight threshold
        //t.setClickable(true);
        t.setText(String.valueOf(sharedPref.getInt("vaegt",0)));
        t.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {

                    Log.d(TAG, "onEditorAction, Editor GetText: " + t.getText().toString());
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

                    //Save the value
                    sharedPref.edit().putInt("vaegt", Integer.parseInt(t.getText().toString())).apply();

                    return true;
                }
                return false;
            }
        });
        // Setup the switches.
        handleSwitches();
        // Enable bluetooth button
        handleBluetoothButton();

        // Register receiver for getting updates on bluetooth devices
        /*IntentFilter filter = new IntentFilter("com.vaegtregistreringaffysiskbelasting.BroadcastNewDevice");
        this.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                addBTItems(intent.getExtras().getString("device"));
            }
        }, filter);
        */

        // The visible list containing the found bluetooth devices
       /* mBluetoothList = (ListView)findViewById(R.id.listView);
        //Initialize bluetooth list
        bluetoothDevices = new ArrayList<String>();
        // Attach adapter to list
        adapter=new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1,
                bluetoothDevices);
        mBluetoothList.setAdapter(adapter);*/

        // When an item is selected
/*        mBluetoothList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Toast.makeText(getBaseContext(), "int i = " + i + ", long l = " + l, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });*/


    }

    // The listener for the switches
    CompoundButton.OnCheckedChangeListener changeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            Switch s = (Switch) compoundButton;
            String switchIdentifier = "";
            switch (s.getId()) {
                case R.id.switchAlarm:
                    switchIdentifier = "switchAlarm";
                    break;
                case R.id.switchLight:
                    switchIdentifier = "switchLight";
                    break;
                case R.id.switchSound:
                    switchIdentifier = "switchSound";
                    break;
                case R.id.switchVibration:
                    switchIdentifier = "switchVibration";
            }
            // Casting the compoundButton to a Switch, we're now putting the new value into the sharedPrefs
            if(!switchIdentifier.equals("")) {
                sharedPref.edit().putBoolean(switchIdentifier, b).apply();
                Log.d(TAG, "onCheckedChanged: " + switchIdentifier + " state: " + b + " gives: " + sharedPref.getBoolean(switchIdentifier, true));
            }
        }
    };

    // When the "Scan" button is pressed, a broadcast requesting a BLE scan is sent.
    private void handleBluetoothButton() {

        Button btButton = (Button) findViewById(R.id.buttonScan);

        btButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(getApplicationContext(),InstillingerActivity.class);
                intent.setAction("com.vaegtregistreringaffysiskbelasting.BLEScan");
                sendBroadcast(intent);
                Log.d(TAG, "Bluetooth/ScanButtonClick");
            }
        });

    }


    private void handleSwitches(){
        Switch switchAlarmOn = (Switch) findViewById(R.id.switchAlarm);
        Switch switchLight = (Switch) findViewById(R.id.switchLight);
        Switch switchSound = (Switch) findViewById(R.id.switchSound);
        Switch switchVibration = (Switch) findViewById(R.id.switchVibration);

        // Shared preferences, basically an XML file to store preferences (as in, small amounts of data) between sessions.
        // Here it will be used for the status of the switches, as well as the maximum allowed weight.

        //switchAlarmOn.setChecked(true); // Puts the switch "on"



        switchLight.setChecked(sharedPref.getBoolean("switchLight", true)); //Gets the stored value. defaults to true. Sets this as the Switch state
        switchSound.setChecked(sharedPref.getBoolean("switchSound", true)); //Gets the stored value. defaults to true. Sets this as the Switch state
        switchVibration.setChecked(sharedPref.getBoolean("switchVibration", true)); //Gets the stored value. defaults to true. Sets this as the Switch state
        switchAlarmOn.setChecked(sharedPref.getBoolean("switchAlarm", true)); //Gets the stored value. defaults to true. Sets this as the Switch state

        Log.d(TAG, "handleSwitches, Light: " + sharedPref.getBoolean("switchLight", true));


        /*switchLight.setChecked(sharedPref.getBoolean("LightAlarm", true)); //Gets the stored value. defaults to true. Sets this as the Switch state
        switchSound.setChecked(sharedPref.getBoolean("SoundAlarm", true)); //Gets the stored value. defaults to true. Sets this as the Switch state
        switchVibration.setChecked(sharedPref.getBoolean("VibrationAlarm", true)); //Gets the stored value. defaults to true. Sets this as the Switch state
        switchAlarmOn.setChecked(sharedPref.getBoolean("EnableAlarm", true)); //Gets the stored value. defaults to true. Sets this as the Switch state
*/

        // set the change listeners on the switches
        switchAlarmOn.setOnCheckedChangeListener(changeListener);
        switchLight.setOnCheckedChangeListener(changeListener);
        switchSound.setOnCheckedChangeListener(changeListener);
        switchVibration.setOnCheckedChangeListener(changeListener);

    }

    // Method to add items to the list and update the view
  /*  public void addBTItems(String item) {
        bluetoothDevices.add(item);
        adapter.notifyDataSetChanged();
    }*/
}