package com.example.lioneats.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;
import com.example.lioneats.R;
import com.example.lioneats.adapters.ImageAdapter;
import com.example.lioneats.adapters.MultiSelectAdapter;
import com.example.lioneats.adapters.RestaurantAdapter;
import com.example.lioneats.api.ApiService;
import com.example.lioneats.dtos.CompositeDTO;
import com.example.lioneats.dtos.LocationDTO;
import com.example.lioneats.dtos.MRTDTO;
import com.example.lioneats.dtos.SearchRequestDTO;
import com.example.lioneats.dtos.ShopDTO;
import com.example.lioneats.dtos.UserLocationDTO;
import com.example.lioneats.fragments.HeaderFragment;
import com.example.lioneats.models.Allergy;
import com.example.lioneats.models.Dish;
import com.example.lioneats.models.UserDTO;
import com.example.lioneats.utils.RetrofitClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.OnFailureListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import retrofit2.Call;
import android.Manifest;
import androidx.annotation.NonNull;
import com.google.android.gms.location.LocationRequest;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements ImageAdapter.OnItemClickListener {

	private Handler handler;
	private Runnable runnable;
	private int currentItem = 0;

	private static final int REQUEST_CODE = 100;
	private static final String TAG = "MainActivity";
	private ViewPager2 viewPager;
	private List<Dish> dishList = new ArrayList<>();
	private List<Allergy> allergyList = new ArrayList<>();
	private List<MRTDTO> mrtList = new ArrayList<>();
	private List<ShopDTO> restaurantList = new ArrayList<>();
	private RestaurantAdapter restaurantAdapter;
	private FusedLocationProviderClient fusedLocationClient;
	private LocationCallback locationCallback;
	private SharedPreferences userSessionPreferences, userPreferences, dishListPreferences, allergyListPreferences, mrtListPreferences;
	private ProgressBar progressBar;

	List<String> dishNames = new ArrayList<>();
	List<String> allergyNames = new ArrayList<>();
	List<String> mrtNames = new ArrayList<>();
	private List<String> selectedDish;
	private List<String> selectedLocation;
	private List<String> selectedAllergies;
	private String selectedBudget;
	private double selectedRating;
	private UserLocationDTO currentLocation;
	private UserDTO user;

	private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		userSessionPreferences = getSharedPreferences("user_session", MODE_PRIVATE);
		userPreferences = getSharedPreferences("user", MODE_PRIVATE);
		dishListPreferences = getSharedPreferences("dish_list", MODE_PRIVATE);
		allergyListPreferences = getSharedPreferences("allergy_list", MODE_PRIVATE);
		mrtListPreferences = getSharedPreferences("mrt_list", MODE_PRIVATE);

		setupUI();
		setupLocationServices();
		loadListsFromPreferencesOrFetch();

		String username = userSessionPreferences.getString("username", null);
		Log.d(TAG, "Username from preferences: " + username);
		LinearLayout userHomeLayout = findViewById(R.id.userHomeLayout);

		if (username != null) {
			userHomeLayout.setVisibility(View.VISIBLE);
			setupSpinners();
			user = new UserDTO();
			String userJson = userPreferences.getString("user", null);
			if (userJson != null) {
				user = new Gson().fromJson(userJson, UserDTO.class);
			}
			//searchAndDisplayShops();
		} else {
			getLastLocation();
		}
	}

	private void loadListsFromPreferencesOrFetch() {
		loadDishesFromPreferences();
		loadAllergiesFromPreferences();
		loadMRTsFromPreferences();

		dishNames.add("");
		for (Dish dish : dishList) {
			dishNames.add(dish.getDishDetailName());
		}
		allergyNames.add(""); // Add an empty item to represent no selection
		for (Allergy allergy : allergyList) {
			allergyNames.add(allergy.getName());
		}
		mrtNames.add("");
		for (MRTDTO mrt : mrtList) {
			mrtNames.add(mrt.getName());
		}
	}

	private void loadDishesFromPreferences() {
		String dishesJson = dishListPreferences.getString("dishes", null);
		if (dishesJson != null) {
			dishList = new Gson().fromJson(dishesJson, new TypeToken<List<Dish>>() {
			}.getType());
			setupViewPager();
		} else {
			fetchAndUpdateDishes();
		}
	}

	private void loadAllergiesFromPreferences() {
		String allergiesJson = allergyListPreferences.getString("allergies", null);
		if (allergiesJson != null) {
			allergyList = new Gson().fromJson(allergiesJson, new TypeToken<List<Allergy>>() {
			}.getType());
		} else {
			fetchAndUpdateAllergies();
		}
	}

	private void loadMRTsFromPreferences() {
		String mrtJson = mrtListPreferences.getString("MRTs", null);
		if (mrtJson != null) {
			mrtList = new Gson().fromJson(mrtJson, new TypeToken<List<MRTDTO>>() {
			}.getType());
		} else {
			fetchAndUpdateMRTs();
		}
	}

	private void fetchAndUpdateDishes() {
		ApiService apiService = RetrofitClient.getApiService();

		Log.d(TAG, "API Request: Fetching Dishes");

		Call<List<Dish>> call = apiService.getAllDishes();

		call.enqueue(new Callback<List<Dish>>() {
			@Override
			public void onResponse(Call<List<Dish>> call, Response<List<Dish>> response) {
				if (response.isSuccessful() && response.body() != null) {
					dishList = response.body();
					SharedPreferences.Editor dishEditor = dishListPreferences.edit();
					dishEditor.putString("dishes", gson.toJson(dishList));
					dishEditor.apply();
					setupViewPager();
				} else {
					Toast.makeText(MainActivity.this, "Failed to fetch dish list", Toast.LENGTH_SHORT).show();
				}
			}

			@Override
			public void onFailure(Call<List<Dish>> call, Throwable t) {
				Toast.makeText(MainActivity.this, "Network Error", Toast.LENGTH_SHORT).show();
				Log.e(TAG, "Network Error: ", t);
			}
		});
	}

	private void fetchAndUpdateAllergies() {
		ApiService apiService = RetrofitClient.getApiService();

		Log.d(TAG, "API Request: Fetching Allergies");

		Call<List<Allergy>> call = apiService.getAllergies();

		call.enqueue(new Callback<List<Allergy>>() {
			@Override
			public void onResponse(Call<List<Allergy>> call, Response<List<Allergy>> response) {
				if (response.isSuccessful() && response.body() != null) {
					allergyList = response.body();
					SharedPreferences.Editor allergyEditor = allergyListPreferences.edit();
					allergyEditor.putString("allergies", gson.toJson(allergyList));
					allergyEditor.apply();
				} else {
					Toast.makeText(MainActivity.this, "Failed to fetch allergy list", Toast.LENGTH_SHORT).show();
				}
			}

			@Override
			public void onFailure(Call<List<Allergy>> call, Throwable t) {
				Toast.makeText(MainActivity.this, "Network Error", Toast.LENGTH_SHORT).show();
				Log.e(TAG, "Network Error: ", t);
			}
		});
	}

	private void fetchAndUpdateMRTs() {
		ApiService apiService = RetrofitClient.getApiService();

		Log.d(TAG, "API Request: Fetching MRTs");

		Call<List<MRTDTO>> call = apiService.getMRTList();

		call.enqueue(new Callback<List<MRTDTO>>() {
			@Override
			public void onResponse(Call<List<MRTDTO>> call, Response<List<MRTDTO>> response) {
				if (response.isSuccessful() && response.body() != null) {
					mrtList = response.body();
					SharedPreferences.Editor mrtEditor = mrtListPreferences.edit();
					mrtEditor.putString("MRTs", gson.toJson(mrtList));
					mrtEditor.apply();
				} else {
					Toast.makeText(MainActivity.this, "Failed to fetch MRT list", Toast.LENGTH_SHORT).show();
				}
			}

			@Override
			public void onFailure(Call<List<MRTDTO>> call, Throwable t) {
				Toast.makeText(MainActivity.this, "Network Error", Toast.LENGTH_SHORT).show();
				Log.e(TAG, "Network Error: ", t);
			}
		});
	}

	private void setupUI() {
		progressBar = findViewById(R.id.progressBar);
		progressBar.setVisibility(View.VISIBLE);

		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		transaction.replace(R.id.headerFragmentContainer, new HeaderFragment());
		transaction.commit();

		viewPager = findViewById(R.id.viewPager);

		RecyclerView recyclerView = findViewById(R.id.recyclerView);
		recyclerView.setLayoutManager(new LinearLayoutManager(this));
		restaurantAdapter = new RestaurantAdapter(restaurantList);
		recyclerView.setAdapter(restaurantAdapter);
	}

	private void setupLocationServices() {
		fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
		locationCallback = new LocationCallback() {
			@Override
			public void onLocationResult(LocationResult locationResult) {
				if (locationResult == null) {
					Log.d(TAG, "Location result is null");
					fetchShopsByDefaultLocation();
					return;
				}
				Location location = locationResult.getLastLocation();
				if (location != null) {
					currentLocation = new UserLocationDTO(location.getLatitude(), location.getLongitude());
					fetchShopsByLocation(currentLocation);
				} else {
					fetchShopsByDefaultLocation();
				}
			}
		};
	}

	private void getLastLocation() {
		if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
			fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
				if (location != null) {
					currentLocation = new UserLocationDTO(location.getLatitude(), location.getLongitude());
					fetchShopsByLocation(currentLocation);
				} else {
					requestNewLocationData();
				}
			}).addOnFailureListener(e -> {
				e.printStackTrace();
				Log.e(TAG, "Error trying to get location", e);
				Toast.makeText(MainActivity.this, "Error trying to get location", Toast.LENGTH_SHORT).show();
				fetchShopsByDefaultLocation();
			});
		} else {
			askPermission();
		}
	}

	private void requestNewLocationData() {
		LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
				.setMinUpdateIntervalMillis(5000)
				.build();

		if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			return;
		}
		fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
	}

	private void askPermission() {
		if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
			Toast.makeText(this, "Location permission is needed to show nearby restaurants.", Toast.LENGTH_LONG).show();
		}
		ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (requestCode == REQUEST_CODE) {
			if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				getLastLocation();
			} else {
				Toast.makeText(this, "Permission denied. Using default location.", Toast.LENGTH_SHORT).show();
				fetchShopsByDefaultLocation();
			}
		}
	}

	private void fetchShopsByLocation(UserLocationDTO location) {
		Log.d(TAG, "API Request: Fetching Shops by Location: " + gson.toJson(location));

		ApiService apiService = RetrofitClient.getApiService();
		Call<List<ShopDTO>> call = apiService.getShopsByLocaiton(location);

		call.enqueue(new Callback<List<ShopDTO>>() {
			@Override
			public void onResponse(Call<List<ShopDTO>> call, Response<List<ShopDTO>> response) {
				if (response.isSuccessful() && response.body() != null) {
					List<ShopDTO> shops = response.body();
					updateRestaurantList(shops);
				} else {
					handleFetchError("Failed to fetch restaurants: " + response.message());
				}
			}

			@Override
			public void onFailure(Call<List<ShopDTO>> call, Throwable t) {
				handleFetchError("Network error while fetching restaurants", t);
			}
		});
	}

	private void fetchShopsByDefaultLocation() {
		Log.d(TAG, "API Request: Fetching Shops by Default Location");

		ApiService apiService = RetrofitClient.getApiService();
		Call<List<ShopDTO>> call = apiService.getShopsDefault();

		call.enqueue(new Callback<List<ShopDTO>>() {
			@Override
			public void onResponse(Call<List<ShopDTO>> call, Response<List<ShopDTO>> response) {
				if (response.isSuccessful() && response.body() != null) {
					List<ShopDTO> shops = response.body();
					updateRestaurantList(shops);
				} else {
					handleFetchError("No restaurants available for default location: " + response.message());
				}
			}

			@Override
			public void onFailure(Call<List<ShopDTO>> call, Throwable t) {
				handleFetchError("Network error while fetching default location restaurants", t);
			}
		});
	}

	private void updateRestaurantList(List<ShopDTO> shops) {
		restaurantList.clear();
		if (shops.size() > 50) {
			restaurantList.addAll(shops.subList(0, 50));
		} else {
			restaurantList.addAll(shops);
		}
		restaurantAdapter.notifyDataSetChanged();
		progressBar.setVisibility(View.GONE);
	}

	private void handleFetchError(String message) {
		progressBar.setVisibility(View.GONE);
		Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
		Log.e(TAG, message);
	}

	private void handleFetchError(String message, Throwable t) {
		progressBar.setVisibility(View.GONE);
		Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
		Log.e(TAG, message, t);
	}

	@Override
	protected void onPause() {
		super.onPause();
		fusedLocationClient.removeLocationUpdates(locationCallback);
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
			getLastLocation();
		} else {
			fetchShopsByDefaultLocation();
		}
	}

	private void setupViewPager() {
		ImageAdapter adapter = new ImageAdapter(this, dishList, this);
		viewPager.setAdapter(adapter);

		handler = new Handler(Looper.getMainLooper());
		runnable = new Runnable() {
			@Override
			public void run() {
				if (currentItem == dishList.size()) {
					currentItem = 0;
				}
				viewPager.setCurrentItem(currentItem++, true);
				handler.postDelayed(this, 2000);
			}
		};
		handler.postDelayed(runnable, 2000);
	}

	@Override
	public void onItemClick(int position) {
		Dish selectedDish = dishList.get(position);
		Intent intent = new Intent(MainActivity.this, DishDetailsActivity.class);
		intent.putExtra("dishID", selectedDish.getId());
		intent.putExtra("dishImageUrl", selectedDish.getImageUrl());
		startActivity(intent);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (handler != null) {
			handler.removeCallbacks(runnable);
		}
	}

	private void setupSpinners() {
		setupBudgetSpinner();
		setupRatingSpinner();
		setupDishSpinner();
		setupAllergySpinner();
		setupLocationSpinner();
		Button refreshBtn = findViewById(R.id.refreshBtn);
		refreshBtn.setOnClickListener(v -> filterAndDisplayShops());
	}

	private void setupBudgetSpinner() {
		Spinner budgetSpinner = findViewById(R.id.spinnerBudget);
		List<String> budgetOptions = new ArrayList<>();
		budgetOptions.add(""); // Add an empty item to represent no selection
		budgetOptions.addAll(Arrays.asList(getResources().getStringArray(R.array.spinnerBudget_items)));
		ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
				android.R.layout.simple_spinner_item, budgetOptions);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		budgetSpinner.setAdapter(adapter);
		budgetSpinner.setSelection(0, true); // Start with the empty item selected
		budgetSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				if (position != 0) {
					selectedBudget = parent.getItemAtPosition(position).toString();
				} else {
					selectedBudget = null; // Ensure budget is null if no selection is made
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				selectedBudget = null;
			}
		});
	}

	private void setupRatingSpinner() {
		Spinner ratingSpinner = findViewById(R.id.spinnerRating);
		List<String> ratingOptions = new ArrayList<>();
		ratingOptions.add(""); // Add an empty item to represent no selection
		ratingOptions.addAll(Arrays.asList(getResources().getStringArray(R.array.spinnerRating_items)));
		ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
				android.R.layout.simple_spinner_item, ratingOptions);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		ratingSpinner.setAdapter(adapter);
		ratingSpinner.setSelection(0, true); // Start with the empty item selected
		ratingSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				if (position != 0) {
					selectedRating = Double.parseDouble(parent.getItemAtPosition(position).toString());
				} else {
					selectedRating = 0.0; // Reset rating to 0 if no rating is selected
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				selectedRating = 0.0;
			}
		});
	}

	private void setupDishSpinner() {
		Spinner dishSpinner = findViewById(R.id.spinnerDish);
		selectedDish = new ArrayList<>();
		// This is the correct way to handle item selection in a Spinner
		dishSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				String selectedDishName = parent.getItemAtPosition(position).toString();
				if (position != 0) { // Avoid the first (empty) selection
					selectedDish.add(selectedDishName);
				} else {
					selectedDish.clear();
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				selectedDish.clear();
			}
		});
	}

	private void setupAllergySpinner() {
		Spinner allergySpinner = findViewById(R.id.spinnerAllergy);
		selectedAllergies = new ArrayList<>();
		// Set the correct listener for item selection in a Spinner
		allergySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				String selectedAllergy = parent.getItemAtPosition(position).toString();
				if (position != 0) { // Avoid the first (empty) selection
					selectedAllergies.add(selectedAllergy);
				} else {
					selectedAllergies.clear();
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				selectedAllergies.clear();
			}
		});
	}
	
	private void showMultiSelectDialog(String title, List<String> items, List<String> selectedItems) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(title);

		View view = getLayoutInflater().inflate(R.layout.multi_select_dialog, null);
		ListView listView = view.findViewById(R.id.listViewMultiSelect);
		MultiSelectAdapter adapter = new MultiSelectAdapter(this, items, selectedItems);
		listView.setAdapter(adapter);

		builder.setView(view);
		builder.setPositiveButton("OK", (dialog, which) -> {
			// Handle "OK" button
		});
		builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

		builder.create().show();
	}

	private void setupLocationSpinner() {
		selectedLocation = new ArrayList<>();
		Spinner locationSpinner = findViewById(R.id.spinnerLocation);
		List<String> mrtNames = new ArrayList<>();
		mrtNames.add(""); // Add an empty item to represent no selection
		for (MRTDTO mrt : mrtList) {
			mrtNames.add(mrt.getName());
		}
		ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
				android.R.layout.simple_spinner_item, mrtNames);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		locationSpinner.setAdapter(adapter);
		locationSpinner.setSelection(0, true); // Start with the empty item selected
		locationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				if (position != 0) {
					selectedLocation.add(parent.getItemAtPosition(position).toString());
				} else {
					selectedLocation.clear(); // Clear selection if no location is selected
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				selectedLocation.clear();
			}
		});
	}

	private void searchAndDisplayShops() {
		String userJson = userPreferences.getString("user", null);
		if (userJson != null) {
			user = new Gson().fromJson(userJson, UserDTO.class);
		}
		if (currentLocation == null) {
			currentLocation = new UserLocationDTO(1.290, 103.84);
		}
		CompositeDTO compositeDTO = new CompositeDTO(user, currentLocation);
		Log.d(TAG, "API Request: Searching Shops: " + gson.toJson(compositeDTO));

		ApiService apiService = RetrofitClient.getApiService();
		Call<List<ShopDTO>> call = apiService.searchShops(compositeDTO);
		call.enqueue(new Callback<List<ShopDTO>>() {
			@Override
			public void onResponse(Call<List<ShopDTO>> call, Response<List<ShopDTO>> response) {
				if (response.isSuccessful() && response.body() != null) {
					List<ShopDTO> searchedShops = response.body();
					updateRestaurantList(searchedShops);
				} else {
					handleFetchError("Failed to search restaurants: " + response.message());
				}
			}

			@Override
			public void onFailure(Call<List<ShopDTO>> call, Throwable t) {
				handleFetchError("Network error while searching restaurants", t);
			}
		});
	}

	private void filterAndDisplayShops() {
		SearchRequestDTO searchRequest = setupSearchRequest();
		Log.d(TAG, "API Request: Filtering Shops: " + gson.toJson(searchRequest));

		ApiService apiService = RetrofitClient.getApiService();
		Call<List<ShopDTO>> call = apiService.filterShops(searchRequest);
		call.enqueue(new Callback<List<ShopDTO>>() {
			@Override
			public void onResponse(Call<List<ShopDTO>> call, Response<List<ShopDTO>> response) {
				if (response.isSuccessful() && response.body() != null) {
					List<ShopDTO> filteredShops = response.body();
					updateRestaurantList(filteredShops);
				} else {
					handleFetchError("Failed to filter restaurants: " + response.message());
				}
			}

			@Override
			public void onFailure(Call<List<ShopDTO>> call, Throwable t) {
				handleFetchError("Network error while filtering restaurants", t);
			}
		});
	}

	private SearchRequestDTO setupSearchRequest() {
		SearchRequestDTO searchRequest = new SearchRequestDTO();

		if (selectedDish != null) {
			searchRequest.setDishes(selectedDish);
		} else {
			//searchRequest.setDishes(List<String>);
		}

		if (selectedBudget != null) {
			searchRequest.setBudget(selectedBudget);
		} else {
			searchRequest.setBudget("");
		}

		if (selectedAllergies != null) {
			searchRequest.setAllergies(selectedAllergies);
		} else {
			//searchRequest.setAllergies(user.getAllergies());
		}

		if (selectedLocation != null) {
			searchRequest.setLocation(selectedLocation);
		} else {

		}

		searchRequest.setMinRating(selectedRating);

		return searchRequest;
	}
}