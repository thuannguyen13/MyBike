package com.example.admin.mybike;

import android.content.Context;
import android.content.Intent;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Button;

public class Homescreen extends AppCompatActivity {
    private Button MyBikeMapButton, FacebookLoginButton, VehicleButton, NotificationButton ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homescreen);

        MyBikeMapButton=(Button)findViewById(R.id.MyBikeMapBtn);
        FacebookLoginButton=(Button)findViewById(R.id.FacebookLoginBtn);
        VehicleButton = (Button)findViewById(R.id.BikeManagementBtn);
        NotificationButton = (Button)findViewById(R.id.NotificationBtn);
        final Context Homescreen = this;

        MyBikeMapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent MyBikeMap = new Intent(Homescreen,MapsActivity.class);
                startActivity(MyBikeMap);
            }
        });
        FacebookLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent FacebookLogin = new Intent(Homescreen,Facebook.class);
                startActivity(FacebookLogin);
            }
        });
        VehicleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent BikeManagement = new Intent(Homescreen,Vehicle.class);
                startActivity(BikeManagement);
            }
        });
        NotificationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent Notification = new Intent(Homescreen, com.example.admin.mybike.Notification.class);
                startActivity(Notification);
            }
        });
    }
}
