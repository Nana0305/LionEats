package com.example.lioneats.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager2.widget.ViewPager2;

import com.example.lioneats.R;
import com.example.lioneats.adapters.ImageAdapter;
import com.example.lioneats.adapters.MyCustomAdapter;
import com.example.lioneats.fragments.HeaderFragment;

public class MainActivity extends AppCompatActivity implements ImageAdapter.OnItemClickListener {
	private ViewPager2 viewPager;
	private final int[] images = {R.drawable.dish_image_1, R.drawable.dish_image_2, R.drawable.dish_image_3, R.drawable.dish_image_4, R.drawable.dish_image_5, R.drawable.dish_image_6, R.drawable.dish_image_7, R.drawable.dish_image_8, R.drawable.dish_image_9, R.drawable.dish_image_10};
	private final String[] ranks = {"best_rating", "best_value", "close_distance"};
	private final String[] titles = {"Good Luck Boneless Chicken Rice", "Ji De Lai Hainanese Chicken Rice", "QMeal Fragrant Chicken Rice"};
	private final String[] addresses = {"769 Yishun Ave 3, #01-277", "105 Yishun Ring Rd, #01-152", "105 Yishun Ring Rd,#01-150"};
	private final String[] ratings = {"4.8", "3.1", "4.0"};

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

		// List view of shops
		ListView listView = findViewById(R.id.listView);
		if (listView != null) {
			listView.setAdapter(new MyCustomAdapter(this, ranks, titles, addresses, ratings));
			listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					Intent intent = new Intent(MainActivity.this, ShopDetailsActivity.class);
					intent.putExtra("shopID", position);
					startActivity(intent);
				}
			});
		}
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
