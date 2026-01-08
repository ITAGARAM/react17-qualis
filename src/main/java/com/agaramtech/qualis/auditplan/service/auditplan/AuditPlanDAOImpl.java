package com.agaramtech.qualis.auditplan.service.auditplan;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import com.agaramtech.qualis.auditplan.model.AuditCategory;
import com.agaramtech.qualis.auditplan.model.AuditMaster;
import com.agaramtech.qualis.auditplan.model.AuditPlan;
import com.agaramtech.qualis.auditplan.model.AuditPlanAuditor;
import com.agaramtech.qualis.auditplan.model.AuditPlanHistory;
import com.agaramtech.qualis.auditplan.model.AuditPlanMember;
import com.agaramtech.qualis.auditplan.model.AuditStandardCategory;
import com.agaramtech.qualis.auditplan.model.AuditType;
import com.agaramtech.qualis.auditplan.model.SeqNoAuditPlan;
import com.agaramtech.qualis.basemaster.model.TransactionStatus;
import com.agaramtech.qualis.credential.model.ControlMaster;
import com.agaramtech.qualis.credential.model.Users;
import com.agaramtech.qualis.auditplan.model.AuditPlanFile;
import com.agaramtech.qualis.global.AuditUtilityFunction;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.EmailDAOSupport;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.FTPUtilityFunction;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.LinkMaster;
import com.agaramtech.qualis.global.ProjectDAOSupport;
import com.agaramtech.qualis.global.StringUtilityFunction;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.organization.model.Department;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import lombok.AllArgsConstructor;
/**
 * This class is used to perform CRUD Operation on 'auditplan','auditplanhistory','auditplanmembers','auditplanauditors' table by 
 * implementing methods from its interface. 
 */
/**
* @author AT-E143 SWSM-6 05/08/2025
*/
@AllArgsConstructor
@Repository
public class AuditPlanDAOImpl implements AuditPlanDAO {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(AuditPlanDAOImpl.class);

	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final ProjectDAOSupport projectDAOSupport;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;
	private final FTPUtilityFunction ftpUtilityFunction;
	private final EmailDAOSupport emailDAOSupport;  //Added by sonia on 6th sept 2025 for jira id:SWSM-12


	/**
	 * This method  calls the getFilterStatus and getAuditPlanData methods and is used to retrieve list of all available AuditPlan for the specified site and transactionstatus.
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return response entity  object holding response status and list of all active AuditPlan and transactionstatus table.
	 * @throws Exception that are thrown from this DAO layer
	 */
	@SuppressWarnings("unchecked")	
	public ResponseEntity<Object> getAuditPlan(final Map<String, Object> inputMap, final UserInfo userInfo)	throws Exception {
		
		final Map<String, Object> outputMap = new HashMap<String, Object>();		
		outputMap.putAll(getFilterStatus(inputMap,userInfo));
		outputMap.putAll((Map<String,Object>)getAuditPlanData(inputMap,userInfo).getBody());		
		return new ResponseEntity<Object>(outputMap, HttpStatus.OK);		
	}	
	/**
	 * This method is used to retrieve list of all available transactionstatus .
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return response entity  object holding response status and list of all active transactionstatus
	 * @throws Exception that are thrown from this DAO layer
	 */
	public Map<String, Object> getFilterStatus(final Map<String, Object> inputMap,UserInfo userInfo) throws Exception{
		final Map<String, Object> map = new HashMap<String, Object>();
		final String strQuery=" select ntranscode,COALESCE(jsondata->'stransdisplaystatus'->>'"+userInfo.getSlanguagetypecode()+"',jsondata->'stransdisplaystatus'->>'en-US') as stransdisplaystatus "
							+ "from  transactionstatus where ntranscode in ("+Enumeration.TransactionStatus.ALL.gettransactionstatus()+","+Enumeration.TransactionStatus.DRAFT.gettransactionstatus()+","+Enumeration.TransactionStatus.SCHEDULED.gettransactionstatus()+","+Enumeration.TransactionStatus.CLOSED.gettransactionstatus()+") and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" order by ntranscode ";
		final List<TransactionStatus> lstTransactionStatus = (List<TransactionStatus>) jdbcTemplate.query(strQuery, new TransactionStatus());
		if (lstTransactionStatus.size() > 0) {
			map.put("transactionStatus", lstTransactionStatus);
			map.put("defaultTransactionStatus", lstTransactionStatus.get(0));
			inputMap.put("ntransactionstatus", lstTransactionStatus.get(0).getNtranscode());
			inputMap.put("nflag", 1);
		} else {
			map.put("transactionStatus", null);
			map.put("defaultTransactionStatus",null);

		}
		return map;		
	}
	/**
	 * This method is used to retrieve list of all available AuditPlan for the specified site.
	 * @param fromDate [String] holding from-date which list the AuditPlan from the from-date.
	 * @param toDate [String] holding to-date which list the AuditPlan till the to-date.
	 * @param currentUIDate [String] holding the current UI date.
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return response entity  object holding response status and list of all active AuditPlan
	 * @throws Exception that are thrown from this DAO layer
	 */
	@SuppressWarnings("unchecked")
	public ResponseEntity<Object> getAuditPlanData(final Map<String, Object> inputMap, final UserInfo userInfo)	throws Exception {
		
		//final ObjectMapper objMapper = new ObjectMapper();
		final Map<String, Object> outputMap = new HashMap<String, Object>();
		
		String fromDate = "";
		String toDate = "";
		if (inputMap.get("fromDate") != null) {
			fromDate = (String) inputMap.get("fromDate");
		}
		if (inputMap.get("toDate") != null) {
			toDate = (String) inputMap.get("toDate");
		}
		final String currentUIDate = (String) inputMap.get("currentdate");
		
		if (currentUIDate != null && currentUIDate.trim().length() != 0) {
			final Map<String, Object> mapObject = projectDAOSupport.getDateFromControlProperties(userInfo, currentUIDate, "datetime","FromDate");
			fromDate = (String) mapObject.get("FromDate");
			toDate = (String) mapObject.get("ToDate");
			outputMap.put("fromDate", mapObject.get("FromDateWOUTC"));
			outputMap.put("toDate", mapObject.get("ToDateWOUTC"));
		} else {
			final DateTimeFormatter dbPattern = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
			final DateTimeFormatter uiPattern = DateTimeFormatter.ofPattern(userInfo.getSdatetimeformat());
			final String fromDateUI = LocalDateTime.parse(fromDate, dbPattern).format(uiPattern);
			final String toDateUI = LocalDateTime.parse(toDate, dbPattern).format(uiPattern);
			outputMap.put("fromDate", fromDateUI);
			outputMap.put("toDate", toDateUI);
			fromDate = dateUtilityFunction.instantDateToString(dateUtilityFunction.convertStringDateToUTC(fromDate, userInfo, true));
			toDate = dateUtilityFunction.instantDateToString(dateUtilityFunction.convertStringDateToUTC(toDate, userInfo, true));
		}
		LocalDateTime fromDateTime = LocalDateTime.parse(fromDate);
		LocalDateTime toDateTime = LocalDateTime.parse(toDate);
		DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		String formattedFromDate = fromDateTime.format(myFormatObj);
		String formattedToDate = toDateTime.format(myFormatObj);
		short ntransactionstatus = 0;
		String filterStatusQuery = "";

		if (inputMap.containsKey("ntranscode")) {
			if (inputMap.containsKey("nflag")) {
				ntransactionstatus = (short) inputMap.get("ntranscode");
			} else {
				Integer transStatusValue = (Integer) inputMap.get("ntranscode");
				ntransactionstatus = transStatusValue.shortValue();
			}	
		}
		if (ntransactionstatus > 0) {
			filterStatusQuery = "and aph.ntransactionstatus =" + ntransactionstatus + " ";
		}

		final String strQuery = "select ap.nauditplancode,ap.naudittypecode,ap.nauditcategorycode,ap.nauditstandardcatcode,ap.ndeptcode, "
							  + "ap.ndeptheadcode,ap.saudittitle,ap.scompauditrep,ap.noffsetdauditdatetime,ap.ntzauditdatetime, "
				              + "ap.sauditid,at.saudittypename,ac.sauditcategoryname,asc1.sauditstandardcatname,d.sdeptname,"
				              + "u.sfirstname ||' '||u.slastname as sdeptheadname,tz.stimezoneid,aph.ntransactionstatus,cm.scolorhexcode,  "
				 			  + "COALESCE(TO_CHAR(ap.dauditdatetime,'" + userInfo.getSpgsitedatetime() + "'),'-') as sauditdatetime,"
						 	  + "COALESCE(TO_CHAR(aph.dcreateddate,'" + userInfo.getSpgsitedatetime() + "'),'-') as screateddate,"
							  + "COALESCE(ts.jsondata->'stransdisplaystatus'->>'"+userInfo.getSlanguagetypecode()+"',ts.jsondata->'stransdisplaystatus'->>'en-US') as stransdisplaystatus " 
							  + "from auditplan ap "
							  + "join audittype at on at.naudittypecode = ap.naudittypecode "
							  + "join auditcategory ac on ac.nauditcategorycode =ap.nauditcategorycode "
							  + "join auditstandardcategory asc1 on asc1.nauditstandardcatcode =ap.nauditstandardcatcode "
							  + "join department d on d.ndeptcode = ap.ndeptcode "
							  + "join users u on u.nusercode = ap.ndeptheadcode "
							  + "join timezone tz on tz.ntimezonecode =ap.ntzauditdatetime "
							  + "join auditplanhistory aph on aph.nauditplancode =ap.nauditplancode "
							  + "join transactionstatus ts on ts.ntranscode= aph.ntransactionstatus "
							  + "join formwisestatuscolor fsc on fsc.ntranscode =aph.ntransactionstatus "
							  + "join colormaster cm on cm.ncolorcode =fsc.ncolorcode " 
							  + "where aph.nauditplanhistorycode=any(select max(nauditplanhistorycode) from auditplanhistory "
							  + "where  nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
							  + "and nsitecode="+userInfo.getNtranssitecode()+" group by nauditplancode) and aph.dcreateddate ::Date between '" + formattedFromDate + "' and '"+ formattedToDate + "' "
							  + " "+filterStatusQuery+" and ap.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"  "
							  + "and at.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and ac.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
							  + "and asc1.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and d.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and u.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
							  + "and fsc.nformcode ="+userInfo.getNformcode()+" and ap.nsitecode ="+userInfo.getNtranssitecode()+" and ap.nauditplancode >0 order by ap.nauditplancode";
		
		List<AuditPlan> lstAuditPlan = jdbcTemplate.query(strQuery,new AuditPlan());
		
		/*final List<AuditPlan> lstUTCConvertedDate = objMapper.convertValue(
				dateUtilityFunction.getSiteLocalTimeFromUTC(lstAuditPlan,
				Arrays.asList("sauditdatetime"),
				Arrays.asList(userInfo.getStimezoneid()), userInfo, false, null, false),
				new TypeReference<List<AuditPlan>>() {
				});*/
		outputMap.put("auditPlan", lstAuditPlan);
		
		if(lstAuditPlan.isEmpty()) {
			outputMap.put("selectedAuditPlan", Collections.emptyList());
			outputMap.put("auditPlanHistory",  Collections.emptyList());
			outputMap.put("auditPlanMember",  Collections.emptyList());
			outputMap.put("auditPlanAuditor",  Collections.emptyList());
			outputMap.put("auditPlanFile",  Collections.emptyList());

		} else {
			final AuditPlan selectedAuditPlan = lstAuditPlan.get(lstAuditPlan.size()-1);
			final int nauditPlanCode = selectedAuditPlan.getNauditplancode();	
			outputMap.put("selectedAuditPlan", Arrays.asList(selectedAuditPlan));
			outputMap.putAll(getAuditPlanHistory(nauditPlanCode, userInfo));
			outputMap.putAll((Map<String,Object>)getAuditPlanMember(nauditPlanCode, userInfo).getBody());
			outputMap.putAll((Map<String, Object>)getAuditPlanAuditor(nauditPlanCode, userInfo).getBody());
			outputMap.putAll((Map<String, Object>)getAuditPlanFile(nauditPlanCode, userInfo).getBody());
		}

		return new ResponseEntity<Object>(outputMap, HttpStatus.OK);		
	}
	
