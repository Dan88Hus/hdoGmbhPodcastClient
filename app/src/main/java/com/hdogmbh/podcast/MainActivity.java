package com.hdogmbh.podcast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
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
import com.google.android.gms.tasks.Task;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    GoogleSignInClient mGoogleSignInClient;
    private static int RC_SIGN_IN = 100;
    private static int fbRC_code = 200;
    //facebook login variables
    private LoginButton loginButton;
    private CallbackManager callbackManager;
    // SharedPreferences
    SharedPreferences sharedPreferences;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // facebook Login button find
        loginButton = findViewById(R.id.login_button);
        callbackManager = CallbackManager.Factory.create();
        // set permission
        loginButton.setReadPermissions(Arrays.asList("email"));

        //facebook register callBack
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {

            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(@NonNull FacebookException e) {

            }
        });


        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.googleServer_client_id))
                .requestEmail()
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Check for existing Google Sign In account, if the user is already signed in
        // the GoogleSignInAccount will be non-null.
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);

        // Set the dimensions of the sign-in button.
        SignInButton signInButton = findViewById(R.id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_STANDARD);

        //GOOGLE SIGN-IN
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
            }
        });


    }

    //facebook accessTokenTracker method
    AccessTokenTracker tokenTracker = new AccessTokenTracker() {
        @Override
        protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {
            if(currentAccessToken == null){
                Toast.makeText(getApplicationContext(),R.string.fb_log_out_success,Toast.LENGTH_SHORT).show();

            } else{
                loaduserProfile(currentAccessToken);
            }
        }
    };

    // load facebook info write onComplete code to startActivity
    private void loaduserProfile(AccessToken newAccessToken){
        GraphRequest request = GraphRequest.newMeRequest(newAccessToken, new GraphRequest.GraphJSONObjectCallback() {
            @Override
            public void onCompleted(@Nullable JSONObject jsonObject, @Nullable GraphResponse graphResponse) {
                try {
                    String userName = jsonObject.getString("first_name");
                    String userSurname = jsonObject.getString("last_name");
                    String userEmail = jsonObject.getString("email");

//                  send data to CreditCardSuccessActivity to register User
                    Intent dataSuccess = new Intent(MainActivity.this, CreditCardSuccessActivity.class);
                    dataSuccess.putExtra("userName", userName);
                    dataSuccess.putExtra("userSurname", userSurname);
                    dataSuccess.putExtra("userEmail", userEmail);

                    // added - save user Preferences to share data for CreditCardSuccessActivity to register User
                    saveUserPreferences(userName, userSurname, userEmail);

                    // Starting another activity and sending data
                    Intent i = new Intent(MainActivity.this, DemanderActivity.class);
                    i.putExtra("userName", userName);
                    i.putExtra("userSurname", userSurname);
                    i.putExtra("userEmail", userEmail);
                    startActivity(i);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        Bundle parameters = new Bundle();
        parameters.putString("fields","first_name,last_name,email");
        request.setParameters(parameters);
        request.executeAsync();

    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(this);
            if (acct != null) {
                String personName = acct.getDisplayName();
                String userName = acct.getGivenName();
                String userSurname = acct.getFamilyName();
                String userEmail = acct.getEmail();
                String personId = acct.getId();
                Uri personPhoto = acct.getPhotoUrl();

//                save user Preferences to share data for CreditCardSuccessActivity to register User
                saveUserPreferences(userName, userSurname, userEmail);


                // Starting another activity
                Intent i = new Intent(MainActivity.this, DemanderActivity.class);
                startActivity(i);
            }

        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            Log.d("signInResult" , e.toString());
        }
    }

    public void saveUserPreferences(String userName, String userSurname, String userEmail){
        sharedPreferences = getSharedPreferences("userData", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("userName",userName);
        editor.putString("userSurname",userSurname);
        editor.putString("userEmail",userEmail);
        editor.apply();
    }
}