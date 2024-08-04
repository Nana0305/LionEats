package com.example.lioneats.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.example.lioneats.R;
import com.example.lioneats.api.ApiService;
import com.example.lioneats.fragments.HeaderFragment;
import com.example.lioneats.models.ML_feedback;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ImageResultActivity extends AppCompatActivity {
	private ML_feedback feedback;
	private Uri imageUri;
	private ImageView imageView;
	private TextView resultTextView;
	private EditText dishNameEditText;
	private EditText remarksEditText;
	private Button submitBtn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_image_result);

		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		transaction.replace(R.id.headerFragmentContainer, new HeaderFragment());
		transaction.commit();

		imageView = findViewById(R.id.imageView);
		resultTextView = findViewById(R.id.resultText);
		dishNameEditText = findViewById(R.id.dishName);
		remarksEditText = findViewById(R.id.remarks);
		submitBtn = findViewById(R.id.submitBtn);

		if (imageView == null || resultTextView == null || dishNameEditText == null ||
				remarksEditText == null || submitBtn == null) {
			Log.e("ImageResultActivity", "One or more views failed to initialize. Please check the layout.");
			return;
		}

		Intent intent = getIntent();
		String feedbackJson = intent.getStringExtra("feedback");
		if (feedbackJson != null) {
			Gson gson = new Gson();
			feedback = gson.fromJson(feedbackJson, ML_feedback.class);

			String imageUriString = intent.getStringExtra("imageUri");
			if (imageUriString != null) {
				imageUri = Uri.parse(imageUriString);
				imageView.setImageURI(imageUri);
			}
			resultTextView.setText(feedback.getResult());
		}

		// Set up the submit button click listener
		submitBtn.setOnClickListener(v -> submitFeedback());
	}
	private void submitFeedback() {
			String dishName = dishNameEditText.getText().toString();
			String remarks = remarksEditText.getText().toString();

			if (!dishName.isEmpty()) {
				feedback.setDishName(dishName);
				feedback.setRemarks(remarks);

				Gson gson = new GsonBuilder().setPrettyPrinting().create();
				String feedbackJson = gson.toJson(feedback);
				Log.d("ImageResultActivity", "Feedback JSON: " + feedbackJson);

				Retrofit retrofit = new Retrofit.Builder()
						.baseUrl("https://a867fedb-31a5-49ed-924f-cc87386050ec.mock.pstmn.io")
						.addConverterFactory(GsonConverterFactory.create())
						.build();

				ApiService apiService = retrofit.create(ApiService.class);
				Call<ResponseBody> call = apiService.feedback(feedback);
				call.enqueue(new Callback<ResponseBody>() {
					@Override
					public void onResponse(Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {
						if (response.isSuccessful()) {
							Toast.makeText(ImageResultActivity.this, "Feedback submitted successfully", Toast.LENGTH_SHORT).show();
						} else {
							Log.e("ImageResultActivity", "Feedback submission failed: " + response.message());
							Toast.makeText(ImageResultActivity.this, "Feedback submission failed", Toast.LENGTH_SHORT).show();
						}
					}

					@Override
					public void onFailure(Call<ResponseBody> call, Throwable t) {
						Log.e("ImageResultActivity", "Feedback submission error: " + t.getMessage());
						Toast.makeText(ImageResultActivity.this, "Failed to submit feedback", Toast.LENGTH_SHORT).show();
					}
				});
			} else {
				Toast.makeText(this, "Please enter a dish name", Toast.LENGTH_SHORT).show();
			}
	}
}