package com.florianisme.wallpaper;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.florianisme.wallpaper.adapter.CreativeCommonsAdapter;
import com.florianisme.wallpaper.adapter.DrawerAdapter;
import com.florianisme.wallpaper.adapter.RecyclerAdapter;
import com.florianisme.wallpaper.fragments.Fragment;
import com.florianisme.wallpaper.helper.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    public static ArrayAdapter mAdapter;
    DrawerLayout mDrawerLayout;
    ActionBarDrawerToggle mDrawerToggle;
    public static ListView mDrawerList;
    Toolbar toolbar;
    Fragment fragment;

    static String[] drawerTitles;
    static int[] icons = {R.drawable.drawer_all,R.drawable.drawer_new,R.drawable.drawer_favorites, 0, R.drawable.drawer_material,R.drawable.drawer_landscape,R.drawable.drawer_nature, R.drawable.drawer_sea, R.drawable.drawer_city, R.drawable.drawer_vintage};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.my_drawer_layout);
        mDrawerLayout.setStatusBarBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
        setDrawer();

        SharedPreferences sharedPreferences = new SharedPreferences(getApplicationContext());
        sharedPreferences.saveBoolean("check_mobile_data", true);

        if (!BuildConfig.DEBUG)
        //checkLicense();

        onClick(0);
    }


    public static void updateDrawer(Context context) {
        mAdapter = new DrawerAdapter(context, drawerTitles, icons, getData(context));
        mDrawerList.setAdapter(mAdapter);
    }

    private void setDrawer() {
        mDrawerList = (ListView) findViewById(R.id.drawer_list_view);
        drawerTitles = getResources().getStringArray(R.array.drawer_items);
        mAdapter = new DrawerAdapter(getApplicationContext(), drawerTitles, icons, getData(getApplicationContext()));
        View header = getLayoutInflater().inflate(R.layout.drawer_header, null);
        mDrawerList.addHeaderView(header, null, false);
        mDrawerList.setAdapter(mAdapter);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, 0, 0) {

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();
        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                onClick(position - 1);
            }
        });

    }


    private void onClick(int position) {
        if (position != 3) {
            RecyclerAdapter.categoryChanged(getApplicationContext());
            fragment = Fragment.newInstance(position);
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.content, fragment).commit();
            setTitle(drawerTitles[position]);
            DrawerAdapter.setSelectedItem(position);
            mAdapter.notifyDataSetChanged();
            mDrawerLayout.closeDrawers();
        }
    }

    public static int[] getData(Context context) {
        ArrayList<String> title = new ArrayList<>();
        String file = readJSON(context);
        int[] count = new int[10];
        try {
            JSONObject jsonObject = new JSONObject(file);
            JSONArray jsonArray = jsonObject.getJSONArray("categories");
            for (int i = 0; i < jsonArray.length(); i++) {
                jsonObject = jsonArray.getJSONObject(i);
                JSONArray jsonArrayWallpaper = jsonObject.getJSONArray("wallpaper");
                count[i+4] = jsonArrayWallpaper.length();
                for (int ii = 0; ii < jsonArrayWallpaper.length(); ii++) {
                    title.add(jsonArrayWallpaper.getJSONObject(ii).getString("title"));
                }
            }
            count[0] = count[4] + count[5] + count[6] + count[7] + count[8] + count[9];

            int version = 0;
            try {
                version = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
            }
            catch (PackageManager.NameNotFoundException ex) {
                ex.printStackTrace();
            }
            jsonObject = new JSONObject(file);
            jsonArray = jsonObject.getJSONArray("categories");
            for (int i = 0; i < jsonArray.length(); i++) {
                jsonObject = jsonArray.getJSONObject(i);
                JSONArray jsonArrayWallpaper = jsonObject.getJSONArray("wallpaper");

                for (int ii = 0; ii < jsonArrayWallpaper.length(); ii++) {
                    if (jsonArrayWallpaper.getJSONObject(ii).getInt("version_added") == version) {
                        count[1] += 1;
                    }
                }
            }

            SharedPreferences sharedPreferences = new SharedPreferences(context);
            for (int i = 0; i < title.size(); i++) {
                if (sharedPreferences.getBoolean(title.get(i).toLowerCase().replaceAll(" ", "_").trim(), false)) {
                    count[2] += 1;
                }
            }
        }
        catch (JSONException ex) {
            ex.printStackTrace();
        }
        if (mAdapter != null) {
            mDrawerList.setAdapter(mAdapter);
            mAdapter.notifyDataSetChanged();
            mDrawerList.deferNotifyDataSetChanged();
        }
        return count;
    }


    public static String readJSON(Context context) {
        try {
            InputStream is = context.getAssets().open("wallpaper.json");
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


    private void checkLicense() {
        String installer = getPackageManager().getInstallerPackageName(getPackageName());
        Log.d("Installer", "Installer: " + installer);
            if (installer == null || !installer.equals("com.android.vending")) {
                AlertDialog.Builder alertDialogBuilder =
                    new AlertDialog.Builder(this)
                            .setTitle(R.string.license)
                            .setMessage(R.string.license_unsuccessful)
                            .setPositiveButton(R.string.download, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + getPackageName()));
                                    startActivity(browserIntent);
                                    dialog.dismiss();
                                    finish();
                                }
                            })
                        .setNegativeButton(R.string.exit, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        })
                        .setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                finish();
                            }
                        });
                alertDialogBuilder.show();
            }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_libraries:
                AlertDialog.Builder alertDialogBuilder =
                        new AlertDialog.Builder(MainActivity.this)
                                .setTitle(R.string.menu_cc)
                                .setAdapter(new CreativeCommonsAdapter(MainActivity.this), null)
                                .setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });
                alertDialogBuilder.show();
                return true;
            case R.id.action_google_plus:
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://plus.google.com/u/0/communities/103161045714462511515"));
                startActivity(browserIntent);
                return true;
            default:
                break;
        }
        return false;
    }

}
