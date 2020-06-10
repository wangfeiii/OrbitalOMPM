package com.example.OMPM;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Transaction {

    public String expenditureDate;
    public String expenditureType;
    public String item;
    public String cost;

    public Transaction(){

    }

    public Transaction(String expenditureDate, String expenditureType, String item, String cost){
        this.expenditureDate = expenditureDate;
        this.expenditureType = expenditureType;
        this.item = item;
        this.cost = cost;
    }
}
