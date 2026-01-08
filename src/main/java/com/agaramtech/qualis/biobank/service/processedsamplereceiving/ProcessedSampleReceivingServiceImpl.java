package com.agaramtech.qualis.biobank.service.processedsamplereceiving;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.UserInfo;

@Transactional(readOnly = true, rollbackFor = Exception.class)
@Service
public class ProcessedSampleReceivingServiceImpl implements ProcessedSampleReceivingService {

	private final ProcessedSampleReceivingDAO processedSampleReceivingDAO;
	private final CommonFunction commonFunction;

	/**
	 * This constructor injection method is used to pass the object dependencies to
	 * the class properties.
	 * 
	 * @param bioParentSampleReceivingDAO BioParentSampleReceivingDAO Interface
	 * @param commonFunction              CommonFunction holding common utility
	 *                                    functions
	 */
	public ProcessedSampleReceivingServiceImpl(ProcessedSampleReceivingDAO processedSampleReceivingDAO,
			CommonFunction commonFunction) {
		this.processedSampleReceivingDAO = processedSampleReceivingDAO;
		this.commonFunction = commonFunction;
	}

	@Override
	public ResponseEntity<Object> getProcessedSampleReceiving(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {
		return processedSampleReceivingDAO.getProcessedSampleReceiving(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Object> getProcessedSampleByFilterSubmit(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {
		return processedSampleReceivingDAO.getProcessedSampleByFilterSubmit(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Map<String, Object>> getActiveSampleCollection(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {
		return processedSampleReceivingDAO.getActiveSampleCollection(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Object> getParentSampleCollectionDataForAdd(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {
		return processedSampleReceivingDAO.getParentSampleCollectionDataForAdd(inputMap, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> createProcessedSampleReceiving(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {
		return processedSampleReceivingDAO.createProcessedSampleReceiving(inputMap, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> deleteProcessedSampleReceiving(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {
		return processedSampleReceivingDAO.deleteProcessedSampleReceiving(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Object> getStorageFreezerData(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return processedSampleReceivingDAO.getStorageFreezerData(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Object> getStorageStructure(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return processedSampleReceivingDAO.getStorageStructure(inputMap, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> updateBioSampleCollectionAsProcessed(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {
		return processedSampleReceivingDAO.updateBioSampleCollectionAsProcessed(inputMap, userInfo);

	}

	@Transactional
	@Override
	public ResponseEntity<Object> storeProcessedSampleReceiving(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {
		return processedSampleReceivingDAO.storeProcessedSampleReceiving(inputMap, userInfo);
	}

}
