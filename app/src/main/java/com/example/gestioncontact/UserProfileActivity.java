package com.example.gestioncontact;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.gestioncontact.db.ContactDbHelper;
import com.example.gestioncontact.models.User;
import com.example.gestioncontact.utils.UpdatePasswordResult;

public class UserProfileActivity extends AppCompatActivity {

    private EditText nameEditText, phoneEditText, currentPassword, newPasswordEditText;
    private Button updateProfileButton;
    private ImageView backButton;
    private ContactDbHelper dbHelper;
    private int userId;
    private Button logoutBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        // Initialize UI components
        backButton = findViewById(R.id.back_button);
        nameEditText = findViewById(R.id.user_profile_name);
        phoneEditText = findViewById(R.id.user_profile_phone);
        newPasswordEditText = findViewById(R.id.user_profile_new_password);
        currentPassword = findViewById(R.id.user_profile_password);
        updateProfileButton = findViewById(R.id.update_user_profile_btn);
        logoutBtn = findViewById(R.id.logout_btn);
        dbHelper = new ContactDbHelper(this);


        SharedPreferences sharedPreferences = getSharedPreferences("USER_PREFS", MODE_PRIVATE);
        if (sharedPreferences.contains("USER_ID")) {
            userId = sharedPreferences.getInt("USER_ID", -1);
            Log.i("UserProfileAvtivity: " ,"SHARED PREFERENCES USER_ID Received : "+ userId);

        } else {
            userId = getIntent().getIntExtra("USER_ID", -1);
            Log.i("UserProfileAvtivity: " ,"INTENT USER_ID Received : "+ userId);
        }

        if (userId != -1) {
            loadUserData(userId);
        } else {
            Toast.makeText(this, "User ID not found in SharedPreferences", Toast.LENGTH_SHORT).show();
        }

        // Enable Update button when new password is filled
        newPasswordEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // You can leave this empty
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                boolean hasText = s.length() > 0;

                // Enable or disable the button based on text presence
                updateProfileButton.setEnabled(hasText);

                if (hasText) {
                    updateProfileButton.setTextColor(Color.WHITE);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        // Update profile button listener
        updateProfileButton.setOnClickListener(v -> updatePassword(userId));

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        logoutBtn.setOnClickListener(v -> logout());
    }


    private void loadUserData(int userId) {

        Cursor cursor = dbHelper.getUserData(userId);

        if (cursor != null && cursor.moveToFirst()) {
            String name = cursor.getString(cursor.getColumnIndexOrThrow("username"));
            String phone = cursor.getString(cursor.getColumnIndexOrThrow("phone"));

            nameEditText.setText(name);
            phoneEditText.setText(phone);

            nameEditText.setInputType(android.text.InputType.TYPE_NULL);
            nameEditText.setFocusable(false);
            phoneEditText.setInputType(android.text.InputType.TYPE_NULL);
            phoneEditText.setFocusable(false);

            cursor.close();
        } else {
            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
        }
    }


    private void updatePassword(int userId) {
        String newPassword = newPasswordEditText.getText().toString().trim();
        String Password = currentPassword.getText().toString().trim();

        if (Password.isEmpty()) {
            Toast.makeText(this, "Please enter your current password", Toast.LENGTH_SHORT).show();
            return;
        }

        if (newPassword.isEmpty()) {
            Toast.makeText(this, "Please enter a new password", Toast.LENGTH_SHORT).show();
            return;
        }

        UpdatePasswordResult result = dbHelper.updateUserPassword(userId, Password, newPassword);

        switch (result) {
            case SUCCESS:
                Toast.makeText(this, "Password updated successfully", Toast.LENGTH_SHORT).show();
                currentPassword.setText("");
                newPasswordEditText.setText("");
                break;
            case EMPTY_NEW_PASSWORD:
                Toast.makeText(this, "Failed to update: New password cannot be empty", Toast.LENGTH_SHORT).show();
                break;
            case EMPTY_CURRENT_PASSWORD:
                Toast.makeText(this, "Failed to update: Current password cannot be empty", Toast.LENGTH_SHORT).show();
                break;
            case INCORRECT_CURRENT_PASSWORD:
                Toast.makeText(this, "Failed to update: Current password is incorrect", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private void logout() {
        new AlertDialog.Builder(UserProfileActivity.this)
                .setTitle("Log Out")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("LOGOUT", (dialog, which) -> {

                    SharedPreferences sharedPreferences = getSharedPreferences("USER_PREFS", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.remove("USER_ID");
                    editor.apply();

                    Intent intent = new Intent(UserProfileActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    dialog.dismiss();
                })
                .show();
    }

}
