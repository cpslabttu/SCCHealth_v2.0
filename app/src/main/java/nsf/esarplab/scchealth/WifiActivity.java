package nsf.esarplab.scchealth;

import android.app.Activity;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class WifiActivity extends AppCompatActivity implements View.OnClickListener {

    TextView tvIsConnected;
    EditText etName, etCountry, etdiseaseType, etTime, etEOI, etAlg;
    Button btnPost;
    String Grid;
    Person person;

    // to get long lat
    private static Context context;
    double x, y, Latitude,Longitude;
    Button addressButton;
    TextView addressTV;
    TextView latLongTV;

    //String url="http://10.100.94.221/nsf/adminlogin/insertjsondb.php";

    // String url="https://10.100.94.221.000webhostapp.com/insertjsondb.php";

    String url="http://sccmobilehealth.com/insertjsondb.php";

    public static String POST(String url, Person person)
    {
        InputStream inputStream = null;
        String result = "";
        try {

            // 1. create HttpClient
            HttpClient httpclient = new DefaultHttpClient();

            // 2. make POST request to the given URL
            HttpPost httpPost = new HttpPost(url);

            String json = "";

            // 3. build jsonObject {"ID":"p11","GRID_CODE":"c3","DT":"BT","EOI":"5.6","TIME":"2017-04-10"}
            // String js = etName.getText().toString();
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("ID",person.getPatientID());
            jsonObject.put("GRID_CODE", person.getGridCode());
            jsonObject.put("DT", person.getDiseaseType());
            jsonObject.put("EOI",person.getEoi());
            jsonObject.put("TIME",person.getTime());
            jsonObject.put("ALG","BT1");
            //jsonObject.put("ALG",person.getAlgorithm());





            // 4. convert JSONObject to JSON to String
            json = jsonObject.toString();
            Log.i("Json data",json);

            // ** Alternative way to convert Person object to JSON string usin Jackson Lib
            // ObjectMapper mapper = new ObjectMapper();
            // json = mapper.writeValueAsString(Person);

            // 5. set json to StringEntity
            StringEntity se = new StringEntity(json);

            // 6. set httpPost Entity
            httpPost.setEntity(se);

            // 7. Set some headers to inform server about the type of the content
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");

            // 8. Execute POST request to the given URL
            HttpResponse httpResponse = httpclient.execute(httpPost);

            // 9. receive response as inputStream
            inputStream = httpResponse.getEntity().getContent();

            // 10. convert inputstream to string
            if (inputStream != null)
                result = convertInputStreamToString(inputStream);
            else
                result = "Did not work!";

        } catch (Exception e) {
            Log.d("InputStream", e.getLocalizedMessage());
        }

        // 11. return result
        return result;
    }

    private static String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while ((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi);

        // show action bar
        ActionBar myActionBar = getSupportActionBar();
        myActionBar.show();

        // get reference to the views
        tvIsConnected = (TextView) findViewById(R.id.tvIsConnected);
        etName = (EditText) findViewById(R.id.etName);
        etCountry = (EditText) findViewById(R.id.etCountry);
        etdiseaseType = (EditText) findViewById(R.id.etDType);
        btnPost = (Button) findViewById(R.id.btnPost);

        etTime =(EditText) findViewById(R.id.etTime);
        etEOI = (EditText) findViewById(R.id.etEOI);
        etAlg = (EditText) findViewById(R.id.etAlg);
        // set date time
        String currentDateTime = DateFormat.getDateTimeInstance().format(new Date());
        etTime.setText(currentDateTime);

        // check if you are connected or not
        if (isConnected()) {
            tvIsConnected.setBackgroundColor(0xFF00CC00);
            tvIsConnected.setText("You are conncted");
        } else {
            tvIsConnected.setText("You are NOT conncted");
        }

        // add click listener to Button "POST"
        btnPost.setOnClickListener(this);


        // get long lat
        addressTV = (TextView) findViewById(R.id.addressTV);
        latLongTV = (TextView) findViewById(R.id.latLongTV);

        addressButton = (Button) findViewById(R.id.addressButton);
        addressButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {

                EditText editText = (EditText) findViewById(R.id.addressET);
                String address = editText.getText().toString();
                getGridCode(address);
            }
        });



    }

    public boolean isConnected() {
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Activity.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected())
            return true;
        else
            return false;
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.btnPost:
                if (!validate())
                    Toast.makeText(getBaseContext(), "Enter some data!", Toast.LENGTH_LONG).show();
                // call AsynTask to perform network operation on separate thread


                person =new Person();
                person.setPatientID(etName.getText().toString());
                person.setGridCode(etCountry.getText().toString());
                person.setEoi(etEOI.getText().toString());
                person.setDiseaseType(etdiseaseType.getText().toString());
                person.setTime(etTime.getText().toString());
                person.setAlgorithm(etAlg.getText().toString());
                new HttpAsyncTask().execute(url);


                break;
        }

    }

    private boolean validate() {
        if (etName.getText().toString().trim().equals(""))
            return false;
        else if (etCountry.getText().toString().trim().equals(""))
            return false;
        else if (etdiseaseType.getText().toString().trim().equals(""))
            return false;
        else
            return true;
    }

    private class HttpAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {

           /* Person = new Person();
            Person.setName(etName.getText().toString());
            Person.setCountry(etCountry.getText().toString());
            Person.setTwitter(etTwitter.getText().toString());*/

            return POST(urls[0], person);
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(getBaseContext(), "Data Sent!", Toast.LENGTH_LONG).show();
        }
    }

    // long lat

    public void getGridCode(String addressinput){

        String locationName=addressinput;

        Geocoder geoCoder = new Geocoder(this, Locale.ENGLISH);
        try {
            List<Address> address = geoCoder.getFromLocationName(locationName, 1);
            Latitude = address.get(0).getLatitude();
            Longitude = address.get(0).getLongitude();
            Log.i("Lat", "" + Latitude);
            Log.i("Lng", "" + Longitude);
        } catch (IOException e) {
            e.printStackTrace();
            Log.i("INFO", "exception");
        }
        x = Longitude;// -89.952302;
        y = Latitude;//35.1149703;

        LatLngBounds AZ19 = new LatLngBounds(
                new LatLng(-90.085643, 34.922824), new LatLng(-90.085642, 35.0317400825));
        LatLngBounds ZG86 = new LatLngBounds(
                new LatLng(-90.085642, 34.922824), new LatLng(-89.861741, 35.0317400825));
        LatLngBounds XP52 = new LatLngBounds(
                new LatLng(-89.8617410, 34.922824), new LatLng(-89.6378400, 35.0317400825));
        LatLngBounds DW46 = new LatLngBounds(
                new LatLng(-89.637840, 34.922824), new LatLng(-89.413939, 35.0317400825));

        LatLngBounds FD32 = new LatLngBounds(
                new LatLng(-90.085643, 35.0317400825), new LatLng(-90.085642, 35.140656165));
        //central area
        LatLngBounds YU76B = new LatLngBounds(
                new LatLng(-90.085642, 35.08619812), new LatLng(-89.9736915, 35.140656165));
        LatLngBounds YU76L = new LatLngBounds(
                new LatLng(-90.085642, 35.0317400825), new LatLng(-89.9736915, 35.08619812));
        LatLngBounds YU76K = new LatLngBounds(
                new LatLng(-89.9736915, 35.08619812), new LatLng(-89.861741, 35.140656165));
        LatLngBounds YU76Z = new LatLngBounds(
                new LatLng(-89.9736915, 35.0317400825), new LatLng(-89.861741, 35.08619812));
        //central end
        LatLngBounds HD93 = new LatLngBounds(
                new LatLng(-89.861741, 35.0317400825), new LatLng(-89.637840, 35.140656165));
        LatLngBounds WG49 = new LatLngBounds(
                new LatLng(-89.637840,35.0317400825), new LatLng(-89.413939, 35.140656165));

        LatLngBounds SP71 = new LatLngBounds(
                new LatLng(-90.309543, 35.140656165), new LatLng(-90.085642, 35.2495722475));
        LatLngBounds KT43 = new LatLngBounds(
                new LatLng(-90.085642, 35.140656165), new LatLng(-89.861741, 35.2495722475));
        LatLngBounds BY28 = new LatLngBounds(
                new LatLng(-89.861741, 35.140656165), new LatLng(-89.637840, 35.2495722475));
        LatLngBounds LC95 = new LatLngBounds(
                new LatLng(-89.637840, 35.140656165), new LatLng(-89.413939, 35.2495722475));

        LatLngBounds CR63 = new LatLngBounds(
                new LatLng(-90.309543, 35.2495722475), new LatLng(-90.085642, 35.35848833));
        LatLngBounds CX38 = new LatLngBounds(
                new LatLng(-90.085642, 35.2495722475), new LatLng(-89.861741, 35.35848833));
        LatLngBounds VJ14 = new LatLngBounds(
                new LatLng(-89.861741, 35.2495722475), new LatLng(-89.637840, 35.35848833));
        LatLngBounds DR27 = new LatLngBounds(
                new LatLng(-89.637840, 35.2495722475), new LatLng(-89.413939, 35.35848833));


        if (AZ19.contains(new LatLng(x, y))) {
            Log.i("Grid Code", "AZ19");
            Grid="AZ19";
        } else if (ZG86.contains(new LatLng(x, y))) {
            Log.i("Grid Code", "ZG86");
            Grid="ZG86";
        } else if (BY28.contains(new LatLng(x, y))) {
            Log.i("Grid Code", "BY28");
            Grid="BY28";
        } else if (XP52.contains(new LatLng(x, y))) {
            Log.i("Grid Code", "XP52");
            Grid="XP52";
        } else if (DW46.contains(new LatLng(x, y))) {
            Log.i("Grid Code", "DW46");
            Grid="DW46";
        } else if (FD32.contains(new LatLng(x, y))) {
            Log.i("Grid Code", "FD32");
            Grid="FD32";
        } else if (BY28.contains(new LatLng(x, y))) {
            Log.i("Grid Code", "BY28");
            Grid="BY28";
        } else if (YU76B.contains(new LatLng(x, y))) {
            Log.i("Grid Code", "YU76B");
            Grid="YU76B";
        } else if (YU76L.contains(new LatLng(x, y))) {
            Log.i("Grid Code", "YU76L");
            Grid="YU76L";
        } else if (YU76K.contains(new LatLng(x, y))) {
            Log.i("Grid Code", "YU76K");
            Grid="YU76K";
        }else if (YU76Z .contains(new LatLng(x, y))) {
            Log.i("Grid Code", "YU76Z");
            Grid="YU76Z";
        } else if (HD93.contains(new LatLng(x, y))) {
            Log.i("Grid Code", "HD93");
            Grid="HD93";
        } else if (WG49.contains(new LatLng(x, y))) {
            Log.i("Grid Code", "WG49");
            Grid="WG49";
        }  else if (SP71.contains(new LatLng(x, y))) {
            Log.i("Grid Code", "SP71");
            Grid="SP71";
        }else if (KT43.contains(new LatLng(x, y))) {
            Log.i("Grid Code", "KT43");
            Grid="KT43";
        }else if (BY28.contains(new LatLng(x, y))) {
            Log.i("Grid Code", "BY28");
            Grid="BY28";
        } else if (LC95.contains(new LatLng(x, y))) {
            Log.i("Grid Code", "LC95");
            Grid="LC95";
        } else if (CR63.contains(new LatLng(x, y))) {
            Log.i("Grid Code", "CR63");
            Grid="CR63";
        } else if (CX38.contains(new LatLng(x, y))) {
            Log.i("Grid Code", "CX38");
            Grid="CX38";
        } else if (VJ14.contains(new LatLng(x, y))) {
            Log.i("Grid Code", "VJ14");
            Grid="VJ14";
        } else if (DR27.contains(new LatLng(x, y))) {
            Log.i("Grid Code", "DR27");
            Grid="DR27";
        } else {
            Log.i("Lat2", "" + x);
            Log.i("Lng2", "" + y);
            Log.i("Grid Code", "Outside Boundary");
            Grid="Outside Boundary";
        }
        latLongTV.setText("Grid Code:"+Grid);
    }
}


