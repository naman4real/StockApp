package com.example.stockapp;

public class StockCard {
    private String ticker;
    private float currentPrice;
    private String info;
    private float change;
    String trend;

    public StockCard(String ticker, float currentPrice, String info, float change, String trend){
        this.ticker=ticker;
        this.currentPrice=currentPrice;
        this.info=info;
        this.change=change;
        this.trend=trend;
    }

    public String getTicker() {
        return ticker;
    }

    public float getCurrentPrice() {
        return currentPrice;
    }

    public String getInfo() {
        return info;
    }

    public float getChange() {
        return change;
    }

    public String getTrend(){return trend;}
}
