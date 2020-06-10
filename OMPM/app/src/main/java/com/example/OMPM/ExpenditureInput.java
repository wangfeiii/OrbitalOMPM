package com.example.OMPM;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
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
import java.util.Locale;

public class ExpenditureInput extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private static final String TAG = "LOG_TAG";

    private FirebaseUser user;
    private DatabaseReference mDatabase;
    private String userId;

    private Spinner sExpenditureChoices;

    private Transaction newTransaction;
    private String date;
    private String expenditureChoice;
    private String item;
    private String cost;


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
                new DatePickerDialog(ExpenditureInput.this,
                        date,
                        myCalendar.get(Calendar.YEAR),
                        myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
    }

    public void updateLabel(Calendar myCalendar){
        TextView tvDate = findViewById(R.id.date);

        String format = "dd/MM/YYYY";
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.ENGLISH);
        tvDate.setText(sdf.format(myCalendar.getTime()));
        date = sdf.format(myCalendar.getTime());

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        expenditureChoice = parent.getItemAtPosition(position).toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    public void confirm(View view) {

        EditText eItem = findViewById(R.id.editText_Item);
        item = eItem.getText().toString();
        EditText eCost = findViewById(R.id.editText_Cost);
        cost = eCost.getText().toString();

        final DatabaseReference expenditureReference = mDatabase.child("users").child(userId).child("Expenditures");

        //Record transaction;
        newTransaction = new Transaction(
                date,
                expenditureChoice,
                item,
                cost);

        expenditureReference.push().setValue(newTransaction);

        finish();
    }
}