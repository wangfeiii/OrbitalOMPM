package com.example.OMPM;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import java.util.HashMap;
import java.util.Map;

public class Expenditure {

    public String date;
    public String type;
    public String item;
    public String cost;

    public Expenditure(){

    }

    public Expenditure(String date, String type, String item, String cost){
        this.date = date;
        this.type = type;
        this.item = item;
        this.cost = cost;
    }

    public String getDate(){
        return this.date;
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
        result.put("date", date);
        result.put("type", type);
        result.put("item", item);
        result.put("cost", cost);
        return result;
    }
}