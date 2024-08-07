package com.example.lioneats.activities;

import android.app.Activity;
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
import com.example.lioneats.models.LoginRequest;
import com.example.lioneats.models.LoginResponse;
import com.example.lioneats.utils.RetrofitClient;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
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
		ApiService apiService = RetrofitClient.getApiService();
		Call<LoginResponse> call = apiService.login(new LoginRequest(username, password));

		call.enqueue(new Callback<LoginResponse>() {
			@Override
			public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
				if (response.isSuccessful() && response.body() != null) {
					saveUserSession(response.body());
					navigateToHome();
				} else {
					Toast.makeText(LoginActivity.this, "Login failed", Toast.LENGTH_SHORT).show();
				}
			}

			@Override
			public void onFailure(Call<LoginResponse> call, Throwable t) {
				Toast.makeText(LoginActivity.this, "Network error", Toast.LENGTH_SHORT).show();
			}
		});
	}

	private void saveUserSession(LoginResponse loginResponse) {
		SharedPreferences sharedPreferences = getSharedPreferences("user_session", MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putString("token", loginResponse.getToken());
		editor.putString("username", loginResponse.getUsername());
		editor.apply();
	}

	private void navigateToHome() {
		Intent intent = new Intent(LoginActivity.this, MainActivity.class);
		startActivity(intent);
		finish();
	}
}