package com.agaramtech.qualis.subcontracttestdetail;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.subcontracttestdetail.pojo.SubContractorTestDetail;

import org.springframework.transaction.annotation.Transactional;


@Transactional(readOnly = true, rollbackFor=Exception.class)
@Service
public class SubContractTestDetailServiceImpl implements SubContractTestDetailService {
	
	private final SubContractTestDetailDAO objSubContractTestDetailDAO;
	private final CommonFunction commonFunction;	
	

	public SubContractTestDetailServiceImpl(SubContractTestDetailDAO objSubContractTestDetailDAO, CommonFunction commonFunction) {
		this.objSubContractTestDetailDAO = objSubContractTestDetailDAO;
		this.commonFunction = commonFunction;
	}

	
	public ResponseEntity<Object> getSubContractTestDetail(UserInfo userInfo) throws Exception{
		return objSubContractTestDetailDAO.getSubContractTestDetail(userInfo);
	}
	
	public ResponseEntity<Object> getSubcontractorBytest(final int ntestcode,final int nsubcontractortestdetailcode,UserInfo userInfo)throws Exception{
		return  objSubContractTestDetailDAO.getSubcontractorBytest(ntestcode,nsubcontractortestdetailcode,userInfo);

	}
	
	@Transactional
	public ResponseEntity<Object> updateSentSubContractTestdetails(final SubContractorTestDetail updateSubContractSamplesdetails,UserInfo userInfo)throws Exception{
		return  objSubContractTestDetailDAO.updateSentSubContractTestdetails(updateSubContractSamplesdetails,userInfo);
	}
	@Transactional
	public ResponseEntity<Object> updateReceiveSTTSubContractTest(final SubContractorTestDetail updateSubContracttestdetails,UserInfo userInfo)throws Exception{
		return  objSubContractTestDetailDAO.updateReceiveSTTSubContractTest(updateSubContracttestdetails,userInfo);
	}
	@Transactional
	public ResponseEntity<Object> updateReceiveResultSubContractTest(final SubContractorTestDetail updateSubContracttestdetails,UserInfo userInfo)throws Exception{
		return objSubContractTestDetailDAO.updateReceiveResultSubContractTest(updateSubContracttestdetails,userInfo);

	}

	@Transactional
	public ResponseEntity<Object> updateSubcontractorTestFile(MultipartHttpServletRequest request, final UserInfo objUserInfo)throws Exception {
		return objSubContractTestDetailDAO.updateSubcontractorTestFile(request, objUserInfo);
	}
	@Transactional
	public ResponseEntity<Object> editSubcontractorTestFile(final SubContractorTestDetail objSubContractorTestDetail, final UserInfo userInfo) throws Exception{
		return objSubContractTestDetailDAO.editSubcontractorTestFile(objSubContractorTestDetail, userInfo);
	}
	public ResponseEntity<Object> viewSubcontractorSampleDetail(final SubContractorTestDetail objSubContractorTestDetail, final UserInfo userInfo) throws Exception{
		return objSubContractTestDetailDAO.viewSubcontractorSampleDetail(objSubContractorTestDetail,userInfo);
	}
	public Map<String, Object> viewSubcontractorTestFile(SubContractorTestDetail objSubContractorTestDetail, UserInfo userInfo) throws Exception{
		return objSubContractTestDetailDAO.viewSubcontractorTestFile(objSubContractorTestDetail,userInfo);
	}
}
