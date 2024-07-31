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
import com.example.lioneats.api.FeedApi;
import com.example.lioneats.dtos.NearByShopIdsDTO;
import com.example.lioneats.dtos.ShopDTO;
import com.example.lioneats.dtos.ShopPlaceIdDTO;
import com.example.lioneats.fragments.HeaderFragment;
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
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements ImageAdapter.OnItemClickListener {
	private ViewPager2 viewPager;
	private final int[] images = {R.drawable.dish_image_1, R.drawable.dish_image_2, R.drawable.dish_image_3, R.drawable.dish_image_4, R.drawable.dish_image_5, R.drawable.dish_image_6, R.drawable.dish_image_7, R.drawable.dish_image_8, R.drawable.dish_image_9, R.drawable.dish_image_10};

	private Handler handler;
	private Runnable runnable;
	private int currentItem = 0;
	private String selectedDish;
	private String selectedLocation;
	private String selectedBudget;
	private String selectedTiming;
	private String selectedAllergen;
	private int[] spinnerIds = {R.id.spinnerDish, R.id.spinnerLocation, R.id.spinnerBudget, R.id.spinnerTiming, R.id.spinnerAllergen};
	private int[] spinnerItemArrays = {R.array.spinnerDish_items, R.array.spinnerLocation_items, R.array.spinnerBudget_items, R.array.spinnerTiming_items, R.array.spinnerAllergen_items};

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

		// Check if user is logged in
		SharedPreferences sharedPreferences = getSharedPreferences("user_session", MODE_PRIVATE);
		String username = sharedPreferences.getString("username", null);

		// Add HeaderFragment to the activity
		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		transaction.replace(R.id.headerFragmentContainer, new HeaderFragment());
		transaction.commit();

		// Show or hide layouts based on login status
		if (username != null) {
			findViewById(R.id.userHomeLayout).setVisibility(View.VISIBLE);
			setupSpinners();
		}

		// Carousel with dish images, click to dish details
		viewPager = findViewById(R.id.viewPager);
		ImageAdapter adapter = new ImageAdapter(this, images, this);
		viewPager.setAdapter(adapter);

		handler = new Handler(Looper.getMainLooper());
		runnable = new Runnable() {
			@Override
			public void run() {
				if (currentItem == images.length) {
					currentItem = 0;
				}
				viewPager.setCurrentItem(currentItem++, true);
				handler.postDelayed(this, 2000);
			}
		};
		handler.postDelayed(runnable, 2000);

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
					fetchNearbyRestaurantIds(location.getLatitude(), location.getLongitude(), null, 0);
				}
			}
		};

		//this method is put inside the onCreate method
		// 1.to receive the permission from the user
		//2.to retrieve the last location as soon as the activity has started
		//3. to start displaying the restaurants as soon as possible
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
						fetchNearbyRestaurantIds(location.getLatitude(), location.getLongitude(), null, 0);
					} else {
						// If it is null, request for the new location
						requestNewLocationData();
					}
				}
			}).addOnFailureListener(new OnFailureListener() {
				@Override
				public void onFailure(@NonNull Exception e) {
					// Handle the failure case
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
		//create the instance of LocationRequest object
		LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
				.setMinUpdateIntervalMillis(5000) // 5 seconds
				.build();

		//if we don't get the permission, cannot go further
		if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			return;
		}

		// Request location updates from fusedLocationClient
		// locationRequest defines update criteria (priority, interval)
		// locationCallback handles the received location updates
		// Looper.getMainLooper ensures the thread on which callBack should be executed
		fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
	}

	private void askPermission()
	{
		ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
	}

	private void fetchNearbyRestaurantIds(double lat, double lng, String nextPageToken, int offsetIndex) {
		isLoading = true;
		FeedApi feedApi = RetrofitService.getInstance().create(FeedApi.class);

		Call<NearByShopIdsDTO> call = feedApi.getNearbyRestaurantsIdsHolder(lat, lng, nextPageToken, offsetIndex);

		call.enqueue(new Callback<NearByShopIdsDTO>() {
			@Override
			public void onResponse(Call<NearByShopIdsDTO> call, Response<NearByShopIdsDTO> response) {
				if (response.isSuccessful() && response.body() != null) {
					//getting the list of placeIds
					List<ShopPlaceIdDTO> placeIds = response.body().getResults();

					for(ShopPlaceIdDTO placeId : placeIds)
					{
						//fetch the detail of particular place
						fetchRestaurantDetail(placeId.getPlaceId());
					}

					//handle pagination
					String nextPageToken = response.body().getNextPageToken();
					if(nextPageToken != null){
						setupPagination(nextPageToken, lat, lng, offsetIndex + 1);
					}

				} else {
					Toast.makeText(MainActivity.this, "Failed to fetch restaurant IDs: " + response.message(), Toast.LENGTH_SHORT).show();
					Log.e(TAG, "Failed to fetch restaurant IDs: " + response.message());
				}
				isLoading = false;
			}

			@Override
			public void onFailure(Call<NearByShopIdsDTO> call, Throwable t) {
				Toast.makeText(MainActivity.this, "Failed to fetch restaurant IDs", Toast.LENGTH_SHORT).show();
				Log.e(TAG, "Error fetching restaurant IDs", t);
				isLoading = false;
			}
		});
	}

	private void setupPagination(String nextPageToken, double lat, double lng, int offsetIndex)
	{
		recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
			@Override
			public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
				super.onScrolled(recyclerView, dx, dy);
				LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();

				//checking if last complete visible item position is equal to the restaurant List length
				//means the user has already scrolled down to the provided list

				if(!isLoading && layoutManager != null && layoutManager.findLastCompletelyVisibleItemPosition() == restaurantList.size()-1)
				{
					fetchNearbyRestaurantIds(lat, lng, nextPageToken, offsetIndex);
				}
			}
		});
	}

	private void fetchRestaurantDetail(String placeId) {
		FeedApi feedApi = RetrofitService.getInstance().create(FeedApi.class);

		Call<ShopDTO> call = feedApi.getRestaurantDetail(placeId);

		call.enqueue(new Callback<ShopDTO>() {
			@Override
			public void onResponse(Call<ShopDTO> call, Response<ShopDTO> response) {
				if (response.isSuccessful() && response.body() != null) {
					restaurantList.add(response.body());
					//notify the adapter that a new item has been inserted into the data set at a specified position
					restaurantAdapter.notifyItemInserted(restaurantList.size() - 1);
				}
			}

			@Override
			public void onFailure(Call<ShopDTO> call, Throwable t) {
				Toast.makeText(MainActivity.this, "Failed to fetch restaurant details", Toast.LENGTH_SHORT).show();
			}
		});
	}

	@Override
	protected void onPause()
	{
		super.onPause();
		fusedLocationClient.removeLocationUpdates(locationCallback);
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

	@Override
	public void onItemClick(int position) {
		Intent intent = new Intent(MainActivity.this, DishDetailsActivity.class);
		intent.putExtra("dishID", position + 1);
		startActivity(intent);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (handler != null) {
			handler.removeCallbacks(runnable);
		}
	}
}
