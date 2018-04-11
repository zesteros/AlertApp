package com.asde.smartalarm;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;

import com.asde.smartalarm.bt.BluetoothConnectionThread;
import com.asde.smartalarm.bt.DeviceDialog;
import com.asde.smartalarm.util.GlobalVariable;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String STATE_PREF = "state";
    private BluetoothAdapter btAdapter;
    private ImageView mSwitch;
    private Button mConnectButton;
    private SharedPreferences.Editor mEditor;
    private SharedPreferences mPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mSwitch = (ImageView) findViewById(R.id.alarm_switch);
        mConnectButton = (Button) findViewById(R.id.connect_button);
        GlobalVariable.getInstance().setConnectButton(mConnectButton);
        mConnectButton.setOnClickListener(this);
        mSwitch.setOnClickListener(this);
        mPrefs = getSharedPreferences(STATE_PREF, MODE_PRIVATE);
        mEditor = mPrefs.edit();
        updateSwitchState();
        /*editor.putString("name", "Elena");
        editor.putInt("idName", 12);
        editor.apply();*/

    }

    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }*/

    public void updateSwitchState() {
        String switchState = mPrefs.getString("switch_state", "0");
        if (switchState.equals("0")) {
            mSwitch.setImageResource(R.drawable.ic_switch_off);
            GlobalVariable.getInstance().isAlarmOn(false);
        } else {
            mSwitch.setImageResource(R.drawable.ic_switch_on);
            GlobalVariable.getInstance().isAlarmOn(true);
        }
    }

    @Override
    public void onClick(View v) {
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(80);
        switch (v.getId()) {
            case R.id.alarm_switch:
                if (GlobalVariable.getInstance().isConnected()) {
                    BluetoothConnectionThread connectionThread = GlobalVariable.getInstance().getConnectionInstance();
                    if (GlobalVariable.getInstance().isAlarmOn()) {
                        mSwitch.setImageResource(R.drawable.ic_switch_off);
                        GlobalVariable.getInstance().isAlarmOn(false);
                        connectionThread.write("0");
                        mEditor.putString("switch_state","0").apply();
                    } else {
                        mSwitch.setImageResource(R.drawable.ic_switch_on);
                        GlobalVariable.getInstance().isAlarmOn(true);
                        connectionThread.write("1");
                        mEditor.putString("switch_state","1").apply();
                    }
                } else showToast("Lo sentimos, no estas conectado a la alarma.");
                break;
            case R.id.connect_button:
                if (isBluetoothEnabled()) {
                    try {
                        if (!GlobalVariable.getInstance().isConnected()) showDialog();
                        else if (GlobalVariable.getInstance().isConnected() &&
                                GlobalVariable.getInstance().getConnectButton().getText().equals("Desconectar")) {
                            GlobalVariable.getInstance().getBtSocket().close();
                            GlobalVariable.getInstance().isConnected(false);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else { //IF BT ISNT ENABLED HIDE UI AND TOAST TO THE USER
                    showToast("ACTIVA TU BLUETOOTH");
                }
                break;
        }

    }

    /**
     * @return true if the bluetooth is enabled, also if is not, request bluetooth activation to the user
     */
    public boolean isBluetoothEnabled() {
        setBluetoothAdapter();
        if (btAdapter != null) {
            if (!btAdapter.isEnabled()) {
                //INSTANTIATE A NEW ACTIVITY FROM SYSTEM
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
                if (btAdapter.isEnabled()) return true;
                else return false;
            } else return true;
        } else return false;
    }


    /**
     * Instantiate Bluetooth adapter
     */
    public void setBluetoothAdapter() {
        btAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    /**
     * Show device list connect dialog
     */
    private void showDialog() {
        try {
            new DeviceDialog(this).showDeviceListDialog();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Button connectButton = GlobalVariable.getInstance().getConnectButton();
        if(connectButton != null){
            if(GlobalVariable.getInstance().isConnected()) connectButton.setText("Desconectar");
        }
    }

    /**
     * @param text the id of string resource for show message
     */
    public void showToast(String text) {
        /*Find the root view*/
        View view = findViewById(android.R.id.content);
        /*Show the snackbar*/
        Snackbar.make(view, text, Snackbar.LENGTH_LONG).show();
        //Toast.makeText(getApplicationContext(), getResources().getString(id),
        //      Toast.LENGTH_SHORT).show();

    }
}
