package com.example.tim.runningapptest14;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;

public class RunningGame {

    private boolean bound;
    private boolean running;
    private boolean saveable;

    private int seconds;

    private double speedLastSecond;
    private double distance;
    private double runTypeDouble;

    private DistanceService distanceService;

    private final TextView distanceView;
    private final TextView speedView;
    private final TextView timeView;
    private final TextView lastTimelineView;
    private final Spinner runSpinner;

    //to save the timeline
    private ArrayList<String> timeline;
    //the last timeline, to compare with timeline
    private ArrayList<String> lastTimeline;

    //to compare the past run with the current run
    private Comparator comparator;

    public RunningGame(TextView distanceView,
                       TextView speedView,
                       TextView timeView,
                       TextView lastTimelineView,
                       Spinner runSpinner){

        this.distanceView = distanceView;
        this.speedView = speedView;
        this.timeView = timeView;
        this.lastTimelineView= lastTimelineView;
        this.runSpinner = runSpinner;

        comparator = new Comparator();
        timeline = new ArrayList<String>();
    }

    //ServiceConnection for the DistanceService
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder binder) {
            DistanceService.DistanceBinder distanceBinder = (DistanceService.DistanceBinder) binder;
            distanceService = distanceBinder.getDistanceBinder();
            bound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            bound = false;
        }

    };

    public void runGame(){

        final Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {

                //the time
                int hours = seconds/3600;
                int minutes = (seconds%3600)/60;
                int secs = seconds%60;
                String time = String.format("%02d:%02d:%02d", hours, minutes, secs);
                timeView.setText(time);

                //increment time / set distance / set lastDistance
                if (distanceService != null && running){

                    distance = distanceService.getDistanceInMeters();
                    String distanceStr = String.format("%1$,.2f", distance);
                    distanceView.setText(distanceStr);
                    System.out.println("extracted the distance in second = "+ seconds);

                    //append the distance to the timeline of the current run
                    timeline.add(distanceStr);
                    System.out.println("The current timeline arrayList is : "+timeline);

                    //show the speed
                    // Not working yet! (https://stackoverflow.com/questions/15570542/determining-the-speed-of-a-vehicle-using-gps-in-android)
                    speedLastSecond = distanceService.getDistanceLastSecond();
                    //String speedStr = String.format("%1$,.2f m/s", speedLastSecond);
                    String speedStr = "tbd";
                    speedView.setText(speedStr);

                    //compare with the last run
                    String lastDistance = comparator.compareWithPast(timeline, lastTimeline);
                    if(!lastDistance.equals(comparator.getCompletionString())){
                        lastDistance = lastDistance + " m";
                    }
                    lastTimelineView.setText(lastDistance);
                    System.out.println("The past timeline ArrayList is: "+ lastDistance);

                    //check if the aspired distance has been reached
                    checkDistance();

                    seconds++;
                }
                handler.postDelayed(this, 1000);
            }
        });
    }

    public void checkDistance(){

        if(running = true){
            String runType = String.valueOf(runSpinner.getSelectedItem());
            String[] parts = runType.split(" ");
            runTypeDouble = Double.parseDouble(parts[0]);

            if(runTypeDouble*1000.0<= distance){
                running= false;
                saveable=true;
            }
        }
    }

    //Getter

    public ServiceConnection getConnection() {
        return connection;
    }

    public boolean isBound() {
        return bound;
    }

    public boolean getRunning(){
        return running;
    }

    public int getSeconds() {
        return seconds;
    }

    public ArrayList<String> getTimeline() {
        return timeline;
    }

    public boolean getSaveable() {
        return saveable;
    }

    public double getRunTypeDouble() {
        return runTypeDouble;
    }

    public DistanceService getDistanceService(){
        return distanceService;
    }

    public Comparator getComparator (){
        return comparator;
    }

    //Setter

    public void setBound (Boolean bound){
        this.bound= bound;
    }

    public void setRunning (Boolean running){
        this.running= running;
    }

    public void setSeconds (int seconds){
        this.seconds= seconds;
    }

    public void setTimeline (ArrayList<String> timeline){
        this.timeline= timeline;
    }

    public void setSaveable (Boolean saveable){
        this.saveable= saveable;
    }

    public void setLastTimeline (ArrayList<String> lastTimeline){
        this.lastTimeline = lastTimeline;
    }

}
