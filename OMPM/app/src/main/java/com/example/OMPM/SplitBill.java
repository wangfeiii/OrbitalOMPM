package com.example.OMPM;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class SplitBill extends AppCompatActivity {
    private static final int PERMISSIONS_REQUEST = 100;
    private static final String TAG = SplitBill.class.getSimpleName();

    private final ArrayList<String> mItemsList = new ArrayList<>();
    private RecyclerView mRecyclerView;
    private WordListAdapter mAdapter;
    double bill_amount, indivBill;
    private Uri contactUri;
    private String contactID;     // contacts unique ID
    private String contactNumber = "";
    String [] contactListArray;
    List<String> contact_list = new ArrayList<>();
    boolean[] selected;
    List<String> selected_list;
    private int noOfpeople = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_split_bill);

        getPermissionToReadUserContacts();

        //initialize widgets
        final TextView resultView = findViewById(R.id.result);

        RecyclerView mRecyclerView = findViewById(R.id.item_list);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        final WordListAdapter mAdapter = new WordListAdapter(mItemsList);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        retrieveContactNumber();
        (findViewById(R.id.fab_AddItems)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.alert_dialog_layout,null);
                AlertDialog.Builder firstBuilder = new AlertDialog.Builder(SplitBill.this);
                firstBuilder.setTitle("Input Amount");
                firstBuilder.setPositiveButton("Next", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        CheckBox gst = view.findViewById(R.id.checkBox_GST);
                        CheckBox sc = view.findViewById(R.id.Service_Charge);
                        CheckBox myself = view.findViewById(R.id.myself);

                        EditText bill = view.findViewById(R.id.input);
                        bill_amount = Double.parseDouble(bill.getText().toString());

                        if (sc.isChecked())
                            bill_amount = bill_amount * 1.1;
                        if (gst.isChecked())
                            bill_amount = bill_amount * 1.07;
                        if (myself.isChecked())
                            noOfpeople = 1;

                        //second dialog
                        showDialog();
                    }
                });
                AlertDialog dialog = firstBuilder.create();
                dialog.setView(view);
                dialog.show();
            }
        });
    }

    //second dialog to choose contact
    private void showDialog() {
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

                        DecimalFormat currency = new DecimalFormat("$###,###.##");
                        List<String> selected_list = new ArrayList<>();

                        for (int i = 0; i < selected.length; i++) {
                            if (selected[i]) {
                                selected_list.add(contactListArray[i]);
                            }
                        }
                        noOfpeople = noOfpeople + selected_list.size();
                        indivBill = bill_amount/noOfpeople;

                        for (int i = 0; i<selected.length; i++) {
                            if (selected[i]) {
                                mItemsList.add(contactListArray[i] + ": " + currency.format(indivBill));
                            }
                        }
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public void getPermissionToReadUserContacts() {
        //Check whether this app has access to the contacts permission//
        int permission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_CONTACTS);
        if (permission!= PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS},
                    PERMISSIONS_REQUEST);
    }

    private void retrieveContactNumber() {

        Cursor cursor = getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        while (cursor.moveToNext()) {
            String contactID = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
            String hasPhone = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
            String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
            if ("1".equals(hasPhone) || Boolean.parseBoolean(hasPhone)) {
                // You know it has a number so now query it like this

                Log.d(TAG, "Contact ID: " + contactID);

                // Using the contact ID now we will get contact phone number
                Cursor cursorPhone = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactID, null, null);

                while (cursorPhone.moveToNext()) {
                    contactNumber = cursorPhone.getString(cursorPhone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    contact_list.add(name + ": " + contactNumber);
                }
                cursorPhone.close();
                Log.d(TAG, "Contact Phone Number: " + contactNumber);
            }
        }
        contactListArray = contact_list.toArray(new String[contact_list.size()]);
    }
}