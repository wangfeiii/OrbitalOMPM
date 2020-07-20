package com.example.OMPM;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.renderscript.Sampler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


public class ExpenditureListAdapter extends RecyclerView.Adapter<ExpenditureListAdapter.ListViewHolder> {

    private ArrayList<Expenditure> itemList;
    private Context mContext;
    private static final String TAG = "LOG_TAG";
    public static final String EXTRA_FLAG = "com.example.twoactivities.extra.FLAG";
    public static final String EXTRA_EXPENDITURE = "com.example.twoactivities.extra.EXPENDITURE";

    private DatabaseReference mDatabase;
    private FirebaseUser user;
    private String userId;

    private Date bDate;

    public ExpenditureListAdapter(Context context, ArrayList<Expenditure> itemList) {
        this.itemList = itemList;
        this.mContext = context;
    }

    @NonNull
    @Override
    public ExpenditureListAdapter.ListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        mDatabase = FirebaseDatabase.getInstance().getReference();
        user = FirebaseAuth.getInstance().getCurrentUser();
        userId = user.getUid();

        LayoutInflater inflater = LayoutInflater.from(mContext);

        //Inflate the custom layout
        View itemView = inflater.inflate(R.layout.expenditurehistorylist, parent, false);
        //Return a new holder instance
        ListViewHolder viewHolder = new ListViewHolder(itemView);
        return viewHolder;
    }

    // Involves populating data into the item through holder
    @Override
    public void onBindViewHolder(@NonNull ExpenditureListAdapter.ListViewHolder holder, final int position) {
        final Expenditure item = itemList.get(position);
        holder.bindTo(item);
        holder.mDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(mContext).setTitle("Delete Entry")
                        .setMessage("Are you sure you want to delete this entry?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                deleteItem(item, position);
                            }
                        })
                        .setNegativeButton("No", null)
                        .show();
            }
        });
        holder.mEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Activity origin = (Activity) mContext;
                Intent editIntent = new Intent(mContext, ExpenditureInput.class);
                editIntent.putExtra(EXTRA_FLAG, "HistoryPage");
                editIntent.putExtra(EXTRA_EXPENDITURE, item);
                origin.startActivity(editIntent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public static class ListViewHolder extends RecyclerView.ViewHolder {
        private TextView mDateText;
        private TextView mCostText;
        private TextView mTypeText;
        private TextView mItemText;
        private ImageView mDelete;
        private ImageView mEdit;

        public ListViewHolder(@NonNull View itemView) {
            super(itemView);

            mDateText = itemView.findViewById(R.id.dateText);
            mCostText = itemView.findViewById(R.id.costText);
            mTypeText = itemView.findViewById(R.id.typeText);
            mItemText = itemView.findViewById(R.id.itemText);
            mDelete = itemView.findViewById(R.id.ic_delete);
            mEdit = itemView.findViewById(R.id.ic_edit);

        }

        void bindTo(Expenditure item) {
            NumberFormat formatter = NumberFormat.getCurrencyInstance();
            mCostText.setText(formatter.format(Float.parseFloat(item.getCost())));
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MMMM/YYYY");
            mDateText.setText(sdf.format(item.getTimestamp()));
            mTypeText.setText(item.getType());
            mItemText.setText(item.getItem());
        }
    }

    public void deleteItem(Expenditure item, int position){
        String itemKey = item.getKey();
        SimpleDateFormat sdf = new SimpleDateFormat("YYYY/MMM");
        Date newDate = new Date(item.getTimestamp());
        final String monthDate = sdf.format(newDate.getTime());
        try {
            bDate = new SimpleDateFormat("yyyy/MMM").parse(monthDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        final String date = Long.toString(bDate.getTime());

        mDatabase.child("users")
                .child(userId)
                .child("Expenditures")
                .child(monthDate)
                .child(itemKey)
                .removeValue();


        Query dateQuery = mDatabase.child("users")
                .child(userId)
                .child("Expenditures")
                .child(monthDate);

        dateQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null) {
                    DatabaseReference dateReference = mDatabase.child("users")
                            .child(userId)
                            .child("ExpenditureDates")
                            .child(date);
                    dateReference.removeValue();
                }
            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        itemList.remove(item);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, itemList.size());
    }
}
