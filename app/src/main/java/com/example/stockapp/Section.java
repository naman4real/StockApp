package com.example.stockapp;

import java.util.ArrayList;
import java.util.List;

public class Section {

    private String sectionName;
    private String netWorth;
    private List<StockCard> sectionItems;


    public Section(String sectionName, String netWorth ,List<StockCard> sectionItems) {
        this.sectionName = sectionName;
        this.netWorth = netWorth;
        this.sectionItems = sectionItems;
    }

    public String getSectionName() {
        return sectionName;
    }

    public String getNetWorth(){ return  netWorth; }

    public void setSectionName(String sectionName) {
        this.sectionName = sectionName;
    }

    public List<StockCard> getSectionItems() {
        return sectionItems;
    }

    public void setSectionItems(List<StockCard> sectionItems) {
        this.sectionItems = sectionItems;
    }

    public void emptySectionItems(){
        this.sectionItems = new ArrayList<>();
    }

    public void addStockCard(StockCard card){
        this.sectionItems.add(card);
    }

    @Override
    public String toString() {
        return "Section{" +
                "sectionName='" + sectionName + '\'' +
                ", sectionItems=" + sectionItems +
                '}';
    }
}
