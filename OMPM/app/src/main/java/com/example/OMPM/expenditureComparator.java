package com.example.OMPM;

import java.util.Comparator;

public class expenditureComparator implements Comparator<Expenditure> {

    @Override
    public int compare(Expenditure o1, Expenditure o2) {
        return Long.compare(o2.timestamp,o1.timestamp);
    }
}
