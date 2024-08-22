package com.example.lioneats.utils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.example.lioneats.R;
import com.example.lioneats.activities.LoginActivity;
import com.example.lioneats.activities.MainActivity;

public class ActivityUtils {

	public static void submitSuccessDialog(Context context, String message, Runnable afterDismissAction) {
		LayoutInflater inflater = LayoutInflater.from(context);
		View dialogView = inflater.inflate(R.layout.dialog_custom, null);
		TextView dialogMessage = dialogView.findViewById(R.id.dialogMessage);
		dialogMessage.setText(message);

		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setView(dialogView);
		builder.setCancelable(false);

		AlertDialog dialog = builder.create();
		dialog.show();

		new Handler().postDelayed(() -> {
			dialog.dismiss();
			if (afterDismissAction != null) {
				afterDismissAction.run();
			}
		}, 2000);
	}

	public static void redirectToActivity(Context context, Class<?> targetActivity, boolean clearBackStack) {
		Intent intent = new Intent(context, targetActivity);
		if (clearBackStack) {
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		}
		context.startActivity(intent);
	}

	public static void logout(Context context, Runnable afterLogoutAction) {
		SharedPreferences userSessionPreferences = context.getSharedPreferences("user_session", Context.MODE_PRIVATE);
		SharedPreferences userPreferences = context.getSharedPreferences("user", Context.MODE_PRIVATE);

		SharedPreferences.Editor sessionEditor = userSessionPreferences.edit();
		sessionEditor.clear();
		sessionEditor.apply();

		SharedPreferences.Editor userEditor = userPreferences.edit();
		userEditor.clear();
		userEditor.apply();

		Toast.makeText(context, "Successfully logged out", Toast.LENGTH_SHORT).show();

		if (afterLogoutAction != null) {
			afterLogoutAction.run();
		}
	}
}
