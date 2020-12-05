package com.example.stockapp;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class HomeScreenActivity extends AppCompatActivity {
    AutoSuggestAdapter autoSuggestAdapter;
    ArrayList<String> searchResults;
    ArrayList<String> favorites;
    ArrayList<String> portfolio;
    RecyclerView mainRecyclerView;
    TextView poweredByTiingo;
    public List<Section> sectionList;
    String ticker = "";
    int flag=0;
    float sumOfStocksValue;
    String sectionTwoName="PORTFOLIO";
    List<StockCard> sectionOneItems;
    String sectionOneName="FAVORITES";
    List<StockCard> sectionTwoItems;
    String newWorth;
    DecimalFormat decimalFormat;
    TextView date;
    Calendar calendar;
    SimpleDateFormat simpleDateFormat;
    String clickedTicker="";
    Handler handler;

    Timer timer;
    ConstraintLayout spinnerLayout;
    Map<String, StockCard> pMap= new HashMap<>();
    Map<String, StockCard> fMap= new HashMap<>();
    SharedPreferences netWorthSharedPreferences;
    SharedPreferences.Editor cashEditor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setTheme(R.style.Theme_StockApp);
        setContentView(R.layout.activity_main);

        sectionOneItems = new ArrayList<>();
        sectionTwoItems = new ArrayList<>();
        getWindow().setStatusBarColor(ContextCompat.getColor(this,R.color.light_gray));
        netWorthSharedPreferences =getSharedPreferences("uninvested_cash",MODE_PRIVATE);
        cashEditor = netWorthSharedPreferences.edit();
        sectionList=new ArrayList<>();
        poweredByTiingo = findViewById(R.id.tiingo);
        spinnerLayout = findViewById(R.id.spinnerLayout);
        date=findViewById(R.id.date);

        Toolbar toolbar= (Toolbar) findViewById(R.id.my_toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        calendar = Calendar.getInstance();
        simpleDateFormat = new SimpleDateFormat("MMMM d, yyyy");
        String d = simpleDateFormat.format(calendar.getTime());
        date.setText(d);


        poweredByTiingo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("https://www.tiingo.com/"));
                startActivity(intent);
            }
        });


        //getUpdatedDataPortfolio();
        //getPortfolioData();
        //getUpdatedDataFavorites();
        //getFavoritesData();

