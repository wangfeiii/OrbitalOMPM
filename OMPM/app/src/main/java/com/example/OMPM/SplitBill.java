package com.example.OMPM;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.InputFilter;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

public class SplitBill extends AppCompatActivity implements AdapterView.OnItemSelectedListener{

    private static final int PERMISSIONS_REQUEST = 100;

    private final ArrayList<Debt> mItemsList = new ArrayList<>();
    private RecyclerView mRecyclerView;
    private WordListAdapter mAdapter;
    private UnequalAdapter adapter;
    double  indivBill;
    private String amount;
    private Uri contactUri;
    private String contactID;     // contacts unique ID
    private String contactNumber = "";
    private String[] contactListArray;
    private List<Contact> contact_list = new ArrayList<>();
    boolean[] selected;
    private List<Contact> selected_list = new ArrayList<>();
    private String myName;
    private int noOfPeople;
    private CheckBox gst;
    private CheckBox sc;
    private CheckBox myself;
    private EditText bill;
    private EditText thing;
    private EditText me;
    private String item;
    private Spinner sExpenditureChoices;
    private Expenditure editable;
    private String expenditureType;
    private Expenditure newExpenditure;
    private String monthDate;
    private String monthsaveDate;
    private Long timestampDate;
    private EditText myShare;
    private TextView total;
    private int tot;
    private boolean filled;

