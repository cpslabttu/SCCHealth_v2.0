package nsf.esarplab.scchealth;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import static nsf.esarplab.bluetoothlibrary.BluetoothState.REQUEST_ENABLE_BT;


public class HomeActivity extends AppCompatActivity {
    final BluetoothAdapter bluetooth = BluetoothAdapter.getDefaultAdapter();
    private Switch btSwitch;
    private WifiManager wifiManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //getActionBar().setIcon(new ColorDrawable(getResources().getColor(android.R.color.transparent)));
        // Set the content of the activity to use the activity_main.xml layout file
        setContentView(R.layout.activity_home);

        ActionBar myActionBar = getSupportActionBar();
        myActionBar.show();

        TextView intro = (TextView) findViewById(R.id.espeech);
        intro.setVisibility(View.INVISIBLE);
        Log.i("Home", "Called");

        // Find the switch that turn on/off bluetooth
        btSwitch = (Switch) findViewById(R.id.mySwitch);

        //Get Login details
        SharedPreferences prefs = getSharedPreferences("logindetails",MODE_PRIVATE);
        String Uname =  prefs.getString("loginname","Default");


        // Find the switch that turn on/off wifi
        //wifiSwitch = (Switch) findViewById(R.id.wifiSwitch);


        // Find the View that shows the  project information
        TextView about = (TextView) findViewById(R.id.about);
        String aboutString = "About \n\t- Know the project";
        SpannableString ss1 = new SpannableString(aboutString);
        ss1.setSpan(new RelativeSizeSpan(2.0f), 0, 5, 0); // set size
        about.setText(ss1);
        // Set a click listener on that View

        about.setOnClickListener(new View.OnClickListener() {
            // The code in this method will be executed when the lab category is clicked on.
            @Override
            public void onClick(View view) {
                // Create a new intent to open the {@link LabActivity}
                Intent aboutIntent = new Intent(HomeActivity.this, About.class);

                // Start the new activity
                startActivity(aboutIntent);

            }
        });

        // Find the View that shows the lab category
        TextView lab = (TextView) findViewById(R.id.lab);
        String diagnosticString = "Diagnostic \n\t- Diagnose flu, arrythmia, sleep apnea & COPD";
        SpannableString ss3 = new SpannableString(diagnosticString);
        ss3.setSpan(new RelativeSizeSpan(2.0f), 0, 10, 0); // set size
        lab.setText(ss3);
        // Set a click listener on that View

        lab.setOnClickListener(new View.OnClickListener() {
            // The code in this method will be executed when the lab category is clicked on.
            @Override
            public void onClick(View view) {
                // Create a new intent to open the {@link LabActivity}
                Intent labIntent = new Intent(HomeActivity.this, LabActivity.class);

                // Start the new activity
                startActivity(labIntent);

            }
        });


        // Find the View that shows the setting category
        TextView setting = (TextView) findViewById(R.id.setting);
        String settingString = "Settings \n\t- Create user, manage profiles & setup network";
        SpannableString ss2 = new SpannableString(settingString);
        ss2.setSpan(new RelativeSizeSpan(2.0f), 0, 8, 0); // set size
        setting.setText(ss2);
        // Set a click listener on that View
        setting.setOnClickListener(new View.OnClickListener() {
            // The code in this method will be executed when the profile category is clicked on.
            @Override
            public void onClick(View view) {
                // Create a new intent to open the {@link ProfileActivity}
                Intent settingIntent = new Intent(HomeActivity.this, DB_login.class);

                // Start the new activity
                startActivity(settingIntent);

            }
        });

        // Find the View that shows the website category
        TextView web = (TextView) findViewById(R.id.web);
        String webString = "Website \n\t- Visit SCC Health Website";
        SpannableString ss4 = new SpannableString(webString);
        ss4.setSpan(new RelativeSizeSpan(2.0f), 0, 8, 0); // set size
        web.setText(ss4);
        // Set a click listener on that View
        web.setOnClickListener(new View.OnClickListener() {
            // The code in this method will be executed when the profile category is clicked on.
            @Override
            public void onClick(View view) {
                Uri uri = Uri.parse("http://sccmobilehealth.com"); // missing 'http://' will cause crashed
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });
        //http://sccmobilehealth.com/insertjsondb.php
        // manage switch position based on connection status
        if (bluetooth.isEnabled()){
            btSwitch.setChecked(true);
        }else {
            btSwitch.setChecked(false);
        }




        //attach a listener to check for changes in state
        btSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {

                if (isChecked) {
                    if (!bluetooth.isEnabled()) {
                        // prompt the user to turn BlueTooth on
                        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                    }
                } else {
                    bluetooth.disable();
                }

            }
        });


        // put setting not clickable
        if (!(Uname.matches("admin"))){
            setting.setClickable(false);
            setting.setBackgroundColor(Color.GRAY);
        }
        // manage switch to turn wifi on/off

       /* wifiManager = (WifiManager) this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if(wifiManager.isWifiEnabled()){
            wifiSwitch.setChecked(true);
        }else{
            wifiSwitch.setChecked(false);

        }

        //attach a listener to check for changes in wifi state
        wifiSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {

                if (isChecked) {
                    wifiManager.setWifiEnabled(true);
                } else {
                    wifiManager.setWifiEnabled(false);
                }

            }
        });*/


    }



    public void logOut(View v) {
        // close the app
        try {

            Intent intentBack = new Intent(this,LoginActivity.class);
            startActivity(intentBack);
            Intent intentExit = new Intent(Intent.ACTION_MAIN);
            intentExit.addCategory(Intent.CATEGORY_HOME);
            intentExit.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intentExit);
            finish();


        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
