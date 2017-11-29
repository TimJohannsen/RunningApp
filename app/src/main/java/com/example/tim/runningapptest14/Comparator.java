package com.example.tim.runningapptest14;

import java.util.ArrayList;

/**
 * Created by Tim on 04.10.2016.
 */
public class Comparator {

    private ArrayList<String> present;
    private ArrayList<String> past;
    private Boolean lastRunFinished;
    private String completionString = "complete";

    public String compareWithPast(ArrayList<String> present, ArrayList<String> past ){

        this.present = present;
        this.past = past;

        int presentSize = present.size();
        int pastSize = past.size();

        //Make sure that there was a run recorded in the past
        if(pastSize <= 1){
            return "no record";
        }

        if(presentSize == 1){
            return "0.00";
        } else if(presentSize < pastSize){
            return past.get(presentSize-1);
        } else if(presentSize >= pastSize){
            lastRunFinished=true;
            return completionString;
        }

        return "--";
    }

    //Getter
    public String getCompletionString(){
        return completionString;
    }

    //Setter
    public void setLastRunFinished(Boolean lastRunFinished) {
        this.lastRunFinished = lastRunFinished;
    }

}
