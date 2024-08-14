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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.example.lioneats.R;
import com.example.lioneats.api.ApiService;
import com.example.lioneats.fragments.HeaderFragment;
import com.example.lioneats.models.Dish;
import com.example.lioneats.models.ML_feedback;
import com.example.lioneats.utils.RetrofitClient;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ImageResultActivity extends AppCompatActivity {
	private ML_feedback feedback;
	private ImageView imageView;
	private TextView resultTextView, viewDishBtn, viewShopsBtn;
	private ProgressBar progressBar;
	private Spinner spinnerDishName;
	private EditText remarksEditText;
	private Button submitBtn;
	private String selectedDishName;
	private Uri imageUri;
	private List<Dish> dishList;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_image_result);

		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		transaction.replace(R.id.headerFragmentContainer, new HeaderFragment());
		transaction.commit();

		feedback = new ML_feedback();
		imageView = findViewById(R.id.imageView);
		resultTextView = findViewById(R.id.resultText);
		progressBar = findViewById(R.id.progressBar);
		progressBar.setVisibility(View.VISIBLE);
		spinnerDishName = findViewById(R.id.spinnerDishName);
		remarksEditText = findViewById(R.id.remarks);
		submitBtn = findViewById(R.id.submitBtn);

		viewDishBtn = findViewById(R.id.viewDishBtn);
		viewShopsBtn = findViewById(R.id.viewShopsBtn);

		List<String> dishNames = getDishNames();
		ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
				android.R.layout.simple_spinner_item, dishNames);

		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinnerDishName.setAdapter(adapter);
		spinnerDishName.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
				selectedDishName = parentView.getItemAtPosition(position).toString();
				Toast.makeText(ImageResultActivity.this, "Selected: " + selectedDishName, Toast.LENGTH_SHORT).show();
			}

			@Override
			public void onNothingSelected(AdapterView<?> parentView) {
				selectedDishName = null;
			}
		});

		Intent intent = getIntent();
		String imageUriString = intent.getStringExtra("imageUri");
		if (imageUriString != null) {
			imageUri = Uri.parse(imageUriString);
			imageView.setImageURI(imageUri);
			uploadImage(imageUri);
		}
		submitBtn.setOnClickListener(v -> submitFeedback());
	}

	private void uploadImage(Uri imageUri) {
		InputStream inputStream = null;
		try {
			inputStream = getContentResolver().openInputStream(imageUri);
			byte[] bytes = new byte[inputStream.available()];
			inputStream.read(bytes);

			String mimeType = getContentResolver().getType(imageUri);
			if (mimeType == null) {
				mimeType = "image/jpeg";
			}

			RequestBody requestFile = RequestBody.create(MediaType.parse(mimeType), bytes);
			MultipartBody.Part imagePart = MultipartBody.Part.createFormData("file", "image.jpg", requestFile);

			ApiService apiService = RetrofitClient.getApiService();
			Call<ResponseBody> call = apiService.uploadImage(imagePart);

			call.enqueue(new Callback<ResponseBody>() {
				@Override
				public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
					progressBar.setVisibility(View.GONE);
					try {
						if (response.isSuccessful() && response.body() != null) {
							String apiResponse = response.body().string();
							Log.d("API Response", "Success response: " + apiResponse);

							Gson gson = new Gson();
							Type type = new TypeToken<Map<String, String>>() {}.getType();
							Map<String, String> responseMap = gson.fromJson(apiResponse, type);

							String predictedDish = responseMap.get("predictedDish");
							String imageBlobUrl = responseMap.get("imageUrl");
							feedback.setMl_result(predictedDish);
							feedback.setImageBlobUrl(imageBlobUrl);

							resultTextView.setText(predictedDish);
							viewDishBtn.setClickable(true);
							viewDishBtn.setOnClickListener(v -> viewDishDetail(predictedDish));
							//viewShopsBtn.setClickable(true);
							//viewShopsBtn.setOnClickListener(v -> viewShops(predictedDish));

						} else {
							Log.e("API Response", "Failed response: " + response.errorBody().string());
							Toast.makeText(ImageResultActivity.this, "Upload failed: " + response.message(), Toast.LENGTH_SHORT).show();
						}
					} catch (IOException | JsonSyntaxException e) {
						e.printStackTrace();
						Log.e("API Response", "Exception during parsing or response: " + e.getMessage());
						Toast.makeText(ImageResultActivity.this, "Failed to parse response", Toast.LENGTH_SHORT).show();
					}
				}

				@Override
				public void onFailure(Call<ResponseBody> call, Throwable t) {
					progressBar.setVisibility(View.GONE);
					Log.e("API Connection Error", "onFailure: " + t.getMessage());
					Toast.makeText(ImageResultActivity.this, "Image upload failed", Toast.LENGTH_SHORT).show();
				}
			});
		} catch (IOException e) {
			progressBar.setVisibility(View.GONE);
			e.printStackTrace();
			Toast.makeText(ImageResultActivity.this, "Failed to open image", Toast.LENGTH_SHORT).show();
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	private List<String> getDishNames() {
		SharedPreferences dishListPreferences = getSharedPreferences("dish_list", MODE_PRIVATE);
		String jsonDishes = dishListPreferences.getString("dishes", "");

		Gson gson = new Gson();
		Type listType = new TypeToken<List<Dish>>() {}.getType();
		dishList = gson.fromJson(jsonDishes, listType);

		List<String> dishNames = new ArrayList<>();
		for (Dish dish : dishList) {
			dishNames.add(dish.getDishDetailName());
		}
		return dishNames;
	}

	private void viewDishDetail(String dishDetailName) {
		int dishID = -1; // Default invalid ID
		for (Dish dish : dishList) {
			if (dish.getDishDetailName().equalsIgnoreCase(dishDetailName)) {
				dishID = dish.getId();
				break;
			}
		}

		if (dishID != -1) { // Check if a valid dish ID was found
			Intent intent = new Intent(ImageResultActivity.this, DishDetailsActivity.class);
			intent.putExtra("dishID", dishID);
			intent.putExtra("dishImageUrl", getDishImageUrl(dishID)); // Pass image URL
			startActivity(intent);
		} else {
			Toast.makeText(this, "Dish not found", Toast.LENGTH_SHORT).show();
		}
	}

	// Helper method to get the image URL of the dish by its ID
	private String getDishImageUrl(int dishID) {
		for (Dish dish : dishList) {
			if (dish.getId() == dishID) {
				return dish.getImageUrl();
			}
		}
		return null;
	}

	private void submitFeedback() {
			String remarks = remarksEditText.getText().toString();

			if (!selectedDishName.isEmpty()) {
				feedback.setUserDish(selectedDishName);
				feedback.setRemarks(remarks);

				Gson gson = new GsonBuilder().setPrettyPrinting().create();
				String feedbackJson = gson.toJson(feedback);
				Log.d("ImageResultActivity", "Feedback JSON: " + feedbackJson);

				ApiService apiService = RetrofitClient.getApiService();
				Call<ResponseBody> call = apiService.submitFeedback(feedback);
				call.enqueue(new Callback<ResponseBody>() {
					@Override
					public void onResponse(Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {
						if (response.isSuccessful()) {
							submitSuccessDialog();
						} else {
							Log.e("ImageResultActivity", "Feedback submission failed: " + response.message());
							Toast.makeText(ImageResultActivity.this, "Feedback submission failed", Toast.LENGTH_SHORT).show();
						}
					}

					@Override
					public void onFailure(Call<ResponseBody> call, Throwable t) {
						Log.e("ImageResultActivity", "Feedback submission error: " + t.getMessage());
						Toast.makeText(ImageResultActivity.this, "Failed to submit submitFeedback", Toast.LENGTH_SHORT).show();
					}
				});
			} else {
				Toast.makeText(this, "Please enter a dish name", Toast.LENGTH_SHORT).show();
			}
	}
	private void submitSuccessDialog() {
		LayoutInflater inflater = getLayoutInflater();
		View dialogView = inflater.inflate(R.layout.dialog_custom, null);
		TextView dialogMessage = dialogView.findViewById(R.id.dialogMessage);
		dialogMessage.setText("Thank you for your submitFeedback!");

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
		Intent intent = new Intent(ImageResultActivity.this, MainActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
		finish();
	}
}