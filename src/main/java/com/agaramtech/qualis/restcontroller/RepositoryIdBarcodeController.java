package com.agaramtech.qualis.restcontroller;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.agaramtech.qualis.barcodeprinting.service.RepositoryIdBarcode.RepositoryIdBarcodeService;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/repositoryidbarcode")
public class RepositoryIdBarcodeController {
	
	@SuppressWarnings("unused")
	private static final Log LOGGER = LogFactory.getLog(RepositoryIdBarcodeController.class);

	private RequestContext requestContext;
	private final RepositoryIdBarcodeService repositoryIdBarcodeService;

	public RepositoryIdBarcodeController(RequestContext requestContext,
			RepositoryIdBarcodeService repositoryIdBarcodeService) {
		super();
		this.requestContext = requestContext;
		this.repositoryIdBarcodeService = repositoryIdBarcodeService;
	}

	@PostMapping(value = "/getRepositoryIdBarcode")
	public ResponseEntity<Object> getRepositoryIdBarcode(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		int nbarcodefiltertypecode=-1;
		if (inputMap.get("nbarcodefiltertypecode") != null) {
			nbarcodefiltertypecode = (int) inputMap.get("nbarcodefiltertypecode");
		}
		requestContext.setUserInfo(userInfo);
		return repositoryIdBarcodeService.getRepositoryIdBarcode(userInfo,nbarcodefiltertypecode);
	}

}
