package com.agaramtech.qualis.credential.service.employeeattendence;

import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import com.agaramtech.qualis.global.UserInfo;

/**
 * This interface holds declarations to perform CRUD operation on 'employeeattendence' table
 */
/**
 * @author sujatha.v AT-E274
 * SWSM-7 14/08/2025  
 */
public interface EmployeeAttendenceDAO {


	/**
	 * This method is used to retrieve list of all available employeeattendence for the specified employeetype.
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetch.
	 * @param currentUIDate [String] holding the current UI date.
	 * @param nemptypecode [int] holding the primary key of the employee type of the Employee Attendence.
	 * @param attendenceDate [String] holding attendencedate which list the Employee Attendence from the attendencedate.
	 * @return response entity  object holding response status and list of all active EmployeeAttendence
	 * @throws Exception that are thrown from this DAO layer
	 */
	public ResponseEntity<Object> getEmployeeAttendence(final UserInfo userInfo, final int nemptypecode,
			String currentUIDate, String attendenceDate) throws Exception;

	/**
	 * This method is used to retrieve list of available employeetype.
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetch	
	 * @param nemptypecode [int] holding the primary key of the employee type of the Employee Attendence.
	 * @return response entity  object holding response status and list of all active EmployeeType
	 * @throws Exception that are thrown from this DAO layer
	 */
	public List<?> getEmpByEmployeeType( int nemptypecode, UserInfo userinfo) throws Exception;
	
	/**
	 * This interface declaration is used to retrieve active employeeattendence object based
	 * on the specified nsamplecollectioncode.
	 * @param inputMap [Map] map object holding primary key of employee attendence and the nemptypecode primary key.
	 * @param userInfo [UserInfo] holding logged in user details based on 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of employeeAttendence object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getActiveEmployeeAttendenceById(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	/**
	 * This interface declaration is used to add a new entry to employeeattendence table.
	 * @param inputMap [Map] map object holding details to be added in employeeattendence table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of added employeeAttendence object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> createEmployeeAttendence(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to update entry in employeeattendence table.
	 * @param inputMap [Map] map object holding details to be updated in employeeattendence table.
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of updated employeeAttendence object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> updateEmployeeAttendence(Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to delete an entry in employeeattendence table.
	 * @param inputMap [Map] map object holding detail to be deleted from employeeattendence table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of deleted employeeAttendence object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> deleteEmployeeAttendence(final Map<String, Object> inputMap,
			UserInfo userInfo) throws Exception;
}
