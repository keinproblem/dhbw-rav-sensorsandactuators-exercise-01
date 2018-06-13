package sensorsandactuators.ravensburg.dhbw.de.dhbw_rav_sensorsandactuators_exercise_01;

import android.content.Context;
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


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MagnetometerFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MagnetometerFragment extends Fragment implements SensorEventListener {
    private static final String TAG = GyroscopeFragment.class.getSimpleName();

    private final static double[] gravity = new double[3];
    private final static double[] linearAcceleration = new double[3];
    private final static SizedStack<Triple> readableStrings = new SizedStack<>(1024);
    private final static Triple<Float> latestTriple = new Triple<>();
    private SensorManager sensorManager;
    private Sensor sensor;
    private TextView textViewX;
    private TextView textViewY;
    private TextView textViewZ;


    private OnFragmentInteractionListener mListener;

    public MagnetometerFragment() {
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
        this.sensor = this.sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_accelerometer, container, false);
        this.textViewX = view.findViewById(R.id.x);
        this.textViewY = view.findViewById(R.id.y);
        this.textViewZ = view.findViewById(R.id.z);
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
            case Sensor.TYPE_MAGNETIC_FIELD:
                processGyroscopeSensorChange(event);
                break;
            default:
                Log.w(TAG, "Received unknown SesnorEvent: " + event.toString());
                //TODO; log unknown sensor event received
        }
    }

    private void processGyroscopeSensorChange(final SensorEvent sensorEvent) {
        synchronized (latestTriple) {
            latestTriple.x = sensorEvent.values[0];
            latestTriple.y = sensorEvent.values[1];
            latestTriple.z = sensorEvent.values[2];
            updateTextViews();
        }
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
