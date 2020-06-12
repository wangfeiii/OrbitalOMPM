package com.example.OMPM;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import java.util.HashMap;
import java.util.Map;

public class Expenditure {

    public String expenditureDate;
    public String expenditureType;
    public String item;
    public String cost;

    public Expenditure(){

    }

    public Expenditure(String expenditureDate, String expenditureType, String item, String cost){
        this.expenditureDate = expenditureDate;
        this.expenditureType = expenditureType;
        this.item = item;
        this.cost = cost;
    }

    public String getDate(){
        return this.expenditureDate;
    }

    public String getType(){
        return this.expenditureType;
    }

    public String getItem(){
        return this.item;
    }

    public String getCost(){
        return this.cost;
    }

    public Map<String, Object> toMap(){
        HashMap<String, Object> result = new HashMap<>();
        result.put("Expenditure Date", expenditureDate);
        result.put("Expenditure Type", expenditureType);
        result.put("Item", item);
        result.put("Cost", cost);
        return result;
    }
}