package com.example.lioneats.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import com.example.lioneats.R;
import com.example.lioneats.databinding.ActivityLoginBinding;
import com.example.lioneats.api.RetrofitClient;
import com.example.lioneats.dtos.UserDTO;
import com.example.lioneats.utils.ActivityUtils;
import com.example.lioneats.viewmodels.LoginViewModel;
import com.example.lioneats.viewmodels.LoginViewModelFactory;
import com.google.gson.Gson;

public class LoginActivity extends AppCompatActivity {
	private static final String TAG = "LoginActivity";
	private LoginViewModel viewModel;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ActivityLoginBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_login);

		LoginViewModelFactory factory = new LoginViewModelFactory(RetrofitClient.getApiServiceWithoutToken());
		viewModel = new ViewModelProvider(this, factory).get(LoginViewModel.class);
		binding.setViewModel(viewModel);
		binding.setLifecycleOwner(this);

		viewModel.getIsLoginSuccessful().observe(this, isSuccess -> {
			if (isSuccess) {
				saveUserSession(viewModel.getUserId(), viewModel.getUsername().getValue(), viewModel.getPassword().getValue(), viewModel.getJwtToken());
				viewModel.fetchUserData(viewModel.getUserId(), viewModel.getJwtToken(), this::onUserDataFetched);
			}
		});

		viewModel.getErrorMessage().observe(this, errorMessage -> {
			if (errorMessage != null) {
				Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
			}
		});

		binding.logoBtn.setOnClickListener(v -> ActivityUtils.redirectToActivity(this, MainActivity.class, true));
		binding.registerAcct.setOnClickListener(v -> ActivityUtils.redirectToActivity(this, RegisterAccountActivity.class, true));
	}

	private void saveUserSession(long userId, String username, String password, String jwtToken) {
		SharedPreferences userSessionPreferences = getSharedPreferences("user_session", Context.MODE_PRIVATE);
		SharedPreferences.Editor sessionEditor = userSessionPreferences.edit();
		sessionEditor.putLong("user_id", userId);
		sessionEditor.putString("username", username);
		sessionEditor.putString("password", password);
		sessionEditor.putString("jwt", jwtToken);
		sessionEditor.commit();
	}

	private void onUserDataFetched() {
		saveUserToPreferences(viewModel.getUser());
		ActivityUtils.redirectToActivity(this, MainActivity.class, true);
	}

	private void saveUserToPreferences(UserDTO user) {
		SharedPreferences userPreferences = getSharedPreferences("user", Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = userPreferences.edit();
		editor.putString("user", new Gson().toJson(user));
		editor.commit();
	}
}

