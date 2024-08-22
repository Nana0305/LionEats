package com.example.lioneats.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import com.example.lioneats.R;
import com.example.lioneats.api.ApiService;
import com.example.lioneats.api.RetrofitClient;
import com.example.lioneats.databinding.ActivityChangePasswordBinding;
import com.example.lioneats.utils.ActivityUtils;
import com.example.lioneats.viewmodels.ChangePasswordViewModel;
import com.example.lioneats.viewmodels.ChangePasswordViewModelFactory;

public class ChangePasswordActivity extends AppCompatActivity {
	private static final String TAG = "ChangePasswordActivity";
	private ChangePasswordViewModel viewModel;
	private SharedPreferences userSessionPreferences, userPreferences;
	private Long userId;
	private String jwtToken;
	private ApiService apiService;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ActivityChangePasswordBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_change_password);

		userPreferences = getSharedPreferences("user", MODE_PRIVATE);
		userSessionPreferences = getSharedPreferences("user_session", MODE_PRIVATE);
		userId = userSessionPreferences.getLong("user_id", -999);
		jwtToken = userSessionPreferences.getString("jwt","");

		apiService = RetrofitClient.getApiService(jwtToken);

		ChangePasswordViewModelFactory factory = new ChangePasswordViewModelFactory(apiService, userId);
		viewModel = new ViewModelProvider(this, factory).get(ChangePasswordViewModel.class);
		binding.setViewModel(viewModel);
		binding.setLifecycleOwner(this);

		Runnable afterDismissAction = () -> {
			ActivityUtils.logout(this, null);
			ActivityUtils.redirectToActivity(this, LoginActivity.class, true);
		};
		viewModel.getIsPasswordChanged().observe(this, isPasswordChanged -> {
			if (isPasswordChanged) {
				ActivityUtils.submitSuccessDialog(this, "Your password is successfuly updated!", afterDismissAction);
			} else {
				Toast.makeText(this, "Change password failed", Toast.LENGTH_SHORT).show();
			}
		});
		binding.logoBtn.setOnClickListener(v -> ActivityUtils.redirectToActivity(this, MainActivity.class, true));
	}
}