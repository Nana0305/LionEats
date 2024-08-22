package com.example.lioneats.dtos;

import java.util.Objects;

public class DishDTO {
	private int id;
	private String dishDetailName;
	private String imageUrl;

	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		DishDTO dish = (DishDTO) o;
		return id == dish.id;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

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
		return "DishDTO{" +
				"id=" + id +
				", dish='" + dishDetailName + '\'' +
				", imageUrl='" + imageUrl + '\'' +
				'}';
	}
}