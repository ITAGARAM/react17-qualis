package com.agaramtech.qualis.storagemanagement.service.storageinstrument;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.instrumentmanagement.model.StorageInstrument;

/**
 * This class holds methods to perform CRUD operation on 'storagesamplecollection' table through its DAO layer.
 */
@Transactional(readOnly = true, rollbackFor = Exception.class)
@Service
public class StorageInstrumentServiceImpl implements StorageInstrumentService {

	private final StorageInstrumentDAO storageInstrumentDAO;
	private final CommonFunction commonFunction;

	public StorageInstrumentServiceImpl(StorageInstrumentDAO storageInstrumentDAO,CommonFunction commonFunction) {
		super();
		this.commonFunction=commonFunction;
		this.storageInstrumentDAO = storageInstrumentDAO;
	}

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
	@Override
	public ResponseEntity<Object> getStorageInstrument(final UserInfo userInfo) throws Exception {
		return storageInstrumentDAO.getStorageInstrument(userInfo);
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
	@Transactional
	@Override
	public ResponseEntity<Object> createStorageInstrument(final Map<String, Object> inputMap) throws Exception {
		return storageInstrumentDAO.createStorageInstrument(inputMap);
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
	@Transactional
	@Override
	public ResponseEntity<Object> updateStorageInstrument(final Map<String,Object> inputMap) throws Exception {
		return storageInstrumentDAO.updateStorageInstrument(inputMap);

		
		
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
	@Transactional
	@Override
	public ResponseEntity<Object> deleteStorageInstrument(final Map<String,Object> inputMap) throws Exception {
		return storageInstrumentDAO.deleteStorageInstrument(inputMap);
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
	@Override
	public ResponseEntity<Object> getActiveStorageInstrument(final int nstorageinstrumentcode,final UserInfo userInfo) throws Exception {
		
		final StorageInstrument objStorageInstrument = storageInstrumentDAO.getActiveStorageInstrument(nstorageinstrumentcode,userInfo);
		if (objStorageInstrument == null) {
			//status code:417
			return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(), userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		}
		else {
			return new ResponseEntity<>(objStorageInstrument, HttpStatus.OK);
		}
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
		@Override
		public ResponseEntity<Object> getStorageStructure(final Map<String, Object> inputMap) throws Exception {
			return storageInstrumentDAO.getStorageStructure(inputMap);
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
		@Override
		public ResponseEntity<Object> getStorageInstrumentCategory(final Map<String, Object> inputMap) throws Exception {
			return storageInstrumentDAO.getStorageInstrumentCategory(inputMap);
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
		@Override
		public ResponseEntity<Map<String,Object>> getInstrumentByCategory(final Map<String, Object> inputMap) throws Exception {
			return storageInstrumentDAO.getInstrumentByCategory(inputMap);
		}

		@Override
		public ResponseEntity<Object> getStorageInstrumentType(final Map<String, Object> inputMap) throws Exception {
			return storageInstrumentDAO.getStorageInstrumentType(inputMap);
		}
		
		
	}
	
