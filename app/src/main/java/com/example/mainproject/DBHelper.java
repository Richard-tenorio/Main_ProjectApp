package com.example.mainproject;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

    public static final String DB_NAME = "userdb";
    public static final int DB_VERSION = 1;

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE IF NOT EXISTS payments (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "`plan` TEXT NOT NULL, " +
                "`name` TEXT NOT NULL, " +
                "`reference_no` TEXT NOT NULL, " +
                "`amount` REAL NOT NULL, " +
                "`confirmed` INTEGER NOT NULL CHECK (confirmed IN (0, 1)), " +
                "created_at DATETIME DEFAULT CURRENT_TIMESTAMP" +
                ")";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS payments");
        onCreate(db);
    }
}
