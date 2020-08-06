package com.example.OMPM;

import android.content.Context;
import android.text.InputFilter;
import android.text.Spanned;
import android.widget.Toast;

public class InputFilterMinMax implements InputFilter {

    private int min, max;
    private Context context;

    public InputFilterMinMax(int min, int max, Context context) {
        this.min = min;
        this.max = max;
        this.context = context;
    }

    public InputFilterMinMax(String min, String max,Context context) {
        this.min = Integer.parseInt(min);
        this.max = Integer.parseInt(max);
        this.context = context;
    }

    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        try {
            String destString = dest.toString();
            String inputString = destString.substring(0, dstart) + source.toString() + destString.substring(dstart);
            int input = Integer.parseInt(inputString);

            if (isInRange( min , max , input))
                return null;
            else
                Toast.makeText(context,"Please enter a number from 1 to 100", Toast.LENGTH_SHORT).show();


        } catch (NumberFormatException nfe) { }
        return "";
    }
    private boolean isInRange(int a, int b, int c) {
        return b > a ? c >= a && c <= b : c >= b && c <= a;
    }
}