package com.example.gestioncontact;

import android.content.Intent;
import android.content.SharedPreferences;
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

public class RegisterActivity extends AppCompatActivity {

    private EditText usernameInput, phoneInput, passwordInput, confirmPasswordInput;
    private Button signupButton;
    private TextView loginText;
    private CheckBox rememberMeCheckbox;
    private ContactDbHelper dbHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_register);

        // Initialize UI elements
        usernameInput = findViewById(R.id.register_input_username);
        phoneInput = findViewById(R.id.register_input_phone);
        passwordInput = findViewById(R.id.register_input_password);
        confirmPasswordInput = findViewById(R.id.register_input_confirm_password);
        signupButton = findViewById(R.id.singup_btn);
        loginText = findViewById(R.id.text_login);
        rememberMeCheckbox = findViewById(R.id.remember_me_checkbox);

        dbHelper = new ContactDbHelper(this);

        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = usernameInput.getText().toString().trim();
                String password = passwordInput.getText().toString().trim();
                String confirmPassword = confirmPasswordInput.getText().toString().trim();
                String phone = phoneInput.getText().toString().trim();

                // Input validation
                if (username.isEmpty()) {
                    Toast.makeText(RegisterActivity.this, "Username is required", Toast.LENGTH_SHORT).show();
                    usernameInput.requestFocus();
                    return;
                }
                if (phone.isEmpty()) {
                    Toast.makeText(RegisterActivity.this, "Phone number is required", Toast.LENGTH_SHORT).show();
                    phoneInput.requestFocus();
                    return;
                }
                if (password.isEmpty()) {
                    Toast.makeText(RegisterActivity.this, "Password is required", Toast.LENGTH_SHORT).show();
                    passwordInput.requestFocus();
                    return;
                }
                if (!password.equals(confirmPassword)) {
                    Toast.makeText(RegisterActivity.this, "Password and Confirm Password do not match", Toast.LENGTH_SHORT).show();
                    confirmPasswordInput.requestFocus();
                    return;
                }

                // Register user in the database
                User user = dbHelper.RegisterUser(username, password, phone);
                if (user != null) {
                    Intent intent = new Intent(RegisterActivity.this, ContactListActivity.class);

                    // Handle "Remember Me" checkbox logic
                    if (rememberMeCheckbox.isChecked()) {
                        SharedPreferences sharedPreferences = getSharedPreferences("USER_PREFS", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putInt("USER_ID", user.getId());
                        Log.i("RegisterActivity --> ContactListActivity: ", "SHARED PREFERENCES USER_ID: "+ user.getId());
                        editor.apply();
                    } else {
                        intent.putExtra("USER_ID", user.getId());
                        Log.i("RegisterActivity --> ContactListActivity: ", "INTENT USER_ID: "+ user.getId());
                    }

                    Toast.makeText(RegisterActivity.this, "New User Registered successfully", Toast.LENGTH_SHORT).show();
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(RegisterActivity.this, "Username or phone number already exists, please try again", Toast.LENGTH_SHORT).show();
                }
            }
        });


        loginText.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
            startActivity(intent);
        });
    }
}
