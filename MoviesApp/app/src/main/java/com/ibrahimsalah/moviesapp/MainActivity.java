package com.ibrahimsalah.moviesapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity implements iSecondaryFrag {
    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (findViewById(R.id.detail_container) != null) {
            mTwoPane = true;
        } else {
            mTwoPane = false;
        }
//      if(mTwoPane){
          GridViewFragment gv = (GridViewFragment) getSupportFragmentManager().findFragmentById(R.id.fragment1);
          gv.setSecondaryFrag(this);
//      }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

    @Override
    public void transferData(Movie movie) {
      if(mTwoPane){

          DetailedActivityFragment detailedActivityFragment = (DetailedActivityFragment) getSupportFragmentManager().findFragmentById(R.id.detail_container);
          detailedActivityFragment.UpdateData(movie);
      }
      else{
        Intent intent = new Intent(this, DetailedActivity.class);
        intent.putExtra("movie", movie);
        startActivity(intent);
      }
    }
}
