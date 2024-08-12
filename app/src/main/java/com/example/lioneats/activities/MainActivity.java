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
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;
import com.example.lioneats.R;
import com.example.lioneats.adapters.ImageAdapter;
import com.example.lioneats.adapters.RestaurantAdapter;
import com.example.lioneats.api.ApiService;
import com.example.lioneats.api.FeedApi;
import com.example.lioneats.dtos.NearByShopIdsDTO;
import com.example.lioneats.dtos.ShopDTO;
import com.example.lioneats.dtos.ShopPlaceIdDTO;
import com.example.lioneats.fragments.HeaderFragment;
import com.example.lioneats.models.Allergy;
import com.example.lioneats.models.Dish;
import com.example.lioneats.utils.RetrofitClient;
import com.example.lioneats.utils.RetrofitService;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import android.Manifest;
import androidx.annotation.NonNull;
import com.google.android.gms.location.LocationRequest;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity implements ImageAdapter.OnItemClickListener {
	private ViewPager2 viewPager;
	private Handler handler;
	private Runnable runnable;
	private int currentItem = 0;
	private List<Dish> dishList = new ArrayList<>();
	private List<Allergy> allergyList = new ArrayList<>();
	private SharedPreferences userSessionPreferences;
	private SharedPreferences dishListPreferences;
	private SharedPreferences allergyListPreferences;
	private int[] spinnerIds = {R.id.spinnerDish, R.id.spinnerLocation, R.id.spinnerBudget, R.id.spinnerTiming, R.id.spinnerAllergen};
	private int[] spinnerItemArrays = {R.array.spinnerDish_items, R.array.spinnerLocation_items, R.array.spinnerBudget_items, R.array.spinnerTiming_items, R.array.spinnerAllergen_items};

	private String selectedDish;
	private String selectedLocation;
	private String selectedBudget;
	private String selectedTiming;
	private String selectedAllergen;

	//for communicating permission request and response
	private static final int REQUEST_CODE = 100;
	//for connecting with google location service
	private FusedLocationProviderClient fusedLocationClient;
	private LocationCallback locationCallback;
	private RecyclerView recyclerView;
	private RestaurantAdapter restaurantAdapter;
	private List<ShopDTO> restaurantList;
	//isLoading is used to prevent multiple requests
	//When the user scroll down and data is being fetched to satisfy that functionality,
	// in that case, we need to prevent for another request
	private boolean isLoading = false;
	private static final String TAG = "FeedActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		userSessionPreferences = getSharedPreferences("user_session", MODE_PRIVATE);
		dishListPreferences = getSharedPreferences("dish_list", MODE_PRIVATE);
		allergyListPreferences = getSharedPreferences("allergy_list", MODE_PRIVATE);

		String username = userSessionPreferences.getString("username", null);
		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		transaction.replace(R.id.headerFragmentContainer, new HeaderFragment());
		transaction.commit();

		if (username != null) {
			findViewById(R.id.userHomeLayout).setVisibility(View.VISIBLE);
			setupSpinners();
		}

		viewPager = findViewById(R.id.viewPager);
		loadDishesFromPreferences();
		fetchAndUpdateDishes();
		fetchAndUpdateAllergies();

		// Recycler view of shops
		recyclerView = findViewById(R.id.recyclerView);
		recyclerView.setLayoutManager(new LinearLayoutManager(this));
		restaurantList = new ArrayList<>();
		restaurantAdapter = new RestaurantAdapter(restaurantList);
		recyclerView.setAdapter(restaurantAdapter);

		//two concepts to use GooglePlay API services - fusedLocationClient and LocationCallback
		//they need to be working together
		//LocationCallback is to triggers when location updates are available or when the availability of location data changes.
		//fusedLocationClient is for removeLocationUpdates, updateLocationUpdates, receives lastKnownLocation, request
		//define LocationCallback to handle the changes received from fusedLocationClient
		//create the instance of fusedLocationClient
		fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

		//set up the callback method
		locationCallback = new LocationCallback() {
			@Override
			public void onLocationResult(LocationResult locationResult) {
				if (locationResult == null) {
					Log.d(TAG, "Location result is null");
					return;
				}
				Location location = locationResult.getLastLocation();
				if (location != null) {
					fetchNearbyRestaurants(location.getLatitude(), location.getLongitude(), null, 0);
				}
			}
		};

		getLastLocation();
	}

	private void getLastLocation() {
		// If user has already granted the permission
		if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
			// Retrieve the last location of the user and if that task is successful
			fusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
				@Override
				public void onSuccess(Location location) {
					// If the location is not null, fetch the restaurant
					if (location != null) {
						fetchNearbyRestaurants(location.getLatitude(), location.getLongitude(), null, 0);
					} else {
						// If it is null, request for the new location
						requestNewLocationData();
					}
				}
			}).addOnFailureListener(new OnFailureListener() {
				@Override
				public void onFailure(@NonNull Exception e) {
					e.printStackTrace();
					Log.e(TAG, "Error trying to get location", e);
					Toast.makeText(MainActivity.this, "Error trying to get location", Toast.LENGTH_SHORT).show();
				}
			});
		} else {
			askPermission();
		}
	}

	private void requestNewLocationData()
	{
		LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
				.setMinUpdateIntervalMillis(5000) // 5 seconds
				.build();


		if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			return;
		}

		fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
	}

	private void askPermission()
	{
		ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
	}

	private void fetchNearbyRestaurants(double lat, double lng, String nextPageToken, int offsetIndex) {
		isLoading = true;
		FeedApi feedApi = RetrofitService.getInstance().create(FeedApi.class);

		Call<List<ShopDTO>> call = feedApi.getNearbyRestaurants(lat, lng, nextPageToken, offsetIndex);

		call.enqueue(new Callback<List<ShopDTO>>() {
			@Override
			public void onResponse(Call<List<ShopDTO>> call, Response<List<ShopDTO>> response) {
				if (response.isSuccessful() && response.body() != null) {
					List<ShopDTO> shops = response.body();

					for (ShopDTO shop : shops) {
						restaurantList.add(shop);
						restaurantAdapter.notifyItemInserted(restaurantList.size() - 1);
					}

					if (nextPageToken != null) {
						setupPagination(nextPageToken, lat, lng, offsetIndex + 1);
					}

				} else {
					Toast.makeText(MainActivity.this, "Failed to fetch restaurants: " + response.message(), Toast.LENGTH_SHORT).show();
					Log.e(TAG, "Failed to fetch restaurants: " + response.message());
				}
				isLoading = false;
			}

			@Override
			public void onFailure(Call<List<ShopDTO>> call, Throwable t) {
				Toast.makeText(MainActivity.this, "Failed to fetch restaurants", Toast.LENGTH_SHORT).show();
				Log.e(TAG, "Error fetching restaurants", t);
				isLoading = false;
			}
		});
	}

	private void setupPagination(String nextPageToken, double lat, double lng, int offsetIndex) {
		recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
			@Override
			public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
				super.onScrolled(recyclerView, dx, dy);
				LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();

				if (!isLoading && layoutManager != null && layoutManager.findLastCompletelyVisibleItemPosition() == restaurantList.size() - 1) {
					fetchNearbyRestaurants(lat, lng, nextPageToken, offsetIndex);
				}
			}
		});
	}

	@Override
	protected void onPause() {
		super.onPause();
		fusedLocationClient.removeLocationUpdates(locationCallback);
	}

	private void loadDishesFromPreferences() {
		String dishesJson = dishListPreferences.getString("dishes", null);
		if (dishesJson != null) {
			dishList = new Gson().fromJson(dishesJson, new TypeToken<List<Dish>>() {}.getType());
			setupViewPager();
		} else {
			fetchAndUpdateDishes();
		}
	}
	private void fetchAndUpdateDishes() {
		ApiService apiService = RetrofitClient.getApiService();
		Call<List<Dish>> call = apiService.getAllDishes();

		call.enqueue(new Callback<List<Dish>>() {
			@Override
			public void onResponse(Call<List<Dish>> call, Response<List<Dish>> response) {
				if (response.isSuccessful() && response.body() != null) {
					List<Dish> updatedDishList = response.body();
					logDishList(updatedDishList);
					if (!updatedDishList.equals(dishList)) {
						dishList = updatedDishList;
						SharedPreferences.Editor dishEditor = dishListPreferences.edit();
						dishEditor.putString("dishes", new Gson().toJson(dishList));
						dishEditor.apply();
						setupViewPager();
					}
				} else {
					Toast.makeText(MainActivity.this, "Failed to fetch dish list", Toast.LENGTH_SHORT).show();
				}
			}

			@Override
			public void onFailure(Call<List<Dish>> call, Throwable t) {
				Toast.makeText(MainActivity.this, "Network Error", Toast.LENGTH_SHORT).show();
				Log.e("MainActivity", "Network Error: ", t);
			}
		});
	}
	private void logDishList(List<Dish> dishList) {
		if (dishList != null && !dishList.isEmpty()) {
			for (Dish dish : dishList) {
				Log.d("DishList", dish.toString());
			}
		} else {
			Log.d("DishList", "Dish list is empty or null.");
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
	public void onItemClick (int position) {
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

	private void fetchAndUpdateAllergies() {
		ApiService apiService = RetrofitClient.getApiService();
		Call<List<Allergy>> call = apiService.getAllergies();

		call.enqueue(new Callback<List<Allergy>>() {
			@Override
			public void onResponse(Call<List<Allergy>> call, Response<List<Allergy>> response) {
				if (response.isSuccessful() && response.body() != null) {
					List<Allergy> updatedAllergyList = response.body();
					logAllergies(updatedAllergyList);
					if (!updatedAllergyList.equals(allergyList)) {
						allergyList = updatedAllergyList;
						SharedPreferences.Editor allergyEditor = allergyListPreferences.edit();
						allergyEditor.putString("allergies", new Gson().toJson(allergyList));
						allergyEditor.apply();
					}
				} else {
					Toast.makeText(MainActivity.this, "Failed to fetch allergy list", Toast.LENGTH_SHORT).show();
				}
			}

			@Override
			public void onFailure(Call<List<Allergy>> call, Throwable t) {
				Toast.makeText(MainActivity.this, "Network Error", Toast.LENGTH_SHORT).show();
				Log.e("MainActivity", "Network Error: ", t);
			}
		});
	}

	private void setupSpinners() {
		for (int i = 0; i < spinnerIds.length; i++) {
			initialiseSpinner(spinnerIds[i], spinnerItemArrays[i], i);
		}
	}

	private void initialiseSpinner(int spinnerId, int arrayResourceId, final int index) {
		Spinner spinner = findViewById(spinnerId);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, arrayResourceId, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				String selectedValue = parent.getItemAtPosition(position).toString();
				switch (index) {
					case 0:
						selectedBudget = selectedValue;
						break;
					case 1:
						selectedLocation = selectedValue;
						break;
					case 2:
						selectedTiming = selectedValue;
						break;
					case 3:
						selectedDish = selectedValue;
						break;
					case 4:
						selectedAllergen = selectedValue;
						break;
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// Do nothing
			}
		});
	}

	private void logAllergies(List<Allergy> allergies) {
		for (Allergy allergy : allergies) {
			Log.d("RegisterAccountActivity", "Allergy: " + allergy.getName());
		}
	}
}

