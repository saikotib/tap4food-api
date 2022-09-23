package com.endeavour.tap4food.merchant.app.controller;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import javax.validation.Valid;

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
import com.endeavour.tap4food.app.model.FoodStall;
import com.endeavour.tap4food.app.model.FoodStallSubscription;
import com.endeavour.tap4food.app.model.FoodStallTimings;
import com.endeavour.tap4food.app.model.WeekDay;
import com.endeavour.tap4food.app.model.menu.Category;
import com.endeavour.tap4food.app.model.menu.Cuisine;
import com.endeavour.tap4food.app.model.menu.CustFoodItem;
import com.endeavour.tap4food.app.model.menu.CustomizeType;
import com.endeavour.tap4food.app.model.menu.SubCategory;
import com.endeavour.tap4food.app.response.dto.MerchantDashboardResponse;
import com.endeavour.tap4food.app.response.dto.ResponseHolder;
import com.endeavour.tap4food.app.util.ImageConstants;
import com.endeavour.tap4food.merchant.app.service.DashboardService;
import com.endeavour.tap4food.merchant.app.service.FoodStallService;

import io.swagger.annotations.Api;

@RestController
@RequestMapping("/api/foodstall")
@Api(tags = "FoodStallController", description = "FoodStallController")
public class FoodStallController {

	@Autowired
	private FoodStallService foodStallService;
	
	@Autowired
	private DashboardService dashboardService;

