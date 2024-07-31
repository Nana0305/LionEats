package com.example.lioneats.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lioneats.R;
import com.example.lioneats.api.FeedApi;
import com.example.lioneats.dtos.ShopDTO;
import com.example.lioneats.utils.RetrofitService;

import java.io.InputStream;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RestaurantAdapter extends RecyclerView.Adapter<RestaurantAdapter.RestaurantViewHolder> {

	private List<ShopDTO> restaurantList;

	public RestaurantAdapter(List<ShopDTO> restaurantList) {
		this.restaurantList = restaurantList;
	}

	@NonNull
	@Override
	public RestaurantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_restaurant, parent, false);
		return new RestaurantViewHolder(view);
	}

	@Override
	public void onBindViewHolder(@NonNull RestaurantViewHolder holder, int position) {
		FeedApi feedApi = RetrofitService.getInstance().create(FeedApi.class);
		ShopDTO restaurant = restaurantList.get(position);
		holder.nameTextView.setText(restaurant.getName());
		holder.addressTextView.setText(restaurant.getFormattedAddress());
		holder.ratingTextView.setText(String.valueOf(restaurant.getRating()));

		if (restaurant.getPhotos() != null && !restaurant.getPhotos().isEmpty()) {
			Call<ResponseBody> photoUrlCall = feedApi.getPhoto(restaurant.getPhotos().get(0).getPhotoReference());

			photoUrlCall.enqueue(new Callback<ResponseBody>() {
				@Override
				public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
					if (response.isSuccessful() && response.body() != null) {
						InputStream inputStream = response.body().byteStream();
						Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
						holder.photoImageView.setImageBitmap(bitmap);
						holder.photoImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
					} else {
						holder.photoImageView.setImageResource(R.drawable.logo);
						holder.photoImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
					}
				}

				@Override
				public void onFailure(Call<ResponseBody> call, Throwable t) {
					holder.photoImageView.setImageResource(R.drawable.logo);
					holder.photoImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
				}
			});
		} else {
			holder.photoImageView.setImageResource(R.drawable.logo);
			holder.photoImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
		}
	}

	@Override
	public int getItemCount() {
		return restaurantList.size();
	}

	public static class RestaurantViewHolder extends RecyclerView.ViewHolder {

		TextView nameTextView;
		TextView addressTextView;
		TextView ratingTextView;
		ImageView photoImageView;

		public RestaurantViewHolder(@NonNull View itemView) {
			super(itemView);
			nameTextView = itemView.findViewById(R.id.restaurant_name);
			addressTextView = itemView.findViewById(R.id.restaurant_address);
			ratingTextView = itemView.findViewById(R.id.restaurant_rating);
			photoImageView = itemView.findViewById(R.id.restaurant_photo);
		}
	}
}

