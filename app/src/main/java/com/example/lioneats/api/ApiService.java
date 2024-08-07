package com.example.lioneats.api;

import com.example.lioneats.models.Dish;
import com.example.lioneats.models.DishDetail;
import com.example.lioneats.models.LoginRequest;
import com.example.lioneats.models.LoginResponse;
import com.example.lioneats.models.ML_feedback;
import com.example.lioneats.models.User;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ApiService {
	@POST("api/login")
	Call<LoginResponse> login(@Body LoginRequest loginRequest);

	@GET("api/user/{username}")
	Call<User> viewUser(@Path("username") String username);

	@POST("api/register")
	Call<ResponseBody> registerUser(@Body User user);

	@POST("api/user/{username}/update")
	Call<ResponseBody> updateUser(@Path("username") String username, @Body User user);

	@GET("api/dishes")
	Call<List<Dish>> getAllDishes();

	@GET("api/dishes/{id}")
	Call<DishDetail> getDishById(@Path("id") int id);

	@POST("/api/upload/result")
	Call<ResponseBody> dishResult(@Body MultipartBody.Part image);

	@POST("api/upload/result/feedback")
	Call<ResponseBody> feedback(@Body ML_feedback feedback);
}
