package com.agaramtech.qualis.scheduler.service;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.agaramtech.qualis.dynamicpreregdesign.service.dynamicpreregdesign.DynamicPreRegDesignDAO;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.UserInfo;


@Service
public class SchedulerConfigurationServiceImpl implements SchedulerConfigurationService {

	private final CommonFunction commonFunction;
	private final DynamicPreRegDesignDAO dynamicPreRegDesignDAO;
	private final SchedulerConfigurationDAO schedulerConfigurationDAO;

	public SchedulerConfigurationServiceImpl(CommonFunction commonFunction,
			DynamicPreRegDesignDAO dynamicPreRegDesignDAO, SchedulerConfigurationDAO schedulerConfigurationDAO) {
		super();
		this.commonFunction = commonFunction;
		this.dynamicPreRegDesignDAO = dynamicPreRegDesignDAO;
		this.schedulerConfigurationDAO = schedulerConfigurationDAO;
	}



	@Override
	public ResponseEntity<Object> getSchedulerConfiguration(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return schedulerConfigurationDAO.getSchedulerConfiguration(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Object> getSampleType(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return schedulerConfigurationDAO.getSampleType(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Object> getRegistrationType(final int nSampleType,final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return schedulerConfigurationDAO.getRegistrationType(nSampleType, inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Object> getRegistrationSubType(final int nregTypeCode,final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return schedulerConfigurationDAO.getRegistrationSubType(nregTypeCode, inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Object> getApprovalConfigVersion(final int nregTypeCode,final int nregSubTypeCode,final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return schedulerConfigurationDAO.getApprovalConfigVersion(nregTypeCode, nregSubTypeCode, inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Object> getApproveConfigVersionRegTemplate(final int nregTypeCode,final int nregSubTypeCode,final int napproveConfigVersionCode,final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return schedulerConfigurationDAO.getApproveConfigVersionRegTemplate(nregTypeCode, nregSubTypeCode, napproveConfigVersionCode, inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Object> getSchedulerConfigByFilterSubmit(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return schedulerConfigurationDAO.getSchedulerConfigByFilterSubmit(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Object> createSchedulerConfig(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {

		final short ntransactionstatus=schedulerConfigurationDAO.getActiveApprovalConfigId((int)inputMap.get("napproveconfversioncode"));

		if(ntransactionstatus==Enumeration.TransactionStatus.APPROVED.gettransactionstatus()) {

			return schedulerConfigurationDAO.createSchedulerConfig(inputMap, userInfo);

		}
		else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_SELECTAPPROVEDCONFIGVERSION", userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}
	}

	@Override
	public ResponseEntity<Object> getSchedulerConfigSubSample(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return schedulerConfigurationDAO.getSchedulerConfigSubSample(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Object> getSchedulerConfigTest(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return schedulerConfigurationDAO.getSchedulerConfigTest(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Object> getSchedulerConfigParameter(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return schedulerConfigurationDAO.getSchedulerConfigParameter(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Object> getComponentBySpec(final Map<String, Object> inputMap) throws Exception {
		return schedulerConfigurationDAO.getComponentBySpec(inputMap);
	}

	@Override
	public ResponseEntity<Object> getTestfromTestPackage(final Map<String, Object> inputMap) throws Exception {
		return schedulerConfigurationDAO.getTestfromTestPackage(inputMap);
	}

	@Override
	public ResponseEntity<Object> getTestfromDB(final Map<String, Object> inputMap) throws Exception {
		return schedulerConfigurationDAO.getTestfromDB(inputMap);
	}

	@Override
	public ResponseEntity<Object> getTestfromSection(final Map<String, Object> inputMap) throws Exception {
		return schedulerConfigurationDAO.getTestfromSection(inputMap);
	}

	@Override
	public ResponseEntity<Object> getTestBasedTestSection(final Map<String, Object> inputMap) throws Exception {
		return schedulerConfigurationDAO.getTestBasedTestSection(inputMap);
	}

	@Override
	public ResponseEntity<Object> createSubSample(final Map<String, Object> inputMap) throws Exception {
		return schedulerConfigurationDAO.createSubSample(inputMap);
	}

	@Override
	public ResponseEntity<Object> updateSchedulerConfigSubSample(final Map<String, Object> inputMap) throws Exception {
		return schedulerConfigurationDAO.updateSchedulerConfigSubSample(inputMap);
	}

	@Override
	public ResponseEntity<Object> getEditSchedulerSubSampleComboService(Map<String, Object> inputMap, final UserInfo userInfo) throws Exception {
		return schedulerConfigurationDAO.getEditSchedulerSubSampleComboService(inputMap,userInfo);
	}

	@Override
	public ResponseEntity<Object> deleteSchedulerConfigSubSample(Map<String, Object> inputMap) throws Exception {
		return schedulerConfigurationDAO.deleteSchedulerConfigSubSample(inputMap);
	}

	@Override
	public ResponseEntity<Object> getMoreTest(Map<String, Object> inputMap, final UserInfo userInfo) throws Exception {
		return schedulerConfigurationDAO.getMoreTest(inputMap,userInfo);
	}

	@Override
	public ResponseEntity<Object> getMoreTestSection(Map<String, Object> inputMap, final UserInfo userInfo) throws Exception {
		return schedulerConfigurationDAO.getMoreTestSection(inputMap,userInfo);
	}

	@Override
	public ResponseEntity<Object> getMoreTestPackage(Map<String, Object> inputMap, final UserInfo userInfo) throws Exception {
		return schedulerConfigurationDAO.getMoreTestPackage(inputMap,userInfo);
	}

	@Override
	public ResponseEntity<Object> createTest(Map<String, Object> inputMap, final UserInfo userInfo) throws Exception {
		return schedulerConfigurationDAO.createTest(inputMap,userInfo);
	}


	@Override
	public ResponseEntity<Object> deleteSchedulerConfigTest(Map<String, Object> inputMap, final UserInfo userInfo) throws Exception {
		return schedulerConfigurationDAO.deleteSchedulerConfigTest(inputMap,userInfo);
	}

	@Override
	public ResponseEntity<Object> getEditSchedulerConfigDetails(Map<String, Object> inputMap, final UserInfo userInfo) throws Exception {

		Map<String, Object> map = (Map<String, Object>) schedulerConfigurationDAO.getEditSchedulerConfigDetails(inputMap, userInfo)
				.getBody();
		// map.putAll(inputMap);
		map.putAll((Map<? extends String, ? extends Object>) dynamicPreRegDesignDAO
				.getComboValuesForEdit(map, inputMap, userInfo).getBody());


		return new ResponseEntity<>(map, HttpStatus.OK);
	}


	@Override
	public ResponseEntity<Object> updateSchedulerConfig(final Map<String, Object> inputMap) throws Exception {
		return schedulerConfigurationDAO.updateSchedulerConfig(inputMap);
	}

	@Override
	public ResponseEntity<Object> getSiteByUser(final Map<String, Object> inputMap) throws Exception {
		return schedulerConfigurationDAO.getSiteByUser(inputMap);
	}

	@Override
	public ResponseEntity<Object> approveSchedulerConfig(final Map<String, Object> inputMap) throws Exception {
		return schedulerConfigurationDAO.approveSchedulerConfig(inputMap);
	}

	@Override
	public ResponseEntity<Object> getSchedulerMaster(final Map<String, Object> inputMap) throws Exception {
		return schedulerConfigurationDAO.getSchedulerMaster(inputMap);
	}

	@Override
	public ResponseEntity<Object> deleteSchedulerConfig(final Map<String, Object> inputMap) throws Exception {
		return schedulerConfigurationDAO.deleteSchedulerConfig(inputMap);
	}

	@Override
	public ResponseEntity<Object> updateActiveStatusSchedulerConfig(final Map<String, Object> inputMap) throws Exception {
		return schedulerConfigurationDAO.updateActiveStatusSchedulerConfig(inputMap);
	}

	@Override
	public ResponseEntity<Object> getSchedulerMasteDetails(final Map<String, Object> inputMap) throws Exception {
		return schedulerConfigurationDAO.getSchedulerMasteDetails(inputMap);
	}
}
