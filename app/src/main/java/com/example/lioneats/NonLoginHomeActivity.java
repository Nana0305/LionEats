package com.example.lioneats;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

public class NonLoginHomeActivity extends AppCompatActivity implements ImageAdapter.OnItemClickListener {
	private ViewPager2 viewPager;
	private final int[] images = {R.drawable.bkt, R.drawable.ckt, R.drawable.kayatoast};
	private final String[] ranks = {"best_rating", "best_value", "close_distance"};
	private final String[] titles = {"Kedai Kopi - Yishun 925", "Ang Mo Kio Fried Kway Teow", "Old Word Bakuteh & Fried Porridge"};
	private final String[] addresses = {"925 Yishun Central 1, #01-211", "724 Ang Mo Kio 6, #01-22", "Blk 101 Yishun Ave 5, #01-55"};
	private final String[] ratings = {"4.2", "4.3", "4.3"};

	private Handler handler;
	private Runnable runnable;
	private int currentItem = 0;
	private Button loginBtn;

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

	@Override
	public void onItemClick(int position) {
		Intent intent = new Intent(NonLoginHomeActivity.this, DishDetailsActivity.class);
		intent.putExtra("dishID", position);
		startActivity(intent);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		handler.removeCallbacks(runnable);
	}
}