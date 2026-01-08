package com.agaramtech.qualis.biobank.service.biorequestbasedtransfer;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.agaramtech.qualis.global.UserInfo;

@Transactional(readOnly = true, rollbackFor = Exception.class)
@Service
public class BioRequestBasedTransferServiceImpl implements BioRequestBasedTransferService {

	private final BioRequestBasedTransferDAO bioRequestBasedTransferDAO;

	public BioRequestBasedTransferServiceImpl(final BioRequestBasedTransferDAO bioRequestBasedTransferDAO) {
		this.bioRequestBasedTransferDAO = bioRequestBasedTransferDAO;
	}

	@Transactional
	@Override
	public ResponseEntity<Object> cancelRequestBasedTransfer(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {
		// TODO Auto-generated method stub
		return bioRequestBasedTransferDAO.cancelRequestBasedTransfer(inputMap, userInfo);
	}

	@Override
	@Transactional
	public ResponseEntity<Object> createBioRequestbasedTransfer(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {
		return bioRequestBasedTransferDAO.createBioRequestbasedTransfer(inputMap, userInfo);
	}

	@Override
	@Transactional
	public ResponseEntity<Object> createChildBioRequestBasedTransfer(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {
		return bioRequestBasedTransferDAO.createChildBioRequestBasedTransfer(inputMap, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> createValidationBioRequestBasedTransfer(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {
		// TODO Auto-generated method stub
		return bioRequestBasedTransferDAO.createValidationBioRequestBasedTransfer(inputMap, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> deleteChildRequestBasedTransfer(final int nBioRequestBasedTransferCode,
			final String nBioRequestBasedTransferDetailsCode, final UserInfo userInfo) throws Exception {
		// TODO Auto-generated method stub
		return bioRequestBasedTransferDAO.deleteChildRequestBasedTransfer(nBioRequestBasedTransferCode,
				nBioRequestBasedTransferDetailsCode, userInfo);
	}

	@Override
	@Transactional
	public ResponseEntity<Object> disposeSamples(final int nBioRequestBasedTransferCode,
			final String nBioRequestBasedTransferDetailCode, final UserInfo userInfo) throws Exception {
		return bioRequestBasedTransferDAO.disposeSamples(nBioRequestBasedTransferCode,
				nBioRequestBasedTransferDetailCode, userInfo);
	}

	@Override
	public int findStatusRequestBasedtTransfer(final int nbioRequestBasedCode, final UserInfo userInfo)
			throws Exception {
		return bioRequestBasedTransferDAO.findStatusRequestBasedtTransfer(nbioRequestBasedCode, userInfo);
	}

	@Override
	public ResponseEntity<Object> getActiveBioRequestBasedTransfer(final Map<String, Object> inputMap,
			final int nBioRequestBasedTransferCode, final UserInfo userInfo) throws Exception {
		// TODO Auto-generated method stub
		return bioRequestBasedTransferDAO.getActiveBioRequestBasedTransfer(inputMap, nBioRequestBasedTransferCode,
				userInfo);
	}

	@Override
	public ResponseEntity<Object> getActiveBioRequestBasedTransferById(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {
		// TODO Auto-generated method stub
		return bioRequestBasedTransferDAO.getActiveBioRequestBasedTransferById(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Object> getBioRequestBasedTransfer(final Map<String, Object> inputMap,
			final int nbioRequestBasedTransferCode, final UserInfo userInfo) throws Exception {
		return bioRequestBasedTransferDAO.getBioRequestBasedTransfer(inputMap, nbioRequestBasedTransferCode, userInfo);
	}

	@Override
	public List<Map<String, Object>> getChildInitialGet(final int nbiorequestbasedtransfercode, final UserInfo userInfo)
			throws Exception {
		return bioRequestBasedTransferDAO.getChildInitialGet(nbiorequestbasedtransfercode, userInfo);
	}

	@Override
	public ResponseEntity<Object> getChildRequestBasedRecord(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {
		// TODO Auto-generated method stub
		return bioRequestBasedTransferDAO.getChildRequestBasedRecord(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Object> getCourier(final UserInfo userInfo) throws Exception {
		return bioRequestBasedTransferDAO.getCourier(userInfo);
	}

	@Override
	public ResponseEntity<Object> getProjectBasedOnSample(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return bioRequestBasedTransferDAO.getProjectBasedOnSample(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Object> getProjectBasedOnSite(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return bioRequestBasedTransferDAO.getProjectBasedOnSite(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Object> getReason(final UserInfo userInfo) throws Exception {
		return bioRequestBasedTransferDAO.getReason(userInfo);
	}

	@Override
	public ResponseEntity<Object> getRequestAcceptanceType(final UserInfo userInfo) throws Exception {
		return bioRequestBasedTransferDAO.getRequestAcceptanceType(userInfo);
	}

	@Override
	public ResponseEntity<Object> getRequestBasedProjectSampleParentLoad(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {
		return bioRequestBasedTransferDAO.getRequestBasedProjectSampleParentLoad(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Object> getSampleConditionStatus(final UserInfo userInfo) throws Exception {
		return bioRequestBasedTransferDAO.getSampleConditionStatus(userInfo);
	}

	@Override
	public ResponseEntity<Object> getSampleReceivingDetails(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return bioRequestBasedTransferDAO.getSampleReceivingDetails(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Object> getStorageCondition(final UserInfo userInfo) throws Exception {
		return bioRequestBasedTransferDAO.getStorageCondition(userInfo);
	}

	@Override
	public ResponseEntity<Object> getStorageType(final UserInfo userInfo) throws Exception {
		return bioRequestBasedTransferDAO.getStorageType(userInfo);
	}

	@Override
	public ResponseEntity<Object> getTransferType(final UserInfo userInfo) throws Exception {
		return bioRequestBasedTransferDAO.getTransferType(userInfo);
	}

	@Override
	public ResponseEntity<Object> getTransferTypeBasedFormNo(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {
		return bioRequestBasedTransferDAO.getTransferTypeBasedFormNo(inputMap, userInfo);

	}

	@Override
	public ResponseEntity<Object> getTransferTypeRecord(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return bioRequestBasedTransferDAO.getTransferTypeRecord(inputMap, userInfo);

	}

	@Override
	public ResponseEntity<Object> getUsersBasedOnSite(final UserInfo userInfo) throws Exception {
		return bioRequestBasedTransferDAO.getUsersBasedOnSite(userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> requestBasedTransfer(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		// TODO Auto-generated method stub
		return bioRequestBasedTransferDAO.requestBasedTransfer(inputMap, userInfo);
	}

	@Override
	@Transactional
	public ResponseEntity<Object> updateBioRequestBasedTransfer(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {
		return bioRequestBasedTransferDAO.updateBioRequestBasedTransfer(inputMap, userInfo);
	}

	@Override
	@Transactional
	public ResponseEntity<Object> updateSampleCondition(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return bioRequestBasedTransferDAO.updateSampleCondition(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Object> getbioparentbasedsampleandvolume(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		return bioRequestBasedTransferDAO.getbioparentbasedsampleandvolume(inputMap, userInfo);
	}
	
	@Override
	public ResponseEntity<Object> checkAccessibleSamples(final int nbioRequestBasedTransferCode, final UserInfo userInfo)
			throws Exception {
		return bioRequestBasedTransferDAO.checkAccessibleSamples(nbioRequestBasedTransferCode, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> undoDisposeSamples(int nbioRequestBasedeTransferCode,
			String nbioRequestBasedTransferDetailsCode, UserInfo userInfo) throws Exception {
		
		return bioRequestBasedTransferDAO.undoDisposeSamples(nbioRequestBasedeTransferCode,nbioRequestBasedTransferDetailsCode, userInfo);
	}

}
