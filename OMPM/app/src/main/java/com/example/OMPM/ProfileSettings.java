package com.example.OMPM;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.renderscript.Sampler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileSettings extends AppCompatActivity {

    private static final String TAG = "LOG_TAG";

    private FirebaseUser user;
    private DatabaseReference mDatabase;
    private StorageReference mStorage;
    private String userId;
    private String name;
    private String email;
    private String phoneNumber;
    private Uri photoUrl;

    private CircleImageView profilePicture;
    private EditText eName;
    private EditText eEmail;
    private TextView tPhoneNumber;
    private Button bUpdateProfile;

    private static final int IMAGE_REQUEST = 1;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_settings);
        getSupportActionBar().setTitle("Profile Page");

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mStorage = FirebaseStorage.getInstance().getReference();
        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null){
            name = user.getDisplayName();
            photoUrl = user.getPhotoUrl();
            userId = user.getUid();
            phoneNumber = user.getPhoneNumber();
        }

        profilePicture = findViewById(R.id.profile_image);
        eName = findViewById(R.id.profile_name);
        bUpdateProfile = findViewById(R.id.update_profile);
        eEmail = findViewById(R.id.email);
        tPhoneNumber = findViewById(R.id.phone);

        if(name != null){
            eName.setText(name);
        }

        if (phoneNumber != null){
            tPhoneNumber.setText(phoneNumber);
        }

        Query emailQuery = mDatabase
                .child("users")
                .child(userId)
                .child("email");
        emailQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    email = String.valueOf(dataSnapshot.getValue());
                    if (!email.equals("")) {
                        eEmail.setText(email);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        if (photoUrl != null){
            updateImage(photoUrl);
        }
        profilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImage();
            }
        });
        bUpdateProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String profileName = eName.getText().toString();
                String profileEmail = eEmail.getText().toString();
                if (!profileName.equals("")) {
                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                            .setDisplayName(profileName).build();
                    mDatabase.child("users")
                            .child(userId)
                            .child("email")
                            .setValue(profileEmail);
                    user.updateProfile(profileUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(getApplicationContext(), "User Profile Updated!", Toast.LENGTH_SHORT).show();
                                Log.d(TAG, "User profile Updated");
                            }
                        }
                    });
                }
                else{
                    Toast.makeText(getApplicationContext(), "Please Enter Name", Toast.LENGTH_SHORT).show();
                }

            }
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMAGE_REQUEST && resultCode == Activity.RESULT_OK){
            imageUri = data.getData();
            uploadImage();
        }
    }

    private void updateImage(Uri uri){
        Picasso.get().load(uri).into(profilePicture);
        Log.d(TAG, "Profile Picture Loaded");
    }

    private void openImage(){
        Log.d(TAG, "Gallery Opened");
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), IMAGE_REQUEST);
    }


    private void uploadImage(){
        final StorageReference ref = mStorage.child("Images").child(imageUri.getLastPathSegment());
        final UploadTask uploadTask = ref.putFile(imageUri);
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Uploading...");
        progressDialog.show();

        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                progressDialog.dismiss();
                uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if (!task.isSuccessful()){
                            throw task.getException();
                        }
                        return ref.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()){
                            final Uri downloadUri = task.getResult();
                            Log.d(TAG, downloadUri.toString());
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder().setPhotoUri(downloadUri).build();
                            user.updateProfile(profileUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                  if (task.isSuccessful()){
                                      Log.d(TAG, "Profile Picture Updated");
                                      Toast.makeText(getApplicationContext(), "Profile Picture Updated!", Toast.LENGTH_SHORT).show();
                                      updateImage(downloadUri);
                                  }
                                }
                            });
                        }
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(ProfileSettings.this, "Image Upload Failed", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Image Upload Failed");
            }
        }).addOnProgressListener(
                new OnProgressListener<UploadTask.TaskSnapshot>() {

                    // Progress Listener for loading
                    // percentage on the dialog box
                    @Override
                    public void onProgress(
                            UploadTask.TaskSnapshot taskSnapshot)
                    {
                        double progress
                                = (100.0
                                * taskSnapshot.getBytesTransferred()
                                / taskSnapshot.getTotalByteCount());
                        progressDialog.setMessage(
                                "Uploaded "
                                        + (int)progress + "%");
                    }
                });
    }
}