package com.example.stockapp;

public class NewsCard {

    private String title;
    private String image_url;
    private String source;
    private String publishedAt;
    private String newsUrl;

    public NewsCard(String title, String image_url, String source, String publishedAt, String newsUrl){
        this.title=title;
        this.image_url=image_url;
        this.source=source;
        this.publishedAt=publishedAt;
        this.newsUrl = newsUrl;
    }

    public String getImageUrl(){
        return image_url;
    }
    public String getTitle(){
        return title;
    }
    public String getDatePublished(){
        return publishedAt;
    }
    public String getSource(){
        return source;
    }
    public String getNewsUrl(){return newsUrl;}

}
