package com.example.potholedetection;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

public class MainActivity extends AppCompatActivity {

    TextView txt_currentAccel, txt_prevAccel, txt_acceleration;
    ProgressBar prog_shakeMeter;

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mMagneticField;
    private Sensor mGravity;

    private float[] mAccelerometerData = new float[3];
    private float[] mMagnetometerData = new float[3];
    private float[] mGravityData = new float[3];

    float[] rotation = new float[9];
    float[] inclination = new float[9];

    private double accelerationCurrentValue;
    private double accelerationPreviousValue;

//    Fused fusedLocationProviderCLient;

    private int pointsPlotted = 5;
    private int graphIntervalCounter = 0;

    private int threshold = 1000;

    private Viewport viewport;

    LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>(new DataPoint[] {
            new DataPoint(0, 1),
            new DataPoint(1, 5),
            new DataPoint(2, 3),
            new DataPoint(3, 2),
            new DataPoint(4, 6)
    });

    private SensorEventListener sensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {

            float x = sensorEvent.values[0];
            float y = sensorEvent.values[1];
            float z = sensorEvent.values[2];

            int sensorType = sensorEvent.sensor.getType();
            switch (sensorType) {
                case Sensor.TYPE_ACCELEROMETER:
                    mAccelerometerData = sensorEvent.values.clone();
                    break;
                case Sensor.TYPE_MAGNETIC_FIELD:
                    mMagnetometerData = sensorEvent.values.clone();
                    break;
                case Sensor.TYPE_GRAVITY:
                    mGravityData = sensorEvent.values.clone();
                    break;
                default:
                    return;

            }

            SensorManager.getRotationMatrix(rotation, inclination, mGravityData,
                    mMagnetometerData);
            double geometryAx = rotation[0]*sensorEvent.values[0] + rotation[1]*sensorEvent.values[1] + rotation[2]*sensorEvent.values[2];
            double geometryAy = rotation[3]*sensorEvent.values[0] + rotation[4]*sensorEvent.values[1] + rotation[5]*sensorEvent.values[2];
            double geometryAz = rotation[6]*sensorEvent.values[0] + rotation[7]*sensorEvent.values[1] + rotation[8]*sensorEvent.values[2];

            double az = geometryAz;


//            double mag = Math.sqrt(geometryAx*geometryAx + geometryAy*geometryAy + geometryAz*geometryAz);
            double theAcceleration = (az);
//            double theAcceleration = (accelerationCurrentValue - accelerationPreviousValue);
//            accelerationPreviousValue = accelerationCurrentValue;

            //update text views
//            txt_currentAccel.setText("Current = " + (int)accelerationCurrentValue);
//            txt_prevAccel.setText("Prev = " + (int)accelerationPreviousValue);
//            txt_acceleration.setText("Acceleration change = " + (int)theAcceleration);

            txt_currentAccel.setText("Current = " + accelerationCurrentValue);
            txt_prevAccel.setText("Prev = " + accelerationPreviousValue);
            txt_acceleration.setText("Acceleration change = " + theAcceleration);

            prog_shakeMeter.setProgress((int) theAcceleration);


            if (theAcceleration > threshold) {

//                if (actualTime - lastUpdate < 200){
//                    return;
//                }
//                lastUpdate = actualTime;
                Toast.makeText(MainActivity.this,"Pothole Detected", Toast.LENGTH_SHORT).show();
                txt_acceleration.setBackgroundColor(Color.parseColor("#fcad03"));
//                dataBaseHelper.getInstance().insert((float) theAcceleration);
            }
//            else if (theAcceleration > 2) {
//                txt_acceleration.setBackgroundColor(Color.YELLOW);
//            }
            else {
                txt_acceleration.setBackgroundColor(Color.parseColor("#ffffff"));
            }

            //update the graph
            pointsPlotted++;
            series.appendData(new DataPoint(pointsPlotted, theAcceleration), true, pointsPlotted);
            viewport.setMaxX(pointsPlotted);
            viewport.setMinX(pointsPlotted - 200);

        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txt_acceleration = findViewById(R.id.txt_accel);
        txt_currentAccel = findViewById(R.id.txt_currentAccel);
        txt_prevAccel = findViewById(R.id.txt_prevAccel);

        prog_shakeMeter = findViewById(R.id.prog_shakeMeter);

        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagneticField = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mGravity = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        //sample graph code
        GraphView graph = (GraphView) findViewById(R.id.graph);
        viewport = graph.getViewport();
        viewport.setScrollable(true);
        viewport.setXAxisBoundsManual(true);
        graph.addSeries(series);
    }

    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(sensorEventListener, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(sensorEventListener, mMagneticField, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(sensorEventListener, mGravity, SensorManager.SENSOR_DELAY_NORMAL);
//
    }

    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(sensorEventListener);
    }
}