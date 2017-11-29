package com.example.tim.runningapptest14;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class RunningAppMainActivity extends Activity {

    private RunningGame game;
    private Database database;
    private SQLiteDatabase db;
    private ServiceConnection connection;
    private ArrayList<String> lastTimeline;
    private Handler handler;

    private TextView timeView;
    private TextView distanceView;
    private TextView speedView;
    private Spinner runSpinner;
    private TextView lastTimelineView;
    private Button startButton;

    //values that will be extracted from RunningGame
    private double runTypeDouble;
    private int runID;
    private int seconds;
    private String timeline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_running_app_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        timeView= (TextView)findViewById(R.id.time);
        distanceView = (TextView)findViewById(R.id.distance);
        speedView = (TextView)findViewById(R.id.speed);
        runSpinner = (Spinner)findViewById(R.id.run);
        lastTimelineView = (TextView)findViewById(R.id.distancePast);
        startButton = (Button)findViewById(R.id.start);

        database = new Database(this);
        db = database.getDb();
        //only for debugging
        database.extractAllRuns();

        handler = new Handler();

        game = new RunningGame(distanceView, speedView, timeView, lastTimelineView, runSpinner);
        connection = game.getConnection();
        game.runGame();

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                System.out.println("Starting to request permisson");
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 10);
                return;
            }
        }
    }

    @Override
    protected void onStart(){
        super.onStart();
        Intent intent = new Intent(this, DistanceService.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop(){
        super.onStop();
        if(game.isBound()){
            unbindService(connection);
            game.setBound(false);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch(requestCode){
            case 10:
                if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED)
                    DistanceService.locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, DistanceService.listener);
                return;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_running_app_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.action_save_run:

                if(game.getSaveable()){
                    //Get the data from the current game
                    runTypeDouble = game.getRunTypeDouble();
                    runID = database.getNextRunID(runTypeDouble);
                    seconds = game.getSeconds();
                    timeline = toJSon(game.getTimeline());

                    //insert the data into the database
                    database.insertRun(db, runTypeDouble, runID, seconds, timeline);

                    handler.post(new Runnable(){
                        @Override
                        public void run(){
                            Toast.makeText(getApplicationContext(), "Your run has been saved", Toast.LENGTH_SHORT).show();
                        }
                    });

                }else{
                    handler.post(new Runnable(){
                        @Override
                        public void run(){
                            Toast.makeText(getApplicationContext(), "Your run is not finished yet", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                return true;

            case R.id.action_settings:
                //implement code for the settings menu
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public static String toJSon(ArrayList<String> list) {

        try {
            JSONObject jsonObj = new JSONObject();
            JSONArray jsonArr = new JSONArray(list);
            jsonObj.put("arrayList", jsonArr);

            return jsonObj.toString();

        } catch(JSONException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public void onClickStart(View view){

        startButton.setText(R.string.Start);

        database.setGameStarted(true);

        game.getComparator().setLastRunFinished(false);
        game.getDistanceService().setDistanceInMeters(0.0);

        String runType = String.valueOf(runSpinner.getSelectedItem());
        String[] parts = runType.split(" ");
        Double runTypeDouble = Double.parseDouble(parts[0]);
        int runID = database.getNextRunID(runTypeDouble)-1;
        lastTimeline = database.extractLastTimeline(runTypeDouble, runID);
        game.setLastTimeline(lastTimeline);
        game.setRunning(true);
        game.setSaveable(false);
    }

    public void onClickPause(View view){
        if(game.getRunning()){
            startButton.setText(R.string.Resume);
        }
        game.setRunning(false);
    }

    public void onClickStop(View view){
        game.setRunning(false);
        game.setSeconds(0);
        game.setTimeline(new ArrayList<String>());

        startButton.setText(R.string.Start);
        distanceView.setText(R.string.meters_detail);
        speedView.setText(R.string.speed_detail);
        lastTimelineView.setText(R.string.distancePast_detail);
    }
}
