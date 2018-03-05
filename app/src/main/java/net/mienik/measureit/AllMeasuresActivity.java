package net.mienik.measureit;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.TextView;

public class AllMeasuresActivity extends AppCompatActivity {

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;
    private SetView setView;

    TextView incomingMessages;
    StringBuilder messages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_measures);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        mToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.open, R.string.close);
        setView = new SetView();

        mDrawerLayout.addDrawerListener(mToggle);
        mToggle.syncState();

        incomingMessages = (TextView) findViewById(R.id.incomingMessages);
        messages = new StringBuilder();

        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, new IntentFilter("incomingMessages"));

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        NavigationView mNavigationView = (NavigationView) findViewById(R.id.nav_menu);
        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case (R.id.btnTemperatureDHT11):
                        setView.setTmpDHT(true);
                        break;
                    case (R.id.btnTempVernier):
                        setView.setTmpVer(true);
                        break;
                    case (R.id.btnHumidity):
                        setView.setHumidity(true);
                        break;
                    case (R.id.btnLatitude):
                        setView.setLatitude(true);
                        break;
                    case (R.id.btnLongitude):
                        setView.setLongitude(true);
                        break;
                    case (R.id.btnLux):
                        setView.setLux(true);
                        break;
                    case (R.id.btnShow):
                        Intent i = new Intent(AllMeasuresActivity.this, SpecViewActivity.class);
                        i.putExtra("measuresObject", setView);
                        startActivity(i);
                        break;
                }
                return true;
            }
        });
    }
    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String text = intent.getStringExtra("theMessage");

            messages.append(text + "\n");
            incomingMessages.setText(messages);
        }
    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (mToggle.onOptionsItemSelected(item))
            return true;

        return super.onOptionsItemSelected(item);
    }
}
