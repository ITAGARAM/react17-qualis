package com.agaramtech.qualis.wqmis.service;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import com.agaramtech.qualis.wqmis.model.JjmBlockList;
import com.agaramtech.qualis.wqmis.model.JjmGpList;
import com.agaramtech.qualis.wqmis.model.JjmVillageList;
import com.agaramtech.qualis.global.UserInfo;

public interface WQMISService {

	public ResponseEntity<Object> getLabSamples(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	// Neeraj start
	public ResponseEntity<Object> getWQMISLabSamples(String fromDate, String toDate, String currentUIDate,
			UserInfo userInfo,Map<String, Object> inputMap) throws Exception;

	public ResponseEntity<Object> getWQMISLabSamplesParametersDetails(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception;
	// end

	// WQMISTransactionAPI BY DHIVYABHARATHI
	public ResponseEntity<Object> getWQMISFTKSamples(String fromDate, String toDate, String currentUIDate,
			UserInfo userInfo,Map<String, Object> inputMap) throws Exception;

	// WQMISTransactionAPI BY DHIVYABHARATHI
	public ResponseEntity<Object> getWQMISFTKSampleParametersDetails(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> getWQMISTransactionApiDropdown(UserInfo userInfo);

	public ResponseEntity<Object> getBlock(JjmBlockList jjmBlockList);

	public ResponseEntity<Object> getPanchayat(JjmGpList jjmGpList);

	public ResponseEntity<Object> getVillage(JjmVillageList jjmVillageList);

	// added by sujatha ATE_274 09-10-2025 SWSM-85 for getting Master API in
	// JJMWQMISMasterApi screen
	public ResponseEntity<Object> getJJMWQMISMasterApi(final Map<String, Object> inputMap) throws Exception;

//	// WQMISMasterAPI BY MULLAIBALAJI
//	public ResponseEntity<Object> getLabDataList(Map<String, Object> inputMap, UserInfo userInfo) throws Exception;

	// WQMISMasterAPI BY MULLAIBALAJI
	public ResponseEntity<Object> getParameterDataList(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception;

	// WQMISMasterAPI BY MULLAIBALAJI
	public ResponseEntity<Object> getParametersDataFTKUserList(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> getDistrictList(Map<String, Object> inputMap, UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getBlocksList(Map<String, Object> inputMap, UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getGpList(Map<String, Object> inputMap, UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getVillageList(Map<String, Object> inputMap, UserInfo userInfo) throws Exception;
	
	public ResponseEntity<Object> getFTKSamples(final Map<String, Object> inputMap,final UserInfo userInfo) throws Exception;
	
	public ResponseEntity<Object> getWQMISContaminatedLabSamples(String fromDate, String toDate, String currentUIDate,
			UserInfo userInfo,Map<String, Object> inputMap) throws Exception;

	public ResponseEntity<Object> getWQMISContaminatedLabSampleParametersDetails(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception;
	
	public ResponseEntity<Object> getWQMISContaminatedFTKSamples(String fromDate, String toDate, String currentUIDate,
			UserInfo userInfo,Map<String, Object> inputMap) throws Exception;

	public ResponseEntity<Object> getWQMISContaminatedFTKSampleParametersDetails(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> syncContaminatedLabSamples(Map<String, Object> inputMap, UserInfo userInfo)throws Exception;

	public ResponseEntity<Object> syncContaminatedFTKSamples(Map<String, Object> inputMap, UserInfo userInfo)throws Exception;

	public ResponseEntity<Object> getLabData(Map<String, Object> inputMap, UserInfo userInfo) throws Exception;

	ResponseEntity<Object> getEquipments_data(Map<String, Object> inputMap, UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> GetSample_submitter(Map<String, Object> inputMap, UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getHblist(Map<String, Object> inputMap, UserInfo userInfo)  throws Exception;
	
	public ResponseEntity<Object> getLabOfficial(Map<String, Object> inputMap, UserInfo userInfo) throws Exception;
 
	public ResponseEntity<Object> GetMeasurement_methods_data(Map<String, Object> inputMap, UserInfo userInfo) throws Exception;
	
	public ResponseEntity<Object> GetReagents_data(Map<String, Object> inputMap, UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> GetSample_Location(Map<String, Object> inputMap, UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> Getdwsm_state_member_secretary(Map<String, Object> inputMap, UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> GetWaterSource(Map<String, Object> inputMap, UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> Getstate_level_mapping(Map<String, Object> inputMap, UserInfo userInfo) throws Exception;


 



}
