package com.agaramtech.qualis.biobank.service.BioEcatalogueReqApproval;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.agaramtech.qualis.biobank.service.biorequestbasedtransfer.BioRequestBasedTransferDAO;
import com.agaramtech.qualis.global.UserInfo;

@Transactional(readOnly = true, rollbackFor = Exception.class)
@Service
public class BioEcatalogueReqApprovalServiceImpl implements BioEcatalogueReqApprovalService {
	
	private final BioEcatalogueReqApprovalDAO bioEcatalogueReqApprovalDAO;

	public BioEcatalogueReqApprovalServiceImpl(final BioEcatalogueReqApprovalDAO bioEcatalogueReqApprovalDAO) {
		this.bioEcatalogueReqApprovalDAO = bioEcatalogueReqApprovalDAO;
	}
	@Override
	public ResponseEntity<Object> getBioEcatalogueReqApproval(final Map<String, Object> inputMap,
			final int nbioRequestBasedTransferCode, final UserInfo userInfo) throws Exception {
		return bioEcatalogueReqApprovalDAO.getBioEcatalogueReqApproval(inputMap, nbioRequestBasedTransferCode, userInfo);
	}
	@Override
	public ResponseEntity<Object> getSelectedBioEcatalogueReqApproval(int nrequestapprovlcode,int ntransfertype,int ntransactionstatus, UserInfo userInfo)  throws Exception{
		return bioEcatalogueReqApprovalDAO.getSelectedBioEcatalogueReqApproval(nrequestapprovlcode,ntransfertype,ntransactionstatus,userInfo);

	}
	
	@Transactional
	@Override
	public ResponseEntity<Object> updatedUserAcceptedVolume(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return bioEcatalogueReqApprovalDAO.updatedUserAcceptedVolume(inputMap, userInfo);
	}
	
	
	@Transactional
	@Override
	public ResponseEntity<Object> updateRejectedRecord(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return bioEcatalogueReqApprovalDAO.updateRejectedRecord(inputMap, userInfo);
	}
	
	@Transactional
	@Override
	public ResponseEntity<Object> updateApproveRecord(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return bioEcatalogueReqApprovalDAO.updateApproveRecord(inputMap, userInfo);
	}
	@Override
	public ResponseEntity<Object> getFilteredRequestDetails(int nbioprojectcode, int nproductcode,
			String sparentsamplecode, int ntransfertypecode, int nsitecode, UserInfo userInfo) throws Exception {
		return bioEcatalogueReqApprovalDAO.getFilteredRequestDetails(nbioprojectcode, nproductcode, sparentsamplecode, ntransfertypecode,  nsitecode, userInfo);

	}
	@Override
	public ResponseEntity<Object> getBioEcatalogueReqApprovalDetails(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		return bioEcatalogueReqApprovalDAO.getBioEcatalogueReqApprovalDetails(inputMap, userInfo);
	}

	
}
