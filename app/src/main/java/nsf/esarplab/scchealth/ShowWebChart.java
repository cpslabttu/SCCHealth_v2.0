package nsf.esarplab.scchealth;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.webkit.WebView;
import android.widget.LinearLayout;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphView.GraphViewData;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.LineGraphView;

import java.util.ArrayList;
import java.util.List;

import static nsf.esarplab.scchealth.TempContract.TempEntry.COLUMN_DATE_TIME;
import static nsf.esarplab.scchealth.TempContract.TempEntry.COLUMN_EOI_RATING;
import static nsf.esarplab.scchealth.TempContract.TempEntry.COLUMN_PATIENT_NAME;
import static nsf.esarplab.scchealth.TempContract.TempEntry.COLUMN_TEMP_VALUE;
import static nsf.esarplab.scchealth.TempContract.TempEntry._ID;

@SuppressLint("SetJavaScriptEnabled")
public class ShowWebChart extends Activity {
    private TempDbHelper mydb;
    WebView webView;
    int num1, num2, num3, num4, num5, num6;
    //LineGraphSeries<DataPoint>series;
    GraphView graph;
    SQLiteDatabase ourDatabase;
    private final Handler mHandler = new Handler();
    private Runnable mTimer1;
    private List<GraphView.GraphViewData> seriesX;
    private GraphViewSeries exampleSeries1;
    private GraphView graphView1;
    int dataCount = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_web_chart);

        //graph = (GraphView) findViewById(R.id.graph);


        mydb = new TempDbHelper(this);
        ourDatabase = mydb.getReadableDatabase();

        //graph.addSeries(series);
        //show graph

        seriesX = new ArrayList<GraphView.GraphViewData>();
        // init example series data
        exampleSeries1 = new GraphViewSeries(new GraphView.GraphViewData[] {});

        graphView1 = new LineGraphView(
                this // context
                , "Temperature Trend" // heading
        );

        graphView1.addSeries(exampleSeries1); // data
        LinearLayout layout = (LinearLayout) findViewById(R.id.graph1);
        layout.addView(graphView1);


        String[] columns = new String[]{_ID, COLUMN_PATIENT_NAME, COLUMN_DATE_TIME, COLUMN_TEMP_VALUE, COLUMN_EOI_RATING};
        Cursor c = ourDatabase.query(TempContract.TempEntry.TABLE_NAME, columns, null, null, null, null, null);
        //DataPoint[] dp = new DataPoint[c.getCount()];
        int iRow = c.getColumnIndex(COLUMN_TEMP_VALUE);

        for (int i = 0;i<c.getCount();i++) {
            c.moveToNext();
            seriesX.add(new GraphViewData(dataCount, c.getInt(4)));
            dataCount++;
            if (dataCount > 7) {
                seriesX.remove(0);
                graphView1.setViewPort(dataCount - 7, 7);

            }
            Log.i("ValueFetched",""+c.getInt(4));

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
}
