package com.example.OMPM;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class ExpenditureListAdapter extends RecyclerView.Adapter<ExpenditureListAdapter.ListViewHolder> {

    private ArrayList<Expenditure> itemList;
    private Context mContext;

    public ExpenditureListAdapter(Context context, ArrayList<Expenditure> itemList){
        this.itemList = itemList;
        this.mContext = context;
    }

    @NonNull
    @Override
    public ExpenditureListAdapter.ListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);

        //Inflate the custom layout
        View itemView = inflater.inflate(R.layout.expenditurehistorylist, parent, false);
        //Return a new holder instance
        ListViewHolder viewHolder = new ListViewHolder(itemView);
        return viewHolder;
    }
    // Involves populating data into the item through holder
    @Override
    public void onBindViewHolder(@NonNull ExpenditureListAdapter.ListViewHolder holder, int position) {
        Expenditure item = itemList.get(position);
        holder.bindTo(item);

    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public static class ListViewHolder extends RecyclerView.ViewHolder{
        private TextView mDateText;
        private TextView mCostText;
        private TextView mTypeText;
        private TextView mItemText;

        public ListViewHolder(@NonNull View itemView) {
            super(itemView);

            mDateText = itemView.findViewById(R.id.dateText);
            mCostText = itemView.findViewById(R.id.costText);
            mTypeText = itemView.findViewById(R.id.typeText);
            mItemText = itemView.findViewById(R.id.itemText);

        }

        void bindTo(Expenditure item){
            mCostText.setText(item.getCost());
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MMMM/YYYY");
            mDateText.setText(sdf.format(item.getTimestamp()));
            mTypeText.setText(item.getType());
            mItemText.setText(item.getItem());
        }
    }
}
