package com.example.OMPM;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.OMPM.dummy.DummyContent;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OweMeFragment extends Fragment {

    private RecyclerView recyclerView;
    private MyItemRecyclerViewAdapter mAdapter;
    private List<Debt> debtList = new ArrayList<>();
    private List<String> keyList = new ArrayList<>();

    private DatabaseReference mDatabase;
    private FirebaseUser user;
    View view;

    public OweMeFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static OweMeFragment newInstance(int columnCount) {
        OweMeFragment fragment = new OweMeFragment();
        Bundle args = new Bundle();
       // args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_owe_me, container, false);
        final RecyclerView recyclerView = view.findViewById(R.id.RV);
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), LinearLayoutManager.VERTICAL));
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        mDatabase = FirebaseDatabase.getInstance().getReference();
        user = FirebaseAuth.getInstance().getCurrentUser();
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot ds: dataSnapshot.child("users").child(user.getUid()).child("owedBy").getChildren()) {
                    String key = ds.getKey();
                    DataSnapshot sc = dataSnapshot.child("debts").child(key);

                    for (DataSnapshot debtor : sc.child("debtors").getChildren()) {
                        Map<String, String> map = (HashMap<String, String>)debtor.getValue();
                        Debt debt = new Debt("$" + sc.child("amount").getValue(), String.valueOf(sc.child("date").getValue()), map.get("phone"),map.get("name"));
                        debtList.add(debt);
                    }
                }
                recyclerView.setAdapter(new MyItemRecyclerViewAdapter(debtList));
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });

        return view;
    }
}