package com.agaramtech.qualis.scheduler.service;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import com.agaramtech.qualis.global.UserInfo;


//ALPD-4941 Created SchedulerConfigurationService for Scheduler configuration screen
public interface SchedulerConfigurationService {
	public ResponseEntity<Object> getSchedulerConfiguration(final Map<String, Object> inputMap, final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getSampleType(final Map<String, Object> inputMap, final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getRegistrationType(final int nSampleType, final Map<String, Object> inputMap, final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getRegistrationSubType(final int nregTypeCode,final Map<String, Object> inputMap, final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getApprovalConfigVersion(final int nregTypeCode,final int nregSubTypeCode,final Map<String, Object> inputMap, final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getApproveConfigVersionRegTemplate(final int nregTypeCode,final int nregSubTypeCode,final int napproveConfigVersionCode,final Map<String, Object> inputMap, final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getSchedulerConfigByFilterSubmit(final Map<String, Object> inputMap, final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> createSchedulerConfig(final Map<String, Object> inputMap, final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getSchedulerConfigSubSample(final Map<String, Object> inputMap, final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getSchedulerConfigTest(final Map<String, Object> inputMap, final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getSchedulerConfigParameter(final Map<String, Object> inputMap, final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getComponentBySpec(final Map<String, Object> inputMap) throws Exception;

	public ResponseEntity<Object> getTestfromDB(final Map<String, Object> inputMap) throws Exception;

	public ResponseEntity<Object> getTestfromTestPackage(final Map<String, Object> inputMap) throws Exception;
	public ResponseEntity<Object> getTestfromSection(final Map<String, Object> inputMap) throws Exception;

	public ResponseEntity<Object> getTestBasedTestSection(final Map<String, Object> inputMap) throws Exception;

	public ResponseEntity<Object> createSubSample(final Map<String, Object> inputMap) throws Exception;
	public ResponseEntity<Object> updateSchedulerConfigSubSample(final Map<String, Object> inputMap) throws Exception;

	public ResponseEntity<Object> getEditSchedulerSubSampleComboService(final Map<String, Object> inputMap, final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> deleteSchedulerConfigSubSample(final Map<String, Object> inputMap) throws Exception;

	public ResponseEntity<Object> getMoreTestPackage(final Map<String, Object> inputMap,final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getMoreTestSection(final Map<String, Object> inputMap,final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getMoreTest(final Map<String, Object> inputMap,final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> createTest(final Map<String, Object> inputMap,final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> deleteSchedulerConfigTest(final Map<String, Object> inputMap, final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getEditSchedulerConfigDetails(final Map<String, Object> inputMap, final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> updateSchedulerConfig(final Map<String, Object> inputMap) throws Exception;

	public ResponseEntity<Object> getSiteByUser(final Map<String, Object> inputMap) throws Exception;

	public ResponseEntity<Object> approveSchedulerConfig(final Map<String, Object> inputMap) throws Exception;

	public ResponseEntity<Object> getSchedulerMaster(final Map<String, Object> inputMap) throws Exception;

	public ResponseEntity<Object> deleteSchedulerConfig(final Map<String, Object> inputMap) throws Exception;

	public ResponseEntity<Object> updateActiveStatusSchedulerConfig(final Map<String, Object> inputMap) throws Exception;

	public ResponseEntity<Object> getSchedulerMasteDetails(final Map<String, Object> inputMap) throws Exception;

}
