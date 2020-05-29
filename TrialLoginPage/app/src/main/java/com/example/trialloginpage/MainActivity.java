package com.example.trialloginpage;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    public static final int TEXT_REQUEST = 1;
    private static EditText mPhone;
    private static EditText mPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mPhone = findViewById(R.id.editText_Phone);
        if (savedInstanceState != null){
            mPhone.setText(savedInstanceState.getString("phone_num"));
        }
    }

    public void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        outState.putString("phone_num", mPhone.getText().toString());
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        if (requestCode == TEXT_REQUEST){
            if (resultCode == RESULT_OK){
                String phone = data.getStringExtra(Register.EXTRA_PHONE);
                mPhone.setText(phone);
            }
        }
    }
    public void launchRegister(View view) {
        Log.d(LOG_TAG, "Registering!");
        Intent intent = new Intent(this, Register.class);
        startActivityForResult(intent,TEXT_REQUEST);
    }

    public void login(View view) {
        Log.d(LOG_TAG, "Logging in!");
        mPassword = findViewById(R.id.editText_Password);
        String sMPhone = mPhone.getText().toString();
        String sMPassword = mPassword.getText().toString();
        if (sMPhone.matches("")){
            Toast.makeText(this, "You did not enter a Phone Number!", Toast.LENGTH_SHORT).show();
            Log.d(LOG_TAG, "No phone number!");
        }
        else if(sMPassword.matches("")) {
            Toast.makeText(this, "You did not enter a Password!", Toast.LENGTH_SHORT).show();
        }else{
            Intent intent = new Intent(this, MainPage.class);
            startActivity(intent);
        }
    }
}
