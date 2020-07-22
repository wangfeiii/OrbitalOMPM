package com.example.OMPM;

import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class OweOthersAdapter extends RecyclerView.Adapter<OweOthersAdapter.ViewHolder> {

    private List<Debt> mValues;

    public OweOthersAdapter(List<Debt> items) {
        mValues = items;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.owe_me_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        if (mValues.get(position).isPaid())
            holder.status.setText("Paid");
        else
            holder.status.setText("Unpaid");

        holder.name.setText(mValues.get(position).getName());
        holder.phoneNumber.setText(mValues.get(position).getNumber());
        holder.date.setText(mValues.get(position).getDate());
        holder.amount.setText(mValues.get(position).getAmount());
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
    }
}
