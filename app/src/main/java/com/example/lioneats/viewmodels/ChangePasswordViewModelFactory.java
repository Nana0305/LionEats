package com.example.lioneats.viewmodels;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.lioneats.api.ApiService;

public class ChangePasswordViewModelFactory implements ViewModelProvider.Factory {
	private ApiService apiService;
	private Long userId;

	public ChangePasswordViewModelFactory (ApiService apiService, Long userId) {
		this.apiService = apiService;
		this.userId = userId;
	}

	@NonNull
	@Override
	public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
		if(modelClass.isAssignableFrom(ChangePasswordViewModel.class)) {
			return (T) new ChangePasswordViewModel(apiService, userId);
		}
		throw new IllegalStateException("Unknown ViewModel class");
	}
}
