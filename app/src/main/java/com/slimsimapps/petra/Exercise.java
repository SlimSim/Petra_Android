package com.slimsimapps.petra;

//import android.speech.tts.TextToSpeech;
//import android.util.Log;

/**
 * This file is part of Petra
 * Created by SlimSim on 2015-02-13.
 */
public class Exercise {
    String strName = "no name";
    int iSeconds = 0;

    public Exercise(String name, int seconds){
        this.strName = name;
        this.iSeconds = seconds;
    }
    public String getName(){ return this.strName; }
    public int getSeconds(){ return this.iSeconds; }

}