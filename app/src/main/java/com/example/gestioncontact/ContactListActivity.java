package com.example.gestioncontact;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gestioncontact.adapters.ContactAdapter;
import com.example.gestioncontact.db.ContactDbHelper;
import com.example.gestioncontact.models.Contact;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.List;

public class ContactListActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ContactAdapter contactAdapter;
    private ContactDbHelper dbHelper;
    private TextView emptyText;  // TextView for showing "add new contact"
    private int userId;
    private FloatingActionButton addContactBtn;
    private ShapeableImageView profileImage;
    private SearchView searchView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_list);

        // Initialize the RecyclerView and emptyText
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        emptyText = findViewById(R.id.empty_contact_list_message);
        addContactBtn = findViewById(R.id.btn_add_contact);
        profileImage = findViewById(R.id.profile_image);
        searchView = findViewById(R.id.search);


        // Initialize the DBHelper
        dbHelper = new ContactDbHelper(this);

        SharedPreferences sharedPreferences = getSharedPreferences("USER_PREFS", MODE_PRIVATE);
        if (sharedPreferences.contains("USER_ID")) {
            userId = sharedPreferences.getInt("USER_ID", -1);
            Log.i("ContactListAvtivity: " ,"SHARED PREFERENCES USER_ID Received : "+ userId);

        } else {
            userId = getIntent().getIntExtra("USER_ID", -1);
            Log.i("ContactListAvtivity: " ,"INTENT USER_ID Received : "+ userId);
        }

        if (userId != -1) {
            // Fetch the contacts for the logged-in user
            List<Contact> contactList = dbHelper.getAllContactsForUser(userId);

            // Check if contacts are found
            if (contactList != null && !contactList.isEmpty()) {
                // Set up the adapter and hide the emptyText
                contactAdapter = new ContactAdapter(contactList, dbHelper, userId, this);
                recyclerView.setAdapter(contactAdapter);
                emptyText.setVisibility(View.GONE);
            } else {
                // Show the emptyText message
                emptyText.setVisibility(View.VISIBLE);
            }
        } else {
            Toast.makeText(this, "Invalid user ID!", Toast.LENGTH_SHORT).show();
        }

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                contactAdapter.filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                contactAdapter.filter(newText);
                return true;
            }
        });

        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ContactListActivity.this, UserProfileActivity.class);
                intent.putExtra("USER_ID", userId);
                Log.i("ContactListActivity --> UserProfileActivity: ", "INTENT USER_ID: "+ userId);
                startActivity(intent);
            }
        });

        addContactBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ContactListActivity.this, AddContactActivity.class);
                intent.putExtra("USER_ID", userId);
                Log.i("ContactListActivity --> AddContactActivity: ", "INTENT USER_ID: "+ userId);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        List<Contact> contactList = dbHelper.getAllContactsForUser(userId);

        if (contactList != null && !contactList.isEmpty()) {
            contactAdapter.updateContacts(contactList);
            emptyText.setVisibility(View.GONE);
        } else {
            emptyText.setVisibility(View.VISIBLE);
        }
    }
}
