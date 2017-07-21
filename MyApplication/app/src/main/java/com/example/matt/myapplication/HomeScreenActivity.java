package com.example.matt.myapplication;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;
import com.google.firebase.quickstart.fcm.Messaging;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static android.Manifest.permission.READ_PHONE_STATE;
import static android.Manifest.permission.SEND_SMS;


public class HomeScreenActivity extends AppCompatActivity implements View.OnClickListener{
    private static final String TAG ="Matt";
    //Keeps track of whether you are logged in or not
    private boolean logStatus;
    //Keeps track of which station
    private int stationTracker=0;
    //String variables
    private String API_URL1="https://lit-retreat-58620.herokuapp.com/nearCait";
    private String API_URL2="https://lit-retreat-58620.herokuapp.com/nearHandicap";
    private String API_URL3="";
    private String API_URL4="";
    private String a="Unavailable";
    private String b="Send Request to Person";
    private String c="Login to Station";
    private String d="Loading";
    private String e="Logout of Station";
    private String f;
    //Message token
    private String token= FirebaseInstanceId.getInstance().getToken();
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
    private DatabaseReference databaseReference;
    private FirebaseUser user;
    //Listener for phone number in database
    private ValueEventListener postListener;
    private ValueEventListener postListener1;
    private ValueEventListener postListener2;
    //Phone Message and phone number
    private String message="Can I have your space?";
    private String phoneNo;
    private String recipient="";
    //Class for ftp
    MyFTPClientFunctions eboard=new MyFTPClientFunctions();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);
        //Adds permissions
        ActivityCompat.requestPermissions(this,new String[]{SEND_SMS},1);
        ActivityCompat.requestPermissions(this,new String[]{READ_PHONE_STATE},1);
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
        databaseReference = FirebaseDatabase.getInstance().getReference();
        //Gets current User
        user=mAuth.getCurrentUser();
        //Sets Listeners for buttons
        Login1.setOnClickListener(this);
        Login2.setOnClickListener(this);
        Login3.setOnClickListener(this);
        Login4.setOnClickListener(this);
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
        //Initializes phone number
        TelephonyManager tMgr = (TelephonyManager)this.getSystemService(Context.TELEPHONY_SERVICE);
        phoneNo = tMgr.getLine1Number();
        //Checks to see who is logged into a station already
        //Initializes listener for database


        Retriever(Login1);
        Retriever(Login2);
        Retriever(Login3);
        Retriever(Login4);


    }
    /*
   //
   //
   //
   //
    */
    /*
    //'128.6.22.22','rutgers3dlab','rutgersmse','Port 21','Website',
    public void eBoardAPI(String server, int portNumber, String user, String password, String filename, String localFile){
        eboard.downloadAndSaveFile(server,portNumber,user,password,filename,localFile);
    }
    */
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
            TimeLeft1.setText(d);
            TimeLeft3.setText(d);
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
                    if( !( Login.getId()==stationTracker )  ){
                        Login.setText(b);
                    }
                    //Issues notification when car is 90% charged
                    if( percent >= 90 ){
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
                    if( !( Login.getId()==stationTracker ) ){
                        Login.setText(b);
                    }
                }
                //When the Station is not plugged in but someone is still logged into it
                else{
                    TimeLeft.setText(R.string.open);
                    if( !( Login.getId()==stationTracker ) ){
                        Login.setText(b);
                    }
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
//Retrieves data from database to see who is logged into a station
    public void Retriever(final Button log){
        //Checks to see if you are logged in to any stations
        postListener1 = new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Logger post = dataSnapshot.child( "stations" ).child( String.valueOf(log.getId()) ).getValue( Logger.class );

                String name=post.name;
                if( name.equals(phoneNo.toString())   ){
                    log.setText(e);
                    logStatus=true;
                    stationTracker=log.getId();
                    StationUpdate(log);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Toast.makeText(HomeScreenActivity.this, "Couldn't retrieve data", Toast.LENGTH_SHORT).show();
                // ...

            }

        };
        //Checks for other people who are logged in to any stations
        postListener2 = new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Logger post = dataSnapshot.child( "stations" ).child( String.valueOf(log.getId()) ).getValue( Logger.class );

                String name=post.name;
                if( !( name.equals( phoneNo.toString()) ) & !(name.equals("None")) ){
                    log.setText(b);
                    StationUpdate(log);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Toast.makeText(HomeScreenActivity.this, "Couldn't retrieve data", Toast.LENGTH_SHORT).show();
                // ...

            }

        };

        databaseReference.addListenerForSingleValueEvent(postListener1);
        databaseReference.addListenerForSingleValueEvent(postListener2);
    }
    /*
    //
    //
    //
    //
    //
     */
