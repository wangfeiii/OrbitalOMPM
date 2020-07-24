package com.example.OMPM;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.Html;
import android.widget.TextView;

public class ContactUs extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_us);
        TextView text = findViewById(R.id.address);
      /* text.setText(Html.fromHtml("Address" + "<font size='200'>" +
                      "123 ABC Road #01-01 Singapore 123456" +
                        "<font size= '10'>" +
                "Temperature: "+ "<small> <font color='#59c3fa'>" + "\n" + "temperature" + "°C</font></small>"));

       */
    }
}