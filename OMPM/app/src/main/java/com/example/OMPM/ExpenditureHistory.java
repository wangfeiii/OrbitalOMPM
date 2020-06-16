package com.example.OMPM;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

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
import java.util.LinkedList;
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


    private Spinner spinner;
    private LinkedList<String> spinnerArray = new LinkedList<>();

    Map<String, List<Expenditure>> monthExpenditureList;
    LinkedList<String> mWordList = new LinkedList<String>();

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

        spinner = findViewById(R.id.spinner_month);
        Query yearQuery = mDatabase
                .child("users")
                .child(userId)
                .child("ExpenditureDates")
                .orderByKey();
        yearQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    for(DataSnapshot ds: dataSnapshot.getChildren()) {
                        String key = ds.getKey();
                        Long timestamp = Long.parseLong(key);
                        SimpleDateFormat monthSDF = new SimpleDateFormat("MMM/YYYY");
                        String newDate = monthSDF.format(timestamp);
                        if(!spinnerArray.contains(newDate)) {
                            spinnerArray.addFirst(newDate);
                        }
                    }
                }
                String newDate = currentDate.substring(5) + "/" + currentDate.substring(0, 4);
                if (!spinnerArray.contains(newDate)) {
                    spinnerArray.addFirst(newDate);
                }
                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(spinner.getContext(),
                        android.R.layout.simple_spinner_dropdown_item,
                        spinnerArray);
                arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner.setAdapter(arrayAdapter);
                spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        String selected = parent.getItemAtPosition(position).toString();
                        String selectedDate = selected.substring(4) + "/" + selected.substring(0, 4);
                        changeDate(selectedDate);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {


                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void changeDate(String selectedDate){
        mWordList.clear();
        monthExpenditureList.clear();

        Query myExpenditure = mDatabase
                .child("users")
                .child(userId)
                .child("Expenditures")
                .child(selectedDate)
                .orderByChild("timestamp");

        myExpenditure.addListenerForSingleValueEvent(new ValueEventListener() {
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
        List<Expenditure> expenditures = new ArrayList<>();
        for(List<Expenditure> dExpenditurelist: expenditureList.values()){
            for (Expenditure expenditure: dExpenditurelist) {
                expenditures.add(expenditure);
                expenditures.sort(new expenditureComparator());
            }
        }
        for(Expenditure expenditure : expenditures){
            String temp = "---------";
            mWordList.addFirst(temp);
            String item = expenditure.getItem();
            mWordList.addFirst(item);
            String cost = expenditure.getCost();
            mWordList.addFirst(cost);
            SimpleDateFormat fullSDF = new SimpleDateFormat("dd/MMMM/YYYY");
            String date = fullSDF.format(expenditure.getTimestamp());
            mWordList.addFirst(date);
            String type = expenditure.getType();
            mWordList.addFirst(type);
            Log.d(TAG, item + "added to word list");
        }
    }
}