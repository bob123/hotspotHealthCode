package com.hotspothealthcode.hotspothealthcode;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.animation.AnimatorCompatHelper;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.AutocompletePredictionBuffer;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.hotspothealthcode.hotspothealthcode.FileActivities.FileLoaderActivity;

import hotspothealthcode.controllers.Controller;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener  {

    private GoogleMap mMap;
    AutoCompleteTextView txtSearchBox;
    private GoogleApiClient mGoogleApiClient;
    PlaceAutocompleteFragment autocompleteFragment;
    int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;
    AutocompleteFilter typeFilter;
    PendingResult<AutocompletePredictionBuffer> result;
    Bundle extras;
    Intent NextIntent;
    LatLng SelectedLocation;

    int PLACE_PICKER_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //txtSearchBox = (AutoCompleteTextView) findViewById(R.id.txtSearchPlace);

        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .build();

        autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {

            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                mMap.clear();
                SelectedLocation = place.getLatLng();
                mMap.addMarker(new MarkerOptions().position(SelectedLocation).title(place.getName().toString()));
                //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(SelectedLocation, 15));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(SelectedLocation, 15));
                //Log.i("a", "Place: " + place.getName());
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                //Log.i("a", "An error occurred: " + status);
            }
        });

        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();

//        try {
//            startActivityForResult(builder.build(this), PLACE_PICKER_REQUEST);
//        } catch (GooglePlayServicesRepairableException e) {
//            e.printStackTrace();
//        } catch (GooglePlayServicesNotAvailableException e) {
//            e.printStackTrace();
//        }



        Toolbar toolbar = (Toolbar) findViewById(R.id.maps_toolbar);
        toolbar.setTitle("Choose location on map");
        setSupportActionBar(toolbar);




        extras = getIntent().getExtras();
        if (extras != null) {
            NextIntent = (Intent)extras.get("NextIntent");
        }
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                mMap.clear();

                Place place = PlacePicker.getPlace(data, this);
                SelectedLocation = place.getLatLng();
                mMap.addMarker(new MarkerOptions().position(SelectedLocation).title(place.getName().toString()));
                //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(SelectedLocation, 15));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(SelectedLocation, 15));
                //Log.i("a", "Place: " + place.getName());
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if ( ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {

        }

        mMap.setMyLocationEnabled(true);
        Object a = mMap.getMyLocation();

        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {

            @Override
            public void onMapLongClick(LatLng latLng) {

                mMap.clear();

                mMap.addMarker(new MarkerOptions()
                        .position(latLng)
                        .anchor((float) 0.5, (float) 0.5));

                SelectedLocation = latLng;
            }
        });
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        txtSearchBox.setText("NO CONNECTION!");
        txtSearchBox.setEnabled(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.map_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        if (SelectedLocation != null) {
            Intent mapsActivity = new Intent(getApplicationContext(), MapsActivity.class);

            Controller.init(SelectedLocation);

            startActivity(NextIntent);
        }
        else{
            Toast.makeText(this, "Please choose a location", Toast.LENGTH_SHORT).show();
        }

//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.actionLoadResult) {
//
//            Intent FileLoaderIntent = new Intent(getApplicationContext(), FileLoaderActivity.class);
//
//            startActivity(FileLoaderIntent);
//
//            return true;
//        }

        return true;
        //return super.onOptionsItemSelected(item);
    }

}
