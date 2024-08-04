package com.example.lioneats.utils;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitService {
	private static Retrofit retrofit;
	private static final String BASE_URL = "http://192.168.1.206:8080";
	// private static final String BASE_URL = "http://10.249.4.214:8080";

	public static Retrofit getInstance() {
		if (retrofit == null) {
			OkHttpClient client = new OkHttpClient.Builder()
					.connectTimeout(30, TimeUnit.SECONDS)
					.readTimeout(30, TimeUnit.SECONDS)
					.writeTimeout(30, TimeUnit.SECONDS)
					.build();
			retrofit = new Retrofit.Builder()
					.baseUrl(BASE_URL)
					.addConverterFactory(GsonConverterFactory.create())
					.client(client)
					.build();
		}
		return retrofit;
	}
}