package com.example.lioneats.dtos;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class PlaceOpeningHoursDTO {

	@SerializedName("weekday_text")
	private List<String> weekdayText;

	public List<String> getWeekdayText() {
		return weekdayText;
	}

	public void setWeekdayText(List<String> weekdayText) {
		this.weekdayText = weekdayText;
	}
}
