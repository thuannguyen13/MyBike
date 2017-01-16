package com.example.admin.mybike;


import android.content.Intent;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.facebook.login.widget.ProfilePictureView;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareButton;

import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

public class Facebook extends AppCompatActivity {
    private TextView info, info2;
    private LoginButton loginButton;
    private CallbackManager callbackManager;
    private LoginManager manager;
    ProfileTracker ProfileTrackerFB;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_facebook);
        info = (TextView)findViewById(R.id.txtOutput) ;
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);


        callbackManager = CallbackManager.Factory.create();
        final ShareButton shareButton = (ShareButton) findViewById(R.id.share_button);
        shareButton.setShareContent(content);



//
        //------------------Permission Manager ------------------
        List<String> permissionNeeds = Arrays.asList("publish_actions");
        manager = LoginManager.getInstance();
        manager.logInWithPublishPermissions(this, permissionNeeds);
//        ------------------------------------LOGIN BUTTON-----------------------------------------------------------
        loginButton = (LoginButton)findViewById(R.id.login_button);
        loginButton.setReadPermissions(Arrays.asList("public_profile", "email", "user_birthday", "user_friends"));
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {

            @Override
            public void onSuccess(LoginResult loginResult) {
                GraphRequest request = GraphRequest.newMeRequest(loginResult.getAccessToken(),
                        new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(JSONObject object, GraphResponse response) {
                                Log.v("LoginActivity", response.toString());
                                BacktoHomescreen();
                            }


                        });
                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,name,email,gender,birthday");
                request.setParameters(parameters);
                request.executeAsync();
            }

            @Override
            public void onCancel() {
                Toast.makeText(Facebook.this, "Login Cancelled", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException exception) {
                Toast.makeText(Facebook.this, "Login Error",Toast.LENGTH_SHORT).show();
                Log.v("LoginActivity", exception.getCause().toString());
            }
        });



    }

    private void BacktoHomescreen() {
        Intent Backtohomescreen = new Intent(this, Homescreen.class);
        startActivity(Backtohomescreen);
    }

//    -----------------------------------FACEBOOK SHARE------------------------------------------------------------------

    ShareLinkContent content = new ShareLinkContent.Builder()
            .setContentUrl(Uri.parse("http://SecretSociety.com/"))
            .setContentTitle("MyBike App for Android")
            .setContentDescription("Hello world")
            .build();

    //--------------------------------------------------
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();

    }
}
