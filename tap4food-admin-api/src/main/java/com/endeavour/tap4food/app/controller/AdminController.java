package com.endeavour.tap4food.app.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.endeavour.tap4food.app.model.BusinessUnit;
import com.endeavour.tap4food.app.model.Merchant;
import com.endeavour.tap4food.app.response.dto.ResponseHolder;
import com.endeavour.tap4food.app.service.AdminService;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

	@Autowired
	AdminService adminService;

	@RequestMapping(value = "/update-merchant-status", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> updateMerchantStatus(@RequestParam Long merchantUniqueId,
			@RequestParam String status) {

		ResponseHolder response = adminService.updateMerchantStatus(status, merchantUniqueId);
		ResponseEntity<ResponseHolder> responseEntity = new ResponseEntity<ResponseHolder>(response, HttpStatus.OK);

		return responseEntity;
	}

	@RequestMapping(value = "/create-merchant", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> createMerchant(@Valid @RequestBody Merchant merchant) {

		String errorMessage = null;

		ResponseEntity<ResponseHolder> responseEntity = null;

		if (adminService.isMerchantFoundByEmail(merchant.getEmail())) {
			errorMessage = "Merchant email is already used.";

			ResponseHolder response = ResponseHolder.builder().status("error")
					.timestamp(String.valueOf(LocalDateTime.now())).data(errorMessage).build();

			responseEntity = ResponseEntity.badRequest().body(response);

		} else if (adminService.isMerchantFoundByPhoneNumber(merchant.getPhoneNumber())) {
			errorMessage = "Merchant phone number is already used.";

			ResponseHolder response = ResponseHolder.builder().status("error")
					.timestamp(String.valueOf(LocalDateTime.now())).data(errorMessage).build();

			responseEntity = ResponseEntity.badRequest().body(response);

		} else {
			merchant = adminService.createMerchant(merchant);

			if (!Objects.isNull(merchant.getId())) {

				ResponseHolder response = ResponseHolder.builder().status("success")
						.timestamp(String.valueOf(LocalDateTime.now())).data(merchant).build();

				responseEntity = ResponseEntity.ok().body(response);

			} else {

				errorMessage = "Error occurred during merchant creation";

				ResponseHolder response = ResponseHolder.builder().status("error")
						.timestamp(String.valueOf(LocalDateTime.now())).data(errorMessage).build();

				responseEntity = ResponseEntity.ok().body(response);

			}
		}

		return responseEntity;
	}

	@RequestMapping(value = "/fetch-merchants", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> fetchMerchants() {

		List<Merchant> merchants = adminService.fetchMerchants();

		ResponseHolder response = ResponseHolder.builder().status("success")
				.timestamp(String.valueOf(LocalDateTime.now())).data(merchants).build();

		return new ResponseEntity<ResponseHolder>(response, HttpStatus.OK);

	}

	@RequestMapping(value = "/add-business-unit", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> saveBusinessUnits(@Valid @RequestBody BusinessUnit businessUnit) {

		BusinessUnit businessUniRes = adminService.saveBusinessUnits(businessUnit);

		ResponseEntity response = null;
		if (Objects.nonNull(businessUniRes)) {
			response = ResponseEntity.ok(ResponseHolder.builder().status("Business Unit saved successfully")
					.timestamp(String.valueOf(LocalDateTime.now())).data(businessUniRes).build());
		} else {
			response = ResponseEntity
					.ok(ResponseHolder.builder().status("Error").timestamp(String.valueOf(LocalDateTime.now()))
							.data("Error occurred while saving Business Unit").build());
		}

		return response;

	}

	@RequestMapping(value = "/update-business-unit", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> updateBusinessUnits(@Valid @RequestBody BusinessUnit businessUnit) {

		BusinessUnit businessUniRes = adminService.saveBusinessUnits(businessUnit);

		ResponseEntity response = null;
		if (Objects.nonNull(businessUniRes)) {
			response = ResponseEntity.ok(ResponseHolder.builder().status("Business Unit updated successfully")
					.timestamp(String.valueOf(LocalDateTime.now())).data(businessUniRes).build());
		} else {
			response = ResponseEntity
					.ok(ResponseHolder.builder().status("Error").timestamp(String.valueOf(LocalDateTime.now()))
							.data("Error occurred while updating Business Unit").build());
		}

		return response;

	}
	
	
	
	@RequestMapping(value = "/delete-business-unit", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> updateBusinessUnits(@Valid @RequestParam String businessUnitId) {

		boolean flag = adminService.deleteBusinessUnitById(businessUnitId);

		ResponseEntity response = null;
		if (flag) {
			response = ResponseEntity.ok(ResponseHolder.builder().status("success")
					.timestamp(String.valueOf(LocalDateTime.now())).data("Business Unit Deleted successfully").build());
		} else {
			response = ResponseEntity
					.ok(ResponseHolder.builder().status("Error").timestamp(String.valueOf(LocalDateTime.now()))
							.data("Error occurred while deleting Business Unit").build());
		}

		return response;

	}
	
	
	
	
	@RequestMapping(value = "/get-business-unit", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> getBusinessUnits(@RequestBody(required = false) Map<String,Object> businessObject) {


		Optional<List<BusinessUnit>> businessUnitRes = adminService.getBusinessUnits(businessObject);

		ResponseEntity response = null;
		if (businessUnitRes.isPresent()) {
			response = ResponseEntity.ok(ResponseHolder.builder().status("Business Units retrieved successfully")
					.timestamp(String.valueOf(LocalDateTime.now())).data(businessUnitRes).build());
		} else {
			response = ResponseEntity
					.ok(ResponseHolder.builder().status("Error").timestamp(String.valueOf(LocalDateTime.now()))
							.data("Error occurred while retrieving Business Units").build());
		}

		return response;

	}
}
