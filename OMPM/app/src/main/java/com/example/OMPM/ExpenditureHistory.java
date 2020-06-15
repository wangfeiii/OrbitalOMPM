package com.example.OMPM;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
//! TODO: add spinner for different months
public class ExpenditureHistory extends AppCompatActivity {

    private static final String TAG = "LOG_TAG";

    private RecyclerView mRecyclerView;
    private ExpenditureListAdapter mAdapter;

    private FirebaseUser user;
    private DatabaseReference mDatabase;
    private String userId;
    private Date cDate;
    private String currentDate;
    DatabaseReference dateReference;
    Query myExpenditure;

    Map<String, List<Expenditure>> monthExpenditureList;
    List<String> mWordList = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expenditure_history);
        //Initialize firebase
        mDatabase = FirebaseDatabase.getInstance().getReference();
        user = FirebaseAuth.getInstance().getCurrentUser();
        userId = user.getUid();

        //Get Current Date
        cDate = Calendar.getInstance().getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("YYYY/MMM");
        currentDate = sdf.format(cDate.getTime());
        monthExpenditureList = new HashMap<>();

        //Get Expenditures from current month
        dateReference = mDatabase
                .child("users")
                .child(userId)
                .child("Expenditures")
                .child(currentDate);
        myExpenditure = dateReference.orderByChild("timestamp");
        myExpenditure.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        Expenditure newExpenditure = ds.getValue(Expenditure.class);
                        Processing(newExpenditure);
                    }
                    Listing(monthExpenditureList);
                } else { mWordList.add("You have not recorded any expenditures this month!"); }
                //Initialize RecyclerView
                mRecyclerView = findViewById(R.id.recyclerview);
                mAdapter = new ExpenditureListAdapter(mWordList);
                mRecyclerView.setAdapter(mAdapter);
                mRecyclerView.setLayoutManager(new LinearLayoutManager(mRecyclerView.getContext()));
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w("LOG_TAG", "loadExpenditure:onCancelled", databaseError.toException());
            }
        });
    }

    private void Processing (Expenditure expenditure){
        List<Expenditure> tempList = new ArrayList<>();
        Long timestamp = expenditure.getTimestamp();
        SimpleDateFormat fullSDF = new SimpleDateFormat("dd/MMMM/YYYY");
        String date = fullSDF.format(timestamp);
        if (!monthExpenditureList.containsKey(date)){
            monthExpenditureList.put(date, tempList);
        }
        monthExpenditureList.get(date).add(expenditure);
        Log.d(TAG, "added" + expenditure.getTimestamp());
    }

    private void Listing (Map<String, List<Expenditure>> expenditureList){
        for(List<Expenditure> dExpenditurelist: expenditureList.values()){
            for (Expenditure expenditure: dExpenditurelist){
                String type = expenditure.getType();
                mWordList.add(type);
                SimpleDateFormat fullSDF = new SimpleDateFormat("dd/MMMM/YYYY");
                String date = fullSDF.format(expenditure.getTimestamp());
                mWordList.add(date);
                String cost = expenditure.getCost();
                mWordList.add(cost);
                String item = expenditure.getItem();
                mWordList.add(item);
                String temp = "---------";
                mWordList.add(temp);
                Log.d(TAG, item + "added to word list");
            }
        }
    }
}