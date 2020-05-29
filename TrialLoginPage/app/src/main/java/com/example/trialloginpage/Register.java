package com.example.trialloginpage;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class Register extends AppCompatActivity {
    private static final String LOG_TAG = Register.class.getSimpleName();
    public static final String EXTRA_PHONE = "com.example.trialloginpage.extra.PHONE";
    private static EditText editPhone;
    private static EditText editPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
    }

    public void launchLogin(View view) {
        editPhone = (EditText) findViewById(R.id.editText_rPhone);
        editPassword = (EditText) findViewById(R.id.editText_rPassword);
        String sEditPhone = editPhone.getText().toString();
        String sEditPassword = editPassword.getText().toString();
        if (sEditPhone.matches("")){
            Toast.makeText(this, "You did not enter a Phone Number!", Toast.LENGTH_SHORT).show();
            Log.d(LOG_TAG, "No phone number!");
        }
        else if(sEditPassword.matches("")){
            Toast.makeText(this, "You did not enter a Password!", Toast.LENGTH_SHORT).show();
        }else{
            Intent phoneIntent = new Intent();
            phoneIntent.putExtra(EXTRA_PHONE, sEditPhone);
            setResult(RESULT_OK, phoneIntent);
            Log.d(LOG_TAG, "Registered!");
            finish();
        }
    }
}
