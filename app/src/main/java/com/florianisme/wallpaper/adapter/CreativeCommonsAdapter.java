package com.florianisme.wallpaper.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.florianisme.wallpaper.R;

public class CreativeCommonsAdapter extends BaseAdapter {

    private final Context mContext;
    private String[] changelog;

    public CreativeCommonsAdapter(Context context) {

        mContext = context;
        changelog = context.getResources().getStringArray(R.array.creative_commons);
    }

    @Override
    public int getCount() {
        return changelog.length;
    }

    @Override
    public String getItem(int position) {
        return changelog[position];
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertView = inflater.inflate(R.layout.creative_commons, parent, false);
        }

        TextView versionContent = (TextView) convertView.findViewById(R.id.creative_content);
        versionContent.setText(changelog[position]);

        return convertView;
    }
}
