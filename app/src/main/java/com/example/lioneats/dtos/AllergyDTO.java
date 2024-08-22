package com.example.lioneats.dtos;

import java.util.List;

public class AllergyDTO {
	private Long id;
	private String name;
	private List<DishDetailDTO> dishes;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<DishDetailDTO> getDishes() {
		return dishes;
	}

	public void setDishes(List<DishDetailDTO> dishes) {
		this.dishes = dishes;
	}
}
