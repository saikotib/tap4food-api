package com.endeavour.tap4food.merchant.app.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.endeavour.tap4food.app.enums.AccountStatusEnum;
import com.endeavour.tap4food.app.exception.custom.TFException;
import com.endeavour.tap4food.app.model.FoodStall;
import com.endeavour.tap4food.app.model.FoodStallSubscription;
import com.endeavour.tap4food.app.model.FoodStallTimings;
import com.endeavour.tap4food.app.model.Subscription;
import com.endeavour.tap4food.app.model.WeekDay;
import com.endeavour.tap4food.app.model.menu.Category;
import com.endeavour.tap4food.app.model.menu.Cuisine;
import com.endeavour.tap4food.app.model.menu.CustFoodItem;
import com.endeavour.tap4food.app.model.menu.CustomizeType;
import com.endeavour.tap4food.app.model.menu.SubCategory;
import com.endeavour.tap4food.app.service.CommonService;
import com.endeavour.tap4food.app.util.DateUtil;
import com.endeavour.tap4food.app.util.MediaConstants;
import com.endeavour.tap4food.merchant.app.repository.FoodStallRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class FoodStallService {

	@Autowired
	private FoodStallRepository foodStallRepository;
	
	@Autowired
	private CommonService commonService;
	
	@Value("${images.server}")
	private String mediaServerUrl;

	public FoodStall createFoodStall(Long merchantUniqNumber, FoodStall foodStall) throws TFException {

		String qrCodeUrl = mediaServerUrl + "/QRCodes/"+ foodStall.getFoodCourtId() +".png";
		
		foodStall.setStatus(AccountStatusEnum.REQUEST_FOR_APPROVAL.name());
		
		foodStall.setRating(4.7); // Need to make it dynamic
		foodStall.setQrCode(qrCodeUrl);
		
		foodStall.setCreatedDate(DateUtil.getToday());
		
		foodStallRepository.createNewFoodStall(merchantUniqNumber, foodStall);

		commonService.createMediaFolderStructure(merchantUniqNumber, foodStall.getFoodStallId());
		return foodStall;
	}
	
	public FoodStall updateFoodStall(FoodStall foodStall) throws TFException {

		foodStallRepository.updateFoodStall(foodStall);

		return foodStall;
	}
	
	public FoodStall updateFoodstallStatus(Long foodStallId, String status) throws TFException {
		
		return foodStallRepository.updateFoodstallStatus(foodStallId, status);
	}
	
	public FoodStall updateFoodstallOpenStatus(Long foodStallId, boolean openStatus) throws TFException {
		
		return foodStallRepository.updateFoodstallOpenStatus(foodStallId, openStatus);
	}

	public void addCategory(Long fsId, Category category) throws TFException {

		foodStallRepository.saveCategory(fsId, category);
	}

	public void addSubCategory(Long fsId,  SubCategory subCategory) throws TFException {
		foodStallRepository.saveSubCategory(fsId, subCategory);
	}

	public void editCategory(Long fsId, Category category) throws TFException {

		foodStallRepository.updateCategory(fsId, category, false);
	}

	public void editSubCategory(Long fsId, SubCategory subCategory) throws TFException {
		foodStallRepository.updateSubCategory(fsId, subCategory, false);
	}

	public void removeCategory(Long fsId, Category category) throws TFException {
		foodStallRepository.removeCategory(fsId, category);
	}

	public void removeSubCategory(Long fsId, SubCategory subCategory) throws TFException {
		foodStallRepository.removeSubCategory(fsId, subCategory);
	}

	public Category toggleCategory(Long fsId, Category category) throws TFException {
		return foodStallRepository.updateCategory(fsId, category, true);
	}

	public SubCategory toggleSubCategory(Long fsId,  SubCategory subCategory) throws TFException {

		return foodStallRepository.updateSubCategory(fsId, subCategory, true);
	}

	public List<Category> getAllCategories(Long fsId) throws TFException {
		Optional<List<Category>> categoryId = foodStallRepository.getAllCategories(fsId);
		if (categoryId.isPresent()) {

			return categoryId.get();
		} else {
			return new ArrayList<Category>();
		}
	}

	public List<SubCategory> getAllSubCategories(Long fsId) throws TFException {
		Optional<List<SubCategory>> categoriesList = foodStallRepository.getAllSubCategories(fsId);
		if (categoriesList.isPresent()) {

			return categoriesList.get();

		} else {
			return new ArrayList<SubCategory>();
		}
	}

	public void addCustomizeType(Long fsId,  CustomizeType customizeType) throws TFException {

		foodStallRepository.saveCustomizeType(fsId, customizeType);
	}
	
	public List<CustomizeType> getAllCustomiseTypes(Long fsId) throws TFException {
		Optional<List<CustomizeType>> customiseTypesData = foodStallRepository.getAllCustomiseTypes(fsId);
		
		List<CustomizeType> customiseTypes = new ArrayList<CustomizeType>();
		
		if (customiseTypesData.isPresent()) {
			
			customiseTypes = customiseTypesData.get();
		} 

		return customiseTypes;
	}
	
	public CustFoodItem addCustomizeFoodItem(Long fsId, String customiseTypeName,  CustFoodItem customiseFoodItem) throws TFException {

		return foodStallRepository.saveCustomizeFoodItem(fsId, customiseTypeName, customiseFoodItem);
	}
	
	public List<CustFoodItem> getAllCustomiseFoodItems(Long fsId) throws TFException {
		Optional<List<CustFoodItem>> customiseFoodItemData = foodStallRepository.getAllCustomiseFoodItems(fsId);
		
		List<CustFoodItem> customiseFoodItems = new ArrayList<CustFoodItem>();
		
		if (customiseFoodItemData.isPresent()) {
			
			customiseFoodItems = customiseFoodItemData.get();
		} 

		return customiseFoodItems;
	}

	public void editCustomizeType(Long fsId, CustomizeType customizeType) throws TFException {
		
		if(!StringUtils.hasText(customizeType.getId())) {
			throw new TFException("ID field is mandatory");
		}
		
		foodStallRepository.updateCustomizeType(fsId, customizeType, false);
	}
	
	public void editCustomizeFoodItem(Long fsId, CustFoodItem foodItem) throws TFException {
		
		foodStallRepository.updateCustomizeFoodItem(fsId, foodItem, false);
	}

	public void removeCustomizeType(Long fsId, CustomizeType customizeType) throws TFException {
		foodStallRepository.removeCustomizeType(fsId, customizeType);
	}

	public CustomizeType toggleCustomizeType(Long fsId,  CustomizeType customizeType) throws TFException {
		return foodStallRepository.updateCustomizeType(fsId, customizeType, true);
	}
	
	public CustFoodItem toggleCustomizeFoodItem(Long fsId,  CustFoodItem customizeFoodItem) throws TFException {
		return foodStallRepository.updateCustomizeFoodItem(fsId, customizeFoodItem, true);
	}

	public void addCuisineName(Long fsId,  Cuisine cuisine) throws TFException {
		
		foodStallRepository.saveCuisine(fsId, cuisine);
	}

	public void editCusine(Long fsId, Cuisine cuisine) throws TFException {
		foodStallRepository.updateCuisine(fsId, cuisine, false);

	}

	public void removeCustomizeType(Long fsId, Cuisine cuisine) throws TFException {
		foodStallRepository.removeCuisine(fsId, cuisine);

	}

	public Cuisine toggleCusine(Long fsId,  Cuisine cuisine) throws TFException {

		return foodStallRepository.updateCuisine(fsId, cuisine, true);
	}

	public List<Cuisine> getAllCuisines(Long fsId) throws TFException {
		Optional<List<Cuisine>> cuisines = foodStallRepository.getAllCuisines(fsId);
		if (cuisines.isPresent()) {

			return cuisines.get();
		} else {
			return new ArrayList<Cuisine>();
		}
	}
	
	public Optional<FoodStallTimings> saveFoodStallTimings(Long fsId, ArrayList<WeekDay> weekDays) throws TFException {

		FoodStallTimings foodStallTimings = new FoodStallTimings();
		
		foodStallTimings.setDays(weekDays);
		
		foodStallTimings = foodStallRepository.savefoodStallTimings(fsId, foodStallTimings, false);

		return Optional.ofNullable(foodStallTimings);
	}
	
	public FoodStall getFoodStallById(Long fsId) {
		
		FoodStall stall = foodStallRepository.getFoodStallById(fsId);
		stall.setQrCode(mediaServerUrl + "/QRCodes/" + stall.getFoodCourtId() + ".png");
		
		return stall;
	}
	
	public FoodStallTimings getFoodStallTimings(final Long fsId) throws TFException {
		
		FoodStallTimings timings = foodStallRepository.getFoodStallTimings(fsId);

		return timings;
	}
	
	public FoodStallTimings updateFoodStallTimings(final Long fsId, ArrayList<WeekDay> weekDays) throws TFException {
		
		FoodStallTimings foodStallTimings = getFoodStallTimings(fsId);
		
		if(Objects.isNull(foodStallTimings)) {
			throw new TFException("Timings are not added yet");
		}
		
		foodStallTimings.setDays(weekDays);
		
		foodStallTimings = foodStallRepository.savefoodStallTimings(fsId, foodStallTimings, true);

		return foodStallTimings;
	}
	
	public FoodStall uploadFoodStallPic(final Long fsId, List<MultipartFile> images, String type) throws TFException {

		FoodStall foodStall = foodStallRepository.getFoodStallById(fsId);
		
		if(Objects.isNull(foodStall)) {
			throw new TFException("Food stall is not found for the given food stall ID");
		}else {
			try {
				if(type.equalsIgnoreCase("FOODSTALL_PICS")) {
					
					String uploadPath = commonService.getMerhantMediaDirs().get(MediaConstants.GET_KEY_STALL_PROFILE_PIC_DIR).replaceAll(MediaConstants.IDENTIFIER_MERCHANTID, String.valueOf(foodStall.getMerchantId())).replaceAll(MediaConstants.IDENTIFIER_FSID, String.valueOf(foodStall.getFoodStallId()));
					
					Path path = Paths.get(uploadPath);
					
					Set<String> existingPics = foodStall.getFoodStallPics();
					
					if(Objects.isNull(existingPics)) {
						existingPics = new HashSet<String>();
					}
					
					for(MultipartFile inputImage : images) {
						
						File existingFile = new File(uploadPath + File.separator + inputImage.getOriginalFilename());
						
						if(existingFile.exists()) {
							if(existingFile.delete()) {
								log.info("Deleted the existing file");
							}
						}
						
						Files.copy(inputImage.getInputStream(), path.resolve(inputImage.getOriginalFilename()));
						
						log.info("Profile Image Path : " + uploadPath);
						log.info("Profile Image Name : " + inputImage.getOriginalFilename());
						
						log.info("Is Base Loc found :" + uploadPath.contains(commonService.getMediaBaseLocation()));
						
						String picLink = uploadPath.replaceAll(commonService.getMediaBaseLocation(), "").replaceAll("\\\\", "/");

						picLink = mediaServerUrl + picLink + "/" + inputImage.getOriginalFilename();
						
						log.info("profilePicLink :" + picLink);
					
						
						existingPics.add(picLink);
					}
					
					foodStall.setFoodStallPics(existingPics);
					
				}else if(type.equalsIgnoreCase("MENU_PICS")) {
					
					String uploadPath = commonService.getMerhantMediaDirs().get(MediaConstants.GET_KEY_MENU_PIC_DIR).replaceAll(MediaConstants.IDENTIFIER_MERCHANTID, String.valueOf(foodStall.getMerchantId())).replaceAll(MediaConstants.IDENTIFIER_FSID, String.valueOf(foodStall.getFoodStallId()));
					
					Path path = Paths.get(uploadPath);
					
					Set<String> existingPics = foodStall.getMenuPics();
					
					if(Objects.isNull(existingPics)) {
						existingPics = new HashSet<String>();
					}
					
					for(MultipartFile inputImage : images) {
						
						File existingFile = new File(uploadPath + File.separator + inputImage.getOriginalFilename());
						
						if(existingFile.exists()) {
							if(existingFile.delete()) {
								log.info("Deleted the existing file");
							}
						}
						
						Files.copy(inputImage.getInputStream(), path.resolve(inputImage.getOriginalFilename()));
						
						log.info("Profile Image Path : " + uploadPath);
						log.info("Profile Image Name : " + inputImage.getOriginalFilename());
						
						log.info("Is Base Loc found :" + uploadPath.contains(commonService.getMediaBaseLocation()));
						
						String picLink = uploadPath.replaceAll(commonService.getMediaBaseLocation(), "").replaceAll("\\\\", "/");

						picLink = mediaServerUrl + picLink + "/" + inputImage.getOriginalFilename();
						
						log.info("profilePicLink :" + picLink);
					
						
						existingPics.add(picLink);
					}
					
					foodStall.setMenuPics(existingPics);
					
				}				
				
			} catch (IOException e) {
				throw new TFException(e.getMessage());
			}
			
			foodStallRepository.updateFoodStallPic(foodStall);
			
			return foodStall;
		}
	}
	
	public FoodStallSubscription getFoodStallSubscriptionDetails(Long foodStallId) {
		FoodStallSubscription subscriptionDetails = foodStallRepository.getMerchantSubscriptionDetails(foodStallId);
		
		return subscriptionDetails;
	}
	
	public Subscription getSubscriptionDetails(String name) {
		
		return foodStallRepository.getSubscriptionDetails(name);
	}
	
	public FoodStallSubscription addMerchantSubscriptionDetails(FoodStallSubscription merchantSubscription) throws TFException {
		
		Subscription subscription = this.getSubscriptionDetails(merchantSubscription.getPlanName());
		
		if(Objects.isNull(subscription)) {
			throw new TFException("Invalid subscription/plan");
		}
		
		return foodStallRepository.addMerchantSubscriptionDetails(merchantSubscription);
	}
	
	public FoodStall deletePic(Long foodStallId, String picType, String picUrl) throws TFException {
		FoodStall foodstall = this.getFoodStallById(foodStallId);
		
		if(picType.equalsIgnoreCase("FOODSTALL_PIC")) {
			Set<String> stallPics = foodstall.getFoodStallPics();
			stallPics.remove(picUrl);
			
			foodstall.setFoodStallPics(stallPics);
		}else if(picType.equalsIgnoreCase("MENU_PIC")) {
			Set<String> menuPics = foodstall.getMenuPics();
			menuPics.remove(picUrl);
			
			foodstall.setMenuPics(menuPics);
		}
		
		foodStallRepository.updateFoodStallPic(foodstall);
		
		return foodstall;
	}
}
