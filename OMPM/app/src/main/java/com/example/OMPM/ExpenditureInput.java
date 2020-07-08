package com.example.OMPM;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

//! TODO: Prevent user from adding more than 2 dp for currency
public class ExpenditureInput extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private static final String TAG = "LOG_TAG";

    private FirebaseUser user;
    private DatabaseReference mDatabase;
    private String userId;

    private Spinner sExpenditureChoices;

    private Expenditure newExpenditure;
    private Long timestampDate;
    private String expenditureType;
    private String item;
    private String cost;
    private String monthDate;
    private String monthsaveDate;
    private String spinnerDate;
    final Calendar myCalendar = Calendar.getInstance();

    private Expenditure editable;
    private String eKey;
    private String flag;
    private Date bDate;
    private Date dMonth;

    private EditText eItem;
    private EditText eCost;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expenditure_input);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        user = FirebaseAuth.getInstance().getCurrentUser();
        userId = user.getUid();

        ActionBar actionBar = getSupportActionBar();

        actionBar.setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        flag = intent.getStringExtra("com.example.twoactivities.extra.FLAG");

        //Date Stuff
        ImageView datePicker = findViewById(R.id.image_DatePicker);
        chooseDate(myCalendar, datePicker);

        //Spinner Stuff
        sExpenditureChoices = findViewById(R.id.spinner_ExpenditureChoice);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.expenditure_choice_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        if (sExpenditureChoices != null) {
            sExpenditureChoices.setAdapter(adapter);
            sExpenditureChoices.setOnItemSelectedListener(this);
        }

        eItem = findViewById(R.id.editText_Item);
        eCost = findViewById(R.id.editText_Cost);
        eCost.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(99,2)});
        if (flag.equals("HistoryPage")){
            editable = getIntent().getParcelableExtra("com.example.twoactivities.extra.EXPENDITURE");
            //Import Date
            Long timestamp = editable.getTimestamp();
            Date tDate = new Date(timestamp);
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MMM/yyyy");
            String tsDate = sdf.format(tDate);
            String month = tsDate.substring(3,6);
            Log.d(TAG, month);
            try {
                dMonth = new SimpleDateFormat("MMM").parse(month);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            Calendar cal = Calendar.getInstance();
            cal.setTime(dMonth);
            int iMonth = cal.get(Calendar.MONTH);

            myCalendar.set(Calendar.YEAR, Integer.parseInt(tsDate.substring(7)));
            Log.d(TAG, tsDate.substring(7));
            myCalendar.set(Calendar.MONTH, iMonth);
            Log.d(TAG, Integer.toString(iMonth));
            myCalendar.set(Calendar.DAY_OF_MONTH,Integer.parseInt(tsDate.substring(0,2)));
            Log.d(TAG, tsDate.substring(0,2));
            try {
                updateLabel(myCalendar);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            timestampDate = timestamp;
            //Import the rest
            expenditureType = editable.getType();
            sExpenditureChoices.setSelection(getIndex(sExpenditureChoices, expenditureType));
            item = editable.getItem();
            eItem.setText(item);
            cost = editable.getCost();
            eCost.setText(cost);
            eKey = editable.getKey();
        }
        //<! TODO: Figure out how to input currency nicely >
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case android.R.id.home:
                if (flag.equals("HistoryPage")){
                    getIntent().removeExtra("com.example.twoactivities.extra.EXPENDITURE");
                }
                getIntent().removeExtra("com.example.twoactivities.extra.FLAG");
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private int getIndex(Spinner sExpenditureChoices, String expenditureType) {
        int index = 0;
        for (int i=0; i<sExpenditureChoices.getCount(); i++){
            if (sExpenditureChoices.getItemAtPosition(i).equals(expenditureType)){
                index = i;
            }
        }
        return index;
    }

    public void chooseDate(final Calendar myCalendar, ImageView datePicker){
        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, month);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                try {
                    updateLabel(myCalendar);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                timestampDate = myCalendar.getTime().getTime();
            }
        };

        datePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog dialog = new DatePickerDialog(ExpenditureInput.this,
                        date,
                        myCalendar.get(Calendar.YEAR),
                        myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH));
                dialog.getDatePicker().setMaxDate(System.currentTimeMillis());
                dialog.show();

            }
        });
    }

    public void updateLabel(Calendar myCalendar) throws ParseException {
        TextView tvDate = findViewById(R.id.date);

        String format = "dd/MMMM/YYYY";
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.ENGLISH);
        tvDate.setText(sdf.format(myCalendar.getTime()));

        String monthFormat = "YYYY/MMM";
        SimpleDateFormat monthSDF = new SimpleDateFormat(monthFormat, Locale.ENGLISH);
        monthDate = monthSDF.format(myCalendar.getTime());
        Date date = new SimpleDateFormat("yyyy/MMM").parse(monthDate);
        monthsaveDate = Long.toString(date.getTime());

        String spinnerFormat = "MMM/YYYY";
        SimpleDateFormat spinnerSDF = new SimpleDateFormat(spinnerFormat);
        spinnerDate = spinnerSDF.format(myCalendar.getTime());

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        expenditureType = parent.getItemAtPosition(position).toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    public void confirm(View view) {

        item = eItem.getText().toString();
        EditText eCost = findViewById(R.id.editText_Cost);
        cost = eCost.getText().toString();

        DatabaseReference expenditureDateReference = mDatabase
                                                .child("users")
                                                .child(userId)
                                                .child("Expenditures")
                                                .child(monthDate);
        String key = expenditureDateReference.push().getKey();
        //Creates new Expenditure;
        newExpenditure = new Expenditure(
                timestampDate,
                expenditureType,
                item,
                cost,
                key);

        //Puts the expenditure at /user/userID/Expenditures/YYYY/mm/
        Map<String, Object> expenditureValues = newExpenditure.toMap();

        Map<String,Object> childUpdates = new HashMap<>();
        childUpdates.put("/users/" + userId + "/Expenditures/" + monthDate + "/" + key, newExpenditure);
        childUpdates.put("/users/" + userId + "/ExpenditureDates/" + monthsaveDate, true);

        mDatabase.updateChildren(childUpdates);

        if (flag.equals("HistoryPage")){
            getIntent().removeExtra("com.example.twoactivities.extra.EXPENDITURE");
            deleteItem(editable);
        }
        getIntent().removeExtra("com.example.twoactivities.extra.FLAG");
        Intent replyIntent = new Intent();
        setResult(RESULT_OK, replyIntent);
        Log.d(TAG, "New Expenditure added");
        finish();
    }

    public void deleteItem(Expenditure item) {
        String itemKey = item.getKey();
        SimpleDateFormat sdf = new SimpleDateFormat("YYYY/MMM");
        Date newDate = new Date(item.getTimestamp());
        final String monthDate = sdf.format(newDate.getTime());
        Log.d(TAG, monthDate);
        try {
            bDate = new SimpleDateFormat("yyyy/MMM").parse(monthDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        final String date = Long.toString(bDate.getTime());
        Log.d(TAG, date);

        mDatabase.child("users")
                .child(userId)
                .child("Expenditures")
                .child(monthDate)
                .child(itemKey)
                .removeValue();


        Query dateQuery = mDatabase.child("users")
                .child(userId)
                .child("Expenditures")
                .child(monthDate);

        dateQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null) {
                    DatabaseReference dateReference = mDatabase.child("users")
                            .child(userId)
                            .child("ExpenditureDates")
                            .child(date);
                    dateReference.removeValue();
                }
            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}