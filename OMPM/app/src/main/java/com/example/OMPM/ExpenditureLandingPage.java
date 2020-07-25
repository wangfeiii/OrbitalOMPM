package com.example.OMPM;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.renderscript.Sampler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
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
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
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

import java.text.NumberFormat;
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
    public static final String EXTRA_FLAG = "com.example.twoactivities.extra.FLAG";

    private FirebaseUser user;
    private DatabaseReference mDatabase;
    private String userId;
    private Date cDate;
    private String currentDate;
    private PieChart pcExpenditure;

    Map<String, Float> dataSet;
    ArrayList<PieEntry> entries;
    private Float tExpenditure;

    private Spinner spinner;
    private LinkedList<String> spinnerArray = new LinkedList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expenditure_landing_page);
        getSupportActionBar().setTitle("Expenditure");

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
        createSpinner();

        (findViewById(R.id.history)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent launchHistory = new Intent(ExpenditureLandingPage.this, ExpenditureHistory.class);
                startActivity(launchHistory);
            }
        });

        pcExpenditure.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                PieEntry pEntry = (PieEntry) e;
                NumberFormat formatter = NumberFormat.getCurrencyInstance();
                String sExpenditure = formatter.format(e.getY());
                pcExpenditure.setCenterText(pEntry.getLabel() + " Expenditure : \n" + sExpenditure);
            }

            @Override
            public void onNothingSelected() {
                NumberFormat formatter = NumberFormat.getCurrencyInstance();
                String sExpenditure = formatter.format(tExpenditure);
                pcExpenditure.setCenterText("Total Expenditure : \n" + sExpenditure);
            }
        });
    }
/*
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.expenditure_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case R.id.action_history:
                Intent launchHistory = new Intent(ExpenditureLandingPage.this, ExpenditureHistory.class);
                startActivity(launchHistory);
                return true;

            default:
        }
        return super.onOptionsItemSelected(item);
    }
 */

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
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
                tExpenditure = 0f;
                if (dataSnapshot.exists()) {
                    pcExpenditure.setVisibility(View.VISIBLE);
                    TextView noData = findViewById(R.id.text_noData);
                    noData.setVisibility(View.INVISIBLE);
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
        tExpenditure += cost;
        if (dataSet.containsKey(type)) {
            cost += dataSet.get(type);
        }
        dataSet.put(type, cost);
    }

    private void createPieChart(PieDataSet set, PieChart pieChart){
        pcExpenditure.setDrawHoleEnabled(true);
        pcExpenditure.setHoleColor(Color.WHITE);
        pcExpenditure.setTransparentCircleRadius(Color.WHITE);
        pcExpenditure.setTransparentCircleAlpha(90);
        pcExpenditure.setHoleRadius(90f);
        pcExpenditure.setDrawCenterText(true);
        set.setColors(ColorTemplate.MATERIAL_COLORS);
        PieData data = new PieData(set);
        data.setValueTextSize(12f);
        set.setYValuePosition(null);
        set.setLabel("");
        pcExpenditure.setRotationEnabled(false);
        pcExpenditure.getDescription().setEnabled(false);
        pcExpenditure.setData(data);
        pcExpenditure.animateXY(500,500);
        pcExpenditure.setDrawEntryLabels(false);
        NumberFormat formatter = NumberFormat.getCurrencyInstance();
        String sExpenditure = formatter.format(tExpenditure);
        pcExpenditure.setCenterText("Total Expenditure : \n" + sExpenditure);
        pcExpenditure.setCenterTextSize(20f);
        pcExpenditure.setExtraBottomOffset(20f);
        pcExpenditure.setExtraLeftOffset(20f);
        pcExpenditure.setExtraRightOffset(20f);
        pcExpenditure.setExtraTopOffset(20f);
        pcExpenditure.invalidate();
    }

    public void launchExpenditureInput(View view) {
        Intent iLaunchExpenditureInput = new Intent(this, ExpenditureInput.class);
        iLaunchExpenditureInput.putExtra(EXTRA_FLAG, "LandingPage");
        startActivityForResult(iLaunchExpenditureInput, TEXT_REQUEST);
    }

    public void launchExpenditureHistory(View view) {
        Intent iLaunchExpenditureHistory = new Intent(this, ExpenditureHistory.class);
        startActivity(iLaunchExpenditureHistory);
    }
}