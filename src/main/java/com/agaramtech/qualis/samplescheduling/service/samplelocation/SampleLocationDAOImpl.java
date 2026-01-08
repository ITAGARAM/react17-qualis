package com.agaramtech.qualis.samplescheduling.service.samplelocation;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.agaramtech.qualis.credential.model.Site;
import com.agaramtech.qualis.global.AuditUtilityFunction;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.ProjectDAOSupport;
import com.agaramtech.qualis.global.StringUtilityFunction;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.global.ValidatorDel;
import com.agaramtech.qualis.samplescheduling.model.SampleLocation;

import lombok.AllArgsConstructor;

/**
 * This class is used to perform CRUD Operation on "samplelocation" table by implementing
 * methods from its interface.
 */
/**
 * @author sujatha.v AT-E274 SWSM-5 24/07/2025
 */
@AllArgsConstructor
@Repository
public class SampleLocationDAOImpl implements SampleLocationDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(SampleLocationDAOImpl.class);

	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;
//added by sujatha ATE_274 for delete validation 27-09-2025
	private final ProjectDAOSupport projectDAOSupport;
	private ValidatorDel validatorDel;
	
	/**
	 * This method is used to retrieve list of all available samplelocation's based
	 * on Region, District, Taluk and Village
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return response entity object holding response status and list of all active
	 *         samplelocation's
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> getSampleLocation(final UserInfo userInfo) throws Exception {
		LOGGER.info("getSampleLocation");
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
//		final String strSampleLocation = "select s.nsamplelocationcode, r.sregionname, s.nregioncode, d.sdistrictname, "
//										+ "s.ndistrictcode, c.scityname, s.ncitycode, v.svillagename, s.nvillagecode, "
//										+ " s.ssamplelocationname, s.slatitude, s.slongitude, s.sdescription, s.nsitecode," + " s.nstatus ,"
//										+ " to_char(v.dmodifieddate, '" + userInfo.getSpgsitedatetime().replace("'T'", " ")
//										+ " ') as smodifieddate from region r, district d, city c, villages v, samplelocation s "
//										+ "where r.nregioncode=s.nregioncode and d.ndistrictcode=s.ndistrictcode and c.ncitycode=s.ncitycode "
//										+ " and v.nvillagecode=s.nvillagecode and" + " r.nstatus= "
//										+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and d.nstatus= "
//										+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and c.nstatus ="
//										+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and v.nstatus= "
//										+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and s.nstatus= "
//										+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and s.nsamplelocationcode>0 and "
//										+ " s.nsitecode = " + userInfo.getNtranssitecode();
//		final String strSampleLocation = "SELECT s.nsamplelocationcode, r.ssitename AS sregionname, s.nregioncode, "
//				+ "d.ssitename AS sdistrictname, s.ndistrictcode, " + "c.ssitename AS scityname, s.ncitycode, "
//				+ "v.svillagename AS svillagename, s.nvillagecode,  cs.ssitename AS scentralsitename, s.ncentralsitecode, "
//				+ "s.ssamplelocationname, s.slatitude, s.slongitude, s.sdescription, s.nsitecode, s.nstatus, "
//				+ "TO_CHAR(s.dmodifieddate, '" + userInfo.getSpgsitedatetime().replace("'T'", " ")
//				+ "') AS smodifieddate " + "FROM samplelocation s, site r, site d, site c, villages v , site cs "
//				+ "WHERE s.nregioncode = r.nsitecode AND r.nstatus = "
//				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//				+ " AND s.ndistrictcode = d.nsitecode AND d.nstatus = "
//				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//				+ " AND s.ncitycode = c.nsitecode AND c.nstatus = "
//				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//				+ " AND s.ncentralsitecode = cs.nsitecode AND cs.nstatus = "
//				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//				+ " AND s.nvillagecode = v.nvillagecode AND v.nstatus = "
//				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " AND s.nstatus = "
//				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " AND s.nsitecode = "
//				+ userInfo.getNmastersitecode() + " AND s.nsamplelocationcode > 0"
//				+ " order by nsamplelocationcode desc;";
		// get based on logged in site and its child record getting
		
		//modified by sujatha v ATE_274 21-09- 2025 SWSM-5 on 21-09-2025 for samplelocation get ( is based on site hierarchy configuration)
		final String strSampleLocation =" WITH RECURSIVE site_base AS ( SELECT DISTINCT "
			    		+ " sc.jsondata, sc.nsitehierarchyconfigcode, sc.ntransactionstatus FROM sitehierarchyconfigdetails sd "
			    		+ " JOIN sitehierarchyconfig sc ON sd.nsitehierarchyconfigcode = sc.nsitehierarchyconfigcode "
			    		+ " WHERE sd.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
			    		+ " AND sc.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
			    		+ " AND sc.ntransactionstatus ="+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus()+ " ), "
			    		+ " json_nodes AS ( SELECT jsonb_build_object('item', sb.jsondata -> 'item', 'nodes', sb.jsondata -> 'nodes') AS node, "
			    		+ " sb.nsitehierarchyconfigcode FROM site_base sb  UNION ALL "
			    		+ " SELECT jsonb_array_elements(j.node -> 'nodes'), j.nsitehierarchyconfigcode "
			    		+ " FROM json_nodes j WHERE j.node ? 'nodes' ), "
			    		+ " node_details AS ( SELECT DISTINCT (node -> 'item' -> 'selectedNodeDetail' ->> 'nsitecode')::int AS nsitecode, "
			    		+ " node -> 'item' -> 'selectedNodeDetail' ->> 'ssitename' AS ssitename, "
//			    		+ " (node -> 'item' -> 'selectedNodeDetail' ->> 'nmastersitecode')::int AS nmastersitecode, "
			    		+ " nsitehierarchyconfigcode FROM json_nodes ), "
			    		+ " login_and_children AS ( SELECT nsitecode, nsitehierarchyconfigcode FROM node_details WHERE"
			    		+ " nsitecode = " + userInfo.getNtranssitecode() + " UNION  SELECT nd.nsitecode, nd.nsitehierarchyconfigcode FROM node_details nd "
			    		+ " JOIN login_and_children lc ON nd.nsitecode = lc.nsitecode) "
			    		+ " SELECT DISTINCT  s.nsamplelocationcode,  r.ssitename AS sregionname,  s.nregioncode, "
			    		+ " d.ssitename AS sdistrictname, s.ndistrictcode, c.ssitename AS scityname, s.ncitycode, "
			    		+ " v.svillagename AS svillagename, s.nvillagecode, cs.ssitename AS scentralsitename, s.ncentralsitecode, "
			    		+ " s.ssamplelocationname, s.slatitude, s.slongitude, s.sdescription, s.nsitecode, s.nstatus, "
			    		+ " TO_CHAR(s.dmodifieddate, '" + userInfo.getSpgsitedatetime().replace("'T'", " ") + "') AS smodifieddate, "
			    		+ " lc.nsitehierarchyconfigcode FROM samplelocation s LEFT JOIN node_details r ON r.nsitecode = s.nregioncode "
			    		+ " LEFT JOIN node_details d ON d.nsitecode = s.ndistrictcode "
			    		+ " LEFT JOIN node_details c ON c.nsitecode = s.ncitycode "
			    		+ " LEFT JOIN node_details cs ON cs.nsitecode = s.ncentralsitecode "
			    		+ " JOIN villages v ON v.nvillagecode = s.nvillagecode AND "
			    		+ " v.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
			    		+ " JOIN login_and_children lc ON ( s.nregioncode = lc.nsitecode OR s.ndistrictcode = lc.nsitecode"
			    		+ " OR s.ncitycode = lc.nsitecode  OR s.ncentralsitecode = lc.nsitecode) and lc.nsitehierarchyconfigcode=s.nsitehierarchyconfigcode "
			    		+ " WHERE s.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
			    		+ " AND s.nsamplelocationcode > 0 "
			    		+ " ORDER BY s.nsamplelocationcode DESC;";
		
		//added and commentted by sujatha ATE_274 for checking performance of group by clause(compared to distinct used above, this below take's too time to execute)
//		final String strSampleLocation = "WITH RECURSIVE site_base AS ( SELECT sc.jsondata, sc.nsitehierarchyconfigcode, sc.ntransactionstatus "
//				+ " FROM sitehierarchyconfigdetails sd JOIN sitehierarchyconfig sc "
//				+ " ON sd.nsitehierarchyconfigcode = sc.nsitehierarchyconfigcode " 
//				+ " WHERE sd.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
//				+ " AND sc.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " 
//				+ " AND sc.ntransactionstatus = " + Enumeration.TransactionStatus.APPROVED.gettransactionstatus()+"), "
//				+ "json_nodes AS ( " +"  SELECT jsonb_build_object('item', sb.jsondata -> 'item', 'nodes', sb.jsondata -> 'nodes') AS node, " 
//				+ " sb.nsitehierarchyconfigcode FROM site_base sb UNION ALL " 
//				+ " SELECT jsonb_array_elements(j.node -> 'nodes'), j.nsitehierarchyconfigcode " 
//				+ " FROM json_nodes j WHERE j.node ? 'nodes' " +"),node_details AS ( " 
//				+ " SELECT (node -> 'item' -> 'selectedNodeDetail' ->> 'nsitecode')::int AS nsitecode, " 
//				+ " node -> 'item' -> 'selectedNodeDetail' ->> 'ssitename' AS ssitename, " 
//				+ " nsitehierarchyconfigcode " +" FROM json_nodes " +"), login_and_children AS ( SELECT nsitecode, "
//				+ " nsitehierarchyconfigcode FROM node_details WHERE nsitecode = " + userInfo.getNtranssitecode() + " " +"  UNION " 
//				+ " SELECT nd.nsitecode, nd.nsitehierarchyconfigcode  FROM node_details nd  "
//				+ " JOIN login_and_children lc ON nd.nsitecode = lc.nsitecode ) " +"SELECT s.nsamplelocationcode, " 
//				+ " r.ssitename AS sregionname, s.nregioncode, d.ssitename AS sdistrictname, s.ndistrictcode,  c.ssitename AS scityname, " 
//				+ " s.ncitycode, v.svillagename AS svillagename, s.nvillagecode, cs.ssitename AS scentralsitename, s.ncentralsitecode, " 
//				+ " s.ssamplelocationname, s.slatitude, s.slongitude, s.sdescription, s.nsitecode, s.nstatus, " 
//				+ " TO_CHAR(s.dmodifieddate, '" + userInfo.getSpgsitedatetime().replace("'T'", " ") + "') AS smodifieddate, " 
//				+ " lc.nsitehierarchyconfigcode FROM samplelocation s " 
//				+ "LEFT JOIN node_details r ON r.nsitecode = s.nregioncode LEFT JOIN node_details d ON d.nsitecode = s.ndistrictcode " 
//				+ "LEFT JOIN node_details c ON c.nsitecode = s.ncitycode LEFT JOIN node_details cs ON cs.nsitecode = s.ncentralsitecode " 
//				+ "JOIN villages v ON v.nvillagecode = s.nvillagecode AND v.nstatus = " 
//				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " 
//				+" JOIN login_and_children lc ON ( s.nregioncode = lc.nsitecode OR s.ndistrictcode = lc.nsitecode " 
//				+" OR s.ncitycode = lc.nsitecode OR s.ncentralsitecode = lc.nsitecode ) " 
//				+" AND lc.nsitehierarchyconfigcode = s.nsitehierarchyconfigcode WHERE s.nstatus = " 
//				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " 
//				+ " AND s.nsamplelocationcode > 0 GROUP BY s.nsamplelocationcode, r.ssitename, s.nregioncode, d.ssitename, s.ndistrictcode, " 
//				+ " c.ssitename, s.ncitycode, v.svillagename, s.nvillagecode, cs.ssitename, s.ncentralsitecode, " 
//				+ " s.ssamplelocationname, s.slatitude, s.slongitude, s.sdescription, s.nsitecode, s.nstatus, " 
//				+ " TO_CHAR(s.dmodifieddate, '" + userInfo.getSpgsitedatetime().replace("'T'", " ") + "'), " 
//				+ " lc.nsitehierarchyconfigcode ORDER BY s.nsamplelocationcode DESC;";

		
//		return new ResponseEntity<>(jdbcTemplate.query(strSampleLocation, new SampleLocation()), HttpStatus.OK);
		List<SampleLocation> samplelocationList = jdbcTemplate.query(strSampleLocation, new SampleLocation());
		outputMap.put("SampleLocation", samplelocationList);
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	/**
	 * This method is used to add a new entry to samplelocation table.
	 * SampleLocation Name is unique for each unique Combination of region,
	 * district, taluk and village. Need to check for duplicate entry of
	 * samplelocation name for the specified site before saving into database.
	 * 
	 * @param objSampleLocation [SampleLocation] object holding details to be added
	 *                          in samplelocation table
	 * @param userInfo          [UserInfo] holding logged in user details based on
	 *                          which the list is to be fetched
	 * @return saved samplelocation object with status code 200 if saved
	 *         successfully else if the samplelocation is already exists, response
	 *         will be returned as 'Already Exists' with status code 409
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> createSampleLocation(final SampleLocation objSampleLocation, final UserInfo userInfo)
			throws Exception {
		final String sQuery = " lock  table samplelocation " + Enumeration.ReturnStatus.EXCLUSIVEMODE.getreturnstatus();
		jdbcTemplate.execute(sQuery);
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> savedSampleLocationList = new ArrayList<>();
		final SampleLocation sampleLocationByName = getSampleLocationByName(objSampleLocation,
				userInfo.getNmastersitecode());
//		String alert = "";
		if (sampleLocationByName == null) {
			final String sequencenoquery = "select nsequenceno from seqnosamplescheduling where stablename ='samplelocation' "
					+ "and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			int nsequenceno = jdbcTemplate.queryForObject(sequencenoquery, Integer.class);
			nsequenceno++;

			//modified by sujatha v ATE_274 SWSM-5 on 21-09-2025 for added 2 more field in samplelocation table
			final String insertquery = "Insert into samplelocation (nsamplelocationcode,nsitehierarchyconfigcode,nregioncode,"
					+ " ndistrictcode,ncitycode, nvillagecode, ssamplelocationname, slatitude, slongitude, sdescription, "
					+ " dmodifieddate,nsitecode,nstatus, ncentralsitecode) " + "values(" + nsequenceno + ", "
					+ objSampleLocation.getNsitehierarchyconfigcode()+", "
					+ objSampleLocation.getNregioncode() + ", " + objSampleLocation.getNdistrictcode() + ", "
					+ objSampleLocation.getNcitycode() + ", " + objSampleLocation.getNvillagecode() + ",N'"
					+ stringUtilityFunction.replaceQuote(objSampleLocation.getSsamplelocationname()) + "',N'"
					+ stringUtilityFunction.replaceQuote(objSampleLocation.getSlatitude()) + "',N'"
					+ stringUtilityFunction.replaceQuote(objSampleLocation.getSlongitude()) + "',N'"
					+ stringUtilityFunction.replaceQuote(objSampleLocation.getSdescription()) + "', '"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNmastersitecode() + ", "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ", "+  objSampleLocation.getNcentralsitecode() +")";
			jdbcTemplate.execute(insertquery);
			final String updatequery = "update seqnosamplescheduling set nsequenceno =" + nsequenceno
					+ " where stablename='samplelocation'" + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			jdbcTemplate.execute(updatequery);
			//added by sujatha v ATE_274 SWSM-5 on 21-09-2025 for getting the name from site table for samplelocation audit trail
			String centralSiteName = getSiteNameForAudit( objSampleLocation.getNcentralsitecode(), userInfo);
			String regionName = getSiteNameForAudit(objSampleLocation.getNregioncode(), userInfo);
			String districtName = getSiteNameForAudit(objSampleLocation.getNdistrictcode(), userInfo);
			String cityName = getSiteNameForAudit(objSampleLocation.getNcitycode(), userInfo);
//			String villageName = getSiteNameForAudit(objSampleLocation.getNvillagecode(), userInfo);
			objSampleLocation.setScentralsitename(centralSiteName);
			objSampleLocation.setSregionname(regionName);
			objSampleLocation.setSdistrictname(districtName);
			objSampleLocation.setScityname(cityName);
//			objSampleLocation.setSvillagename(villageName);
			objSampleLocation.setNsamplelocationcode(nsequenceno);
			savedSampleLocationList.add(objSampleLocation);
			multilingualIDList.add("IDS_ADDSAMPLELOCATION");
			auditUtilityFunction.fnInsertAuditAction(savedSampleLocationList, 1, null, multilingualIDList, userInfo);
			return getSampleLocation(userInfo);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}
	}

	/**
	 * This method is used to fetch the samplelocation object for the specified
	 * samplelocation name and it fetches based on region, district, taluk and
	 * village
	 * 
	 * @param objSampleLocation [SampleLocation] object holding details newly added
	 *                          samplelocation
	 * @param nmasterSiteCode   [int] site code of the samplelocation
	 * @return samplelocation object based on the specified samplelocation name for
	 *         same region, district, taluk, village and site
	 * @throws Exception that are thrown from this DAO layer
	 */
	private SampleLocation getSampleLocationByName(final SampleLocation objSampleLocation, final int transsitecode)
			throws Exception {
		//modified by sujatha v ATE_274 SWSM-5 on 21-09-2025 for samplelocation along with combination to check name specific to sitehierarchyconfigcode
		final String strQuery = "select nsamplelocationcode from samplelocation where ssamplelocationname = N'"
								+ stringUtilityFunction.replaceQuote(objSampleLocation.getSsamplelocationname()) 
								+ "' and nsitehierarchyconfigcode="+objSampleLocation.getNsitehierarchyconfigcode()
								+ " and nregioncode= "+ objSampleLocation.getNregioncode() 
								+ " and ndistrictcode= " + objSampleLocation.getNdistrictcode()
								+ " and ncentralsitecode= "+ objSampleLocation.getNcentralsitecode()
								+ " and ncitycode= " + objSampleLocation.getNcitycode() + " and nvillagecode= "
								+ objSampleLocation.getNvillagecode() + " and nstatus = "
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode =" + transsitecode;
		final SampleLocation sampleLocation = (SampleLocation) jdbcUtilityFunction.queryForObject(strQuery,
				SampleLocation.class, jdbcTemplate);
		return sampleLocation;
	}

	/**
	 * This method is used to retrieve active samplelocation object based on the
	 * specified nsamplelocationcode and check for same region, district, taluk and
	 * village.
	 * 
	 * @param nsamplelocationcode [int] primary key of samplelocation object
	 * @param userInfo            [UserInfo] holding logged in user details based on
	 *                            which the list is to be fetched
	 * @return response entity object holding response status and data of
	 *         samplelocation object
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public SampleLocation getActiveSampleLocationById(int nsamplelocationcode) throws Exception {

//		final String strQuery = "select s.nsamplelocationcode, r.sregionname, s.nregioncode, d.sdistrictname, s.ndistrictcode, "
//								+ " c.scityname, s.ncitycode, v.svillagename, s.nvillagecode, s.ssamplelocationname, s.slatitude,"
//								+ " s.slongitude, s.sdescription, v.nsitecode, v.nstatus , "
//								+ " v.ncitycode from region r, district d, city c, villages v, samplelocation s " 
//								+ " where r.nregioncode=s.nregioncode and d.ndistrictcode=s.ndistrictcode and"
//								+ " c.ncitycode=s.ncitycode and v.nvillagecode=s.nvillagecode"
//								+ " and r.nstatus ="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
//								+ " and d.nstatus ="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
//								+ " and c.nstatus ="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
//								+ " and v.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//								+ " and s.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//								+ " and s.nsamplelocationcode = " + nsamplelocationcode;	
		//added by sujatha v ATE_274 SWSM-5 on 21-09-2025 for samplelocation get Active (based on site )
		final String strQuery = "SELECT s.nsamplelocationcode, r.ssitename AS sregionname, s.nregioncode, "
				+ "d.ssitename AS sdistrictname, s.ndistrictcode, c.ssitename AS scityname, s.ncitycode, "
				+ "v.svillagename AS svillagename, s.nvillagecode, cs.ssitename AS scentralsitename, s.ncentralsitecode, "
				+ "s.ssamplelocationname, s.slatitude, s.slongitude, s.sdescription, " + "s.nsitecode, s.nstatus  "
				+ "FROM samplelocation s, site r, site d, site c, villages v, site cs "
				+ "WHERE s.nregioncode = r.nsitecode AND r.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " AND s.ndistrictcode = d.nsitecode AND d.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " AND s.ncitycode = c.nsitecode AND c.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " AND s.ncentralsitecode = cs.nsitecode AND cs.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " AND s.nvillagecode = v.nvillagecode AND v.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " AND s.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " AND s.nsamplelocationcode="
				+ nsamplelocationcode;

		final SampleLocation sampleLocation = (SampleLocation) jdbcUtilityFunction.queryForObject(strQuery,
				SampleLocation.class, jdbcTemplate);
		return sampleLocation;
	}

	/**
	 * This method is used to update entry in samplelocation table. Need to validate
	 * that the samplelocation object to be updated is active before updating
	 * details in database. Need to check for duplicate entry of village name for
	 * the specified site and also check that the same combination is exits before
	 * before and then saving into database.
	 * 
	 * @param objSampleLocation [SampleLocation] object holding details to be
	 *                          updated in samplelocation table
	 * @param userInfo          [UserInfo] holding logged in user details based on
	 *                          which the list is to be fetched
	 * @return saved samplelocation object with status code 200 if saved
	 *         successfully else if the samplelocation already exists, response will
	 *         be returned as 'Already Exists' with status code 409 else if the
	 *         samplelocation to be updated is not available, response will be
	 *         returned as 'Already Deleted' with status code 417
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> updateSampleLocation(SampleLocation objSampleLocation, UserInfo userInfo)
			throws Exception {
		final SampleLocation sampleLocation = getActiveSampleLocationById(objSampleLocation.getNsamplelocationcode());
		String alert = "";
		if (sampleLocation == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			//modified by sujatha v ATE_274 SWSM-5 on 21-09-2025 to check
			final String querySampleLocation = "select s.nsamplelocationcode from samplelocation s"
					+ " where s.nregioncode = " + objSampleLocation.getNregioncode() 
					+ " and nsitehierarchyconfigcode="+objSampleLocation.getNsitehierarchyconfigcode()
					+ " and s.ndistrictcode = "+ objSampleLocation.getNdistrictcode() 
					+ " and s.ncitycode = " + objSampleLocation.getNcitycode()
					+ " and s.ncentralsitecode = " + objSampleLocation.getNcentralsitecode()
					+ " and s.nvillagecode = " + objSampleLocation.getNvillagecode() + " and s.ssamplelocationname= '"
					+ stringUtilityFunction.replaceQuote(objSampleLocation.getSsamplelocationname()) + "'"
					+ " and s.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and s.nsamplelocationcode <> " + objSampleLocation.getNsamplelocationcode();

			final List<SampleLocation> sampleLocationList = jdbcTemplate.query(querySampleLocation,
					new SampleLocation());
			if (sampleLocationList.isEmpty()) {
				
				//modifed by sujatha v ATE_274 SWSM-5 on 21-09-2025 for added 2 more field in samplelocation table
				final String updateQueryString = "update samplelocation set nregioncode="
						+ objSampleLocation.getNregioncode() + " , ndistrictcode="
						+ objSampleLocation.getNdistrictcode() + " , ncitycode=" + objSampleLocation.getNcitycode()
						+ " , nvillagecode=" + objSampleLocation.getNvillagecode()
						+ " , ncentralsitecode=" +  objSampleLocation.getNcentralsitecode()
						+ " , ssamplelocationname='"+ stringUtilityFunction.replaceQuote(objSampleLocation.getSsamplelocationname())
						// added and modified by sujatha swsm-5
						+ "', slatitude='" + stringUtilityFunction.replaceQuote(objSampleLocation.getSlatitude())
						+ "', slongitude='" + stringUtilityFunction.replaceQuote(objSampleLocation.getSlongitude())
						+ "', sdescription='" + stringUtilityFunction.replaceQuote(objSampleLocation.getSdescription())
						+ "', dmodifieddate ='" + dateUtilityFunction.getCurrentDateTime(userInfo)
						+ "' where nsamplelocationcode=" + objSampleLocation.getNsamplelocationcode()
						+ " and nsitecode= " + userInfo.getNmastersitecode();
				jdbcTemplate.execute(updateQueryString);
				
				//added by sujatha v ATE_274 SWSM-5 on 21-09-2025 for getting the name of the site from the site table for audit trail
				String centralSiteName = getSiteNameForAudit(sampleLocation.getNcentralsitecode(), userInfo);
				String regionName = getSiteNameForAudit(sampleLocation.getNregioncode(), userInfo);
				String districtName = getSiteNameForAudit(sampleLocation.getNdistrictcode(), userInfo);
				String cityName = getSiteNameForAudit(sampleLocation.getNcitycode(), userInfo);
//				String villageName = getSiteNameForAudit(sampleLocation.getNvillagecode(), userInfo);
				sampleLocation.setScentralsitename(centralSiteName);
				sampleLocation.setSregionname(regionName);
				sampleLocation.setSdistrictname(districtName);
				sampleLocation.setScityname(cityName);
//				sampleLocation.setSvillagename(villageName);

				String centralSiteName1 = getSiteNameForAudit(objSampleLocation.getNcentralsitecode(), userInfo);
				String regionName1 = getSiteNameForAudit(objSampleLocation.getNregioncode(), userInfo);
				String districtName1 = getSiteNameForAudit(objSampleLocation.getNdistrictcode(), userInfo);
				String cityName1 = getSiteNameForAudit(objSampleLocation.getNcitycode(), userInfo);
//				String villageName1 = getSiteNameForAudit(objSampleLocation.getNvillagecode(), userInfo);
				objSampleLocation.setScentralsitename(centralSiteName1);
				objSampleLocation.setSregionname(regionName1);
				objSampleLocation.setSdistrictname(districtName1);
				objSampleLocation.setScityname(cityName1);
//				objSampleLocation.setSvillagename(villageName1);
				final List<String> multilingualIDList = new ArrayList<>();
				multilingualIDList.add("IDS_EDITSAMPLELOCATION");
				final List<Object> listAfterSave = new ArrayList<>();
				listAfterSave.add(objSampleLocation);
				final List<Object> listBeforeSave = new ArrayList<>();
				listBeforeSave.add(sampleLocation);
				auditUtilityFunction.fnInsertAuditAction(listAfterSave, 2, listBeforeSave, multilingualIDList,
						userInfo);
				return getSampleLocation(userInfo);
			} else {

				if (!sampleLocationList.isEmpty()) {
					alert = commonFunction.getMultilingualMessage("IDS_SAMPLELOCATION",
							userInfo.getSlanguagefilename());
				}
				return new ResponseEntity<>(alert + " " + commonFunction.getMultilingualMessage(
						Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(), userInfo.getSlanguagefilename()),
						HttpStatus.CONFLICT);
			}
		}
	}

	/**
	 * This method id used to delete an entry in SampleLocation table Need to check
	 * the record is already deleted
	 * 
	 * @param objSampleLocation [SampleLocation] an Object holds the record to be
	 *                          deleted
	 * @param userInfo          [UserInfo] holding logged in user details based on
	 *                          which the list is to be fetched
	 * @return a response entity with list of available samplelocation objects
	 * @exception Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> deleteSampleLocation(SampleLocation objSampleLocation, UserInfo userInfo)
			throws Exception {
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> savedSampleLocationList = new ArrayList<>();
		final SampleLocation sampleLocation = getActiveSampleLocationById(objSampleLocation.getNsamplelocationcode());
		if (sampleLocation == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			//added by sujatha v ATE-274 27-09-2025 for sample scheduling delete validation
			final String query = " Select 'IDS_SAMPLESCHEDULINGPLAN' as Msg from sampleschedulinglocation where nsamplelocationcode= "
					+ objSampleLocation.getNsamplelocationcode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() ;
			validatorDel = projectDAOSupport.getTransactionInfo(query, userInfo);
			boolean validRecord = false;
			if (validatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
				validRecord = true;
				validatorDel = projectDAOSupport
						.validateDeleteRecord(Integer.toString(objSampleLocation.getNsamplelocationcode()), userInfo);
				if (validatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
					validRecord = true;
				} else {
					validRecord = false;
				}
			}
			if (validRecord) {
			final String updateQueryString = "update samplelocation set nstatus = "
					+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ", dmodifieddate ='"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where nsamplelocationcode= "
					+ objSampleLocation.getNsamplelocationcode() + " and nsitecode=" + userInfo.getNmastersitecode();
			jdbcTemplate.execute(updateQueryString);
			
			//added by sujatha v ATE_274 SWSM-5 on 21-09-2025 for getting site name from site table for audit trial
			String centralSiteName = getSiteNameForAudit(objSampleLocation.getNcentralsitecode(), userInfo);
			String regionName = getSiteNameForAudit(objSampleLocation.getNregioncode(), userInfo);
			String districtName = getSiteNameForAudit(objSampleLocation.getNdistrictcode(), userInfo);
			String cityName = getSiteNameForAudit(objSampleLocation.getNcitycode(), userInfo);
//			String villageName = getSiteNameForAudit(objSampleLocation.getNvillagecode(), userInfo);
			objSampleLocation.setScentralsitename(centralSiteName);
			objSampleLocation.setSregionname(regionName);
			objSampleLocation.setSdistrictname(districtName);
			objSampleLocation.setScityname(cityName);
//			objSampleLocation.setSvillagename(villageName);
			objSampleLocation.setNstatus((short) Enumeration.TransactionStatus.DELETED.gettransactionstatus());
			savedSampleLocationList.add(objSampleLocation);
			multilingualIDList.add("IDS_DELETESAMPLELOCATION");
			auditUtilityFunction.fnInsertAuditAction(savedSampleLocationList, 1, null, multilingualIDList, userInfo);
			return getSampleLocation(userInfo);
		} else {
			return new ResponseEntity<>(validatorDel.getSreturnmessage(), HttpStatus.EXPECTATION_FAILED);
		}
		}
	}

	/**
	 * This method is used to retrieve list of all available region's based on the approved site hierarchy configuration
	 * and get as cuuren, parent and child site 
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return response entity object holding response status and list of all active
	 *           current, parents site and child site based on site hierarchy configuration
	 * @throws Exception that are thrown from this DAO layer
	 */
	//commented by sujatha v ATE_274 SWSM-5 on 21-09-2025 for samplelocation(based on site hierarchy configuration)
