package com.slimsimapps.petra;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements TextToSpeech.OnInitListener {

    private final String TAG = "MainActivity";
    Workout oWorkout;
    TextToSpeech ttobj;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_main);
        ttobj=new TextToSpeech(getApplicationContext(),
                new TextToSpeech.OnInitListener() {
                    @Override
                    public void onInit(int status) {
                        if(status != TextToSpeech.ERROR){
                            ttobj.setLanguage(Locale.UK);
                        }
                    }
                });
        readFromDB();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(ttobj != null){
            oWorkout.endWorkout(/*silent=*/true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    //this is TTS-specific
    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            Toast.makeText(MainActivity.this,
                    "Text-To-Speech engine is initialized", Toast.LENGTH_SHORT).show();
        }
        else if (status == TextToSpeech.ERROR) {
            Toast.makeText(MainActivity.this,
                    "Error occurred while initializing Text-To-Speech engine", Toast.LENGTH_SHORT).show();
        }
    }

    public void showToast(final String strMessage) {
        runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(MainActivity.this, strMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void readFromDB(){
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        Map<String, ?> prefs = sharedPref.getAll();
        if(prefs.size() == 0){
            // This is used the first time the user starts the app, to load the default exercises
            saveWorkout("666:Name_Ex_Splitter:2:Name_Ex_Splitter:Quick Double Workout:Name_Ex_Splitter:The Plank, Leg raises, Lunges, Back extensions, Jumping jacks, Burpees");
            saveWorkout("668:Name_Ex_Splitter:2:Name_Ex_Splitter:Basic Double Workout:Name_Ex_Splitter:Crunches, Back extensions, Wall sit, Dive bombers, Side crunches, Bear crawls, Jump squats, Push-ups");
            saveWorkout("670:Name_Ex_Splitter:1:Name_Ex_Splitter:7 Min Workout:Name_Ex_Splitter:Jumping jacks, Wall sit, Push-up, Abdominal crunch, Step-up onto chair, Squats, Triceps dip on chair, The plank, High knees running, Lunges, Push-up and rotation, Side plank");
            saveWorkout("672:Name_Ex_Splitter:0:Name_Ex_Splitter:Basic Stretch Routine:Name_Ex_Splitter:Right hamstring, Left hamstring, Butterfly groin, Right laying hip, Left laying hip, Right quad, Left quad, Right calf, Left calf, Right shoulder, Left shoulder, Right triceps, Left triceps");
            prefs = sharedPref.getAll();
        }
        for (String key : prefs.keySet()) {
            String sWorkoutInfo = (String) prefs.get(key);
            //Log.i(TAG, key + " : " +  sWorkoutInfo);

            addWorkoutToXML(sWorkoutInfo);
        }
    }


    public void gotoCreateWorkout(View view){
        Intent intent = new Intent(this, CreateWorkoutActivity.class);
        startActivityForResult(intent, RESULT_FIRST_USER);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if(resultCode == RESULT_OK){
                String sRetur = data.getStringExtra("sRetur");
                saveWorkout(sRetur);
                addWorkoutToXML(sRetur);
            }
            if (resultCode == RESULT_CANCELED) {
                Log.i(TAG, "RESULT_CANCELED from AddWorkoutActivity");
                //Write your code if there's no result
            }
        }
    }//onActivityResult

    private void saveWorkout(String sWorkoutInfo){
        String sUniqueTime = sWorkoutInfo.split(":Name_Ex_Splitter:")[0];
        String sIdentifier = getString(R.string.DB_Saved_Workout) + sUniqueTime;

        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(sIdentifier, sWorkoutInfo);

        editor.apply();
    }

    public void removeWorkout(String sWorkoutInfo) {
        String sUniqueTime = sWorkoutInfo.split(":Name_Ex_Splitter:")[0];
        String sIdentifier = getString(R.string.DB_Saved_Workout) + sUniqueTime;

        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.remove(sIdentifier);
        editor.apply();

        int iUniqueShortId = getWorkoutShortId(sWorkoutInfo);
        LinearLayout llWorkout = (LinearLayout)findViewById(iUniqueShortId);
        LinearLayout llWorkoutList = (LinearLayout)findViewById(R.id.linearLayout_WorkoutList);
        llWorkoutList.removeView(llWorkout);
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void addWorkoutToXML(String sWorkoutInfo){
        LinearLayout llWorkoutList = (LinearLayout)findViewById(R.id.linearLayout_WorkoutList);
        LinearLayout llOuterWorkout = new LinearLayout(this);
        int iColor = 0;
        switch (Integer.parseInt(sWorkoutInfo.split(":Name_Ex_Splitter:")[1])){
            case G.WORKOUT_STRETCH: iColor = this.getResources().getColor(R.color.stretchWorkout); break;
            case G.WORKOUT_SINGLE: iColor = this.getResources().getColor(R.color.singleWorkout); break;
            case G.WORKOUT_DOUBLE: iColor = this.getResources().getColor(R.color.doubleWorkout); break;
            case G.WORKOUT_TABATA: iColor = this.getResources().getColor(R.color.tabataWorkout); break;
        }

        int iUniqueTime = getWorkoutShortId(sWorkoutInfo);
        llOuterWorkout.setId(iUniqueTime);
        llOuterWorkout.setOrientation(LinearLayout.HORIZONTAL);
        llOuterWorkout.setBackgroundColor(iColor);
        llOuterWorkout.setTag(sWorkoutInfo);
        llOuterWorkout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        llOuterWorkout.setElevation(getResources().getDimension(R.dimen.workoutElivation));

        llOuterWorkout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                doWorkout(arg0.getTag().toString());
            }
        });

        final Context context = this;
        llOuterWorkout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                final String sWorkoutInfo = v.getTag().toString();
                new AlertDialog.Builder(context)
                        .setTitle("Remove this workout?")
                        .setMessage("This action can not be undone")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                removeWorkout(sWorkoutInfo);
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
                return true;
            }
        });
        LinearLayout.LayoutParams lpOuterlayoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        int iMargin = getResources().getDimensionPixelSize(R.dimen.workoutMargin);
        lpOuterlayoutParams.setMargins(iMargin, iMargin, iMargin, iMargin);
        int iPadding = getResources().getDimensionPixelSize(R.dimen.workoutPadding);
        llOuterWorkout.setPadding(iPadding, iPadding, iPadding, iPadding);
        llWorkoutList.addView(llOuterWorkout, lpOuterlayoutParams);

        TextView tvExerciseList = new TextView(this);
        tvExerciseList.setGravity(Gravity.START);
        tvExerciseList.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        tvExerciseList.setTextAppearance(this, R.style.Theme_AppCompat);
        tvExerciseList.setText(getWorkoutExercises(sWorkoutInfo));

        LinearLayout llInnerRightInfo = new LinearLayout(this);
        llInnerRightInfo.setOrientation(LinearLayout.VERTICAL);
        llInnerRightInfo.setLayoutParams(new TableRow.LayoutParams(0/*width calculated later*/, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        llInnerRightInfo.setPadding((int) getResources().getDimension(R.dimen.workoutNameMargin), 0, 0, 0);
        llInnerRightInfo.setGravity(Gravity.END);

        TextView tvWorkoutName = new TextView(this);
        tvWorkoutName.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        tvWorkoutName.setTextColor(getResources().getColor(R.color.workoutName));
        tvWorkoutName.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.workoutName));
        tvWorkoutName.setText(getWorkoutName(sWorkoutInfo));
        tvWorkoutName.setGravity(Gravity.END);

        TextView tvTypeAndTime = new TextView(this);
        tvTypeAndTime.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        tvTypeAndTime.setTextAppearance(this, R.style.Theme_AppCompat);
        tvTypeAndTime.setText(getWorkoutType(sWorkoutInfo) + ", " + getWorkoutTime(sWorkoutInfo));

        llInnerRightInfo.addView(tvWorkoutName);
        llInnerRightInfo.addView(tvTypeAndTime);
        llOuterWorkout.addView(tvExerciseList);
        llOuterWorkout.addView(llInnerRightInfo);
    }// end addWorkoutToXML

    public void resetTextfields() {

        runOnUiThread(new Runnable() {
            public void run() {
                ((TextView) findViewById(R.id.iWorkoutTimeLeft)).setText(R.string.iWorkoutTimeLeft);
                ((TextView) findViewById(R.id.iExerciseTimeLeft)).setText(R.string.iExerciseTimeLeft);
                ((TextView) findViewById(R.id.currExercise)).setText(R.string.currentExercise);
                ((TextView) findViewById(R.id.currWorkout)).setText(R.string.currentWorkout);
            }
        });
    }

    public void stopExercise(View view){
        if(oWorkout == null){
            resetTextfields();
            return;
        }
        oWorkout.endWorkout(/*silent=*/false);
    }

    public void setCurrExercise(final String strExercise){
        runOnUiThread(new Runnable() {
            public void run() {
                ((TextView) findViewById(R.id.currExercise)).setText(strExercise);
            }
        });
    }

    public void setWorkAndExSecLeft(final int iTimeLeft, final int iCurrentExTimeLeft){
        runOnUiThread(new Runnable() {
            public void run() {
                ((TextView)findViewById(R.id.iWorkoutTimeLeft)).setText( secToDisp(iTimeLeft) );
                ((TextView)findViewById(R.id.iExerciseTimeLeft )).setText("" + iCurrentExTimeLeft);
            }
        });
    }

    public String secToDisp(int iSeconds){
        int iMin = iSeconds / 60;
        int iSec = iSeconds % 60;
        String strSec;
        if(iSec < 10)
            strSec = "0" + iSec;
        else
            strSec = "" + iSec;
        return iMin + ":" + strSec;
    }

    private int getWorkoutShortId(String sWorkoutInfo){
        String sUniqueTime = sWorkoutInfo.split(":Name_Ex_Splitter:")[0];
        int subLength = Math.max(sUniqueTime.length() - 9, 0);
        String sShortUniqueTime = sUniqueTime.substring(subLength);
        return Integer.parseInt(sShortUniqueTime); //iShortUniqueTime;
    }

    private String getWorkoutTime(String sWorkoutInfo){
        Exercise[] aExercise = getWorkoutExerciseList(sWorkoutInfo);
        int iTime = 0;
        for (Exercise anAExercise : aExercise) {
            iTime += anAExercise.getSeconds();
        }
        return secToDisp(iTime);
    }

    private String getWorkoutType(String sWorkoutInfo){
        String[] asWorkoutInfo = sWorkoutInfo.split(":Name_Ex_Splitter:");
        int iWorkoutType = Integer.parseInt(asWorkoutInfo[1]);
        switch (iWorkoutType) { //sWorkoutType
            case G.WORKOUT_STRETCH: return "Stretch";
            case 1: return "Single";
            case 2: return "Double";
            case 3: return "Tabata";
        }
        Log.e(TAG, "returning Unknown workout type");
        return "Unknown"; //this should not happen....
    }

    private String getWorkoutName(String sWorkoutInfo){
        String[] asWorkoutInfo = sWorkoutInfo.split(":Name_Ex_Splitter:");
        return asWorkoutInfo[2]; //sWorkoutName
    }

    public String getWorkoutExercises(String sWorkoutInfo){
        String[] asWorkoutInfo = sWorkoutInfo.split(":Name_Ex_Splitter:");
        String[] asExercises = asWorkoutInfo[3].split(","); //sWorkoutExercises
        if(asExercises.length == 0) return "";
        String sRet = asExercises[0].trim();
        if(asExercises.length > 0) sRet += ", ";
        for(int i=1; i<asExercises.length-1; i++){
            sRet += asExercises[i].trim();
            if(i%2 == 0)
                sRet += ", ";
            else
                sRet += ", \n";
        }
        sRet += asExercises[asExercises.length-1].trim();
        return sRet;

    }

    public Exercise[] getWorkoutExerciseList(String sWorkoutInfo){
        String[] asWorkoutInfo = sWorkoutInfo.split(":Name_Ex_Splitter:");
        int iWorkoutType = Integer.parseInt(asWorkoutInfo[1]);

        //TODO: fixa denna spit så att den funkar:
//        String[] asExerciseList = asWorkoutInfo[3].split("/[.,]+/"); //Denna funkar inte! detta är det regular expression jag använt i javascriptet, men det funkar inte riktigt nu....
        String[] asExerciseList = asWorkoutInfo[3].split(","); //Denna funkar inte! detta är det regular expression jag använt i javascriptet, men det funkar inte riktigt nu....

        switch (iWorkoutType) {
            case G.WORKOUT_STRETCH:
                return createExerciseArrayStretch(asExerciseList);
            case G.WORKOUT_SINGLE:
                return createExerciseArraySingle(asExerciseList);
            case G.WORKOUT_DOUBLE:
                return createExerciseArrayDouble(asExerciseList);
//TODO: fix the TABATA-workout!
//            case G.WORKOUT_TABATA:
//                return createExerciseArrayTabata(asExerciseList);
        }

        Log.e(TAG, "this workout type is not implemented yet! make it so!");
        return new Exercise[0];
    }

    private Exercise[] createExerciseArrayStretch(String[] asExerciseList){
        Exercise[] ret = new Exercise[asExerciseList.length];
        for(int i=0; i<asExerciseList.length; i+=1){
            ret[i] = new Exercise(asExerciseList[i], 24);
        }
        return ret;
    }

    private Exercise[] createExerciseArraySingle(String[] asExerciseList){
        ArrayList<String> alsName = new ArrayList<>();
        ArrayList<Integer> aliTime = new ArrayList<>();
        for (String anAsExerciseList : asExerciseList) {
            alsName.add(anAsExerciseList);
            aliTime.add(30);
            alsName.add("Rest");
            aliTime.add(10);
        }
        int iSize = alsName.size()-1;
        Exercise[] ret = new Exercise[iSize];
        for(int i=0; i<iSize; i++){
            ret[i] = new Exercise(alsName.get(i), aliTime.get(i));
        }
        return ret;
    }

    private Exercise[] createExerciseArrayDouble(String[] asExerciseList){
        ArrayList<String> alsName = new ArrayList<>();
        ArrayList<Integer> aliTime = new ArrayList<>();
        for(int i=0; i<asExerciseList.length; i+=2){
            alsName.add(asExerciseList[i]);
            aliTime.add(45);
            if(i+1<asExerciseList.length) {
                alsName.add(asExerciseList[i+1]);
                aliTime.add(45);
            }
            alsName.add(asExerciseList[i]);
            aliTime.add(30);
            if(i+1<asExerciseList.length) {
                alsName.add(asExerciseList[i + 1]);
                aliTime.add(30);
            }
            alsName.add("Rest");
            aliTime.add(15);
        }
        int iSize = alsName.size()-1;

        Exercise[] ret =  new Exercise[iSize];
        for(int i=0; i<iSize; i++){
            ret[i] = new Exercise(alsName.get(i), aliTime.get(i));
        }
        return ret;
    }

    public void doWorkout(String sWorkoutInfo){
        Exercise[] aExercise = getWorkoutExerciseList(sWorkoutInfo);
        String sWorkoutName = getWorkoutName(sWorkoutInfo);

        if(aExercise.length == 0) return;

        ((TextView)findViewById(R.id.currWorkout)).setText(sWorkoutName);
        ((TextView)findViewById(R.id.currExercise)).setText(aExercise[0].getName());

        if (oWorkout == null) {
            oWorkout = new Workout();
        } else {
            if(oWorkout.isRunning()){
                oWorkout.endWorkout(/*silent=*/true);
            }
            oWorkout = null;
            oWorkout = new Workout();
        }

        oWorkout.setup(this, ttobj, sWorkoutName, aExercise);

        int iExTime = aExercise[0].getSeconds();
        int iWorkTime = oWorkout.iWorkoutLength;
        setWorkAndExSecLeft(iWorkTime, iExTime);

        oWorkout.startWorkout();

    } // end doWorkout

} // end MainActivity Class
