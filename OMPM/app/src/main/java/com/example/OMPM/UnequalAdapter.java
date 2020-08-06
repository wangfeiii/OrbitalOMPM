package com.example.OMPM;

import android.content.Context;
import android.content.Intent;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class UnequalAdapter extends RecyclerView.Adapter<UnequalAdapter.ListViewHolder> {

    List<Contact> itemList;
    Context context;
    EditText et;

    public UnequalAdapter(List<Contact> itemList, Context context) {

        this.itemList = itemList;
        this.context = context;
    }

    @NonNull
    @Override
    public UnequalAdapter.ListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View itemView = inflater.inflate(R.layout.unequal_item, parent, false);
        // Return a new holder instance
        UnequalAdapter.ListViewHolder viewHolder = new UnequalAdapter.ListViewHolder(itemView);
        return viewHolder;
    }

    // Involves populating data into the item through holder
    @Override
    public void onBindViewHolder(@NonNull UnequalAdapter.ListViewHolder holder, final int position) {
        final Contact item = itemList.get(position);
        holder.contact.setText(item.getName());
        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                itemList.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position,itemList.size());
            }
        });
        et= holder.percentage;
        et.setText(item.getPercentage());
        et.setFilters(new InputFilter[]{ new InputFilterMinMax("1", "100", context)});
        et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }
            @Override
            public void afterTextChanged(Editable s) {
                String percent = s.toString();
                item.setPercentage(percent);
            }
        });
    }


    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public static class ListViewHolder extends RecyclerView.ViewHolder {

        public TextView contact;
        public ImageView delete;
        public EditText percentage;

        public ListViewHolder(View itemView) {
            super(itemView);

            contact = itemView.findViewById(R.id.contacts);
            delete = itemView.findViewById(R.id.imageView);
            percentage = itemView.findViewById(R.id.percent);

        }
    }

}