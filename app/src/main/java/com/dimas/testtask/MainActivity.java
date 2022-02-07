package com.dimas.testtask;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.dimas.testtask.Model.CoinModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigFetchThrottledException;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private RecyclerView currencies;
    private int pageIndex = 0;
    private ProgressBar progressBarLoader;
    private ArrayList<CoinModel> coinModelArrayList;
    private CurrencyAdapter currencyAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private FirebaseRemoteConfig mFirebaseRemoteConfig;
    private TextView textView;
    private MenuItem menuItem;
    private static final String KEY_INDEX = "index";
    private FirebaseDatabase database;
    private DatabaseReference reference;


    //No Internet
    String s1[], s2[], s3[];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState != null){
            pageIndex = savedInstanceState.getInt(KEY_INDEX, 0);
        }

        database = FirebaseDatabase.getInstance("https://testtask-832bc-default-rtdb.firebaseio.com/");
        reference = database.getReference("currencies");




        textView = findViewById(R.id.idPrice);
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        mFirebaseRemoteConfig.setDefaultsAsync(R.xml.remote_config);
        getConfigData();

        swipeRefreshLayout = findViewById(R.id.idSwipe);
        currencies = findViewById(R.id.idCurrency);
        progressBarLoader = findViewById(R.id.idProgressBarLoading);
        coinModelArrayList = new ArrayList<>();

        //Internet available
        currencyAdapter = new CurrencyAdapter(coinModelArrayList, this);
        currencies.setLayoutManager(new LinearLayoutManager(this));
        currencies.setAdapter(currencyAdapter);
        getCurrencyData();

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onRefresh() {

                getCurrencyData();
                currencyAdapter.notifyDataSetChanged();
                swipeRefreshLayout.setRefreshing(false);

            }
        });

    }



    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        savedInstanceState.putInt(KEY_INDEX, pageIndex);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu,menu);
        menuItem = menu.findItem(R.id.moon);

        /*if (AppCompatDelegate.isCompatVectorFromResourcesEnabled()) {

            menuItem.setIcon(R.drawable.ic_baseline_nights_stay_24);
        } else {
            menuItem.setIcon(R.drawable.ic_baseline_wb_sunny_24);
        }*/
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.moon){
            //Night on
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);

            //Night off
        } else if (item.getItemId() == R.id.sun){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        return super.onOptionsItemSelected(item);
    }

    private void getConfigData() {
        mFirebaseRemoteConfig.fetch(0).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isComplete()){
                    Toast.makeText(MainActivity.this, "Remote config successfully connected", Toast.LENGTH_SHORT).show();
                    mFirebaseRemoteConfig.fetchAndActivate();
                } else {
                    Toast.makeText(MainActivity.this, "Remote config dose`t connected", Toast.LENGTH_SHORT).show();

                    showText();
                }
            }
        });
    }

    private void showText() {textView.setText(mFirebaseRemoteConfig.getString("price_key"));
    }



    private void getCurrencyData(){
        //Update data
        progressBarLoader.setVisibility(View.VISIBLE);
        String url = "https://pro-api.coinmarketcap.com/v1/cryptocurrency/listings/latest";
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        progressBarLoader.setVisibility(View.GONE);
                        try{
                            JSONArray dataArray = response.getJSONArray("data");
                            for(int i=0; i<dataArray.length(); i++){
                                JSONObject dataObject = dataArray.getJSONObject(i);
                                String name = dataObject.getString("name");
                                String symbol = dataObject.getString("symbol");
                                JSONObject quote = dataObject.getJSONObject("quote");
                                JSONObject USD = quote.getJSONObject("USD");
                                double price = USD.getDouble("price");
                                coinModelArrayList.add(new CoinModel(name, symbol, price));
                                reference.setValue(coinModelArrayList);

                            }
                            currencyAdapter.notifyDataSetChanged();
                        }catch (JSONException e){
                            e.printStackTrace();
                            Toast.makeText(MainActivity.this, "Fail to extract json data", Toast.LENGTH_SHORT).show();
                        }

                    }
                }, error -> {
                    showData();
                    progressBarLoader.setVisibility(View.GONE);
                    Toast.makeText(MainActivity.this, "Fail to get data", Toast.LENGTH_SHORT).show();
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("X-CMC_PRO_API_KEY","267f402c-cd10-4703-b6a5-a8616a9b7e9f");
                return headers;
            }
        };
        requestQueue.add(jsonObjectRequest);
    }
    private void showData() {
        s1 = getResources().getStringArray(R.array.nameCoin);
        s2 = getResources().getStringArray(R.array.symbolCoin);
        s3 = getResources().getStringArray(R.array.priceCoin);
        //No Internet
        NoInternetAdapter noInternetAdapter = new NoInternetAdapter(this, s1, s2, s3);
        currencies.setAdapter(noInternetAdapter);
        currencies.setLayoutManager(new LinearLayoutManager(this));
    }

    private void updateList(){
        reference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                coinModelArrayList.add(snapshot.getValue(CoinModel.class));
                currencyAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                CoinModel model = snapshot.getValue(CoinModel.class);


            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}