package com.example.lioneats.fragments;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.lioneats.R;
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
import androidx.loader.content.CursorLoader;

import com.example.lioneats.activities.ImageResultActivity;
import com.example.lioneats.activities.LoginActivity;
import com.example.lioneats.activities.MainActivity;
import com.example.lioneats.activities.RegisterAccountActivity;
import com.example.lioneats.activities.UpdateUserActivity;
import com.example.lioneats.api.ApiService;
import com.example.lioneats.models.ML_feedback;
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
	private MultipartBody.Part image;
	private TextView usernameText;
	private TextView actionBtn;
	private ImageButton cameraBtn;
	private ImageView logoBtn;
	private SharedPreferences sharedPreferences;

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_header_row, container, false);

		usernameText = view.findViewById(R.id.usernameText);
		actionBtn = view.findViewById(R.id.actionBtn);
		cameraBtn = view.findViewById(R.id.cameraBtn);
		logoBtn = view.findViewById(R.id.logoBtn);

		sharedPreferences = getActivity().getSharedPreferences("user_session", getActivity().MODE_PRIVATE);
		String username = sharedPreferences.getString("username", null);

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
		logoBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getActivity(), MainActivity.class);
				startActivity(intent);
			}
		});
		return view;
	}

	private void logout() {
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.clear();
		editor.apply();

		Intent intent = new Intent(getActivity(), MainActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
		getActivity().finish();
	}

	private void showLoginDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle("Login for dish classifier function")
				.setMessage("No Account yet? Register Now! ")
				.setPositiveButton("Login", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Intent intent = new Intent(getActivity(), LoginActivity.class);
						startActivity(intent);
					}
				})
				.setNegativeButton("Register Account", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Intent intent = new Intent(getActivity(), RegisterAccountActivity.class);
						startActivity(intent);
					}
				});
		builder.create().show();
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
				Log.d(getActivity().toString(), "photoUri: " + photoURI.toString());
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
			uploadImage(photoURI);
		} else if (requestCode == REQUEST_IMAGE_PICK && resultCode == getActivity().RESULT_OK) {
			if (data != null) {
				photoURI = data.getData();
				uploadImage(photoURI);
			}
		}
	}

	private void uploadImage(Uri imageUri) {
		try {
			InputStream inputStream = getActivity().getContentResolver().openInputStream(imageUri);
			byte[] bytes = new byte[inputStream.available()];
			inputStream.read(bytes);

			RequestBody requestFile = RequestBody.create(MediaType.parse("image/jpeg"), bytes);
			image = MultipartBody.Part.createFormData("image", "image.jpg", requestFile);

			Retrofit retrofit = new Retrofit.Builder()
					.baseUrl("https://a867fedb-31a5-49ed-924f-cc87386050ec.mock.pstmn.io")
					.addConverterFactory(GsonConverterFactory.create())
					.build();

			ApiService apiService = retrofit.create(ApiService.class);
			Call<ResponseBody> call = apiService.dishResult(image);

			call.enqueue(new Callback<ResponseBody>() {
				@Override
				public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
					try {
						if (response.isSuccessful()) {
							String apiResponse = response.body().string();

							// Log the JSON response from the API
							Log.d("API Response", "Success response: " + apiResponse);

							// Parse JSON response into a Map
							Gson gson = new Gson();
							Type type = new TypeToken<Map<String, String>>() {}.getType();
							Map<String, String> responseMap = gson.fromJson(apiResponse, type);

							// Extract the result value using the key
							String result = responseMap.get("result");

							// Create an ML_feedback object to store data
							ML_feedback feedback = new ML_feedback();
							feedback.setImage(image);
							feedback.setResult(result);

							// Start ImageResultActivity with the feedback object
							Intent intent = new Intent(getActivity(), ImageResultActivity.class);
							intent.putExtra("feedback", gson.toJson(feedback)); // Pass the feedback object as JSON
							intent.putExtra("imageUri", imageUri.toString()); // Pass URI as a string
							startActivity(intent);

						} else {
							// Log the error response
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
					// Log the connection error
					Log.e("API Connection Error", "onFailure: " + t.getMessage());
					Toast.makeText(getActivity(), "Image upload failed", Toast.LENGTH_SHORT).show();
				}
			});
		} catch (IOException e) {
			e.printStackTrace();
			Toast.makeText(getActivity(), "Failed to open image", Toast.LENGTH_SHORT).show();
		}
	}

	private String getRealPathFromURI(Uri contentUri) {
		String[] proj = {MediaStore.Images.Media.DATA};
		CursorLoader loader = new CursorLoader(getActivity(), contentUri, proj, null, null, null);
		Cursor cursor = loader.loadInBackground();
		int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		cursor.moveToFirst();
		String result = cursor.getString(column_index);
		cursor.close();
		return result;
	}
}
