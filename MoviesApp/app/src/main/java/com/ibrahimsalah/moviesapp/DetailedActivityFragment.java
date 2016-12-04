package com.ibrahimsalah.moviesapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.squareup.picasso.Picasso;
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


public class DetailedActivityFragment extends Fragment implements View.OnClickListener {
    public String id;
    public ListView list;
    Movie myMovieObject;
    private  List<Trailers> data;
    FetchTrailer fetch;
    Button rev;
    Button favourite ;
    String sortingmethod;
    MovieDBHelper DB;
    TextView title,vote,release,review ;
    ImageView poster;
    SharedPreferences sharedPreferences;
    public DetailedActivityFragment() {
    }

    @Override
    public void onResume() {
        super.onResume();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        sortingmethod = sharedPreferences.getString(getString(R.string.sort_type), getString(R.string.pref_default_value));
        if(sortingmethod.equals("favourite"))
            favourite.setText("Remove From Favourite");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_detailed, container, false);
        View header = inflater.inflate(R.layout.header, null, false);
        list = (ListView)rootView.findViewById(R.id.list_trailers);
        list.addHeaderView(header);

        rev = (Button) rootView.findViewById(R.id.reviews);
        title = (TextView) rootView.findViewById(R.id.movie_title);
        vote = (TextView) rootView.findViewById(R.id.movie_vote_average);
        release = (TextView) rootView.findViewById(R.id.movie_release_date);
        review = (TextView) rootView.findViewById(R.id.movie_overview);
        poster = (ImageView) rootView.findViewById(R.id.movie_poster);
        favourite = (Button) rootView.findViewById(R.id.favor_id);

        UpdateData(getMovieObject());
        rev.setOnClickListener(this);
        favourite.setOnClickListener(this);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
             @Override
             public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                 startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(data.get(position-1).getKey())));
             }
         });

        return rootView;
    }


    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.favor_id) {
            if(favourite.getText().toString().equals("Add to favorite")) {
                DB = new MovieDBHelper(getActivity());
                DB.insertMovie(myMovieObject);
                Toast.makeText(getActivity(), "Added to Favourite List", Toast.LENGTH_SHORT).show();
               }
            else {
                favourite.setText("Remove From Favourite");
                DB = new MovieDBHelper(getActivity());
                DB.deleteMovie(myMovieObject.getiD());

                Toast.makeText(getActivity(), "Removed from Favourite List", Toast.LENGTH_SHORT).show();
                myMovieObject.insertedToDB=false;
            }
        }
        else if (v.getId()==R.id.reviews){
         Intent intent = new Intent(getActivity(), Review.class);
         intent.putExtra("id", id);
         startActivity(intent);
        }
    }

    public Movie getMovieObject(){
        if(getArguments()!=null&&getArguments().getParcelable("movie")!=null) {
            myMovieObject = getArguments().getParcelable("movie");
        }
        return myMovieObject;
    }

    public void UpdateData(Movie movie){
        myMovieObject = movie;
        if(myMovieObject!=null) {
            Picasso.with(getContext()).load(myMovieObject.getMoviePoster()).into(poster);
            title.setText(myMovieObject.getTitle());
            vote.setText(myMovieObject.getVoteAverage());
            review.setText(myMovieObject.getPlotSynopsis());
            release.setText(myMovieObject.getRelease_data());
            id = myMovieObject.getiD();
            fetch = new FetchTrailer();
            fetch.execute();
        }
  }
    public boolean isNetworkAvailable(final Context context) {
        final ConnectivityManager connectivityManager = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
    }


    class FetchTrailer extends AsyncTask<String, Void, List<Trailers> > {
       private final String LOG_TAG = FetchTrailer.class.getSimpleName();
       public ArrayAdapter<Trailers>  adapter;

       public List<Trailers> getMovieTrailerFromJson(String MovieJsonStr)
               throws JSONException{
           data = new ArrayList<Trailers>();
           final String WEB_RESULT = "results";
           JSONObject initial = new JSONObject(MovieJsonStr);
           JSONArray moviesArray = initial.getJSONArray(WEB_RESULT);
           if(moviesArray==null){
               return null;
           }
           else{
               for(int i=0;i<moviesArray.length();i++){
                   JSONObject movieDetail = moviesArray.getJSONObject(i);
                   Trailers trailer = new Trailers();
                   trailer.setId(movieDetail.getString("id"));
                   trailer.setKey(movieDetail.getString("key"));
                   trailer.setName(movieDetail.getString("name"));
                   data.add(trailer);
               }
           }
           return data;
       }
       @Override
       protected List<Trailers> doInBackground(String... params) {

           HttpURLConnection urlConnection = null;
           BufferedReader reader = null;
           String MovieJsonStr = null;
           Uri builtUri;

           try {
               final String FETCH_MOVIE_BASE_URL = "http://api.themoviedb.org/3/movie/";
               final String API_KEY = "api_key";
               builtUri = Uri.parse(FETCH_MOVIE_BASE_URL).buildUpon()
                       .appendPath(id)
                       .appendPath("videos")
                       .appendQueryParameter(API_KEY, BuildConfig.Movie_App_API_KEY)
                       .build();
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
               if (buffer.length() == 0){
                   return null;
               }
               MovieJsonStr = buffer.toString();
           }catch(IOException e) {
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
               return getMovieTrailerFromJson(MovieJsonStr);

           } catch (JSONException e) {
               e.printStackTrace();
           }
           return null;
       }
       @Override
       protected void onPostExecute(List<Trailers> result) {
           if (isNetworkAvailable(getContext())) {
               adapter = new ArrayAdapter<Trailers>(getActivity(), R.layout.trailer_list_item,R.id.trailers_text_view, result);
               list.setAdapter(adapter);
           }
           else {
               adapter = new ArrayAdapter<Trailers>(getActivity(),  R.layout.trailer_list_item,R.id.trailers_text_view, data);
               list.setAdapter(adapter);
           }
       }

   }
}