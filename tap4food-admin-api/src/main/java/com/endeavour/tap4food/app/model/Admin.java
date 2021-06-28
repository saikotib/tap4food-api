package com.endeavour.tap4food.app.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Admin {
	
	private List<Customer> customers;
	
	private List<Merchant> merchants;

}
