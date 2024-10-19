package com.example.peerstudy;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide; // Make sure to add Glide dependency for image loading
import com.bumptech.glide.request.RequestOptions;
import com.example.peerstudy.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileActivity extends AppCompatActivity {

    private TextView userFullName;
    private TextView userEmail;
    private TextView userRoomTimeSpent; // New TextView for room names and time spent
    private ImageView userPhoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        userFullName = findViewById(R.id.userFullName);
        userEmail = findViewById(R.id.userEmail);
        userRoomTimeSpent = findViewById(R.id.userRoomTimeSpent); // Initialize the new TextView
        userPhoto = findViewById(R.id.userPhoto);

        // Retrieve data from Intent
        Intent intent = getIntent();
        String fullName = intent.getStringExtra("USER_FULL_NAME");
        String email = intent.getStringExtra("USER_EMAIL");
        String photoUrl = intent.getStringExtra("USER_PHOTO");

        // Set the data to the views
        userFullName.setText(fullName);
        userEmail.setText(email);

        // Load the photo using Glide
        Glide.with(this)
                .load(photoUrl)
                .apply(RequestOptions.circleCropTransform())
                .into(userPhoto);

        // Fetch time spent in chat rooms from Firebase
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("timeSpent").child(fullName);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                StringBuilder roomTimeSpent = new StringBuilder();

                for (DataSnapshot roomSnapshot : dataSnapshot.getChildren()) {
                    String roomName = roomSnapshot.getKey();
                    Long timeSpentInSeconds = roomSnapshot.getValue(Long.class);

                    // Convert seconds to minutes and round up
                    long timeSpentInMinutes = (long) Math.ceil(timeSpentInSeconds / 60.0);

                    roomTimeSpent.append(roomName).append(": ").append(timeSpentInMinutes).append(" min\n");
                }

                userRoomTimeSpent.setText(roomTimeSpent.toString().trim());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle possible errors.
            }
        });
    }
}
