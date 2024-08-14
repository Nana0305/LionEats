package com.example.lioneats.utils;

import com.example.lioneats.api.ApiService;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

	//private static final String BASE_URL = "https://a867fedb-31a5-49ed-924f-cc87386050ec.mock.pstmn.io";
	private static final String BASE_URL = "http://10.0.2.2:8080";
	//private static final String BASE_URL = "https://lioneats.azurewebsites.net";
	private static Retrofit retrofit = null;

	private RetrofitClient() {}

	public static Retrofit getInstance() {
		if (retrofit == null) {
			retrofit = new Retrofit.Builder()
					.baseUrl(BASE_URL)
					.addConverterFactory(GsonConverterFactory.create())
					.build();
		}
		return retrofit;
	}
	public static ApiService getApiService() {
		return getInstance().create(ApiService.class);
	}
}

