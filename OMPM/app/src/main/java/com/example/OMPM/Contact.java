package com.example.OMPM;

public class Contact {
    private String name;
    private String phone;
    private boolean paid = false;
    private String percentage = "1";


    public Contact(String name, String phone) {
        this.name = name;
        this.phone = phone;
    }

    public Contact(String name, String phone,String percentage) {
        this.name = name;
        this.phone = phone;
        this.percentage =percentage;
    }

    public String getName() {
        return name;
    }

    public String getPercentage() {
        return percentage;
    }

    public String getPhone() {
        return phone;
    }

    public boolean isPaid() { return paid;}

    public void setPercentage(String perc) {
        this.percentage = perc;
    }

    public String toList() {
        return this.name +": " + this.phone;
    }

}

