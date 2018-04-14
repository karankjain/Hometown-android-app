package com.example.karanjain.weconnect;

import android.util.Log;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class CreateUserActivity extends AppCompatActivity {

    private static final int INTENT_SET_LOCATION = 213;

    private FirebaseAuth firebaseAuth;

    private Button buttonSetLocation, buttonRegisterUser;
    private EditText emailText, nicknameText, passwordText, cityText, yearText;
    private TextView textLong, textLat;
    private String country, state, nickname;
    private Spinner countryValue, stateValue;
    double longitude, latitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_user);
        firebaseAuth = FirebaseAuth.getInstance();
        textLong = (TextView) findViewById(R.id.longitude);
        textLat = (TextView)findViewById(R.id.latitude);
        emailText = (EditText) findViewById(R.id.emailAddress);
        nicknameText = (EditText)findViewById(R.id.nickName);
        passwordText =(EditText) findViewById(R.id.password);
        cityText = (EditText)findViewById(R.id.city);
        yearText = (EditText)findViewById(R.id.year);
        countryValue =(Spinner) findViewById(R.id.country);
        stateValue = (Spinner)findViewById(R.id.state);
        buttonRegisterUser = (Button) findViewById(R.id.registerUser);
        buttonSetLocation = (Button)findViewById(R.id.setLocation);

        if (getIntent()!= null && getIntent().getExtras() != null) {
            latitude = getIntent().getExtras().getDouble("latitude");
            longitude = getIntent().getExtras().getDouble("longitude");
            textLat.setText(String.valueOf(latitude));
            textLong.setText(String.valueOf(longitude));
        }

        getAllCountries();
        countryValue.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                country = parent.getItemAtPosition(position).toString();
                getAllStates(country);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        stateValue.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                state = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        buttonSetLocation.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(CreateUserActivity.this,MapsActivity.class);
                startActivityForResult(intent, INTENT_SET_LOCATION);
            }
        });

        buttonRegisterUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NewUser(v);
            }
        });
    }




    public void getAllCountries() {

        Response.Listener<JSONArray> success = new Response.Listener<JSONArray>() {
            public void onResponse(JSONArray response) {
                String strings[] = Utility.toArray(response.toString());
                for(int i=0; i<response.length(); i++){
                    ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(CreateUserActivity.this, android.R.layout.simple_spinner_item, strings);
                    dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    countryValue.setAdapter(dataAdapter);
                }
            }
        };

        Response.ErrorListener failure = new Response.ErrorListener() {
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        };

        String url = "http://bismarck.sdsu.edu/hometown/countries";
        JsonArrayRequest getRequest = new JsonArrayRequest(url, success, failure);
        VolleyQueue.instance(CreateUserActivity.this).add(getRequest);
    }

    public void getAllStates(String selectedCountry) {
        Response.Listener<JSONArray> success = new Response.Listener<JSONArray>() {
            public void onResponse(JSONArray response) {
                String strings[] = Utility.toArray(response.toString());
                for(int i=0; i<response.length(); i++){
                    ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(CreateUserActivity.this, android.R.layout.simple_spinner_item, strings);
                    dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    stateValue.setAdapter(dataAdapter);
                }
            }
        };

        Response.ErrorListener failure = new Response.ErrorListener() {
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        };

        String url = Constants.GET_STATES + selectedCountry;
        JsonArrayRequest getRequest = new JsonArrayRequest(url, success, failure);
        VolleyQueue.instance(CreateUserActivity.this).add(getRequest);
    }

    public void NewUser(View v){

        if(checkUser()) {
            insertData();
        }
        else {
            Utility.dialogBox("Sorry!","Please enter all values.",true, CreateUserActivity.this);
        }

        Utility.displayProgressDialog(CreateUserActivity.this);
        (firebaseAuth.createUserWithEmailAndPassword(emailText.getText().toString(),
                passwordText.getText().toString()))
                .addOnCompleteListener(
                        new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                Utility.removeProgressDialog();
                                if (task.isSuccessful()){
                                    firebaseAuth.signInWithEmailAndPassword(emailText.getText().toString(),
                                            passwordText.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                        @Override
                                        public void onComplete(@NonNull Task<AuthResult> task) {
                                            if (task.isSuccessful()){
                                                FirebaseUser user = firebaseAuth.getCurrentUser();
                                                nickname = nicknameText.getText().toString();
                                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("users");
                                                ref.child(user.getUid()).push().setValue(nickname);
                                                FirebaseAuth.getInstance().signOut();
                                            }
                                        }
                                    });

                                    Utility.dialogBox(".Congrats!", "Registration Successful.", false,CreateUserActivity.this);
                                    Intent intent = new Intent(CreateUserActivity.this, MainActivity.class);
                                    startActivity(intent);
                                } else {
                                    Utility.dialogBox("Oops!", "User not registered.", true, CreateUserActivity.this);
                                }
                            }
                        }

                );
    }

    private boolean checkUser(){
        if (!Utility.checkIsNullOrIsEmpty(nicknameText.getText().toString())
                || !Utility.checkIsNullOrIsEmpty(passwordText.getText().toString())
                ||  !Utility.checkIsNullOrIsEmpty(country)
                ||  !Utility.checkIsNullOrIsEmpty(state)
                ||  !Utility.checkIsNullOrIsEmpty(cityText.getText().toString())
                ||  !Utility.checkIsNullOrIsEmpty(yearText.getText().toString())) {
            return false;
        } else {
            return  true;
        }
    }

    private void insertData() {
        JSONObject data = new JSONObject();
        try {
            data.put("nickname", nicknameText.getText().toString());
            data.put("password", passwordText.getText().toString());
            data.put("country", country);
            data.put("state", state);
            data.put("city", cityText.getText().toString());
            data.put("year", Integer.valueOf(yearText.getText().toString()));
            if(!textLong.getText().equals("") && !textLat.getText().equals("")){
                data.put("latitude", latitude);
                data.put("longitude", longitude);
            }
        } catch (JSONException error) {
            error.printStackTrace();
            return;
        }

        Response.Listener<JSONObject> success = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Utility.dialogBox("Congrats", "Registration Successful!!",false, CreateUserActivity.this);

            }
        };

        Response.ErrorListener failure = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Utility.dialogBox("Oops", new String(error.networkResponse.data),false,CreateUserActivity.this);
            }
        };

        JsonObjectRequest postRequest = new JsonObjectRequest(Constants.ADD_USER, data, success, failure);
        VolleyQueue.instance(CreateUserActivity.this).add(postRequest);

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == INTENT_SET_LOCATION) {
            switch (resultCode) {
                case RESULT_OK:
                    textLat.setText(String.valueOf(data.getExtras().get("latitude")));
                    textLong.setText(String.valueOf(data.getExtras().get("longitude")));
                    break;
                case RESULT_CANCELED:
                    break;
            }
        }
    }

}
