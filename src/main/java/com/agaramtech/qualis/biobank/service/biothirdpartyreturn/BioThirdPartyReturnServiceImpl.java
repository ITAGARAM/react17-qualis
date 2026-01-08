package com.agaramtech.qualis.biobank.service.biothirdpartyreturn;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.agaramtech.qualis.global.UserInfo;

@Transactional(readOnly = true, rollbackFor = Exception.class)
@Service
public class BioThirdPartyReturnServiceImpl implements BioThirdPartyReturnService {

	private final BioThirdPartyReturnDAO bioThirdPartyReturnDAO;

	public BioThirdPartyReturnServiceImpl(BioThirdPartyReturnDAO bioThirdPartyReturnDAO) {
		this.bioThirdPartyReturnDAO = bioThirdPartyReturnDAO;
	}

	@Override
	public ResponseEntity<Object> getBioThirdPartyReturn(final Map<String, Object> inputMap,
			final int nbioThirdPartyReturnCode, final UserInfo userInfo) throws Exception {
		return bioThirdPartyReturnDAO.getBioThirdPartyReturn(inputMap, nbioThirdPartyReturnCode, userInfo);
	}

	@Override
	public ResponseEntity<Object> getFormNumberDetails(final int noriginSiteCode, final UserInfo userInfo)
			throws Exception {
		return bioThirdPartyReturnDAO.getFormNumberDetails(noriginSiteCode, userInfo);
	}

	@Override
	public ResponseEntity<Object> getThirdPartyFormAcceptanceDetails(final int nbioThirdPartyFormAccetanceCode,
			final UserInfo userInfo) throws Exception {
		return bioThirdPartyReturnDAO.getThirdPartyFormAcceptanceDetails(nbioThirdPartyFormAccetanceCode, userInfo);
	}

	@Override
	@Transactional
	public ResponseEntity<Object> createBioThirdPartyReturn(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return bioThirdPartyReturnDAO.createBioThirdPartyReturn(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Object> getActiveBioThirdPartyReturn(final int nbioThirdPartyReturnCode, final int saveType,
			final UserInfo userInfo) throws Exception {
		return bioThirdPartyReturnDAO.getActiveBioThirdPartyReturn(nbioThirdPartyReturnCode, saveType, userInfo);
	}

	@Override
	public ResponseEntity<Object> getActiveBioThirdPartyReturnById(final int nbioThirdPartyReturnCode,
			final UserInfo userInfo) throws Exception {
		return bioThirdPartyReturnDAO.getActiveBioThirdPartyReturnById(nbioThirdPartyReturnCode, userInfo);
	}

	@Override
	@Transactional
	public ResponseEntity<Object> updateBioThirdPartyReturn(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return bioThirdPartyReturnDAO.updateBioThirdPartyReturn(inputMap, userInfo);
	}

	@Override
	public int findStatusThirdPartyReturn(final int nbioThirdPartyReturnCode, final UserInfo userInfo)
			throws Exception {
		return bioThirdPartyReturnDAO.findStatusThirdPartyReturn(nbioThirdPartyReturnCode, userInfo);
	}

	@Override
	public ResponseEntity<Object> getValidateFormDetails(final int nbioThirdPartyReturnCode, final UserInfo userInfo)
			throws Exception {
		return bioThirdPartyReturnDAO.getValidateFormDetails(nbioThirdPartyReturnCode, userInfo);
	}

	@Override
	public ResponseEntity<Object> getStorageCondition(final UserInfo userInfo) throws Exception {
		return bioThirdPartyReturnDAO.getStorageCondition(userInfo);
	}

	@Override
	public ResponseEntity<Object> getUsersBasedOnSite(final UserInfo userInfo) throws Exception {
		return bioThirdPartyReturnDAO.getUsersBasedOnSite(userInfo);
	}

	@Override
	public ResponseEntity<Object> getCourier(final UserInfo userInfo) throws Exception {
		return bioThirdPartyReturnDAO.getCourier(userInfo);
	}

	@Override
	@Transactional
	public ResponseEntity<Object> createValidationBioThirdPartyReturn(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {
		return bioThirdPartyReturnDAO.createValidationBioThirdPartyReturn(inputMap, userInfo);
	}

	@Override
	@Transactional
	public ResponseEntity<Object> returnThirdPartyReturn(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return bioThirdPartyReturnDAO.returnThirdPartyReturn(inputMap, userInfo);
	}

	@Override
	@Transactional
	public ResponseEntity<Object> cancelThirdPartyReturn(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return bioThirdPartyReturnDAO.cancelThirdPartyReturn(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Object> addThirdPartyReturnDetails(final int nbioThirdPartyReturnCode,
			final int noriginSiteCode, final UserInfo userInfo) throws Exception {
		return bioThirdPartyReturnDAO.addThirdPartyReturnDetails(nbioThirdPartyReturnCode, noriginSiteCode, userInfo);
	}

	@Override
	@Transactional
	public ResponseEntity<Object> createChildBioThirdPartyReturn(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {
		return bioThirdPartyReturnDAO.createChildBioThirdPartyReturn(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Object> acceptRejectThirdPartyReturnSlide(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {
		return bioThirdPartyReturnDAO.acceptRejectThirdPartyReturnSlide(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Object> getSampleConditionStatus(final UserInfo userInfo) throws Exception {
		return bioThirdPartyReturnDAO.getSampleConditionStatus(userInfo);
	}

	@Override
	public ResponseEntity<Object> getReason(final UserInfo userInfo) throws Exception {
		return bioThirdPartyReturnDAO.getReason(userInfo);
	}

	@Override
	@Transactional
	public ResponseEntity<Object> updateSampleCondition(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return bioThirdPartyReturnDAO.updateSampleCondition(inputMap, userInfo);
	}

	@Override
	@Transactional
	public ResponseEntity<Object> deleteChildThirdPartyReturn(final int nbioThirdPartyReturnCode,
			final String nbioThirdPartyReturnDetailsCode, final UserInfo userInfo) throws Exception {
		return bioThirdPartyReturnDAO.deleteChildThirdPartyReturn(nbioThirdPartyReturnCode,
				nbioThirdPartyReturnDetailsCode, userInfo);
	}

	@Override
	@Transactional
	public ResponseEntity<Object> disposeSamples(final int nbioThirdPartyReturnCode,
			final String nbioThirdPartyReturnDetailsCode, final UserInfo userInfo) throws Exception {
		return bioThirdPartyReturnDAO.disposeSamples(nbioThirdPartyReturnCode, nbioThirdPartyReturnDetailsCode,
				userInfo);
	}

	@Override
	@Transactional
	public ResponseEntity<Object> undoDisposeSamples(final int nbioThirdPartyReturnCode,
			final String nbioThirdPartyReturnDetailsCode, final UserInfo userInfo) throws Exception {
		return bioThirdPartyReturnDAO.undoDisposeSamples(nbioThirdPartyReturnCode, nbioThirdPartyReturnDetailsCode,
				userInfo);
	}

}
