package com.example.stockapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
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
import android.os.Bundle;
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

    Timer timer;
    ConstraintLayout spinnerLayout;
    Map<String, StockCard> pMap= new HashMap<>();
    Map<String, StockCard> fMap= new HashMap<>();
    SharedPreferences netWorthSharedPreferences;
    SharedPreferences.Editor cashEditor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sectionOneItems = new ArrayList<>();
        sectionTwoItems = new ArrayList<>();

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

        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Log.d("now","run now");
                flag=0;
                getPortfolioData();
                getFavoritesData();
            }
        },0,15000);
    }


    @Override
    protected void onPause() {
        super.onPause();
        timer.cancel();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        spinnerLayout.setVisibility(View.VISIBLE);
        //sectionList.clear();
        //getUpdatedDataPortfolio();
        getNames();
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Log.d("now","run now");
                flag=0;
                getPortfolioData();
                getFavoritesData();
            }
        },0,15000);
        //getPortfolioData();
        //getUpdatedDataFavorites();
        //getFavoritesData();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search_button,menu);
        MenuItem menuItem = menu.findItem(R.id.search);
        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setQueryHint("");
        searchView.setIconified(false);
        final AutoCompleteTextView autoCompleteTextView = searchView.findViewById(androidx.appcompat.R.id.search_src_text);
        autoSuggestAdapter = new AutoSuggestAdapter(HomeScreenActivity.this, android.R.layout.simple_dropdown_item_1line);

        autoCompleteTextView.setThreshold(3);
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

    private void getUpdatedDataFavorites(){
        String sectionOneName="FAVORITES";
        SharedPreferences favoritesSharedPref = getSharedPreferences("favorites", Context.MODE_PRIVATE);
        SharedPreferences portlofioSharedPref = getSharedPreferences("portfolio", Context.MODE_PRIVATE);
        SharedPreferences ticker_name = getSharedPreferences("ticker_name", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = ticker_name.edit();

        Map<String,?> keys = favoritesSharedPref.getAll();
        List<StockCard> sectionOneItems = new ArrayList<>();
        favorites = new ArrayList<>();
        SharedPreferences fav = getSharedPreferences("fav_list",Context.MODE_PRIVATE);
        String json = fav.getString("new_fav", "");
        String[] stocksList = json.split(",");



        if(json.length()>0){

            for(int i=0;i<stocksList.length;i++){
                if(!stocksList[i].equals("")){
                    favorites.add(stocksList[i]);
                }
            }
        }
        else{

            for(Map.Entry<String,?> entry : keys.entrySet()){

                favorites.add(entry.getKey());
            }
        }
        String stocks = "";

        for(int i=0;i<favorites.size();i++){
            stocks+=favorites.get(i) + ",";
            String ticker = favorites.get(i);
            final String url = "http://nodejshw8app.us-east-1.elasticbeanstalk.com/detail/topleft/" + favorites.get(i);
            RequestQueue queue = Volley.newRequestQueue(HomeScreenActivity.this);
            JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        String name = response.getString("name");
                        System.out.println(ticker+"andv"+name);
                        editor.putString(ticker,name);
                        editor.apply();
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

        stocks = stocks.substring(0,stocks.length()-1);

        System.out.println("jaare"+stocks);
        final String url = "http://nodejshw8app.us-east-1.elasticbeanstalk.com/detail/topright/" + stocks;
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
                        int numShares = portlofioSharedPref.getInt(ticker,0);
                        String trend;
                        trend = change>0?"increase":"decrease";
                        if(numShares>0){
                            //sectionOneItems.add( new StockCard(ticker,Float.parseFloat(price),numShares +" shares",change));
                            fMap.put(ticker,new StockCard(ticker,Float.parseFloat(price),numShares +".0 shares",change,trend));
                        }else{

                            //sectionOneItems.add( new StockCard(ticker,Float.parseFloat(price),ticker_name.getString(ticker,""),change));
                            fMap.put(ticker,new StockCard(ticker,Float.parseFloat(price),ticker_name.getString(ticker,""),change,trend));
                        }
                    }

                    for(int i=0;i<favorites.size();i++){
                        sectionOneItems.add(fMap.get(favorites.get(i)));
                    }

                    sectionList.add((new Section(sectionOneName,"null",sectionOneItems)));

                    mainRecyclerView = findViewById(R.id.mainRecyclerView);
                    MainRecyclerAdapter mainRecyclerAdapter = new MainRecyclerAdapter(sectionList);
                    mainRecyclerView.setLayoutManager(new LinearLayoutManager(HomeScreenActivity.this));
                    mainRecyclerView.setAdapter(mainRecyclerAdapter);
                    ViewCompat.setNestedScrollingEnabled(mainRecyclerView,false);
                    mainRecyclerView.addItemDecoration(new DividerItemDecoration(HomeScreenActivity.this,DividerItemDecoration.VERTICAL));

                    spinnerLayout.setVisibility(View.GONE);

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

    private void getUpdatedDataPortfolio(){
        String sectionTwoName="PORTFOLIO";

        SharedPreferences favoritesSharedPref = getSharedPreferences("portfolio", Context.MODE_PRIVATE);
        Map<String, Integer> keys = (Map<String, Integer>) favoritesSharedPref.getAll();
        List<StockCard> sectionTwoItems = new ArrayList<>();
        portfolio = new ArrayList<String>();

        SharedPreferences port = getSharedPreferences("port_list",Context.MODE_PRIVATE);
        String json = port.getString("new_fav", "");
        String[] stocksList = json.split(",");


//        if(json.length()>0){
//            for(int i=0;i<stocksList.length;i++){
//                if(!stocksList[i].equals("")){
//                    portfolio.add(stocksList[i]);
//                }
//            }
//        }
        //else{
            for(Map.Entry<String,Integer> entry : keys.entrySet()){
                if(entry.getValue()>0){
                    portfolio.add(entry.getKey());
                }
            }
        //}

        String stocks = "";
        for(int i=0;i<portfolio.size();i++){
            stocks+=portfolio.get(i) + ",";
        }
        if(stocks.length()>0){
            stocks = stocks.substring(0,stocks.length()-1);
            final String url = "http://nodejshw8app.us-east-1.elasticbeanstalk.com/detail/topright/" + stocks;
            RequestQueue queue = Volley.newRequestQueue(HomeScreenActivity.this);

            JsonArrayRequest getRequest = new JsonArrayRequest(Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
                @Override
                public void onResponse(JSONArray response) {
                    try {
                        float sumOfStocksValue=0;
                        for (int i = 0; i < response.length(); i++) {

                            String ticker = response.getJSONObject(i).getString("ticker");
                            String price = response.getJSONObject(i).getString("last");
                            String prev = response.getJSONObject(i).getString("prevClose");
                            float change = Float.parseFloat(price) - Float.parseFloat(prev);
                            String trend;
                            trend = change>0?"increase":"decrease";
                            pMap.put(ticker,new StockCard(ticker,Float.parseFloat(price),favoritesSharedPref.getInt(ticker,0) + " shares",change,trend));
                            sumOfStocksValue +=Float.parseFloat(price)*favoritesSharedPref.getInt(ticker,0);
                            //sectionTwoItems.add( new StockCard(ticker,Float.parseFloat(price),favoritesSharedPref.getInt(ticker,0) + " shares",change));
                        }
                        for(int i=0;i<portfolio.size();i++){
                            sectionTwoItems.add(pMap.get(portfolio.get(i)));
                        }
                        float liquidCash = netWorthSharedPreferences.getFloat("cash",-1);
//                        SharedPreferences netWorthSharedPref = getSharedPreferences("net_worth",MODE_PRIVATE);
//                        SharedPreferences.Editor editor = netWorthSharedPref.edit();
//                        float netWorth = netWorthSharedPref.getFloat("net_worth",-1);
//                        if(worth+sumOfStocksValue > netWorth){
//                            editor.putFloat("net_worth",worth + sumOfStocksValue);
//                        }

                        cashEditor.apply();
                        //editor.apply();
                        //String newWorth = Float.toString(netWorthSharedPreferences.getFloat("net_worth",-1));
                        String newWorth = Float.toString(liquidCash + sumOfStocksValue);
                        System.out.println("net worth"+liquidCash +"a"+ sumOfStocksValue);
                        sectionList.add((new Section(sectionTwoName,newWorth,sectionTwoItems)));
                        mainRecyclerView = findViewById(R.id.mainRecyclerView);
                        MainRecyclerAdapter mainRecyclerAdapter = new MainRecyclerAdapter(sectionList);
                        mainRecyclerView.setAdapter(mainRecyclerAdapter);

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
        else{

            cashEditor.putFloat("cash",20000f);
            cashEditor.apply();
            sectionList.add((new Section(sectionTwoName,"20000.00",sectionTwoItems)));

            mainRecyclerView = findViewById(R.id.mainRecyclerView);
            MainRecyclerAdapter mainRecyclerAdapter = new MainRecyclerAdapter(sectionList);
            mainRecyclerView.setAdapter(mainRecyclerAdapter);
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
            sectionList.add((new Section(sectionOneName,"null",sectionOneItems)));
            mainRecyclerView = findViewById(R.id.mainRecyclerView);
            MainRecyclerAdapter mainRecyclerAdapter = new MainRecyclerAdapter(sectionList);
            mainRecyclerView.setLayoutManager(new LinearLayoutManager(HomeScreenActivity.this));
            mainRecyclerView.setAdapter(mainRecyclerAdapter);
            ViewCompat.setNestedScrollingEnabled(mainRecyclerView,false);
            //mainRecyclerView.addItemDecoration(new DividerItemDecoration(HomeScreenActivity.this,DividerItemDecoration.VERTICAL));
            spinnerLayout.setVisibility(View.GONE);
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
        sectionTwoItems = new ArrayList<>();
        SharedPreferences portfolioSharedPref = getSharedPreferences("portfolio", Context.MODE_PRIVATE);
        SharedPreferences portfolioPref = getSharedPreferences("stocks_in_portfolio", MODE_PRIVATE);
        SharedPreferences.Editor portfolioPrefEditor = portfolioPref.edit();
        String stocks = portfolioPref.getString("stocks","");
        String[] stocksList;
        if(stocks.equals("")){
            cashEditor.putFloat("cash",20000f);
            cashEditor.apply();
            sectionList.add((new Section(sectionTwoName,"20000.00",sectionTwoItems)));

            mainRecyclerView = findViewById(R.id.mainRecyclerView);
            MainRecyclerAdapter mainRecyclerAdapter = new MainRecyclerAdapter(sectionList);
            mainRecyclerView.setAdapter(mainRecyclerAdapter);
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