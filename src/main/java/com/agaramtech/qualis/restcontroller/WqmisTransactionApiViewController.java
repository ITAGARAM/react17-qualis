package com.agaramtech.qualis.restcontroller;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.wqmis.model.JjmBlockList;
import com.agaramtech.qualis.wqmis.model.JjmGpList;
import com.agaramtech.qualis.wqmis.model.JjmVillageList;
import com.agaramtech.qualis.wqmis.service.WQMISService;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/wqmistransactionapiview")
public class WqmisTransactionApiViewController {

	private final WQMISService wqmisService;
	private final RequestContext requestContext;

	public WqmisTransactionApiViewController(WQMISService wqmisService,RequestContext requestContext) {
		super();
		this.wqmisService = wqmisService;
		this.requestContext = requestContext;
	}

	@PostMapping(value = "/getWQMISTransactionApiDropdown")
	public ResponseEntity<Object> getWQMISTransactionApiDropdown(@RequestBody final Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return wqmisService.getWQMISTransactionApiDropdown(userInfo);
	}

	@PostMapping(value = "/getBlock")
	public ResponseEntity<Object> getBlock(@RequestBody JjmBlockList jjmBlockList) throws Exception {
		return wqmisService.getBlock(jjmBlockList);

	}

	@PostMapping(value = "/getPanchayat")
	public ResponseEntity<Object> getPanchayat(@RequestBody JjmGpList jjmGpList) throws Exception {
		return wqmisService.getPanchayat(jjmGpList);

	}

	@PostMapping(value = "/getVillage")
	public ResponseEntity<Object> getVillage(@RequestBody JjmVillageList JjmVillageList) throws Exception {
		return wqmisService.getVillage(JjmVillageList);

	}
	
	@PostMapping(value = "/getLabSamples")
	public ResponseEntity<Object> getLabSamples(@RequestBody final Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return wqmisService.getLabSamples(inputMap,userInfo);

	}
	
	@PostMapping(value = "/getFTKSamples")
	public ResponseEntity<Object> getFTKSamples(@RequestBody final Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return wqmisService.getFTKSamples(inputMap,userInfo);

	}
	
	@PostMapping(value = "/syncContaminatedLabSamples")
	public ResponseEntity<Object> syncCotaminatedLabSamples(@RequestBody final Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return wqmisService.syncContaminatedLabSamples(inputMap, userInfo);
	}
	
	@PostMapping(value = "/syncContaminatedFTKSamples")
	public ResponseEntity<Object> syncContaminatedFTKSamples(@RequestBody final Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return wqmisService.syncContaminatedFTKSamples(inputMap, userInfo);
	}
}
