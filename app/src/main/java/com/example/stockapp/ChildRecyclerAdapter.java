package com.example.stockapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;

public class ChildRecyclerAdapter extends RecyclerView.Adapter<ChildRecyclerAdapter.ViewHolder>
    implements ItemMoveCallback.ItemTouchHelperContract{

    List<StockCard> items;
    int id;
    ViewGroup parent;
    ConstraintLayout stockCardLayout;
    public ChildRecyclerAdapter(List<StockCard> items, int id){
        this.id=id;
        this.items=items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.stock_card,parent,false);
        this.parent=parent;


        return new ViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        StockCard currentCard = items.get(position);
        holder.change.setText(Float.toString(currentCard.getChange()));
        holder.currentPrice.setText(Float.toString(currentCard.getCurrentPrice()));
        holder.ticker.setText(currentCard.getTicker());
        holder.info.setText(currentCard.getInfo());
        if(currentCard.getTrend().equals("increase")){
            holder.trend.setImageResource(R.drawable.increase);
            holder.trend.setColorFilter(Color.parseColor("#00AA00"));
            holder.change.setTextColor(Color.parseColor("#00AA00"));
        }else if(currentCard.getTrend().equals("decrease")){
            holder.trend.setImageResource(R.drawable.decrease);
            holder.trend.setColorFilter(Color.parseColor("#DD0000"));
            holder.change.setTextColor(Color.parseColor("#DD0000"));
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }






    class ViewHolder extends RecyclerView.ViewHolder{

        CardView stockCardView;
        TextView currentPrice, change, info, ticker;
        Button button;
        ImageView trend;
        ConstraintLayout stockCardLayout;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            stockCardView = itemView.findViewById(R.id.stock_card);
            currentPrice = itemView.findViewById(R.id.card_price);
            change = itemView.findViewById(R.id.change);
            info = itemView.findViewById(R.id.card_info);
            ticker = itemView.findViewById(R.id.card_ticker);
            button = itemView.findViewById(R.id.button);
            trend = itemView.findViewById(R.id.trend);
            stockCardLayout = itemView.findViewById(R.id.stockCard);
            this.stockCardLayout=stockCardLayout;
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(parent.getContext(),StockScreenActivity.class);
                    intent.putExtra("ticker",ticker.getText());
                    parent.getContext().startActivity(intent);
                }
            });

            stockCardLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(parent.getContext(),StockScreenActivity.class);
                    intent.putExtra("ticker",ticker.getText());
                    parent.getContext().startActivity(intent);
                }
            });

        }
    }

    @Override
    public void onRowMoved(int fromPosition, int toPosition) {
        SharedPreferences sharedPreferences;

        if(id==1){
            sharedPreferences = parent.getContext().getSharedPreferences("stocks_in_favorites",Context.MODE_PRIVATE);
        }else{
            sharedPreferences = parent.getContext().getSharedPreferences("stocks_in_portfolio",Context.MODE_PRIVATE);
        }
        SharedPreferences.Editor editor = sharedPreferences.edit();

        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(items, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(items, i, i - 1);
            }
        }
        String favsList="";
        for(int i=0;i<items.size();i++){
            favsList+=items.get(i).getTicker()+",";
        }
        //favsList=favsList.substring(0,favsList.length()-1);
        editor.putString("stocks",favsList);
        editor.apply();
        notifyItemMoved(fromPosition, toPosition);
    }


    @Override
    public void onRowSelected(ViewHolder myViewHolder) {
    myViewHolder.stockCardLayout.setBackgroundColor(Color.GRAY);
    myViewHolder.info.setTextColor(Color.parseColor("#5A5A5A"));
    }

    @Override
    public void onRowClear(ViewHolder myViewHolder) {
        myViewHolder.stockCardLayout.setBackgroundColor(Color.parseColor("#f4f2f4")); //light Gray
        myViewHolder.info.setTextColor(Color.parseColor("#808080"));
    }




    public void removeItem(int position) {

        SharedPreferences favorites = parent.getContext().getSharedPreferences("stocks_in_favorites", Context.MODE_PRIVATE);
        SharedPreferences.Editor editorFavorites = favorites.edit();
        String favStocks = favorites.getString("stocks","");
        favStocks=favStocks.replace(items.get(position).getTicker()+",","");
        editorFavorites.putString("stocks",favStocks);
        editorFavorites.apply();


        items.remove(position);
        notifyItemRemoved(position);

    }

    public void restoreItem(StockCard item, int position) {
        items.add(position, item);
        notifyItemInserted(position);
    }

    public List<StockCard> getData() {
        return items;
    }

}
