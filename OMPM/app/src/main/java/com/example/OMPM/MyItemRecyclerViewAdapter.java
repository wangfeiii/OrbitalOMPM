package com.example.OMPM;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.NumberFormat;
import java.util.List;

public class MyItemRecyclerViewAdapter extends RecyclerView.Adapter<MyItemRecyclerViewAdapter.ViewHolder> {

    private List<Debt> mValues;

    public MyItemRecyclerViewAdapter(List<Debt> items) {
        mValues = items;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.owe_me_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        if (mValues.get(position).isPaid()) {
            holder.status.setText("Paid");
            holder.status.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();;
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();;
                    final DatabaseReference ref = mDatabase.child("debts").child(mValues.get(position).getKey()).child("debtors");
                    ref.child(mValues.get(position).getNumber()).child("paid").setValue(false);
                    holder.status.setText("Unpaid");
                }
            });
        }
        else {
            holder.status.setText("Unpaid");
            holder.status.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();;
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();;
                    final DatabaseReference ref = mDatabase.child("debts").child(mValues.get(position).getKey()).child("debtors");
                    ref.child(mValues.get(position).getNumber()).child("paid").setValue(true);
                    holder.status.setText("Paid");
                }
            });
        }

        holder.name.setText(mValues.get(position).getName());
        holder.phoneNumber.setText(mValues.get(position).getNumber());
        holder.date.setText(mValues.get(position).getDate());
        NumberFormat formatter = NumberFormat.getCurrencyInstance();
        holder.amount.setText(formatter.format(Float.parseFloat(mValues.get(position).getAmount())));
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public final TextView name;
        public final TextView phoneNumber;
        public final TextView date;
        public final TextView amount;
        public Button status;

        public ViewHolder(View view) {
            super(view);
            name = view.findViewById(R.id.Name);
            phoneNumber = view.findViewById(R.id.phoneNumber);
            date = view.findViewById(R.id.date);
            amount = view.findViewById(R.id.amount);
            status = view.findViewById(R.id.status);
        }
/*
        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }

 */
    }
}