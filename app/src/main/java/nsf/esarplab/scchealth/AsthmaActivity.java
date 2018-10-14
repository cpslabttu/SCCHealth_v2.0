package nsf.esarplab.scchealth;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphView.GraphViewData;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.LineGraphView;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import nsf.esarplab.bluetoothlibrary.BluetoothSPP;
import nsf.esarplab.bluetoothlibrary.BluetoothState;
import nsf.esarplab.bluetoothlibrary.DeviceList;

import static nsf.esarplab.scchealth.R.id.graph1;

//import com.google.android.gms.common.api.GoogleApiClient;

public class AsthmaActivity extends AppCompatActivity {
    BluetoothSPP bt;
    private TextView hrData, oximetryData, connectionRead,severityDisplay;
    EditText etMessage;
    //private GoogleApiClient client;
    private Button connectScanner, test, dispResult;
    private boolean diseaseKey = false;
    private boolean sensorKey = false;
    private boolean timeKey = false;
    private boolean connected=false;
    private int sensor=1;
    private int fileSeq=1;
    private LinearLayout graph, manualSpo2,mainDisplay,sensorSelection;
    private ProgressDialog progressDialog;
    private final Handler mHandler = new Handler();
    private Runnable mTimer1;
    private CountDownTimer Count;
    private ArrayList<String> arr_hex = new ArrayList<String>();
    private ArrayList<Short> arr_received = new ArrayList<Short>();
    Menu menu;
    String s = "";
    private GraphView graphView1;
    private GraphViewSeries exampleSeries1;
    private double sensorX = 0;
    private List<GraphViewData> seriesX;
    int dataCount = 1;
    private double EOI;
    private String sSeverity;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asthma);

        // show action bar
        ActionBar myActionBar = getSupportActionBar();
        myActionBar.show();
        TextView mNameText = (TextView) findViewById(R.id.display_name);
        bt = new BluetoothSPP(this);
        // receive intent
        test=(Button) findViewById(R.id.test);
        hrData = (TextView) findViewById(R.id.display_bdt);
        oximetryData=(TextView) findViewById(R.id.display_spo2);
        connectionRead = (TextView) findViewById(R.id.textStatus);
        graph=(LinearLayout) findViewById(graph1);
        connectScanner = (Button) findViewById(R.id.cScanner);
        severityDisplay=(TextView) findViewById(R.id.display_eoi);
        dispResult = (Button) findViewById(R.id.displayResult);
        manualSpo2=(LinearLayout) findViewById(R.id.manualspo2);
        mainDisplay=(LinearLayout) findViewById(R.id.md);
        sensorSelection=(LinearLayout) findViewById(R.id.sensorSelection);


        //Show active profile
        SharedPreferences prefs = getSharedPreferences("logindetails",MODE_PRIVATE);
        String Uname =  prefs.getString("loginname","Default");
        mNameText.setText("\t\t"+Uname);

        s = mNameText.getText().toString().trim();

        //show graph

        seriesX = new ArrayList<GraphViewData>();
        // init example series data
        exampleSeries1 = new GraphViewSeries(new GraphViewData[] {});

        graphView1 = new LineGraphView(
                this // context
                , "Real time plot" // heading
        );
        graphView1.setViewPort(0, 600);
        graphView1.setScalable(true);
        //graphView1.setDrawBackground(true);

        graphView1.addSeries(exampleSeries1); // data
        //Viewport viewport = graph.getViewport();
        LinearLayout layout = (LinearLayout) findViewById(R.id.graph1);
        layout.addView(graphView1);

        //hide the graph view and edit text
        graph.setVisibility(View.GONE);
        manualSpo2.setVisibility(View.GONE);
        mainDisplay.setVisibility(View.GONE);
        sensorSelection.setVisibility(View.GONE);


        // Set active profile


        /*//reading profile from file
        try {
            FileInputStream fileIn = openFileInput("mytextfile.txt");
            InputStreamReader InputRead = new InputStreamReader(fileIn);
            char[] inputBuffer = new char[READ_BLOCK_SIZE];
            *//*String s="";*//*
            int charRead;

            while ((charRead = InputRead.read(inputBuffer)) > 0) {
                // char to string conversion
                String readstring = String.copyValueOf(inputBuffer, 0, charRead);
                s += readstring;
            }
            InputRead.close();
            *//*mNameText.setText(s);*//*
            *//*Toast.makeText(getBaseContext(), s,Toast.LENGTH_SHORT).show();*//*

        } catch (Exception e) {
            e.printStackTrace();
        }
        mNameText.setText(s);*/

        // set the bluetooth connection

        bt.setOnDataReceivedListener(new BluetoothSPP.OnDataReceivedListener() {
            public void onDataReceived(byte[] data, String message) {
                short val = 0;
                String readAscii = new String(data);
                //Log.i("Str@activity", readAscii);
                //textReceived.append(message + "\n");
                if (timeKey) {
                    //receive data

                    arr_hex.add(message);
                    Log.i("size_arr_hex",""+arr_hex.size());


                    if (arr_hex.size() == 2) {
                        String catHex = arr_hex.get(0) + arr_hex.get(1);
                        /*int b0 = (arr_hex.get(0) & 255); // converts to unsigned
                        int b1 = (arr_hex.get(1) & 255); // converts to unsigned
                        int val = b0 << 8 | b1;*/
                        Log.i("val1@final", catHex);
                        val = (short) (Integer.parseInt(catHex, 16));
                        Log.i("val2@final", String.valueOf(val));
                        arr_received.add(val);
                        //Textv.append(Integer.toString(val) + "\n");
                        seriesX.add(new GraphViewData(dataCount, val));
                        dataCount++;
                        /*if (arr_received.size() > 800) {
                            seriesX.remove(0);
                            graphView1.setViewPort(dataCount - 800, 800);

                        }*/
                        try {
                            writeToCsv(Integer.toString(val));
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        arr_hex.clear();
                        Log.i("sizearr_received", "" + arr_received.size());
                    }


                } else {
                    if (readAscii.equals("OS")) {

                        bt.send("TP", true);
                        diseaseKey = true;

                    } else {
                        if (readAscii.equals("TP") && diseaseKey) {
                            bt.send("00040", true);
                            sensorKey = true;
                        } else {
                            if (readAscii.equals("00040") && diseaseKey && sensorKey) {
                                bt.send("OK", true);
                                timeKey = true;

                            } else {
                               /* mDisplay.setVisibility(View.VISIBLE);
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
                                Toast.makeText(getApplicationContext(), "Failed Handshake", Toast.LENGTH_LONG).show();
                                Count.cancel();
                                progressDialog.dismiss();*/

                            }
                        }
                    }
                }
            }

        });

       /* bt.setOnDataReceivedListener(new BluetoothSPP.OnDataReceivedListener() {
            public void onDataReceived(byte[] data, String message) {
                short val = 0;
                String readAscii = new String(data);
                //textReceived.append(message + "\n");
                if (tenReceived) {
                    //receive data
                   *//* switch (sensor) {
                        case 1: {
                            arr_hex.add(message);
                            break;
                        }
                        case 2: {

                            oximetryData.append(message + "\n");
                            break;
                        }

                    }
*//*
                    arr_hex.add(message);
                    if (arr_hex.size() == 2) {
                        String catHex = arr_hex.get(0) + arr_hex.get(1);
                        *//*int b0 = (arr_hex.get(0) & 255); // converts to unsigned
                        int b1 = (arr_hex.get(1) & 255); // converts to unsigned
                        int val = b0 << 8 | b1;*//*
                        Log.i("val1@final", catHex);
                        val = (short) (Integer.parseInt(catHex, 16));
                        Log.i("val2@final", String.valueOf(val));
                        arr_received.add(val);
                        //Textv.append(Integer.toString(val) + "\n");
                        seriesX.add(new GraphViewData(dataCount, val));
                        dataCount++;
                        if (arr_received.size() > 600) {
                            seriesX.remove(0);
                            graphView1.setViewPort(dataCount - 600, 600);

                        }
                        arr_hex.clear();
                        Log.i("sizearr_received", "" + arr_received.size());

                }
                } else {
                    if (readAscii.equals("OS")) {

                        switch (sensor) {
                            case 1: {
                                bt.send("TP", true);
                                pdReceived = true;
                                break;
                            }
                            case 2: {
                                bt.send("PO", true);
                                pdReceived = true;
                                break;
                            }
                        }
                    } else {
                        if (readAscii.equals("TP") && pdReceived) {
                            bt.send("20", true);
                            hrReceived = true;
                        } else if (readAscii.equals("PO") && pdReceived) {
                            bt.send("5", true);
                            poReceived = true;
                        } else {
                            if (readAscii.equals("20") && pdReceived && hrReceived) {
                                bt.send("OK", true);
                                tenReceived = true;
                            }
                            else if (readAscii.equals("5") && pdReceived && poReceived) {
                                bt.send("OK", true);
                                tenReceived = true;
                            } else {
                                hrData.append("Failed Handshake");
                            }
                        }
                    }
                }
            }

        });*/

        bt.setBluetoothConnectionListener(new BluetoothSPP.BluetoothConnectionListener() {
            public void onDeviceDisconnected() {
                connectionRead.setText("Status : Not connect");
                menu.clear();
                getMenuInflater().inflate(R.menu.menu_connection, menu);
            }

            public void onDeviceConnectionFailed() {
                connectionRead.setText("Status : Connection failed");
            }

            public void onDeviceConnected(String name, String address) {
                connectionRead.setText("Status : Connected to " + name);
                connected=true;
                menu.clear();
                getMenuInflater().inflate(R.menu.menu_disconnection, menu);
            }
        });



        // set corresponding display for selected sensor
        final LinearLayout hrlayout = (LinearLayout) findViewById(R.id.hr_result);
        final LinearLayout spo2layout = (LinearLayout) findViewById(R.id.spo2_result);
        RadioButton rbOxygen = (RadioButton) findViewById(R.id.rb_o2);
        RadioButton rbheartRate = (RadioButton) findViewById(R.id.rb_hr);
        hrlayout.setVisibility(View.VISIBLE);
        spo2layout.setVisibility(View.GONE);

        RadioGroup radioGroup = (RadioGroup) findViewById(R.id.rg1);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
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
        });

