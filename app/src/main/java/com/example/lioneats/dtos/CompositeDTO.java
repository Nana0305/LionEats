package com.example.lioneats.dtos;

import com.example.lioneats.models.UserDTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CompositeDTO {

	private UserDTO userDTO;
	private UserLocationDTO userLocationDTO;

}