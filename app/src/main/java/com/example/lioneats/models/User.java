package com.example.lioneats.models;

import java.util.List;

public class User {
	private String name;
	private String username;
	private String password;
	private String email;
	private int ageGroup;
	private boolean isMale;
	private String country;
	private List<String> dishPref;
	private boolean likesSpicy;
	private String budget;
	private List<String> allergy;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public int getAgeGroup() {
		return ageGroup;
	}

	public void setAgeGroup(int ageGroup) {
		this.ageGroup = ageGroup;
	}

	public boolean isMale() {
		return isMale;
	}

	public void setMale(boolean male) {
		isMale = male;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public List<String> getDishPref() {
		return dishPref;
	}

	public void setDishPref(List<String> dishPref) {
		this.dishPref = dishPref;
	}

	public boolean isLikesSpicy() {
		return likesSpicy;
	}

	public void setLikesSpicy(boolean likesSpicy) {
		this.likesSpicy = likesSpicy;
	}

	public String getBudget() {
		return budget;
	}

	public void setBudget(String budget) {
		this.budget = budget;
	}

	public List<String> getAllergy() {
		return allergy;
	}

	public void setAllergy(List<String> allergy) {
		this.allergy = allergy;
	}
}
