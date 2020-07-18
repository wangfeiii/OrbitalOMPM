package com.example.OMPM;

import android.util.Log;


import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "LOG_TAG";


    @Override
    public void onNewToken(String token){
        Log.d(TAG, "Refreshed token: " + token);
        sendRegistrationToServer(token);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage){
    }

    private void sendRegistrationToServer(String token){

    }
}
