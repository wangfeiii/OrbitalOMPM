package com.example.OMPM;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class ExpenditureHistory extends AppCompatActivity {

    private static final String TAG = "LOG_TAG";

    private FirebaseUser user;
    private DatabaseReference mDatabase;
    private String userId;
    private Date cDate;
    private String currentDate;
    private DatabaseReference dateReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expenditure_history);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        user = FirebaseAuth.getInstance().getCurrentUser();
        userId = user.getUid();

        cDate = Calendar.getInstance().getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("YYYY/MMM");
        currentDate = sdf.format(cDate.getTime());

        dateReference = mDatabase.child("user").child(userId).child("Expenditure").child(currentDate);
        //TO-DO Create a nice Interface to display data
        dateReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    Transaction transaction = ds.getValue(Transaction.class);
                    String tDate = transaction.getDate();
                    String tType = transaction.getType();
                    String tItem = transaction.getItem();
                    String tCost = "$" + transaction.getCost();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w("LOG_TAG", "loadExpenditure:onCancelled", databaseError.toException());
            }
        });
    }
}