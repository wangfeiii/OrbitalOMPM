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
    String [] contactListArray;
    List<String> contact_list = new ArrayList<>();
    boolean[] selected;


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

        retrieveContactNumber();
        selected = new boolean[contactListArray.length];
        (findViewById(R.id.fab_AddItems)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                                String str = "";
                                for (int i = 0; i<selected.length; i++)
                                    if (selected[i])
                                        str = str + contactListArray[i] +" ";

                                resultView.setText(str);
                                dialog.dismiss();
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                /*
                Intent i = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                startActivityForResult(i, PICK_CONTACT);

                 */
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });
    }

    public void getPermissionToReadUserContacts() {
        //Check whether this app has access to the contacts permission//
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
            retrieveContactNumber();
        }
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