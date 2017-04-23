package com.example.android.MotoHelm;

import android.location.Location;

import com.example.android.common.logger.Log;
import com.google.android.gms.maps.model.LatLng;

/**
 * Created by joshr on 3/16/2017.
 */

public class SharedValues {
    public static LatLng StartLoc;
    public static Boolean FirstTime = true;
    public static String Destination = "Tooele UT"; //nothing for now
    public static String jsonOuptut = "";
    public static String Instructions[]; //HTML_INSTRUCTIONS
    public static String Manuever[]; //Manuver
    public static String DistMan[]; //Distance
    public static Double Lat_Coord[]; //Coordinates for Start of each direction. we only want n-1 of them. Check them for the new direction set
    public static Double Lang_Coord[];
    public static int NumSteps = 0; //init 0
    public static int StepNumber = 0; //Start at zero
    public static boolean ArrayFilled;
    public static boolean DirecRequest;
    private static int BOUND_CONDITION = 1000; //meters, for the bounds check on next directions
    public static String MessageToSend ="";
    public static double CurrentLat,CurrentLang;


    public static String Message;
    public static String  Speed,LatMes,LangMes;
    public static boolean isFinished = false;

    ///WE NEED SETTINGS BITCH
    public static void setSpeed(String temp){
        Speed = temp;
    }
    public static void setLatMes(String temp){
        LatMes = temp;
    }
    public static void setLangMes(String temp){
        LangMes = temp;
    }

    public static void Instructor(){
        //builds instructions to send upon request

        if(StepNumber != NumSteps){
        Message = Instructions[StepNumber] +"," +Manuever[StepNumber] + "," +Speed +"\n";
        }
        else {Message = "You have arrived" +"\n";
    }

    }
    public static void CheckBounds() {
        //if lat and lang are within
    if(StepNumber != NumSteps) {
        Location loc1 = new Location("");
        Location loc2 = new Location("");
        //init two objects first

        loc1.setLatitude(CurrentLat);
        loc1.setLongitude(CurrentLang);
        loc2.setLatitude(Lat_Coord[StepNumber + 1]);
        loc2.setLongitude(Lang_Coord[StepNumber + 1]);
        //Get the current and next position in the arrays
        //current set and next set of instruction coordinates

        float DistInMeters = loc1.distanceTo(loc2);
        //reutnrs distance in meters

        //if we are within 10 m
        int DistCeil = Math.abs ((int) Math.ceil((double) DistInMeters));
        Log.d("Distance from object: ", String.valueOf(DistCeil));
        if (DistCeil <= BOUND_CONDITION) {
            StepNumber++;
        }
        else{
            //we just keep going, not sure why this is needed but hey maybe ill need it..like later check heading and make sure we are on course...that would be cool i guess
        }


    }
    }


}
