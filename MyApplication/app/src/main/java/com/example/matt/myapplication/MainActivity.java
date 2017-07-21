package com.example.matt.myapplication;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseUser;

import static android.Manifest.permission.READ_PHONE_STATE;
import static android.Manifest.permission.SEND_SMS;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private EditText username;
    private EditText password;
    private TextView registerLink;
    private Button loginLink;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActivityCompat.requestPermissions(this,new String[]{SEND_SMS},1);
        ActivityCompat.requestPermissions(this,new String[]{READ_PHONE_STATE},1);
        //instantiates variables
        mAuth = FirebaseAuth.getInstance();
        username = (EditText) findViewById(R.id.bUsername);
        password = (EditText) findViewById(R.id.bPassword);


        registerLink = (TextView) findViewById(R.id.bRegister);
        loginLink = (Button) findViewById(R.id.bLogin);
        //Sets up listeners for moving to different page
        registerLink.setOnClickListener(this);
        loginLink.setOnClickListener(this);


        if(mAuth.getCurrentUser()!=null) { // means theat the user is already logged in
            //profile activity
            finish();
            startActivity(new Intent(getApplicationContext(),HomeScreenActivity.class));
        }

    }

    public void signIn(){
        String Username = username.getText().toString().trim();
        String Password = password.getText().toString().trim();
        mAuth.signInWithEmailAndPassword(Username, Password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Toast.makeText(MainActivity.this, "Authentication succeeded.",
                                    Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(MainActivity.this,HomeScreenActivity.class));

                        } else {
                            // If sign in fails, display a message to the user.

                            Toast.makeText(MainActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();

                        }

                        // ...
                    }
                });
    }

    @Override
    public void onClick(View v){
        if(v==loginLink){
            signIn();
        }
        else if(v==registerLink){
            startActivity(new Intent(MainActivity.this,RegisterActivity.class));
        }
    }
}
