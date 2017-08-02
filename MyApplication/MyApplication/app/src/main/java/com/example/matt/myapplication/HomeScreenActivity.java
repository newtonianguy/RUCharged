package com.example.matt.myapplication;

import android.app.Dialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import static android.Manifest.permission.READ_PHONE_STATE;
import static android.Manifest.permission.SEND_SMS;
import static java.lang.Math.abs;


public class HomeScreenActivity extends AppCompatActivity implements View.OnClickListener{
    //For error checking
    private static final String TAG ="Matt";
    //cooldown for when a message can be sent(5 minutes represented in milliseconds)
    double cooldown = 300000.0;
    //Keeps track of whether you are logged in or not
    private boolean logStatus;
    //Keeps track of whether a station is taken or not
    private boolean stationStatus=false;
    //String variables
    private String API_URL1="https://lit-retreat-58620.herokuapp.com/nearCait";
    private String API_URL2="https://lit-retreat-58620.herokuapp.com/nearHandicap";
    private String a="Unavailable";
    private String d="Loading";
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
    private ValueEventListener postListener;//actively watches station log status
    private ValueEventListener postListener1;//Watches stations once when called
    private ValueEventListener postListener2;//watches users once when called
    //Phone Message and phone number
    private String phoneMessage="";
    private String phoneNo;

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
        Retriever(Login1,1);
        Retriever(Login2,1);
        Retriever(Login3,1);
        Retriever(Login4,1);
        //Checks to see if the log status for stations has changed in realtime
        Update(Login1);
        Update(Login2);
        Update(Login3);
        Update(Login4);
    }
    /*
   //
   //
   //
   //
    */
    //Gets information on Stations 2 and 4 using ftp
    private class eBoardApi extends AsyncTask<String,Void,String> {

        //Initializes buttons and textviews
        private TextView Station;
        private TextView TimeLeft;
        private ProgressBar Status;


        @Override
        protected String doInBackground(String... params) {
            //Sets parameters and ftp client
            FTPClient ftp = null;
            String server= params[0];
            Integer portNumber= Integer.valueOf(params[1]);
            String username=params[2];
            String password=params[3];
            String filename=params[4];
            String localFile=params[5];
            String station=params[6];

            //Decides which station is used for operation
            if( station.equals("Station2") ) {
                Station=Station2;
                TimeLeft=TimeLeft2;
                Status=Status2;
            }
            else {
                Station=Station4;
                TimeLeft=TimeLeft4;
                Status=Status4;
            }

            //Gets eboard data using ftp
            try {
                //Connects to ftp client
                ftp = new FTPClient();
                ftp.connect(server, portNumber);
                ftp.login(username, password);
                ftp.setFileType(FTP.BINARY_FILE_TYPE);
                ftp.enterLocalPassiveMode();
                //changes directory
                ftp.changeWorkingDirectory(localFile);
                //Reads file
                InputStream inStream = ftp.retrieveFileStream(filename);
                InputStreamReader isr = new InputStreamReader(inStream, "UTF8");
                BufferedReader bufferedReader = new BufferedReader(isr);
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line);
                }
                bufferedReader.close();
                //Returns contents of file
                return stringBuilder.toString();
            } catch (UnsupportedEncodingException e1) {
                e1.printStackTrace();
            } catch (SocketException e1) {
                e1.printStackTrace();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            } finally {
                if (ftp != null) {
                    try {
                        ftp.logout();
                        ftp.disconnect();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return null;
        }
        @Override
        public void onPostExecute(String response){
            //Gets individual data from csv file
            StringTokenizer tokens = new StringTokenizer(response, ",");
            tokens.nextToken();
            tokens.nextToken();
            String third = tokens.nextToken();//Egauge Station2
            String fourth = tokens.nextToken();//Egauge Station2
            String fifth = tokens.nextToken();//Egauge Station4
            String sixth = tokens.nextToken();//Egauge Station4
            tokens.nextToken();
            tokens.nextToken();
            tokens.nextToken();
            String tenth = tokens.nextToken();//Juicenet Station1
            tokens.nextToken();
            tokens.nextToken();
            String thirteenth = tokens.nextToken();//Juicenet Station3
            //Trims data to the necessary components
            String sub1=third.substring(0,third.length()-2);
            String sub2=fourth.substring(0,fourth.length()-2);
            String sub3=fifth.substring(0,fifth.length()-2);
            String sub4=sixth.substring(0,sixth.length()-2);
            String sub5=tenth.substring(0,tenth.length()-3);
            String sub6=thirteenth;
            //Converts data to decimals from string
            Double power1= Double.valueOf(sub1);
            Double power2= Double.valueOf(sub2);
            Double power3= Double.valueOf(sub3);
            Double power4= Double.valueOf(sub4);
            Double power5= 1000*Double.valueOf(sub5);
            Double power6= 1000*Double.valueOf(sub6);
            //Finds power draw from each station
            Double stat2=power1+power2-power5;
            Double stat4=power3+power4-power6;
            //If getting info for station 2
            if(Station==Station2){
                if( stat2==0 ){
                    TimeLeft.setText("Open");
                    Status.setProgress(0);
                    Status.setMax(100);
                }
                else if( 0<abs(stat2) & abs(stat2)<1000){
                    TimeLeft.setText("Not Charging");
                }
                else if( abs(stat2)>1000 ){
                    TimeLeft.setText("Charging");
                }
            }
            //If getting info for station 4
            else{
                if( stat4==0 ){
                    TimeLeft.setText("Open");
                    Status.setProgress(0);
                    Status.setMax(100);
                }
                else if( 0<abs(stat4) & abs(stat4)<1000){
                    TimeLeft.setText("Not Charging");
                }
                else if( abs(stat4)>1000 ){
                    TimeLeft.setText("Charging");
                }
            }

        }
    }
    /*
    //
    //
    //
    //
     */
    //Gets information on Stations 1 and 3 using juicenet api
    private class JuicenetApi extends AsyncTask<String,Void,String> {
        private TextView Station;
        private TextView TimeLeft;
        private ProgressBar Status;
        ///
        ///
        //Other portion of api request is done on a website
        protected String doInBackground(String...API_URL) {
            //Decides which station is used for operation
            if( API_URL[0].equals("https://lit-retreat-58620.herokuapp.com/nearCait") ) {
                Station=Station1;
                TimeLeft=TimeLeft1;
                Status=Status1;
            }
            else {
                Station=Station3;
                TimeLeft=TimeLeft3;
                Status=Status3;
            }
            //Reads JSON as String
            HttpURLConnection client = null;

            try {
                //Connects to url
                URL url = new URL(API_URL[0]);
                client = (HttpURLConnection) url.openConnection();
                //Reads websites contents
                client.setRequestMethod("GET");
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(client.getInputStream()));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line).append("/n");
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
                }
                //When the Station is not plugged in but someone is still logged into it
                else{
                    TimeLeft.setText(R.string.open);
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
//Retrieves from database who is logged into a specified station
    //***IMPORTANT***//
    //Any other functions using data retrieved from data base must be used within "onDataChanged"
    //***IMPORTANT***//
    public void Retriever(final Button log,final int method){
        //Checks to see if you are logged in to any stations
        postListener1 = new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Logger post = dataSnapshot.child( "stations" ).child( String.valueOf(log.getId()) ).getValue( Logger.class );
                String val =post.name;
                switch(method){
                    case 1:
                        StationUpdate(log,val);
                        break;
                    case 2:
                        CustomMessage(val);
                        break;
                    case 3:
                        checkStatus(val);
                        break;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(HomeScreenActivity.this, "Couldn't retrieve data", Toast.LENGTH_SHORT).show();
            }
        };
        databaseReference.addListenerForSingleValueEvent(postListener1);
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
        return;
    }
    //Checks to make sure no one else is logged in
    Retriever(log,3);
    if( stationStatus ){
        Toast.makeText(HomeScreenActivity.this, "Someone is logged in already",
                Toast.LENGTH_SHORT).show();
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
    //Tells User operation was successful
    Toast.makeText(HomeScreenActivity.this, "You are logged into the station.",
            Toast.LENGTH_SHORT).show();
    //Updates homescreen
    Retriever(log,1);
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
        Retriever(log,1);
    }
    /*
    //
    //
    //
    //
     */
    private void sendSMS(final String phoneNumber, final String message1) {
        //Looks to see when last message was sent
        postListener2 = new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Post post = dataSnapshot.child( "users" ).child( user.getUid() ).getValue( Post.class );
                String name=post.name;
                String type=post.type;
                Date time =post.time;
                Date now=new Date();
                //Log.w(TAG, String.valueOf(now.getTime()));
                //Log.w(TAG, String.valueOf(time.getTime()));
                //Log.w(TAG, String.valueOf( now.getTime()-time.getTime() ));
                if( ( now.getTime() - time.getTime() ) >= cooldown ){
                    //Sends Message
                    SmsManager sms = SmsManager.getDefault();
                    String message=message1+"\n-"+name;
                    sms.sendTextMessage(phoneNumber, null, message, null, null);
                    Toast.makeText(HomeScreenActivity.this, "Message Sent", Toast.LENGTH_SHORT).show();
                    //Updates database with the time the message was sent
                    Post userInfo=new Post(name,type,phoneNo,now);
                    databaseReference.child("users").child( user.getUid() ).setValue(userInfo);
                }
                else{
                    Toast.makeText(HomeScreenActivity.this, "You can only send a message once every five minutes", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(HomeScreenActivity.this, "Couldn't retrieve time previous message was sent", Toast.LENGTH_SHORT).show();
            }

        };
        databaseReference.addListenerForSingleValueEvent(postListener2);
    }
    /*
    //
    //
    //
    //
     */
    //Updates home screen with information from database
    public void StationUpdate(final Button log,String val){
        //Updates home screen
        switch(log.getId()){
            //
            //Station 1
            //
            case 2131427431:
                //No one is logged into spot
                if(val.equals("None")){
                    TimeLeft1.setText("Open");
                    Status1.setMax(100);
                    Status1.setProgress(0);
                    log.setBackgroundResource(R.drawable.loginbuttongreen);
                }
                //You are logged into spot
                else if( val.equals(phoneNo) ){
                    logStatus=true;
                    TimeLeft1.setText(d);
                    log.setBackgroundResource(R.drawable.logoutbuttonred);
                    new JuicenetApi().execute(API_URL1);
                }
                //Some one else is logged into this spot
                else{
                    TimeLeft1.setText(d);
                    log.setBackgroundResource(R.drawable.sendrequestbutton);
                    new JuicenetApi().execute(API_URL1);
                }
                break;
            //
            //Station 2
            //
            case 2131427439:
                //No one is logged into spot
                if(val.equals("None")){
                    TimeLeft2.setText("Open");
                    Status2.setMax(100);
                    Status2.setProgress(0);
                    log.setBackgroundResource(R.drawable.loginbuttongreen);
                }
                //You are logged into spot
                else if( val.equals(phoneNo) ){
                    logStatus=true;
                    TimeLeft2.setText(d);
                    log.setBackgroundResource(R.drawable.logoutbuttonred);
                    new eBoardApi().execute("128.6.22.22", "21", "rutgers3dlab", "rutgersmse", "transfer.csv", "Website","Station2");
                }
                //Some one else is logged into this spot
                else{
                    TimeLeft2.setText(d);
                    log.setBackgroundResource((R.drawable.sendrequestbutton));
                    new eBoardApi().execute("128.6.22.22", "21", "rutgers3dlab", "rutgersmse", "transfer.csv", "Website","Station2");
                }
                break;
            //
            //Station 3
            //
            case 2131427441:
                //No one is logged into spot
                if(val.equals("None")){
                    TimeLeft3.setText("Open");
                    Status3.setMax(100);
                    Status3.setProgress(0);
                    log.setBackgroundResource(R.drawable.loginbuttongreen);
                }
                //You are logged into spot
                else if( val.equals(phoneNo) ){
                    logStatus=true;
                    TimeLeft3.setText(d);
                    log.setBackgroundResource(R.drawable.logoutbuttonred);
                    new JuicenetApi().execute(API_URL2);
                }
                //Some one else is logged into this spot
                else{
                    TimeLeft3.setText(d);
                    log.setBackgroundResource((R.drawable.sendrequestbutton));
                    new JuicenetApi().execute(API_URL2);
                }
                break;
            //
            //Station 4
            //
            case 2131427445:
                //No one is logged into spot
                if(val.equals("None")){
                    TimeLeft4.setText("Open");
                    Status4.setMax(100);
                    Status4.setProgress(0);
                    log.setBackgroundResource(R.drawable.loginbuttongreen);
                }
                //You are logged into spot
                else if( val.equals(phoneNo) ){
                    logStatus=true;
                    TimeLeft4.setText(d);
                    log.setBackgroundResource(R.drawable.logoutbuttonred);
                    new eBoardApi().execute("128.6.22.22", "21", "rutgers3dlab", "rutgersmse", "transfer.csv", "Website","Station4");
                }
                //Some one else is logged into this spot
                else{
                    TimeLeft4.setText(d);
                    log.setBackgroundResource((R.drawable.sendrequestbutton));
                    new eBoardApi().execute("128.6.22.22", "21", "rutgers3dlab", "rutgersmse", "transfer.csv", "Website","Station4");
                }
                break;
        }
    }
    /*
    //
    //
    //
    //
     */
    //Watches the database for changes and updates app when it does
    public void Update(final Button log){
        postListener = new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Logger post = dataSnapshot.child( "stations" ).child( String.valueOf(log.getId()) ).getValue( Logger.class );
                String val =post.name;
                StationUpdate(log,val);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "Yes");
                Toast.makeText(HomeScreenActivity.this, "Couldn't retrieve Active Station data", Toast.LENGTH_SHORT).show();
            }
        };
        databaseReference.addValueEventListener(postListener);
    }
    /*
    //
    //
    //
    //
     */
    //Checks to see if anyone is already logged into station
    public void checkStatus( String value ){
        stationStatus = !value.equals("None");
    }
    /*
    //
    //
    //
    //
     */
    //Code for custom message pop (dialog)
