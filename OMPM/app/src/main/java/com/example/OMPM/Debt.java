package com.example.OMPM;

public class Debt {
    private String key;
    private String amount;
    private String date;
    private String number;
    private String name;
    private boolean paid;

    public Debt(String key, String amount, String date, String number, String name, boolean paid) {
        this.amount = amount;
        this.date = date;
        this.number = number;
        this.name = name;
        this.paid = paid;
        this.key = key;
    }

    public Debt(String amount, String number, String name) {
        this.amount = amount;
        this.number = number;
        this.name = name;
    }

    public boolean isPaid() {
        return paid;
    }
    public String getAmount() { return amount; }

    public String getKey() { return key; }

    public String getDate() {
        return date;
    }

    public String getNumber() {
        return number;
    }

    public String getName() { return name; }
}

