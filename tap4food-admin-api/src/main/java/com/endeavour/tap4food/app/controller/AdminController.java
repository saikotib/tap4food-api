package com.endeavour.tap4food.app.controller;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import javax.validation.Valid;

import org.bson.BsonBinarySubType;
import org.bson.types.Binary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.endeavour.tap4food.app.exception.custom.TFException;
import com.endeavour.tap4food.app.model.Access;
import com.endeavour.tap4food.app.model.Admin;
import com.endeavour.tap4food.app.model.AdminDashboardData;
import com.endeavour.tap4food.app.model.AdminRole;
import com.endeavour.tap4food.app.model.BusinessUnit;
import com.endeavour.tap4food.app.model.FoodCourt;
import com.endeavour.tap4food.app.model.FoodStall;
import com.endeavour.tap4food.app.model.Merchant;
import com.endeavour.tap4food.app.model.RoleConfiguration;
import com.endeavour.tap4food.app.model.Subscription;
import com.endeavour.tap4food.app.model.admin.AboutUs;
import com.endeavour.tap4food.app.response.dto.FoodCourtResponse;
import com.endeavour.tap4food.app.response.dto.MerchantFoodStall;
import com.endeavour.tap4food.app.response.dto.ResponseHolder;
import com.endeavour.tap4food.app.service.AdminService;
import com.endeavour.tap4food.app.util.AvatarImage;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

	@Autowired
	AdminService adminService;

	@RequestMapping(value = "/update-foodstall-status", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> updateFoodstallStatus(@RequestParam Long merchantUniqueId,
			@RequestParam Long foodStallId,
			@RequestParam String status) throws TFException {

		FoodStall foodstall = adminService.updateFoodstallStatus(status, foodStallId, merchantUniqueId);

		ResponseHolder response = ResponseHolder.builder().data(foodstall).status("success")
				.timestamp(String.valueOf(LocalDateTime.now())).build();

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
			MerchantFoodStall merchantFoodstall = adminService.createMerchant(merchant);

			if (Objects.nonNull(merchantFoodstall)) {

				ResponseHolder response = ResponseHolder.builder().status("success")
						.timestamp(String.valueOf(LocalDateTime.now())).data(merchantFoodstall).build();

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

		List<MerchantFoodStall> merchants = adminService.fetchMerchants();

		ResponseHolder response = ResponseHolder.builder().status("success")
				.timestamp(String.valueOf(LocalDateTime.now())).data(merchants).build();

		return new ResponseEntity<ResponseHolder>(response, HttpStatus.OK);

	}
	
	@RequestMapping(value = "/fetch-merchant-requests", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> fetchMerchantRequests() {

		List<MerchantFoodStall> merchants = adminService.fetchMerchantRequests();

		ResponseHolder response = ResponseHolder.builder().status("success")
				.timestamp(String.valueOf(LocalDateTime.now())).data(merchants).build();

		return new ResponseEntity<ResponseHolder>(response, HttpStatus.OK);

	}

	@RequestMapping(value = "/add-business-unit", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> saveBusinessUnits(@Valid @RequestBody BusinessUnit businessUnit) {

		BusinessUnit businessUniRes = adminService.saveBusinessUnits(businessUnit);

		ResponseEntity<ResponseHolder> response = null;
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

		ResponseEntity<ResponseHolder> response = null;
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

	@RequestMapping(value = "/bunit/{bu-id}/delete", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> updateBusinessUnits(@Valid @PathVariable("bu-id") String businessUnitId) {

		boolean flag = adminService.deleteBusinessUnitById(businessUnitId);

		ResponseEntity<ResponseHolder> response = null;
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
	public ResponseEntity<ResponseHolder> getBusinessUnits(
			@RequestBody(required = false) Map<String, Object> filterMap) {

		Optional<List<BusinessUnit>> businessUnitRes = adminService.getBusinessUnits(filterMap);

		ResponseEntity<ResponseHolder> response = null;
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

	@RequestMapping(value = "/bunit/{bu-id}/upload-logo", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> uploadLogo(@Valid @PathVariable("bu-id") Long buId,
			@RequestParam(value = "logo", required = true) MultipartFile logo) {

		Optional<BusinessUnit> businessUnitRes = adminService.uploadLogo(buId, logo);

		ResponseEntity<ResponseHolder> response = null;
		if (businessUnitRes.isPresent()) {
			response = ResponseEntity.ok(ResponseHolder.builder().status("Logo uploaded successfully")
					.timestamp(String.valueOf(LocalDateTime.now())).data(businessUnitRes).build());
		} else {
			response = ResponseEntity
					.ok(ResponseHolder.builder().status("Error").timestamp(String.valueOf(LocalDateTime.now()))
							.data("Error occurred while uploading Logo").build());
		}

		return response;

	}

	/* Food Court End Points */

	@RequestMapping(value = "/bunit/{bu-id}/add-food-court", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> addFoodCourt(@Valid @PathVariable("bu-id") Long buId,
			@RequestBody FoodCourt foodCourt) throws TFException {

		Optional<FoodCourt> foodCourtRes = adminService.addFoodCourt(buId, foodCourt);

		ResponseEntity<ResponseHolder> response = null;
		if (foodCourtRes.isPresent()) {
			response = ResponseEntity.ok(ResponseHolder.builder().status("Food Court saved successfully")
					.timestamp(String.valueOf(LocalDateTime.now())).data(foodCourtRes).build());
		} else {
			response = ResponseEntity
					.ok(ResponseHolder.builder().status("Error").timestamp(String.valueOf(LocalDateTime.now()))
							.data("Error occurred while saving Food Court").build());
		}

		return response;

	}

	@RequestMapping(value = "/food-court/{fc-id}/update-food-court ", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> updateFoodCourt(@Valid @PathVariable("fc-id") Long foodCourtId,
			@RequestBody FoodCourt foodCourt) {

		Optional<FoodCourt> foodCourtRes = adminService.updateFoodCourt(foodCourtId, foodCourt);

		ResponseEntity<ResponseHolder> response = null;
		if (foodCourtRes.isPresent()) {
			response = ResponseEntity.ok(ResponseHolder.builder().status("Food Court saved successfully")
					.timestamp(String.valueOf(LocalDateTime.now())).data(foodCourtRes).build());
		} else {
			response = ResponseEntity
					.ok(ResponseHolder.builder().status("Error").timestamp(String.valueOf(LocalDateTime.now()))
							.data("Error occurred while saving Food Court").build());
		}

		return response;

	}

	@RequestMapping(value = "/food-court/{fc-id}/upload-logo", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> uploadFoodCourtLogo(@Valid @PathVariable("fc-id") Long foodCourtId,
			@RequestParam(value = "logo", required = true) MultipartFile logo) {

		Optional<FoodCourt> foodCourt = adminService.uploadFoodCourtLogo(foodCourtId, logo);

		ResponseEntity<ResponseHolder> response = null;
		if (foodCourt.isPresent()) {
			response = ResponseEntity.ok(ResponseHolder.builder().status("Food Court Logo uploaded successfully")
					.timestamp(String.valueOf(LocalDateTime.now())).data(foodCourt).build());
		} else {
			response = ResponseEntity
					.ok(ResponseHolder.builder().status("Error").timestamp(String.valueOf(LocalDateTime.now()))
							.data("Error occurred while uploading Food Court Logo").build());
		}

		return response;

	}

	@RequestMapping(value = "/food-court/{fc-id}/get", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> deleteFoodCourt(@Valid @PathVariable("fc-id") Long foodCourtId) {

		Optional<FoodCourt> foodCourt = adminService.getFoodCourtById(foodCourtId);

		ResponseEntity<ResponseHolder> response = null;
		if (foodCourt.isPresent()) {
			response = ResponseEntity.ok(ResponseHolder.builder().status("Food Court detailes retreived successfully")
					.timestamp(String.valueOf(LocalDateTime.now())).data(foodCourt).build());
		} else {
			response = ResponseEntity
					.ok(ResponseHolder.builder().status("Error").timestamp(String.valueOf(LocalDateTime.now()))
							.data("Error occurred while retreiving Food Court detailes").build());
		}

		return response;

	}
	
	@RequestMapping(value = "/get-food-courts", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> getFoodCourts() {

		List<FoodCourtResponse> foodCourts = adminService.getFoodCourts();

		ResponseEntity<ResponseHolder> response = ResponseEntity.ok(ResponseHolder.builder().status("Food Court detailes retreived successfully")
					.timestamp(String.valueOf(LocalDateTime.now())).data(foodCourts).build());
		
		return response;

	}

	@RequestMapping(value = "/food-court/{fc-id}/delete", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> getFoodCourt(@Valid @PathVariable("fc-id") Long foodCourtId) {

		boolean flag = adminService.deleteFoodCourtId(foodCourtId);

		ResponseEntity<ResponseHolder> response = null;
		if (flag) {
			response = ResponseEntity.ok(ResponseHolder.builder().status("success")
					.timestamp(String.valueOf(LocalDateTime.now())).data("Food Court Deleted successfully").build());
		} else {
			response = ResponseEntity
					.ok(ResponseHolder.builder().status("Error").timestamp(String.valueOf(LocalDateTime.now()))
							.data("Error occurred while deleting Food Court").build());
		}

		return response;

	}

	@RequestMapping(value = "/get-dashboard-data", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> getAdminDashboardData() {

		AdminDashboardData adminDashboardData = adminService.loadAdminDashboardData();

		ResponseHolder response = ResponseHolder.builder().status("success")
				.timestamp(String.valueOf(LocalDateTime.now())).data(adminDashboardData).build();

		return new ResponseEntity<ResponseHolder>(response, HttpStatus.OK);
	}

	@RequestMapping(value = "/associate-fc-fs", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> correlateFCFS(@RequestParam("fc-id") Long foodCourtId,
			@RequestParam("fs-id") Long foodStallId) throws TFException {

		adminService.correlateFCFS(foodCourtId, foodStallId);

		ResponseHolder response = ResponseHolder.builder().status("success")
				.data("Food Court & Food stall are associated.").build();

		ResponseEntity<ResponseHolder> responseEntity = new ResponseEntity<ResponseHolder>(response, HttpStatus.OK);

		return responseEntity;
	}

	@RequestMapping(value = "/add-admin-role", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> saveAdminRole(@RequestBody AdminRole adminRole) throws TFException {

		AdminRole adminRoleRes = adminService.saveAdminRole(adminRole);

		ResponseEntity<ResponseHolder> response = null;
		if (Objects.nonNull(adminRoleRes)) {
			response = ResponseEntity.ok(ResponseHolder.builder().status("success")
					.timestamp(String.valueOf(LocalDateTime.now())).data(adminRoleRes).build());
		} else {
			throw new TFException("Error occurred while saving Admin Role");
		}

		return response;
	}

	@RequestMapping(value = "/update-admin-role", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> updateAdminRole(@RequestBody AdminRole adminRole) throws TFException {

		AdminRole adminRoleRes = adminService.saveAdminRole(adminRole);

		ResponseEntity<ResponseHolder> response = null;
		if (Objects.nonNull(adminRoleRes)) {
			response = ResponseEntity.ok(ResponseHolder.builder().status("success")
					.timestamp(String.valueOf(LocalDateTime.now())).data(adminRoleRes).build());
		} else {
			throw new TFException("Error occurred while updating Admin Role");
		}

		return response;

	}

	@RequestMapping(value = "/get-admin-roles", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> getAdminRoles() {

		List<AdminRole> adminRoleRes = adminService.getAdminRoles();

		ResponseEntity<ResponseHolder> response = null;
		if (!ObjectUtils.isEmpty(adminRoleRes)) {
			response = ResponseEntity.ok(ResponseHolder.builder().status("Admin Roles retrieved successfully")
					.timestamp(String.valueOf(LocalDateTime.now())).data(adminRoleRes).build());
		} else {
			response = ResponseEntity
					.ok(ResponseHolder.builder().status("Error").timestamp(String.valueOf(LocalDateTime.now()))
							.data("Error occurred while retrieving Admin Roles").build());
		}

		return response;

	}

	@RequestMapping(value = "/add-admin-user", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> saveAdminUser(@RequestBody Admin admin) throws TFException {

		try {
			admin.setAdminUserProfilePic(new Binary(BsonBinarySubType.BINARY, (new AvatarImage()).avatarImage()));
		} catch (IOException e) {
			throw new TFException(e.getMessage());
		}

		Admin adminUserRes = adminService.saveAdminUser(admin);

		ResponseEntity<ResponseHolder> response = null;
		if (Objects.nonNull(adminUserRes)) {
			response = ResponseEntity.ok(ResponseHolder.builder().status("Admin User saved successfully")
					.timestamp(String.valueOf(LocalDateTime.now())).data(adminUserRes).build());
		} else {
			response = ResponseEntity
					.ok(ResponseHolder.builder().status("Error").timestamp(String.valueOf(LocalDateTime.now()))
							.data("Error occurred while saving Admin User").build());
		}

		return response;

	}

	@RequestMapping(value = "/get-admin-user", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> getAdminUserByRole(@RequestParam String role) throws TFException {

		List<Admin> adminRes = adminService.getAdminUserByRole(role);

		ResponseEntity<ResponseHolder> response = null;
		if (!ObjectUtils.isEmpty(adminRes)) {
			response = ResponseEntity.ok(ResponseHolder.builder().status("success")
					.timestamp(String.valueOf(LocalDateTime.now())).data(adminRes).build());
		} else {

			throw new TFException("Error occurred while retrieving Admin User Details");
		}

		return response;

	}

	@RequestMapping(value = "/update-admin-user", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> updateAdminUser(@RequestParam long adminUserId, @RequestBody Admin admin)
			throws TFException {

		Admin adminRes = adminService.updateAdmin(admin, adminUserId);

		ResponseEntity<ResponseHolder> response = null;
		if (!ObjectUtils.isEmpty(adminRes)) {
			response = ResponseEntity.ok(ResponseHolder.builder().status("Admin User Details updated successfully")
					.timestamp(String.valueOf(LocalDateTime.now())).data(adminRes).build());
		} else {
			throw new TFException("Error occurred while updating Admin User Details");

		}

		return response;

	}

	@RequestMapping(value = "/add-admin-user-profile-pic", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> addAdminUserProfilePic(@RequestParam long adminUserId,
			@RequestBody MultipartFile adminProfilePic) throws TFException {

		Admin adminRes = adminService.addAdminUserProfilePic(adminProfilePic, adminUserId);

		ResponseEntity<ResponseHolder> response = null;
		if (!ObjectUtils.isEmpty(adminRes)) {
			response = ResponseEntity.ok(ResponseHolder.builder().status("Admin User Profile Pic added successfully")
					.timestamp(String.valueOf(LocalDateTime.now())).data(adminRes).build());
		} else {

			throw new TFException("Error occurred while adding Admin User Profile Pic");

		}

		return response;

	}

	@RequestMapping(value = "/delete-admin-user", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> deleteAdminUser(@RequestParam long adminUserId) throws TFException {

		Boolean flag = adminService.deleteAdminUser(adminUserId);

		ResponseEntity<ResponseHolder> response = null;
		if (flag) {
			response = ResponseEntity.ok(ResponseHolder.builder().status("success")
					.timestamp(String.valueOf(LocalDateTime.now())).data("Admin User deleted successfully").build());
		} else {
			throw new TFException("Error occurred while deleting Admin User");
		}

		return response;
	}

	@RequestMapping(value = "/add-admin-role-configuration", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> saveAdminRoleConfiguration(@RequestParam String roleName,
			@RequestBody List<Access> accessDetails) throws TFException {

		RoleConfiguration roleConfigurationRes = adminService.saveAdminRoleConfiguration(roleName, accessDetails);

		ResponseEntity<ResponseHolder> response = null;
		if (Objects.nonNull(roleConfigurationRes)) {
			response = ResponseEntity.ok(ResponseHolder.builder().status("success")
					.timestamp(String.valueOf(LocalDateTime.now())).data(roleConfigurationRes).build());
		} else {
			throw new TFException("Error occurred while saving Admin User");
		}

		return response;

	}
	
	@RequestMapping(value = "/update-aboutus-content", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> updateAboutUsContent(@RequestBody AboutUs request) throws TFException {

		AboutUs aboutUsData = adminService.saveAboutUsData(request);

		ResponseEntity<ResponseHolder> response = ResponseEntity.ok(ResponseHolder.builder().status("success")
					.timestamp(String.valueOf(LocalDateTime.now())).data(aboutUsData).build());

		return response;

	}

	@RequestMapping(value = "/get-aboutus-content", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> getAboutUsContent() throws TFException {

		List<AboutUs> aboutUsData = adminService.getAboutUsData();

		ResponseEntity<ResponseHolder> response = ResponseEntity.ok(ResponseHolder.builder().status("success")
					.timestamp(String.valueOf(LocalDateTime.now())).data(aboutUsData).build());

		return response;

	}
	
	@RequestMapping(value = "/get-merchant-requests", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> getMerchantRequestsData(){
		
		List<MerchantFoodStall> stalls = adminService.fetchMerchantRequests();
		ResponseEntity<ResponseHolder> response = ResponseEntity.ok(ResponseHolder.builder().status("success")
				.timestamp(String.valueOf(LocalDateTime.now())).data(stalls).build());
		
		return response;
	}
	
	@RequestMapping(value = "/get-reviewed-merchants", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> getReviewedMerchants(){
		
		List<MerchantFoodStall> stalls = adminService.fetchMerchants();

		ResponseEntity<ResponseHolder> response = ResponseEntity.ok(ResponseHolder.builder().status("success")
				.timestamp(String.valueOf(LocalDateTime.now())).data(stalls).build());
		
		return response;
	}
	
	@RequestMapping(value = "/add-subscription", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> addSubscription(@RequestBody Subscription subscription) throws TFException {

		subscription = adminService.addSubscription(subscription);

		ResponseEntity<ResponseHolder> response = ResponseEntity.ok(ResponseHolder.builder().status("success")
					.timestamp(String.valueOf(LocalDateTime.now())).data(subscription).build());

		return response;

	}
	
	@RequestMapping(value = "/get-subscription", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> getSubscription() throws TFException {

		List<Subscription> subscriptions = adminService.getExistingSubscriptions();

		ResponseEntity<ResponseHolder> response = ResponseEntity.ok(ResponseHolder.builder().status("success")
					.timestamp(String.valueOf(LocalDateTime.now())).data(subscriptions).build());

		return response;

	}
}
