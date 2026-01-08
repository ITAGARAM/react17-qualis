package com.agaramtech.qualis.restcontroller;

import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.agaramtech.qualis.barcodeprinting.service.ParentSampleBarcode.ParentSampleBarcodeService;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/parentsamplebarcode")
public class ParentSampleBarcodeController {

	@SuppressWarnings("unused")
	private static final Log LOGGER = LogFactory.getLog(ParentSampleBarcodeController.class);

	private RequestContext requestContext;
	private final ParentSampleBarcodeService parentSampleBarcodeService;

	public ParentSampleBarcodeController(RequestContext requestContext,
			ParentSampleBarcodeService parentSampleBarcodeService) {
		super();
		this.requestContext = requestContext;
		this.parentSampleBarcodeService = parentSampleBarcodeService;
	}

	@PostMapping(value = "/getParentSampleBarcode")
	public ResponseEntity<Object> getParentSampleBarcode(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return parentSampleBarcodeService.getParentSampleBarcode(userInfo);
	}

	@PostMapping(value = "/getPrinter")
	public ResponseEntity<Object> getPrinter(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userInfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return parentSampleBarcodeService.getPrinter(userInfo);
	}

	@PostMapping(value = "/printBarcode")
	public ResponseEntity<Object> PrintBarcode(@RequestBody Map<String, Object> inputMap) throws Exception {
		return parentSampleBarcodeService.PrintBarcode(inputMap);
	}

	@PostMapping(value = "/getControlBasedBarcode")
	public ResponseEntity<Object> getControlBasedBarcode(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userInfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		final int ncontrolcode = (Integer) inputMap.get("ncontrolcode");
		// modified by sujatha ATE_274 by adding userInfo for throwing alert if sql query not there in table DEMO src
		return parentSampleBarcodeService.getControlBasedBarcode(ncontrolcode, userInfo);
	}

	@PostMapping(value = "/getParentSampleBarcodedata")
	public ResponseEntity<Object> getParentSampleBarcodedata(@RequestBody Map<String, Object> inputMap) throws Exception {
		return parentSampleBarcodeService.getParentSampleBarcodedata(inputMap);
	}

}