	@RequestMapping(value = "/create", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> createFoodStall(@RequestParam("merchant-number") Long merchantId,
			@RequestBody FoodStall foodStall) throws TFException {

		foodStallService.createFoodStall(merchantId, foodStall);

		ResponseHolder response = ResponseHolder.builder().data(foodStall).status("success")
				.timestamp(String.valueOf(LocalDateTime.now())).build();

		ResponseEntity<ResponseHolder> responseEntity = new ResponseEntity<ResponseHolder>(response, HttpStatus.OK);

		return responseEntity;
	}
	
	@RequestMapping(value = "/update", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> updateFoodStall(@RequestBody FoodStall foodStall) throws TFException {

		foodStallService.updateFoodStall(foodStall);

		ResponseHolder response = ResponseHolder.builder().data(foodStall).status("success")
				.timestamp(String.valueOf(LocalDateTime.now())).build();

		ResponseEntity<ResponseHolder> responseEntity = new ResponseEntity<ResponseHolder>(response, HttpStatus.OK);

		return responseEntity;
	}
	
	@RequestMapping(value = "/{fs-id}/update-status", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> updateFoodStallStatus(
			@PathVariable("fs-id") Long fsId,
			@RequestParam("status") String newStatus) throws TFException {

		FoodStall foodstall = foodStallService.updateFoodstallStatus(fsId, newStatus);

		ResponseHolder response = ResponseHolder.builder().data(foodstall).status("success")
				.timestamp(String.valueOf(LocalDateTime.now())).build();

		ResponseEntity<ResponseHolder> responseEntity = new ResponseEntity<ResponseHolder>(response, HttpStatus.OK);

		return responseEntity;
	}
	
	@RequestMapping(value = "/{fs-id}/update-tax/{tax}", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> updateTax(
			@PathVariable("fs-id") Long fsId,
			@PathVariable("tax") Double tax) throws TFException {

		FoodStall foodstall = foodStallService.updateTax(fsId, tax);

		ResponseHolder response = ResponseHolder.builder().data(foodstall).status("success")
				.timestamp(String.valueOf(LocalDateTime.now())).build();

		ResponseEntity<ResponseHolder> responseEntity = new ResponseEntity<ResponseHolder>(response, HttpStatus.OK);

		return responseEntity;
	}
	
	@RequestMapping(value = "/{fs-id}/get-tax", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> getTax(
			@PathVariable("fs-id") Long fsId) throws TFException {

		Double tax = foodStallService.getTax(fsId);
		
		Map<String, Double> responseMap = new HashMap<String, Double>();
		responseMap.put("tax", tax);

		ResponseHolder response = ResponseHolder.builder().data(responseMap)
				.status("success")
				.timestamp(String.valueOf(LocalDateTime.now()))
				.build();

		ResponseEntity<ResponseHolder> responseEntity = new ResponseEntity<ResponseHolder>(response, HttpStatus.OK);

		return responseEntity;
	}
	
	@RequestMapping(value = "/{fs-id}/update-open-status", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> updateFoodStallOpenStatus(
			@PathVariable("fs-id") Long fsId,
			@RequestParam("openStatus") boolean openStatus) throws TFException {

		System.out.println("Openstatus :" + openStatus);
		
		FoodStall foodstall = foodStallService.updateFoodstallOpenStatus(fsId, openStatus);

		ResponseHolder response = ResponseHolder.builder().data(foodstall).status("success")
				.timestamp(String.valueOf(LocalDateTime.now())).build();

		ResponseEntity<ResponseHolder> responseEntity = new ResponseEntity<ResponseHolder>(response, HttpStatus.OK);

		return responseEntity;
	}
	
	@RequestMapping(value = "/{fs-id}/get-details", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> getFoodStallDetails(@Valid @PathVariable("fs-id") Long fsId) {

		FoodStall foodStall = foodStallService.getFoodStallById(fsId);
		
		ResponseEntity<ResponseHolder> response = null;

		if (!ObjectUtils.isEmpty(foodStall)) {

			response = ResponseEntity.ok(ResponseHolder.builder().status("success")
					.timestamp(String.valueOf(LocalDateTime.now())).data(foodStall).build());
		} else {
			response = ResponseEntity.badRequest()
					.body(ResponseHolder.builder().status("error")
							.timestamp(String.valueOf(LocalDateTime.now())).data("Error occurred while retrieving Food Stall Timings").build());

		}
		return response;
	}

	@RequestMapping(path = "/{fs-id}/add-category", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> addCategory(@PathVariable("fs-id") Long foodStallId,
			@Valid @RequestBody Category category) throws TFException {

		foodStallService.addCategory(foodStallId, category);

		ResponseHolder response = ResponseHolder.builder().status("success").timestamp(String.valueOf(LocalDateTime.now()))
				.data(category).build();

		return new ResponseEntity<ResponseHolder>(response, HttpStatus.OK);

	}

	@RequestMapping(path = "/{fs-id}/edit-category", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> editCategory(@PathVariable("fs-id") Long foodStallId, @Valid @RequestBody Category category) throws TFException {

		foodStallService.editCategory(foodStallId, category);

		ResponseHolder response = ResponseHolder.builder().status("success")
				.timestamp(String.valueOf(LocalDateTime.now())).data(category).build();

		return new ResponseEntity<ResponseHolder>(response, HttpStatus.OK);

	}

	@RequestMapping(path = "/{fs-id}/remove-category", method = RequestMethod.DELETE, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> removeCategory(@PathVariable("fs-id") Long foodStallId, @Valid @RequestBody Category category) throws TFException {

		foodStallService.removeCategory(foodStallId, category);

		ResponseHolder response = ResponseHolder.builder().status("success")
				.timestamp(String.valueOf(LocalDateTime.now())).data(category).build();

		return new ResponseEntity<ResponseHolder>(response, HttpStatus.OK);

	}

	@RequestMapping(path = "/{fs-id}/toggle-category", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> hideCategory(@PathVariable("fs-id") Long foodStallId, @Valid @RequestBody Category category) throws TFException {

		category = foodStallService.toggleCategory(foodStallId, category);

		ResponseHolder response = ResponseHolder.builder().status("success")
				.timestamp(String.valueOf(LocalDateTime.now())).data(category).build();

		return new ResponseEntity<ResponseHolder>(response, HttpStatus.OK);

	}

	@RequestMapping(path = "/{fs-id}/add-subcategory", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> addSubCategory(@PathVariable("fs-id") Long foodStallId, @Valid @RequestBody SubCategory menuSubCategories) throws TFException {

		foodStallService.addSubCategory(foodStallId, menuSubCategories);

		ResponseHolder response = ResponseHolder.builder().status("success").timestamp(String.valueOf(LocalDateTime.now()))
				.data(menuSubCategories).build();

		return new ResponseEntity<ResponseHolder>(response, HttpStatus.OK);
	}

	@RequestMapping(path = "/{fs-id}/edit-subcategory", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> editSubCategory(@PathVariable("fs-id") Long foodStallId, @Valid @RequestBody SubCategory subCategory) throws TFException {

		foodStallService.editSubCategory(foodStallId, subCategory);

		ResponseHolder response = ResponseHolder.builder().status("success")
				.timestamp(String.valueOf(LocalDateTime.now())).data(subCategory).build();

		return new ResponseEntity<ResponseHolder>(response, HttpStatus.OK);
	}

	@RequestMapping(path = "/{fs-id}/remove-subcategory", method = RequestMethod.DELETE, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> removeSubCategory(@PathVariable("fs-id") Long foodStallId, @Valid @RequestBody SubCategory subCategory) throws TFException {

		foodStallService.removeSubCategory(foodStallId, subCategory);

		ResponseHolder response = ResponseHolder.builder().status("success")
				.timestamp(String.valueOf(LocalDateTime.now())).data(subCategory).build();

		return new ResponseEntity<ResponseHolder>(response, HttpStatus.OK);
	}

	@RequestMapping(path = "/{fs-id}/toggle-subcategory", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> hideSubCategory(@PathVariable("fs-id") Long foodStallId, @Valid @RequestBody SubCategory menuSubCategories) throws TFException {

		menuSubCategories = foodStallService.toggleSubCategory(foodStallId, menuSubCategories);

		ResponseHolder response = ResponseHolder.builder().status("success")
				.timestamp(String.valueOf(LocalDateTime.now())).data(menuSubCategories).build();

		return new ResponseEntity<ResponseHolder>(response, HttpStatus.OK);
	}

	@RequestMapping(path = "/{fs-id}/add-cuisine", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> addCuisineType(@PathVariable("fs-id") Long foodStallId, @Valid @RequestBody Cuisine cuisine) throws TFException {

		foodStallService.addCuisineName(foodStallId, cuisine);

		ResponseHolder response = ResponseHolder.builder().status("success").timestamp(String.valueOf(LocalDateTime.now()))
				.data(cuisine).build();

		return new ResponseEntity<ResponseHolder>(response, HttpStatus.OK);
	}

	@RequestMapping(path = "/{fs-id}/edit-cuisine", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> editCuisineName(@PathVariable("fs-id") Long foodStallId, @Valid @RequestBody Cuisine cuisine) throws Exception {

		foodStallService.editCusine(foodStallId, cuisine);

		ResponseHolder response = ResponseHolder.builder().status("success")
				.timestamp(String.valueOf(LocalDateTime.now())).data(cuisine).build();

		return new ResponseEntity<ResponseHolder>(response, HttpStatus.OK);
	}

	@RequestMapping(path = "/{fs-id}/remove-cuisine", method = RequestMethod.DELETE, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> removeCuisine(@PathVariable("fs-id") Long foodStallId, @Valid @RequestBody Cuisine cuisine) throws TFException {

		foodStallService.removeCustomizeType(foodStallId, cuisine);

		ResponseHolder response = ResponseHolder.builder().status("success")
				.timestamp(String.valueOf(LocalDateTime.now())).data(cuisine).build();

		return new ResponseEntity<ResponseHolder>(response, HttpStatus.OK);
	}

	@RequestMapping(path = "/{fs-id}/toggle-cuisine", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> hideCuisine(@PathVariable("fs-id") Long foodStallId, @Valid @RequestBody Cuisine cuisine) throws Exception {

		cuisine = foodStallService.toggleCusine(foodStallId, cuisine);

		ResponseHolder response = ResponseHolder.builder().status("success")
				.timestamp(String.valueOf(LocalDateTime.now())).data(cuisine).build();

		return new ResponseEntity<ResponseHolder>(response, HttpStatus.OK);
	}

	@RequestMapping(path = "/{fs-id}/add-customize-type", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> addCustomizeType(@PathVariable("fs-id") Long foodStallId, @Valid @RequestBody CustomizeType customizeType) throws TFException {

		foodStallService.addCustomizeType(foodStallId, customizeType);

		ResponseHolder response = ResponseHolder.builder().status("success").timestamp(String.valueOf(LocalDateTime.now()))
				.data(customizeType).build();

		return new ResponseEntity<ResponseHolder>(response, HttpStatus.OK);
	}

	@RequestMapping(path = "/{fs-id}/edit-customize-type", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> editCustomizeType(@PathVariable("fs-id") Long foodStallId, @Valid @RequestBody CustomizeType customizeType) throws TFException {

		foodStallService.editCustomizeType(foodStallId, customizeType);

		ResponseHolder response = ResponseHolder.builder().status("success")
				.timestamp(String.valueOf(LocalDateTime.now())).data(customizeType).build();

		return new ResponseEntity<ResponseHolder>(response, HttpStatus.OK);
	}
	
	@RequestMapping(path = "/{fs-id}/edit-customize-food-item", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> editCustomizeFoodItem(@PathVariable("fs-id") Long foodStallId, @RequestBody CustFoodItem foodItem) throws TFException {

		foodStallService.editCustomizeFoodItem(foodStallId, foodItem);

		ResponseHolder response = ResponseHolder.builder().status("success")
				.timestamp(String.valueOf(LocalDateTime.now())).data("Customise Food item is updated successfully").build();

		return new ResponseEntity<ResponseHolder>(response, HttpStatus.OK);
	}

	@RequestMapping(path = "/{fs-id}/remove-customize-type", method = RequestMethod.DELETE, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> removeCustomizeType(@PathVariable("fs-id") Long foodStallId, @Valid @RequestBody CustomizeType customizeType) throws TFException {

		foodStallService.removeCustomizeType(foodStallId, customizeType);

		ResponseHolder response = ResponseHolder.builder().status("success")
				.timestamp(String.valueOf(LocalDateTime.now())).data(customizeType).build();

		return new ResponseEntity<ResponseHolder>(response, HttpStatus.OK);
	}

	@RequestMapping(path = "/{fs-id}/toggle-customize-type", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> hideCustomizeType(@PathVariable("fs-id") Long foodStallId, @Valid @RequestBody CustomizeType customizeType) throws TFException {

		customizeType = foodStallService.toggleCustomizeType(foodStallId, customizeType);

		ResponseHolder response = ResponseHolder.builder().status("success")
				.timestamp(String.valueOf(LocalDateTime.now())).data(customizeType).build();

		return new ResponseEntity<ResponseHolder>(response, HttpStatus.OK);
	}
	
	@RequestMapping(path = "/{fs-id}/toggle-customize-fooditem", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> hideCustomizeFoodItem(@PathVariable("fs-id") Long foodStallId, @Valid @RequestBody CustFoodItem customizeFoodItem) throws TFException {

		customizeFoodItem = foodStallService.toggleCustomizeFoodItem(foodStallId, customizeFoodItem);

		ResponseHolder response = ResponseHolder.builder().status("success")
				.timestamp(String.valueOf(LocalDateTime.now())).data(customizeFoodItem).build();

		return new ResponseEntity<ResponseHolder>(response, HttpStatus.OK);
	}
	
	@RequestMapping(path = "/{fs-id}/remove-customize-fooditem", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> removeCustomizeFoodItem(@PathVariable("fs-id") Long foodStallId, @RequestParam("custFoodItemId") String custFoodItemId) throws TFException {

		foodStallService.removeCustomizeFoodItem(custFoodItemId);

		ResponseHolder response = ResponseHolder.builder().status("success")
				.timestamp(String.valueOf(LocalDateTime.now())).data("Item is deleted").build();

		return new ResponseEntity<ResponseHolder>(response, HttpStatus.OK);
	}
	
	@RequestMapping(path = "/{fs-id}/add-customize-food-item", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> addCustomizeFoodItem(@PathVariable("fs-id") Long foodStallId, String customiseTypeName, @Valid @RequestBody CustFoodItem customizeFoodItem) throws TFException {

		customizeFoodItem = foodStallService.addCustomizeFoodItem(foodStallId, customiseTypeName, customizeFoodItem);

		ResponseHolder response = ResponseHolder.builder().status("success").timestamp(String.valueOf(LocalDateTime.now()))
				.data(customizeFoodItem).build();

		return new ResponseEntity<ResponseHolder>(response, HttpStatus.OK);
	}

	@RequestMapping(value = "/{fs-id}/fetch-categories", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> getAllCategories(@PathVariable("fs-id") Long foodStallId) throws TFException {
		
		List<Category> categoryNames = foodStallService.getAllCategories(foodStallId);
		
		if (!categoryNames.isEmpty()) {
			ResponseHolder response = ResponseHolder.builder().status("success")
					.timestamp(String.valueOf(LocalDateTime.now())).data(categoryNames).build();
			return new ResponseEntity<ResponseHolder>(response, HttpStatus.OK);
		} else {
			ResponseHolder response = ResponseHolder.builder().status("error")
					.timestamp(String.valueOf(LocalDateTime.now())).data("no categories are available").build();
			return new ResponseEntity<ResponseHolder>(response, HttpStatus.BAD_REQUEST);
		}

	}

	@RequestMapping(value = "/{fs-id}/fetch-sub-categories", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> getAllSubCategories(@PathVariable("fs-id") Long foodStallId) throws TFException {
		
		List<SubCategory> subCategories = foodStallService.getAllSubCategories(foodStallId);
		
		if (!subCategories.isEmpty()) {
			ResponseHolder response = ResponseHolder.builder().status("success")
					.timestamp(String.valueOf(LocalDateTime.now())).data(subCategories).build();
			return new ResponseEntity<ResponseHolder>(response, HttpStatus.OK);
		} else {
			ResponseHolder response = ResponseHolder.builder().status("error")
					.timestamp(String.valueOf(LocalDateTime.now())).data("no subcategories available").build();
			return new ResponseEntity<ResponseHolder>(response, HttpStatus.BAD_REQUEST);
		}

	}

	@RequestMapping(value = "/{fs-id}/fetch-cuisines", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> getAllCuisines(@PathVariable("fs-id") Long foodStallId) throws TFException {
		
		List<Cuisine> cuisines = foodStallService.getAllCuisines(foodStallId);
		
		if (!cuisines.isEmpty()) {
			ResponseHolder response = ResponseHolder.builder().status("success")
					.timestamp(String.valueOf(LocalDateTime.now())).data(cuisines).build();
			return new ResponseEntity<ResponseHolder>(response, HttpStatus.OK);
		} else {
			throw new TFException("no cuisines available");
		}

	}
	
	@RequestMapping(value = "/{fs-id}/fetch-customise-types", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> getCustomiseTypes(@PathVariable("fs-id") Long foodStallId) throws TFException {
		
		List<CustomizeType> customiseTypes = foodStallService.getAllCustomiseTypes(foodStallId);
		
		if (!customiseTypes.isEmpty()) {
			ResponseHolder response = ResponseHolder.builder().status("success")
					.timestamp(String.valueOf(LocalDateTime.now())).data(customiseTypes).build();
			return new ResponseEntity<ResponseHolder>(response, HttpStatus.OK);
		} else {
			ResponseHolder response = ResponseHolder.builder().status("error")
					.timestamp(String.valueOf(LocalDateTime.now())).data("no subcategories available").build();
			return new ResponseEntity<ResponseHolder>(response, HttpStatus.BAD_REQUEST);
		}

	}
	
	@RequestMapping(value = "/{fs-id}/fetch-customise-fooditems", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> getCustomiseFoodItems(@PathVariable("fs-id") Long foodStallId) throws TFException {
		
		List<CustFoodItem> customiseFoodItem = foodStallService.getAllCustomiseFoodItems(foodStallId);
		
		if (!customiseFoodItem.isEmpty()) {
			ResponseHolder response = ResponseHolder.builder().status("success")
					.timestamp(String.valueOf(LocalDateTime.now())).data(customiseFoodItem).build();
			return new ResponseEntity<ResponseHolder>(response, HttpStatus.OK);
		} else {
			ResponseHolder response = ResponseHolder.builder().status("error")
					.timestamp(String.valueOf(LocalDateTime.now())).data("No data found").build();
			return new ResponseEntity<ResponseHolder>(response, HttpStatus.BAD_REQUEST);
		}

	}
	
	@RequestMapping(value = "/{fs-id}/add-foodstall-timings", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> saveFoodStallTimings(@Valid @PathVariable("fs-id") Long foodStallId, @RequestBody ArrayList<WeekDay> weekDay) throws TFException {

		Optional<FoodStallTimings> merchantFoodStallTimingsResponse = foodStallService.saveFoodStallTimings(foodStallId,
				weekDay);

		ResponseEntity<ResponseHolder> response = null;

		if (merchantFoodStallTimingsResponse.isPresent()) {

			response = ResponseEntity.ok(ResponseHolder.builder().status("success")
					.timestamp(String.valueOf(LocalDateTime.now())).data(merchantFoodStallTimingsResponse.get())
					.build());
		} else {
			response = ResponseEntity.badRequest()
					.body(ResponseHolder.builder().status("Error occurred while saving Food Stall Timings")
							.timestamp(String.valueOf(LocalDateTime.now())).data(merchantFoodStallTimingsResponse.get())
							.build());

		}
		return response;
	}
	
	@RequestMapping(value = "/{fs-id}/update-foodstall-timings", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> updateFoodStallTimings(
			@Valid @PathVariable("fs-id") Long fsId, @RequestBody ArrayList<WeekDay> weekDay) throws TFException {

		FoodStallTimings stallTimings = foodStallService.updateFoodStallTimings(fsId, weekDay);
		ResponseEntity<ResponseHolder> response = null;

		if (!Objects.isNull(stallTimings)) {

			response = ResponseEntity.ok(ResponseHolder.builder().status("Food Stall Timings saved succesfully")
					.timestamp(String.valueOf(LocalDateTime.now())).data(stallTimings).build());
		} else {
			response = ResponseEntity.badRequest()
					.body(ResponseHolder.builder().status("Error occurred while saving Food Stall Timings")
							.timestamp(String.valueOf(LocalDateTime.now())).data(stallTimings)
							.build());
		}
		return response;
	}

	@RequestMapping(value = "/{fs-id}/get-foodstall-timings", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> getFoodStallTimings(@Valid @PathVariable("fs-id") Long fsId) throws TFException {

		FoodStallTimings foodStallTimings = foodStallService.getFoodStallTimings(fsId);
		
		ResponseEntity<ResponseHolder> response = null;

		if (!ObjectUtils.isEmpty(foodStallTimings)) {

			response = ResponseEntity.ok(ResponseHolder.builder().status("Food Stall Timings retrieved succesfully")
					.timestamp(String.valueOf(LocalDateTime.now())).data(foodStallTimings).build());
		} else {

			throw new TFException("Error occurred while retrieving Food Stall Timings");
		}
		return response;
	}
	
	@RequestMapping(value = "/{fs-id}/upload-foodstall-pics", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> uploadProfilePic(@Valid @PathVariable("fs-id") Long fsId,
			@RequestParam(value = "pic", required = true) List<MultipartFile> pics) throws TFException {

		ResponseEntity<ResponseHolder> response = null;

		for(MultipartFile pic : pics) {
			System.out.println(pic.getSize());
			String picType = pic.getOriginalFilename().split("\\.")[1].toLowerCase();
			System.out.println(picType);
			if (!Arrays.asList(ImageConstants.IMAGE_JPEG, ImageConstants.IMAGE_PNG, ImageConstants.IMAGE_JPG)
					.contains(picType)) {

				throw new TFException("File must be an Image");
			}
		}
			
		FoodStall foodStal = foodStallService.uploadFoodStallPic(fsId, pics, "FOODSTALL_PICS");
		
		ResponseHolder responseHolder = ResponseHolder.builder()
				.status("success")
				.data(foodStal)
				.build();
		
		response = ResponseEntity.ok().body(responseHolder);
		
		return response;
	}
	
	@RequestMapping(value = "/{fs-id}/upload-menu-pics", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> uploadMenuPic(@Valid @PathVariable("fs-id") Long fsId,
			@RequestParam(value = "pic", required = true) List<MultipartFile> pics) throws TFException {

		ResponseEntity<ResponseHolder> response = null;

		for(MultipartFile pic : pics) {
			System.out.println(pic.getSize());
			String picType = pic.getOriginalFilename().split("\\.")[1].toLowerCase();
			System.out.println(picType);
			if (!Arrays.asList(ImageConstants.IMAGE_JPEG, ImageConstants.IMAGE_PNG, ImageConstants.IMAGE_JPG)
					.contains(picType)) {

				throw new TFException("File must be an Image");
			}
		}
			
		FoodStall foodStall = foodStallService.uploadFoodStallPic(fsId, pics, "MENU_PICS");
		
		ResponseHolder responseHolder = ResponseHolder.builder()
				.status("success")
				.data(foodStall)
				.build();
		
		response = ResponseEntity.ok().body(responseHolder);
		
		return response;
	}
	
	@RequestMapping(value = "/{fs-id}/delete-pic", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> deletePic(@Valid @PathVariable("fs-id") Long fsId,
			@RequestParam(value = "picType", required = true) String picType,
			@RequestParam(value = "picUrl", required = true) String picUrl) throws TFException {

		ResponseEntity<ResponseHolder> response = null;

		FoodStall foodStall = foodStallService.deletePic(fsId, picType, picUrl);
		
		ResponseHolder responseHolder = ResponseHolder.builder()
				.status("success")
				.data(foodStall)
				.build();
		
		response = ResponseEntity.ok().body(responseHolder);
		
		return response;
	}
	
	@RequestMapping(value = "/save-foodstall-subscription-details", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> updateMerchantSubscriptionDetails(@Valid @RequestBody FoodStallSubscription foodstallSubscriptionDetails) throws TFException {

		foodstallSubscriptionDetails = foodStallService.addMerchantSubscriptionDetails(foodstallSubscriptionDetails);
		
		ResponseHolder responseHolder = ResponseHolder.builder()
				.status("OK")
				.data(foodstallSubscriptionDetails)
				.build();
		
		ResponseEntity<ResponseHolder> response = new ResponseEntity<ResponseHolder>(responseHolder, HttpStatus.OK);
		
		return response;
	}
	
	@RequestMapping(value = "/get-foodstall-subscription-details", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> getMerchantSubscriptionDetails(@RequestParam("foodstallId") Long fsId) throws TFException {

		FoodStallSubscription subscriptionDetails = foodStallService.getFoodStallSubscriptionDetails(fsId);
		
		ResponseHolder responseHolder = ResponseHolder.builder()
				.status("OK")
				.data(subscriptionDetails)
				.build();
		
		ResponseEntity<ResponseHolder> response = new ResponseEntity<ResponseHolder>(responseHolder, HttpStatus.OK);
		
		return response;
	}
	
	@RequestMapping(value = "/get-dashboard", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> getDashboardData(@RequestParam("foodstallId") Long fsId) throws TFException {


		MerchantDashboardResponse dashboardResponse = dashboardService.getDashboardData(fsId);
		
		ResponseHolder responseHolder = ResponseHolder.builder()
				.status("OK")
				.data(dashboardResponse)
				.build();
		
		ResponseEntity<ResponseHolder> response = new ResponseEntity<ResponseHolder>(responseHolder, HttpStatus.OK);
		
		return response;
	}
}
