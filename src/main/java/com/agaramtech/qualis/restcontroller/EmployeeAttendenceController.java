package com.agaramtech.qualis.restcontroller;

import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.agaramtech.qualis.credential.service.employeeattendence.EmployeeAttendenceService;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * This controller is used to dispatch the input request to its relevant service methods
 * to access the Employee Attendence Service methods.
 */
/**
 * @author sujatha.v AT-E274
 * SWSM-7 14/08/2025  
 */
@RestController
@RequestMapping("/employeeattendence")
public class EmployeeAttendenceController {

	private static final Log LOGGER = LogFactory.getLog(EmployeeAttendenceController.class);
	private RequestContext requestContext;
	private EmployeeAttendenceService employeeAttendenceService;

	/**
	 * This constructor injection method is used to pass the object dependencies to the class.
	 * @param requestContext RequestContext to hold the request
	 * @param EmployeeAttendenceService employeeAttendenceService
	 */
	public EmployeeAttendenceController(RequestContext requestContext,
			EmployeeAttendenceService employeeAttendenceService) {
		super();
		this.requestContext = requestContext;
		this.employeeAttendenceService = employeeAttendenceService;
	}

	/**
	 * This method is used to retrieve list of available employee attendence(s). 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in user details and 
	 *                 nmasterSiteCode [int] primary key of site object for  which the list is to be fetched 	
	 * 		  Input : {"userinfo":{nmastersitecode": -1}}				
	 * @return response entity object holding response status and list of all employee attendence(s)
	 * @throws Exception exception
	 */
	@PostMapping(value = "/getEmployeeAttendence")
	public ResponseEntity<Object> getEmployeeAttendence(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		int nemptypecode = -1;
		if (inputMap.containsKey("nemptypecode")) {
			nemptypecode = (int) inputMap.get("nemptypecode");
		}
		final String currentUIDate = (String) inputMap.get("currentdate");
		String attendenceDate = inputMap.get("attendenceDate") != null
				? inputMap.get("attendenceDate").toString()
				: "null";
		LOGGER.info("getEmployeeAttendence() called");
		requestContext.setUserInfo(userInfo);
		return employeeAttendenceService.getEmployeeAttendence(userInfo, nemptypecode, currentUIDate, attendenceDate);
	}

	/**
	 * This method is used to retrieve list of available employee type(s). 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in user details and 
	 *                 nmasterSiteCode [int] primary key of site object for  which the list is to be fetched 	
	 * 		  Input : {"userinfo":{nmastersitecode": -1}}				
	 * @return response entity object holding response status and list of all employee type(s)
	 * @throws Exception exception
	 */
	@PostMapping(value = "/getEmpByEmployeeType")
	public ResponseEntity<Object> getEmpByEmployeeType(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		Integer nemptypecode = null;
		if (inputMap.get("nemptypecode") != null) {
			nemptypecode = (Integer) inputMap.get("nemptypecode");
		}
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return employeeAttendenceService.getEmpByEmployeeType(nemptypecode, userInfo);
	}
	
	/**
	 * This method will is used to make a new entry to employeeattendence table. 
	 * 
	 * @param inputMap map object holding params ( 
	 * 								employeeattendence [EmployeeAttendence] object holding details to be added in employeeattendence table,
	 * 								userinfo [UserInfo] holding logged in user details
	 *                              ) 
	 *                           Input:{
									    "employeeattendence":{"nempattendencecode":1,"nemptypecode":1,
									    	"nusercode":2,"nispresent":3, "ntzentrydatetime":"2025-06-03T15:19:35Z",
									    	"sremarks":"","ntzentrydatetime":-1,"noffsetdentrydatetime":-1
									    	"dexitdatetime":"2025-06-03T15:19:35Z","ntzexitdatetime":-1, "noffsetdexitdatetime":-1
									    	"dattendencedate":"2025-06-03T15:19:35Z","ntzattendencedate":-1, "noffsetdattendencedate":-1},
									    "attendenceDate": "2025-05-04T00:00:00",
  										"nemptypecode": 2,
									    "userinfo":{
							                "activelanguagelist": ["en-US"],
							                "isutcenabled": 4,"ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole": -1,
							                "nformcode": 274,"nmastersitecode": -1,"nmodulecode": 3,
							                "nreasoncode": 0,"nsitecode": 1,"ntimezonecode": -1,"ntranssitecode": 1,"nusercode": -1,
							                "nuserrole": -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss",                
							                "sgmtoffset": "UTC +00:00","slanguagefilename": "Msg_en_US","slanguagename": "English",
							                "slanguagetypecode": "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
							                "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss","sreason": "","ssitedate": "dd/MM/yyyy",
							                "ssitedatetime": "dd/MM/yyyy HH:mm:ss"}
									}
	 * @return ResponseEntity with list of employeeattendence along with the newly added employeeattendence.
	 * @throws Exception exception
	 */
	@PostMapping(value = "/createEmployeeAttendence")
	public ResponseEntity<Object> createEmployeeAttendence(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return employeeAttendenceService.createEmployeeAttendence(inputMap, userInfo);

	}