//Logs a person into a desired station
public void Login(final Button log){
    //Checks log status
    if(logStatus){
        Toast.makeText(HomeScreenActivity.this, "You can't login to two stations",
                Toast.LENGTH_SHORT).show();
                f=c;
        return;
    }
    //Initializes user information
    Logger posted = new Logger(phoneNo);
    Map<String, Object> postValues = posted.toMap();
    Map<String, Object> childUpdates = new HashMap<>();
    childUpdates.put("/stations/" + log.getId() , postValues);
    //Updates database with new info
    databaseReference.updateChildren(childUpdates);
    //Sets log Status
    logStatus=true;
    stationTracker=log.getId();
    f=e;
    //Tells User operation was successful
    Toast.makeText(HomeScreenActivity.this, "You are logged into the station.",
            Toast.LENGTH_SHORT).show();
    StationUpdate(log);
}
    /*
    //
    //
    //
    //
     */
    public void Logout(final Button log){
        //Declares variables for storage
        String myUserId = "None";
        //Initializes user information
        Logger post = new Logger(myUserId);
        Map<String, Object> postValues = post.toMap();
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/stations/" + log.getId() , postValues);
        //Updates database with new info
        databaseReference.updateChildren(childUpdates);
        //Tells User operation was successful
        Toast.makeText(HomeScreenActivity.this, "You are logged out of the station.",
                Toast.LENGTH_SHORT).show();
        //Sets log Status
        logStatus=false;
        StationUpdate(log);
        stationTracker=0;
        f=c;
    }
    /*
    //
    //
    //
    //
     */
    private void sendSMS(String phoneNumber, String message)
    {
            SmsManager sms = SmsManager.getDefault();
            sms.sendTextMessage(phoneNumber, null, message, null, null);
    }
    /*
    //
    //
    //
    //
     */
    //Access api response from custom website and updates stations when someone logs into one
    public void StationUpdate(final Button log){
        //Logout
        if( !logStatus & stationTracker==log.getId() ){
            switch (log.getId()){
                //Station 1
                case 2131427431:
                    TimeLeft1.setText("Open");
                    Status1.setMax(100);
                    Status1.setProgress(0);
                    break;
                //Station 2
                case 2131427439:
                    TimeLeft2.setText("Open");
                    Status2.setMax(100);
                    Status2.setProgress(0);
                    break;
                //Station 3
                case 2131427441:
                    TimeLeft3.setText("Open");
                    Status3.setMax(100);
                    Status3.setProgress(0);
                    break;
                //Station 4
                case 2131427445:
                    TimeLeft4.setText("Open");
                    Status4.setMax(100);
                    Status4.setProgress(0);
                    break;

            }
        }
        //Login for user and update stations others are logged into
        else{
            switch (log.getId()){
                //Station 1
                case 2131427431:
                    new JuicenetApi().execute(API_URL1);
                    break;
                //Station 2
                case 2131427439:
                    break;
                //Station 3
                case 2131427441:
                    new JuicenetApi().execute(API_URL2);
                    break;
                //Station 4
                case 2131427445:
                    break;
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
    public void onClick(final View v) {
        //Station button protocol
        if(v==Login1 | v==Login2 | v==Login3 | v==Login4){
            final Button click= (Button) v;
            if( click.getText().equals(a) ) {
                Toast.makeText(HomeScreenActivity.this, "You can't log in while someone's car is charging.",
                        Toast.LENGTH_SHORT).show();
            }
            else if( click.getText().equals(b) ){
                postListener1 = new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        Logger post = dataSnapshot.child("stations").child(String.valueOf(v.getId())).getValue(Logger.class);
                        //Declares variables for storage
                        recipient=post.name;
                        sendSMS(recipient,message);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        // Getting Post failed, log a message
                        Toast.makeText(HomeScreenActivity.this, "Couldn't retrieve data", Toast.LENGTH_SHORT).show();
                        // ...
                    }
                };
                databaseReference.addListenerForSingleValueEvent(postListener1);
            }
            else if( click.getText().equals(c) ){
                Login( click );
                click.setText(f);
            }
            else if( click.getText().equals(e) ){
                Logout( click );
                click.setText(f);
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
        //Refresh
        else if(v==Refresh){
            Retriever(Login1);
            Retriever(Login2);
            Retriever(Login3);
            Retriever(Login4);
        }
    }

}
