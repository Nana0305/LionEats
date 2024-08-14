package com.example.lioneats.adapters;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.lioneats.R;
import com.example.lioneats.activities.ShopDetailsActivity;
import com.example.lioneats.dtos.PhotoDTO;
import com.example.lioneats.dtos.ShopDTO;

import java.util.List;

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
		ShopDTO restaurant = restaurantList.get(position);

		String name = restaurant.getName() != null ? restaurant.getName() : "Unknown";
		holder.nameTextView.setText(name);

		String address = restaurant.getFormattedAddress() != null ? restaurant.getFormattedAddress() : "Unknown";
		holder.addressTextView.setText(address);

		String rating = restaurant.getRating() != 0 ? String.valueOf(restaurant.getRating()) : "Unknown";
		holder.ratingTextView.setText(rating);

		String key = restaurant.getKeyWord() != null ? restaurant.getKeyWord() : "Unknown";
		holder.keyTextView.setText(key);

		String price = restaurant.getPriceLevel() != 0 ? getPriceLevel(restaurant.getPriceLevel()) : "Unknown";
		holder.priceTextView.setText(price);

		if (restaurant.getPhotos() != null && !restaurant.getPhotos().isEmpty()) {
			PhotoDTO photo = restaurant.getPhotos().get(0);
			String photoUrl = photo.getPhotoReference();

			Log.d("RestaurantAdapter", "Photo URL for " + restaurant.getName() + ": " + photoUrl);

			if (photoUrl != null && !photoUrl.isEmpty()) {
				Glide.with(holder.photoImageView.getContext())
						.load(photoUrl)
						.placeholder(R.drawable.default_image)
						.error(R.drawable.default_image)
						.into(holder.photoImageView);
			} else {
				holder.photoImageView.setImageResource(R.drawable.default_image);
			}
		} else {
			holder.photoImageView.setImageResource(R.drawable.default_image);
		}

		holder.itemView.setOnClickListener(v -> {
			Intent intent = new Intent(holder.itemView.getContext(), ShopDetailsActivity.class);
			intent.putExtra("shop", restaurant);
			holder.itemView.getContext().startActivity(intent);
		});
	}

	@Override
	public int getItemCount() {
		return restaurantList.size();
	}

	public static class RestaurantViewHolder extends RecyclerView.ViewHolder {

		TextView nameTextView;
		TextView addressTextView;
		TextView ratingTextView;
		TextView keyTextView;
		TextView priceTextView;
		ImageView photoImageView;

		public RestaurantViewHolder(@NonNull View itemView) {
			super(itemView);
			nameTextView = itemView.findViewById(R.id.restaurant_name);
			addressTextView = itemView.findViewById(R.id.restaurant_address);
			ratingTextView = itemView.findViewById(R.id.restaurant_rating);
			keyTextView = itemView.findViewById(R.id.restaurant_key);
			priceTextView = itemView.findViewById(R.id.restaurant_price);
			photoImageView = itemView.findViewById(R.id.restaurant_photo);
		}
	}

	private String getPriceLevel(int priceLevel) {
		switch (priceLevel) {
			case 1:
				return "$";
			case 2:
				return "$$";
			case 3:
				return "$$$";
			case 4:
				return "$$$$";
			default:
				return "Unknown";
		}
	}
}


