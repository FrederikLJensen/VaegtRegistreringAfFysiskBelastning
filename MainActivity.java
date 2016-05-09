package bachelor.vaegtregistreringaffysiskbelastning.GitRepository;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.ActionProvider;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import bachelor.vaegtregistreringaffysiskbelastning.R;

public class MainActivity extends ActionBarActivity {

    private BroadcastReceiver vaegtReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if(intent.getIntArrayExtra("Data") != null){

                int total = 0;
                int counter = 0;
                for (int i : intent.getIntArrayExtra("Data")) {

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

// start bluetooth service
        startService(new Intent(this, BluetoothService.class));
        //TODO: test:
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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    private void updateText(int value, int sensor){
//TODO: set text of textviews. Ensure correct order.
        TextView temp;
        switch (value) {
            case 1:
                temp = (TextView) findViewById(R.id.textViewRightSensor);
                temp.setText(value);
                break;
            case 2:
                temp = (TextView) findViewById(R.id.textViewLeftSensor);
                temp.setText(value);
                break;
            case 3:
                temp = (TextView) findViewById(R.id.textViewFrontSensor);
                temp.setText(value);
                break;
            case 4:
                temp = (TextView) findViewById(R.id.textViewBackSensor);
                temp.setText(value);
                break;
            case 5:
                temp = (TextView) findViewById(R.id.textViewWeight);
                temp.setText(value);
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
