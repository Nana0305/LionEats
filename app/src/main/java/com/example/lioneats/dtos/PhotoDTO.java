package com.example.lioneats.dtos;

import com.google.gson.annotations.SerializedName;

public class PhotoDTO {

	@SerializedName("photo_reference")
	private String photoReference;

	@SerializedName("height")
	private int height;

	@SerializedName("width")
	private int width;

	public String getPhotoReference() {
		return photoReference;
	}

	public void setPhotoReference(String photoReference) {
		this.photoReference = photoReference;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}
}
