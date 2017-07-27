package com.example.matt.myapplication;

import com.google.firebase.database.Exclude;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Matt on 6/29/2017.
 */

//Class for updating user information
public class Post {
    public String name;
    public String type;
    public String number;
    public Date time;

    // Default constructor required for calls to DataSnapshot.getValue(Post.class)
    public Post(){

    }

    public Post(String Name, String Type, String Number,Date now) {
        this.name = Name;
        this.type = Type;
        this.number =Number;
        this.time =now;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("name", name);
        result.put("type", type);
        result.put("number",number);
        result.put("time",time);
        return result;
    }

}
