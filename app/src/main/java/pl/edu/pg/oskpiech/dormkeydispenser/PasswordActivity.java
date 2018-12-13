package pl.edu.pg.oskpiech.dormkeydispenser;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class PasswordActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password);

        Button submitBtn = findViewById(R.id.submitBtn);
        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText editText = findViewById(R.id.passwordEditText);
                String password = editText.getText().toString();

                Intent intent = new Intent();
                intent.putExtra("pl.edu.pg.oskpiech.AUTH_DATA", password);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }
}
