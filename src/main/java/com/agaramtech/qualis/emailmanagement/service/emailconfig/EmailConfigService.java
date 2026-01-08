package com.agaramtech.qualis.emailmanagement.service.emailconfig;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import com.agaramtech.qualis.global.UserInfo;

public interface EmailConfigService {

	ResponseEntity<Object> getEmailConfig(final Map<String, Object> inputMap, final UserInfo userInfo) throws Exception;

	ResponseEntity<Object> getEmailConfigData(final Map<String, Object> inputMap, final UserInfo userInfo) throws Exception;
	ResponseEntity<Object> getEmailConfigDetails(final UserInfo userInfo) throws Exception;

	ResponseEntity<Object> getEmailConfigControl(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;
	ResponseEntity<Object> getEmailConfigScheduler(final Map<String, Object> inputMap, final UserInfo userInfo) throws Exception;
	ResponseEntity<Object> getEmailUserQuery(final Map<String, Object> inputMap, final UserInfo userInfo) throws Exception;

	ResponseEntity<Object> createEmailConfig(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	ResponseEntity<Object> getActiveEmailConfigById(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	ResponseEntity<Object> updateEmailConfig(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	ResponseEntity<Object> deleteEmailConfig(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	ResponseEntity<Object> getUserRoleEmail(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	ResponseEntity<Object> getUserEmailConfig(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	ResponseEntity<Object> createUsers(final Map<String, Object> inputMap, final UserInfo userInfo) throws Exception;

	ResponseEntity<Object> deleteUsers(final Map<String, Object> inputMap, final UserInfo userInfo) throws Exception;

	ResponseEntity<Object> getUserRoles(final UserInfo userInfo) throws Exception;

	ResponseEntity<Object> getEmailUserOnUserRole(final Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception;

	ResponseEntity<Object> getEmailUsers(Map<String, Object> inputMap, UserInfo userInfo) throws Exception;

	ResponseEntity<Object> createUserRoles(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	ResponseEntity<Object> deleteUserRole(final Map<String, Object> inputMap, final UserInfo userInfo) throws Exception;

}
