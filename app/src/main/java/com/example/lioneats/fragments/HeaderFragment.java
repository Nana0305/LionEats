package com.example.lioneats.fragments;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.lioneats.R;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;

import androidx.annotation.NonNull;
import retrofit2.Callback;
import retrofit2.Response;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.example.lioneats.activities.ImageResultActivity;
import com.example.lioneats.activities.LoginActivity;
import com.example.lioneats.activities.MainActivity;
import com.example.lioneats.activities.RegisterAccountActivity;
import com.example.lioneats.activities.UpdateUserActivity;
import com.example.lioneats.api.ApiService;
import com.example.lioneats.models.ML_feedback;
import com.example.lioneats.utils.RetrofitClient;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class HeaderFragment extends Fragment {
	private static final int REQUEST_CAMERA_PERMISSION = 100;
	private static final int REQUEST_IMAGE_CAPTURE = 1;
	private static final int REQUEST_IMAGE_PICK = 2;
	private Uri photoURI;
	private TextView usernameText;
	private TextView actionBtn;
	private ImageButton cameraBtn;
	private ImageView logoBtn;
	private SharedPreferences userSessionPreferences;
	private SharedPreferences dishListPreferences;

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_header_row, container, false);

		usernameText = view.findViewById(R.id.usernameText);
		actionBtn = view.findViewById(R.id.actionBtn);
		cameraBtn = view.findViewById(R.id.cameraBtn);
		logoBtn = view.findViewById(R.id.logoBtn);

		userSessionPreferences = getActivity().getSharedPreferences("user_session", getActivity().MODE_PRIVATE);
		dishListPreferences = getActivity().getSharedPreferences("dish_list", getActivity().MODE_PRIVATE);

		String username = userSessionPreferences.getString("username", null);
		if (username != null) {
			usernameText.setText(username);
			usernameText.setClickable(true);
			usernameText.setOnClickListener(v -> {
				Intent intent = new Intent(getActivity(), UpdateUserActivity.class);
				startActivity(intent);
			});
			actionBtn.setText("Logout");
			actionBtn.setVisibility(View.VISIBLE);
			actionBtn.setOnClickListener(v -> logout());
			cameraBtn.setOnClickListener(v -> showImageSourceDialog());

		} else {
			usernameText.setText("Guest");
			actionBtn.setText("Login");
			actionBtn.setVisibility(View.VISIBLE);
			actionBtn.setOnClickListener(v -> {
				Intent intent = new Intent(getActivity(), LoginActivity.class);
				startActivity(intent);
			});
			cameraBtn.setOnClickListener(v -> showLoginDialog());
		}
		logoBtn.setClickable(true);
		logoBtn.setOnClickListener(v -> {
			Intent intent = new Intent(getActivity(), MainActivity.class);
			startActivity(intent);
		});
		return view;
	}

	private void logout() {
		SharedPreferences.Editor sessionEditor = userSessionPreferences.edit();
		sessionEditor.clear();
		sessionEditor.apply();

		Intent intent = new Intent(getActivity(), MainActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
		getActivity().finish();
	}

	private void showLoginDialog() {
		LayoutInflater inflater = getLayoutInflater();
		View dialogView = inflater.inflate(R.layout.dialog_login, null);
		Button positiveButton = dialogView.findViewById(R.id.positiveButton);
		Button negativeButton = dialogView.findViewById(R.id.negativeButton);

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setView(dialogView)
				.setCancelable(true);
		AlertDialog dialog = builder.create();
		dialog.show();

		positiveButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getActivity(), LoginActivity.class);
				startActivity(intent);
				dialog.dismiss();
			}
		});

		negativeButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getActivity(), RegisterAccountActivity.class);
				startActivity(intent);
				dialog.dismiss();
			}
		});
	}

	private void showImageSourceDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle("Choose Image Source")
				.setItems(new CharSequence[]{"Camera", "Gallery"},  new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						switch (which) {
							case 0:
								if (ContextCompat.checkSelfPermission((getActivity()), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
									ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
								} else {
									dispatchTakePictureIntent();
								}
								break;
							case 1:
								dispatchPickPictureIntent();
								break;
						}
					}
				});
		builder.create().show();
	}
	private void dispatchTakePictureIntent() {
		Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
			File photoFile = null;
			try {
				photoFile = createImageFile();
			} catch (IOException ex) {
				Log.e(getActivity().toString(), "Network Error: ");
			}
			if (photoFile != null) {
				photoURI = FileProvider.getUriForFile(getActivity(), "com.example.lioneats.provider", photoFile);
				Log.d("File URI", "Photo URI: " + photoURI.toString());
				takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
				startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
			}
		}
	}

	private void dispatchPickPictureIntent() {
		Intent pickPictureIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		if (pickPictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
			startActivityForResult(pickPictureIntent, REQUEST_IMAGE_PICK);
		}
	}

	private File createImageFile() throws IOException {
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		String imageFileName = "JPEG_" + timeStamp + "_";
		File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
		return File.createTempFile(imageFileName, ".jpg", storageDir);
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		if (requestCode == REQUEST_CAMERA_PERMISSION) {
			if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				dispatchTakePictureIntent();
			} else {
				// Permission denied
				Toast.makeText(getActivity(), "Camera permission is required to take pictures", Toast.LENGTH_SHORT).show();
			}
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == getActivity().RESULT_OK) {
			Log.d("ActivityResult", "Image capture successful. URI: " + photoURI.toString());
			uploadImage(photoURI);
		} else if (requestCode == REQUEST_IMAGE_PICK && resultCode == getActivity().RESULT_OK) {
			if (data != null) {
				photoURI = data.getData();
				Log.d("ActivityResult", "Image selected from gallery. URI: " + photoURI.toString());
				uploadImage(photoURI);
			}
		}
	}

	private void uploadImage(Uri imageUri) {
		InputStream inputStream = null;
		try {
			inputStream = getActivity().getContentResolver().openInputStream(imageUri);
			byte[] bytes = new byte[inputStream.available()];
			inputStream.read(bytes);

			Log.d("Image Upload", "Read image bytes, size: " + bytes.length);

			String mimeType = getActivity().getContentResolver().getType(imageUri);
			if (mimeType == null) {
				mimeType = "image/jpeg";
			}

			Log.d("UploadImage", "URI: " + imageUri.toString());
			Log.d("UploadImage", "MIME Type: " + mimeType);

			RequestBody requestFile = RequestBody.create(MediaType.parse(mimeType), bytes);
			MultipartBody.Part imagePart = MultipartBody.Part.createFormData("file", "image.jpg", requestFile);

			Log.d("Image Upload", "MultipartBody created for image upload");

			ApiService apiService = RetrofitClient.getApiService();
			Call<ResponseBody> call = apiService.uploadImage(imagePart);

			call.enqueue(new Callback<ResponseBody>() {
				@Override
				public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
					try {
						if (response.isSuccessful()) {
							String apiResponse = response.body().string();
							Log.d("API Response", "Success response: " + apiResponse);

							Gson gson = new Gson();
							Type type = new TypeToken<Map<String, String>>() {}.getType();
							Map<String, String> responseMap = gson.fromJson(apiResponse, type);

							String imageBlobUrl = responseMap.get("imageUrl");
							String predictedDish = responseMap.get("predictedDish");
							Log.d("API Response", "Image Blob URL: " + imageBlobUrl);
							Log.d("API Response", "Predicted Dish: " + predictedDish);

							ML_feedback feedback = new ML_feedback();
							feedback.setImageBlobUrl(imageBlobUrl);
							feedback.setMl_result(predictedDish);

							Intent intent = new Intent(getActivity(), ImageResultActivity.class);
							intent.putExtra("feedBack", gson.toJson(feedback));
							intent.putExtra("imageUri", imageUri.toString());
							startActivity(intent);

						} else {
							Log.e("API Response", "Failed response: " + response.errorBody().string());
							Toast.makeText(getActivity(), "Upload failed: " + response.message(), Toast.LENGTH_SHORT).show();
						}
					} catch (IOException | JsonSyntaxException e) {
						e.printStackTrace();
						Log.e("API Response", "Exception during parsing or response: " + e.getMessage());
						Toast.makeText(getActivity(), "Failed to parse response", Toast.LENGTH_SHORT).show();
					}
				}

				@Override
				public void onFailure(Call<ResponseBody> call, Throwable t) {
					Log.e("API Connection Error", "onFailure: " + t.getMessage());
					Toast.makeText(getActivity(), "Image upload failed", Toast.LENGTH_SHORT).show();
				}
			});
		} catch (IOException e) {
			e.printStackTrace();
			Toast.makeText(getActivity(), "Failed to open image", Toast.LENGTH_SHORT).show();
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
}
