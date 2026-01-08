package com.agaramtech.qualis.global;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import com.agaramtech.qualis.basemaster.model.ContainerStorageCondition;
import com.agaramtech.qualis.storagemanagement.model.SampleStorageStructure;
import com.agaramtech.qualis.storagemanagement.model.SampleStorageVersion;
import com.agaramtech.qualis.storagemanagement.model.StorageCategory;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@Repository
public class SampleStorageCommons {

	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;

	public ResponseEntity<List<StorageCategory>> getStorageCategory(final UserInfo userInfo) throws Exception {
		final String strQuery = " select nstoragecategorycode,sstoragecategoryname,sdescription,nsitecode,nstatus  from storagecategory "
				+ " where nstatus =" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and nstoragecategorycode>0  and nsitecode=" + userInfo.getNmastersitecode();
		return new ResponseEntity<List<StorageCategory>>(jdbcTemplate.query(strQuery, new StorageCategory()),
				HttpStatus.OK);
	}

	public StorageCategory getActiveStorageCategoryById(final int nstorageCategoryCode, final UserInfo userInfo)
			throws Exception {
		final String strQuery = " select nstoragecategorycode,sstoragecategoryname,sdescription,nsitecode,nstatus "
				+ " from storagecategory  where nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nstoragecategorycode = "
				+ nstorageCategoryCode;
		return (StorageCategory) jdbcUtilityFunction.queryForObject(strQuery, StorageCategory.class, jdbcTemplate);
	}

	public ResponseEntity<Map<String, Object>> getContainerStorageCondition(final String scontainercode,
			final UserInfo userInfo) throws Exception {
		final Map<String, Object> outputMap = new HashMap<>();
		final String strQuery = "select a.sstorageconditionname, a.nstorageconditioncode,b.ncontainerstoragecode, b.scontainercode from storagecondition a, containerstoragecondition b where a.nstorageconditioncode = b.nstorageconditioncode and b.scontainercode = '"
				+ scontainercode + "' and a.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and b.nstatus  = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		final ContainerStorageCondition obj = (ContainerStorageCondition) jdbcUtilityFunction.queryForObject(strQuery,
				ContainerStorageCondition.class, jdbcTemplate);
		outputMap.put("storageContainer", obj);
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

//ALPDJ21-33--Added by Vignesh R(01-08-2025)-->Storage Instrument - Screen Development
	public ResponseEntity<? extends Object> getSelectedSampleStorageStructure(final int nstorageinstrumentcode,final int nsampleStorageLocationCode,
			final int nsamplestorageversioncode, final UserInfo userInfo) throws Exception {
		
			SampleStorageStructure deleteSampleStorageLocation = getActiveLocation((short) nsampleStorageLocationCode);

			int nNeedInstrumentFlow = -1;
			
				final String sSetting = "select ssettingvalue from settings where nsettingcode ="
						+ Enumeration.Settings.INSTRUMENT_BASED_STORAGE.getNsettingcode() + " and nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
			

				final String sNeedInstrumentFlow = (String) jdbcUtilityFunction.queryForObject(sSetting, String.class,
						jdbcTemplate);
				nNeedInstrumentFlow = Integer.parseInt(sNeedInstrumentFlow);

		if (deleteSampleStorageLocation == null) {

			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);

		}

		String sQuery = "";
		
