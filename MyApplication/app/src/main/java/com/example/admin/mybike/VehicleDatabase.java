package com.example.admin.mybike;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;


public class VehicleDatabase extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;

    private static final String DATABASE_NAME = "VehicleManager",
    TABLE_CONTACTS = "Vehicle",
    KEY_ID = "id",
    KEY_NAME = "VehicleName",
    KEY_DISTANCE = "TravelledDistance",
    KEY_OILDATE = "OilChangedDate",
    KEY_CHAINDATE = "ChainChangedDate",
    KEY_IMAGEURI = "imageUri";

    public VehicleDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_CONTACTS + "(" + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + KEY_NAME + " TEXT," + KEY_DISTANCE + " TEXT," + KEY_OILDATE + " TEXT," + KEY_CHAINDATE + " TEXT," + KEY_IMAGEURI + " BLOB)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONTACTS);

        onCreate(db);
    }

    public void createContact(VehicleHandler contact) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(KEY_NAME, contact.getName());
        values.put(KEY_DISTANCE, contact.getDistance());
        values.put(KEY_OILDATE, contact.getDate());
        values.put(KEY_CHAINDATE, contact.getAddress());
        values.put(KEY_IMAGEURI, contact.getImageURI());
        db.insert(TABLE_CONTACTS, null, values);
        db.close();
    }

    public void deleteContact(VehicleHandler contact) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_CONTACTS, KEY_ID + "=?", new String[] {String.valueOf(contact.getId())});
        db.close();
    }

    public int getContactsCount() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_CONTACTS, null);
        int count = cursor.getCount();
        db.close();
        cursor.close();
        return count;
    }
    public List<VehicleHandler> getAllContacts() {
        List<VehicleHandler> contacts = new ArrayList<VehicleHandler>();
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_CONTACTS, null);
        cursor.moveToFirst();
         do {
             contacts.add(new VehicleHandler(Integer.parseInt(cursor.getString(0)), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4), cursor.getBlob(5)));
         }
         while (cursor.moveToNext());

        cursor.close();
        db.close();
        return contacts;
    }
//    =============================================================================================================================
//    public VehicleHandler getVehicle(int id) {
//        SQLiteDatabase db = getReadableDatabase();
//
//        Cursor cursor = db.query(TABLE_CONTACTS, new String[] { KEY_ID, KEY_NAME, KEY_DISTANCE, KEY_OILDATE, KEY_CHAINDATE, KEY_IMAGEURI }, KEY_ID + "=?", new String[] { String.valueOf(id) }, null, null, null, null );
//
//        if (cursor != null)
//            cursor.moveToFirst();
//
//        VehicleHandler contact = new VehicleHandler(Integer.parseInt(cursor.getString(0)), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4), Uri.parse(cursor.getString(5)));
//        db.close();
//        cursor.close();
//        return contact;
//    }
//   =============================================================================================================================
//    public int updateContact(VehicleHandler contact) {
//        SQLiteDatabase db = getWritableDatabase();
//        ContentValues values = new ContentValues();
//        values.put(KEY_NAME, contact.getName());
//        values.put(KEY_DISTANCE, contact.getDistance());
//        values.put(KEY_DATE, contact.getDate());
//        values.put(KEY_ADDRESS, contact.getAddress());
//        values.put(KEY_IMAGEURI, contact.getImageURI().toString());
//        int rowsAffected = db.update(TABLE_CONTACTS, values, KEY_ID + "=?", new String[] { String.valueOf(contact.getId()) });
//        db.close();
//        return rowsAffected;
//    }



}
