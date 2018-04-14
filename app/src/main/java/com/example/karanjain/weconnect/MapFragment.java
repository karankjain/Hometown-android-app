package com.example.karanjain.weconnect;

import android.app.Activity;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * Created by karanjain on 4/12/17.
 */

public class MapFragment extends SupportMapFragment implements OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener {


    String nickname, country, url, queryString;
    double latitude, longitude;
    private DbHelper dbHelper;
    private SQLiteDatabase nameDb;
    Geocoder locator;
    GoogleMap map;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(null != getArguments()){
            queryString = getArguments().getString("query");
            url = getArguments().getString("url");
        }
        dbHelper = (new DbHelper( getContext()));
        nameDb = dbHelper.getWritableDatabase();
        dbHelper.onCreate(nameDb);

        this.getFragmentManager().findFragmentById(R.id.map);
        this.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        locator= new Geocoder(getContext());

        getMapMarker(getActivity(), map, 0);
        map.setOnInfoWindowClickListener(this);
    }

    @Override
    public void onInfoWindowClick(Marker marker){
        final Bundle data = new Bundle();
        data.putString("nickname", nickname);
        Intent intent = new Intent(getActivity(),ChatActivity.class);
        intent.putExtras(data);
        startActivity(intent);
    }

    JSONArray mapPoints;
    int id;

    public void getMapMarker(final Activity a, final GoogleMap map, int startId) {
        String url;
        Response.Listener<JSONArray> success = new Response.Listener<JSONArray>() {
            public void onResponse(JSONArray response) {
                mapPoints = response;

                for (int i = 0 ; i < 50 ; i++) {

                    JSONObject rec;
                    try {
                        rec = mapPoints.getJSONObject(i);
                        id = rec.getInt("id");
                        nickname = rec.getString("nickname");
                        latitude = rec.getDouble("latitude");
                        longitude = rec.getDouble("longitude");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    LatLng marker = new LatLng(latitude, longitude);
                    if(nickname!=null){
                        map.addMarker(new MarkerOptions().position(marker).title(nickname));
                    }
                }

                if(response.length()>0){
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            getMapMarker(a, map,id);
                        }
                    }, 3000);

                }
            }
        };

        Response.ErrorListener failure = new Response.ErrorListener() {
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        };

        if(id!=0)
            url = this.url+ "&beforeid="+startId;
        else
            url = this.url;

        JsonArrayRequest getRequest = new JsonArrayRequest(url, success, failure);
        RequestQueue queue = Volley.newRequestQueue(a);
        queue.add(getRequest);

    }

}

