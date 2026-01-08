package com.agaramtech.qualis.restcontroller;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.agaramtech.qualis.emailmanagement.service.emailconfig.EmailConfigService;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/emailconfig")
public class EmailConfigController {

	private static final Logger LOGGER = LoggerFactory.getLogger(EmailConfigController.class);

	private final EmailConfigService emailconfigService;
	private RequestContext requestContext;

	public EmailConfigController(EmailConfigService emailconfigService, RequestContext requestContext) {
		super();
		this.emailconfigService = emailconfigService;
		this.requestContext = requestContext;
	}
	
	@PostMapping(value = "/getEmailConfig")
	public ResponseEntity<Object> getEmailConfig(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {});
		requestContext.setUserInfo(userInfo);
		LOGGER.info("getEmailConfig() called");
		return emailconfigService.getEmailConfig(inputMap, userInfo);
	}
	@PostMapping(value = "/getEmailConfigData")
	public ResponseEntity<Object> getEmailConfigData(@RequestBody final Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {});
		requestContext.setUserInfo(userInfo);
		return emailconfigService.getEmailConfigData(inputMap, userInfo);
	}

	@PostMapping(value = "/getEmailConfigDetails")
	public ResponseEntity<Object> getEmailConfigDetails(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {});
		requestContext.setUserInfo(userInfo);
		return emailconfigService.getEmailConfigDetails(userInfo);
	}

	@PostMapping(value = "/getEmailConfigControl")
	public ResponseEntity<Object> getEmailConfigControl(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {});
		return emailconfigService.getEmailConfigControl(inputMap, userInfo);
	}

	@PostMapping(value = "/getEmailConfigScheduler")
	public ResponseEntity<Object> getEmailConfigScheduler(@RequestBody final Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {});
		return emailconfigService.getEmailConfigScheduler(inputMap, userInfo);
	}
	
	@PostMapping(value = "/getEmailUserQuery")
	public ResponseEntity<Object> getEmailUserQuery(@RequestBody final Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {});
		return emailconfigService.getEmailUserQuery(inputMap, userInfo);
	}

	@PostMapping(value = "/createEmailConfig")
	public ResponseEntity<Object> createEmailConfig(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return emailconfigService.createEmailConfig(inputMap, userInfo);
	}

	@PostMapping(value = "/getActiveEmailConfigById")
	public ResponseEntity<Object> getActiveEmailConfigById(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();		
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {});
		requestContext.setUserInfo(userInfo);
		return emailconfigService.getActiveEmailConfigById(inputMap, userInfo);
	}

	@PostMapping(value = "/updateEmailConfig")
	public ResponseEntity<Object> updateEmailConfig(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return emailconfigService.updateEmailConfig(inputMap, userInfo);
	}

	@PostMapping(value = "/deleteEmailConfig")
	public ResponseEntity<Object> deleteEmailConfig(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return emailconfigService.deleteEmailConfig(inputMap, userInfo);
	}

	@PostMapping(value = "/getUserRoleEmail")
	public ResponseEntity<Object> getUserRoleEmail(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {});
		requestContext.setUserInfo(userInfo);
		return emailconfigService.getUserRoleEmail(inputMap, userInfo);
	}

	@PostMapping(value = "/getUserEmailConfig")
	public ResponseEntity<Object> getUserEmailConfig(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();		
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {});
		requestContext.setUserInfo(userInfo);
		return emailconfigService.getUserEmailConfig(inputMap, userInfo);
	}

	@PostMapping(value = "/createUsers")
	public ResponseEntity<Object> createUsers(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();		
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return emailconfigService.createUsers(inputMap, userInfo);
	}

	@PostMapping(value = "/deleteUsers")
	public ResponseEntity<Object> deleteUsers(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();		
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return emailconfigService.deleteUsers(inputMap, userInfo);
	}
	
	// Added by Gowtham on 30 Oct 2025 for jira-id:BGSI-147
	@PostMapping(value = "/getUserRoles")
	public ResponseEntity<Object> getUserRoles(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return emailconfigService.getUserRoles(userInfo);
	}
	
	// Added by Gowtham on 30 Oct 2025 for jira-id:BGSI-147
	@PostMapping(value = "/getEmailUserOnUserRole")
	public ResponseEntity<Object> getEmailUserOnUserRole(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return emailconfigService.getEmailUserOnUserRole(inputMap, userInfo);
	}
	
	// Added by Gowtham on 30 Oct 2025 for jira-id:BGSI-147
	@PostMapping(value = "/getEmailUsers")
	public ResponseEntity<Object> getEmailUsers(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return emailconfigService.getEmailUsers(inputMap, userInfo);
	}
	
	// Added by Gowtham on 30 Oct 2025 for jira-id:BGSI-147
	@PostMapping(value = "/createUserRoles")
	public ResponseEntity<Object> createUserRoles(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return emailconfigService.createUserRoles(inputMap, userInfo);
	}
	
	// Added by Gowtham on Oct 29 2025 for jira-id:BGSI-147
	@PostMapping(value = "/deleteUserRole")
	public ResponseEntity<Object> deleteUserRole(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return emailconfigService.deleteUserRole(inputMap, userInfo);
	}
}