    private DatabaseReference mDatabase;
    private FirebaseUser user;
    private double bill_amount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_split_bill);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        user = FirebaseAuth.getInstance().getCurrentUser();

        int permission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_CONTACTS);
        if (permission!= PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS},
                    PERMISSIONS_REQUEST);
        } else {
            //initialize widgets
            final RadioGroup radioGroup = findViewById(R.id.toggleGroup);
            gst = findViewById(R.id.checkBox_GST);
            sc = findViewById(R.id.Service_Charge);
            myself = findViewById(R.id.myself);
            me = findViewById(R.id.name);
            me.setText(user.getDisplayName());
            thing = findViewById(R.id.thing);
            myShare = findViewById(R.id.myShare);
            myShare.setFilters(new InputFilter[]{ new InputFilterMinMax("1", "100", getApplicationContext())});
            total = findViewById(R.id.total);
            findViewById(R.id.calc).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    tot = 0;
                    checkBlank();
                    setTextView(String.valueOf(tot));
                }
            });

            //Spinner Stuff
            sExpenditureChoices = findViewById(R.id.spinner_ExpenditureChoice);
            ArrayAdapter<CharSequence> exAdapter = ArrayAdapter.createFromResource(this,
                    R.array.expenditure_choice_array, android.R.layout.simple_spinner_item);
            exAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            if (sExpenditureChoices != null) {
                sExpenditureChoices.setAdapter(exAdapter);
                sExpenditureChoices.setOnItemSelectedListener(this);
            }
            sExpenditureChoices.setSelection(getIndex(sExpenditureChoices, expenditureType));


            bill = findViewById(R.id.input);
            bill.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(99,2)});
            retrieveContactNumber();

            int id = radioGroup.getCheckedRadioButtonId();
            switch (id) {
                case R.id.btn1:
                    findViewById(R.id.perc).setVisibility(View.GONE);
                    myShare.setVisibility(View.GONE);
                    total.setVisibility(View.INVISIBLE);
                    findViewById(R.id.calc).setVisibility(View.INVISIBLE);
                    equal();
                    break;

                case R.id.btn2:
                    myself.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            if (isChecked) {
                                myShare.setVisibility(View.VISIBLE);
                                findViewById(R.id.perc).setVisibility(View.VISIBLE);
                            } else {
                                myShare.setVisibility(View.INVISIBLE);
                                findViewById(R.id.perc).setVisibility(View.INVISIBLE);
                            }
                        }
                    });
                    unequal();
                    break;
            }

            radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    switch (checkedId) {
                        case R.id.btn1:
                            clear();
                            myself.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                @Override
                                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                    if (isChecked) {
                                        myShare.setVisibility(View.INVISIBLE);
                                        findViewById(R.id.perc).setVisibility(View.INVISIBLE);
                                    } else {
                                        myShare.setVisibility(View.INVISIBLE);
                                        findViewById(R.id.perc).setVisibility(View.INVISIBLE);
                                    }
                                }
                            });
                            myShare.setVisibility(View.GONE);
                            findViewById(R.id.perc).setVisibility(View.GONE);
                            total.setVisibility(View.INVISIBLE);
                            findViewById(R.id.calc).setVisibility(View.INVISIBLE);
                            adapter.notifyDataSetChanged();
                            equal();
                            break;

                        case R.id.btn2:
                            clear();
                            total.setVisibility(View.VISIBLE);
                            findViewById(R.id.calc).setVisibility(View.VISIBLE);
                            myself.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                @Override
                                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                    if (isChecked) {
                                        myShare.setVisibility(View.VISIBLE);
                                        findViewById(R.id.perc).setVisibility(View.VISIBLE);
                                    } else {
                                        myShare.setVisibility(View.INVISIBLE);
                                        findViewById(R.id.perc).setVisibility(View.INVISIBLE);
                                    }
                                }
                            });
                            mAdapter.notifyDataSetChanged();
                            unequal();
                            break;
                    }
                }
            });

            findViewById(R.id.Confirm).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    myName = me.getText().toString();
                    item = thing.getText().toString();
                    amount = bill.getText().toString();
                    checkBlank();
                    setTextView(String.valueOf(tot));
                    if (!filled) {
                        return;
                    }
                    if (amount.isEmpty()) {
                        Toast.makeText(getApplicationContext(), "Please key in amount!",
                                Toast.LENGTH_SHORT).show();
                        return;
                    } else if (selected_list.size()<1) {
                        Toast.makeText(getApplicationContext(), "Please choose debtors!",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    bill_amount = Double.parseDouble(amount);

                    if (sc.isChecked())
                        bill_amount = bill_amount * 1.1;
                    if (gst.isChecked())
                        bill_amount = bill_amount * 1.07;

                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd");
                    String dateToday = simpleDateFormat.format(new Date());

                    //date for expenditure
                    String format = "dd/MMMM/YYYY";
                    SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.ENGLISH);
                    String monthFormat = "YYYY/MMM";
                    SimpleDateFormat monthSDF = new SimpleDateFormat(monthFormat, Locale.ENGLISH);
                    monthDate = monthSDF.format(Calendar.getInstance().getTime());
                    Date date = null;
                    try {
                        date = new SimpleDateFormat("yyyy/MMM").parse(monthDate);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    monthsaveDate = Long.toString(date.getTime());
                    timestampDate = Calendar.getInstance().getTime().getTime();

                    int id = radioGroup.getCheckedRadioButtonId();
                    switch (id) {
                        case R.id.btn1:
                            if (myself.isChecked())
                                noOfPeople = selected_list.size() +1;
                            else
                                noOfPeople = selected_list.size();

                            indivBill = bill_amount/noOfPeople;

                            final String key = mDatabase.child("debts").push().getKey();
                            mDatabase.child("debts").child(key).child("date").setValue(dateToday);
                            mDatabase.child("debts").child(key).child("amount").setValue(indivBill);
                            for (Contact ct: selected_list)
                                mDatabase.child("debts").child(key).child("debtors").child(ct.getPhone().replaceAll("\\s","")).setValue(new Contact(ct.getName(),null));
                            mDatabase.child("debts").child(key).child("creditor").setValue(new Contact(myName, user.getPhoneNumber().replaceAll("\\s","")));
                            mDatabase.child("users").child(user.getUid()).child("owedBy").child(key).setValue(true);

                            for (int i = 0; i<selected_list.size(); i++) {
                                mDatabase.child("users").orderByChild("phoneNumber").equalTo((selected_list.get(i).getPhone()).replaceAll("\\s","")).addListenerForSingleValueEvent(new ValueEventListener() {

                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        for (DataSnapshot ds: dataSnapshot.getChildren()) {
                                            String anotherKey = ds.getKey();
                                            mDatabase.child("users").child(anotherKey).child("owedTo").child(key).setValue(true);
                                            DatabaseReference expenditureDateReference = mDatabase.child("users").child(anotherKey).child("Expenditures").child(monthDate);
                                            String expKey = expenditureDateReference.push().getKey();
                                            //Creates new Expenditure;
                                            newExpenditure = new Expenditure(
                                                    timestampDate,
                                                    expenditureType,
                                                    item,
                                                    String.valueOf(indivBill),
                                                    expKey);
                                            //Puts the expenditure at /user/userID/Expenditures/YYYY/mm/
                                            Map<String, Object> expenditureValues = newExpenditure.toMap();

                                            Map<String, Object> childUpdates = new HashMap<>();
                                            childUpdates.put("/users/" + anotherKey + "/Expenditures/" + monthDate + "/" + expKey, newExpenditure);
                                            childUpdates.put("/users/" + anotherKey + "/ExpenditureDates/" + monthsaveDate, true);
                                            mDatabase.updateChildren(childUpdates);

                                        }
                                    }
                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) { }
                                });
                            }

                            if (myself.isChecked()) {
                                //add expenditure to myself
                                DatabaseReference expenditureDateReference = mDatabase
                                        .child("users")
                                        .child(user.getUid())
                                        .child("Expenditures")
                                        .child(monthDate);
                                String expKey = expenditureDateReference.push().getKey();
                                //Creates new Expenditure;
                                newExpenditure = new Expenditure(
                                        timestampDate,
                                        expenditureType,
                                        item,
                                        String.valueOf(indivBill),
                                        expKey);
                                //Puts the expenditure at /user/userID/Expenditures/YYYY/mm/
                                Map<String, Object> expenditureValues = newExpenditure.toMap();

                                Map<String, Object> childUpdates = new HashMap<>();
                                childUpdates.put("/users/" + user.getUid() + "/Expenditures/" + monthDate + "/" + expKey, newExpenditure);
                                childUpdates.put("/users/" + user.getUid() + "/ExpenditureDates/" + monthsaveDate, true);
                                mDatabase.updateChildren(childUpdates);
                            }
                            clear();
                            mAdapter.notifyDataSetChanged();
                            break;

                        case R.id.btn2:
                            if (tot>100) {
                                Toast.makeText(getApplicationContext(), "Total percentage cannot be more than 100!", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            if (myself.isChecked()) {
                                DatabaseReference expenditureDateReference = mDatabase
                                        .child("users")
                                        .child(user.getUid())
                                        .child("Expenditures")
                                        .child(monthDate);
                                String expKey = expenditureDateReference.push().getKey();
                                //Creates new Expenditure;
                                newExpenditure = new Expenditure(
                                        timestampDate,
                                        expenditureType,
                                        item,
                                        String.valueOf(bill_amount*(Double.parseDouble(myShare.getText().toString())/100)),
                                        expKey);
                                //Puts the expenditure at /user/userID/Expenditures/YYYY/mm/
                                Map<String, Object> expenditureValues = newExpenditure.toMap();

                                Map<String, Object> childUpdates = new HashMap<>();
                                childUpdates.put("/users/" + user.getUid() + "/Expenditures/" + monthDate + "/" + expKey, newExpenditure);
                                childUpdates.put("/users/" + user.getUid() + "/ExpenditureDates/" + monthsaveDate, true);
                                mDatabase.updateChildren(childUpdates);
                            }

                            for (Contact ct: selected_list) {
                                final String keytwo = mDatabase.child("debts").push().getKey();
                                mDatabase.child("debts").child(keytwo).child("date").setValue(dateToday);
                                mDatabase.child("debts").child(keytwo).child("creditor").setValue(new Contact(myName, user.getPhoneNumber().replaceAll("\\s", "")));
                                mDatabase.child("debts").child(keytwo).child("debtors").child(ct.getPhone().replaceAll("\\s", "")).setValue(new Contact(ct.getName(), null));
                                final double expenses = bill_amount*(Double.parseDouble(ct.getPercentage())/100);
                                mDatabase.child("debts").child(keytwo).child("amount").setValue(expenses);
                                mDatabase.child("users").child(user.getUid()).child("owedBy").child(keytwo).setValue(true);
                                mDatabase.child("users").orderByChild("phoneNumber").equalTo((ct.getPhone()).replaceAll("\\s","")).addListenerForSingleValueEvent(new ValueEventListener() {

                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        for (DataSnapshot ds: dataSnapshot.getChildren()) {
                                            String anotherKey = ds.getKey();
                                            mDatabase.child("users").child(anotherKey).child("owedTo").child(keytwo).setValue(true);
                                            DatabaseReference expenditureDateReference = mDatabase.child("users").child(anotherKey).child("Expenditures").child(monthDate);
                                            String expKey = expenditureDateReference.push().getKey();
                                            //Creates new Expenditure;
                                            newExpenditure = new Expenditure(
                                                    timestampDate,
                                                    expenditureType,
                                                    item,
                                                    String.valueOf(expenses),
                                                    expKey);
                                            //Puts the expenditure at /user/userID/Expenditures/YYYY/mm/
                                            Map<String, Object> expenditureValues = newExpenditure.toMap();

                                            Map<String, Object> childUpdates = new HashMap<>();
                                            childUpdates.put("/users/" + anotherKey + "/Expenditures/" + monthDate + "/" + expKey, newExpenditure);
                                            childUpdates.put("/users/" + anotherKey + "/ExpenditureDates/" + monthsaveDate, true);
                                            mDatabase.updateChildren(childUpdates);
                                        }
                                    }
                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) { }
                                });
                            }
                            clear();
                            adapter.notifyDataSetChanged();
                            break;
                    }
                    Toast.makeText(getApplicationContext(), "Debt Updated!", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void setTextView(String x) {
        total.setText("Total Percentage: " + x + "%");
    }

    private void clear() {
        bill.setText("");
        gst.setChecked(false);
        sc.setChecked(false);
        myself.setChecked(false);
        selected_list.clear();
        thing.setText("");
    }

    private void checkBlank() {
        for (Contact c:selected_list) {
            try {
                tot = tot + Integer.parseInt(c.getPercentage());
                filled = true;
            } catch (NumberFormatException e) {
                Toast.makeText(getApplicationContext(), "Please do not leave blanks", Toast.LENGTH_SHORT).show();
                filled = false;
            }
        }
    }

    private void equal() {
        mAdapter = new WordListAdapter(selected_list);

        (findViewById(R.id.fab_AddItems)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selected = new boolean[contactListArray.length];
                AlertDialog.Builder builder = new AlertDialog.Builder(SplitBill.this);
                builder.setCancelable(true)
                        .setTitle("Select Contacts")
                        .setMultiChoiceItems(contactListArray, selected, new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int pos, boolean isChecked) {
                                selected[pos] = isChecked;
                            }
                        })
                        .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                for (int i = 0; i < selected.length; i++) {
                                    if (selected[i]) {
                                        selected_list.add(contact_list.get(i));
                                    }
                                }
                                dialog.dismiss();
                                RecyclerView mRecyclerView = findViewById(R.id.item_list);
                                mRecyclerView.setAdapter(mAdapter);
                                mRecyclerView.setLayoutManager(new LinearLayoutManager(mRecyclerView.getContext()));
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
    }

    public void unequal() {

        adapter = new UnequalAdapter(selected_list, getApplicationContext());

        (findViewById(R.id.fab_AddItems)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selected = new boolean[contactListArray.length];
                AlertDialog.Builder builder = new AlertDialog.Builder(SplitBill.this);
                builder.setCancelable(true)
                        .setTitle("Select Contacts")
                        .setMultiChoiceItems(contactListArray, selected, new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int pos, boolean isChecked) {
                                selected[pos] = isChecked;
                            }
                        })
                        .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                for (int i = 0; i < selected.length; i++) {
                                    if (selected[i]) {
                                        selected_list.add(contact_list.get(i));
                                    }
                                }
                                dialog.dismiss();
                                RecyclerView mRecyclerView = findViewById(R.id.item_list);
                                mRecyclerView.setAdapter(adapter);
                                mRecyclerView.setLayoutManager(new LinearLayoutManager(mRecyclerView.getContext()));
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
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


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        expenditureType = parent.getItemAtPosition(position).toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) { }

    private void retrieveContactNumber() {

        Cursor cursor = getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);

        while (cursor.moveToNext()) {
            String contactID = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
            String hasPhone = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
            String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
            if ("1".equals(hasPhone) || Boolean.parseBoolean(hasPhone)) {
                // Using the contact ID now we will get contact phone number
                Cursor cursorPhone = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null
                        , ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactID
                        , null
                        , null);

                while (cursorPhone.moveToNext()) {
                    contactNumber = cursorPhone.getString(cursorPhone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    Contact contact = new Contact(name, contactNumber);
                    contact_list.add(contact);
                }
                cursorPhone.close();
            }
        }
        contactListArray = new String[contact_list.size()];
        for (int i = 0; i<contact_list.size();i++) {
            contactListArray[i] = contact_list.get(i).toList();
        }
    }
}

