package com.example.lioneats.dtos;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ShopDTO {

	@SerializedName("name")
	private String name;

	@SerializedName("formatted_address")
	private String formattedAddress;

	@SerializedName("formatted_phone_number")
	private String formattedPhoneNumber;

	@SerializedName("rating")
	private double rating;

	@SerializedName("price_level")
	private int priceLevel;

	@SerializedName("website")
	private String websiteUrl;

	@SerializedName("url")
	private String googleUrl;

	@SerializedName("user_ratings_total")
	private int userRatingsTotal;

	@SerializedName("opening_hours")
	private PlaceOpeningHoursDTO openingHours;

	@SerializedName("reviews")
	private List<ReviewDTO> reviews;

	@SerializedName("photos")
	private List<PhotoDTO> photos;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getFormattedAddress() {
		return formattedAddress;
	}

	public void setFormattedAddress(String formattedAddress) {
		this.formattedAddress = formattedAddress;
	}

	public String getFormattedPhoneNumber() {
		return formattedPhoneNumber;
	}

	public void setFormattedPhoneNumber(String formattedPhoneNumber) {
		this.formattedPhoneNumber = formattedPhoneNumber;
	}

	public double getRating() {
		return rating;
	}

	public void setRating(double rating) {
		this.rating = rating;
	}

	public int getPriceLevel() {
		return priceLevel;
	}

	public void setPriceLevel(int priceLevel) {
		this.priceLevel = priceLevel;
	}

	public String getWebsiteUrl() {
		return websiteUrl;
	}

	public void setWebsiteUrl(String websiteUrl) {
		this.websiteUrl = websiteUrl;
	}

	public int getUserRatingsTotal() {
		return userRatingsTotal;
	}

	public void setUserRatingsTotal(int userRatingsTotal) {
		this.userRatingsTotal = userRatingsTotal;
	}

	public PlaceOpeningHoursDTO getOpeningHours() {
		return openingHours;
	}

	public void setOpeningHours(PlaceOpeningHoursDTO openingHours) {
		this.openingHours = openingHours;
	}

	public List<ReviewDTO> getReviews() {
		return reviews;
	}

	public void setReviews(List<ReviewDTO> reviews) {
		this.reviews = reviews;
	}

	public List<PhotoDTO> getPhotos() {
		return photos;
	}

	public void setPhotos(List<PhotoDTO> photos) {
		this.photos = photos;
	}

	public String getGoogleUrl() {
		return googleUrl;
	}

	public void setGoogleUrl(String googleUrl) {
		this.googleUrl = googleUrl;
	}
}

