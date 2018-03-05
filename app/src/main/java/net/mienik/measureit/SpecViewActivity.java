package net.mienik.measureit;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.TextView;

public class SpecViewActivity extends AppCompatActivity {

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spec_view);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        mToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.open, R.string.close);

        mDrawerLayout.addDrawerListener(mToggle);
        mToggle.syncState();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        NavigationView mNavigationView = (NavigationView) findViewById(R.id.nav_menu);
        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                Intent i;
                switch (menuItem.getItemId()) {
                    case (R.id.btnClear):
                        /*setView.setTmpDHT(true);
                        break;*/
                    case (R.id.btnStop):
                        /*setView.setTmpVer(true);
                        break;*/
                    case (R.id.btnSave):
                        i = new Intent(SpecViewActivity.this, SaveActivity.class);
                        startActivity(i);
                        break;
                    case (R.id.btnSend):
                        i = new Intent(SpecViewActivity.this, SendActivity.class);
                        startActivity(i);
                        break;
                   /* case(R.id.btnShow):
                        Intent i = new Intent(AllMeasuresActivity.this, SpecViewActivity.class);
                        i.putExtra("measuresObject", setView);
                        startActivity(i);
                        break;*/
                }
                return true;
            }
        });

        Intent i = getIntent();
        SetView setView = (SetView) i.getSerializableExtra("measuresObject");
        final TextView outShowValues = (TextView) findViewById(R.id.outShowValues);
        if (setView.getTmpDHT())
            outShowValues.setText("TempDHT: " + setView.getTmpDHT() + " TempVer: " + setView.getTmpVer() + " Humidity: " + setView.getHumidity() + " Latitude: " + setView.getLongitude() + " Longitude: " + setView.getLongitude() + " Lux: " + setView.getLux());

    }

    public boolean onOptionsItemSelected(MenuItem item) {

        if (mToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
/*    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            Log.d(this.getClass().getName(), "back button pressed");
            System.gc(); //for now until i find better solution
        }
        return super.onKeyDown(keyCode, event);
    }*/
}
