package com.example.gestioncontact.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.gestioncontact.models.User;
import com.example.gestioncontact.models.Contact;
import com.example.gestioncontact.utils.UpdatePasswordResult;

import java.util.ArrayList;
import java.util.List;

public class ContactDbHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "contactDB";

    // User Table
    private static final String TABLE_USER = "User";
    private static final String COLUMN_USER_ID = "user_id";
    private static final String COLUMN_USER_USERNAME = "username";
    private static final String COLUMN_USER_PASSWORD = "password";
    private static final String COLUMN_USER_PHONE = "phone";  // Added phone column

    // Contact Table
    private static final String TABLE_CONTACT = "Contact";
    private static final String COLUMN_CONTACT_ID = "contact_id";
    private static final String COLUMN_CONTACT_NAME = "name";
    private static final String COLUMN_CONTACT_PHONE = "phone";
    private static final String COLUMN_CONTACT_EMAIL = "email";
    private static final String COLUMN_CONTACT_ADDRESS = "Address";


    // UserContact Table
    private static final String TABLE_USER_CONTACT = "UserContact";
    private static final String COLUMN_USER_ID_FK = "user_id";  // Foreign Key for User
    private static final String COLUMN_CONTACT_ID_FK = "contact_id";  // Foreign Key for Contact

    public ContactDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create User Table
        String CREATE_USER_TABLE = "CREATE TABLE " + TABLE_USER + "("
                + COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_USER_USERNAME + " TEXT,"
                + COLUMN_USER_PASSWORD + " TEXT,"
                + COLUMN_USER_PHONE + " TEXT UNIQUE" + ")";  // Unique phone number
        db.execSQL(CREATE_USER_TABLE);

        // Create Contact Table
        String CREATE_CONTACT_TABLE = "CREATE TABLE " + TABLE_CONTACT + "("
                + COLUMN_CONTACT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_CONTACT_NAME + " TEXT,"
                + COLUMN_CONTACT_PHONE + " TEXT UNIQUE,"
                + COLUMN_CONTACT_EMAIL + " TEXT,"
                + COLUMN_CONTACT_ADDRESS + " TEXT" + ")";
        db.execSQL(CREATE_CONTACT_TABLE);

        // Create UserContact Table
        String CREATE_USER_CONTACT_TABLE = "CREATE TABLE " + TABLE_USER_CONTACT + "("
                + COLUMN_USER_ID_FK + " INTEGER,"
                + COLUMN_CONTACT_ID_FK + " INTEGER,"
                + "FOREIGN KEY(" + COLUMN_USER_ID_FK + ") REFERENCES " + TABLE_USER + "(" + COLUMN_USER_ID + "),"
                + "FOREIGN KEY(" + COLUMN_CONTACT_ID_FK + ") REFERENCES " + TABLE_CONTACT + "(" + COLUMN_CONTACT_ID + ")"
                + ")";
        db.execSQL(CREATE_USER_CONTACT_TABLE);

        // Insert default test user (username: majdi, password: majdi, phone: 1234567890)
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_USERNAME, "majdi");
        values.put(COLUMN_USER_PASSWORD, "majdi");
        values.put(COLUMN_USER_PHONE, "98472523");  // You can change this to any valid phone number

        db.insert(TABLE_USER, null, values);  // Insert user into User table
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER_CONTACT);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONTACT);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);
        onCreate(db);
    }

    // User Operations

    public User RegisterUser(String username, String password, String phoneNumber) {
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.query(TABLE_USER,
                new String[]{COLUMN_USER_ID},
                COLUMN_USER_PHONE + "=? OR " + COLUMN_USER_USERNAME + "=?",
                new String[]{phoneNumber, username},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            cursor.close();
            db.close();
            return null;
        }

        cursor.close();

        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_USERNAME, username);
        values.put(COLUMN_USER_PASSWORD, password);
        values.put(COLUMN_USER_PHONE, phoneNumber);

        long userId = db.insert(TABLE_USER, null, values);
        if (userId == -1) {
            db.close();
            return null;
        }

        User user = new User((int) userId, username, phoneNumber, password);

        db.close();
        return user;
    }

    public User loginUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_USER, new String[]{COLUMN_USER_ID, COLUMN_USER_USERNAME, COLUMN_USER_PASSWORD, COLUMN_USER_PHONE},
                COLUMN_USER_USERNAME + "=? AND " + COLUMN_USER_PASSWORD + "=?",
                new String[]{username, password}, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            User user = new User(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_USER_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_USERNAME)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_PASSWORD)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_PHONE)));
            cursor.close();
            return user;
        }

        return null;
    }

    public Cursor getUserData(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {COLUMN_USER_USERNAME, COLUMN_USER_PHONE};
        return db.query(TABLE_USER, columns, COLUMN_USER_ID + "=?", new String[]{String.valueOf(userId)}, null, null, null);
    }


    public UpdatePasswordResult updateUserPassword(int userId, String currentPassword, String newPassword) {
        if (newPassword == null || newPassword.isEmpty()) {
            return UpdatePasswordResult.EMPTY_NEW_PASSWORD;
        }

        SQLiteDatabase db = this.getReadableDatabase();

        // Query to get the current password stored in the database
        Cursor cursor = db.query(TABLE_USER,
                new String[]{COLUMN_USER_PASSWORD},
                COLUMN_USER_ID + "=?",
                new String[]{String.valueOf(userId)},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            String storedPassword = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_PASSWORD));
            cursor.close();

            if (!currentPassword.equals(storedPassword)) {
                return UpdatePasswordResult.INCORRECT_CURRENT_PASSWORD;
            }
        } else {
            // Close cursor if no result found
            if (cursor != null) {
                cursor.close();
            }
            return UpdatePasswordResult.INCORRECT_CURRENT_PASSWORD; // Handle case where user ID doesn't exist
        }

        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_PASSWORD, newPassword);

        int rowsAffected = db.update(TABLE_USER, values, COLUMN_USER_ID + "=?", new String[]{String.valueOf(userId)});
        db.close();

        return rowsAffected > 0 ? UpdatePasswordResult.SUCCESS : UpdatePasswordResult.INCORRECT_CURRENT_PASSWORD;
    }


    // Contact Operations

    public long addContact(Contact contact, int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_CONTACT_NAME, contact.getName());
        values.put(COLUMN_CONTACT_PHONE, contact.getPhone());
        values.put(COLUMN_CONTACT_EMAIL, contact.getEmail());
        values.put(COLUMN_CONTACT_ADDRESS, contact.getAddress());

        // Insert the contact into the Contact table
        long contactId = db.insert(TABLE_CONTACT, null, values);

        if (contactId != -1) {  // Check if contact was added successfully
            // Link the contact to the user in the UserContact table
            ContentValues userContactValues = new ContentValues();
            userContactValues.put(COLUMN_USER_ID_FK, userId);
            userContactValues.put(COLUMN_CONTACT_ID_FK, contactId);

            db.insert(TABLE_USER_CONTACT, null, userContactValues);
        }

        db.close();
        return contactId;
    }


    public List<Contact> getAllContactsForUser(int userId) {
        List<Contact> contactList = new ArrayList<>();
        String selectQuery = "SELECT c.* FROM " + TABLE_CONTACT + " c "
                + "JOIN " + TABLE_USER_CONTACT + " uc ON c." + COLUMN_CONTACT_ID + " = uc." + COLUMN_CONTACT_ID_FK
                + " WHERE uc." + COLUMN_USER_ID_FK + " = ?";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, new String[]{String.valueOf(userId)});

        if (cursor.moveToFirst()) {
            do {
                Contact contact = new Contact();
                contact.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CONTACT_ID)));
                contact.setName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONTACT_NAME)));
                contact.setPhone(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONTACT_PHONE)));
                contact.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONTACT_EMAIL)));

                contactList.add(contact);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return contactList;
    }

    public Contact getContact(int contactId) {

        String selectQuery = "SELECT * FROM " + TABLE_CONTACT + " WHERE " + COLUMN_CONTACT_ID + " = ?";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, new String[]{String.valueOf(contactId)});

        Contact contact = null;

        if (cursor != null && cursor.moveToFirst()) {
            contact = new Contact();
            contact.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CONTACT_ID)));
            contact.setName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONTACT_NAME)));
            contact.setPhone(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONTACT_PHONE)));
            contact.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONTACT_EMAIL)));
            contact.setAddress(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONTACT_ADDRESS)));
        }

        if (cursor != null) {
            cursor.close();
        }
        db.close();

        return contact;
    }


    public boolean updateContact(Contact contact) {
        SQLiteDatabase db = this.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(COLUMN_CONTACT_NAME, contact.getName());
            values.put(COLUMN_CONTACT_EMAIL, contact.getEmail());
            values.put(COLUMN_CONTACT_ADDRESS, contact.getAddress());

            int rowsAffected = db.update(TABLE_CONTACT, values, COLUMN_CONTACT_ID + "=?",
                    new String[]{String.valueOf(contact.getId())});

            db.close();
            return rowsAffected > 0;
    }


    public void deleteContact(int contactId, int userId) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Ensure there's a space after "FROM" and before "WHERE"
        String deleteQuery1 = "DELETE FROM " + TABLE_USER_CONTACT
                + " WHERE " + COLUMN_CONTACT_ID_FK + " = " + contactId
                + " AND " + COLUMN_USER_ID_FK + " = " + userId;
        String deleteQuery2 = "DELETE FROM " + TABLE_CONTACT
                + " WHERE " + COLUMN_CONTACT_ID + " = " + contactId;

        // Execute the delete queries
        db.execSQL(deleteQuery1);
        db.execSQL(deleteQuery2);

        db.close(); // Close the database connection
    }
}
