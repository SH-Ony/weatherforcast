package com.example.weatherforcast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private RelativeLayout homeRL;
    private TextView cityTV,tempTV,conditionTV;
    private ImageView backIV,searchIV,iconIV;
    private ProgressBar loadingPB;
    private RecyclerView weatherRV;
    private TextInputEditText cityEDT;
    private ArrayList<WeatherRVModel> weatherRVModelArrayList;
    private WeatherAdapter weatherAdapter;
    private String cityName;

    private LocationManager locationManager;
    private int PERMISSION_CODE=1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        setContentView(R.layout.activity_main);

        homeRL= findViewById(R.id.RLhome);
        cityTV= findViewById(R.id.TVcityname);
        tempTV= findViewById(R.id.TVtemp);
        conditionTV= findViewById(R.id.TVconditon);
        backIV= findViewById(R.id.IVback);
        searchIV= findViewById(R.id.IVsearch);
        iconIV= findViewById(R.id.IVicon);
        loadingPB= findViewById(R.id.PBloading);
        weatherRV= findViewById(R.id.RVweather);
        cityEDT= findViewById(R.id.ETcity);
        weatherRVModelArrayList = new ArrayList<>();
        weatherAdapter = new WeatherAdapter(this,weatherRVModelArrayList);
        weatherRV.setAdapter(weatherAdapter);

        locationManager =(LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if(ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},PERMISSION_CODE);

        }

        Location location=locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        cityName=getCityName(location.getLongitude(),location.getLatitude());

        getWeatherInfo(cityName);

        searchIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String city=cityEDT.getText().toString();
                if(city.isEmpty()){
                    Toast.makeText(MainActivity.this,"Please Enter City Name",Toast.LENGTH_SHORT).show();
                }else{
                    cityTV.setText(cityName);
                    getWeatherInfo(city);
                }
            }
        });


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==PERMISSION_CODE){
            if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this,"Permission Granted..",Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(this,"Please provide permission",Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private String getCityName(double longitude, double latitude)
    {
        String cityName="Not Found";
        Geocoder gcd=new Geocoder(getBaseContext(), Locale.getDefault());

    try {
        List<Address> addresses=gcd.getFromLocation(latitude,longitude, 10);
        for (Address adr: addresses)
        {
            if(adr!=null)
            {
                String city;
                city = adr.getLocality();
                if(city!=null&& !city.equals("")){
                    cityName=city;
                }else{
                    Log.d("TAG","City not found");
                    Toast.makeText(this,"user city not found",Toast.LENGTH_SHORT).show();
                }
            }
        }


    } catch (IOException e) {
        e.printStackTrace();
    }

    return  cityName;

    }

    private void getWeatherInfo(String cityName)
    {
        String url = "http://api.weatherapi.com/v1/forecast.json?key=894ccd7d65774eaab3f222701220704&q="+cityName+"&days=1&aqi=no&alerts=no";
        cityTV.setText(cityName);
        RequestQueue requestQueue=Volley.newRequestQueue(MainActivity.this);

        JsonObjectRequest jsonObjectRequest=new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                loadingPB.setVisibility(View.GONE);
                homeRL.setVisibility(View.VISIBLE);
                weatherRVModelArrayList.clear();



                try {
                    String temp=response.getJSONObject("current").getString("temp_c");
                    tempTV.setText(temp+"℃");
                    int isDay=response.getJSONObject("current").getInt("is_day");
                    String condition =response.getJSONObject("current").getJSONObject("condition").getString("temp_c");
                    String conditionIcon =response.getJSONObject("current").getJSONObject("condition").getString("icon");
                    Picasso.get().load("http".concat(conditionIcon)).into(iconIV);
                    conditionTV.setText(condition);
                    if (isDay==1)
                    {
                        Picasso.get().load("https://images.unsplash.com/photo-1470252649378-9c29740c9fa8?ixlib=rb-1.2.1&ixid=MnwxMjA3fDB8MHxzZWFyY2h8Mnx8bW9ybmluZ3xlbnwwfHwwfHw%3D&w=1000&q=80").into(backIV);
                    }else {
                        Picasso.get().load("https://www.pexels.com/photo/photography-of-turned-on-street-lamps-beside-bay-during-night-time-771883/").into(backIV);
                    }

                    JSONObject forcastObj=response.getJSONObject("forcast");
                    JSONObject forcastO= forcastObj.getJSONArray("forcastday").getJSONObject(0);
                    JSONArray hourArray= forcastO.getJSONArray("hour");

                    for(int i=0;i<hourArray.length();i++)
                    {
                        JSONObject hourObj=hourArray.getJSONObject(i);
                        String time=hourObj.getString("time");
                        String temper=hourObj.getString("temp_c");
                        String img=hourObj.getJSONObject("condition").getString("icon");
                        String wind=hourObj.getString("wind_kph");

                        weatherRVModelArrayList.add(new WeatherRVModel(time,temper,img,wind));
                    }
                    weatherAdapter.notifyDataSetChanged();


                }catch (JSONException e){
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this,"Please Enter Valid City",Toast.LENGTH_SHORT).show();
            }
        });

        requestQueue.add(jsonObjectRequest);

    }
}