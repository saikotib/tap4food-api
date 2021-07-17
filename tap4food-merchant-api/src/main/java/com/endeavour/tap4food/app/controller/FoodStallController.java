package com.endeavour.tap4food.app.controller;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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

import com.endeavour.tap4food.app.exception.custom.TFException;
import com.endeavour.tap4food.app.model.FoodStall;
import com.endeavour.tap4food.app.model.FoodStallTimings;
import com.endeavour.tap4food.app.model.WeekDay;
import com.endeavour.tap4food.app.model.menu.Category;
import com.endeavour.tap4food.app.model.menu.Cuisine;
import com.endeavour.tap4food.app.model.menu.CustomizeType;
import com.endeavour.tap4food.app.model.menu.SubCategory;
import com.endeavour.tap4food.app.response.dto.ResponseHolder;
import com.endeavour.tap4food.app.service.FoodStallService;

@RestController
@RequestMapping("/api/foodstall")
public class FoodStallController {

	@Autowired
	private FoodStallService foodStallService;

	@RequestMapping(value = "/create", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> createFoodStall(@RequestParam("merchant-number") Long merchantId,
			@RequestBody FoodStall foodStall) throws TFException {

		foodStallService.createFoodStall(merchantId, foodStall);

		ResponseHolder response = ResponseHolder.builder().data(foodStall).status("success")
				.timestamp(String.valueOf(LocalDateTime.now())).build();

		ResponseEntity<ResponseHolder> responseEntity = new ResponseEntity<ResponseHolder>(response, HttpStatus.OK);

		return responseEntity;
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

	@RequestMapping(path = "/{fs-id}/remove-category", method = RequestMethod.DELETE)
	public ResponseEntity<ResponseHolder> removeCategory(@PathVariable("fs-id") Long foodStallId, @Valid @RequestBody Category category) throws TFException {

		foodStallService.removeCategory(foodStallId, category);

		ResponseHolder response = ResponseHolder.builder().status("success")
				.timestamp(String.valueOf(LocalDateTime.now())).data(category).build();

		return new ResponseEntity<ResponseHolder>(response, HttpStatus.OK);

	}

	@RequestMapping(path = "/{fs-id}/toggle-visible-category", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> hideCategory(@PathVariable("fs-id") Long foodStallId, @Valid @RequestBody Category category) throws TFException {

		foodStallService.hideCategory(foodStallId, category);

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

	@RequestMapping(path = "/{fs-id}/remove-subcategory", method = RequestMethod.DELETE)
	public ResponseEntity<ResponseHolder> removeSubCategory(@PathVariable("fs-id") Long foodStallId, @Valid @RequestBody SubCategory subCategory) throws TFException {

		foodStallService.removeSubCategory(foodStallId, subCategory);

		ResponseHolder response = ResponseHolder.builder().status("success")
				.timestamp(String.valueOf(LocalDateTime.now())).data(subCategory).build();

		return new ResponseEntity<ResponseHolder>(response, HttpStatus.OK);
	}

	@RequestMapping(path = "/{fs-id}/toggle-visible-subcategory", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> hideSubCategory(@PathVariable("fs-id") Long foodStallId, @Valid @RequestBody SubCategory menuSubCategories) throws TFException {

		foodStallService.hideSubCategory(foodStallId, menuSubCategories);

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

	@RequestMapping(path = "/{fs-id}/remove-cuisine", method = RequestMethod.DELETE)
	public ResponseEntity<ResponseHolder> removeCuisine(@PathVariable("fs-id") Long foodStallId, @Valid @RequestBody Cuisine cuisine) throws TFException {

		foodStallService.removeCustomizeType(foodStallId, cuisine);

		ResponseHolder response = ResponseHolder.builder().status("success")
				.timestamp(String.valueOf(LocalDateTime.now())).data(cuisine).build();

		return new ResponseEntity<ResponseHolder>(response, HttpStatus.OK);
	}

	@RequestMapping(path = "/{fs-id}/toggle-visible-cuisine", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> hideCuisine(@PathVariable("fs-id") Long foodStallId, @Valid @RequestBody Cuisine cuisine) throws Exception {

		foodStallService.hideCustomizeType(foodStallId, cuisine);

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

	@RequestMapping(path = "/{fs-id}/remove-customize-type", method = RequestMethod.DELETE)
	public ResponseEntity<ResponseHolder> removeCustomizeType(@PathVariable("fs-id") Long foodStallId, @Valid @RequestBody CustomizeType customizeType) throws TFException {

		foodStallService.removeCustomizeType(foodStallId, customizeType);

		ResponseHolder response = ResponseHolder.builder().status("success")
				.timestamp(String.valueOf(LocalDateTime.now())).data(customizeType).build();

		return new ResponseEntity<ResponseHolder>(response, HttpStatus.OK);
	}

	@RequestMapping(path = "/{fs-id}/toggle-visible-customize-type", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> hideCustomizeType(@PathVariable("fs-id") Long foodStallId, @Valid @RequestBody CustomizeType customizeType) throws TFException {

		foodStallService.hideCustomizeType(foodStallId, customizeType);

		ResponseHolder response = ResponseHolder.builder().status("success")
				.timestamp(String.valueOf(LocalDateTime.now())).data(customizeType).build();

		return new ResponseEntity<ResponseHolder>(response, HttpStatus.OK);
	}

	@RequestMapping(value = "/{fs-id}/fetch-categories", method = RequestMethod.GET)
	public ResponseEntity<ResponseHolder> getAllCategories(@PathVariable("fs-id") Long foodStallId) throws TFException {
		
		List<Category> categoryNames = foodStallService.getAllCategories(foodStallId);
		
		if (!categoryNames.isEmpty()) {
			ResponseHolder response = ResponseHolder.builder().status("Done")
					.timestamp(String.valueOf(LocalDateTime.now())).data(categoryNames).build();
			return new ResponseEntity<ResponseHolder>(response, HttpStatus.OK);
		} else {
			ResponseHolder response = ResponseHolder.builder().status("no categories are available")
					.timestamp(String.valueOf(LocalDateTime.now())).data(categoryNames).build();
			return new ResponseEntity<ResponseHolder>(response, HttpStatus.BAD_REQUEST);
		}

	}

	@RequestMapping(value = "/{fs-id}/fetch-sub-categories", method = RequestMethod.GET)
	public ResponseEntity<ResponseHolder> getAllSubCategories(@PathVariable("fs-id") Long foodStallId) throws TFException {
		
		List<SubCategory> subCategories = foodStallService.getAllSubCategories(foodStallId);
		
		if (!subCategories.isEmpty()) {
			ResponseHolder response = ResponseHolder.builder().status("Done")
					.timestamp(String.valueOf(LocalDateTime.now())).data(subCategories).build();
			return new ResponseEntity<ResponseHolder>(response, HttpStatus.OK);
		} else {
			ResponseHolder response = ResponseHolder.builder().status("no subcategories available")
					.timestamp(String.valueOf(LocalDateTime.now())).data(subCategories).build();
			return new ResponseEntity<ResponseHolder>(response, HttpStatus.BAD_REQUEST);
		}

	}

	@RequestMapping(value = "/{fs-id}/fetch-cuisines", method = RequestMethod.GET)
	public ResponseEntity<ResponseHolder> getAllCuisines(@PathVariable("fs-id") Long foodStallId) throws TFException {
		
		List<Cuisine> cuisines = foodStallService.getAllCuisines(foodStallId);
		
		if (!cuisines.isEmpty()) {
			ResponseHolder response = ResponseHolder.builder().status("Done")
					.timestamp(String.valueOf(LocalDateTime.now())).data(cuisines).build();
			return new ResponseEntity<ResponseHolder>(response, HttpStatus.OK);
		} else {
			ResponseHolder response = ResponseHolder.builder().status("no subcategories available")
					.timestamp(String.valueOf(LocalDateTime.now())).data(cuisines).build();
			return new ResponseEntity<ResponseHolder>(response, HttpStatus.BAD_REQUEST);
		}

	}
	
	@RequestMapping(value = "/{fs-id}/add-foodstall-timings", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseHolder> saveFoodStallTimings(@Valid @PathVariable("fs-id") Long foodStallId, @RequestBody ArrayList<WeekDay> weekDay) throws TFException {

		Optional<FoodStallTimings> merchantFoodStallTimingsResponse = foodStallService.saveFoodCourtTimings(foodStallId,
				weekDay);

		ResponseEntity<ResponseHolder> response = null;

		if (merchantFoodStallTimingsResponse.isPresent()) {

			response = ResponseEntity.ok(ResponseHolder.builder().status("Food Stall Timings saved succesfully")
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
	public ResponseEntity<ResponseHolder> getFoodStallTimings(@Valid @PathVariable("fs-id") Long fsId) {

		FoodStallTimings foodStallTimings = foodStallService.getFoodStallTimings(fsId);
		
		ResponseEntity<ResponseHolder> response = null;

		if (!ObjectUtils.isEmpty(foodStallTimings)) {

			response = ResponseEntity.ok(ResponseHolder.builder().status("Food Stall Timings retrieved succesfully")
					.timestamp(String.valueOf(LocalDateTime.now())).data(foodStallTimings).build());
		} else {
			response = ResponseEntity.badRequest()
					.body(ResponseHolder.builder().status("Error occurred while retrieving Food Stall Timings")
							.timestamp(String.valueOf(LocalDateTime.now())).data(foodStallTimings).build());

		}
		return response;
	}
	
}
