package com.example.lioneats.viewmodels;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.lioneats.api.ApiService;
import com.example.lioneats.dtos.DishDTO;
import com.example.lioneats.dtos.ML_feedbackDTO;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ImageResultViewModel extends ViewModel {
	private static final String TAG = "ImageResultViewModel";
	private final MutableLiveData<String> resultText = new MutableLiveData<>("");
	private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
	private final MutableLiveData<String> remarks = new MutableLiveData<>("");
	private final MutableLiveData<String> errorMessage = new MutableLiveData<>("");
	private final MutableLiveData<Boolean> isFeedbackSubmitted = new MutableLiveData<>(false);
	private final MutableLiveData<Integer> navigateToDishDetails = new MutableLiveData<>(null);
	private ExecutorService executorService = Executors.newSingleThreadExecutor();
	private String selectedDishName;
	private List<DishDTO> dishList;
	private ML_feedbackDTO feedback = new ML_feedbackDTO();
	private ApiService apiService;

	public ImageResultViewModel(ApiService apiService) {
		this.apiService = apiService;
	}

	public MutableLiveData<String> getResultText() {
		return resultText;
	}

	public MutableLiveData<Boolean> getIsLoading() {
		return isLoading;
	}

	public MutableLiveData<String> getRemarks() {
		return remarks;
	}

	public MutableLiveData<String> getErrorMessage() {
		return errorMessage;
	}

	public MutableLiveData<Boolean> getIsFeedbackSubmitted() {
		return isFeedbackSubmitted;
	}

	public void setSelectedDishName(String selectedDishName) {
		this.selectedDishName = selectedDishName;
		feedback.setUserDish(selectedDishName);
	}

	public void setDishList(List<DishDTO> dishList) {
		this.dishList = dishList;
	}

	public MutableLiveData<Integer> getNavigateToDishDetails() {
		return navigateToDishDetails;
	}

	public void onViewDishClicked() {
		String predictedDish = resultText.getValue();
		if (predictedDish != null && !predictedDish.isEmpty()) {
			int dishID = getDishIDByName(predictedDish);
			if (dishID != -1) {
				navigateToDishDetails.setValue(dishID);
			} else {
				errorMessage.setValue("Dish not found");
			}
		} else {
			errorMessage.setValue("Predicted dish is null");
		}
	}

	public int getDishIDByName(String dishDetailName) {
		for (DishDTO dish : dishList) {
			Log.d(TAG, "Checking dish: " + dish.getDishDetailName());
			if (dish.getDishDetailName().equalsIgnoreCase(dishDetailName)) {
				Log.d(TAG, "Match found: " + dish.getDishDetailName() + " with ID: " + dish.getId());
				return dish.getId();
			}
		}
		Log.e(TAG, "No match found for: " + dishDetailName);
		return -1;
	}

	public String getDishImageUrl(int dishID) {
		for (DishDTO dish : dishList) {
			if (dish.getId() == dishID) {
				return dish.getImageUrl();
			}
		}
		return null;
	}

	public void uploadImage(Uri imageUri, ContentResolver contentResolver) {
		Log.d(TAG, "Starting image upload with URI: " + imageUri);
		isLoading.postValue(true);

		executorService.execute(() -> {
			try (InputStream inputStream = contentResolver.openInputStream(imageUri)) {
				if (inputStream != null) {
					byte[] compressedBytes = compressImage(inputStream);

					if (compressedBytes != null) {
						uploadImageToServer(compressedBytes);
					} else {
						errorMessage.postValue("Image compression failed");
						isLoading.postValue(false);
					}
				} else {
					errorMessage.postValue("Failed to open image");
					isLoading.postValue(false);
				}
			} catch (IOException e) {
				errorMessage.postValue("Image processing error: " + e.getMessage());
				isLoading.postValue(false);
			}
		});
	}

	@Override
	protected void onCleared() {
		super.onCleared();
		executorService.shutdown();
	}

	private void uploadImageToServer(byte[] compressedBytes) {
		try {
			String mimeType = "image/jpeg";
			RequestBody requestFile = RequestBody.create(MediaType.parse(mimeType), compressedBytes);
			MultipartBody.Part imagePart = MultipartBody.Part.createFormData("file", "image.jpg", requestFile);

			Call<ResponseBody> call = apiService.uploadImage(imagePart);
			call.enqueue(new ImageUploadCallback());
		} catch (Exception e) {
			isLoading.postValue(false);
			errorMessage.postValue("Unexpected error during upload: " + e.getMessage());
		}
	}

	private byte[] compressImage(InputStream inputStream) {
		try {
			Bitmap originalBitmap = BitmapFactory.decodeStream(inputStream);

			if (originalBitmap == null) {
				Log.e(TAG, "Failed to decode image from input stream.");
				return null;
			}
			int originalWidth = originalBitmap.getWidth();
			int originalHeight = originalBitmap.getHeight();
			int maxWidth = 1000;
			int maxHeight = 1000;
			float scalingFactor = Math.min(
					(float) maxWidth / originalWidth,
					(float) maxHeight / originalHeight
			);
			int newWidth = Math.round(originalWidth * scalingFactor);
			int newHeight = Math.round(originalHeight * scalingFactor);
			Bitmap resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, true);
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream);

			originalBitmap.recycle();

			return stream.toByteArray();
		} finally {
			try {
				if (inputStream != null) {
					inputStream.close();
				}
			} catch (IOException e) {
				Log.e(TAG, "Failed to close InputStream: " + e.getMessage(), e);
			}
		}
	}

	private class ImageUploadCallback implements Callback<ResponseBody> {
		@Override
		public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
			if (response.isSuccessful() && response.body() != null) {
				handleSuccessfulImageUpload(response);
			} else {
				Log.e(TAG, "API Error: " + response.message());
				errorMessage.setValue("Upload failed: " + response.message());
				isLoading.postValue(false);
			}
		}

		@Override
		public void onFailure(Call<ResponseBody> call, Throwable t) {
			Log.e(TAG, "Image upload failed: " + t.getMessage(), t);
			errorMessage.setValue("Image upload failed");
			isLoading.postValue(false);
		}
	}

	private void handleSuccessfulImageUpload(Response<ResponseBody> response) {
		try {
			String apiResponse = response.body().string();
			Log.d(TAG, "API Response: " + apiResponse);

			Gson gson = new Gson();
			Type type = new TypeToken<Map<String, String>>() {}.getType();
			Map<String, String> responseMap = gson.fromJson(apiResponse, type);

			String predictedDish = responseMap.get("predictedDish");
			String imageLocation = responseMap.get("imageUrl");
			feedback.setMl_result(predictedDish);
			feedback.setImageLocation(imageLocation);

			resultText.postValue(predictedDish);
			isLoading.postValue(false);
		} catch (IOException | JsonSyntaxException e) {
			Log.e(TAG, "Error parsing response: " + e.getMessage(), e);
			errorMessage.setValue("Failed to parse response");
			isLoading.postValue(false);
		}
	}

	public void onSubmit() {
		String remarksText = remarks.getValue();

		if (feedback.getUserDish() != null && !feedback.getUserDish().isEmpty()) {
			feedback.setRemarks(remarksText);

			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			String feedbackJson = gson.toJson(feedback);
			Log.d(TAG, "Feedback JSON: " + feedbackJson);

			Call<ResponseBody> call = apiService.submitFeedback(feedback);

			call.enqueue(new FeedbackCallback());
		} else {
			errorMessage.setValue("Please enter a dish name");
		}
	}

	private class FeedbackCallback implements Callback<ResponseBody> {
		@Override
		public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
			if (response.isSuccessful()) {
				Log.d(TAG, "Feedback submitted successfully");
				isFeedbackSubmitted.setValue(true);
			} else {
				Log.e(TAG, "Feedback submission failed: " + response.message());
				errorMessage.setValue("Feedback submission failed");
			}
		}

		@Override
		public void onFailure(Call<ResponseBody> call, Throwable t) {
			Log.e(TAG, "Feedback submission error: " + t.getMessage(), t);
			errorMessage.setValue("Failed to submit feedback");
		}
	}
}