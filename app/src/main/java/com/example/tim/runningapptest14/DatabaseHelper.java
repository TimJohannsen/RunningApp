package com.example.tim.runningapptest14;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Tim on 26.09.2016.
 */
class DatabaseHelper extends SQLiteOpenHelper {

    //Name and version of database
    private static final String DB_NAME = "runTest";
    //If DB_VERSION has been incremented and the user updated the app, then onUpgrade() is called and his DB_VERSION updates
    private static final int DB_VERSION = 1;

    DatabaseHelper(Context context){
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db){

        db.execSQL("CREATE TABLE RUNSTATS("
                +"_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                +"RUN REAL, "
                +"RUNID INTEGER, "
                +"SECONDS INTEGER, "
                +"TIMELINE STRING);");

        double i;
        for(i=1.0; i<=10.0; i++){
            ContentValues initialValues = new ContentValues();
            initialValues.put("RUN", i);
            initialValues.put("RUNID", 0);
            initialValues.put("SECONDS", 0);
            initialValues.put("TIMELINE", -1);
            db.insert("RUNSTATS", null, initialValues);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){

    }
}
