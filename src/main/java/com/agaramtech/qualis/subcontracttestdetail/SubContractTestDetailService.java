package com.agaramtech.qualis.subcontracttestdetail;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.subcontracttestdetail.pojo.SubContractorTestDetail;


public interface SubContractTestDetailService {

	
	public ResponseEntity<Object> getSubContractTestDetail(UserInfo userInfo) throws Exception;
	public ResponseEntity<Object> getSubcontractorBytest(final int ntestcode,final int nsubcontractortestdetailcode,UserInfo userInfo)throws Exception;
	public ResponseEntity<Object> updateSentSubContractTestdetails(final SubContractorTestDetail updateSubContracttestdetails,UserInfo userInfo)throws Exception;
	public ResponseEntity<Object> updateReceiveSTTSubContractTest(final SubContractorTestDetail updateSubContracttestdetails,UserInfo userInfo)throws Exception;
	public ResponseEntity<Object> updateReceiveResultSubContractTest(final SubContractorTestDetail updateSubContracttestdetails,UserInfo userInfo)throws Exception;
	public ResponseEntity<Object> updateSubcontractorTestFile(MultipartHttpServletRequest request, final UserInfo objUserInfo) throws Exception;
	public ResponseEntity<Object> editSubcontractorTestFile(final SubContractorTestDetail objSubContractorTestDetail, final UserInfo objUserInfo) throws Exception;
	public ResponseEntity<Object> viewSubcontractorSampleDetail(final SubContractorTestDetail objSubContractorTestDetail, final UserInfo objUserInfo) throws Exception;
	public Map<String, Object> viewSubcontractorTestFile(SubContractorTestDetail objSubContractorTestDetail, UserInfo userInfo) throws Exception;

}
