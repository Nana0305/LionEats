package com.example.lioneats.api;

import android.content.SharedPreferences;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
public class TokenInterceptor implements Interceptor {
	private SharedPreferences sharedPreferences;

	public TokenInterceptor(SharedPreferences sharedPreferences) {
		this.sharedPreferences = sharedPreferences;
	}

	@Override
	public Response intercept(Chain chain) throws IOException {
		Request originalRequest = chain.request();

		String token = sharedPreferences.getString("token", null);
		if (token == null) {
			return chain.proceed(originalRequest);
		}

		Request.Builder builder = originalRequest.newBuilder()
				.header("Authorization", "Bearer " + token);
		Request newRequest = builder.build();

		return chain.proceed(newRequest);
	}
}
