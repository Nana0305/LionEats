package com.example.lioneats.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.example.lioneats.R;
import com.example.lioneats.api.ApiService;
import com.example.lioneats.fragments.HeaderFragment;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import com.example.lioneats.models.Dish;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class DishDetailsActivity extends AppCompatActivity {
	private TextView dishNameText, dishAllergiesText, dishIngredientsText, dishHistoryText, dishDescriptionText;
	private ImageView dishImage;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_dish_details);

		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		transaction.replace(R.id.headerFragmentContainer, new HeaderFragment());
		transaction.commit();

		dishNameText = findViewById(R.id.dishNameText);
		dishAllergiesText = findViewById(R.id.dishAllergiesText);
		dishIngredientsText = findViewById(R.id.dishIngredientsText);
		dishHistoryText = findViewById(R.id.dishHistoryText);
		dishDescriptionText = findViewById(R.id.dishDescriptionText);
		dishImage = findViewById(R.id.dishImage);

		int dishID = getIntent().getIntExtra("dishID", -1);
		if (dishID != -1){
			setDishImage(dishID);
			fetchDishData(dishID);
		} else {
			Toast.makeText(this, "Invalid dish ID", Toast.LENGTH_SHORT).show();
		}
	}

	private void setDishImage(int dishID) {
		int imageResource = getDrawableResourceByDishID(dishID);
		dishImage.setImageResource(imageResource);
	}

	private int getDrawableResourceByDishID(int dishID) {
		switch (dishID) {
			case 1:
				return R.drawable.dish_image_1;
			case 2:
				return R.drawable.dish_image_2;
			case 3:
				return R.drawable.dish_image_3;
			case 4:
				return R.drawable.dish_image_4;
			case 5:
				return R.drawable.dish_image_5;
			case 6:
				return R.drawable.dish_image_6;
			case 7:
				return R.drawable.dish_image_7;
			case 8:
				return R.drawable.dish_image_8;
			case 9:
				return R.drawable.dish_image_9;
			case 10:
				return R.drawable.dish_image_10;
			default:
				return R.drawable.default_image;
		}
	}

	private void fetchDishData(int dishID){
		String baseUrl = "https://a867fedb-31a5-49ed-924f-cc87386050ec.mock.pstmn.io/"; // Make sure this ends with a slash

		Retrofit retrofit = new Retrofit.Builder()
				.baseUrl(baseUrl)
				.addConverterFactory(GsonConverterFactory.create())
				.build();

		ApiService apiService = retrofit.create(ApiService.class);
		Call<Dish> call = apiService.getDishById(dishID);

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
