package com.example.karanjain.weconnect;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by karanjain on 4/12/17.
 */

public class ListFragment extends Fragment{

    private ArrayList<UserDetails> mainList;
    View locationDetails, footerView;
    private ListView listView;
    private ListBaseHelper listAdapter;
    private boolean isLoading = false;
    private static boolean serverFlag = true;
    private DbHelper dbHelper;
    private SQLiteDatabase nameDb;
    private MyHandler myHandler;
    private String selectQueryString, url, id, nickname, country, state, city, year, longitude, latitude;
    private int leastId = 0, minId = 0;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        locationDetails = inflater.inflate(R.layout.activity_show_home_town_list, container, false);
        footerView = inflater.inflate(R.layout.footer_view, container, false);
        listView = (ListView) locationDetails.findViewById(R.id.listView);
        mainList = new ArrayList<>();
        dbHelper = (new DbHelper(getContext()));
        nameDb = dbHelper.getWritableDatabase();
        dbHelper.onCreate(nameDb);
        myHandler = new MyHandler();

        if (null != getArguments()) {
            url = getArguments().getString("url");
            selectQueryString = getArguments().getString("query");
        }

        LatestData(getLastId());

        return locationDetails;
    }


    public class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    listView.addFooterView(footerView);
                    break;
                case 1:
                    listAdapter.addListItemToAdapter((ArrayList<UserDetails>) msg.obj);
                    listView.removeFooterView(footerView);
                    isLoading = false;
                    break;
                default:
                    break;
            }
        }
    }

    public class MoreDataThread {
        int pageNumber, initialCount;

        MoreDataThread() {
            this.pageNumber = 1;
            this.initialCount = 0;
        }

        public void run(int leastId) {
            myHandler.sendEmptyMessage(0);
            ArrayList<UserDetails> listResult;
            if (serverFlag) {
                if (minId == 0) {
                    listResult = getMoreDataFromServer(pageNumber, leastId);
                } else {
                    listResult = getMoreDataFromServer(pageNumber, minId);
                }
                pageNumber++;
            } else {
                listResult = getDataFromSQL(leastId).entrySet().iterator().next().getKey();
                if (listResult.size() == 0) {
                    listResult = getMoreDataFromServer(pageNumber, leastId);
                } else if (listResult.size() < 25) {
                    serverFlag = true;
                    pageNumber = 0;
                    minId = (getDataFromSQL(leastId).entrySet().iterator().next().getValue()) + 1;
                }
            }
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Message msg = myHandler.obtainMessage(1, listResult);
            myHandler.sendMessage(msg);
        }
    }

    private void LatestData(int lastId) {
        final int maxId = lastId;
        Response.Listener<JSONArray> success = new Response.Listener<JSONArray>() {
            public void onResponse(JSONArray response) {
                try {
                    final int beforeId;
                    if (response != null) {
                        for (int i = 0; i < response.length(); i++) {
                            ContentValues dataValues = new ContentValues();
                            UserDetails userInfo = new UserDetails();
                            JSONObject jsonObj = response.getJSONObject(i);
                            id = (jsonObj.get("id").toString());
                            nickname = (jsonObj.get("nickname").toString());
                            country = (jsonObj.get("country").toString());
                            state = (jsonObj.get("state").toString());
                            city = (jsonObj.get("city").toString());
                            year = (jsonObj.get("year").toString());
                            latitude = (jsonObj.get("latitude").toString());
                            longitude = (jsonObj.get("longitude").toString());
                            userInfo.setId(Integer.valueOf(id));
                            userInfo.setNickName(nickname);
                            userInfo.setCountry(country);
                            userInfo.setState(state);
                            userInfo.setCity(city);
                            userInfo.setYear(year);
                            userInfo.setLatitude(latitude);
                            userInfo.setLongitude(longitude);
                            dataValues.put("ID", id);
                            dataValues.put("NICKNAME", nickname);
                            dataValues.put("COUNTRY", country);
                            dataValues.put("STATE", state);
                            dataValues.put("CITY", city);
                            dataValues.put("YEAR", year);
                            dataValues.put("LONGITUDE", longitude);
                            dataValues.put("LATITUDE", latitude);
                            mainList.add(userInfo);
                            nameDb.insert("HOMETOWNLOCATION_DETAILS", null, dataValues);
                        }
                        if (!mainList.isEmpty() && mainList.size() !=0) {
                            beforeId = mainList.get(mainList.size() - 1).getId();
                        } else{
                            beforeId = 0;
                        }
                        if (null != mainList && mainList.size() != 0 && beforeId <= maxId) {
                            serverFlag = false;
                        }
                        leastId = beforeId;

                        listAdapter = new ListBaseHelper(getActivity(), mainList);
                        listView.setAdapter(listAdapter);
                        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
                            MoreDataThread threadGetMoreData = new MoreDataThread();

                            @Override
                            public void onScrollStateChanged(AbsListView view, int scrollState) {

                            }

                            @Override
                            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                                if (view.getLastVisiblePosition() == totalItemCount - 1 && listView.getCount() >= 25 && isLoading == false) {
                                    isLoading = true;
                                    threadGetMoreData.run(leastId);
                                }
                            }
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };

        Response.ErrorListener failure = new Response.ErrorListener() {
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        };
        String getUrl = url + "&page=0";
        JsonArrayRequest getRequest = new JsonArrayRequest(getUrl, success, failure);
        VolleyQueue.instance(getActivity()).add(getRequest);
    }

    private ArrayList<UserDetails> getMoreDataFromServer(int page, int leastId) {
        final ArrayList<UserDetails> lst = new ArrayList<>();
        Response.Listener<JSONArray> success = new Response.Listener<JSONArray>() {
            public void onResponse(JSONArray response) {
                try {
                    if (response != null) {
                        for (int i = 0; i < response.length(); i++) {
                            UserDetails userInfo = new UserDetails();
                            ContentValues dataValues = new ContentValues();
                            JSONObject jsonObj = response.getJSONObject(i);
                            id = (jsonObj.get("id").toString());
                            nickname = (jsonObj.get("nickname").toString());
                            country = (jsonObj.get("country").toString());
                            state = (jsonObj.get("state").toString());
                            city = (jsonObj.get("city").toString());
                            year = (jsonObj.get("year").toString());
                            latitude = (jsonObj.get("latitude").toString());
                            longitude = (jsonObj.get("longitude").toString());
                            userInfo.setId(Integer.valueOf(id));
                            userInfo.setNickName(nickname);
                            userInfo.setCountry(country);
                            userInfo.setState(state);
                            userInfo.setCity(city);
                            userInfo.setYear(year);
                            userInfo.setLatitude(latitude);
                            userInfo.setLongitude(longitude);
                            dataValues.put("ID", id);
                            dataValues.put("NICKNAME", nickname);
                            dataValues.put("COUNTRY", country);
                            dataValues.put("STATE", state);
                            dataValues.put("CITY", city);
                            dataValues.put("YEAR", year);
                            dataValues.put("LONGITUDE", longitude);
                            dataValues.put("LATITUDE", latitude);
                            lst.add(userInfo);
                            nameDb.insert("HOMETOWNLOCATION_DETAILS", null, dataValues);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        };

        Response.ErrorListener failure = new Response.ErrorListener() {
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        };

        String getUrl = url + "&beforeid=" + leastId + "&page=" + page;
        JsonArrayRequest getRequest = new JsonArrayRequest(getUrl, success, failure);
        VolleyQueue.instance(getActivity()).add(getRequest);
        return lst;
    }



    private HashMap<ArrayList<UserDetails>, Integer> getDataFromSQL(int lastId) {
        ArrayList<UserDetails> lst = new ArrayList<>();
        Cursor result;
        if(selectQueryString.equals("SELECT * FROM HOMETOWNLOCATION_DETAILS "))
            result = nameDb.rawQuery(selectQueryString + " WHERE ID < " + lastId + " ORDER BY ID DESC LIMIT 50", null);
        else {
            result = nameDb.rawQuery(selectQueryString + " AND ID < " + lastId + " ORDER BY ID DESC LIMIT 50", null);
        }
        if (result.moveToFirst()) {
            do {
                UserDetails details = new UserDetails();
                details.setNickName(result.getString(1));
                details.setId(Integer.parseInt(result.getString(0)));
                details.setCountry(result.getString(2));
                details.setCity(result.getString(4));
                details.setState(result.getString(3));
                lst.add(details);
                leastId = details.getId();
            } while (result.moveToNext());
        }
        HashMap<ArrayList<UserDetails>, Integer> resultMap = new HashMap<>();
        resultMap.put(lst, leastId);
        return resultMap;
    }

    private int getLastId() {
        int lastId = 0;
        Cursor result = nameDb.rawQuery("SELECT MAX(ID) FROM HOMETOWNLOCATION_DETAILS", null);
        if (null != result && result.moveToFirst()) {
            lastId = (int) result.getLong(0);
        }
        return lastId;
    }




}
