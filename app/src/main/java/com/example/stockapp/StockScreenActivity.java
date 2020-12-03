package com.example.stockapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;


public class StockScreenActivity extends AppCompatActivity {

    String currentTicker;
    String currentCompany;
    String currentPrice;
    Float amount;
    boolean isCheck = true;
    GridView statsGrid;
    TextView tickerTextView, priceTextView, nameTextView, changeTextView, aboutTextView, sharesOwnedTextView, marketValueTextView;
    TextView firstNewsCardTitleTextView, firstNewsCardPublishedAtView, firstNewsCardSourceTextView, moreless;
    ImageView firstNewsCardImageView;
    RecyclerView recyclerView;
    MenuItem star_empty;
    MenuItem star_filled;
    ConstraintLayout spinnerLayout;
    List<NewsCard> newsCardList = new ArrayList<>();
    MenuItem star;
    SharedPreferences netWorthSharedPreferences;
    SharedPreferences.Editor worthEditor;
    WebView wv;
    int flag=0;


    SharedPreferences portfolioPref;
    SharedPreferences.Editor portfolioPrefEditor;
    SharedPreferences favoritesPref;
    SharedPreferences.Editor favoritesPrefEditor;
    String stocksInPortfolio;
    String stocksInFavorites;

    DecimalFormat decimalFormat;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_screen);

        portfolioPref = getSharedPreferences("stocks_in_portfolio", MODE_PRIVATE);
        favoritesPref = getSharedPreferences("stocks_in_favorites", MODE_PRIVATE);
        portfolioPrefEditor = portfolioPref.edit();
        favoritesPrefEditor = favoritesPref.edit();
        stocksInPortfolio = portfolioPref.getString("stocks","");
        stocksInFavorites = favoritesPref.getString("stocks","");

        netWorthSharedPreferences = getSharedPreferences("uninvested_cash",MODE_PRIVATE);
        worthEditor = netWorthSharedPreferences.edit();

        spinnerLayout = findViewById(R.id.spinnerLayoutStockScreen);

        Intent intent = getIntent();
        Toolbar toolbar= (Toolbar) findViewById(R.id.my_toolbar2);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back_button);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        currentTicker=intent.getStringExtra("ticker");
        tickerTextView = findViewById(R.id.tickerStockScreen);
        priceTextView = findViewById(R.id.currentPriceStockScreen1);
        nameTextView = findViewById(R.id.nameStockScreen);
        changeTextView = findViewById(R.id.changeStockScreen);
        moreless = findViewById(R.id.moreless);

        aboutTextView=findViewById(R.id.aboutStockScreen);
        sharesOwnedTextView = findViewById(R.id.sharesOwnedStockScreen);
        marketValueTextView = findViewById(R.id.marketValueStockScreen);

        firstNewsCardImageView=findViewById(R.id.firstNewsCardImage);
        firstNewsCardSourceTextView=findViewById(R.id.firstNewsCardSource);
        firstNewsCardPublishedAtView=findViewById(R.id.firstNewsCardPublishedAt);
        firstNewsCardTitleTextView=findViewById(R.id.firstNewsCardTitle);

        tickerTextView.setText(currentTicker);

        getStockData();
        func();
        getNews();

        findViewById(R.id.tradeButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String casheLeft = Float.toString(netWorthSharedPreferences.getFloat("cash",0));
                final Dialog myDialog = new Dialog(StockScreenActivity.this);
                myDialog.setContentView(R.layout.trade_dialog);
                TextView title = myDialog.findViewById(R.id.tradeDialogTitle);
                EditText quantity = myDialog.findViewById(R.id.quantityInput);
                TextView calculation = myDialog.findViewById(R.id.calculation);
                TextView tradeText = myDialog.findViewById(R.id.tradeText);

                System.out.println("edit text is"+quantity.getText().toString()+"he"+quantity.getText().toString().length());

                title.setText("Trade "+currentCompany+" shares");
                myDialog.show();
                calculation.setText("0 x $"+currentPrice+"/share = $0.00");
                tradeText.setText("$"+casheLeft+" available to buy "+currentTicker);
                quantity.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }
                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        System.out.println("quan text "+s.toString());
                        if(!s.toString().equals("") && !s.toString().equals(".")){
                            calculation.setText(s+" x $"+currentPrice+"/share = $"+Float.parseFloat(s.toString())*Float.parseFloat(currentPrice));
                            amount=Float.parseFloat(s.toString())*Float.parseFloat(currentPrice);
                        }
                        else{
                            calculation.setText(" x $"+currentPrice+"/share = $0.0");
                        }
                    }
                    @Override
                    public void afterTextChanged(Editable s) {

                    }
                });

                SharedPreferences sharedpreferences = getSharedPreferences("portfolio", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedpreferences.edit();
                Toast toast = Toast.makeText(StockScreenActivity.this, "error", Toast.LENGTH_SHORT);
                SharedPreferences port = getSharedPreferences("port_list",Context.MODE_PRIVATE);
                SharedPreferences.Editor editorPortfolio = port.edit();




                myDialog.findViewById(R.id.buyButton).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        float currentWorth =  netWorthSharedPreferences.getFloat("cash",0);

                        if(quantity.getText().toString().length()==0){
                            toast.setText("Please enter valid amount");
                            toast.show();
                            return;
                        }
                        int q = Integer.parseInt(quantity.getText().toString());
                        if(q>0 && amount<=currentWorth){
                            int updatedQ = sharedpreferences.getInt(currentTicker,0) + q;
                            editor.putInt(currentTicker,updatedQ);
                            editor.apply();
                            setPortfolioData(Float.parseFloat(currentPrice));
                            String portStocks = port.getString("new_fav","");
                            System.out.println("yahu0"+portStocks);
                            currentWorth-=amount;
                            worthEditor.putFloat("cash",currentWorth);
                            worthEditor.apply();
                            if(!stocksInPortfolio.contains(currentTicker)){
                                stocksInPortfolio+=currentTicker+",";
                                portfolioPrefEditor.putString("stocks",stocksInPortfolio);
                                portfolioPrefEditor.apply();
                            }

                            myDialog.dismiss();
                            final Dialog boughtDialog = new Dialog(StockScreenActivity.this);
                            boughtDialog.setContentView(R.layout.dialog_bought);
                            boughtDialog.show();
                            TextView tradeText= boughtDialog.findViewById(R.id.tradeText);
                            tradeText.setText("You have successfully bought "+quantity.getText().toString()+" shares of "+currentTicker);
                            boughtDialog.findViewById(R.id.doneButton).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    boughtDialog.dismiss();
                                }
                            });
                        }
                        else if(q==0){
                            toast.setText("Cannot buy less than 0 shares");
                            toast.show();
                        }
                        else if(amount>currentWorth){
                            toast.setText("Not enough money to buy");
                            toast.show();
                        }
                    }
                });

                myDialog.findViewById(R.id.sellButton).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(quantity.getText().toString().length()==0){
                            toast.setText("Please enter valid amount");
                            toast.show();
                            return;
                        }

                        int q = Integer.parseInt(quantity.getText().toString());
                        int numShares = sharedpreferences.getInt(currentTicker,0);
                        if(q>0 && q<=numShares){
                            int updatedQ = sharedpreferences.getInt(currentTicker,0) - q;
                            editor.putInt(currentTicker,updatedQ);
                            editor.apply();
                            setPortfolioData(Float.parseFloat(priceTextView.getText().toString()));
                            String portStocks = port.getString("new_fav","");
                            float currentWorth =  netWorthSharedPreferences.getFloat("cash",0);
                            currentWorth+=amount;
                            worthEditor.putFloat("cash",currentWorth);
                            worthEditor.apply();
                            if(updatedQ==0){
                                stocksInPortfolio=stocksInPortfolio.replace(currentTicker+",","");
                                portfolioPrefEditor.putString("stocks",stocksInPortfolio);
                                portfolioPrefEditor.apply();
                            }

                            myDialog.dismiss();
                            final Dialog soldDialog = new Dialog(StockScreenActivity.this);
                            soldDialog.setContentView(R.layout.dialog_sold);
                            soldDialog.show();
                            TextView tradeText= soldDialog.findViewById(R.id.tradeText);
                            tradeText.setText("You have successfully sold "+quantity.getText().toString()+" shares of "+currentTicker);

                            soldDialog.findViewById(R.id.doneButton).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    soldDialog.dismiss();
                                }
                            });
                        }
                        else if(q==0){
                            toast.setText("Cannot sell less than 0 shares");
                            toast.show();
                        }
                        else if(q>numShares){
                            System.out.println("numShares"+q+" "+numShares);
                            toast.setText("Not enough shares to sell");
                            toast.show();
                        }
                    }
                });
            }
        });


        wv = (WebView) findViewById(R.id.webView);
        wv.getSettings().setJavaScriptEnabled(true);
        wv.loadUrl("file:///android_asset/chart.html?message="+currentTicker);



    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //SharedPreferences favoritesSharedPref = getSharedPreferences("favorites", Context.MODE_PRIVATE);
        SharedPreferences favoritesSharedPref = getSharedPreferences("stocks_in_favorites", Context.MODE_PRIVATE);

        getMenuInflater().inflate(R.menu.stock_screen_menu_buttons,menu);
        star_empty = menu.findItem(R.id.star_empty);
        star_filled = menu.findItem(R.id.star_filled);
        star_empty.setVisible(false);
        star_filled.setVisible(false);
        if(!favoritesSharedPref.getString("stocks","").contains(currentTicker)){
            star = star_empty;
        }
        else{
            star = star_filled;
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        SharedPreferences fav = getSharedPreferences("fav_list",Context.MODE_PRIVATE);
        SharedPreferences favoritesSharedPref = getSharedPreferences("favorites", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = favoritesSharedPref.edit();
        SharedPreferences.Editor editorFavorites = fav.edit();
        String favStocks = fav.getString("new_fav","");
        Toast toast = Toast.makeText(StockScreenActivity.this, "favorites text", Toast.LENGTH_SHORT);
        switch (item.getItemId()){
            case R.id.star_empty:
                editor.putString(currentTicker,"1");
                editor.apply();

//                if(!favStocks.equals("")){
//                    favStocks+=","+currentTicker;
//                    editorFavorites.putString("new_fav",favStocks);
//                    editorFavorites.apply();
//                }

                if(!stocksInFavorites.contains(currentTicker)){
                    stocksInFavorites+=currentTicker+",";
                    favoritesPrefEditor.putString("stocks",stocksInFavorites);
                    favoritesPrefEditor.apply();
                }

                toast.setText("  \""+currentTicker+"\" was added to favorites");
                toast.show();
                star_empty.setVisible(false);
                star_filled.setVisible(true);
                return true;
            case R.id.star_filled:
                editor.remove(currentTicker);
                editor.apply();
//                if(!favStocks.equals("")) {
//                    favStocks=favStocks.replace(currentTicker+",","");
//                    editorFavorites.putString("new_fav",favStocks);
//                    editorFavorites.apply();
//                }

                stocksInFavorites=stocksInFavorites.replace(currentTicker+",","");
                favoritesPrefEditor.putString("stocks",stocksInFavorites);
                favoritesPrefEditor.apply();

                toast.setText("  \""+currentTicker+"\" was removed from favorites");
                toast.show();
                star_empty.setVisible(true);
                star_filled.setVisible(false);
                return true;
            case android.R.id.home:
                this.finish();
                return true;
            default: return super.onOptionsItemSelected(item);

        }

    }


    private void setPortfolioData(Float price){

        //called from getStockData too
        SharedPreferences sharedpreferences = getSharedPreferences("portfolio", Context.MODE_PRIVATE);
        if(sharedpreferences.getInt(currentTicker,0)==0){
            sharesOwnedTextView.setText("You have 0 shares of "+currentTicker+".");
            marketValueTextView.setText("Start trading!");
        }
        else{
            sharesOwnedTextView.setText("Shares Owned: "+sharedpreferences.getInt(currentTicker,0)+".0000");
            marketValueTextView.setText("Market Value: $"+sharedpreferences.getInt(currentTicker,0) * price);
        }

    }


    private void getStockData(){
        final String url = "http://nodejshw8app.us-east-1.elasticbeanstalk.com/detail/topright/" + currentTicker;
        RequestQueue queue = Volley.newRequestQueue(StockScreenActivity.this);
        JsonArrayRequest getRequest = new JsonArrayRequest(Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                try {
                    String price = response.getJSONObject(0).getString("last");
                    String prev = response.getJSONObject(0).getString("prevClose");
                    String low = response.getJSONObject(0).getString("low");
                    String bidPrice = response.getJSONObject(0).getString("bidPrice");
                    String openPrice = response.getJSONObject(0).getString("open");
                    String mid = response.getJSONObject(0).getString("mid");
                    String high = response.getJSONObject(0).getString("high");
                    String volume = response.getJSONObject(0).getString("volume");

                    priceTextView.setText("$"+price);
                    currentPrice=price;
                    float change = Float.parseFloat(price) - Float.parseFloat(prev);
                    decimalFormat = new DecimalFormat("#,###.##");
                    if(change>0){
                        changeTextView.setTextColor(getResources().getColor(R.color.green));
                        changeTextView.setText("$"+decimalFormat.format(change));
                    } else if(change<0){
                        changeTextView.setTextColor(getResources().getColor(R.color.red));
                        changeTextView.setText("-$"+decimalFormat.format(-1*change));
                    }else{
                        changeTextView.setText("$"+decimalFormat.format(change));
                    }

                    decimalFormat = new DecimalFormat("###.00");
                    Double vol;
                    String volString;
                    if(volume!="null"){
                        vol = Double.parseDouble(volume);
                        volString = decimalFormat.format(vol);
                    }else{
                        volString="0.0";
                    }

                    if(bidPrice==null){
                        System.out.println("hey zero");
                    }else{
                        System.out.println("hey non");
                    }
                    String[] items = {  "Current price: "+(price!="null"?price:"0.0"),
                                        "Low: "+(low!="null"?low:"0.0"),
                                        "Bid Price: "+(bidPrice!="null"?bidPrice:"0.0"),
                                        "Open price: "+(openPrice!="null"?openPrice:"0.0"),
                                        "Mid: "+(mid!="null"?mid:"0.0"),
                                        "High: "+(high!="null"?high:"0.0"),
                                        "Volume: "+(volume!="null"?volString:"0.0")  };

                    statsGrid=findViewById(R.id.statsGrid);
                    GridAdapter gridAdapter = new GridAdapter(getApplicationContext(),items );
                    statsGrid.setAdapter(gridAdapter);

                    setPortfolioData(Float.parseFloat(price));

                    flag++;
                    if(flag==3){
                        spinnerLayout.setVisibility(View.GONE);
                        aboutTextView.post(new Runnable() {
                            @Override
                            public void run() {
                                if(aboutTextView.getLineCount() <2)
                                {
                                    moreless.setVisibility(View.GONE);
                                    aboutTextView.setGravity(Gravity.CENTER);
                                }
                                else
                                {
                                    moreless.setVisibility(View.VISIBLE);
                                    aboutTextView.setMaxLines(2);

                                }
                            }
                        });
                        star.setVisible(true);
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

        getRequest.setRetryPolicy(new RetryPolicy() {
            @Override
            public int getCurrentTimeout() {
                return 5000;
            }

            @Override
            public int getCurrentRetryCount() {
                Log.e("Retry", "retrying");
                return 5000;
            }

            @Override
            public void retry(VolleyError error) throws VolleyError {
                Log.e("Volley error","Volley timeout error for stock stats");
            }
        });




    }

    private void func(){

        final String url = "http://nodejshw8app.us-east-1.elasticbeanstalk.com/detail/topleft/" + currentTicker;
        RequestQueue queue = Volley.newRequestQueue(StockScreenActivity.this);
        JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    String name = response.getString("name");
                    String about=response.getString("description");
                    currentCompany=name;
                    nameTextView.setText(name);
                    aboutTextView.setText(about);

                    flag++;
                    if(flag==3){
                        spinnerLayout.setVisibility(View.GONE);
                        aboutTextView.post(new Runnable() {
                            @Override
                            public void run() {
                                if(aboutTextView.getLineCount() <2)
                                {
                                    moreless.setVisibility(View.GONE);
                                    aboutTextView.setGravity(Gravity.CENTER);
                                }
                                else
                                {
                                    moreless.setVisibility(View.VISIBLE);
                                    aboutTextView.setMaxLines(2);

                                }
                            }
                        });
                        star.setVisible(true);
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
        getRequest.setRetryPolicy(new RetryPolicy() {
            @Override
            public int getCurrentTimeout() {
                return 5000;
            }

            @Override
            public int getCurrentRetryCount() {
                Log.e("Retry", "retrying");
                return 5000;
            }

            @Override
            public void retry(VolleyError error) throws VolleyError {
                Log.e("Volley error","Volley timeout error for stock info");
            }
        });




    }


    private void getNews(){

        final String url = "http://nodejshw8app.us-east-1.elasticbeanstalk.com/news/" + currentTicker;
        RequestQueue queue = Volley.newRequestQueue(StockScreenActivity.this);
        JsonArrayRequest getRequest = new JsonArrayRequest(Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                try {
                    String publishedAtFirstCard = calculatePublishedTime(response.getJSONObject(0).getString("publishedAt"));
                    String sourceFirstCard= response.getJSONObject(0).getJSONObject("source").getString("name");
                    firstNewsCardTitleTextView.setText(response.getJSONObject(0).getString("title"));
                    firstNewsCardPublishedAtView.setText(publishedAtFirstCard);
                    firstNewsCardSourceTextView.setText(sourceFirstCard);

                    Picasso.get().load(response.getJSONObject(0).getString("urlToImage")).into(firstNewsCardImageView);
                    setOnLongClickListenerForFirstNewsCard(
                            response.getJSONObject(0).getString("title"),
                            response.getJSONObject(0).getString("urlToImage"),
                            response.getJSONObject(0).getString("url")
                    );
                    setOnClickListnerForFirstCard(
                            response.getJSONObject(0).getString("url")
                    );

                    for(int i=1;i<response.length();i++){
                        String publishedAt = response.getJSONObject(i).getString("publishedAt");
                        String urlToImage = response.getJSONObject(i).getString("urlToImage");
                        String title = response.getJSONObject(i).getString("title");
                        String newsUrl = response.getJSONObject(i).getString("url");
                        String source = response.getJSONObject(i).getJSONObject("source").getString("name");


                        String publishedAtString = calculatePublishedTime(publishedAt);
                        newsCardList.add(new NewsCard(title,urlToImage,source,publishedAtString, newsUrl));
                    }

                    recyclerView=findViewById(R.id.newsRecyclerView);
                    RecyclerViewAdapter recyclerViewAdapter = new RecyclerViewAdapter(StockScreenActivity.this,newsCardList);
                    recyclerView.setAdapter(recyclerViewAdapter);
                    recyclerView.setLayoutManager(new LinearLayoutManager(StockScreenActivity.this));
//                    recyclerView.addItemDecoration(new DividerItemDecoration(StockScreenActivity.this,DividerItemDecoration.VERTICAL));
                    System.out.println("current ticker"+currentTicker);
                    flag++;
                    if(flag==3){
                        spinnerLayout.setVisibility(View.GONE);
                        aboutTextView.post(new Runnable() {
                            @Override
                            public void run() {
                                if(aboutTextView.getLineCount() <2)
                                {
                                    moreless.setVisibility(View.GONE);
                                    aboutTextView.setGravity(Gravity.CENTER);
                                }
                                else
                                {
                                    moreless.setVisibility(View.VISIBLE);
                                    aboutTextView.setMaxLines(2);

                                }
                            }
                        });
                        star.setVisible(true);
                    }


                    moreless.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View view) {
                            if (isCheck) {
                                aboutTextView.setMaxLines(30);
                                isCheck = false;
                                moreless.setText("Show less");
                            } else {
                                aboutTextView.setMaxLines(2);
                                isCheck = true;
                                moreless.setText("Show more...");
                            }
                        }
                    });



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
        getRequest.setRetryPolicy(new RetryPolicy() {
            @Override
            public int getCurrentTimeout() {
                return 5000;
            }

            @Override
            public int getCurrentRetryCount() {
                Log.e("Retry", "retrying");
                return 5000;
            }

            @Override
            public void retry(VolleyError error) throws VolleyError {
                Log.e("Volley error","Volley timeout error for news");
            }
        });

    }

    private String calculatePublishedTime(String publishedAt) throws ParseException {
        String publishedAtString;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        long difference = System.currentTimeMillis() - simpleDateFormat.parse(publishedAt).getTime();
        difference=difference/60000;
        publishedAtString = difference+ " minutes ago";
        if(difference>60){
            difference/=60;
            publishedAtString = difference+ " hours ago";
        }
        if(difference>=24){
            difference/=24;
            if(difference<2){
                publishedAtString = difference + " day ago";
            }
            else{
                publishedAtString = difference + " days ago";
            }
        }
        return publishedAtString;
    }

    private void setOnLongClickListenerForFirstNewsCard(String title, String imageUrl, String newsUrl){
        findViewById(R.id.firstNewsCardLayout).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                final Dialog myDialog = new Dialog(StockScreenActivity.this);
                myDialog.setContentView(R.layout.dialog_card);
                TextView dialog_title=(TextView) myDialog.findViewById(R.id.dialog_title);
                ImageView dialog_image=(ImageView) myDialog.findViewById(R.id.dialog_image);
                dialog_title.setText(title);
                Picasso.get().load(imageUrl).fit().centerInside().into(dialog_image);
                myDialog.show();

                ImageView twitter = myDialog.findViewById(R.id.twitter_image);
                ImageView chrome = myDialog.findViewById(R.id.chrome_image);

                twitter.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse("https://twitter.com/intent/tweet?text="+"Check out this Link: \n"+newsUrl));
                        startActivity(intent);

                    }
                });

                chrome.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse(newsUrl));
                        startActivity(intent);
                    }
                });


                return false;
            }
        });
    }

    private void setOnClickListnerForFirstCard(String newsUrl){
        findViewById(R.id.firstNewsCardLayout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(newsUrl));
                startActivity(intent);
            }
        });
    }

}