package com.agaramtech.qualis.restcontroller;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.subcontracttestdetail.SubContractTestDetailService;
import com.agaramtech.qualis.subcontracttestdetail.pojo.SubContractorTestDetail;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/subcontracttestdetail")
public class SubContractTestDetailController {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(SubContractTestDetailController.class);

	private RequestContext requestContext;
	final  private SubContractTestDetailService objSubContractTestDetailService;
;
	

	public SubContractTestDetailController(RequestContext requestContext, SubContractTestDetailService objSubContractTestDetailService) {
		super();
		this.requestContext = requestContext;
		this.objSubContractTestDetailService = objSubContractTestDetailService;
	}

	
	@PostMapping(value = "/getSubContractTestDetail")
	public ResponseEntity<Object> getSubContractTestDetail(@RequestBody Map<String, Object> inputMap) throws Exception{
		//try {
			ObjectMapper objMapper = new ObjectMapper();
			objMapper.registerModule(new JavaTimeModule());
			final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class) ;
			return objSubContractTestDetailService.getSubContractTestDetail(userInfo);

	}
	
	@PostMapping(value = "/getSubcontractorBytest")
	public ResponseEntity<Object> getSubcontractorBytest(@RequestBody Map<String, Object> inputMap) throws Exception{
		//try {
			ObjectMapper objMapper = new ObjectMapper();
			objMapper.registerModule(new JavaTimeModule());
			final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
			final int ntestcode = (int) inputMap.get("ntestcode");
			final int nsubcontractortestdetailcode = (int) inputMap.get("nsubcontractortestdetailcode");
			
	
			return objSubContractTestDetailService.getSubcontractorBytest(ntestcode,nsubcontractortestdetailcode,userInfo);

	}
	
	@PostMapping(value = "/updateSentSubContractTestdetails")
	public ResponseEntity<Object> updateSentSubContractTestdetails(@RequestBody Map<String, Object> inputMap) throws Exception{
		//try {
			ObjectMapper objMapper = new ObjectMapper();
			objMapper.registerModule(new JavaTimeModule());
			final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
			final SubContractorTestDetail updateSubContracttestdetails = objMapper.convertValue(inputMap.get("subcontractortestdetail"), SubContractorTestDetail.class);
		
			return objSubContractTestDetailService.updateSentSubContractTestdetails(updateSubContracttestdetails,userInfo);

	}
	
	

	@PostMapping(value = "/updateReceiveSTTSubContractTest")
	public ResponseEntity<Object> updateReceiveSTTSubContractTest(@RequestBody Map<String, Object> inputMap) throws Exception{
		//try {
			ObjectMapper objMapper = new ObjectMapper();
			objMapper.registerModule(new JavaTimeModule());
			final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
			final SubContractorTestDetail updateSubContracttestdetails = objMapper.convertValue(inputMap.get("subcontractortestdetail"), SubContractorTestDetail.class);
		
			return objSubContractTestDetailService.updateReceiveSTTSubContractTest(updateSubContracttestdetails,userInfo);

	}
	
	@PostMapping(value = "/updateReceiveResultSubContractTest")
	public ResponseEntity<Object> updateReceiveResultSubContractTest(@RequestBody Map<String, Object> inputMap) throws Exception{
		//try {
			ObjectMapper objMapper = new ObjectMapper();
			objMapper.registerModule(new JavaTimeModule());
			final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
			final SubContractorTestDetail updateSubContracttestdetails = objMapper.convertValue(inputMap.get("subcontractortestdetail"), SubContractorTestDetail.class);
		
			return objSubContractTestDetailService.updateReceiveResultSubContractTest(updateSubContracttestdetails,userInfo);

	}
	
	@PostMapping(value = "/updateSubcontractorTestFile")
	public ResponseEntity<Object> UpdateSubcontractorTestFile(MultipartHttpServletRequest request) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.readValue(request.getParameter("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return objSubContractTestDetailService.updateSubcontractorTestFile(request, userInfo);

	}
	
	@PostMapping(value = "/editSubcontractorTestFile")
	public ResponseEntity<Object> editSubcontractorTestFile(@RequestBody Map<String, Object> inputMap) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final SubContractorTestDetail objSubContractorTestDetail = objMapper.convertValue(inputMap.get("subcontractortestdetail"), SubContractorTestDetail.class);
		requestContext.setUserInfo(userInfo);
		return objSubContractTestDetailService.editSubcontractorTestFile(objSubContractorTestDetail, userInfo);

	}
	
	@PostMapping(value = "/viewSubcontractorSampleDetail")
	public ResponseEntity<Object> viewSubcontractorSampleDetail(@RequestBody Map<String, Object> inputMap) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final SubContractorTestDetail objSubContractorTestDetail = objMapper.convertValue(inputMap.get("subcontractortestdetail"), SubContractorTestDetail.class);
		requestContext.setUserInfo(userInfo);
		return objSubContractTestDetailService.viewSubcontractorSampleDetail(objSubContractorTestDetail, userInfo);

	}

	@PostMapping(value = "/viewSubcontractorTestFile")
	public ResponseEntity<Object> viewSubcontractorTestFile(@RequestBody Map<String, Object> inputMap,
			HttpServletResponse response) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final SubContractorTestDetail objSSubContractorTestDetail = objMapper.convertValue(inputMap.get("subcontractortestdetail"), SubContractorTestDetail.class);
		Map<String, Object> outputMap = objSubContractTestDetailService.viewSubcontractorTestFile(objSSubContractorTestDetail, userInfo);
		requestContext.setUserInfo(userInfo);
		return new ResponseEntity<Object>(outputMap, HttpStatus.OK);
	}
}
