package com.agaramtech.qualis.biobank.service.bgsiexternalapi;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.UserInfo;

@Transactional(readOnly = true, rollbackFor = Exception.class)
@Service
public class BGSIExternalApiServiceImpl implements BGSIExternalApiService{

	private final BGSIExternalApiDAO objBGSIExternalApiDAO;
	private final CommonFunction commonFunction;

	/**
	 * This constructor injection method is used to pass the object dependencies to
	 * the class properties.
	 * 
	 * @param objBGSiECatalogueDAO BGSiECatalogueDAO Interface
	 * @param commonFunction       CommonFunction holding common utility functions
	 */
	public BGSIExternalApiServiceImpl(BGSIExternalApiDAO objBGSIExternalApiDAO, CommonFunction commonFunction) {
		this.objBGSIExternalApiDAO = objBGSIExternalApiDAO;
		this.commonFunction = commonFunction;
	}

	@Transactional
	@Override
	public ResponseEntity<Object> updatepatientconsent(final Map<String, Object> inputMap, final UserInfo userinfo)
			throws Exception {
		return objBGSIExternalApiDAO.updatepatientconsent(inputMap, userinfo);
	}
	
	@Transactional
	@Override
	public ResponseEntity<Object> bioExternalTransferData(final UserInfo userinfo) throws Exception {
		return objBGSIExternalApiDAO.bioExternalTransferData(userinfo);
	}
	
	@Transactional
	@Override
	public ResponseEntity<Object> acknowledgeTransferred(final Map<String, Object> inputMap, final UserInfo userinfo)
			throws Exception {
		return objBGSIExternalApiDAO.acknowledgeTransferred(inputMap, userinfo);
	}
	
	@Transactional
	@Override
	public ResponseEntity<Object> receiveOriginalSample(final Map<String, Object> inputMap, final UserInfo userinfo) throws Exception {
		return objBGSIExternalApiDAO.receiveOriginalSample(inputMap, userinfo);
	}
	
	@Transactional
	@Override
	public String getAuthorisationTokenForNGS(final UserInfo userinfo) throws Exception {
		return objBGSIExternalApiDAO.getAuthorisationTokenForNGS(userinfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> sendAcceptedThirdPartySamplestoNGS(final Map<String, Object> inputMap,final UserInfo userinfo) throws Exception {
		return objBGSIExternalApiDAO.sendAcceptedThirdPartySamplestoNGS(inputMap, userinfo);
	}
	
	@Transactional
	@Override
	public ResponseEntity<Object> getTotalRepoIdBasedSampleType(final Map<String, Object> inputMap) throws Exception {
		return objBGSIExternalApiDAO.getTotalRepoIdBasedSampleType(inputMap);
	}
	
	@Transactional
	@Override
	public ResponseEntity<Object> getTotalRepoIdRequestedAndSent(final Map<String, Object> inputMap) throws Exception {
		return objBGSIExternalApiDAO.getTotalRepoIdRequestedAndSent(inputMap);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> getTotalSubjectid(final Map<String, Object> inputMap) throws Exception {
		return objBGSIExternalApiDAO.getTotalSubjectid(inputMap);
	}
	
	@Transactional
	@Override
	public ResponseEntity<Object> getStorageCapacity(final Map<String, Object> inputMap) throws Exception {
		return objBGSIExternalApiDAO.getStorageCapacity(inputMap);
	}
	
	@Transactional
	@Override
	public ResponseEntity<Object> receiveDNASample(final Map<String, Object> inputMap, final UserInfo userinfo) throws Exception {
		return objBGSIExternalApiDAO.receiveDNASample(inputMap, userinfo);
	}
	
}
