package com.agaramtech.qualis.restcontroller;

import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.agaramtech.qualis.invoice.model.UserRoleFieldControl;
import com.agaramtech.qualis.invoice.model.UsersRoleField;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.invoice.model.InvoicePreferenceSetting;
import com.agaramtech.qualis.invoice.service.invoicepreferencesetting.InvoicePreferenceSettingService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * This controller is used to dispatch the input request to its relevant service
 * methods to access the invoicepreferencesetting Service methods. IN-451
 * Preference Settings
 */
@RestController
@RequestMapping("/invoicepreferencesetting")
public class InvoicePreferenceSettingController {
	final Log logging = LogFactory.getLog(InvoicePreferenceSetting.class);

	private RequestContext requestContext;
	private InvoicePreferenceSettingService invoicePreferenceSettingService;

	/**
	 * This constructor injection method is used to pass the object dependencies to
	 * the class.
	 * 
	 * @param requestContext                  RequestContext to hold the request
	 * @param invoicePreferenceSettingService
	 * @param invoicePreferenceSettingService invoicePreferenceSettingService
	 */
	public InvoicePreferenceSettingController(RequestContext requestContext,
			InvoicePreferenceSettingService invoicePreferenceSettingService) {
		super();
		this.requestContext = requestContext;
		this.invoicePreferenceSettingService = invoicePreferenceSettingService;
	}

	/**
	 * This interface declaration is used to get all the available
	 * InvoicePreferenceSettings with respect to site
	 * 
	 * @param inputMap [Map<String, Object>] holding user information and user role
	 *                 preference code
	 * @return a response entity which holds the list of InvoicePreferenceSetting
	 *         records with respect to site
	 * @throws Exception that are thrown in the Service layer
	 */
	@PostMapping(value = "/getInvoicePreferenceSetting")
	public ResponseEntity<Object> getInvoicePreferenceSetting(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		Integer nuserrolescreencode = null;
		if (inputMap.get("nuserrolepreferencecode") != null) {
			nuserrolescreencode = (Integer) inputMap.get("nuserrolepreferencecode");
		}
		requestContext.setUserInfo(userInfo);
		return (ResponseEntity<Object>) invoicePreferenceSettingService.getInvoicePreferenceSetting(nuserrolescreencode,
				userInfo);

	}

	/**
	 * This interface declaration is used to get all available screens for invoice
	 * preference settings with respect to site
	 * 
	 * @param inputMap [Map<String, Object>] holding user information and user role
	 *                 code
	 * @return a response entity which holds the list of available screens with
	 *         respect to site
	 * @throws Exception that are thrown in the Service layer
	 */

	@PostMapping(value = "/getAvailableScreen")
	public ResponseEntity<Object> getAvailableScreen(@RequestBody Map<String, Object> inputMap) throws Exception {

		ObjectMapper objMapper = new ObjectMapper();
		Integer nuserrolecode = null;
		if (inputMap.get("nuserrolecode") != null) {
			nuserrolecode = (Integer) inputMap.get("nuserrolecode");
		}
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});

		requestContext.setUserInfo(userInfo);
		return invoicePreferenceSettingService.getAvailableScreen(nuserrolecode, userInfo);// siteCode);

	}

	/**
	 * This interface declaration is used to get single select screen rights for
	 * invoice preference settings
	 * 
	 * @param inputMap [Map<String, Object>] holding screen rights, user
	 *                 information, and user role code
	 * @return a response entity which holds the single select screen rights
	 * @throws Exception that are thrown in the Service layer
	 */

	@PostMapping(value = "/getSingleSelectScreenRights")
	public ResponseEntity<Object> getSingleSelectScreenRights(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		final ObjectMapper objmapper = new ObjectMapper();
		objmapper.registerModule(new JavaTimeModule());

		List<UsersRoleField> lstusersrolescreen = (List<UsersRoleField>) objmapper
				.convertValue(inputMap.get("screenrights"), new TypeReference<List<UsersRoleField>>() {
				});
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final int nuserrolecode = (int) inputMap.get("nuserrolecode");

		requestContext.setUserInfo(userInfo);
		return invoicePreferenceSettingService.getSingleSelectScreenRights(lstusersrolescreen, userInfo, nuserrolecode);

	}

	/**
	 * Get all available InvoicePreferenceSettings for a specific site based on user
	 * role code
	 * 
	 * @param inputMap Request body containing user information and user role code
	 * @return ResponseEntity with list of InvoicePreferenceSetting records
	 * @throws Exception Propagates DAO layer exceptions
	 */

	@PostMapping(value = "/getpreferenceByUserRoleCode")
	public ResponseEntity<Object> getpreferenceByUserRoleCode(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		ObjectMapper objMapper = new ObjectMapper();
		Integer nuserrolecode = null;
		if (inputMap.get("nuserrolecode") != null) {
			nuserrolecode = (Integer) inputMap.get("nuserrolecode");
		}
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});

		requestContext.setUserInfo(userInfo);
		return invoicePreferenceSettingService.getpreferenceByUserRoleCode(nuserrolecode, userInfo);// siteCode);
	}

	/**
	 * Update control rights for invoice preference settings
	 * 
	 * @param inputMap Contains screen rights, selected screen rights, user info,
	 *                 flag and need rights
	 * @return ResponseEntity with status of the control rights update operation
	 * @throws Exception Propagates Service layer exceptions
	 */

	@PostMapping(value = "/updateControlRights")
	public ResponseEntity<Object> createControlRights(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objmapper = new ObjectMapper();
		objmapper.registerModule(new JavaTimeModule());
		Integer nflag = null;
		UserRoleFieldControl userroleController = null;
		if (inputMap.containsKey("screenrights")) {

			userroleController = objmapper.convertValue(inputMap.get("screenrights"), UserRoleFieldControl.class);
		}
		List<UsersRoleField> lstusersrolescreen = (List<UsersRoleField>) objmapper
				.convertValue(inputMap.get("selectedscreenrights"), new TypeReference<List<UsersRoleField>>() {
				});
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);

		nflag = (Integer) inputMap.get("nflag");

		int nneedrights = (int) inputMap.get("nneedrights");

		requestContext.setUserInfo(userInfo);
		return invoicePreferenceSettingService.createControlRights(userInfo, userroleController, lstusersrolescreen,
				nflag, nneedrights);

	}

	/**
	 * Get all available InvoicePreferenceSettings for a specific site
	 * 
	 * @param inputMap Request body containing user information and user role field
	 *                 code
	 * @return ResponseEntity with list of InvoicePreferenceSetting records
	 * @throws Exception Propagates DAO layer exceptions
	 */

	@PostMapping(value = "/getSearchScreenRights")
	public ResponseEntity<Object> getSearchScreenRights(@RequestBody Map<String, Object> inputMap) throws Exception {

		ObjectMapper objMapper = new ObjectMapper();
		final UserInfo objUserInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		String nuserrolefieldcode = null;
		if (inputMap.get("nuserrolefieldcode") != null) {
			nuserrolefieldcode = (String) inputMap.get("nuserrolefieldcode");
		}
		return (ResponseEntity<Object>) invoicePreferenceSettingService.getSearchScreenRights(nuserrolefieldcode,
				objUserInfo);
	}
}
