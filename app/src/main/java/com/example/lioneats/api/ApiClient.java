package com.example.lioneats.api;

import android.content.Context;
import android.content.SharedPreferences;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
	private static Retrofit retrofit = null;

	public static Retrofit getClient(Context context) {
		if (retrofit == null) {
			SharedPreferences sharedPreferences = context.getSharedPreferences("user_session", Context.MODE_PRIVATE);

			OkHttpClient client = new OkHttpClient.Builder()
					.addInterceptor(new TokenInterceptor(sharedPreferences))
					.build();

			retrofit = new Retrofit.Builder()
					.baseUrl("https://your-backend-url.com/")
					.client(client)
					.addConverterFactory(GsonConverterFactory.create())
					.build();
		}
		return retrofit;
	}
}
