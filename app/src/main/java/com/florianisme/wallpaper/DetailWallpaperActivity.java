package com.florianisme.wallpaper;

import android.Manifest;
import android.app.ProgressDialog;
import android.app.WallpaperManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.florianisme.wallpaper.fragments.Fragment;
import com.florianisme.wallpaper.helper.SharedPreferences;
import com.squareup.picasso.Callback;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;


public class DetailWallpaperActivity extends AppCompatActivity {

    Toolbar toolbar;
    String wallpaper;
    String title;
    String author;
    TextView authorName;
    ImageView imageView;
    ImageView error;
    ProgressBar progressBar;
    String WALLS_LOCATION;
    File file;
    final String TAG = "recycler";
    boolean logging;
    final String[] PERMISSIONS = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
    boolean permission;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            title = extras.getString("title");
            wallpaper = extras.getString("wallpaper");
            author = extras.getString("author");

        } else if (savedInstanceState.getSerializable("index") != null) {
            title = (String) savedInstanceState.getSerializable("title");
            wallpaper = (String) savedInstanceState.getSerializable("wallpaper");
            author = (String) savedInstanceState.getSerializable("author");
        }

        logging = BuildConfig.DEBUG;

        setTitle(title);

        WALLS_LOCATION = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Paperz/Paperz";

        getPermissions();

        File folder = new File(WALLS_LOCATION + "/Paperz/Paperz/");
        if (permission && !folder.exists())
        folder.mkdirs();

        file = new File(WALLS_LOCATION, title.toLowerCase().replaceAll(" ", "_").trim() + author.toLowerCase().replaceAll(" ","_").trim() + ".png");

        setWallpaper();
    }

    private void setWallpaper() {

        imageView = (ImageView) findViewById(R.id.wallpaper_image);
        progressBar = (ProgressBar) findViewById(R.id.progress_detail);
        authorName = (TextView) findViewById(R.id.wallpaper_author);
        error = (ImageView) findViewById(R.id.wallpaper_error);

        authorName.setText(String.format(getString(R.string.image_credits), author));

        Picasso.with(getApplicationContext())
                .setIndicatorsEnabled(logging);
        Picasso.with(getApplicationContext())
                .setLoggingEnabled(logging);

            Picasso.with(getApplicationContext())
                    .load(wallpaper)
                    .resize(1020, 1980)
                    .noFade()
                    .config(Bitmap.Config.RGB_565)
                    .centerInside()
                    .priority(Picasso.Priority.HIGH)
                    .into(imageView, new com.squareup.picasso.Callback() {
                        @Override
                        public void onSuccess() {
                            authorName.setVisibility(View.VISIBLE);
                            progressBar.setVisibility(View.GONE);
                        }

                        @Override
                        public void onError() {
                            error.setImageDrawable(getResources().getDrawable(R.drawable.ic_connection_issue));
                            progressBar.setVisibility(View.GONE);
                            Log.e("Picasso", "Error while loading the image");
                        }
                    });
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        MenuItem settingsItem = menu.findItem(R.id.action_favorite);
        SharedPreferences sharedPreferences = new SharedPreferences(getApplicationContext());
        settingsItem.setIcon(sharedPreferences.getBoolean(title.toLowerCase().replaceAll(" ", "_").trim(), false) ? getResources().getDrawable(R.drawable.ic_action_favorite) : getResources().getDrawable(R.drawable.ic_action_favorite_outline));

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_details, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Picasso.with(getApplicationContext()).resumeTag(TAG);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Picasso.with(getApplicationContext()).resumeTag(TAG);
                finish();
                return true;
            case R.id.action_save:
                runPicasso(saveTarget);
                return true;
            case R.id.action_set:
                runPicasso(wallTarget);
                return true;
            case R.id.action_favorite:
                addToFavorites(item);
                return true;
            default:
                break;
        }

        return false;
    }


    private final com.squareup.picasso.Target wallTarget = new com.squareup.picasso.Target() {
        ProgressDialog dialog;

        @Override
        public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
            if (dialog == null) {
                dialog = new ProgressDialog(DetailWallpaperActivity.this);

                dialog.setMessage(getResources().getString(R.string.saving));
                dialog.show();
            }
            final FileSaveThread fileSaveThread = new FileSaveThread(file, bitmap, true, dialog);
            dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    fileSaveThread.cancel(true);
                }
            });
            dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    fileSaveThread.cancel(true);
                }
            });
            fileSaveThread.execute();
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {
            Toast.makeText(DetailWallpaperActivity.this, "There was a problem while saving your wallpaper", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {
            dialog = new ProgressDialog(DetailWallpaperActivity.this);

            dialog.setMessage(getString(R.string.preparing));
            dialog.show();
        }
    };


    private final com.squareup.picasso.Target saveTarget = new com.squareup.picasso.Target() {
        ProgressDialog dialog;

        @Override
        public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
            if (dialog == null) {
                dialog = new ProgressDialog(DetailWallpaperActivity.this);

                dialog.setMessage(getResources().getString(R.string.saving));
                dialog.show();
            }
            final FileSaveThread fileSaveThread = new FileSaveThread(file, bitmap, false, dialog);
            dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    fileSaveThread.cancel(true);
                }
            });
            dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    fileSaveThread.cancel(true);
                }
            });
            fileSaveThread.execute();
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {
            Toast.makeText(DetailWallpaperActivity.this, R.string.wallpaper_saving_failure, Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {
            dialog = new ProgressDialog(DetailWallpaperActivity.this);

            dialog.setMessage(getResources().getString(R.string.saving));
            dialog.show();
        }
    };

    private void getPermissions() {
        int perm = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        this.permission = perm == PackageManager.PERMISSION_GRANTED;

        if (perm != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    this,
                    PERMISSIONS,
                    1
            );
        }
    }

    private Uri getImageContentUri(Context context, File imageFile) {
        String filePath = imageFile.getAbsolutePath();
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Images.Media._ID},
                MediaStore.Images.Media.DATA + "=? ",
                new String[]{filePath}, null);
        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
            return Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "" + id);
        } else {
            if (imageFile.exists()) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DATA, filePath);
                return context.getContentResolver().insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            } else {
                return Uri.parse("");
            }
        }
    }

    private class FileSaveThread extends AsyncTask {

        File file;
        Bitmap bitmap;
        boolean setWallpaper;
        ProgressDialog dialog;
        boolean exists;

        public FileSaveThread(File file, Bitmap bitmap, boolean setWallpaper, ProgressDialog dialog) {
            this.file = file;
            this.bitmap = bitmap;
            this.setWallpaper = setWallpaper;
            this.dialog = dialog;
        }

        @Override
        protected void onPostExecute(Object o) {

                dialog.dismiss();

            if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
                if (permission) {
                    if (setWallpaper) {
                        Intent setWall = new Intent(Intent.ACTION_ATTACH_DATA);
                        setWall.setDataAndType(getImageContentUri(getApplicationContext(), file), "image/*");
                        setWall.putExtra("png", "image/*");
                        startActivityForResult(Intent.createChooser(setWall, getString(R.string.set_wallpaper_dialog)), 1);
                    } else {
                        if (!exists)
                            Toast.makeText(DetailWallpaperActivity.this, getString(R.string.wallpaper_saved) + WALLS_LOCATION, Toast.LENGTH_SHORT).show();
                        else
                            Toast.makeText(DetailWallpaperActivity.this, getString(R.string.wallpaper_existing) + WALLS_LOCATION, Toast.LENGTH_SHORT).show();
                    }
                }
                else {
                    getPermissions();
                    Toast.makeText(DetailWallpaperActivity.this, R.string.error_grant_perissions, Toast.LENGTH_SHORT).show();
                }
            }
            else {
                Toast.makeText(DetailWallpaperActivity.this, R.string.sd_not_available, Toast.LENGTH_SHORT).show();
            }

        }

        @Override
        protected String doInBackground(Object[] params) {
            try {
                if (permission) {
                    if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
                        exists = file.exists();
                        if (!exists) {
                            file.createNewFile();
                            FileOutputStream ostream = new FileOutputStream(file);
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, ostream);
                            ostream.close();
                        }
                    }
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }

            final String trash = null;
            return trash;
        }
    }

    private void runPicasso(Target target) {
        Picasso.with(getApplicationContext())
                .load(wallpaper)
                .resize(4096, 4096)
                .noFade()
                .config(Bitmap.Config.RGB_565)
                .centerInside()
                .onlyScaleDown()
                .priority(Picasso.Priority.HIGH)
                .into(target);
    }

    private void addToFavorites(MenuItem item) {
        SharedPreferences sharedPreferences = new SharedPreferences(getApplicationContext());
        sharedPreferences.saveBoolean(title.toLowerCase().replaceAll(" ", "_").trim(), !sharedPreferences.getBoolean(title.toLowerCase().replaceAll(" ", "_").trim(), false));
        if (sharedPreferences.getBoolean(title.toLowerCase().replaceAll(" ", "_").trim(), false)) {
            item.setIcon(getResources().getDrawable(R.drawable.ic_action_favorite));
            Toast.makeText(getApplicationContext(), R.string.added_to_favorites, Toast.LENGTH_SHORT).show();
        }
        else {
            item.setIcon(getResources().getDrawable(R.drawable.ic_action_favorite_outline));
            Toast.makeText(getApplicationContext(), R.string.removed_from_favorites, Toast.LENGTH_SHORT).show();
        }
        MainActivity.updateDrawer(getApplicationContext());
    }

}
