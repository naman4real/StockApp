package com.example.stockapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class GridViewAdapter extends BaseAdapter {
    Context context;
    String[] items;
    String[] itemValues;
    LayoutInflater inflater;
    public GridViewAdapter(Context context, String[] items, String[] itemValues){
        this.context=context;
        this.items=items;
        this.itemValues=itemValues;
        inflater=(LayoutInflater.from(context));
    }

    @Override
    public int getCount() {
        return items.length;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
//        convertView = inflater.inflate(R.layout.stats_grid_item_layout, null);
//        TextView heading = (TextView) convertView.findViewById(R.id.heading);
//        TextView value = (TextView) convertView.findViewById(R.id.value);
//        heading.setText(items[position]);
//        value.setText(itemValues[position]);
        return convertView;
    }
}
