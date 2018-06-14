package sensorsandactuators.ravensburg.dhbw.de.dhbw_rav_sensorsandactuators_exercise_01;

import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.Date;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link AccelerometerFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AccelerometerFragment extends Fragment implements SensorEventListener {
    private static final String TAG = AccelerometerFragment.class.getSimpleName();

    private final static double[] gravity = new double[3];
    private final static double[] linearAcceleration = new double[3];
    private final static SizedStack<Triple> readableStrings = new SizedStack<>(1024);
    private final static Triple<Double> latestTriple = new Triple<>();

    private static LineGraphSeries<DataPoint> seriesX = new LineGraphSeries<>();
    private static LineGraphSeries<DataPoint> seriesY = new LineGraphSeries<>();
    private static LineGraphSeries<DataPoint> seriesZ = new LineGraphSeries<>();
    private GraphView graphView;

    private SensorManager sensorManager;
    private Sensor sensor;
    private TextView textViewX;
    private TextView textViewY;
    private TextView textViewZ;

    private OnFragmentInteractionListener mListener;

    public AccelerometerFragment() {
        // Required empty public constructor

    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment AccelerometerFragment.
     */
    public static AccelerometerFragment newInstance() {
        AccelerometerFragment fragment = new AccelerometerFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            final Object sensoreServiceResult = this.getContext().getSystemService(Context.SENSOR_SERVICE);
            final SensorManager sensorManager = sensoreServiceResult instanceof SensorManager ? (SensorManager) sensoreServiceResult : null;
            if (sensorManager == null) {
                throw new RuntimeException("Could not acquire SensorManager");
            }
            this.sensorManager = sensorManager;
            this.sensor = this.sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_accelerometer, container, false);
        this.graphView = (GraphView) view.findViewById(R.id.accGraph);
        this.graphView.getGridLabelRenderer().setHorizontalLabelsVisible(false);
        graphView.getViewport().setXAxisBoundsManual(true);
        graphView.getViewport().setMinX(0);
        graphView.getViewport().setMaxX(10000);
        seriesX.setColor(Color.RED);
        seriesY.setColor(Color.GREEN);
        seriesZ.setColor(Color.BLUE);

        this.graphView.addSeries(seriesX);
        this.graphView.addSeries(seriesY);
        this.graphView.addSeries(seriesZ);

        this.textViewX = view.findViewById(R.id.accX);
        this.textViewX.setTextColor(Color.RED);
        this.textViewY = view.findViewById(R.id.accY);
        this.textViewY.setTextColor(Color.GREEN);
        this.textViewZ = view.findViewById(R.id.accZ);
        this.textViewZ.setTextColor(Color.BLUE);
        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                processAccelerometerSensorChange(event);
                break;
            default:
                Log.w(TAG, "Received unknown SesnorEvent: " + event.toString());
        }
    }

    private void processAccelerometerSensorChange(final SensorEvent event) {
        //Straight outa: https://developer.android.com/guide/topics/sensors/sensors_motion#sensors-motion-accel
        final double alpha = 0.8;
        synchronized (gravity) {
            gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
            gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
            gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];
            synchronized (linearAcceleration) {
                linearAcceleration[0] = event.values[0] - gravity[0];
                linearAcceleration[1] = event.values[1] - gravity[1];
                linearAcceleration[2] = event.values[2] - gravity[2];

                latestTriple.x = linearAcceleration[0];
                latestTriple.y = linearAcceleration[1];
                latestTriple.z = linearAcceleration[2];
                updateTextViews();
            }
        }

    }

    private void updateTextViews() {
        this.textViewX.setText(String.valueOf(Math.round(latestTriple.x * 1000) / 1000.0));
        this.textViewY.setText(String.valueOf(Math.round(latestTriple.y * 1000) / 1000.0));
        this.textViewZ.setText(String.valueOf(Math.round(latestTriple.z * 1000) / 1000.0));
        final long now = System.currentTimeMillis();
        seriesX.appendData(new DataPoint(new Date(now), latestTriple.x), true, Integer.MAX_VALUE);
        seriesY.appendData(new DataPoint(new Date(now), latestTriple.y), true, Integer.MAX_VALUE);
        seriesZ.appendData(new DataPoint(new Date(now), latestTriple.z), true, Integer.MAX_VALUE);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Log.i(TAG, String.format("Sensor: %s changed accuracy: %d", sensor.toString(), accuracy));
    }

    @Override
    public void onResume() {
        super.onResume();
        if (this.sensorManager != null) {
            Log.d(TAG, "Registering Listener for sensor: " + this.sensor);
            sensorManager.registerListener(this, this.sensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (this.sensorManager != null) {
            Log.d(TAG, "Unregistering Listener for sensor: " + this.sensor);
            sensorManager.unregisterListener(this, this.sensor);
        }
    }
}
