package com.example.OMPM;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ContactUs extends AppCompatActivity {
    private FirebaseUser user;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        user = FirebaseAuth.getInstance().getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        setContentView(R.layout.activity_contact_us);
        TextView text = findViewById(R.id.address);

        final EditText me = findViewById(R.id.name);
        me.setText(user.getDisplayName());

        final EditText subjectET = findViewById(R.id.subject);

        final EditText emailET = findViewById(R.id.email);

        final EditText messageET = findViewById(R.id.message);


        findViewById(R.id.submit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String myName = me.getText().toString();
                String subject = subjectET.getText().toString();
                String email = emailET.getText().toString();
                String message = messageET.getText().toString();

                if (subject.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Please key in subject!",
                            Toast.LENGTH_SHORT).show();
                    return;

                } else if (email.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Please key in email!",
                            Toast.LENGTH_SHORT).show();
                    return;

                }  else if (message.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Please key in message!",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                String key = mDatabase.child("contactUs").push().getKey();
                mDatabase.child("contactUs").child(key).child("name").setValue(myName);
                mDatabase.child("contactUs").child(key).child("subject").setValue(subject);
                mDatabase.child("contactUs").child(key).child("email").setValue(email);
                mDatabase.child("contactUs").child(key).child("message").setValue(message);
            }
        });

    }
}