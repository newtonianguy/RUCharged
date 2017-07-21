package com.example.matt.myapplication;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class UpdateActivity extends AppCompatActivity implements View.OnClickListener{
    private FirebaseAuth mAuth;
    private EditText OldEmail;
    private EditText OldPassword;
    private EditText Username;
    private EditText Password;
    private EditText Email;
    private EditText Type;
    private String PhoneNo;
    private Button update;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference databaseReference;
    private FirebaseUser user;
    //Puts up current profile details
    private String myUserId;
    //Listener for Database
    private ValueEventListener postListener;

    /*
    ///
    ///
    ///
    ///
    ///
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update);
        //Initializes variables
        Username = (EditText) findViewById(R.id.uUsername);
        Password = (EditText) findViewById(R.id.uPassword);
        Email = (EditText) findViewById(R.id.uEmail);
        Type = (EditText) findViewById(R.id.uType);
        update = (Button) findViewById(R.id.uUpdate);
        OldEmail=(EditText)findViewById(R.id.uOldEmail);
        OldPassword=(EditText)findViewById(R.id.uOldPassword);
        TelephonyManager tMgr = (TelephonyManager)this.getSystemService(Context.TELEPHONY_SERVICE);
        PhoneNo = tMgr.getLine1Number();
        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        //Sets listener for button
        update.setOnClickListener(this);
        //Gets current User
        user=mAuth.getCurrentUser();
        //Puts up current email
        if(user!=null){
                //email address
                String email = user.getEmail();
                OldEmail.setText(email);
        }
        //Initializes listener data
        myUserId = user.getUid();

        postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Post post = dataSnapshot.child("users").child(myUserId).getValue(Post.class);

                String name=post.name;
                String type=post.type;
                Username.setText(name);
                Type.setText(type);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Toast.makeText(UpdateActivity.this, "Couldn't retrieve data", Toast.LENGTH_SHORT).show();
                // ...
            }
        };
        //Attaches Listener to query
        databaseReference.addValueEventListener(postListener);
    }
    /*
    //
    //
    //
    //
     */
    //updates Users profile
    public void updateUserInformation(){
        //Declares variables for storage
        String oldEmail=OldEmail.getText().toString();
        String oldPassword=OldPassword.getText().toString();
        String name = Username.getText().toString();
        String CarType = Type.getText().toString();
        String email = Email.getText().toString();
        String password=Password.getText().toString();
        String phoneNo=PhoneNo;
        //Reauthenticates user for password and email update
        // Get auth credentials from the user for re-authentication. The example below shows
// email and password credentials but there are multiple possible providers,
// such as GoogleAuthProvider or FacebookAuthProvider.
        AuthCredential credential = EmailAuthProvider
                .getCredential(oldEmail, oldPassword);

// Prompt the user to re-provide their sign-in credentials
        user.reauthenticate(credential)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(UpdateActivity.this, "Account Reauthentication Successful",
                                Toast.LENGTH_SHORT).show();
                    }
                });
        //Checks email
        if(TextUtils.isEmpty(email)) {
            //if username is empty do this
            Toast.makeText(UpdateActivity.this, "Please enter email", Toast.LENGTH_SHORT).show();
            //stopping the function execution further
            return;
        }
        //Updates users email
        user.updateEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(UpdateActivity.this, "Email Updated",
                                    Toast.LENGTH_SHORT).show();
                        }
                        else {
                            Toast.makeText(UpdateActivity.this, "There was an error while updating your email",
                                    Toast.LENGTH_SHORT).show();
                            mAuth.signOut();
                            startActivity(new Intent(UpdateActivity.this,MainActivity.class));
                        }
                    }
                });
        //Checks password
        if(password.length() < 6 ){
            //if password is empty
            Toast.makeText(this, "Please enter a password with at least 6 Characters", Toast.LENGTH_SHORT).show();
            //stopping the function execution further
            return;
        }
        //Updates password
        user.updatePassword(password)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(UpdateActivity.this, "Password Updated",
                                    Toast.LENGTH_SHORT).show();
                        }
                        else{
                            Toast.makeText(UpdateActivity.this, "There was an error while updating your password",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        //Initializes user information
        Post post = new Post(name,CarType,phoneNo);
        Map<String, Object> postValues = post.toMap();

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/users/" + user.getUid(), postValues);
        //Updates database with new info
        databaseReference.updateChildren(childUpdates);
        //Tells User operation was successful
        Toast.makeText(UpdateActivity.this, "Information Updated",
                Toast.LENGTH_SHORT).show();

    }
    /*
    //
    //
    //
    //
    */
//Registers user and moves to homescreen when button is clicked
    @Override
    public void onClick(View v) {
        updateUserInformation();
        //detaches listener
        databaseReference.removeEventListener(postListener);
        //goes back to homescreen
        startActivity(new Intent(UpdateActivity.this,HomeScreenActivity.class));
    }

}