		final Map<String, Object> outputMap = new HashMap<>();
		
		
		 sQuery = "SELECT "
			    + "si.nmappingtranscode, ic.sinstrumentcatname, si.ninstrumentcode, si.nregionalsitecode,i.sinstrumentid, si.nstorageinstrumentcode, "
			    + "ts1.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode() + "' AS sneedautomapping, "
			    + "sc.sstoragecategoryname, p.sproductname, pt.sprojecttypename, "
			    + "d.jsondata->'sdirection'->>'" + userInfo.getSlanguagetypecode() + "' AS sdirection, "
			    + "ssl.nrow, ssl.ncolumn, ct.scontainertype, cs.scontainerstructurename, "
			    + "ssl.nsamplestoragelocationcode, ssl.nstoragecategorycode, ssl.ncontainertypecode, ssl.ncontainerstructurecode, "
			    + "ssl.nneedposition, ssl.nneedautomapping, ssl.nquantity, ssl.ndirectionmastercode, "
			    + "ssl.nprojecttypecode, ssl.nproductcode, ssl.nrow, ssl.ncolumn, CASE  "
			    + "	 WHEN si.ninstrumentcode != "+Enumeration.TransactionStatus.NA.gettransactionstatus()+" THEN CONCAT(i.sinstrumentid, ' | ', ssl.ssamplestoragelocationname) "
			    + "	 ELSE ssl.ssamplestoragelocationname END AS slocationinstrumentinfo, "
			    + "ts.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode() + "' AS stransdisplaystatus, "
			    + "u.sunitname,ssl.nsitecode "
			    + "FROM storagecategory sc, samplestoragelocation ssl, transactionstatus ts, transactionstatus ts1, "
			    + "product p, containertype ct, containerstructure cs, projecttype pt, directionmaster d, unit u, "
			    + "storageinstrument si, instrumentcategory ic, instrument i "
			    + "WHERE ssl.nsamplestoragelocationcode = " + nsampleStorageLocationCode + " "
			    + "AND ts1.ntranscode = ssl.nneedautomapping "
			    + "AND p.nproductcode = ssl.nproductcode "
			    + "AND ssl.nstoragecategorycode = sc.nstoragecategorycode "
			    + "AND pt.nprojecttypecode = ssl.nprojecttypecode "
			    + "AND cs.ncontainerstructurecode = ssl.ncontainerstructurecode "
			    + "AND ct.ncontainertypecode = ssl.ncontainertypecode "
			    + "AND d.ndirectionmastercode = ssl.ndirectionmastercode "
			    + "AND u.nunitcode = ssl.nunitcode "
			    + "AND i.ninstrumentcode = si.ninstrumentcode "
			    + "AND ic.ninstrumentcatcode = i.ninstrumentcatcode "
			    + "AND ic.ninstrumentcatcode = si.ninstrumentcatcode "
			    + "AND si.nstoragecategorycode = sc.nstoragecategorycode "
			    + "AND si.nsamplestoragelocationcode = ssl.nsamplestoragelocationcode "
			    + "AND si.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
			    + "AND ic.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
			    + "AND i.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
			    + "AND i.nsitecode = " + userInfo.getNmastersitecode() + " "
			    + "AND si.nsitecode = " + userInfo.getNmastersitecode() + " "
			    + "AND ic.nsitecode = " + userInfo.getNmastersitecode() + " "
			    + "AND ts.ntranscode = si.nmappingtranscode "
			    + "AND si.nstorageinstrumentcode = " + nstorageinstrumentcode + " "
			    + "AND u.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
			    + "AND ssl.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
			    + "AND p.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
			    + "AND pt.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
			    + "AND d.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
			    + "AND ct.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
			    + "AND cs.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
			    + "AND sc.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
			    + "ORDER BY si.nstorageinstrumentcode";

	
		List<Map<String, Object>> list = jdbcTemplate.queryForList(sQuery);
		if (list.size() > 0) {
			outputMap.put("selectedSampleStorageLocation", list.get(0));
			outputMap.putAll((Map<String, Object>) getAllSampleStorageVersion(nsampleStorageLocationCode,
					nsamplestorageversioncode, userInfo).getBody());
		} else {
			outputMap.put("selectedSampleStorageLocation", null);
		}
		outputMap.put("nNeedInstrumentFlow", nNeedInstrumentFlow);
		//outputMap.put("nstorageinstrumentcode", (int)paramMap.get("nstorageinstrumentcode"));

