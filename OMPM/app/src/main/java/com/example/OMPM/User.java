package com.example.OMPM;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class User {

    public String phoneNumber;

    public User(){
    }

    public User(String phoneNumber){
        this.phoneNumber = phoneNumber;
    }
}
