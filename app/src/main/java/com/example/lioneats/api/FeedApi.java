package com.example.lioneats.api;
import com.example.lioneats.dtos.NearByShopIdsDTO;
import com.example.lioneats.dtos.ShopDTO;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface FeedApi {

	//@Query is used to map key/value pair for the backend
	@GET("/api/feed/nearByRestaurants")
	Call<NearByShopIdsDTO> getNearbyRestaurantsIdsHolder(
			@Query("lat") double lat,
			@Query("lng") double lng,
			@Query("next_page_token") String nextPageToken,
			@Query("offset_index") Integer offsetIndex
	);

	@GET("/api/feed/getRestaurantDetail")
	Call<ShopDTO> getRestaurantDetail(
			@Query("placeId") String placeId
	);

	@GET("/api/feed/photoUrl")
	Call<ResponseBody> getPhoto(
			@Query("photoReference") String photoReference
	);

}
