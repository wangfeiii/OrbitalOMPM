package com.example.OMPM;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

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

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExpenditureLandingPage extends AppCompatActivity {
    //! TODO: Add spinner for different filters
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
                if (dataSnapshot.exists()) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        Expenditure newExpenditure = ds.getValue(Expenditure.class);
                        createDataSet(newExpenditure);
                        monthExpenditureList.add(newExpenditure);
                    }
                    for (Map.Entry<String, Float> data : dataSet.entrySet()) {
                        entries.add(new PieEntry(data.getValue(), data.getKey()));
                    }
                    PieDataSet set = new PieDataSet(entries, "Monthly Expenditure");
                    createPieChart(set, pcExpenditure);
                } else {
                    pcExpenditure.setVisibility(View.INVISIBLE);
                    TextView noData = findViewById(R.id.text_noData);
                    noData.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }


    private void createDataSet(Expenditure expenditure){
        String type = expenditure.getType();
        Float cost = Float.parseFloat(expenditure.getCost());
        if (dataSet.containsKey(type)) {
            cost += dataSet.get(type);
        }
        dataSet.put(type, cost);
    }

    private void createPieChart(PieDataSet set, PieChart pieChart){
            set.setColors(ColorTemplate.COLORFUL_COLORS);
            PieData data = new PieData(set);
            pcExpenditure.setData(data);
            pcExpenditure.animateXY(5000,5000);
            pcExpenditure.invalidate();
    }
}