		return new ResponseEntity<>(outputMap, HttpStatus.OK);

	}
	
	

	//ALPDJ21-33--Added by Vignesh R(01-08-2025)-->Storage Instrument - Screen Development
	public ResponseEntity<? extends Object> getSelectedSampleStorageLocation(final int nsampleStorageLocationCode,
			final int nsamplestorageversioncode, final UserInfo userInfo) throws Exception {
		SampleStorageStructure deleteSampleStorageLocation = getActiveLocation((short) nsampleStorageLocationCode);


		if (deleteSampleStorageLocation == null) {

			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);

		}

		String sQuery = "";
		final Map<String, Object> outputMap = new HashMap<>();
		//if (nNeedStorageFlow ==  Enumeration.TransactionStatus.NO.gettransactionstatus()) {
		
		    //ALPDJ21-96 L.Subashini Added sitename in query
			sQuery = "select ts1.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode()
					+ "' as sneedautomapping,sc.sstoragecategoryname,p.sproductname,pt.sprojecttypename,"
					+ " d.jsondata->'sdirection'->>'"+ userInfo.getSlanguagetypecode()
					+ "' as sdirection,ssl.nrow,ssl.ncolumn, ct.scontainertype,"
					+ " cs.scontainerstructurename,ssl.*,ts.jsondata->'stransdisplaystatus'->>'"
					+ userInfo.getSlanguagetypecode() + "' as stransdisplaystatus,u.sunitname, s.ssitename "
					+ " from storagecategory sc,samplestoragelocation ssl, transactionstatus ts,"
					+ " transactionstatus ts1, product p,containertype ct,containerstructure cs, projecttype pt,"
					+ " directionmaster d, unit u , site s "
					+ " where ssl.nsamplestoragelocationcode = " + nsampleStorageLocationCode 
					+ " and ts.ntranscode=ssl.nmappingtranscode "
					+ " and ts1.ntranscode=ssl.nneedautomapping and p.nproductcode=ssl.nproductcode "
					+ " and ssl.nstoragecategorycode=sc.nstoragecategorycode "
					+ " and pt.nprojecttypecode=ssl.nprojecttypecode "
					+ " and cs.ncontainerstructurecode=ssl.ncontainerstructurecode "
					+ " and ct.ncontainertypecode=ssl.ncontainertypecode "
					+ " and d.ndirectionmastercode=ssl.ndirectionmastercode " 
					+ " and u.nunitcode=ssl.nunitcode "
					+ " and ssl.nsitecode = s.nsitecode "
					+ " and s.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
					+ " and u.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and ssl.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and p.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and pt.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and d.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and ct.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and cs.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and sc.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		//} 
		
		List<Map<String, Object>> list = jdbcTemplate.queryForList(sQuery);
		if (list.size() > 0) {
			outputMap.put("selectedSampleStorageLocation", list.get(0));
			outputMap.putAll((Map<String, Object>) getAllSampleStorageVersion(nsampleStorageLocationCode,
					nsamplestorageversioncode, userInfo).getBody());
		} else {
			outputMap.put("selectedSampleStorageLocation", null);
		}
		return new ResponseEntity<>(outputMap, HttpStatus.OK);

	}

	public ResponseEntity<? extends Object> getActiveSampleStorageVersion(final int nsampleStorageVersionCode,
			final UserInfo userInfo) throws Exception {
		final SampleStorageVersion deletedSampleStorageVersion = getActiveVersion((short) nsampleStorageVersionCode);
		if (deletedSampleStorageVersion == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}
		final Map<String, Object> outputMap = new HashMap<>();
		// ATE234 Janakumar BGSI-10->Storage Structure ? Instrument id & Site combo load based on Setting table.
		//ALPDJ21-96 L.Subashini Added sitename in query
		final String sQuery = " select (a.jsondata->'additionalinfo'->>'nprojecttypecode')::integer as nprojecttypecode,(a.jsondata->'additionalinfo'->>'nunitcode')::integer as nunitcode,a.nsamplestoragelocationcode,a.nsamplestorageversioncode,"
				+ " (a.jsondata->'additionalinfo'->>'ncolumn')::integer as ncolumn,(a.jsondata->'additionalinfo'->>'nrow')::integer as nrow, case when a.nversionno = 0 then '-' else cast(a.nversionno as character varying(10)) end nversionno,"
				+ " (a.jsondata->'additionalinfo'->>'nquantity') as nquantity,(a.jsondata->'additionalinfo'->>'nproductcode')::integer as nproductcode,a.napprovalstatus,a.jsondata, a.nstatus,b.ssamplestoragelocationname,b.nstoragecategorycode, "
				+ " (a.jsondata->'additionalinfo'->>'nneedposition')::integer as nneedposition,(a.jsondata->'additionalinfo'->>'nnoofcontainer')::integer as nnoofcontainer,(a.jsondata->'additionalinfo'->>'nneedautomapping')::integer as nneedautomapping,(a.jsondata->'additionalinfo'->>'ncontainertypecode')::integer as ncontainertypecode, "
				+ " (a.jsondata->'additionalinfo'->>'ncontainertypecode')::integer as ncontainertypecode,(a.jsondata->'additionalinfo'->>'ndirectionmastercode')::integer as ndirectionmastercode, "
				+ " (a.jsondata->'additionalinfo'->>'nstoragecategorycode')::integer as nstoragecategorycode,(a.jsondata->'additionalinfo'->>'ncontainerstructurecode')::integer as ncontainerstructurecode,"
				+ "  (a.jsondata->'additionalinfo'->>'ssamplestoragelocationname') as ssamplestoragelocationname,"
				+ "  (a.jsondata->'additionalinfo'->>'sproductname') as sproductname, "
				+ "  (a.jsondata->'additionalinfo'->>'sdirection') as sdirection, "
				+ "  (a.jsondata->'additionalinfo'->>'scontainertype') as scontainertype, "
				+ "  (a.jsondata->'additionalinfo'->>'sprojecttypename') as sprojecttypename, "
				+ "  (a.jsondata->'additionalinfo'->>'scontainerstructurename') as scontainerstructurename, "
				+ "  (a.jsondata->'additionalinfo'->>'sstoragecategoryname') as sstoragecategoryname, "
				+ "  (a.jsondata->'additionalinfo'->>'sunitname') as sunitname,"
				+ " ts.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode() + "' as sneedautomapping,"
				+ " a.napprovalstatus,b.nsitecode, s.ssitename "
				+ " from samplestorageversion a left join transactionstatus ts on   ts.ntranscode=(a.jsondata->'additionalinfo'->>'nneedautomapping')::integer and ts.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ "  ,samplestoragelocation b, containertype ct,containerstructure cs, site s"
				+ "  where a.nsamplestoragelocationcode = b.nsamplestoragelocationcode"
				+ " and a.nsamplestorageversioncode = " + nsampleStorageVersionCode
				+ " and b.ncontainertypecode=ct.ncontainertypecode   "
				+ " and b.nsitecode = s.nsitecode "
				+ " and b.ncontainerstructurecode=cs.ncontainerstructurecode " + " and a.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and b.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and cs.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
				+ " and ct.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and s.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		final List<Map<String, Object>> list = jdbcTemplate.queryForList(sQuery);
		if (list.size() > 0) {
			outputMap.put("selectedSampleStorageVersion", list.get(0));
		} else {
			outputMap.put("selectedSampleStorageVersion", null);
		}
		outputMap.put("storageContainer", null);
		outputMap.put("containers", null);
		outputMap.put("sampleStorageMaster", null);
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	public ResponseEntity<Map<String, Object>> getAllSampleStorageVersion(final int nsampleStorageLocationCode,
			final int nsamplestorageversioncode, final UserInfo userInfo) throws Exception {
		final Map<String, Object> outputMap = new HashMap<>();
		final String sQuery = "select nsamplestoragelocationcode, nsamplestorageversioncode, case when nversionno = 0 "
				+ "then '-' else cast(nversionno as character varying(10)) end nversionno, napprovalstatus, jsondata, nstatus "
				+ "from samplestorageversion where nsamplestoragelocationcode = " + nsampleStorageLocationCode
				+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " order by nsamplestorageversioncode asc";

		List<Map<String, Object>> list = jdbcTemplate.queryForList(sQuery);
		if (list.size() > 0) {
			outputMap.put("sampleStorageVersion", list);
			if (nsamplestorageversioncode == 0) {
				outputMap.putAll((Map<String, Object>) getActiveSampleStorageVersion(
						(int) list.get(list.size() - 1).get("nsamplestorageversioncode"), userInfo).getBody());
			} else {
				outputMap
						.putAll((Map<String, Object>) getActiveSampleStorageVersion(nsamplestorageversioncode, userInfo)
								.getBody());
			}

		} else {
			outputMap.put("sampleStorageVersion", null);
			outputMap.put("selectedSampleStorageVersion", null);
		}
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	public SampleStorageStructure getActiveLocation(final short sampleStorageLocationCode) throws Exception {
		
		
		
		final String insertQueryString = "select *  from samplestoragelocation where nsamplestoragelocationcode = "
				+ sampleStorageLocationCode + " and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
		final SampleStorageStructure sampleStorageLocation = (SampleStorageStructure) jdbcUtilityFunction
				.queryForObject(insertQueryString, SampleStorageStructure.class, jdbcTemplate);
		return sampleStorageLocation;
	}

	public SampleStorageVersion getActiveVersion(final short nsamplestorageversioncode) throws Exception {
		final String insertQueryString = "select *  from samplestorageversion where nsamplestorageversioncode = "
				+ nsamplestorageversioncode + " and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
		final SampleStorageVersion sampleStorageVersion = (SampleStorageVersion) jdbcUtilityFunction
				.queryForObject(insertQueryString, SampleStorageVersion.class, jdbcTemplate);
		return sampleStorageVersion;
	}

}
