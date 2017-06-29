package com.example.matt.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class UpdateActivity extends AppCompatActivity implements View.OnClickListener{
    private FirebaseAuth mAuth;
    private EditText Username;
    private EditText Password;
    private EditText Email;
    private EditText Type;
    private Button update;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference databaseReference;
    private FirebaseUser user;

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
                Email.setText(email);
        }
        //Puts up current profile details

        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI
                Post post = dataSnapshot.getValue(Post.class);
                String name = post.getName();
                String type = post.getType();
                Username.setText(name);
                Type.setText(type);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                // ...
            }
        };
        databaseReference.addListenerForSingleValueEvent(postListener);

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
        String name = Username.getText().toString();
        String CarType = Type.getText().toString();
        String email = Email.getText().toString();
        String password=Password.getText().toString();

        //Checks email
        if(TextUtils.isEmpty(email)) {
            //if username is empty do this
            Toast.makeText(this, "Please enter email", Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(UpdateActivity.this, "Credential too old. Login again. Then try to update profile",
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
                            Toast.makeText(UpdateActivity.this, "Credential too old. Login again. Then try to update profile",
                                    Toast.LENGTH_SHORT).show();
                            mAuth.signOut();
                            startActivity(new Intent(UpdateActivity.this,MainActivity.class));
                        }
                    }
                });

        //Initializes user information
        Post post = new Post(name,CarType);
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
    //Class for updating user information
    @IgnoreExtraProperties
    public class Post {

        public String name;
        public String type;

        // Default constructor required for calls to DataSnapshot.getValue(Post.class)
        public Post(){
            this.name=null;
            this.type=null;
        }

        public Post(String Name, String Type) {
            this.name = Name;
            this.type = Type;
        }
        //Get methods
        public String getName(){
            return name;
        }
        public String getType(){
            return type;
        }

        @Exclude
        public Map<String, Object> toMap() {
            HashMap<String, Object> result = new HashMap<>();
            result.put("name", name);
            result.put("type", type);
            return result;
        }

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
        startActivity(new Intent(UpdateActivity.this,HomeScreenActivity.class));
    };

}
