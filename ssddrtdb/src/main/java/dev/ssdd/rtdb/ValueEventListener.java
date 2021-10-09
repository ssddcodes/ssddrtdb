package dev.ssdd.rtdb;

import androidx.annotation.Nullable;

import org.json.JSONException;

import java.util.List;

public abstract class ValueEventListener {
    public abstract void onDataChange(@Nullable List<DataSnapshot> dataSnapshots);
    public abstract void onError(@Nullable JSONException e);

    public void updateData(List<DataSnapshot> dataSnapshots){
        onDataChange(dataSnapshots);
    }
    public void throwError(JSONException e){
        onError(e);
    }
}
