package com.example.lioneats.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.lioneats.R;
import com.example.lioneats.api.ApiService;
import com.example.lioneats.models.Dish;
import com.example.lioneats.utils.RetrofitClient;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

import com.example.lioneats.models.User;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class UpdateUserActivity extends AppCompatActivity {

	private TextView nameText, usernameText;
	private EditText passwordEditText, emailEditText, countryEditText;
	private RadioGroup ageOptions, genderOptions, spicyOptions, budgetOptions;
	private final List<String> allergies = Arrays.asList("Gluten", "Seafood", "Dairy", "Egg", "Peanut", "Sesame", "Soy");
	private GridLayout dishContainer;
	private List<Dish> dishList = new ArrayList<>();
	private List<Dish> selectedDishes = new ArrayList<>();
	private List<CheckBox> allergyCheckboxes = new ArrayList<>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_update_user);

		nameText = findViewById(R.id.nameText);
		usernameText = findViewById(R.id.usernameText);
		passwordEditText = findViewById(R.id.passwordEditText);
		emailEditText = findViewById(R.id.emailEditText);
		countryEditText = findViewById(R.id.countryEditText);
		ageOptions = findViewById(R.id.ageOptions);
		genderOptions = findViewById(R.id.genderOptions);
		spicyOptions = findViewById(R.id.spicyOptions);
		budgetOptions = findViewById(R.id.budgetOptions);
		dishContainer = findViewById(R.id.dishContainer);

		for (int i = 0; i < allergies.size(); i++) {
			int resId = getResources().getIdentifier("allergyOption" + (i + 1), "id", getPackageName());
			CheckBox checkBox = findViewById(resId);
			if (checkBox != null) {
				allergyCheckboxes.add(checkBox);
			}
		}

		loadDishesFromPreferences();

		SharedPreferences sharedPreferences = getSharedPreferences("user_session", MODE_PRIVATE);
		String username = sharedPreferences.getString("username", null);
		fetchUserData(username);

		Button editBtn = findViewById(R.id.editBtn);
		Button updateBtn = findViewById(R.id.updateBtn);

		editBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				setFieldsEditable(true);
				editBtn.setVisibility(View.GONE);
				updateBtn.setVisibility(View.VISIBLE);
			}
		});
		updateBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				updateUser();
				editBtn.setVisibility(View.VISIBLE);
				updateBtn.setVisibility(View.GONE);
			}
		});

		updateBtn.setVisibility(View.GONE);
		setFieldsEditable(false);
	}

	private void fetchUserData(String username) {
		ApiService apiService = RetrofitClient.getApiService();
		Call<User> call = apiService.viewUser(username);

		call.enqueue(new Callback<User>() {
			@Override
			public void onResponse(Call<User> call, Response<User> response) {
				if (response.isSuccessful() && response.body() != null) {
					Gson gson = new GsonBuilder().setPrettyPrinting().create();
					String jsonResponse = gson.toJson(response.body());
					Log.d("UpdateUserActivity", "JSON Response: " + jsonResponse);

					User user = response.body();
					updateUI(user);
				} else {
					Toast.makeText(UpdateUserActivity.this, "Failed to fetch data", Toast.LENGTH_SHORT).show();
				}
			}

			@Override
			public void onFailure(Call<User> call, Throwable t) {
				Toast.makeText(UpdateUserActivity.this, "Network Error", Toast.LENGTH_SHORT).show();
				Log.e("UpdateUserActivity", "Network Error: ", t);
			}
		});
	}

	private void updateUI(User user) {
		if (user == null) {
			Log.e("UpdateUserActivity", "User data is null");
			return;
		}

		nameText.setText(user.getName());
		usernameText.setText(user.getUsername());
		passwordEditText.setText(user.getPassword());
		emailEditText.setText(user.getEmail());
		countryEditText.setText(user.getCountry());

		if (user.getAgeGroup() == 1) {
			ageOptions.check(R.id.ageOption1);
		} else if (user.getAgeGroup() == 2) {
			ageOptions.check(R.id.ageOption2);
		} else if (user.getAgeGroup() == 3) {
			ageOptions.check(R.id.ageOption3);
		} else if (user.getAgeGroup() == 4) {
			ageOptions.check(R.id.ageOption4);
		}

		genderOptions.check(user.isMale() ? R.id.genderOption1 : R.id.genderOption2);
		spicyOptions.check(user.isLikesSpicy() ? R.id.spicyOption1 : R.id.spicyOption2);

		switch (user.getBudget()) {
			case "Low":
				budgetOptions.check(R.id.budgetOption1);
				break;
			case "Medium":
				budgetOptions.check(R.id.budgetOption2);
				break;
			case "High":
				budgetOptions.check(R.id.budgetOption3);
				break;
		}

		updateDishSelections(user.getDishPref());
		updateAllergySelections(user.getAllergy());
	}

	private void updateDishSelections(List<Dish> dishPrefs) {
		selectedDishes.clear(); // Clear any existing selected dishes

		// Iterate over the dish container to set the correct backgrounds and selections
		for (int i = 0; i < dishContainer.getChildCount(); i++) {
			View dishView = dishContainer.getChildAt(i);
			TextView dishNameView = dishView.findViewById(R.id.dishName);

			if (dishNameView != null) {
				String dishName = dishNameView.getText().toString();

				// Find the dish in dishList that matches the name
				for (Dish dish : dishList) {
					if (dish.getName().equals(dishName)) {
						if (dishPrefs.contains(dish)) {
							dishView.setBackground(ContextCompat.getDrawable(this, R.drawable.selected_background));
							selectedDishes.add(dish); // Use the existing Dish object from dishList
						} else {
							dishView.setBackground(ContextCompat.getDrawable(this, R.drawable.selector_background));
						}
						break; // Break since we found the match
					}
				}
			}
		}
	}

	private void updateAllergySelections(List<String> userAllergies) {
		for (int i = 0; i < allergyCheckboxes.size(); i++) {
			CheckBox checkBox = allergyCheckboxes.get(i);
			String allergy = allergies.get(i);
			checkBox.setChecked(userAllergies.contains(allergy));
		}
	}

	private void setFieldsEditable(boolean enabled) {
		passwordEditText.setEnabled(enabled);
		emailEditText.setEnabled(enabled);
		countryEditText.setEnabled(enabled);

		// Enable/Disable RadioGroups
		for (int i = 0; i < ageOptions.getChildCount(); i++) {
			ageOptions.getChildAt(i).setEnabled(enabled);
		}

		for (int i = 0; i < genderOptions.getChildCount(); i++) {
			genderOptions.getChildAt(i).setEnabled(enabled);
		}

		for (int i = 0; i < spicyOptions.getChildCount(); i++) {
			spicyOptions.getChildAt(i).setEnabled(enabled);
		}

		for (int i = 0; i < budgetOptions.getChildCount(); i++) {
			budgetOptions.getChildAt(i).setEnabled(enabled);
		}

		for (CheckBox checkBox : allergyCheckboxes) {
			checkBox.setEnabled(enabled);
		}
	}

	public void updateUser() {
		if (!validateInputs()) {
			Toast.makeText(this, "Invalid inputs", Toast.LENGTH_SHORT).show();
			return;
		}

		User user = createUserFromInput();

		// Convert User object to JSON and log it
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		String userJson = gson.toJson(user);
		Log.d("UpdateUserActivity", "User JSON: " + userJson);

		ApiService apiService = RetrofitClient.getApiService();
		Call<ResponseBody> call = apiService.updateUser(user.getUsername(), user);

		call.enqueue(new Callback<ResponseBody>() {
			@Override
			public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
				if (response.isSuccessful()) {
					Log.d("UpdateUserActivity", "Update Successful: " + response.body().toString());
					setFieldsEditable(false);
					updateSuccessDialog();
				} else {
					try {
						Log.e("UpdateUserActivity", "Update Failed: " + response.errorBody().string());
					} catch (IOException e) {
						Log.e("UpdateUserActivity", "Error parsing error response", e);
					}
					Toast.makeText(UpdateUserActivity.this, "Update Failed!", Toast.LENGTH_SHORT).show();
				}
			}

			@Override
			public void onFailure(Call<ResponseBody> call, Throwable t) {
				Log.e("UpdateUserActivity", "Network Error: ", t);
				Toast.makeText(UpdateUserActivity.this, "Network Error!", Toast.LENGTH_SHORT).show();
			}
		});
	}

	private boolean validateInputs() {
		return true;
	}

	private User createUserFromInput() {
		String name = nameText.getText().toString();
		String username = usernameText.getText().toString();
		String password = passwordEditText.getText().toString();
		String email = emailEditText.getText().toString();
		String country = countryEditText.getText().toString();
		int ageGroup = getSelectedAgeGroup();
		boolean isMale = getSelectedGender();
		boolean likesSpicy = getSelectedSpicyOption();
		String budget = getSelectedBudget();
		List<String> selectedAllergies = getSelectedAllergies();

		User user = new User();
		user.setName(name);
		user.setUsername(username);
		user.setPassword(password);
		user.setEmail(email);
		user.setAgeGroup(ageGroup);
		user.setMale(isMale);
		user.setCountry(country);
		user.setDishPref(selectedDishes);
		user.setLikesSpicy(likesSpicy);
		user.setBudget(budget);
		user.setAllergy(selectedAllergies);

		return user;
	}

	private int getSelectedAgeGroup() {
		int selectedId = ageOptions.getCheckedRadioButtonId();
		if (selectedId == R.id.ageOption1) {
			return 1; // <21
		} else if (selectedId == R.id.ageOption2) {
			return 2; // 21-35
		} else if (selectedId == R.id.ageOption3) {
			return 3; // 35-60
		} else if (selectedId == R.id.ageOption4) {
			return 4; // >60
		} else {
			return 0;
		}
	}

	private boolean getSelectedGender() {
		int selectedId = genderOptions.getCheckedRadioButtonId();
		return selectedId == R.id.genderOption1;
	}

	private boolean getSelectedSpicyOption() {
		int selectedId = spicyOptions.getCheckedRadioButtonId();
		return selectedId == R.id.spicyOption1;
	}

	private String getSelectedBudget() {
		int selectedId = budgetOptions.getCheckedRadioButtonId();
		if (selectedId == R.id.budgetOption1) {
			return "Low";
		} else if (selectedId == R.id.budgetOption2) {
			return "Medium";
		} else if (selectedId == R.id.budgetOption3) {
			return "High";
		} else {
			return "";
		}
	}

	private List<String> getSelectedAllergies() {
		List<String> selectedAllergies = new ArrayList<>();
		for (CheckBox checkBox : allergyCheckboxes) {
			if (checkBox.isChecked()) {
				selectedAllergies.add(checkBox.getText().toString());
			}
		}
		return selectedAllergies;
	}
	private void updateSuccessDialog() {
		LayoutInflater inflater = getLayoutInflater();
		View dialogView = inflater.inflate(R.layout.dialog_custom, null);
		TextView dialogMessage = dialogView.findViewById(R.id.dialogMessage);
		dialogMessage.setText("Your profile is successfully updated!");

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setView(dialogView);
		builder.setCancelable(false); // Make the dialog non-cancelable

		AlertDialog dialog = builder.create();
		dialog.show();

		new Handler().postDelayed(() -> {
			dialog.dismiss();
			redirectToMainActivity();
		}, 3000);
	}

	private void redirectToMainActivity() {
		Intent intent = new Intent(UpdateUserActivity.this, LoginActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
		finish();
	}

	private void loadDishesFromPreferences() {
		SharedPreferences sharedPreferences = getSharedPreferences("dish_list", MODE_PRIVATE);
		String dishJson = sharedPreferences.getString("dishes", null);

		if (dishJson != null) {
			Type listType = new TypeToken<List<Dish>>() {}.getType();
			dishList = new Gson().fromJson(dishJson, listType);
			populateDishPreferences();
		} else {
			Toast.makeText(this, "No dishes found", Toast.LENGTH_SHORT).show();
			Log.e("UpdateUserActivity", "No dishes found in SharedPreferences");
		}
	}

	private void populateDishPreferences() {
		dishContainer.removeAllViews(); // Clear any existing views

		for (Dish dish : dishList) {
			// Inflate the dish item layout
			View dishView = LayoutInflater.from(this).inflate(R.layout.item_dish, dishContainer, false);

			ImageView dishImage = dishView.findViewById(R.id.dishImage);
			TextView dishName = dishView.findViewById(R.id.dishName);

			// Load the image using Picasso
			Picasso.get()
					.load(dish.getImageUrl())
					.placeholder(R.drawable.default_image) // Optional: Placeholder image
					.into(dishImage);

			// Set the dish name
			dishName.setText(dish.getName());

			// Set initial background based on selection
			if (selectedDishes.contains(dish)) {
				dishView.setBackground(ContextCompat.getDrawable(this, R.drawable.selected_background));
			} else {
				dishView.setBackground(ContextCompat.getDrawable(this, R.drawable.selector_background));
			}

			// Set click listener to toggle selection
			dishView.setOnClickListener(v -> toggleDishSelection(dishView, dish));

			// Add the dish view to the container
			dishContainer.addView(dishView);
		}
	}

	private void toggleDishSelection(View dishView, Dish dish) {
		if (selectedDishes.contains(dish)) {
			selectedDishes.remove(dish);
			dishView.setBackground(ContextCompat.getDrawable(this, R.drawable.selector_background)); // Normal background
		} else {
			selectedDishes.add(dish);
			dishView.setBackground(ContextCompat.getDrawable(this, R.drawable.selected_background)); // Selected background
		}

		Log.d("UpdateUserActivity", "Selected Dishes: " + selectedDishes.toString());
	}
}