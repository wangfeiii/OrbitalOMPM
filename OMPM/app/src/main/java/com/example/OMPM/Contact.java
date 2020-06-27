package com.example.OMPM;

public class Contact {
    private String name;
    private String phone;
    private double debt;

    public Contact(String name, String phone) {
        this.name = name;
        this.phone = phone;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public String toList() {
        return this.name +": " + this.phone;
    }
    @Override
    public String toString() {
        return this.name +": " + this.debt;
    }
}

