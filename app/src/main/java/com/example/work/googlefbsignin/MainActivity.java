package com.example.work.googlefbsignin;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

public class    MainActivity extends AppCompatActivity {
    SignInButton googleSignInBTN;
    GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;
    int RC_SIGN_IN = 1;


    //facebook
    CallbackManager fbcallBackManager;
    Button fb;
    LoginButton loginButton;


    @Override
    protected void onStart() {
        super.onStart();
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        updateUI(account);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_main);

        mAuth=FirebaseAuth.getInstance();
        googleSignInRequest();
        googleSignInBTN = findViewById(R.id.googleBTN);
        googleSignInBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
                GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(MainActivity.this);
                if (acct != null) {
                    Log.e("Acct Value nt null", String.valueOf(acct));
                    String personName = acct.getDisplayName();
                    String personEmail = acct.getEmail();
                    Uri personPhoto = acct.getPhotoUrl();
                    String personPic=String.valueOf(personPhoto);

                    Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                    intent.putExtra("Person name", personName);
                    intent.putExtra("person email", personEmail);
                    intent.putExtra("Person photo", personPic);
                    startActivity(intent);
                    Log.e("person name", personName);
                    Log.e("person email id", personEmail);
                    Log.e("person profile pic", String.valueOf(personPhoto));
                    Toast.makeText(MainActivity.this, "Log in Successfull", Toast.LENGTH_SHORT).show();

                }
            }
        });

        fbcallBackManager = CallbackManager.Factory.create();
        fb = (Button) findViewById(R.id.fb);
        loginButton = (LoginButton) findViewById(R.id.login_button);
        fbUserProfile();
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // fbcallBackManager.onActivityResult(requestCode, resultCode, data);
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.e("TAG", "Google sign in failed", e);
                // ...
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d("TAG", "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        Log.d("CREDENTIAL VALUE", String.valueOf(credential));
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("TAG", "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            //   updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("TAG", "signInWithCredential:failure", task.getException());
                            //Snackbar.make(findViewById(R.id.main_layout), "Authentication Failed.", Snackbar.LENGTH_SHORT).show();
                            updateUI(null);
                        }
                        // ...
                    }
                });
    }

    private void updateUI(GoogleSignInAccount account) {
    }

    private void googleSignInRequest() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }


    //  facebook  begin

    public void perform(View v) {
        if (v == fb) {
            loginButton.performClick();
        }
    }


    public void fbUserProfile() {
        List<String> permissionNeeds = Arrays.asList("user_photos", "email",
                "user_birthday", "public_profile", "AccessToken");
        loginButton.registerCallback(fbcallBackManager, new FacebookCallback<LoginResult>()

        {
            @Override
            public void onSuccess(LoginResult loginResult) {

                Log.e("on sucesssss", "onSuccess");

                String accessToken = loginResult.getAccessToken()
                        .getToken();
                Log.i("accessToken", accessToken);

                GraphRequest request = GraphRequest.newMeRequest(
                        loginResult.getAccessToken(),
                        new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(JSONObject object,
                                                    GraphResponse response) {

                                Log.i("LoginActivity",
                                        response.toString());
                                try {
                                    String id = object.getString("id");
                                    try {
                                        URL profile_pic = new URL(
                                                "http://graph.facebook.com/" + id + "/picture?type=large");
                                        Log.i("profile_pic",
                                                profile_pic + "");

                                    } catch (MalformedURLException e) {
                                        e.printStackTrace();
                                    }

                                    String name = object.getString("name");
                                    String email = object.getString("email");
                                    String gender = object.getString("gender");
                                    String birthday = object.getString("birthday");

                                    Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                                    intent.putExtra("Person name", name);
                                    intent.putExtra("person email", email);
                                    startActivity(intent);

                                    Log.e("User name ", name);
                                    Log.e("User email ", email);
                                    Log.e("User genger ", gender);
                                    Log.e("user birthday", birthday);


                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                Bundle parameters = new Bundle();
                parameters.putString("fields",
                        "id,name,email,gender, birthday");
                request.setParameters(parameters);
                request.executeAsync();
            }

            @Override
            public void onCancel() {
                System.out.println("onCancel");
            }

            @Override
            public void onError(FacebookException exception) {
                System.out.println("onError");
                Log.v("LoginActivity", exception.getCause().toString());
            }
        });
    }
}
