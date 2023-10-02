package com.endeavour.tap4food.merchant.app.service;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.csv.QuoteMode;
import org.bson.BsonBinarySubType;
import org.bson.types.Binary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.endeavour.tap4food.app.exception.custom.TFException;
import com.endeavour.tap4food.app.model.FoodStall;
import com.endeavour.tap4food.app.model.fooditem.AddOns;
import com.endeavour.tap4food.app.model.fooditem.FoodItem;
import com.endeavour.tap4food.app.model.fooditem.FoodItemCustomiseDetails;
import com.endeavour.tap4food.app.model.fooditem.FoodItemCustomizationPricing;
import com.endeavour.tap4food.app.model.fooditem.FoodItemDirectOffer;
import com.endeavour.tap4food.app.model.fooditem.FoodItemPricing;
import com.endeavour.tap4food.app.request.dto.FoodItemEditRequest;
import com.endeavour.tap4food.app.response.dto.CategorisedFoodItemsResponse;
import com.endeavour.tap4food.app.response.dto.FoodItemDataToEdit;
import com.endeavour.tap4food.app.response.dto.FoodItemResponse;
import com.endeavour.tap4food.merchant.app.repository.FoodItemRepository;
import com.endeavour.tap4food.merchant.app.repository.FoodStallRepository;
import com.endeavour.tap4food.merchant.app.repository.PreProcessorRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class FoodItemService {

	@Autowired
	private FoodItemRepository foodItemRepository;

	@Autowired
	private FoodStallRepository foodStallRepository;

	@Autowired
	private MenuCacheService menuCacheService;

	@Autowired
	private PreProcessorRepository preProcessorRepository;

	private Map<Long, List<FoodItemResponse>> itemsMap = new HashMap<Long, List<FoodItemResponse>>();

	private List<FoodItemResponse> getItemsFromCache(Long fsId) {

		return itemsMap.get(fsId);
	}

	private void addFoodItemsToCache(Long foodStallId) {
		ExecutorService executor = Executors.newSingleThreadExecutor();
		executor.execute(new Runnable() {
			@Override
			public void run() {
				menuCacheService.addFoodItemsListToCache(foodStallId);
			}
		});
		executor.shutdown();
	}

	public void addFoodItem(Long merchantId, Long fsId, FoodItem foodItem) throws TFException {

		foodItem.setFoodStallId(fsId);

//		if(!foodItem.isEgg()) {
//			foodItem.setVeg(true);		
//		}

		FoodStall foodStall = foodStallRepository.getFoodStallById(fsId);

		System.out.println("FoodStall : " + foodStall);

		if (Objects.isNull(foodStall)) {
			throw new TFException("Foodstall is not found");
		} else {
			FoodItem existingFoodItem = foodItemRepository.getFoodItemByReqId(foodItem.getRequestId());

			if (!Objects.isNull(existingFoodItem)) {

				foodItem.setId(existingFoodItem.getId());
				foodItem.setPic(existingFoodItem.getPic());

				if (!StringUtils.hasText(foodItem.getSubCategory())) {
					foodItem.setSubCategory(foodItem.getCategory());
				}

				foodItemRepository.addFoodItem(foodItem);

				System.out.println("Food Item is added. Now adding to procing");
				this.addItemPricing(foodItem);
			} else {
				System.out.println("creating new item....");
			}
		}
		addToken(foodItem.getFoodItemName(), foodStall);
//		addFoodItemsToCache(fsId);
	}

	private void addToken(String itemName, FoodStall stall) {

//		String tokens[] = itemName.split("\\s+");
		Set<String> keywords = stall.getKeywords();
		if (Objects.isNull(keywords)) {
			keywords = new HashSet<String>();
		}
//		for(String token : tokens) {
//			keywords.add(token.toLowerCase());
//		}

		keywords.add(itemName);

		stall.setKeywords(keywords);

		foodStallRepository.saveKeywords(stall);
	}

	public FoodItem updateFoodItem(FoodItem item) throws TFException {

		FoodItem existingFoodItem = foodItemRepository.getFoodItem(item.getFoodItemId());

		existingFoodItem.setCategory(item.getCategory());
		existingFoodItem.setAddOn(item.isAddOn());
		existingFoodItem.setCuisine(item.getCuisine());
		existingFoodItem.setDescription(item.getDescription());
		existingFoodItem.setEgg(item.isEgg());
		existingFoodItem.setNonVeg(item.isNonVeg());
		existingFoodItem.setFoodItemName(item.getFoodItemName());
		existingFoodItem.setSubCategory(item.getSubCategory());
		existingFoodItem.setReccommended(item.isReccommended());
		existingFoodItem.setVeg(item.isVeg());
		existingFoodItem.setPrice(Double.valueOf(0));

		foodItemRepository.updateFoodItem(existingFoodItem);

		addFoodItemsToCache(existingFoodItem.getFoodStallId());

		return existingFoodItem;
	}

	private String validateEditData(FoodItemEditRequest foodItemRequest) {
		String message = null;

		if (foodItemRequest.isCustomizationFlag()) {

			List<String> custTypes = foodItemRequest.getCustomizationTypes();
			List<String> custItems = foodItemRequest.getCustomiseFoodItems();

			if (ObjectUtils.isEmpty(custTypes) || ObjectUtils.isEmpty(foodItemRequest.getCustomiseFoodItems())) {
				message = "Invalid customization data";
				return message;
			}
			Set<String> custItemsSet = new HashSet<String>();
			for (String custItem : custItems) {
				String custItemTokens[] = custItem.split("~");
				custItemsSet.add(custItemTokens[0]);
				if (!StringUtils.hasText(custItemTokens[1])) {
					message = "Invalid customization data";
					return message;
				}
			}

			if (!custTypes.containsAll(custItemsSet)) {
				message = "Invalid customization data";
				return message;
			}

//			if(custTypes.size() != foodItemRequest.getCustomiseFoodItemsDescriptions().size()) {
//				message = "Invalid customization data";
//				return message;
//			}

		}

		return message;
	}

	public FoodItem updateFoodItem(FoodItemEditRequest foodItemRequest) throws TFException {

		String validationMessage = validateEditData(foodItemRequest);

		if (StringUtils.hasText(validationMessage)) {
			throw new TFException(validationMessage);
		}

		Long fsId = foodItemRequest.getFoodStallId();

		FoodItem existingFoodItem = foodItemRepository.getFoodItem(foodItemRequest.getFoodItemId());

		existingFoodItem.setAddOn(foodItemRequest.isAddOnFlag());
		existingFoodItem.setCuisine(foodItemRequest.getCuisine());
		existingFoodItem.setDescription(foodItemRequest.getDescription());
		existingFoodItem.setEgg(foodItemRequest.isEggFlag());
		existingFoodItem.setFoodItemName(foodItemRequest.getFoodItemName());
		existingFoodItem.setSubCategory(foodItemRequest.getSubCategory());
		existingFoodItem.setReccommended(foodItemRequest.isRecomendedFlag());
		existingFoodItem.setVeg(foodItemRequest.isVegFlag());
		existingFoodItem.setTaxType(foodItemRequest.getTaxType());

		boolean isExistingItemHasCustomization = existingFoodItem.isAvailableCustomisation();

		existingFoodItem.setAvailableCustomisation(foodItemRequest.isCustomizationFlag());

		FoodItemPricing foodItemPricing = foodItemRepository.getFoodItemPricingDetails(fsId,
				foodItemRequest.getFoodItemId());
		List<FoodItemCustomizationPricing> foodItemCustomizationsList = foodItemRepository
				.getFoodItemPricingDetailsWithCustomization(fsId, foodItemRequest.getFoodItemId());
		List<FoodItem> combinationFoodItems = foodItemRepository.getCombinationFoodItems(fsId,
				foodItemRequest.getFoodItemId());

		foodItemRepository.updateFoodItem(existingFoodItem);
		foodItemRepository.deleteFoodItemExistingDataBeforeEdit(foodItemRequest, existingFoodItem.getPrice());

		if (existingFoodItem.isAvailableCustomisation()) {

			FoodItemCustomiseDetails custDetails = new FoodItemCustomiseDetails();
			custDetails.setAddOnDescription(foodItemRequest.getAddOnDescription());
			custDetails.setAddOnItemsIds(foodItemRequest.getAddOnItemsIds());
			custDetails.setAddOnSelectButton(foodItemRequest.getAddOnSelectButton());
			custDetails.setCustomiseFoodItems(foodItemRequest.getCustomiseFoodItems());
			custDetails.setCustomiseTypes(foodItemRequest.getCustomizationTypes());
			custDetails.setCustomiseFoodItemsDescriptions(foodItemRequest.getCustomiseFoodItemsDescriptions());
			custDetails.setCustomiseFoodItemsSelectButtons(foodItemRequest.getCustomiseFoodItemsSelectButtons());
			custDetails.setFoodItemDescription(foodItemRequest.getDescription());
			custDetails.setFoodItemId(foodItemRequest.getFoodItemId());
			custDetails.setFoodItemName(foodItemRequest.getFoodItemName());
			custDetails.setFoodStallId(foodItemRequest.getFoodStallId());
			custDetails.setCustomiseFoodItemsCustomerSpecifications(new ArrayList<String>());
			custDetails.setAddOnCustomerSpecification("Optional");

			foodItemRepository.addFoodItemCustomiseDetails(foodItemRequest.getFoodItemId(), custDetails);

			if (Objects.nonNull(custDetails.getId())) {
				System.out.println("Food item customisation details are saved successfully");
			}

//			this.addItemCustomizationPricing(existingFoodItem, custDetails, foodItemPricing, foodItemCustomizationsList);
			this.addItemCustomizationPricing(existingFoodItem, custDetails);

			FoodItemPricing foodItemPricingLatest = foodItemRepository
					.getFoodItemPricingDetails(foodItemRequest.getFoodStallId(), foodItemRequest.getFoodItemId());
			List<FoodItemCustomizationPricing> foodItemCustomizationsListLatest = foodItemRepository
					.getFoodItemPricingDetailsWithCustomization(foodItemRequest.getFoodStallId(),
							foodItemRequest.getFoodItemId());

//			System.out.println("Existing DATA ========================START");
//			
//			System.out.println(foodItemPricing);
//			System.out.println(foodItemCustomizationsList);
//			
//			System.out.println("Existing DATA ========================END");
//			
//			System.out.println("Latest DATA ========================START");
//			
//			System.out.println(foodItemPricingLatest);
//			System.out.println(foodItemCustomizationsListLatest);
//			
//			System.out.println("Latest DATA ========================END");

			this.updateFoodItemPrice(foodItemRequest.getFoodStallId(), foodItemPricingLatest.getId(),
					foodItemPricing.getPrice(), foodItemPricing.getPackagingPrice());

			List<FoodItem> combinationFoodItemsLatest = foodItemRepository.getCombinationFoodItems(fsId,
					foodItemRequest.getFoodItemId());

			for (FoodItem combinationItem : combinationFoodItems) {

				for (FoodItem combinationItemLatest : combinationFoodItemsLatest) {

					if (combinationItem.getCombination().equalsIgnoreCase(combinationItemLatest.getCombination())) {

						FoodItemPricing combinationFoodItemPricing = foodItemRepository.getFoodItemPricingDetails(fsId,
								combinationItemLatest.getFoodItemId());

						this.updateFoodItemPrice(fsId, combinationFoodItemPricing.getId(), combinationItem.getPrice(),
								combinationItemLatest.getPackagingPrice());

						break;
					}
				}
			}

			for (FoodItemCustomizationPricing custPrice : foodItemCustomizationsList) {

				String combination = custPrice.getCustomiseType();
				Double price = custPrice.getPrice();

				for (FoodItemCustomizationPricing custPriceLatest : foodItemCustomizationsListLatest) {

					if (custPriceLatest.getCustomiseType().equalsIgnoreCase(combination)) {

						this.updateFoodItemCustomizationPrice(fsId, custPriceLatest.getId(), price);
						break;
					}
				}
			}
		}

		addFoodItemsToCache(fsId);

		return existingFoodItem;
	}

	public void addItemPricing(FoodItem foodItem) {
		foodItemRepository.addItemPricing(foodItem, Double.valueOf(0));
	}

	public void addItemPricing(FoodItem foodItem, Double price) {
		foodItemRepository.addItemPricing(foodItem, price);
	}

	public FoodItem uploadFoodItemPics(final Long fsId, final String requestId, List<MultipartFile> images)
			throws TFException {

		FoodStall foodStall = foodStallRepository.getFoodStallById(fsId);

		if (Objects.isNull(foodStall)) {
			throw new TFException("Food stall is not found for the given food stall ID");
		} else {

			FoodItem foodItem = foodItemRepository.getFoodItemByReqId(requestId);

			List<Binary> existingPics = foodItem.getPic();

			if (Objects.isNull(existingPics)) {
				existingPics = new ArrayList<Binary>();
			}

			for (MultipartFile inputImage : images) {
				try {
					existingPics.add(new Binary(BsonBinarySubType.BINARY, inputImage.getBytes()));
				} catch (IOException e) {
					throw new TFException(e.getMessage());
				}
			}

			foodItem.setPic(existingPics);

			foodItemRepository.updateFoodItem(foodItem);

			System.out.println(">>>" + foodItem);

			return foodItem;
		}
	}

	public FoodItem uploadFoodItemPics(final Long fsId, final Long foodItemId, List<MultipartFile> images)
			throws TFException {

		FoodStall foodStall = foodStallRepository.getFoodStallById(fsId);

		if (Objects.isNull(foodStall)) {
			throw new TFException("Food stall is not found for the given food stall ID");
		} else {

			FoodItem foodItem = foodItemRepository.getFoodItem(foodItemId);

			List<Binary> existingPics = foodItem.getPic();

			if (Objects.isNull(existingPics)) {
				existingPics = new ArrayList<Binary>();
			}

			for (MultipartFile inputImage : images) {
				try {
					existingPics.add(new Binary(BsonBinarySubType.BINARY, inputImage.getBytes()));
				} catch (IOException e) {
					throw new TFException(e.getMessage());
				}
			}

			foodItem.setPic(existingPics);

			foodItemRepository.updateFoodItem(foodItem);

			System.out.println(">>>" + foodItem);

			return foodItem;
		}
	}

	public List<FoodItemResponse> getFoodItems(Long fsId) {

//		if(itemsMap.containsKey(fsId)) {
//			return getItemsFromCache(fsId);
//		}

//		PreProcessedFoodItems preProcessedData =  preProcessorRepository.getPreProcessedItems(fsId);
//		
//		if(Objects.isNull(preProcessedData)) {
//			return Collections.emptyList();
//		}else {
//			return new ArrayList(preProcessedData.getFoodItemsMapById().values());
//		}

		Logger logger = Logger.getLogger(FoodItemService.class.getName());

		List<FoodItem> foodItems = foodItemRepository.getFoodItems(fsId);
		List<FoodItemPricing> foodItemPricingDetails = foodItemRepository.getFoodItemPricingDetails(fsId);
		Map<Long, Double> pricingList = new HashMap<Long, Double>();
		for (FoodItemPricing itemP : foodItemPricingDetails) {
			pricingList.put(itemP.getFoodItemId(), itemP.getPrice());
		}
		List<FoodItemResponse> foodItemsResponseList = new ArrayList<FoodItemResponse>();

		for (FoodItem item : foodItems) {
			if (Objects.nonNull(item.getStatus()) && item.getStatus().equalsIgnoreCase("DELETED")) {
				continue;
			}
			FoodItemResponse foodItem = new FoodItemResponse();
			foodItem.setDbId(item.getId());
			foodItem.setAddOn(item.isAddOn());
			foodItem.setCategory(item.getCategory());
			foodItem.setCuisine(item.getCuisine());
			foodItem.setDescription(item.getDescription());
			foodItem.setEgg(item.isEgg());
			foodItem.setFoodItemId(item.getFoodItemId());
			foodItem.setFoodItemName(item.getFoodItemName());
			foodItem.setFoodStallId(item.getFoodStallId());
//			foodItem.setPic(item.getPic());
			foodItem.setPrice(pricingList.get(item.getFoodItemId()));
			foodItem.setRating(item.getRating());
			foodItem.setReccommended(item.isReccommended());
			foodItem.setSubCategory(item.getSubCategory());
			foodItem.setTotalReviews(item.getTotalReviews());
			foodItem.setVeg(item.isVeg());
			foodItem.setHasCustomizations(item.isAvailableCustomisation());
			foodItem.setStatus(item.getStatus());

			foodItemsResponseList.add(foodItem);
		}

		return foodItemsResponseList;

	}

	public List<CategorisedFoodItemsResponse> getCategorisedItems(Long fsId) {

		List<FoodItemResponse> items = getFoodItems(fsId);

		Map<String, List<FoodItemResponse>> itemsMap = new LinkedHashMap<String, List<FoodItemResponse>>();

		itemsMap = items.stream()
				.collect(Collectors.groupingBy(item -> !item.getCategory().equalsIgnoreCase(item.getSubCategory())
						? String.format("%s-%s", item.getCategory(), item.getSubCategory())
						: item.getCategory()));

		List<CategorisedFoodItemsResponse> responseList = new ArrayList<>();

		for (Map.Entry<String, List<FoodItemResponse>> entry : itemsMap.entrySet()) {
			CategorisedFoodItemsResponse responseDto = new CategorisedFoodItemsResponse();
			responseDto.setCategory(entry.getKey());
			responseDto.setItems(entry.getValue());

			responseList.add(responseDto);
		}

		return responseList;
	}

	public List<FoodItemResponse> getFoodItemsForOffers(Long fsId) {

		List<FoodItem> foodItems = foodItemRepository.getFoodItemsForOffers(fsId);

		List<FoodItemResponse> foodItemsResponseList = new ArrayList<FoodItemResponse>();

		for (FoodItem item : foodItems) {

			if (item.isDefaultCombination()) {
				continue;
			}

			FoodItemResponse foodItem = new FoodItemResponse();
			foodItem.setDbId(item.getId());
			foodItem.setAddOn(item.isAddOn());
			foodItem.setCategory(item.getCategory());
			foodItem.setCuisine(item.getCuisine());
			foodItem.setDescription(item.getDescription());
			foodItem.setEgg(item.isEgg());
			foodItem.setFoodItemId(item.getFoodItemId());
			foodItem.setFoodItemName(item.getFoodItemName());
			foodItem.setFoodStallId(item.getFoodStallId());
			foodItem.setPrice(foodItemRepository.getFoodItemPrice(item.getFoodItemId()));
			foodItem.setRating(item.getRating());
			foodItem.setReccommended(item.isReccommended());
			foodItem.setSubCategory(item.getSubCategory());
			foodItem.setTotalReviews(item.getTotalReviews());
			foodItem.setVeg(item.isVeg());
			foodItem.setCombination(
					StringUtils.isEmpty(item.getCombination()) ? "" : item.getCombination().replaceAll("##", " "));
			foodItemsResponseList.add(foodItem);
		}

		return foodItemsResponseList;
	}

	public List<FoodItemPricing> getFoodItemPricingDetails(Long fsId) {

		List<FoodItemPricing> pricingDetails = foodItemRepository.getFoodItemPricingDetailsV2(fsId);

		/*
		 * List<FoodItemPricing> latestPricingDetails = new
		 * ArrayList<FoodItemPricing>();
		 * 
		 * System.out.println("START :" + LocalDateTime.now());
		 * 
		 * Map<Long, FoodItem> foodItemsMap = this.getFoodItemsMap(fsId);
		 * 
		 * pricingDetails.parallelStream().forEach(pricing -> { FoodItem item = null;
		 * try { item = foodItemsMap.containsKey(pricing.getFoodItemId()) ?
		 * foodItemsMap.get(pricing.getFoodItemId()) :
		 * foodItemRepository.getFoodItem(pricing.getFoodItemId());
		 * 
		 * if(Objects.nonNull(item) && !item.isDefaultCombination()) { String name =
		 * pricing.getFoodItemName(); name = name.replaceAll("##", " ");
		 * pricing.setFoodItemName(name);
		 * 
		 * pricing.setCombination(item.getCombination());
		 * 
		 * latestPricingDetails.add(pricing); }
		 * 
		 * } catch (TFException e) { e.printStackTrace(); } });
		 */

//		System.out.println("END :" + LocalDateTime.now());
		return pricingDetails;
	}

	public ByteArrayInputStream downloadFoodItemPricingDetails(Long fsId) {

		List<FoodItemPricing> pricingDetails = foodItemRepository.getFoodItemPricingDetailsV2(fsId);

		CSVFormat format = CSVFormat.DEFAULT.withQuoteMode(QuoteMode.MINIMAL);

		try (ByteArrayOutputStream out = new ByteArrayOutputStream();
				CSVPrinter csvPrinter = new CSVPrinter(new PrintWriter(out), format);) {

			List<String> header = Arrays.asList("ID", "FoodItem ID", "Food Item Name", "Customization", "Price",
					"FoodStall ID", "Flag");

			csvPrinter.printRecord(header);

			for (FoodItemPricing pricingObject : pricingDetails) {

				String price = pricingObject.getPrice() == null ? "" : String.valueOf(pricingObject.getPrice());

				List<String> data = Arrays.asList(String.valueOf(pricingObject.getId()),
						String.valueOf(pricingObject.getFoodItemId()), pricingObject.getFoodItemName(),
						StringUtils.hasText(pricingObject.getCombination())
								? pricingObject.getCombination().replaceAll("##", " ")
								: "",
						price, String.valueOf(pricingObject.getFoodStallId()), "YES");

				csvPrinter.printRecord(data);
			}

			csvPrinter.flush();
			return new ByteArrayInputStream(out.toByteArray());
		} catch (IOException e) {
			throw new RuntimeException("fail to export data to CSV file: " + e.getMessage());
		}
	}

	public void readPricingFile(Long fsId, MultipartFile multiPartFile) throws TFException {
		System.out.println(multiPartFile.getOriginalFilename());

		try (BufferedReader fileReader = new BufferedReader(
				new InputStreamReader(multiPartFile.getInputStream(), "UTF-8"));
				CSVParser csvParser = new CSVParser(fileReader,
						CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim());) {

			Iterable<CSVRecord> csvRecords = csvParser.getRecords();

			for (CSVRecord csvRecord : csvRecords) {

				String pricingId = csvRecord.get("ID");
				String flag = csvRecord.get("Flag");
				if ("YES".equalsIgnoreCase(flag)) {
					String foodItemName = csvRecord.get("Food Item Name");
					String customization = csvRecord.get("Customization");
					Long foodItemId = Long.parseLong(csvRecord.get("FoodItem ID"));
					Long foodStallId = Long.parseLong(csvRecord.get("FoodStall ID"));
					Double price = Double.parseDouble(csvRecord.get("Price"));
					Double packagingPrice = Double.parseDouble(csvRecord.get("Packaging Price"));
					log.info("Updating price : {}, {}, {}", foodStallId, pricingId, price);
					updateFoodItemPrice(foodStallId, pricingId, price, packagingPrice);
				}
			}
		} catch (IOException e) {
			throw new RuntimeException("fail to parse CSV file: " + e.getMessage());
		}

	}

	public List<FoodItemPricing> getFoodItemPricingDetails1(Long fsId) {

		List<FoodItemPricing> pricingDetails = foodItemRepository.getFoodItemPricingDetails(fsId);

		List<FoodItemPricing> latestPricingDetails = new ArrayList<FoodItemPricing>();

		System.out.println("START :" + LocalDateTime.now());

		Map<Long, FoodItem> foodItemsMap = this.getFoodItemsMap(fsId);

		pricingDetails.parallelStream().forEach(pricing -> {
			FoodItem item = null;
			try {
				item = foodItemsMap.containsKey(pricing.getFoodItemId()) ? foodItemsMap.get(pricing.getFoodItemId())
						: foodItemRepository.getFoodItem(pricing.getFoodItemId());

				if (Objects.nonNull(item) && !item.isDefaultCombination()) {
					String name = pricing.getFoodItemName();
					name = name.replaceAll("##", " ");
					pricing.setFoodItemName(name);

					pricing.setCombination(item.getCombination());

					latestPricingDetails.add(pricing);
				}

			} catch (TFException e) {
				e.printStackTrace();
			}
		});

		System.out.println("END :" + LocalDateTime.now());
		return latestPricingDetails;
	}

	private Map<Long, FoodItem> getFoodItemsMap(Long fsId) {
		List<FoodItem> items = foodItemRepository.getFoodItems(fsId);
		Map<Long, FoodItem> foodItemsMap = items.parallelStream()
				.collect(Collectors.toMap(f -> f.getFoodItemId(), f -> f));

		return foodItemsMap;
	}

	public List<FoodItem> getCombinationFoodItems(Long fsId, Long baseItemId) {

		return foodItemRepository.getCombinationFoodItems(fsId, baseItemId);
	}

	public void updateItemPriceBulkProcess() {

	}

	public FoodItemPricing updateFoodItemPrice(Long fsId, String pricingId, Double newPrice, Double newPackagingPrice)
			throws TFException {

		FoodItemPricing itemPricingExistingDetails = foodItemRepository.getFoodItemPricingDetails(pricingId);

		Double foodItemExistingPrice = itemPricingExistingDetails.getPrice();
		if (foodItemExistingPrice == null) {
			foodItemExistingPrice = Double.valueOf(0);
		}

		FoodItem foodItem = foodItemRepository.getFoodItem(itemPricingExistingDetails.getFoodItemId());
		if (foodItem == null) {
			System.out.println(
					"itemPricingExistingDetails.getFoodItemId() : " + itemPricingExistingDetails.getFoodItemId());
			return null;
		}

		foodItem.setPrice(newPrice);
		foodItem.setPackagingPrice(newPackagingPrice);

		if (Objects.isNull(foodItem.getBaseItem())) {
			FoodItem childFoodItem = foodItemRepository.getChileFoodItem(foodItem.getFoodItemId(),
					foodItem.getCombination());

			System.out.println("childFoodItem id : " + childFoodItem);

			if (Objects.nonNull(childFoodItem)) {
				childFoodItem.setPrice(newPrice);
				foodItemRepository.updateFoodItem(childFoodItem);

				FoodItemPricing childItemPricingInfo = foodItemRepository.getFoodItemPricingDetails(fsId,
						childFoodItem.getFoodItemId());

				foodItemRepository.updateFoodItemPrice(fsId, childItemPricingInfo.getId(), newPrice, newPackagingPrice);
			}
		}

		foodItemRepository.updateFoodItem(foodItem); // Just to update the latest price of food item

		FoodItemPricing itemPricing = foodItemRepository.updateFoodItemPrice(fsId, pricingId, newPrice,
				newPackagingPrice);

		System.out.println("FoodItem price is updated.");

		System.out.println("CustType comb name : " + foodItem.getFoodItemId() + " : " + foodItem.getCombination());

		if (!StringUtils.hasText(foodItem.getCombination())) {
			return null;
		}

		Long foodItemId = Objects.isNull(foodItem.getBaseItem()) ? foodItem.getFoodItemId() : foodItem.getBaseItem();

		List<FoodItemCustomizationPricing> foodItemCustomizationPricingDetails = this
				.getFoodItemCustomizationPricingDetails(fsId, foodItemId);

		for (FoodItemCustomizationPricing foodItemCustomizationPricing : foodItemCustomizationPricingDetails) {

			Double existingPrice = foodItemCustomizationPricing.getPrice();

			if (Objects.isNull(existingPrice)) {
				existingPrice = Double.valueOf(0);
			}

			String combination = foodItemCustomizationPricing.getCustomiseType();
			List<String> combinationTokens = Arrays.asList(combination.split("##"));

			String custNameTokens[] = foodItem.getCombination().split("##");

			boolean flag = true;
			for (int i = 0; i < custNameTokens.length; i++) {
				if (!combinationTokens.contains(custNameTokens[i])) {
					flag = false;
					break;
				}
			}

			if (!flag) {
				continue;
			}

			if (existingPrice == 0) {
//					System.out.println("In true case");
				foodItemCustomizationPricing.setPrice(newPrice);
			} else {
//					System.out.println("In false case");
//					System.out.println("In false case existingPrice : " + existingPrice);
//					System.out.println("In false case foodItemExistingPrice : " + foodItemExistingPrice);
				existingPrice = existingPrice - foodItemExistingPrice;
				Double revisedPrice = existingPrice + newPrice;

//					System.out.println("In false case revisedPrice : " + revisedPrice);

				foodItemCustomizationPricing.setPrice(revisedPrice);
			}

			foodItemRepository.updateFoodItemCustomizingPrice(fsId, foodItemCustomizationPricing.getId(),
					foodItemCustomizationPricing.getPrice());
		}
		addFoodItemsToCache(fsId);
		return itemPricing;
	}

	public List<FoodItemCustomizationPricing> getFoodItemCustomizationPricingDetails(Long fsId) {

		return foodItemRepository.getFoodItemPricingDetailsWithCustomization(fsId);
	}

	public List<FoodItemCustomizationPricing> getFoodItemCustomizationPricingDetails(Long fsId, Long foodItemId) {

		return foodItemRepository.getFoodItemPricingDetailsWithCustomization(fsId, foodItemId);
	}

	public List<FoodItemCustomizationPricing> getFoodItemCustomizationPricingDetailsForResponse(Long fsId) {

		List<FoodItemCustomizationPricing> responseList = new ArrayList<FoodItemCustomizationPricing>();

		List<FoodItemCustomizationPricing> existingList = this.getFoodItemCustomizationPricingDetails(fsId);

		for (FoodItemCustomizationPricing pricingObject : existingList) {
			pricingObject.setCustomiseType(pricingObject.getCustomiseType().replaceAll("##", " "));
			responseList.add(pricingObject);
		}

		return responseList;
	}

	public FoodItemCustomizationPricing updateFoodItemCustomizationPrice(Long fsId, String pricingId, Double newPrice) {
		System.out.println("In updateFoodItemCustomizationPrice()");
		FoodItemCustomizationPricing itemPricing = foodItemRepository.updateFoodItemCustomizingPrice(fsId, pricingId,
				newPrice);

		return itemPricing;
	}

	public List<AddOns> getAddOns(Long fsId) {

		return foodItemRepository.getAddOns(fsId);
	}

	public void addFoodItemCustomiseDetails(String requestId, FoodItemCustomiseDetails foodItemCustomiseDetails)
			throws TFException {

		FoodItem foodItem = foodItemRepository.getFoodItemByReqId(requestId);

		foodItem.setAvailableCustomisation(true);

		foodItemRepository.updateFoodItem(foodItem);

		foodItemCustomiseDetails.setFoodStallId(foodItem.getFoodStallId());

//		foodItem.setPizza(true);
		foodItemRepository.addFoodItemCustomiseDetails(foodItem.getFoodItemId(), foodItemCustomiseDetails);

		if (Objects.nonNull(foodItemCustomiseDetails.getId())) {
			System.out.println("Food item customisation details are saved successfully");
		}

		this.addItemCustomizationPricing(foodItem, foodItemCustomiseDetails);
	}

	public void addItemCustomizationPricing(FoodItem foodItem, FoodItemCustomiseDetails customizationDetails)
			throws TFException {

		String category = foodItem.getCategory();
		String subCategory = foodItem.getSubCategory();
		String foodItemName = foodItem.getFoodItemName();
		Long foodItemId = foodItem.getFoodItemId();
		String foodItemDescription = foodItem.getDescription();

		List<String> customiseTypes = customizationDetails.getCustomiseTypes();
		Map<String, List<String>> customizeFoodItems = this
				.processCustomizationLists(customizationDetails.getCustomiseFoodItems(), customiseTypes);
		Map<String, List<String>> customiseFoodItemsCustomerSpecifications = this.processCustomizationLists(
				customizationDetails.getCustomiseFoodItemsCustomerSpecifications(), customiseTypes);
		Map<String, List<String>> customiseFoodItemsDescriptions = this
				.processCustomizationLists(customizationDetails.getCustomiseFoodItemsDescriptions(), customiseTypes);
		Map<String, List<String>> customiseFoodItemsSelectButtons = this
				.processCustomizationLists(customizationDetails.getCustomiseFoodItemsSelectButtons(), customiseTypes);

		List<String> addOnItems = customizationDetails.getAddOnItemsIds();

		boolean isSingleCustType = true;

		if (customizeFoodItems.size() > 1) {
			isSingleCustType = false;
		}

		List<String> foodItemCombinations = new ArrayList<String>();

		List<String> foodItemCustCombinations = new ArrayList<String>();

		System.out.println("customizeFoodItems data for combinations : " + customizeFoodItems);

		if (foodItem.isPizza()) {
			int count = 0;
			for (List<String> list : customizeFoodItems.values()) {
				foodItemCustCombinations = preparePizzaCombinations(foodItemCustCombinations, list, isSingleCustType,
						count++);
				foodItemCombinations = preparePizzaCombinations(foodItemCombinations, list, isSingleCustType);
			}

		} else {
			for (List<String> list : customizeFoodItems.values()) {
				foodItemCustCombinations = prepareCombinations(foodItemCustCombinations, list, isSingleCustType);
			}
		}

		System.out.println("foodItemCombinations >>" + foodItemCombinations);

		List<FoodItem> foodItemsListToBulkInsert = new ArrayList<FoodItem>();

		if (foodItem.isPizza()) {
			foodItemsListToBulkInsert = new ArrayList<FoodItem>();
			boolean isDefaultCombination = true;
			for (String combination : foodItemCombinations) {

				if (!combination.contains("##") || (combination.indexOf("##") < combination.lastIndexOf("##"))) {
					continue;
				}

				FoodItem custSupportItem = new FoodItem();

				custSupportItem.setAddOn(false);
				custSupportItem.setBaseItem(foodItem.getFoodItemId());
				custSupportItem.setFoodStallId(foodItem.getFoodStallId());

				custSupportItem.setCombination(combination);

				custSupportItem.setFoodItemName(foodItem.getFoodItemName());

				custSupportItem.setCategory(foodItem.getCategory());
				custSupportItem.setSubCategory(foodItem.getSubCategory());
				custSupportItem.setAvailableCustomisation(false);
				custSupportItem.setDescription("NA");
				custSupportItem.setCuisine(foodItem.getCuisine());
				custSupportItem.setPizza(foodItem.isPizza());
				custSupportItem.setDefaultCombination(isDefaultCombination);

				if (isDefaultCombination && foodItem.isPizza()) {
					foodItem.setCombination(combination);
					foodItemRepository.updateFoodItem(foodItem);
					System.out.println("Base Item updated with combination : " + foodItem.getCombination() + " : "
							+ foodItem.getFoodItemName());
				}

				foodItemsListToBulkInsert.add(custSupportItem);
//				custSupportItem = foodItemRepository.addFoodItem(custSupportItem);
//				this.addItemPricing(custSupportItem);

				isDefaultCombination = false;
			}
		} else {
			foodItemsListToBulkInsert = new ArrayList<FoodItem>();
			boolean isDefaultCombination = true;
			for (String combination : foodItemCustCombinations) {
				FoodItem custSupportItem = new FoodItem();

				custSupportItem.setAddOn(false);
				custSupportItem.setBaseItem(foodItem.getFoodItemId());
				custSupportItem.setFoodStallId(foodItem.getFoodStallId());

				custSupportItem.setCombination(combination);

				custSupportItem.setFoodItemName(foodItem.getFoodItemName());
				custSupportItem.setCategory(foodItem.getCategory());
				custSupportItem.setSubCategory(foodItem.getSubCategory());
				custSupportItem.setAvailableCustomisation(false);
				custSupportItem.setDescription("NA");
				custSupportItem.setCuisine(foodItem.getCuisine());
				custSupportItem.setPizza(foodItem.isPizza());
				custSupportItem.setDefaultCombination(isDefaultCombination);

				if (isDefaultCombination) {
					foodItem.setCombination(combination);
					foodItemRepository.updateFoodItem(foodItem);
					System.out.println("Base Item updated with combination : " + foodItem.getCombination() + " : "
							+ foodItem.getFoodItemName());
				}

				foodItemsListToBulkInsert.add(custSupportItem);
//				custSupportItem = foodItemRepository.addFoodItem(custSupportItem);
//				this.addItemPricing(custSupportItem);

				isDefaultCombination = false;
			}

			/*
			 * for(List<String> list : customizeFoodItems.values()) { for(String custName :
			 * list) { FoodItem custSupportItem = new FoodItem();
			 * 
			 * custSupportItem.setAddOn(false);
			 * custSupportItem.setBaseItem(foodItem.getFoodItemId());
			 * custSupportItem.setFoodStallId(foodItem.getFoodStallId());
			 * 
			 * custSupportItem.setCombination(custName);
			 * 
			 * custSupportItem.setFoodItemName(foodItem.getFoodItemName());
			 * custSupportItem.setCategory(foodItem.getCategory());
			 * custSupportItem.setSubCategory(foodItem.getSubCategory());
			 * custSupportItem.setAvailableCustomisation(false);
			 * custSupportItem.setDescription("NA");
			 * custSupportItem.setCuisine(foodItem.getCuisine());
			 * custSupportItem.setPizza(foodItem.isPizza());
			 * 
			 * custSupportItem = foodItemRepository.addFoodItem(custSupportItem);
			 * this.addItemPricing(custSupportItem);
			 * 
			 * } }
			 */
		}

		foodItemRepository.addFoodItems(foodItemsListToBulkInsert);

		System.out.println("Combinations : " + foodItemCustCombinations);

		List<FoodItemCustomizationPricing> foodItemCustPricing = new ArrayList<FoodItemCustomizationPricing>();
		List<FoodItemDirectOffer> foodItemOffers = new ArrayList<FoodItemDirectOffer>();

		for (String combination : foodItemCustCombinations) {
			if (foodItem.isPizza()) {

				if (!combination.contains("##") || combination.indexOf("##") == combination.lastIndexOf("##"))
					continue;
				// This is to skip the single items for PIZZA
			}

			FoodItemCustomizationPricing custPricingData = new FoodItemCustomizationPricing();
			custPricingData.setCategory(category);
			custPricingData.setSubCategory(subCategory);
			custPricingData.setFoodItemId(foodItemId);
			custPricingData.setFoodItemName(foodItemName);

			Double foodItemPrice = foodItemRepository.getFoodItemPrice(foodItemId);
			if (Objects.isNull(foodItemPrice))
				custPricingData.setPrice(Double.valueOf(0));
			else
				custPricingData.setPrice(foodItemPrice);

			custPricingData.setCustomiseType(combination);
			custPricingData.setFoodStallId(foodItem.getFoodStallId());

			foodItemCustPricing.add(custPricingData);
		}

		foodItemRepository.addItemCustomizationPricing(foodItemCustPricing);

		addFoodItemsToCache(foodItem.getFoodStallId());
	}

	public void addItemCustomizationPricing(FoodItem foodItem, FoodItemCustomiseDetails customizationDetails,
			FoodItemPricing existingItemPriceDetails, List<FoodItemCustomizationPricing> existingCustPricingDetails)
			throws TFException {

		String category = foodItem.getCategory();
		String subCategory = foodItem.getSubCategory();
		String foodItemName = foodItem.getFoodItemName();
		Long foodItemId = foodItem.getFoodItemId();

		List<String> customiseTypes = customizationDetails.getCustomiseTypes();
		Map<String, List<String>> customizeFoodItems = this
				.processCustomizationLists(customizationDetails.getCustomiseFoodItems(), customiseTypes);
		Map<String, List<String>> customiseFoodItemsCustomerSpecifications = this.processCustomizationLists(
				customizationDetails.getCustomiseFoodItemsCustomerSpecifications(), customiseTypes);
		Map<String, List<String>> customiseFoodItemsDescriptions = this
				.processCustomizationLists(customizationDetails.getCustomiseFoodItemsDescriptions(), customiseTypes);
		Map<String, List<String>> customiseFoodItemsSelectButtons = this
				.processCustomizationLists(customizationDetails.getCustomiseFoodItemsSelectButtons(), customiseTypes);

		List<String> addOnItems = customizationDetails.getAddOnItemsIds();

		boolean isSingleCustType = true;

		if (customizeFoodItems.size() > 1) {
			isSingleCustType = false;
		}

		List<String> foodItemCombinations = new ArrayList<String>();

		List<String> foodItemCustCombinations = new ArrayList<String>();

		System.out.println("customizeFoodItems data for combinations : " + customizeFoodItems);

		if (foodItem.isPizza()) {
			int count = 0;
			for (List<String> list : customizeFoodItems.values()) {
				foodItemCustCombinations = preparePizzaCombinations(foodItemCustCombinations, list, isSingleCustType,
						count++);
				foodItemCombinations = preparePizzaCombinations(foodItemCombinations, list, isSingleCustType);
			}

		} else {
			for (List<String> list : customizeFoodItems.values()) {
				foodItemCustCombinations = prepareCombinations(foodItemCustCombinations, list, isSingleCustType);
			}
		}

		System.out.println("foodItemCombinations >>" + foodItemCombinations);

		if (foodItem.isPizza()) {
			boolean isDefaultCombination = true;
			for (String combination : foodItemCombinations) {

				if (!combination.contains("##") || (combination.indexOf("##") < combination.lastIndexOf("##"))) {
					continue;
				}

				FoodItem custSupportItem = new FoodItem();

				custSupportItem.setAddOn(false);
				custSupportItem.setBaseItem(foodItem.getFoodItemId());
				custSupportItem.setFoodStallId(foodItem.getFoodStallId());

				custSupportItem.setCombination(combination);

				custSupportItem.setFoodItemName(foodItem.getFoodItemName());

				custSupportItem.setCategory(foodItem.getCategory());
				custSupportItem.setSubCategory(foodItem.getSubCategory());
				custSupportItem.setAvailableCustomisation(false);
				custSupportItem.setDescription("NA");
				custSupportItem.setCuisine(foodItem.getCuisine());
				custSupportItem.setPizza(foodItem.isPizza());
				custSupportItem.setDefaultCombination(isDefaultCombination);

				if (isDefaultCombination && foodItem.isPizza()) {
					foodItem.setCombination(combination);
					foodItemRepository.updateFoodItem(foodItem);
					System.out.println("Base Item updated with combination : " + foodItem.getCombination() + " : "
							+ foodItem.getFoodItemName());
				}

				custSupportItem = foodItemRepository.addFoodItem(custSupportItem);
				this.addItemPricing(custSupportItem);

				isDefaultCombination = false;
			}
		} else {
			boolean isDefaultCombination = true;
			for (String combination : foodItemCustCombinations) {
				FoodItem custSupportItem = new FoodItem();

				custSupportItem.setAddOn(false);
				custSupportItem.setBaseItem(foodItem.getFoodItemId());
				custSupportItem.setFoodStallId(foodItem.getFoodStallId());

				custSupportItem.setCombination(combination);

				custSupportItem.setFoodItemName(foodItem.getFoodItemName());
				custSupportItem.setCategory(foodItem.getCategory());
				custSupportItem.setSubCategory(foodItem.getSubCategory());
				custSupportItem.setAvailableCustomisation(false);
				custSupportItem.setDescription("NA");
				custSupportItem.setCuisine(foodItem.getCuisine());
				custSupportItem.setPizza(foodItem.isPizza());
				custSupportItem.setDefaultCombination(isDefaultCombination);

				if (isDefaultCombination) {
					foodItem.setCombination(combination);
					foodItemRepository.updateFoodItem(foodItem);
					System.out.println("Base Item updated with combination : " + foodItem.getCombination() + " : "
							+ foodItem.getFoodItemName());
				}

				Double price = Double.valueOf(0);

				for (FoodItemCustomizationPricing custItemPrice : existingCustPricingDetails) {
					if (combination.equalsIgnoreCase(custItemPrice.getCustomiseType())) {

						price = custItemPrice.getPrice();
						break;
					}
				}
				custSupportItem.setPrice(price);

				custSupportItem = foodItemRepository.addFoodItem(custSupportItem);
				this.addItemPricing(custSupportItem, price);

				isDefaultCombination = false;
			}

			/*
			 * for(List<String> list : customizeFoodItems.values()) { for(String custName :
			 * list) { FoodItem custSupportItem = new FoodItem();
			 * 
			 * custSupportItem.setAddOn(false);
			 * custSupportItem.setBaseItem(foodItem.getFoodItemId());
			 * custSupportItem.setFoodStallId(foodItem.getFoodStallId());
			 * 
			 * custSupportItem.setCombination(custName);
			 * 
			 * custSupportItem.setFoodItemName(foodItem.getFoodItemName());
			 * custSupportItem.setCategory(foodItem.getCategory());
			 * custSupportItem.setSubCategory(foodItem.getSubCategory());
			 * custSupportItem.setAvailableCustomisation(false);
			 * custSupportItem.setDescription("NA");
			 * custSupportItem.setCuisine(foodItem.getCuisine());
			 * custSupportItem.setPizza(foodItem.isPizza());
			 * 
			 * custSupportItem = foodItemRepository.addFoodItem(custSupportItem);
			 * this.addItemPricing(custSupportItem);
			 * 
			 * } }
			 */
		}

		System.out.println("Combinations : " + foodItemCustCombinations);

		List<FoodItemCustomizationPricing> foodItemCustPricing = new ArrayList<FoodItemCustomizationPricing>();
		List<FoodItemDirectOffer> foodItemOffers = new ArrayList<FoodItemDirectOffer>();

		for (String combination : foodItemCustCombinations) {
			if (foodItem.isPizza()) {

				if (!combination.contains("##") || combination.indexOf("##") == combination.lastIndexOf("##"))
					continue;
				// This is to skip the single items for PIZZA
			}

			FoodItemCustomizationPricing custPricingData = new FoodItemCustomizationPricing();
			custPricingData.setCategory(category);
			custPricingData.setSubCategory(subCategory);
			custPricingData.setFoodItemId(foodItemId);
			custPricingData.setFoodItemName(foodItemName);

			Double foodItemPrice = foodItemRepository.getFoodItemPrice(foodItemId);
			if (Objects.isNull(foodItemPrice))
				custPricingData.setPrice(Double.valueOf(0));
			else
				custPricingData.setPrice(foodItemPrice);

			custPricingData.setCustomiseType(combination);
			custPricingData.setFoodStallId(foodItem.getFoodStallId());

			foodItemCustPricing.add(custPricingData);
		}

		foodItemRepository.addItemCustomizationPricing(foodItemCustPricing);
		addFoodItemsToCache(foodItem.getFoodStallId());
	}

	public List<String> prepareCombinations(List<String> combinations, List<String> list, boolean isSingleCustType) {

		List<String> latestCombinations = new ArrayList<String>();
		for (String str1 : combinations) {
			for (String str2 : list) {
				String str = str1 + "##" + str2;
				latestCombinations.add(str);
			}
		}

		if ((combinations.isEmpty() && !combinations.containsAll(list)) || isSingleCustType)
			combinations.addAll(list);
		combinations.addAll(latestCombinations);

		return combinations;
	}

	public List<String> preparePizzaCombinations(List<String> combinations, List<String> list,
			boolean isSingleCustType) {

		List<String> latestCombinations = new ArrayList<String>();
		for (String str1 : combinations) {

			for (String str2 : list) {
				String str = str1 + "##" + str2;
				latestCombinations.add(str);
			}
		}

		if ((combinations.isEmpty() && !combinations.containsAll(list)) || isSingleCustType)
			combinations.addAll(list);
		combinations.addAll(latestCombinations);

		return combinations;
	}

	public List<String> preparePizzaCombinations(List<String> combinations, List<String> list, boolean isSingleCustType,
			int count) {

		List<String> latestCombinations = new ArrayList<String>();
		for (String str1 : combinations) {

			String dilimTokens[] = str1.split("##");
			int dilimCount = dilimTokens.length - 1;

			if ((count - 1) == dilimCount)
				for (String str2 : list) {
					String str = str1 + "##" + str2;
					latestCombinations.add(str);
				}
		}

		if ((combinations.isEmpty() && !combinations.containsAll(list)) || isSingleCustType)
			combinations.addAll(list);
		combinations.addAll(latestCombinations);

		return combinations;
	}

	private Map<String, List<String>> processCustomizationLists(List<String> dataList, List<String> customiseTypes) {
		Map<String, List<String>> dataMap = new LinkedHashMap<String, List<String>>();
		Map<String, List<String>> sortedDataMap = new LinkedHashMap<String, List<String>>();
		for (String data : dataList) {
			String dataTokens[] = data.split("~");

			System.out.println("dataTokens : " + data);

			if (!dataMap.containsKey(dataTokens[0])) {
				dataMap.put(dataTokens[0], new ArrayList<String>());
			}

			List<String> processedList = dataMap.get(dataTokens[0]);
			if (dataTokens.length == 2) {
				processedList.add(dataTokens[1]);
			} else {
				processedList.add("");
			}

			dataMap.put(dataTokens[0], processedList);
		}

		customiseTypes.forEach(c -> sortedDataMap.put(c, dataMap.get(c)));

		return sortedDataMap;
	}

	public void deleteFoodItem(Long foodItemId) {

		foodItemRepository.deleteFoodItem(foodItemId);
	}

	public void changeFoodItemVisibility(Long foodItemId, String status) throws TFException {

		FoodItem foodItem = foodItemRepository.getFoodItem(foodItemId);

		foodItem.setStatus(status.toUpperCase());

		foodItemRepository.updateFoodItem(foodItem);
	}

	public FoodItemDataToEdit getFoodItemDataForEdit(Long foodItemId) throws TFException {

		FoodItemDataToEdit foodItemDataToEdit = new FoodItemDataToEdit();

		FoodItem foodItem = foodItemRepository.getFoodItem(foodItemId);

		FoodItemCustomiseDetails customizationDetails = foodItemRepository.getFoodItemCustomizeDetails(foodItemId);

		if (Objects.nonNull(customizationDetails)) {
			foodItemDataToEdit.setCustomizationFlag(true);

			foodItemDataToEdit.setCustomiseTypes(customizationDetails.getCustomiseTypes());

			List<String> customizations = customizationDetails.getCustomiseFoodItems();

			Map<String, List<String>> customizationsMap = new HashMap<String, List<String>>();

			for (String custVal : customizations) {
				String custTokens[] = custVal.split("~");

				String keyToken = custTokens[0];
				String valToken = custTokens[1];

				if (!customizationsMap.containsKey(keyToken)) {
					customizationsMap.put(keyToken, new ArrayList<String>());
				}

				customizationsMap.get(keyToken).add(valToken);
			}

			List<FoodItemDataToEdit.CustomizationEntry> customizationEntries = new ArrayList<FoodItemDataToEdit.CustomizationEntry>();

			for (Map.Entry<String, List<String>> entry : customizationsMap.entrySet()) {

				FoodItemDataToEdit.CustomizationEntry custEntry = new FoodItemDataToEdit.CustomizationEntry();

				custEntry.setKey(entry.getKey());
				custEntry.setValues(entry.getValue());

				customizationEntries.add(custEntry);
			}

			foodItemDataToEdit.setCustomizationEntries(customizationEntries);

			List<String> buttons = customizationDetails.getCustomiseFoodItemsSelectButtons();

			Map<String, String> customizationButtonsMap = new HashMap<String, String>();

			for (String btn : buttons) {
				String btnTokens[] = btn.split("~");

				String keyToken = btnTokens[0];
				String valToken = btnTokens[1];

				customizationButtonsMap.put(keyToken, valToken);
			}

			foodItemDataToEdit.setButtons(customizationButtonsMap);

			List<String> descriptions = customizationDetails.getCustomiseFoodItemsDescriptions();

			Map<String, String> customizationDescriptionsMap = new HashMap<String, String>();

			System.out.println("descriptions : " + descriptions);

			for (String desc : descriptions) {
				String descTokens[] = desc.split("~");

				String keyToken = descTokens[0];
				String valToken = "";
				if (descTokens.length == 2) {
					valToken = descTokens[1];
				}
				customizationDescriptionsMap.put(keyToken, valToken);
			}

			foodItemDataToEdit.setDescriptions(customizationDescriptionsMap);

			List<String> customerSpecifications = customizationDetails.getCustomiseFoodItemsCustomerSpecifications();

			Map<String, String> customerSpecificationsMap = new HashMap<String, String>();

			for (String spec : customerSpecifications) {
				String specTokens[] = spec.split("~");

				String keyToken = specTokens[0];
				String valToken = specTokens[1];

				customerSpecificationsMap.put(keyToken, valToken);
			}

			foodItemDataToEdit.setCustomerSpecifications(customerSpecificationsMap);

			List<String> adOnItemIds = customizationDetails.getAddOnItemsIds();

			List<FoodItem> addOnItems = new ArrayList<FoodItem>();

			System.out.println("Addon ItemIds : " + adOnItemIds);

			if (!ObjectUtils.isEmpty(adOnItemIds)) {
				for (String addOnItemId : adOnItemIds) {
					addOnItems.add(foodItemRepository.getFoodItem(Long.parseLong(addOnItemId)));
				}
			}

			System.out.println("addOnItems : " + addOnItems);

			foodItemDataToEdit.setAddOnItems(addOnItems);
			foodItemDataToEdit.setAddOnDescription(customizationDetails.getAddOnDescription());
		} else {
			foodItemDataToEdit.setCustomiseTypes(new ArrayList<String>());
			foodItemDataToEdit.setAddOnItems(new ArrayList<FoodItem>());
			foodItemDataToEdit.setCustomizationEntries(new ArrayList<FoodItemDataToEdit.CustomizationEntry>());
			foodItemDataToEdit.setDescriptions(new HashMap<String, String>());
			foodItemDataToEdit.setButtons(new HashMap<String, String>());
			foodItemDataToEdit.setAddOnDescription("");
			foodItemDataToEdit.setCustomerSpecifications(new HashMap<String, String>());

		}

		foodItemDataToEdit.setFoodItemDetails(foodItem);

		return foodItemDataToEdit;
	}
}