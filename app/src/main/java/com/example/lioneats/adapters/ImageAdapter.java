package com.example.lioneats.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lioneats.R;
import com.example.lioneats.models.Dish;
import com.squareup.picasso.Picasso;

import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {
	private Context context;
	private List<Dish> dishList;
	private OnItemClickListener onItemClickListener;

	public interface OnItemClickListener {
		void onItemClick(int position);
	}

	public ImageAdapter(Context context, List<Dish> dishList, OnItemClickListener onItemClickListener) {
		this.context = context;
		this.dishList = dishList;
		this.onItemClickListener = onItemClickListener;
	}

	@NonNull
	@Override
	public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(context).inflate(R.layout.item_image, parent, false);
		return new ImageViewHolder(view);
	}

	@Override
	public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
		Dish dish = dishList.get(position);
		Picasso.get()
				.load(dish.getImageUrl())
				.placeholder(R.drawable.default_image)
				.into(holder.imageView);
		holder.imageView.setOnClickListener(v -> onItemClickListener.onItemClick(position));
	}

	@Override
	public int getItemCount() {
		return dishList.size();
	}

	static class ImageViewHolder extends RecyclerView.ViewHolder {
		ImageView imageView;

		public ImageViewHolder(@NonNull View itemView) {
			super(itemView);
			imageView = itemView.findViewById(R.id.imageView);
		}
	}
}

