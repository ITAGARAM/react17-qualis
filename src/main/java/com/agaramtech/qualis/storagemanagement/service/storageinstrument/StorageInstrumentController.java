package com.agaramtech.qualis.storagemanagement.service.storageinstrument;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.restcontroller.RequestContext;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This controller is used to dispatch the input request to its relevant service methods
 * to access the Storage Instrument Service methods.
 */
@RestController
@RequestMapping("/storageinstrument")
public class StorageInstrumentController {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(StorageInstrumentController.class);
	
	private final StorageInstrumentService storageInstrumentService;
	private RequestContext requestContext;

	/**
	 * This constructor injection method is used to pass the object dependencies to the class.
	 * @param requestContext RequestContext to hold the request
	 * @param storageInstrumentService StorageInstrumentService
	 */
	public StorageInstrumentController(StorageInstrumentService storageInstrumentService, RequestContext requestContext) {
		super();
		this.storageInstrumentService = storageInstrumentService;
		this.requestContext = requestContext;
	}

	/**
	 * This method is used to retrieve list of available storage instrument(s). 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched 	
	 * 					Input : {"userinfo":{nmastersitecode": -1}}				
	 * @return response entity object holding response status and list of all sample collections
	 * @throws Exception exception
	 */
	@PostMapping(value = "/getStorageInstrument")
	public ResponseEntity<Object> getStorageInstrument(@RequestBody Map<String, Object> inputMap) throws Exception {
		
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		
		LOGGER.info("getStorageInstrument called");
		return storageInstrumentService.getStorageInstrument(userInfo);
	}
	
	
	/**
	 * This method will is used to make a new entry to storageinstrument and samplestoragemapping table.
	 * 
	 * @param inputMap map object holding params ( storageinstrument
	 *                 [StorageInstrument] object holding details to be added in
	 *                 storageinstrument table, userinfo [UserInfo] holding logged
	 *                 in user details ) Input:{ "storageinstrument": {
	 *                 "nstoragecategorycode": 1, "nsamplestoragelocationcode": 1,
	 *                 nsamplestorageversioncode:1,nstorageconditioncode:1,ninstrumentcatcode:1,ninstrumentcode:1,nstorageconditioncode:1},
	 *                 "userinfo":{ "activelanguagelist": ["en-US"], "isutcenabled":
	 *                 4,"ndeptcode": - 1,"ndeputyusercode": -1,"ndeputyuserrole":
	 *                 -1, "nformcode": 33,"nmastersitecode": -1, "nmodulecode": 1,
	 *                 "nreasoncode": 0,"nsitecode": 1,"ntimezonecode":
	 *                 -1,"ntranssitecode": 1,"nusercode": -1, "nuserrole":
	 *                 -1,"nusersitecode": -1, "sdatetimeformat": "dd/MM/yyyy
	 *                 HH:mm:ss", "sgmtoffset": "UTC +00:00","slanguagefilename":
	 *                 "Msg_en_US","slanguagename": "English","slanguagetypecode":
	 *                 "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss","sreason":
	 *                 "","ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy
	 *                 HH:mm:ss"} }
	 * @return ResponseEntity with list of storageinstrument along with the newly
	 *         added storageinstrument.
	 * @throws Exception exception
	 */
	@PostMapping(value = "/createStorageInstrument")
	public ResponseEntity<Object> createStorageInstrument(@RequestBody Map<String, Object> inputMap) throws Exception {
		
		
		LOGGER.info("createStorageInstrument called");
		return storageInstrumentService.createStorageInstrument(inputMap);
	}
	
