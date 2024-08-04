package com.example.lioneats.api;
import com.example.lioneats.dtos.NearByShopIdsDTO;
import com.example.lioneats.dtos.ShopDTO;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface FeedApi {

	// Fetches a list of nearby restaurants directly, with the associated keyword (dish) in each ShopDTO
	@GET("/api/feedGeneric/nearByRestaurants")
	Call<List<ShopDTO>> getNearbyRestaurants(
			@Query("lat") double lat,
			@Query("lng") double lng,
			@Query("next_page_token") String nextPageToken,
			@Query("offset_index") Integer offsetIndex
	);

	// Fetches details for a specific restaurant by placeId
	@GET("/api/feed/getRestaurantDetail")
	Call<ShopDTO> getRestaurantDetail(
			@Query("placeId") String placeId
	);

	// Fetches a photo by its reference
	@GET("/api/feed/photoUrl")
	Call<ResponseBody> getPhoto(
			@Query("photoReference") String photoReference
	);

}