package com.example.gestioncontact;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.gestioncontact.db.ContactDbHelper;
import com.example.gestioncontact.models.Contact;
import com.google.android.material.imageview.ShapeableImageView;

public class ContactDetailsActivity extends AppCompatActivity {

    private EditText ContactName, ContactPhone, ContactEmail, ContactAddress;
    private Button btnUpdateContact, btnDeleteContact;
    private ContactDbHelper dbHelper;
    private Contact contact;
    private int userId;
    private ImageView backButton; // Back button ImageView
    private ShapeableImageView profileImage;
    private int contactId;
    private String originalName, originalPhone, originalEmail, originalAddress; // To track original values

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_details);

        dbHelper = new ContactDbHelper(this);

        ContactName = findViewById(R.id.contact_name_input);
        ContactPhone = findViewById(R.id.contact_phone_input);
        ContactEmail = findViewById(R.id.contact_email_input);
        ContactAddress = findViewById(R.id.contact_address_input);
        btnUpdateContact = findViewById(R.id.update_contact_btn);
        btnDeleteContact = findViewById(R.id.delete_contact_btn);
        backButton = findViewById(R.id.back_button); // Find back button by its ID
        profileImage = findViewById(R.id.profile_image);

        SharedPreferences sharedPreferences = getSharedPreferences("USER_PREFS", MODE_PRIVATE);
        if (sharedPreferences.contains("USER_ID")) {
            userId = sharedPreferences.getInt("USER_ID", -1);
        } else {
            // If not in SharedPreferences, get it from the Intent
            userId = getIntent().getIntExtra("USER_ID", -1);
        }

        // Get the contactId passed from the intent
        Intent intent = getIntent();
        contactId = intent.getIntExtra("CONTACT_ID", -1);
        Log.i("ContactDetailsActivity: " ,"INTENT CONTACT_ID Received: "+ contactId);


        if (contactId != -1) {
            loadContactDetails(contactId);
        } else {
            Toast.makeText(this, "Contact ID not found. Returning to contact list.", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Handle update button click
        btnUpdateContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateContact();
                btnUpdateContact.setEnabled(false);
                btnUpdateContact.setTextColor(ContextCompat.getColor(ContactDetailsActivity.this, R.color.colorTextSecondary));
            }
        });

        // Handle back button click
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate back to ContactListActivity
                Intent intent = new Intent(ContactDetailsActivity.this, ContactListActivity.class);
                intent.putExtra("USER_ID", userId);
                Log.i("ContactDetailsActivity --> ContactListActivity: " ,"INTENT USER_ID: "+ userId);
                startActivity(intent);
                finish();
            }
        });

        // Handle delete button click with confirmation dialog
        btnDeleteContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteContact();
            }
        });

        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ContactDetailsActivity.this, UserProfileActivity.class);
                intent.putExtra("USER_ID", userId);
                Log.i("ContactDetailsActivity --> UserProfileActivity: " ,"INTENT USER_ID: "+ userId);
                startActivity(intent);
            }
        });

        addTextWatchers();
    }

    private void loadContactDetails(int contactId) {
        if (contactId != -1) {
            contact = dbHelper.getContact(contactId);

            if (contact != null) {
                originalName = contact.getName();
                originalPhone = contact.getPhone();
                originalEmail = contact.getEmail();
                originalAddress = contact.getAddress();

                // Set the original contact details in the input fields
                ContactName.setText(originalName);
                ContactPhone.setText(originalPhone);
                ContactEmail.setText(originalEmail);
                ContactAddress.setText(originalAddress);
            } else {
                Toast.makeText(this, "Contact not found", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    // Method to update contact details
    private void updateContact() {
        String updatedName = ContactName.getText().toString().trim();
        String updatedPhone = ContactPhone.getText().toString().trim();
        String updatedEmail = ContactEmail.getText().toString().trim();
        String updatedAddress = ContactAddress.getText().toString().trim();

        if (TextUtils.isEmpty(updatedName) || TextUtils.isEmpty(updatedPhone)) {
            Toast.makeText(this, "Name and phone number cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        contact.setId(contactId);
        contact.setName(updatedName);
        contact.setPhone(updatedPhone);
        contact.setEmail(updatedEmail);
        contact.setAddress(updatedAddress);

        boolean isUpdated = dbHelper.updateContact(contact);

        if (isUpdated) {
            Toast.makeText(this, "Contact updated successfully", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Update failed, contact may not exist", Toast.LENGTH_SHORT).show();
        }
    }

    // Method to delete contact with confirmation dialog
    private void deleteContact() {
        if (contact != null) {
            // Create and show confirmation dialog
            new AlertDialog.Builder(ContactDetailsActivity.this)
                    .setTitle("Delete Contact")
                    .setMessage("Are you sure you want to delete " + contact.getName() + "?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        // Perform deletion after confirmation
                        dbHelper.deleteContact(contact.getId(), userId);  // Delete by contact ID and user ID
                        Toast.makeText(ContactDetailsActivity.this, "Contact deleted successfully", Toast.LENGTH_SHORT).show();

                        // Return to the previous screen (e.g., contacts list)
                        Intent intent = new Intent(ContactDetailsActivity.this, ContactListActivity.class);
                        intent.putExtra("USER_ID", userId);
                        Log.i("ContactDetailsActivity --> ContactListActivity: " ,"INTENT USER_ID: "+ userId);
                        startActivity(intent);
                        finish();
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> {
                        dialog.dismiss();
                    })
                    .show();
        }
    }

    // Add TextWatchers to detect changes in input fields
    private void addTextWatchers() {
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                checkForChanges();
            }

            @Override
            public void afterTextChanged(Editable s) {
                // No action needed after text changes
            }
        };

        // Add the TextWatcher to each input field
        ContactName.addTextChangedListener(textWatcher);
        ContactPhone.addTextChangedListener(textWatcher);
        ContactEmail.addTextChangedListener(textWatcher);
        ContactAddress.addTextChangedListener(textWatcher);
    }

    // Check if any field has changed from its original value
    private void checkForChanges() {
        String currentName = ContactName.getText().toString().trim();
        String currentPhone = ContactPhone.getText().toString().trim();
        String currentEmail = ContactEmail.getText().toString().trim();
        String currentAddress = ContactAddress.getText().toString().trim();

        // Enable the update button if any field is changed
        if (!currentName.equals(originalName) ||
                !currentPhone.equals(originalPhone) ||
                !currentEmail.equals(originalEmail) ||
                !currentAddress.equals(originalAddress)) {
            btnUpdateContact.setEnabled(true);
            btnUpdateContact.setTextColor(Color.WHITE);

        } else {
            btnUpdateContact.setEnabled(false);
        }
    }
}
