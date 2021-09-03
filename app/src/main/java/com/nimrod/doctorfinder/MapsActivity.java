package com.nimrod.doctorfinder;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.SearchView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener, LocationListener {
    Toolbar toolbar;
    private double latitude = 0.00;
    private double longitude = 0.00;
    private GoogleMap googleMap;
    private Marker doctorMarker;
    SearchView searchView;
    String doctor_id;
    JSONArray jsonArray;
    HashMap<Object, String> mMarkerMap;
    Location mLastLocation;
    GoogleApiClient googleApiClient;
    Marker mCurrentMarker;
    LocationRequest mLocationRequest;
    boolean doubleBack = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
//        toolbar = findViewById(R.id.toolbar);
//        toolbar.setTitle("Nearby Doctors");
        setSupportActionBar(toolbar);
        searchView = findViewById(R.id.searchDoctors);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
//        mapFragment.getMapAsync(this);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                String loc = searchView.getQuery().toString();
                System.out.println("You searched " + loc);
                List<Address> addressList = null;

                if (loc != null || !loc.equals("")) {

                    Geocoder geocoder = new Geocoder(MapsActivity.this);
                    try {
                        addressList = geocoder.getFromLocationName(loc, 1);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (addressList.isEmpty()) {
                        Toast.makeText(MapsActivity.this, "Please check your location", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Address address = addressList.get(0);
                        LatLng latlong = new LatLng(address.getLatitude(), address.getLongitude());
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latlong, 10));
                    }

                }


                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        mapFragment.getMapAsync(this);
    }


    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;
        LatLng nairobi = new LatLng(-1.3015458, 36.7923256);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(nairobi, 10));
//        googleMap.setOnMarkerClickListener(this);
        new fetchDoctorsLocation().execute();

        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                if (marker.equals(doctorMarker)) {
//                    for ()
                    String doctor_id = marker.getSnippet();
                    System.out.println("Snippet id " + doctor_id);
                    String profile_id = mMarkerMap.get(marker.getTag());
                    Intent intent = new Intent(MapsActivity.this, DoctorDetail.class);
                    intent.putExtra("doctor_id", doctor_id);
                    intent.putExtra("profile_id", profile_id);
                    startActivity(intent);
                    Toast.makeText(MapsActivity.this, "Doctor id " + doctor_id, Toast.LENGTH_SHORT).show();

                }
                return false;
            }
        });


    }

    private static double roundMyData(double Rval, int numberOfDigitsAfterDecimal) {
        double p = (float) Math.pow(10, numberOfDigitsAfterDecimal);
        Rval = Rval * p;
        double tmp = Math.floor(Rval);
        System.out.println("~~~~~~tmp~~~~~" + tmp);
        return tmp / p;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.map_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.optionSearch:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
//        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


    public class fetchDoctorsLocation extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... strings) {
            String result = null;
            try {
                URL url = new URL(getString(R.string.link) + "getAllDoctors.php");
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.connect();

                if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    InputStreamReader inputStreamReader = new InputStreamReader(urlConnection.getInputStream());
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                    StringBuilder stringBuilder = new StringBuilder();
                    String temp;
                    while ((temp = bufferedReader.readLine()) != null) {
                        stringBuilder.append(temp);
                    }
                    result = stringBuilder.toString();
                } else {
                    result = "Error occurred retrieving data!!";
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return result;
        }

        @Override
        protected void onPostExecute(String s) {

            super.onPostExecute(s);
            try {
                BitmapDescriptor descriptor= BitmapDescriptorFactory.fromResource(R.drawable.doctor);
//                JSONObject jsonObject=new JSONObject(s);

                jsonArray = new JSONArray(s);
                for (int i = 0; i < jsonArray.length(); i++) {
                    mMarkerMap = new HashMap<>();
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    latitude = Double.parseDouble(jsonObject.getString("lat"));
                    longitude = Double.parseDouble(jsonObject.getString("lon"));
                    String name = "Dr." + jsonObject.getString("name");
                    doctor_id = jsonObject.getString("id");
//                    System.out.println("Doctor Id " + doctor_id);
                    doctorMarker = googleMap.addMarker(new MarkerOptions()
                            .title(name).snippet(doctor_id).position(new LatLng(latitude, longitude)).icon(descriptor));
//                    mMarkerMap.put(doctorMarker, String.valueOf(i));
                    mMarkerMap.put(doctorMarker.getId(), doctor_id);
                    mMarkerMap.put(doctorMarker.getTag(), jsonObject.getString("spcat_id"));
                    doctorMarker.showInfoWindow();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }



}