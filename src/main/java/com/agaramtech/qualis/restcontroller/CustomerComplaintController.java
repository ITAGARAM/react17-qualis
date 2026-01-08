package com.agaramtech.qualis.restcontroller;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import com.agaramtech.qualis.credential.model.CustomerComplaintFile;
import com.agaramtech.qualis.credential.service.customercomplaint.CustomerComplaintService;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletResponse;

/**
 * This controller is used to dispatch the input request to its relevant service
 * methods to access the CustomerComplaint Service methods.
 * 
 * @author Mullai Balaji.V [SWSM-9] Customer Complaints - Screen Development -
 *         Agaram Technologies
 */

@RestController
@RequestMapping("/customercomplaint")
public class CustomerComplaintController {

	private static final Logger LOGGER = LoggerFactory.getLogger(CustomerComplaintController.class);
	private RequestContext requestContext;
	private final CustomerComplaintService customerComplaintService;

	/**
	 * This constructor injection method is used to pass the object dependencies to
	 * the class.
	 * 
	 * @param requestContext           RequestContext to hold the request
	 * @param customerComplaintService CustomerComplaintService
	 */
	public CustomerComplaintController(RequestContext requestContext,
			CustomerComplaintService customerComplaintService) {
		super();
		this.requestContext = requestContext;
		this.customerComplaintService = customerComplaintService;
	}

	/**
	 * This method will is used to make a new entry to customercomplaint table.
	 * 
	 * @param inputMap map object holding params ( customercomplaint
	 *                 [CustomerComplaint] object holding details to be added in
	 *                 customercomplaint table, userinfo [UserInfo] holding logged
	 *                 in user details ) Input:{ "customercomplaint": {
	 *                 "ncustomercomplaintcode": "masters" }, "userinfo":{
	 *                 "activelanguagelist": ["en-US"], "isutcenabled":
	 *                 4,"ndeptcode": - 1,"ndeputyusercode": -1,"ndeputyuserrole":
	 *                 -1, "nformcode": 275,"nmastersitecode": -1,"nmodulecode": 1,
	 *                 "nreasoncode": 0,"nsitecode": 1,"ntimezonecode":
	 *                 -1,"ntranssitecode": 1,"nusercode": -1, "nuserrole":
	 *                 -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy
	 *                 HH:mm:ss", "sgmtoffset": "UTC +00:00","slanguagefilename":
	 *                 "Msg_en_US","slanguagename": "English", "slanguagetypecode":
	 *                 "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss","sreason":
	 *                 "","ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy
	 *                 HH:mm:ss"} }
	 * @return ResponseEntity with string message as 'Already Exists' if the
	 *         customercomplaint already exists/ list of customercomplaint along
	 *         with the newly added customercomplaint.
	 * @throws Exception exception
	 */

	@PostMapping(value = "/createCustomerComplaint")
	public ResponseEntity<Object> createCustomerComplaint(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return customerComplaintService.createCustomerComplaint(inputMap);
	}

	/**
	 * This method will is used to make a record as Initiate in
	 * CustomerComplaintHistory Table
	 * 
	 * @param inputMap map object holding params ( customercomplaint
	 *                 [CustomerComplaint] object holding details to be added in
	 *                 customercomplaint table, userinfo [UserInfo] holding logged
	 *                 in user details ) Input:{ "customercomplaint": {
	 *                 "ncustomercomplaintcode": "masters" }, "userinfo":{
	 *                 "activelanguagelist": ["en-US"], "isutcenabled":
	 *                 4,"ndeptcode": - 1,"ndeputyusercode": -1,"ndeputyuserrole":
	 *                 -1, "nformcode": 275,"nmastersitecode": -1,"nmodulecode": 1,
	 *                 "nreasoncode": 0,"nsitecode": 1,"ntimezonecode":
	 *                 -1,"ntranssitecode": 1,"nusercode": -1, "nuserrole":
	 *                 -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy
	 *                 HH:mm:ss", "sgmtoffset": "UTC +00:00","slanguagefilename":
	 *                 "Msg_en_US","slanguagename": "English", "slanguagetypecode":
	 *                 "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss","sreason":
	 *                 "","ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy
	 *                 HH:mm:ss"} }
	 * @return ResponseEntity with string message as 'Already Exists' if the
	 *         customercomplaint already exists/ list of customercomplaint along
	 *         with the newly added customercomplaint.
	 * @throws Exception exception
	 */

