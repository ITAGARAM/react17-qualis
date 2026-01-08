package com.agaramtech.qualis.biobank.service.processedsamplereceiving;

import java.util.Map;

import org.springframework.http.ResponseEntity;

import com.agaramtech.qualis.global.UserInfo;

public interface ProcessedSampleReceivingService {


	public ResponseEntity<Object> getProcessedSampleReceiving(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getProcessedSampleByFilterSubmit(final Map<String, Object> inputMap, final UserInfo userInfo) throws Exception;

	public ResponseEntity<Map<String, Object>> getActiveSampleCollection(final Map<String, Object> inputMap, final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getParentSampleCollectionDataForAdd(final Map<String, Object> inputMap, final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> createProcessedSampleReceiving(final Map<String, Object> inputMap,final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> deleteProcessedSampleReceiving(final Map<String, Object> inputMap, final UserInfo userInfo) throws Exception;
	
	public ResponseEntity<Object> getStorageFreezerData(final Map<String, Object> inputMap, final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> updateBioSampleCollectionAsProcessed(final Map<String, Object> inputMap,final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getStorageStructure(final Map<String, Object> inputMap, final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> storeProcessedSampleReceiving(final Map<String, Object> inputMap, final UserInfo userInfo) throws Exception;


}
