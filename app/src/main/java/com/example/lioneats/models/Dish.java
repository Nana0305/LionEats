package com.example.lioneats.models;

public class Dish {
	private int id;
	private String dishDetailName;
	private String imageUrl;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}

	public String getDishDetailName() {
		return dishDetailName;
	}

	public void setDishDetailName(String dishDetailName) {
		this.dishDetailName = dishDetailName;
	}

	@Override
	public String toString() {
		return "Dish{" +
				"id=" + id +
				", dish='" + dishDetailName + '\'' +
				", imageUrl='" + imageUrl + '\'' +
				'}';
	}
}
