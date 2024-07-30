package com.example.lioneats;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import model.Dish;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class DishDetailsActivity extends AppCompatActivity {
	private TextView dishNameText, dishAllergiesText, dishIngredientsText, dishHistoryText, dishDescriptionText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_dish_details);

		dishNameText = findViewById(R.id.dishNameText);
		dishAllergiesText = findViewById(R.id.dishAllergiesText);
		dishIngredientsText = findViewById(R.id.dishIngredientsText);
		dishHistoryText = findViewById(R.id.dishHistoryText);
		dishDescriptionText = findViewById(R.id.dishDescriptionText);

		fetchDishData();
	}

	private void fetchDishData(){
		String baseUrl = "https://a867fedb-31a5-49ed-924f-cc87386050ec.mock.pstmn.io"; // Replace with your actual mock server URL

		Retrofit retrofit = new Retrofit.Builder()
				.baseUrl(baseUrl)
				.addConverterFactory(GsonConverterFactory.create())
				.build();

		ApiService apiService = retrofit.create(ApiService.class);
		Call<Dish> call = apiService.getDish();

		call.enqueue(new Callback<Dish>() {
			@Override
			public void onResponse(Call<Dish> call, Response<Dish> response) {
				if (response.isSuccessful() && response.body() != null) {
					Toast.makeText(DishDetailsActivity.this, "Data fetched", Toast.LENGTH_SHORT).show();
					Gson gson = new GsonBuilder().setPrettyPrinting().create();
					String jsonResponse = gson.toJson(response.body());
					Log.d("DishDetailsActivity", "JSON Response: " + jsonResponse);

					Dish dish = response.body();
					updateUI(dish);
				} else {
					Toast.makeText(DishDetailsActivity.this, "Failed to fetch data", Toast.LENGTH_SHORT).show();
				}
			}

			@Override
			public void onFailure(Call<Dish> call, Throwable t) {
				Toast.makeText(DishDetailsActivity.this, "Network Error", Toast.LENGTH_SHORT).show();
				Log.e("DishDetailsActivity", "Network Error: ", t);
			}
		});
	}

	private void updateUI(Dish dish){
		dishNameText.setText(dish.getDishName());
		dishIngredientsText.setText(dish.getDishIngredients());
		dishHistoryText.setText(dish.getDishHistory());
		dishDescriptionText.setText(dish.getDishDescription());

		StringBuilder dishAllergies = new StringBuilder();
		for (String allergy : dish.getDishAllergies()) {
			dishAllergies.append(allergy).append(" allergy    ");
		}
		dishAllergiesText.setText(dishAllergies.toString());
	}
}
