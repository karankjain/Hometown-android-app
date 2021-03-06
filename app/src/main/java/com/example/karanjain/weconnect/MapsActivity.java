package com.example.karanjain.weconnect;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.support.v4.app.FragmentActivity;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,  GoogleMap.OnMapClickListener, GoogleMap.OnCameraChangeListener  {

    private String country, state, nickname, city, address;
    private double latitude, longitude;
    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        country = getIntent().getStringExtra("country");
        state = getIntent().getStringExtra("state");
        nickname = getIntent().getStringExtra("nickname");
        city = getIntent().getStringExtra("city");

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapClickListener(this);
        mMap.setOnCameraChangeListener(this);

        getLocation();
    }

    @Override
    public void onMapClick(LatLng location) {
        latitude = location.latitude;
        longitude = location.longitude;
        LatLng myLocation = new LatLng(latitude,longitude);
        mMap.addMarker(new MarkerOptions().position(myLocation).title(nickname));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(myLocation));
    }

    @Override
    public void onCameraChange(CameraPosition position){
    }

    private void getLocation(){
        if(country != null && state != null){
            address = state + ", " + country;
            Geocoder locator = new Geocoder(this);
            try{
                List<Address> state = locator.getFromLocationName(address, 1);
                for (Address stateLocation: state){
                    if(stateLocation.hasLatitude())
                        latitude = stateLocation.getLatitude();
                    if(stateLocation.hasLongitude())
                        longitude = stateLocation.getLongitude();
                }
            }catch(Exception e){
                e.printStackTrace();
            }
            LatLng stateLatLng = new LatLng(latitude, longitude);
            CameraUpdate newLocation = CameraUpdateFactory.newLatLngZoom(stateLatLng, 6);
            mMap.addMarker(new MarkerOptions().position(stateLatLng).title(nickname));
            mMap.moveCamera(newLocation);
        }
    }

    public void setLocation(View button) {
        Intent toPassBack = getIntent();
        toPassBack.putExtra("latitude", latitude);
        toPassBack.putExtra("longitude", longitude);
        setResult(RESULT_OK, toPassBack);
        finish();
    }

}