public void CustomMessage(final String phoneNumber){
    // custom dialog
    final Dialog dialog = new Dialog(HomeScreenActivity.this);
    dialog.setContentView(R.layout.custom_message);
    //Sets dialog variables
    Button send=(Button) dialog.findViewById(R.id.pSend);
    final EditText message=(EditText) dialog.findViewById(R.id.pMessage);
    //Sets the message to be sent to whatever the user typed in
    //default is "Can I have your space?"
    send.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            phoneMessage= message.getText().toString();
            Log.w(TAG, phoneMessage);
            if( phoneMessage.isEmpty() ){
                phoneMessage="Can I have your space?";
            }
            sendSMS(phoneNumber,phoneMessage);
            dialog.dismiss();
        }
    });
    dialog.show();
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
            //Multiple Login Error
            if( click.getText().equals(a) ) {
                Toast.makeText(HomeScreenActivity.this, "You can't log in while someone's car is charging.",
                        Toast.LENGTH_SHORT).show();
            }
            //Send Message
            else if( click.getBackground().getConstantState().equals
                    (getResources().getDrawable(R.drawable.sendrequestbutton).getConstantState()) ){
                Retriever(click,2);
            }
            //Login
            else if( click.getBackground().getConstantState().equals
                    (getResources().getDrawable(R.drawable.loginbuttongreen).getConstantState()) ){
                Login( click );
            }
            //Logout of station
            else if( click.getBackground().getConstantState().equals
                    (getResources().getDrawable(R.drawable.logoutbuttonred).getConstantState()) ){
                Logout( click );
            }
        }
        //Logout of app
        else if(v==Logout){
            //detaches Listeners
            databaseReference.removeEventListener(postListener);
            databaseReference.removeEventListener(postListener);
            databaseReference.removeEventListener(postListener);

            //Signs person out of the app
            FirebaseAuth.getInstance().signOut();
            //Moves to the login screen
            startActivity(new Intent(HomeScreenActivity.this,MainActivity.class));
        }
        //Update Profile
        else if(v==Update){
            //detaches Listeners
            databaseReference.removeEventListener(postListener);
            databaseReference.removeEventListener(postListener);
            databaseReference.removeEventListener(postListener);
            databaseReference.removeEventListener(postListener);
            //Moves to update profile activity
            startActivity(new Intent(HomeScreenActivity.this,UpdateActivity.class));
        }
        //Refresh
        else if(v==Refresh){
            //detaches Listeners
            databaseReference.removeEventListener(postListener);
            databaseReference.removeEventListener(postListener);
            databaseReference.removeEventListener(postListener);
            databaseReference.removeEventListener(postListener);
            //refresh homescreen
            startActivity(new Intent(HomeScreenActivity.this,HomeScreenActivity.class));
        }
    }
}
