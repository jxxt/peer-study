package com.example.peerstudy;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        String userName = getIntent().getStringExtra("USER_NAME");
        String userPhoto = getIntent().getStringExtra("USER_PHOTO");

        TextView welcomeTextView = findViewById(R.id.welcomeTextView);
        welcomeTextView.setText(userName);

        ImageView profileImageView = findViewById(R.id.profileImageView);
        if (userPhoto != null) {
            Glide.with(this)
                    .load(userPhoto)
                    .apply(RequestOptions.circleCropTransform())
                    .into(profileImageView);
        }

        Button joinRoomButton = findViewById(R.id.join_study_room);
        joinRoomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(WelcomeActivity.this, StudyRoomListActivity.class);
                startActivity(intent);
            }
        });
    }
}
