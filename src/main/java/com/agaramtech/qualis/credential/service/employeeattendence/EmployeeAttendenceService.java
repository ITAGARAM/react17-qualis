package com.agaramtech.qualis.credential.service.employeeattendence;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import com.agaramtech.qualis.global.UserInfo;

/**
 * This interface declaration holds methods to perform CRUD operation on 'employeeattendence' table
 */
/**
 * @author sujatha.v AT-E274
 * SWSM-7 14/08/2025  
 */
public interface EmployeeAttendenceService {

	/**
	 * This service implementation method will access the DAO layer that is used 
	 * to get all available EmployeeAttendence with respect to site
	 * @param userinfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetche	
	 * @param nemptypecode [int] holding the primary key of the employee type of the employee attendence.
	 * @param currentUIDate [String] holding the current UI date.
	 * @param attendenceDate [String] holding attendenceDate which list the EmployeeAttendence from the attendenceDate.
	 * @return a response entity which holds the list of Active EmployeeAttendence records with respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getEmployeeAttendence(final UserInfo userinfo, final int nemptypecode,
			 String currentUIDate, String attendenceDate) throws Exception;

	/**
	 * This service implementation method will access the DAO layer that is used 
	 * to get all EmployeeType with respect to nemptypecode
	 * @param userinfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetche	
	 * @param nemptypecode [int] holding the primary key of the employee type of the employee attendence.
	 * @return a response entity which holds the list of EmployeeType records with respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getEmpByEmployeeType(final int nemptypecode, final UserInfo userInfo)
			throws Exception;
	
	/**
	 * This service interface declaration will access the DAO layer that is used to retrieve active employeeattendence object based
	 * on the specified nempattendencecode and nemptypecode
	 * @param inputMap [Map] map object holding primary key of employee attendence and the emptypecode primary key.
	 * @param userInfo [UserInfo] holding logged in user details based on 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of employeeAttendence object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getActiveEmployeeAttendenceById(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	/**
	 * This service interface declaration will access the DAO layer that is used to
	 * add a new entry to employeeattendence table
	 * @param inputMap [Map] map object holding details to be added in employeeattendence table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of added employeeattendence object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> createEmployeeAttendence(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception;

	/**
	 * This service interface declaration will access the DAO layer that is used to
	 *  update entry in employeeattendence table
	 * @param inputMap [Map] map object holding details to be updated in employeeattendence table.
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of updated employeeAttendence object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> updateEmployeeAttendence(Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception;

	/**
	 * This service interface declaration will access the DAO layer that is used to delete an entry in employeeattendence table.
	 * @param inputMap [Map] map object holding detail to be deleted from employeeattendence table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of deleted employeeAttendence object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> deleteEmployeeAttendence(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception;
}
