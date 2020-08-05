package com.example.OMPM;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.InputFilter;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SplitBill extends AppCompatActivity {
    /*
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private SplitEquallyFragment splitEquallyFragment;
    private SplitUnequallyFragment splitUnequallyFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.split_bill);

        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);
        splitEquallyFragment = new SplitEquallyFragment();
        splitUnequallyFragment = new SplitUnequallyFragment();

        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(this);
        viewPager.setAdapter(viewPagerAdapter);
        TabLayoutMediator tabLayoutMediator = new TabLayoutMediator(tabLayout, viewPager, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                switch (position) {
                    case 0:
                        tab.setText("Split Equally");
                        break;
                    case 1:
                        tab.setText("Split Unequally");
                        break;
                }
            }
        });
        tabLayoutMediator.attach();
    }

    private class ViewPagerAdapter extends FragmentStateAdapter {

        public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0:
                    return new SplitEquallyFragment();
                case 1:
                    return new SplitUnequallyFragment();
                default:
                    return null;
            }
        }

        @Override
        public int getItemCount() {
            return 2;
        }
    }
}
*/

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
            EditText me = findViewById(R.id.name);
            me.setText(user.getDisplayName());
            myName = me.getText().toString();
            bill = findViewById(R.id.input);
            bill.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(99,2)});
            retrieveContactNumber();

            int id = radioGroup.getCheckedRadioButtonId();
            switch (id) {
                case R.id.btn1:
                    equal();
                    break;

                case R.id.btn2:
                    unequal();
                    break;
            }

            radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    switch (checkedId) {
                        case R.id.btn1:
                            clear();
                            adapter.notifyDataSetChanged();
                            equal();
                            break;

                        case R.id.btn2:
                            clear();
                            mAdapter.notifyDataSetChanged();
                            unequal();
                            break;
                    }
                }
            });

            findViewById(R.id.Confirm).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    amount = bill.getText().toString();
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

                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
                    String date = sdf.format(new Date());

                    int id = radioGroup.getCheckedRadioButtonId();
                    switch (id) {
                        case R.id.btn1:
                            if (myself.isChecked())
                                noOfPeople = selected_list.size() +1;
                            else
                                noOfPeople = selected_list.size();

                            indivBill = bill_amount/noOfPeople;

                            final String key = mDatabase.child("debts").push().getKey();
                            mDatabase.child("debts").child(key).child("date").setValue(date);
                            mDatabase.child("debts").child(key).child("amount").setValue(indivBill);
                            for (Contact ct: selected_list)
                                mDatabase.child("debts").child(key).child("debtors").child(ct.getPhone().replaceAll("\\s","")).setValue(new Contact(ct.getName(),null));
                            mDatabase.child("debts").child(key).child("creditor").setValue(new Contact(myName, user.getPhoneNumber().replaceAll("\\s","")));
                            mDatabase.child("users").child(user.getUid()).child("owedBy").child(key).setValue(true);

                            for (int i = 0; i<selected_list.size(); i++) {
                                mDatabase.child("users").orderByChild("phoneNumber").equalTo((selected_list.get(i).getPhone()).replaceAll("\\s","")).addValueEventListener(new ValueEventListener() {

                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        for (DataSnapshot ds: dataSnapshot.getChildren()) {
                                            String anotherKey = ds.getKey();
                                            mDatabase.child("users").child(anotherKey).child("owedTo").child(key).setValue(true);
                                        }
                                    }
                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) { }
                                });
                            }
                            break;

                        case R.id.btn2:

                            for (Contact ct: selected_list) {
                                final String keytwo = mDatabase.child("debts").push().getKey();
                                mDatabase.child("debts").child(keytwo).child("date").setValue(date);
                                mDatabase.child("debts").child(keytwo).child("creditor").setValue(new Contact(myName, user.getPhoneNumber().replaceAll("\\s", "")));
                                mDatabase.child("debts").child(keytwo).child("debtors").child(ct.getPhone().replaceAll("\\s", "")).setValue(new Contact(ct.getName(), null));
                                Log.d("bloop", ct.getPercentage());
                                mDatabase.child("debts").child(keytwo).child("amount").setValue(bill_amount*(Double.parseDouble(ct.getPercentage())/100));
                                mDatabase.child("users").child(user.getUid()).child("owedBy").child(keytwo).setValue(true);
                                mDatabase.child("users").orderByChild("phoneNumber").equalTo((ct.getPhone()).replaceAll("\\s","")).addValueEventListener(new ValueEventListener() {

                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        for (DataSnapshot ds: dataSnapshot.getChildren()) {
                                            String anotherKey = ds.getKey();
                                            mDatabase.child("users").child(anotherKey).child("owedTo").child(keytwo).setValue(true);
                                        }
                                    }
                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) { }
                                });
                            }
                            break;
                    }

                    clear();
                    mAdapter.notifyDataSetChanged();
                    adapter.notifyDataSetChanged();
                    Toast.makeText(getApplicationContext(), "Debt Updated!", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void clear() {
        bill.setText("");
        gst.setChecked(false);
        sc.setChecked(false);
        myself.setChecked(false);
        selected_list.clear();
    }

    private void equal() {
        Toast.makeText(getApplicationContext(), "1", Toast.LENGTH_SHORT).show();

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

        Toast.makeText(getApplicationContext(), "2", Toast.LENGTH_SHORT).show();
        adapter = new UnequalAdapter(selected_list);

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

