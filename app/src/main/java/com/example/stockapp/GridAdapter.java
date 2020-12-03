package com.example.stockapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class GridAdapter extends BaseAdapter {
    private Context context;
    private final String[] list;

    public GridAdapter(Context context,String[] list) {
        this.list = list;
        this.context=context;
    }


    @Override
    public int getCount() {
        return list.length;
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
        LayoutInflater inflater= (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View gridview;
        if(convertView==null)
        {
            gridview= inflater.inflate(R.layout.stats_grid_item_layout,null);
            TextView tv=(TextView) gridview.findViewById(R.id.gridItem);
            tv.setText(list[position]);
            tv.setOnClickListener(null);
        }
        else
        {
            gridview=(View) convertView;
        }
        return gridview;
    }
}

