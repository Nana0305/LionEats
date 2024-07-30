package model;

import java.util.List;

public class Dish {
	private String dishName;
	private List<String> dishAllergies;
	private String dishIngredients;
	private String dishHistory;
	private String dishDescription;

	public String getDishName() {
		return dishName;
	}

	public void setDishName(String dishName) {
		this.dishName = dishName;
	}

	public List<String> getDishAllergies() {
		return dishAllergies;
	}

	public void setDishAllergies(List<String> dishAllergies) {
		this.dishAllergies = dishAllergies;
	}

	public String getDishHistory() {
		return dishHistory;
	}

	public void setDishHistory(String dishHistory) {
		this.dishHistory = dishHistory;
	}

	public String getDishDescription() {
		return dishDescription;
	}

	public void setDishDescription(String dishDescription) {
		this.dishDescription = dishDescription;
	}

	public String getDishIngredients() {
		return dishIngredients;
	}

	public void setDishIngredients(String dishIngredients) {
		this.dishIngredients = dishIngredients;
	}
}
