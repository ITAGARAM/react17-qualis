package com.agaramtech.qualis.restcontroller;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.wqmis.service.WQMISService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/wqmismasterapiview")
public class WqmisMasterApiViewController {

	private final WQMISService wqmisService;
	private RequestContext requestContext;

	public WqmisMasterApiViewController(WQMISService wqmisService, RequestContext requestContext) {
		super();
		this.wqmisService = wqmisService;
		this.requestContext = requestContext;
	}

	@PostMapping(value = "/getJjmWqmisMasterApi")
	public ResponseEntity<Object> getJJMWQMISMasterApi(@RequestBody Map<String, Object> inputMap) throws Exception {
		return wqmisService.getJJMWQMISMasterApi(inputMap);
	}
// commented by dhivya bharathi .. new labdata added
//	@PostMapping(value = "/getLabDataList")
//	public ResponseEntity<Object> getLabDataList(@RequestBody Map<String, Object> inputMap) throws Exception {
//		final ObjectMapper objMapper = new ObjectMapper();
//		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
//		});
//		requestContext.setUserInfo(userInfo);
//		return wqmisService.getLabDataList(inputMap, userInfo);
//
//	}

	@PostMapping(value = "/getParameterDataList")
	public ResponseEntity<Object> getParameterDataList(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return wqmisService.getParameterDataList(inputMap, userInfo);

	}

	@PostMapping(value = "/getParametersDataFTKUserList")
	public ResponseEntity<Object> getParametersDataFTKUserList(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return wqmisService.getParametersDataFTKUserList(inputMap, userInfo);

	}

	@PostMapping(value = "/getDistrictList")
	public ResponseEntity<Object> getDistrictList(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		return wqmisService.getDistrictList(inputMap, userInfo);

	}

	@PostMapping(value = "/getBlockList")
	public ResponseEntity<Object> getBlockList(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		return wqmisService.getBlocksList(inputMap, userInfo);
	}

	@PostMapping(value = "/getGpList")
	public ResponseEntity<Object> getGpList(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		return wqmisService.getGpList(inputMap, userInfo);
	}

	@PostMapping(value = "/getVillageList")
	public ResponseEntity<Object> getVillageList(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		return wqmisService.getVillageList(inputMap, userInfo);
	}
	
	//ate135 committed by Dhanalakshmi on 21-11-2025 for Getlabdata
	//SWSM-122 WQMIS Branch creation for inetgartion
	
	@PostMapping(value = "/getLabData")
	public ResponseEntity<Object> getLabData(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		return wqmisService.getLabData(inputMap, userInfo);
	}
	
	//ate135 committed by Dhanalakshmi on 21-11-2025 for getEquipments_data
		//SWSM-122 WQMIS Branch creation for inetgartion
	
	@PostMapping(value = "/getEquipments_data")
	public ResponseEntity<Object> getEquipments_data(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		return wqmisService.getEquipments_data(inputMap, userInfo);
	}
	
	//ate199 committed by DhivyaBharathi on 21-11-2025 for getLabOfficial
	//SWSM-122 WQMIS Branch creation for inetgartion
	 
	   @PostMapping(value = "/getLabOfficial")
		public ResponseEntity<Object> getLabOfficial(@RequestBody Map<String, Object> inputMap) throws Exception {
			final ObjectMapper objMapper = new ObjectMapper();
			final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
			});
			return wqmisService.getLabOfficial(inputMap, userInfo);
		}
	
	 //ate199 committed by DhivyaBharathi on 21-11-2025 for GetMeasurement_methods_data
		//SWSM-122 WQMIS Branch creation for inetgartion
		@PostMapping(value = "/GetMeasurement_methods_data")
		public ResponseEntity<Object> GetMeasurement_methods_data(@RequestBody Map<String, Object> inputMap) throws Exception {
			final ObjectMapper objMapper = new ObjectMapper();
			final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
			});
			return wqmisService.GetMeasurement_methods_data(inputMap, userInfo);
		}
	 
	 
		//ate135 committed by Dhanalakshmi on 21-11-2025 for GetSample_submitter
		//SWSM-122 WQMIS Branch creation for inetgartion
	@PostMapping(value = "/GetSample_submitter")
	public ResponseEntity<Object> GetSample_submitter(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		return wqmisService.GetSample_submitter(inputMap, userInfo);
	}


	//ate135 committed by Dhanalakshmi on 21-11-2025 for getEquipments_data
			//SWSM-122 WQMIS Branch creation for inetgartion
	@PostMapping(value = "/getHblist")
	public ResponseEntity<Object> getHblist(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		return wqmisService.getHblist(inputMap, userInfo);
	}
	
	//ate225 committed by Mohammed Ashik on 21-11-2025 for GetReagents_data
	//SWSM-122 WQMIS Branch creation for inetgartion
	@PostMapping(value = "/GetReagents_data")
	public ResponseEntity<Object> GetReagents_data(@RequestBody final Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return wqmisService.GetReagents_data(inputMap,userInfo);

	}
	
	//ate225 committed by Mohammed Ashik on 21-11-2025 for GetSample_Location
	//SWSM-122 WQMIS Branch creation for inetgartion
	@PostMapping(value = "/GetSample_Location")
	public ResponseEntity<Object> GetSample_Location(@RequestBody final Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return wqmisService.GetSample_Location(inputMap,userInfo);

	}
	
	//ate225 committed by Mohammed Ashik on 21-11-2025 for Getdwsm_state_member_secretary
	//SWSM-122 WQMIS Branch creation for inetgartion
	@PostMapping(value = "/Getdwsm_state_member_secretary")
	public ResponseEntity<Object> Getdwsm_state_member_secretary(@RequestBody final Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return wqmisService.Getdwsm_state_member_secretary(inputMap,userInfo);

	}
	
	//ate199 committed by DhivyaBharathi on 21-11-2025 for GetWaterSource
		//SWSM-122 WQMIS Branch creation for inetgartion
	@PostMapping(value = "/GetWaterSource")
	public ResponseEntity<Object> GetWaterSource(@RequestBody final Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return wqmisService.GetWaterSource(inputMap,userInfo);

	}
	//ate225 committed by Mohammed Ashik on 21-11-2025 for Getstate_level_mapping
	//SWSM-122 WQMIS Branch creation for inetgartion
	@PostMapping(value = "/Getstate_level_mapping")
	public ResponseEntity<Object> Getstate_level_mapping(@RequestBody final Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return wqmisService.Getstate_level_mapping(inputMap,userInfo);

	}

	
}
