package com.agaramtech.qualis.wqmis.service;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.agaramtech.qualis.wqmis.model.JjmBlockList;
import com.agaramtech.qualis.wqmis.model.JjmGpList;
import com.agaramtech.qualis.wqmis.model.JjmVillageList;
import com.agaramtech.qualis.global.UserInfo;

@Transactional(readOnly = true, rollbackFor = Exception.class)
@Service
public class WQMISServiceImpl implements WQMISService {

	private final WQMISDAO wqmisDAO;

	public WQMISServiceImpl(WQMISDAO wqmisDAO) {
		super();
		this.wqmisDAO = wqmisDAO;
	}


	@Transactional
	public ResponseEntity<Object> getLabSamples(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return wqmisDAO.getLabSamples(inputMap, userInfo);
	}

	// Neeraj Start
	@Override
	public ResponseEntity<Object> getWQMISLabSamples(String fromDate, String toDate, String currentUIDate,
			UserInfo userInfo,Map<String, Object> inputMap) throws Exception {
		return wqmisDAO.getWQMISLabSamples(fromDate, toDate, currentUIDate, userInfo, inputMap);
	}

	@Override
	public ResponseEntity<Object> getWQMISLabSamplesParametersDetails(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		return wqmisDAO.getWQMISLabSamplesParametersDetails(inputMap, userInfo);
	}
	// end

	@Override
	public ResponseEntity<Object> getWQMISTransactionApiDropdown(UserInfo userInfo) {
		return wqmisDAO.getWQMISTransactionApiDropdown(userInfo);
	}

	@Override
	public ResponseEntity<Object> getBlock(JjmBlockList jjmBlockList) {
		return wqmisDAO.getBlock(jjmBlockList);
	}

	@Override
	public ResponseEntity<Object> getPanchayat(JjmGpList jjmGpList) {
		return wqmisDAO.getPanchayat(jjmGpList);
	}

	@Override
	public ResponseEntity<Object> getVillage(JjmVillageList jjmVillageList) {
		return wqmisDAO.getVillage(jjmVillageList);
	}

	// WQMISTransactionAPI BY DHIVYABHARATHI
	@Override
	public ResponseEntity<Object> getWQMISFTKSamples(String fromDate, String toDate, String currentUIDate,
			UserInfo userInfo,Map<String, Object> inputMap) throws Exception {
		return wqmisDAO.getWQMISFTKSamples(fromDate, toDate, currentUIDate, userInfo, inputMap);
	}

	// WQMISTransactionAPI BY DHIVYABHARATHI
	@Override
	public ResponseEntity<Object> getWQMISFTKSampleParametersDetails(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		return wqmisDAO.getWQMISFTKSampleParametersDetails(inputMap, userInfo);
	}

	// added by sujatha ATE_274 09-10-2025 SWSM-85 for getting Master API in
	// JJMWQMISMasterApi screen
	@Override
	public ResponseEntity<Object> getJJMWQMISMasterApi(final Map<String, Object> inputMap) throws Exception {
		return wqmisDAO.getJJMWQMISMasterApi(inputMap);
	}

//	// WQMISMasterAPI BY MullaiBalaji
//	@Transactional
//	@Override
//	public ResponseEntity<Object> getLabDataList(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
//		return wqmisDAO.getLabDataList(inputMap, userInfo);
//	}

	// WQMISMasterAPI BY MullaiBalaji
	@Transactional
	@Override
	public ResponseEntity<Object> getParameterDataList(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		return wqmisDAO.getParameterDataList(inputMap, userInfo);
	}

