package com.agaramtech.qualis.emailmanagement.service.emailconfig;

import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import com.agaramtech.qualis.credential.model.UserRole;
import com.agaramtech.qualis.emailmanagement.model.EmailConfig;
import com.agaramtech.qualis.emailmanagement.model.EmailUserConfig;
import com.agaramtech.qualis.emailmanagement.model.EmailUserRoleConfig;
import com.agaramtech.qualis.global.UserInfo;

public interface EmailConfigDAO {

	ResponseEntity<Object> getEmailConfig(final Map<String, Object> inputMap, final UserInfo userInfo) throws Exception;

	ResponseEntity<Object> getEmailConfigData(final int nemailConfigCode, final short nemailTypeCode,
			final UserInfo userInfo) throws Exception;

	Map<String, Object> getEmailConfigDetails(final int nformcode, final UserInfo userInfo) throws Exception;

	Map<String, Object> getEmailConfigControl(final int nformcode, final UserInfo userInfo) throws Exception;

	Map<String, Object> getEmailConfigScheduler(final int nformcode, final UserInfo userInfo) throws Exception;

	ResponseEntity<Object> getEmailUserQuery(final int nformCode, final int ncontrolCode,
			final int nemailScreenSchedulerCode, final int nemailTypeCode, final UserInfo userInfo) throws Exception;

	ResponseEntity<Object> createEmailConfig(final EmailConfig emailconfig, final List<EmailUserConfig> nusercode,
			final List<UserRole> nuserrolecode, final UserInfo userInfo) throws Exception;

	ResponseEntity<Object> getActiveEmailConfigById(final int nemailconfigcode, final short nemailTypeCode,
			final UserInfo userInfo) throws Exception;;

	ResponseEntity<Object> deleteEmailConfig(final EmailConfig emailconfig, final UserInfo userInfo) throws Exception;

	ResponseEntity<Object> updateEmailConfig(final EmailConfig emailconfig, final UserInfo userInfo) throws Exception;

	ResponseEntity<Object> getUserRoleEmail(final int nuserrolecode, final UserInfo userInfo) throws Exception;

	ResponseEntity<Object> getUserEmailConfig(final int nemailconfigcode, final int nuserrolecode, final boolean isUserRole,
			final UserInfo userInfo) throws Exception;

	ResponseEntity<Object> createUsers(final int nemailconfigcode, final String nusercode, final int nuserrolecode,
			final UserInfo userInfo) throws Exception;

	ResponseEntity<Object> deleteUsers(final EmailUserConfig emailuserconfig, final UserInfo userInfo) throws Exception;

	ResponseEntity<Object> getUserRoles(UserInfo userInfo) throws Exception;

	ResponseEntity<Object> getEmailUserOnUserRole(int nuserrolecode, int nemailconfigcode, UserInfo userInfo)
			throws Exception;

	ResponseEntity<Object> getEmailUsers(int nuserrolecode, UserInfo userInfo) throws Exception;

	ResponseEntity<Object> createUserRoles(final int nemailconfigcode, final String nuserrolecode,
			final UserInfo userInfo) throws Exception;

	ResponseEntity<Object> deleteUserRole(final EmailUserRoleConfig emailUserRoleConfig, final UserInfo userInfo)
			throws Exception;

}
