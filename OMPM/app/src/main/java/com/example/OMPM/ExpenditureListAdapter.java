package com.example.OMPM;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ExpenditureListAdapter extends RecyclerView.Adapter<ExpenditureListAdapter.ListViewHolder> {
    List<String> itemList;

    public ExpenditureListAdapter(List<String> itemList){
        this.itemList = itemList;
    }

    @NonNull
    @Override
    public ExpenditureListAdapter.ListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        //Inflate the custom layout
        View itemView = inflater.inflate(R.layout.expenditurehistorylist, parent, false);
        //Return a new holder instance
        ListViewHolder viewHolder = new ListViewHolder(itemView);
        return viewHolder;
    }
    // Involves populating data into the item through holder
    @Override
    public void onBindViewHolder(@NonNull ExpenditureListAdapter.ListViewHolder holder, int position) {
        String item = itemList.get(position);
        holder.expenditure.setText(item);

    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public static class ListViewHolder extends RecyclerView.ViewHolder{
        public TextView expenditure;

        public ListViewHolder(@NonNull View itemView) {
            super(itemView);

            expenditure = itemView.findViewById(R.id.expenditure);
        }
    }
}
