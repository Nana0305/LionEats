package com.example.lioneats.viewmodels;

import android.net.Uri;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class HeaderViewModel extends ViewModel {
	public MutableLiveData<String> username = new MutableLiveData<>("");
	public MutableLiveData<String> actionText = new MutableLiveData<>("");
	public MutableLiveData<Boolean> isLoggedIn = new MutableLiveData<>(false);
	public MutableLiveData<Uri> photoURI = new MutableLiveData<>();

	public void updateUserStatus (String username) {
		if (username != null) {
			this.username.setValue(username);
			this.actionText.setValue("Logout");
			this.isLoggedIn.setValue(true);
		} else {
			this.username.setValue("Guest");
			this.actionText.setValue("Login");
			this.isLoggedIn.setValue(false);
		}
	}

	public void setPhotoURI (Uri uri) {
		this.photoURI.setValue(uri);
	}
}
