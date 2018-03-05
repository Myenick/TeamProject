package net.mienik.measureit;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class StartActivity extends AppCompatActivity {

    BluetoothService bt;
    private final static int REQUEST_ENABLE_BT = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        final Button btnStartMeasures = (Button) findViewById(R.id.btnStartMeasures);

        checkBt();


        /*Starting measures from here*/
        btnStartMeasures.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent goToMeasuresIntent = new Intent(StartActivity.this, AllMeasuresActivity.class);
                startActivity(goToMeasuresIntent);
            }
        });


    }


    protected void checkBt() {

        final Button btnEnableBluetooth = (Button) findViewById(R.id.btnEnableBluetooth);

        if (!bt.isBluetoothEnabled()) {

            final TextView outBtStatus = (TextView) findViewById(R.id.outBtStatus);
            outBtStatus.setText(R.string.bt_stateDisabled);
            outBtStatus.setTextColor(Color.RED);

            /*Listener for enabling bluetooth*/
            btnEnableBluetooth.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    try {
                        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                    } catch (Exception e) {
                        e.printStackTrace(); //Prints stack trace of exception to System.err
                        throw new Error("Couldn't initialize bluetooth!");
                    }

                    btnEnableBluetooth.setVisibility(View.GONE);
                    outBtStatus.setVisibility(View.GONE);
                    getWindow().getDecorView().findViewById(android.R.id.content).invalidate();
                }
            });

        } else {
            btnEnableBluetooth.setVisibility(View.GONE);
        }
    }
}