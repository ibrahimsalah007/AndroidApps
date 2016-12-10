package com.ibrahimsalah.moviesapp;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;


public class GridViewAdapter extends ArrayAdapter<Movie> {
    private Context mContext;
    private int layoutResourceId;
    private List<Movie > mGridImages = new ArrayList<Movie>();
    public GridViewAdapter(Context context, int resource, List<Movie> objects) {
        super(context,resource, objects);
        this.mContext=context;
        this.layoutResourceId=resource;
        this.mGridImages=objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        ImageView image;
        if(row == null){
            LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
            row = inflater.inflate(layoutResourceId,parent, false);
            image= (ImageView)row.findViewById(R.id.movieImage);
            row.setTag(image);
        }
        else{
            image = (ImageView)row.getTag();
        }
         Picasso.with(mContext).load("http://image.tmdb.org/t/p/w185/" + mGridImages.get(position).getMoviePoster()).into(image);
        return row;
    }
}