	/**
	 * This method is used to retrieve active auditplanhistory object based on the specified nauditPlanCode.
	 * @param nauditPlanCode [int] primary key of auditplan object
	 * @param userInfo [UserInfo] holding logged in user details based on 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of auditplanhistory object
	 * @throws Exception that are thrown from this DAO layer
	 */	
	public Map<String, Object> getAuditPlanHistory(int nauditPlanCode, UserInfo userInfo) throws Exception{
		final Map<String, Object> map = new HashMap<String, Object>();
		final String strQuery=" select ap.nauditplancode,ap.saudittitle,ap.sauditid,aph.sremarks, aph.ntransactionstatus,aph.dauditdatetime, "
							 +" aph.nuserrolecode ,aph.nusercode,ur.suserrolename,u.sfirstname ||' '|| u.slastname as susername, "
							 +" COALESCE(ts.jsondata->'stransdisplaystatus'->>'"+userInfo.getSlanguagetypecode()+"',ts.jsondata->'stransdisplaystatus'->>'en-US') as stransdisplaystatus, " 
							 +" COALESCE(TO_CHAR(aph.dauditdatetime,'" + userInfo.getSpgsitedatetime() + "'),'-') as sauditdatetime, "
							 +" COALESCE(TO_CHAR(aph.dcreateddate,'" + userInfo.getSpgsitedatetime() + "'),'-') as screateddate "
							 +" from auditplan ap "
							 +" join auditplanhistory aph on aph.nauditplancode =ap.nauditplancode "
							 +" join transactionstatus ts on ts.ntranscode= aph.ntransactionstatus "
							 +" join userrole ur on ur.nuserrolecode =aph.nuserrolecode " 
							 +" join users u on u.nusercode =aph.nusercode "
							 +" where ap.nstatus ="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and aph.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and ts.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
							 +" and ur.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and u.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
							 +" and ap.nsitecode ="+userInfo.getNtranssitecode()+" and ap.nauditplancode="+nauditPlanCode+" "
							 +" order by aph.dauditdatetime ";
		final List<AuditPlanHistory> lstAuditPlanHistory = (List<AuditPlanHistory>) jdbcTemplate.query(strQuery, new AuditPlanHistory());
		if (lstAuditPlanHistory.size() > 0) {
			map.put("auditPlanHistory", lstAuditPlanHistory);
		} else {
			map.put("auditPlanHistory", null);
		}
		return map;		
	}
	/**
	 * This method is used to retrieve active auditplanmembers object based on the specified nauditPlanCode.
	 * @param nauditPlanCode [int] primary key of auditplan object
	 * @param userInfo [UserInfo] holding logged in user details based on 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of auditplanmembers object
	 * @throws Exception that are thrown from this DAO layer
	 */	
	public ResponseEntity<Object> getAuditPlanMember(int nauditPlanCode, UserInfo userInfo) throws Exception{
		final Map<String, Object> map = new HashMap<String, Object>();
		final String strQuery=" select apm.*,u.sfirstname ||' '|| u.slastname as susername,ap.saudittitle,ap.sauditid from auditplan ap "
							 +" join auditplanmembers apm on apm.nauditplancode =ap.nauditplancode "		
							 +" join users u on u.nusercode =apm.nusercode " 
							 +" where apm.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and u.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and apm.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
							 +" and apm.nsitecode ="+userInfo.getNtranssitecode()+" and apm.nauditplancode="+nauditPlanCode+" ";	
		final List<AuditPlanMember> lstAuditPlanMember = (List<AuditPlanMember>) jdbcTemplate.query(strQuery, new AuditPlanMember());
		if (lstAuditPlanMember.size() > 0) {
			map.put("auditPlanMember", lstAuditPlanMember);
		} else {
			map.put("auditPlanMember", null);
		}
		return new ResponseEntity<Object>(map, HttpStatus.OK);
	}
	/**
	 * This method is used to retrieve active auditplanauditors object based on the specified nauditPlanCode.
	 * @param nauditPlanCode [int] primary key of auditplan object
	 * @param userInfo [UserInfo] holding logged in user details based on 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of auditplanauditors object
	 * @throws Exception that are thrown from this DAO layer
	 */	
	public ResponseEntity<Object> getAuditPlanAuditor(int nauditPlanCode, UserInfo userInfo) throws Exception{
		final Map<String, Object> map = new HashMap<String, Object>();
		final String strQuery=" select apa.*,am.sauditorname,ap.saudittitle,ap.sauditid from auditplan ap "
							 +" join auditplanauditors apa on apa.nauditplancode =ap.nauditplancode "		
							 +" join auditmaster am on am.nauditmastercode =apa.nauditmastercode " 
							 +" where apa.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and am.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and ap.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
							 +" and apa.nsitecode ="+userInfo.getNtranssitecode()+" and apa.nauditplancode="+nauditPlanCode+" ";	
		final List<AuditPlanAuditor> lstAuditPlanAuditor = (List<AuditPlanAuditor>) jdbcTemplate.query(strQuery, new AuditPlanAuditor());
		if (lstAuditPlanAuditor.size() > 0) {
			map.put("auditPlanAuditor", lstAuditPlanAuditor);
		} else {
			map.put("auditPlanAuditor", null);
		}
		return new ResponseEntity<Object>(map, HttpStatus.OK);
	}
	/**
	 * This method is used to retrieve active auditplanfile object based on the specified nauditPlanCode.
	 * @param nauditPlanCode [int] primary key of auditplan object
	 * @param userInfo [UserInfo] holding logged in user details based on 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of auditplanfile object
	 * @throws Exception that are thrown from this DAO layer
	 */	
	public ResponseEntity<Object> getAuditPlanFile(int nauditPlanCode, UserInfo userInfo) throws Exception {
		final Map<String, Object> outputMap = new HashMap<String, Object>();

		String queryformat = "COALESCE(TO_CHAR(apf.dcreateddate,'" + userInfo.getSpgsitedatetime() + "'),'-') ";

		final String protocolFileQry = " select apf.nauditplanfilecode,apf.nauditplancode,apf.noffsetdcreateddate,"
								     + " apf.nlinkcode, apf.nattachmenttypecode, apf.sfilename,apf.ssystemfilename, apf.sdescription,"
								     + " COALESCE(at.jsondata->'sattachmenttype'->>'" + userInfo.getSlanguagetypecode() + "',"
								     + " at.jsondata->'sattachmenttype'->>'en-US') as stypename, "
								     + " case when apf.nlinkcode = "+Enumeration.TransactionStatus.NA.gettransactionstatus()+" then '-' else lm.jsondata->>'slinkname' end slinkname,"
								     + " case when apf.nlinkcode = "+Enumeration.TransactionStatus.NA.gettransactionstatus()+" then cast(apf.nfilesize as text) else '-' end sfilesize,"
								     + " case when apf.nlinkcode = "+Enumeration.TransactionStatus.NA.gettransactionstatus()+" then " + queryformat + " else '-' end screateddate"
								     + " from auditplanfile apf "
								     + " join auditplan ap on ap.nauditplancode = apf.nauditplancode "
								     + " join attachmenttype at on at.nattachmenttypecode = apf.nattachmenttypecode "
								     + " join linkmaster lm on lm.nlinkcode = apf.nlinkcode "
								     + " where at.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " 
								     + " and lm.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " 
								     + " and apf.nstatus =" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
								     + " and ap.nstatus =" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " 
								     + " and apf.nsitecode="+userInfo.getNtranssitecode()+" "
								     + " and apf.nauditplancode = "+ nauditPlanCode + " ";
		outputMap.put("auditPlanFile",
				dateUtilityFunction.getSiteLocalTimeFromUTC(jdbcTemplate.query(protocolFileQry, new AuditPlanFile()),
						Arrays.asList("screateddate"), Arrays.asList(userInfo.getStimezoneid()), userInfo, false, null,
						false));

		return new ResponseEntity<Object>(outputMap, HttpStatus.OK);
	}
	/**
	 * This method is used to retrieve list of all available auditmaster for the specified site.
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return response entity  object holding response status and list of all active auditmaster
	 * @throws Exception that are thrown from this DAO layer
	 */
	//Modified the select query by sonia on 23rd August 2025 for jira id:SWSM-6
	public Map<String, Object> getAuditMaster(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		final Map<String, Object> outputMap = new HashMap<String, Object>();
		
		final String strQuery ="select * from auditmaster where nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and nleadauditor="+Enumeration.TransactionStatus.YES.gettransactionstatus()+" and nsitecode="+userInfo.getNmastersitecode()+" and nauditmastercode >0 ";
		final List<AuditMaster> lstAuditMaster = (List<AuditMaster>) jdbcTemplate.query(strQuery, new AuditMaster());
		outputMap.put("auditMaster", lstAuditMaster);
		return outputMap;
	}
	/**
	 * This method is used to retrieve list of all available audittype for the specified site.
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return response entity  object holding response status and list of all active audittype
	 * @throws Exception that are thrown from this DAO layer
	 */
	public Map<String, Object> getAuditType(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		final Map<String, Object> outputMap = new HashMap<String, Object>();
		final String strQuery ="select * from audittype where nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and nsitecode="+userInfo.getNmastersitecode()+" and naudittypecode >0 ";
		final List<AuditType> lstAuditType = (List<AuditType>) jdbcTemplate.query(strQuery, new AuditType());
		outputMap.put("auditType", lstAuditType);

		return outputMap;
	}
	/**
	 * This method is used to retrieve list of all available auditcategory for the specified site.
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return response entity  object holding response status and list of all active auditcategory
	 * @throws Exception that are thrown from this DAO layer
	 */
	public Map<String, Object> getAuditCategory(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		final Map<String, Object> outputMap = new HashMap<String, Object>();
		final String strQuery ="select * from auditcategory where nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and nsitecode="+userInfo.getNmastersitecode()+" and nauditcategorycode >0 ";
		final List<AuditCategory> lstAuditCategory = (List<AuditCategory>) jdbcTemplate.query(strQuery, new AuditCategory());
		outputMap.put("auditCategory", lstAuditCategory);

		return outputMap;
	}
	/**
	 * This method is used to retrieve list of all available auditstandardcategory for the specified site.
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return response entity  object holding response status and list of all active auditstandardcategory
	 * @throws Exception that are thrown from this DAO layer
	 */
	public Map<String, Object> getAuditStandardCategory(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		final Map<String, Object> outputMap = new HashMap<String, Object>();
		final String strQuery ="select * from auditstandardcategory where nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and nsitecode="+userInfo.getNmastersitecode()+" and nauditstandardcatcode >0 ";
		final List<AuditStandardCategory> lstAuditStandardCategory = (List<AuditStandardCategory>) jdbcTemplate.query(strQuery, new AuditStandardCategory());
		outputMap.put("auditStandardCategory", lstAuditStandardCategory);

		return outputMap;
	}
	/**
	 * This method is used to retrieve list of all available department for the specified site.
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return response entity  object holding response status and list of all active department
	 * @throws Exception that are thrown from this DAO layer
	 */
	public Map<String, Object> getDepartment(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		final Map<String, Object> outputMap = new HashMap<String, Object>();
		final String strQuery ="select * from department where nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and nsitecode="+userInfo.getNmastersitecode()+" and ndeptcode >0";
		final List<Department> lstDepartment = (List<Department>) jdbcTemplate.query(strQuery, new Department());
		outputMap.put("department", lstDepartment);		
		return outputMap;
	}
	/**
	 * This method is used to retrieve list of all available users based on the selected department for the specified site.
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return response entity  object holding response status and list of all active users based on the selected department
	 * @throws Exception that are thrown from this DAO layer
	 */
	public Map<String, Object> getDepartmentHead(int ndepartmentCode, UserInfo userInfo) throws Exception {
		final Map<String, Object> outputMap = new HashMap<String, Object>();
		final String strQuery = " select nusercode as ndeptheadcode,sfirstname||' '||slastname  as sdeptheadname from users where ndeptcode ="+ndepartmentCode+" "
							  + " and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and nsitecode ="+userInfo.getNmastersitecode()+" and nusercode >0 ";
		final List<Users> lstUsers = (List<Users>) jdbcTemplate.query(strQuery, new Users());
		outputMap.put("departmentHead", lstUsers);
		return outputMap;
	}
	/**
	 * This method is used to retrieve list of all available users  for the specified site.
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return response entity  object holding response status and list of all active users 
	 * @throws Exception that are thrown from this DAO layer
	 */
	public Map<String, Object> getUsers(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		final Map<String, Object> outputMap = new HashMap<String, Object>();
		final String strQuery = " select nusercode ,sfirstname||' '||slastname  as susername from users where  "
							  + " nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and nsitecode ="+userInfo.getNmastersitecode()+" and nusercode >0 ";
		final List<Users> lstUsers = (List<Users>) jdbcTemplate.query(strQuery, new Users());
		outputMap.put("Users", lstUsers);		
		return outputMap;
	}
	/**
	 * This method is used to add a new entry to "auditplan","auditplanhistory","auditplanmembers","auditplanauditors" table.
	 * Need to check for duplicate entry of saudittitle for the specified site before saving into database.
	 * @param objAuditPlan [AuditPlan] object holding details to be added in auditplan table
	 * @param objAuditPlanHistory[AuditPlanHistory]  object holding details to be added in auditplanhistory table,
	 * @param objAuditPlanMember[AuditPlanMember]  object holding details to be added in auditplanmembers table,
	 * @param objAuditPlanMember[AuditPlanAuditor]  object holding details to be added in auditplanauditors table,
	 * @param userInfo [UserInfo] holding logged in user details based on 
	 * 							  which the list is to be fetched
	 * @return saved "auditplan","auditplanhistory","auditplanmembers","auditplanauditors" object with status code 200 if saved successfully 
	 *  else if the Audit Title already exists, response will be returned as 'Already Exists' with status code 417
	 * @throws Exception that are thrown from this DAO layer
	 */
	@SuppressWarnings({ "unchecked" })
	public ResponseEntity<Object> createAuditPlan(final Map<String, Object> inputMap, final UserInfo userInfo)throws Exception {
		
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> savedAuditPlanList = new ArrayList<>();	
		Map<String, Object> returnMap = new HashMap<String, Object>();
		final ObjectMapper objmapper = new ObjectMapper();
		objmapper.registerModule(new JavaTimeModule());	
		
		
		final AuditPlan objAuditPlan = objmapper.convertValue(inputMap.get("auditplan"), AuditPlan.class);
		final AuditPlanHistory objAuditPlanHistory = objmapper.convertValue(inputMap.get("auditplanhistory"), AuditPlanHistory.class);
		final AuditPlanMember objAuditPlanMember = objmapper.convertValue(inputMap.get("auditplanmember"), AuditPlanMember.class);
		final AuditPlanAuditor objAuditPlanAuditor = objmapper.convertValue(inputMap.get("auditplanauditor"), AuditPlanAuditor.class);

		final AuditPlan auditPlanByName = getAuditPlanByName(objAuditPlan.getSaudittitle(), userInfo.getNtranssitecode()); //Modified by sonia on 9th sept 2025 for jira id:SWSM-6
		if (auditPlanByName == null) {
			

			final String strQuery = "lock table lockauditplan " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus()+ "; ";
			jdbcTemplate.execute(strQuery);

		    final String strSelectSeqno = "select stablename,nsequenceno from seqnoauditplan  where stablename in ('auditplan','auditplanhistory','auditplanmembers','auditplanauditors')";

		    final List<?> lstMultiSeqNo = projectDAOSupport.getMultipleEntitiesResultSetInList(strSelectSeqno,jdbcTemplate, SeqNoAuditPlan.class);

			final List<SeqNoAuditPlan> lstSeqNoAuditPlan = (List<SeqNoAuditPlan>) lstMultiSeqNo.get(0);

			returnMap = lstSeqNoAuditPlan.stream().collect(
						Collectors.toMap(SeqNoAuditPlan::getStablename, SeqNoAuditPlan -> SeqNoAuditPlan.getNsequenceno()));
			
			/*final String dAuditDateTime=(String) objAuditPlan.getDauditdatetime().toString();
			
			final short ntzAuditDateTime =objAuditPlan.getNtzauditdatetime();
			
				
			final List<String> secondList = new ArrayList<>();		
			final Map<String,Object> utcConvertedData  = projectDAOSupport.convertToUtcBasedOnTimeZone(dAuditDateTime,ntzAuditDateTime,userInfo);
		    
		    
		    final String formattedAuditDateTime = (String) utcConvertedData.get("dateTime"); 
		    final Short timeZoneCode = (Short) utcConvertedData.get("ntzdatetime"); 
		    final boolean flag =(boolean) utcConvertedData.get("flag"); 

		    objAuditPlan.setSauditdatetime(formattedAuditDateTime);			
		    objAuditPlan.setNtzauditdatetime(timeZoneCode);
			
			if(flag) {
			    final Short inputTimeZoneCode = (Short) utcConvertedData.get("ninputtimezonecode"); 
			    objAuditPlan.setNinputtimezonecode(inputTimeZoneCode);
				secondList.add("ninputtimezonecode");
			}else {
				secondList.add("ntzauditdatetime"); 
			}
						 
			final AuditPlan convertedObject = objmapper.convertValue(
					dateUtilityFunction.convertInputDateToUTCByZone(objAuditPlan,
							Arrays.asList("sauditdatetime"), secondList, true, userInfo),
					new TypeReference<AuditPlan>() {});*/

			int auditPlanSeqNo = (int) returnMap.get("auditplan") + 1;
			int auditPlanHistorySeqNo = (int) returnMap.get("auditplanhistory") + 1;
			int auditPlanMemberSeqNo = (int) returnMap.get("auditplanmembers") + 1;
			int auditPlanAuditorSeqNo = (int) returnMap.get("auditplanauditors") + 1;

			String auditPlanInsert = " Insert into auditplan(nauditplancode,naudittypecode,nauditcategorycode,nauditstandardcatcode,ndeptcode,ndeptheadcode,saudittitle,"
								  + " scompauditrep,sauditid,dauditdatetime,ntzauditdatetime,noffsetdauditdatetime,dmodifieddate,nsitecode,nstatus)"
								  + " values("+auditPlanSeqNo+","+objAuditPlan.getNaudittypecode()+","+objAuditPlan.getNauditcategorycode()+","
								  + " "+objAuditPlan.getNauditstandardcatcode()+","+objAuditPlan.getNdeptcode()+","+objAuditPlan.getNdeptheadcode()+","
								  + " N'"+stringUtilityFunction.replaceQuote(objAuditPlan.getSaudittitle())+"',N'"+stringUtilityFunction.replaceQuote(objAuditPlan.getScompauditrep())+"','"+objAuditPlan.getSauditid()+"',"
								  + "'"+objAuditPlan.getDauditdatetime()+"',"+userInfo.getNtimezonecode()+","+dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())+","
								  + "'"+dateUtilityFunction.getCurrentDateTime(userInfo)+"',"+userInfo.getNtranssitecode()+", "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+");";

			auditPlanInsert += "Insert into auditplanhistory(nauditplanhistorycode,nauditplancode,ntransactionstatus,nusercode,nuserrolecode,"
							+ "ndeputyusercode,ndeputyuserrolecode,sremarks,dauditdatetime,ntzauditdatetime,noffsetdauditdatetime,dcreateddate,ntzcreateddate,noffsetdcreateddate,dmodifieddate,nsitecode,nstatus)"
							+ "values(" + auditPlanHistorySeqNo + "," + auditPlanSeqNo + "," + Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + ","
							+ " "+userInfo.getNusercode() + ", " + userInfo.getNuserrole() +" , "+userInfo.getNdeputyusercode()+", "+userInfo.getNdeputyuserrole()+", "
							+ " N'"+stringUtilityFunction.replaceQuote(objAuditPlanHistory.getSremarks())+"',"
							+ "'"+objAuditPlan.getDauditdatetime()+"',"+userInfo.getNtimezonecode()+","+dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())+","
							+ " '"+dateUtilityFunction.getCurrentDateTime(userInfo)+"',"+userInfo.getNtimezonecode()+","+dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())+","
						    + "'"+dateUtilityFunction.getCurrentDateTime(userInfo)+"',"+userInfo.getNtranssitecode() + "," + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+ " );";
			
			auditPlanInsert += "Insert into auditplanmembers(nauditplanmembercode,nauditplancode,nusercode,dmodifieddate,nsitecode,nstatus)"
							+ "values(" + auditPlanMemberSeqNo + "," + auditPlanSeqNo + ", "+objAuditPlanMember.getNusercode() + ","
							+ "'"+dateUtilityFunction.getCurrentDateTime(userInfo)+"', "+userInfo.getNtranssitecode() + "," + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+ " );";
			
			auditPlanInsert += "Insert into auditplanauditors(nauditplanauditorcode,nauditplancode,nauditmastercode,dmodifieddate,nsitecode,nstatus)"
							+ "values(" + auditPlanAuditorSeqNo + "," + auditPlanSeqNo + ", "+objAuditPlanAuditor.getNauditmastercode() + ","
							+ "'"+dateUtilityFunction.getCurrentDateTime(userInfo)+"', "+userInfo.getNtranssitecode() + "," + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+ " );";

			auditPlanInsert += "update seqnoauditplan set nsequenceno = " + auditPlanSeqNo + " where stablename='auditplan';" 
						    +  "update seqnoauditplan set nsequenceno = " + auditPlanHistorySeqNo + " where stablename='auditplanhistory';"
						    +  "update seqnoauditplan set nsequenceno = " + auditPlanMemberSeqNo + " where stablename='auditplanmembers';"
						    +  "update seqnoauditplan set nsequenceno = " + auditPlanAuditorSeqNo + " where stablename='auditplanauditors';";

			jdbcTemplate.execute(auditPlanInsert);
			
			objAuditPlan.setSauditdatetime(dateUtilityFunction.instantDateToStringWithFormat(objAuditPlan.getDauditdatetime(),userInfo.getSsitedatetime()));		
			objAuditPlan.setNauditplancode(auditPlanSeqNo);
			objAuditPlanHistory.setNauditplanhistorycode(auditPlanHistorySeqNo);
			objAuditPlanMember.setNauditplanmembercode(auditPlanMemberSeqNo);	
			objAuditPlanAuditor.setNauditplanauditorcode(auditPlanAuditorSeqNo);
			objAuditPlan.setNtransactionstatus(Enumeration.TransactionStatus.DRAFT.gettransactionstatus());
			objAuditPlan.setNusercode(objAuditPlanMember.getNusercode());
			objAuditPlan.setNauditmastercode(objAuditPlanAuditor.getNauditmastercode());

		 	savedAuditPlanList.add(objAuditPlan);			
			multilingualIDList.add("IDS_ADDAUDITPLAN");			
			auditUtilityFunction.fnInsertAuditAction(savedAuditPlanList, 1, null, multilingualIDList, userInfo);
			
			return getAuditPlanData(inputMap,userInfo);

		}else {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		}				
	}
	/**
	 * This method is used to fetch the auditplan for the specified saudittitle and site.
	 * @param saudittitle [String] Audit Title of the auditplan
	 * @param nmasterSiteCode [int] userInfo object
	 * @return auditplan get query [String] created by specified saudittitle and site
	 * @throws Exception that are thrown from this DAO layer
	 */
	private AuditPlan getAuditPlanByName(String saudittitle, int nmasterSiteCode) throws Exception {		
		final String strQuery = "select nauditplancode from auditplan where saudittitle = N'"+ stringUtilityFunction.replaceQuote(saudittitle) + "'"
							  + " and nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and nsitecode =" + nmasterSiteCode;
		return (AuditPlan) jdbcUtilityFunction.queryForObject(strQuery, AuditPlan.class, jdbcTemplate);
	}
	/**
	 * This method is used to retrieve active auditplan object based on the specified nauditPlanCode.
	 * @param nauditPlanCode [int] primary key of auditplan object.
	 * @param userInfo [UserInfo] holding logged in user details based on 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of auditplan object
	 * 	 else if the auditplan is not available, response will be returned as 'Already  Deleted' with status code 417
	 * 	 else if the auditplan record status is not draft,  response will be returned as 'Select Draft Record Only' with status code 417	
	 * @throws Exception that are thrown from this DAO layer
	 */
	@SuppressWarnings("unchecked")
	public ResponseEntity<Object> getActiveAuditPlanById(int nauditPlanCode,UserInfo userInfo) throws Exception {		
		//final ObjectMapper objMapper = new ObjectMapper();
		final Map<String, Object> outputMap = new HashMap<String, Object>();
		
		final String sValidationQuery=" select  ap.nauditplancode,aph.ntransactionstatus from auditplan ap join auditplanhistory aph on aph.nauditplancode =ap.nauditplancode "
				 					 +" where aph.nauditplanhistorycode=any	(select max(nauditplanhistorycode) from auditplanhistory where  nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and nsitecode="+userInfo.getNtranssitecode()+"	group by nauditplancode) "
				 					 +" and ap.nstatus ="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and aph.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and ap.nsitecode ="+userInfo.getNtranssitecode()+" and ap.nauditplancode="+nauditPlanCode+" ";	
		final AuditPlan auditplan =  (AuditPlan) jdbcUtilityFunction.queryForObject(sValidationQuery, AuditPlan.class, jdbcTemplate);
		if(auditplan!=null) {
			if(auditplan.getNtransactionstatus()==Enumeration.TransactionStatus.DRAFT.gettransactionstatus()) {
				final String strQuery = "select ap.nauditplancode,ap.naudittypecode,ap.nauditcategorycode,ap.nauditstandardcatcode,ap.ndeptcode, "
						  			  + "ap.ndeptheadcode,ap.saudittitle,ap.scompauditrep,ap.noffsetdauditdatetime,ap.ntzauditdatetime, "
						  			  + "ap.sauditid,at.saudittypename,ac.sauditcategoryname,asc1.sauditstandardcatname,d.sdeptname,"
						  			  + "u.sfirstname ||' '||u.slastname as sdeptheadname,tz.stimezoneid,aph.ntransactionstatus,cm.scolorhexcode, "
						  			  + "COALESCE(TO_CHAR(ap.dauditdatetime,'" + userInfo.getSpgsitedatetime() + "'),'-') as sauditdatetime,  "
						  			  + "COALESCE(ts.jsondata->'stransdisplaystatus'->>'"+userInfo.getSlanguagetypecode()+"',ts.jsondata->'stransdisplaystatus'->>'en-US') as stransdisplaystatus " 
						  			  + "from auditplan ap "
						  			  + "join audittype at on at.naudittypecode = ap.naudittypecode "
						  			  + "join auditcategory ac on ac.nauditcategorycode =ap.nauditcategorycode "
						  			  + "join auditstandardcategory asc1 on asc1.nauditstandardcatcode =ap.nauditstandardcatcode "
						  			  + "join department d on d.ndeptcode = ap.ndeptcode "
						  			  + "join users u on u.nusercode = ap.ndeptheadcode "
						  			  + "join timezone tz on tz.ntimezonecode =ap.ntzauditdatetime "
						  			  + "join auditplanhistory aph on aph.nauditplancode =ap.nauditplancode "
						  			  + "join transactionstatus ts on ts.ntranscode= aph.ntransactionstatus "
						  			  + "join formwisestatuscolor fsc on fsc.ntranscode =aph.ntransactionstatus "
						  			  + "join colormaster cm on cm.ncolorcode =fsc.ncolorcode " 
						  			  + "where aph.nauditplanhistorycode=any(select max(nauditplanhistorycode) from auditplanhistory "
						  			  + "where  nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and nsitecode="+userInfo.getNtranssitecode()+" and nauditplancode = "+nauditPlanCode+" group by nauditplancode)"
						  			  + "and ap.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
						  			  + "and at.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and ac.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
						  			  + "and asc1.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and d.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and u.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
						  			  + "and fsc.nformcode ="+userInfo.getNformcode()+" and ap.nsitecode ="+userInfo.getNtranssitecode()+" and ap.nauditplancode = "+nauditPlanCode+"";

				final List<AuditPlan> lstAuditPlan = jdbcTemplate.query(strQuery,new AuditPlan());
				/*final List<AuditPlan> lstUTCConvertedDate = objMapper.convertValue(
						dateUtilityFunction.getSiteLocalTimeFromUTC(lstAuditPlan,
						Arrays.asList("sauditdatetime"),
						Arrays.asList(userInfo.getStimezoneid()), userInfo, false, null, false),
						new TypeReference<List<AuditPlan>>() {
						});*/
				
				if(lstAuditPlan.isEmpty()) {
					outputMap.put("selectedAuditPlan", Collections.emptyList());
					outputMap.put("auditPlanHistory",  Collections.emptyList());
					outputMap.put("auditPlanMember",  Collections.emptyList());
					outputMap.put("auditPlanAuditor",  Collections.emptyList());
					outputMap.put("auditPlanFile",  Collections.emptyList());

				} else {
						
					outputMap.put("selectedAuditPlan", lstAuditPlan);
					outputMap.putAll(getAuditPlanHistory(nauditPlanCode, userInfo));
					outputMap.putAll((Map<String, Object>)getAuditPlanMember(nauditPlanCode, userInfo).getBody());
					outputMap.putAll((Map<String, Object>)getAuditPlanAuditor(nauditPlanCode, userInfo).getBody());
					outputMap.putAll((Map<String, Object>)getAuditPlanFile(nauditPlanCode, userInfo).getBody());
				}
				return new ResponseEntity<Object>(outputMap, HttpStatus.OK);
			}else {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTONLYDRAFTRECORD",userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}
		}else {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		}
		
	}
	/**
	 * This method is used to retrieve active auditplan object based on the specified nauditPlanCode.
	 * @param nauditPlanCode [int] primary key of auditplan object.
	 * @param userInfo [UserInfo] holding logged in user details based on 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of auditplan object
	 * 	else if the auditplan is not available, response will be returned as 'Already  Deleted' with status code 417
	 * @throws Exception that are thrown from this DAO layer
	 */
	@SuppressWarnings("unchecked")
	public ResponseEntity<Object> getSelectionAuditPlanById(int nauditPlanCode,UserInfo userInfo) throws Exception {		
		//final ObjectMapper objMapper = new ObjectMapper();
		final Map<String, Object> outputMap = new HashMap<String, Object>();
		
		final String sValidationQuery=" select  ap.nauditplancode,aph.ntransactionstatus from auditplan ap join auditplanhistory aph on aph.nauditplancode =ap.nauditplancode "
				 					 +" where aph.nauditplanhistorycode=any	(select max(nauditplanhistorycode) from auditplanhistory where  nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and nsitecode="+userInfo.getNtranssitecode()+"	group by nauditplancode) "
				 					 +" and ap.nstatus ="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and aph.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and ap.nsitecode ="+userInfo.getNtranssitecode()+" and ap.nauditplancode="+nauditPlanCode+" ";	
		final AuditPlan auditplan =  (AuditPlan) jdbcUtilityFunction.queryForObject(sValidationQuery, AuditPlan.class, jdbcTemplate);
		if(auditplan!=null) {
				final String strQuery = "select ap.nauditplancode,ap.naudittypecode,ap.nauditcategorycode,ap.nauditstandardcatcode,ap.ndeptcode, "
						  			  + "ap.ndeptheadcode,ap.saudittitle,ap.scompauditrep,ap.noffsetdauditdatetime,ap.ntzauditdatetime, "
						  			  + "ap.sauditid,at.saudittypename,ac.sauditcategoryname,asc1.sauditstandardcatname,d.sdeptname,"
						  			  + "u.sfirstname ||' '||u.slastname as sdeptheadname,tz.stimezoneid,aph.ntransactionstatus,cm.scolorhexcode, "
						  			  + "COALESCE(TO_CHAR(ap.dauditdatetime,'" + userInfo.getSpgsitedatetime() + "'),'-') as sauditdatetime,  "
						  			  + "COALESCE(ts.jsondata->'stransdisplaystatus'->>'"+userInfo.getSlanguagetypecode()+"',ts.jsondata->'stransdisplaystatus'->>'en-US') as stransdisplaystatus " 
						  			  + "from auditplan ap "
						  			  + "join audittype at on at.naudittypecode = ap.naudittypecode "
						  			  + "join auditcategory ac on ac.nauditcategorycode =ap.nauditcategorycode "
						  			  + "join auditstandardcategory asc1 on asc1.nauditstandardcatcode =ap.nauditstandardcatcode "
						  			  + "join department d on d.ndeptcode = ap.ndeptcode "
						  			  + "join users u on u.nusercode = ap.ndeptheadcode "
						  			  + "join timezone tz on tz.ntimezonecode =ap.ntzauditdatetime "
						  			  + "join auditplanhistory aph on aph.nauditplancode =ap.nauditplancode "
						  			  + "join transactionstatus ts on ts.ntranscode= aph.ntransactionstatus "
						  			  + "join formwisestatuscolor fsc on fsc.ntranscode =aph.ntransactionstatus "
						  			  + "join colormaster cm on cm.ncolorcode =fsc.ncolorcode " 
						  			  + "where aph.nauditplanhistorycode=any(select max(nauditplanhistorycode) from auditplanhistory "
						  			  + "where  nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and nsitecode="+userInfo.getNtranssitecode()+" and nauditplancode = "+nauditPlanCode+" group by nauditplancode)"
						  			  + "and ap.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"  "
						  			  + "and at.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and ac.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
						  			  + "and asc1.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and d.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and u.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
						  			  + "and fsc.nformcode ="+userInfo.getNformcode()+" and ap.nsitecode ="+userInfo.getNtranssitecode()+" and ap.nauditplancode = "+nauditPlanCode+"";

				final List<AuditPlan> lstAuditPlan = jdbcTemplate.query(strQuery,new AuditPlan());
				/*final List<AuditPlan> lstUTCConvertedDate = objMapper.convertValue(
						dateUtilityFunction.getSiteLocalTimeFromUTC(lstAuditPlan,
						Arrays.asList("sauditdatetime"),
						Arrays.asList(userInfo.getStimezoneid()), userInfo, false, null, false),
						new TypeReference<List<AuditPlan>>() {
						});*/
				
				if(lstAuditPlan.isEmpty()) {
					outputMap.put("selectedAuditPlan", Collections.emptyList());
					outputMap.put("auditPlanHistory",  Collections.emptyList());
					outputMap.put("auditPlanMember",  Collections.emptyList());
					outputMap.put("auditPlanAuditor",  Collections.emptyList());
					outputMap.put("auditPlanFile",  Collections.emptyList());

				} else {
						
					outputMap.put("selectedAuditPlan", lstAuditPlan);
					outputMap.putAll(getAuditPlanHistory(nauditPlanCode, userInfo));
					outputMap.putAll((Map<String, Object>)getAuditPlanMember(nauditPlanCode, userInfo).getBody());
					outputMap.putAll((Map<String, Object>)getAuditPlanAuditor(nauditPlanCode, userInfo).getBody());
					outputMap.putAll((Map<String, Object>)getAuditPlanFile(nauditPlanCode, userInfo).getBody());
				}
				return new ResponseEntity<Object>(outputMap, HttpStatus.OK);
			
		}else {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		}
		
	}
	/**
	 * This method is used to update entry in auditplan table.
	 * Need to validate that the auditplan object to be updated is active before updating details in database.
	 * Need to check for duplicate entry of saudittitle for the specified site before saving into database.
	 * @param inputMap [Map] map object holding details to be updated in auditplan table.
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return saved auditplan object with status code 200 if saved successfully 
	 * 			else if the auditplan already exists, response will be returned as 'Already Exists' with status code 417
	 *          else if the auditplan to be updated is not available, response will be returned as 'Already Deleted' with status code 417
	 *          else if the auditplan record status is not draft, response will be returned as 'Select Draft Record Only' with status code 417 
	 * @throws Exception that are thrown from this DAO layer
	 */
	public ResponseEntity<Object> updateAuditPlan(final Map<String, Object> inputMap, final UserInfo userInfo)throws Exception {
		
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> beforeAuditPlan = new ArrayList<>();
		final List<Object> afterAuditPlan = new ArrayList<>();		
		
		final ObjectMapper objmapper = new ObjectMapper();
		objmapper.registerModule(new JavaTimeModule());	
				
		final AuditPlan objAuditPlan = objmapper.convertValue(inputMap.get("auditplan"),new TypeReference<AuditPlan>() {});
		
		final String sValidationQuery=" select  ap.nauditplancode,aph.ntransactionstatus from auditplan ap join auditplanhistory aph on aph.nauditplancode =ap.nauditplancode "
									 +" where aph.nauditplanhistorycode=any	(select max(nauditplanhistorycode) from auditplanhistory where  nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and nsitecode="+userInfo.getNtranssitecode()+"	group by nauditplancode) "
									 +" and ap.nstatus ="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and aph.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and ap.nsitecode ="+userInfo.getNtranssitecode()+" and ap.nauditplancode="+objAuditPlan.getNauditplancode()+" ";
		final AuditPlan activeAuditPlan = (AuditPlan) jdbcUtilityFunction.queryForObject(sValidationQuery, AuditPlan.class, jdbcTemplate);
		if (activeAuditPlan != null) {
			if(activeAuditPlan.getNtransactionstatus()==Enumeration.TransactionStatus.DRAFT.gettransactionstatus()) {
				
				final AuditPlan auditplan =  AuditPlanAuditGet(objAuditPlan.getNauditplancode(), userInfo);					
				
				/*final String dAuditDateTime=(String) objAuditPlan.getDauditdatetime().toString();			
				final short ntzAuditDateTime =objAuditPlan.getNtzauditdatetime();		
					
				final List<String> secondList = new ArrayList<>();		
				final Map<String,Object> utcConvertedData  = projectDAOSupport.convertToUtcBasedOnTimeZone(dAuditDateTime,ntzAuditDateTime,userInfo);
			    
			    
			    final String formattedAuditDateTime = (String) utcConvertedData.get("dateTime"); 
			    final Short timeZoneCode = (Short) utcConvertedData.get("ntzdatetime"); 
			    final boolean flag =(boolean) utcConvertedData.get("flag"); 

			    objAuditPlan.setSauditdatetime(formattedAuditDateTime);			
			    objAuditPlan.setNtzauditdatetime(timeZoneCode);
				
				if(flag) {
				    final Short inputTimeZoneCode = (Short) utcConvertedData.get("ninputtimezonecode"); 
				    objAuditPlan.setNinputtimezonecode(inputTimeZoneCode);
					secondList.add("ninputtimezonecode");
				}else {
					secondList.add("ntzauditdatetime"); 
				}
							 
				final AuditPlan convertedObject = objmapper.convertValue(
						dateUtilityFunction.convertInputDateToUTCByZone(objAuditPlan,
								Arrays.asList("sauditdatetime"), secondList, true, userInfo),
						new TypeReference<AuditPlan>() {});*/
				
				final String updateQuery = "update auditplan set dauditdatetime='"+ objAuditPlan.getDauditdatetime() + "',"
										 + "ntzauditdatetime="+ userInfo.getNtimezonecode() + ","
										 + "noffsetdauditdatetime="+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ","
										 + "saudittitle=N'"+ stringUtilityFunction.replaceQuote(objAuditPlan.getSaudittitle()) + "',"
										 + "scompauditrep=N'"+ stringUtilityFunction.replaceQuote(objAuditPlan.getScompauditrep()) + "',"
										 + "naudittypecode="+ objAuditPlan.getNaudittypecode() + ","
										 + "nauditcategorycode="+ objAuditPlan.getNauditcategorycode() + ","
										 + "nauditstandardcatcode="+ objAuditPlan.getNauditstandardcatcode() + ","
										 + "ndeptcode="+ objAuditPlan.getNdeptcode() + ","
										 + "ndeptheadcode="+ objAuditPlan.getNdeptheadcode() + ","
										 + "dmodifieddate='"+ dateUtilityFunction.getCurrentDateTime(userInfo) + "',"
										 + "nsitecode=" + userInfo.getNtranssitecode()+ " " 
										 + "where nauditplancode=" + objAuditPlan.getNauditplancode() + ";";
				jdbcTemplate.execute(updateQuery);
				inputMap.put("nauditplancode", objAuditPlan.getNauditplancode());
				beforeAuditPlan.add(auditplan);
				afterAuditPlan.add(objAuditPlan);				
				multilingualIDList.add("IDS_EDITAUDITPLAN");	
				auditUtilityFunction.fnInsertAuditAction(afterAuditPlan, 2, beforeAuditPlan,multilingualIDList, userInfo);
				return editGetAuditPlanData(inputMap,userInfo);
			}else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage("IDS_SELECTONLYDRAFTRECORD",userInfo.getSlanguagefilename()),HttpStatus.EXPECTATION_FAILED);
			}	
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),userInfo.getSlanguagefilename()),HttpStatus.EXPECTATION_FAILED);
		}		
	}
	/**
	 * This method is used to delete an entry in "auditplan","auditplanhistory","auditplanmembers","auditplanauditors","auditplanfile" table
	 * Need to check the record is already deleted or not
	 * @param inputMap [Map] map object holding detail to be deleted from auditplan table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return a response entity with list of available auditplan object
	 *   else if the auditplan record status is not draft, response will be returned as 'Select Draft Record Only' with status code 417
	 * @exception Exception that are thrown from this DAO layer
	 */
	public ResponseEntity<Object> deleteAuditPlan(final Map<String, Object> inputMap, final UserInfo userInfo)throws Exception {
		
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> deleteAuditPlanList = new ArrayList<>();
		
		final ObjectMapper objmapper = new ObjectMapper();
		objmapper.registerModule(new JavaTimeModule());	
		
		
		final AuditPlan objAuditPlan = objmapper.convertValue(inputMap.get("auditplan"),new TypeReference<AuditPlan>() {});
		
		final String sValidationQuery=" select  ap.nauditplancode,aph.ntransactionstatus from auditplan ap join auditplanhistory aph on aph.nauditplancode =ap.nauditplancode "
									 +" where aph.nauditplanhistorycode=any	(select max(nauditplanhistorycode) from auditplanhistory where  nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and nsitecode="+userInfo.getNtranssitecode()+"	group by nauditplancode) "
									 +" and ap.nstatus ="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and aph.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and ap.nsitecode ="+userInfo.getNtranssitecode()+" and ap.nauditplancode="+objAuditPlan.getNauditplancode()+" ";
		final AuditPlan activeAuditPlan = (AuditPlan) jdbcUtilityFunction.queryForObject(sValidationQuery, AuditPlan.class, jdbcTemplate);
		if (activeAuditPlan != null) {
			if(activeAuditPlan.getNtransactionstatus()==Enumeration.TransactionStatus.DRAFT.gettransactionstatus()) {
				
				
				final String query = "select * from auditplanfile where nauditplancode=" + objAuditPlan.getNauditplancode() + "";
				List<AuditPlanFile> lstAuditPlanFile = jdbcTemplate.query(query, new AuditPlanFile());

				
				String updateQuery = "update auditplan set nstatus="+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ","
										 + "dmodifieddate='"+ dateUtilityFunction.getCurrentDateTime(userInfo) + "',"
										 + "nsitecode=" + userInfo.getNtranssitecode()+ " " 
										 + "where nauditplancode=" + objAuditPlan.getNauditplancode() + ";";
				
				updateQuery=updateQuery + "update auditplanhistory set nstatus="+Enumeration.TransactionStatus.DELETED.gettransactionstatus()+", "
						 				+ "dmodifieddate='"+ dateUtilityFunction.getCurrentDateTime(userInfo) + "',"
						 				+ "nsitecode=" + userInfo.getNtranssitecode()+ " " 
						 				+ "where nauditplancode=" + objAuditPlan.getNauditplancode() + ";";
				
				updateQuery=updateQuery + "update auditplanmembers set nstatus="+Enumeration.TransactionStatus.DELETED.gettransactionstatus()+", "
		 								+ "dmodifieddate='"+ dateUtilityFunction.getCurrentDateTime(userInfo) + "',"
		 								+ "nsitecode=" + userInfo.getNtranssitecode()+ " " 
		 								+ "where nauditplancode=" + objAuditPlan.getNauditplancode() + ";";
				
				updateQuery=updateQuery + "update auditplanauditors set nstatus="+Enumeration.TransactionStatus.DELETED.gettransactionstatus()+", "
		 								+ "dmodifieddate='"+ dateUtilityFunction.getCurrentDateTime(userInfo) + "',"
		 								+ "nsitecode=" + userInfo.getNtranssitecode()+ " " 
		 								+ "where nauditplancode=" + objAuditPlan.getNauditplancode() + ";";
				if(lstAuditPlanFile.size()>0) {
					updateQuery=updateQuery + "update auditplanfile set nstatus="+Enumeration.TransactionStatus.DELETED.gettransactionstatus()+", "
											+ "dmodifieddate='"+ dateUtilityFunction.getCurrentDateTime(userInfo) + "',"
											+ "nsitecode=" + userInfo.getNtranssitecode()+ " " 
											+ "where nauditplancode=" + objAuditPlan.getNauditplancode() + ";";
				}
				
				
				jdbcTemplate.execute(updateQuery);
				
				deleteAuditPlanList.add(objAuditPlan);
				multilingualIDList.add("IDS_DELETEAUDITPLAN");	
				auditUtilityFunction.fnInsertAuditAction(deleteAuditPlanList, 1, null, multilingualIDList, userInfo);
				return getAuditPlanData(inputMap,userInfo);
			}else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage("IDS_SELECTONLYDRAFTRECORD",userInfo.getSlanguagefilename()),HttpStatus.EXPECTATION_FAILED);
			}	
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),userInfo.getSlanguagefilename()),HttpStatus.EXPECTATION_FAILED);
		}		
	}
	/**
	 * This method is used to add a new entry in auditplanhistory table and update an entry in the auditplan table. 
	 * @param inputMap [Map] map object holding details to be updated in auditplan table and added in the auditplanhistory table.
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return a response entity with list of available auditplan object
	 *   else if the auditplan record status is not draft, response will be returned as 'Select Draft Record Only' with status code 417
	 *   else if the auditplan date and time returns validation 'Select Current and Future Dates Only' when choosing paste dates with status code 417
	 *   else if the auditplan to be updated is not available, response will be returned as 'Already Deleted' with status code 417
	 * @exception Exception that are thrown from this DAO layer
	 */
	@SuppressWarnings("unused")
	public ResponseEntity<Object> scheduleAuditPlan(final Map<String, Object> inputMap, final UserInfo userInfo)throws Exception {
	
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> savedAuditPlanList = new ArrayList<>();
		
		final ObjectMapper objmapper = new ObjectMapper();
		objmapper.registerModule(new JavaTimeModule());	
		
		
		final AuditPlan objAuditPlan = objmapper.convertValue(inputMap.get("auditplan"),new TypeReference<AuditPlan>() {});
		final AuditPlanHistory objAuditPlanHistory = objmapper.convertValue(inputMap.get("auditplanhistory"),new TypeReference<AuditPlanHistory>() {});

		
		final String sValidationQuery=" select  ap.nauditplancode,aph.ntransactionstatus,ap.saudittitle from auditplan ap join auditplanhistory aph on aph.nauditplancode =ap.nauditplancode "
									 +" where aph.nauditplanhistorycode=any	(select max(nauditplanhistorycode) from auditplanhistory where  nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and nsitecode="+userInfo.getNtranssitecode()+"	group by nauditplancode) "
									 +" and ap.nstatus ="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and aph.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and ap.nsitecode ="+userInfo.getNtranssitecode()+" and ap.nauditplancode="+objAuditPlan.getNauditplancode()+" ";
		final AuditPlan activeAuditPlan = (AuditPlan) jdbcUtilityFunction.queryForObject(sValidationQuery, AuditPlan.class, jdbcTemplate);
		if (activeAuditPlan != null) {
			//Added by sonia on 13th Oct 2025 for jira id:SWSM-90
			//start
			String strMemberQuery =" select * from auditplanmembers where nauditplancode ="+objAuditPlan.getNauditplancode()+" and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and nsitecode="+userInfo.getNtranssitecode()+" ";
			final List<AuditPlanMember> lstAPM = jdbcTemplate.query(strMemberQuery,new AuditPlanMember());
			
			String strAuditorQuery =" select * from auditplanauditors where nauditplancode ="+objAuditPlan.getNauditplancode()+" and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and nsitecode="+userInfo.getNtranssitecode()+" ";
			final List<AuditPlanAuditor> lstAPA = jdbcTemplate.query(strAuditorQuery,new AuditPlanAuditor()); 
			
			String validationMsg="";
			
			if (lstAPM.size()==0 && lstAPA.size()==0) {
			    validationMsg = "IDS_NOAUDITPLANMEMBERSANDAUDITORS";
			} else if (lstAPM.size()==0) {
			    validationMsg = "IDS_ADDAUDITPLANMEMBERS";
			} else if (lstAPA.size()==0) {
			    validationMsg = "IDS_ADDAUDITPLANAUDITORS";
			}
			
			if(lstAPM.size()>0 && lstAPA.size()>0) {
				//End
				boolean actionStatus=false;
				String sActionMsg="";	
				String sAuditActionMsg="";
				final String sControlName =(String) inputMap.get("soperation");
				if(sControlName.equals("schedule")) {
					if(activeAuditPlan.getNtransactionstatus()==Enumeration.TransactionStatus.DRAFT.gettransactionstatus()) {
						actionStatus=true;
						sAuditActionMsg="IDS_SCHEDULEAUDITPLAN";
					}else {
						sActionMsg="IDS_SELECTONLYDRAFTRECORD";
					}
				}else {
					if(activeAuditPlan.getNtransactionstatus()==Enumeration.TransactionStatus.SCHEDULED.gettransactionstatus()) {
						actionStatus=true;
						sAuditActionMsg="IDS_RESCHEDULEAUDITPLAN";
					}else {
						sActionMsg="IDS_SELECTONLYSCHEDULEDRECORD";
					}
				}
				if(actionStatus) {					
					Instant instant = objAuditPlan.getDauditdatetime();
			        // Convert to LocalDate (removes time part)
			        LocalDate dateOnly = instant.atZone(ZoneOffset.UTC).toLocalDate();
			        LocalDate today = LocalDate.now();			        
			        if(dateOnly.compareTo(today)>=0){
			        	if(sControlName.equals("schedule")) {
							final String strformat = projectDAOSupport.getSeqfnFormat("auditplan","seqnoformatgeneratorauditplan", 0, 0, userInfo);
							final String updatequery = "update auditplan set sauditid='" + strformat+ "' where nauditplancode =" + objAuditPlan.getNauditplancode() + " ";
							jdbcTemplate.execute(updatequery);
							objAuditPlan.setSauditid(strformat);

						}						
						
						final String updateQuery = "update auditplan set dauditdatetime='"+ objAuditPlan.getDauditdatetime() + "',"
								 				 + "ntzauditdatetime="+ userInfo.getNtimezonecode() + ","
								 				 + "noffsetdauditdatetime="+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ","
								 				 + "dmodifieddate='"+ dateUtilityFunction.getCurrentDateTime(userInfo) + "',"
								 				 + "nsitecode=" + userInfo.getNtranssitecode()+ " " 
								 				 + "where nauditplancode=" + objAuditPlan.getNauditplancode() + ";";
						jdbcTemplate.execute(updateQuery);
						short transCode=  (short) Enumeration.TransactionStatus.SCHEDULED.gettransactionstatus();
						int auditPlanHistorySeqNo = (int) jdbcUtilityFunction.queryForObject("select nsequenceno from seqnoauditplan where stablename='auditplanhistory'", Integer.class,jdbcTemplate);
						auditPlanHistorySeqNo++;
						
						String auditPlanInsert = "Insert into auditplanhistory(nauditplanhistorycode,nauditplancode,ntransactionstatus,nusercode,nuserrolecode,"
											   + "ndeputyusercode,ndeputyuserrolecode,sremarks,dauditdatetime,noffsetdauditdatetime,ntzauditdatetime,dcreateddate,ntzcreateddate,noffsetdcreateddate,dmodifieddate,nsitecode,nstatus)"
											   + "values(" + auditPlanHistorySeqNo + "," + objAuditPlan.getNauditplancode() + "," + transCode + ","
											   + " "+userInfo.getNusercode() + ", " + userInfo.getNuserrole() +" , "+userInfo.getNdeputyusercode()+", "+userInfo.getNdeputyuserrole()+", "
											   + " N'"+stringUtilityFunction.replaceQuote(objAuditPlanHistory.getSremarks())+"',"
											   + "'"+objAuditPlan.getDauditdatetime()+"',"+dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())+","+userInfo.getNtimezonecode()+","
											   + " '"+dateUtilityFunction.getCurrentDateTime(userInfo)+"',"+userInfo.getNtimezonecode()+","+dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())+","
											   + "'"+dateUtilityFunction.getCurrentDateTime(userInfo)+"',"
											   + " "+userInfo.getNtranssitecode() + "," + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+ " );";

						auditPlanInsert += "update seqnoauditplan set nsequenceno = " + auditPlanHistorySeqNo + " where stablename='auditplanhistory';";
						jdbcTemplate.execute(auditPlanInsert);						
						inputMap.put("nauditplancode", objAuditPlan.getNauditplancode());
						
						//Added by sonia on 6th sept 2025 for jira id:SWSM-12
						//start
						final Map<String, Object> mailMap = new HashMap<String, Object>();					 
						final String strquery=" select COALESCE(TO_CHAR(TIMESTAMP '"+objAuditPlan.getDauditdatetime()+"'- asc1.sauditremainderdays::int * INTERVAL '1 day' ,'" + userInfo.getSpgsitedatetime() + "'),'-') as sremainderauditdatetime,ap.sauditid "
									   		 +" from auditplan ap "
									   		 +" join auditplanhistory aph on aph.nauditplancode = ap.nauditplancode "
									   		 +" join auditstandardcategory asc1 on asc1.nauditstandardcatcode = ap.nauditstandardcatcode "
									   		 +" where aph.nauditplanhistorycode = ( "
									   		 +" select max(aph2.nauditplanhistorycode) "
									   		 +" from auditplanhistory aph2 "
									   		 +" where aph2.nauditplancode = ap.nauditplancode "
									   		 +" and aph2.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and aph2.nsitecode = "+userInfo.getNtranssitecode()+" "
									   		 +" )	"					  
									   		 +" and ap.nauditplancode="+objAuditPlan.getNauditplancode()+" "	   
									   		 +" and ap.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
									   		 +" and aph.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
									   		 +" and asc1.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
									   		 +" and ap.nsitecode="+userInfo.getNtranssitecode()+" and asc1.nsitecode="+userInfo.getNmastersitecode()+" ";
						final List<AuditPlan> lstAP = (List<AuditPlan>) jdbcTemplate.query(strquery, new AuditPlan());						
						final Instant currentInstant = Instant.now();
						final Instant truncatedInstant = currentInstant.truncatedTo(ChronoUnit.SECONDS);						
						final String reminderDateTimeStr = lstAP.get(0).getSremainderauditdatetime();
				        
				        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
				        LocalDateTime reminderLocalDateTime = LocalDateTime.parse(reminderDateTimeStr , dateTimeFormatter);

				        // Convert LocalDateTime -> Instant (using system default zone)
				        final Instant reminderInstant = reminderLocalDateTime .atZone(ZoneId.systemDefault()).toInstant();

				        System.out.println("LocalDateTime: " + reminderLocalDateTime);
				        System.out.println("Instant: " + reminderInstant);
				        Instant resultInstant;
				        if(reminderInstant.compareTo(truncatedInstant)<=0) {
				        	resultInstant=truncatedInstant;
				        }else {
				        	resultInstant=reminderInstant;
				        }				
						
				        final String str=" select cm.ncontrolcode from controlmaster cm join sitecontrolmaster scm on cm.ncontrolcode =scm.ncontrolcode "
								  +" where cm.nformcode ="+userInfo.getNformcode()+" and cm.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
								  +" and scm.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and scontrolname='ScheduleMail' ";
				        final List<ControlMaster> lstCM = (List<ControlMaster>) jdbcTemplate.query(str, new ControlMaster());

						final Short controlCodeShort = lstCM.get(0).getNcontrolcode();
						final int controlCode = (controlCodeShort != null) ? controlCodeShort.intValue() : 0;
						mailMap.put("ncontrolcode",  controlCode);
						mailMap.put("nauditplancode",objAuditPlan.getNauditplancode());
						mailMap.put("ssystemid",lstAP.get(0).getSauditid());
						mailMap.put("remainderDate",resultInstant);
						emailDAOSupport.createEmailAlertTransaction(mailMap, userInfo);
						//end
						
						if(!sControlName.equals("schedule")) {
							objAuditPlanHistory .setSrescheduleremarks(objAuditPlanHistory.getSremarks());
						}
						objAuditPlanHistory.setSauditdatetime(dateUtilityFunction.instantDateToStringWithFormat(objAuditPlan.getDauditdatetime(),userInfo.getSsitedatetime()));		
						objAuditPlanHistory.setNauditplancode(objAuditPlan.getNauditplancode());
						objAuditPlanHistory.setNtransactionstatus(transCode); 
						savedAuditPlanList.add(objAuditPlanHistory);			
						multilingualIDList.add(sAuditActionMsg);			
						auditUtilityFunction.fnInsertAuditAction(savedAuditPlanList, 1, null, multilingualIDList, userInfo);
						
						return editGetAuditPlanData(inputMap, userInfo);
			        }else {
			        	return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTCURRENTANDFUTURE",userInfo.getSlanguagefilename()),HttpStatus.EXPECTATION_FAILED);
			        }				
				}else {
					return new ResponseEntity<>(commonFunction.getMultilingualMessage(sActionMsg,userInfo.getSlanguagefilename()),HttpStatus.EXPECTATION_FAILED);
				}
			}else {
	        	return new ResponseEntity<>(commonFunction.getMultilingualMessage(validationMsg,userInfo.getSlanguagefilename()),HttpStatus.EXPECTATION_FAILED);
			}
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),userInfo.getSlanguagefilename()),HttpStatus.EXPECTATION_FAILED);
		}		
	}
	/**
	 * This method is used to retrieve list of Audit Date & Time,Scheduled,Rescheduled,Closed Based on the transaction status. 
	 * @param inputMap  [Map] map object with "soperation" and "userinfo" as keys for which the data is to be fetched
	 * @return response entity object holding response status and list of all data based on the transactionstatus
	 * 	else if the auditplan is not available, response will be returned as 'Already Deleted' with status code 417
	 * @throws Exception exception that are thrown from this DAO layer
	 */
	public ResponseEntity<Object> getTransactionDates(final Map<String, Object> inputMap,UserInfo userInfo) throws Exception {		
		
		//final ObjectMapper objmapper = new ObjectMapper();
		Map<String, Object> outputMap = new HashMap<String, Object>();
		
		final String sValidationQuery=" select  ap.nauditplancode,aph.ntransactionstatus,COALESCE(TO_CHAR(aph.dauditdatetime,'" + userInfo.getSpgsitedatetime() + "'),'-') as sauditdatetime,aph.ntzauditdatetime "
									 +" from auditplan ap join auditplanhistory aph on aph.nauditplancode =ap.nauditplancode "
									 +" where aph.nauditplanhistorycode=any	(select max(nauditplanhistorycode) from auditplanhistory where  nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and nsitecode="+userInfo.getNtranssitecode()+"	group by nauditplancode) "
									 +" and ap.nstatus ="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and aph.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and ap.nsitecode ="+userInfo.getNtranssitecode()+" and ap.nauditplancode="+inputMap.get("nauditplancode")+" ";
		final List<AuditPlan> lstAuditPlan = jdbcTemplate.query(sValidationQuery,new AuditPlan());
		if (lstAuditPlan.size()>0) {
			//Added by sonia on 13th Oct 2025 for jira id :SWSM-90
			//Start
			String strMemberQuery =" select * from auditplanmembers where nauditplancode ="+inputMap.get("nauditplancode")+" and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and nsitecode="+userInfo.getNtranssitecode()+" ";
			final List<AuditPlanMember> lstAPM = jdbcTemplate.query(strMemberQuery,new AuditPlanMember());
			
			String strAuditorQuery =" select * from auditplanauditors where nauditplancode ="+inputMap.get("nauditplancode")+" and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and nsitecode="+userInfo.getNtranssitecode()+" ";
			final List<AuditPlanAuditor> lstAPA = jdbcTemplate.query(strAuditorQuery,new AuditPlanAuditor()); 
			
			String validationMsg="";
			
			if (lstAPM.size()==0 && lstAPA.size()==0) {
			    validationMsg = "IDS_NOAUDITPLANMEMBERSANDAUDITORS";
			} else if (lstAPM.size()==0) {
			    validationMsg = "IDS_ADDAUDITPLANMEMBERS";
			} else if (lstAPA.size()==0) {
			    validationMsg = "IDS_ADDAUDITPLANAUDITORS";
			}
			if(lstAPM.size() > 0 && lstAPA.size()>0) {
				//End
				final String sControlName =(String) inputMap.get("soperation");
				boolean actionStatus=false;
				String sActionMsg="";
				if(sControlName.equals("schedule")) {				
					if(lstAuditPlan.get(0).getNtransactionstatus()==Enumeration.TransactionStatus.DRAFT.gettransactionstatus()) {
						actionStatus=true;
					}else {
						sActionMsg="IDS_SELECTONLYDRAFTRECORD";
					}
				}else if(sControlName.equals("reschedule")) {
					
					if(lstAuditPlan.get(0).getNtransactionstatus()==Enumeration.TransactionStatus.SCHEDULED.gettransactionstatus()) {
						actionStatus=true;
					}else {
						sActionMsg="IDS_SELECTONLYSCHEDULERECORD";
					}
				}else if(sControlName.equals("close")) {
					if(lstAuditPlan.get(0).getNtransactionstatus()==Enumeration.TransactionStatus.SCHEDULED.gettransactionstatus()) {  
						actionStatus=true;
					}else {
						sActionMsg="IDS_SELECTONLYSCHEDULERECORD";
					}
				}				
				if(actionStatus) {							
					if(lstAuditPlan.isEmpty()) {
						outputMap.put("date", Collections.emptyList());
					} else {						
						outputMap.put("date", lstAuditPlan);					
					}
					return new ResponseEntity<Object>(outputMap, HttpStatus.OK);
				}else {
					return new ResponseEntity<>(commonFunction.getMultilingualMessage(sActionMsg,userInfo.getSlanguagefilename()),HttpStatus.EXPECTATION_FAILED);
				}
			}else {
	        	return new ResponseEntity<>(commonFunction.getMultilingualMessage(validationMsg,userInfo.getSlanguagefilename()),HttpStatus.EXPECTATION_FAILED);
			}			
		}else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),userInfo.getSlanguagefilename()),HttpStatus.EXPECTATION_FAILED);
		}
	}
	/**
	 * This method is used to add a new entry in auditplanhistory table and update an entry in the auditplan table. 
	 * @param inputMap [Map] map object holding details to be updated in auditplan table and added in the auditplanhistory table.
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return a response entity with list of available auditplan object
	 *   else if the auditplan record status is not scheduled/rescheduled, response will be returned as 'Select Scheduled/Rescheduled Record Only' with status code 417
	 *   else if the auditplan date and time returns validation 'Select Current and Future Dates Only' when choosing paste dates with status code 417
	 *   else if the auditplan to be updated is not available, response will be returned as 'Already Deleted' with status code 417
	 * @exception Exception that are thrown from this DAO layer
	 */
	public ResponseEntity<Object> closeAuditPlan(final Map<String, Object> inputMap, final UserInfo userInfo)throws Exception {
		
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> savedAuditPlanList = new ArrayList<>();
		
		final ObjectMapper objmapper = new ObjectMapper();
		objmapper.registerModule(new JavaTimeModule());	
		
		
		final AuditPlan objAuditPlan = objmapper.convertValue(inputMap.get("auditplan"),new TypeReference<AuditPlan>() {});
		final AuditPlanHistory objAuditPlanHistory = objmapper.convertValue(inputMap.get("auditplanhistory"),new TypeReference<AuditPlanHistory>() {});

		
		final String sValidationQuery=" select  ap.nauditplancode,aph.ntransactionstatus from auditplan ap join auditplanhistory aph on aph.nauditplancode =ap.nauditplancode "
									 +" where aph.nauditplanhistorycode=any	(select max(nauditplanhistorycode) from auditplanhistory where  nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and nsitecode="+userInfo.getNtranssitecode()+"	group by nauditplancode) "
									 +" and ap.nstatus ="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and aph.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and ap.nsitecode ="+userInfo.getNtranssitecode()+" and ap.nauditplancode="+objAuditPlan.getNauditplancode()+" ";
		final AuditPlan activeAuditPlan = (AuditPlan) jdbcUtilityFunction.queryForObject(sValidationQuery, AuditPlan.class, jdbcTemplate);
		if (activeAuditPlan != null) {
			if(activeAuditPlan.getNtransactionstatus()==Enumeration.TransactionStatus.SCHEDULED.gettransactionstatus()) {  
				
				Instant instant = objAuditPlan.getDauditdatetime();
		        // Convert to LocalDate (removes time part)
		        LocalDate dateOnly = instant.atZone(ZoneOffset.UTC).toLocalDate();
		        LocalDate today = LocalDate.now();
		        
		        if(dateOnly.compareTo(today)>=0){	        	
					
		        	/*final String dAuditDateTime=(String) objAuditPlan.getDauditdatetime().toString();
					
					final short ntzAuditDateTime =objAuditPlan.getNtzauditdatetime();
					
						
					final List<String> secondList = new ArrayList<>();		
					final Map<String,Object> utcConvertedData  = projectDAOSupport.convertToUtcBasedOnTimeZone(dAuditDateTime,ntzAuditDateTime,userInfo);
				    
				    
				    final String formattedAuditDateTime = (String) utcConvertedData.get("dateTime"); 
				    final Short timeZoneCode = (Short) utcConvertedData.get("ntzdatetime"); 
				    final boolean flag =(boolean) utcConvertedData.get("flag"); 

				    objAuditPlan.setSauditdatetime(formattedAuditDateTime);			
				    objAuditPlan.setNtzauditdatetime(timeZoneCode);
					
					if(flag) {
					    final Short inputTimeZoneCode = (Short) utcConvertedData.get("ninputtimezonecode"); 
					    objAuditPlan.setNinputtimezonecode(inputTimeZoneCode);
						secondList.add("ninputtimezonecode");
					}else {
						secondList.add("ntzauditdatetime"); 
					}
								 
					final AuditPlan convertedObject = objmapper.convertValue(
							dateUtilityFunction.convertInputDateToUTCByZone(objAuditPlan,
									Arrays.asList("sauditdatetime"), secondList, true, userInfo),
							new TypeReference<AuditPlan>() {});*/
					
					
					final String updateQuery = "update auditplan set dauditdatetime='"+ objAuditPlan.getDauditdatetime() + "',"
							 				 + "ntzauditdatetime="+ userInfo.getNtimezonecode() + ","
							 				 + "noffsetdauditdatetime="+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ","
							 				 + "dmodifieddate='"+ dateUtilityFunction.getCurrentDateTime(userInfo) + "',"
							 				 + "nsitecode=" + userInfo.getNtranssitecode()+ " " 
							 				 + "where nauditplancode=" + objAuditPlan.getNauditplancode() + ";";
					jdbcTemplate.execute(updateQuery);
					

					
					int auditPlanHistorySeqNo = (int) jdbcUtilityFunction.queryForObject("select nsequenceno from seqnoauditplan where stablename='auditplanhistory'", Integer.class,jdbcTemplate);
					auditPlanHistorySeqNo++;

					
					
					String auditPlanInsert = "Insert into auditplanhistory(nauditplanhistorycode,nauditplancode,ntransactionstatus,nusercode,nuserrolecode,"
										   + "ndeputyusercode,ndeputyuserrolecode,sremarks,dauditdatetime,noffsetdauditdatetime,ntzauditdatetime,dcreateddate,ntzcreateddate,noffsetdcreateddate,dmodifieddate,nsitecode,nstatus)"
										   + "values(" + auditPlanHistorySeqNo + "," + objAuditPlan.getNauditplancode() + "," + Enumeration.TransactionStatus.CLOSED.gettransactionstatus() + ","
										   + " "+userInfo.getNusercode() + ", " + userInfo.getNuserrole() +" , "+userInfo.getNdeputyusercode()+", "+userInfo.getNdeputyuserrole()+", "
										   + " N'"+stringUtilityFunction.replaceQuote(objAuditPlanHistory.getSremarks())+"','"+objAuditPlan.getDauditdatetime()+"',"
										   + " "+dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())+","+userInfo.getNtimezonecode()+","
										   + " '"+dateUtilityFunction.getCurrentDateTime(userInfo)+"',"+userInfo.getNtimezonecode()+","+dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())+","
										   + "'"+dateUtilityFunction.getCurrentDateTime(userInfo)+"',"
										   + " "+userInfo.getNtranssitecode() + "," + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+ " );";
					
					

					auditPlanInsert += "update seqnoauditplan set nsequenceno = " + auditPlanHistorySeqNo + " where stablename='auditplanhistory';";
					jdbcTemplate.execute(auditPlanInsert);					
					inputMap.put("nauditplancode", objAuditPlan.getNauditplancode());
					objAuditPlanHistory.setNauditplancode(objAuditPlan.getNauditplancode());
					objAuditPlanHistory.setNtransactionstatus((short) Enumeration.TransactionStatus.CLOSED.gettransactionstatus()); 
					objAuditPlanHistory.setSauditdatetime(dateUtilityFunction.instantDateToStringWithFormat(objAuditPlan.getDauditdatetime(),userInfo.getSsitedatetime()));		
					savedAuditPlanList.add(objAuditPlanHistory);			
					multilingualIDList.add("IDS_CLOSEAUDITPLAN");			
					auditUtilityFunction.fnInsertAuditAction(savedAuditPlanList, 1, null, multilingualIDList, userInfo);
					return editGetAuditPlanData(inputMap, userInfo);
		        }else {
		        	return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTCURRENTANDFUTURE",userInfo.getSlanguagefilename()),HttpStatus.EXPECTATION_FAILED);
		        }				
			}else {				
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTONLYSCHEDULEDRECORD",userInfo.getSlanguagefilename()),HttpStatus.EXPECTATION_FAILED);
			}	
		} else {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),userInfo.getSlanguagefilename()),HttpStatus.EXPECTATION_FAILED);
		}		
	}	
	/**
	 * This method is used to retrieve list of all available Users for the specified site.
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return response entity  object holding response status and list of all active Users
	 * 	 else if the auditplan is not available, response will be returned as 'Already  Deleted' with status code 417
	 *   else if the auditplan record status is not draft, response will be returned as 'Select Draft Record Only' with status code 417
	 * @throws Exception that are thrown from this DAO layer
	 */
	public ResponseEntity<Object> getAvailableAuditPlanMember(final Map<String, Object> inputMap, final UserInfo userInfo)	throws Exception {
		
		final Map<String, Object> outputMap = new HashMap<String, Object>();	
		int nauditPlanCode =(int) inputMap.get("nauditplancode");
		final String sValidationQuery=" select  ap.nauditplancode,aph.ntransactionstatus from auditplan ap join auditplanhistory aph on aph.nauditplancode =ap.nauditplancode "
				 					 +" where aph.nauditplanhistorycode=any	(select max(nauditplanhistorycode) from auditplanhistory where  nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and nsitecode="+userInfo.getNtranssitecode()+"	group by nauditplancode) "
				 					 +" and ap.nstatus ="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and aph.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and ap.nsitecode ="+userInfo.getNtranssitecode()+" and ap.nauditplancode="+nauditPlanCode+" ";	
		final AuditPlan auditplan =  (AuditPlan) jdbcUtilityFunction.queryForObject(sValidationQuery, AuditPlan.class, jdbcTemplate);
		if(auditplan!=null) {
			if(auditplan.getNtransactionstatus()==Enumeration.TransactionStatus.DRAFT.gettransactionstatus()) {		

				final String strQuery = " SELECT nusercode ,sfirstname||' '||slastname  as susername FROM users u "
							  		  + " WHERE NOT EXISTS ( "
							  		  + " SELECT 1 FROM auditplanmembers apm "
							  		  + " WHERE apm.nusercode = u.nusercode "
							  		  + "  AND apm.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" AND apm.nsitecode = "+userInfo.getNtranssitecode()+" and apm.nauditplancode="+nauditPlanCode+" "
							  		  + " )  AND u.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" AND u.nsitecode = "+userInfo.getNmastersitecode()+" and u.nusercode >0 ";
				final List<Users> lstUsers = (List<Users>) jdbcTemplate.query(strQuery, new Users());
				outputMap.put("Users", lstUsers);		
				return new ResponseEntity<Object>(outputMap, HttpStatus.OK);
			}else {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTONLYDRAFTRECORD",userInfo.getSlanguagefilename()),HttpStatus.EXPECTATION_FAILED);
			}
		}else {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),userInfo.getSlanguagefilename()),HttpStatus.EXPECTATION_FAILED);
		}
	}
	/**
	 * This method is used to retrieve list of all available auditmaster for the specified site.
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return response entity  object holding response status and list of all active auditmaster
	 * 	 else if the auditplan is not available, response will be returned as 'Already  Deleted' with status code 417
	 *   else if the auditplan record status is not draft, response will be returned as 'Select Draft Record Only' with status code 417
	 * @throws Exception that are thrown from this DAO layer
	 */
	public ResponseEntity<Object> getAvailableAuditPlanAuditor(final Map<String, Object> inputMap, final UserInfo userInfo)	throws Exception {
		final Map<String, Object> outputMap = new HashMap<String, Object>();
		int nauditPlanCode =(int) inputMap.get("nauditplancode");
		final String sValidationQuery=" select  ap.nauditplancode,aph.ntransactionstatus from auditplan ap join auditplanhistory aph on aph.nauditplancode =ap.nauditplancode "
				 					 +" where aph.nauditplanhistorycode=any	(select max(nauditplanhistorycode) from auditplanhistory where  nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and nsitecode="+userInfo.getNtranssitecode()+"	group by nauditplancode) "
				 					 +" and ap.nstatus ="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and aph.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and ap.nsitecode ="+userInfo.getNtranssitecode()+" and ap.nauditplancode="+nauditPlanCode+" ";	
		final AuditPlan auditplan =  (AuditPlan) jdbcUtilityFunction.queryForObject(sValidationQuery, AuditPlan.class, jdbcTemplate);
		if(auditplan!=null) {
			if(auditplan.getNtransactionstatus()==Enumeration.TransactionStatus.DRAFT.gettransactionstatus()) {		
		
				final String strQuery ="SELECT * FROM auditmaster am "
							  		  +"WHERE NOT EXISTS ( "
							  		  +"SELECT 1 FROM auditplanauditors apa "
							  		  +"WHERE apa.nauditmastercode = am.nauditmastercode "
							  		  +"AND apa.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" AND apa.nsitecode = "+userInfo.getNtranssitecode()+" AND apa.nauditplancode="+nauditPlanCode+" "
							  		  +")  AND am.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" AND am.nsitecode = "+userInfo.getNmastersitecode()+" and nauditmastercode >0" ;
		
				final List<AuditMaster> lstAuditMaster = (List<AuditMaster>) jdbcTemplate.query(strQuery, new AuditMaster());
				outputMap.put("auditMaster", lstAuditMaster);
				return new ResponseEntity<Object>(outputMap, HttpStatus.OK);
			}else {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTONLYDRAFTRECORD",userInfo.getSlanguagefilename()),HttpStatus.EXPECTATION_FAILED);
			}
		}else {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),userInfo.getSlanguagefilename()),HttpStatus.EXPECTATION_FAILED);
		}

	}
	/**
	 * This method is used to add a new entry to "auditplanmembers" table.
	 * @param lstAuditPlanMember[AuditPlanMember]  list holding details to be added in auditplanmembers table,
	 * @param userInfo [UserInfo] holding logged in user details based on 
	 * 							  which the list is to be fetched
	 * @return saved "auditplanmembers" object with status code 200 if saved successfully 
	 * 	 else if the auditplan is not available, response will be returned as 'Already  Deleted' with status code 417
	 *   else if the auditplan record status is not draft, response will be returned as 'Select Draft Record Only' with status code 417	 
	 * @throws Exception that are thrown from this DAO layer
	 */
	//Modified by sonia on 9th sept 2025 for jira id:SWSM-6
	public ResponseEntity<Object> createAuditPlanMember(final List<AuditPlanMember> lstAuditPlanMember, final UserInfo userInfo)throws Exception {		
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> savedAuditPlanMemberList = new ArrayList<>();	
		int nauditPlanCode = lstAuditPlanMember.get(0).getNauditplancode();
		
		final String sValidationQuery=" select  ap.nauditplancode,aph.ntransactionstatus from auditplan ap join auditplanhistory aph on aph.nauditplancode =ap.nauditplancode "
				 					 +" where aph.nauditplanhistorycode=any	(select max(nauditplanhistorycode) from auditplanhistory where  nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and nsitecode="+userInfo.getNtranssitecode()+"	group by nauditplancode) "
				 					 +" and ap.nstatus ="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and aph.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and ap.nsitecode ="+userInfo.getNtranssitecode()+" and ap.nauditplancode="+nauditPlanCode+" ";
		final AuditPlan activeAuditPlan = (AuditPlan) jdbcUtilityFunction.queryForObject(sValidationQuery, AuditPlan.class, jdbcTemplate);
		if (activeAuditPlan != null) {
			if(activeAuditPlan.getNtransactionstatus()==Enumeration.TransactionStatus.DRAFT.gettransactionstatus()) {		

				final String userCode = stringUtilityFunction.fnDynamicListToString(lstAuditPlanMember, "getNusercode");
				
				final String strQuery = "lock table lockauditplan " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus()+ "; ";
				jdbcTemplate.execute(strQuery);
								
				final String strSelectSeqno = "select nsequenceno from seqnoauditplan  where stablename ='auditplanmembers'";
				int auditPlanMemberSeqNo = jdbcTemplate.queryForObject(strSelectSeqno, Integer.class);
				
				final int addedUserCount = auditPlanMemberSeqNo  + lstAuditPlanMember.size();

				String sInsertQuery = " INSERT INTO auditplanmembers (nauditplanmembercode, nauditplancode, nusercode,dmodifieddate, nsitecode, nstatus) "
								   + " SELECT rank() over(order by nusercode asc)+ " + auditPlanMemberSeqNo + " as nauditplanmembercode, "
								   + " "+ nauditPlanCode + " , nusercode, '" + dateUtilityFunction.getCurrentDateTime(userInfo) + "',"
								   + " "+ userInfo.getNtranssitecode() + ", " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
								   + " from users u 	where u.nsitecode ="	+ userInfo.getNmastersitecode()+" and u.nusercode in (" + userCode + ") "
								   + " and NOT EXISTS ( SELECT * FROM auditplanmembers apm WHERE apm.nusercode = u.nusercode  and apm.nauditplancode=" + nauditPlanCode + ""
								   + " and apm.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()  + ")";
				jdbcTemplate.execute(sInsertQuery);

				final String sUpdateSeqNoQuery = "update seqnoauditplan set nsequenceno =" + addedUserCount 	+ " where stablename = 'auditplanmembers';";
				jdbcTemplate.execute(sUpdateSeqNoQuery);			
				
				final String sAuditQuery = " SELECT rank() over(order by nusercode asc)+ " + auditPlanMemberSeqNo + " as nauditplanmembercode, "
										 + nauditPlanCode + " as nauditplancode , nusercode, dmodifieddate,	" +userInfo.getNtranssitecode() + " as nsitecode, "
										 + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " as nstatus from users where nusercode in (" + userCode + ")";
				
				List<AuditPlanMember> auditPlanMemberList = (List<AuditPlanMember>) jdbcTemplate.query(sAuditQuery, new AuditPlanMember());
				
				savedAuditPlanMemberList.add(auditPlanMemberList);			
				multilingualIDList.add("IDS_ADDAUDITPLANMEMBER");			
				auditUtilityFunction.fnInsertListAuditAction(savedAuditPlanMemberList, 1, null, multilingualIDList, userInfo);		

				return getAuditPlanMember(nauditPlanCode,userInfo);				
			}else {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTONLYDRAFTRECORD",userInfo.getSlanguagefilename()),HttpStatus.EXPECTATION_FAILED);
			}	
		} else {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),userInfo.getSlanguagefilename()),HttpStatus.EXPECTATION_FAILED);
		}		
	}
	/**
	 * This method is used to add a new entry to "auditplanauditors" table.
	 * @param lstAuditPlanAuditor[AuditPlanAuditor]  list holding details to be added in auditplanauditors table,
	 * @param userInfo [UserInfo] holding logged in user details based on 
	 * 							  which the list is to be fetched
	 * @return saved "auditplanauditors" object with status code 200 if saved successfully 
	 * 	 else if the auditplan is not available, response will be returned as 'Already  Deleted' with status code 417
	 *   else if the auditplan record status is not draft, response will be returned as 'Select Draft Record Only' with status code 417	 
	 * @throws Exception that are thrown from this DAO layer
	 */
	//Modified by sonia on 9th sept 2025 for jira id:SWSM-6
	public ResponseEntity<Object> createAuditPlanAuditor(final List<AuditPlanAuditor> lstAuditPlanAuditor, final UserInfo userInfo)throws Exception {		
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> savedAuditPlanAuditorList = new ArrayList<>();	
		int nauditPlanCode = lstAuditPlanAuditor.get(0).getNauditplancode();		
		

		final String sValidationQuery=" select  ap.nauditplancode,aph.ntransactionstatus from auditplan ap join auditplanhistory aph on aph.nauditplancode =ap.nauditplancode "
				 					 +" where aph.nauditplanhistorycode=any	(select max(nauditplanhistorycode) from auditplanhistory where  nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and nsitecode="+userInfo.getNtranssitecode()+"	group by nauditplancode) "
				 					 +" and ap.nstatus ="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and aph.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and ap.nsitecode ="+userInfo.getNtranssitecode()+" and ap.nauditplancode="+nauditPlanCode+" ";
		final AuditPlan activeAuditPlan = (AuditPlan) jdbcUtilityFunction.queryForObject(sValidationQuery, AuditPlan.class, jdbcTemplate);
		if (activeAuditPlan != null) {
			if(activeAuditPlan.getNtransactionstatus()==Enumeration.TransactionStatus.DRAFT.gettransactionstatus()) {	
				
				final String auditMasterCode = stringUtilityFunction.fnDynamicListToString(lstAuditPlanAuditor, "getNauditmastercode");

				
				final String strQuery = "lock table lockauditplan " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus()+ "; ";
				jdbcTemplate.execute(strQuery);				
				
				final String strSelectSeqno = "select nsequenceno from seqnoauditplan  where stablename ='auditplanauditors'";
				int auditPlanAuditorSeqNo = jdbcTemplate.queryForObject(strSelectSeqno, Integer.class);
				
				final int addedMasterCount = auditPlanAuditorSeqNo  + lstAuditPlanAuditor.size();

				String sInsertQuery = " INSERT INTO auditplanauditors (nauditplanauditorcode, nauditplancode, nauditmastercode,dmodifieddate, nsitecode, nstatus) "
						   			+ " SELECT rank() over(order by nauditmastercode asc)+ " + auditPlanAuditorSeqNo + " as nauditplanauditorcode, "
						   			+ " "+ nauditPlanCode + " , nauditmastercode, '" + dateUtilityFunction.getCurrentDateTime(userInfo) + "',"
						   			+ " "+ userInfo.getNtranssitecode() + ", " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
						   			+ " from auditmaster am where am.nsitecode ="	+ userInfo.getNmastersitecode()+" and am.nauditmastercode in (" + auditMasterCode + ") "
						   			+ " and NOT EXISTS ( SELECT * FROM auditplanauditors apa WHERE apa.nauditmastercode = am.nauditmastercode  and apa.nauditplancode=" + nauditPlanCode + ""
						   			+ " and apa.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()  + ")";
				jdbcTemplate.execute(sInsertQuery);

				final String sUpdateSeqNoQuery = "update seqnoauditplan set nsequenceno =" + addedMasterCount 	+ " where stablename = 'auditplanauditors';";
				jdbcTemplate.execute(sUpdateSeqNoQuery);
					
				
				final String sAuditQuery = " SELECT rank() over(order by nauditmastercode asc)+ " + auditPlanAuditorSeqNo + " as nauditplanauditorcode, "
										 + nauditPlanCode + " as nauditplancode , nauditmastercode, dmodifieddate,	" +userInfo.getNtranssitecode() + " as nsitecode, "
										 + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " as nstatus from auditmaster where nauditmastercode in (" + auditMasterCode + ")";
				List<AuditPlanAuditor> auditPlanAuditorList = (List<AuditPlanAuditor>) jdbcTemplate.query(sAuditQuery, new AuditPlanAuditor());

				savedAuditPlanAuditorList.add(auditPlanAuditorList);					
				multilingualIDList.add("IDS_ADDAUDITPLANAUDITOR");			
				auditUtilityFunction.fnInsertListAuditAction(savedAuditPlanAuditorList, 1, null, multilingualIDList, userInfo);		

				return getAuditPlanAuditor(nauditPlanCode,userInfo);				
			}else {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTONLYDRAFTRECORD",userInfo.getSlanguagefilename()),HttpStatus.EXPECTATION_FAILED);
			}	
		} else {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),userInfo.getSlanguagefilename()),HttpStatus.EXPECTATION_FAILED);
		}	
	}
	/**
	 * This method is used to delete an entry in "auditplanmembers" table
	 * Need to check the record is already deleted or not
	 * @param inputMap [Map] map object holding detail to be deleted from auditplanmembers table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return a response entity with list of available auditplanmembers object
	 *   else if the auditplan record status is not draft, response will be returned as 'Select Draft Record Only' with status code 417
	 *   else if the auditplan is not available, response will be returned as 'Already  Deleted' with status code 417	
	 *   else if the auditplanmember is not available, response will be returned as 'Already  Deleted' with status code 417
	 * @exception Exception that are thrown from this DAO layer
	 */
	public ResponseEntity<Object> deleteAuditPlanMember(final Map<String, Object> inputMap, final UserInfo userInfo)throws Exception {
		
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> deleteAuditPlanMemberList = new ArrayList<>();
		
		final ObjectMapper objmapper = new ObjectMapper();	
		objmapper.registerModule(new JavaTimeModule());	

		final AuditPlanMember objAuditPlanMember = objmapper.convertValue(inputMap.get("auditplanmember"), AuditPlanMember.class);		
		
		final String sValidationQuery=" select  ap.nauditplancode,aph.ntransactionstatus from auditplan ap join auditplanhistory aph on aph.nauditplancode =ap.nauditplancode "
									 +" where aph.nauditplanhistorycode=any	(select max(nauditplanhistorycode) from auditplanhistory where  nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and nsitecode="+userInfo.getNtranssitecode()+"	group by nauditplancode) "
									 +" and ap.nstatus ="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and aph.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and ap.nsitecode ="+userInfo.getNtranssitecode()+" and ap.nauditplancode="+objAuditPlanMember.getNauditplancode()+" ";
		final AuditPlan activeAuditPlan = (AuditPlan) jdbcUtilityFunction.queryForObject(sValidationQuery, AuditPlan.class, jdbcTemplate);
		if (activeAuditPlan != null) {
			if(activeAuditPlan.getNtransactionstatus()==Enumeration.TransactionStatus.DRAFT.gettransactionstatus()) {
				
				final String activeQuery="select * from auditplanmembers where nauditplanmembercode="+objAuditPlanMember.getNauditplanmembercode()+" and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and nsitecode=" + userInfo.getNtranssitecode()+ " ";
				final AuditPlanMember activeAuditPlanMember = (AuditPlanMember) jdbcUtilityFunction.queryForObject(activeQuery, AuditPlanMember.class, jdbcTemplate);
				if(activeAuditPlanMember !=null) {
					final String updateQuery = "update auditplanmembers set nstatus='"+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + "',"
							 				 + "dmodifieddate='"+ dateUtilityFunction.getCurrentDateTime(userInfo) + "',"
							 				 + "nsitecode=" + userInfo.getNtranssitecode()+ " " 
							 				 + "where nauditplancode=" + objAuditPlanMember.getNauditplancode() + " and nauditplanmembercode="+objAuditPlanMember.getNauditplanmembercode()+" ;";
					jdbcTemplate.execute(updateQuery);
					int nauditPlanCode =objAuditPlanMember.getNauditplancode();
					deleteAuditPlanMemberList.add(objAuditPlanMember);
					multilingualIDList.add("IDS_DELETEAUDITPLANMEMBER");	
					auditUtilityFunction.fnInsertAuditAction(deleteAuditPlanMemberList, 1, null, multilingualIDList, userInfo);
					return getAuditPlanMember(nauditPlanCode,userInfo);
				}else {
					return new ResponseEntity<>(
							commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),userInfo.getSlanguagefilename()),HttpStatus.EXPECTATION_FAILED);
				}
				
			}else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage("IDS_SELECTONLYDRAFTRECORD",userInfo.getSlanguagefilename()),HttpStatus.EXPECTATION_FAILED);
			}	
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),userInfo.getSlanguagefilename()),HttpStatus.EXPECTATION_FAILED);
		}		
	}
	/**
	 * This method is used to delete an entry in "auditplanauditors" table
	 * Need to check the record is already deleted or not
	 * @param inputMap [Map] map object holding detail to be deleted from auditplanauditors table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return a response entity with list of available auditplanauditors object
	 *   else if the auditplan record status is not draft, response will be returned as 'Select Draft Record Only' with status code 417
	 *   else if the auditplan is not available, response will be returned as 'Already  Deleted' with status code 417	
	 *   else if the auditplanauditor is not available, response will be returned as 'Already  Deleted' with status code 417
	 * @exception Exception that are thrown from this DAO layer
	 */
	public ResponseEntity<Object> deleteAuditPlanAuditor(final Map<String, Object> inputMap, final UserInfo userInfo)throws Exception {
		
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> deleteAuditPlanAuditorList = new ArrayList<>();		
		final ObjectMapper objmapper = new ObjectMapper();		
		objmapper.registerModule(new JavaTimeModule());	

		final AuditPlanAuditor objAuditPlanAuditor = objmapper.convertValue(inputMap.get("auditplanauditor"), AuditPlanAuditor.class);		
		
		final String sValidationQuery=" select  ap.nauditplancode,aph.ntransactionstatus from auditplan ap join auditplanhistory aph on aph.nauditplancode =ap.nauditplancode "
									 +" where aph.nauditplanhistorycode=any	(select max(nauditplanhistorycode) from auditplanhistory where  nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and nsitecode="+userInfo.getNtranssitecode()+"	group by nauditplancode) "
									 +" and ap.nstatus ="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and aph.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and ap.nsitecode ="+userInfo.getNtranssitecode()+" and ap.nauditplancode="+objAuditPlanAuditor.getNauditplancode()+" ";
		final AuditPlan activeAuditPlan = (AuditPlan) jdbcUtilityFunction.queryForObject(sValidationQuery, AuditPlan.class, jdbcTemplate);
		if (activeAuditPlan != null) {
			if(activeAuditPlan.getNtransactionstatus()==Enumeration.TransactionStatus.DRAFT.gettransactionstatus()) {
				
				final String activeQuery="select * from auditplanauditors where nauditplanauditorcode="+objAuditPlanAuditor.getNauditplanauditorcode()+" and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and nsitecode=" + userInfo.getNtranssitecode()+ " ";
				final AuditPlanMember activeAuditPlanMember = (AuditPlanMember) jdbcUtilityFunction.queryForObject(activeQuery, AuditPlanMember.class, jdbcTemplate);
				
				if(activeAuditPlanMember !=null) {
					final String updateQuery = "update auditplanauditors set nstatus='"+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + "',"
							 				 + "dmodifieddate='"+ dateUtilityFunction.getCurrentDateTime(userInfo) + "',"
							 				 + "nsitecode=" + userInfo.getNtranssitecode()+ " " 
							 				 + "where nauditplancode=" + objAuditPlanAuditor.getNauditplancode() + " and nauditplanauditorcode="+objAuditPlanAuditor.getNauditplanauditorcode()+" ;";
					jdbcTemplate.execute(updateQuery);
					int nauditPlanCode =objAuditPlanAuditor.getNauditplancode();
					deleteAuditPlanAuditorList.add(objAuditPlanAuditor);
					multilingualIDList.add("IDS_DELETEAUDITPLANAUDITOR");	
					auditUtilityFunction.fnInsertAuditAction(deleteAuditPlanAuditorList, 1, null, multilingualIDList, userInfo);
					return getAuditPlanAuditor(nauditPlanCode,userInfo);
				}else {
					return new ResponseEntity<>(
							commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),userInfo.getSlanguagefilename()),HttpStatus.EXPECTATION_FAILED);
				}
				
				
			}else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage("IDS_SELECTONLYDRAFTRECORD",userInfo.getSlanguagefilename()),HttpStatus.EXPECTATION_FAILED);
			}	
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),userInfo.getSlanguagefilename()),HttpStatus.EXPECTATION_FAILED);
		}		
	}
	/**
	 * This method is used to add a new entry to "auditplanfile" table.
	 * @param lstAuditPlanFile[AuditPlanFile]  list holding details to be added in auditplanfile table,
	 * @param userInfo [UserInfo] holding logged in user details based on 
	 * 							  which the list is to be fetched
	 * @return saved "auditplanfile" object with status code 200 if saved successfully 
	 *   else if the auditplan record status is not draft, response will be returned as 'Select Draft Record Only' with status code 417
	 *   else if the auditplan is not available, response will be returned as 'Already  Deleted' with status code 417	
	 *   else if the ftp is not connected properly,response will be returned as 'Check FTP Connection' with status code 417
	 * @throws Exception that are thrown from this DAO layer
	 */
	public ResponseEntity<Object> createAuditPlanFile(final UserInfo userInfo, final MultipartHttpServletRequest request) throws Exception {

		final String sQuery = " lock  table lockauditplan " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
		jdbcTemplate.execute(sQuery);

		final ObjectMapper objMapper = new ObjectMapper();
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> savedAuditPlanFileList = new ArrayList<Object>();

		final List<AuditPlanFile> lstAuditPlanFile = objMapper.readValue(request.getParameter("auditplanfile"),new TypeReference<List<AuditPlanFile>>() {});
		if (lstAuditPlanFile != null && lstAuditPlanFile.size() > 0) {
			final String sValidationQuery=" select  ap.nauditplancode,aph.ntransactionstatus from auditplan ap join auditplanhistory aph on aph.nauditplancode =ap.nauditplancode "
					 					 +" where aph.nauditplanhistorycode=any	(select max(nauditplanhistorycode) from auditplanhistory where  nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and nsitecode="+userInfo.getNtranssitecode()+"	group by nauditplancode) "
					 					 +" and ap.nstatus ="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and aph.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and ap.nsitecode ="+userInfo.getNtranssitecode()+" and ap.nauditplancode="+lstAuditPlanFile.get(0).getNauditplancode()+" ";
			final AuditPlan activeAuditPlan = (AuditPlan) jdbcUtilityFunction.queryForObject(sValidationQuery, AuditPlan.class, jdbcTemplate);	
			if (activeAuditPlan != null) {
				//if (activeAuditPlan.getNtransactionstatus() == Enumeration.TransactionStatus.DRAFT.gettransactionstatus()){
					String sReturnString = Enumeration.ReturnStatus.SUCCESS.getreturnstatus();
					if (lstAuditPlanFile.get(0).getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()) {
						sReturnString = ftpUtilityFunction.getFileFTPUpload(request, -1, userInfo);
					}
					if ((Enumeration.ReturnStatus.SUCCESS.getreturnstatus()).equals(sReturnString)) {
						final Instant instantDate = dateUtilityFunction.getCurrentDateTime(userInfo).truncatedTo(ChronoUnit.SECONDS);
						final String sattachmentDate = dateUtilityFunction.instantDateToString(instantDate);
						final int offset = dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid());
						lstAuditPlanFile.forEach(objtf -> {
							objtf.setDcreateddate(instantDate);
							objtf.setNoffsetdcreateddate(offset);
							objtf.setScreateddate(sattachmentDate.replace("T", " "));
						});

						String sequencequery = "select nsequenceno from seqnoauditplan where stablename ='auditplanfile'";
						int nsequenceno = (int) jdbcUtilityFunction.queryForObject(sequencequery, Integer.class,jdbcTemplate);

						nsequenceno++;
						
						String insertquery = "Insert into auditplanfile(nauditplanfilecode,nauditplancode,nlinkcode,nattachmenttypecode,nfilesize,sfilename,"
										   + "sdescription,ssystemfilename,dcreateddate,noffsetdcreateddate,ntzcreateddate,dmodifieddate,nsitecode,nstatus)"
										   + "values (" + nsequenceno + "," + lstAuditPlanFile.get(0).getNauditplancode() + ","	+ lstAuditPlanFile.get(0).getNlinkcode() + ","
										   + lstAuditPlanFile.get(0).getNattachmenttypecode() + ","+ lstAuditPlanFile.get(0).getNfilesize() + "," 
										   + " N'"+ stringUtilityFunction.replaceQuote(lstAuditPlanFile.get(0).getSfilename()) + "',"
										   + " N'"+ stringUtilityFunction.replaceQuote(lstAuditPlanFile.get(0).getSdescription()) + "',"
										   + " N'"+ lstAuditPlanFile.get(0).getSsystemfilename() + "', "
										   + " '"+lstAuditPlanFile.get(0).getDcreateddate() + "'," + lstAuditPlanFile.get(0).getNoffsetdcreateddate() + "," + userInfo.getNtimezonecode()+", "
										   + " '"+dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + " "
										   + userInfo.getNtranssitecode()  + "," +Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+ ")";
						jdbcTemplate.execute(insertquery);

						String updatequery = "update seqnoauditplan set nsequenceno =" + nsequenceno + " where stablename ='auditplanfile'";
						jdbcTemplate.execute(updatequery);
						int nauditPlanCode =lstAuditPlanFile.get(0).getNauditplancode();

						multilingualIDList.add(lstAuditPlanFile.get(0).getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype() ? "IDS_ADDAUDITPLANFILE" : "IDS_ADDAUDITPLANLINK");						
						lstAuditPlanFile.get(0).setNauditplanfilecode(nsequenceno);
						savedAuditPlanFileList.add(lstAuditPlanFile);
						auditUtilityFunction.fnInsertListAuditAction(savedAuditPlanFileList, 1, null, multilingualIDList, userInfo);
						return getAuditPlanFile(nauditPlanCode, userInfo);
					} else {
						return new ResponseEntity<>(commonFunction.getMultilingualMessage(sReturnString, userInfo.getSlanguagefilename()),HttpStatus.EXPECTATION_FAILED);
					}
//				} else {
//					return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTONLYDRAFTRECORD",userInfo.getSlanguagefilename()),HttpStatus.EXPECTATION_FAILED);
//				}
			} else {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(), userInfo.getSlanguagefilename()),HttpStatus.EXPECTATION_FAILED);
			}
		} else {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_FILENOTFOUND", userInfo.getSlanguagefilename()),	HttpStatus.EXPECTATION_FAILED);
		}
	}
	/**
	 * This method is used to retrieve active auditplanfile object based on the specified nauditPlanFileCode.
	 * @param nauditPlanFileCode [int] primary key of auditplanfile object.
	 * @param userInfo [UserInfo] holding logged in user details based on 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of auditplanfile object
	 * 	 else if the auditplan is not available, response will be returned as 'Already  Deleted' with status code 417
	 * 	 else if the auditplan record status is not draft,  response will be returned as 'Select Draft Record Only' with status code 417	
	 * 	 else if the auditplanfile is not available, response will be returned as 'Already  Deleted' with status code 417
	 * @throws Exception that are thrown from this DAO layer
	 */
	public ResponseEntity<Object> getActiveAuditPlanFileById(final Map<String, Object> inputMap, final UserInfo userInfo) throws Exception {
		final Map<String, Object> outputMap = new HashMap<String, Object>();
		final int nauditPlanCode= (int) inputMap.get("nauditplancode");
		final int nauditPlanFileCode= (int) inputMap.get("nauditplanfilecode");
		
		final String sValidationQuery=" select  ap.nauditplancode,aph.ntransactionstatus from auditplan ap join auditplanhistory aph on aph.nauditplancode =ap.nauditplancode "
				 					 +" where aph.nauditplanhistorycode=any	(select max(nauditplanhistorycode) from auditplanhistory where  nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and nsitecode="+userInfo.getNtranssitecode()+"	group by nauditplancode) "
				 					 +" and ap.nstatus ="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and aph.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and ap.nsitecode ="+userInfo.getNtranssitecode()+" and ap.nauditplancode="+nauditPlanCode+" ";
		final AuditPlan activeAuditPlan = (AuditPlan) jdbcUtilityFunction.queryForObject(sValidationQuery, AuditPlan.class, jdbcTemplate);
		if (activeAuditPlan != null) {
			//if(activeAuditPlan.getNtransactionstatus()==Enumeration.TransactionStatus.DRAFT.gettransactionstatus()) {
				final String sQuery = "select * from auditplanfile where nauditplanfilecode = "+ nauditPlanFileCode + " and nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				final AuditPlanFile objAPF = (AuditPlanFile) jdbcUtilityFunction.queryForObject(sQuery,AuditPlanFile.class, jdbcTemplate);
				if (objAPF != null) {
					String queryformat = "COALESCE(TO_CHAR(apf.dcreateddate,'" + userInfo.getSpgsitedatetime() + "'),'-') ";
					
					final String protocolFileQry = " select apf.nauditplanfilecode,apf.nauditplancode,apf.noffsetdcreateddate,"
						     					 + " apf.nlinkcode, apf.nattachmenttypecode, apf.sfilename,apf.ssystemfilename, apf.sdescription,"
						     					 + " COALESCE(at.jsondata->'sattachmenttype'->>'" + userInfo.getSlanguagetypecode() + "',"
						     					 + " at.jsondata->'sattachmenttype'->>'en-US') as stypename, "
						     					 + " case when apf.nlinkcode = "+Enumeration.TransactionStatus.NA.gettransactionstatus()+" then '-' else lm.jsondata->>'slinkname' end slinkname,"
						     					 + " case when apf.nlinkcode = "+Enumeration.TransactionStatus.NA.gettransactionstatus()+" then cast(apf.nfilesize as text) else '-' end sfilesize,"
						     					 + " case when apf.nlinkcode = "+Enumeration.TransactionStatus.NA.gettransactionstatus()+" then " + queryformat + " else '-' end screateddate"
						     					 + " from auditplanfile apf "
						     					 + " join auditplan ap on ap.nauditplancode = apf.nauditplancode "
						     					 + " join attachmenttype at on at.nattachmenttypecode = apf.nattachmenttypecode "
						     					 + " join linkmaster lm on lm.nlinkcode = apf.nlinkcode "
						     					 + " where at.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " 
						     					 + " and lm.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " 
						     					 + " and apf.nstatus =" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
						     					 + " and ap.nstatus =" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " 
						     					 + " and apf.nsitecode="+userInfo.getNtranssitecode()+" "
						     					 + " and apf.nauditplancode = "+ nauditPlanCode + " and apf.nauditplanfilecode= "+nauditPlanFileCode+" ";
					outputMap.put("auditPlanFile",
							dateUtilityFunction.getSiteLocalTimeFromUTC(jdbcTemplate.query(protocolFileQry, new AuditPlanFile()),
									Arrays.asList("screateddate"), Arrays.asList(userInfo.getStimezoneid()), userInfo, false, null,false));
					return new ResponseEntity<Object>(outputMap, HttpStatus.OK);
				}else {
					return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
				}		
//			}else {
//				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTONLYDRAFTRECORD",userInfo.getSlanguagefilename()),HttpStatus.EXPECTATION_FAILED);
//			}
		}else {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		}
	}	
	/**
	 * This method is used to update entry in auditplanfile table.
	 * @param lstAuditPlanFile[AuditPlanFile]  list holding details to be updated in auditplanfile table,
	 * @param userInfo [UserInfo] holding logged in user details based on 
	 * 							  which the list is to be fetched
	 * @return saved "auditplanfile" object with status code 200 if saved successfully 
	 *   else if the auditplan record status is not draft, response will be returned as 'Select Draft Record Only' with status code 417
	 *   else if the auditplan is not available, response will be returned as 'Already  Deleted' with status code 417	
	 *   else if the auditplanfile is not available, response will be returned as 'Already  Deleted' with status code 417	
	 *   else if the ftp is not connected properly,response will be returned as 'Check FTP Connection' with status code 417
	 * @throws Exception that are thrown from this DAO layer
	 */
	public ResponseEntity<Object> updateAuditPlanFile(UserInfo userInfo, MultipartHttpServletRequest request) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> lstOldObject = new ArrayList<Object>();
		final List<Object> lstNewObject = new ArrayList<Object>();
		final List<AuditPlanFile> lstAuditPlanFile = objMapper.readValue(request.getParameter("auditplanfile"),new TypeReference<List<AuditPlanFile>>() {});

		if (lstAuditPlanFile != null && lstAuditPlanFile.size() > 0) {
			final AuditPlanFile objFile = lstAuditPlanFile.get(0);
			final String sValidationQuery=" select  ap.nauditplancode,aph.ntransactionstatus from auditplan ap join auditplanhistory aph on aph.nauditplancode =ap.nauditplancode "
					 					 +" where aph.nauditplanhistorycode=any	(select max(nauditplanhistorycode) from auditplanhistory where  nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and nsitecode="+userInfo.getNtranssitecode()+"	group by nauditplancode) "
					 					 +" and ap.nstatus ="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and aph.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and ap.nsitecode ="+userInfo.getNtranssitecode()+" and ap.nauditplancode="+lstAuditPlanFile.get(0).getNauditplancode()+" ";
			final AuditPlan activeAuditPlan = (AuditPlan) jdbcUtilityFunction.queryForObject(sValidationQuery, AuditPlan.class, jdbcTemplate);
			if (activeAuditPlan != null) {
				//if (activeAuditPlan.getNtransactionstatus() == Enumeration.TransactionStatus.DRAFT.gettransactionstatus()) {

					final int isFileEdited = Integer.valueOf(request.getParameter("isFileEdited"));
					String sReturnString = Enumeration.ReturnStatus.SUCCESS.getreturnstatus();

					if (isFileEdited == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
						if (objFile.getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()) {
							sReturnString = ftpUtilityFunction.getFileFTPUpload(request, -1, userInfo);
						}
					}

					if ((Enumeration.ReturnStatus.SUCCESS.getreturnstatus()).equals(sReturnString)) {
						final String sQuery = "select * from auditplanfile where nauditplanfilecode = "+ objFile.getNauditplanfilecode() + " and nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
						final AuditPlanFile objAPF = (AuditPlanFile) jdbcUtilityFunction.queryForObject(sQuery,AuditPlanFile.class, jdbcTemplate);
						if (objAPF != null) {
							String ssystemfilename = "";
							if (objFile.getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()) {
								ssystemfilename = objFile.getSsystemfilename();
							}
							final String sUpdateQuery = " update auditplanfile set sfilename=N'"+ stringUtilityFunction.replaceQuote(objFile.getSfilename()) + "',"
													  + " sdescription=N'" + stringUtilityFunction.replaceQuote(objFile.getSdescription())+ "',"
													  + " ssystemfilename= N'" + ssystemfilename + "',"
													  + " nattachmenttypecode = "+ objFile.getNattachmenttypecode() + ","
													  + " nlinkcode=" + objFile.getNlinkcode() + ","
													  + " nfilesize = " + objFile.getNfilesize() + ""
													  + ",dmodifieddate='"+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'"
													  + " where nauditplanfilecode = " + objFile.getNauditplanfilecode();

							objFile.setDcreateddate(objAPF.getDcreateddate());
							objAPF.setScreateddate(objFile.getScreateddate());

							jdbcTemplate.execute(sUpdateQuery);							

							int nauditPlanCode= lstAuditPlanFile.get(0).getNauditplancode();
							lstNewObject.add(objFile);
							lstOldObject.add(objAPF);
							multilingualIDList.add(objFile.getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()	? "IDS_EDITAUDITPLANFILE" : "IDS_EDITAUDITPLANLINK");
							auditUtilityFunction.fnInsertAuditAction(lstNewObject, 2, lstOldObject, multilingualIDList,	userInfo);							
							return getAuditPlanFile(nauditPlanCode, userInfo);
						} else {
							// status code:417
							return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
						}
					} else {
						return new ResponseEntity<>(
								commonFunction.getMultilingualMessage(sReturnString, userInfo.getSlanguagefilename()),
								HttpStatus.EXPECTATION_FAILED);
					}
//				} else {
//					return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTONLYDRAFTRECORD",userInfo.getSlanguagefilename()),HttpStatus.EXPECTATION_FAILED);
//				}
			} else {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_FILENOTFOUND", userInfo.getSlanguagefilename()),HttpStatus.EXPECTATION_FAILED);
		}
	}
	/**
	 * This method is used to delete an entry in auditplanfile table.
	 * @param objAuditPlanFile[AuditPlanFile] object holding details to be updated in auditplanfile table,
	 * @param userInfo [UserInfo] holding logged in user details based on 
	 * 							  which the list is to be fetched
	 * @return a response entity with list of available auditplanfile object
	 *   else if the auditplan record status is not draft, response will be returned as 'Select Draft Record Only' with status code 417
	 *   else if the auditplan is not available, response will be returned as 'Already  Deleted' with status code 417	
	 *   else if the auditplanfile is not available, response will be returned as 'Already  Deleted' with status code 417	
	 * @throws Exception that are thrown from this DAO layer
	 */
	public ResponseEntity<Object> deleteAuditPlanFile(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();

		final AuditPlanFile objAuditPlanFile = objMapper.convertValue(inputMap.get("auditplanfile"), AuditPlanFile.class);	

		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> deleteAuditPlanFileList  = new ArrayList<>();
		

		final String sValidationQuery=" select  ap.nauditplancode,aph.ntransactionstatus from auditplan ap join auditplanhistory aph on aph.nauditplancode =ap.nauditplancode "
				 					 +" where aph.nauditplanhistorycode=any	(select max(nauditplanhistorycode) from auditplanhistory where  nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and nsitecode="+userInfo.getNtranssitecode()+"	group by nauditplancode) "
				 					 +" and ap.nstatus ="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and aph.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and ap.nsitecode ="+userInfo.getNtranssitecode()+" and ap.nauditplancode="+objAuditPlanFile.getNauditplancode()+" ";
		final AuditPlan activeAuditPlan = (AuditPlan) jdbcUtilityFunction.queryForObject(sValidationQuery, AuditPlan.class, jdbcTemplate);

		if (activeAuditPlan != null) {
			//if (activeAuditPlan.getNtransactionstatus() == Enumeration.TransactionStatus.DRAFT.gettransactionstatus()) {
				String sQuery = "select * from auditplanfile where nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nauditplanfilecode = "+ objAuditPlanFile.getNauditplanfilecode();
				final AuditPlanFile objAPF = (AuditPlanFile) jdbcUtilityFunction.queryForObject(sQuery, AuditPlanFile.class,jdbcTemplate);
				if (objAPF != null) {
					if (objAPF.getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()) {
						ftpUtilityFunction.deleteFTPFile(Arrays.asList(objAPF.getSsystemfilename()), "", userInfo);
					} else {
						objAPF.setScreateddate(null);
					}
					final String sUpdateQuery = "update auditplanfile set nstatus = "+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ",dmodifieddate='"+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where nauditplanfilecode = " + objAuditPlanFile.getNauditplanfilecode();
					jdbcTemplate.execute(sUpdateQuery);
					int nauditPlanCode =objAuditPlanFile.getNauditplancode();
					multilingualIDList.add(objAPF.getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype() ? "IDS_DELETEAUDITPLANFILE" : "IDS_DELETEAUDITPLANLINK");
					deleteAuditPlanFileList.add(objAPF);
					auditUtilityFunction.fnInsertAuditAction(deleteAuditPlanFileList, 1, null, multilingualIDList, userInfo);

					return getAuditPlanFile(nauditPlanCode, userInfo);
				} else {
					return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(), userInfo.getSlanguagefilename()),HttpStatus.EXPECTATION_FAILED);
				}
