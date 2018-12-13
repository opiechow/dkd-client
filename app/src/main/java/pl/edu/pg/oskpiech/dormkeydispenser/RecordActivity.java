package pl.edu.pg.oskpiech.dormkeydispenser;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import java.io.File;
import java.io.IOException;

public class RecordActivity extends AppCompatActivity {
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    Handler timerHandler = new Handler();
    Handler progressHandler = new Handler();
    Runnable progressRunnable = new Runnable() {
        @Override
        public void run() {
            ProgressBar pb = findViewById(R.id.recordProgressBar);
            pb.setProgress(pb.getProgress() + 1);
            if (pb.getProgress() < pb.getMax()) {
                progressHandler.postDelayed(progressRunnable, 30);
            } else {
                timerHandler.postDelayed(timerRunnable, 0);
            }
        }
    };
    private boolean permissionToRecordAccepted = false;
    private SoundMeter soundMeter;
    Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            soundMeter.stop();
            Intent intent = new Intent();
            setResult(RESULT_OK, intent);
            finish();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        soundMeter = new SoundMeter();
        Button recordBtn = findViewById(R.id.recordBtn);
        recordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String filename = "/resources/sample.mp4";
                String dir = "/resources";
                File filedir = new File(getFilesDir(), dir);
                filedir.mkdir();
                File file = new File(getFilesDir(), filename);
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                soundMeter.start(getFilesDir() + filename);
                progressHandler.postDelayed(progressRunnable, 0);
            }
        });
    }

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
}
