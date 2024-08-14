package com.example.lioneats.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.lioneats.R;
import com.example.lioneats.api.ApiService;
import com.example.lioneats.models.UserDTO;
import com.example.lioneats.utils.RetrofitClient;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
	private ImageView logoBtn;
	private Button loginBtn;
	private EditText usernameEditText;
	private EditText passwordEditText;
	private TextView registerAcctBtn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		logoBtn = findViewById(R.id.logoBtn);
		loginBtn = findViewById(R.id.loginBtn);
		usernameEditText = findViewById(R.id.username);
		passwordEditText = findViewById(R.id.password);
		registerAcctBtn = findViewById(R.id.registerAcct);

		logoBtn.setOnClickListener(v -> navigateToHome());
		loginBtn.setOnClickListener(v -> login());
		registerAcctBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(LoginActivity.this, RegisterAccountActivity.class);
				startActivity(intent);
				finish();
			}
		});
	}

	private void login() {
		String username = usernameEditText.getText().toString();
		String password = passwordEditText.getText().toString();
		authenticateUser(username, password);
	}

	private void authenticateUser(String username, String password) {
		UserDTO user = new UserDTO();
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
					saveUserToSharedPreferences(userId);
					navigateToHome();
				} else {
					Toast.makeText(LoginActivity.this, "Login failed", Toast.LENGTH_SHORT).show();
				}
			}

			@Override
			public void onFailure(Call<Long> call, Throwable t) {
				Toast.makeText(LoginActivity.this, "Network error", Toast.LENGTH_SHORT).show();
				Log.e("LoginActivity", "onFailure: ", t);
			}
		});
	}

	private void saveUserSession(Long userId, String username) {
		SharedPreferences userSessionPreferences = getSharedPreferences("user_session", MODE_PRIVATE);
		SharedPreferences.Editor sessionEditor = userSessionPreferences.edit();
		sessionEditor.putLong("user_id", userId);
		sessionEditor.putString("username", username);
		sessionEditor.apply();
	}

	private void navigateToHome() {
		Intent intent = new Intent(LoginActivity.this, MainActivity.class);
		startActivity(intent);
		finish();
	}

	private void saveUserToSharedPreferences(Long userId) {
		SharedPreferences userPreferences = getSharedPreferences("user", MODE_PRIVATE);
		SharedPreferences.Editor userEditor = userPreferences.edit();

		ApiService apiService = RetrofitClient.getApiService();
		Call<UserDTO> call = apiService.viewUser(userId);

		call.enqueue(new Callback<UserDTO>() {
			@Override
			public void onResponse(Call<UserDTO> call, Response<UserDTO> response) {
				if (response.isSuccessful() && response.body() != null) {
					Gson gson = new GsonBuilder().setPrettyPrinting().create();
					String jsonResponse = gson.toJson(response.body());
					Log.d("saveUserToSharedPreferences", "JSON Response: " + jsonResponse);
					UserDTO user = response.body();
					userEditor.putString("user", new Gson().toJson(user));
					userEditor.apply();
				} else {
					Log.d("saveUserToSharedPreferences", "Failed to fetch data");
				}
			}

			@Override
			public void onFailure(Call<UserDTO> call, Throwable t) {
				Log.e("saveUserToSharedPreferences", "Network Error: ", t);
			}
		});
	}
}
