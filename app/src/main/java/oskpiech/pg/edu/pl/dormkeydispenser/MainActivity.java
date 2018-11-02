package oskpiech.pg.edu.pl.dormkeydispenser;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private final static int movingAverageWindow = 10;
    private static int soundThreshold = 200;
    private static int lightThreshold = 20;
    final ProgramState state = new ProgramState();
    Handler timerHandler = new Handler();
    /* Sound meter related variables */
    private SoundMeter soundMeter;
    private MovingAverage soundAverage = new MovingAverage(movingAverageWindow);
    Runnable timerRunnable = new Runnable() {

        @Override
        public void run() {
            double soundAvg;
            double soundAmplitude = soundMeter.getAmplitude();

            soundAverage.addSample(soundAmplitude);
            soundAvg = soundAverage.getAverage();

            TextView soundTextView = findViewById(R.id.soundLevelTextView);
            soundTextView.setText(String.format(getString(R.string.sound_val), soundAvg));

            state.setSoundValue(soundAvg);
            updateAuthMethod();

            timerHandler.postDelayed(this, 100);
        }
    };
    /* Light sensor related variables */
    private SensorManager mSensorManager;
    private Sensor mLight;
    private boolean permissionToRecordAccepted = false;
    private String[] permissions = {Manifest.permission.RECORD_AUDIO};

    /* "Dangerous" permissions requesting */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_RECORD_AUDIO_PERMISSION:
                permissionToRecordAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
        }
        if (!permissionToRecordAccepted) finish();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mLight = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        soundMeter = new SoundMeter();
        timerHandler.postDelayed(timerRunnable, 0);

        final ImageButton keyButton = findViewById(R.id.keyImageButton);
        keyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /* TODO: Prepare a JSON to send somewhere around here */
                new SocketSender(state).execute();
            }
        });
    }

    @Override
    protected void onResume() {
        // Register a listener for the light sensor.
        super.onResume();
        if (permissionToRecordAccepted) {
            soundMeter.start();
        }
        timerHandler.postDelayed(timerRunnable, 0);
        mSensorManager.registerListener(this, mLight, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        // Be sure to unregister the sensor when the activity pauses.
        super.onPause();
        mSensorManager.unregisterListener(this);
        timerHandler.removeCallbacks(timerRunnable);
        soundMeter.stop();
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        TextView luxTextView = findViewById(R.id.lightLevelTextView);
        float luxVal = event.values[0];
        luxTextView.setText(String.format(getString(R.string.light_val), luxVal));
        state.setLightValue(luxVal);
        updateAuthMethod();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        /* TODO: Accuracy change handling? */
    }

    private void updateAuthMethod() {
        if (state.getLightValue() > lightThreshold) {
            state.setAuthMethod("photo");
        } else if (state.getSoundValue() < soundThreshold) {
            state.setAuthMethod("audio");
        } else {
            state.setAuthMethod("password");
        }
        TextView authMethodTextView = findViewById(R.id.authMethodTextView);
        authMethodTextView.setText(String.format(getString(R.string.auth_method),
                state.getAuthMethod()));

        ImageButton key = findViewById(R.id.keyImageButton);
        if (state.isAuthenticated()) {
            key.setImageResource(R.drawable.key_authorized);
        }
        if (state.isLockerOpened()) {
            key.setImageResource(R.drawable.key_opened);
        }
    }
}
