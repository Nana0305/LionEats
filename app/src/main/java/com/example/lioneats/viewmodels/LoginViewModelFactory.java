package com.example.lioneats.viewmodels;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.lioneats.api.ApiService;

public class LoginViewModelFactory implements ViewModelProvider.Factory {
	private final ApiService apiService;

	public LoginViewModelFactory(ApiService apiService) {
		this.apiService = apiService;
	}

	@NonNull
	@Override
	public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
		if (modelClass.isAssignableFrom(LoginViewModel.class)) {
			return (T) new LoginViewModel(apiService);
		}
		throw new IllegalArgumentException("Unknown ViewModel class");
	}
}