package com.example.OMPM;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;

import java.util.LinkedList;

public class SplitBill extends AppCompatActivity {
    public static final int PICK_CONTACT = 1;

    private final LinkedList<String> mItemsList = new LinkedList<>();
    private RecyclerView mRecyclerView;
    private WordListAdapter mAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_split_bill);
        mRecyclerView = findViewById(R.id.recyclerView);
        mAdapter = new WordListAdapter(this, mItemsList);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        (findViewById(R.id.fab_AddItems)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                startActivityForResult(i, PICK_CONTACT);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_CONTACT && resultCode == RESULT_OK) {
            Uri contactUri = data.getData();
            Cursor cursor = getContentResolver().query(contactUri, null, null, null, null);
            cursor.moveToFirst();
            int column = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
            Log.d("phone number", cursor.getString(column));
        }
    }

    public void onCheckboxClicked(View view) {
        CheckBox mCheckboxSplit = (CheckBox) findViewById(R.id.checkBox_splitType);
        if (mCheckboxSplit.isChecked()){
            findViewById(R.id.editText_Amount).setVisibility(View.VISIBLE);
        }
        else{
            findViewById(R.id.editText_Amount).setVisibility(View.INVISIBLE);
        }
    }
}