	/**
	 * This method is used to update selected storageinstrument details.
	 * 
	 * @param inputMap [map object holding params( unit [Unit] object holding
	 *                 details to be updated in unit table, userinfo [UserInfo]
	 *                 holding logged in user details) Input:{ {"storageinstrument":
	 *                 { "nstoragecategorycode": 1, "nsamplestoragelocationcode": 1,
	 *                 nsamplestorageversioncode:1,nstorageconditioncode:1,ninstrumentcatcode:1,ninstrumentcode:1,nstorageconditioncode:1}},
	 *                 "userinfo":{ "activelanguagelist": ["en-US"],"isutcenabled":
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
	 * 
	 * 
	 * @return ResponseEntity with list of storageinstrument along with the newly
	 *         added storageinstrument.
	 * @throws Exception exception
	 */
	@PostMapping(value = "/updateStorageInstrument")
	public ResponseEntity<Object> updateStorageInstrument(@RequestBody Map<String, Object> inputMap) throws Exception {
		
		
		LOGGER.info("updateStorageInstrument called");
		return storageInstrumentService.updateStorageInstrument(inputMap);
	}
	
	/**
	 * This method is used to delete an entry in storageinstrument table
	 * 
	 * @param inputMap [Map] object with keys of Unit entity and UserInfo object.
	 *                 Input:{ {"storageinstrument": { "nstoragecategorycode": 1,
	 *                 "nsamplestoragelocationcode": 1,
	 *                 nsamplestorageversioncode:1,nstorageconditioncode:1,ninstrumentcatcode:1,ninstrumentcode:1,nstorageconditioncode:1}},
	 *                 "userinfo":{ "activelanguagelist": ["en-US"],"isutcenabled":
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
	 * @return ResponseEntity with string message as 'Already deleted' if the unit record is not available/ 
	 * 			string message as 'Record is used in....' when the unit is associated in transaction /
	 * 			list of all units excluding the deleted record 
	 * @throws Exception exception
	 */
	
	@PostMapping(value = "/deleteStorageInstrument")
	public ResponseEntity<Object> deleteStorageInstrument(@RequestBody Map<String, Object> inputMap) throws Exception {
		
		LOGGER.info("deleteStorageInstrument called");

		return storageInstrumentService.deleteStorageInstrument(inputMap);

	}
	
	/**
	 * This method is used to retrieve a specific storageinstrument record.
	 * 
	 * @param inputMap [Map] map object with "nstorageinstrumentcode" and "userinfo" as keys for
	 *                 which the data is to be fetched Input:{ "nstorageinstrumentcode": 1,
	 *                 "userinfo":{ "activelanguagelist": ["en-US"], "isutcenabled":
	 *                 4,"ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole":
	 *                 -1, "nformcode": 33,"nmastersitecode": -1,"nmodulecode": 1,
	 *                 "nreasoncode": 0,"nsitecode": 1,"ntimezonecode":
	 *                 -1,"ntranssitecode": 1,"nusercode": -1, "nuserrole":
	 *                 -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy
	 *                 HH:mm:ss", "sgmtoffset": "UTC +00:00","slanguagefilename":
	 *                 "Msg_en_US","slanguagename": "English", "slanguagetypecode":
	 *                 "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss","sreason":
	 *                 "","ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy
	 *                 HH:mm:ss"}}
	 * @return ResponseEntity with Storage Instrument object for the specified primary key / with
	 *         string message as 'Deleted' if the storageinstrument record is not available
	 * @throws Exception exception
	 */
	@PostMapping(value = "/getActiveStorageInstrument")
	public ResponseEntity<Object> getActiveStorageInstrument(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		int nstorageinstrumentcode=-1;
		
		if(inputMap.containsKey("nstorageinstrumentcode")) {
			nstorageinstrumentcode=(int)inputMap.get("nstorageinstrumentcode");
		}
		LOGGER.info("getActiveStorageInstrument called");
		return storageInstrumentService.getActiveStorageInstrument(nstorageinstrumentcode,userInfo);
	}
	
