package com.example.stockapp;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import java.util.List;

public class FavoritesRecyclerAdapter extends RecyclerView.Adapter<FavoritesRecyclerAdapter.ViewHolder> {

    List<Section> sectionList;
    RecyclerView childRecyclerView;
    CoordinatorLayout coordinatorLayout;
    ViewGroup currentParent;
    public FavoritesRecyclerAdapter(List<Section> sectionList){
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

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Section section = sectionList.get(position);
        String sectionName = section.getSectionName();
        List<StockCard> items = section.getSectionItems();
        holder.sectionName.setText(sectionName);
        int id=-1;
        if(sectionName=="FAVORITES"){
            id=1;
        }else{
            id=0;
        }
        ChildRecyclerAdapter childRecyclerAdapter = new ChildRecyclerAdapter(items,id);
        enableSwipeToDeleteAndUndo(childRecyclerAdapter);
        ItemTouchHelper.Callback callback = new ItemMoveCallback(childRecyclerAdapter);
        ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(childRecyclerView);

        holder.childRecyclerView.setAdapter(childRecyclerAdapter);
    }

    @Override
    public int getItemCount() {
        return sectionList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView sectionName;
        RecyclerView childRecyclerView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            System.out.println("hello I am naman");
            sectionName=itemView.findViewById(R.id.sectionName);
            childRecyclerView=itemView.findViewById(R.id.childRecyclerView);
        }
    }

    private void enableSwipeToDeleteAndUndo(ChildRecyclerAdapter mAdapter) {
        SwipeToDeleteCallback swipeToDeleteCallback = new SwipeToDeleteCallback(currentParent.getContext()) {
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {


                final int position = viewHolder.getAdapterPosition();
                final StockCard item = mAdapter.getData().get(position);

                mAdapter.removeItem(position);


                Snackbar snackbar = Snackbar
                        .make(coordinatorLayout, "Item was removed from the list.", Snackbar.LENGTH_LONG);
                snackbar.setAction("UNDO", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        mAdapter.restoreItem(item, position);
                        childRecyclerView.scrollToPosition(position);
                    }
                });

                snackbar.setActionTextColor(Color.YELLOW);
                snackbar.show();

            }
        };

        ItemTouchHelper itemTouchhelper = new ItemTouchHelper(swipeToDeleteCallback);
        itemTouchhelper.attachToRecyclerView(childRecyclerView);
    }
}
