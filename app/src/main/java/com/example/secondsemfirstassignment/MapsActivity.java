package com.example.secondsemfirstassignment;

import android.content.Intent;
import android.content.IntentSender;
import android.inputmethodservice.InputMethodService;
import android.location.Location;
import android.os.Bundle;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FetchPlaceResponse;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.mancj.materialsearchbar.adapter.SuggestionsAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    //This is the object of google Map
    private GoogleMap mMap;
    //this class is for getting user's Current Location
    private FusedLocationProviderClient mFusedLocationProviderClient;
    //this class is for suggestions as user type the location
    private PlacesClient placesClient;
    //array list to Toast the Suggestions
    private List<AutocompletePrediction> predictionList;
    //Storing the last known location of a device
    private Location mLastKnownLocation;

    //Updating the user request if mLastKnownLocation is null
    private LocationCallback locationCallback;

    private MaterialSearchBar materialSearchBar;
    private View mapView;
    private Button btnFind;
    private final  float DEFAULT_ZOOM=18;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        materialSearchBar=findViewById(R.id.searchBar);
        btnFind=findViewById(R.id.btn_find);
        //To find and load the fragment so we can used it further
        SupportMapFragment mapFragment=(SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mapView=mapFragment.getView();
        mFusedLocationProviderClient=LocationServices.getFusedLocationProviderClient(MapsActivity.this);
        Places.initialize(MapsActivity.this,"AIzaSyBoZ8uCqKp7NeC0WoJik1kEXrVXWzJxYzU");
        placesClient=Places.createClient(this);
        final AutocompleteSessionToken token=AutocompleteSessionToken.newInstance();
        materialSearchBar.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {
            @Override
            public void onSearchStateChanged(boolean enabled) {

            }

            @Override
            public void onSearchConfirmed(CharSequence text) {

                startSearch(text.toString(),true,null,true);


            }

            @Override
            public void onButtonClicked(int buttonCode) {
                if(buttonCode==materialSearchBar.BUTTON_NAVIGATION)
                {

                }
                else if(buttonCode==materialSearchBar.BUTTON_BACK)
                {
                    materialSearchBar.disableSearch();
                }
//                else if(buttonCode==materialSearchBar.BUTTON_SPEECH)
//                {
//
//                }

            }
        });
        materialSearchBar.addTextChangeListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                final FindAutocompletePredictionsRequest predictionsRequest=FindAutocompletePredictionsRequest.builder().setCountry("ca").setTypeFilter(TypeFilter.ADDRESS).setSessionToken(token).setQuery(charSequence.toString()).build();

                placesClient.findAutocompletePredictions(predictionsRequest).addOnCompleteListener(new OnCompleteListener<FindAutocompletePredictionsResponse>() {
                    @Override
                    public void onComplete(@NonNull Task<FindAutocompletePredictionsResponse> task) {
                        if(task.isSuccessful())
                        {
                            FindAutocompletePredictionsResponse predictionsResponse=task.getResult();
                            if(predictionsResponse!=null)
                            {
                                predictionList=predictionsResponse.getAutocompletePredictions();

                                //convert prediction list into string
                                List<String>suggestionList=new ArrayList<>();
                                for(int i=0;i<predictionList.size();i++)
                                {
                                    AutocompletePrediction prediction=predictionList.get(i);
                                    suggestionList.add(prediction.getFullText(null).toString());
                                }

                                materialSearchBar.updateLastSuggestions(suggestionList);
                                if(materialSearchBar.isSuggestionsVisible())
                                {
                                    materialSearchBar.showSuggestionsList();
                                }
                            }
                        }
                        else {
                            Log.i("mytag","Prediction fetching task is unsucessful");
                        }
                    }
                });
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        materialSearchBar.setSuggestionsClickListener(new SuggestionsAdapter.OnItemViewClickListener() {
            @Override
            public void OnItemClickListener(int position, View v) {
                if(position>=predictionList.size())
                {
                    return;
                }
                AutocompletePrediction selectedprediction=predictionList.get(position);
                String suggestion=materialSearchBar.getLastSuggestions().get(position).toString();
                materialSearchBar.setText(suggestion);
                materialSearchBar.clearSuggestions();
                //to close the keyboard on phone

                InputMethodManager inputMethodManager=(InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                if(inputMethodManager!=null)
                {
                    inputMethodManager.hideSoftInputFromWindow(materialSearchBar.getWindowToken(),InputMethodManager.HIDE_IMPLICIT_ONLY);

                    //Pass this id to google to get Lat and LOng
                    String placeId=selectedprediction.getPlaceId();
                    List<Place.Field> placefield= Arrays.asList(Place.Field.LAT_LNG);
                    final FetchPlaceRequest fetchPlaceRequest=FetchPlaceRequest.builder(placeId,placefield).build();
                    placesClient.fetchPlace(fetchPlaceRequest).addOnSuccessListener(new OnSuccessListener<FetchPlaceResponse>() {
                        @Override
                        public void onSuccess(FetchPlaceResponse fetchPlaceResponse) {

                            Place place=fetchPlaceResponse.getPlace();
                            Log.i("mytag","places found: "+place.getName());
                            LatLng latLngPlace=place.getLatLng();
                            if(latLngPlace!=null)
                            {
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLngPlace,DEFAULT_ZOOM));
                            }

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            if(e instanceof ApiException)
                            {
                                ApiException apiException=(ApiException) e;
                                int statusCode=apiException.getStatusCode();

                                Log.i("mytag","Places not found: " +e.getMessage());
                                Log.i("mytag","Status Code: "+statusCode);
                            }

                        }
                    });




                }


            }

            @Override
            public void OnItemDeleteListener(int position, View v) {

            }
        });


    }

    //This callback function is called when map is ready and loaded and making some UI changes in layout

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap=googleMap;
        mMap.setMyLocationEnabled(true);

        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        if(mapView!=null && mapView.findViewById(Integer.parseInt("1")) !=null)
        {
          View locationButton=((View)mapView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
          //fetching the layour params for Location Button
          RelativeLayout.LayoutParams layoutParams=(RelativeLayout.LayoutParams)locationButton.getLayoutParams();

          //adding rule so location button is no longer at the Top of Screen
          layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP,0);
            //adding rule so location button is  at the botton of Screen
          layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM,RelativeLayout.TRUE);

          //bottom margin is large because of button
          layoutParams.setMargins(0,0,40,180);


        }
        //Check if Gps is enabled on user's Device or not  in Order to fetch User Location and then request user to enable it
        LocationRequest locationRequest=LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationSettingsRequest.Builder builder=new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        SettingsClient settingsClient= LocationServices.getSettingsClient(MapsActivity.this);

        //this will check all the Gps Settings permissions
        Task<LocationSettingsResponse>task=settingsClient.checkLocationSettings(builder.build());

        task.addOnSuccessListener(MapsActivity.this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {

                getDeviceLocation();
            }
        });
        task.addOnFailureListener(MapsActivity.this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                if(e instanceof ResolvableApiException)
                {
                    ResolvableApiException resolvableApiException=(ResolvableApiException) e;
                    try {
                        resolvableApiException.startResolutionForResult(MapsActivity.this,51);
                    } catch (IntentSender.SendIntentException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });





    }

    //check if user denied or allowd the location Access

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==51)
        {
            if(resultCode==RESULT_OK)
            {
                getDeviceLocation();
            }
        }
    }


    //we asked to mFusedLocationProvider for the last location of user's Device.
    private void getDeviceLocation() {

        mFusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {

                if(task.isSuccessful())
                {
                    mLastKnownLocation=task.getResult();
                    if(mLastKnownLocation!=null)
                    {
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLastKnownLocation.getLatitude(),mLastKnownLocation.getLongitude()),DEFAULT_ZOOM));

                    }
                    else

                    {
                        //request for updated location
                        final LocationRequest locationRequest=LocationRequest.create();
                        locationRequest.setInterval(10000);
                        locationRequest.setFastestInterval(5000);
                        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

                        //locationCallback  function is executed when updated location is Received
                    locationCallback=new LocationCallback(){
                        @Override
                        public void onLocationResult(LocationResult locationResult) {
                            super.onLocationResult(locationResult);

                            if(locationResult==null)
                            {
                                return;
                            }
                            mLastKnownLocation=locationResult.getLastLocation();
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLastKnownLocation.getLatitude(),mLastKnownLocation.getLongitude()),DEFAULT_ZOOM));
                            mFusedLocationProviderClient.removeLocationUpdates(locationCallback);
                        }
                    };
                    mFusedLocationProviderClient.requestLocationUpdates(locationRequest,locationCallback,null);
                    }

                }
                else {
                    Toast.makeText(MapsActivity.this, "unable to get last location", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }
}
