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
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.Toast;

import java.util.ArrayList;

import bachelor.vaegtregistreringaffysiskbelastning.R;

public class InstillingerActivity extends AppCompatActivity {

    private final static String TAG = BluetoothService.class.getSimpleName();

    private ListView mBluetoothList;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> bluetoothDevices;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instillinger);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Setup the switches.
        handleSwitches();
        // Enable bluetooth button
        handleBluetoothButton();
        // Register receiver for getting updates on bluetooth devices
        IntentFilter filter = new IntentFilter("com.vaegtregistreringaffysiskbelasting.BroadcastNewDevice");
        this.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                addBTItems(intent.getExtras().getString("device"));
            }
        }, filter);



        //
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        // The visible list containing the found bluetooth devices
        mBluetoothList = (ListView)findViewById(R.id.listView);
        //Initialize bluetooth list
        bluetoothDevices = new ArrayList<String>();
        // Attach adapter to list
        adapter=new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1,
                bluetoothDevices);
        mBluetoothList.setAdapter(adapter);

        // When an item is selected
        mBluetoothList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Toast.makeText(getBaseContext(), "int i = " + i + ", long l = " + l, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


    }

    // The listener for the switches
    CompoundButton.OnCheckedChangeListener changeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            Switch s = (Switch) compoundButton;
            Log.d(TAG, "onCheckedChanged: " + s.getId() + " state: " + b);
        }
    };

    // When the "Scan" button is pressed, a broadcast requesting a BLE scan is sent.
    private void handleBluetoothButton(){

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
        Switch switchAlarmOn = (Switch) findViewById(R.id.switchAlarmON);
        Switch switchLight = (Switch) findViewById(R.id.switchLight);
        Switch switchSound = (Switch) findViewById(R.id.switchSound);
        Switch switchVibration = (Switch) findViewById(R.id.switchVibration);




        // Shared preferences, basically an XML file to store preferences (as in, small amounts of data) between sessions.
        // Here it will be used for the status of the switches, as well as the maximum allowed weight.
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);

        switchAlarmOn.setChecked(true); // Puts the switch "on"

        switchAlarmOn.isChecked();
        sharedPref.edit().putString("LightAlarm","ON");

        sharedPref.edit().commit();

        // set the change listeners on the switches
        switchAlarmOn.setOnCheckedChangeListener(changeListener);
        switchLight.setOnCheckedChangeListener(changeListener);
        switchSound.setOnCheckedChangeListener(changeListener);
        switchVibration.setOnCheckedChangeListener(changeListener);

    }

    // Method to add items to the list and update the view
    public void addBTItems(String item) {
        bluetoothDevices.add(item);
        adapter.notifyDataSetChanged();
    }
}