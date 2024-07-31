package com.example.lioneats.dtos;

import com.google.gson.annotations.SerializedName;

public class ReviewDTO {

	@SerializedName("author_name")
	private String authorName;

	@SerializedName("rating")
	private double rating;

	@SerializedName("text")
	private String text;

	public String getAuthorName() {
		return authorName;
	}

	public void setAuthorName(String authorName) {
		this.authorName = authorName;
	}

	public double getRating() {
		return rating;
	}

	public void setRating(double rating) {
		this.rating = rating;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}
}

