package com.agaramtech.qualis.restcontroller;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.agaramtech.qualis.biobank.service.processedsamplereceiving.ProcessedSampleReceivingService;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/processedsamplereceiving")
public class ProcessedSampleReceivingController {
	private static final Logger LOGGER = LoggerFactory.getLogger(ProcessedSampleReceivingController.class);

	private RequestContext requestContext;
	private final ProcessedSampleReceivingService processedSampleReceivingService;
	
	/**
	 * This constructor injection method is used to pass the object dependencies to the class.
	 * @param requestContext RequestContext to hold the request
	 * @param processedSampleReceivingService ProcessedSampleReceivingService
	 */
	
	public ProcessedSampleReceivingController(RequestContext requestContext, ProcessedSampleReceivingService processedSampleReceivingService) {
		this.requestContext = requestContext;
		this.processedSampleReceivingService = processedSampleReceivingService;
	 }	
	
	@PostMapping(value = "/getProcessedSampleReceiving")
	public ResponseEntity<Object> getProcessedSampleReceiving(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		LOGGER.info("getProcessedSampleStatuses() called");
		return processedSampleReceivingService.getProcessedSampleReceiving(inputMap, userInfo);
	}
	@PostMapping(value = "/getProcessedSampleByFilterSubmit")
	public ResponseEntity<Object> getProcessedSampleByFilterSubmit(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return processedSampleReceivingService.getProcessedSampleByFilterSubmit(inputMap, userInfo);
	}
	@PostMapping(value = "/getActiveSampleCollection")
	public ResponseEntity<Map<String, Object>> getActiveSampleCollection(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return processedSampleReceivingService.getActiveSampleCollection(inputMap, userInfo);
	}
	@PostMapping(value = "/getParentSampleCollectionDataForAdd")
	public ResponseEntity<Object> getParentSampleCollectionDataForAdd(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return processedSampleReceivingService.getParentSampleCollectionDataForAdd(inputMap, userInfo);
	}
	@PostMapping(value = "/createProcessedSampleReceiving")
	public ResponseEntity<Object> createProcessedSampleReceiving(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return processedSampleReceivingService.createProcessedSampleReceiving(inputMap, userInfo);
	}
	@PostMapping(value = "/deleteProcessedSampleReceiving")
	public ResponseEntity<Object> deleteProcessedSampleReceiving(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return processedSampleReceivingService.deleteProcessedSampleReceiving(inputMap, userInfo);
	}

@PostMapping(value = "/getStorageFreezerData")
	public ResponseEntity<Object> getStorageFreezerData(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return processedSampleReceivingService.getStorageFreezerData(inputMap, userInfo);
	}


@PostMapping(value = "/getStorageStructure")
	public ResponseEntity<Object> getStorageStructure(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return processedSampleReceivingService.getStorageStructure(inputMap, userInfo);
	}

@PostMapping(value = "/updateBioSampleCollectionAsProcessed")
	public ResponseEntity<Object> updateBioSampleCollectionAsProcessed(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return processedSampleReceivingService.updateBioSampleCollectionAsProcessed(inputMap, userInfo);
	}
@PostMapping(value = "/storeProcessedSampleReceiving")
public ResponseEntity<Object> storeProcessedSampleReceiving(@RequestBody Map<String, Object> inputMap)
		throws Exception {
	final ObjectMapper objMapper = new ObjectMapper();
	final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
	});
	requestContext.setUserInfo(userInfo);
	return processedSampleReceivingService.storeProcessedSampleReceiving(inputMap, userInfo);
}

}
