package com.agaramtech.qualis.credential.service.employeeattendence;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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
import com.agaramtech.qualis.credential.model.EmployeeAttendence;
import com.agaramtech.qualis.credential.model.EmployeeType;
import com.agaramtech.qualis.credential.model.ManPower;
import com.agaramtech.qualis.credential.model.Users;
import com.agaramtech.qualis.global.AuditUtilityFunction;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.ProjectDAOSupport;
import com.agaramtech.qualis.global.StringUtilityFunction;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.AllArgsConstructor;

/**
 * This class is used to perform CRUD Operation on "employeeattendence" table by 
 * implementing methods from its interface. 
 */
/**
 * @author sujatha.v AT-E274
 * SWSM-7 14/08/2025  
 */
@AllArgsConstructor
@Repository
public class EmployeeAttendenceDAOImpl implements EmployeeAttendenceDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(EmployeeAttendenceDAOImpl.class);

	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;
	private final ProjectDAOSupport projectDAOSupport;

	/**
	 * This method is used to retrieve list of all available employeeattendence for the specified employeetype.
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetch.
	 * @param currentUIDate [String] holding the current UI date.
	 * @param emptypecode [int] holding the primary key of the employee type of the Employee Attendence.
	 * @param attendenceDate [String] holding attendencedate which list the Employee Attendence from the attendencedate.
	 * @return response entity  object holding response status and list of all active EmployeeAttendence
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> getEmployeeAttendence(final UserInfo userInfo, final int emptypecode,
			String currentUIDate, String attendenceDate) throws Exception {
		LOGGER.info("getEmployeeAttendence");
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		if (currentUIDate != null && currentUIDate.trim().length() != 0) {
			final Map<String, Object> mapObject = projectDAOSupport.getDateFromControlProperties(userInfo,
					currentUIDate, "datetime", "attendenceDate");
			attendenceDate = (String) mapObject.get("ToDate");
			outputMap.put("attendenceDate", mapObject.get("ToDateWOUTC"));
		} else {
			final DateTimeFormatter dbPattern = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
			final DateTimeFormatter uiPattern = DateTimeFormatter.ofPattern(userInfo.getSdatetimeformat());
			final String fromDateUI = LocalDateTime.parse(attendenceDate, dbPattern).format(uiPattern);
			outputMap.put("attendenceDate", fromDateUI);
		}

		final String strQuery = "select e.nemptypecode, COALESCE(e.jsondata->'sdisplayname'->>'"
				+ userInfo.getSlanguagetypecode() + "', " + " e.jsondata->'sdisplayname'->>'en-US') AS semptypename, "
				+ " e.nsitecode, e.nstatus" + " from employeetype e where nemptypecode>0 and e.nsitecode="
				+ userInfo.getNmastersitecode() + " and e.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

		List<EmployeeType> list = jdbcTemplate.query(strQuery, new EmployeeType());
		outputMap.put("EmployeeType", list);
		outputMap.put("selectedEmployeeType",
				(emptypecode == Enumeration.TransactionStatus.NA.gettransactionstatus()
						|| emptypecode == Enumeration.EmployeeType.LIMSEMPLOYEE.getEmployeeType()) ? list.get(0)
								: list.get(1));
		int nemptypecode = 0;
		String stablename;
		String nprimarykey;
		String loginId;
		
		if (emptypecode == -1) {
			nemptypecode = list.get(0).getNemptypecode();
			stablename = "users";
			nprimarykey = "nusercode";
			 loginId="sloginid";
		} else {
			nemptypecode = emptypecode;
			stablename = (nemptypecode == Enumeration.EmployeeType.LIMSEMPLOYEE.getEmployeeType()) ? "users"
					: "outsourceemployee";
			nprimarykey = (nemptypecode == Enumeration.EmployeeType.LIMSEMPLOYEE.getEmployeeType()) ? "nusercode"
					: "noutsourceempcode";
			 loginId=(nemptypecode == Enumeration.EmployeeType.LIMSEMPLOYEE.getEmployeeType()) ? "sloginid"
					: "noutsourceempcode";
		}
		final String strEmployeeAttendence = "select e.nempattendencecode, e.nemptypecode, e.nusercode, e.nispresent, "
				+ " e.noffsetdentrydatetime, e.ntzentrydatetime, e.noffsetdexitdatetime, e.ntzexitdatetime, "
				+ " e.noffsetdattendencedate, e.ntzattendencedate,"
				+ "case when e.sremarks is null then '-' when e.sremarks = '' then '-' else e.sremarks end sremarks,"
				+ " e.nsitecode, e.nstatus, " + " COALESCE(to_char(e.dentrydatetime, '" + userInfo.getSpgsitedatetime()
				+ "'), '-') AS sentrydatetime, " + " COALESCE(to_char(e.dexitdatetime, '"
				+ userInfo.getSpgsitedatetime() + "'), '-') AS sexitdatetime, "
				+ " COALESCE(to_char(e.dattendencedate, '" + userInfo.getSpgsitedatetime()
				+ "'), '-') AS sattendencedate, " + " coalesce(et.jsondata->'sdisplayname'->>'"
				+ userInfo.getSlanguagetypecode() + "', et.jsondata->'sdisplayname'->>'en-US') AS semptypename, "
				+ " coalesce(ts.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode()
				+ "', ts.jsondata->'stransdisplaystatus'->>'en-US') AS sispresent, "
				+ " CONCAT(s.sfirstname,' ',s.slastname)||' '||'(' ||s."+ loginId +" || ')' as susername" + " FROM employeeattendence e," + stablename
				+ " s, employeetype et, transactionstatus ts "
				+ " WHERE et.nemptypecode = e.nemptypecode and e.nemptypecode=" + nemptypecode
				+ " and ts.ntranscode=e.nispresent  and e.nusercode=s." + nprimarykey + " AND e.dattendencedate::date='"
				+ attendenceDate + "' AND et.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " AND e.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " AND ts.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " AND e.nempattendencecode > 0 " + " AND e.nsitecode = " + userInfo.getNtranssitecode() // modified by sujatha on 26-08-2025 to get based on transsitecode
				+ " order by e.nempattendencecode desc";

		List<EmployeeAttendence> employeeAttendenceList = jdbcTemplate.query(strEmployeeAttendence,
				new EmployeeAttendence());
		outputMap.put("EmployeeAttendence", employeeAttendenceList);
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	/**
	 * This method is used to fetch EmployeeType based on the specified nemptypecode
	 * @param nemptypecode [int] primary key of the EmployeeType
	 * @param userInfo [UserInfo] holding logged in user details based on 
	 * 							  which the list is to be fetched
	 * @return employeeTypeList get query [String] based on the specified nemptypecode and site
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public List<?> getEmpByEmployeeType(final int nemptypecode, final UserInfo userInfo) throws Exception {

		String stablename;
		String nprimarykey;
		String loginId;
		String condition = null;
		String squery;
		String sitecode = null;
		if (nemptypecode == -1) {
			stablename = "users";
			nprimarykey = "nusercode";
			loginId="sloginid";
			//added  by sujatha ATE_274 for getting employees based on site on 26-08-2025
			sitecode="nsitecode";
			condition=", userssite us where e.nusercode=us.nusercode and and e.ntransactionstatus=1 and "  // added by sujatha ATE_274 16-09-2025 SWSM-32 added transactionstatus to check users are active to load in drop down
					   + " us.nsitecode="+ userInfo.getNtranssitecode()+" and e.nsitecode="
					   +userInfo.getNmastersitecode()+" and e.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					   + " and us.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					   + " and e."+nprimarykey+">0";
		} else {
			stablename = (nemptypecode == Enumeration.EmployeeType.LIMSEMPLOYEE.getEmployeeType()) ? "users"
					: "outsourceemployee";
			nprimarykey = (nemptypecode == Enumeration.EmployeeType.LIMSEMPLOYEE.getEmployeeType()) ? "nusercode"
					: "noutsourceempcode";
			loginId = (nemptypecode == Enumeration.EmployeeType.LIMSEMPLOYEE.getEmployeeType()) ? "sloginid"
					: "noutsourceempcode";
			//added  by sujatha ATE_274 for site based getting employees on 26-08-2025
			sitecode = (nemptypecode == Enumeration.EmployeeType.LIMSEMPLOYEE.getEmployeeType()) ? "nsitecode" : "nsitemastercode";
			
			condition= (nemptypecode == Enumeration.EmployeeType.LIMSEMPLOYEE.getEmployeeType()) ? 
					" , userssite us where e.nusercode=us.nusercode and e.ntransactionstatus=1 and "  // added by sujatha ATE_274 16-09-2025 SWSM-32 added transactionstatus to check users are active to load in drop down
					+ " us."+sitecode+"= "+ userInfo.getNtranssitecode()+" and e.nsitecode="
					+userInfo.getNmastersitecode()+" and e.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and us.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and e."+nprimarykey+">0"
					: " where e.nsitecode="+userInfo.getNmastersitecode()+" and e."+sitecode+"= "+userInfo.getNtranssitecode()
					+ " and e.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and e."+nprimarykey+">0";
		}

		//modified by sujatha ATE_274 for site based getting employees on 26-08-2025
		 squery = " select e." + nprimarykey
				+ " as nusercode, CONCAT(e.sfirstname,' ',e.slastname)||' '||'(' || e."+loginId +"|| ')' as susername  from " 
				+ stablename+ " e"+condition;

		List<?> employeeList = jdbcTemplate.queryForList(squery);
		return employeeList;
	}

	/**
	 * This method is used to retrieve active employeeAttendence object based on the specified nemployeeattendencecode.
	 * @param inputMap [Map] map object holding primary key of Employee Attendence and the nemptype primary key.
	 * @param userInfo [UserInfo] holding logged in user details based on 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of employeeAttendence object
	 * @throws Exception that are thrown from this DAO layer
	 */	
	@Override
	public ResponseEntity<Object> getActiveEmployeeAttendenceById(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {
		String stablename;
		String nprimarykey;
		final int nemployeeattendencecode = (int) inputMap.get("nemployeeattendencecode");
		final int nemptypecode = (int) inputMap.get("nemptypecode");
		final String activeQuery = getActiveEmployeeAttendence(nemployeeattendencecode, userInfo);
		final EmployeeAttendence activeEmployeeAttendence = (EmployeeAttendence) jdbcUtilityFunction
				.queryForObject(activeQuery, EmployeeAttendence.class, jdbcTemplate);
		if (nemptypecode == -1) {
			stablename = "users";
			nprimarykey = "nusercode";
		} else {
			stablename = (nemptypecode == Enumeration.EmployeeType.LIMSEMPLOYEE.getEmployeeType()) ? "users"
					: "outsourceemployee";
			nprimarykey = (nemptypecode == Enumeration.EmployeeType.LIMSEMPLOYEE.getEmployeeType()) ? "nusercode"
					: "noutsourceempcode";
		}
		if (activeEmployeeAttendence != null) {
			Map<String, Object> outputMap = new HashMap<>();
			String query = "select e.nempattendencecode,e.nusercode,e.nispresent, CONCAT(s.sfirstname,' ',s.slastname) as susername, "
					+ " coalesce(ts.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode()
					+ "', ts.jsondata->'stransdisplaystatus'->>'en-US') AS sispresent, "
					+ "COALESCE(TO_CHAR(e.dentrydatetime,'" + "" + userInfo.getSpgsitedatetime()
					+ "'),'') as sentrydatetime," + " COALESCE(TO_CHAR(e.dexitdatetime,'" + ""
					+ userInfo.getSpgsitedatetime() + "'),'') as sexitdatetime,"
					+ " COALESCE(TO_CHAR(e.dattendencedate,'" + "" + userInfo.getSpgsitedatetime()
					+ "'),'') as sattendencedate,"
					+ " e.sremarks,e.nemptypecode from employeeattendence e, employeetype et, transactionstatus ts, "
					+ stablename
					+ " s where ts.ntranscode=e.nispresent and et.nemptypecode=e.nemptypecode and e.nusercode=s."
					+ nprimarykey + " and et.nstatus= " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()  
					+ " and " + "  e.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""      
					+ " and e.nsitecode=" + userInfo.getNtranssitecode()    //modified by sujatha ATE_274 from mastersitecode to transsitecode on 26-08-2025
					+ " and et.nsitecode=" + userInfo.getNmastersitecode() + " and " + " nempattendencecode="
					+ nemployeeattendencecode + " and s.nstatus= "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " AND ts.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

			final EmployeeAttendence employeeAttendence = jdbcTemplate.queryForObject(query, new EmployeeAttendence());
			outputMap.put("activeEmployeeAttendenceById", employeeAttendence);
			return new ResponseEntity<>(outputMap, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	/**
	 * This method is used to add a new entry to employeeattendence table.
	 * @param inputMap [Map] map object holding details to be added in employeeattendence table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return saved employeeAttendenceObj object with status code 200 if saved successfully. 
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> createEmployeeAttendence(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {

		final String sQuery = " lock  table employeeattendence "
				+ Enumeration.ReturnStatus.EXCLUSIVEMODE.getreturnstatus();
		jdbcTemplate.execute(sQuery);
		final ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new JavaTimeModule());
		final List<Object> savedEmployeeAttendence = new ArrayList<>();
		final List<String> multilingualIDList = new ArrayList<>();
		String attendenceDate = "";

		EmployeeAttendence employeeAttendenceObj = mapper.convertValue(inputMap.get("employeeattendence"),
				EmployeeAttendence.class);

		List<EmployeeAttendence> empAttendence=getEmployeeAttendenceByName(employeeAttendenceObj, userInfo);
		if(empAttendence.size()==0) {
			
			//added by sujatha ATE_274 for entry and exit date only validation
			if (employeeAttendenceObj.getDattendencedate() != null) {
			    Instant attendenceDateTime = employeeAttendenceObj.getDattendencedate();
			    Instant entryDateTime = employeeAttendenceObj.getDentrydatetime();
			    Instant exitDateTime = employeeAttendenceObj.getDexitdatetime();
			    String attendenceDateStr = dateUtilityFunction.instantDateToStringWithFormat(attendenceDateTime, userInfo.getSsitedate());

			    if (entryDateTime != null) {
			        String entryDateStr = dateUtilityFunction.instantDateToStringWithFormat(entryDateTime, userInfo.getSsitedate());
			        if (!entryDateStr.equals(attendenceDateStr)) {
			            return new ResponseEntity<>(
			                commonFunction.getMultilingualMessage("IDS_ENTRYDATEMUSTBESAMEASATTENDENCEDATE", userInfo.getSlanguagefilename()),
			                HttpStatus.EXPECTATION_FAILED
			            );
			        }
			    }
			    if (exitDateTime != null) {
			        String exitDateStr = dateUtilityFunction.instantDateToStringWithFormat(exitDateTime, userInfo.getSsitedate());
			        if (!exitDateStr.equals(attendenceDateStr)) {
			            return new ResponseEntity<>(
			                commonFunction.getMultilingualMessage("IDS_EXITDATEMUSTBESAMEASATTENDENCEDATE", userInfo.getSlanguagefilename()),
			                HttpStatus.EXPECTATION_FAILED
			            );
			        }
			    }
			}

		final String seqNo = "select nsequenceno from SeqNoCredentialManagement where stablename='employeeattendence' "
				+ "and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
		int nsequenceno = jdbcTemplate.queryForObject(seqNo, Integer.class);
		nsequenceno++;

		final String insertQuery = "insert into employeeattendence(nempattendencecode,nemptypecode,nusercode,nispresent,"
				+ "dentrydatetime,noffsetdentrydatetime,ntzentrydatetime,dexitdatetime,noffsetdexitdatetime,"
				+ "ntzexitdatetime, dattendencedate, noffsetdattendencedate, ntzattendencedate, "
				+ "sremarks,dmodifieddate,nsitecode,nstatus) values(" + nsequenceno + ","
				+ employeeAttendenceObj.getNemptypecode() + "," + employeeAttendenceObj.getNusercode() + ","
				+ employeeAttendenceObj.getNispresent() + ","
				+ (employeeAttendenceObj.getDentrydatetime() != null? "'" + employeeAttendenceObj.getDentrydatetime() + "'"	: "null")
				+ "," + employeeAttendenceObj.getNoffsetdentrydatetime() + ","
				+ employeeAttendenceObj.getNtzentrydatetime() + ","
				+ (employeeAttendenceObj.getDexitdatetime() != null? "'" + employeeAttendenceObj.getDexitdatetime() + "'": "null")
				+ "," + employeeAttendenceObj.getNoffsetdexitdatetime() + ","
				+ employeeAttendenceObj.getNtzexitdatetime() + ","
				+ (employeeAttendenceObj.getDattendencedate() != null? "'" + employeeAttendenceObj.getDattendencedate() + "'": "null")
				+ "," + employeeAttendenceObj.getNoffsetdattendencedate() + ","
				+ employeeAttendenceObj.getNtzattendencedate() + ",N'"
				+ stringUtilityFunction.replaceQuote(employeeAttendenceObj.getSremarks()) + "','"
				+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + userInfo.getNtranssitecode() + ","    //modified by sujatha ATE_274 from mastersitecode to transsitecode on 26-08-2025
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ");";
		jdbcTemplate.execute(insertQuery);
		final String updatequery = "update SeqNoCredentialManagement set nsequenceno =" + nsequenceno
				+ " where stablename='employeeattendence'";
		jdbcTemplate.execute(updatequery);
		
		employeeAttendenceObj.setNempattendencecode(nsequenceno);
		String userName = getEmployeeNameForAudit(employeeAttendenceObj,userInfo);
		employeeAttendenceObj.setSusername(userName);
		if (employeeAttendenceObj.getDattendencedate() != null) {
			attendenceDate = (employeeAttendenceObj.getDattendencedate().toString()).replace("Z", "");
		}
		employeeAttendenceObj.setSispresent(String.valueOf(employeeAttendenceObj.getNispresent()));
		employeeAttendenceObj.setSattendencedate(employeeAttendenceObj.getDattendencedate() == null ? null
				: dateUtilityFunction.instantDateToStringWithFormat(employeeAttendenceObj.getDattendencedate(),
						userInfo.getSsitedate()));
		employeeAttendenceObj.setSentrydatetime(employeeAttendenceObj.getDentrydatetime() == null ? null
				: dateUtilityFunction.instantDateToStringWithFormat(employeeAttendenceObj.getDentrydatetime(),
						userInfo.getSsitedatetime())); // modified by sujatha ATE_274 on 26-08-2025 for getting date along with time in audit trial
		employeeAttendenceObj.setSexitdatetime(employeeAttendenceObj.getDexitdatetime() == null ? null
				: dateUtilityFunction.instantDateToStringWithFormat(employeeAttendenceObj.getDexitdatetime(),
						userInfo.getSsitedatetime())); // modified by sujatha ATE_274 on 26-08-2025 for getting date along with time in audit trial
		multilingualIDList.add("IDS_ADDEMPLOYEEATTENDENCE");
		savedEmployeeAttendence.add(employeeAttendenceObj);
		auditUtilityFunction.fnInsertAuditAction(savedEmployeeAttendence, 1, null, multilingualIDList, userInfo);
		return getEmployeeAttendence(userInfo, (int) employeeAttendenceObj.getNemptypecode(), null, attendenceDate);
		}
		else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}
	}
	
	private List<EmployeeAttendence> getEmployeeAttendenceByName(final EmployeeAttendence employeeAttendenceObj, UserInfo userInfo)
			throws Exception {
		String stablename;
		String nprimarykey;
		int nemptypecode=employeeAttendenceObj.getNemptypecode();
		if (nemptypecode == -1) {
			stablename = "users";
			nprimarykey = "nusercode";
		} else {
			stablename = (nemptypecode == Enumeration.EmployeeType.LIMSEMPLOYEE.getEmployeeType()) ? "users"
					: "outsourceemployee";
			
			nprimarykey = (nemptypecode == Enumeration.EmployeeType.LIMSEMPLOYEE.getEmployeeType()) ? "nusercode"
					: "noutsourceempcode";
		}
		
		final String strQuery = "select nempattendencecode from employeeattendence e, "+stablename+" s where e.nusercode=s."
								+ nprimarykey + " and e.nusercode="+employeeAttendenceObj.getNusercode()
								+ " and e.dattendencedate::date='"
								+ employeeAttendenceObj.getDattendencedate()
								+ "' and nemptypecode="+employeeAttendenceObj.getNemptypecode()
								+ " and e.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " and e.nsitecode= " + userInfo.getNtranssitecode()   //modified by sujatha ATE_274 from mastersitecode to transsitecode on 26-08-2025
								+ " and s.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " and s.nsitecode= " + userInfo.getNmastersitecode();
		 List<EmployeeAttendence> lstEmplnoyee = jdbcTemplate.query(strQuery,new EmployeeAttendence());
		 return lstEmplnoyee;
		
	}

	/**
	 * This method is used to update entry in employeeattendence table.Need to
	 * validate that the employeeattendence object to be updated is active
	 * before updating details in database.
	 * @param inputMap [Map] map object holding details to be updated in employeeattendence table.
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return saved employeeAttendence object with status code 200 if saved successfully 
	 *          else if the employeeAttendence to be updated is not available, response will be returned as 'Already Deleted' with status code 417
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> updateEmployeeAttendence(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		final ObjectMapper mapper = new ObjectMapper();
		String attendenceDate = "";
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> beforeEmployeeAttendence = new ArrayList<>();
		final List<Object> afterEmployeeAttendence = new ArrayList<>();
		mapper.registerModule(new JavaTimeModule());
		final EmployeeAttendence objEmployeeAttendence = mapper.convertValue(inputMap.get("employeeattendence"),
				new TypeReference<EmployeeAttendence>() {
				});
		final String activeQuery = getActiveEmployeeAttendence(objEmployeeAttendence.getNempattendencecode(), userInfo);
		final EmployeeAttendence activeEmployeeAttendence = (EmployeeAttendence) jdbcUtilityFunction
				.queryForObject(activeQuery, EmployeeAttendence.class, jdbcTemplate);
		if (activeEmployeeAttendence == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);	
		} else {

			//added by sujatha ATE_274 for entry and exit date only validation			
			if (objEmployeeAttendence.getDattendencedate() != null) {
			    Instant attendenceDateTime = objEmployeeAttendence.getDattendencedate();
			    Instant entryDateTime = objEmployeeAttendence.getDentrydatetime();
			    Instant exitDateTime = objEmployeeAttendence.getDexitdatetime();
			    String attendenceDateStr = dateUtilityFunction.instantDateToStringWithFormat(attendenceDateTime, userInfo.getSsitedate());

			    if (entryDateTime != null) {
			        String entryDateStr = dateUtilityFunction.instantDateToStringWithFormat(entryDateTime, userInfo.getSsitedate());
			        if (!entryDateStr.equals(attendenceDateStr)) {
			            return new ResponseEntity<>(
			                commonFunction.getMultilingualMessage("IDS_ENTRYDATEMUSTBESAMEASATTENDENCEDATE", userInfo.getSlanguagefilename()),
			                HttpStatus.EXPECTATION_FAILED
			            );
			        }
			    }
			    if (exitDateTime != null) {
			        String exitDateStr = dateUtilityFunction.instantDateToStringWithFormat(exitDateTime, userInfo.getSsitedate());
			        if (!exitDateStr.equals(attendenceDateStr)) {
			            return new ResponseEntity<>(
			                commonFunction.getMultilingualMessage("IDS_EXITDATEMUSTBESAMEASATTENDENCEDATE", userInfo.getSlanguagefilename()),
			                HttpStatus.EXPECTATION_FAILED
			            );
			        }
			    }
			}

		
			if (objEmployeeAttendence.getDattendencedate() != null) {
				attendenceDate = (objEmployeeAttendence.getDattendencedate().toString()).replace("Z", "");
			}

			final String updateQuery = "update employeeattendence set nispresent="+ objEmployeeAttendence.getNispresent()
					+" ,dentrydatetime= "+ (objEmployeeAttendence.getDentrydatetime() != null ? "'" + objEmployeeAttendence.getDentrydatetime() + "'" : "null")
					+ ", ntzentrydatetime="+ objEmployeeAttendence.getNtzentrydatetime() + " , noffsetdentrydatetime="
					+ objEmployeeAttendence.getNoffsetdentrydatetime() + " , dexitdatetime="
					+ (objEmployeeAttendence.getDexitdatetime() != null ? "'" + objEmployeeAttendence.getDexitdatetime() + "'" : "null")
					+ " , ntzexitdatetime="+ objEmployeeAttendence.getNtzexitdatetime() + " , noffsetdexitdatetime="
					+ objEmployeeAttendence.getNoffsetdexitdatetime() + " , dattendencedate="
					+ (objEmployeeAttendence.getDattendencedate() != null ? "'" + objEmployeeAttendence.getDattendencedate() + "'" : "null")
					+ ",ntzattendencedate="+ objEmployeeAttendence.getNtzattendencedate() + "," + " noffsetdattendencedate="
					+ objEmployeeAttendence.getNoffsetdattendencedate() + ", sremarks=N'"
					+ stringUtilityFunction.replaceQuote(objEmployeeAttendence.getSremarks()) + "',dmodifieddate='"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "',nsitecode=" + userInfo.getNtranssitecode() //modified by sujatha ATE_274 from mastersitecode to transsitecode on 26-08-2025
					+ " where nempattendencecode=" + objEmployeeAttendence.getNempattendencecode() + "";
			jdbcTemplate.execute(updateQuery);

			String userName = getEmployeeNameForAudit(activeEmployeeAttendence,userInfo);
			activeEmployeeAttendence.setSusername(userName);
			activeEmployeeAttendence.setSispresent(String.valueOf(activeEmployeeAttendence.getNispresent()));
			activeEmployeeAttendence.setSattendencedate(activeEmployeeAttendence.getDattendencedate() == null ? null
					: dateUtilityFunction.instantDateToStringWithFormat(activeEmployeeAttendence.getDattendencedate(),
							userInfo.getSsitedate()));
			
			beforeEmployeeAttendence.add(activeEmployeeAttendence);
			
			String userNameUpdated = getEmployeeNameForAudit(objEmployeeAttendence,userInfo);
			objEmployeeAttendence.setSusername(userNameUpdated);
			if (objEmployeeAttendence.getDattendencedate() != null) {
				attendenceDate = (objEmployeeAttendence.getDattendencedate().toString()).replace("Z", "");
			}
			objEmployeeAttendence.setSispresent(String.valueOf(objEmployeeAttendence.getNispresent()));
			objEmployeeAttendence.setSattendencedate(objEmployeeAttendence.getDattendencedate() == null ? null
					: dateUtilityFunction.instantDateToStringWithFormat(objEmployeeAttendence.getDattendencedate(),
							userInfo.getSsitedate()));
			objEmployeeAttendence.setSentrydatetime(objEmployeeAttendence.getDentrydatetime() == null ? null
					: dateUtilityFunction.instantDateToStringWithFormat(objEmployeeAttendence.getDentrydatetime(),
							userInfo.getSsitedatetime()));  // modified by sujatha ATE_274 on 26-08-2025 for getting date along with time in audit trial
			objEmployeeAttendence.setSexitdatetime(objEmployeeAttendence.getDexitdatetime() == null ? null
					: dateUtilityFunction.instantDateToStringWithFormat(objEmployeeAttendence.getDexitdatetime(),
							userInfo.getSsitedatetime()));  // modified by sujatha ATE_274 on 26-08-2025 for getting date along with time in audit trial
			afterEmployeeAttendence.add(objEmployeeAttendence);
			multilingualIDList.add("IDS_EDITEMPLOYEEATTENDENCE");
			auditUtilityFunction.fnInsertAuditAction(afterEmployeeAttendence, 2, beforeEmployeeAttendence,
					multilingualIDList, userInfo);
			return getEmployeeAttendence(userInfo, (int) objEmployeeAttendence.getNemptypecode(), null, attendenceDate);
		}
	}

	/**
	 * This method is used to delete an entry in employeeattendence table
	 * Need to check the record is already deleted or not
	 * @param inputMap [Map] map object holding detail to be deleted from employeeattendence table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return a response entity with list of available employeeAttendence object
	 * @exception Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> deleteEmployeeAttendence(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		final List<Object> deleteEmployeeAttendence = new ArrayList<>();
		final List<String> multilingualIDList = new ArrayList<>();
		final ObjectMapper mapper = new ObjectMapper();
		final EmployeeAttendence employeeAttendenceObj = mapper.convertValue(inputMap.get("employeeattendence"),
				EmployeeAttendence.class);

		String attendenceDate = "";
		DateTimeFormatter inputPattern = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
		DateTimeFormatter dbPattern = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

		if (employeeAttendenceObj.getSattendencedate() != null) {
			String inputDateStr = employeeAttendenceObj.getSattendencedate().toString().replace("Z", "");
			LocalDateTime parsedDate = LocalDateTime.parse(inputDateStr, inputPattern);
			attendenceDate = parsedDate.format(dbPattern);
		}

		final String activeQuery = getActiveEmployeeAttendence(employeeAttendenceObj.getNempattendencecode(), userInfo);
		final EmployeeAttendence activeEmployeeAttendence = (EmployeeAttendence) jdbcUtilityFunction
				.queryForObject(activeQuery, EmployeeAttendence.class, jdbcTemplate);
		if (activeEmployeeAttendence == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);

		} else {

			String deleteQuery = "update employeeattendence set nstatus="
					+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ", dmodifieddate ='"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where nempattendencecode="
					+ employeeAttendenceObj.getNempattendencecode() + "";
			jdbcTemplate.execute(deleteQuery);

			String userName = getEmployeeNameForAudit(employeeAttendenceObj,userInfo);
			employeeAttendenceObj.setSusername(userName);
			employeeAttendenceObj.setSispresent(String.valueOf(employeeAttendenceObj.getNispresent()));
			deleteEmployeeAttendence.add(employeeAttendenceObj);
			multilingualIDList.add("IDS_DELETEEMPLOYEEATTENDENCE");
			auditUtilityFunction.fnInsertAuditAction(deleteEmployeeAttendence, 1, null, multilingualIDList, userInfo);
			return getEmployeeAttendence(userInfo, (int) employeeAttendenceObj.getNemptypecode(), null, attendenceDate);
		}
	}

	/**
	 * This method is used to fetch the employeeAttendence for the specified nemplattendencecode and site.
	 * @param nempattendencecode [int] primary key of the employeeAttendence
	 * @param userInfo [UserInfo] holding logged in user details based on 
	 * 							  which the list is to be fetched
	 * @return employeeAttendence get query [String] created by specified nempattendencecode and site
	 * @throws Exception that are thrown from this DAO layer
	 */
	private String getActiveEmployeeAttendence(final int nempattendencecode, final UserInfo userInfo) {
		//modified query by sujatha ATE_274 for audit trial issues on 26-08-2025
		return "select nemptypecode, nusercode, nispresent, dentrydatetime, dexitdatetime, dattendencedate,"
				+ " to_char( dentrydatetime, '"+ userInfo.getSpgsitedatetime()
				+  "') as sentrydatetime," + " to_char ( dexitdatetime, '"+ userInfo.getSpgsitedatetime()
				+ "') as sexitdatetime," + " to_char ( dattendencedate, '"+ userInfo.getSpgsitedatetime()
				+ "') as sattendencedate, sremarks "
				+ " from employeeattendence where nempattendencecode=" + nempattendencecode + " and  nsitecode="
				+ userInfo.getNtranssitecode() + " and " + "nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	}

	/**
	 * This method is used to fetch the employeeName/userName for the specified nemptypecode and site.
	 * @param employeeAttendenceObj [EmployeeAttendence] object of employeeattenance table
	 * @param userInfo [UserInfo] holding logged in user details based on 
	 * 							  which the list is to be fetched
	 * @return employeeAttendence get query [String] created by specified nemptypecode and site
	 * @throws Exception that are thrown from this DAO layer
	 */
	private String getEmployeeNameForAudit(final EmployeeAttendence employeeAttendenceObj, final UserInfo userInfo) throws Exception {
		String userName = "";
		String str = "";
		if (employeeAttendenceObj.getNemptypecode() == Enumeration.EmployeeType.OUTSOURCEEMPLOYEE.getEmployeeType()) {
			str = "select sfirstname ||' '|| slastname as susername from outsourceemployee where noutsourceempcode="
					+ employeeAttendenceObj.getNusercode() + "" + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode ="
					+ userInfo.getNmastersitecode() + "";
			final ManPower outSource = jdbcTemplate.queryForObject(str, new ManPower());
			userName = outSource.getSusername();

		} else {
			str = "select sfirstname ||' '|| slastname as susername from users where nusercode="
					+ employeeAttendenceObj.getNusercode() + "" + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode ="
					+ userInfo.getNmastersitecode() + "";
			final Users users = jdbcTemplate.queryForObject(str, new Users());
			userName = users.getSusername();
		}
		return userName;
	}
}
