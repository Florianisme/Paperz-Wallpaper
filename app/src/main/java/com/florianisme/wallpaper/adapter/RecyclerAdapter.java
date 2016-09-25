package com.florianisme.wallpaper.adapter;


import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.florianisme.wallpaper.BuildConfig;
import com.florianisme.wallpaper.DetailWallpaperActivity;
import com.florianisme.wallpaper.R;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {

    ArrayList<String> title = new ArrayList<>();
    ArrayList<String> icon = new ArrayList<>();
    ArrayList<String> author = new ArrayList<>();
    boolean logging;
    final static String TAG = "recycler";

    Context context;

    public RecyclerAdapter(Context context, ArrayList<String> title, ArrayList<String> icon, ArrayList<String> author) {
        super();
        this.context = context;
        this.title = title;
        this.icon = icon;
        this.author = author;
        logging = BuildConfig.DEBUG;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent,int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.wallpaper_preview, parent, false);
        return new ViewHolder(v);
    }

    @Override public void onBindViewHolder(final ViewHolder holder, final int position) {

        if (title.size() != 0) {
            holder.title.setText(title.get(position));

            holder.progressBar.setVisibility(View.VISIBLE);
            holder.error.setVisibility(View.INVISIBLE);

            Picasso.with(context)
                    .setLoggingEnabled(logging);
            Picasso.with(context)
                    .setIndicatorsEnabled(logging);

                Picasso.with(context)
                        .load(icon.get(position))
                        .resize(320, 320)
                        .centerCrop()
                        .tag(TAG)
                        .config(Bitmap.Config.RGB_565)
                        .noFade()
                        .priority(Picasso.Priority.NORMAL)
                        .into(holder.icon, new com.squareup.picasso.Callback() {
                            @Override
                            public void onSuccess() {
                                holder.error.setVisibility(View.INVISIBLE);
                                if (holder.progressBar != null)
                                    holder.progressBar.setVisibility(View.GONE);
                            }

                            @Override
                            public void onError() {
                                holder.error.setVisibility(View.VISIBLE);
                                holder.error.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_connection_issue));
                                if (holder.progressBar != null)
                                    holder.progressBar.setVisibility(View.GONE);
                            }
                        });

            holder.v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Picasso.with(context).pauseTag(TAG);

                    Intent intent = new Intent(context, DetailWallpaperActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("wallpaper", icon.get(position));
                    intent.putExtra("title", title.get(position));
                    intent.putExtra("author", author.get(position));
                    context.startActivity(intent);
                }
            });
        }
}

    public static void categoryChanged(Context context) {
        Picasso.with(context).cancelTag(TAG);
    }

    @Override public int getItemCount() {
                return title.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        ImageView icon;
        ImageView error;
        ProgressBar progressBar;
        View v;
                public ViewHolder(View v) {
                        super(v);
                    this.v = v;
                    title = (TextView) v.findViewById(R.id.cards_title);
                    icon = (ImageView) v.findViewById(R.id.cards_icon);
                    error = (ImageView) v.findViewById(R.id.cards_error);
                    progressBar = (ProgressBar) v.findViewById(R.id.progress);
                    }
            }
    }

