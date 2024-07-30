package com.example.lioneats;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

import model.User;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class UpdateUserActivity extends AppCompatActivity {

	private TextView nameText, usernameText;
	private EditText passwordText, emailText, countryText;
	private RadioGroup ageOptions, genderOptions, spicyOptions, budgetOptions;
	private LinearLayout dishOption1, dishOption2, dishOption3, dishOption4, dishOption5;
	private List<LinearLayout> dishOptionsList;
	private final List<String> dishPreferences = Arrays.asList("Chicken rice", "Bak kut teh", "Char kway teow", "Kaya toast", "Nasi lemak");
	private final List<String> allergies = Arrays.asList("Gluten", "Dairy", "Seafood", "Peanut", "Egg", "Sesame", "Soy");

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_update_user);

		nameText = findViewById(R.id.nameText);
		usernameText = findViewById(R.id.usernameText);
		passwordText = findViewById(R.id.passwordText);
		emailText = findViewById(R.id.emailText);
		countryText = findViewById(R.id.countryText);
		ageOptions = findViewById(R.id.ageOptions);
		genderOptions = findViewById(R.id.genderOptions);
		spicyOptions = findViewById(R.id.spicyOptions);
		budgetOptions = findViewById(R.id.budgetOptions);

		dishOption1 = findViewById(R.id.dishOption1);
		dishOption2 = findViewById(R.id.dishOption2);
		dishOption3 = findViewById(R.id.dishOption3);
		dishOption4 = findViewById(R.id.dishOption4);
		dishOption5 = findViewById(R.id.dishOption5);

		dishOptionsList = Arrays.asList(dishOption1, dishOption2, dishOption3, dishOption4, dishOption5);

		fetchUserData();

		Button updateBtn = findViewById(R.id.updateBtn);
		updateBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				updateUser();
			}
		});
	}

	private void fetchUserData() {
		String baseUrl = "https://a867fedb-31a5-49ed-924f-cc87386050ec.mock.pstmn.io"; // Replace with your actual mock server URL

		Retrofit retrofit = new Retrofit.Builder()
				.baseUrl(baseUrl)
				.addConverterFactory(GsonConverterFactory.create())
				.build();

		ApiService apiService = retrofit.create(ApiService.class);
		Call<User> call = apiService.getUser();

		call.enqueue(new Callback<User>() {
			@Override
			public void onResponse(Call<User> call, Response<User> response) {
				if (response.isSuccessful() && response.body() != null) {
					Toast.makeText(UpdateUserActivity.this, "Data fetched", Toast.LENGTH_SHORT).show();
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
		nameText.setText(user.getName());
		usernameText.setText(user.getUsername());
		passwordText.setText(user.getPassword());
		emailText.setText(user.getEmail());
		countryText.setText(user.getCountry());

		if (user.getAgeGroup() == 1) {
			ageOptions.check(R.id.ageOption1);
		} else if (user.getAgeGroup() == 2) {
			ageOptions.check(R.id.ageOption2);
		} else if (user.getAgeGroup() == 3) {
			ageOptions.check(R.id.ageOption3);
		} else if (user.getAgeGroup() == 4) {
			ageOptions.check(R.id.ageOption4);
		}

		if (user.isMale()) {
			genderOptions.check(R.id.genderOption1);
		} else {
			genderOptions.check(R.id.genderOption2);
		}

		if (user.isLikesSpicy()) {
			spicyOptions.check(R.id.spicyOption1);
		} else {
			spicyOptions.check(R.id.spicyOption2);
		}

		if (user.getBudget().equals("Low")) {
			budgetOptions.check(R.id.budgetOption1);
		} else if (user.getBudget().equals("Medium")) {
			budgetOptions.check(R.id.budgetOption2);
		} else if (user.getBudget().equals("High")) {
			budgetOptions.check(R.id.budgetOption3);
		}

		for (int i = 0; i < dishPreferences.size(); i++) {
			if (user.getDishPref().contains(dishPreferences.get(i))) {
				dishOptionsList.get(i).setSelected(true);
			}
		}

		for (String allergy : user.getAllergy()) {
			int resId = getResources().getIdentifier("allergyOption" + (allergies.indexOf(allergy) + 1), "id", getPackageName());
			CheckBox checkBox = findViewById(resId);
			if (checkBox != null) {
				checkBox.setChecked(true);
			}
		}
	}

	public void updateUser() {
		// Your Postman Mock Server URL (using HTTPS)
		String baseUrl = "https://a867fedb-31a5-49ed-924f-cc87386050ec.mock.pstmn.io"; // Use HTTPS

		// Collect input data
		String name = nameText.getText().toString();
		String username = usernameText.getText().toString();
		String password = passwordText.getText().toString();
		String email = emailText.getText().toString();
		String country = countryText.getText().toString();
		int ageGroup = getSelectedAgeGroup();
		boolean isMale = getSelectedGender();
		boolean likesSpicy = getSelectedSpicyOption();
		String budget = getSelectedBudget();
		List<String> selectedDishPrefs = getSelectedDishPreferences();
		List<String> selectedAllergies = getSelectedAllergies();

		// Create User object and set its fields
		User user = new User();
		user.setName(name);
		user.setUsername(username);
		user.setPassword(password);
		user.setEmail(email);
		user.setAgeGroup(ageGroup);
		user.setMale(isMale);
		user.setCountry(country);
		user.setDishPref(selectedDishPrefs);
		user.setLikesSpicy(likesSpicy);
		user.setBudget(budget);
		user.setAllergy(selectedAllergies);

		// Convert User object to JSON and log it
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		String userJson = gson.toJson(user);
		Log.d("RegisterAccountActivity", "User JSON: " + userJson);

		// Create Retrofit instance
		Retrofit retrofit = new Retrofit.Builder()
				.baseUrl(baseUrl)
				.addConverterFactory(GsonConverterFactory.create())
				.build();

		ApiService apiService = retrofit.create(ApiService.class);
		Call<ResponseBody> call = apiService.updateUser(user);

		// Make the API call
		call.enqueue(new Callback<ResponseBody>() {
			@Override
			public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
				if (response.isSuccessful()) {
					Log.d("UpdateUserActivity", "Update Successful: " + response.body().toString());
					Toast.makeText(UpdateUserActivity.this, "Update User Details Successful!", Toast.LENGTH_SHORT).show();
					Intent intent = new Intent(UpdateUserActivity.this, UserHomeActivity.class);
					intent.putExtra("USERNAME", username);
					startActivity(intent);
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

	private List<String> getSelectedDishPreferences() {
		List<String> selectedDishPrefs = new ArrayList<>();
		for (int i = 0; i < dishOptionsList.size(); i++) {
			if (dishOptionsList.get(i).isSelected()) {
				selectedDishPrefs.add(dishPreferences.get(i));
			}
		}
		return selectedDishPrefs;
	}

	private List<String> getSelectedAllergies() {
		List<String> selectedAllergies = new ArrayList<>();
		for (String allergy : allergies) {
			int resId = getResources().getIdentifier("allergyOption" + (allergies.indexOf(allergy) + 1), "id", getPackageName());
			CheckBox checkBox = findViewById(resId);
			if (checkBox != null && checkBox.isChecked()) {
				selectedAllergies.add(allergy);
			}
		}
		return selectedAllergies;
	}
}