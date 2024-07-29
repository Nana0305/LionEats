package com.example.lioneats;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class ImageResultActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_image_result);

		// Header row elements
		TextView userNameText = findViewById(R.id.usernameText);
		TextView logoutBtn = findViewById(R.id.logoutBtn);
		ImageButton cameraBtn = findViewById(R.id.cameraBtn);
		String username = getIntent().getStringExtra("USERNAME");
		userNameText.setText(username);
		logoutBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//logout
			}
		});
		cameraBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//image result again
			}
		});
	}
}