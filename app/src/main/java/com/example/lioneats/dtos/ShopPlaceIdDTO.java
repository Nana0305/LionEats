package com.example.lioneats.dtos;

import com.google.gson.annotations.SerializedName;

public class ShopPlaceIdDTO {

	@SerializedName("place_id")
	private String placeId;

	public String getPlaceId() {
		return placeId;
	}

	public void setPlaceId(String placeId) {
		this.placeId = placeId;
	}
}
