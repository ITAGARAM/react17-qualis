package com.agaramtech.qualis.biobank.service.biothirdpartyecatalogue;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.agaramtech.qualis.global.UserInfo;

@Transactional(readOnly = true, rollbackFor = Exception.class)
@Service
public class BioThirdPartyECatalogueServiceImpl implements BioThirdPartyECatalogueService {

	private final BioThirdPartyECatalogueDAO objBioThirdPartyECatalogueDAO;

	/**
	 * This constructor injection method is used to pass the object dependencies to
	 * the class properties.
	 * 
	 * @param objBGSiECatalogueDAO BGSiECatalogueDAO Interface
	 */
	public BioThirdPartyECatalogueServiceImpl(BioThirdPartyECatalogueDAO objBioThirdPartyECatalogueDAO) {
		this.objBioThirdPartyECatalogueDAO = objBioThirdPartyECatalogueDAO;
	}

	@Override
	public ResponseEntity<Object> getBioThirdPartyECatalogue(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {
		return objBioThirdPartyECatalogueDAO.getBioThirdPartyECatalogue(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Object> getThirdPartyECatalogueByFilterSubmit(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		return objBioThirdPartyECatalogueDAO.getThirdPartyECatalogueByFilterSubmit(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Object> getComboDataForCatalogue(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return objBioThirdPartyECatalogueDAO.getComboDataForCatalogue(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Object> getAggregatedDataForCatalogue(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {
		return objBioThirdPartyECatalogueDAO.getAggregatedDataForCatalogue(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Object> getDetailedDataForCatalogue(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		return objBioThirdPartyECatalogueDAO.getDetailedDataForCatalogue(inputMap, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> createThirdPartyECatalogueRequest(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		return objBioThirdPartyECatalogueDAO.createThirdPartyECatalogueRequest(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Map<String, Object>> getActiveThirdPartyECatalogueRequestForm(Map<String, Object> inputMap,
			UserInfo userInfo) throws Exception {
		return objBioThirdPartyECatalogueDAO.getActiveThirdPartyECatalogueRequestForm(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Object> getSiteComboForProject(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		return objBioThirdPartyECatalogueDAO.getSiteComboForProject(inputMap, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> sendThirdPartyECatalogueRequest(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		return objBioThirdPartyECatalogueDAO.sendThirdPartyECatalogueRequest(inputMap, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> cancelThirdPartyECatalogueRequest(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		return objBioThirdPartyECatalogueDAO.cancelThirdPartyECatalogueRequest(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Object> getProductComboDataForSampleAdd(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		return objBioThirdPartyECatalogueDAO.getProductComboDataForSampleAdd(inputMap, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> createThirdPartyECatalogueRequestSample(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {
		return objBioThirdPartyECatalogueDAO.createThirdPartyECatalogueRequestSample(inputMap, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> updateThirdPartyECatalogueRequestSample(Map<String, Object> inputMap,
			UserInfo userInfo) throws Exception {
		return objBioThirdPartyECatalogueDAO.updateThirdPartyECatalogueRequestSample(inputMap, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> deleteThirdPartyECatalogueRequestSample(Map<String, Object> inputMap,
			UserInfo userInfo) throws Exception {
		return objBioThirdPartyECatalogueDAO.deleteThirdPartyECatalogueRequestSample(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Map<String, Object>> getActiveSampleDetail(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		return objBioThirdPartyECatalogueDAO.getActiveSampleDetail(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Object> getBioSampleAvailability(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		return this.objBioThirdPartyECatalogueDAO.getBioSampleAvailability(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Object> getParentSamples(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		return this.objBioThirdPartyECatalogueDAO.getParentSamples(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Object> getSubjectCountsByProductAndProject(UserInfo userInfo) throws Exception {
		return this.objBioThirdPartyECatalogueDAO.getSubjectCountsByProductAndProject(userInfo);
	}

	@Override
	public ResponseEntity<Object> getSubjectCountsByProductAndDisease(UserInfo userInfo) throws Exception {
		return this.objBioThirdPartyECatalogueDAO.getSubjectCountsByProductAndDisease(userInfo);
	}

	@Override
	public ResponseEntity<Object> getBioSampleTypeCombo(final int nselectedprojectcode, final UserInfo userinfo)
			throws Exception {
		return this.objBioThirdPartyECatalogueDAO.getBioSampleTypeCombo(nselectedprojectcode, userinfo);
	}

}
