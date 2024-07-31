package com.example.lioneats.dtos;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class NearByShopIdsDTO {

	@SerializedName("results")
	private List<ShopPlaceIdDTO> results;

	@SerializedName("next_page_token")
	private String nextPageToken;

	public List<ShopPlaceIdDTO> getResults() {
		return results;
	}

	public void setResults(List<ShopPlaceIdDTO> results) {
		this.results = results;
	}

	public String getNextPageToken() {
		return nextPageToken;
	}

	public void setNextPageToken(String nextPageToken) {
		this.nextPageToken = nextPageToken;
	}
}
