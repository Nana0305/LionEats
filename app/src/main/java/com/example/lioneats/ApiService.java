package com.example.lioneats;

import java.util.List;

import model.Dish;
import model.User;
import okhttp3.ResponseBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ApiService {

	@GET("api/user")
	Call<User> getUser();

	@POST("api/registerUser")
	Call<ResponseBody> registerUser(@Body User user);

	@POST("api/updateUser")
	Call<ResponseBody> updateUser(@Body User user);

	@GET("api/dish/{id}")
	Call<Dish> getDishById(@Path("id") int id);
}
