package com.ubx.rfid_demo;
// AdminActivity.java

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

public class AdminActivity extends Activity {
    private EditText urlEditText;
    private Button saveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        urlEditText = findViewById(R.id.urlEditText);
        saveButton = findViewById(R.id.saveButton);

        SharedPreferences sharedPreferences = getSharedPreferences("WebViewPrefs", MODE_PRIVATE);
        String defaultUrl = sharedPreferences.getString("defaultUrl", "http://192.168.0.103:8082/");
        urlEditText.setText(defaultUrl);

        saveButton.setOnClickListener(v -> {
            String newUrl = urlEditText.getText().toString();
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("defaultUrl", newUrl);
            editor.apply();

            // Return the new URL as a result
            Intent resultIntent = new Intent();
            resultIntent.putExtra("newUrl", newUrl);
            setResult(RESULT_OK, resultIntent);
            finish();  // Close the admin activity after saving
        });
    }
}
