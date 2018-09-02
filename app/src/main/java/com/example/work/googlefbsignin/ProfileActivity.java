package com.example.work.googlefbsignin;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

public class ProfileActivity extends AppCompatActivity {

    ImageView userImage;
    EditText userName, userEmail;
    Button logOut;
    FirebaseAuth mAuth;
    FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Bundle bundle = getIntent().getExtras();
        String personName = bundle.getString("Person name");
        String personEmail = bundle.getString("person email");
        String personPic = bundle.getString("Person photo");
        Log.e("person name P.A", personName);
        Log.e("person email id P.A", personEmail);
       // Log.e("person profile pic P.A",personPic);


        userImage = (ImageView) findViewById(R.id.profile_Pic);
        userName = (EditText) findViewById(R.id.user_Name);
        userEmail = (EditText) findViewById(R.id.email_ID);
        logOut = (Button) findViewById(R.id.log_Out);
        mAuth=FirebaseAuth.getInstance();



        userName.setText(personName);
        userEmail.setText(personEmail);
        Picasso.with(getApplicationContext()).load(personPic).into(userImage);


        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                Log.e("Current user", String.valueOf(firebaseAuth.getCurrentUser()));
                if (firebaseAuth.getCurrentUser() == null) {
                  //  Toast.makeText(ProfileActivity.this, ""+firebaseAuth.getCurrentUser(), Toast.LENGTH_SHORT).show();
                   startActivity(new Intent(ProfileActivity.this,MainActivity.class));
                }
            }
        };
        logOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               mAuth.signOut();
                // FirebaseAuth.getInstance().signOut();
                Toast.makeText(ProfileActivity.this, "Log Out Successfull", Toast.LENGTH_SHORT).show();

            }
        });
    }
}
