package com.agaramtech.qualis.biobank.service.biobankecatalogueexternal;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.UserInfo;

@Transactional(readOnly = true, rollbackFor = Exception.class)
@Service
public class BiobankECatalogueExternalServiceImpl implements BiobankECatalogueExternalService {
	
	private final BiobankECatalogueExternalDAO objBiobankECatalogueExternalDAO;
	private final CommonFunction commonFunction;

	/**
	 * This constructor injection method is used to pass the object dependencies to
	 * the class properties.
	 * 
	 * @param objBiobankECatalogueExternalDAO BiobankECatalogueExternalDAO Interface
	 * @param commonFunction              CommonFunction holding common utility
	 *                                    functions
	 */
	public BiobankECatalogueExternalServiceImpl(BiobankECatalogueExternalDAO objBiobankECatalogueExternalDAO,
			CommonFunction commonFunction) {
		this.objBiobankECatalogueExternalDAO = objBiobankECatalogueExternalDAO;
		this.commonFunction = commonFunction;
	}

	@Override
	public ResponseEntity<Object> getBiobankECatalogueExternal(final Map<String, Object> inputMap,final UserInfo userInfo) throws Exception {
		return objBiobankECatalogueExternalDAO.getBiobankECatalogueExternal(inputMap, userInfo);
	}
	
	@Override
	public ResponseEntity<Object> getBiobankECatalogueExternalByFilterSubmit(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		return objBiobankECatalogueExternalDAO.getBiobankECatalogueExternalByFilterSubmit(inputMap,userInfo);
	}
	

	@Override
	public ResponseEntity<Object> getComboDataForCatalogue(final Map<String, Object> inputMap,final UserInfo userInfo)
			throws Exception {
		return objBiobankECatalogueExternalDAO.getComboDataForCatalogue(inputMap,userInfo) ;
	}
	
	@Override
	public ResponseEntity<Object> getComboDataForCatalogueForAdd(final Map<String, Object> inputMap,final UserInfo userInfo)
			throws Exception {
		return objBiobankECatalogueExternalDAO.getComboDataForCatalogueForAdd(inputMap,userInfo) ;
	}

	@Override
	public ResponseEntity<Object> getAggregatedDataForCatalogue(final Map<String, Object> inputMap,final UserInfo userInfo)
			throws Exception {
		return objBiobankECatalogueExternalDAO.getAggregatedDataForCatalogue(inputMap,userInfo);
	}

	@Override
	public ResponseEntity<Object> getDetailedDataForCatalogue(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		return objBiobankECatalogueExternalDAO.getDetailedDataForCatalogue(inputMap,userInfo);
	}
	
	@Transactional
	@Override
	public ResponseEntity<Object> createBiobankECatalogueExternalRequest(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		return objBiobankECatalogueExternalDAO.createBiobankECatalogueExternalRequest(inputMap,userInfo);
	}

	@Override
	public ResponseEntity<Map<String, Object>> getActiveBiobankECatalogueExternalRequestForm(Map<String, Object> inputMap,
			UserInfo userInfo) throws Exception {
		return objBiobankECatalogueExternalDAO.getActiveBiobankECatalogueExternalRequestForm(inputMap,userInfo);
	}

	@Override
	public ResponseEntity<Object> getSiteComboForProject(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		return objBiobankECatalogueExternalDAO.getSiteComboForProject(inputMap,userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> sendBiobankECatalogueExternalRequest(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		return objBiobankECatalogueExternalDAO.sendBiobankECatalogueExternalRequest(inputMap,userInfo);
	}
	@Transactional
	@Override
	public ResponseEntity<Object> cancelBiobankECatalogueExternalRequest(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		return objBiobankECatalogueExternalDAO.cancelBiobankECatalogueExternalRequest(inputMap,userInfo);
	}

	@Override
	public ResponseEntity<Object> getProductComboDataForSampleAdd(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		return objBiobankECatalogueExternalDAO.getProductComboDataForSampleAdd(inputMap,userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> createBiobankECatalogueExternalRequestSample(final Map<String, Object> inputMap,final UserInfo userInfo)
			throws Exception {
		return objBiobankECatalogueExternalDAO.createBiobankECatalogueExternalRequestSample(inputMap,userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> updateBiobankECatalogueExternalRequestSample(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		return objBiobankECatalogueExternalDAO.updateBiobankECatalogueExternalRequestSample(inputMap,userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> deleteBiobankECatalogueExternalRequestSample(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		return objBiobankECatalogueExternalDAO.deleteBiobankECatalogueExternalRequestSample(inputMap,userInfo);
	}

	@Override
	public ResponseEntity<Map<String, Object>> getActiveSampleDetail(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		return objBiobankECatalogueExternalDAO.getActiveSampleDetail(inputMap,userInfo);
	}
	
	@Override
	public ResponseEntity<Object> getBioSampleAvailability(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
	    return this.objBiobankECatalogueExternalDAO.getBioSampleAvailability(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Object> getParentSamples(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
	    return this.objBiobankECatalogueExternalDAO.getParentSamples(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Object> getSubjectCountsByProductAndProject(UserInfo userInfo)
			throws Exception {
		return this.objBiobankECatalogueExternalDAO.getSubjectCountsByProductAndProject(userInfo);
	}

	@Override
	public ResponseEntity<Object> getSubjectCountsByProductAndDisease(UserInfo userInfo)
			throws Exception {
		return this.objBiobankECatalogueExternalDAO.getSubjectCountsByProductAndDisease(userInfo);
	}

	@Override
	public ResponseEntity<Object> getBioSampleTypeCombo(final int nselectedprojectcode, final int nselectedsitecode,final UserInfo userinfo)
			throws Exception {
		return this.objBiobankECatalogueExternalDAO.getBioSampleTypeCombo(nselectedprojectcode, nselectedsitecode, userinfo);
	}


	
}