//			} else {
//				return new ResponseEntity<>(
//						commonFunction.getMultilingualMessage("IDS_SELECTONLYDRAFTRECORD",userInfo.getSlanguagefilename()),HttpStatus.EXPECTATION_FAILED);
//			}
		} else {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		}

	}
	/**
	 * This method is used to fetch a file/ link which need to view
	 * @param inputMap [Map] map object holding detail to be view from auditplanauditors table
	 * @param userInfo [UserInfo] holding logged in user details based on 
	 * 		  which the list is to be fetched
	 * @return response entity holds the list of AuditPlanFile details
	 *   else if the auditplan record status is not draft, response will be returned as 'Select Draft Record Only' with status code 417
	 *   else if the auditplan is not available, response will be returned as 'Already  Deleted' with status code 417	
	 *   else if the auditplanfile is not available, response will be returned as 'Already  Deleted' with status code 417	
	 * @throws Exception 
	 */ 
	public ResponseEntity<Object> viewAuditPlanFile(Map<String, Object> inputMap, UserInfo objUserInfo)	throws Exception {
		Map<String,Object> map=new HashMap<>();
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> viewAuditPlanFileList = new ArrayList<>();		
		int nauditPlanCode= (int) inputMap.get("nauditplancode");
		int nauditPlanFileCode= (int) inputMap.get("nauditplanfilecode");
		
		final String sValidationQuery=" select  ap.nauditplancode,aph.ntransactionstatus from auditplan ap join auditplanhistory aph on aph.nauditplancode =ap.nauditplancode "
				 					 +" where aph.nauditplanhistorycode=any	(select max(nauditplanhistorycode) from auditplanhistory where  nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and nsitecode="+objUserInfo.getNtranssitecode()+"	group by nauditplancode) "
				 					 +" and ap.nstatus ="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and aph.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and ap.nsitecode ="+objUserInfo.getNtranssitecode()+" and ap.nauditplancode="+nauditPlanCode+" ";
		final AuditPlan activeAuditPlan = (AuditPlan) jdbcUtilityFunction.queryForObject(sValidationQuery, AuditPlan.class, jdbcTemplate);
		if (activeAuditPlan != null) {
			//if(activeAuditPlan.getNtransactionstatus()==Enumeration.TransactionStatus.DRAFT.gettransactionstatus()) {
				String sQuery = "select * from auditplanfile where nauditplanfilecode = "+nauditPlanFileCode+" and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				final AuditPlanFile objAPF = (AuditPlanFile) jdbcUtilityFunction.queryForObject(sQuery, AuditPlanFile.class,jdbcTemplate);
				if(objAPF != null) {
					if(objAPF.getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()) {
						map = ftpUtilityFunction.FileViewUsingFtp(objAPF.getSsystemfilename(), -1, objUserInfo, "", "");
					} else {
						sQuery = "select jsondata->>'slinkname' as slinkname from linkmaster where nlinkcode="+objAPF.getNlinkcode()+" and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
						LinkMaster objlinkmaster = (LinkMaster) jdbcUtilityFunction.queryForObject(sQuery, LinkMaster.class,jdbcTemplate);
						map.put("AttachLink", objlinkmaster.getSlinkname()+objAPF.getSfilename());
						objAPF.setScreateddate(null);
					}
					multilingualIDList.add(objAPF.getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()? "IDS_VIEWAUDITPLANFILE": "IDS_VIEWAUDITPLANLINK");
					viewAuditPlanFileList.add(objAPF);
					auditUtilityFunction.fnInsertAuditAction(viewAuditPlanFileList, 1, null, multilingualIDList, objUserInfo);	
					return new ResponseEntity<Object>(map, HttpStatus.OK);

				}else {
					return new ResponseEntity<>(
							commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),objUserInfo.getSlanguagefilename()),HttpStatus.EXPECTATION_FAILED);
				}
