package com.example.admin.mybike;

import android.Manifest;
import android.app.ActionBar;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.multidex.MultiDex;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.admin.mybike.Modules.DirectionFinder;
import com.example.admin.mybike.Modules.DirectionFinderListener;
import com.example.admin.mybike.Modules.PlaceJSONParser;
import com.example.admin.mybike.Modules.Route;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener, DirectionFinderListener, GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {

    private static final String TAG = "MainActivity";
    double mLatitude = 0;
    double mLongitude = 0;

    private LocationRequest mLocationRequest;
    private String mLastUpdateTime;
    public GoogleMap mMap;
    EditText MarkerNameTxt, MarkerDesTxt;
    Button SaveMarkerBtn, ClearMarketBtn, LoadMarkerBtn, DeleteMarkerBtn;
    private TextView etOrigin,etDestination,etAddress;
    private List<Marker> originMarkers = new ArrayList<>();
    private List<Marker> destinationMarkers = new ArrayList<>();
    private List<Polyline> polylinePaths = new ArrayList<>();
    private ProgressDialog progressDialog;
    private GoogleApiClient mGoogleApiClient;
    int locationCount = 0;
    final Context context = this;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        etDestination = (TextView) findViewById(R.id.etDestination);
        etOrigin = (TextView) findViewById(R.id.etOrigin);
        etAddress = (TextView) findViewById(R.id.etAddress);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addApi(AppIndex.API)
                .build();
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);// Creating a criteria object to retrieve provider
        Criteria criteria = new Criteria();// Getting the name of the best provider
        String provider = locationManager.getBestProvider(criteria, true);// Getting Current Location From GPS
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        final Location location = locationManager.getLastKnownLocation(provider);
        if (location != null) {
            onLocationChanged(location);
            mLatitude = location.getLatitude();
            mLongitude = location.getLongitude();
        }

    }
    /**===================================== MENU NAVIGATION ======================================*/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main_action, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) { switch(item.getItemId()) {
        case R.id.marker_manager:
            final Dialog dialog = new Dialog(context);
            dialog.setContentView(R.layout.marker_popup);
            MarkerNameTxt = (EditText)dialog.findViewById(R.id.MarkerNameTxt);
            MarkerDesTxt = (EditText)dialog.findViewById(R.id.MarkerDesTxt);
            DeleteMarkerBtn = (Button)dialog.findViewById(R.id.DeleteMarkerBtn);
            SaveMarkerBtn = (Button)dialog.findViewById(R.id.SaveMarkerBtn);
            ClearMarketBtn = (Button)dialog.findViewById(R.id.ClearMarkerBtn);
            LoadMarkerBtn =(Button)dialog.findViewById(R.id.LoadMarkerBtn);
            LoadMarkerBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SharedPreferences shareMarkers = getSharedPreferences("OfflineMakers", Context.MODE_PRIVATE);
                    // Getting number of locations already stored
                    locationCount = shareMarkers.getInt("locationCount", 0);
                    String[] location_name = new String[locationCount];
                    mMap.clear();
                    if (locationCount != 0) {
                        String lat = "";
                        String lng = "";
                        String title_name = "";
                        String maker_des = "";
                        // Iterating through all the locations stored
                        for (int i = 0; i < locationCount; i++) {
                            // Getting the latitude of the i-th location
                            lat = shareMarkers.getString("lat" + i, "0");
                            title_name = shareMarkers.getString("title" +i, "");
                            maker_des = shareMarkers.getString("des" +i, "");
                            // Getting the longitude of the i-th location
                            lng = shareMarkers.getString("lng" + i, "0");
                            location_name[i] = title_name;
                            location_name[i] = maker_des;
                            // Drawing marker on the map
                            drawMarker(new LatLng(Double.parseDouble(lat), Double.parseDouble(lng)), title_name, maker_des);
                        }
                    }
                    dialog.dismiss();
                }
            });
            ClearMarketBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mMap.clear();
                    SharedPreferences shareMarkers = getSharedPreferences("OfflineMakers", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = shareMarkers.edit();// Opening the editor object to delete data from sharedPreferences
                    editor.clear();// Clearing the editor
                    editor.commit();// Committing the changes
                    locationCount=0;// Setting locationCount to zero
                    dialog.dismiss();
                }
            });
            SaveMarkerBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LatLng curLatLng = new LatLng(mLatitude, mLongitude);
                    String MarkerTitle = MarkerNameTxt.getText().toString();
                    String MarkerDes = MarkerDesTxt.getText().toString();
                    SaveMarker(curLatLng, MarkerTitle, MarkerDes);
                    dialog.dismiss();
                }
            });
            DeleteMarkerBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DeleteMarker();
                    dialog.dismiss();
                }
            });
            dialog.show();
            return true;
        case R.id.nearby_location:
            StringBuilder sb = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
            sb.append("location=" + mLatitude + "," + mLongitude);
            sb.append("&radius=50000");
            sb.append("&type=store");
            sb.append("&keyword=motorcycle");
            sb.append("&key=AIzaSyC8VsN6ex1GKhtr3_T4mYHiZiP-itb9J-k");
            // Creating a new non-ui thread task to download Google place json data
            PlacesTask placesTask = new PlacesTask();
            // Invokes the "doInBackground()" method of the class PlaceTask
            placesTask.execute(sb.toString());
            return true;
    }
        return(super.onOptionsItemSelected(item));
    }
    /**===================================== DIRECTION ============================================*/
    private void sendRequest() {
        String origin = mLatitude+","+mLongitude;
        String destination = etDestination.getText().toString();
        SelectMarker();
        if (origin.isEmpty()) {
            if (destination.isEmpty()) {
                Toast.makeText(this, "Please enter origin address!", Toast.LENGTH_SHORT).show();
            }
            return;
        }
        if (destination.isEmpty()) {
            Toast.makeText(this, "Please enter destination address!", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            new DirectionFinder((DirectionFinderListener) this, origin, destination).execute();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
    public void onDirectionFinderStart() {
        progressDialog = ProgressDialog.show(this, "Please wait.", "Finding direction..!", true);

        if (originMarkers != null) {
            for (Marker marker : originMarkers) {
                marker.remove();
            }
        }

        if (destinationMarkers != null) {
            for (Marker marker : destinationMarkers) {
                marker.remove();
            }
        }

        if (polylinePaths != null) {
            for (Polyline polyline : polylinePaths) {
                polyline.remove();
            }
        }
    }
    public void onDirectionFinderSuccess(List<Route> routes) {
        progressDialog.dismiss();
        polylinePaths = new ArrayList<>();
        originMarkers = new ArrayList<>();
        destinationMarkers = new ArrayList<>();

        for (Route route : routes) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(route.startLocation, 16));
            ((TextView) findViewById(R.id.tvDuration)).setText(route.duration.text);
            ((TextView) findViewById(R.id.tvDistance)).setText(route.distance.text);
            originMarkers.add(mMap.addMarker(new MarkerOptions()

                    .title(route.startAddress)
                    .position(route.startLocation)));
            destinationMarkers.add(mMap.addMarker(new MarkerOptions()

                    .title(route.endAddress)
                    .position(route.endLocation)));

            PolylineOptions polylineOptions = new PolylineOptions().
                    geodesic(true).
                    color(Color.RED).
                    width(20);
            for (int i = 0; i < route.points.size(); i++)
                polylineOptions.add(route.points.get(i));

            polylinePaths.add(mMap.addPolyline(polylineOptions));
        }

    }
    /**===================================== MARKERS ==============================================*/
    public void SaveMarker(LatLng point, String MarkerTitle, String MarkerDes) {
        if(MarkerNameTxt.getText().toString().isEmpty() || MarkerDesTxt.getText().toString().isEmpty()){
            Toast.makeText(getBaseContext(), "Please Input Marker Data", Toast.LENGTH_SHORT).show();
            return;
        }else{
            SharedPreferences shareMarkers = getSharedPreferences("OfflineMakers", Context.MODE_PRIVATE);
            locationCount++;
            drawMarker(point,MarkerTitle,MarkerDes);
            SharedPreferences.Editor editor = shareMarkers.edit();/** Opening the editor object to write data to sharedPreferences */
            editor.putString("lat"+ Integer.toString((locationCount-1)), Double.toString(point.latitude));// Storing the latitude for the i-th location
            editor.putString("lng"+ Integer.toString((locationCount-1)), Double.toString(point.longitude));// Storing the longitude for the i-th location
            editor.putString("title"+ Integer.toString((locationCount-1)), MarkerTitle.toString());// Storing the longitude for the i-th location
            editor.putString("des"+ Integer.toString((locationCount-1)), MarkerDes.toString());// Storing the longitude for the i-th location
            editor.putInt("locationCount", locationCount);// Storing the count of locations or marker count
            editor.commit();/** Saving the values stored in the shared preferences */
            Toast.makeText(getBaseContext(), "Marker is added to the Map", Toast.LENGTH_SHORT).show();
        }
    }
    public void drawMarker(LatLng point,String MarkerTitle, String MarkerDes){
        mMap.addMarker(new MarkerOptions().position(point).title(MarkerTitle).snippet(MarkerDes));
    }
    public void SelectMarker(){
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                LatLng currentmarker = marker.getPosition();
                double MarkerLat = currentmarker.latitude;
                double MarkerLng = currentmarker.longitude;
                String MarkerAdd = marker.getSnippet();
                String MarkerName = marker.getTitle();
                etDestination.setText(MarkerLat+","+MarkerLng);
                etOrigin.setText(MarkerName);
                etAddress.setText(MarkerAdd);
                return false;
            }
        });
        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                SelectMarker();
                sendRequest();
            }
        });
    }
    public void DisplayMarker(){
        SharedPreferences shareMarkers = getSharedPreferences("OfflineMakers", Context.MODE_PRIVATE);
        // Getting number of locations already stored
        locationCount = shareMarkers.getInt("locationCount", 0);
        String[] location_name = new String[locationCount];
        mMap.clear();
        if (locationCount != 0) {
            String lat = "";
            String lng = "";
            String title_name = "";
            String maker_des = "";
            // Iterating through all the locations stored
            for (int i = 0; i < locationCount; i++) {
                // Getting the latitude of the i-th location
                lat = shareMarkers.getString("lat" + i, "0");
                title_name = shareMarkers.getString("title" +i, "");
                maker_des = shareMarkers.getString("des" +i, "");
                // Getting the longitude of the i-th location
                lng = shareMarkers.getString("lng" + i, "0");
                location_name[i] = title_name;
                location_name[i] = maker_des;
                // Drawing marker on the map
                drawMarker(new LatLng(Double.parseDouble(lat), Double.parseDouble(lng)), title_name, maker_des);
            }
        }
    }
    public void DeleteMarker(){
        SharedPreferences shareMarkers = getSharedPreferences("OfflineMakers", Context.MODE_PRIVATE);
        locationCount--;
        SharedPreferences.Editor editor = shareMarkers.edit();/** Opening the editor object to write data to sharedPreferences */
        editor.remove("lat"+ Integer.toString((locationCount+1)));// Delete the latitude for the i-th location
        editor.remove("lng"+ Integer.toString((locationCount+1)));// Delete the longitude for the i-th location
        editor.remove("title"+ Integer.toString((locationCount+1)));// Delete the longitude for the i-th location
        editor.remove("des"+ Integer.toString((locationCount+1)));// Delete the longitude for the i-th location
        editor.putInt("locationCount", locationCount);// stored the current count of locations or marker count
        editor.commit();/** Saving the values stored in the shared preferences */
        DisplayMarker();
        Toast.makeText(getBaseContext(), "Marker is deleted to the Map", Toast.LENGTH_SHORT).show();
    }
    /**===================================== NEARBY PLACES ========================================*/
    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);
            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();
            // Connecting to url
            urlConnection.connect();
            // Reading data from url
            iStream = urlConnection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
            StringBuffer sb = new StringBuffer();
            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            data = sb.toString();
            br.close();
        } catch (Exception e) {
            Log.d("Ex downloading url", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }/** A method to download json data from url*/
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("Maps Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    } /** ATTENTION: This was auto-generated to implement the App Indexing API. See https://g.co/AppIndexing/AndroidStudio for more information.*/
    private class PlacesTask extends AsyncTask<String, Integer, String> {
        String data = null;

        // Invoked by execute() method of this object
        @Override
        protected String doInBackground(String... url) {
            try {
                data = downloadUrl(url[0]);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        // Executed after the complete execution of doInBackground() method
        @Override
        protected void onPostExecute(String result) {
            ParserTask parserTask = new ParserTask();

            // Start parsing the Google places in JSON format
            // Invokes the "doInBackground()" method of the class ParseTask
            parserTask.execute(result);
        }

    }/**A class, to download Google Places*/
    private class ParserTask extends AsyncTask<String, Integer, List<HashMap<String, String>>> {

        JSONObject jObject;

        // Invoked by execute() method of this object
        @Override
        protected List<HashMap<String, String>> doInBackground(String... jsonData) {

            List<HashMap<String, String>> places = null;
            PlaceJSONParser placeJsonParser = new PlaceJSONParser();

            try {
                jObject = new JSONObject(jsonData[0]);

                /** Getting the parsed data as a List construct */
                places = placeJsonParser.parse(jObject);

            } catch (Exception e) {
                Log.d("Exception", e.toString());
            }
            return places;
        }

        // Executed after the complete execution of doInBackground() method
        @Override
        protected void onPostExecute(List<HashMap<String, String>> list) {

            // Clears all the existing markers
            mMap.clear();
            for (int i = 0; i < list.size(); i++) {

                // Creating a marker
                MarkerOptions markerOptions = new MarkerOptions();

                // Getting a place from the places list
                HashMap<String, String> hmPlace = list.get(i);

                // Getting latitude of the place
                double lat = Double.parseDouble(hmPlace.get("lat"));

                // Getting longitude of the place
                double lng = Double.parseDouble(hmPlace.get("lng"));

                // Getting name
                String name = hmPlace.get("place_name");

                // Getting vicinity
                String vicinity = hmPlace.get("vicinity");

                LatLng latLng = new LatLng(lat, lng);

                // Setting the position for the marker
                markerOptions.position(latLng);

                // Setting the title for the marker.
                //This will be displayed on taping the marker
                markerOptions.title(name);
                markerOptions.snippet(vicinity);
                // Placing a marker on the touched position
                mMap.addMarker(markerOptions);

            }

        }


    } /** A class to parse the Google Places in JSON format*/
    /**============================================================================================*/
    @Override
    public void onMapReady(GoogleMap googleMap) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.setPadding(0,280,0,0);
        /**================LOAD MARKERS===================*/
        DisplayMarker();
        SelectMarker();
    }

    @Override
    public void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.start(mGoogleApiClient, getIndexApiAction());
    }
    @Override
    protected void onStop() {
        super.onStop();// ATTENTION: This was auto-generated to implement the App Indexing API.
// See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(mGoogleApiClient, getIndexApiAction());
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }
    @Override
    public void onConnected(Bundle bundle) {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(5000);
        mLocationRequest.setFastestInterval(3000);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }
    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Connection Suspended");
        mGoogleApiClient.connect();
    }
    @Override
    public void onLocationChanged(Location location) {
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        mLatitude = (location.getLatitude());
        mLongitude = (location.getLongitude());
        Toast.makeText(this, "Updated: " + mLastUpdateTime, Toast.LENGTH_SHORT).show();
    }
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(TAG, "Connection failed. Error: " + connectionResult.getErrorCode());
    }

}
