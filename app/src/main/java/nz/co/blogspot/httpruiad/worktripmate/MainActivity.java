package nz.co.blogspot.httpruiad.worktripmate;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;


public class MainActivity extends AppCompatActivity implements WeatherFragment.OnFragmentInteractionListener, OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    private static final String LOG_TAG = "MainActivity";
    protected GoogleApiClient mGoogleApiClient;
    protected Location mLastLocation;
    private boolean isConnected = false;
    private GoogleMap mMap;


    protected LocationRequest mLocationRequest;
    private String type = "locality";

    private static final int REQUEST_CODE = 1;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {

            switch (item.getItemId()) {
                case R.id.navigation_map: {
                    Log.d(LOG_TAG, "Navigation to map");
                    MapFragment mMapFragment = MapFragment.newInstance();
                    mMapFragment.getMapAsync(new OnMapReadyCallback() {
                        @Override
                        public void onMapReady(GoogleMap googleMap) {
                            Log.d(LOG_TAG, "Navigation_map onMapReady");
                            mMap = googleMap;
                            if(mLastLocation!=null){
                                   LatLng latLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                                    mMap.clear();

                                    mMap.addMarker(new MarkerOptions().position(latLng)
                                            .title("Current position"));
                                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12));


                                }
                        }
                    });
                    android.app.FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();

                    fragmentTransaction.add(R.id.map, mMapFragment);
                    fragmentTransaction.commit();

                    return true;
                }
                case R.id.navigation_weather: {
                    WeatherFragment weatherFragment = new WeatherFragment();

                    MapFragment mMapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
                    if (mMapFragment != null) {

                        Log.d(LOG_TAG, "mMap no null");
                        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

                        transaction.replace(R.id.map, weatherFragment);


                        transaction.commit();
                    }


                    return true;
                }
                case R.id.navigation_setting: {
                    Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
                    startActivity(intent);
                    return true;
                }
            }
            return false;
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ConnectivityManager cm =
                (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        if (isConnected == true) {
            Log.d(LOG_TAG, "is connected");


            buildGoogleClient();
        }


        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        Log.d("deubg", "--------onsaveinstancestate--------");
        if(mLastLocation!=null) {
            outState.putDouble("CurrentLat", mLastLocation.getLatitude());
            outState.putDouble("CurrentLng", mLastLocation.getLongitude());
        }
        super.onSaveInstanceState(outState, outPersistentState);
    }

    protected synchronized void buildGoogleClient() {
        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setTrafficEnabled(true);
    }


    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient != null)
            mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        Log.d(LOG_TAG, "onConnected");

        //BottomNavigationView bottomNavigationView;
        //bottomNavigationView = (BottomNavigationView) findViewById(R.id.navigation);
        //bottomNavigationView.setSelectedItemId(R.id.navigation_map);

        if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED | ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Log.d(LOG_TAG, "permission check");


            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this, new String[]{
                                android.Manifest.permission.ACCESS_FINE_LOCATION,
                                android.Manifest.permission.ACCESS_COARSE_LOCATION},
                        REQUEST_CODE);


                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
            if (LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient) != null) {
                mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                LatLng latLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                mMap.clear();

                mMap.addMarker(new MarkerOptions().position(latLng)
                        .title("Current position"));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12));


            } else {
                startLocationUpdates();


            }

            return;
        }
        else {
            Log.d(LOG_TAG,"on Connection, already has permission");

            if (LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient) != null) {

                mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                LatLng latLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                mMap.clear();

                mMap.addMarker(new MarkerOptions().position(latLng)
                        .title("Current position"));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12));


                Log.d(LOG_TAG, "last known location is not empty");


            } else {
                Log.d(LOG_TAG, "last known location is empty");
                startLocationUpdates();

            }
            return;
        }
    }
    protected void startLocationUpdates() {
        Log.d(LOG_TAG, "startLocationUpdates");


        if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
        mLastLocation= LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if(mLastLocation!=null) {
            mMap.clear();
            LatLng latLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());

            mMap.addMarker(new MarkerOptions().position(latLng)
                    .title("Current position"));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12));

        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        startLocationUpdates();

    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
