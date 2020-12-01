package com.example.stockapp;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

    List<NewsCard> newsCardList;
    HomeScreenActivity homeScreenActivity;
    Dialog myDialog;

    Context context;
    public RecyclerViewAdapter(Context ct, List<NewsCard> newsCardList){
        homeScreenActivity=new HomeScreenActivity();
        context=ct;
        this.newsCardList=newsCardList;
    }

    @NonNull
    @Override

    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(R.layout.news_card,parent,false);

        final ViewHolder viewHolder=new ViewHolder(view);
        context=parent.getContext();
        viewHolder.news_card_layout.setOnLongClickListener(new View.OnLongClickListener(){
            @Override
            public boolean onLongClick(View v) {

                //Toast.makeText(context,"test Click"+String.valueOf(viewHolder.getAdapterPosition()),Toast.LENGTH_SHORT).show();
                myDialog=new Dialog(context);
                myDialog.setContentView(R.layout.dialog_card);
                TextView dialog_title=(TextView) myDialog.findViewById(R.id.dialog_title);
                ImageView dialog_image=(ImageView) myDialog.findViewById(R.id.dialog_image);
                dialog_title.setText(newsCardList.get(viewHolder.getAdapterPosition()).getTitle());
                Picasso.get().load(newsCardList.get(viewHolder.getAdapterPosition()).getImageUrl()).fit().centerInside().into(dialog_image);
                myDialog.show();


                ImageView twitter = myDialog.findViewById(R.id.twitter_image);
                ImageView chrome = myDialog.findViewById(R.id.chrome_image);

                twitter.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse("https://twitter.com/intent/tweet?text="+"Check out this Link: \n"+newsCardList.get(viewHolder.getAdapterPosition()).getNewsUrl()));
                        parent.getContext().startActivity(intent);

                    }
                });

                chrome.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse(newsCardList.get(viewHolder.getAdapterPosition()).getNewsUrl()));
                        parent.getContext().startActivity(intent);
                    }
                });
                return false;
            }
        });

        viewHolder.news_card_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(newsCardList.get(viewHolder.getAdapterPosition()).getNewsUrl()));
                context.startActivity(intent);
            }
        });

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.titleTextView.setText(newsCardList.get(position).getTitle());
        holder.publishedAtTextView.setText(newsCardList.get(position).getDatePublished());
        Picasso.get().load(newsCardList.get(position).getImageUrl()).fit().centerInside().into(holder.imageView);

    }

    @Override
    public int getItemCount() {
        return newsCardList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private ConstraintLayout news_card_layout;
        TextView sourceTextView,titleTextView, publishedAtTextView;
        ImageView imageView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            news_card_layout=(ConstraintLayout) itemView.findViewById((R.id.news_card_layout));
            titleTextView=itemView.findViewById(R.id.titleNewsCard);
            publishedAtTextView=itemView.findViewById(R.id.publishedAt);
            imageView=itemView.findViewById(R.id.newsCardImg);
        }
    }
}
