package com.example.lioneats.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lioneats.R;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {
	private Context context;
	private int[] images;
	private OnItemClickListener onItemClickListener;

	public ImageAdapter(Context context, int[] images, OnItemClickListener onItemClickListener) {
		this.context = context;
		this.images = images;
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
		holder.imageView.setImageResource(images[position]);
		holder.imageView.setOnClickListener(v -> onItemClickListener.onItemClick(position));
	}

	@Override
	public int getItemCount() {
		return images.length;
	}

	public interface OnItemClickListener {
		void onItemClick(int position);
	}

	static class ImageViewHolder extends RecyclerView.ViewHolder {
		ImageView imageView;

		ImageViewHolder(@NonNull View itemView) {
			super(itemView);
			imageView = itemView.findViewById(R.id.imageView);
		}
	}
}

