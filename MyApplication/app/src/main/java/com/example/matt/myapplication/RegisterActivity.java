package com.example.matt.myapplication;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener{
    private FirebaseAuth mAuth;
    private EditText Username;
    private EditText Password;
    private EditText Email;
    private EditText Type;
    private Button register;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        //Initializes variables
        Username = (EditText) findViewById(R.id.rUsername);
        Password = (EditText) findViewById(R.id.rPassword);
        Email = (EditText) findViewById(R.id.rEmail);
        Type = (EditText) findViewById(R.id.rType);
        register = (Button) findViewById(R.id.rRegister);
        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        //Sets listener for button
        register.setOnClickListener(this);
    }

//Registers User
    private void registerUser() {
        String email = Email.getText().toString();
        String password = Password.getText().toString();

        if(TextUtils.isEmpty(email)) {
            //if username is empty do this
            Toast.makeText(this, "Please enter email", Toast.LENGTH_SHORT).show();
            //stopping the function execution further
            return;
        }
        if(password.length() < 6 ){
            //if password is empty
            Toast.makeText(this, "Please enter a password with at least 6 Characters", Toast.LENGTH_SHORT).show();
            //stopping the function execution further
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            //Tells user the operation was successful
                            Toast.makeText(RegisterActivity.this, "Authentication succeeded.",
                                    Toast.LENGTH_SHORT).show();
                            //Stores user information
                            saveUserInformation();
                            //Logs user in
                            startActivity(new Intent(RegisterActivity.this,HomeScreenActivity.class));


                        } else {
                            // If sign in fails, display a message to the user.

                            Toast.makeText(RegisterActivity.this, "Authentication failed. Your email or password was not valid. Change them then try again.",
                                    Toast.LENGTH_SHORT).show();
                        }

                        // ...
                    }
                });
    }

    //adds information to a database
    public void saveUserInformation(){
        //Declares variables for storage
        String name = Username.getText().toString();
        String CarType = Type.getText().toString();

        //Initializes user information
        User userInformation = new User(name, CarType);
        //Gets current User for user Id
        FirebaseUser user = mAuth.getCurrentUser();
        //Stores information
        databaseReference.child("users").child( user.getUid() ).setValue(userInformation);
    }

    //Class for saving user information
    public class User {

        public String name;
        public String type;

        public User() {
            // Default constructor required for calls to DataSnapshot.getValue(User.class)
        }

        public User(String Name, String Type) {
            this.name = Name;
            this.type = Type;
        }

    }

//Registers user and moves to homescreen when button is clicked
    @Override
    public void onClick(View v) {
        registerUser();
    };

}
