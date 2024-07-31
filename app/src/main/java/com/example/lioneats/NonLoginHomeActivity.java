package com.example.lioneats;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

public class NonLoginHomeActivity extends AppCompatActivity implements ImageAdapter.OnItemClickListener {
	private ViewPager2 viewPager;
	private final int[] images = {R.drawable.dish_image_1, R.drawable.dish_image_2, R.drawable.dish_image_3, R.drawable.dish_image_4, R.drawable.dish_image_5, R.drawable.dish_image_6, R.drawable.dish_image_7, R.drawable.dish_image_8, R.drawable.dish_image_9, R.drawable.dish_image_10};
	private final String[] ranks = {"best_rating", "best_value", "close_distance"};
	private final String[] titles = {"Kedai Kopi - Yishun 925", "Ang Mo Kio Fried Kway Teow", "Old Word Bakuteh & Fried Porridge"};
	private final String[] addresses = {"925 Yishun Central 1, #01-211", "724 Ang Mo Kio 6, #01-22", "Blk 101 Yishun Ave 5, #01-55"};
	private final String[] ratings = {"4.2", "4.3", "4.3"};

	private Handler handler;
	private Runnable runnable;
	private int currentItem = 0;
	private TextView loginBtn;
	private ImageButton cameraBtn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_non_login_user_home);

		loginBtn = findViewById(R.id.loginBtn);
		loginBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent (NonLoginHomeActivity.this, LoginActivity.class);
				startActivity(intent);
			}
		});

		cameraBtn = findViewById(R.id.cameraBtn);
		cameraBtn.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v){
				showPopup();
			}
		});

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
				handler.postDelayed(this, 3000);
			}
		};
		handler.postDelayed(runnable, 3000);

		ListView listView = findViewById(R.id.listView);
		if (listView != null) {
			listView.setAdapter(new MyCustomAdapter(this, ranks, titles, addresses, ratings));
		}
	}

	private void showPopup() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Login for dish classifier function")
				.setMessage("No Account yet? Register Now! ")
				.setPositiveButton("Login", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Intent intent = new Intent(NonLoginHomeActivity.this, LoginActivity.class);
						startActivity(intent);
					}
				})
				.setNegativeButton("Register Account", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Intent intent = new Intent(NonLoginHomeActivity.this, RegisterAccountActivity.class);
						startActivity(intent);
					}
				});
		builder.create().show();
	}

	@Override
	public void onItemClick(int position) {
		Intent intent = new Intent(NonLoginHomeActivity.this, DishDetailsActivity.class);
		intent.putExtra("dishID", position+1);
		startActivity(intent);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		handler.removeCallbacks(runnable);
	}
}