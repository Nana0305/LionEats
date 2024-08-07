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
import com.example.lioneats.models.Dish;
import com.example.lioneats.models.DishDetail;
import com.example.lioneats.utils.RetrofitClient;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class DishDetailsActivity extends AppCompatActivity {
	private TextView dishNameText, dishAllergiesText, dishIngredientsText, dishHistoryText, dishDescriptionText;
	private ImageView dishImage;
	private List<Dish> dishList = new ArrayList<>();
	private SharedPreferences dishListPreferences;

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

		dishListPreferences = getSharedPreferences("dish_list", MODE_PRIVATE);
		int dishID = getIntent().getIntExtra("dishID", -1);
		String dishImageUrl = getIntent().getStringExtra("dishImageUrl");
		if (dishID != -1){
			setDishImage(dishImageUrl);
			fetchDishData(dishID);
		} else {
			Toast.makeText(this, "Invalid dish ID", Toast.LENGTH_SHORT).show();
		}
	}
	private void setDishImage(String dishImageUrl) {
		Picasso.get()
				.load(dishImageUrl)
				.placeholder(R.drawable.default_image)
				.into(dishImage);
	}

	private void fetchDishData(int dishID){
		ApiService apiService = RetrofitClient.getApiService();
		Call<DishDetail> call = apiService.getDishById(dishID);

		call.enqueue(new Callback<DishDetail>() {
			@Override
			public void onResponse(Call<DishDetail> call, Response<DishDetail> response) {
				if (response.isSuccessful() && response.body() != null) {
					Toast.makeText(DishDetailsActivity.this, "Data fetched", Toast.LENGTH_SHORT).show();
					Gson gson = new GsonBuilder().setPrettyPrinting().create();
					String jsonResponse = gson.toJson(response.body());
					Log.d("DishDetailsActivity", "JSON Response: " + jsonResponse);

					DishDetail dishDetail = response.body();
					updateUI(dishDetail);
				} else {
					Toast.makeText(DishDetailsActivity.this, "Failed to fetch data", Toast.LENGTH_SHORT).show();
				}
			}

			@Override
			public void onFailure(Call<DishDetail> call, Throwable t) {
				Toast.makeText(DishDetailsActivity.this, "Network Error", Toast.LENGTH_SHORT).show();
				Log.e("DishDetailsActivity", "Network Error: ", t);
			}
		});
	}

	private void updateUI(DishDetail dishDetail){
		dishNameText.setText(dishDetail.getName());
		dishIngredientsText.setText(dishDetail.getIngredients());
		dishHistoryText.setText(dishDetail.getHistory());
		dishDescriptionText.setText(dishDetail.getDescription());

		StringBuilder dishAllergies = new StringBuilder();
		for (String allergy : dishDetail.getAllergies()) {
			dishAllergies.append(allergy).append(" allergy    ");
		}
		dishAllergiesText.setText(dishAllergies.toString());
	}
}
