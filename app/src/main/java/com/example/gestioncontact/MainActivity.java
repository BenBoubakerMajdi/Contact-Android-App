package com.example.gestioncontact;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.gestioncontact.db.ContactDbHelper;
import com.example.gestioncontact.models.User;

import android.content.SharedPreferences;

public class MainActivity extends AppCompatActivity {

    private EditText usernameInput;
    private EditText passwordInput;
    private Button loginBtn, exitBtn;
    private TextView registerText;
    private CheckBox rememberMeCheckbox;

    private ContactDbHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI components
        usernameInput = findViewById(R.id.auth_input_username);
        passwordInput = findViewById(R.id.auth_input_password);
        loginBtn = findViewById(R.id.login_btn);
        exitBtn = findViewById(R.id.exit_btn);
        rememberMeCheckbox = findViewById(R.id.remember_me_checkbox);
        registerText = findViewById(R.id.text_signup);

        // Initialize database helper
        dbHelper = new ContactDbHelper(this);

        SharedPreferences sharedPreferences = getSharedPreferences("USER_PREFS", MODE_PRIVATE);

        if (sharedPreferences.contains("USER_ID")) {
            Intent intent = new Intent(MainActivity.this, ContactListActivity.class);
            startActivity(intent);
            finish();
        }


        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = usernameInput.getText().toString().trim();
                String password = passwordInput.getText().toString().trim();

                if (username.isEmpty()) {
                    usernameInput.setError("Username is required");
                    usernameInput.requestFocus();
                    return;
                }

                if (password.isEmpty()) {
                    passwordInput.setError("Password is required");
                    passwordInput.requestFocus();
                    return;
                }
                User user = dbHelper.loginUser(username, password);
                if (user != null) {
                    Intent intent = new Intent(MainActivity.this, ContactListActivity.class);

                    if (rememberMeCheckbox.isChecked()) {
                        SharedPreferences sharedPreferences = getSharedPreferences("USER_PREFS", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putInt("USER_ID", user.getId());
                        Log.i("MainAvtivity --> ContactListActivity: ", "SHARED PREFERENCES USER_ID: " + user.getId());
                        editor.apply();
                    } else {
                        intent.putExtra("USER_ID", user.getId());
                        Log.i("MainAvtivity --> ContactListActivity: ", "INTENT USER_ID: " + user.getId());

                    }
                    Toast.makeText(MainActivity.this, "Login successful", Toast.LENGTH_SHORT).show();
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(MainActivity.this, "Invalid credentials, please try again", Toast.LENGTH_SHORT).show();
                }
            }

        });

        // Set exit button click listener
        exitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishAffinity();
            }
        });

        // Set register text click listener
        registerText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to RegisterActivity
                Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }
}
