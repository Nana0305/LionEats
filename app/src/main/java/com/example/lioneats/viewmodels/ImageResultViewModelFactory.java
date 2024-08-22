package com.example.lioneats.viewmodels;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.lioneats.api.ApiService;

public class ImageResultViewModelFactory implements ViewModelProvider.Factory {
	private final ApiService apiService;

	public ImageResultViewModelFactory(ApiService apiService) {
		this.apiService = apiService;
	}

	@NonNull
	@Override
	public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
		if (modelClass.isAssignableFrom(ImageResultViewModel.class)) {
			return (T) new ImageResultViewModel(apiService);
		}
		throw new IllegalArgumentException("Unknown ViewModel class");
	}
}
