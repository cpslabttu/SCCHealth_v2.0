package nsf.esarplab.scchealth;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import nsf.esarplab.bluetoothlibrary.BluetoothSPP;
import nsf.esarplab.bluetoothlibrary.BluetoothState;
import nsf.esarplab.bluetoothlibrary.DeviceList;

public class oneStopService extends AppCompatActivity {

    BluetoothSPP bt;
    private TextView intro, btData, brData,hrData, ecData, oximetryData, connectionRead;
    private TextView statusTemp, statusHR, statusBR, statusO2, statusECG, gradientFlu,gradientSA,gradientAR,gradientPD, fluScreening,postStatus;
    private LinearLayout layout1,layoutIntro,layoutProfile,sensorDisplay,sensorStatus;
    private ScrollView  mDisplay;
    EditText etMessage;
    private GoogleApiClient client;
    private Button connectScanner, collectData, dispResult, shareResult;
    private boolean diseaseKey = false;
    private boolean sensorKeyBT = false;
    private boolean sensorKeyPO = false;
    private boolean sensorKeyHR = false;
    private boolean sensorKeyBR = false;
    private boolean sensorKeyEC = false;
    private boolean timeKeyBT = false;
    private boolean timeKeyPO = false;
    private boolean timeKeyHR = false;
    private boolean timeKeyBR = false;
    private boolean timeKeyEC=false;
    private boolean handShake=false;
    private int sensor = 1;
    private int fileSeq=0;
    private int sensorNo;
    private String eoiValue,sSeverity;
    double ratingOfEOI = 0.0;
    private  RadioGroup radioGroup;
    private String sEOI="";
    private String sTemperature="";
    private RadioButton rb1, rb2, rb3, rb4, rb5;
    Menu menu;
    private ArrayList<String> arr_hex = new ArrayList<String>();
    private ArrayList<Short> arr_received = new ArrayList<Short>();
    private ArrayList<Short> arr_respiration = new ArrayList<Short>();
    private String currentDateTime = "";
    String s = "";
    private PopupWindow mPopupWindow;
    private CheckBox ch1,ch2;
    private double temperature=0.0f;
    private ProgressDialog progressDialog, progressDialog2;
    private CountDownTimer Count;
    private boolean failedHandshake;
    private boolean invalid;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_one_stop_service);

        // show action bar
        ActionBar myActionBar = getSupportActionBar();
        myActionBar.show();
        TextView mNameText = (TextView) findViewById(R.id.display_name);
        bt = new BluetoothSPP(this);
        // initialize layouts
        layout1=(LinearLayout)findViewById(R.id.layout1);
        layoutIntro=(LinearLayout)findViewById(R.id.layout_intro);
        layoutProfile=(LinearLayout)findViewById(R.id.layout2);
        sensorStatus=(LinearLayout) findViewById(R.id.sensorStatus);

        //intro=(TextView) findViewById(R.id.display_intro);
        sensorDisplay=(LinearLayout) findViewById(R.id.sensorDisplay);
        mDisplay=(ScrollView) findViewById(R.id.maindisp);
        collectData=(Button) findViewById(R.id.test);
        btData=(TextView) findViewById(R.id.value_flu);

        // sensor status
        statusTemp=(TextView) findViewById(R.id.statusTemp);
        statusHR=(TextView) findViewById(R.id.statusHR);
        statusBR=(TextView) findViewById(R.id.statusBR);
        //statusO2=(TextView) findViewById(R.id.statusO2);
        statusECG=(TextView) findViewById(R.id.statusECG);
        fluScreening=(TextView) findViewById(R.id.screening_flu);
        postStatus=(TextView) findViewById(R.id.postStatus);

        oximetryData=(TextView) findViewById(R.id.display_spo2);
        connectionRead = (TextView) findViewById(R.id.textStatus);
        dispResult = (Button) findViewById(R.id.displayResult);
        connectScanner = (Button) findViewById(R.id.cScanner);
        shareResult=(Button) findViewById(R.id.shareResult);
        sensorNo=0;

        // hide sensor list & collect data button
        sensorDisplay.setVisibility(View.GONE);
        collectData.setVisibility(View.GONE);
        // hide compute & share button
        dispResult.setVisibility(View.GONE);
        shareResult.setVisibility(View.GONE);

        //Show active profile
        SharedPreferences prefs = getSharedPreferences("logindetails",MODE_PRIVATE);
        String Uname =  prefs.getString("loginname","Default");
        mNameText.setText("\t\t"+Uname);

        s = mNameText.getText().toString().trim();



        // color line gradient


        gradientFlu = (TextView) findViewById(R.id.active_gradient_flu);
        gradientSA = (TextView) findViewById(R.id.active_gradient_SA);
        gradientAR = (TextView) findViewById(R.id.active_gradient_AR);
        gradientPD = (TextView) findViewById(R.id.active_gradient_PD);

        int[] colors = {Color.parseColor("#008000"), Color.parseColor("#FFFF00"), Color.parseColor("#FFA500"), Color.parseColor("#ff0000"), Color.parseColor("#800000")};
        GradientDrawable gd = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT, colors);
        gradientFlu.setBackground(gd);
        gradientSA.setBackground(gd);
        gradientAR.setBackground(gd);
        gradientPD.setBackground(gd);

        // radio group for sensors

        radioGroup = (RadioGroup) findViewById(R.id.rg1);
        rb1 = (RadioButton) findViewById(R.id.rb_bt);
        //rb2 = (RadioButton) findViewById(R.id.rb_o2);
        //rb3 = (RadioButton) findViewById(R.id.rb_hr);
        //rb4 = (RadioButton) findViewById(R.id.rb_resp);
        //rb5 = (RadioButton) findViewById(R.id.rb_ecg);
        rb1.setTextColor(Color.BLUE);



        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {

                // set text color
                int selectedId = radioGroup.getCheckedRadioButtonId();
                RadioButton selected = (RadioButton) findViewById(selectedId);
                rb1.setTextColor(Color.BLACK);
                //rb2.setTextColor(Color.BLACK);
                //rb3.setTextColor(Color.BLACK);
                //rb4.setTextColor(Color.BLACK);
                //rb5.setTextColor(Color.BLACK);

                selected.setTextColor(Color.BLUE);
                // checkedId is the RadioButton selected
                if(checkedId==R.id.rb_bt){

                    sensor=1;
                }else if(checkedId==R.id.rb_resp){

                    sensor=2;
                }
                else if(checkedId==R.id.rb_o2){

                    sensor=3;
                }else if(checkedId==R.id.rb_hr){

                    sensor=4;
                }else if(checkedId==R.id.rb_ecg){

                    sensor=5;
                }

            }
        });


        // set the bluetooth connection

        bt.setOnDataReceivedListener(new BluetoothSPP.OnDataReceivedListener() {
            public void onDataReceived(byte[] data, String message) {
                short val = 0;
                String readAscii = new String(data);
                //textReceived.append(message + "\n");
                if (handShake) {

                    arr_hex.add(message);
                    Log.i("Str2@activity",message);
                    Log.i("size_arr_hex",""+arr_hex.size());


                    if (arr_hex.size() == 2) {
                        String catHex = arr_hex.get(0) + arr_hex.get(1);
                        /*int b0 = (arr_hex.get(0) & 255); // converts to unsigned
                        int b1 = (arr_hex.get(1) & 255); // converts to unsigned
                        int val = b0 << 8 | b1;*/
                        Log.i("val1@final", catHex);
                        //val = (short) (Integer.parseInt(catHex, 16));
                        //Log.i("val2@final", String.valueOf(val));

                        //val= (short) Long.parseLong(catHex, 16);
                        val = (short) (Integer.parseInt(catHex, 16));

                        //val= (int) Long.parseLong(catHex, 16);
                        Log.i("val3@final", String.valueOf(val));



                        arr_received.add(val);
                        //Textv.append(Integer.toString(val) + "\n");
                        /*try {
                            writeToCsv(Integer.toString(val));
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }*/
                        arr_hex.clear();

                    }
                    //receive data



                    /*switch (sensor) {
                        case 1: {

                            arr_received.add(val);
                            Log.i("sizearr_received", "" + arr_received.size());
                            break;
                        }
                        case 2: {

                            arr_respiration.add(val);
                            //Log.i("sizearr_received", "" + arr_received.size());
                            break;
                        }
                        case 3: {

                            //hrData.append(message + "\n");
                            break;
                        }
                        case 4: {

                            //brData.append(message + "\n");
                            break;
                        }
                        case 5: {

                            //ecData.append(message + "\n");
                            break;
                        }


                    }*/

                } else {
                    if (readAscii.equals("OS")) {

                        switch (sensor) {
                            case 1: {
                                bt.send("TP", true);
                                diseaseKey = true;
                                break;
                            }
                            case 2: {
                                bt.send("PO", true);
                                diseaseKey = true;
                                break;
                            }
                            case 3: {
                                bt.send("HR", true);
                                diseaseKey = true;
                                break;
                            }
                            case 4: {
                                bt.send("BR", true);
                                diseaseKey = true;
                                break;
                            }
                            case 5: {
                                bt.send("EC", true);
                                diseaseKey = true;
                                break;
                            }
                        }

                    } else {
                        if (readAscii.equals("TP") && diseaseKey) {
                            bt.send("00020", true);
                            sensorKeyBT = true;
                        } else if (readAscii.equals("PO") && diseaseKey) {
                            bt.send("5", true);
                            sensorKeyPO= true;
                        } else if (readAscii.equals("HR") && diseaseKey) {
                            bt.send("6", true);
                            sensorKeyHR = true;
                        }else if (readAscii.equals("BR") && diseaseKey) {
                            bt.send("7", true);
                            sensorKeyBR = true;
                        }else if (readAscii.equals("EC") && diseaseKey) {
                            bt.send("8", true);
                            sensorKeyEC = true;
                        }else {
                            if (readAscii.equals("00020") && sensorKeyBT && diseaseKey) {
                                bt.send("OK", true);
                                handShake = true;
                            } else if (readAscii.equals("5") && sensorKeyPO && diseaseKey) {
                                bt.send("OK", true);
                                handShake = true;
                            } else if (readAscii.equals("6") && sensorKeyHR && diseaseKey) {
                                bt.send("OK", true);
                                handShake = true;
                            }else if (readAscii.equals("7") && sensorKeyBR && diseaseKey) {
                                bt.send("OK", true);
                                handShake = true;
                            }else if (readAscii.equals("8") && sensorKeyEC && diseaseKey) {
                                bt.send("OK", true);
                                handShake = true;
                            }else {
                                failedHandshake=true;
                            }
                        }
                    }
                }
            }

        });

        bt.setBluetoothConnectionListener(new BluetoothSPP.BluetoothConnectionListener() {
            public void onDeviceDisconnected() {
                connectionRead.setText("Status : Not connect");
                connectionRead.setBackgroundColor(Color.parseColor("#D3D3D3"));
                menu.clear();
                getMenuInflater().inflate(R.menu.menu_connection, menu);
            }

            public void onDeviceConnectionFailed() {
                connectionRead.setText("Status : Connection failed");
                try {
                    writeToLog("Connect Failed from OSS");
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                connectionRead.setBackgroundColor(Color.parseColor("#D3D3D3"));

                AlertDialog.Builder builder = new AlertDialog.Builder(oneStopService.this);
                builder.setTitle("Connection Error");
                builder.setMessage("Retry to connect");

                // add the buttons
                builder.setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // do something ...
                        bt.setDeviceTarget(BluetoothState.DEVICE_OTHER);
                        Intent intent = new Intent(getApplicationContext(), DeviceList.class);
                        startActivityForResult(intent, BluetoothState.REQUEST_CONNECT_DEVICE);
                        dialog.dismiss();
                    }
                });
                builder.setNegativeButton("Cancel", null);

                // create and show the alert dialog
                AlertDialog dialog = builder.create();
                dialog.show();
            }

            public void onDeviceConnected(String name, String address) {
                connectionRead.setText("Status : Connected to " + name);
                connectionRead.setBackgroundColor(Color.parseColor("#228B22"));
                try {
                    writeToLog("Connect success using OSS ");
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                menu.clear();
                getMenuInflater().inflate(R.menu.menu_disconnection, menu);
                sensorDisplay.setVisibility(View.VISIBLE);
                layoutIntro.setVisibility(View.GONE);
                layoutProfile.setVisibility(View.VISIBLE);
                collectData.setVisibility(View.VISIBLE);
                connectScanner.setVisibility(View.GONE);

            }
        });



            /*// set corresponding display for selected sensor
            final LinearLayout hrlayout = (LinearLayout) findViewById(R.id.hr_result);
            final LinearLayout spo2layout = (LinearLayout) findViewById(R.id.spo2_result);
            RadioButton rbOxygen = (RadioButton) findViewById(R.id.rb_o2);
            RadioButton rbheartRate = (RadioButton) findViewById(R.id.rb_hr);
            hrlayout.setVisibility(View.VISIBLE);
            spo2layout.setVisibility(View.GONE);

            RadioGroup radioGroup2 = (RadioGroup) findViewById(R.id.rg1);
            radioGroup2.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    if (checkedId == R.id.rb_o2) {
                        sensor=2;
                        hrlayout.setVisibility(View.GONE);
                        spo2layout.setVisibility(View.VISIBLE);
                    } else {
                        hrlayout.setVisibility(View.VISIBLE);
                        spo2layout.setVisibility(View.GONE);
                    }
                }
            });*/

