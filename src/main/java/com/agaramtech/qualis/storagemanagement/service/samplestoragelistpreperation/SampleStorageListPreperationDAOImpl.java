package com.agaramtech.qualis.storagemanagement.service.samplestoragelistpreperation;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.StringJoiner;

import org.apache.commons.text.StringEscapeUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.agaramtech.qualis.configuration.model.Settings;
import com.agaramtech.qualis.credential.model.Site;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.UserInfo;

import lombok.AllArgsConstructor;

/**
 * This class is used to perform only read operation on "samplestoragelocation"
 * table by implementing methods from its interface.
 *
 * @author ATE236
 * @version 10.0.0.2
 */
@AllArgsConstructor
@Repository
public class SampleStorageListPreperationDAOImpl implements SampleStorageListPreperationDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(SampleStorageListPreperationDAOImpl.class);

	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;

	/**
	 * This method is used to retrieve list of all available sample repository(s)
	 * with respect to site
	 *
	 * @param inputMap [Map] map object holding details to be fetched data from
	 *                 samplestoragelocation table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return a response entity which holds the list of sampleRepositories records
	 *         with respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> getsamplestoragelistpreperation(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {
		final Map<String, Object> outputMap = new HashMap<>();

		final var str = "select ssettingvalue from settings where nsettingcode=40 and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		final var objSettings = (Settings) jdbcUtilityFunction.queryForObject(str, Settings.class, jdbcTemplate);
		var conditonstr = "";
		// ALPDJ21-32--Added by Vignesh(30-09-2025)-->added the Storage instrument
		// validation
		if (Integer.parseInt(objSettings.getSsettingvalue()) == Enumeration.TransactionStatus.YES
				.gettransactionstatus()) {
			conditonstr = "  AND  si.nmappingtranscode="
					+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus();
		}

		var strQuery = " select "
				// + "ssl.*,"
				+ "ssl.nsamplestoragelocationcode, ssl.ssamplestoragelocationname, sst.nsitecode,"
				+ "ts.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode()
				+ "'  as stransdisplaystatus from samplestoragelocation ssl,transactionstatus ts, samplestoragetransaction sst,storageinstrument si "
				+ " where ssl.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				// + " and ssl.nsitecode="
				// + userInfo.getNtranssitecode()
				+ " and ssl.nmastersitecode=" + userInfo.getNmastersitecode()
				+ " and ssl.nsamplestoragelocationcode=si.nsamplestoragelocationcode "
				+ " and ssl.nsamplestoragelocationcode in "
				+ "(select nsamplestoragelocationcode from samplestorageversion where nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and napprovalstatus="
				+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + " and nsitecode="
				+ userInfo.getNmastersitecode() + ") and ts.ntranscode=si.nmappingtranscode"
				+ " and sst.nsamplestoragelocationcode=ssl.nsamplestoragelocationcode and sst.nsitecode="
				+ userInfo.getNtranssitecode() + " and sst.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + conditonstr + " and si.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " group by ssl.nsamplestoragelocationcode, ssl.ssamplestoragelocationname, sst.nsitecode, stransdisplaystatus "
				+ "  order by ssl.nsamplestoragelocationcode desc ";
		var list = jdbcTemplate.queryForList(strQuery);
		outputMap.put("sampleStorageLocation", list);

		strQuery = "select * from containertype where nsitecode=" + userInfo.getNmastersitecode() + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		list = jdbcTemplate.queryForList(strQuery);
		outputMap.put("containerType", list);

		strQuery = "select * from containerstructure where nsitecode=" + userInfo.getNmastersitecode() + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		list = jdbcTemplate.queryForList(strQuery);
		outputMap.put("containerStructure", list);

		strQuery = "select * from visitnumber where nsitecode=" + userInfo.getNmastersitecode() + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		list = jdbcTemplate.queryForList(strQuery);
		outputMap.put("VisitNumber", list);

		// ATE234 Janakumar ALPDJ21-37 Product Enhancement for sample storage &
		// Repository. -- Start
		final var strQueryValidation = "select * from projectsitehierarchymapping where nprojectsitehierarchymapcode>0 and nsitecode="
				+ userInfo.getNmastersitecode() + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

		final var listSiteHierMapping = jdbcTemplate.queryForList(strQueryValidation);

		if (!listSiteHierMapping.isEmpty()) {

			strQuery = "SELECT bp.sprojecttypename ,bp.nprojecttypecode "
					+ "FROM projectsitemapping psm,projecttype bp WHERE psm.nbioprojectcode=bp.nprojecttypecode "
					+ "and bp.nsitecode=" + userInfo.getNmastersitecode() + " and psm.nnodesitecode="
					+ userInfo.getNtranssitecode() + " and bp.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "and psm.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " ;";

			// If need to enable the site combo set the true.
			outputMap.put("needSiteCombo", false);

			final List<Map<String, Object>> lsTthirdPartysharableMap = new ArrayList<>();
			final List<Map<String, Object>> lssampleaccesabletMap = new ArrayList<>();

			// Third-party sharable YES
			Map<String, Object> sharableYes = new HashMap<>();
			sharableYes.put("sthirdpartysharable",
					commonFunction.getMultilingualMessage("IDS_YES", userInfo.getSlanguagefilename()));
			sharableYes.put("nisthirdpartysharable", Enumeration.TransactionStatus.YES.gettransactionstatus());
			lsTthirdPartysharableMap.add(sharableYes);

			// Third-party sharable NO
			Map<String, Object> sharableNo = new HashMap<>();
			sharableNo.put("sthirdpartysharable",
					commonFunction.getMultilingualMessage("IDS_NO", userInfo.getSlanguagefilename()));
			sharableNo.put("nisthirdpartysharable", Enumeration.TransactionStatus.NO.gettransactionstatus());
			lsTthirdPartysharableMap.add(sharableNo);

			// Sample accessible YES
			Map<String, Object> sampleAccessYes = new HashMap<>();
			sampleAccessYes.put("ssampleaccesable",
					commonFunction.getMultilingualMessage("IDS_YES", userInfo.getSlanguagefilename()));
			sampleAccessYes.put("nissampleaccesable", Enumeration.TransactionStatus.YES.gettransactionstatus());
			lssampleaccesabletMap.add(sampleAccessYes);

			// Sample accessible NO
			Map<String, Object> sampleAccessNo = new HashMap<>();
			sampleAccessNo.put("ssampleaccesable",
					commonFunction.getMultilingualMessage("IDS_NO", userInfo.getSlanguagefilename()));
			sampleAccessNo.put("nissampleaccesable", Enumeration.TransactionStatus.NO.gettransactionstatus());
			lssampleaccesabletMap.add(sampleAccessNo);

			// Add to outputMap
			outputMap.put("nisthirdpartysharable", lsTthirdPartysharableMap);
			outputMap.put("nissampleaccesable", lssampleaccesabletMap);

			list = jdbcTemplate.queryForList(strQuery);

			if (list.size() > 1) {
				final Map<String, Object> allItem = new HashMap<>();

				allItem.put("nprojecttypecode", Enumeration.TransactionStatus.ALL.gettransactionstatus());
				allItem.put("sprojecttypename",
						commonFunction.getMultilingualMessage("IDS_ALL", userInfo.getSlanguagefilename()));

				list.add(allItem);
			}

		} else {
			strQuery = "select * from projecttype where nsitecode=" + userInfo.getNmastersitecode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			outputMap.put("needSiteCombo", false);

			list = jdbcTemplate.queryForList(strQuery);

			outputMap.put("projectType", list);

		}

		if (!list.isEmpty()) {
			outputMap.put("selectedProjectType", list.get(0));
			outputMap.put("projectbarcodeconfig", list);
			outputMap.put("nprojecttypecode", list.get(0).get("nprojecttypecode"));
		} else {
			outputMap.put("selectedProjectType", null);
			outputMap.put("projectbarcodeconfig", null);
			outputMap.put("nprojecttypecode", Enumeration.TransactionStatus.NA.gettransactionstatus());
		}

		outputMap.putAll(getProjectbarcodeconfig(outputMap, userInfo).getBody());
		// outputMap.putAll(projectBasedSiteLoad(outputMap, userInfo).getBody());
		// ATE234 Janakumar ALPDJ21-37 Product Enhancement for sample storage &
		// Repository. -- Start
		return new ResponseEntity<Object>(outputMap, HttpStatus.OK);

	}

	/**
	 * This method is used to retrieve list of all Site And Instrument based on
	 * bio-project with respect to site
	 *
	 * @param inputMap [Map] map object holding details to be fetched data from
	 *                 selectedProjectType
	 * @throws Exception that are thrown in the DAO layer
	 */
	// ATE234 Janakumar ALPDJ21-37 Product Enhancement for sample storage &
	// Repository. -- Start
	private ResponseEntity<Map<String, Object>> projectBasedSiteLoad(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {

		final Map<String, Object> outputMap = new HashMap<>();
		String strQuery;
		var selectedSiteCode = -1;

		final var strQuerySite = "select shcd.schildsitecode "
				+ "from projectsitehierarchymapping pshm, sitehierarchyconfigdetails shcd "
				+ "where pshm.nsitehierarchyconfigcode = shcd.nsitehierarchyconfigcode " + "and pshm.nbioprojectcode = "
				+ inputMap.get("nprojecttypecode") + " " + "and shcd.nnodesitecode = " + userInfo.getNtranssitecode()
				+ " " + "and pshm.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "and shcd.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "and pshm.nsitecode = " + userInfo.getNmastersitecode();

		final var listSiteHierMapping = jdbcTemplate.queryForList(strQuerySite);

		if (!listSiteHierMapping.isEmpty()) {
			final var schildsitecode = (String) listSiteHierMapping.get(0).get("schildsitecode");

			final var ssitecode = new StringBuilder("").append(userInfo.getNtranssitecode());
			if (schildsitecode != null && !schildsitecode.isEmpty()) {
				ssitecode.append(",").append(schildsitecode);
			}
			strQuery = "select nsitecode, ssitename, ndefaultstatus from site " + "where nsitecode in ("
					+ ssitecode.append(") ").append("and nstatus = ")
							.append(Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()).append(" ").toString();

			final List<Site> lstSite = jdbcTemplate.query(strQuery, new Site());

			if (!lstSite.isEmpty()) {
				outputMap.put("lstSite", lstSite);
				final var selectedSite = lstSite.stream().filter(x -> x.getNsitecode() == userInfo.getNtranssitecode())
						.findFirst().orElse(new Site());
				// outputMap.put("selectedSite", lstSite.get(lstSite.size() - 1));
				outputMap.put("selectedSite", selectedSite);
				selectedSiteCode = selectedSite.getNsitecode();

				final var instumentQuery = "select i.sinstrumentid,i.ninstrumentcode from  storageinstrument si,instrument i "
						+ "where si.ninstrumentcode=i.ninstrumentcode and  i.ninstrumentcode>0 and i.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "and si.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
						+ "and si.nregionalsitecode=" + selectedSiteCode + "  order by 1 desc ";

				final var listInstument = jdbcTemplate.queryForList(instumentQuery);

				outputMap.put("lstInstrument", listInstument);

			} else {
				outputMap.put("lstSite", null);
				outputMap.put("selectedSite", null);
				outputMap.put("lstInstrument", null);

			}
		} else {
			outputMap.put("lstSite", null);
			outputMap.put("selectedSite", null);
			outputMap.put("lstInstrument", null);

		}

		var strQuery1 = "";
		final var nNeedInstrumentFlow = getInstrumentBased();
		List<Map<String, Object>> list = new ArrayList<>();
		if (nNeedInstrumentFlow == Enumeration.TransactionStatus.YES.gettransactionstatus()) {

			strQuery1 = "select p.nproductcode as nsampletypecode, sproductname from samplestoragetransaction sst, product p where"
					+ " sst.nproductcode=p.nproductcode and sst.nsitecode =" + selectedSiteCode + " and sst.nsitecode="
					+ userInfo.getNtranssitecode() + " and p.nsitecode=" + userInfo.getNmastersitecode()
					+ " and sst.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and p.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " group by p.nproductcode," + " sproductname order by 1 desc";
			list = jdbcTemplate.queryForList(strQuery1);
			outputMap.put("sampleType", list);

		} else {

			strQuery1 = "select p.nproductcode, p.sproductname, sst.nsitecode from  product p, samplestoragemapping ssm,"
					+ " samplestoragetransaction sst where p.nproductcode=ssm.nproductcode and p.nsitecode="
					+ userInfo.getNmastersitecode() + " and ssm.nsitecode=" + userInfo.getNtranssitecode()
					+ " and p.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and ssm.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and ssm.nsamplestoragemappingcode=sst.nsamplestoragemappingcode and sst.nsitecode="
					+ userInfo.getNtranssitecode() + " and sst.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " group by p.nproductcode, p.sproductname, sst.nsitecode order by 1 desc";
			list = jdbcTemplate.queryForList(strQuery1);
			outputMap.put("sampleType", list);

		}

		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}
	// ATE234 Janakumar ALPDJ21-37 Product Enhancement for sample storage &
	// Repository. -- End

	/**
	 * This method is used to retrieve list of all available sample repository(s)
	 * with respect to site and filter credentials
	 *
	 * @param inputMap [Map] map object holding details to read in
	 *                 bulkbarcodeconfigdetails table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return a response entity which holds the list of sampleRepositories records
	 *         with respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Map<String, Object>> getDynamicFilterExecuteData(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {
		List<Map<String, Object>> lst = new ArrayList<>();
		final var str = "SELECT p.sprojecttypename, " + "       pbc.nprojecttypecode, "
				+ "	   pbc.jsondata->>'sfieldname' as sfieldname " + "FROM   bulkbarcodeconfigdetails pbc, "
				+ "       projecttype p " + "WHERE  p.nprojecttypecode = pbc.nprojecttypecode "
				+ "       AND pbc.nprojecttypecode in (  " + inputMap.get("nprojecttypecode")
				+ "  )     AND p.nstatus =   " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ "       AND pbc.nstatus =  " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " AND p.nsitecode=" + userInfo.getNmastersitecode()
				+ " GROUP  BY  p.sprojecttypename,pbc.nprojecttypecode, " + "	   pbc.jsondata->>'sfieldname'  ";
		try {
			lst = jdbcTemplate.queryForList(str);
		} catch (final Exception e) {
			lst = new ArrayList<>();
		}
		if (lst.size() == 0) {
			inputMap.put("view_nprojecttypecode", 0);
		} else {
			// ALPD-4774-Vignesh R(31-08-2024)
			inputMap.put("view_nprojecttypecode",
					(int) inputMap.get("nprojecttypecode") > 3 ? 0 : inputMap.get("nprojecttypecode"));
		}
		if ((int) inputMap.get("nprojecttypecode") == -1) {
			inputMap.put("view_nprojecttypecode", 0);
		}

		System.out.println("JVM Bit size: " + System.getProperty("sun.arch.data.model"));
		var tableName = "";
		var getJSONKeysQuery = "";
		final Map<String, Object> returnObject = new HashMap<>();
		final var sourceName = (String) inputMap.get("source") + (int) inputMap.get("view_nprojecttypecode");
		var conditionString = inputMap.containsKey("conditionstring") ? (String) inputMap.get("conditionstring") : "";
		if (conditionString.isEmpty()) {
			conditionString = inputMap.containsKey("filterquery") ? "and " + (String) inputMap.get("filterquery") : "";
		}

		final var scollate = "collate \"default\"";
		if (conditionString.contains("LIKE")) {

			while (conditionString.contains("LIKE")) {
				final var sb = conditionString;
				var sQuery = conditionString;
				final var colanindex = sb.indexOf("LIKE '");
				final var str1 = sQuery.substring(0, colanindex + 6);
				sQuery = sQuery.substring(colanindex + 6);
				final var sb3 = new StringBuilder(str1);
				final var sb4 = new StringBuilder(sQuery);
				sb3.replace(colanindex, colanindex + 4, "ilike");
				System.out.println(sQuery);
				final var indexofsv = sQuery.indexOf("'");

				sb4.replace(indexofsv, indexofsv + 1, "'" + scollate + " ");
				conditionString = sb3.toString() + sb4.toString();
			}

		}

		tableName = sourceName.toLowerCase();

		final var getJSONFieldQuery = "select string_agg(column_name ,'||')FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = '"
				+ tableName + "' and data_type = 'jsonb'";
		var jsonField = jdbcTemplate.queryForObject(getJSONFieldQuery, String.class);
		jsonField = jsonField != null ? "||" + jsonField : "";
		final var getFieldQuery = "select string_agg(column_name ,'||')FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = '"
				+ tableName + "'";
		final var fields = jdbcTemplate.queryForObject(getFieldQuery, String.class);
		if (fields != null && fields.contains(inputMap.get("valuemember").toString())) {
			getJSONKeysQuery = "select " + tableName + ".* from " + tableName + " where nstatus ="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and \""
					+ inputMap.get("valuemember") + "\" > '0' " + conditionString + " ;";
		} else {
			getJSONKeysQuery = "select  " + tableName + ".* from " + tableName + " where nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + conditionString + " ;";
		}
		SampleStorageListPreperationDAOImpl.LOGGER.info("Filter Query---> " + getJSONKeysQuery);

		final var data = jdbcTemplate.queryForList(getJSONKeysQuery);
		returnObject.put((String) inputMap.get("label"), data);
		return new ResponseEntity<>(returnObject, HttpStatus.OK);
	}

	/**
	 * This method is used to retrieve active sampleRepository Barcode object, based
	 * on the specified nprojecttypecode and spositionvalue.
	 *
	 * @param inputMap [Map] map object holding details to read in
	 *                 view_sampleretrieval_0 table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return a response entity object holding response status and data of
	 *         sampleRepository Barcode
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> getSelectedBarcodeData(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		final Map<String, Object> returnObject = new HashMap<>();
		Map<String, Object> object = new HashMap<>();

		// ALPD-4774-Vignesh R(31-08-2024)
		final var str = "select * from view_sampleretrieval_"
				+ ((int) inputMap.get("nprojecttypecode") == -1 || (int) inputMap.get("nprojecttypecode") > 3 ? 0
						: (int) inputMap.get("nprojecttypecode"))
				+ " where spositionvalue='" + inputMap.get("spositionvalue") + "';";
		try {
			object = jdbcTemplate.queryForMap(str);
		} catch (final Exception e) {
			object = new HashMap<>();
		}
		returnObject.put("selectedBarcodeValue", object);
		return new ResponseEntity<>(returnObject, HttpStatus.OK);
	}

	/**
	 * This method is used to get all the available sampleRepositories for the
	 * specified nprojecttypecode and site.
	 *
	 * @param inputMap [Map] map object holding details to read in
	 *                 bulkbarcodeconfigdetails table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return a response entity which holds the list of sampleRepositories records
	 *         with respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Map<String, Object>> getProjectbarcodeconfig(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {
		final Map<String, Object> returnObject = new HashMap<>();
		List<Map<String, Object>> lst = new ArrayList<>();

		final var str = " select bbcd.nprojecttypecode,pt.sprojecttypename,bbcd.jsondata->>'sfieldname' as sfieldname from bulkbarcodeconfig bbc, bulkbarcodeconfigversion bbcv,"
				+ " bulkbarcodeconfigdetails bbcd ,projecttype pt where bbc.nbulkbarcodeconfigcode=bbcv.nbulkbarcodeconfigcode and "
				+ " bbcd.nbulkbarcodeconfigcode=bbc.nbulkbarcodeconfigcode and pt.nprojecttypecode = bbc.nprojecttypecode and bbcd.nprojecttypecode in ("
				+ inputMap.get("nprojecttypecode") + ")" + " and bbcv.ntransactionstatus="
				+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + " and bbc.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and bbcv.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and bbcd.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and pt.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and bbc.nsitecode="
				+ userInfo.getNmastersitecode() + " and bbcv.nsitecode=" + userInfo.getNmastersitecode()
				+ " and bbcd.nsitecode=" + userInfo.getNmastersitecode() + " and pt.nsitecode="
				+ userInfo.getNmastersitecode() + " group by bbcd.nprojecttypecode,pt.sprojecttypename,"
				+ " bbcd.jsondata->>'sfieldname',bbcd.nsorter order by bbcd.nsorter desc ";
		try {
			lst = jdbcTemplate.queryForList(str);
		} catch (final Exception e) {
			lst = new ArrayList<>();
		}
		returnObject.put("selectedProjectTypeList", lst);
		returnObject.putAll(projectBasedSiteLoad(inputMap, userInfo).getBody());

		return new ResponseEntity<>(returnObject, HttpStatus.OK);
	}

	/**
	 * This method is used to retrieve sample storage details based on imported
	 * excel. Here the imported excel sheet is iterated to get the sampleIDs as
	 * String ("filterquery") with conditions added to it and is put into inputMap
	 * [Map] map with keys "source","label","valuemember","nprojecttypecode" and
	 * "filterquery". This inputMap and userInfo is then sent to
	 * getDynamicFilterExecuteData() in the same DAOImpl class to get the stored
	 * sample details.
	 *
	 * @param request [MultipartHttpServletRequest] multipart request with
	 *                parameters:
	 *                ImportFile,nformcode,userinfo,nprojecttypecode,source,fieldName,label,valuemember
	 * @return response object with list of sample storage details based on imported
	 *         excel.
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> getImportSampleIDData(final MultipartHttpServletRequest request,
			final UserInfo userInfo) throws Exception {
		final Map<String, Object> inputMap = new HashMap<>();
		final var source = StringEscapeUtils.unescapeJava(request.getParameter("source")).toString();
		final var label = StringEscapeUtils.unescapeJava(request.getParameter("label")).toString();
		final var valuemember = StringEscapeUtils.unescapeJava(request.getParameter("valuemember")).toString();
		final var nprojecttypecode = Integer.parseInt(request.getParameter("nprojecttypecode"));
		inputMap.put("source", source);
		inputMap.put("label", label);
		inputMap.put("valuemember", valuemember);
		inputMap.put("nprojecttypecode", nprojecttypecode);

		final var multipartFile = request.getFile("ImportFile");
		final var fieldName = StringEscapeUtils.unescapeJava(request.getParameter("fieldName")).toString();
		final var ins = multipartFile.getInputStream();
		final var baos = new ByteArrayOutputStream();
		final var buffer = new byte[1024];
		int len;
		while ((len = ins.read(buffer)) > -1) {
			baos.write(buffer, 0, len);
		}
		baos.flush();
		try {
			final InputStream is1 = new ByteArrayInputStream(baos.toByteArray());
			final var workbook = WorkbookFactory.create(is1);
			final var sheet = workbook.getSheetAt(0);

			var rowIndex = 0;
			final List<String> lstHeader = new ArrayList<>();
			final var positionValue = new StringJoiner(",");
			var cellIndex = 0;
			for (final Row row : sheet) {
				if (rowIndex > 0) {
					if (!lstHeader.isEmpty()) {
						for (final String element : lstHeader) {
							if (row.getCell(cellIndex) != null) {
								SampleStorageListPreperationDAOImpl.LOGGER
										.info(row.getCell(cellIndex).toString().trim());
								final var cell = row.getCell(cellIndex);
								positionValue.add("'" + cell.toString().trim() + "'");
							} else {
								positionValue.add("''");
							}
						}
					} else {
						return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_INVALIDTEMPLATEHEADERS",
								userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
					}
				} else {

					for (final Cell cell : row) // iteration over cell using for each loop
					{
						final var header = cell.getStringCellValue();
						if (header.trim().equals(fieldName.trim())) {
							lstHeader.add(header);
							break;
						}
						cellIndex++;
					}
				}
				rowIndex++;
			}
			final var spositionvalue = positionValue.toString();
			final var filterquery = " nprojecttypecode=" + nprojecttypecode + " and spositionvalue in ("
					+ spositionvalue + ")";
			inputMap.put("filterquery", filterquery);

			final var returnObject = getDynamicFilterExecuteData(inputMap, userInfo).getBody();

			final var data = (List<Map<String, Object>>) returnObject.get(inputMap.get("label"));
			if (!data.isEmpty()) {
				return new ResponseEntity<>(returnObject, HttpStatus.OK);
			} else {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage(
						"IDS_NORECORDSFOUNDFORGIVENPROJECTTYPE", userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
			}

		} catch (final Exception e) {
			e.printStackTrace();
			SampleStorageListPreperationDAOImpl.LOGGER.error("Error: upload file not proper file." + e);

			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_INVALIDFILE", userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}
	}

	/**
	 * This method is used to retrieve list of available sample repository(s) for
	 * the specified nprojecttypecode and site.
	 * 
	 * @param inputMap [Map] map object with "nprojecttypecode" and "userinfo" as
	 *                 keys for which the data is to be fetched Input : {
	 *                 "selectedSiteCode": 1, "userinfo":{nmastersitecode": -1,
	 *                 "slanguagetypecode": "en-US", "ntranssitecode": 1} }
	 * @return response entity object holding response status and list of all
	 *         sampleRepositories
	 * @throws Exception exception
	 */
	@Override
	public ResponseEntity<Map<String, Object>> getSiteBasedInstrument(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		// TODO Auto-generated method stub

		final Map<String, Object> outputMap = new HashMap<>();

		final var nNeedInstrumentFlow = getInstrumentBased();

		final var instumentQuery = "select i.sinstrumentid,i.ninstrumentcode from  storageinstrument si,instrument i "
				+ "where si.ninstrumentcode=i.ninstrumentcode and  i.ninstrumentcode>0 and i.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "and si.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "and si.nregionalsitecode="
				+ inputMap.get("selectedSiteCode") + "  order by 1 desc ";

		final var listInstument = jdbcTemplate.queryForList(instumentQuery);

		outputMap.put("lstInstrument", Optional.of(listInstument).filter(l -> !l.isEmpty()).orElse(null));

		// final var str = "select ssettingvalue from settings where nsettingcode=40 and
		// nstatus = "
		// + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		// final var objSettings = (Settings) jdbcUtilityFunction.queryForObject(str,
		// Settings.class, jdbcTemplate);

		var conditonstr = "";
		if (nNeedInstrumentFlow == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
			conditonstr = "  AND  ssl.nmappingtranscode="
					+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus();
		}

		var strQuery = " select "
				// + "ssl.*,"
				+ "ssl.nsamplestoragelocationcode, ssl.ssamplestoragelocationname,"
				+ "ts.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode()
				+ "'  as stransdisplaystatus from samplestoragelocation ssl,transactionstatus ts, samplestoragetransaction sst"
				+ " where ssl.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				// + " and ssl.nsitecode="
				// + userInfo.getNtranssitecode()
				+ " and ssl.nmastersitecode=" + userInfo.getNmastersitecode()
				+ " and ssl.nsamplestoragelocationcode in "
				+ "(select nsamplestoragelocationcode from samplestorageversion where nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and napprovalstatus="
				+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + " and nsitecode="
				+ userInfo.getNmastersitecode() + ") and ts.ntranscode=ssl.nmappingtranscode"
				+ " and sst.nsamplestoragelocationcode=ssl.nsamplestoragelocationcode and sst.nsitecode="
				+ inputMap.get("selectedSiteCode") + " and sst.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + conditonstr
				+ " group by ssl.nsamplestoragelocationcode, ssl.ssamplestoragelocationname, stransdisplaystatus "
				+ "  order by ssl.nsamplestoragelocationcode desc ";
		var list = jdbcTemplate.queryForList(strQuery);
		outputMap.put("sampleStorageLocation", list);

		if (nNeedInstrumentFlow == Enumeration.TransactionStatus.YES.gettransactionstatus()) {

			strQuery = "select p.nproductcode as nsampletypecode, sproductname from samplestoragetransaction sst, product p where"
					+ " sst.nproductcode=p.nproductcode and sst.nsitecode=" + inputMap.get("selectedSiteCode")
					+ " and p.nsitecode=" + userInfo.getNmastersitecode() + " and sst.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and p.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " group by p.nproductcode,"
					+ " sproductname order by 1 desc";
			list = jdbcTemplate.queryForList(strQuery);

		} else {
			strQuery = "select p.nproductcode, p.sproductname from  product p, samplestoragemapping ssm,"
					+ " samplestoragetransaction sst where p.nproductcode=ssm.nproductcode and p.nsitecode="
					+ userInfo.getNmastersitecode() + " and ssm.nsitecode=" + inputMap.get("selectedSiteCode")
					+ " and p.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and ssm.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and ssm.nsamplestoragemappingcode=sst.nsamplestoragemappingcode and sst.nsitecode="
					+ inputMap.get("selectedSiteCode") + " and sst.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " group by p.nproductcode, p.sproductname order by 1 desc";
			list = jdbcTemplate.queryForList(strQuery);
		}
		outputMap.put("sampleType", list);

		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	// ALPDJ21-71--Added by Vignesh(06-09-2025)--new fields added the for the
	// partition of sample storage mapping
	private int getInstrumentBased() throws Exception {

		final var sSetting = "select ssettingvalue from settings where nsettingcode ="
				+ Enumeration.Settings.INSTRUMENT_BASED_STORAGE.getNsettingcode() + " and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
		final var sNeedStorageFlow = (String) jdbcUtilityFunction.queryForObject(sSetting, String.class, jdbcTemplate);
		final var nNeedInstrumentFlow = Integer.parseInt(sNeedStorageFlow);
		return nNeedInstrumentFlow;
	}

}
