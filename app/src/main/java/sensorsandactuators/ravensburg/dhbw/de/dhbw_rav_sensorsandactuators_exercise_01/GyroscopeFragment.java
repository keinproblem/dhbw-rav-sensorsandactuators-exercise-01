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
 * Use the {@link GyroscopeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GyroscopeFragment extends Fragment implements SensorEventListener {
    private static final String TAG = GyroscopeFragment.class.getSimpleName();

    private static final float NS2S = 1.0f / 1000000000.0f;
    private static LineGraphSeries<DataPoint> seriesX = new LineGraphSeries<>();
    private static LineGraphSeries<DataPoint> seriesY = new LineGraphSeries<>();

    private final static SizedStack<Triple> readableStrings = new SizedStack<>(1024);
    private final static Triple<Double> latestTriple = new Triple<>();
    private SensorManager sensorManager;
    private Sensor sensor;
    private TextView textViewX;
    private TextView textViewY;
    private TextView textViewZ;
    private static LineGraphSeries<DataPoint> seriesZ = new LineGraphSeries<>();
    private final float[] deltaRotationVector = new float[4];
    private float timestamp;
    private GraphView graphView;

    private OnFragmentInteractionListener mListener;

    public GyroscopeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment GyroscopeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static GyroscopeFragment newInstance(String param1, String param2) {
        GyroscopeFragment fragment = new GyroscopeFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }

        final Object sensorServiceResult = this.getContext().getSystemService(Context.SENSOR_SERVICE);
        final SensorManager sensorManager = sensorServiceResult instanceof SensorManager ? (SensorManager) sensorServiceResult : null;
        if (sensorManager == null) {
            //TODO
            return;
        }
        this.sensorManager = sensorManager;
        this.sensor = this.sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
    }

    private int s;

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
            case Sensor.TYPE_GYROSCOPE:
                processGyroscopeSensorChange(event);
                break;
            default:
                Log.w(TAG, "Received unknown SesnorEvent: " + event.toString());
                //TODO; log unknown sensor event received
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_gyroscope, container, false);
        this.graphView = (GraphView) view.findViewById(R.id.accGyro);
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
        //SimpleDateFormat simpleDateFormat = new SimpleDateFormat("mm:ss.SSS");
        //final DateAsXAxisLabelFormatter dateAsXAxisLabelFormatter = new DateAsXAxisLabelFormatter(getActivity(), simpleDateFormat);
        //graphView.getGridLabelRenderer().setLabelFormatter(dateAsXAxisLabelFormatter);
        this.textViewX = view.findViewById(R.id.x);
        this.textViewY = view.findViewById(R.id.y);
        this.textViewZ = view.findViewById(R.id.z);
        return view;
    }

    private void processGyroscopeSensorChange(final SensorEvent sensorEvent) {
        latestTriple.x = (double) sensorEvent.values[0];
        latestTriple.y = (double) sensorEvent.values[1];
        latestTriple.z = (double) sensorEvent.values[2];
        seriesX.appendData(new DataPoint(new Date(System.currentTimeMillis()), latestTriple.x), true, Integer.MAX_VALUE);
        seriesY.appendData(new DataPoint(new Date(System.currentTimeMillis()), latestTriple.y), true, Integer.MAX_VALUE);
        seriesZ.appendData(new DataPoint(new Date(System.currentTimeMillis()), latestTriple.z), true, Integer.MAX_VALUE);
        //this.series.appendData(new DataPoint(((++s==40)?(s=0):s), latestTriple.x), true, 40);


        updateTextViews();
        // This time step's delta rotation to be multiplied by the current rotation
        // after computing it from the gyro sample data.
        /*if (timestamp != 0) {
            final float dT = (sensorEvent.timestamp - timestamp) * NS2S;
            // Axis of the rotation sample, not normalized yet.
            float axisX = sensorEvent.values[0];
            float axisY = sensorEvent.values[1];
            float axisZ = sensorEvent.values[2];

            // Calculate the angular speed of the sample
            float omegaMagnitude = (float) Math.sqrt(axisX*axisX + axisY*axisY + axisZ*axisZ);

            // Normalize the rotation vector if it's big enough to get the axis
            if (omegaMagnitude > NS2S) {
                axisX /= omegaMagnitude;
                axisY /= omegaMagnitude;
                axisZ /= omegaMagnitude;
            }

            // Integrate around this axis with the angular speed by the time step
            // in order to get a delta rotation from this sample over the time step
            // We will convert this axis-angle representation of the delta rotation
            // into a quaternion before turning it into the rotation matrix.
            float thetaOverTwo = omegaMagnitude * dT / 2.0f;
            float sinThetaOverTwo = (float) Math.sin(thetaOverTwo);
            float cosThetaOverTwo = (float) Math.cos(thetaOverTwo);
            deltaRotationVector[0] = sinThetaOverTwo * axisX;
            deltaRotationVector[1] = sinThetaOverTwo * axisY;
            deltaRotationVector[2] = sinThetaOverTwo * axisZ;
            deltaRotationVector[3] = cosThetaOverTwo;
        }
        timestamp = sensorEvent.timestamp;
        float[] deltaRotationMatrix = new float[9];
        SensorManager.getRotationMatrixFromVector(deltaRotationMatrix, deltaRotationVector);

        for (int i = 0; i < deltaRotationMatrix.length; i=i+3){
            Log.i(TAG, "\n" + deltaRotationMatrix[i] + " " + deltaRotationMatrix[i+1] +" "+deltaRotationMatrix[i+2]);
        }*/
        //Log.i(TAG, Arrays.toString(deltaRotationMatrix));
        // User code should concatenate the delta rotation we computed with the current
        // rotation in order to get the updated rotation.
        // rotationCurrent = rotationCurrent * deltaRotationMatrix;

    }

    private void updateTextViews() {
        this.textViewX.setText(String.valueOf(Math.round(latestTriple.x * 1000) / 1000.0));
        this.textViewY.setText(String.valueOf(Math.round(latestTriple.y * 1000) / 1000.0));
        this.textViewZ.setText(String.valueOf(Math.round(latestTriple.z * 1000) / 1000.0));
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onResume() {
        super.onResume();
        if (this.sensorManager != null) {
            sensorManager.registerListener(this, this.sensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (this.sensorManager != null) {
            sensorManager.unregisterListener(this, this.sensor);
        }
    }
}
