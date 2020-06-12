package com.example.OMPM;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
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

public class ExpenditureLandingPage extends AppCompatActivity {
    //<!-- TODO: Cleanup code>
    private static final String TAG = "LOG_TAG";

    private FirebaseUser user;
    private DatabaseReference mDatabase;
    private String userId;
    private Date cDate;
    private String currentDate;
    private PieChart pcExpenditure;
    DatabaseReference dateReference;
    Query myExpenditure;

    ArrayList<Expenditure> monthExpenditureList;
    Map<String, Float> dataSet;
    ArrayList<PieEntry> entries;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expenditure_landing_page);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        user = FirebaseAuth.getInstance().getCurrentUser();
        userId = user.getUid();
        cDate = Calendar.getInstance().getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("YYYY/MMM");
        currentDate = sdf.format(cDate.getTime());
        monthExpenditureList = new ArrayList<>();
        dataSet = new HashMap<>();
        entries = new ArrayList<>();

        pcExpenditure = findViewById(R.id.pc_Expenditure);
        dateReference = mDatabase
                        .child("users")
                        .child(userId)
                        .child("Expenditures")
                        .child(currentDate);
        myExpenditure = dateReference;
        myExpenditure.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    Expenditure newExpenditure = ds.getValue(Expenditure.class);
                    String type = newExpenditure.getType();
                    Float cost = Float.parseFloat(newExpenditure.getCost());
                    monthExpenditureList.add(newExpenditure);
                    if (dataSet.containsKey(type)) {
                        cost += dataSet.get(type);
                    }
                    dataSet.put(type, cost);
                    }
                for (Map.Entry<String, Float> data: dataSet.entrySet()){
                    entries.add(new PieEntry(data.getValue(), data.getKey()));
                }
                PieDataSet set = new PieDataSet(entries, "Monthly Expenditure");
                set.setColors(ColorTemplate.COLORFUL_COLORS);
                PieData data = new PieData(set);
                pcExpenditure.setData(data);
                pcExpenditure.animateXY(5000,5000);
                pcExpenditure.invalidate();
                }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}