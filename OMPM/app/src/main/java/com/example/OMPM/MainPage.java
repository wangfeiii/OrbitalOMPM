package com.example.OMPM;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainPage extends AppCompatActivity {

    private static final String TAG = "LOG_TAG";

    private FirebaseUser user;
    private DatabaseReference mDatabase;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        user = FirebaseAuth.getInstance().getCurrentUser();
        userId = user.getUid();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case R.id.action_notifications:
                Intent launchNotifications = new Intent(MainPage.this, Notifications.class);
                startActivity(launchNotifications);
                return true;

            case R.id.action_profile_settings:
                Intent launchProfileS = new Intent(MainPage.this, ProfileSettings.class);
                startActivity(launchProfileS);
                return true;

            case R.id.action_app_settings:
                Intent launchAppS = new Intent(MainPage.this, AppSettings.class);
                startActivity(launchAppS);
                return true;

            case R.id.action_rate_us:
                AlertDialog.Builder myAlertBuilder = new AlertDialog.Builder(MainPage.this);
                myAlertBuilder.setTitle(R.string.rate_us);
                myAlertBuilder.setMessage(R.string.placeholder);
                myAlertBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getApplicationContext(), "Thank you for your feedback!",
                                Toast.LENGTH_SHORT).show();
                    }
                });
                myAlertBuilder.show();
                return true;

            case R.id.action_contact_us:
                Intent launchContactUs = new Intent(MainPage.this, ContactUs.class);
                startActivity(launchContactUs);
                return true;

            default:
        }
        return super.onOptionsItemSelected(item);
    }

    public void launchSplit(View view) {
        Intent iLaunchSplit = new Intent(MainPage.this, SplitBill.class);
        startActivity(iLaunchSplit);
    }

    public void launchExpenditure(View view) {
        Intent iLaunchExpenditure = new Intent(MainPage.this, ExpenditureHistory.class);
        startActivity(iLaunchExpenditure);
    }

    public void launchHistory(View view) {
        Intent iLaunchHistory = new Intent(MainPage.this, History.class);
        startActivity(iLaunchHistory);
    }
}
