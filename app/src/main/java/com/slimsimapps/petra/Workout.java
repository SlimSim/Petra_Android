package com.slimsimapps.petra;


//import android.os.PowerManager;
import android.speech.tts.TextToSpeech;
import android.util.Log;


import java.util.Timer;
import java.util.TimerTask;

class Workout extends TimerTask {

    private final String TAG = "MainActivity";

    private boolean isRunning = false;


    private int iCurrentEx;
    private int iCurrentExTimeLeft;
    private int iCurrentExLength;
    TextToSpeech ttobj;
//    PowerManager.WakeLock wl;
//    View view;
    MainActivity parentActivity;



    String strWorkoutName;
    Exercise[] aExercise;
    int iWorkoutLength;
    int iTimeLeft;
    Timer timer = new Timer();

//    public void setWakeLock(PowerManager.WakeLock wakeLock){
        //       wl = wakeLock;
//    }



    public void setup(MainActivity a, TextToSpeech tts, String workoutName, Exercise[] aExc){
        parentActivity = a;

        ttobj = tts;

        aExercise = aExc;
        iCurrentEx = 0;
        iWorkoutLength = getTimeOfWorkout();
        iTimeLeft = iWorkoutLength;
        iCurrentExLength = aExercise[0].getSeconds();
        iCurrentExTimeLeft = iCurrentExLength;

        strWorkoutName = workoutName;

    }

    /*
    public void setView(View v){
        view = v;
    }
    */
    public void startWorkout(){
        timer.schedule(this, 5000, 1000);
        isRunning = true;
        String startWorkoutSpeach = "Lets do the " + strWorkoutName
                +", we start with " + aExercise[0].getName();
        ttobj.speak(startWorkoutSpeach, TextToSpeech.QUEUE_FLUSH, null);
    }


    private int getTimeOfWorkout(){
        //bla bla bla...
        int iTime = 0;
        for (Exercise anAExercise : aExercise) {
            iTime += anAExercise.getSeconds();
        }
        return iTime;

    }

    public boolean isRunning(){
        return isRunning;
    }

    public void endWorkout(boolean bSilent){
        parentActivity.resetTextfields();

        if(!bSilent && isRunning) {
            String mess = "Workout stopped";
            ttobj.speak(mess, TextToSpeech.QUEUE_FLUSH, null);
            parentActivity.showToast(mess);
        }
        isRunning = false;
        this.cancel();
    }

    private String addSomeToEx(String strInput){
        if(strInput.length()<3)
            return strInput;
        if(strInput.substring(0,3).equalsIgnoreCase("The"))
            return strInput;
        return "some " + strInput;
    }

    public void run() {
        parentActivity.showToast("Run!");
        if(iWorkoutLength == iTimeLeft){
            String exName = this.addSomeToEx(aExercise[0].getName());
            ttobj.speak("Lets do " + exName, TextToSpeech.QUEUE_FLUSH, null);
        }
        iCurrentExTimeLeft--;
        iTimeLeft--;
        if(iCurrentExTimeLeft != 0)
            this.parentActivity.setWorkAndExSecLeft(iTimeLeft, iCurrentExTimeLeft);

        int halfTime = iCurrentExLength / 2;
        boolean bRest = "Rest".equals(aExercise[iCurrentEx].getName());
        if(iCurrentExTimeLeft == halfTime){
            if(!bRest) {
                ttobj.speak("half time", TextToSpeech.QUEUE_FLUSH, null);
                return;
            }
        }

        switch(iCurrentExTimeLeft){
            case 30:
                ttobj.speak("30 seconds left", TextToSpeech.QUEUE_FLUSH, null);
                break;
            case 10:
                if(halfTime > 14)
                    ttobj.speak("10", TextToSpeech.QUEUE_FLUSH, null);
                break;
            case 5:
                if(aExercise[iCurrentEx].getSeconds() < 12)
                    break;
                String nextExercise = "Freedom!";
                if(iCurrentEx+1 < aExercise.length )
                    nextExercise = aExercise[iCurrentEx+1].getName();
                String strNextUp = "Next up " + nextExercise;
                ttobj.speak(strNextUp, TextToSpeech.QUEUE_FLUSH, null);
                break;
            case 0:
                if(++iCurrentEx >= aExercise.length) {
                    ttobj.speak("Great work, Now we are free!", TextToSpeech.QUEUE_FLUSH, null);
                    parentActivity.setCurrExercise("Good job!");
                    this.endWorkout(/*silent=*/ true);
                    break;
                }
                String strNext;
                if( aExercise[iCurrentEx].getName().equalsIgnoreCase("Rest") ){
                    strNext = "Great work! Lets rest for " + aExercise[iCurrentEx].getSeconds()
                            + " seconds, then we'll do "
                            + this.addSomeToEx(aExercise[iCurrentEx + 1].getName());
                    parentActivity.setCurrExercise(aExercise[iCurrentEx].getName()
                            + ", then "
                            + this.aExercise[iCurrentEx + 1].getName());
                } else {
                    if(aExercise[iCurrentEx-1].getName().equalsIgnoreCase("Rest")){
                        strNext = "OK!";
                    } else {
                        strNext = "Great work!";
                    }
                    strNext += " Lets do " + this.addSomeToEx(aExercise[iCurrentEx].getName());
                    parentActivity.setCurrExercise(aExercise[iCurrentEx].getName());
                }

                ttobj.speak(strNext, TextToSpeech.QUEUE_FLUSH, null);
                iCurrentExLength = aExercise[iCurrentEx].getSeconds();
                iCurrentExTimeLeft = iCurrentExLength;
                this.parentActivity.setWorkAndExSecLeft(iTimeLeft, iCurrentExTimeLeft);
                break;
        }//end switch (iCurrentExTimeLeft)
    } // end run()
}


