package com.agaramtech.qualis.scheduler.service;

import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import com.agaramtech.qualis.global.UserInfo;

//ALPD-4941 Created SchedulerConfigurationDAO for Scheduler configuration screen
public interface SchedulerConfigurationDAO {
	public ResponseEntity<Object> getSchedulerConfiguration(final Map<String, Object> inputMap,final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> getSampleType(final Map<String, Object> inputMap, final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getRegistrationType(final int nSampleType,final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getRegistrationSubType(final int nregTypeCode,final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getApprovalConfigVersion(final int nregTypeCode,final int nregSubTypeCode,
			final Map<String, Object> inputMap, final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getApproveConfigVersionRegTemplate(final int nregTypeCode,final int nregSubTypeCode,
			final int napproveConfigVersionCode,final Map<String, Object> inputMap, final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getSchedulerConfigByFilterSubmit(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> createSchedulerConfig(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> getSchedulerConfigSubSample(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> getSchedulerConfigTest(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> getSchedulerConfigParameter(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> getComponentBySpec(final Map<String, Object> inputMap) throws Exception;

	public ResponseEntity<Object> getTestfromDB(final Map<String, Object> inputMap) throws Exception;

	public ResponseEntity<Object> getTestfromTestPackage(final Map<String, Object> inputMap) throws Exception;

	public ResponseEntity<Object> getTestfromSection(final Map<String, Object> inputMap) throws Exception;

	public ResponseEntity<Object> getTestBasedTestSection(final Map<String, Object> inputMap) throws Exception;

	public ResponseEntity<Object> createSubSample(Map<String, Object> inputMap) throws Exception;

	public ResponseEntity<Object> updateSchedulerConfigSubSample(Map<String, Object> inputMap) throws Exception;

	public ResponseEntity<Object> getEditSchedulerSubSampleComboService(Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> deleteSchedulerConfigSubSample(Map<String, Object> inputMap) throws Exception;

	public ResponseEntity<Object> getMoreTest(Map<String, Object> inputMap, final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getMoreTestSection(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> getMoreTestPackage(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> createTest(Map<String, Object> inputMap, final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> deleteSchedulerConfigTest(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> getEditSchedulerConfigDetails(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> updateSchedulerConfig(final Map<String, Object> inputMap) throws Exception;

	public Map<String, Object> validateUniqueConstraintScheduler(final List<Map<String, Object>> masterUniqueValidation,
			final Map<String, Object> map,final UserInfo userInfo,final String string,final Class<?> class1,final  String string2, boolean b)
					throws Exception;

	public ResponseEntity<Object> getSiteByUser(final Map<String, Object> inputMap) throws Exception;

	public ResponseEntity<Object> approveSchedulerConfig(final Map<String, Object> inputMap) throws Exception;

	public ResponseEntity<Object> getSchedulerMaster(final Map<String, Object> inputMap) throws Exception;

	public ResponseEntity<Object> deleteSchedulerConfig(final Map<String, Object> inputMap) throws Exception;

	public ResponseEntity<Object> updateActiveStatusSchedulerConfig(final Map<String, Object> inputMap)
			throws Exception;

	public ResponseEntity<Object> getSchedulerMasteDetails(final Map<String, Object> inputMap) throws Exception;

	// ALPD-5530--Vignesh R(06-03-2025)--record allowing the pre-register when the
	// approval config retired
	// start
	public short getActiveApprovalConfigId(final int ntransactionstatus) throws Exception;
	// end

}
