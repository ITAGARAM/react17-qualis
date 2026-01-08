package com.agaramtech.qualis.emailmanagement.service.emailconfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.agaramtech.qualis.credential.model.UserRole;
import com.agaramtech.qualis.emailmanagement.model.EmailConfig;
import com.agaramtech.qualis.emailmanagement.model.EmailUserConfig;
import com.agaramtech.qualis.emailmanagement.model.EmailUserRoleConfig;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;

@Transactional(readOnly = true, rollbackFor = Exception.class) 
@Service 
@RequiredArgsConstructor 
public class EmailConfigServiceImpl implements EmailConfigService {
	
	private final EmailConfigDAO emailconfigDAO;

	@Override
	public ResponseEntity<Object> getEmailConfig(final Map<String, Object> inputMap, final UserInfo userInfo) throws Exception {
		return emailconfigDAO.getEmailConfig(inputMap, userInfo);
	}
	
	@Override
	public ResponseEntity<Object> getEmailConfigData(final Map<String, Object> inputMap, final UserInfo userInfo) throws Exception {
		int nemailConfigCode=-1;
		short nemailTypeCode =-1;
		if (inputMap.containsKey("nemailconfigcode") && inputMap.get("nemailconfigcode") != null) {
			nemailConfigCode = (int) inputMap.get("nemailconfigcode");
		}
		if (inputMap.containsKey("nemailtypecode") && inputMap.get("nemailtypecode") != null) {
			 nemailTypeCode = ((Integer) inputMap.get("nemailtypecode")).shortValue();
		}
		return emailconfigDAO.getEmailConfigData(nemailConfigCode,nemailTypeCode, userInfo);
	}


	public ResponseEntity<Object> getEmailConfigControl(final Map<String, Object> inputMap, final UserInfo userInfo) throws Exception {
		int nformcode = 0;
		if (inputMap.containsKey("nformcode") && inputMap.get("nformcode") != null) {
			nformcode = (int) inputMap.get("nformcode");
		}
		return new ResponseEntity<>(emailconfigDAO.getEmailConfigControl(nformcode, userInfo), HttpStatus.OK);
	}

