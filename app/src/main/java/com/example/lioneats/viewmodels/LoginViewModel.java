package com.example.lioneats.viewmodels;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.lioneats.api.ApiService;
import com.example.lioneats.api.RetrofitClient;
import com.example.lioneats.dtos.LoginResponseDTO;
import com.example.lioneats.dtos.UserDTO;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginViewModel extends ViewModel {
	public MutableLiveData<String> username = new MutableLiveData<>("");
	public MutableLiveData<String> password = new MutableLiveData<>("");
	private MutableLiveData<String> errorMessage = new MutableLiveData<>("");
	private MutableLiveData<Boolean> isLoginSuccessful = new MutableLiveData<>(false);
	private ApiService apiService;
	private String jwtToken;
	private long userId;
	private UserDTO user;

	public LoginViewModel(ApiService apiService) {
		this.apiService = apiService;
	}

	public MutableLiveData<String> getUsername() {
		return username;
	}

	public MutableLiveData<String> getPassword() {
		return password;
	}

	public MutableLiveData<String> getErrorMessage() {
		return errorMessage;
	}

	public MutableLiveData<Boolean> getIsLoginSuccessful() {
		return isLoginSuccessful;
	}

	public String getJwtToken() {
		return jwtToken;
	}

	public long getUserId() {
		return userId;
	}

	public UserDTO getUser() {
		return user;
	}

	public void onLoginClicked() {
		if (!validateInputs()) {
			return;
		}

		UserDTO user = createUserDTO();
		apiService.login(user).enqueue(new Callback<LoginResponseDTO>() {
			@Override
			public void onResponse(Call<LoginResponseDTO> call, Response<LoginResponseDTO> response) {
				if(response.isSuccessful() && response.body() != null) {
					jwtToken = response.body().getJwt();
					userId = response.body().getUserId();
					isLoginSuccessful.setValue(true);
				} else {
					errorMessage.setValue("Login failed: " + response.message());
				}
			}

			@Override
			public void onFailure(Call<LoginResponseDTO> call, Throwable t) {
				errorMessage.setValue("Login failed: " + t.getMessage());
			}
		});
	}

	public void fetchUserData(long userId, String jwtToken, Runnable onSuccess) {
		ApiService apiService = RetrofitClient.getApiService(jwtToken);
		Call<UserDTO> call = apiService.viewUser(userId);

		call.enqueue(new Callback<UserDTO>() {
			@Override
			public void onResponse(Call<UserDTO> call, Response<UserDTO> response) {
				if (response.isSuccessful() && response.body() != null) {
					user = response.body();
					onSuccess.run();
				} else {
					errorMessage.setValue("Failed to fetch user data: " + response.message());
				}
			}

			@Override
			public void onFailure(Call<UserDTO> call, Throwable t) {
				errorMessage.setValue("Network Error: " + t.getMessage());
			}
		});
	}

	public UserDTO createUserDTO() {
		UserDTO user = new UserDTO();
		user.setUsername(username.getValue());
		user.setPassword(password.getValue());
		return user;
	}

	public boolean validateInputs() {
		boolean isValid = true;

		if (username.getValue().isEmpty()) {
			errorMessage.setValue("Username is required");
			isValid = false;
		} else if (username.getValue().length() > 50) {
			errorMessage.setValue("Username must be less than 50 characters");
			isValid = false;
		}

		if (password.getValue().isEmpty()) {
			errorMessage.setValue("Password is required");
			isValid = false;
		} else if (password.getValue().length() < 8) {
			errorMessage.setValue("Password must be at least 8 characters");
			isValid = false;
		}

		return isValid;
	}
}
