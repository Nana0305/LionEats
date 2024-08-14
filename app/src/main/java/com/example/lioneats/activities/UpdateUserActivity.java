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
import com.example.lioneats.models.UserDTO;
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

public class UpdateUserActivity extends AppCompatActivity {
	private TextView nameText, usernameText;
	private EditText passwordEditText, emailEditText, ageEditText, countryEditText;
	private RadioGroup genderOptions, budgetOptions, spicyOptions;
	private GridLayout dishContainer, allergyOptionsGrid;
	private List<Dish> dishList = new ArrayList<>();
	private List<Allergy> allergyList = new ArrayList<>();
	private List<CheckBox> allergyCheckboxes = new ArrayList<>();
	private Long userId;
	private List<Dish> dishSelections = new ArrayList<>();
	private SharedPreferences userSessionPreferences, userPreferences, dishListPreferences, allergyListPreferences;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_update_user);

		// Initialize UI components
		initializeUIComponents();

		// Initialize SharedPreferences
		initializeSharedPreferences();

		userId = userSessionPreferences.getLong("user_id", -999);

		loadDishesFromPreferences();
		loadAllergiesFromPreferences();
		setFieldsEditable(false);

		setupButtonListeners();

		loadUserFromPreferencesOrFetch();
	}

	private void initializeUIComponents() {
		nameText = findViewById(R.id.nameText);
		usernameText = findViewById(R.id.usernameText);
		passwordEditText = findViewById(R.id.passwordEditText);
		emailEditText = findViewById(R.id.emailEditText);
		countryEditText = findViewById(R.id.countryEditText);
		ageEditText = findViewById(R.id.ageEditText);
		genderOptions = findViewById(R.id.genderOptions);
		budgetOptions = findViewById(R.id.budgetOptions);
		spicyOptions = findViewById(R.id.spicyOptions);
		dishContainer = findViewById(R.id.dishContainer);
		allergyOptionsGrid = findViewById(R.id.allergyOptionsGrid);
	}

	private void initializeSharedPreferences() {
		userSessionPreferences = getSharedPreferences("user_session", MODE_PRIVATE);
		userPreferences = getSharedPreferences("user", MODE_PRIVATE);
		dishListPreferences = getSharedPreferences("dish_list", MODE_PRIVATE);
		allergyListPreferences = getSharedPreferences("allergy_list", MODE_PRIVATE);
	}

	private void setupButtonListeners() {
		Button homeBtn = findViewById(R.id.homeBtn);
		homeBtn.setOnClickListener(v -> {
			Intent intent = new Intent(UpdateUserActivity.this, MainActivity.class);
			startActivity(intent);
		});

		Button editBtn = findViewById(R.id.editBtn);
		Button updateBtn = findViewById(R.id.updateBtn);
		editBtn.setOnClickListener(v -> {
			setFieldsEditable(true);
			editBtn.setVisibility(View.GONE);
			updateBtn.setVisibility(View.VISIBLE);
		});
		updateBtn.setOnClickListener(v -> {
			updateUser();
			editBtn.setVisibility(View.VISIBLE);
			updateBtn.setVisibility(View.GONE);
		});
		updateBtn.setVisibility(View.GONE);
	}

	private void loadAllergiesFromPreferences() {
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

			GridLayout.LayoutParams params = new GridLayout.LayoutParams();
			params.setMargins(8, 8, 8, 8);
			checkBox.setLayoutParams(params);

			allergyCheckboxes.add(checkBox); // Keep track of created checkboxes
			allergyOptionsGrid.addView(checkBox);
		}
	}

	private void loadDishesFromPreferences() {
		String dishJson = dishListPreferences.getString("dishes", null);

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

	private void loadUserFromPreferencesOrFetch() {
		String userJson = userPreferences.getString("user", null);

		if (userJson != null) {
			UserDTO user = new Gson().fromJson(userJson, UserDTO.class);
			updateUI(user);
		} else {
			fetchUserData();
		}
	}

	private void fetchUserData() {
		ApiService apiService = RetrofitClient.getApiService();
		Call<UserDTO> call = apiService.viewUser(userId);

		call.enqueue(new Callback<UserDTO>() {
			@Override
			public void onResponse(Call<UserDTO> call, Response<UserDTO> response) {
				if (response.isSuccessful() && response.body() != null) {
					UserDTO user = response.body();

					// Save user data to SharedPreferences
					saveUserToPreferences(user);

					updateUI(user);
				} else {
					Toast.makeText(UpdateUserActivity.this, "Failed to fetch data", Toast.LENGTH_SHORT).show();
				}
			}

			@Override
			public void onFailure(Call<UserDTO> call, Throwable t) {
				Toast.makeText(UpdateUserActivity.this, "Network Error", Toast.LENGTH_SHORT).show();
				Log.e("UpdateUserActivity", "Network Error: ", t);
			}
		});
	}

	private void saveUserToPreferences(UserDTO user) {
		SharedPreferences.Editor editor = userPreferences.edit();
		editor.putString("user", new Gson().toJson(user));
		editor.apply();
	}

	private void updateUI(UserDTO user) {
		if (user == null) {
			Log.e("UpdateUserActivity", "UserDTO data is null");
			return;
		}

		nameText.setText(user.getName());
		usernameText.setText(user.getUsername());
		passwordEditText.setText(user.getPassword());
		emailEditText.setText(user.getEmail());
		countryEditText.setText(user.getCountry());
		ageEditText.setText(String.valueOf(user.getAge()));
		if (user.isLikesSpicy()) {
			spicyOptions.check(R.id.spicyOption1);
		} else {
			spicyOptions.check(R.id.spicyOption2);
		}
		switch (user.getGender()) {
			case "Male":
				genderOptions.check(R.id.genderOption1);
				break;
			case "Female":
				genderOptions.check(R.id.genderOption2);
				break;
			case "Other":
				genderOptions.check(R.id.genderOption3);
				break;
		}
		switch (user.getPreferredBudget()) {
			case "LOW":
				budgetOptions.check(R.id.budgetOption1);
				break;
			case "MEDIUM":
				budgetOptions.check(R.id.budgetOption2);
				break;
			case "HIGH":
				budgetOptions.check(R.id.budgetOption3);
				break;
		}
		updateAllergySelections(user.getAllergies());
		updateDishSelections(user.getDishPreferences());
	}

	private void updateAllergySelections(List<String> userAllergies) {
		for (CheckBox checkBox : allergyCheckboxes) {
			String allergyName = checkBox.getText().toString();
			boolean isSelected = false;
			for (String allergy : userAllergies) {
				if (allergy.equals(allergyName)) {
					isSelected = true;
					break;
				}
			}
			checkBox.setChecked(isSelected);
		}
	}

	private void updateDishSelections(List<String> dishPrefs) {
		for (int i = 0; i < dishContainer.getChildCount(); i++) {
			View dishView = dishContainer.getChildAt(i);
			TextView dishNameView = dishView.findViewById(R.id.dishName);

			if (dishNameView != null) {
				String dishName = dishNameView.getText().toString();
				if (dishPrefs.contains(dishName)) {
					dishView.setBackground(ContextCompat.getDrawable(this, R.drawable.selected_background));
				} else {
					dishView.setBackground(ContextCompat.getDrawable(this, R.drawable.selector_background));
				}
			}
		}
	}

	private void setFieldsEditable(boolean enabled) {
		passwordEditText.setEnabled(enabled);
		emailEditText.setEnabled(enabled);
		countryEditText.setEnabled(enabled);
		ageEditText.setEnabled(enabled);

		for (int i = 0; i < genderOptions.getChildCount(); i++) {
			genderOptions.getChildAt(i).setEnabled(enabled);
		}

		for (int i = 0; i < budgetOptions.getChildCount(); i++) {
			budgetOptions.getChildAt(i).setEnabled(enabled);
		}

		for (int i = 0; i < spicyOptions.getChildCount(); i++) {
			spicyOptions.getChildAt(i).setEnabled(enabled);
		}

		for (CheckBox checkBox : allergyCheckboxes) {
			checkBox.setClickable(enabled);
			checkBox.setEnabled(enabled);
		}

		for (int i = 0; i < dishContainer.getChildCount(); i++) {
			Dish dish = dishList.get(i);
			View dishView = dishContainer.getChildAt(i);
			dishView.setClickable(enabled);
			if (enabled) {
				dishView.setOnClickListener(v -> toggleDishSelection(dishView, dish));
			} else {
				dishView.setOnClickListener(null);
			}
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

		Log.d("UpdateUserActivity", "Selected Dishes: " + dishSelections.toString());
	}

	public void updateUser() {
		if (!validateInputs()) {
			Toast.makeText(this, "Invalid inputs", Toast.LENGTH_SHORT).show();
			return;
		}

		UserDTO user = createUserFromInput();

		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		String userJson = gson.toJson(user);
		Log.d("UpdateUserActivity", "UserDTO JSON: " + userJson);

		ApiService apiService = RetrofitClient.getApiService();
		Call<ResponseBody> call = apiService.updateUser(userId, user);

		call.enqueue(new Callback<ResponseBody>() {
			@Override
			public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
				if (response.isSuccessful()) {
					Log.d("UpdateUserActivity", "Update Successful: " + response.body().toString());
					setFieldsEditable(false);
					logout();
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
		// Add input validation logic here
		return true;
	}

	private UserDTO createUserFromInput() {
		String name = nameText.getText().toString();
		String username = usernameText.getText().toString();
		String password = passwordEditText.getText().toString();
		String email = emailEditText.getText().toString();
		String country = countryEditText.getText().toString();
		Integer age = Integer.parseInt(ageEditText.getText().toString());
		String gender = getSelectedGender();
		String budget = getSelectedBudget();
		boolean likesSpicy = getSpicyPreference();
		List<String> selectedAllergies = getSelectedAllergies();
		List<String> selectedDishes = getSelectedDishes();

		UserDTO user = new UserDTO();
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
		} else if (selectedId == R.id.genderOption3) {
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

	private void updateSuccessDialog() {
		LayoutInflater inflater = getLayoutInflater();
		View dialogView = inflater.inflate(R.layout.dialog_custom, null);
		TextView dialogMessage = dialogView.findViewById(R.id.dialogMessage);
		dialogMessage.setText("Your profile is successfully updated!");

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setView(dialogView);
		builder.setCancelable(false);
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

	private void logout() {
		SharedPreferences.Editor sessionEditor = userSessionPreferences.edit();
		sessionEditor.clear();
		sessionEditor.apply();

		SharedPreferences.Editor userEditor = userPreferences.edit();
		userEditor.clear();
		userEditor.apply();
	}
}