	/**
	 * This method is used to retrieve a specific employeeattendence record.
	 * @param inputMap [Map] map object with "nempattendencecode", "nemptypecode" and "userinfo" as keys for which the data is to be fetched
	 *                  Input:{ "nempattendencecode": 1,
	 *                  		"nemptypecode":2,
							    "userinfo":{
							                "activelanguagelist": ["en-US"],
							                "isutcenabled": 4,"ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole": -1,
							                "nformcode": 274,"nmastersitecode": -1,"nmodulecode": 3,
							                "nreasoncode": 0,"nsitecode": 1,"ntimezonecode": -1,"ntranssitecode": 1,"nusercode": -1,
							                "nuserrole": -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss",                
							                "sgmtoffset": "UTC +00:00","slanguagefilename": "Msg_en_US","slanguagename": "English",
							                "slanguagetypecode": "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
							                "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss","sreason": "","ssitedate": "dd/MM/yyyy",
							                "ssitedatetime": "dd/MM/yyyy HH:mm:ss"}}
	 * @return ResponseEntity with EmployeeAttendence object for the specified primary key / with string message as
	 * 						'Deleted' if the employeeattendence record is not available
	 * @throws Exception exception
	 */
	@PostMapping(value = "/getActiveEmployeeAttendenceById")
	public ResponseEntity<Object> getActiveEmployeeAttendenceById(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return employeeAttendenceService.getActiveEmployeeAttendenceById(inputMap, userInfo);
	}

	/**
	 * This method is used to update entry in employeeattendence table. Need to
	 * validate that the employeeattendence object to be updated is active
	 * before updating details in database. 
	 * 
	 * @param inputMap [map object holding params(
	 * 					employeeattendence [EmployeeAttendence] object holding details to be updated in employeeattendence table,
	 * 								userinfo [UserInfo] holding logged in user details) 
	 * 					Input:{
     						"employeeattendence":{"nusercode":1,"nispresent":3,}",
									    	"nemptypecode":2,"dentrydatetime":"2025-06-08T15:19:35Z",
									    	"sremarks":"NA","ntzentrydatetime":-1,"noffsetdentrydatetime":-1
									    	"dattendencedate":"2025-06-08T15:19:35Z","ntzattendencedate":-1,"noffsetdattendencedate":-1 },
							"attendenceDate": "2025-05-08T00:00:00",
  							"nprojecttypecode": 2,
							"userinfo":{
								"activelanguagelist": ["en-US"],
							    "isutcenabled": 4,"ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole": -1,
							    "nformcode": 274,"nmastersitecode": -1,"nmodulecode": 3,
							    "nreasoncode": 0,"nsitecode": 1,"ntimezonecode": -1,"ntranssitecode": 1,"nusercode": -1,
							    "nuserrole": -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss",                
							    "sgmtoffset": "UTC +00:00","slanguagefilename": "Msg_en_US","slanguagename": "English",
							    "slanguagetypecode": "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
							    "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss","sreason": "","ssitedate": "dd/MM/yyyy",
							    "ssitedatetime": "dd/MM/yyyy HH:mm:ss"}
	 * 	  					}
	 * @return ResponseEntity with string message as 'Already Deleted' if the employeeAttendence record is not available/ 
	 * 			list of all employeeattendence and along with the updated employeeattendence.	 
	 * @throws Exception exception
	 */	
	@PostMapping(value = "/updateEmployeeAttendence")
	public ResponseEntity<Object> updateEmployeeAttendence(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		objmapper.registerModule(new JavaTimeModule());
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return employeeAttendenceService.updateEmployeeAttendence(inputMap, userInfo);
	}

	/**
	 * This method is used to delete an entry in employeeattendence table.
	 * 
	 * @param inputMap [Map] object with keys of EmployeeAttendence entity and UserInfo object.
	 *                    Input:{ "nempattendencecode": 1,
	 *                  		"nemptypecode":2,
							    "userinfo":{
							                "activelanguagelist": ["en-US"],
							                "isutcenabled": 4,"ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole": -1,
							                "nformcode": 274,"nmastersitecode": -1,"nmodulecode": 3,
							                "nreasoncode": 0,"nsitecode": 1,"ntimezonecode": -1,"ntranssitecode": 1,"nusercode": -1,
							                "nuserrole": -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss",                
							                "sgmtoffset": "UTC +00:00","slanguagefilename": "Msg_en_US","slanguagename": "English",
							                "slanguagetypecode": "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
							                "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss","sreason": "","ssitedate": "dd/MM/yyyy",
							                "ssitedatetime": "dd/MM/yyyy HH:mm:ss"}}
	 * @return ResponseEntity with string message as 'Already Deleted' if the employeeattendence already deleted/ 
	 * 			list of employeeattendence along with the newly added employeeattendence.
	 * @throws Exception exception
	 */
	@PostMapping(value = "/deleteEmployeeAttendence")
	public ResponseEntity<Object> deleteEmployeeAttendence(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		objmapper.registerModule(new JavaTimeModule());

		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return employeeAttendenceService.deleteEmployeeAttendence(inputMap, userInfo);
	}
}
