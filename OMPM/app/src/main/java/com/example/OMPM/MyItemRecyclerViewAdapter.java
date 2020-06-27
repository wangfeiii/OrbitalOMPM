package com.example.OMPM;

import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.OMPM.dummy.DummyContent.DummyItem;

import java.util.List;

public class MyItemRecyclerViewAdapter extends RecyclerView.Adapter<MyItemRecyclerViewAdapter.ViewHolder> {

    private List<Debt> mValues;

    public MyItemRecyclerViewAdapter(List<Debt> items) {
        mValues = items;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.debt_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
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

        public ViewHolder(View view) {
            super(view);
            name = view.findViewById(R.id.Name);
            phoneNumber = view.findViewById(R.id.phoneNumber);
            date = view.findViewById(R.id.date);
            amount = view.findViewById(R.id.amount);
        }
/*
        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }

 */
    }
}