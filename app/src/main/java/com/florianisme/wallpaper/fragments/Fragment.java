package com.florianisme.wallpaper.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.florianisme.wallpaper.R;
import com.florianisme.wallpaper.adapter.RecyclerAdapter;
import com.florianisme.wallpaper.helper.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class Fragment extends android.support.v4.app.Fragment {

    static int position;

    ArrayList<String> title = new ArrayList<>();
    ArrayList<String> icon = new ArrayList<>();
    ArrayList<String> author = new ArrayList<>();

    public Fragment() {
        // Required empty public constructor
    }

    public static Fragment newInstance(int drawerPosition) {
        position = drawerPosition;
        return new Fragment();
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkOnline();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_wallpaper, container, false);
        RecyclerView recyclerView = (RecyclerView) v.findViewById(R.id.recycler_view);
        title.clear();
        icon.clear();
        author.clear();
        getData(position);
        RecyclerAdapter recyclerAdapter = new RecyclerAdapter(getActivity(), title, icon, author);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 2, GridLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(gridLayoutManager);

        recyclerView.setAdapter(recyclerAdapter);
        recyclerAdapter.notifyDataSetChanged();
        return v;
    }

    private void getData(int position) {
        String file = readJSON();

        try {
            // All
            if (position == 0) {
                JSONObject jsonObject = new JSONObject(file);
                JSONArray jsonArray = jsonObject.getJSONArray("categories");
                for (int i = 0; i < jsonArray.length(); i++) {
                    jsonObject = jsonArray.getJSONObject(i);
                    JSONArray jsonArrayWallpaper = jsonObject.getJSONArray("wallpaper");

                    for (int ii = 0; ii < jsonArrayWallpaper.length(); ii++) {
                        icon.add(jsonArrayWallpaper.getJSONObject(ii).getString("url"));
                        title.add(jsonArrayWallpaper.getJSONObject(ii).getString("title"));
                        author.add(jsonArrayWallpaper.getJSONObject(ii).getString("author"));
                    }
                }
                return;
            }

            // New
            else if (position == 1) {
                int version = 0;
                try {
                    version = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0).versionCode;
                }
                catch (PackageManager.NameNotFoundException ex) {
                    ex.printStackTrace();
                }
                JSONObject jsonObject = new JSONObject(file);
                JSONArray jsonArray = jsonObject.getJSONArray("categories");
                for (int i = 0; i < jsonArray.length(); i++) {
                    jsonObject = jsonArray.getJSONObject(i);
                    JSONArray jsonArrayWallpaper = jsonObject.getJSONArray("wallpaper");

                    for (int ii = 0; ii < jsonArrayWallpaper.length(); ii++) {
                        if (jsonArrayWallpaper.getJSONObject(ii).getInt("version_added") == version) {
                            icon.add(jsonArrayWallpaper.getJSONObject(ii).getString("url"));
                            title.add(jsonArrayWallpaper.getJSONObject(ii).getString("title"));
                            author.add(jsonArrayWallpaper.getJSONObject(ii).getString("author"));
                        }
                    }
                }
                return;
            }

            // Favorites
            else if (position == 2) {
                JSONObject jsonObject = new JSONObject(file);
                JSONArray jsonArray = jsonObject.getJSONArray("categories");
                SharedPreferences sharedPreferences = new SharedPreferences(getActivity());
                for (int i = 0; i < jsonArray.length(); i++) {
                    jsonObject = jsonArray.getJSONObject(i);
                    JSONArray jsonArrayWallpaper = jsonObject.getJSONArray("wallpaper");

                    for (int ii = 0; ii < jsonArrayWallpaper.length(); ii++) {
                        if (sharedPreferences.getBoolean(jsonArrayWallpaper.getJSONObject(ii).getString("title").toLowerCase().replaceAll(" ", "_").trim(), false)) {
                            icon.add(jsonArrayWallpaper.getJSONObject(ii).getString("url"));
                            title.add(jsonArrayWallpaper.getJSONObject(ii).getString("title"));
                            author.add(jsonArrayWallpaper.getJSONObject(ii).getString("author"));
                        }
                    }
                }
                return;
            }

            // All other categories
            else {
                JSONObject jsonObject = new JSONObject(file);
                JSONArray jsonArray = jsonObject.getJSONArray("categories");
                jsonObject = jsonArray.getJSONObject(position - 4);
                jsonArray = jsonObject.getJSONArray("wallpaper");

                for (int i = 0; i < jsonArray.length(); i++) {
                    icon.add(i, jsonArray.getJSONObject(i).getString("url"));
                    title.add(i, jsonArray.getJSONObject(i).getString("title"));
                    author.add(i, jsonArray.getJSONObject(i).getString("author"));
                }
            }
        }
        catch (JSONException ex) {
            ex.printStackTrace();
        }
    }

    private String readJSON() {
        try {
            InputStream is = getActivity().getAssets().open("wallpaper.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            return new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return "";
    }

    private void checkOnline() {
        final ConnectivityManager connMgr = (ConnectivityManager)
                getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        final android.net.NetworkInfo wifi = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        final android.net.NetworkInfo mobile = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        if (mobile.isConnectedOrConnecting ()) {
            final SharedPreferences sharedPreferences = new SharedPreferences(getActivity());
            if (sharedPreferences.getBoolean("check_mobile_data", true)) {
                AlertDialog.Builder alertDialogBuilder =
                        new AlertDialog.Builder(getActivity())
                                .setTitle(R.string.mobile_data)
                                .setMessage(R.string.mobile_data_warning)
                                .setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                        sharedPreferences.saveBoolean("check_mobile_data", false);
                                    }
                                });
                alertDialogBuilder.show();
            }
        }

        else if (!wifi.isConnectedOrConnecting()){
            AlertDialog.Builder alertDialogBuilder =
                new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.no_connection)
                        .setMessage(R.string.connection_error_message)
                        .setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
            alertDialogBuilder.show();
        }
    }
}
