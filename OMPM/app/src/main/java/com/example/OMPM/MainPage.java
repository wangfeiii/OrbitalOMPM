package com.example.OMPM;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

public class MainPage extends AppCompatActivity{

    private static final String TAG = "LOG_TAG";

    private FirebaseUser user;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseAuth.AuthStateListener authStateListener = new FirebaseAuth.AuthStateListener() {
        @Override
        public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if(user == null){
                Intent intent = new Intent(MainPage.this, LoginPage.class);
                startActivity(intent);
                finish();
            }
        }
    };
    private DatabaseReference mDatabase;
    private String userId;
    private String userToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);
        getSupportActionBar().setTitle("OMPM");

        mDatabase = FirebaseDatabase.getInstance().getReference();
        user = FirebaseAuth.getInstance().getCurrentUser();
        userId = user.getUid();
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.d(TAG, "getInstanceId failed", task.getException());
                            return;
                        }

                        // Get new Instance ID token
                        userToken = task.getResult().getToken();

                        // Log and toast
                        Log.d(TAG, userToken);
                        saveToken(userToken);
                    }
                });
        Query phoneQuery = mDatabase.child("users").child(userId).child("phoneNumber");
        phoneQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()){
                    String userPhone = user.getPhoneNumber();
                    mDatabase.child("users").child(userId).child("phoneNumber").setValue(userPhone);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    @Override
    public void onStart(){
        super.onStart();
        mAuth.addAuthStateListener(authStateListener);
    }

    @Override
    public void onStop(){
        super.onStop();
        mAuth.removeAuthStateListener(authStateListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.main_menu, menu);
        MenuItem iNoti = menu.findItem(R.id.action_notifications);
        iNoti.setIcon(R.drawable.ic_notification_true);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case R.id.action_notifications:
                Intent launchNotifications = new Intent(MainPage.this, NotificationsPage.class);
                startActivity(launchNotifications);
                return true;

            case R.id.action_profile_settings:
                Intent launchProfileS = new Intent(MainPage.this, ProfileSettings.class);
                startActivity(launchProfileS);
                return true;
/*
            case R.id.action_app_settings:
                Intent launchAppS = new Intent(MainPage.this, AppSettings.class);
                startActivity(launchAppS);
                return true;
 */

            case R.id.action_rate_us:
                LayoutInflater inflater = getLayoutInflater();
                AlertDialog.Builder myAlertBuilder = new AlertDialog.Builder(MainPage.this);
                myAlertBuilder.setTitle(R.string.rate_us);
                myAlertBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getApplicationContext(), "Thank you for your feedback!",
                                Toast.LENGTH_SHORT).show();
                    }
                });
                View dialogLayout = inflater.inflate(R.layout.alert_dialog_layout, null);
                myAlertBuilder.setView(dialogLayout);
                myAlertBuilder.show();
                return true;

            case R.id.action_contact_us:
                Intent launchContactUs = new Intent(MainPage.this, ContactUs.class);
                startActivity(launchContactUs);
                return true;

            case R.id.action_logout:
                mAuth.signOut();
                return true;

            default:
        }
        return super.onOptionsItemSelected(item);
    }

    public void saveToken(String token){
        mDatabase.child("users")
                .child(userId)
                .child("DeviceToken")
                .setValue(token);
    }

    public void launchSplit(View view) {
        Intent iLaunchSplit = new Intent(MainPage.this, SplitBill.class);
        startActivity(iLaunchSplit);
    }

    public void launchExpenditure(View view) {
        Intent iLaunchExpenditure = new Intent(MainPage.this, ExpenditureLandingPage.class);
        startActivity(iLaunchExpenditure);
    }

    public void launchHistory(View view) {
        Intent iLaunchHistory = new Intent(MainPage.this, History.class);
        startActivity(iLaunchHistory);
    }
}
