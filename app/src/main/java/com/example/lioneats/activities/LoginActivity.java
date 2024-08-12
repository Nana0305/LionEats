package com.example.lioneats.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.lioneats.R;
import com.example.lioneats.api.ApiService;
import com.example.lioneats.models.User;
import com.example.lioneats.utils.RetrofitClient;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Callback;

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
		loginBtn.setOnClickListener(v -> login());

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
	private void login(){
		String username = usernameEditText.getText().toString();
		String password = passwordEditText.getText().toString();
		authenticateUser(username, password);
	}
	private void authenticateUser(String username, String password) {
		User user = new User();
		user.setUsername(username);
		user.setPassword(password);
		ApiService apiService = RetrofitClient.getApiService();
		Call<Long> call = apiService.login(user);

		call.enqueue(new Callback<Long>() {
			@Override
			public void onResponse(Call<Long> call, Response<Long> response) {
				if (response.isSuccessful() && response.body() != null) {
					Long userId = response.body();
					saveUserSession(userId, username);
					navigateToHome();
				} else {
					Toast.makeText(LoginActivity.this, "Login failed", Toast.LENGTH_SHORT).show();
				}
			}

			@Override
			public void onFailure(Call<Long> call, Throwable t) {
				Toast.makeText(LoginActivity.this, "Network error", Toast.LENGTH_SHORT).show();
			}
		});
	}

	private void saveUserSession(Long userId, String username) {
		SharedPreferences sharedPreferences = getSharedPreferences("user_session", MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putLong("user_id", userId);
		editor.putString("username", username);
		editor.apply();
	}

	private void navigateToHome() {
		Intent intent = new Intent(LoginActivity.this, MainActivity.class);
		startActivity(intent);
		finish();
	}
}