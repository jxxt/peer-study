package com.example.peerstudy;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;



public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        // Retrieve user details from the Intent
        String userName = getIntent().getStringExtra("USER_NAME");
        String userEmail = getIntent().getStringExtra("USER_EMAIL");
        String userPhoto = getIntent().getStringExtra("USER_PHOTO");


        // Find the TextView and set the user's name
        TextView welcomeTextView = findViewById(R.id.welcomeTextView);
        welcomeTextView.setText(userName);

        ImageView profileImageView = findViewById(R.id.profileImageView);

        // Load the user's profile picture into the ImageView using Glide (or any other library)
        if (userPhoto != null) {
            Glide.with(this)
                    .load(userPhoto)
                    .apply(RequestOptions.circleCropTransform())
                    .into(profileImageView);

        }
    }
}
