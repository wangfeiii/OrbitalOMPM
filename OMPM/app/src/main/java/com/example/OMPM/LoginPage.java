package com.example.OMPM;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONException;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class LoginPage extends AppCompatActivity implements View.OnClickListener {

    private static final String LOG_TAG = LoginPage.class.getSimpleName();

    private static final String KEY_VERIFY_IN_PROGRESS = "key_verify_in_progress";

    private static final int STATE_INITIALIZED = 1;
    private static final int STATE_CODE_SENT = 2;
    private static final int STATE_VERIFY_FAILED = 3;
    private static final int STATE_VERIFY_SUCCESS = 4;
    private static final int STATE_SIGNIN_FAILED = 5;
    private static final int STATE_SIGNIN_SUCCESS = 6;

    private FirebaseAuth mAuth = FirebaseAuth.getInstance(); ;
    private boolean mVerificationInProgress = false;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private DatabaseReference mDatabase;
    FirebaseAuth.AuthStateListener authStateListener = new FirebaseAuth.AuthStateListener() {
        @Override
        public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if(user != null){
                Intent intent = new Intent(LoginPage.this, MainPage.class);
                startActivity(intent);
            }
        }
    };

    private EditText mPhone;
    private EditText mVerificationCode;
    private Button buttonLogin;
    private Button buttonResend;
    private Button buttonVerify;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_page);

        if (savedInstanceState != null){
            onRestoreInstanceState(savedInstanceState);
        }

        findViewById(R.id.button_login).setOnClickListener(this);
        findViewById(R.id.button_resend).setOnClickListener(this);
        findViewById(R.id.button_verify).setOnClickListener(this);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                Log.d(LOG_TAG, getString(R.string.verificationC) + phoneAuthCredential);
                mVerificationInProgress = false;
                updateUI(STATE_VERIFY_SUCCESS, phoneAuthCredential);
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                Log.w(LOG_TAG, getString(R.string.verificationF), e);
                mVerificationInProgress = false;
                if (e instanceof FirebaseAuthInvalidCredentialsException){
                    displayToast(getString(R.string.e_invalid_phone_number));
                }
                else if (e instanceof FirebaseTooManyRequestsException){
                    displayToast(getString(R.string.too_many_requests_error));
                }
            }

            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token){
                Log.d(LOG_TAG, "onCodeSent:" + verificationId);
                mVerificationId = verificationId;
                mResendToken = token;
                updateUI(STATE_CODE_SENT);
            }
        };
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(authStateListener);
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
        if (mVerificationInProgress && validatePhoneNumber()){
            mPhone = (EditText) findViewById(R.id.editText_Phone);
            startPhoneNumberVerification(mPhone.getText().toString());
        }
    }

    @Override
    public void onStop(){
        super.onStop();
        mAuth.removeAuthStateListener(authStateListener);
    }

    @Override
    public void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_VERIFY_IN_PROGRESS, mVerificationInProgress);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState){
        super.onRestoreInstanceState(savedInstanceState);
        mVerificationInProgress = savedInstanceState.getBoolean(KEY_VERIFY_IN_PROGRESS);
    }

    public void displayToast(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void startPhoneNumberVerification(String phoneNumber){
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,            //Phone Number to verify
                60,                   //Timeout duration
                TimeUnit.SECONDS,       //Unit of timeout
                this,            //Activity
                mCallbacks);            //OnVerificationStateChangedCallbacks
        mVerificationInProgress = true;
    }

    private void verifyPhoneNumberWithCode(String verificationId, String code){
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        signInWithPhoneAuthCredential(credential);
    }

    private void resendVerificationCode(String phoneNumber,
                                        PhoneAuthProvider.ForceResendingToken token) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            phoneNumber,
            60,
            TimeUnit.SECONDS,
            this,
            mCallbacks,
            token);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>(){

                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task){
                        if (task.isSuccessful()){
                            Log.d(LOG_TAG,"signInWithCredential:success");
                            FirebaseUser user = task.getResult().getUser();
                            updateUI(STATE_SIGNIN_SUCCESS, user);
                            long creationTimestamp = user.getMetadata().getCreationTimestamp();
                            long lastSignInTimeStamp = user.getMetadata().getLastSignInTimestamp();
                            if (creationTimestamp == lastSignInTimeStamp) {
                                try {
                                    writeNewUser(user.getUid(), user.getPhoneNumber());
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            }
                            Intent launchNotifications = new Intent(LoginPage.this, MainPage.class);
                            startActivity(launchNotifications);

                        } else {
                            Log.w(LOG_TAG, "signInWithCredential:failure", task.getException());
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException){
                                displayToast(getString(R.string.e_invalid_code));
                            }
                            updateUI(STATE_SIGNIN_FAILED);
                        }
                    }
                });
    }

    private void signOut(){
        mAuth.signOut();
        updateUI(STATE_INITIALIZED);
    }

    private void updateUI(int uiState) {
        updateUI(uiState, mAuth.getCurrentUser(), null);
    }

    private void updateUI(FirebaseUser user){
        if (user != null){
            updateUI(STATE_SIGNIN_SUCCESS, user);
        } else {
            updateUI(STATE_INITIALIZED);
        }
    }

    private void updateUI(int uiState, FirebaseUser user){
        updateUI(uiState, user, null);
    }

    private void updateUI(int uiState, PhoneAuthCredential cred){
        updateUI(uiState, null, cred);
    }

    private void updateUI(int uiState, FirebaseUser user, PhoneAuthCredential cred){

        buttonLogin = (Button) findViewById(R.id.button_login);
        buttonResend = (Button) findViewById(R.id.button_resend);
        buttonVerify = (Button) findViewById(R.id.button_verify);
        mPhone = (EditText) findViewById(R.id.editText_Phone);
        mVerificationCode = (EditText) findViewById(R.id.editText_VerificationCode);

        switch (uiState){
            case STATE_INITIALIZED:
                buttonLogin.setVisibility(View.VISIBLE);
                mVerificationCode.setVisibility(View.INVISIBLE);
                break;

            case STATE_CODE_SENT:
                buttonVerify.setVisibility(View.VISIBLE);
                buttonResend.setVisibility(View.VISIBLE);
                mVerificationCode.setVisibility(View.VISIBLE);
                buttonLogin.setVisibility(View.INVISIBLE);
                displayToast(getString(R.string.code_sent));
                break;

            case STATE_VERIFY_FAILED:
                displayToast(getString(R.string.verification_failed));
                break;

            case STATE_VERIFY_SUCCESS:
                displayToast(getString(R.string.verification_succeeded));
                if (cred != null){
                    if (cred.getSmsCode() != null){
                        mVerificationCode.setText(cred.getSmsCode());
                    } else {
                        mVerificationCode.setText(R.string.instant_validation);
                    }
                }
                break;

            case STATE_SIGNIN_FAILED:
                displayToast(getString(R.string.sign_in_failed));
                break;

            case STATE_SIGNIN_SUCCESS:
                break;
        }
    }

    private void writeNewUser(String userId, String phoneNumber) throws JSONException, IOException, ParseException {

        User user = new User(phoneNumber);
        mDatabase.child("users")
                .child(userId)
                .setValue(user);
    }

    private boolean validatePhoneNumber(){
        String phoneNumber = mPhone.getText().toString();
        if(TextUtils.isEmpty(phoneNumber)){
            displayToast(getString(R.string.e_invalid_phone_number));
            return false;
        }
        return true;
    }

    @Override
    public void onClick(View view) {

        mPhone = (EditText) findViewById(R.id.editText_Phone);
        mVerificationCode = (EditText) findViewById(R.id.editText_VerificationCode);

        switch (view.getId()){
            case R.id.button_login:
                if (!validatePhoneNumber()){
                    return;
                }

                startPhoneNumberVerification(mPhone.getText().toString());
                break;

            case R.id.button_verify:
                String vCode = mVerificationCode.getText().toString();
                if (TextUtils.isEmpty(vCode)){
                    displayToast(getString(R.string.e_invalid_code));
                    return;
                }

                verifyPhoneNumberWithCode(mVerificationId, vCode);
                break;

            case R.id.button_resend:
                resendVerificationCode(mPhone.getText().toString(), mResendToken);
                break;
        }
    }
}
