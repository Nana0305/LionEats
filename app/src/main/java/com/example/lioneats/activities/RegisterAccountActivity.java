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
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.gridlayout.widget.GridLayout;

import com.example.lioneats.R;
import com.example.lioneats.api.ApiService;
import com.example.lioneats.models.Allergy;
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
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterAccountActivity extends AppCompatActivity {
	private EditText nameText, usernameText, passwordText, emailText, ageText, countryText;
	private RadioGroup genderOptions, budgetOptions, spicyOptions;
	private GridLayout dishContainer, allergyOptionsGrid;
	private List<Dish> dishList = new ArrayList<>();
	private List<Allergy> allergyList = new ArrayList<>();
	private List<CheckBox> allergyCheckboxes = new ArrayList<>();
	private List<Dish> dishSelections = new ArrayList<>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register_account);

		nameText = findViewById(R.id.nameText);
		usernameText = findViewById(R.id.usernameText);
		passwordText = findViewById(R.id.passwordText);
		emailText = findViewById(R.id.emailText);
		countryText = findViewById(R.id.countryText);
		ageText = findViewById(R.id.ageText);
		genderOptions = findViewById(R.id.genderOptions);
		budgetOptions = findViewById(R.id.budgetOptions);
		spicyOptions = findViewById(R.id.spicyOptions);
		dishContainer = findViewById(R.id.dishContainer);
		allergyOptionsGrid = findViewById(R.id.allergyOptionsGrid);

		loadDishesFromPreferences();
		loadAllergiesFromPreferences();

		Button homeBtn = findViewById(R.id.homeBtn);
		homeBtn.setOnClickListener(v -> {
			Intent intent = new Intent(RegisterAccountActivity.this, MainActivity.class);
			startActivity(intent);
		});
		Button registerBtn = findViewById(R.id.registerBtn);
		registerBtn.setOnClickListener(v -> registerUser());
	}


	private void loadAllergiesFromPreferences() {
		SharedPreferences allergyListPreferences = getSharedPreferences("allergy_list", MODE_PRIVATE);
		String allergyJson = allergyListPreferences.getString("allergies", null);

		if (allergyJson != null) {
			Type listType = new TypeToken<List<Allergy>>() {}.getType();
			allergyList = new Gson().fromJson(allergyJson, listType);

			populateAllergyCheckBoxes();
		} else {
			Toast.makeText(this, "No allergies found", Toast.LENGTH_SHORT).show();
		}
	}

	private void populateAllergyCheckBoxes() {
		allergyOptionsGrid.removeAllViews(); // Clear existing views

		for (Allergy allergy : allergyList) {
			CheckBox checkBox = new CheckBox(this);
			checkBox.setText(allergy.getName());
			checkBox.setTextSize(15);
			checkBox.setId(View.generateViewId());

			// Add CheckBox to GridLayout
			GridLayout.LayoutParams params = new GridLayout.LayoutParams();
			params.setMargins(8, 8, 8, 8);
			checkBox.setLayoutParams(params);

			allergyCheckboxes.add(checkBox); // Keep track of created checkboxes
			allergyOptionsGrid.addView(checkBox);
		}
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
			Log.e("RegisterAccountActivity", "No dishes found in SharedPreferences");
		}
	}

	private void populateDishPreferences() {
		dishContainer.removeAllViews(); // Clear any existing views

		for (Dish dish : dishList) {
			View dishView = LayoutInflater.from(this).inflate(R.layout.item_dish, dishContainer, false);

			ImageView dishImage = dishView.findViewById(R.id.dishImage);
			TextView dishName = dishView.findViewById(R.id.dishName);

			Picasso.get()
					.load(dish.getImageUrl())
					.placeholder(R.drawable.default_image) // Optional: Placeholder image
					.into(dishImage);

			dishName.setText(dish.getDishDetailName());
			dishView.setOnClickListener(v -> toggleDishSelection(dishView, dish));
			dishContainer.addView(dishView);
		}
	}

	private void toggleDishSelection(View dishView, Dish dish) {
		if (dishSelections.contains(dish)) {
			dishSelections.remove(dish);
			dishView.setBackground(ContextCompat.getDrawable(this, R.drawable.selector_background)); // Normal background
		} else {
			dishSelections.add(dish);
			dishView.setBackground(ContextCompat.getDrawable(this, R.drawable.selected_background)); // Selected background
		}

		Log.d("RegisterAccountActivity", "Selected Dishes: " + dishSelections.toString());
	}

	public void registerUser() {
		if (!validateInputs()) {
			Toast.makeText(this, "Invalid inputs", Toast.LENGTH_SHORT).show();
			return;
		}

		User user = createUserFromInput();

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
		Integer age = Integer.parseInt(ageText.getText().toString());
		String gender = getSelectedGender();
		String budget = getSelectedBudget();
		boolean likesSpicy = getSpicyPreference();
		List<String> selectedAllergies = getSelectedAllergies();
		List<String> selectedDishes = getSelectedDishes();

		User user = new User();
		user.setName(name);
		user.setUsername(username);
		user.setPassword(password);
		user.setEmail(email);
		user.setAge(age);
		user.setGender(gender);
		user.setCountry(country);
		user.setLikesSpicy(likesSpicy);
		user.setPreferredBudget(budget);
		user.setAllergies(selectedAllergies);
		user.setDishPreferences(selectedDishes);
		return user;
	}

	private String getSelectedGender() {
		int selectedId = genderOptions.getCheckedRadioButtonId();
		if (selectedId == R.id.genderOption1) {
			return "Male";
		} else if (selectedId == R.id.genderOption2) {
			return "Female";
		} else if (selectedId == R.id.genderOption3){
			return "Other";
		} else {
			return "";
		}
	}

	private String getSelectedBudget() {
		int selectedId = budgetOptions.getCheckedRadioButtonId();
		if (selectedId == R.id.budgetOption1) {
			return "LOW";
		} else if (selectedId == R.id.budgetOption2) {
			return "MEDIUM";
		} else if (selectedId == R.id.budgetOption3) {
			return "HIGH";
		} else {
			return "";
		}
	}

	private boolean getSpicyPreference() {
		int selectedId = spicyOptions.getCheckedRadioButtonId();
		return selectedId == R.id.spicyOption1; // Returns true if "Yes, of course!" is selected
	}

	private List<String> getSelectedAllergies() {
		List<String> selectedAllergies = new ArrayList<>();
		for (CheckBox checkBox : allergyCheckboxes) {
			if (checkBox.isChecked()) {
				String allergyName = checkBox.getText().toString();
				selectedAllergies.add(allergyName);
			}
		}
		return selectedAllergies;
	}

	private List<String> getSelectedDishes() {
		List<String> selectedDishes = new ArrayList<>();
		for (Dish dish : dishSelections) {
			String dishName = dish.getDishDetailName();
			selectedDishes.add(dishName);
		}
		return selectedDishes;
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
