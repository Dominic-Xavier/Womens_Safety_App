package com.myapp.womenssafetyapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;

public class Sqlite extends SQLiteOpenHelper {

    Context context;
    ContentValues cv;
    SQLiteDatabase db;
    Cursor cursor;
    List<String> All_Contact_Numbers = new ArrayList<>();

    public Sqlite(@Nullable Context context) {
        super(context, "womens.db", null, 1);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE ContactDetails (Phone_Numbers INTEGER NOT NULL)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists ContactDetails");
        onCreate(db);
    }

    void saveData(String callNumber, String message1, String message2, String message3){
        boolean insertValues = false;
        long row = 0;
        db = this.getWritableDatabase();
        String arr[] = new String[4];
        arr[0] = callNumber;
        arr[1] = message1;
        arr[2] = message2;
        arr[3] = message3;
        cv = new ContentValues();
        for (int i=0; i<arr.length; i++){
            cv.put("Phone_Numbers", arr[i]);
            //Checking Row Count
            if(getRowCount()<4){
                row = db.insert("ContactDetails", null, cv);
            }
            else{
                insertValues = true;
                break;
            }

            if(row==-1){
                insertValues = true;
                break;
            }
        }
        if(insertValues)
            Toast.makeText(context, "Phone numbers are already present in database", Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(context, "Values inserted into database...!", Toast.LENGTH_SHORT).show();
    }

    List<String> fecthData(){
        db = this.getReadableDatabase();
        cursor = db.query("ContactDetails", new String[]{"Phone_Numbers"}, null, null, null, null, null);
        while (cursor.moveToNext()){
            All_Contact_Numbers.add(cursor.getString(0));
        }
        return All_Contact_Numbers;
    }

    void updateData(){
        db = this.getWritableDatabase();

    }

    int getRowCount(){
        db = this.getReadableDatabase();
        String countQuery = "select * from ContactDetails";
        cursor = db.rawQuery(countQuery, null);
        return cursor.getCount();
    }
}
