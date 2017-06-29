package com.example.matt.myapplication;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Hashtable;


public class HomeScreenActivity extends AppCompatActivity implements View.OnClickListener{
    //String variables
    private String API_URL1="https://lit-retreat-58620.herokuapp.com/nearCait";
    private String API_URL2="https://lit-retreat-58620.herokuapp.com/nearHandicap";
    private String a="Unavailable";
    private String b="Send Request to Person";
    private String c="Log Into Station";
    private String d="Loading";
    //For tracking when someone logs in
    private boolean Log1=false;
    private boolean Log2=false;
    private boolean Log3=false;
    private boolean Log4=false;
    //Variables for visible stuff
    private TextView Station1;
    private TextView Station2;
    private TextView Station3;
    private TextView Station4;
    private TextView TimeLeft1;
    private TextView TimeLeft2;
    private TextView TimeLeft3;
    private TextView TimeLeft4;
    private ProgressBar Status1;
    private ProgressBar Status2;
    private ProgressBar Status3;
    private ProgressBar Status4;
    private Button Login1;
    private Button Login2;
    private Button Login3;
    private Button Login4;
    private Button Logout;
    private Button Update;
    private Button Refresh;
    //Notification
    private NotificationCompat.Builder mBuilder;
    //For logout
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);
        //Initializes variables
        mAuth = FirebaseAuth.getInstance();
        Station1= (TextView)findViewById(R.id.hStation1);
        Station2= (TextView)findViewById(R.id.hStation2);
        Station3= (TextView)findViewById(R.id.hStation3);
        Station4= (TextView)findViewById(R.id.hStation4);
        TimeLeft1=(TextView)findViewById(R.id.hTimeLeft1);
        TimeLeft2=(TextView)findViewById(R.id.hTimeLeft2);
        TimeLeft3=(TextView)findViewById(R.id.hTimeLeft3);
        TimeLeft4=(TextView)findViewById(R.id.hTimeLeft4);
        Status1= (ProgressBar)findViewById(R.id.hStatus1);
        Status2= (ProgressBar)findViewById(R.id.hStatus2);
        Status3= (ProgressBar)findViewById(R.id.hStatus3);
        Status4= (ProgressBar)findViewById(R.id.hStatus4);
        Login1=(Button)findViewById(R.id.hLogin1);
        Login2=(Button)findViewById(R.id.hLogin2);
        Login3=(Button)findViewById(R.id.hLogin3);
        Login4=(Button)findViewById(R.id.hLogin4);
        Logout=(Button)findViewById(R.id.hLogOut);
        Update=(Button)findViewById(R.id.hUpdate);
        Refresh=(Button)findViewById(R.id.hRefresh);
        //Sets Listeners for buttons
        Login1.setOnClickListener(this);
        Login2.setOnClickListener(this);
        Login3.setOnClickListener(this);
        Logout.setOnClickListener(this);
        Update.setOnClickListener(this);
        Refresh.setOnClickListener(this);
        //Makes Notification
        mBuilder = (NotificationCompat.Builder) new NotificationCompat.Builder(this)
                //Notification appearance
                        .setSmallIcon(R.drawable.notification_icon)
                //Title for notification
                        .setContentTitle("RU Charged")
                //Notification message
                        .setContentText("Your car is almost done charging.");
        //Tells notification what activity to open when clicked
        Intent resultIntent = new Intent(this, HomeScreenActivity.class);
        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);

        //Access api response from custom website
        new JuicenetApi().execute(API_URL1);
        new JuicenetApi().execute(API_URL2);
    }
    /*
    //
    //
    //
    //
     */
    private class JuicenetApi extends AsyncTask<String,Void,String> {
        private TextView Station;
        private TextView TimeLeft;
        private ProgressBar Status;
        private Button Login;
        protected void onPreExecute(){
            Login1.setText("Loading");
            TimeLeft1.setText("Loading");
            Login3.setText("Loading");
            TimeLeft3.setText("Loading");
        }
        ///
        ///
        protected String doInBackground(String...API_URL) {
            //Decides which station is used for operation
            if( API_URL[0].equals("https://lit-retreat-58620.herokuapp.com/nearCait") ) {
                Station=Station1;
                TimeLeft=TimeLeft1;
                Status=Status1;
                Login=Login1;
            }
            else {
                Station=Station3;
                TimeLeft=TimeLeft3;
                Status=Status3;
                Login=Login3;
            }
            //Reads JSON as String
            HttpURLConnection client = null;

            try {
                URL url = new URL(API_URL[0]);
                client = (HttpURLConnection) url.openConnection();

                client.setRequestMethod("GET");
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(client.getInputStream()));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line).append("\n");
                }
                bufferedReader.close();
                return stringBuilder.toString();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (client != null) // Make sure the connection is not null.
                    client.disconnect();
            }
            return null;
        }
        ///
        ///
        //Takes the JSON string and uses it to perform tasks
        public void onPostExecute(String response){

            if(response == null) {
                response = "THERE WAS AN ERROR";
                Station.setText(response);
                return;
            }

            try{
                //Converts string to JSON
                JSONObject object = (JSONObject) new JSONTokener(response).nextValue();
                //Time Left
                Integer timeLeft = object.getInt("charging_time_left");
                //How long it has been charging
                String chargeInfo = object.getString("charging");
                JSONObject chargeObject = (JSONObject) new JSONTokener(chargeInfo).nextValue();
                Integer timeSoFar= chargeObject.getInt("seconds_charging");
                //How long the total charge time is
                Integer max=timeSoFar+timeLeft;
                //The state of the charging station(finished,charging,unplugged,etc)
                String state = object.getString("state");
                //Aesthetics and how the info is presented

                //For when the car is charging
                if( state.equals("charging") ) {
                    Status.setMax(max);
                    Status.setProgress(timeSoFar);
                    Integer timeRemaining=timeLeft/60;
                    Integer percent=( (timeSoFar*100) / max) ;
                    TimeLeft.setText( (timeSoFar/60) +" minutes // "+timeRemaining+" minutes // "+percent+"%");
                    Login.setText(a);
                    //Issues notification when car is 90% charged
                    if( ( (timeSoFar*100) / max) > 90 ){
                        // Sets an ID for the notification
                        int mNotificationId = 001;
                        // Gets an instance of the NotificationManager service
                        NotificationManager mNotifyMgr =
                                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                        // Builds the notification and issues it.
                        mNotifyMgr.notify(mNotificationId, mBuilder.build());
                    }

                }
                //When the station is plugged in but not charging
                else if( state.equals("plugged") ){
                    Status.setMax(max);
                    Status.setProgress(timeSoFar);
                    TimeLeft.setText(R.string.notCharging);
                    Login.setText(b);
                }
                //When the Station is not plugged in or something else
                else{
                    TimeLeft.setText(R.string.open);
                    Login.setText(c);
                    Status.setMax(max);
                    Status.setProgress(0);
                }
            //Catches errors in JSON conversion from string
            } catch (JSONException e) {
                e.printStackTrace();
                response = "THERE WAS JSON ERROR";
                Station.setText(response);
                return;
            }
        }
    }
