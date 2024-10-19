package com.example.peerstudy;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide; // Make sure to add Glide dependency for image loading
import com.bumptech.glide.request.RequestOptions;
import com.example.peerstudy.R;

public class ProfileActivity extends AppCompatActivity {

    private TextView userFullName;
    private TextView userEmail;
    private ImageView userPhoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        userFullName = findViewById(R.id.userFullName);
        userEmail = findViewById(R.id.userEmail);
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
    }
}
