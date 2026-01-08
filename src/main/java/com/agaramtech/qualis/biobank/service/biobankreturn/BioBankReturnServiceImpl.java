package com.agaramtech.qualis.biobank.service.biobankreturn;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.agaramtech.qualis.global.UserInfo;

@Transactional(readOnly = true, rollbackFor = Exception.class)
@Service
public class BioBankReturnServiceImpl implements BioBankReturnService {

	private final BioBankReturnDAO bioBankReturnDAO;

	public BioBankReturnServiceImpl(BioBankReturnDAO bioBankReturnDAO) {
		this.bioBankReturnDAO = bioBankReturnDAO;
	}

	@Override
	public ResponseEntity<Object> getBioBankReturn(final Map<String, Object> inputMap, final int nbioBankReturnCode,
			final UserInfo userInfo) throws Exception {
		return bioBankReturnDAO.getBioBankReturn(inputMap, nbioBankReturnCode, userInfo);
	}

	@Override
	public ResponseEntity<Object> getFormNumberDetails(final int noriginSiteCode, final UserInfo userInfo)
			throws Exception {
		return bioBankReturnDAO.getFormNumberDetails(noriginSiteCode, userInfo);
	}

	@Override
	public ResponseEntity<Object> getFormAcceptanceDetails(final int nbioFormAccetanceCode, final UserInfo userInfo)
			throws Exception {
		return bioBankReturnDAO.getFormAcceptanceDetails(nbioFormAccetanceCode, userInfo);
	}

	@Override
	@Transactional
	public ResponseEntity<Object> createBioBankReturn(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return bioBankReturnDAO.createBioBankReturn(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Object> getActiveBioBankReturn(final int nbioBankReturnCode, final int saveType,
			final UserInfo userInfo) throws Exception {
		return bioBankReturnDAO.getActiveBioBankReturn(nbioBankReturnCode, saveType, userInfo);
	}

	@Override
	public ResponseEntity<Object> getActiveBioBankReturnById(final int nbioBankReturnCode, final UserInfo userInfo)
			throws Exception {
		return bioBankReturnDAO.getActiveBioBankReturnById(nbioBankReturnCode, userInfo);
	}

	@Override
	@Transactional
	public ResponseEntity<Object> updateBioBankReturn(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return bioBankReturnDAO.updateBioBankReturn(inputMap, userInfo);
	}

	@Override
	public int findStatusBankReturn(final int nbioBankReturnCode, final UserInfo userInfo) throws Exception {
		return bioBankReturnDAO.findStatusBankReturn(nbioBankReturnCode, userInfo);
	}

	@Override
	public ResponseEntity<Object> getValidateFormDetails(final int nbioBankReturnCode, final UserInfo userInfo)
			throws Exception {
		return bioBankReturnDAO.getValidateFormDetails(nbioBankReturnCode, userInfo);
	}

	@Override
	public ResponseEntity<Object> getStorageCondition(final UserInfo userInfo) throws Exception {
		return bioBankReturnDAO.getStorageCondition(userInfo);
	}

	@Override
	public ResponseEntity<Object> getUsersBasedOnSite(final UserInfo userInfo) throws Exception {
		return bioBankReturnDAO.getUsersBasedOnSite(userInfo);
	}

	@Override
	public ResponseEntity<Object> getCourier(final UserInfo userInfo) throws Exception {
		return bioBankReturnDAO.getCourier(userInfo);
	}

	@Override
	@Transactional
	public ResponseEntity<Object> createValidationBioBankReturn(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {
		return bioBankReturnDAO.createValidationBioBankReturn(inputMap, userInfo);
	}

	@Override
	@Transactional
	public ResponseEntity<Object> returnBankReturn(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return bioBankReturnDAO.returnBankReturn(inputMap, userInfo);
	}

	@Override
	@Transactional
	public ResponseEntity<Object> cancelBankReturn(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return bioBankReturnDAO.cancelBankReturn(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Object> addBankReturnDetails(final int nbioBankReturnCode, final int noriginSiteCode,
			final UserInfo userInfo) throws Exception {
		return bioBankReturnDAO.addBankReturnDetails(nbioBankReturnCode, noriginSiteCode, userInfo);
	}

	@Override
	@Transactional
	public ResponseEntity<Object> createChildBioBankReturn(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return bioBankReturnDAO.createChildBioBankReturn(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Object> acceptRejectBankReturnSlide(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {
		return bioBankReturnDAO.acceptRejectBankReturnSlide(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Object> getSampleConditionStatus(final UserInfo userInfo) throws Exception {
		return bioBankReturnDAO.getSampleConditionStatus(userInfo);
	}

	@Override
	public ResponseEntity<Object> getReason(final UserInfo userInfo) throws Exception {
		return bioBankReturnDAO.getReason(userInfo);
	}

	@Override
	@Transactional
	public ResponseEntity<Object> updateSampleCondition(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return bioBankReturnDAO.updateSampleCondition(inputMap, userInfo);
	}

	@Override
	@Transactional
	public ResponseEntity<Object> deleteChildBankReturn(final int nbioBankReturnCode,
			final String nbioBankReturnDetailsCode, final UserInfo userInfo) throws Exception {
		return bioBankReturnDAO.deleteChildBankReturn(nbioBankReturnCode, nbioBankReturnDetailsCode, userInfo);
	}

	@Override
	@Transactional
	public ResponseEntity<Object> disposeSamples(final int nbioBankReturnCode, final String nbioBankReturnDetailsCode,
			final UserInfo userInfo) throws Exception {
		return bioBankReturnDAO.disposeSamples(nbioBankReturnCode, nbioBankReturnDetailsCode, userInfo);
	}

	@Override
	public ResponseEntity<Object> getStorageFreezerData(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return bioBankReturnDAO.getStorageFreezerData(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Object> getStorageStructure(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return bioBankReturnDAO.getStorageStructure(inputMap, userInfo);
	}

	@Override
	@Transactional
	public ResponseEntity<Object> storeSamples(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return bioBankReturnDAO.storeSamples(inputMap, userInfo);
	}

	@Override
	@Transactional
	public ResponseEntity<Object> undoDisposeSamples(final int nbioBankReturnCode,
			final String nbioBankReturnDetailsCode, final UserInfo userInfo) throws Exception {
		return bioBankReturnDAO.undoDisposeSamples(nbioBankReturnCode, nbioBankReturnDetailsCode, userInfo);
	}

}
