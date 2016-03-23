package me.yamakaja.accelerometer;

import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis.AxisDependency;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.Arrays;

public class MainMenu extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor sensorAccelerometer, sensorGravity;

    private float[] gravityData;
    private boolean gravityMeasured;

    LineChart chart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main_menu);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        chart = (LineChart)findViewById(R.id.lineChart);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        sensorAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorGravity = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);

        chart.getAxisRight().setEnabled(false);
        chart.setBackgroundColor(0xFFFFFF);
        chart.setDescription("");
        chart.setVisibleXRangeMaximum(100);
        chart.setData(new LineData());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main_menu, menu);
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, sensorAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, sensorGravity, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                if (!gravityMeasured) break;
                float[] temp = new float[3];

                temp[0] = event.values[0] - gravityData[0];
                temp[1] = event.values[1] - gravityData[1];
                temp[2] = event.values[2] - gravityData[2];

                gravityMeasured = false;

                float total = ((ToggleButton)findViewById(R.id.excludeGravity)).isChecked() ? (float) Math.sqrt(temp[0] * temp[0] + temp[1] * temp[1] + temp[2] * temp[2]) : (float) Math.sqrt(event.values[0] * event.values[0] + event.values[1] * event.values[1] + event.values[2] * event.values[2]);

                ((TextView) findViewById(R.id.textViewData)).setText(String.format("X: %3.3f m/s²\nY: %3.3f m/s²\nZ: %3.3f m/s²\n\nTotal: %3.3f m/s²", temp[0], temp[1], temp[2], total));

                LineData data = chart.getData();

                if (data != null) {
                    LineDataSet set = (LineDataSet)data.getDataSetByIndex(0);

                    if (set == null) {
                        set = createSet();
                        data.addDataSet(set);
                    }

                    set.setDrawCircles(false);

                    data.addXValue(data.getXValCount() + "");
                    data.addEntry(new Entry(total, set.getEntryCount()), 0);

                    chart.notifyDataSetChanged();
                    chart.moveViewToX(set.getEntryCount() - 100);
                }
                break;
            case Sensor.TYPE_GRAVITY:
                gravityMeasured = true;
                gravityData = Arrays.copyOf(event.values, 3);
                break;
        }
    }

    private LineDataSet createSet() {

        LineDataSet set = new LineDataSet(null, "Acceleration");
        set.setAxisDependency(AxisDependency.LEFT);
        set.setColor(ColorTemplate.getHoloBlue());
        set.setCircleColor(Color.WHITE);
        set.setLineWidth(2f);
        set.setCircleRadius(4f);
        set.setFillAlpha(65);
        set.setFillColor(ColorTemplate.getHoloBlue());
        set.setHighLightColor(Color.rgb(244, 117, 117));
        set.setValueTextColor(Color.WHITE);
        set.setValueTextSize(9f);
        set.setDrawValues(false);
        return set;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
