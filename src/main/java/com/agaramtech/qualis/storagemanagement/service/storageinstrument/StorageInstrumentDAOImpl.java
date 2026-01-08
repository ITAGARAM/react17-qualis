package com.agaramtech.qualis.storagemanagement.service.storageinstrument;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.agaramtech.qualis.basemaster.model.SeqNoBasemaster;
import com.agaramtech.qualis.configuration.model.Settings;
import com.agaramtech.qualis.global.AuditUtilityFunction;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.ProjectDAOSupport;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.global.ValidatorDel;
import com.agaramtech.qualis.instrumentmanagement.model.Instrument;
import com.agaramtech.qualis.instrumentmanagement.model.InstrumentCategory;
import com.agaramtech.qualis.instrumentmanagement.model.InstrumentType;
import com.agaramtech.qualis.instrumentmanagement.model.StorageInstrument;
import com.agaramtech.qualis.storagemanagement.model.SampleStorageStructure;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import lombok.RequiredArgsConstructor;

/**
 * This class is used to perform CRUD Operation on "storageinstrument" table by
 * implementing methods from its interface.
 */
@RequiredArgsConstructor
@Repository
public class StorageInstrumentDAOImpl implements StorageInstrumentDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(StorageInstrumentDAOImpl.class);

	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;
	private ValidatorDel validatorDel;
	private final ProjectDAOSupport projectDAOSupport;

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
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();

		final String query = "select si.nstorageinstrumentcode,si.nstoragecategorycode,sc.sstoragecategoryname,si.nsamplestoragelocationcode,"
				+ " sl.ssamplestoragelocationname,si.nsamplestorageversioncode,ic.sinstrumentcatname,si.ninstrumentcatcode,"
				+ " si.ninstrumentcode,i.sinstrumentid,scon.nstorageconditioncode,scon.sstorageconditionname,"
				+ " coalesce(it.jsondata->'sinstrumenttype'->>'"+userInfo.getSlanguagetypecode()+"'"
				+ "	, it.jsondata->'sinstrumenttype'->>'en-US') as sinstrumenttype,si.ninstrumenttypecode "
				+ "  from storagecategory sc,storageinstrument si,instrumentcategory ic,storagecondition scon,"
				+ " samplestorageversion sv,instrument i,samplestoragelocation sl,instrumenttype it where  si.nstoragecategorycode=sc.nstoragecategorycode"
				+ " and si.nsamplestoragelocationcode=sl.nsamplestoragelocationcode and i.ninstrumentcatcode=ic.ninstrumentcatcode"
				+ " and si.ninstrumentcatcode=ic.ninstrumentcatcode "
				+ " and scon.nstorageconditioncode=si.nstorageconditioncode "
				+ " and si.ninstrumentcode=i.ninstrumentcode and si.nsamplestorageversioncode=sv.nsamplestorageversioncode "
				+ " and sc.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
				+ " and si.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
				+ " and scon.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
				+ " and sl.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
				+ " and sv.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
				+ " and ic.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
				+ " and i.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
				+ " and sc.nsitecode=" + userInfo.getNmastersitecode() + "" + " and si.nsitecode="
				+ userInfo.getNmastersitecode() + "" + " and sl.nsitecode=" + userInfo.getNmastersitecode() + ""
				+ " and sv.nsitecode=" + userInfo.getNmastersitecode() + "" + " and ic.nsitecode="
				+ userInfo.getNmastersitecode() + "" + " and i.nsitecode=" + userInfo.getNmastersitecode() + ""
				+ " and scon.nsitecode=" + userInfo.getNmastersitecode() + " "
				+ " and si.nstorageinstrumentcode>0 and si.ninstrumentcode>0 and si.ninstrumenttypecode=it.ninstrumenttypecode";

		List<StorageInstrument> lstStorageInstrument = jdbcTemplate.query(query, new StorageInstrument());

		outputMap.put("StorageInstrument", lstStorageInstrument);
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	/**
	 * This method will is used to make a new entry to storageinstrument and
	 * samplestoragemapping table.
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
	@Override
	public ResponseEntity<Object> createStorageInstrument(final Map<String, Object> inputMap) throws Exception {
		final String sQuery = " lock  table storageinstrument "+ Enumeration.ReturnStatus.EXCLUSIVEMODE.getreturnstatus();
		jdbcTemplate.execute(sQuery);	
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		final StorageInstrument objStorageInstrument = objMapper.convertValue(inputMap.get("storageinstrument"),
				new TypeReference<StorageInstrument>() {
				});

		final String sequencequery = "select nsequenceno from seqnostoragemanagement where stablename ='storageinstrument'";
		int nsequenceno = jdbcTemplate.queryForObject(sequencequery, Integer.class);

		final String queryString = "INSERT INTO storageinstrument (nstorageinstrumentcode,nstoragecategorycode,nsamplestoragelocationcode,nsamplestorageversioncode,nstorageconditioncode,"
				+ "ninstrumentcatcode,ninstrumentcode,nregionalsitecode,dmodifieddate,nsitecode,nstatus,ninstrumenttypecode)" + "SELECT "
				+ nsequenceno + " + RANK() OVER (ORDER BY ninstrumentcode) AS nstorageinstrumentcode, "
				+ objStorageInstrument.getNstoragecategorycode() + " as nstoragecategorycode,"
				+ objStorageInstrument.getNsamplestoragelocationcode() + " AS nsamplestoragelocationcode," + ""
				+ objStorageInstrument.getNsamplestorageversioncode() + " AS nsamplestorageversioncode," + "  "
				+ objStorageInstrument.getNstorageconditioncode() + " as nstorageconditioncode, "
				+ objStorageInstrument.getNinstrumentcatcode()
				+ " as ninstrumentcatcode,ninstrumentcode,nregionalsitecode," + " '"
				+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + userInfo.getNmastersitecode() + ","
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " ,"+objStorageInstrument.getNinstrumenttypecode()+" as ninstrumenttypecode"
				+ " FROM instrument WHERE ninstrumentcode in (" + objStorageInstrument.getSinstrumentcode() + ");";


		final String updatequery = "update seqnostoragemanagement set nsequenceno =(select max(nstorageinstrumentcode) from storageinstrument) where stablename ='storageinstrument' and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
		
		jdbcTemplate.execute(queryString + updatequery);
	

		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> savedUnitList = new ArrayList<>();
		multilingualIDList.add("IDS_ADDSTORAGEINSTRUMENT");
		savedUnitList.add(objStorageInstrument);

		auditUtilityFunction.fnInsertAuditAction(savedUnitList, 1, null, multilingualIDList, userInfo);

		final String query = "select * from samplestoragelocation sl,samplestorageversion sv "
				+ " where sl.nsamplestoragelocationcode=sv.nsamplestoragelocationcode "
				+ " and sl.nsamplestoragelocationcode=" + objStorageInstrument.getNsamplestoragelocationcode() + ""
				+ " and sv.nsamplestorageversioncode=" + objStorageInstrument.getNsamplestorageversioncode() + ""
				+ " and  sv.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and sv.nsitecode=" + userInfo.getNmastersitecode() + " and sl.nsitecode="
				+ userInfo.getNmastersitecode() + "";
		final SampleStorageStructure objSampleStorageStructure = (SampleStorageStructure) jdbcUtilityFunction
				.queryForObject(query, SampleStorageStructure.class, jdbcTemplate);

		createPartition(objStorageInstrument.getSinstrumentcode());

		if (objSampleStorageStructure.getNneedautomapping() == Enumeration.TransactionStatus.YES
				.gettransactionstatus()) {
			final String query1 = "select nsamplestoragecontainerpathcode from samplestoragecontainerpath  "
					+ " where nsamplestorageversioncode=" + objStorageInstrument.getNsamplestorageversioncode() + " "
					+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
					+ " and nsitecode=" + userInfo.getNmastersitecode() + ""
					+ " order by  nsamplestoragecontainerpathcode";
			final List<Integer> listContainerPathCode = jdbcTemplate.queryForList(query1, Integer.class);

			inputMap.put("nprojecttypecode", objSampleStorageStructure.getNprojecttypecode());

			final String sectSeq = "select nsequenceno from seqnobasemaster where stablename='samplestoragemapping' and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			final SeqNoBasemaster objseq = (SeqNoBasemaster) jdbcUtilityFunction.queryForObject(sectSeq,
					SeqNoBasemaster.class, jdbcTemplate);

			StringBuilder insertQueryBuilder = new StringBuilder();


			final String sStorageInsQuery = "select * from storageinstrument WHERE ninstrumentcode in ("
					+ objStorageInstrument.getSinstrumentcode() + ") " + " and nsamplestoragelocationcode="
					+ objStorageInstrument.getNsamplestoragelocationcode() + " " + " and nsamplestorageversioncode="
					+ objStorageInstrument.getNsamplestorageversioncode() + "" + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and nsitecode="
					+ userInfo.getNmastersitecode() + "";

			final List<StorageInstrument> lstStorageInstrument = jdbcTemplate.query(sStorageInsQuery,
					new StorageInstrument());

			insertQueryBuilder.append(
					"INSERT INTO samplestoragemapping (nsamplestoragemappingcode, nsamplestoragecontainerpathcode, "
							+ "ncontainertypecode, ncontainerstructurecode, nneedposition, nquantity, nnoofcontainer, "
							+ "nunitcode, ndirectionmastercode, nprojecttypecode, nproductcode, nrow, ncolumn, sboxid, "
							+ "dmodifieddate, nsitecode, nstatus, nstorageinstrumentcode,npartitioncode,"
							+ "nsamplestoragelocationcode,nsamplestorageversioncode,ninstrumentcode) VALUES ");
			
			int nrow;
			int ncolumn;

			if (objSampleStorageStructure.getNnoofcontainer() == 1) {
				nrow = 1;
				ncolumn = 1;
			} else {
				nrow = objSampleStorageStructure.getNrow();
				ncolumn = objSampleStorageStructure.getNcolumn();
			}
			//Added by ATE234 ninstrumentcode not added.
			List<String> valueList = new ArrayList<>();
			int sequenceNo = objseq.getNsequenceno();

			for (Integer containerPathCode : listContainerPathCode) {
				for (StorageInstrument objStorage : lstStorageInstrument) {
					sequenceNo++;
					final String values = "(" + sequenceNo + ", " + containerPathCode + ", "
							+ objSampleStorageStructure.getNcontainertypecode() + ", "
							+ objSampleStorageStructure.getNcontainerstructurecode() + ", "
							+ objSampleStorageStructure.getNneedposition() + ", "
							+ objSampleStorageStructure.getNquantity() + ", "
							+ objSampleStorageStructure.getNnoofcontainer() + ", "
							+ objSampleStorageStructure.getNunitcode() + ", "
							+ objSampleStorageStructure.getNdirectionmastercode() + ", "
							+ objSampleStorageStructure.getNprojecttypecode() + ", "
							+ objSampleStorageStructure.getNproductcode() + ", " + nrow + ", " + ncolumn + ", " + "'"
							+ "" + "', " + "'" + dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + ""
							+ objStorage.getNregionalsitecode() + ", "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ", "
							+ objStorage.getNstorageinstrumentcode() + ","+objStorage.getNinstrumentcode()+","
							+ objStorage.getNsamplestoragelocationcode() +","
							+ ""+objStorage.getNsamplestorageversioncode()+","+objStorage.getNinstrumentcode()+")";
					valueList.add(values);

				}
			}

			insertQueryBuilder.append(String.join(", ", valueList));
			final String updateQuery = " update seqnobasemaster set nsequenceno= (select max(nsamplestoragemappingcode) "
					+ " from samplestoragemapping) where stablename='samplestoragemapping';";

			if (!valueList.isEmpty()) {
				jdbcTemplate.execute(insertQueryBuilder.toString() + ";" + updateQuery);
			}
		}
		return getStorageInstrument(userInfo);
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
	@Override
	public ResponseEntity<Object> updateStorageInstrument(final Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		final StorageInstrument objStorageInstrument = objMapper.convertValue(inputMap.get("storageinstrument"),
				new TypeReference<StorageInstrument>() {
				});

		final StorageInstrument objActiveStorageInstrument = getActiveStorageInstrument(
				objStorageInstrument.getNstorageinstrumentcode(), userInfo);
		if (objActiveStorageInstrument == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {

			final String updateQuery = " update storageinstrument set nstorageconditioncode="
					+ objStorageInstrument.getNstorageconditioncode() + ",dmodifieddate='"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' " + " where nstorageinstrumentcode="
					+ objStorageInstrument.getNstorageinstrumentcode() + "" + " and nsitecode="
					+ userInfo.getNmastersitecode() + " " + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";

			jdbcTemplate.execute(updateQuery);

		}

		final List<String> multilingualIDList = new ArrayList<>();

		final List<Object> listAfterUpdate = new ArrayList<>();
		final List<Object> listBeforeUpdate = new ArrayList<>();

		objActiveStorageInstrument.setSinstrumentid(objStorageInstrument.getSinstrumentid());
		listAfterUpdate.add(objStorageInstrument);
		listBeforeUpdate.add(objActiveStorageInstrument);

		multilingualIDList.add("IDS_EDITSTORAGEINSTRUMENT");

		auditUtilityFunction.fnInsertAuditAction(listAfterUpdate, 2, listBeforeUpdate, multilingualIDList, userInfo);

		return getStorageInstrument(userInfo);
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
	 * @return ResponseEntity with string message as 'Already deleted' if the unit
	 *         record is not available/ string message as 'Record is used in....'
	 *         when the unit is associated in transaction / list of all units
	 *         excluding the deleted record
	 * @throws Exception exception
	 */
	@Override
	public ResponseEntity<Object> deleteStorageInstrument(final Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());

		final StorageInstrument objStorageInstrument = objMapper.convertValue(inputMap.get("storageinstrument"),
				new TypeReference<StorageInstrument>() {
				});

		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});

		final StorageInstrument StorageInstrument = getActiveStorageInstrument(
				objStorageInstrument.getNstorageinstrumentcode(), userInfo);
		if (StorageInstrument == null) {
			// status code:417
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {

			final String str = "select ssettingvalue from settings where nsettingcode=40 and nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			final Settings objSettings = (Settings) jdbcUtilityFunction.queryForObject(str, Settings.class,
					jdbcTemplate);

			String query = "";
			if (Integer.parseInt(objSettings.getSsettingvalue()) == Enumeration.TransactionStatus.NO
					.gettransactionstatus()) {
				query = "select 'IDS_SAMPLESTORAGE' as Msg from samplestoragelocation sl, samplestorageversion sv, "
						+ " samplestoragecontainerpath ssc, samplestoragetransaction sst, samplestoragemapping ssm "
						+ " where sl.nsamplestoragelocationcode=sv.nsamplestoragelocationcode "
						+ " and sl.nsamplestoragelocationcode = ssc.nsamplestoragelocationcode and sv.napprovalstatus= "
						+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
						+ " and ssc.nsamplestoragecontainerpathcode=ssm.nsamplestoragecontainerpathcode "
						+ " and sst.nsamplestoragemappingcode=ssm.nsamplestoragemappingcode "
						+ " and sl.nsamplestoragelocationcode=" + objStorageInstrument.getNsamplestoragelocationcode()
						+ " and ssm.nstorageinstrumentcode=" + objStorageInstrument.getNstorageinstrumentcode()
						+ " and sl.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and ssm.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and ssc.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and sst.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and sv.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and sl.nsitecode=" + userInfo.getNmastersitecode() + ""
						+ " and ssc.nsitecode=" + userInfo.getNmastersitecode() + ""
						+ " and sv.nsitecode=" + userInfo.getNmastersitecode() + ""
						+ " and ssm.nsitecode=" + userInfo.getNtranssitecode()+ ""
						+ " and sst.nsitecode=" + userInfo.getNtranssitecode()+ "";
			} else {
				query = "select 'IDS_SAMPLESTORAGEMAPPING' as Msg from samplestoragemapping ssm,"
						+ " samplestoragelocation ssl,samplestoragecontainerpath ssc,storageinstrument si where "
						+ " ssc.nsamplestoragecontainerpathcode=ssm.nsamplestoragecontainerpathcode "
						+ "  and ssl.nsamplestoragelocationcode ="
						+ objStorageInstrument.getNsamplestoragelocationcode() + ""
						+ " and si.nstorageinstrumentcode=ssm.nstorageinstrumentcode "
						+ " and ssl.nsamplestoragelocationcode=ssc.nsamplestoragelocationcode "
						+ " and si.nmappingtranscode=" + Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
						+ " and ssm.nstorageinstrumentcode= " + objStorageInstrument.getNstorageinstrumentcode()
						+ " and ssl.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and ssm.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and ssc.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and si.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and ssl.nsitecode=" + userInfo.getNmastersitecode() + ""
						+ " and ssc.nsitecode=" + userInfo.getNmastersitecode() + ""
						//+ " and sv.nsitecode=" + userInfo.getNmastersitecode() + ""
						+ " and si.nsitecode=" + userInfo.getNtranssitecode()+ ""
						+ " and ssm.nsitecode=" + userInfo.getNtranssitecode()+ "";

			}

			validatorDel = projectDAOSupport.getTransactionInfo(query, userInfo);

			boolean validRecord = false;
			if (validatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
				validRecord = true;
				validatorDel = projectDAOSupport.validateDeleteRecord(
						Integer.toString(objStorageInstrument.getNstorageinstrumentcode()), userInfo);
				if (validatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
					validRecord = true;
				} else {
					validRecord = false;
				}
			}

			if (validRecord) {

				String updateQuery = " update storageinstrument set nstatus="
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + "" + " ,dmodifieddate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' " + " where nstorageinstrumentcode="
						+ objStorageInstrument.getNstorageinstrumentcode() + ";";

				updateQuery = updateQuery + " delete from samplestoragemapping where nstorageinstrumentcode="
						+ objStorageInstrument.getNstorageinstrumentcode() + "";

				jdbcTemplate.execute(updateQuery);

			}

			else {
				// status code:417
				return new ResponseEntity<>(validatorDel.getSreturnmessage(), HttpStatus.EXPECTATION_FAILED);
			}
			final List<String> multilingualIDList = new ArrayList<>();

			final List<Object> deletedUnitList = new ArrayList<>();
			objStorageInstrument.setNstatus((short) Enumeration.TransactionStatus.DELETED.gettransactionstatus());
			deletedUnitList.add(objStorageInstrument);

			multilingualIDList.add("IDS_DELETESTORAGEINSTRUMENT");
			auditUtilityFunction.fnInsertAuditAction(deletedUnitList, 1, null, multilingualIDList, userInfo);

			return getStorageInstrument(userInfo);
		}
	}

	/**
	 * This method is used to retrieve a specific storageinstrument record.
	 * 
	 * @param inputMap [Map] map object with "nstorageinstrumentcode" and "userinfo"
	 *                 as keys for which the data is to be fetched Input:{
	 *                 "nstorageinstrumentcode": 1, "userinfo":{
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
	 * @return ResponseEntity with Storage Instrument object for the specified
	 *         primary key / with string message as 'Deleted' if the
	 *         storageinstrument record is not available
	 * @throws Exception exception
	 */
	@Override
	public StorageInstrument getActiveStorageInstrument(final int nstorageinstrumentcode, final UserInfo userInfo)
			throws Exception {

		final String query = "select si.nstorageinstrumentcode,si.nstoragecategorycode,sc.sstoragecategoryname,si.nsamplestoragelocationcode,"
				+ " sl.ssamplestoragelocationname,si.nsamplestorageversioncode,ic.sinstrumentcatname,si.ninstrumentcatcode,"
				+ " si.ninstrumentcode,i.sinstrumentid,scon.nstorageconditioncode,scon.sstorageconditionname ,"
				+ " coalesce(it.jsondata->'sinstrumenttype'->>'"+userInfo.getSlanguagetypecode()+"'"
				+ "	 ,it.jsondata->'sinstrumenttype'->>'en-US') as sinstrumenttype,si.ninstrumenttypecode "
				+ " from storagecategory sc,storageinstrument si,samplestoragelocation sl,"
				+ " samplestorageversion sv,instrumentcategory ic,instrument i,storagecondition scon ,instrumenttype it where  si.nstoragecategorycode=sc.nstoragecategorycode"
				+ " and si.nsamplestoragelocationcode=sl.nsamplestoragelocationcode and i.ninstrumentcatcode=ic.ninstrumentcatcode"
				+ " and si.ninstrumentcatcode=ic.ninstrumentcatcode "
				+ " and si.nstorageconditioncode=scon.nstorageconditioncode "
				+ " and si.ninstrumentcode=i.ninstrumentcode and si.nsamplestorageversioncode=sv.nsamplestorageversioncode "
				+ " and sc.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
				+ " and si.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
				+ " and sl.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
				+ " and sv.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
				+ " and ic.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
				+ " and i.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
				+ " and scon.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
				+ " and sc.nsitecode=" + userInfo.getNmastersitecode() + "" + " and si.nsitecode="
				+ userInfo.getNmastersitecode() + "" + " and sl.nsitecode=" + userInfo.getNmastersitecode() + ""
				+ " and sv.nsitecode=" + userInfo.getNmastersitecode() + "" + " and ic.nsitecode="
				+ userInfo.getNmastersitecode() + "" + " and i.nsitecode=" + userInfo.getNmastersitecode() + ""
				+ " and si.ninstrumenttypecode=it.ninstrumenttypecode and scon.nsitecode=" + userInfo.getNmastersitecode() + "" + " and si.nstorageinstrumentcode="
				+ nstorageinstrumentcode + "";

		return (StorageInstrument) jdbcUtilityFunction.queryForObject(query, StorageInstrument.class, jdbcTemplate);

	}

	/**
	 * This method is used to retrieve a specific storage structure record based on
	 * the storage category.
	 * 
	 * @param inputMap [Map] map object with "nstoragecategorycode" and "userinfo"
	 *                 as keys for which the data is to be fetched Input:{
	 *                 "nstoragecategorycode": 1, "userinfo":{ "activelanguagelist":
	 *                 ["en-US"], "isutcenabled": 4,"ndeptcode":
	 *                 -1,"ndeputyusercode": -1,"ndeputyuserrole": -1, "nformcode":
	 *                 33,"nmastersitecode": -1,"nmodulecode": 1, "nreasoncode":
	 *                 0,"nsitecode": 1,"ntimezonecode": -1,"ntranssitecode":
	 *                 1,"nusercode": -1, "nuserrole": -1,"nusersitecode":
	 *                 -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss", "sgmtoffset":
	 *                 "UTC +00:00","slanguagefilename":
	 *                 "Msg_en_US","slanguagename": "English", "slanguagetypecode":
	 *                 "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss","sreason":
	 *                 "","ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy
	 *                 HH:mm:ss"}}
	 * @return ResponseEntity will return a list of StorageStructure objects for the
	 *         specified nstoragecategorycode.
	 * @throws Exception exception
	 */
	@Override
	public ResponseEntity<Object> getStorageStructure(final Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});

		int nstoragecategorycode = -1;

		if (inputMap.containsKey("nstoragecategorycode")) {
			nstoragecategorycode = (int) inputMap.get("nstoragecategorycode");
		}

		final String query = "select sl.nsamplestoragelocationcode,sv.nsamplestorageversioncode,sl.ssamplestoragelocationname,sv.nsamplestorageversioncode"
				+ " from samplestoragelocation sl,samplestorageversion sv,storagecategory sc"
				+ " where sl.nsamplestoragelocationcode=sv.nsamplestoragelocationcode "
				+ " and sl.nstoragecategorycode=" + nstoragecategorycode + ""
				+ " and sc.nstoragecategorycode=sl.nstoragecategorycode" + " and sv.napprovalstatus="
				+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + "" + " and sv.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and sv.nsitecode="
				+ userInfo.getNmastersitecode() + "" + " and sl.nsitecode=" + userInfo.getNmastersitecode() + "";

		final List<SampleStorageStructure> lstStorageInstrument = jdbcTemplate.query(query,
				new SampleStorageStructure());

		return new ResponseEntity<>(lstStorageInstrument, HttpStatus.OK);
	}

	/**
	 * This method is used to retrieve the specific Instrument Category associated
	 * with the given Storage Instrument..
	 * 
	 * @param inputMap [Map] map object with "userinfo" as keys for which the data
	 *                 is to be fetched Input:{ "userinfo":{ "activelanguagelist":
	 *                 ["en-US"], "isutcenabled": 4,"ndeptcode":
	 *                 -1,"ndeputyusercode": -1,"ndeputyuserrole": -1, "nformcode":
	 *                 33,"nmastersitecode": -1,"nmodulecode": 1, "nreasoncode":
	 *                 0,"nsitecode": 1,"ntimezonecode": -1,"ntranssitecode":
	 *                 1,"nusercode": -1, "nuserrole": -1,"nusersitecode":
	 *                 -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss", "sgmtoffset":
	 *                 "UTC +00:00","slanguagefilename":
	 *                 "Msg_en_US","slanguagename": "English", "slanguagetypecode":
	 *                 "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss","sreason":
	 *                 "","ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy
	 *                 HH:mm:ss"}}
	 * @return ResponseEntity will return a list of Instrument Category objects for
	 *         the specified storage instrument.
	 * @throws Exception exception
	 */
	@Override
	public ResponseEntity<Object> getStorageInstrumentCategory(final Map<String, Object> inputMap) throws Exception {
		Map<String, Object> responseMap = new HashMap<>();
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		
		int ninstrumenttypecode = -1;


		if (inputMap.containsKey("ninstrumenttypecode")) {
			ninstrumenttypecode = (int) inputMap.get("ninstrumenttypecode");
		}

//		final String query = "select ic.ninstrumentcatcode,ic.sinstrumentcatname,ic.ninstrumenttypecode,ic.ndefaultstatus from instrumentcategory ic,"
//				+ " instrumenttype it where it.ninstrumenttypecode=ic.ninstrumenttypecode"
//				+ " and ic.ninstrumenttypecode="
//				+ Enumeration.InstrumentType.STORAGE_INSTRUMENT.getNinstrumenttypecode() + "" + "	and ic.nstatus ="
//				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + "	and it.nstatus ="
//				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + "	and ic.nsitecode ="
//				+ userInfo.getNmastersitecode() + "";
		final String query = "select ic.ninstrumentcatcode,ic.sinstrumentcatname,ic.ninstrumenttypecode,"
				+ "ic.ndefaultstatus from instrumentcategory ic where "
				+ "  ic.ninstrumenttypecode="+ ninstrumenttypecode + "" + "	and ic.nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()  + "	and ic.nsitecode ="
				+ userInfo.getNmastersitecode() + "";

		final List<InstrumentCategory> lstInstrumentCategory = jdbcTemplate.query(query, new InstrumentCategory());

		final int instrumentcatcode = lstInstrumentCategory.stream().filter(
				category -> category.getNdefaultstatus() == Enumeration.TransactionStatus.YES.gettransactionstatus())
				.map(InstrumentCategory::getNinstrumentcatcode).findFirst()
				.orElse(Enumeration.TransactionStatus.NA.gettransactionstatus());

		responseMap.put("InstrumentCategory", lstInstrumentCategory);

		if (!lstInstrumentCategory.isEmpty()) {
				if(instrumentcatcode != Enumeration.TransactionStatus.NA.gettransactionstatus()) {

					inputMap.put("ninstrumentcatcode",instrumentcatcode);
				}

				else
				{
					inputMap.put("ninstrumentcatcode",lstInstrumentCategory.get(lstInstrumentCategory.size() - 1).getNinstrumentcatcode());
				}

			responseMap.putAll(getInstrumentByCategory(inputMap).getBody());

		}

		return new ResponseEntity<>(responseMap, HttpStatus.OK);
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
	public ResponseEntity<Map<String, Object>> getInstrumentByCategory(final Map<String, Object> inputMap)
			throws Exception {
		Map<String, Object> responseMap = new HashMap<>();
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});

		int ninstrumentcatcode = -1;


		if (inputMap.containsKey("ninstrumentcatcode")) {
			ninstrumentcatcode = (int) inputMap.get("ninstrumentcatcode");
		}

		final String query = "SELECT i.sinstrumentid, i.ninstrumentcode, ic.ninstrumenttypecode "
				+ " FROM instrumentcategory ic, instrument i " + " WHERE ic.ninstrumentcatcode = i.ninstrumentcatcode "
				+ " AND i.ninstrumentcatcode = " + ninstrumentcatcode + " " + " AND i.ninstrumentstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " AND ic.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " AND i.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " AND i.nsitecode = "
				+ userInfo.getNmastersitecode() + " " + " AND ic.nsitecode = " + userInfo.getNmastersitecode() + " "
				+ " AND NOT EXISTS ( " + " SELECT 1 FROM storageinstrument si "
				+ " WHERE  si.ninstrumentcode = i.ninstrumentcode " + " AND si.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " AND si.nsitecode = "
				+ userInfo.getNmastersitecode() + " )";

		final List<Instrument> lstInstrument = jdbcTemplate.query(query, new Instrument());

		responseMap.put("Instrument", lstInstrument);

		return new ResponseEntity<Map<String, Object>>(responseMap, HttpStatus.OK);
	}

	/**
	 * This method is used to create Partition SampleStorageMapping table based on
	 * nprojecttypecode.
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched
	 */
	
	//ALPDJ21-71--Added by Vignesh(06-09-2025)--new fields added the for the partition of sample storage mapping
	public void createPartition(final String sinstrumentcode) {
		
		String partitionMapping = "";
		for (String inscode : sinstrumentcode.split(",")) {
 
			partitionMapping = partitionMapping + " CREATE TABLE IF NOT EXISTS samplestoragemapping_"+inscode+""
					+ " PARTITION OF samplestoragemapping FOR VALUES IN ("+inscode+");";
	}
		jdbcTemplate.execute(partitionMapping);
	}
	
	@Override
	public ResponseEntity<Object> getStorageInstrumentType(final Map<String, Object> inputMap) throws Exception {
		Map<String, Object> responseMap = new HashMap<>();
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});

		final String query = "select *,coalesce(jsondata->'sinstrumenttype'->>'"+userInfo.getSlanguagetypecode()+"',"
				+ "	 jsondata->'sinstrumenttype'->>'en-US') as sinstrumenttype  from instrumenttype  where "
				+ "  ninstrumenttypecode in ("+ Enumeration.InstrumentType.STORAGE_INSTRUMENT.getNinstrumenttypecode() +","
				+ " "+Enumeration.InstrumentType.TEMPORARY_STORAGE_INSTRUMENT.getNinstrumenttypecode()+") " 
				+ "	and nstatus ="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "	and nsitecode ="
				+ userInfo.getNmastersitecode() + " order by ninstrumenttypecode desc";

		final List<InstrumentType> lstInstrumentType = jdbcTemplate.query(query, new InstrumentType());

		if(!lstInstrumentType.isEmpty()) {
			responseMap.put("InstrumentType", lstInstrumentType);
			responseMap.put("selectedInstrumentType", Arrays.asList(lstInstrumentType.get(lstInstrumentType.size() - 1)));
			final String queryInstrumentCategory = "select ic.ninstrumentcatcode,ic.sinstrumentcatname,ic.ninstrumenttypecode,"
					+ "ic.ndefaultstatus from instrumentcategory ic where "
					+ "  ic.ninstrumenttypecode="
					+ lstInstrumentType.get(lstInstrumentType.size() - 1).getNinstrumenttypecode() + "" + "	and ic.nstatus ="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()  + "	and ic.nsitecode ="
					+ userInfo.getNmastersitecode() + "";

		final List<InstrumentCategory> lstInstrumentCategory = jdbcTemplate.query(queryInstrumentCategory, new InstrumentCategory());
		responseMap.put("InstrumentCategory", lstInstrumentCategory);
		
		final int instrumentcatcode = lstInstrumentCategory.stream().filter(
				category -> category.getNdefaultstatus() == Enumeration.TransactionStatus.YES.gettransactionstatus())
				.map(InstrumentCategory::getNinstrumentcatcode).findFirst()
				.orElse(Enumeration.TransactionStatus.NA.gettransactionstatus());

		if (!lstInstrumentCategory.isEmpty()) {
				if(instrumentcatcode != Enumeration.TransactionStatus.NA.gettransactionstatus()) {

					inputMap.put("ninstrumentcatcode",instrumentcatcode);
				}
				else
				{
					inputMap.put("ninstrumentcatcode",lstInstrumentCategory.get(lstInstrumentCategory.size() - 1).getNinstrumentcatcode());
				}
			responseMap.putAll(getInstrumentByCategory(inputMap).getBody());

		}
		}
		return new ResponseEntity<>(responseMap, HttpStatus.OK);
	}

}