	public ResponseEntity<Object> getEmailConfigDetails(final UserInfo userInfo) throws Exception {
		return new ResponseEntity<>(emailconfigDAO.getEmailConfigDetails(0, userInfo), HttpStatus.OK);
	}

	
	@Override
	public ResponseEntity<Object> getEmailConfigScheduler(final Map<String, Object> inputMap, final UserInfo userInfo) throws Exception {
		int nformcode = -1;
		if (inputMap.containsKey("nformcode") && inputMap.get("nformcode") != null) {
			nformcode = (int) inputMap.get("nformcode");
		}
		return new ResponseEntity<>(emailconfigDAO.getEmailConfigScheduler(nformcode, userInfo), HttpStatus.OK);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> getEmailUserQuery(final Map<String, Object> inputMap, final UserInfo userInfo) throws Exception {
		int nformCode = -1;
		int ncontrolCode=-1;
		int nemailScreenSchedulerCode=-1;
		int nemailTypeCode=-1;
		final Map<String, Object> params = (Map<String, Object>) inputMap.get("params");
		if (params.containsKey("nformcode") && params.get("nformcode") != null) {
			nformCode = (int) params.get("nformcode");
		}
		if (params.containsKey("ncontrolcode") && params.get("ncontrolcode") != null) {
			ncontrolCode = (int) params.get("ncontrolcode");
		}
		if (params.containsKey("nemailscreenschedulercode") && params.get("nemailscreenschedulercode") != null) {
			nemailScreenSchedulerCode = (int) params.get("nemailscreenschedulercode");
		}
		if (inputMap.containsKey("nemailtypecode") && inputMap.get("nemailtypecode") != null) {
			nemailTypeCode = (int) inputMap.get("nemailtypecode");
		}
		return emailconfigDAO.getEmailUserQuery(nformCode,ncontrolCode,nemailScreenSchedulerCode,nemailTypeCode, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> createEmailConfig(final Map<String, Object> inputMap,final UserInfo userInfo)	throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		List<UserRole> nuserrolecode = new ArrayList<UserRole>();
		List<EmailUserConfig> nusercode = new ArrayList<EmailUserConfig>();
		EmailConfig emailConfig = null;
		if (inputMap.containsKey("emailuserroleconfig") && inputMap.get("emailuserroleconfig") != null) {
			nuserrolecode = objmapper.convertValue(inputMap.get("emailuserroleconfig"),
					new TypeReference<List<UserRole>>() {
			});
		}
		if (inputMap.containsKey("emailuserconfig") && inputMap.get("emailuserconfig") != null) {
			nusercode = objmapper.convertValue(inputMap.get("emailuserconfig"),
					new TypeReference<List<EmailUserConfig>>() {
			});
		}
		if (inputMap.containsKey("emailconfig") && inputMap.get("emailconfig") != null) {
			emailConfig = objmapper.convertValue(inputMap.get("emailconfig"), EmailConfig.class);
		}
		return emailconfigDAO.createEmailConfig(emailConfig, nusercode, nuserrolecode, userInfo);
	}

	@Override
	public ResponseEntity<Object> getActiveEmailConfigById(final Map<String, Object> inputMap,final UserInfo userInfo) throws Exception {
		int nemailconfigcode = -1;
		short nemailTypeCode =-1;
		if (inputMap.containsKey("nemailconfigcode") && inputMap.get("nemailconfigcode") != null) {
			nemailconfigcode = (Integer) inputMap.get("nemailconfigcode");
		}
		if (inputMap.containsKey("nemailtypecode") && inputMap.get("nemailtypecode") != null) {
			 nemailTypeCode = ((Integer) inputMap.get("nemailtypecode")).shortValue();
		}
		return emailconfigDAO.getActiveEmailConfigById(nemailconfigcode,nemailTypeCode, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> updateEmailConfig(final Map<String, Object> inputMap, final UserInfo userInfo)	throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		EmailConfig emailConfig = null;
		if (inputMap.containsKey("emailconfig") && inputMap.get("emailconfig") != null) {
			emailConfig = objmapper.convertValue(inputMap.get("emailconfig"), EmailConfig.class);
		}
		return emailconfigDAO.updateEmailConfig(emailConfig, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> deleteEmailConfig(final Map<String, Object> inputMap, final UserInfo userInfo) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		EmailConfig emailConfig = null;
		if (inputMap.containsKey("emailconfig") && inputMap.get("emailconfig") != null) {
			emailConfig = objmapper.convertValue(inputMap.get("emailconfig"), EmailConfig.class);
		}
		return emailconfigDAO.deleteEmailConfig(emailConfig, userInfo);
	}

	@Override
	public ResponseEntity<Object> getUserRoleEmail(final Map<String, Object> inputMap,final UserInfo userInfo) throws Exception {
		int nuserrolecode = -1;
		if (inputMap.containsKey("nuserrolecode") && inputMap.get("nuserrolecode") != null) {
			nuserrolecode = (int) inputMap.get("nuserrolecode");
		}
		return emailconfigDAO.getUserRoleEmail(nuserrolecode, userInfo);
	}

	@Override
	public ResponseEntity<Object> getUserEmailConfig(final Map<String, Object> inputMap,final UserInfo userInfo) throws Exception {
		int nemailconfigcode = -1;
		int nuserrolecode = -1;
		boolean isUserRole = false;
		if (inputMap.containsKey("nemailconfigcode") && inputMap.get("nemailconfigcode") != null) {
			nemailconfigcode = (int) inputMap.get("nemailconfigcode");
		}
		if (inputMap.containsKey("nuserrolecode") && inputMap.get("nuserrolecode") != null) {
			nuserrolecode = (int) inputMap.get("nuserrolecode");
		}
		if (inputMap.containsKey("isUserRole") && inputMap.get("isUserRole") != null) {
			isUserRole = (boolean) inputMap.get("isUserRole");
		}
		return emailconfigDAO.getUserEmailConfig(nemailconfigcode, nuserrolecode, isUserRole, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> createUsers(final Map<String, Object> inputMap, final UserInfo userInfo)throws Exception {
		String nusercode = null;
		int nuserrolecode = -1;
		int nemailconfigcode = -1;
		if (inputMap.containsKey("nusercode") && inputMap.get("nusercode") != null) {
			nusercode = (String) inputMap.get("nusercode");
		}
		if (inputMap.containsKey("nuserrolecode") && inputMap.get("nuserrolecode") != null) {
			nuserrolecode = (int) inputMap.get("nuserrolecode");
		}
		if (inputMap.containsKey("nemailconfigcode") && inputMap.get("nemailconfigcode") != null) {
			nemailconfigcode = (int) inputMap.get("nemailconfigcode");
		}
		return emailconfigDAO.createUsers(nemailconfigcode, nusercode, nuserrolecode, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> deleteUsers(final Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		EmailUserConfig emailuserconfig = null;
		if (inputMap.containsKey("emailuserconfig") && inputMap.get("emailuserconfig") != null) {
			emailuserconfig = objmapper.convertValue(inputMap.get("emailuserconfig"), EmailUserConfig.class);
		}
		return emailconfigDAO.deleteUsers(emailuserconfig, userInfo);
	}
	
	@Override
	public ResponseEntity<Object> getUserRoles(UserInfo userInfo) throws Exception {
		return emailconfigDAO.getUserRoles(userInfo);
	}
	
	@Override
	public ResponseEntity<Object> getEmailUserOnUserRole(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		int nuserrolecode = -1;
		int nemailconfigcode = -1;
		if (inputMap.containsKey("nuserrolecode") && inputMap.get("nuserrolecode") != null) {
			nuserrolecode = (int) inputMap.get("nuserrolecode");
		}
		if (inputMap.containsKey("nemailconfigcode") && inputMap.get("nemailconfigcode") != null) {
			nemailconfigcode = (int) inputMap.get("nemailconfigcode");
		}
		return emailconfigDAO.getEmailUserOnUserRole(nuserrolecode, nemailconfigcode, userInfo);
	}
	
	@Override
	public ResponseEntity<Object> getEmailUsers(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		int nuserrolecode = -1;
		if (inputMap.containsKey("nuserrolecode") && inputMap.get("nuserrolecode") != null) {
			nuserrolecode = (int) inputMap.get("nuserrolecode");
		}
		return emailconfigDAO.getEmailUsers(nuserrolecode, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> createUserRoles(final Map<String, Object> inputMap,final UserInfo userInfo)throws Exception {
		String nuserrolecode = null;
		int nemailconfigcode = -1;
		if (inputMap.containsKey("nuserrolecode") && inputMap.get("nuserrolecode") != null) {
			nuserrolecode = (String) inputMap.get("nuserrolecode");
		}
		if (inputMap.containsKey("nemailconfigcode") && inputMap.get("nemailconfigcode") != null) {
			nemailconfigcode = (Integer) inputMap.get("nemailconfigcode");
		}
		return emailconfigDAO.createUserRoles(nemailconfigcode, nuserrolecode, userInfo);
	}
	
	@Transactional
	@Override
	public ResponseEntity<Object> deleteUserRole(final Map<String, Object> inputMap,final UserInfo userInfo) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		EmailUserRoleConfig emailUserRoleConfig = null;
		if (inputMap.containsKey("emailUserRoleConfig") && inputMap.get("emailUserRoleConfig") != null) {
			emailUserRoleConfig = objmapper.convertValue(inputMap.get("emailUserRoleConfig"), EmailUserRoleConfig.class);
		}
		return emailconfigDAO.deleteUserRole(emailUserRoleConfig, userInfo);
	}
	
}
