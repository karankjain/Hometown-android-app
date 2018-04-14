package com.example.karanjain.weconnect;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by karanjain on 4/12/17.
 */

public class DbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "hometown.db";
    private static final int DATABASE_VERSION =1;
    private static final String CREATE_DB = "CREATE TABLE IF NOT EXISTS " + "HOMETOWNLOCATION_DETAILS " +
            "( " + "ID" + " INTEGER PRIMARY KEY NOT NULL ," +
            " NICKNAME" + " VARCHAR  , " +
            " COUNTRY" + " VARCHAR NOT NULL ," +
            " STATE" + " VARCHAR NOT NULL ," +
            " CITY" + " VARCHAR NOT NULL ," +
            " YEAR" + " VARCHAR NOT NULL ," +
            " LONGITUDE" + " DOUBLE NOT NULL ," +
            " LATITUDE" + " DOUBLE NOT NULL" + " )";

    public DbHelper(Context context) {
        super(context, DATABASE_NAME,null,DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_DB);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}
}
