package com.agaramtech.qualis.biobank.service.bgsiecatalogue;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.agaramtech.qualis.biobank.service.processedsamplereceiving.ProcessedSampleReceivingDAO;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.UserInfo;

@Transactional(readOnly = true, rollbackFor = Exception.class)
@Service
public class BGSiECatalogueServiceImpl implements BGSiECatalogueService {
	
	private final BGSiECatalogueDAO objBGSiECatalogueDAO;
	private final CommonFunction commonFunction;

	/**
	 * This constructor injection method is used to pass the object dependencies to
	 * the class properties.
	 * 
	 * @param objBGSiECatalogueDAO BGSiECatalogueDAO Interface
	 * @param commonFunction              CommonFunction holding common utility
	 *                                    functions
	 */
	public BGSiECatalogueServiceImpl(BGSiECatalogueDAO objBGSiECatalogueDAO,
			CommonFunction commonFunction) {
		this.objBGSiECatalogueDAO = objBGSiECatalogueDAO;
		this.commonFunction = commonFunction;
	}

	@Override
	public ResponseEntity<Object> getBGSiECatalogue(final Map<String, Object> inputMap,final UserInfo userInfo) throws Exception {
		return objBGSiECatalogueDAO.getBGSiECatalogue(inputMap, userInfo);
	}
	
	@Override
	public ResponseEntity<Object> getBGSiECatalogueByFilterSubmit(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		return objBGSiECatalogueDAO.getBGSiECatalogueByFilterSubmit(inputMap,userInfo);
	}
	

	@Override
	public ResponseEntity<Object> getComboDataForCatalogue(final Map<String, Object> inputMap,final UserInfo userInfo)
			throws Exception {
		return objBGSiECatalogueDAO.getComboDataForCatalogue(inputMap,userInfo) ;
	}

	@Override
	public ResponseEntity<Object> getAggregatedDataForCatalogue(final Map<String, Object> inputMap,final UserInfo userInfo)
			throws Exception {
		return objBGSiECatalogueDAO.getAggregatedDataForCatalogue(inputMap,userInfo);
	}

	@Override
	public ResponseEntity<Object> getDetailedDataForCatalogue(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		return objBGSiECatalogueDAO.getDetailedDataForCatalogue(inputMap,userInfo);
	}
	
	@Transactional
	@Override
	public ResponseEntity<Object> createBGSiECatalogueRequest(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		return objBGSiECatalogueDAO.createBGSiECatalogueRequest(inputMap,userInfo);
	}

	@Override
	public ResponseEntity<Map<String, Object>> getActiveBGSiECatalogueRequestForm(Map<String, Object> inputMap,
			UserInfo userInfo) throws Exception {
		return objBGSiECatalogueDAO.getActiveBGSiECatalogueRequestForm(inputMap,userInfo);
	}

	@Override
	public ResponseEntity<Object> getSiteComboForProject(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		return objBGSiECatalogueDAO.getSiteComboForProject(inputMap,userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> sendBGSiECatalogueRequest(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		return objBGSiECatalogueDAO.sendBGSiECatalogueRequest(inputMap,userInfo);
	}
	@Transactional
	@Override
	public ResponseEntity<Object> cancelBGSiECatalogueRequest(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		return objBGSiECatalogueDAO.cancelBGSiECatalogueRequest(inputMap,userInfo);
	}

	@Override
	public ResponseEntity<Object> getProductComboDataForSampleAdd(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		return objBGSiECatalogueDAO.getProductComboDataForSampleAdd(inputMap,userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> createBGSiECatalogueRequestSample(final Map<String, Object> inputMap,final UserInfo userInfo)
			throws Exception {
		return objBGSiECatalogueDAO.createBGSiECatalogueRequestSample(inputMap,userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> updateBGSiECatalogueRequestSample(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		return objBGSiECatalogueDAO.updateBGSiECatalogueRequestSample(inputMap,userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> deleteBGSiECatalogueRequestSample(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		return objBGSiECatalogueDAO.deleteBGSiECatalogueRequestSample(inputMap,userInfo);
	}

	@Override
	public ResponseEntity<Map<String, Object>> getActiveSampleDetail(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		return objBGSiECatalogueDAO.getActiveSampleDetail(inputMap,userInfo);
	}
	
	@Override
	public ResponseEntity<Object> getBioSampleAvailability(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
	    return this.objBGSiECatalogueDAO.getBioSampleAvailability(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Object> getParentSamples(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
	    return this.objBGSiECatalogueDAO.getParentSamples(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Object> getSubjectCountsByProductAndProject(UserInfo userInfo)
			throws Exception {
		return this.objBGSiECatalogueDAO.getSubjectCountsByProductAndProject(userInfo);
	}

	@Override
	public ResponseEntity<Object> getSubjectCountsByProductAndDisease(UserInfo userInfo)
			throws Exception {
		return this.objBGSiECatalogueDAO.getSubjectCountsByProductAndDisease(userInfo);
	}

	@Override
	public ResponseEntity<Object> getBioSampleTypeCombo(final int nselectedprojectcode, final int nselectedsitecode,final UserInfo userinfo)
			throws Exception {
		return this.objBGSiECatalogueDAO.getBioSampleTypeCombo(nselectedprojectcode, nselectedsitecode, userinfo);
	}


	
}
