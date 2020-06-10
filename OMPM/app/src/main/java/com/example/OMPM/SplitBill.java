package com.example.OMPM;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
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

public class SplitBill extends AppCompatActivity {
    public static final int PICK_CONTACT = 1;
    private static final int PERMISSIONS_REQUEST = 100;
    private static final String TAG = SplitBill.class.getSimpleName();

    private final ArrayList<String> mItemsList = new ArrayList<>();
    private RecyclerView mRecyclerView;
    private WordListAdapter mAdapter;
    double bill_amount, bil_split;
    int noOfPeople;
    private Uri contactUri;
    private String contactID;     // contacts unique ID
    private String contactNumber = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_split_bill);

        getPermissionToReadUserContacts();

        //initialize widgets
        final EditText bill = findViewById(R.id.editText_Amount);
        final EditText no = findViewById(R.id.editTextNumber);
        final TextView resultView = findViewById(R.id.result);
        Button split = findViewById(R.id.split);
        final CheckBox gst = findViewById(R.id.checkBox_GST);
        final CheckBox sc = findViewById(R.id.Service_Charge);

        RecyclerView mRecyclerView = findViewById(R.id.item_list);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        final WordListAdapter mAdapter = new WordListAdapter(mItemsList);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        split.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                double bill_amount = Double.parseDouble(bill.getText().toString());

                if (sc.isChecked())
                    bill_amount = bill_amount * 1.1;
                if (gst.isChecked())
                    bill_amount = bill_amount * 1.07;

                noOfPeople = Integer.parseInt(no.getText().toString());
                bil_split = bill_amount/noOfPeople;
                DecimalFormat currency = new DecimalFormat("$###,###.##");
                resultView.setText("Each: " + currency.format(bil_split));
                mItemsList.add(contactNumber + ": " + currency.format(bil_split));
                mAdapter.notifyItemInserted(mItemsList.size()-1);
            }
        });

        (findViewById(R.id.fab_AddItems)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                startActivityForResult(i, PICK_CONTACT);
            }
        });
    }

    public void getPermissionToReadUserContacts() {
        //Check whether this app has access to the location permission//
        int permission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_CONTACTS);
        if (permission!= PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS},
                    PERMISSIONS_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_CONTACT && resultCode == RESULT_OK) {
            Uri contactUri = data.getData();
            retrieveContactNumber(contactUri);

        }
    }

    private void retrieveContactNumber(Uri contactUri) {

        Cursor cursorID = getContentResolver().query(contactUri, null, null, null, null);

        if (cursorID.moveToFirst()) {
            contactID = cursorID.getString(cursorID.getColumnIndex(ContactsContract.Contacts._ID));
        }

        Log.d(TAG, "Contact ID: " + contactID);
        cursorID.close();

        // Using the contact ID now we will get contact phone number
        Cursor cursorPhone = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactID, null, null);

        while (cursorPhone.moveToNext()) {
            contactNumber = cursorPhone.getString(cursorPhone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
        }

        cursorPhone.close();
        Log.d(TAG, "Contact Phone Number: " + contactNumber);
        Toast toast = Toast.makeText(getApplicationContext(), contactNumber, Toast.LENGTH_SHORT);
        toast.show();
    }
}