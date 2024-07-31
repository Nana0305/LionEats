package com.example.lioneats.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.lioneats.R;

public class MyCustomAdapter extends ArrayAdapter<Object> {
	private final Context context;

	protected String[] ranks,titles, addresses, ratings;

	public MyCustomAdapter(Context context, String[] ranks, String[] titles, String[] addresses, String[] ratings) {
		super(context, R.layout.row);
		this.context = context;
		this.ranks = ranks;
		this.titles = titles;
		this.addresses = addresses;
		this.ratings = ratings;

		addAll(new Object[titles.length]);
	}

	@androidx.annotation.NonNull
	public View getView(int pos, View view, @NonNull ViewGroup parent) {
		if (view == null) {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(
					Activity.LAYOUT_INFLATER_SERVICE);

			// if we are not responsible for adding the view to the parent,
			// then attachToRoot should be 'false' (which is in our case)
			view = inflater.inflate(R.layout.row, parent, false);
		}

		// set the image for ImageView
		ImageView imageView = view.findViewById(R.id.shopRank);
		int id = context.getResources().getIdentifier(ranks[pos],
				"drawable", context.getPackageName());
		imageView.setImageResource(id);

		// set the text for TextView
		TextView titleView = view.findViewById(R.id.shopTitle);
		titleView.setText(titles[pos]);
		TextView addressView = view.findViewById(R.id.shopAddress);
		addressView.setText(addresses[pos]);
		TextView ratingView = view.findViewById(R.id.shopRating);
		ratingView.setText(ratings[pos]);
		return view;
	}
}

