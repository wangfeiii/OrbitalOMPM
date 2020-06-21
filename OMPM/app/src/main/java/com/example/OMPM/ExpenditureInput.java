package com.example.OMPM;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

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
    public static final String EXTRA_DATE = "com.example.OMPM.extra.REPLY";

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

    private EditText eItem;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expenditure_input);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        user = FirebaseAuth.getInstance().getCurrentUser();
        userId = user.getUid();


        sExpenditureChoices = findViewById(R.id.spinner_ExpenditureChoice);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.expenditure_choice_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        if (sExpenditureChoices != null) {
            sExpenditureChoices.setAdapter(adapter);
            sExpenditureChoices.setOnItemSelectedListener(this);
        }

        final Calendar myCalendar = Calendar.getInstance();

        ImageView datePicker = findViewById(R.id.image_DatePicker);

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

        eItem = findViewById(R.id.editText_Item);
        //<! TODO: Figure out how to input currency nicely >
        eItem.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
            private String current ="";
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().equals(current)){
                    eItem.removeTextChangedListener(this);

                }
            }

            @Override
            public void afterTextChanged(Editable s) {

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

        Log.d(TAG, monthsaveDate + " " + monthDate);

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

        Intent replyIntent = new Intent();
        replyIntent.putExtra(EXTRA_DATE, spinnerDate);
        setResult(RESULT_OK, replyIntent);
        Log.d(TAG, "New Expenditure added");
        finish();
    }
}