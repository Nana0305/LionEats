package com.example.lioneats.api;

import com.example.lioneats.models.Allergy;
import com.example.lioneats.models.Dish;
import com.example.lioneats.models.DishDetail;
import com.example.lioneats.models.ML_feedback;
import com.example.lioneats.models.User;

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
	Call<Long> login(@Body User loginRequest);

	@GET("api/user/{id}")
	Call<User> viewUser(@Path("id") Long id);

	@POST("api/user/register")
	Call<ResponseBody> registerUser(@Body User user);

	@PUT("api/user/{id}")
	Call<ResponseBody> updateUser(@Path("id") Long id, @Body User user);

	@GET("api/dishes")
	Call<List<Dish>> getAllDishes();

	@GET("api/dishes/{id}")
	Call<DishDetail> getDishById(@Path("id") int id);

	@GET("api/allergies")
	Call<List<Allergy>> getAllergies();

	@Multipart
	@POST("api/upload")
	Call<ResponseBody> uploadImage(@Part MultipartBody.Part image);

	@POST("api/feedback")
	Call<ResponseBody> submitFeedback(@Body ML_feedback feedback);
}
