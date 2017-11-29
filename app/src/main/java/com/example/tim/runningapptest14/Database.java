package com.example.tim.runningapptest14;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Tim on 28.09.2016.
 */
public class Database {

    private SQLiteOpenHelper dbHelper;
    private SQLiteDatabase db;
    private Cursor cursor;

    public boolean gameStarted = true;

    // if instantiated from RunningAppMainActivity the context should the 'this'
    // Problem to solve: when to call db.close(); and cursor.close();
    public Database(Context context){
        dbHelper = new DatabaseHelper(context);
        try{
            db = dbHelper.getReadableDatabase();

        }catch(SQLiteException e){
            System.out.println("PROBLEM IN DATABASE CONSTRUCTOR "+e);
        }
    }

    public void insertRun(SQLiteDatabase db, Double run, Integer runID, Integer seconds, String timeline){
        ContentValues runValues = new ContentValues();
        runValues.put("RUN", run);
        runValues.put("RUNID", runID);
        runValues.put("SECONDS", seconds);
        runValues.put("TIMELINE", timeline);
        db.insert("RUNSTATS", null, runValues);
        System.out.println("Inserted the run: "+ run+ " / "+runID+" / "+seconds+" / "+timeline);
    }

    //returns all of the saved runs, only for debugging
    public void extractAllRuns(){
        try{
            cursor = db.query("RUNSTATS",
                    new String[]{"RUN", "RUNID", "SECONDS", "TIMELINE"},
                    null, null, null, null, null );
            System.out.println("The cursor has started the query");

            int i = 0;
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                Double runDouble = cursor.getDouble(0);
                Integer runID = cursor.getInt(1);
                Integer seconds = cursor.getInt(2);
                String nameText = cursor.getString(3);
                System.out.println(runDouble+ " / " + runID+ " / " + seconds+ " / " + nameText);
                i++;
                cursor.moveToNext();
            }
        }catch(SQLiteException e){
            System.out.println(" PROBLEM IN extractRUN "+e);
        }
    }

    public Integer getNextRunID(Double run){
        int extractedRunID = -1;
        String sentRun = String.valueOf(run);
        try{
            cursor = db.rawQuery("SELECT MAX(RUNID) FROM RUNSTATS WHERE RUN IN (SELECT RUN " +
                    "FROM RUNSTATS WHERE RUN = "+ sentRun +")", null);

            cursor.moveToFirst();
            String nameText = cursor.getString(0);

            System.out.println("The CURRENT runID for the "+ sentRun + " km distance is: "+ nameText);

            extractedRunID = Integer.parseInt(nameText)+1;
            System.out.println("The NEXT runID for the "+ sentRun + " km distance is: "+extractedRunID);

        }catch(SQLiteException e){
            System.out.println(" PROBLEM IN extractRUN "+e);
        }
        return extractedRunID;
    }

    public ArrayList<String> extractLastTimeline(double run, int runID){

        ArrayList<String> lastTimeArrayList= new ArrayList<String>();

        if(gameStarted){

            String stringOfRun = String.valueOf(run);
            String stringOfRunID = String.valueOf(runID);

            try{
                cursor = db.query("RUNSTATS",
                        new String[]{"TIMELINE", "RUN", "RUNID"},
                        "RUN = ? AND RUNID = ?", new String[]{stringOfRun, stringOfRunID}, null, null, null );

                cursor.moveToFirst();

                String timeline = cursor.getString(0);

                System.out.println("THE EXTRACTED TIMELINE IS: "+ timeline);

                try{
                    JSONObject json = new JSONObject(timeline);
                    JSONArray jArray = json.optJSONArray("arrayList");

                    for (int y = 0; y < jArray.length(); y++) {
                        String str_value=jArray.optString(y);  //<< jget value from jArray

                        lastTimeArrayList.add(y, str_value);
                    }
                }catch(JSONException ex) {
                    ex.printStackTrace();
                }
            }catch(SQLiteException e) {
                e.printStackTrace();
            }
        }
        gameStarted = false;
        return lastTimeArrayList;
    }

    //Getter
    public SQLiteDatabase getDb (){
        return db;
    }

    //Setter
    public void setGameStarted(boolean gameStarted){
        this.gameStarted = gameStarted;
    }
}