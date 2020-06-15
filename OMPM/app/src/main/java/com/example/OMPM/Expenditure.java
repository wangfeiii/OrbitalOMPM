package com.example.OMPM;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

public class Expenditure {

    public Long timestamp;
    public String type;
    public String item;
    public String cost;

    public Expenditure(){

    }

    public Expenditure(Long timestamp, String type, String item, String cost){
        this.timestamp = timestamp;
        this.type = type;
        this.item = item;
        this.cost = cost;
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

    public Map<String, Object> toMap(){
        HashMap<String, Object> result = new HashMap<>();
        result.put("date", timestamp);
        result.put("type", type);
        result.put("item", item);
        result.put("cost", cost);
        return result;
    }
}