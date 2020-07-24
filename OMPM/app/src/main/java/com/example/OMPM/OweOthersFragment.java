package com.example.OMPM;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link OweOthersFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class OweOthersFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;
    private List<Debt> debtList;

    public OweOthersFragment() {
        // Required empty public constructor
        }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment OweOthersFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static OweOthersFragment newInstance(String param1, String param2) {
        OweOthersFragment fragment = new OweOthersFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_owe_others, container, false);
        final RecyclerView recyclerView = view.findViewById(R.id.recyclerview);
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), LinearLayoutManager.VERTICAL));
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                debtList = new ArrayList<>();
                for (DataSnapshot ds: dataSnapshot.child("users").child(user.getUid()).child("owedTo").getChildren()) {
                    String key = ds.getKey();
                    DataSnapshot sc = dataSnapshot.child("debts").child(key);
                    String num = String.valueOf(sc.child("creditor").child("phone").getValue());
                    String name = String.valueOf(sc.child("creditor").child("name").getValue());
                    String paid = String.valueOf(sc.child("debtors").child(user.getPhoneNumber().replaceAll("\\s","")).child("paid").getValue());
                    Debt debt = new Debt(key,String.valueOf(sc.child("amount").getValue()), String.valueOf(sc.child("date").getValue()), num,name,Boolean.parseBoolean(paid));
                    debtList.add(debt);
                }
                recyclerView.setAdapter(new OweOthersAdapter(debtList));
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });

        return view;
    }
}