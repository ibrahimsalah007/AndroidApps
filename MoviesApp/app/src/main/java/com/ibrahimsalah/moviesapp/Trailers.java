package com.ibrahimsalah.moviesapp;


public class Trailers {
    String id, key, name;

    public Trailers(){}

    public Trailers(String id, String key, String name) {
        this.id = id;
        this.key ="https://www.youtube.com/watch?v="+ key;
        this.name = name;
    }

    public void setId(String id){
        this.id=id;
    }

    public void setKey(String key){
        this.key="https://www.youtube.com/watch?v="+key;
    }

    public void setName(String name){
        this.name=name;
    }

    public String getId(){
        return id;
    }

    public String getKey(){
        return key;
    }

    public String getName(){
        return name;
    }

    @Override
    public String toString() {
        return name;
    }
}
