package com.agaramtech.qualis.submitter.service.villages;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import com.agaramtech.qualis.configuration.model.SiteHierarchyConfig;
import com.agaramtech.qualis.global.AuditUtilityFunction;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.ProjectDAOSupport;
import com.agaramtech.qualis.global.StringUtilityFunction;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.global.ValidatorDel;
import com.agaramtech.qualis.submitter.model.Villages;

import lombok.AllArgsConstructor;

/**
 * This class is used to perform CRUD Operation on "villages" table by implementing
 * methods from its interface.
 */
/**
 * @author sujatha.v SWSM-4 22/07/2025
 */
@AllArgsConstructor
@Repository
public class VillagesDAOImpl implements VillagesDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(VillagesDAOImpl.class);

	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;
	private final ProjectDAOSupport projectDAOSupport;
	private ValidatorDel validatorDel;

	/**
	 * This method is used to retrieve list of all available villages for the
	 * specified site.
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return response entity object holding response status and list of all active
	 *         villages
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> getVillage(UserInfo userInfo) throws Exception {
		LOGGER.info("getVillage");
		// added new query and commented the old by sujatha ATE_274 SWSM-4 19-04-2025 for getting the village based on nsitehierarchyconfigcode
		// by joining sitehierarchyconfig table -start
//		final String strVillage = "select v.nvillagecode, v.svillagename, v.svillagecode, v.nsitecode, v.nstatus ,c.scityname ,"
//				+ "v.ncitycode," + "to_char(v.dmodifieddate, '" + userInfo.getSpgsitedatetime().replace("'T'", " ")
//				+ "') as smodifieddate from villages v, city c where c.ncitycode=v.ncitycode " + " and c.nstatus ="
//				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and v.nstatus="
//				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and v.nvillagecode>0 and "
//				+ " v.nsitecode = " + userInfo.getNmastersitecode();
		
//		final String strVillage="WITH RECURSIVE latest_sitetype AS ( SELECT nsitetypecode FROM sitetype ORDER BY "
//				+ " nsitetypecode DESC LIMIT 1 ), sitecodes_for_latest_type AS ( SELECT nsitecode FROM siteconfig "
//				+ " WHERE nsitetypecode = (SELECT nsitetypecode FROM latest_sitetype)),"
//				+ " site_base AS (SELECT sd.nnodesitecode, sc.jsondata, sc.sconfigname, sc.ntransactionstatus,"
//				+ " sc.nsitehierarchyconfigcode, s.ssitename FROM site s JOIN sitehierarchyconfigdetails sd "
//				+ " ON s.nsitecode = sd.nnodesitecode JOIN sitehierarchyconfig sc ON "
//				+ " sd.nsitehierarchyconfigcode = sc.nsitehierarchyconfigcode WHERE s.nsitecode "
//				+ " IN (SELECT nsitecode FROM sitecodes_for_latest_type) and sd.nnodesitecode "
//				+ " IN (SELECT nsitecode FROM sitecodes_for_latest_type)  AND sd.nstatus ="
//				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" AND sc.nstatus ="
//				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" AND sc.ntransactionstatus =31), "
//				+ " json_nodes AS ( SELECT sb.nnodesitecode, jsonb_array_elements(sb.jsondata -> 'nodes') AS node,"
//				+ " sb.nsitehierarchyconfigcode FROM site_base sb UNION ALL  SELECT j.nnodesitecode,"
//				+ " jsonb_array_elements(j.node -> 'nodes'), j.nsitehierarchyconfigcode FROM json_nodes j WHERE j.node ? 'nodes')"
//				+ " SELECT DISTINCT sb.nnodesitecode, n.node -> 'item' -> 'selectedNodeDetail' ->> 'ssiteconfigname' AS siteconfigname,"
//				+ " sb.ntransactionstatus, v.* FROM site_base sb JOIN json_nodes n "
//				+ " ON sb.nnodesitecode = (n.node -> 'item' -> 'selectedNodeDetail' ->> 'nsitecode')::int"
//				+ " JOIN villages v on v.nnodesitecode=sb.nnodesitecode AND sb.nsitehierarchyconfigcode = n.nsitehierarchyconfigcode"
//				+ " AND v.nsitehierarchyconfigcode=sb.nsitehierarchyconfigcode "
//				+ " AND v.nstatus=1 and v.nsitecode="+userInfo.getNmastersitecode()+";";
		//end
		// added by sujatha ATE_274 25-09-2025 because of an issue with taking latest site type, if we add more no. of record in site type we can't get taluk
		final String strVillage = "WITH RECURSIVE json_tree AS ("
		        + " SELECT sc.nsitehierarchyconfigcode, sc.sconfigname, sc.ntransactionstatus,"
		        + " jsonb_array_elements(sc.jsondata -> 'nodes') AS node" + " FROM sitehierarchyconfig sc"
		        + " WHERE sc.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
		        + " AND sc.ntransactionstatus ="+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
		        + " UNION ALL SELECT jt.nsitehierarchyconfigcode, jt.sconfigname, jt.ntransactionstatus,"
		        + " jsonb_array_elements(jt.node -> 'nodes') AS node"
		        + " FROM json_tree jt WHERE (jt.node -> 'nodes') IS NOT NULL), "
		        + " parsed_nodes AS ( SELECT nsitehierarchyconfigcode, sconfigname, ntransactionstatus,"
		        + " (node -> 'item' -> 'selectedNodeDetail' ->> 'nsitecode')::int AS nsitecode, node FROM json_tree)"
		        + " SELECT DISTINCT d.nnodesitecode,"
		        + " pn.node -> 'item' -> 'selectedNodeDetail' ->> 'ssiteconfigname' AS siteconfigname,"
		        + " pn.ntransactionstatus, v.*"
		        + " FROM sitehierarchyconfigdetails d"
		        + " JOIN parsed_nodes pn ON d.nnodesitecode = pn.nsitecode"
		        + " AND d.nsitehierarchyconfigcode = pn.nsitehierarchyconfigcode"
		        + " JOIN villages v ON v.nnodesitecode = d.nnodesitecode"
		        + " AND v.nsitehierarchyconfigcode = d.nsitehierarchyconfigcode"
		        + " WHERE d.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
		        + " AND v.nstatus ="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//		        + " AND d.nsitehierarchyconfigcode = v.nsitehierarchyconfigcode"  
		        + " AND v.nsitecode = " + userInfo.getNmastersitecode()
		        + " ORDER BY v.nvillagecode desc;";

		return new ResponseEntity<>(jdbcTemplate.query(strVillage, new Villages()), HttpStatus.OK);
	}

	// added by sujatha ATE_274 SWSM-4 19-05-2025 for getting the taluka from the site hierarchy config screen instead of city table how we used
	@Override
	public ResponseEntity<Object> getTaluka(UserInfo userInfo) throws Exception {

//		final String strTaluk = " WITH RECURSIVE "
//				+ " latest_sitetype AS ( SELECT nsitetypecode FROM sitetype ORDER BY nsitetypecode DESC LIMIT 1),"
//				+ " sitecodes_for_latest_type AS ( SELECT nsitecode FROM siteconfig WHERE "
//				+ " nsitetypecode = (SELECT nsitetypecode FROM latest_sitetype)), site_base AS ("
//				+ " SELECT sd.nnodesitecode, sc.jsondata, sc.sconfigname, sc.ntransactionstatus,  sc.nsitehierarchyconfigcode,"
//				+ " s.ssitename FROM site s JOIN sitehierarchyconfigdetails sd  ON s.nsitecode = sd.nnodesitecode"
//				+ " JOIN sitehierarchyconfig sc ON sd.nsitehierarchyconfigcode = sc.nsitehierarchyconfigcode"
//				+ " WHERE s.nsitecode IN (SELECT nsitecode FROM sitecodes_for_latest_type) and "
//				+ "	sd.nnodesitecode IN (SELECT nsitecode FROM sitecodes_for_latest_type) AND sd.nstatus ="
//				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  AND sc.nstatus ="
//				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " AND sc.ntransactionstatus =31),"
//				+ " json_nodes AS ( SELECT sb.nnodesitecode, jsonb_array_elements(sb.jsondata -> 'nodes') AS node,"
//				+ " sb.nsitehierarchyconfigcode FROM site_base sb UNION ALL SELECT j.nnodesitecode,"
//				+ " jsonb_array_elements(j.node -> 'nodes'), j.nsitehierarchyconfigcode FROM json_nodes j"
//				+ " WHERE j.node ? 'nodes') SELECT DISTINCT sb.nnodesitecode, sb.nsitehierarchyconfigcode,"
//				+ " n.node -> 'item' -> 'selectedNodeDetail' ->> 'ssiteconfigname' AS siteconfigname, sb.sconfigname,"
//				+ " sb.ntransactionstatus, sb.ssitename FROM site_base sb JOIN json_nodes n"
//				+ " ON sb.nnodesitecode = (n.node -> 'item' -> 'selectedNodeDetail' ->> 'nsitecode')::int"
//				+ " AND sb.nsitehierarchyconfigcode = n.nsitehierarchyconfigcode;";
		
		// added by sujatha ATE_274 25-09-2025 because of an issue with taking latest site type, if we add more no. of record in site type we can't get taluk
		String strTaluk="WITH RECURSIVE json_tree AS ( SELECT  sc.nsitehierarchyconfigcode,  sc.sconfigname,"
				+ " sc.ntransactionstatus, jsonb_array_elements(sc.jsondata -> 'nodes') AS node"
				+ " FROM sitehierarchyconfig sc WHERE  sc.nstatus ="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " AND sc.ntransactionstatus ="+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
				+ " UNION ALL SELECT  jt.nsitehierarchyconfigcode, jt.sconfigname,"
				+ " jt.ntransactionstatus, jsonb_array_elements(jt.node -> 'nodes') AS node"
				+ " FROM json_tree jt WHERE (jt.node -> 'nodes') IS NOT NULL)"
				+ " SELECT  d.nnodesitecode, d.nsitehierarchyconfigcode,"
				+ " jt.node -> 'item' -> 'selectedNodeDetail' ->> 'ssiteconfigname' AS siteconfigname,"
				+ " jt.sconfigname,  jt.ntransactionstatus"
				+ " FROM sitehierarchyconfigdetails d JOIN ( SELECT  nsitehierarchyconfigcode, sconfigname, ntransactionstatus,"
				+ " (node -> 'item' -> 'selectedNodeDetail' ->> 'nsitecode')::int AS nsitecode, node"
				+ " FROM json_tree) jt"
				+ " ON d.nnodesitecode = jt.nsitecode"
				+ " AND d.nsitehierarchyconfigcode = jt.nsitehierarchyconfigcode WHERE "
				+ " d.nstatus ="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " AND NOT EXISTS ( SELECT 1 "
				+ " FROM sitehierarchyconfigdetails d2"
				+ " WHERE d2.nsitehierarchyconfigcode = d.nsitehierarchyconfigcode"
				+ " AND d2.nparentsitecode = d.nnodesitecode"
				+ " AND d2.nstatus ="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " )";
		
		return new ResponseEntity<>(jdbcTemplate.query(strTaluk, new SiteHierarchyConfig()), HttpStatus.OK);
	}

	// added by sujatha ATE_274 SWSM-4 19-05-2025 for getting the nsitehierarchyconfigcode used for insert in village table 
	// and alert exist validation for village name & code
	
	private int getSiteHierarchyConfigCode (UserInfo userInfo, int nnodesitecode) throws Exception{

//		final String strTaluk = " WITH RECURSIVE "
//				+ " latest_sitetype AS ( SELECT nsitetypecode FROM sitetype ORDER BY nsitetypecode DESC LIMIT 1),"
//				+ " sitecodes_for_latest_type AS ( SELECT nsitecode FROM siteconfig WHERE "
//				+ " nsitetypecode = (SELECT nsitetypecode FROM latest_sitetype)), site_base AS ("
//				+ " SELECT sd.nnodesitecode, sc.jsondata, sc.sconfigname, sc.ntransactionstatus,  sc.nsitehierarchyconfigcode,"
//				+ " s.ssitename FROM site s JOIN sitehierarchyconfigdetails sd  ON s.nsitecode = sd.nnodesitecode"
//				+ " JOIN sitehierarchyconfig sc ON sd.nsitehierarchyconfigcode = sc.nsitehierarchyconfigcode"
//				+ " WHERE s.nsitecode IN (SELECT nsitecode FROM sitecodes_for_latest_type) and "
//				+ "	sd.nnodesitecode IN (SELECT nsitecode FROM sitecodes_for_latest_type) AND sd.nstatus ="
//				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  AND sc.nstatus ="
//				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " AND sc.ntransactionstatus =31),"
//				+ " json_nodes AS ( SELECT sb.nnodesitecode, jsonb_array_elements(sb.jsondata -> 'nodes') AS node,"
//				+ " sb.nsitehierarchyconfigcode FROM site_base sb UNION ALL SELECT j.nnodesitecode,"
//				+ " jsonb_array_elements(j.node -> 'nodes'), j.nsitehierarchyconfigcode FROM json_nodes j"
//				+ " WHERE j.node ? 'nodes') SELECT DISTINCT sb.nnodesitecode, sb.nsitehierarchyconfigcode,"
//				+ " n.node -> 'item' -> 'selectedNodeDetail' ->> 'ssiteconfigname' AS siteconfigname, sb.sconfigname,"
//				+ " sb.ntransactionstatus, sb.ssitename FROM site_base sb JOIN json_nodes n"
//				+ " ON sb.nnodesitecode = (n.node -> 'item' -> 'selectedNodeDetail' ->> 'nsitecode')::int"
//				+ " AND sb.nsitehierarchyconfigcode = n.nsitehierarchyconfigcode and sb.nnodesitecode="+nnodesitecode+";";
		
		// added by sujatha ATE_274 25-09-2025 because of an issue with taking latest site type, if we add more no. of record in site type we can't get taluk
		final String strTaluk="WITH RECURSIVE json_tree AS ( SELECT  sc.nsitehierarchyconfigcode, sc.sconfigname,"
				+ " sc.ntransactionstatus, jsonb_array_elements(sc.jsondata -> 'nodes') AS node FROM sitehierarchyconfig sc"
				+ " WHERE sc.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " AND sc.ntransactionstatus ="+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
				+ " UNION ALL SELECT jt.nsitehierarchyconfigcode, jt.sconfigname, jt.ntransactionstatus,"
				+ " jsonb_array_elements(jt.node -> 'nodes') AS node FROM json_tree jt WHERE (jt.node -> 'nodes') IS NOT NULL)"
				+ " SELECT  d.nnodesitecode, d.nsitehierarchyconfigcode,"
				+ " jt.node -> 'item' -> 'selectedNodeDetail' ->> 'ssiteconfigname' AS siteconfigname, jt.sconfigname,"
				+ " jt.ntransactionstatus FROM sitehierarchyconfigdetails d JOIN ( SELECT nsitehierarchyconfigcode,"
				+ " sconfigname, ntransactionstatus,"
				+ " (node -> 'item' -> 'selectedNodeDetail' ->> 'nsitecode')::int AS nsitecode, node"
				+ " FROM json_tree ) jt ON d.nnodesitecode = jt.nsitecode"
				+ " AND d.nsitehierarchyconfigcode = jt.nsitehierarchyconfigcode"
				+ " WHERE d.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " AND d.nnodesitecode ="+nnodesitecode
				+ " AND NOT EXISTS ( SELECT 1 FROM sitehierarchyconfigdetails d2"
				+ " WHERE d2.nsitehierarchyconfigcode = d.nsitehierarchyconfigcode AND d2.nparentsitecode = d.nnodesitecode"
				+ " AND d2.nstatus ="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " ) ORDER BY d.nnodesitecode;";
		
		final SiteHierarchyConfig outSource = jdbcTemplate.queryForObject(strTaluk, new SiteHierarchyConfig());
		int nsitehierarchyconfigcode=outSource.getNsitehierarchyconfigcode();		
		return nsitehierarchyconfigcode;
	}
	/**
	 * This method is used to add a new entry to village table. Village Name is
	 * unique across the database. Need to check for duplicate entry of village name
	 * for the specified site before saving into database. Need to check for
	 * duplicate entry of village code for the specified site before saving into
	 * database.
	 * 
	 * @param objVillage [Villages] object holding details to be added in village
	 *                   table
	 * @param userInfo   [UserInfo] holding logged in user details based on which
	 *                   the list is to be fetched
	 * @return saved village object with status code 200 if saved successfully else
	 *         if the city already exists, response will be returned as 'Already
	 *         Exists' with status code 409
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> createVillage(Villages objVillage, UserInfo userInfo) throws Exception {

		final String sQuery = " lock  table villages " + Enumeration.ReturnStatus.EXCLUSIVEMODE.getreturnstatus();
		jdbcTemplate.execute(sQuery);
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> savedVillageList = new ArrayList<>();
		//added by sujatha ATE_274 SWSM-4 19-05-2025 used to pass nsitehierarchyconfigcode to the 2 method to check for delete validation which is based on nsitehierarchyconfigcode
		final int nsitehierarchyconfigcode=getSiteHierarchyConfigCode(userInfo, objVillage.getNnodesitecode());
		
		final Villages villageByName = getVillageByName(objVillage.getSvillagename(), userInfo.getNmastersitecode(), nsitehierarchyconfigcode);
		final Villages villageByCode = getVillageByCode(objVillage.getSvillagecode(), userInfo.getNmastersitecode(), nsitehierarchyconfigcode);
		String alert = "";
		if (villageByName == null && villageByCode == null) {
			final String sequencenoquery = "select nsequenceno from seqnosubmittermanagement where stablename ='villages' "
					+ "and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			int nsequenceno = jdbcTemplate.queryForObject(sequencenoquery, Integer.class);
			nsequenceno++;
			//modified by sujatha ATE_274 SWSM-4 19-05-2025 added 1 more field and altered ncitycode into nnodesitecode
			final String insertquery = "Insert into villages (nvillagecode,svillagename,svillagecode,nnodesitecode, "
					+ " nsitehierarchyconfigcode, dmodifieddate,nsitecode,nstatus) " + "values(" + nsequenceno + ",N'"
					+ stringUtilityFunction.replaceQuote(objVillage.getSvillagename()) + "',N'"
					+ stringUtilityFunction.replaceQuote(objVillage.getSvillagecode()) + "',"
					+ objVillage.getNnodesitecode() 
					+ "," + nsitehierarchyconfigcode
					+",'" + dateUtilityFunction.getCurrentDateTime(userInfo) + "', "
					+ userInfo.getNmastersitecode() + ", " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ ")";
			jdbcTemplate.execute(insertquery);
			final String updatequery = "update seqnosubmittermanagement set nsequenceno =" + nsequenceno
					+ " where stablename='villages'" + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			jdbcTemplate.execute(updatequery);
			objVillage.setNvillagecode(nsequenceno);
			//modified by sujatha v ATE_274 SWSM-4 19-05-2025 to to assign what we get in siteconfigname to scityname
			objVillage.setScityname(objVillage.getSiteconfigname());
			//modified by sujatha v ATE_274 SWSM-4 19-05-2025 from ncitycode to nnodesitecode
			objVillage.setNnodesitecode(objVillage.getNnodesitecode());
			savedVillageList.add(objVillage);
			multilingualIDList.add("IDS_ADDVILLAGE");
			auditUtilityFunction.fnInsertAuditAction(savedVillageList, 1, null, multilingualIDList, userInfo);
			return getVillage(userInfo);
		} else {
			if (villageByName != null && villageByCode != null) {
				alert = commonFunction.getMultilingualMessage("IDS_VILLAGE", userInfo.getSlanguagefilename()) + " and "
						+ commonFunction.getMultilingualMessage("IDS_VILLAGECODE", userInfo.getSlanguagefilename());
			} else if (villageByName != null) {
				alert = commonFunction.getMultilingualMessage("IDS_VILLAGE", userInfo.getSlanguagefilename());
			} else {
				alert = commonFunction.getMultilingualMessage("IDS_VILLAGECODE", userInfo.getSlanguagefilename());
			}
			return new ResponseEntity<>(
					alert + " " + commonFunction.getMultilingualMessage(
							Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(), userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}
	}

	/**
	 * This method is used to fetch the village object for the specified village
	 * name and site.
	 * 
	 * @param svillagename    [String] name of the village
	 * @param nmasterSiteCode [int] site code of the village
	 * @return village object based on the specified village name and site
	 * @throws Exception that are thrown from this DAO layer
	 */
	private Villages getVillageByName(final String svillagename, final int nmasterSiteCode, final int nsitehierarchyconfigcode)
			throws Exception {
		//modified by sujatha ATE_274 SWSM-4 19-05-2025 to add one more condition to check instead of table specific it becomes  nsitehierarchyconfigcode specific
		final String strQuery = "select nvillagecode from villages where nsitehierarchyconfigcode="+nsitehierarchyconfigcode
				+ " and svillagename = N'"
				+ stringUtilityFunction.replaceQuote(svillagename) + "' and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode =" + nmasterSiteCode;
		final Villages objVillage = (Villages) jdbcUtilityFunction.queryForObject(strQuery, Villages.class,
				jdbcTemplate);
		return objVillage;
	}

	/**
	 * This method is used to fetch the village object for the specified village
	 * code and site.
	 * 
	 * @param svillagecode    [String] code of the village
	 * @param nmasterSiteCode [int] site code of the village
	 * @return village object based on the specified village code and site
	 * @throws Exception that are thrown from this DAO layer
	 */
	private Villages getVillageByCode(final String svillagecode, final int nmasterSiteCode, final int nsitehierarchyconfigcode) 
			throws Exception {
		//modified by sujatha ATE_274 19-05-2025 to add one more condition to check instead of table specific it becomes  nsitehierarchyconfigcode specific
		final String strQuery = "select nvillagecode from villages where nsitehierarchyconfigcode="+nsitehierarchyconfigcode
				+ " and svillagecode=N'"+ stringUtilityFunction.replaceQuote(svillagecode) + "' and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode =" + nmasterSiteCode;
		final Villages objVillage = (Villages) jdbcUtilityFunction.queryForObject(strQuery, Villages.class,
				jdbcTemplate);
		return objVillage;
	}

	/**
	 * This method is used to retrieve active village object based on the specified
	 * nvillagecode.
	 * 
	 * @param nvillagecode [int] primary key of village object
	 * @param userInfo     [UserInfo] holding logged in user details based on which
	 *                     the list is to be fetched
	 * @return response entity object holding response status and data of village
	 *         object
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public Villages getActiveVillageById(int nvillagecode, UserInfo userInfo) throws Exception {
		
		// Modified and latest query added by sujatha ATE_274 SWSM-4 19-05-2025 for getting based nnodesitecode and the siteconfigname on the below condition
//		final String strQuery = "select v.nvillagecode, v.svillagename, v.svillagecode, v.nsitecode, v.nstatus , c.scityname , "
//				+ " v.ncitycode from villages v, city c " + " where c.ncitycode=v.ncitycode and " + "c.nstatus ="
//				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and v.nstatus="
//				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and v.nvillagecode = " + nvillagecode;
		
		// modified because of an issue with taking latest site type

//		final String strQuery="WITH RECURSIVE latest_sitetype AS ( SELECT nsitetypecode  FROM sitetype "
//				+ " ORDER BY nsitetypecode DESC LIMIT 1), sitecodes_for_latest_type AS ( SELECT nsitecode"
//				+ " FROM siteconfig WHERE nsitetypecode = (SELECT nsitetypecode FROM latest_sitetype)),"
//				+ " site_base AS ( SELECT sd.nnodesitecode, sc.jsondata, sc.sconfigname, sc.ntransactionstatus,"
//				+ " sc.nsitehierarchyconfigcode, s.ssitename FROM site s JOIN sitehierarchyconfigdetails sd "
//				+ " ON s.nsitecode = sd.nnodesitecode JOIN sitehierarchyconfig sc "
//				+ " ON sd.nsitehierarchyconfigcode = sc.nsitehierarchyconfigcode WHERE "
//				+ " s.nsitecode IN (SELECT nsitecode FROM sitecodes_for_latest_type) and"
//				+ "	sd.nnodesitecode IN (SELECT nsitecode FROM sitecodes_for_latest_type) AND sd.nstatus ="
//				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//				+ " AND sc.nstatus ="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//				+ " AND sc.ntransactionstatus = 31), json_nodes AS ( SELECT sb.nnodesitecode,"
//				+ " jsonb_array_elements(sb.jsondata -> 'nodes') AS node, sb.nsitehierarchyconfigcode "
//				+ " FROM site_base sb  UNION ALL SELECT  j.nnodesitecode, jsonb_array_elements(j.node -> 'nodes'),"
//				+ " j.nsitehierarchyconfigcode FROM json_nodes j WHERE j.node ? 'nodes') SELECT DISTINCT sb.nnodesitecode ,"
//				+ " n.node -> 'item' -> 'selectedNodeDetail' ->> 'ssiteconfigname' AS siteconfigname, sb.ntransactionstatus,"
//				+ " v.nvillagecode, v.svillagename, v.svillagecode, v.nsitecode, v.nstatus, v.nnodesitecode,"
//				+ " v.nsitehierarchyconfigcode FROM site_base sb"
//				+ " JOIN json_nodes n ON sb.nnodesitecode = (n.node -> 'item' -> 'selectedNodeDetail' ->> 'nsitecode')::int"
//				+ " JOIN villages v on v.nnodesitecode=sb.nnodesitecode "
//				+ " AND sb.nsitehierarchyconfigcode = n.nsitehierarchyconfigcode and v.nstatus="
//				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and nsitecode="+userInfo.getNmastersitecode()
//				+ " and v.nvillagecode ="+nvillagecode+ ";";
		
		// added by sujatha ATE_274 25-09-2025 because of an issue with taking latest site type, if we add more no. of record in site type we can't get taluk
		final String strQuery = "WITH RECURSIVE json_tree AS ("
		        + " SELECT sc.nsitehierarchyconfigcode, sc.sconfigname, sc.ntransactionstatus,"
		        + " jsonb_array_elements(sc.jsondata -> 'nodes') AS node"
		        + " FROM sitehierarchyconfig sc"
		        + " WHERE sc.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
		        + " AND sc.ntransactionstatus ="+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
		        + " UNION ALL SELECT jt.nsitehierarchyconfigcode, jt.sconfigname, jt.ntransactionstatus,"
		        + " jsonb_array_elements(jt.node -> 'nodes') AS node"
		        + " FROM json_tree jt"
		        + " WHERE (jt.node -> 'nodes') IS NOT NULL), "
		        + " parsed_nodes AS ( SELECT nsitehierarchyconfigcode, sconfigname, ntransactionstatus,"
		        + " (node -> 'item' -> 'selectedNodeDetail' ->> 'nsitecode')::int AS nsitecode, node"
		        + " FROM json_tree)"
		        + " SELECT DISTINCT d.nnodesitecode,"
		        + " pn.node -> 'item' -> 'selectedNodeDetail' ->> 'ssiteconfigname' AS siteconfigname, pn.ntransactionstatus,"
		        + " v.nvillagecode, v.svillagename, v.svillagecode, v.nsitecode, v.nstatus, v.nnodesitecode,"
		        + " v.nsitehierarchyconfigcode FROM sitehierarchyconfigdetails d"
		        + " JOIN parsed_nodes pn ON d.nnodesitecode = pn.nsitecode"
		        + " AND d.nsitehierarchyconfigcode = pn.nsitehierarchyconfigcode"
		        + " JOIN villages v ON v.nnodesitecode = d.nnodesitecode"
		        + " AND v.nsitehierarchyconfigcode = d.nsitehierarchyconfigcode"
		        + " WHERE d.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
		        + " AND v.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
		        + " AND v.nsitecode = " + userInfo.getNmastersitecode()
		        + " AND v.nvillagecode = " + nvillagecode;

		final Villages objVillage = (Villages) jdbcUtilityFunction.queryForObject(strQuery, Villages.class,
				jdbcTemplate);
		return objVillage;
	}

	/**
	 * This method is used to update entry in village table. Need to validate that
	 * the village object to be updated is active before updating details in
	 * database. Need to check for duplicate entry of village name for the specified
	 * site before saving into database. Need to check for duplicate entry of
	 * village code for the specified site before saving into database
	 * 
	 * @param objVillage [Villages] object holding details to be updated in village
	 *                   table
	 * @param userInfo   [UserInfo] holding logged in user details based on which
	 *                   the list is to be fetched
	 * @return saved village object with status code 200 if saved successfully else
	 *         if the village already exists, response will be returned as 'Already
	 *         Exists' with status code 409 else if the village to be updated is not
	 *         available, response will be returned as 'Already Deleted' with status
	 *         code 417
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> updateVillage(Villages objVillage, UserInfo userInfo) throws Exception {
		final Villages village = getActiveVillageById(objVillage.getNvillagecode(), userInfo);
		String alert = "";
		if (village == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			//added by sujatha ATE_274 to SWSM-4 19-05-2025 add one more condition to check instead of table specific it becomes  nsitehierarchyconfigcode specific

			final int nsitehierarchyconfigcode=getSiteHierarchyConfigCode(userInfo, objVillage.getNnodesitecode());
			
			//modified by sujatha ATE_274 SWSM-4 19-05-2025 to add one more condition to check instead of table specific it becomes  nsitehierarchyconfigcode specific
			final String queryVillage = "select nvillagecode from villages where nsitehierarchyconfigcode="+nsitehierarchyconfigcode
					+ " and svillagename = '"
					+ stringUtilityFunction.replaceQuote(objVillage.getSvillagename()) + "' and nvillagecode <> "
					+ objVillage.getNvillagecode() + " and nsitecode=" + userInfo.getNmastersitecode()
					+ " and  nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

			final List<Villages> villageList = jdbcTemplate.query(queryVillage, new Villages());
			
			//modified by sujatha ATE_274 19-05-2025 to add one more condition to check instead of table specific it becomes  nsitehierarchyconfigcode specific
			final String queryVillageCode = "select nvillagecode from villages where nsitehierarchyconfigcode="+nsitehierarchyconfigcode
					+ " and svillagecode='"
					+ stringUtilityFunction.replaceQuote(objVillage.getSvillagecode()) + "' and nvillagecode <> "
					+ objVillage.getNvillagecode() + " and nsitecode=" + userInfo.getNmastersitecode()
					+ " and  nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			final List<Villages> villageCodeList = jdbcTemplate.query(queryVillageCode, new Villages());
			if (villageList.isEmpty() && villageCodeList.isEmpty()) {
				final String updateQueryString = "update villages set nnodesitecode=" + objVillage.getNnodesitecode() //modified by sujatha ATE_274 from ncitycode to nnodesitecode 19-05-2025 SWSM-4
						+ " ,svillagename='" + stringUtilityFunction.replaceQuote(objVillage.getSvillagename()) + "', "
						+ "svillagecode ='" + stringUtilityFunction.replaceQuote(objVillage.getSvillagecode())
						+ "', dmodifieddate ='" + dateUtilityFunction.getCurrentDateTime(userInfo)
						+ "' where nvillagecode=" + objVillage.getNvillagecode() + " and nsitecode= "
						+ userInfo.getNmastersitecode();
				jdbcTemplate.execute(updateQueryString);
				final List<String> multilingualIDList = new ArrayList<>();
				multilingualIDList.add("IDS_EDITVILLAGE");
				//modified by sujatha v ATE_274 SWSM-4 19-05-2025 to to assign what we get in siteconfigname to scityname
				village.setScityname(village.getSiteconfigname());
				//modified by sujatha v ATE_274 SWSM-4 19-05-2025 from ncitycode to nnodesitecode
				village.setNnodesitecode(village.getNnodesitecode());
				objVillage.setScityname(objVillage.getSiteconfigname());
				//modified by sujatha v ATE_274 SWSM-4 19-05-2025 from ncitycode to nnodesitecode
				objVillage.setNnodesitecode(objVillage.getNnodesitecode());
				final List<Object> listAfterSave = new ArrayList<>();
				listAfterSave.add(objVillage);
				final List<Object> listBeforeSave = new ArrayList<>();
				listBeforeSave.add(village);
				auditUtilityFunction.fnInsertAuditAction(listAfterSave, 2, listBeforeSave, multilingualIDList,
						userInfo);
				return getVillage(userInfo);
			} else {
				if (!villageList.isEmpty() && !villageCodeList.isEmpty()) {
					alert = commonFunction.getMultilingualMessage("IDS_VILLAGE", userInfo.getSlanguagefilename())
							+ " and "
							+ commonFunction.getMultilingualMessage("IDS_VILLAGECODE", userInfo.getSlanguagefilename());
				} else if (!villageList.isEmpty()) {
					alert = commonFunction.getMultilingualMessage("IDS_VILLAGE", userInfo.getSlanguagefilename());
				} else {
					alert = commonFunction.getMultilingualMessage("IDS_VILLAGECODE", userInfo.getSlanguagefilename());
				}
				return new ResponseEntity<>(alert + " " + commonFunction.getMultilingualMessage(
						Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(), userInfo.getSlanguagefilename()),
						HttpStatus.CONFLICT);
			}
		}
	}

	/**
	 * This method id used to delete an entry in Villages table Need to check the
	 * record is already deleted or Need to check whether the record is used in
	 * other tables such as 'samplelocation'
	 * 
	 * @param objVillage [Villages] an Object holds the record to be deleted
	 * @param userInfo   [UserInfo] holding logged in user details based on which
	 *                   the list is to be fetched
	 * @return a response entity with list of available village objects
	 * @exception Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> deleteVillage(final Villages objVillage, final UserInfo userInfo) throws Exception {
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> deletedVillageList = new ArrayList<>();
		final Villages village = getActiveVillageById(objVillage.getNvillagecode(), userInfo);
		if (village == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			// added by sujatha.v SWSM-5 01/08/2025
			final String query = " Select 'IDS_SAMPLELOCATION' as Msg from samplelocation where nvillagecode= "
					+ objVillage.getNvillagecode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " union all "    // added by sujatha 22-09-2025 to fix 500 bug while delete village
							+ " select 'IDS_CUSTOMERCOMPLAINT' as Msg from customercomplaint where nvillagecode="
							+ objVillage.getNvillagecode() + " and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(); 
			// modified by sujatha ATE_274 19-05-2025 for delete validation not to be sitecode based
//					+ " and nsitecode="
//					+ userInfo.getNtranssitecode();
			validatorDel = projectDAOSupport.getTransactionInfo(query, userInfo);
			boolean validRecord = false;
			if (validatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
				validRecord = true;
				validatorDel = projectDAOSupport.validateDeleteRecord(Integer.toString(objVillage.getNvillagecode()),
						userInfo);
				if (validatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
					validRecord = true;
				} else {
					validRecord = false;
				}
			}
			if (validRecord) {
				final String updateQueryString = "update villages set nstatus = "
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ", dmodifieddate ='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where nvillagecode= "
						+ objVillage.getNvillagecode() + " and nsitecode=" + userInfo.getNmastersitecode();
				jdbcTemplate.execute(updateQueryString);
				objVillage.setNstatus((short) Enumeration.TransactionStatus.DELETED.gettransactionstatus());
				//modified by sujatha v ATE_274 SWSM-4 19-05-2025 to to assign what we get in siteconfigname to scityname
				objVillage.setScityname(objVillage.getSiteconfigname());
				//modified by sujatha v ATE_274 SWSM-4 19-05-2025 from ncitycode to nnodesitecode
				objVillage.setNnodesitecode(objVillage.getNnodesitecode());
				deletedVillageList.add(objVillage);
				multilingualIDList.add("IDS_DELETEVILLAGE");
				auditUtilityFunction.fnInsertAuditAction(deletedVillageList, 1, null, multilingualIDList, userInfo);
				return getVillage(userInfo);
			} else {
				return new ResponseEntity<>(validatorDel.getSreturnmessage(), HttpStatus.EXPECTATION_FAILED);
			}
		}
	}
}
