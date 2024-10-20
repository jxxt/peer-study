package com.example.peerstudy;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide; // Ensure Glide is added in dependencies
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.auth.api.signin.GoogleSignIn; // Import for Google Sign-In
import com.google.android.gms.auth.api.signin.GoogleSignInAccount; // Import for Google Sign-In Account
import com.google.android.gms.auth.api.signin.GoogleSignInClient; // Import for GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions; // Import for Google Sign-In Options
import com.google.firebase.auth.FirebaseAuth; // Import FirebaseAuth
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
    private Button signOut; // Add a reference for the Sign Out button
    private GoogleSignInClient googleSignInClient; // GoogleSignInClient

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        userFullName = findViewById(R.id.userFullName);
        userEmail = findViewById(R.id.userEmail);
        userRoomTimeSpent = findViewById(R.id.userRoomTimeSpent); // Initialize the new TextView
        userPhoto = findViewById(R.id.userPhoto);
        signOut = findViewById(R.id.signout); // Initialize the Sign Out button

        // Configure Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail() // Request email
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

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

        // Set OnClickListener for the Sign Out button
        signOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut(); // Call the sign-out method
            }
        });
    }

    private void signOut() {
        // Sign out from Firebase Authentication
        FirebaseAuth.getInstance().signOut();

        // Sign out from Google Sign-In
        googleSignInClient.signOut().addOnCompleteListener(this, task -> {
            // Redirect to MainActivity
            Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
            startActivity(intent);

            // Finish the current activity
            finish();
        });
    }
}
