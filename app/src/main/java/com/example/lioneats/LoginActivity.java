package com.example.lioneats;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class LoginActivity extends AppCompatActivity {
	private Button loginBtn;
	private EditText usernameEditText;
	private EditText passwordEditText;

	private TextView registerAcctBtn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		loginBtn = findViewById(R.id.loginBtn);
		usernameEditText = findViewById(R.id.username);
		passwordEditText = findViewById(R.id.password);
		loginBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String username = usernameEditText.getText().toString().trim();
				// For simplicity, assume any non-empty username and password are valid
				if (!username.isEmpty() && !passwordEditText.getText().toString().trim().isEmpty()) {
					Intent intent = new Intent(LoginActivity.this, UserHomeActivity.class);
					intent.putExtra("USERNAME", username);
					startActivity(intent);
					finish();
				}
			}
		});

		registerAcctBtn = findViewById(R.id.registerAcct);
		registerAcctBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(LoginActivity.this, RegisterAccountActivity.class);
				startActivity(intent);
				finish();
			}
		});
	}
}