	// WQMISMasterAPI BY MullaiBalaji
	@Transactional
	@Override
	public ResponseEntity<Object> getParametersDataFTKUserList(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		return wqmisDAO.getParametersDataFTKUserList(inputMap, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> getDistrictList(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		return wqmisDAO.getDistrictList(inputMap, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> getBlocksList(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		return wqmisDAO.getBlocksList(inputMap, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> getGpList(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		return wqmisDAO.getGpList(inputMap, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> getVillageList(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		return wqmisDAO.getVillageList(inputMap, userInfo);
	}
	
	@Transactional
	public ResponseEntity<Object>getFTKSamples(final Map<String, Object> inputMap,final UserInfo userInfo) throws Exception {
		return wqmisDAO.getFTKSamples(inputMap,userInfo);
	}
	
	@Override
	public ResponseEntity<Object> getWQMISContaminatedLabSamples(String fromDate, String toDate, String currentUIDate,
			UserInfo userInfo,Map<String, Object> inputMap) throws Exception {
		return wqmisDAO.getWQMISContaminatedLabSamples(fromDate, toDate, currentUIDate, userInfo, inputMap);
	}

	@Override
	public ResponseEntity<Object> getWQMISContaminatedLabSampleParametersDetails(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		return wqmisDAO.getWQMISContaminatedLabSampleParametersDetails(inputMap, userInfo);
	}
	
	@Override
	public ResponseEntity<Object> getWQMISContaminatedFTKSamples(String fromDate, String toDate, String currentUIDate,
			UserInfo userInfo,Map<String, Object> inputMap) throws Exception {
		return wqmisDAO.getWQMISContaminatedFTKSamples(fromDate, toDate, currentUIDate, userInfo, inputMap);
	}

	@Override
	public ResponseEntity<Object> getWQMISContaminatedFTKSampleParametersDetails(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		return wqmisDAO.getWQMISContaminatedFTKSampleParametersDetails(inputMap, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> syncContaminatedLabSamples(Map<String, Object> inputMap, UserInfo userInfo)throws Exception {
		return wqmisDAO.syncContaminatedLabSamples(inputMap, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> syncContaminatedFTKSamples(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		return wqmisDAO.syncContaminatedFTKSamples(inputMap, userInfo);
	}
	
	
	//ate135 committed by Dhanalakshmi on 21-11-2025 for Getlabdata
		//SWSM-122 WQMIS Branch creation for inetgartion
	@Transactional
	@Override
	public ResponseEntity<Object> getLabData(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		return wqmisDAO.getLabData(inputMap, userInfo);
	}
	
	//ate135 committed by Dhanalakshmi on 21-11-2025 for getEquipments_data
		//SWSM-122 WQMIS Branch creation for inetgartion
	@Transactional
	@Override
	public ResponseEntity<Object> getEquipments_data(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		return wqmisDAO.getEquipments_data(inputMap, userInfo);
	}
	
	//ate135 committed by Dhanalakshmi on 21-11-2025 for GetSample_submitter
		//SWSM-122 WQMIS Branch creation for inetgartion
	@Transactional
	@Override
	public ResponseEntity<Object> GetSample_submitter(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		return wqmisDAO.GetSample_submitter(inputMap, userInfo);
	}

	//ate135 committed by Dhanalakshmi on 21-11-2025 for getHblist
		//SWSM-122 WQMIS Branch creation for inetgartion
	@Transactional
	@Override
	public ResponseEntity<Object> getHblist(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		return wqmisDAO.getHblist(inputMap, userInfo);
	}
		 
	//ate199 committed by DhivyaBharathi on 21-11-2025 for getLabOfficial
		//SWSM-122 WQMIS Branch creation for inetgartion
		 
	@Transactional
		@Override
		public ResponseEntity<Object> getLabOfficial(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
			return wqmisDAO.getLabOfficial(inputMap, userInfo);
		}
	//ate199 committed by DhivyaBharathi on 21-11-2025 for GetMeasurement_methods_data
		//SWSM-122 WQMIS Branch creation for inetgartion
		 
		@Transactional
		@Override
		public ResponseEntity<Object> GetMeasurement_methods_data(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
			// TODO Auto-generated method stub
			return wqmisDAO.GetMeasurement_methods_data(inputMap, userInfo);
		}
		//ate225 committed by Mohammed Ashik on 21-11-2025 for GetReagents_data
		//SWSM-122 WQMIS Branch creation for inetgartion
		@Transactional
		@Override
		public ResponseEntity<Object> GetReagents_data(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
			return wqmisDAO.GetReagents_data(inputMap, userInfo);	
		}
		//ate225 committed by Mohammed Ashik on 21-11-2025 for GetSample_Location
				//SWSM-122 WQMIS Branch creation for inetgartion
			
		@Transactional
		@Override
		public ResponseEntity<Object> GetSample_Location(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
			return wqmisDAO.GetSample_Location(inputMap, userInfo);
		}
		//ate225 committed by Mohammed Ashik on 21-11-2025 for Getdwsm_state_member_secretary
		//SWSM-122 WQMIS Branch creation for inetgartion
		@Transactional
		@Override
		public ResponseEntity<Object> Getdwsm_state_member_secretary(Map<String, Object> inputMap, UserInfo userInfo)
				throws Exception {
			return wqmisDAO.Getdwsm_state_member_secretary(inputMap, userInfo);
		}
		//ate199 committed by DhivyaBharathi on 21-11-2025 for GetWaterSource
		//SWSM-122 WQMIS Branch creation for inetgartion
		 
		@Transactional
		@Override
		public ResponseEntity<Object> GetWaterSource(Map<String, Object> inputMap, UserInfo userInfo)
				throws Exception {
			return wqmisDAO.GetWaterSource(inputMap, userInfo);
		}
		//ate225 committed by Mohammed Ashik on 21-11-2025 for Getstate_level_mapping
		//SWSM-122 WQMIS Branch creation for inetgartion
		@Transactional
		@Override
		public ResponseEntity<Object> Getstate_level_mapping(Map<String, Object> inputMap, UserInfo userInfo)
				throws Exception {
			return wqmisDAO.Getstate_level_mapping(inputMap, userInfo);
		}
	
}
