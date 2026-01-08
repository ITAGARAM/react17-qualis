package com.agaramtech.qualis.storagemanagement.service.storageinstrument;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.instrumentmanagement.model.StorageInstrument;

/**
 * This interface holds declarations to perform CRUD operation on
 * 'storageinstrument' table
 */
public interface StorageInstrumentDAO {

	/**
	 * This method is used to retrieve list of available Storage Instrument(s).
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode": -1}}
	 * @return response entity object holding response status and list of all
	 *         Storage Instrument
	 * @throws Exception exception
	 */
	public ResponseEntity<Object> getStorageInstrument(final UserInfo userInfo) throws Exception;

	
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
	public ResponseEntity<Object> createStorageInstrument(final Map<String, Object> inputMap) throws Exception;

	
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
	public ResponseEntity<Object> updateStorageInstrument(final Map<String, Object> inputMap) throws Exception;

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
	public ResponseEntity<Object> deleteStorageInstrument(final Map<String, Object> inputMap) throws Exception;
	
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
	public StorageInstrument getActiveStorageInstrument(final int nstorageinstrumentcode, final UserInfo userInfo)
			throws Exception;
	
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
	public ResponseEntity<Object> getStorageStructure(final Map<String, Object> inputMap) throws Exception;

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
	
	public ResponseEntity<Object> getStorageInstrumentCategory(final Map<String, Object> inputMap) throws Exception;
	
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
	public ResponseEntity<Map<String, Object>> getInstrumentByCategory(final Map<String, Object> inputMap) throws Exception;
	
	public ResponseEntity<Object> getStorageInstrumentType(final Map<String,Object> inputMap) throws Exception;

}
