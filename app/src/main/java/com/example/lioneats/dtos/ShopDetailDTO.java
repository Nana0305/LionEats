package com.example.lioneats.dtos;

import com.google.gson.annotations.SerializedName;

public class ShopDetailDTO {

	@SerializedName("result")
	private ShopDTO shopDetail;

	public ShopDTO getShopDetail() {
		return shopDetail;
	}

	public void setShopDetail(ShopDTO shopDetail) {
		this.shopDetail = shopDetail;
	}
}

