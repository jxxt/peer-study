package com.example.peerstudy;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;

public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        String userName = getIntent().getStringExtra("USER_NAME");
        String userEmail = getIntent().getStringExtra("USER_EMAIL");
        String userPhoto = getIntent().getStringExtra("USER_PHOTO");

        TextView welcomeTextView = findViewById(R.id.welcomeTextView);
        welcomeTextView.setText(userName);

        ImageView gifImageView = findViewById(R.id.gifImageView);

        // Load and autoplay the GIF
        Glide.with(this)
                .asGif()
                .load(R.drawable.hi)  // Replace 'my_gif' with your actual GIF name
                .apply(RequestOptions.bitmapTransform(new RoundedCorners(60))) // 30 for corner radius
                .into(gifImageView);

//        ImageView profileImageView = findViewById(R.id.profileImageView);
//        if (userPhoto != null) {
//            Glide.with(this)
//                    .load(userPhoto)
//                    .apply(RequestOptions.circleCropTransform())
//                    .into(profileImageView);
//        }

        Button joinRoomButton = findViewById(R.id.join_study_room);
        joinRoomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(WelcomeActivity.this, StudyRoomListActivity.class);
                intent.putExtra("USER_FULL_NAME", userName); // Pass the user's name
                startActivity(intent);
            }
        });

        Button leaderboardButton = findViewById(R.id.leaderboard);
        leaderboardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(WelcomeActivity.this, LeaderboardActivity.class);
                startActivity(intent);
            }
        });

        Button profileButton = findViewById(R.id.profile);
        profileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(WelcomeActivity.this, ProfileActivity.class);
                intent.putExtra("USER_FULL_NAME", userName); // Pass the user's name
                intent.putExtra("USER_EMAIL", userEmail); // Pass the user's email
                intent.putExtra("USER_PHOTO", userPhoto); // Pass the user's photo URL
                startActivity(intent);
            }
        });


    }
}
