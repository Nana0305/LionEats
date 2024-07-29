package com.example.lioneats;

import java.util.List;

import model.User;
import okhttp3.ResponseBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ApiService {

	@POST("api/registerUser")
	Call<ResponseBody> registerUser(@Body User user);
}
