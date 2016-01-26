package com.slimsimapps.petra;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

public class CreateWorkoutActivity extends AppCompatActivity {
    private static final String TAG = "AddWorkoutActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_workout);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_add_workout, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void sendNewWorkout(int iWorkoutType){
        EditText etWorkoutList = (EditText) findViewById(R.id.edit_workoutList);
        EditText etWorkoutName = (EditText) findViewById(R.id.edit_workoutName);

        String sWorkoutList = etWorkoutList.getText().toString();
        String sWorkoutName = etWorkoutName.getText().toString();


        if(sWorkoutList.length() == 0){
            new AlertDialog.Builder(this)
                    .setTitle("To fiew exercises")
                    .setMessage("You must have atleast one exercise in the list")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
            return;
        }

        long lDate = System.currentTimeMillis();

        String sRetur = lDate + ":Name_Ex_Splitter:" +
                iWorkoutType + ":Name_Ex_Splitter:" +
                sWorkoutName + ":Name_Ex_Splitter:" +
                sWorkoutList;

        Intent returnIntent = new Intent();
        returnIntent.putExtra("sRetur", sRetur);
        setResult(RESULT_OK, returnIntent);

        finish();
    }

    public void sendNewStretchWorkout(View view){
        sendNewWorkout(G.WORKOUT_STRETCH);
    }
    public void sendNewSingleWorkout(View view){
        sendNewWorkout(G.WORKOUT_SINGLE);
    }
    public void sendNewDoubleWorkout(View view){
        sendNewWorkout(G.WORKOUT_DOUBLE);
    }
    public void sendNewTabataWorkout(View view){
        sendNewWorkout(G.WORKOUT_TABATA);
    }

}
