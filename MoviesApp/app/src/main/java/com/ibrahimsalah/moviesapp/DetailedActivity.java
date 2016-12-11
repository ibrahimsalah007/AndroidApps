package com.ibrahimsalah.moviesapp;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class DetailedActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailed);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
       getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //if (isNetworkAvailable(getApplicationContext()))
         //   Toast.makeText(getApplicationContext(), "Failed to Connect. Try again", Toast.LENGTH_SHORT).show();
       // else
         //   Toast.makeText(getApplicationContext(), "Failed to Connect data! Check connection", Toast.LENGTH_SHORT).show();
        //checkForInternet();

        Bundle extras = getIntent().getExtras();
        if (null == savedInstanceState) {
            DetailedActivityFragment mDetailsFragment = new DetailedActivityFragment();
            //Pass the "extras" Bundle that contains the selected "name" to the fragment
            mDetailsFragment.setArguments(extras);
            getSupportFragmentManager().beginTransaction().add(R.id.detail_container, mDetailsFragment).commit();
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_detailed, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    public boolean isNetworkAvailable(final Context context) {
        final ConnectivityManager connectivityManager = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
    }

    public void checkForInternet() {

        if (InternetConnectionStatus.getInstance(getApplicationContext()).isOnline()) {
            Toast.makeText(getApplicationContext(), "Connected Successfully", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(), "You Are No Connected ", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getApplicationContext(), ConnectionFail.class);
            startActivity(intent);
            //finish();
        }
    }


}
