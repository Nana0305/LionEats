package com.example.lioneats.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.lioneats.R;
import com.example.lioneats.api.RetrofitClient;
import com.example.lioneats.databinding.ActivityImageResultBinding;
import com.example.lioneats.fragments.HeaderFragment;
import com.example.lioneats.dtos.DishDTO;
import com.example.lioneats.utils.ActivityUtils;
import com.example.lioneats.viewmodels.ImageResultViewModel;
import com.example.lioneats.viewmodels.ImageResultViewModelFactory;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ImageResultActivity extends AppCompatActivity {
	private static final String TAG = "ImageResultActivity";
	private SharedPreferences userSessionPreferences;
	private String jwtToken;
	private ImageResultViewModel viewModel;
	private Uri imageUri;
	ActivityImageResultBinding binding;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		binding = DataBindingUtil.setContentView(this, R.layout.activity_image_result);

		userSessionPreferences = getSharedPreferences("user_session", MODE_PRIVATE);
		jwtToken = userSessionPreferences.getString("jwt","");

		ImageResultViewModelFactory factory = new ImageResultViewModelFactory(RetrofitClient.getApiService(jwtToken));
		viewModel = new ViewModelProvider(this, factory).get(ImageResultViewModel.class);
		binding.setViewModel(viewModel);
		binding.setLifecycleOwner(this);

		setupHeaderFragment();
		setupSpinner(getDishNames());

		String imageUriString = getIntent().getStringExtra("imageUri");
		if (imageUriString != null) {
			imageUri = Uri.parse(imageUriString);
			Glide.with(this)
					.load(imageUri)
					.placeholder(R.drawable.default_image)
					.error(R.drawable.default_image)
					.into(binding.imageView);

			viewModel.uploadImage(imageUri, getContentResolver());
		}

		observeViewModel();
	}

	private void setupHeaderFragment() {
		getSupportFragmentManager().beginTransaction()
				.replace(R.id.headerFragmentContainer, new HeaderFragment())
				.commit();
	}

	private List<String> getDishNames() {
		SharedPreferences dishListPreferences = getSharedPreferences("dish_list", MODE_PRIVATE);
		String jsonDishes = dishListPreferences.getString("dishes", "");

		List<DishDTO> dishList = new Gson().fromJson(jsonDishes, new TypeToken<List<DishDTO>>() {
		}.getType());

		List<String> dishNames = new ArrayList<>();
		dishNames.add("Select the correct dish");
		for (DishDTO dish : dishList) {
			dishNames.add(dish.getDishDetailName());
		}
		viewModel.setDishList(dishList);
		return dishNames;
	}

	private void setupSpinner(List<String> dishNames) {
		ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, dishNames);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		binding.spinnerDishName.setAdapter(adapter);

		binding.spinnerDishName.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
				String selectedDishName = position == 0 ? null : parentView.getItemAtPosition(position).toString();
				viewModel.setSelectedDishName(selectedDishName);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parentView) {
				viewModel.setSelectedDishName(null);
			}
		});
	}

	private void observeViewModel() {
		viewModel.getIsLoading().observe(this, isLoading -> {
			if (isLoading) {
				binding.progressBar.setVisibility(View.VISIBLE);
				binding.resultText.setVisibility(View.GONE);
			} else {
				binding.progressBar.setVisibility(View.GONE);
				String predictedDish = viewModel.getResultText().getValue();
				if (predictedDish != null && !predictedDish.isEmpty()) {
					binding.resultText.setVisibility(View.VISIBLE);
					binding.resultText.setText(predictedDish);

					binding.viewDishBtn.setClickable(true);
				} else {
					binding.resultText.setVisibility(View.GONE);
					binding.viewDishBtn.setClickable(false);
				}
			}
		});

		viewModel.getNavigateToDishDetails().observe(this, dishID -> {
			if (dishID != null) {
				Intent intent = new Intent(this, DishDetailsActivity.class);
				intent.putExtra("dishID", dishID);
				intent.putExtra("dishImageUrl", viewModel.getDishImageUrl(dishID));
				startActivity(intent);
			}
		});

		viewModel.getIsFeedbackSubmitted().observe(this, isSubmitted -> {
			if (isSubmitted) {
				submitSuccessDialog();
			}
		});

		viewModel.getErrorMessage().observe(this, errorMessage -> {
			if (errorMessage != null && !errorMessage.isEmpty()) {
				Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
			}
		});

		binding.viewDishBtn.setOnClickListener(v -> viewModel.onViewDishClicked());
		binding.submitBtn.setOnClickListener(v -> viewModel.onSubmit());
	}

	private void navigateToDishDetails() {
		Integer dishID = viewModel.getNavigateToDishDetails().getValue();
		if (dishID != null) {
			Intent intent = new Intent(this, DishDetailsActivity.class);
			intent.putExtra("dishID", dishID);
			intent.putExtra("dishImageUrl", viewModel.getDishImageUrl(dishID));
			startActivity(intent);
		} else {
			Toast.makeText(this, "Dish ID is not available", Toast.LENGTH_SHORT).show();
		}
	}

	private void submitSuccessDialog() {
		if (!isFinishing() && !isDestroyed()) {
			LayoutInflater inflater = getLayoutInflater();
			View dialogView = inflater.inflate(R.layout.dialog_custom, null);
			TextView dialogMessage = dialogView.findViewById(R.id.dialogMessage);
			dialogMessage.setText("Thank you for your feedback!");

			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setView(dialogView);
			builder.setCancelable(false);

			AlertDialog dialog = builder.create();
			dialog.show();

			new Handler().postDelayed(() -> {
				if (!isFinishing() && !isDestroyed()) {
					dialog.dismiss();
					ActivityUtils.redirectToActivity(this, MainActivity.class, true);
				}
			}, 3000);
		}
	}
}
