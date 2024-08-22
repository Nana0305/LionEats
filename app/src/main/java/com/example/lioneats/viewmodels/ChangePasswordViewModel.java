package com.example.lioneats.viewmodels;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.lioneats.api.ApiService;
import com.example.lioneats.dtos.PasswordChangeDTO;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;

public class ChangePasswordViewModel extends ViewModel {
	public MutableLiveData<String> oldPassword = new MutableLiveData<>("");
	public MutableLiveData<String> newPassword = new MutableLiveData<>("");
	public MutableLiveData<String> confirmPassword = new MutableLiveData<>("");

	private MutableLiveData<Boolean> isPasswordChanged = new MutableLiveData<>(false);
	private ApiService apiService;
	private Long userId;

	public ChangePasswordViewModel(ApiService apiService, Long userId) {
		this.apiService = apiService;
		this.userId = userId;
	}

	public MutableLiveData<Boolean> getIsPasswordChanged() {
		return isPasswordChanged;
	}

	public PasswordChangeDTO createPasswordChangeDTO() {
		PasswordChangeDTO passwordChange = new PasswordChangeDTO();
		passwordChange.setOldPassword(oldPassword.getValue());
		passwordChange.setNewPassword(newPassword.getValue());
		passwordChange.setConfirmNewPassword(confirmPassword.getValue());
		return passwordChange;
	}

	public boolean validateInputs() {
		boolean isValid = true;
		if (oldPassword.getValue().isEmpty() || oldPassword.getValue().length() < 8) {
			isValid = false;
		}

		if (newPassword.getValue().isEmpty() || newPassword.getValue().length() < 8) {
			isValid = false;
		}

		if (confirmPassword.getValue().isEmpty() || !confirmPassword.getValue().equals(newPassword.getValue())) {
			isValid = false;
		}

		return isValid;
	}

	public void changePassword() {
		if (!validateInputs()) {
			return;
		}

		PasswordChangeDTO passwordChangeDTO = createPasswordChangeDTO();

		apiService.changePassword(userId, passwordChangeDTO).enqueue(new Callback<ResponseBody>() {
			@Override
			public void onResponse(Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {
				if (response.isSuccessful()) {
					isPasswordChanged.setValue(true);
				} else {
					isPasswordChanged.setValue(false);
				}
			}

			@Override
			public void onFailure(Call<ResponseBody> call, Throwable t) {
				isPasswordChanged.setValue(false);
			}
		});
	}
}
