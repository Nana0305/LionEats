package com.example.lioneats.models;

import okhttp3.MultipartBody;

public class ML_feedback {
	private MultipartBody.Part image;
	private String result;
	private String dishName;
	private String remarks;

	public MultipartBody.Part getImage() {
		return image;
	}

	public void setImage(MultipartBody.Part image) {
		this.image = image;
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public String getDishName() {
		return dishName;
	}

	public void setDishName(String dishName) {
		this.dishName = dishName;
	}

	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}
}