//        timer = new Timer();
//        timer.schedule(new TimerTask() {
//            @Override
//            public void run() {
//                Log.d("now","run now");
//                flag=0;
//                getPortfolioData();
//                getFavoritesData();
//            }
//        },0,15000);
        getPortfolioData();
        getFavoritesData();
        handler=new Handler();
        handler.postDelayed(runnable, 15000);

    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            flag=0;
            getPortfolioData();
            getFavoritesData();
            handler.postDelayed(this, 15000);
        }
    };



    @Override
    protected void onPause() {
        super.onPause();
        //timer.cancel();
        handler.removeCallbacks(runnable);

    }

    @Override
    protected void onRestart() {
        super.onRestart();
        spinnerLayout.setVisibility(View.VISIBLE);
        //sectionList.clear();
        //getUpdatedDataPortfolio();
        getNames();
//        timer = new Timer();
//        timer.schedule(new TimerTask() {
//            @Override
//            public void run() {
//                Log.d("now","run now");
//                flag=0;
//                getPortfolioData();
//                getFavoritesData();
//            }
//        },0,15000);

        flag=0;
        getPortfolioData();
        getFavoritesData();
        handler = new Handler();
        handler.postDelayed(runnable,15000);

    }


    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search_button,menu);
        MenuItem menuItem = menu.findItem(R.id.search);
        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setQueryHint("");
        searchView.setIconified(false);

        final AutoCompleteTextView autoCompleteTextView = searchView.findViewById(androidx.appcompat.R.id.search_src_text);
        autoSuggestAdapter = new AutoSuggestAdapter(HomeScreenActivity.this, android.R.layout.simple_dropdown_item_1line);
        autoCompleteTextView.setTextCursorDrawable(R.drawable.cursor_color);
        autoCompleteTextView.setThreshold(3);
        autoCompleteTextView.setDropDownHeight(1100);
        autoCompleteTextView.setAdapter(autoSuggestAdapter);
        autoCompleteTextView.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        autoCompleteTextView.setText(autoSuggestAdapter.getObject(position));
                        clickedTicker = autoSuggestAdapter.getObject(position).split("-")[0];
                    }
                });


        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchView.clearFocus();
                Intent intent = new Intent(HomeScreenActivity.this,StockScreenActivity.class);
                intent.putExtra("ticker",clickedTicker.substring(0,clickedTicker.length()-1));
                startActivity(intent);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                getData(newText);
                return true;
            }

        });


        return super.onCreateOptionsMenu(menu);
    }

    public void getData(String value){
        final String url = "http://nodejshw8app.us-east-1.elasticbeanstalk.com/" + value;
        Log.d("url",url);
        searchResults = new ArrayList<String>();
        RequestQueue queue = Volley.newRequestQueue(HomeScreenActivity.this);

        JsonArrayRequest getRequest = new JsonArrayRequest(Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                try {
                    for (int i = 0; i < response.length(); i++) {
                        ticker = response.getJSONObject(i).getString("ticker");
                        String name = response.getJSONObject(i).getString("name");

                        searchResults.add(ticker + " - " + name);
                    }
                    autoSuggestAdapter.setData(searchResults);
                    autoSuggestAdapter.notifyDataSetChanged();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("HttpClient", "error: " + error.toString());
            }
        });
        queue.add(getRequest);
    }

    private void getNames(){
        SharedPreferences company_names = getSharedPreferences("company_names", Context.MODE_PRIVATE);
        SharedPreferences.Editor companyNamesEditor = company_names.edit();


        SharedPreferences portfolioPref = getSharedPreferences("stocks_in_portfolio", MODE_PRIVATE);
        SharedPreferences favoritesPref = getSharedPreferences("stocks_in_favorites", MODE_PRIVATE);
        String stocksPortfolio = portfolioPref.getString("stocks","");
        String stocksFavorites = favoritesPref.getString("stocks","");
        List<String> stockList = new ArrayList<>();
        if(stocksPortfolio.length()>0){
            String[] stocksListPortfolio=stocksPortfolio.substring(0,stocksPortfolio.length()-1).split(",");
            for(int i=0;i<stocksListPortfolio.length;i++){
                stockList.add(stocksListPortfolio[i]);
            }
        }
        if(stocksFavorites.length()>0){
            String[] stocksListFavorites=stocksFavorites.substring(0,stocksFavorites.length()-1).split(",");
            for(int i=0;i<stocksListFavorites.length;i++){
                if(!stockList.contains(stocksListFavorites[i])){
                    stockList.add(stocksListFavorites[i]);
                }
            }
        }


        for(int i=0;i<stockList.size();i++){
            final String url = "http://nodejshw8app.us-east-1.elasticbeanstalk.com/detail/topleft/" + stockList.get(i);
            RequestQueue queue = Volley.newRequestQueue(HomeScreenActivity.this);
            JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        String name = response.getString("name");
                        String ticker = response.getString("ticker");
                        companyNamesEditor.putString(ticker,name);
                        companyNamesEditor.apply();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e("HttpClient", "error: " + error.toString());
                }
            });
            queue.add(getRequest);
        }


    }

    public void getFavoritesData(){
        sectionOneItems = new ArrayList<>();
        SharedPreferences company_names = getSharedPreferences("company_names", Context.MODE_PRIVATE);
        SharedPreferences portfolioSharedPref = getSharedPreferences("portfolio", Context.MODE_PRIVATE);
        SharedPreferences favoritesPref = getSharedPreferences("stocks_in_favorites", MODE_PRIVATE);
        SharedPreferences.Editor favoritesEditor = favoritesPref.edit();
        String stocks = favoritesPref.getString("stocks","");
        String[] stocksList;
        if(stocks.equals("")){
            flag++;
            if(flag==2){
                sectionList.clear();
                sectionList.add((new Section(sectionTwoName,newWorth,sectionTwoItems)));
                sectionList.add((new Section(sectionOneName,newWorth,sectionOneItems)));
                spinnerLayout.setVisibility(View.GONE);
                mainRecyclerView = findViewById(R.id.mainRecyclerView);
                MainRecyclerAdapter mainRecyclerAdapter = new MainRecyclerAdapter(sectionList);
                mainRecyclerView.setLayoutManager(new LinearLayoutManager(HomeScreenActivity.this));
                mainRecyclerView.setAdapter(mainRecyclerAdapter);
                ViewCompat.setNestedScrollingEnabled(mainRecyclerView,false);
            }

        }
        else{
            stocksList=stocks.substring(0,stocks.length()-1).split(",");
            final String url = "http://nodejshw8app.us-east-1.elasticbeanstalk.com/detail/topright/" + stocks.substring(0,stocks.length()-1);
            RequestQueue queue = Volley.newRequestQueue(HomeScreenActivity.this);

            JsonArrayRequest getRequest = new JsonArrayRequest(Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
                @Override
                public void onResponse(JSONArray response) {
                    try {
                        for (int i = 0; i < response.length(); i++) {

                            String ticker = response.getJSONObject(i).getString("ticker");
                            String price = response.getJSONObject(i).getString("last");
                            String prev = response.getJSONObject(i).getString("prevClose");
                            float change = Float.parseFloat(price) - Float.parseFloat(prev);
                            int numShares = portfolioSharedPref.getInt(ticker,0);
                            String trend;
                            if(change>0){
                                trend="increase";
                            } else if(change<0){
                                trend = "decrease";
                            } else{
                                trend = "same";
                            }
                            decimalFormat = new DecimalFormat("####.##");
                            String changeString = decimalFormat.format(change);
                            if(numShares>0){
                                if(numShares==1){
                                    fMap.put(ticker,new StockCard(ticker,Float.parseFloat(price),numShares +".00 share",Float.parseFloat(changeString),trend));
                                }else{
                                    fMap.put(ticker,new StockCard(ticker,Float.parseFloat(price),numShares +".00 shares",Float.parseFloat(changeString),trend));
                                }
                            }else{
                                fMap.put(ticker,new StockCard(ticker,Float.parseFloat(price),company_names.getString(ticker,""),Float.parseFloat(changeString),trend));
                            }
                        }
                        for(int i=0;i<stocksList.length;i++){
                            sectionOneItems.add(fMap.get(stocksList[i]));
                        }
                        //sectionList.add((new Section(sectionOneName,"null",sectionOneItems)));
                        float liquidCash = netWorthSharedPreferences.getFloat("cash",-1);

                        cashEditor.apply();
                        newWorth = Float.toString(liquidCash + sumOfStocksValue);

                        flag++;
                        if(flag==2){
                            sectionList.clear();
                            sectionList.add((new Section(sectionTwoName,newWorth,sectionTwoItems)));
                            sectionList.add((new Section(sectionOneName,newWorth,sectionOneItems)));
                            spinnerLayout.setVisibility(View.GONE);
                            mainRecyclerView = findViewById(R.id.mainRecyclerView);
                            MainRecyclerAdapter mainRecyclerAdapter = new MainRecyclerAdapter(sectionList);
                            mainRecyclerView.setLayoutManager(new LinearLayoutManager(HomeScreenActivity.this));
                            mainRecyclerView.setAdapter(mainRecyclerAdapter);
                            ViewCompat.setNestedScrollingEnabled(mainRecyclerView,false);
                            //mainRecyclerView.addItemDecoration(new DividerItemDecoration(HomeScreenActivity.this,DividerItemDecoration.VERTICAL));
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e("HttpClient", "error: " + error.toString());
                }
            });
            queue.add(getRequest);

        }

    }

    public void getPortfolioData(){
        System.out.println("ouch");
        sectionTwoItems = new ArrayList<>();
        SharedPreferences portfolioSharedPref = getSharedPreferences("portfolio", Context.MODE_PRIVATE);
        SharedPreferences portfolioPref = getSharedPreferences("stocks_in_portfolio", MODE_PRIVATE);
        SharedPreferences.Editor portfolioPrefEditor = portfolioPref.edit();
        String stocks = portfolioPref.getString("stocks","");
        String[] stocksList;
        if(stocks.equals("")){
            flag++;
            cashEditor.putFloat("cash",20000f);
            cashEditor.apply();
            if(flag==2){
                sectionList.clear();
                sectionList.add((new Section(sectionTwoName,newWorth,sectionTwoItems)));
                sectionList.add((new Section(sectionOneName,newWorth,sectionOneItems)));

                spinnerLayout.setVisibility(View.GONE);
                mainRecyclerView = findViewById(R.id.mainRecyclerView);
                MainRecyclerAdapter mainRecyclerAdapter = new MainRecyclerAdapter(sectionList);
                mainRecyclerView.setLayoutManager(new LinearLayoutManager(HomeScreenActivity.this));
                mainRecyclerView.setAdapter(mainRecyclerAdapter);
                ViewCompat.setNestedScrollingEnabled(mainRecyclerView,false);
            }

        }
        else{
            stocksList=stocks.substring(0,stocks.length()-1).split(",");
            final String url = "http://nodejshw8app.us-east-1.elasticbeanstalk.com/detail/topright/" + stocks.substring(0,stocks.length()-1);
            RequestQueue queue = Volley.newRequestQueue(HomeScreenActivity.this);

            JsonArrayRequest getRequest = new JsonArrayRequest(Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
                @Override
                public void onResponse(JSONArray response) {
                    try {
                        sumOfStocksValue=0;
                        for (int i = 0; i < response.length(); i++) {

                            String ticker = response.getJSONObject(i).getString("ticker");
                            String price = response.getJSONObject(i).getString("last");
                            String prev = response.getJSONObject(i).getString("prevClose");
                            float change = Float.parseFloat(price) - Float.parseFloat(prev);
                            String trend;
                            decimalFormat = new DecimalFormat("##.##");
                            String changeString = decimalFormat.format(change);
                            if(change>0){
                                trend="increase";
                            } else if(change<0){
                                trend = "decrease";
                            } else{
                                trend = "same";
                            }
                            int numShares = portfolioSharedPref.getInt(ticker,0);
                            if(numShares==1){
                                pMap.put(ticker,new StockCard(ticker,Float.parseFloat(price),numShares +".00 share",Float.parseFloat(changeString),trend));
                            }else{
                                pMap.put(ticker,new StockCard(ticker,Float.parseFloat(price),numShares +".00 shares",Float.parseFloat(changeString),trend));
                            }
                            sumOfStocksValue +=Float.parseFloat(price)*portfolioSharedPref.getInt(ticker,0);
                        }
                        for(int i=0;i<stocksList.length;i++){
                            sectionTwoItems.add(pMap.get(stocksList[i]));
                        }
                        float liquidCash = netWorthSharedPreferences.getFloat("cash",-1);

                        cashEditor.apply();
                        newWorth = Float.toString(liquidCash + sumOfStocksValue);
//                        System.out.println("net worth"+liquidCash +"a"+ sumOfStocksValue);

//                        mainRecyclerView = findViewById(R.id.mainRecyclerView);
//                        MainRecyclerAdapter mainRecyclerAdapter = new MainRecyclerAdapter(sectionList);
//                        mainRecyclerView.setAdapter(mainRecyclerAdapter);

                        flag++;
                        if(flag==2){
                            sectionList.clear();
                            sectionList.add((new Section(sectionTwoName,newWorth,sectionTwoItems)));
                            sectionList.add((new Section(sectionOneName,newWorth,sectionOneItems)));
                            spinnerLayout.setVisibility(View.GONE);
                            mainRecyclerView = findViewById(R.id.mainRecyclerView);
                            MainRecyclerAdapter mainRecyclerAdapter = new MainRecyclerAdapter(sectionList);
                            mainRecyclerView.setLayoutManager(new LinearLayoutManager(HomeScreenActivity.this));
                            mainRecyclerView.setAdapter(mainRecyclerAdapter);
                            ViewCompat.setNestedScrollingEnabled(mainRecyclerView,false);
                            //mainRecyclerView.addItemDecoration(new DividerItemDecoration(HomeScreenActivity.this,DividerItemDecoration.VERTICAL));
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e("HttpClient", "error: " + error.toString());
                }
            });
            queue.add(getRequest);
        }






















    }


}

//class Update extends TimerTask {
//
//    @Override
//    public void run() {
//        HomeScreenActivity homeScreenActivity = new HomeScreenActivity();
//        homeScreenActivity.sectionList.clear();
//        homeScreenActivity.getPortfolioData();
//        homeScreenActivity.getFavoritesData();
//    }
//}