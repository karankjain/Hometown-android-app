package com.example.karanjain.weconnect;


import android.util.Log;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONArray;

public class DisplayUserActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    TextView filterTextView;
    private String TAG = "DisplayUserActivity:";
    FragmentManager fragmentManager;
    private Spinner countryValue, stateValue, yearValue;
    String inputCountry, inputState, inputYear, url, query;
    private DbHelper dbHelper;
    private SQLiteDatabase nameDb;
    Bundle bundle;
    boolean isListView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_show_user);
        mAuth = FirebaseAuth.getInstance();

        countryValue = (Spinner) findViewById(R.id.spinnerCountry);
        stateValue = (Spinner) findViewById(R.id.spinnerState);
        yearValue = (Spinner) findViewById(R.id.spinnerYear);

        filterTextView = (TextView) findViewById(R.id.filterText);

        fragmentManager = getSupportFragmentManager();

        dbHelper = (new DbHelper( DisplayUserActivity.this));
        nameDb = dbHelper.getWritableDatabase();
        dbHelper.onCreate(nameDb);

        getAllCountries();
        countryValue.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                inputCountry = parent.getItemAtPosition(position).toString();
                if(checkIfEmpty(inputCountry)){
                    getAllStates(inputCountry);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        stateValue.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                inputState = parent.getItemAtPosition(position).toString();

            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        String yearStr[] = new String[49];
        int startYear = 1970;
        yearStr[0] = "Select Year";
        for(int i = 1; i<=48; i++){
            yearStr[i] = String.valueOf(startYear);
            startYear++;
        }

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(DisplayUserActivity.this, android.R.layout.simple_spinner_item, yearStr);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        yearValue.setAdapter(dataAdapter);

        yearValue.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                inputYear = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    Log.d(TAG, "StateChanged:sign_in:" + user.getUid());
                } else {
                    Log.d(TAG, "StateChanged:sign_out");
                }
            }
        };

        isListView = true;
        listView();
    }

    public void listView() {
        url = getUrlPath();
        isListView = true;
        query = generateQuery();
        bundle = new Bundle();
        bundle.putString("query", query);
        bundle.putString("url", url);
        ListFragment userList = new ListFragment();
        userList.setArguments(bundle);
        fragmentManager.beginTransaction()
                .replace(R.id.content_frame, userList)
                .addToBackStack(null)
                .commit();
    }

    public void mapView() {
        isListView = false;
        url = getUrlPath();
        query = generateQuery();
        bundle = new Bundle();
        bundle.putString("query", query);
        bundle.putString("url", url);
        MapFragment userList = new MapFragment();
        userList.setArguments(bundle);
        fragmentManager.beginTransaction()
                .replace(R.id.content_frame, userList)
                .addToBackStack(null)
                .commit();
    }



    public void getAllCountries() {
        Response.Listener<JSONArray> success = new Response.Listener<JSONArray>() {
            public void onResponse(JSONArray response) {
                String strings[] = Utility.toArray(response.toString());

                String newStr[] = new String[strings.length+1];
                newStr[0] = "Select Country";
                for(int i=1; i<newStr.length; i++){
                    newStr[i] = strings[i-1];
                }
                ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(DisplayUserActivity.this,
                        android.R.layout.simple_spinner_item, newStr);
                dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                countryValue.setAdapter(dataAdapter);

            }
        };
        Response.ErrorListener failure = new Response.ErrorListener() {
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        };
        String url ="http://bismarck.sdsu.edu/hometown/countries";
        JsonArrayRequest getRequest = new JsonArrayRequest( url, success, failure);
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(getRequest);
    }

    public void getAllStates(String countrySelected) {
        Response.Listener<JSONArray> success = new Response.Listener<JSONArray>() {
            public void onResponse(JSONArray response) {
                String strings[] = Utility.toArray(response.toString());
                String newStr[] = new String[strings.length+1];
                newStr[0] = "Select State";
                for(int i=1; i<newStr.length; i++){
                    newStr[i] = strings[i-1];
                }
                ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(DisplayUserActivity.this,
                        android.R.layout.simple_spinner_item, newStr);
                dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                stateValue.setAdapter(dataAdapter);
            }
        };
        Response.ErrorListener failure = new Response.ErrorListener() {
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        };
        String url ="http://bismarck.sdsu.edu/hometown/states?country="+countrySelected;
        JsonArrayRequest getRequest = new JsonArrayRequest( url, success, failure);
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(getRequest);
    }

    public boolean checkIfEmpty(String string){
        if( null != string && !string.isEmpty() && !string.contains("Select") ) {
            return true;
        }
        return false;
    }

    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case R.id.signOutMenu:
                mAuth.signOut();
                this.finish();
                break;
            case R.id.chatMenu:
                Intent intent = new Intent(DisplayUserActivity.this,ChatHistoryActivity.class);
                startActivity(intent);
                break;
            case R.id.mapMenu:
                mapView();
                break;
            case R.id.listMenu:
                listView();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    public void onApplyFilter(View v){
        if(isListView){
            listView();
        }else{
            mapView();
        }
    }

    public String getUrlPath(){
        filterTextView.setText("Filter applied");
        if(checkIfEmpty(inputCountry) && checkIfEmpty(inputState) && checkIfEmpty(inputYear)){
            url = "http://bismarck.sdsu.edu/hometown/users?reverse=true&country=" + inputCountry + "&state=" + inputState + "&year=" + inputYear;
        }else if(checkIfEmpty(inputCountry)&& checkIfEmpty(inputState)){
            url = "http://bismarck.sdsu.edu/hometown/users?reverse=true&country=" + inputCountry + "&state=" + inputState;
        }else if(checkIfEmpty(inputCountry)&& checkIfEmpty(inputYear)){
            url = "http://bismarck.sdsu.edu/hometown/users?reverse=true&country=" + inputCountry + "&year=" + inputYear;
        }else if(checkIfEmpty(inputState)&& checkIfEmpty(inputYear)){
            url = "http://bismarck.sdsu.edu/hometown/users?reverse=true&state=" + inputState + "&year=" + inputYear;
        }else if(checkIfEmpty(inputState)){
            url = "http://bismarck.sdsu.edu/hometown/users?reverse=true&state=" + inputState;
        }else if(checkIfEmpty(inputYear)){
            url = "http://bismarck.sdsu.edu/hometown/users?reverse=true&year=" + inputYear;
        }else if(checkIfEmpty(inputCountry)){
            url = "http://bismarck.sdsu.edu/hometown/users?reverse=true&country=" + inputCountry;
        }else{
            url = "http://bismarck.sdsu.edu/hometown/users?reverse=true";
            filterTextView.setText("Filter not applied");
        }
        return url;
    }

    public String generateQuery(){
        if (checkIfEmpty(inputCountry) && checkIfEmpty(inputState)
                && checkIfEmpty(inputYear)) {
            query = " SELECT * FROM HOMETOWNLOCATION_DETAILS WHERE COUNTRY = '" + inputCountry + "'AND STATE = '" + inputState + "'AND YEAR= " + Integer.valueOf(inputYear);
        } else if (checkIfEmpty(inputCountry) &&
                checkIfEmpty(inputState)) {
            query = " SELECT * FROM HOMETOWNLOCATION_DETAILS WHERE COUNTRY='" + inputCountry + "' AND STATE = '" + inputState +"' ";
        } else if (checkIfEmpty(inputCountry) &&
                checkIfEmpty(inputYear)) {
            query = " SELECT * FROM HOMETOWNLOCATION_DETAILS WHERE COUNTRY='" + inputCountry + "'AND YEAR = " + Integer.valueOf(inputYear);
        } else if (checkIfEmpty(inputState) && checkIfEmpty(inputYear)) {
            query = " SELECT * FROM HOMETOWNLOCATION_DETAILS WHERE STATE='" + inputState + "'AND YEAR = " + Integer.valueOf(inputYear);
        } else if (checkIfEmpty(inputState)) {
            query = " SELECT * FROM HOMETOWNLOCATION_DETAILS WHERE STATE='" + inputState +"' ";
        } else if (checkIfEmpty(inputYear)) {
            query = " SELECT * FROM HOMETOWNLOCATION_DETAILS WHERE YEAR=" + Integer.valueOf(inputYear);
        } else if (checkIfEmpty(inputCountry)) {
            query = " SELECT * FROM HOMETOWNLOCATION_DETAILS WHERE COUNTRY='" + inputCountry +"' ";
        } else {
            query = "SELECT * FROM HOMETOWNLOCATION_DETAILS ";
        }
        return query;
    }

}
