package com.example.lioneats.models;

import java.util.List;

public class User {
	private String name;
	private String username;
	private String password;
	private String email;
	private Integer age;
	private String gender;
	private String country;
	private List<String> dishPreferences;
	private String preferredBudget;
	private List<String> allergies;
	private boolean likesSpicy;

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

	public Integer getAge() {
		return age;
	}

	public void setAge(Integer age) {
		this.age = age;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public List<String> getDishPreferences() {
		return dishPreferences;
	}

	public void setDishPreferences(List<String> dishPreferences) {
		this.dishPreferences = dishPreferences;
	}

	public String getPreferredBudget() {
		return preferredBudget;
	}

	public void setPreferredBudget(String preferredBudget) {
		this.preferredBudget = preferredBudget;
	}

	public List<String> getAllergies() {
		return allergies;
	}

	public void setAllergies(List<String> allergies) {
		this.allergies = allergies;
	}

	public boolean isLikesSpicy() {
		return likesSpicy;
	}

	public void setLikesSpicy(boolean likesSpicy) {
		this.likesSpicy = likesSpicy;
	}
}
