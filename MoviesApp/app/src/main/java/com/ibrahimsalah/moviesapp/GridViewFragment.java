package com.ibrahimsalah.moviesapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class GridViewFragment extends Fragment {
    private  List<Movie> data;
    GridView gridView;
    private GridViewAdapter mGridAdapter;
    SharedPreferences sharedPreferences;
    String sortingmethod,oldPref,newPref,old2Pref,new2Pref;
    iSecondaryFrag secondaryFrag;
    int mPostion = GridView.INVALID_POSITION;
    private static final String SELECTED_KEY = "selected_position";
    boolean check;
    public GridViewFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        if(savedInstanceState!=null){
            check=true;
        }


    }

    @Override
    public void onResume() {
        super.onResume();
        which_pref();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        gridView = (GridView) rootView.findViewById(R.id.postersgridView);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener(

        ) {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Movie m = data.get(position);
                mPostion = position;
                secondaryFrag.transferData(m);

            }
        });
        if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_KEY)) {
            mPostion = savedInstanceState.getInt(SELECTED_KEY);
            check=true;
        }
        if(isTablet(getActivity())) {
            if (data!= null) {
                Movie m = data.get(0);
                gridView.setSelection(0);
                secondaryFrag.transferData(m);
            }
        }
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        old2Pref = sharedPreferences.getString(getString(R.string.sort_type), getString(R.string.pref_default_value));
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        oldPref = sharedPreferences.getString(getString(R.string.sort_type), getString(R.string.pref_default_value));
        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mPostion != GridView.INVALID_POSITION) {
            outState.putInt(SELECTED_KEY, mPostion);
        }
        super.onSaveInstanceState(outState);
    }

    public void which_pref(){
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        sortingmethod = sharedPreferences.getString(getString(R.string.sort_type), getString(R.string.pref_default_value));
        if(sortingmethod.equals("favourite")){
            MovieDBHelper db = new MovieDBHelper(getActivity());
            try {
                data = db.getAllMovie();
                gridView.setAdapter(new GridViewAdapter(getActivity(), R.layout.grid_item_posters, data));
            }catch (Exception e){
                Toast.makeText(getActivity(), "No Favorites", Toast.LENGTH_SHORT).show();
            }
            if (mPostion != GridView.INVALID_POSITION&&sortingmethod.equals(oldPref)) {
                gridView.setSelection(mPostion);
                if (isTablet(getActivity())) {
                    secondaryFrag.transferData(data.get(mPostion));
                }
            }
            else if (data!= null){
                try {
                    Movie m = data.get(0);
                    gridView.setSelection(0);
                    if (isTablet(getActivity())) {
                        secondaryFrag.transferData(m);
                    }
                }catch (Exception e){
                    Toast.makeText(getActivity(),"No Favourite",Toast.LENGTH_LONG);
                }
            }

        }
        else{
            new FetchMovieTask().execute();
        }
    }

    public void setSecondaryFrag(iSecondaryFrag secondaryFrag) {
        this.secondaryFrag = secondaryFrag;
    }

    public boolean isNetworkAvailable(final Context context) {
        final ConnectivityManager connectivityManager = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
    }

    public static boolean isTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    class FetchMovieTask extends AsyncTask<String, Void, List<Movie>> {
        private final String LOG_TAG = FetchMovieTask.class.getSimpleName();


        public List<Movie> getMovieDataFromJson(String MovieJsonStr)
                throws JSONException {
            data = new ArrayList<Movie>();
            final String WEB_RESULT = "results";
            JSONObject initial = new JSONObject(MovieJsonStr);
            JSONArray moviesArray = initial.getJSONArray(WEB_RESULT);
            if (moviesArray == null) {
                return null;
            } else {
                for (int i = 0; i < moviesArray.length(); i++) {
                    JSONObject movieDetail = moviesArray.getJSONObject(i);
                    Movie movie = new Movie();
                    movie.setMoviePoster(movieDetail.getString("poster_path"));
                    movie.setTitle(movieDetail.getString("title"));
                    movie.setRelease_data(movieDetail.getString("release_date"));
                    movie.setVoteAverage(movieDetail.getString("vote_average") + "/10");
                    movie.setPlotSynopsis(movieDetail.getString("overview"));
                    movie.setiD(movieDetail.getString("id"));
                    data.add(movie);
                }
            }
            return data;
        }

        @Override
        protected List<Movie> doInBackground(String... params) {

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String MovieJsonStr = null;
            Uri builtUri;

            try {
                final String FETCH_MOVIE_TOP_RATED_BASE_URL = "http://api.themoviedb.org/3/movie/top_rated?";
                final String FETCH_MOVIE_popular_BASE_URL = "http://api.themoviedb.org/3/movie/popular?";
                final String API_KEY = "api_key";

                if (sortingmethod.equals("popular")) {
                    builtUri = Uri.parse(FETCH_MOVIE_popular_BASE_URL).buildUpon()
                            .appendQueryParameter(API_KEY, BuildConfig.Movie_App_API_KEY)
                            .build();
                } else {
                    builtUri = Uri.parse(FETCH_MOVIE_TOP_RATED_BASE_URL).buildUpon()
                            .appendQueryParameter(API_KEY, BuildConfig.Movie_App_API_KEY)
                            .build();
                }
                URL url = new URL(builtUri.toString());
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();

                if (inputStream == null) {
                    Log.d(LOG_TAG, "input stream is null");
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }
                if (buffer.length() == 0) {
                    return null;
                }
                MovieJsonStr = buffer.toString();
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }
            try {
                return getMovieDataFromJson(MovieJsonStr);

            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<Movie> result) {
            if (result != null) {
                gridView.setAdapter(new GridViewAdapter(getActivity(), R.layout.grid_item_posters, result));
                sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
                newPref = sharedPreferences.getString(getString(R.string.sort_type), getString(R.string.pref_default_value));
                if (data!= null) {
                    Movie m = data.get(0);
                    gridView.setSelection(0);
                    if(isTablet(getActivity())) {
                        secondaryFrag.transferData(m);
                    }
                }
            }
            else {
                if (isNetworkAvailable(getActivity()))
                    Toast.makeText(getActivity(), "Failed to Connect. Try again", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(getActivity(), "Failed to Connect data! Check connection", Toast.LENGTH_SHORT).show();
            }
            if (mPostion != GridView.INVALID_POSITION&&oldPref.equals(newPref)) {
                gridView.setSelection(mPostion);
                if(isTablet(getActivity())) {
                    secondaryFrag.transferData(data.get(mPostion));
                }
            }else  if (!oldPref.equals(newPref)) {
                gridView.setSelection(0);
                if (isTablet(getActivity())) {
                    secondaryFrag.transferData(data.get(0));
                }
                oldPref = newPref;
            }

        }
    }

}
