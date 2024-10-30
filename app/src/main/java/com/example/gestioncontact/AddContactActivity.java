package com.example.gestioncontact;

import static com.example.gestioncontact.db.ContactDbContract.ContactEntry.COLUMN_CONTACT_ADDRESS;
import static com.example.gestioncontact.db.ContactDbContract.ContactEntry.COLUMN_CONTACT_EMAIL;
import static com.example.gestioncontact.db.ContactDbContract.ContactEntry.COLUMN_CONTACT_NAME;
import static com.example.gestioncontact.db.ContactDbContract.ContactEntry.COLUMN_CONTACT_PHONE;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.gestioncontact.db.ContactDbContract;
import com.example.gestioncontact.db.ContactDbHelper;
import com.example.gestioncontact.models.Contact;
import com.google.android.material.imageview.ShapeableImageView;

public class AddContactActivity extends AppCompatActivity {
    private int userId;
    private EditText nameInput, phoneInput, emailInput, addressInput;
    private Button addContactBtn;
    private ContactDbHelper dbHelper;
    private ImageView backButton;
    private ShapeableImageView profileImage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact);

        // Initialize the database helper
        dbHelper = new ContactDbHelper(this);

        // Initialize UI components
        nameInput = findViewById(R.id.contact_name_input);
        phoneInput = findViewById(R.id.contact_phone_input);
        emailInput = findViewById(R.id.contact_email_input);
        addressInput = findViewById(R.id.contact_address_input);
        addContactBtn = findViewById(R.id.add_contact_btn);
        backButton = findViewById(R.id.back_button);
        profileImage = findViewById(R.id.profile_image);


        SharedPreferences sharedPreferences = getSharedPreferences("USER_PREFS", MODE_PRIVATE);
        if (sharedPreferences.contains("USER_ID")) {
            userId = sharedPreferences.getInt("USER_ID", -1);
            Log.i("AddContactActivity: ", "SHARED PREFERENCES USER_ID Received: " + userId);
        } else {
            userId = getIntent().getIntExtra("USER_ID", -1);
            Log.i("AddContactActivity: ", "INTENT USER_ID Received: " + userId);

        }

        // Set click listener for the "Add Contact" button
        addContactBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addContact();
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate back to ContactListActivity
                Intent intent = new Intent(AddContactActivity.this, ContactListActivity.class);
                intent.putExtra("USER_ID", userId);
                Log.i("AddContactActivity --> ContactListActivity: ", "INTENT USER_ID: " + userId);
                startActivity(intent);
                finish();
            }
        });

        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AddContactActivity.this, UserProfileActivity.class);
                intent.putExtra("USER_ID", userId);
                Log.i("AddContactActivity --> UserProfileActivity: ", "INTENT USER_ID: " + userId);
                startActivity(intent);
            }
        });
    }

    // Method to handle adding the contact
    private void addContact() {
        String name = nameInput.getText().toString().trim();
        String phone = phoneInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String address = addressInput.getText().toString().trim();


        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, "Name is required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(phone)) {
            Toast.makeText(this, "Phone number is required", Toast.LENGTH_SHORT).show();
            return;
        }

//        // Optional: Validate email format (simple regex check)
//        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
//            Toast.makeText(this, "Invalid email address", Toast.LENGTH_SHORT).show();
//            return;
//        }

        if (userId != -1) {
            Contact contact = new Contact(name, phone, email, address);
            long savedContactId = dbHelper.addContact(contact, userId);
            if (savedContactId == -1) {
                Toast.makeText(this, "Error saving contact", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Contact added successfully with ID: " + savedContactId, Toast.LENGTH_SHORT).show();
                clearFields();

                Intent intent = new Intent(AddContactActivity.this, ContactListActivity.class);
                intent.putExtra("USER_ID", userId);
                Log.i("AddContactActivity --> ContactListActivity: ", "INTENT USER_ID: " + userId);
                startActivity(intent);
                finish();
            }
        } else {
            Toast.makeText(this, "Invalid user ID!", Toast.LENGTH_SHORT).show();
        }


    }

    private void clearFields() {
        nameInput.setText("");
        phoneInput.setText("");
        emailInput.setText("");
        addressInput.setText("");
    }
}