	@PostMapping(value = "/initiateCustomerComplaint")
	public ResponseEntity<Object> initiateCustomerComplaint(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);

		return customerComplaintService.initiateCustomerComplaint(inputMap);
	}

	/**
	 * This method will is used to make a record as Closed in
	 * CustomerComplaintHistory Table
	 * 
	 * @param inputMap map object holding params ( customercomplaint
	 *                 [CustomerComplaint] object holding details to be added in
	 *                 customercomplaint table, userinfo [UserInfo] holding logged
	 *                 in user details ) Input:{ "customercomplaint": {
	 *                 "ncustomercomplaintcode": "masters" }, "userinfo":{
	 *                 "activelanguagelist": ["en-US"], "isutcenabled":
	 *                 4,"ndeptcode": - 1,"ndeputyusercode": -1,"ndeputyuserrole":
	 *                 -1, "nformcode": 275,"nmastersitecode": -1,"nmodulecode": 1,
	 *                 "nreasoncode": 0,"nsitecode": 1,"ntimezonecode":
	 *                 -1,"ntranssitecode": 1,"nusercode": -1, "nuserrole":
	 *                 -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy
	 *                 HH:mm:ss", "sgmtoffset": "UTC +00:00","slanguagefilename":
	 *                 "Msg_en_US","slanguagename": "English", "slanguagetypecode":
	 *                 "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss","sreason":
	 *                 "","ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy
	 *                 HH:mm:ss"} }
	 * @return ResponseEntity with string message as 'Already Exists' if the
	 *         customercomplaint already exists/ list of customercomplaint along
	 *         with the newly added customercomplaint.
	 * @throws Exception exception
	 */

	@PostMapping(value = "/closeCustomerComplaint")
	public ResponseEntity<Object> closeCustomerComplaint(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);

		return customerComplaintService.closeCustomerComplaint(inputMap);
	}

	/**
	 * This Method is used to get the over all customercomplaint with respect to
	 * site at Initital Get
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode": -1}}
	 * @return a response entity which holds the list of getCustomerComplaint with
	 *         respect to site and also have the HTTP response code
	 * @throws Exception
	 */
	@PostMapping(value = "/getCustomerComplaint")
	public ResponseEntity<Object> getCustomerComplaint(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		LOGGER.info("getCustomerComplaint called");
		requestContext.setUserInfo(userInfo);
		return customerComplaintService.getCustomerComplaint(inputMap, userInfo);
	}

	/**
	 * This Method is used to get the over all customercomplaintData and History Record and File While refresh 
	 * respect to site 
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode": -1}}
	 * @return a response entity which holds the list of getCustomerComplaintData
	 *         with respect to site and also have the HTTP response code
	 * @throws Exception
	 */
	@PostMapping(value = "/getCustomerComplaintData")
	public ResponseEntity<Object> getCustomerComplaintData(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return customerComplaintService.getCustomerComplaintData(inputMap, userInfo);

	}

	/**
	 * This Method is used to get the over all Region
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode": -1}}
	 * @return a response entity which holds the list of getRegion with respect to
	 *         site and also have the HTTP response code
	 * @throws Exception
	 */
	@PostMapping(value = "/getRegion")
	public ResponseEntity<Object> getRegion(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();

		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);

		return customerComplaintService.getRegion(userInfo);

	}

	/**
	 * This Method is used to get the over all District with respect to Region
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode": -1}}
	 * @return a response entity which holds the list of getDistrict 
	 *         and also have the HTTP response code
	 * @throws Exception
	 */
	@PostMapping(value = "/getDistrictLab")
	public ResponseEntity<Object> getDistrict(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		final int nregioncode = (int) inputMap.get("primarykey");
		return customerComplaintService.getDistrict(nregioncode, userInfo);

	}

	/**
	 * This Method is used to get the over all City with respect to District
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode": -1}}
	 * @return a response entity which holds the list of getCity
	 *    and also have the HTTP response code
	 * @throws Exception
	 */

	@PostMapping(value = "/getCustomerComplaintSubDivisionalLab")
	public ResponseEntity<Object> getCity(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		final int ndistrictcode = (int) inputMap.get("primarykey");
		return customerComplaintService.getCity(ndistrictcode, userInfo);

	}

	/**
	 * This Method is used to get the over all Village with respect to City
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode": -1}}
	 * @return a response entity which holds the list of getVillage 
	 *         and also have the HTTP response code
	 * @throws Exception
	 */

	//added by sujatha ATE_274 23-09-2025 to get villages with one more check based on nsitehierarchyconfigcode
	@PostMapping(value = "/getVillage")
	public ResponseEntity<Object> getVillage(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		final int ncitycode = (int) inputMap.get("primarykey");
		final int nsitehierarchyconfigcode = (int) inputMap.get("nsitehierarchyconfigcode");
		return customerComplaintService.getVillage(nsitehierarchyconfigcode,ncitycode, userInfo);

	}

	/**
	 * This Method is used to get the particular record by primarykey in
	 * CustomerComplaint Table
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode": -1}}
	 * @return a response entity which holds the list of
	 *         getActiveCustomerComplaintById with respect to site and also have the
	 *         HTTP response code
	 * @throws Exception
	 */
	@PostMapping(value = "/getActiveCustomerComplaintById")
	public ResponseEntity<Object> getActiveCustomerComplaintById(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final int ncustomercomplaintcode = (int) inputMap.get("ncustomercomplaintcode");
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);

		return customerComplaintService.getActiveCustomerComplaintById(ncustomercomplaintcode, userInfo);
	}

	/**
	 * This method is used to update selected customercomplaint details.
	 * 
	 * @param inputMap [map object holding params( customercomplaint
	 *                 [customercomplaint] object holding details to be updated in
	 *                 customercomplaint table, userinfo [UserInfo] holding logged
	 *                 in user details) Input:{ "customercomplaint":
	 *                 {"ncustomercomplaintcode":1,"scustomercomplaintname":
	 *                 "m","scustomercomplaintsynonym": "m", "sdescription": "m",
	 *                 "ndefaultstatus": 3 }, "userinfo":{ "activelanguagelist":
	 *                 ["en-US"],"isutcenabled": 4,"ndeptcode":
	 *                 -1,"ndeputyusercode": -1,"ndeputyuserrole": -1, "nformcode":
	 *                 33,"nmastersitecode": -1, "nmodulecode": 1, "nreasoncode":
	 *                 0,"nsitecode": 1,"ntimezonecode": -1,"ntranssitecode":
	 *                 1,"nusercode": -1, "nuserrole": -1,"nusersitecode":
	 *                 -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss", "sgmtoffset":
	 *                 "UTC +00:00", "slanguagefilename":
	 *                 "Msg_en_US","slanguagename": "English", "slanguagetypecode":
	 *                 "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss", "sreason": "",
	 *                 "ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy
	 *                 HH:mm:ss"}}
	 * 
	 * 
	 * @return ResponseEntity with string message as 'Already Deleted' if the
	 *         customercomplaint record is not available/ list of all
	 *         customercomplaints and along with the updated customercomplaint.
	 * @throws Exception exception
	 */
	@PostMapping(value = "/updateCustomerComplaint")
	public ResponseEntity<Object> updateCustomerComplaint(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);

		return customerComplaintService.updateCustomerComplaint(inputMap);
	}

	/**
	 * This method is used to delete an entry in customercomplaint table
	 * 
	 * @param inputMap [Map] object with keys of customercomplaint entity and
	 *                 UserInfo object. Input:{ "customercomplaint":
	 *                 {"ncustomercomplaintcode":1}, "userinfo":{
	 *                 "activelanguagelist": ["en-US"],"isutcenabled":
	 *                 4,"ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole":
	 *                 -1, "nformcode": 33,"nmastersitecode": -1, "nmodulecode": 1,
	 *                 "nreasoncode": 0,"nsitecode": 1,"ntimezonecode":
	 *                 -1,"ntranssitecode": 1,"nusercode": -1, "nuserrole":
	 *                 -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy
	 *                 HH:mm:ss", "sgmtoffset": "UTC +00:00", "slanguagefilename":
	 *                 "Msg_en_US","slanguagename": "English", "slanguagetypecode":
	 *                 "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss", "sreason": "",
	 *                 "ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy
	 *                 HH:mm:ss"}}
	 * @return ResponseEntity with string message as 'Already deleted' if the
	 *         customercomplaint record is not available/ string message as 'Record
	 *         is used in....' when the customercomplaint is associated in
	 *         transaction / list of all customercomplaints excluding the deleted
	 *         record
	 * @throws Exception exception
	 */

	@PostMapping(value = "/deleteCustomerComplaint")
	public ResponseEntity<Object> deleteCustomerComplaint(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);

		return customerComplaintService.deleteCustomerComplaint(inputMap);
	}

	/**
	 * This Method is used to get the particular record by primarykey in
	 * CustomerComplaint Table 
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode": -1}}
	 * @return a response entity which holds the list of
	 *         getCustomerComplaintRecord with respect to site and also have the
	 *         HTTP response code
	 * @throws Exception
	 */
	@PostMapping(value = "/getCustomerComplaintRecord")
	public ResponseEntity<Object> getCustomerComplaintRecord(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		final int ncustomercomplaintcode = objMapper.convertValue(inputMap.get("ncustomercomplaintcode"),
				Integer.class);

		requestContext.setUserInfo(userInfo);
		return customerComplaintService.getCustomerComplaintRecord(ncustomercomplaintcode, userInfo);

	}

	/**
	 * This Method is used to create a File in CustomerComplaintFile respect to site
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode": -1}}
	 * @return a response entity which holds the list of createCustomerComplaintFile
	 *         with respect to site and also have the HTTP response code
	 * @throws Exception
	 */
	@PostMapping(value = "/createCustomerComplaintFile")
	public ResponseEntity<Object> createCustomerComplaintFile(MultipartHttpServletRequest request) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.readValue(request.getParameter("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return customerComplaintService.createCustomerComplaintFile(request, userInfo);

	}

	/**
	 * This Method is used to update a File in CustomerComplaintFile respect to site
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode": -1}}
	 * @return a response entity which holds the list of createCustomerComplaintFile
	 *         with respect to site and also have the HTTP response code
	 * @throws Exception
	 */
	@PostMapping(value = "/editCustomerComplaintFile")
	public ResponseEntity<Object> editCustomerComplaintFile(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final CustomerComplaintFile objCustomerComplaintFile = objMapper
				.convertValue(inputMap.get("customercomplaintfile"), CustomerComplaintFile.class);
		requestContext.setUserInfo(userInfo);
		return customerComplaintService.editCustomerComplaintFile(objCustomerComplaintFile, userInfo);
	}

	/**
	 * This method is used to delete an entry in CustomerComplaintFile table
	 * 
	 * @param inputMap [Map] object with keys of CustomerComplaintFile entity and
	 *                 UserInfo object. Input:{ "CustomerComplaintFile":
	 *                 {"ncustomercomplaintfilecode":1}, "userinfo":{
	 *                 "activelanguagelist": ["en-US"],"isutcenabled":
	 *                 4,"ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole":
	 *                 -1, "nformcode": 33,"nmastersitecode": -1, "nmodulecode": 1,
	 *                 "nreasoncode": 0,"nsitecode": 1,"ntimezonecode":
	 *                 -1,"ntranssitecode": 1,"nusercode": -1, "nuserrole":
	 *                 -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy
	 *                 HH:mm:ss", "sgmtoffset": "UTC +00:00", "slanguagefilename":
	 *                 "Msg_en_US","slanguagename": "English", "slanguagetypecode":
	 *                 "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss", "sreason": "",
	 *                 "ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy
	 *                 HH:mm:ss"}}
	 * @return ResponseEntity with string message as 'Already deleted' if the
	 *         CustomerComplaintFile record is not available/ string message as
	 *         'Record is used in....' when the CustomerComplaintFile is associated
	 *         in transaction / list of all CustomerComplaintFiles excluding the
	 *         deleted record
	 * @throws Exception exception
	 */
	@PostMapping(value = "/deleteCustomerComplaintFile")
	public ResponseEntity<Object> deleteCustomerComplaintFile(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final CustomerComplaintFile objCustomerComplaintFile = objMapper
				.convertValue(inputMap.get("customercomplaintfile"), CustomerComplaintFile.class);
		requestContext.setUserInfo(userInfo);
		return customerComplaintService.deleteCustomerComplaintFile(objCustomerComplaintFile, userInfo);
	}

	/**
	 * This method is used to edit the file an entry in customercomplaintfile table
	 * 
	 * @param inputMap [Map] object with keys of CustomerComplaintFile entity and
	 *                 UserInfo object. Input:{ "customercomplaintfile":
	 *                 {"ncustomercomplaintfilecode":1}, "userinfo":{
	 *                 "activelanguagelist": ["en-US"],"isutcenabled":
	 *                 4,"ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole":
	 *                 -1, "nformcode": 33,"nmastersitecode": -1, "nmodulecode": 1,
	 *                 "nreasoncode": 0,"nsitecode": 1,"ntimezonecode":
	 *                 -1,"ntranssitecode": 1,"nusercode": -1, "nuserrole":
	 *                 -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy
	 *                 HH:mm:ss", "sgmtoffset": "UTC +00:00", "slanguagefilename":
	 *                 "Msg_en_US","slanguagename": "English", "slanguagetypecode":
	 *                 "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss", "sreason": "",
	 *                 "ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy
	 *                 HH:mm:ss"}}
	 * @return ResponseEntity with string message as 'Already deleted' if the
	 *         CustomerComplaintFile record is not available/ string message as
	 *         'Record is used in....' when the CustomerComplaintFile is associated
	 *         in transaction / list of all CustomerComplaintFiles excluding the
	 *         edit record
	 * @throws Exception exception
	 */
	@PostMapping(value = "/updateCustomerComplaintFile")
	public ResponseEntity<Object> updateCustomerComplaintFile(MultipartHttpServletRequest request) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.readValue(request.getParameter("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return customerComplaintService.updateCustomerComplaintFile(request, userInfo);

	}

	/**
	 * This method is used to view the file an entry in customercomplaintfile table
	 * 
	 * @param inputMap [Map] object with keys of CustomerComplaintFile entity and
	 *                 UserInfo object. Input:{ "customercomplaintfile":
	 *                 {"ncustomercomplaintfilecode":1}, "userinfo":{
	 *                 "activelanguagelist": ["en-US"],"isutcenabled":
	 *                 4,"ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole":
	 *                 -1, "nformcode": 33,"nmastersitecode": -1, "nmodulecode": 1,
	 *                 "nreasoncode": 0,"nsitecode": 1,"ntimezonecode":
	 *                 -1,"ntranssitecode": 1,"nusercode": -1, "nuserrole":
	 *                 -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy
	 *                 HH:mm:ss", "sgmtoffset": "UTC +00:00", "slanguagefilename":
	 *                 "Msg_en_US","slanguagename": "English", "slanguagetypecode":
	 *                 "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss", "sreason": "",
	 *                 "ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy
	 *                 HH:mm:ss"}}
	 * @return ResponseEntity with string message as 'Already deleted' if the
	 *         CustomerComplaintFile record is not available/ string message as
	 *         'Record is used in....' when the CustomerComplaintFile is associated
	 *         in transaction / list of all CustomerComplaintFiles excluding the
	 *         view record
	 * @throws Exception exception
	 */
	@PostMapping(value = "/viewAttachedCustomerComplaintFile")
	public ResponseEntity<Object> viewAttachedCustomerComplaintFile(@RequestBody Map<String, Object> inputMap,
			HttpServletResponse response) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final CustomerComplaintFile objCustomerComplaintFile = objMapper
				.convertValue(inputMap.get("customercomplaintfile"), CustomerComplaintFile.class);
		requestContext.setUserInfo(userInfo);
		return customerComplaintService.viewAttachedCustomerComplaintFile(objCustomerComplaintFile, userInfo);
	}

}
