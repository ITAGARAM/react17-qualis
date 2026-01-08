package com.agaramtech.qualis.restcontroller;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.agaramtech.qualis.biobank.service.BioEcatalogueReqApproval.BioEcatalogueReqApprovalService;
import com.agaramtech.qualis.biobank.service.biorequestbasedtransfer.BioRequestBasedTransferService;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/bioecataloguereqapproval")
public class BioEcatalogueReqApprovalController {
	private static final Logger LOGGER = LoggerFactory.getLogger(BioEcatalogueReqApprovalController.class);

	private final BioEcatalogueReqApprovalService bioEcatalogueReqApprovalService;
	private final RequestContext requestContext;

	public BioEcatalogueReqApprovalController(final RequestContext requestContext,
			final BioEcatalogueReqApprovalService bioEcatalogueReqApprovalService) {
		this.requestContext = requestContext;
		this.bioEcatalogueReqApprovalService = bioEcatalogueReqApprovalService;
	}
	
	
	
	@PostMapping(value = "/getBioEcatalogueReqApproval")
	public ResponseEntity<Object> getBioEcatalogueReqApproval(@RequestBody final Map<String, Object> inputMap)
			throws Exception {

		final var objMapper = new ObjectMapper();
		final var userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		final var nbioRequestBasedTransferCode = -1;
		return bioEcatalogueReqApprovalService.getBioEcatalogueReqApproval(inputMap, nbioRequestBasedTransferCode,
				userInfo);
	}
	
	
	@PostMapping(value = "/getSelectedBioEcatalogueReqApproval")
	public ResponseEntity<Object> getSelectedBioEcatalogueReqApproval(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		final String necatrequestreqapprovalcode = (String) inputMap.get("necatrequestreqapprovalcode");
		final int ntransfertype = (int) inputMap.get("ntransfertype");
		final int ntransactionstatus = (int) inputMap.get("ntransactionstatus");
		final int nrequestapprovlcode = Integer.parseInt(necatrequestreqapprovalcode);

		requestContext.setUserInfo(userInfo);
		return bioEcatalogueReqApprovalService.getSelectedBioEcatalogueReqApproval(nrequestapprovlcode,ntransfertype,ntransactionstatus, userInfo);

	}
	@PostMapping(value = "/getFilteredRequestDetails")
	public ResponseEntity<Object> getFilteredRequestDetails(@RequestBody Map<String, Object> inputMap)
	        throws Exception {

	    final ObjectMapper objMapper = new ObjectMapper();
	    final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
	    });

	    final int nbioprojectcode = Integer.parseInt(String.valueOf(inputMap.get("nbioprojectcode")));
	    final int nproductcode = Integer.parseInt(String.valueOf(inputMap.get("nproductcode")));
	    final String sparentsamplecode = (String) inputMap.get("sparentsamplecode");
	    final int nsitecode = Integer.parseInt(String.valueOf(inputMap.get("nsitecode")));
	    final int ntransfertypecode= Integer.parseInt(String.valueOf(inputMap.get("ntransfertypecode")));

	    requestContext.setUserInfo(userInfo);
	    return bioEcatalogueReqApprovalService.getFilteredRequestDetails(nbioprojectcode, nproductcode,
	            sparentsamplecode, ntransfertypecode, nsitecode, userInfo);
	}

	
	@PostMapping(value = "/updatedUserAcceptedVolume")
	public ResponseEntity<Object> updatedUserAcceptedVolume(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return bioEcatalogueReqApprovalService.updatedUserAcceptedVolume(inputMap, userInfo);
	}
	
	@PostMapping(value = "/updateRejectedRecord")
	public ResponseEntity<Object> updateRejectedRecord(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return bioEcatalogueReqApprovalService.updateRejectedRecord(inputMap, userInfo);
	}
	@PostMapping(value = "/updateApproveRecord")
	public ResponseEntity<Object> updateApproveRecord(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return bioEcatalogueReqApprovalService.updateApproveRecord(inputMap, userInfo);
	}
	
	@PostMapping(value = "/getBioEcatalogueReqApprovalDetails")
	public ResponseEntity<Object> getBioEcatalogueReqApprovalDetails(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return bioEcatalogueReqApprovalService.getBioEcatalogueReqApprovalDetails(inputMap, userInfo);
	}
	
}