// inflate the screen for info and instruction
        final TextView btnOpenPopup = (TextView) findViewById(R.id.info);
        btnOpenPopup.setOnClickListener(new Button.OnClickListener() {

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

        final TextView btnOpenInstruction = (TextView) findViewById(R.id.inst);
        btnOpenInstruction.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                LayoutInflater layoutInflater
                        = (LayoutInflater) getBaseContext()
                        .getSystemService(LAYOUT_INFLATER_SERVICE);
                View popupView = layoutInflater.inflate(R.layout.inst_copd, null);
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


        // connect scanner by bluetooth

        connectScanner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //bt.setDeviceTarget(BluetoothState.DEVICE_OTHER);
            /*
			if(bt.getServiceState() == BluetoothState.STATE_CONNECTED)
    			bt.disconnect();*/
                Intent bleIntent = new Intent(AsthmaActivity.this, copdActivity.class);
                startActivity(bleIntent);
                //startActivityForResult(intent, BluetoothState.REQUEST_CONNECT_DEVICE);

            }
        });

        dispResult.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    writeToLog("Compute button clicked from Flu");
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                copdAlgorithm(v);

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
        }

        else if (id == R.id.menu_reinitialize) {
            hrData.setText("");
            oximetryData.setText("");
            arr_received.clear();
            seriesX.clear();
            dataCount = 1;
            diseaseKey = false;
            sensorKey = false;
            timeKey = false;
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
            } else {
                Toast.makeText(getApplicationContext()
                        , "BluetoothActivity was not enabled."
                        , Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mTimer1 = new Runnable() {
            @Override
            public void run() {
                GraphViewData[] gvd = new GraphViewData[seriesX.size()];
                seriesX.toArray(gvd);
                exampleSeries1.resetData(gvd);
                mHandler.post(this); //, 100);
            }
        };
        mHandler.postDelayed(mTimer1, 100);

    }

    public void setup() {

        test.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                {
                    if (connected) {
                        bt.send("OS", true);
                        fileSeq++;
                        graph.setVisibility(View.VISIBLE);
                        // progress indicator
                        progressDialog = new ProgressDialog(AsthmaActivity.this,
                                R.style.AppTheme_Dark_Dialog);
                        progressDialog.setIndeterminate(true);
                        progressDialog.setMessage("Collecting data...");

                        Count=new CountDownTimer(500, 100) {

                            public void onTick(long millisecondsUntilDone) {

                                progressDialog.show();
                            }

                            @Override
                            public void onFinish() {
                                Log.i("Done", "Count Down Timer Finished");
                                progressDialog.dismiss();

                            }
                        }.start();

                    } else {
                        Toast.makeText(getApplicationContext(), "Get Connected First", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }


    public void copdAlgorithm(View v)
    {
        int Disease = 1; // COPD 1 and Asthma 0
        int[] HRarray = {70, 90, 80};
        int[] SParray = {100, 80, 91};
        double severity=computeEOI(HRarray,SParray, Disease);
        sSeverity=String.valueOf(new DecimalFormat("###.##").format(severity));
        severityDisplay.setText(sSeverity);

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
            String sensorType="HR";
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

    // EOI calculation

    public double computeEOI(int[] HRarray, int[] Sparray, int Disease) {
        // TODO Auto-generated constructor stub

        //System.out.println(Arrays.toString(HRarray));
        //System.out.println(Arrays.toString(Sparray));

        /** if (Disease == 1) {
         *String DiseaseString = {"COPD"};
         *}else {
         *	String DiseaseString = {"Asthma"};
         } */

        //System.out.println("The Disease is " +DiseaseString);

        // Data Processing
        Map<Integer, Integer> values = new HashMap<Integer, Integer>();
//SpO2 Labeling
        int[] definition = {19, 80, 85, 90, 92, 100}; // SpO2 Guidelines
        int[] buckets = new int[definition.length];

        Random rnd = new Random();
        for (int i=0; i<5; i++) {
            int time = rnd.nextInt(101)+20; // Random 100 SpO2 Array
            System.out.println( "" + time  );
            Integer calls = values.get(time);
            if (calls == null) {
                calls = Integer.valueOf(0);
            }
            calls += 1;
            values.put(time, calls);
        }

        for (int time : values.keySet()) {
            for (int i=definition.length-1; i>=0; i--) {
                if (time >= definition[i]) {
                    buckets[i] += values.get(time);
                    break;
                }
            }
        }
        // Feature Extraction SpO2
        int sumSp =0;	double[] percentageSp = new double[definition.length-1];

        for (int i=0; i<definition.length; i++) {
            String period = "";
            if (i == definition.length-1) {
                period = "greater than " + definition[i] + "ms";
            } else {
                period = "between " +
                        (definition[i]+1) +
                        " and " +
                        definition[i+1] + "%";
                sumSp += buckets[i];
            }
            System.out.println(buckets[i] + " came back " + period);
        }
        // Produce SpO2 Percentages
        if (sumSp == 0)
            System.out.println ("No valid HR samples were found");
        else
        {
            System.out.println(sumSp + " are the total SpO2 samples");
            for (int i=0; i<definition.length-1; i++) {
                DecimalFormat fmt = new DecimalFormat ("0.##");
                percentageSp[i] = (double)buckets[i]*100/sumSp;
                String period = "";
                period = "between " +
                        (definition[i]+1) +
                        " and " +
                        definition[i+1] + " %";
                System.out.println(fmt.format(percentageSp[i]) + " Percentage of time in " + period);
            }
        }
        // Heart rate binding
        int[] HRlevel = {39, 90, 100, 110, 120, 200}; // HR Guidelines
        int[] bins = new int[HRlevel.length];

        Random rnd1 = new Random();
        for (int i=0; i<5; i++) {
            int HRArray = rnd1.nextInt(201)+40; // Random 100 Heart Rate Array
            System.out.println( "" + HRArray  );
            Integer calls = values.get(HRArray);
            if (calls == null) {
                calls = Integer.valueOf(0);
            }
            calls += 1;
            values.put(HRArray, calls);
        }

        for (int HRArray : values.keySet()) {
            for (int i=HRlevel.length-1; i>=0; i--) {
                if (HRArray >= HRlevel[i]) {
                    bins[i] += values.get(HRArray);
                    break;
                }
            }
        }
        // Feature Extraction Heart Rate
        int sumHR =0;	double[] percentageHR = new double[HRlevel.length-1];

        for (int i=0; i<HRlevel.length; i++) {
            String period = "";
            if (i == HRlevel.length-1) {
                period = "greater than " + HRlevel[i] + " BPM";
            } else {
                period = "between " +
                        (HRlevel[i]+1) +
                        " and " +
                        HRlevel[i+1] + "BPM";
                sumHR += bins[i];
            }
            //System.out.println(bins[i] + " came back " + period);
        }
        // Produce HR Percentages
        if (sumHR == 0)
            System.out.println ("No valid HR samples were found");
        else
        {
            System.out.println(sumHR + " are the total HR samples");
            for (int i=0; i<HRlevel.length-1; i++) {
                DecimalFormat fmt = new DecimalFormat ("0.##");
                percentageHR[i] = (double)bins[i]*100/sumHR;
                String period = "";
                period = "between " +
                        (HRlevel[i]+1) +
                        " and " +
                        HRlevel[i+1] + " BPM";
                System.out.println(fmt.format(percentageHR[i]) + " Percentage of time in " + period);
            }
        }
        // Tangent Model
        double[] raw_alpha_degree= {1, 22.5, 45, 67.5, 89};
        double[] radians = new double[raw_alpha_degree.length];
        // Percentages
        double sumtan =0; double[] percentagetan = new double[raw_alpha_degree.length];

        for (int i=0; i<raw_alpha_degree.length; i++) {
            radians[i] = Math.toRadians(raw_alpha_degree[i]);
            // print the tangent of these doubles
            System.out.println("Tangent(" + radians[i] + ")=" + Math.tan(radians[i]));
            sumtan += Math.tan(radians[i]);
        }
        System.out.println(sumtan + " is the total tangent sum");
        for (int i=0; i<raw_alpha_degree.length; i++) {
            DecimalFormat fmt = new DecimalFormat ("0.##");
            percentagetan[i] = (double)Math.tan(radians[i])*100/sumtan;
            System.out.println(fmt.format(percentagetan[i]) + " Percentage of share for tangent");
        }

        // EOI Generation
        double sum_score =0, Min = 2.8551, Range = 0, betaHR, betaSp;
        for (int i=0; i<raw_alpha_degree.length; i++) {
            if (Disease == 1)
            {Range = 4.3833e+03;
                betaHR= 0.4;
                betaSp=0.6;
            }else {
                Range = 3.2781e+03;
                betaHR= 0.5;
                betaSp=0.5;
            }
            double score = betaHR*(percentagetan[i])*(percentageHR[i])+betaSp*(percentagetan[i])*(percentageSp[i]);
            System.out.println(score + " is the score");
            sum_score += score;
            System.out.println(sum_score + " is the total sum");
        }
        EOI = (sum_score-Min)/Range;
        System.out.println(EOI + " is the EOI");
        return EOI;
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
    @Override
    public void onStop() {
        super.onStop();

    }
}