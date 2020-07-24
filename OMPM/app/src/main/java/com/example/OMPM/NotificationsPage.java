package com.example.OMPM;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class NotificationsPage extends AppCompatActivity {
    private static final String TAG = "LOG_TAG";

    private RecyclerView mRecyclerView;
    private NotificationListAdapter mAdapter;
    ArrayList<Debt> mDebtList = new ArrayList<>();
    private String phoneNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);


        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        phoneNumber = user.getPhoneNumber();

        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds: dataSnapshot.child("users").child(user.getUid()).child("owedTo").getChildren()) {
                    String key = ds.getKey();
                    DataSnapshot sc = dataSnapshot.child("debts").child(key);
                    Log.d(TAG, String.valueOf(sc.child("debtors").child(phoneNumber).child("paid").getValue()));
//                    if (sc.child("debtors").child(phoneNumber).child("paid").getValue().equals(false)) {
//                        String num = String.valueOf(sc.child("creditor").child("phone").getValue());
//                        String name = String.valueOf(sc.child("creditor").child("name").getValue());
//                        String paid = String.valueOf(sc.child("debtors").child(user.getPhoneNumber()).child("paid").getValue());
//                        Debt debt = new Debt(key,String.valueOf(sc.child("amount").getValue()), String.valueOf(sc.child("date").getValue()), num,name,Boolean.parseBoolean(paid));
//                        mDebtList.add(debt);
//                    }
                }
                mRecyclerView = findViewById(R.id.recyclerview);
                mAdapter = new NotificationListAdapter(mRecyclerView.getContext(), mDebtList);
                mRecyclerView.setLayoutManager(new LinearLayoutManager(mRecyclerView.getContext()));
                DividerItemDecoration itemDecor = new DividerItemDecoration(mRecyclerView.getContext(), DividerItemDecoration.VERTICAL);
                mRecyclerView.addItemDecoration(itemDecor);
                mRecyclerView.setAdapter(mAdapter);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });

    }
}