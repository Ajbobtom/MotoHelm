package com.example.android.MotoHelm;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by joshr on 4/1/2017.
 */

public class DirectionStuff extends AsyncTask  {
    private static String APIKEY = "&key=AIzaSyBnaSoDe6vRqFcQiclMLgoqfV7UAyBC-l4";
    public static String finURL = null;
     public static JSONObject json = null;
    String TAG = "DIRECTION: ";

    private   String stringURL = "https://maps.googleapis.com/maps/api/directions/json?origin=";
    public void BuildURL(){
        String LATLNG = String.valueOf(SharedValues.StartLoc.latitude) +","+ String.valueOf(SharedValues.StartLoc.longitude);
        //spit it out
        Log.d("STRING URL ", LATLNG);
    String urlLoc = LATLNG;
        String Destination = SharedValues.Destination;
    Destination = Destination.replaceAll(" ","+");
    Destination = "&destination=" + Destination;
    finURL= stringURL + urlLoc + Destination + APIKEY;

}


public void  URLReqest() throws IOException, JSONException {
    URL url = new URL(finURL);
    StringBuffer response = new StringBuffer();

    HttpURLConnection httpconn= (HttpURLConnection)url.openConnection();
    if(httpconn.getResponseCode() == HttpURLConnection.HTTP_OK){
        BufferedReader input = new BufferedReader(new InputStreamReader(httpconn.getInputStream()));
        String strLine = "";
        while ((strLine = input.readLine()) != null){
            response.append(strLine);
        }
        input.close();
    }
SharedValues.jsonOuptut = response.toString();
    httpconn.disconnect();
     //converts to JSON object for reading
}

    public void ConvertToJSON() throws JSONException {
        json = new JSONObject(SharedValues.jsonOuptut);


    }
    public void MakeUseFull() throws JSONException {
            JSONObject jsonObject = new JSONObject(SharedValues.jsonOuptut);
            JSONArray routesArray = jsonObject.getJSONArray("routes");
            JSONObject route = routesArray.getJSONObject(0);
            //got the routes
            JSONArray legs = route.getJSONArray("legs");
            //weve got the legs array
            JSONObject leg = legs.getJSONObject(0); //get first object in legs
            JSONObject step = leg.getJSONObject("duration"); //steps array
            String shtml = step.getString("text");
            Log.d("DirecitonStuff:", "MakeUseFull: " + shtml);
            ///errorr here with the input string
            JSONArray steps = leg.getJSONArray("steps"); //Steps array
            int length = steps.length();
            SharedValues.NumSteps = length; //number of steps
            SharedValues.Instructions = new String[length]; //HTML_INSTRUCTIONS
            SharedValues.Manuever= new String[length]; //Manuver
            SharedValues.DistMan= new String[length]; //Distance
            SharedValues.Lat_Coord= new Double[length]; //Coordinates for start
            SharedValues.Lang_Coord= new Double[length];
            Log.d("LENGTH: ", String.valueOf(length));
            for(int i=0; i<length; i++) {

                String manuever =null;
                Double lat=0.0,lang=0.0;
                JSONObject StepsObj = steps.getJSONObject(i); //First step
                shtml=  StepsObj.getString("html_instructions"); //Html instructions

                shtml = shtml.replaceAll("\\<.*?\\>",""); //Parse the htlm for reading and sending
                SharedValues.Instructions[i] = shtml; //HTML_INSTRUCTIONS

                 //parse and replaced
                try{
                manuever = StepsObj.getString("maneuver");
                  }
                catch(JSONException exception){
                    exception.printStackTrace();
                    manuever = "N/A";
                }
                //manuevers
                SharedValues.Manuever[i] = manuever; //Manuver

               JSONObject Dist = StepsObj.getJSONObject("distance");
                SharedValues.DistMan[i] =Dist.getString("text"); //Distance

                JSONObject Coord = StepsObj.getJSONObject("start_location");
                lat = Coord.getDouble("lat");
                lang = Coord.getDouble("lng");
                SharedValues.Lat_Coord[i] = lat; //Coordinates for start
                SharedValues.Lang_Coord[i] = lang;


                Log.d(TAG, "MakeUseFull: " + shtml);
            }
        for(int i =0; i<length; i++){
            Log.d("Manuever: ", String.valueOf(i) + SharedValues.Manuever[i] + " :" +String.valueOf(SharedValues.Lat_Coord[i]) + " "+String.valueOf(SharedValues.Lang_Coord[i]));
        }
        SharedValues.isFinished = true;
        SharedValues.ArrayFilled = true;

    }

    @Override
    protected Object doInBackground(Object[] params) {
        BuildURL();
        try {
            URLReqest();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            MakeUseFull();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
    //We need to make some fuctions to test the heading.
   // request direciton here aswell
}
