package com.example.OMPM;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.renderscript.Sampler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ExpenditureLandingPage extends AppCompatActivity {
    //! TODO: Add spinner for different filters
    private static final String TAG = "LOG_TAG";
    public static final int TEXT_REQUEST = 1;

    private FirebaseUser user;
    private DatabaseReference mDatabase;
    private String userId;
    private Date cDate;
    private String currentDate;
    private PieChart pcExpenditure;

    Map<String, Float> dataSet;
    ArrayList<PieEntry> entries;

    private Spinner spinner;
    private LinkedList<String> spinnerArray = new LinkedList<>();

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

        dataSet = new HashMap<>();
        entries = new ArrayList<>();
        pcExpenditure = findViewById(R.id.pc_Expenditure);

        // Spinner Stuff
        spinner = findViewById(R.id.spinner_monthly);
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == TEXT_REQUEST){
            if (resultCode == RESULT_OK){
                String newDate = data.getStringExtra(ExpenditureInput.EXTRA_DATE);
                spinnerArray.add(newDate);
            }
        }
    }

    //Pie Chart Stuff
    private void changeDate(String selectedDate){
        //Empty previous Data
        dataSet.clear();
        entries.clear();

        Query myExpenditure = mDatabase
                .child("users")
                .child(userId)
                .child("Expenditures")
                .child(selectedDate);

        myExpenditure.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        Expenditure newExpenditure = ds.getValue(Expenditure.class);
                        createDataSet(newExpenditure);
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
            pcExpenditure.setDrawEntryLabels(false);
            pcExpenditure.invalidate();
    }

    public void launchExpenditureInput(View view) {
        Intent iLaunchExpenditureInput = new Intent(this, ExpenditureInput.class);
        startActivityForResult(iLaunchExpenditureInput, TEXT_REQUEST);
    }

    public void launchExpenditureHistory(View view) {
        Intent iLaunchExpenditureHistory = new Intent(this, ExpenditureHistory.class);
        startActivity(iLaunchExpenditureHistory);
    }
}