package com.example.OMPM;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

public class Expenditure implements Parcelable {

    public Long timestamp;
    public String type;
    public String item;
    public String cost;
    public String key;


    public Expenditure(){

    }

    public Expenditure(Long timestamp, String type, String item, String cost, String key){
        this.timestamp = timestamp;
        this.type = type;
        this.item = item;
        this.cost = cost;
        this.key = key;
    }

    public Long getTimestamp(){
        return this.timestamp;
    }

    public String getType(){
        return this.type;
    }

    public String getItem(){
        return this.item;
    }

    public String getCost(){
        return this.cost;
    }

    public String getKey() { return this.key;}

    public Map<String, Object> toMap(){
        HashMap<String, Object> result = new HashMap<>();
        result.put("date", timestamp);
        result.put("type", type);
        result.put("item", item);
        result.put("cost", cost);
        return result;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(key);
        dest.writeString(type);
        dest.writeString(item);
        dest.writeString(cost);
        dest.writeLong(timestamp);
    }

    public static final Parcelable.Creator<Expenditure> CREATOR = new Parcelable.Creator<Expenditure>(){
        @Override
        public Expenditure createFromParcel(Parcel source) {
            return new Expenditure(source);
        }

        @Override
        public Expenditure[] newArray(int size) {
            return new Expenditure[size];
        }
    };
    private Expenditure(Parcel source){
        key = source.readString();
        type = source.readString();
        item = source.readString();
        cost = source.readString();
        timestamp = source.readLong();
    }
}