// inflate the screen for info and instruction
        final TextView btnOpenPopup = (TextView) findViewById(R.id.info);
        btnOpenPopup.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                LayoutInflater layoutInflater
                        = (LayoutInflater) getBaseContext()
                        .getSystemService(LAYOUT_INFLATER_SERVICE);
                View popupView = layoutInflater.inflate(R.layout.info_oss, null);
                final PopupWindow popupWindow = new PopupWindow(
                        popupView,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);

                Button btnDismiss = (Button) popupView.findViewById(R.id.dismiss);
                btnDismiss.setOnClickListener(new Button.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // TODO Auto-generated method stub
                        popupWindow.dismiss();
                    }
                });

                popupWindow.showAsDropDown(btnOpenPopup, 50, -30);

            }
        });

        final TextView btnOpenInstruction = (TextView) findViewById(R.id.inst);
        btnOpenInstruction.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                LayoutInflater layoutInflater
                        = (LayoutInflater) getBaseContext()
                        .getSystemService(LAYOUT_INFLATER_SERVICE);
                View popupView = layoutInflater.inflate(R.layout.inst_oss, null);
                final PopupWindow popupWindow = new PopupWindow(
                        popupView,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);

                Button btnDismiss = (Button) popupView.findViewById(R.id.dismiss);
                btnDismiss.setOnClickListener(new Button.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // TODO Auto-generated method stub
                        popupWindow.dismiss();
                    }
                });

                // Find the View that shows the video Demo
                Button videoDemo = (Button) popupView.findViewById(R.id.video);
                // Set a click listener on that View
                videoDemo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Uri uri = Uri.parse("https://www.youtube.com/playlist?list=PLO1huaFZzTzNaBK5Pbde1GDeGRdCXOhUm"); // missing 'http://' will cause crashed
                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                        startActivity(intent);
                    }
                });

                popupWindow.showAsDropDown(btnOpenPopup, 50, -30);

            }
        });

        /*final TextView btnFluInfo = (Button) findViewById(R.id.buttonFlu);
        btnFluInfo.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                LayoutInflater layoutInflater
                        = (LayoutInflater) getBaseContext()
                        .getSystemService(LAYOUT_INFLATER_SERVICE);
                View popupView = layoutInflater.inflate(R.layout.info_bodytemp, null);
                final PopupWindow popupWindow = new PopupWindow(
                        popupView,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);

                Button btnDismiss = (Button) popupView.findViewById(R.id.dismiss);
                btnDismiss.setOnClickListener(new Button.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // TODO Auto-generated method stub
                        popupWindow.dismiss();
                    }
                });

                popupWindow.showAsDropDown(btnOpenPopup, 50, -30);

            }
        });

        final TextView btnSAInfo = (Button) findViewById(R.id.buttonSA);
        btnSAInfo.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                LayoutInflater layoutInflater
                        = (LayoutInflater) getBaseContext()
                        .getSystemService(LAYOUT_INFLATER_SERVICE);
                View popupView = layoutInflater.inflate(R.layout.info_sleepapnea, null);
                final PopupWindow popupWindow = new PopupWindow(
                        popupView,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);

                Button btnDismiss = (Button) popupView.findViewById(R.id.dismiss);
                btnDismiss.setOnClickListener(new Button.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // TODO Auto-generated method stub
                        popupWindow.dismiss();
                    }
                });

                popupWindow.showAsDropDown(btnOpenPopup, 50, -30);

            }
        });

        final TextView btnAsthmaInfo = (Button) findViewById(R.id.buttonAsthma);
        btnAsthmaInfo.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                LayoutInflater layoutInflater
                        = (LayoutInflater) getBaseContext()
                        .getSystemService(LAYOUT_INFLATER_SERVICE);
                View popupView = layoutInflater.inflate(R.layout.info_copd, null);
                final PopupWindow popupWindow = new PopupWindow(
                        popupView,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);

                Button btnDismiss = (Button) popupView.findViewById(R.id.dismiss);
                btnDismiss.setOnClickListener(new Button.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // TODO Auto-generated method stub
                        popupWindow.dismiss();
                    }
                });

                popupWindow.showAsDropDown(btnOpenPopup, 50, -30);

            }
        });

        final TextView btnARInfo = (Button) findViewById(R.id.buttonAR);
        btnARInfo.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                LayoutInflater layoutInflater
                        = (LayoutInflater) getBaseContext()
                        .getSystemService(LAYOUT_INFLATER_SERVICE);
                View popupView = layoutInflater.inflate(R.layout.info_arrhythmia, null);
                final PopupWindow popupWindow = new PopupWindow(
                        popupView,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);

                Button btnDismiss = (Button) popupView.findViewById(R.id.dismiss);
                btnDismiss.setOnClickListener(new Button.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // TODO Auto-generated method stub
                        popupWindow.dismiss();
                    }
                });

                popupWindow.showAsDropDown(btnOpenPopup, 50, -30);

            }
        });*/
        // connect scanner by bluetooth

        connectScanner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    writeToLog("Connect button clicked from OSS");
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                bt.setDeviceTarget(BluetoothState.DEVICE_OTHER);
            /*
			if(bt.getServiceState() == BluetoothState.STATE_CONNECTED)
    			bt.disconnect();*/
                Intent intent = new Intent(getApplicationContext(), DeviceList.class);
                startActivityForResult(intent, BluetoothState.REQUEST_CONNECT_DEVICE);

            }
        });
    }

    // @Override
  /* public void onStart() {
        super.onStart();
        if (!mBoundService.isBluetoothEnabled()) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, BluetoothState.REQUEST_ENABLE_BT);
        } else {
            if (!mBoundService.isServiceAvailable()) {
                mBoundService.setupService();
                mBoundService.startService(BluetoothState.DEVICE_ANDROID);
                setup();
            }
        }

    }*/


    public void onDestroy() {
        super.onDestroy();
        bt.stopService();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        getMenuInflater().inflate(R.menu.menu_connection, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_android_connect) {
            bt.setDeviceTarget(BluetoothState.DEVICE_ANDROID);
            /*
			if(bt.getServiceState() == BluetoothState.STATE_CONNECTED)
    			bt.disconnect();*/
            Intent intent = new Intent(getApplicationContext(), DeviceList.class);
            startActivityForResult(intent, BluetoothState.REQUEST_CONNECT_DEVICE);


        } else if (id == R.id.menu_device_connect) {
            bt.setDeviceTarget(BluetoothState.DEVICE_OTHER);
			/*
			if(bt.getServiceState() == BluetoothState.STATE_CONNECTED)
    			bt.disconnect();*/
            Intent intent = new Intent(getApplicationContext(), DeviceList.class);
            startActivityForResult(intent, BluetoothState.REQUEST_CONNECT_DEVICE);

        } else if (id == R.id.menu_disconnect) {
            if (bt.getServiceState() == BluetoothState.STATE_CONNECTED)
                bt.disconnect();
            connectScanner.setVisibility(View.VISIBLE);
            sensorDisplay.setVisibility(View.GONE);
            mDisplay.setVisibility(View.GONE);
            collectData.setVisibility(View.GONE);
            dispResult.setVisibility(View.GONE);
            shareResult.setVisibility(View.GONE);
            sensorStatus.setVisibility(View.GONE);
            layoutIntro.setVisibility(View.VISIBLE);
            arr_received.clear();
            arr_hex.clear();
            handShake = false;
            diseaseKey = false;
            sensorKeyBT = false;
        }

        else if (id == R.id.menu_reinitialize) {
            layout1.setVisibility(View.VISIBLE);
            sensorDisplay.setVisibility(View.VISIBLE);
            mDisplay.setVisibility(View.GONE);
            collectData.setVisibility(View.VISIBLE);
            sensorStatus.setVisibility(View.GONE);
            dispResult.setVisibility(View.GONE);
            shareResult.setVisibility(View.GONE);
            arr_received.clear();
            handShake = false;
            diseaseKey = false;
            sensorKeyBT = false;
        }

        else if (id == R.id.action_save) {
            // Save record to database
            //insertTemp();
            // Exit activity
            //finish();
        }
        // Respond to a click on the "Share to SCC" menu option
        else if (id == R.id.action_share){


            if(s.matches("")) {
                Toast.makeText(getApplicationContext(), "Create Profile First ", Toast.LENGTH_LONG).show();

            }else {

                // Go to cloud activity
                Intent shareIntent = new Intent(oneStopService.this, CloudActivity.class);
                //Bundle extras = new Bundle();
                shareIntent.putExtra("DT", "BT");
                shareIntent.putExtra("profile", s);
                shareIntent.putExtra("EOI", eoiValue);
                shareIntent.putExtra("Time", currentDateTime);
                shareIntent.putExtra("Algorithm", "BT1");
                startActivity(shareIntent);
            }

        }
        else if (id == R.id.action_sms) {
            String messageToSend = "EOI:" + eoiValue;
            String number = "9018340057";

            SmsManager.getDefault().sendTextMessage(number, null, messageToSend, null, null);
            //finish();
        }
        else if (id == R.id.action_history) {

            // Create a new intent to open the {@link Temperature History}
            Intent temperatureHistoryIntent = new Intent(oneStopService.this, Temp_HistoryActivity.class);

            // Start the new activity
            startActivity(temperatureHistoryIntent);
            //finish();
        }

        else if (id == R.id.action_algorithm) {
            Intent algIntent = new Intent(oneStopService.this, FluMethods.class);
            startActivity(algIntent);
        }



        return super.onOptionsItemSelected(item);
    }
    public void onStart() {
        super.onStart();
        if (!bt.isBluetoothEnabled()) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, BluetoothState.REQUEST_ENABLE_BT);
        } else {
            if (!bt.isServiceAvailable()) {
                bt.setupService();
                bt.startService(BluetoothState.DEVICE_ANDROID);
                setup();
                summarizedOutput();
                shareWithSCC();
            }
        }

    }
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == BluetoothState.REQUEST_CONNECT_DEVICE) {
            if (resultCode == Activity.RESULT_OK)
                bt.connect(data);
        } else if (requestCode == BluetoothState.REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                bt.setupService();
                bt.startService(BluetoothState.DEVICE_ANDROID);
                setup();
                summarizedOutput();
            } else {
                Toast.makeText(getApplicationContext()
                        , "BluetoothActivity was not enabled."
                        , Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }


    public void setup() {

        collectData.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                {
                    try {
                        writeToLog("Collect button clicked from OSS");
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    failedHandshake=false;
                    fileSeq++;
                    //arr_received.clear();
                    //arr_respiration.clear();
                    bt.send("OS", true);
                    // progress indicator
                    final ProgressDialog progressDialog = new ProgressDialog(oneStopService.this,
                            R.style.AppTheme_Dark_Dialog);
                    progressDialog.setIndeterminate(true);
                    progressDialog.setMessage("Handshaking & Collecting data...");

                    new CountDownTimer(1200, 100) {

                        public void onTick(long millisecondsUntilDone) {

                            progressDialog.show();
                        }

                        @Override
                        public void onFinish() {
                            Log.i("Done", "Count Down Timer Finished");
                            progressDialog.dismiss();
                            if(handShake){
                                sensorDisplay.setVisibility(View.GONE);
                                collectData.setVisibility(View.GONE);
                                // make compute button visible
                                dispResult.setVisibility(View.VISIBLE);
                                sensorStatus.setVisibility(View.VISIBLE);
                                //statusTemp.setBackgroundColor(Color.parseColor("#4CAF50"));
                            }else if(failedHandshake){
                                // do something
                                AlertDialog.Builder builder = new AlertDialog.Builder(oneStopService.this);
                                builder.setTitle("Communication Error");
                                builder.setMessage("Restart Scanner and re-connect");

                                // add the buttons
                                builder.setPositiveButton("Reconnect", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        // do something ...
                                        dialog.dismiss();
                                        if (bt.getServiceState() == BluetoothState.STATE_CONNECTED)
                                            bt.disconnect();
                                        connectScanner.setVisibility(View.VISIBLE);
                                        sensorDisplay.setVisibility(View.GONE);
                                        mDisplay.setVisibility(View.GONE);
                                        collectData.setVisibility(View.GONE);
                                        dispResult.setVisibility(View.GONE);
                                        shareResult.setVisibility(View.GONE);
                                        sensorStatus.setVisibility(View.GONE);
                                        layoutIntro.setVisibility(View.VISIBLE);
                                        arr_received.clear();
                                        handShake = false;
                                        diseaseKey = false;
                                        sensorKeyBT = false;
                                        Count.cancel();
                                        progressDialog.dismiss();


                                    }
                                });
                                builder.setNegativeButton("Cancel", null);

                                // create and show the alert dialog
                                AlertDialog dialog = builder.create();
                                dialog.show();

                            }else{
                                AlertDialog.Builder builder = new AlertDialog.Builder(oneStopService.this);
                                builder.setTitle("No Communication");
                                builder.setMessage("Restart Scanner and re-connect");

                                // add the buttons
                                builder.setPositiveButton("Reconnect", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        // do something ...
                                        dialog.dismiss();
                                        if (bt.getServiceState() == BluetoothState.STATE_CONNECTED)
                                            bt.disconnect();
                                        connectScanner.setVisibility(View.VISIBLE);
                                        sensorDisplay.setVisibility(View.GONE);
                                        mDisplay.setVisibility(View.GONE);
                                        collectData.setVisibility(View.GONE);
                                        dispResult.setVisibility(View.GONE);
                                        shareResult.setVisibility(View.GONE);
                                        sensorStatus.setVisibility(View.GONE);
                                        layoutIntro.setVisibility(View.VISIBLE);
                                        arr_received.clear();
                                        handShake = false;
                                        diseaseKey = false;
                                        sensorKeyBT = false;

                                    }
                                });
                                builder.setNegativeButton("Cancel", null);

                                // create and show the alert dialog
                                AlertDialog dialog = builder.create();
                                dialog.show();
                            }
                            if (arr_received.size() > 100) {
                                statusTemp.setBackgroundColor(Color.parseColor("#4CAF50"));
                                postStatus.setText("2. Click on COMPUTE SEVERITY ");
                                dispResult.setClickable(true);
                            } else {
                                statusTemp.setBackgroundColor(Color.parseColor("#000000"));
                                postStatus.setText("2. Position Sensor properly, Refresh and collect data again");
                                //dispResult.setClickable(false);
                            }
                            // after timer delay
                                /*sensorDisplay.setVisibility(View.GONE);
                                collectData.setVisibility(View.GONE);
                                // make compute button visible
                                dispResult.setVisibility(View.VISIBLE);
                                sensorStatus.setVisibility(View.VISIBLE);
                                statusTemp.setBackgroundColor(Color.parseColor("#4CAF50"));*/
                                /*AlertDialog alertDialog = new AlertDialog.Builder(oneStopService.this).create();
                                alertDialog.setTitle("Instruction");
                                alertDialog.setMessage("Check sensor status and click on COMPUTE SEVERITY");
                                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                            }
                                        });
                                alertDialog.show();*/
                        }
                    }.start();



                }
            }
        });
    }






    public void summarizedOutput() {


        dispResult.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                {
                    try {
                        writeToLog("Compute button clicked from OSS");
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    if (handShake) {

                            final ProgressDialog progressDialog2 = new ProgressDialog(oneStopService.this,
                                    R.style.AppTheme_Dark_Dialog);
                            progressDialog2.setIndeterminate(true);
                            progressDialog2.setMessage("Processing data...");

                            new CountDownTimer(1000, 100) {

                                public void onTick(long millisecondsUntilDone) {

                                    progressDialog2.show();
                                }

                                @Override
                                public void onFinish() {
                                    Log.i("Done", "Count Down Timer Finished");
                                    fluAlgorithm();
                                    sleepapneaAlgorithm();
                                    arrhythmiaAlgorithm();
                                    asthmaAlgorithm();

                                    if(invalid){
                                        AlertDialog.Builder builder = new AlertDialog.Builder(oneStopService.this);
                                        builder.setTitle("Unreliable Data");
                                        builder.setMessage("Position the sensor properly, refresh and collect data again.");

                                        // add the buttons
                                        builder.setPositiveButton("Refresh", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                // do something ...
                                                dialog.dismiss();
                                                layout1.setVisibility(View.VISIBLE);
                                                sensorDisplay.setVisibility(View.VISIBLE);
                                                mDisplay.setVisibility(View.GONE);
                                                collectData.setVisibility(View.VISIBLE);
                                                sensorStatus.setVisibility(View.GONE);
                                                dispResult.setVisibility(View.GONE);
                                                shareResult.setVisibility(View.GONE);
                                                arr_received.clear();
                                                handShake = false;
                                                diseaseKey = false;
                                                sensorKeyBT = false;

                                            }
                                        });

                                        builder.setNegativeButton("Proceed", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                // do something ...
                                                dialog.dismiss();
                                                layout1.setVisibility(View.GONE);
                                                layoutIntro.setVisibility(View.GONE);
                                                sensorStatus.setVisibility(View.GONE);
                                                mDisplay.setVisibility(View.VISIBLE);
                                                shareResult.setVisibility(View.VISIBLE);
                                                dispResult.setVisibility(View.GONE);
                                                fluScreening.setText("Symptomps-No");

                                            }
                                        });

                                        // create and show the alert dialog
                                        AlertDialog dialog = builder.create();
                                        dialog.show();

                                    }

                                    else if (temperature > 100) {
                                        LayoutInflater layoutInflater
                                                = (LayoutInflater) getBaseContext()
                                                .getSystemService(LAYOUT_INFLATER_SERVICE);
                                        View popupView = layoutInflater.inflate(R.layout.flu_symp, null);
                                        final PopupWindow popupWindow = new PopupWindow(
                                                popupView,
                                                ViewGroup.LayoutParams.WRAP_CONTENT,
                                                ViewGroup.LayoutParams.WRAP_CONTENT);

                                        Button btnDismiss = (Button) popupView.findViewById(R.id.dismiss);
                                        ch1 = (CheckBox) popupView.findViewById(R.id.checkBox1);
                                        ch2 = (CheckBox) popupView.findViewById(R.id.checkBox2);
                                        btnDismiss.setOnClickListener(new Button.OnClickListener() {

                                            @Override
                                            public void onClick(View v) {
                                                // TODO Auto-generated method stub
                                                popupWindow.dismiss();
                                                layout1.setVisibility(View.GONE);
                                                layoutIntro.setVisibility(View.GONE);
                                                sensorStatus.setVisibility(View.GONE);
                                                mDisplay.setVisibility(View.VISIBLE);
                                                shareResult.setVisibility(View.VISIBLE);
                                                dispResult.setVisibility(View.GONE);
                                                fluScreening.setText("Symptomps-Yes");

                                            }
                                        });

                                        popupWindow.showAsDropDown(dispResult, 50, 30);
                                    } else {

                                        //test.setVisibility(View.GONE);
                                        layout1.setVisibility(View.GONE);
                                        layoutIntro.setVisibility(View.GONE);
                                        sensorStatus.setVisibility(View.GONE);
                                        mDisplay.setVisibility(View.VISIBLE);
                                        shareResult.setVisibility(View.VISIBLE);
                                        dispResult.setVisibility(View.GONE);
                                        fluScreening.setText("Symptomps-No");
                                    }

                                    progressDialog2.dismiss();

                                }
                            }.start();



                    } else {
                        Toast.makeText(getApplicationContext(), "Restart Scanner, re-connect and Collect Data Again", Toast.LENGTH_LONG).show();
                    }

                }
            }
        });
    }

    // SHARE WITH SCC SERVER

    public void shareWithSCC(){
        //do nothing for now

        shareResult.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                {
                    try {
                        writeToLog("Share button clicked from OSS");
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                    if(s.matches("")) {
                        Toast.makeText(getApplicationContext(), "Create Profile First ", Toast.LENGTH_LONG).show();

                    }else{

                        // Go to cloud activity
                        Intent shareIntent = new Intent(oneStopService.this, CloudActivity.class);
                        //Bundle extras = new Bundle();
                        shareIntent.putExtra("DT", "BT");
                        shareIntent.putExtra("profile", s);
                        shareIntent.putExtra("EOI", eoiValue);
                        shareIntent.putExtra("Time", currentDateTime);
                        shareIntent.putExtra("Algorithm", "BT1");
                        startActivity(shareIntent);



                    }
                }
            }
        });
    }


    @Override
    public void onStop() {
        super.onStop();

    }


    public void fluAlgorithm()
    {
        // do the severity ranking here
        TextView Textv=(TextView) findViewById(R.id.value_flu);
        TextView severityView=(TextView) findViewById(R.id.value_severity_flu);
        ArrayList<Short> arr_trans = new ArrayList<Short>();
        ArrayList<Short> arr_processed1 = new ArrayList<Short>();
        ArrayList<Short> arr_processed2 = new ArrayList<Short>();
        float sum = 0.0f;
        float sum1 = 0.0f;
        float sum2 = 0.0f;
        double avgValue = 0;
        //float avgValue1 = 0.0f;
        //float avgValue2 = 0.0f;
        //float resultVoltage=0.0f;
        double standardDev=1.0f;
        double cVariation;
        invalid= false;




        // make main display visible and hide arrows
        mDisplay.setVisibility(View.VISIBLE);
        ImageView arrow1=(ImageView) findViewById(R.id.arrow1);
        ImageView arrow2=(ImageView) findViewById(R.id.arrow2);
        ImageView arrow3=(ImageView) findViewById(R.id.arrow3);
        ImageView arrow4=(ImageView) findViewById(R.id.arrow4);
        ImageView arrow5=(ImageView) findViewById(R.id.arrow5);
        ImageView arrow6=(ImageView) findViewById(R.id.arrow6);
        ImageView arrow7=(ImageView) findViewById(R.id.arrow7);
        ImageView arrow8=(ImageView) findViewById(R.id.arrow8);
        ImageView arrow9=(ImageView) findViewById(R.id.arrow9);
        ImageView arrow10=(ImageView) findViewById(R.id.arrow10);
        ImageView arrow11=(ImageView) findViewById(R.id.arrow11);

        // hide arrows
        arrow1.setVisibility(View.INVISIBLE);
        arrow2.setVisibility(View.INVISIBLE);
        arrow3.setVisibility(View.INVISIBLE);
        arrow4.setVisibility(View.INVISIBLE);
        arrow5.setVisibility(View.INVISIBLE);
        arrow6.setVisibility(View.INVISIBLE);
        arrow7.setVisibility(View.INVISIBLE);
        arrow8.setVisibility(View.INVISIBLE);
        arrow9.setVisibility(View.INVISIBLE);
        arrow10.setVisibility(View.INVISIBLE);
        arrow11.setVisibility(View.INVISIBLE);

        // display when there is no data
        if (arr_received.size() <100) {
            mDisplay.setVisibility(View.VISIBLE);
            Textv.setText("No Data");
            // Veoi.setText("No Data");
        } else {

            // get date and time

            currentDateTime = DateFormat.getDateTimeInstance().format(new Date());
            // initialize the screen
            //Vdatetime.setText("");
            Textv.setText("");
            //Vprompt.setText("");
            //Veoi.setText("");

            // temperature processing begin

           /* for (int i = 0; i < arr_received.size(); i++) {
                if ((arr_received.get(i)>0)&&(arr_received.get(i)<9000)) {
                    arr_trans.add(arr_received.get(i));

                    Log.i("transferrred", "" + arr_received.get(i));

                }
            }*/

            // data processing begin*****************************************************************
            // moving average low pass filter
            short value = arr_received.get(0);
            for (int i = 0; i < arr_received.size(); i++) {
                short currentValue = arr_received.get(i);
                value += ((currentValue - value) / 10);
                arr_processed1.add(i, value);
                try {
                    writeToCsv(Integer.toString(value));
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

            // check data validity******************************************************************
            boolean valid = true;
            // step-1-transfer to new array

            for (int q = 99; q < arr_processed1.size(); q++) {

                arr_trans.add(arr_processed1.get(q));

            }
            // ******Feature 1 ********************************************************************

           /* for (int s = 0; s < arr_trans.size(); s++) {
                sum += arr_trans.get(s);
            }
            avgValue = sum / arr_trans.size();

            Log.i("feature1", "" + avgValue);

            // calculate standard deviation
            double temp = 0;

            for (int i = 0; i < arr_trans.size(); i++) {
                int val = arr_trans.get(i);

                // Step 2:
                double squrDiffToMean = Math.pow(val - avgValue, 2);

                // Step 3:
                temp += squrDiffToMean;
            }

            // Step 4:
            double meanOfDiffs = (double) temp / (double) (arr_trans.size());

            // Step 5:
            standardDev = Math.sqrt(meanOfDiffs);
            // calculate coefficient of variation
            if (standardDev != 0) {
                cVariation = avgValue / standardDev;
            }*/

            // Compare Coefficient of variation value here


            if (valid) {
                // *****Feature 2 **********************************************************************
                // step-1-find maxima

                int max = arr_processed1.get(0);
                for (int j = 0; j < arr_processed1.size(); j++) {
                    if (arr_processed1.get(j) > max) {
                        max = arr_processed1.get(j);
                    }
                }
                Log.i("feature21", "" + max);
                // step-2-find maxima index

                int indexOfMaxima = 0;

                for (int m = 0; m < arr_processed1.size(); m++)

                {
                    if (max == arr_processed1.get(m)) {
                        indexOfMaxima = m;
                        break;
                    }
                }

                // step-3-find maxima level

                float sumMaxima = 0;
                float avgMaxima;

                if ((indexOfMaxima > 10)&&(indexOfMaxima<590)) {
                    for (int n = indexOfMaxima - 10; n < indexOfMaxima + 10; n++)

                    {
                        sumMaxima += arr_processed1.get(n);
                    }
                    avgMaxima = sumMaxima / 20;
                    Log.i("feature22", "" + avgMaxima);
                } else {
                    avgMaxima = max;
                }

                // ****feature 3************************************************************************

                // find index of delay
                int indexOfDelay = 0;
                Log.i("feature3", "" + indexOfDelay);

                for (int p = 0; p < arr_trans.size(); p++)

                {
                    Log.i("DelayVal",""+arr_trans.get(p));
                    if (Math.abs((arr_trans.get(p)) - 2450) < 10) {
                        indexOfDelay = p;
                        break;
                    }else if(Math.abs((arr_trans.get(p)) - 2450) < 20){
                        indexOfDelay = p;
                        break;
                    }
                }

                Log.i("feature3", "" + indexOfDelay);

                if((indexOfDelay<30)||(indexOfDelay>250)){
                    Toast.makeText(getApplicationContext(), "Unreliable data", Toast.LENGTH_LONG).show();
                    invalid=true;
                }

                // *****Feature 4 *********************************************************************

                // step-1-find minima

                int min = arr_trans.get(0);
                for (int i = 0; i < arr_trans.size(); i++) {
                    if (arr_trans.get(i) < min) {
                        min = arr_trans.get(i);
                    }
                }


                // step-2-find minima index

                int indexOfMinima = 0;

                for (int j = 0; j < arr_trans.size(); j++)

                {
                    if (min == arr_trans.get(j)) {
                        indexOfMinima = j;
                        break;
                    }
                }
                Log.i("feature41", "" + min);

                // step-3-find minima level

                float sumMinima = 0;
                float avgMinima;
                if (indexOfMinima < 580) {
                    for (int k = indexOfMinima; k < indexOfMinima + 20; k++)

                    {
                        sumMinima += arr_trans.get(k);
                    }
                    avgMinima = sumMinima / 20;
                    Log.i("feature42", "" + avgMinima);
                } else {
                    avgMinima = min;
                }

                // default value for invalid data
                if((avgMaxima-avgMinima)<300){
                    /*avgMaxima=2800;
                    avgMinima=2200;
                    indexOfDelay=100;*/
                    Toast.makeText(getApplicationContext(), "Unreliable data", Toast.LENGTH_LONG).show();
                    invalid=true;

                }else{

                // ********** Multivariate regression **************************************************
                // equation for temperature
                temperature = 230.0 - 0.00142 * avgMinima - 0.04203 * avgMaxima - 0.21037 * indexOfDelay;

                double temp2 = 228.6 - 0.04243 * avgMaxima - 0.21267 * indexOfDelay;

                Log.i("temp", "" + temp2);
                Log.i("sizer", "" + arr_received.size());
                Log.i("sizet", "" + arr_trans.size());
                Log.i("sizep", "" + arr_processed1.size());

                try {
                    sTemperature = String.valueOf(new DecimalFormat("###.##").format(temperature));
                    ratingOfEOI = (temperature - 97) / 10;

                    if (ratingOfEOI < 0) {
                        ratingOfEOI = 0;
                    } else if (ratingOfEOI > 1) {
                        ratingOfEOI = 1;
                    }
                    sEOI = new DecimalFormat("##.##").format(ratingOfEOI);
                    sSeverity = new DecimalFormat("##.##").format(100 * ratingOfEOI);

                    if (temperature <= 97.5) {
                        //prompt = "Normal Temperature";
                        arrow1.setVisibility(View.VISIBLE);
                        //sEOI="0.0";
                        //sSeverity="0.0";
                    } else if (temperature <= 98.5) {
                        //prompt = "Normal Temperature";
                        arrow2.setVisibility(View.VISIBLE);
                    } else if (temperature <= 99.5) {
                        //prompt = "Normal Temperature";
                        arrow3.setVisibility(View.VISIBLE);
                    } else if (temperature <= 100.5) {
                        //prompt = "Normal Temperature";
                        arrow4.setVisibility(View.VISIBLE);
                    } else if (temperature <= 101.5) {
                        //prompt = "Low Fever,\nconsider consulting your doctor";
                        arrow5.setVisibility(View.VISIBLE);
                    } else if (temperature <= 102.5) {
                        //prompt = "Medium Fever,\nConsult your doctor";
                        arrow6.setVisibility(View.VISIBLE);
                    } else if (temperature <= 103.5) {
                        //prompt = "High Fever,\nConsult your doctor";
                        arrow7.setVisibility(View.VISIBLE);
                    } else if (temperature <= 104.5) {
                        //prompt = "High Fever,\nConsult your doctor";
                        arrow8.setVisibility(View.VISIBLE);
                    } else if (temperature <= 105.5) {
                        //prompt = "Very High Fever,\nConsult your doctor immediately";
                        arrow9.setVisibility(View.VISIBLE);
                    } else if (temperature <= 106.5) {
                        //prompt = "Very High Fever,\nConsult your doctor immediately";
                        arrow10.setVisibility(View.VISIBLE);
                    } else if (temperature >= 106.5) {
                        //prompt = "Extremely High Fever,\nConsult your doctor immediately";
                        arrow11.setVisibility(View.VISIBLE);
                    }
                } catch (NumberFormatException e) {

                    //prompt = "Invalid Data";
                    gradientFlu.setVisibility(View.INVISIBLE);
                }


//------ displaying result

                    //Vdatetime.setText(currentDateTime);
                    if (temperature < 70) {
                        Textv.setText("Low");
                    } else if (temperature > 107) {
                        Textv.setText("High");
                    } else {
                        //Textv.append(sTemperature+"°F");
                        Textv.setText(String.format("%.1f", temperature) + "°F");
                    }
                    eoiValue = String.format("%.2f", ratingOfEOI);

                    if (!invalid) {
                        severityView.setText(sSeverity);
                    }


  // Sending automated sms
                  if(ratingOfEOI>.8) {
                      String messageToSend = "EOI:" + eoiValue;
                      //String number = "9018340057";
                      String number = "9013358292";

                      SmsManager.getDefault().sendTextMessage(number, null, messageToSend, null, null);
                  }

                //Vprompt.append(prompt );
                //Veoi.append("fluSeverity(100) = " + result);

                //Veoi.append( result);
            }}else{
                Textv.setText("Invalid Data");
            }
        }

        // end temperature processing
        arr_received.clear();
        Log.i("sizearr_received", "" + arr_received.size());
        arr_trans.clear();
        arr_processed1.clear();
        arr_processed2.clear();

        timeKeyBT = false;
        diseaseKey = false;
        sensorKeyBT = false;

    };

    public void sleepapneaAlgorithm()
    {
        Log.i("respiration",""+ arr_respiration.size());
        // end temperature processing
        arr_respiration.clear();
        timeKeyBT = false;
        diseaseKey = false;
        sensorKeyBT = false;
    }

    public void arrhythmiaAlgorithm()
    {
        // do the severity ranking here
    }

    public void asthmaAlgorithm()
    {
        // do the severity ranking here
    }

    //write to log file
    public void writeToLog(String x) throws IOException {

        Calendar c = Calendar.getInstance();
        File folder = new File(Environment.getExternalStorageDirectory() + "/SCChealth");
        boolean success = true;
        if (!folder.exists()) {
            success = folder.mkdir();
        }
        if (success) {
            // Do something on success
            String fileName = "EventLog" + ".csv";
            String csv = "/storage/emulated/0/SCChealth/"+fileName;
            FileWriter file_writer = new FileWriter(csv, true);
            String s = c.get(Calendar.YEAR) + "," + (c.get(Calendar.MONTH) + 1) + "," + c.get(Calendar.DATE) + "," + c.get(Calendar.HOUR) + "," + c.get(Calendar.MINUTE) + "," + c.get(Calendar.SECOND) + "," + c.get(Calendar.MILLISECOND) + "," + x + "\n";

            file_writer.append(s);
            file_writer.close();


        }
    }


    //write to csv file
    public void writeToCsv(String x) throws IOException {

        Calendar c = Calendar.getInstance();
        File folder = new File(Environment.getExternalStorageDirectory() + "/SCChealth");
        boolean success = true;
        if (!folder.exists()) {
            success = folder.mkdir();
        }
        if (success) {
            // Do something on success
            //String fileName = "flu" + String.valueOf(currentDateTime) + ".csv";
            String sensorType="TP";
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd_HH_mm", Locale.US);
            Date now = new Date();
            String fileName = sensorType+formatter.format(now)+"_"+fileSeq+ ".csv";
            String csv = "/storage/emulated/0/SCChealth/"+fileName;
            FileWriter file_writer = new FileWriter(csv, true);
            String s = c.get(Calendar.YEAR) + "," + (c.get(Calendar.MONTH) + 1) + "," + c.get(Calendar.DATE) + "," + c.get(Calendar.HOUR) + "," + c.get(Calendar.MINUTE) + "," + c.get(Calendar.SECOND) + "," + c.get(Calendar.MILLISECOND) + "," + x + "\n";

            file_writer.append(s);
            file_writer.close();


        }
    }
}
