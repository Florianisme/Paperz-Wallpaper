package com.florianisme.wallpaper.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.florianisme.wallpaper.R;


public class DrawerAdapter extends ArrayAdapter<String> {

    static int selectedItem = -1;
    String[] items;
    int[] icons;
    int[] count;
    Typeface tf;
    int selectionColor;

    public DrawerAdapter(Context context, String[] items, int[] icons, int[] count) {
        super(context, R.layout.drawer_item, items);
        this.items = items;
        this.icons = icons;
        this.count = count;
        tf = Typeface.createFromAsset(context.getAssets(), "Roboto-Medium.ttf");
        selectionColor = context.getResources().getColor(R.color.colorPrimary);
    }

    public static void setSelectedItem(int index) {
        selectedItem = index;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (position != 3) {
            View item = inflater.inflate(R.layout.drawer_item, parent, false);
            TextView textView = (TextView) item.findViewById(R.id.drawer_item_text);
            TextView info = (TextView) item.findViewById(R.id.drawer_item_info);
            ImageView imageView = (ImageView) item.findViewById(R.id.drawer_item_icon);
            LinearLayout ll = (LinearLayout) item.findViewById(R.id.drawer_item);

            info.setText(String.valueOf(count[position]));

            textView.setText(items[position]);
            textView.setTypeface(tf);
            imageView.setImageResource(icons[position]);

            if (position == selectedItem) {
                textView.setTextColor(selectionColor);
                imageView.setColorFilter(Color.parseColor("#FFFFFF"));
                imageView.setColorFilter(selectionColor);
                info.setTextColor(selectionColor);
                item.setSelected(true);
                ll.setBackgroundColor(Color.parseColor("#10000000"));
            }

            return item;
        }
        else {
            return inflater.inflate(R.layout.drawer_divider, parent, false);
        }
        }
}
