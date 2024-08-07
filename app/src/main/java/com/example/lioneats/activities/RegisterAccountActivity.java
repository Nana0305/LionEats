package com.example.lioneats.activities;

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
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.lioneats.R;
import com.example.lioneats.api.ApiService;
import com.example.lioneats.models.Dish;
import com.example.lioneats.models.User;
import com.example.lioneats.utils.RetrofitClient;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterAccountActivity extends AppCompatActivity {

	private EditText nameText, usernameText, passwordText, emailText, countryText;
	private RadioGroup ageOptions, genderOptions, spicyOptions, budgetOptions;
	private GridLayout dishContainer;
	private final List<String> allergies = Arrays.asList("Gluten", "Dairy", "Seafood", "Peanut", "Egg", "Sesame", "Soy");
	private List<Dish> dishList = new ArrayList<>();
	private List<Dish> selectedDishes = new ArrayList<>();
	private List<CheckBox> allergyCheckboxes = new ArrayList<>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register_account);

		nameText = findViewById(R.id.nameText);
		usernameText = findViewById(R.id.usernameText);
		passwordText = findViewById(R.id.passwordText);
		emailText = findViewById(R.id.emailText);
		countryText = findViewById(R.id.countryText);
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

		Button registerBtn = findViewById(R.id.registerBtn);
		registerBtn.setOnClickListener(v -> registerUser());
	}

	public void registerUser() {
		if (!validateInputs()) {
			Toast.makeText(this, "Invalid inputs", Toast.LENGTH_SHORT).show();
			return;
		}

		User user = createUserFromInput();

		// Convert User object to JSON and log it
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		String userJson = gson.toJson(user);
		Log.d("RegisterAccountActivity", "User JSON: " + userJson);

		ApiService apiService = RetrofitClient.getApiService();
		Call<ResponseBody> call = apiService.registerUser(user);

		call.enqueue(new Callback<ResponseBody>() {
			@Override
			public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
				if (response.isSuccessful()) {
					Log.d("RegisterAccountActivity", "Registration Successful: " + response.body().toString());
					registerSuccessDialog();
				} else {
					try {
						Log.e("RegisterAccountActivity", "Registration Failed: " + response.errorBody().string());
					} catch (IOException e) {
						Log.e("RegisterAccountActivity", "Error parsing error response", e);
					}
					Toast.makeText(RegisterAccountActivity.this, "Registration Failed!", Toast.LENGTH_SHORT).show();
				}
			}

			@Override
			public void onFailure(Call<ResponseBody> call, Throwable t) {
				Log.e("RegisterAccountActivity", "Network Error: ", t);
				Toast.makeText(RegisterAccountActivity.this, "Network Error!", Toast.LENGTH_SHORT).show();
			}
		});
	}

	private boolean validateInputs() {
		// Add input validation logic here
		return true;
	}

	private User createUserFromInput() {
		String name = nameText.getText().toString();
		String username = usernameText.getText().toString();
		String password = passwordText.getText().toString();
		String email = emailText.getText().toString();
		String country = countryText.getText().toString();
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
		user.setDishPref(selectedDishes); // Use selectedDishes here
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

	private void loadDishesFromPreferences() {
		// Load the dish list from shared preferences or a database
		SharedPreferences sharedPreferences = getSharedPreferences("dish_list", MODE_PRIVATE);
		String dishJson = sharedPreferences.getString("dishes", null);

		if (dishJson != null) {
			Type listType = new TypeToken<List<Dish>>() {}.getType();
			dishList = new Gson().fromJson(dishJson, listType);
			populateDishPreferences();
		} else {
			Toast.makeText(this, "No dishes found", Toast.LENGTH_SHORT).show();
			Log.e("RegisterAccountActivity", "No dishes found in SharedPreferences");
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

		Log.d("RegisterAccountActivity", "Selected Dishes: " + selectedDishes.toString());
	}

	private void registerSuccessDialog() {
		LayoutInflater inflater = getLayoutInflater();
		View dialogView = inflater.inflate(R.layout.dialog_custom, null);
		TextView dialogMessage = dialogView.findViewById(R.id.dialogMessage);
		dialogMessage.setText("Successful Registration!");

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setView(dialogView);
		builder.setCancelable(false); // Make the dialog non-cancelable

		AlertDialog dialog = builder.create();
		dialog.show();

		new Handler().postDelayed(() -> {
			dialog.dismiss();
			redirectToLoginActivity();
		}, 3000);
	}

	private void redirectToLoginActivity() {
		Intent intent = new Intent(RegisterAccountActivity.this, LoginActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
		finish();
	}
}
