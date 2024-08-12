package com.example.lioneats.models;

public class ML_feedback {
	private String imageBlobUrl;
	private String ml_result;
	private String userDish;
	private String remarks;

	public String getImageBlobUrl() {
		return imageBlobUrl;
	}

	public void setImageBlobUrl(String imageBlobUrl) {
		this.imageBlobUrl = imageBlobUrl;
	}

	public String getMl_result() {
		return ml_result;
	}

	public void setMl_result(String ml_result) {
		this.ml_result = ml_result;
	}

	public String getUserDish() {
		return userDish;
	}

	public void setUserDish(String userDish) {
		this.userDish = userDish;
	}

	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}
}