/*
//
//
//
//
 */
//What happens when a button is clicked
    @Override
    public void onClick(View v) {
        //Station 1
        if(v==Login1){
            if( Login1.getText().equals(a) ) {
                Toast.makeText(HomeScreenActivity.this, "You can't log in while someone's car is charging.",
                        Toast.LENGTH_SHORT).show();
            }
            else if( Login1.getText().equals(b) ){

            }
            else if( Login1.getText().equals(c) ){
                Log1=true;
                Toast.makeText(HomeScreenActivity.this, "You are logged into the station.",
                        Toast.LENGTH_SHORT).show();
            }
        }

        //Station 2
        else if(v==Login2){
            if( Login2.getText().equals(a) ) {
                Toast.makeText(HomeScreenActivity.this, "You can't log in while someone's car is charging.",
                        Toast.LENGTH_SHORT).show();
            }
            else if( Login2.getText().equals(b) ){

            }
            else if( Login2.getText().equals(c) ){
                Log2=true;
                Toast.makeText(HomeScreenActivity.this, "You are logged into the station.",
                        Toast.LENGTH_SHORT).show();
            }
        }

        //Station 3
        else if(v==Login3){
            if( Login3.getText().equals(a) ) {
                Toast.makeText(HomeScreenActivity.this, "You can't log in while someone's car is charging.",
                        Toast.LENGTH_SHORT).show();
            }
            else if( Login3.getText().equals(b) ){

            }
            else if( Login3.getText().equals(c) ){
                Log3=true;
                Toast.makeText(HomeScreenActivity.this, "You are logged into the station.",
                        Toast.LENGTH_SHORT).show();
            }
        }
        //Logout
        else if(v==Logout){
            mAuth.signOut();
            startActivity(new Intent(HomeScreenActivity.this,MainActivity.class));
        }
        //Update Profile
        else if(v==Update){
            startActivity(new Intent(HomeScreenActivity.this,UpdateActivity.class));
        }
        else if(v==Refresh){
            new JuicenetApi().execute(API_URL1);
            new JuicenetApi().execute(API_URL2);
        }
    }

}
