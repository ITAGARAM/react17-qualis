package com.agaramtech.qualis.credential.service.employeeattendence;

import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.UserInfo;

/**
 * This class holds methods to perform CRUD operation on 'employeeattendence' table through its DAO layer.
 */
/**
 * @author sujatha.v AT-E274
 * SWSM-7 14/08/2025  
 */
@Transactional(readOnly = true, rollbackFor = Exception.class)
@Service
public class EmployeeAttendenceServiceImpl implements EmployeeAttendenceService {

	private final EmployeeAttendenceDAO employeeAttendenceDAO;
	private final CommonFunction commonFunction;

	public EmployeeAttendenceServiceImpl(EmployeeAttendenceDAO employeeAttendenceDAO, CommonFunction commonFunction) {
		super();
		this.employeeAttendenceDAO = employeeAttendenceDAO;
		this.commonFunction = commonFunction;
	}

	/**
	 * This service implementation method will access the DAO layer that is used 
	 * to get all available EmployeeAttendence with respect to site
	 * @param userinfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetche	
	 * @param nemptypecode [int] holding the primary key of the employee type of the employee attendence.
	 * @param currentUIDate [String] holding the current UI date.
	 * @param attendenceDate [String] holding attendenceDate which list the EmployeeAttendence from the attendenceDate.
	 * @return a response entity which holds the list of EmployeeAttendence records with respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> getEmployeeAttendence(UserInfo userinfo, int nemptypecode, String currentUIDate, String attendenceDate) throws Exception {
		return employeeAttendenceDAO.getEmployeeAttendence(userinfo, nemptypecode, currentUIDate, attendenceDate);
	}
	
	/**
	 * This service implementation method will access the DAO layer that is used 
	 * to get all  EmployeeType with respect to nemptypecode
	 * @param userinfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetche	
	 * @param nemptypecode [int] holding the primary key of the employee type of the employee attendence.
	 * @return a response entity which holds the list of EmployeeType records with respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> getEmpByEmployeeType( int nemptypecode, UserInfo userInfo) throws Exception {
		final List<?> employee = employeeAttendenceDAO.getEmpByEmployeeType(nemptypecode, userInfo);
		if (employee == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			return new ResponseEntity<>(employee, HttpStatus.OK);
		}
	}

	/**
	 * This service implementation method will access the DAO layer that is used to retrieve active employeeattendence object based
	 * on the specified nempattendencecode and nemptypecode
	 * @param inputMap [Map] map object holding primary key of employeeattendence and the nemptypecode primary key.
	 * @param userInfo [UserInfo] holding logged in user details based on 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of employeeAttendence object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> getActiveEmployeeAttendenceById(final Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		return employeeAttendenceDAO.getActiveEmployeeAttendenceById(inputMap, userInfo);
	}

	/**
	 * This service implementation method will access the DAO layer that is used to
	 * add a new entry to employeeattendence table
	 * @param inputMap [Map] map object holding details to be added in employeeattendence table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of added employeeattendence object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> createEmployeeAttendence(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		return employeeAttendenceDAO.createEmployeeAttendence(inputMap, userInfo);
	}

	/**
	 * This service implementation method will access the DAO layer that is used to
	 *  update entry in employeeattendence table
	 * @param inputMap [Map] map object holding details to be updated in employeeattendence table.
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of updated employeeAttendence object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> updateEmployeeAttendence(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		return employeeAttendenceDAO.updateEmployeeAttendence(inputMap, userInfo);
	}

	/**
	 * This service implementation method will access the DAO layer that is used to delete an entry in employeeattendence table.
	 * @param inputMap [Map] map object holding detail to be deleted from employeeattendence table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of deleted employeeAttendence object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> deleteEmployeeAttendence(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		return employeeAttendenceDAO.deleteEmployeeAttendence(inputMap, userInfo);
	}
}
