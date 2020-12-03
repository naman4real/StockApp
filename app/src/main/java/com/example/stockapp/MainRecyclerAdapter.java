package com.example.stockapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;

public class MainRecyclerAdapter extends RecyclerView.Adapter<MainRecyclerAdapter.ViewHolder> {

    List<Section> sectionList;
    RecyclerView childRecyclerView;
    ViewGroup currentParent;
    View view;
    int id;
    public MainRecyclerAdapter(List<Section> sectionList){
        this.sectionList=sectionList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        System.out.println("on create view holder");
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.section_row,parent,false);
        childRecyclerView=view.findViewById(R.id.childRecyclerView);
        currentParent=parent;
        this.view=view;

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Section section = sectionList.get(position);
        String netWorth = section.getNetWorth();
        String sectionName = section.getSectionName();
        List<StockCard> items = section.getSectionItems();

        holder.sectionName.setText(sectionName);
        holder.netWorth.setText(String.format("%.2f",Float.parseFloat(netWorth)));

        id=-1;
        if(sectionName=="FAVORITES"){
            id=1;
            TextView netWorthTextView = view.findViewById(R.id.netWorth);
            netWorthTextView.setVisibility(View.GONE);
            TextView netWorthValue = view.findViewById(R.id.netWorthValue);
            netWorthValue.setVisibility(View.GONE);
        }else{
            id=0;
        }
        ChildRecyclerAdapter childRecyclerAdapter = new ChildRecyclerAdapter(items,id);
        if(id==1){
            enableSwipeToDeleteAndUndo(childRecyclerAdapter);
        }
        ItemTouchHelper.Callback callback = new ItemMoveCallback(childRecyclerAdapter);
        ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(childRecyclerView);

        holder.childRecyclerView.setAdapter(childRecyclerAdapter);
        childRecyclerView.addItemDecoration(new DividerItemDecoration(currentParent.getContext(),DividerItemDecoration.VERTICAL));
    }

    public void clear(){
        int size = sectionList.size();
        sectionList.clear();
        notifyItemRangeChanged(0,size);
    }

    @Override
    public int getItemCount() {
        return sectionList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView sectionName;
        TextView netWorth;
        RecyclerView childRecyclerView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            sectionName=itemView.findViewById(R.id.sectionName);
            netWorth = itemView.findViewById(R.id.netWorthValue);
            childRecyclerView=itemView.findViewById(R.id.childRecyclerView);
        }
    }


    private void enableSwipeToDeleteAndUndo(ChildRecyclerAdapter mAdapter) {
        SwipeToDeleteCallback swipeToDeleteCallback = new SwipeToDeleteCallback(currentParent.getContext(),mAdapter) {
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {

                final int position = viewHolder.getAdapterPosition();
                final StockCard item = mAdapter.getData().get(position);
                //item.get

                mAdapter.removeItem(position);

            }
        };

        ItemTouchHelper itemTouchhelper = new ItemTouchHelper(swipeToDeleteCallback);
        itemTouchhelper.attachToRecyclerView(childRecyclerView);
    }


}
