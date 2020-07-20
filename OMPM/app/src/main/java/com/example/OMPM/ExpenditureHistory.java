package com.example.OMPM;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

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
    ArrayList<Expenditure> mExpenditureList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expenditure_history);
        getSupportActionBar().setTitle("Expenditure");
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
        createSpinner();
    }

    @Override
    public void onResume(){
        super.onResume();
        createSpinner();
    }


    private void createSpinner(){
        spinnerArray.clear();
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
        mExpenditureList.clear();
        monthExpenditureList.clear();

        Query myExpenditure = mDatabase
                .child("users")
                .child(userId)
                .child("Expenditures")
                .child(selectedDate);

        myExpenditure.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        Expenditure newExpenditure = ds.getValue(Expenditure.class);
                        Processing(newExpenditure);
                    }
                    Listing(monthExpenditureList);
                }
                //Initialize RecyclerView
                mRecyclerView = findViewById(R.id.recyclerview);
                mAdapter = new ExpenditureListAdapter(mRecyclerView.getContext(), mExpenditureList);
                mRecyclerView.setLayoutManager(new LinearLayoutManager(mRecyclerView.getContext()));
                DividerItemDecoration itemDecor = new DividerItemDecoration(mRecyclerView.getContext(), DividerItemDecoration.VERTICAL);
                mRecyclerView.addItemDecoration(itemDecor);
                mRecyclerView.setAdapter(mAdapter);
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
    }

    private void Listing (Map<String, List<Expenditure>> expenditureList){
        List<Expenditure> expenditures = new ArrayList<>();
        for(List<Expenditure> dExpenditurelist: expenditureList.values()){
            for (Expenditure expenditure: dExpenditurelist) {
                expenditures.add(expenditure);
                expenditures.sort(new expenditureComparator());
            }
        }
        mExpenditureList.addAll(expenditures);
    }
}