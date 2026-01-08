package com.agaramtech.qualis.invoice.service.invoicepreferencesetting;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.invoice.model.UserRoleFieldControl;
import com.agaramtech.qualis.invoice.model.UsersRoleField;

@Service
public class InvoicePreferenceSettingServiceImpl implements InvoicePreferenceSettingService {

	@Autowired
	private InvoicePreferenceSettingDAO invoicePreferenceSettingDAO;

	/**
	 * This interface declaration is used to get single select screen rights for
	 * invoice preference settings
	 * 
	 * @param inputMap [Map<String, Object>] holding screen rights, user
	 *                 information, and user role code
	 * @return a response entity which holds the single select screen rights
	 * @throws Exception that are thrown in the Service layer
	 */
	@Override
	public ResponseEntity<Object> getInvoicePreferenceSetting(Integer nuserrolescreencode, UserInfo objUserInfo)
			throws Exception {
		// TODO Auto-generated method stub
		return invoicePreferenceSettingDAO.getInvoicePreferenceSetting(nuserrolescreencode, objUserInfo);
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
	@Override
	public ResponseEntity<Object> getAvailableScreen(Integer nuserrolecode, final UserInfo userInfo) throws Exception {
		return invoicePreferenceSettingDAO.getAvailableScreen(nuserrolecode, userInfo);// siteCode);
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
	public ResponseEntity<Object> getSingleSelectScreenRights(List<UsersRoleField> lstusersrolescreen,
			UserInfo objUserInfo, Integer nuserrolecode) throws Exception {
		// TODO Auto-generated method stub
		return invoicePreferenceSettingDAO.getSingleSelectScreenRights(lstusersrolescreen, objUserInfo, nuserrolecode);
	}

	/**
	 * Get all available InvoicePreferenceSettings for a specific site based on user
	 * role code
	 * 
	 * @param inputMap Request body containing user information and user role code
	 * @return ResponseEntity with list of InvoicePreferenceSetting records
	 * @throws Exception Propagates DAO layer exceptions
	 */
	@Override
	public ResponseEntity<Object> getpreferenceByUserRoleCode(Integer nuserrolecode, final UserInfo userInfo)
			throws Exception {
		return invoicePreferenceSettingDAO.getpreferenceByUserRoleCode(nuserrolecode, userInfo);// siteCode);
	}

	/**
	 * This method is used to add a new entry to InvoicePreferenceSetting table.
	 * Need to check for duplicate entry for the specified site before saving into
	 * database. * Need to check that there should be only one default
	 * InvoicePreferenceSetting for a site.
	 * 
	 * @param objInvoicePreferenceSetting [InvoicePreferenceSetting] object holding
	 *                                    details to be added in
	 *                                    InvoicePreferenceSetting table
	 * @param userInfo                    [UserInfo] holding logged in user details
	 *                                    based on which the list is to be fetched
	 * @return saved InvoicePreferenceSetting object with status code 200 if saved
	 *         successfully else if the InvoicePreferenceSetting already exists,
	 *         response will be returned as 'Already Exists' with status code 409
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public ResponseEntity<Object> createControlRights(UserInfo userInfo, UserRoleFieldControl userroleController,
			List<UsersRoleField> lstusersrolescreen, int nflag, int nneedrights) throws Exception {

		return invoicePreferenceSettingDAO.createControlRights(userInfo, userroleController, lstusersrolescreen, nflag,
				nneedrights);
	}

	/**
	 * Get all available InvoicePreferenceSettings for a specific site
	 * 
	 * @param inputMap Request body containing user information and user role field
	 *                 code
	 * @return ResponseEntity with list of InvoicePreferenceSetting records
	 * @throws Exception Propagates DAO layer exceptions
	 */
	@Override
	public ResponseEntity<Object> getSearchScreenRights(String nuserrolefieldcode, UserInfo objUserInfo)
			throws Exception {
		// TODO Auto-generated method stub
		return invoicePreferenceSettingDAO.getSearchScreenRights(nuserrolefieldcode, objUserInfo);
	}

}