package com.example.lioneats.api;

import com.example.lioneats.models.Dish;
import com.example.lioneats.models.LoginRequest;
import com.example.lioneats.models.LoginResponse;
import com.example.lioneats.models.User;
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
	Call<User> getUser(@Path("username") String username);

	@POST("api/registerUser")
	Call<ResponseBody> registerUser(@Body User user);

	@POST("api/updateUser")
	Call<ResponseBody> updateUser(@Body User user);

	@GET("api/dish/{id}")
	Call<Dish> getDishById(@Path("id") int id);
}
