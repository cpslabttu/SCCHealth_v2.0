package nsf.esarplab.scchealth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class copdActivity extends AppCompatActivity {

    Boolean isListeningHeartRate = false;

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    BluetoothAdapter bluetoothAdapter;
    BluetoothGatt bluetoothGatt;
    BluetoothDevice bluetoothDevice;

    private Button btnStartConnecting, btnGetBatteryInfo, btnGetHeartRate, btnWalkingInfo, btnStartVibrate, btnStopVibrate, computeSeverity,shareSeverity;
    EditText txtPhysicalAddress;
    TextView txtState, txtByte, severityDisplay;
    private EditText spo2Input;
    private String mDeviceName;
    private String mDeviceAddress;
    private String sSeverity;
    private double EOI;
    private int fileSeq=0;
    String s = "s0";
    private String currentDateTime = "";
    private ArrayList<Short> arr_HR = new ArrayList<Short>();
    private ArrayList<String> arr_SPO2 = new ArrayList<String>();
    int[] HRarray={80,80,80,80,80};
    int[] SParray={85,85,80,80,80};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_copd);
        spo2Input=(EditText)findViewById(R.id.manualSp);
        computeSeverity=(Button) findViewById(R.id.compSeverity);
        shareSeverity=(Button) findViewById(R.id.shareSeverity);

        initializeObjects();
        initilaizeComponents();
        initializeEvents();

        getBoundedDevice();

        shareSeverity.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                {
                    try {
                        writeToLog("Share button clicked from COPD");
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                    if(s.matches("")) {
                        Toast.makeText(getApplicationContext(), "Create Profile First ", Toast.LENGTH_LONG).show();

                    }else{

                        // Go to cloud activity
                        Intent shareIntent = new Intent(copdActivity.this, CloudActivity.class);
                        //Bundle extras = new Bundle();
                        shareIntent.putExtra("DT", "PD");
                        shareIntent.putExtra("profile", s);
                        shareIntent.putExtra("EOI", EOI);
                        shareIntent.putExtra("Time", currentDateTime);
                        shareIntent.putExtra("Algorithm", "TM");
                        startActivity(shareIntent);



                    }
                }
            }
        });

    }

    void getBoundedDevice() {

        mDeviceName = getIntent().getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = getIntent().getStringExtra(EXTRAS_DEVICE_ADDRESS);
        txtPhysicalAddress.setText(mDeviceAddress);

        Set<BluetoothDevice> boundedDevice = bluetoothAdapter.getBondedDevices();
        for (BluetoothDevice bd : boundedDevice) {
            if (bd.getName().contains("MI Band 2")) {
                txtPhysicalAddress.setText(bd.getAddress());
            }
        }
    }

    void initializeObjects() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    void initilaizeComponents() {
        btnStartConnecting = (Button) findViewById(R.id.btnStartConnecting);
        btnGetBatteryInfo = (Button) findViewById(R.id.btnGetBatteryInfo);
        btnWalkingInfo = (Button) findViewById(R.id.btnWalkingInfo);
        btnStartVibrate = (Button) findViewById(R.id.btnStartVibrate);
        btnStopVibrate = (Button) findViewById(R.id.btnStopVibrate);
        btnGetHeartRate = (Button) findViewById(R.id.btnGetHeartRate);
        txtPhysicalAddress = (EditText) findViewById(R.id.txtPhysicalAddress);
        txtState = (TextView) findViewById(R.id.txtState);
        txtByte = (TextView) findViewById(R.id.txtByte);
        severityDisplay=(TextView) findViewById(R.id.txtSeverity);

    }

    void initializeEvents() {
        btnStartConnecting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startConnecting();
            }
        });
        btnGetBatteryInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getBatteryStatus();
            }
        });
        btnStartVibrate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startVibrate();
            }
        });
        btnStopVibrate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopVibrate();
            }
        });
        btnGetHeartRate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startScanHeartRate();
            }
        });
    }

    void startConnecting() {

        String address = txtPhysicalAddress.getText().toString();
        bluetoothDevice = bluetoothAdapter.getRemoteDevice(address);

        Log.v("test", "Connecting to " + address);
        Log.v("test", "Device name " + bluetoothDevice.getName());

        bluetoothGatt = bluetoothDevice.connectGatt(this, true, bluetoothGattCallback);

    }

    void stateConnected() {
        bluetoothGatt.discoverServices();
        txtState.setText("Connected");
    }

    void stateDisconnected() {
        bluetoothGatt.disconnect();
        txtState.setText("Disconnected");
    }

    void startScanHeartRate() {
        txtByte.setText("...");
        BluetoothGattCharacteristic bchar = bluetoothGatt.getService(CustomBluetoothProfile.HeartRate.service)
                .getCharacteristic(CustomBluetoothProfile.HeartRate.controlCharacteristic);
        bchar.setValue(new byte[]{21, 2, 1});
        bluetoothGatt.writeCharacteristic(bchar);
    }

    void listenHeartRate() {
        BluetoothGattCharacteristic bchar = bluetoothGatt.getService(CustomBluetoothProfile.HeartRate.service)
                .getCharacteristic(CustomBluetoothProfile.HeartRate.measurementCharacteristic);
        bluetoothGatt.setCharacteristicNotification(bchar, true);
        BluetoothGattDescriptor descriptor = bchar.getDescriptor(CustomBluetoothProfile.HeartRate.descriptor);
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        bluetoothGatt.writeDescriptor(descriptor);
        isListeningHeartRate = true;
    }

    void getBatteryStatus() {
        txtByte.setText("...");
        BluetoothGattCharacteristic bchar = bluetoothGatt.getService(CustomBluetoothProfile.Basic.service)
                .getCharacteristic(CustomBluetoothProfile.Basic.batteryCharacteristic);
        if (!bluetoothGatt.readCharacteristic(bchar)) {
            Toast.makeText(this, "Failed get battery info", Toast.LENGTH_SHORT).show();
        }

    }

    void startVibrate() {
        BluetoothGattCharacteristic bchar = bluetoothGatt.getService(CustomBluetoothProfile.AlertNotification.service)
                .getCharacteristic(CustomBluetoothProfile.AlertNotification.alertCharacteristic);
        bchar.setValue(new byte[]{2});
        if (!bluetoothGatt.writeCharacteristic(bchar)) {
            Toast.makeText(this, "Failed start vibrate", Toast.LENGTH_SHORT).show();
        }
    }

    void stopVibrate() {
        BluetoothGattCharacteristic bchar = bluetoothGatt.getService(CustomBluetoothProfile.AlertNotification.service)
                .getCharacteristic(CustomBluetoothProfile.AlertNotification.alertCharacteristic);
        bchar.setValue(new byte[]{0});
        if (!bluetoothGatt.writeCharacteristic(bchar)) {
            Toast.makeText(this, "Failed stop vibrate", Toast.LENGTH_SHORT).show();
        }
    }

    final BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            Log.v("test", "onConnectionStateChange");

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                stateConnected();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                stateDisconnected();
            }

        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            Log.v("test", "onServicesDiscovered");
            listenHeartRate();
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            Log.v("test", "onCharacteristicRead");
            byte[] data = characteristic.getValue();
            txtByte.setText(Arrays.toString(data));
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            Log.v("test", "onCharacteristicWrite");
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            Log.v("test", "onCharacteristicChanged");
            byte[] data = characteristic.getValue();
            ByteBuffer wrapped = ByteBuffer.wrap(data); // big-endian by default
            short num = wrapped.getShort(); // 1
            Log.i("display",num+"");
            //txtByte.setText(Arrays.toString(data));
            txtByte.setText("Heart Rate: "+num);
            spo2Input.setText("");
            HRarray[0]=(int) (num);
            HRarray[1]=(int) (num);
            HRarray[2]=(int) (num);
            HRarray[3]=(int) (num);
            HRarray[4]=(int) (num);
            arr_HR.add(num);
            //Toast.makeText(getApplicationContext(), "Enter Oxygen Saturation Data", Toast.LENGTH_LONG).show();
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt, descriptor, status);
            Log.v("test", "onDescriptorRead");
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
            Log.v("test", "onDescriptorWrite");
        }

        @Override
        public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
            super.onReliableWriteCompleted(gatt, status);
            Log.v("test", "onReliableWriteCompleted");
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            super.onReadRemoteRssi(gatt, rssi, status);
            Log.v("test", "onReadRemoteRssi");
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            super.onMtuChanged(gatt, mtu, status);
            Log.v("test", "onMtuChanged");
        }

    };

    public void copdAlgorithm(View v)
    {   //spo2Input.setText("");
        currentDateTime = DateFormat.getDateTimeInstance().format(new Date());
        computeSeverity.setVisibility(View.GONE);
        shareSeverity.setVisibility(View.VISIBLE);
        severityDisplay.setText("");
        //int[] HRarray={80,80,80,80,80};
        //int[] SParray={85,85,80,80,80};
        //arr_HR.add(txtByte.getText().toString());
        Integer xsp=Integer.parseInt(spo2Input.getText().toString().trim());
        SParray[0]=xsp;
        SParray[1]=xsp;
        SParray[2]=xsp;
        SParray[3]=xsp;
        SParray[4]=xsp;

      /* for(int i=0;i<arr_HR.size();i++)
        {
            HRarray[i]=(int)(arr_HR.get(i));
            //Log.d("HR","+HRarray[i])
            SParray[i]=Integer.valueOf(arr_SPO2.get(i));
        }*/
        int Disease = 1; // COPD 1 and Asthma 0
        //int[] HRarray = {70, 90, 80};
        //int[] SParray = {100, 80, 91};
        double severity=computeEOI(HRarray,SParray, Disease);
        sSeverity=String.valueOf(new DecimalFormat("###.##").format(severity));
        severityDisplay.append(sSeverity);
        if(arr_HR.size()>5){
            arr_HR.clear();
            arr_SPO2.clear();
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

        //Random rnd = new Random();
        for (int i=0; i<5; i++) {
            int spArray = Sparray[i]; // Random 100 SpO2 Array
            System.out.println( "" + spArray  );
            Log.i("sp","" + spArray );
            Integer calls = values.get(spArray);
            if (calls == null) {
                calls = Integer.valueOf(0);
            }
            calls += 1;
            values.put(spArray, calls);
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

        //Random rnd1 = new Random();
        for (int i=0; i<5; i++) {
            int HRArray = HRarray[i]; // Random 100 Heart Rate Array
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
            double score = betaHR*(percentagetan[i])*(percentageHR[i])+betaSp*(percentagetan[i])*(percentageSp[4-i]);
            System.out.println(score + " is the score");
            sum_score += score;
            System.out.println(sum_score + " is the total sum");
        }
        EOI = (sum_score-Min)/Range;
        System.out.println(EOI + " is the EOI");
        return EOI;
    }

   /* public void shareWithSCC(){
        //do nothing for now

        shareSeverity.setOnClickListener(new View.OnClickListener() {
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
                        Intent shareIntent = new Intent(copdActivity.this, CloudActivity.class);
                        //Bundle extras = new Bundle();
                        shareIntent.putExtra("DT", "BT");
                        shareIntent.putExtra("profile", s);
                        shareIntent.putExtra("EOI", EOI);
                        shareIntent.putExtra("Time", currentDateTime);
                        shareIntent.putExtra("Algorithm", "BT1");
                        startActivity(shareIntent);



                    }
                }
            }
        });
    }*/

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

}