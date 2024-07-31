package com.example.lioneats.fragments;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.lioneats.R;
import com.example.lioneats.activities.ImageResultActivity;
import com.example.lioneats.activities.LoginActivity;
import com.example.lioneats.activities.MainActivity;
import com.example.lioneats.activities.RegisterAccountActivity;
import com.example.lioneats.activities.UpdateUserActivity;

public class HeaderFragment extends Fragment {
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
			usernameText.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(getActivity(), UpdateUserActivity.class);
					startActivity(intent);
				}
			});
			actionBtn.setText("Logout");
			actionBtn.setVisibility(View.VISIBLE);
			actionBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					logout();
				}
			});
			cameraBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(getActivity(), ImageResultActivity.class);
					startActivity(intent);
				}
			});
			logoBtn.setClickable(true);
			logoBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(getActivity(), MainActivity.class);
					startActivity(intent);
				}
			});
		} else {
			usernameText.setText("Guest");
			actionBtn.setText("Login");
			actionBtn.setVisibility(View.VISIBLE);
			actionBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(getActivity(), LoginActivity.class);
					startActivity(intent);
				}
			});
			cameraBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					showLoginDialog();
				}
			});
		}
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
}
