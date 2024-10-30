package com.example.gestioncontact.adapters;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gestioncontact.ContactDetailsActivity;
import com.example.gestioncontact.R;
import com.example.gestioncontact.db.ContactDbHelper;
import com.example.gestioncontact.models.Contact;

import java.util.ArrayList;
import java.util.List;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ContactViewHolder> {
    private List<Contact> contactList;
    private ContactDbHelper dbHelper; // Reference to your database helper
    private int userId;
    private Context context; // Reference to context for AlertDialog
    private List<Contact> filteredContactList;


    public ContactAdapter(List<Contact> contactList, ContactDbHelper dbHelper, int userId, Context context) {
        this.contactList = contactList;
        this.filteredContactList = new ArrayList<>(contactList); // Initialize with the full list
        this.dbHelper = dbHelper;
        this.userId = userId;
        this.context = context;
    }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_contact, parent, false);
        return new ContactViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
        Contact contact = contactList.get(position);
        holder.contactName.setText(contact.getName());
        holder.contactPhone.setText(contact.getPhone());

        // Set an OnClickListener on the delete button

        holder.contactProfile.setOnClickListener(v -> {
            Intent intent = new Intent(context, ContactDetailsActivity.class);
            intent.putExtra("CONTACT_ID", contact.getId());
            intent.putExtra("USER_ID", userId);
            Log.i("ContactListItemActivity --> ContactDetailsActivity: ", "INTENT USER_ID: "+ userId);
            context.startActivity(intent);
        });

        holder.callBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent dialIntent = new Intent(Intent.ACTION_DIAL); // Use ACTION_DIAL to open the dialer
                dialIntent.setData(Uri.parse("tel:" + contact.getPhone()));
                context.startActivity(dialIntent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return contactList.size();
    }

    public static class ContactViewHolder extends RecyclerView.ViewHolder {
        TextView contactName, contactPhone;
        ImageButton callBtn;

        LinearLayout contactProfile;


        public ContactViewHolder(@NonNull View itemView) {
            super(itemView);

            contactName = itemView.findViewById(R.id.contact_name);
            contactPhone = itemView.findViewById(R.id.contact_phone);
            contactProfile = itemView.findViewById(R.id.contact);
            callBtn = itemView.findViewById(R.id.btn_call);
        }
    }

    // Update ContactList on refresh
    public void updateContacts(List<Contact> contacts) {
        this.contactList = contacts;
        notifyDataSetChanged();
    }

    public void filter(String text) {
        contactList.clear();
        if (text.isEmpty()) {
            contactList.addAll(filteredContactList);
        } else {
            String filterPattern = text.toLowerCase().trim();
            for (Contact contact : filteredContactList) {
                if (contact.getName().toLowerCase().contains(filterPattern)) {
                    contactList.add(contact);
                }
            }
        }
        notifyDataSetChanged();
    }
}
