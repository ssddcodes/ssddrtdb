package dev.ssdd.rtdb;

import com.google.gson.Gson;

public class DataSnapshot {

    private final String json;
    public DataSnapshot(String json) {
        this.json = json;
    }
    public <T> T getValue(Class<T> valueType){
        return new Gson().fromJson(json,valueType);
    }
}
