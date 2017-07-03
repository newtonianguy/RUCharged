package com.example.matt.myapplication;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Matt on 6/29/2017.
 */

public class Logger {
    public String name;

    public Logger() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public Logger(String Name) {
        this.name = Name;
    }

    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("name", name);
        return result;
    }
}
