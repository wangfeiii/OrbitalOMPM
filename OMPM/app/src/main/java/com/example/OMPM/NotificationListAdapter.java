package com.example.OMPM;

import android.app.Notification;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class NotificationListAdapter extends RecyclerView.Adapter<NotificationListAdapter.ListViewHolder> {

    private ArrayList<Debt> itemList;
    private Context mContext;
    private static final String TAG = "LOG_TAG";

    public NotificationListAdapter(Context context, ArrayList<Debt> itemList) {
        this.itemList = itemList;
        this.mContext = context;
    }

    @NonNull
    @Override
    public NotificationListAdapter.ListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);

        //Inflate the custom layout
        View itemView = inflater.inflate(R.layout.notificationlist, parent, false);
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openNotification();
            }
        });
        //Return a new holder instance
        NotificationListAdapter.ListViewHolder viewHolder = new NotificationListAdapter.ListViewHolder(itemView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationListAdapter.ListViewHolder holder, final int position) {
        final Debt item = itemList.get(position);
        holder.bindTo(item);
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public static class ListViewHolder extends RecyclerView.ViewHolder {
        private TextView mNotiText;
        private ImageView mProfilePic;

        public ListViewHolder(@NonNull View itemView) {
            super(itemView);

            mNotiText = itemView.findViewById(R.id.notificationText);
            mProfilePic = itemView.findViewById(R.id.profile_picture);

        }

        void bindTo(Debt item) {
            NumberFormat formatter = NumberFormat.getCurrencyInstance();
            String cost = formatter.format(Float.parseFloat(item.getAmount()));
            String creditor = item.getName();
            mNotiText.setText("You owe " + creditor + cost + "!");
        }
    }

    public void openNotification(){

    }
}
