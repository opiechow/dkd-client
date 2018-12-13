package pl.edu.pg.oskpiech.dormkeydispenser;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private static final int ACTIVITY_REQUEST_CODE_PASSWORD = 0;
    private static final int ACTIVITY_REQUEST_CODE_AUDIO = 1;
    private static final int ACTIVITY_REQUEST_CODE_PHOTO = 2;

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
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mLight = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        soundMeter = new SoundMeter();
        timerHandler.postDelayed(timerRunnable, 0);
        promptUserID();

        final ImageButton keyButton = findViewById(R.id.keyImageButton);
        keyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (state.getAuthMethod()) {
                    case "password":
                        dipatchEnterPasswordIntent();
                        break;
                    case "photo":
                        dispatchTakePictureIntent();
                        break;
                    case "audio":
                        dispatchRecordAudioIntent();
                        break;
                }

            }
        });

        final Button resetButton = findViewById(R.id.resetBtn);
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                state.setAuthenticated(false);
                promptUserID();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == ACTIVITY_REQUEST_CODE_PASSWORD) {
                // get String data from Intent
                String returnString = data.getStringExtra("pl.edu.pg.oskpiech.AUTH_DATA");
                // set text view with string
                String encoded = android.util.Base64.encodeToString(returnString.getBytes(), android.util.Base64.DEFAULT);
                state.setDataToSend(encoded);
                state.setAuthMethodToSend("passwdAuth");
            }
            if (requestCode == ACTIVITY_REQUEST_CODE_PHOTO) {
                Bundle extras = data.getExtras();
                Bitmap imageBitmap = (Bitmap) extras.get("data");
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                byte[] byteArray = byteArrayOutputStream.toByteArray();
                String s = android.util.Base64.encodeToString(byteArray, android.util.Base64.DEFAULT);
                state.setDataToSend(s);
                state.setAuthMethodToSend("photoAuth");
            }
            if (requestCode == ACTIVITY_REQUEST_CODE_AUDIO) {
                byte[] array = null;
                try {
                    byte[] buffer = new byte[4096];
                    File f = new File(getFilesDir() + "/resources/sample.mp4");
                    ByteArrayOutputStream ous = new ByteArrayOutputStream();
                    InputStream ios = new FileInputStream(f);
                    int read = 0;
                    while ((read = ios.read(buffer)) != -1) {
                        ous.write(buffer, 0, read);
                    }
                    ous.close();
                    ios.close();
                    array = ous.toByteArray();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                String s = android.util.Base64.encodeToString(array, android.util.Base64.DEFAULT);
                state.setDataToSend(s);
                state.setAuthMethodToSend("audioAuth");
            }
            new SocketSender(state).execute();
        }
    }

    @Override
    protected void onResume() {
        // Register a listener for the light sensor.
        super.onResume();
        if (permissionToRecordAccepted) {
            soundMeter.start("/dev/null");
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
        if (!state.isAuthenticated()) {
            key.setImageResource(R.drawable.key_start);
        } else if (state.isLockerOpened()) {
            key.setImageResource(R.drawable.key_opened);
        } else {
            key.setImageResource(R.drawable.key_unauthorized);
        }
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, ACTIVITY_REQUEST_CODE_PHOTO);
        }
    }

    private void dipatchEnterPasswordIntent() {
        Intent startIntent = new Intent(getApplicationContext(), PasswordActivity.class);
        startActivityForResult(startIntent, ACTIVITY_REQUEST_CODE_PASSWORD);
    }

    private void dispatchRecordAudioIntent() {
        Intent startIntent = new Intent(getApplicationContext(), RecordActivity.class);
        startActivityForResult(startIntent, ACTIVITY_REQUEST_CODE_AUDIO);
    }

    private void promptUserID() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle("Welcome");
        alert.setMessage("Please provide your Student's ID");

// Set an EditText view to get user input
        final EditText input = new EditText(this);
        alert.setView(input);

        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String value = String.valueOf(input.getText());
                state.setUserId(value);
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                state.setUserId("0");
            }
        });

        alert.show();
    }
}
