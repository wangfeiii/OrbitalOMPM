package com.example.OMPM;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class WordListAdapter extends RecyclerView.Adapter<WordListAdapter.ListViewHolder> {

    List<Contact> itemList;

    // Constructor
    public WordListAdapter(List<Contact> itemList) {
        this.itemList = itemList;
    }

    @NonNull
    @Override
    public ListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View itemView = inflater.inflate(R.layout.wordlist_item, parent, false);
        // Return a new holder instance
        ListViewHolder viewHolder = new ListViewHolder(itemView);
        return viewHolder;
    }

    // Involves populating data into the item through holder
    @Override
    public void onBindViewHolder(@NonNull ListViewHolder holder, final int position) {
        Contact item = itemList.get(position);
        holder.contact.setText(item.getName());
        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                itemList.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position,itemList.size());
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

        public ListViewHolder(View itemView) {
            super(itemView);

            contact = itemView.findViewById(R.id.contacts);
            delete = itemView.findViewById(R.id.imageView);

        }
    }

}

