package com.puttey.pustikins.ribbit;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.Toast;

import com.parse.ParseUser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


// ***The TabListener has been deprecated, will look for update later***
// ***The MainActivity class is implementing TabListener, so the     ***
// *** activity itself is listening for tab changes. We'll have to   ***
// *** replace this later. Remember that TabListener is an interface,***
// *** so we'll be looking for a replacement interface later.        ***
public class MainActivity extends ActionBarActivity  {

    public static final String TAG = MainActivity.class.getSimpleName();
    public static final int TAKE_PHOTO_REQUEST = 0;
    public static final int TAKE_VIDEO_REQUEST = 1;
    public static final int PIC_PHOTO = 2;
    public static final int PIC_VIDEO = 3;
    public static final int MEDIA_TYPE_IMAGE = 4;
    public static final int MEDIA_TYPE_VIDEO = 5;
    public static final int FILE_SIZE_LIMIT = 1024 * 1024 * 10; //10MB
    protected Uri mMediaUri;


    protected DialogInterface.OnClickListener mDialogListener = new DialogInterface.OnClickListener(){
        @Override
        public void onClick(DialogInterface dialog, int which){
            switch(which){
                case 0:
                    mMediaUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);
                    if(mMediaUri==null){
                        Toast.makeText(MainActivity.this, getString(R.string.error_external_storage), Toast.LENGTH_LONG ).show();
                    }
                    else{
                        Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, mMediaUri);
                        startActivityForResult(takePhotoIntent, TAKE_PHOTO_REQUEST);
                    }
                    break;
                case 1:
                    Intent videoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                    mMediaUri = getOutputMediaFileUri(MEDIA_TYPE_VIDEO);
                    if(mMediaUri==null){
                        Toast.makeText(MainActivity.this, getString(R.string.error_external_storage), Toast.LENGTH_LONG ).show();
                    }
                    else{
                        videoIntent.putExtra(MediaStore.EXTRA_OUTPUT, mMediaUri);
                        videoIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 10);
                        videoIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0);
                        startActivityForResult(videoIntent, TAKE_VIDEO_REQUEST);
                    }
                    break;
                case 2:
                    Intent choosePhotoIntent = new Intent(Intent.ACTION_GET_CONTENT);
                    choosePhotoIntent.setType("image/*");
                    startActivityForResult(choosePhotoIntent, PIC_PHOTO);
                    break;
                case 3:
                    Intent chooseVideoIntent = new Intent(Intent.ACTION_GET_CONTENT);
                    Toast.makeText(MainActivity.this, getString(R.string.video_file_size_warning), Toast.LENGTH_LONG).show();
                    chooseVideoIntent.setType("video/*");
                    startActivityForResult(chooseVideoIntent, PIC_VIDEO);
                    break;
            }
        }

        private Uri getOutputMediaFileUri(int mediaType){
            if(isExternalStorageAvailable()){
                String appName = MainActivity.this.getString(R.string.app_name);
                File mediaStorageDir =  new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                        appName);
                if(! mediaStorageDir.exists()){
                    if(!mediaStorageDir.mkdirs()){
                        Log.e(TAG, "Failed to create directory");
                        return null;
                    }
                }
                File mediaFile;
                Date now = new Date();
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(now);

                String path = mediaStorageDir.getPath() + File.separator;
                if(mediaType == MEDIA_TYPE_IMAGE){
                    mediaFile = new File(path + "IMG_" + timeStamp + ".jpg");
                }
                else if(mediaType == MEDIA_TYPE_VIDEO){
                    mediaFile = new File(path + "VID_" + timeStamp + ".mp4");
                }
                else{
                    return null;
                }
                Log.d(TAG, "File: " + Uri.fromFile(mediaFile));
                return Uri.fromFile(mediaFile);
            }
            else{
                return null;
            }
        }

        private boolean isExternalStorageAvailable(){
            String state = Environment.getExternalStorageState();
            if(state.equals(Environment.MEDIA_MOUNTED)){
                return true;
            }
            else{
                return false;
            }
        }
    };

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_main);

        // Get the user cached on disk if present
        ParseUser currentUser = ParseUser.getCurrentUser();

        if (currentUser == null) {
            // If no one is logged in, go to the login screen
            navigateToLogin();
        } else {
            Log.i(TAG, currentUser.getUsername());
        }


        // Set up the action bar.
        final ActionBar actionBar = getSupportActionBar();

        // ***This is where setNavigationMode has been deprecated***


        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK){

            if(requestCode == PIC_PHOTO || requestCode == PIC_VIDEO){
                if(data == null){
                    Toast.makeText(this, R.string.general_error_message, Toast.LENGTH_LONG).show();
                }
                else{
                    mMediaUri = data.getData();
                }
                Log.i(TAG, "Media URI: " + mMediaUri);

                if(requestCode == PIC_VIDEO){
                    int fileSize = 0;
                    InputStream inputStream = null;
                    try{
                        inputStream = getContentResolver().openInputStream(mMediaUri);
                        fileSize = inputStream.available();
                    }
                    catch (FileNotFoundException e){
                        Toast.makeText(this, getString(R.string.error_locating_file), Toast.LENGTH_LONG).show();
                        return;
                    }
                    catch (IOException e){
                        Toast.makeText(this, getString(R.string.error_locating_file), Toast.LENGTH_LONG).show();
                        return;
                    }
                    finally{
                        try{
                            inputStream.close();
                        }
                        catch(IOException e){
                            //intentionally left blank
                        }
                    }

                    if(fileSize >= FILE_SIZE_LIMIT){
                        Toast.makeText(this, getString(R.string.file_size_too_large), Toast.LENGTH_LONG).show();
                        return;
                    }


                }
            }
            else{
                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                mediaScanIntent.setData(mMediaUri);
                sendBroadcast(mediaScanIntent);
            }
            Log.d(TAG, "Starting ChooseRecipients Intent");
            Intent recipientsIntent = new Intent(this, ChooseRecipients.class);
            recipientsIntent.setData(mMediaUri);
            startActivity(recipientsIntent);
        }

        else if(resultCode != RESULT_CANCELED){
            Toast.makeText(this, getString(R.string.general_error_message), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {

            case R.id.action_logout:
                ParseUser.logOut();
                navigateToLogin();
                break;
            case R.id.action_edit_friends:
                Intent intent = new Intent(this, EditFriendsActivity.class);
                startActivity(intent);
                break;
            case R.id.action_camera:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setItems(R.array.camera_choices, mDialogListener);
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
        }
        return super.onOptionsItemSelected(item);
    }


    private void navigateToLogin() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }



}