//	@Override
//	public ResponseEntity<Object> getRegion(UserInfo userInfo) throws Exception {
//		String strQuery = "select *, to_char(dmodifieddate, '" + userInfo.getSpgsitedatetime().replace("'T'", " ")
//				+ "') as smodifieddate from region " + " where nstatus = "
//				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nregioncode>0  and nsitecode="
//				+ userInfo.getNmastersitecode()+";";
//		LOGGER.info("Get Method:" + strQuery);
//		return new ResponseEntity<Object>(jdbcTemplate.query(strQuery, new Region()), HttpStatus.OK);
//	}

//	@Override
//	public ResponseEntity<Object> getRegion(final UserInfo userInfo) throws Exception {
//		String strCurrent = "WITH current_site AS ( " + " SELECT s.nsitecode, s.ssitename, 'Current' AS srelation "
//				+ " FROM site s " + " WHERE s.nsitecode = " + userInfo.getNtranssitecode() + "), "
//				+ "json_expanded AS ( " + " SELECT DISTINCT (elem->>'nsitecode')::int AS nsitecode, "
//				+ " elem->>'ssitetypename' AS ssitetypename " + " FROM sitehierarchyconfig sc, "
//				+ " jsonb_path_query(sc.jsondata, '$.**.item.selectedNodeDetail') elem "
//				+ " WHERE sc.ntransactionstatus = " + Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
//				+ ") " + "SELECT c.nsitecode, c.ssitename, c.srelation, j.ssitetypename " + "FROM current_site c "
//				+ "JOIN json_expanded j ON j.nsitecode = c.nsitecode";
//
//		final List<SampleLocation> currentList = (List<SampleLocation>) jdbcTemplate.query(strCurrent,
//				new SampleLocation());
//
//		String strParent = "WITH RECURSIVE site_hierarchy AS ( " + "    SELECT "
//				+ " (sc.jsondata->'item'->'selectedNodeDetail'->>'nsitecode')::int AS nsitecode, "
//				+ "  sc.jsondata->'item'->'selectedNodeDetail'->>'ssitename' AS ssitename, "
//				+ "  sc.jsondata->'item'->'selectedNodeDetail'->>'ssitetypename' AS ssitetypename, "
//				+ " (sc.jsondata->>'parentKey')::int AS parentkey, " + " sc.jsondata->'nodes' AS children "
//				+ "    FROM sitehierarchyconfig sc " + "  WHERE sc.ntransactionstatus = "
//				+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + "  " + " UNION ALL " + " "
//				+ " SELECT " + "  (child->'item'->'selectedNodeDetail'->>'nsitecode')::int AS nsitecode, "
//				+ "  child->'item'->'selectedNodeDetail'->>'ssitename' AS ssitename, "
//				+ "  child->'item'->'selectedNodeDetail'->>'ssitetypename' AS ssitetypename, "
//				+ " (child->>'parentKey')::int AS parentkey, " + "        child->'nodes' AS children "
//				+ " FROM site_hierarchy sh, " + " jsonb_array_elements(sh.children) AS child " + "), " + "parents AS ( "
//				+ "    SELECT sh.* " + "    FROM site_hierarchy sh " + "    WHERE sh.nsitecode = "
//				+ userInfo.getNtranssitecode() + " " + "    UNION ALL " + "    SELECT sh.* "
//				+ "    FROM site_hierarchy sh " + "    JOIN parents p ON sh.nsitecode = p.parentkey " + ") " + "SELECT "
//				+ "    nsitecode, " + "    split_part(ssitename, '(', 1) AS ssitename, " + "    ssitetypename, "
//				+ "    parentkey " + "FROM parents " + "WHERE nsitecode != " + userInfo.getNtranssitecode() + " "
//				+ "ORDER BY nsitecode;";
//
//		final List<SampleLocation> parentList = (List<SampleLocation>) jdbcTemplate.query(strParent,
//				new SampleLocation());
//
//		String strChild = "WITH RECURSIVE site_hierarchy AS ( " + "    SELECT "
//				+ "elem->'item'->'selectedNodeDetail'->>'nsitecode' AS nsitecode, "
//				+ "elem->'item'->'selectedNodeDetail'->>'ssitename' AS ssitename, "
//				+ "elem->'item'->'selectedNodeDetail'->>'ssitetypename' AS ssitetypename, "
//				+ " elem->>'parentKey' AS parentkey, " + " elem->'nodes' AS children "
//				+ " FROM sitehierarchyconfig sc, " + "jsonb_array_elements(sc.jsondata->'nodes') AS elem "
//				+ " WHERE sc.ntransactionstatus =  " + Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
//				+ " " + " UNION ALL " + " " + " SELECT "
//				+ " child->'item'->'selectedNodeDetail'->>'nsitecode' AS nsitecode, "
//				+ " child->'item'->'selectedNodeDetail'->>'ssitename' AS ssitename, "
//				+ " child->'item'->'selectedNodeDetail'->>'ssitetypename' AS ssitetypename, "
//				+ " child->>'parentKey' AS parentkey, " + "        child->'nodes' AS children "
//				+ " FROM site_hierarchy sh, " + "  jsonb_array_elements(sh.children) AS child " + ") " + "SELECT "
//				+ " nsitecode::int, " + " split_part(ssitename, '(', 1) AS ssitename, " + " ssitetypename, "
//				+ "  parentkey::int " + "FROM site_hierarchy " + "WHERE parentkey = '" + userInfo.getNtranssitecode()
//				+ "'" + "   OR parentkey IN ( " + " SELECT nsitecode " + " FROM site_hierarchy "
//				+ " WHERE parentkey = '" + userInfo.getNtranssitecode() + "'" + "   ) " + "ORDER BY nsitecode::int;";
//
//		final List<SampleLocation> childList = (List<SampleLocation>) jdbcTemplate.query(strChild,
//				new SampleLocation());
//
//		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
//		if (!currentList.isEmpty()) {
//			outputMap.put("currentList", currentList);
//			outputMap.put("parentList", parentList);
//			outputMap.put("childList", childList);
//
//		} else {
//			outputMap.put("currentList", null);
//		}
//		return new ResponseEntity<Object>(outputMap, HttpStatus.OK);
//	}
	
	//added by sujatha v ATE_274 SWSM-5 on 21-09-2025 for samplelocation(based on site hierarchy configuration)
	@Override
	public ResponseEntity<Object> getRegion(final UserInfo userInfo) throws Exception {
 
		String configFilter = "WITH RECURSIVE selected_config AS ( " + " SELECT * FROM sitehierarchyconfig sc "
				+ " WHERE sc.ntransactionstatus = " + Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
				+ "   AND jsonb_path_exists(sc.jsondata, '$.** ? (@.item.selectedNodeDetail.nsitecode == "
				+ userInfo.getNtranssitecode() + ")') " + " ORDER BY sc.dmodifieddate DESC LIMIT 1 " + " ) ";
 
		//commented by sujatha ATE_274 to use group by clause in the same query as shown below
//		String strCurrent = configFilter + " , current_site AS ( "
//				+ " SELECT s.nsitecode, s.ssitename, 'Current' AS srelation " + " FROM site s "
//				+ " WHERE s.nsitecode = " + userInfo.getNtranssitecode() + " ) " + " , json_expanded AS ( "
//				+ " SELECT DISTINCT (elem->>'nsitecode')::int AS nsitecode, "
//				+ " elem->>'ssitetypename' AS ssitetypename, " + " (elem->>'nhierarchicalorderno')::int AS nlevel, "
//				+ " sc.nsitehierarchyconfigcode FROM selected_config sc, " + " jsonb_path_query(sc.jsondata, '$.**.item.selectedNodeDetail') elem "
//				+ " ) " + " SELECT c.nsitecode, c.ssitename, c.srelation, j.ssitetypename, j.nlevel, j.nsitehierarchyconfigcode "
//				+ " FROM current_site c " + " JOIN json_expanded j ON j.nsitecode = c.nsitecode ";
		
		//modified by sujatha ATE_274 for changing the above query from distinct to group by clause
		String strCurrent = configFilter + " , current_site AS ( " + " SELECT s.nsitecode, s.ssitename, 'Current' AS srelation " 
					+ " FROM site s " + " WHERE s.nsitecode = " + userInfo.getNtranssitecode() +" ), json_expanded AS ( " 
					+ " SELECT (elem->>'nsitecode')::int AS nsitecode, elem->>'ssitetypename' AS ssitetypename, " 
					+ " (elem->>'nhierarchicalorderno')::int AS nlevel, sc.nsitehierarchyconfigcode FROM selected_config sc, " 
					+ " jsonb_path_query(sc.jsondata, '$.**.item.selectedNodeDetail') elem " 
					+ " GROUP BY  (elem->>'nsitecode')::int,  elem->>'ssitetypename',  (elem->>'nhierarchicalorderno')::int, " 
					+ " sc.nsitehierarchyconfigcode ) SELECT c.nsitecode, c.ssitename, c.srelation, " 
					+ " j.ssitetypename, j.nlevel, j.nsitehierarchyconfigcode FROM current_site c " 
					+ " JOIN json_expanded j ON j.nsitecode = c.nsitecode ";

 
		final List<SampleLocation> currentList = jdbcTemplate.query(strCurrent, new SampleLocation());
		String strParent = configFilter + " , site_hierarchy AS ( " + "    SELECT "
				+ " (sc.jsondata->'item'->'selectedNodeDetail'->>'nsitecode')::int AS nsitecode, "
				+ " sc.jsondata->'item'->'selectedNodeDetail'->>'ssitename' AS ssitename, "
				+ " sc.jsondata->'item'->'selectedNodeDetail'->>'ssitetypename' AS ssitetypename, "
				+ " (sc.jsondata->'item'->'selectedNodeDetail'->>'nhierarchicalorderno')::int AS nlevel, "
				+ " (sc.jsondata->>'parentKey')::int AS parentkey, " + " sc.jsondata->'nodes' AS children "
				+ "    FROM selected_config sc " + " UNION ALL " + " SELECT "
				+ " (child->'item'->'selectedNodeDetail'->>'nsitecode')::int AS nsitecode, "
				+ " child->'item'->'selectedNodeDetail'->>'ssitename' AS ssitename, "
				+ " child->'item'->'selectedNodeDetail'->>'ssitetypename' AS ssitetypename, "
				+ " (child->'item'->'selectedNodeDetail'->>'nhierarchicalorderno')::int AS nlevel, "
				+ " (child->>'parentKey')::int AS parentkey, " + " child->'nodes' AS children "
				+ " FROM site_hierarchy sh, " + " jsonb_array_elements(sh.children) AS child " + " ) , parents AS ( "
				+ "    SELECT sh.* " + "    FROM site_hierarchy sh " + "    WHERE sh.nsitecode = "
				+ userInfo.getNtranssitecode() + "    UNION ALL " + "    SELECT sh.* " + "    FROM site_hierarchy sh "
				+ "    JOIN parents p ON sh.nsitecode = p.parentkey " + " ) SELECT " + " nsitecode, "
				+ " split_part(ssitename, '(', 1) AS ssitename, " + " ssitetypename, " + " parentkey, " + " nlevel "
				+ " FROM parents " + " WHERE nsitecode != " + userInfo.getNtranssitecode() + " ORDER BY nsitecode ";
 
		final List<SampleLocation> parentList = jdbcTemplate.query(strParent, new SampleLocation());
 
		// Child sites query
		String strChild = configFilter + " , site_hierarchy AS ( " + "    SELECT "
				+ " elem->'item'->'selectedNodeDetail'->>'nsitecode' AS nsitecode, "
				+ " elem->'item'->'selectedNodeDetail'->>'ssitename' AS ssitename, "
				+ " elem->'item'->'selectedNodeDetail'->>'ssitetypename' AS ssitetypename, "
				+ " (elem->'item'->'selectedNodeDetail'->>'nhierarchicalorderno')::int AS nlevel, "
				+ " elem->>'parentKey' AS parentkey, " + " elem->'nodes' AS children, sc.nsitehierarchyconfigcode " 
				+ " FROM selected_config sc, "
				+ " jsonb_array_elements(sc.jsondata->'nodes') AS elem " + " UNION ALL " + " SELECT "
				+ " child->'item'->'selectedNodeDetail'->>'nsitecode' AS nsitecode, "
				+ " child->'item'->'selectedNodeDetail'->>'ssitename' AS ssitename, "
				+ " child->'item'->'selectedNodeDetail'->>'ssitetypename' AS ssitetypename, "
				+ " (child->'item'->'selectedNodeDetail'->>'nhierarchicalorderno')::int AS nlevel, "
				+ " child->>'parentKey' AS parentkey, " + " child->'nodes' AS children, sh.nsitehierarchyconfigcode " 
				+ " FROM site_hierarchy sh, "
				+ " jsonb_array_elements(sh.children) AS child " + " ) SELECT " + " nsitecode::int, "
				+ " split_part(ssitename, '(', 1) AS ssitename, " + " ssitetypename, " + " parentkey::int, "
				+ " nlevel, nsitehierarchyconfigcode " + " FROM site_hierarchy " + " WHERE parentkey::int = " 
				+ userInfo.getNtranssitecode()
				+ "   OR parentkey::int IN ( SELECT nsitecode::int FROM site_hierarchy WHERE parentkey::int = "
				+ userInfo.getNtranssitecode() + " ) " + " ORDER BY nsitecode::int ";
 
		final List<SampleLocation> childList = jdbcTemplate.query(strChild, new SampleLocation());
 
		final Map<String, Object> outputMap = new LinkedHashMap<>();
		if (!currentList.isEmpty()) {
			outputMap.put("currentList", currentList);
			outputMap.put("parentList", parentList);
			outputMap.put("childList", childList);
		} else {
			outputMap.put("currentList", null);
		}
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

//	@Override
//	public ResponseEntity<Object> getVillage(final int nprimarykey, final UserInfo userInfo) throws Exception {
//
//		String strVillage = "WITH RECURSIVE site_hierarchy AS ( " + " SELECT "
//				+ " (elem->'item'->'selectedNodeDetail'->>'nsitecode')::int AS nsitecode, "
//				+ " elem->'item'->'selectedNodeDetail'->>'ssitename' AS ssitename, "
//				+ " elem->'item'->'selectedNodeDetail'->>'ssitetypename' AS ssitetypename, "
//				+ " (elem->>'parentKey')::int AS parentkey, " + " elem->'nodes' AS children "
//				+ " FROM sitehierarchyconfig sc, jsonb_array_elements(sc.jsondata->'nodes') AS elem "
//				+ " WHERE sc.ntransactionstatus = " + Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
//				+ " UNION ALL " + " SELECT "
//				+ " (child->'item'->'selectedNodeDetail'->>'nsitecode')::int AS nsitecode, "
//				+ " child->'item'->'selectedNodeDetail'->>'ssitename' AS ssitename, "
//				+ " child->'item'->'selectedNodeDetail'->>'ssitetypename' AS ssitetypename, "
//				+ " (child->>'parentKey')::int AS parentkey, " + " child->'nodes' AS children "
//				+ " FROM site_hierarchy sh, jsonb_array_elements(sh.children) AS child " + ") "
//				+ "SELECT nsitecode, split_part(ssitename, '(', 1) AS ssitename, ssitetypename, parentkey "
//				+ "FROM site_hierarchy " + "WHERE parentkey = " + nprimarykey + " ORDER BY nsitecode;";
//
//		final List<SampleLocation> villageList = (List<SampleLocation>) jdbcTemplate.query(strVillage,
//				new SampleLocation());
//
//		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
//		if (!villageList.isEmpty()) {
//			outputMap.put("villageList", villageList);
//		} else {
//			outputMap.put("villageList", null);
//		}
//		return new ResponseEntity<Object>(outputMap, HttpStatus.OK);
//	}

	/**
	 * This method is used to retrieve list of all available taluks based on
	 * selected district which is from the approved site hierarchy configuration 
	 * 
	 * @ndistrictCode  holding  selected taluka details
	 * @param userInfo             [UserInfo] holding logged in user details and
	 *                             nmasterSiteCode [int] primary key of site object
	 *                             for which the list is to be fetched
	 * @return response entity object holding response status and list of all active
	 *         district's for the selected region
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> getTaluka(final int ndistrictCode, final UserInfo userInfo) throws Exception {

		String strTaluka = "WITH RECURSIVE site_hierarchy AS ( " + " SELECT "
				+ " (elem->'item'->'selectedNodeDetail'->>'nsitecode')::int AS nsitecode, "
				+ " elem->'item'->'selectedNodeDetail'->>'ssitename' AS ssitename, "
				+ " elem->'item'->'selectedNodeDetail'->>'ssitetypename' AS ssitetypename, "
				+ " (elem->>'parentKey')::int AS parentkey, " + " elem->'nodes' AS children, sc.nsitehierarchyconfigcode "
				+ " FROM sitehierarchyconfig sc, jsonb_array_elements(sc.jsondata->'nodes') AS elem "
				+ " WHERE sc.ntransactionstatus = " + Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
				+ " UNION ALL " + " SELECT "
				+ " (child->'item'->'selectedNodeDetail'->>'nsitecode')::int AS nsitecode, "
				+ " child->'item'->'selectedNodeDetail'->>'ssitename' AS ssitename, "
				+ " child->'item'->'selectedNodeDetail'->>'ssitetypename' AS ssitetypename, "
				+ " (child->>'parentKey')::int AS parentkey, " + " child->'nodes' AS children, sh.nsitehierarchyconfigcode "
				+ " FROM site_hierarchy sh, jsonb_array_elements(sh.children) AS child " + ") "
				+ "SELECT nsitecode, split_part(ssitename, '(', 1) AS ssitename, ssitetypename, parentkey, nsitehierarchyconfigcode "
				+ "FROM site_hierarchy " + "WHERE parentkey = " + ndistrictCode + " ORDER BY nsitecode;";

		final List<SampleLocation> talukaList = (List<SampleLocation>) jdbcTemplate.query(strTaluka,
				new SampleLocation());

		final Map<String, Object> outputMap = new LinkedHashMap<>();
		if (!talukaList.isEmpty()) {
			outputMap.put("talukaList", talukaList);
		} else {
			outputMap.put("talukaList", null);
		}
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	/**
	 * This method is used to retrieve list of all available district's based on
	 * selected region 
	 * 
	 * @ndistrictCode   holding the selected region 
	 * @param userInfo             [UserInfo] holding logged in user details and
	 *                             nmasterSiteCode [int] primary key of site object
	 *                             for which the list is to be fetched
	 * @return response entity object holding response status and list of all active
	 *         district's for the selected region
	 * @throws Exception that are thrown from this DAO layer
	 */
//	@Override
//	public ResponseEntity<Object> getDistrict(final Map<String, Object> inputMap, final UserInfo userInfo) throws Exception {
//		String strQuery = "select d.ndistrictcode, d.sdistrictname, d.sdistrictcode, d.nsitecode,d.nstatus, d.nregioncode, r.sregionname,"
//				+ " to_char(d.dmodifieddate, '" + userInfo.getSpgsitedatetime()
//				+ "') as smodifieddate from district d, region r" + " where d.nstatus = "
//				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and r.nstatus = "
//				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//				+ " and ndistrictcode>0 and d.nsitecode=r.nsitecode and d.nsitecode=" + userInfo.getNmastersitecode()
//				+ " and d.nregioncode = r.nregioncode and d.nregioncode="+inputMap.get("nregioncode")+"";
//		LOGGER.info("Get Method:"+ strQuery);
//		return new ResponseEntity<Object>(jdbcTemplate.query(strQuery, new District()), HttpStatus.OK);
//	}

	@Override
	public ResponseEntity<Object> getDistrict(final int ndistrictCode, final UserInfo userInfo) throws Exception {

		String strTaluka = "WITH RECURSIVE site_hierarchy AS ( " + " SELECT "
				+ " (elem->'item'->'selectedNodeDetail'->>'nsitecode')::int AS nsitecode, "
				+ " elem->'item'->'selectedNodeDetail'->>'ssitename' AS ssitename, "
				+ " elem->'item'->'selectedNodeDetail'->>'ssitetypename' AS ssitetypename, "
				+ " (elem->>'parentKey')::int AS parentkey, " + " elem->'nodes' AS children "
				+ " FROM sitehierarchyconfig sc, jsonb_array_elements(sc.jsondata->'nodes') AS elem "
				+ " WHERE sc.ntransactionstatus = " + Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
				+ " UNION ALL " + " SELECT "
				+ " (child->'item'->'selectedNodeDetail'->>'nsitecode')::int AS nsitecode, "
				+ " child->'item'->'selectedNodeDetail'->>'ssitename' AS ssitename, "
				+ " child->'item'->'selectedNodeDetail'->>'ssitetypename' AS ssitetypename, "
				+ " (child->>'parentKey')::int AS parentkey, " + " child->'nodes' AS children "
				+ " FROM site_hierarchy sh, jsonb_array_elements(sh.children) AS child " + ") "
				+ "SELECT nsitecode, split_part(ssitename, '(', 1) AS ssitename, ssitetypename, parentkey "
				+ "FROM site_hierarchy " + "WHERE parentkey = " + ndistrictCode + " ORDER BY nsitecode;";

		final List<SampleLocation> talukaList = (List<SampleLocation>) jdbcTemplate.query(strTaluka,
				new SampleLocation());

		final Map<String, Object> outputMap = new LinkedHashMap<>();
		if (!talukaList.isEmpty()) {
			outputMap.put("talukaList", talukaList);
		} else {
			outputMap.put("talukaList", null);
		}
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}
	/**
	 * This method is used to retrieve list of all available taluk's based on
	 * selected district
	 * 
	 * @param [Map<String,Object>] holding the selected district details
	 * @param userInfo             [UserInfo] holding logged in user details and
	 *                             nmasterSiteCode [int] primary key of site object
	 *                             for which the list is to be fetched
	 * @return response entity object holding response status and list of all active
	 *         taluk's for the selected district
	 * @throws Exception that are thrown from this DAO layer
	 */
	//added by sujatha v ATE_274 SWSM-5 on 21-09-2025 for samplelocation(based on site hierarchy configuration)
//	@Override
//	public ResponseEntity<Object> getCity(final Map<String, Object> inputMap, final UserInfo userInfo) throws Exception {
//		LOGGER.info("getCity");
//		final String strCity = "select c.ncitycode, c.scityname, c.scitycode, c.nsitecode, c.nstatus ,d.sdistrictname ,c.ndistrictcode,"
//				+ "to_char(c.dmodifieddate, '" + userInfo.getSpgsitedatetime().replace("'T'", " ")
//				+ "') as smodifieddate from city c,district d where d.ndistrictcode=c.ndistrictcode "
//				+ "and c.ndistrictcode="+ inputMap.get("ndistrictcode")
//				+ " and d.nstatus ="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and c.nstatus="
//				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and c.ncitycode>0 and "
//				+ " c.nsitecode = " + userInfo.getNmastersitecode();
//		return new ResponseEntity<>(jdbcTemplate.query(strCity, new City()), HttpStatus.OK);
//	}

	/**
	 * This method is used to retrieve list of all available village's based on
	 * selected taluk
	 * 
	 * @param [Map<String,Object>] holding the selected taluk details
	 * @param userInfo             [UserInfo] holding logged in user details and
	 *                             nmasterSiteCode [int] primary key of site object
	 *                             for which the list is to be fetched
	 * @return response entity object holding response status and list of all active
	 *         villages for the selected taluk
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> getVillage(final Map<String, Object> inputMap, final UserInfo userInfo) throws Exception {
		LOGGER.info("getVillage");
//		final String strVillage = " select v.nvillagecode, v.svillagename, v.nsitecode, v.nstatus ,c.scityname ,"
//									+ "v.ncitycode," + "to_char(v.dmodifieddate, '" + userInfo.getSpgsitedatetime().replace("'T'", " ")
//									+ "') as smodifieddate from villages v, city c where c.ncitycode=v.ncitycode "
//									+ "and v.ncitycode="+ inputMap.get("ncitycode")
//									+ " and c.nstatus ="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
//									+ " and v.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
//									+ " and v.nvillagecode>0 and "
//									+ " v.nsitecode = " + userInfo.getNmastersitecode();
		
//		final String strVillage=" select v.nvillagecode, v.svillagename from villages v, city c"
//								+ " where scityname= '"+inputMap.get("scityname")
//								+ "' and c.ncitycode=v.ncitycode"
//								+ " and v.nsitecode="+userInfo.getNmastersitecode()+" and v.nstatus="
//								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		//added by sujatha ATE_274 for getting village based on primarykey of selected taluka
		final String strVillage=" select v.nvillagecode, v.svillagename from villages v"
								+ " where v.nnodesitecode="+inputMap.get("primarykey")+" and nsitehierarchyconfigcode="
								+ inputMap.get("nsitehierarchyconfigcode")
								+ " and v.nsitecode="+userInfo.getNmastersitecode()+" and v.nstatus="
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		
		final List<SampleLocation> villList = (List<SampleLocation>) jdbcTemplate.query(strVillage,
				new SampleLocation());

		final Map<String, Object> outputMap = new LinkedHashMap<>();
		if (!villList.isEmpty()) {
			outputMap.put("villageList", villList);
		} else {
			outputMap.put("villageList", null);
		}
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
//		return new ResponseEntity<>(jdbcTemplate.query(strVillage, new Villages()), HttpStatus.OK);
	}

	//added by sujatha ATE_274 for getting name of site's for audit trial from site table
	private String getSiteNameForAudit(final int siteCode, final UserInfo userInfo) throws Exception {
		String siteName = "";
		String query = "SELECT ssitename FROM site WHERE nsitecode = " + siteCode + " AND nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " AND nmastersitecode = "
				+ userInfo.getNmastersitecode();

		final Site site = jdbcTemplate.queryForObject(query, new Site());
		if (site != null) {
			siteName = site.getSsitename();
		}
		return siteName;
	}
}
