package com.example.OMPM;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
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
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

//BIGGEST TO-DO: Make the UI like a messaging app
public class ExpenditureInput extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private static final String TAG = "LOG_TAG";

    private FirebaseUser user;
    private DatabaseReference mDatabase;
    private String userId;

    private Spinner sExpenditureChoices;

    private Expenditure newExpenditure;
    private String date;
    private String expenditureChoice;
    private String item;
    private String cost;
    private String monthDate;
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
                updateLabel(myCalendar);
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
        //TO-DO Figure out how to input currency nicely
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

    public void updateLabel(Calendar myCalendar){
        TextView tvDate = findViewById(R.id.date);

        String format = "dd/MMM/YYYY";
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.ENGLISH);
        tvDate.setText(sdf.format(myCalendar.getTime()));
        date = sdf.format(myCalendar.getTime());

        String monthFormat = "YYYY/MMM/dd";
        SimpleDateFormat monthSDF = new SimpleDateFormat(monthFormat, Locale.ENGLISH);
        monthDate = monthSDF.format(myCalendar.getTime());

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        expenditureChoice = parent.getItemAtPosition(position).toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    public void confirm(View view) {

        item = eItem.getText().toString();
        EditText eCost = findViewById(R.id.editText_Cost);
        cost = eCost.getText().toString();

        DatabaseReference expenditureReference = mDatabase.child("users").child(userId).child("Expenditures").child(monthDate);
        //Creates new Expenditure;
        newExpenditure = new Expenditure(
                date,
                expenditureChoice,
                item,
                cost);

        //Creates an Expenditure at /YYYY/MMM/dd/
        expenditureReference.push().setValue(newExpenditure);

        Log.i("LOG_TAG", "New Expenditure added");
        finish();
    }
}