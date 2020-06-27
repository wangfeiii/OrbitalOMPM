package com.example.OMPM;

public class Debt {
    private String amount;
    private String date;
    private String number;
    private String name;

    public Debt(String amount, String date, String number, String name) {
        this.amount = amount;
        this.date = date;
        this.number = number;
        this.name = name;
    }

    public String getAmount() { return amount; }

    public String getDate() {
        return date;
    }

    public String getNumber() {
        return number;
    }

    public String getName() { return name; }
}