	/**
	 * This method is used to retrieve a specific storage structure record based on the storage category.
	 * 
	 * @param inputMap [Map] map object with "nstoragecategorycode" and "userinfo" as keys for
	 *                 which the data is to be fetched Input:{ "nstoragecategorycode": 1,
	 *                 "userinfo":{ "activelanguagelist": ["en-US"], "isutcenabled":
	 *                 4,"ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole":
	 *                 -1, "nformcode": 33,"nmastersitecode": -1,"nmodulecode": 1,
	 *                 "nreasoncode": 0,"nsitecode": 1,"ntimezonecode":
	 *                 -1,"ntranssitecode": 1,"nusercode": -1, "nuserrole":
	 *                 -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy
	 *                 HH:mm:ss", "sgmtoffset": "UTC +00:00","slanguagefilename":
	 *                 "Msg_en_US","slanguagename": "English", "slanguagetypecode":
	 *                 "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss","sreason":
	 *                 "","ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy
	 *                 HH:mm:ss"}}
	 * @return ResponseEntity will return a list of StorageStructure objects for the specified nstoragecategorycode.
	 * @throws Exception exception
	 */
	@PostMapping(value = "/getStorageStructure")
	public ResponseEntity<Object> getStorageStructure(@RequestBody Map<String, Object> inputMap) throws Exception {
		
	
		LOGGER.info("getStorageStructure called");
		return storageInstrumentService.getStorageStructure(inputMap);
	}
	
	/**
	 * This method is used to retrieve the specific Instrument Category associated with the given Storage Instrument..
	 * 
	 * @param inputMap [Map] map object with "userinfo" as keys for
	 *                 which the data is to be fetched Input:{
	 *                 "userinfo":{ "activelanguagelist": ["en-US"], "isutcenabled":
	 *                 4,"ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole":
	 *                 -1, "nformcode": 33,"nmastersitecode": -1,"nmodulecode": 1,
	 *                 "nreasoncode": 0,"nsitecode": 1,"ntimezonecode":
	 *                 -1,"ntranssitecode": 1,"nusercode": -1, "nuserrole":
	 *                 -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy
	 *                 HH:mm:ss", "sgmtoffset": "UTC +00:00","slanguagefilename":
	 *                 "Msg_en_US","slanguagename": "English", "slanguagetypecode":
	 *                 "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss","sreason":
	 *                 "","ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy
	 *                 HH:mm:ss"}}
	 * @return ResponseEntity will return a list of Instrument Category objects for the specified storage instrument.
	 * @throws Exception exception
	 */
	@PostMapping(value = "/getStorageInstrumentCategory")
	public ResponseEntity<Object> getStorageInstrumentCategory(@RequestBody Map<String, Object> inputMap) throws Exception {
		
		
		LOGGER.info("getStorageInstrumentCategory called");
		return storageInstrumentService.getStorageInstrumentCategory(inputMap);
	}
	
	/**
	 * This method is used to retrieve the specific Instrument(s) associated with
	 * the given Instrument Category.
	 * 
	 * @param inputMap [Map] map object with "ninstrumentcatcode" and "userinfo" as
	 *                 keys for which the data is to be fetched
	 *                 Input:{"ninstrumentcatcode":1, "userinfo":{
	 *                 "activelanguagelist": ["en-US"], "isutcenabled":
	 *                 4,"ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole":
	 *                 -1, "nformcode": 33,"nmastersitecode": -1,"nmodulecode": 1,
	 *                 "nreasoncode": 0,"nsitecode": 1,"ntimezonecode":
	 *                 -1,"ntranssitecode": 1,"nusercode": -1, "nuserrole":
	 *                 -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy
	 *                 HH:mm:ss", "sgmtoffset": "UTC +00:00","slanguagefilename":
	 *                 "Msg_en_US","slanguagename": "English", "slanguagetypecode":
	 *                 "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss","sreason":
	 *                 "","ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy
	 *                 HH:mm:ss"}}
	 * @return ResponseEntity will return a list of Instrument objects for the
	 *         specified ninstrumentcatcode.
	 * @throws Exception exception
	 */
	@PostMapping(value = "/getInstrumentByCategory")
	public ResponseEntity<Map<String,Object>> getInstrumentByCategory(@RequestBody Map<String, Object> inputMap) throws Exception {
		
		
		LOGGER.info("getInstrumentByCategory called");
		return storageInstrumentService.getInstrumentByCategory(inputMap);
	}
	
	@PostMapping(value = "/getStorageInstrumentType")
	public ResponseEntity<Object> getStorageInstrumentType(@RequestBody Map<String, Object> inputMap) throws Exception {
		
				return storageInstrumentService.getStorageInstrumentType(inputMap);
	}
}