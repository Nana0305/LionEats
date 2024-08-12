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
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;

public class ImageResultActivity extends AppCompatActivity {
	private ML_feedback feedback;
	private ImageView imageView;
	private TextView resultTextView;
	private Spinner spinnerDishName;
	private EditText remarksEditText;
	private Button submitBtn;
	private String selectedDishName;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_image_result);

		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		transaction.replace(R.id.headerFragmentContainer, new HeaderFragment());
		transaction.commit();

		imageView = findViewById(R.id.imageView);
		resultTextView = findViewById(R.id.resultText);
		spinnerDishName = findViewById(R.id.spinnerDishName);
		remarksEditText = findViewById(R.id.remarks);
		submitBtn = findViewById(R.id.submitBtn);

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
		String feedbackJson = intent.getStringExtra("feedBack");
		if (feedbackJson != null) {
			Gson gson = new Gson();
			feedback = gson.fromJson(feedbackJson, ML_feedback.class);

			String imageUriString = intent.getStringExtra("imageUri");
			if (imageUriString != null) {
				Uri imageUri = Uri.parse(imageUriString);
				imageView.setImageURI(imageUri);
			}
			resultTextView.setText(feedback.getMl_result());
		}
		submitBtn.setOnClickListener(v -> submitFeedback());
	}

	private List<String> getDishNames() {
		SharedPreferences sharedPreferences = getSharedPreferences("dish_list", MODE_PRIVATE);
		String jsonDishes = sharedPreferences.getString("dishes", "");

		Gson gson = new Gson();
		Type listType = new TypeToken<List<Dish>>() {}.getType();
		List<Dish> dishList = gson.fromJson(jsonDishes, listType);

		List<String> dishNames = new ArrayList<>();
		for (Dish dish : dishList) {
			dishNames.add(dish.getDishDetailName());
		}
		return dishNames;
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