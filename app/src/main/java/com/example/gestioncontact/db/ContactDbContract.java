package com.example.gestioncontact.db;

import android.provider.BaseColumns;

public class ContactDbContract {

    // To prevent someone from accidentally instantiating the contract class,
    // make the constructor private.
    private ContactDbContract() {
    }

    /* Inner class that defines the table contents */
    public static class ContactEntry implements BaseColumns {

        // Contact Table
        public static final String TABLE_CONTACT = "Contact";
        public static final String COLUMN_CONTACT_ID = "contact_id";
        public static final String COLUMN_CONTACT_NAME = "name";
        public static final String COLUMN_CONTACT_PHONE = "phone";
        public static final String COLUMN_CONTACT_EMAIL = "email";
        public static final String COLUMN_CONTACT_ADDRESS = "Address";


        // UserContact Table
        private static final String TABLE_USER_CONTACT = "UserContact";
        private static final String COLUMN_USER_ID_FK = "user_id";  // Foreign Key for User
        private static final String COLUMN_CONTACT_ID_FK = "contact_id";  // Foreign Key for Contact
    }
}
