package com.example.lioneats.api;

import com.example.lioneats.dtos.CompositeDTO;
import com.example.lioneats.dtos.MRTDTO;
import com.example.lioneats.dtos.SearchRequestDTO;
import com.example.lioneats.dtos.ShopDTO;
import com.example.lioneats.dtos.UserLocationDTO;
import com.example.lioneats.models.Allergy;
import com.example.lioneats.models.Dish;
import com.example.lioneats.models.DishDetail;
import com.example.lioneats.models.ML_feedback;
import com.example.lioneats.models.UserDTO;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface ApiService {
	@POST("api/auth/login")
	Call<Long> login(@Body UserDTO loginRequest);

	@GET("api/user/{id}")
	Call<UserDTO> viewUser(@Path("id") Long id);

	@POST("api/user/register")
	Call<ResponseBody> registerUser(@Body UserDTO user);

	@PUT("api/user/{id}")
	Call<ResponseBody> updateUser(@Path("id") Long id, @Body UserDTO user);

	@GET("api/dishes")
	Call<List<Dish>> getAllDishes();

	@GET("api/dishes/{id}")
	Call<DishDetail> getDishById(@Path("id") int id);

	@GET("api/allergies")
	Call<List<Allergy>> getAllergies();

	@GET("/api/mrt/all")
	Call<List<MRTDTO>> getMRTList();

	@Multipart
	@POST("api/upload")
	Call<ResponseBody> uploadImage(@Part MultipartBody.Part image);

	@POST("api/feedback")
	Call<ResponseBody> submitFeedback(@Body ML_feedback feedback);

	@POST("api/feed/default")
	Call<List<ShopDTO>> getShopsByLocaiton(@Body UserLocationDTO locationDTO);

	@POST("api/feed/default")
	Call<List<ShopDTO>> getShopsDefault();

	@POST("api/feed/search")
	Call<List<ShopDTO>> searchShops(@Body CompositeDTO compositeDTO);

	@POST("api/feed/filter")
	Call<List<ShopDTO>> filterShops(@Body SearchRequestDTO filterRequestDTO);

	@GET("api/shop/{id}")
	Call<ShopDTO> getShopDetail(@Path("id") Long id);

	@POST("api/mrt/nearest")
	Call<List<MRTDTO>> getNearestMRTs(@Body UserLocationDTO userLocation);
}