//			}else {
//				return new ResponseEntity<>(
//						commonFunction.getMultilingualMessage("IDS_SELECTONLYDRAFTRECORD",objUserInfo.getSlanguagefilename()),HttpStatus.EXPECTATION_FAILED);
//			}
			
		}else {	
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),objUserInfo.getSlanguagefilename()),HttpStatus.EXPECTATION_FAILED);
		}
	}
	/**
	 * This method is used to retrieve active auditplan object based on the specified nauditPlanCode.
	 * @param nauditPlanCode [int] primary key of auditplan object.
	 * @param userInfo [UserInfo] holding logged in user details based on 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of auditplan object
	 * @throws Exception that are thrown from this DAO layer
	 */
	private AuditPlan AuditPlanAuditGet(int nauditPlanCode, UserInfo userInfo) throws Exception {
		final String strQuery = "select ap.nauditplancode,ap.naudittypecode,ap.nauditcategorycode,ap.nauditstandardcatcode,ap.ndeptcode, "
							  + "ap.ndeptheadcode,ap.saudittitle,ap.scompauditrep,ap.noffsetdauditdatetime,ap.ntzauditdatetime, "
							  + "ap.sauditid,at.saudittypename,ac.sauditcategoryname,asc1.sauditstandardcatname,d.sdeptname,"
							  + "u.sfirstname ||' '||u.slastname as sdeptheadname,tz.stimezoneid,aph.ntransactionstatus,cm.scolorhexcode, "
							  + "COALESCE(TO_CHAR(ap.dauditdatetime,'" + userInfo.getSpgsitedatetime() + "'),'-') as sauditdatetime,  "
							  + "COALESCE(ts.jsondata->'stransdisplaystatus'->>'"+userInfo.getSlanguagetypecode()+"',ts.jsondata->'stransdisplaystatus'->>'en-US') as stransdisplaystatus " 
							  + "from auditplan ap "
							  + "join audittype at on at.naudittypecode = ap.naudittypecode "
							  + "join auditcategory ac on ac.nauditcategorycode =ap.nauditcategorycode "
							  + "join auditstandardcategory asc1 on asc1.nauditstandardcatcode =ap.nauditstandardcatcode "
							  + "join department d on d.ndeptcode = ap.ndeptcode "
							  + "join users u on u.nusercode = ap.ndeptheadcode "
							  + "join timezone tz on tz.ntimezonecode =ap.ntzauditdatetime "
							  + "join auditplanhistory aph on aph.nauditplancode =ap.nauditplancode "
							  + "join transactionstatus ts on ts.ntranscode= aph.ntransactionstatus "
							  + "join formwisestatuscolor fsc on fsc.ntranscode =aph.ntransactionstatus "
							  + "join colormaster cm on cm.ncolorcode =fsc.ncolorcode " 
							  + "where ap.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
							  + "and at.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and ac.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
							  + "and asc1.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and d.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and u.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
							  + "and fsc.nformcode ="+userInfo.getNformcode()+" and ap.nsitecode ="+userInfo.getNtranssitecode()+" and ap.nauditplancode = "+nauditPlanCode+"";

		return (AuditPlan) jdbcUtilityFunction.queryForObject(strQuery, AuditPlan.class, jdbcTemplate);
	}	
	/**
	 * This method is used to retrieve active auditplan object based on the specified nauditPlanCode.
	 * @param inputMap [Map] map object holding detail of auditplan object. 
	 * @param userInfo [UserInfo] holding logged in user details based on 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of auditplan object
	 * @throws Exception that are thrown from this DAO layer
	 */
	@SuppressWarnings({ "unchecked" })
	private ResponseEntity<Object> editGetAuditPlanData(final Map<String, Object> inputMap, final UserInfo userInfo) throws Exception {
		//final ObjectMapper objMapper = new ObjectMapper();
		final Map<String, Object> outputMap = new HashMap<String, Object>();
		String fromDate = (String) inputMap.get("fromDate");
		String toDate = (String) inputMap.get("toDate");

		final DateTimeFormatter dbPattern = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
		final DateTimeFormatter uiPattern = DateTimeFormatter.ofPattern(userInfo.getSdatetimeformat());
		final String fromDateUI = LocalDateTime.parse(fromDate, dbPattern).format(uiPattern);
		final String toDateUI = LocalDateTime.parse(toDate, dbPattern).format(uiPattern);
		outputMap.put("fromDate", fromDateUI);
		outputMap.put("toDate", toDateUI);
		fromDate = dateUtilityFunction
				.instantDateToString(dateUtilityFunction.convertStringDateToUTC(fromDate, userInfo, true));
		toDate = dateUtilityFunction.instantDateToString(dateUtilityFunction.convertStringDateToUTC(toDate, userInfo, true));
	
		LocalDateTime fromDateTime = LocalDateTime.parse(fromDate);
		LocalDateTime toDateTime = LocalDateTime.parse(toDate);
		DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		String formattedFromDate = fromDateTime.format(myFormatObj);
		String formattedToDate = toDateTime.format(myFormatObj);

		final String strQuery = "select ap.nauditplancode,ap.naudittypecode,ap.nauditcategorycode,ap.nauditstandardcatcode,ap.ndeptcode, "
					  		  + "ap.ndeptheadcode,ap.saudittitle,ap.scompauditrep,ap.noffsetdauditdatetime,ap.ntzauditdatetime, "
					  		  + "ap.sauditid,at.saudittypename,ac.sauditcategoryname,asc1.sauditstandardcatname,d.sdeptname,"
					  		  + "u.sfirstname ||' '||u.slastname as sdeptheadname,tz.stimezoneid,aph.ntransactionstatus,cm.scolorhexcode,  "
					  		  + "COALESCE(TO_CHAR(ap.dauditdatetime,'" + userInfo.getSpgsitedatetime() + "'),'-') as sauditdatetime,"
							  + "COALESCE(TO_CHAR(aph.dcreateddate,'" + userInfo.getSpgsitedatetime() + "'),'-') as screateddate,"
					  		  + "COALESCE(ts.jsondata->'stransdisplaystatus'->>'"+userInfo.getSlanguagetypecode()+"',ts.jsondata->'stransdisplaystatus'->>'en-US') as stransdisplaystatus " 
					  		  + "from auditplan ap "
					  		  + "join audittype at on at.naudittypecode = ap.naudittypecode "
					  		  + "join auditcategory ac on ac.nauditcategorycode =ap.nauditcategorycode "
					  		  + "join auditstandardcategory asc1 on asc1.nauditstandardcatcode =ap.nauditstandardcatcode "
					  		  + "join department d on d.ndeptcode = ap.ndeptcode "
					  		  + "join users u on u.nusercode = ap.ndeptheadcode "
					  		  + "join timezone tz on tz.ntimezonecode =ap.ntzauditdatetime "
					  		  + "join auditplanhistory aph on aph.nauditplancode =ap.nauditplancode "
					  		  + "join transactionstatus ts on ts.ntranscode= aph.ntransactionstatus "
					  		  + "join formwisestatuscolor fsc on fsc.ntranscode =aph.ntransactionstatus "
					  		  + "join colormaster cm on cm.ncolorcode =fsc.ncolorcode " 
					  		  + "where aph.nauditplanhistorycode=any(select max(nauditplanhistorycode) from auditplanhistory "
					  		  + "where  nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
					  		  + "and nsitecode="+userInfo.getNtranssitecode()+" group by nauditplancode) and aph.dcreateddate ::Date between '" + formattedFromDate + "' and '"+ formattedToDate + "' "
					  		  + "and ap.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"  "
					  		  + "and at.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and ac.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
					  		  + "and asc1.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and d.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and u.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
					  		  + "and fsc.nformcode ="+userInfo.getNformcode()+" and ap.nsitecode ="+userInfo.getNtranssitecode()+" and ap.nauditplancode >0";
	
		List<AuditPlan> lstAuditPlan = jdbcTemplate.query(strQuery,new AuditPlan());
	
		/*final List<AuditPlan> lstUTCConvertedDate = objMapper.convertValue(
		dateUtilityFunction.getSiteLocalTimeFromUTC(lstAuditPlan,
		Arrays.asList("sauditdatetime"),
		Arrays.asList(userInfo.getStimezoneid()), userInfo, false, null, false),
		new TypeReference<List<AuditPlan>>() {
		});*/
		outputMap.put("auditPlan", lstAuditPlan);
		int nauditPlanCode = (int) inputMap.get("nauditplancode");
		List<AuditPlan> filteredPlans = lstAuditPlan.stream()
		    .filter(plan -> plan.getNauditplancode() == nauditPlanCode)
		    .collect(Collectors.toList());
		
		outputMap.put("selectedAuditPlan", filteredPlans);
		outputMap.putAll(getAuditPlanHistory(nauditPlanCode, userInfo));
		outputMap.putAll((Map<String,Object>)getAuditPlanMember(nauditPlanCode, userInfo).getBody());
		outputMap.putAll((Map<String, Object>)getAuditPlanAuditor(nauditPlanCode, userInfo).getBody());
		outputMap.putAll((Map<String, Object>)getAuditPlanFile(nauditPlanCode, userInfo).getBody());

	
		return new ResponseEntity<Object>(outputMap, HttpStatus.OK);
	}
	
}



