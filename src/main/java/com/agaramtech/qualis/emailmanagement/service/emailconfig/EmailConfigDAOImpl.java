package com.agaramtech.qualis.emailmanagement.service.emailconfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import com.agaramtech.qualis.credential.model.ControlMaster;
import com.agaramtech.qualis.credential.model.UserRole;
import com.agaramtech.qualis.credential.model.Users;
import com.agaramtech.qualis.emailmanagement.model.EmailConfig;
import com.agaramtech.qualis.emailmanagement.model.EmailHost;
import com.agaramtech.qualis.emailmanagement.model.EmailScreen;
import com.agaramtech.qualis.emailmanagement.model.EmailScreenScheduler;
import com.agaramtech.qualis.emailmanagement.model.EmailTemplate;
import com.agaramtech.qualis.emailmanagement.model.EmailType;
import com.agaramtech.qualis.emailmanagement.model.EmailUserConfig;
import com.agaramtech.qualis.emailmanagement.model.EmailUserQuery;
import com.agaramtech.qualis.emailmanagement.model.EmailUserRoleConfig;
import com.agaramtech.qualis.global.AuditUtilityFunction;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.UserInfo;
import lombok.RequiredArgsConstructor;

/**
 * This class is used to perform CRUD Operation on "emailconfig" table by
 * implementing methods from its interface.
 */
@RequiredArgsConstructor
@Repository
public class EmailConfigDAOImpl implements EmailConfigDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(EmailConfigDAOImpl.class);
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;

	
	@SuppressWarnings("unchecked")
	public ResponseEntity<Object> getEmailConfig(final Map<String, Object> inputMap, final UserInfo userInfo) throws Exception {
		final Map<String, Object> outputMap = new HashMap<String, Object>();		
		outputMap.putAll(getEmailType(inputMap,userInfo));
		final int nemailConfigCode = (int) inputMap.get("nemailconfigcode");
		final short nemailTypeCode = (short) inputMap.get("nemailtypecode");
		outputMap.putAll((Map<String,Object>)getEmailConfigData(nemailConfigCode,nemailTypeCode,userInfo).getBody());
		return new ResponseEntity<Object>(outputMap, HttpStatus.OK);		
	}
	
	private Map<String, Object> getEmailType(final Map<String, Object> inputMap,final UserInfo userInfo) throws Exception{
		final Map<String, Object> map = new HashMap<String, Object>();
		final String strQuery="select nemailtypecode,COALESCE(jsondata->'semailtypename'->>'" + userInfo.getSlanguagetypecode()
						+ "',jsondata->'semailtypename'->>'en-US') as semailtypename from emailtype where nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " order by nemailtypecode";
		final List<EmailType> lstEmailType = jdbcTemplate.query(strQuery, new EmailType());
		if (lstEmailType.size() > 0) {
			map.put("emailType", lstEmailType);
			map.put("emailTypeValue", lstEmailType.get(0));
			inputMap.put("nemailtypecode", lstEmailType.get(0).getNemailtypecode());
			inputMap.put("nemailconfigcode",-1);
		} else {
			map.put("emailType", null);
			map.put("emailTypeValue",null);

		}
		return map;		
	}
	
	@Override
	// Modified by sonia on 3rd sept 2025 for jira id:SWSM-12
	public ResponseEntity<Object> getEmailConfigData(int nemailconfigcode,final short nemailTypeCode, final UserInfo userinfo) throws Exception {
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		EmailConfig selectedEmailConfig = null;

		if (nemailconfigcode == -1) {
			// Added nenablesms,senablesmsstatus by Gowtham on 27th Sept 2025 for jira-id:SWSM-64
			 //Modified the select query by sonia on 18th Nov 2025 for jira id:BGSI-234
			final String strQuery = " select etc.nemailconfigcode , etc.nemailhostcode ,etc.nsitecode,etc.nenableemail,"
					+ " etc.nenablesms,eh.shostname,et.stemplatename, coalesce (ts.jsondata ->'stransdisplaystatus'->>'"
					+ userinfo.getSlanguagetypecode() + "',ts.jsondata ->'stransdisplaystatus'->>'en-US') as senablemailstatus,"
					+ " coalesce (ts1.jsondata ->'stransdisplaystatus'->>'" + userinfo.getSlanguagetypecode()
					+ "',ts1.jsondata ->'stransdisplaystatus'->>'en-US') as senablesmsstatus,"
					+ " etc.nneedattachment,euq.scolumnname,etc.nemailtemplatecode,etc.ncontrolcode,etc.nemailscreencode,etc.nstatus,"
					+ " coalesce (ts.jsondata ->'stransdisplaystatus'->>'" + userinfo.getSlanguagetypecode()
					+ "',ts.jsondata ->'stransdisplaystatus'->>'en-US') as senablestatus,"
					+ " coalesce (cm.jsondata ->'scontrolids'->>'" + userinfo.getSlanguagetypecode()
					+ "',cm.jsondata ->'scontrolids'->>'en-US') as  scontrolids,"
					+ " qf.nformcode, coalesce (qf.jsondata->'sdisplayname'->>'" + userinfo.getSlanguagetypecode()
					+ "',qf.jsondata->'sdisplayname'->>'en-US')as sscreenname,"
					+ " coalesce (qf.jsondata->'sdisplayname'->>'" + userinfo.getSlanguagetypecode()
					+ "',qf.jsondata->'sdisplayname'->>'en-US')as sformname, euq.sdisplayname,etc.nemailtypecode, "
					+ " coalesce (etp.jsondata->'semailtypename'->>'" + userinfo.getSlanguagetypecode()
					+ "',etp.jsondata->'semailtypename'->>'en-US')as semailtypename,etc.nemailscreenschedulercode,ess.sscheduletypename"
					+ " from emailconfig etc join emailhost eh on eh.nemailhostcode=etc.nemailhostcode"
					+ " join emailtemplate et on et.nemailtemplatecode = etc.nemailtemplatecode"
					+ " join emailtype etp on etp.nemailtypecode = etc.nemailtypecode"
					+ " join emailscreen es on es.nemailscreencode  = etc.nemailscreencode"
					+ " join emailuserquery euq on euq.nemailuserquerycode = etc.nemailuserquerycode"
					+ " join transactionstatus ts on ts.ntranscode=etc.nenableemail"
					+ " join transactionstatus ts1 on ts1.ntranscode=etc.nenablesms"
					+ " join controlmaster cm on cm.ncontrolcode = etc.ncontrolcode"
					+ " join emailscreenscheduler ess on ess.nemailscreenschedulercode=etc.nemailscreenschedulercode"
					+ " join qualisforms qf on qf.nformcode=etc.nformcode where etc.nemailtypecode=" + nemailTypeCode + " and etc.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and eh.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and et.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and es.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and euq.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ts.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ts1.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and cm.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ess.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and etp.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and qf.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and etc.nsitecode="
					+ userinfo.getNmastersitecode() + " and eh.nsitecode=" + userinfo.getNmastersitecode()
					+ " and et.nsitecode=" + userinfo.getNmastersitecode() + " and etp.nsitecode="
					+ userinfo.getNmastersitecode() + " and es.nsitecode=" + userinfo.getNmastersitecode()
					+ " order by nemailconfigcode desc";
			final List<EmailConfig> usersEmailConfig = jdbcTemplate.query(strQuery, new EmailConfig());
			LOGGER.info("getEmailConfig() called");
			if (usersEmailConfig.isEmpty()) {
				outputMap.put("EmailConfig", usersEmailConfig);
				outputMap.put("SelectedEmailConfig", null);
				return new ResponseEntity<>(outputMap, HttpStatus.OK);
			} else {
				outputMap.put("EmailConfig", usersEmailConfig);
				selectedEmailConfig = usersEmailConfig.get(0);
				nemailconfigcode = selectedEmailConfig.getNemailconfigcode();

			}
		} else {
			// Added nenablesms,senablesmsstatus by Gowtham on 27th Sept 2025 for jira-id:SWSM-64
			final String strQuery1 = " select eh.sprofilename,etc.nemailconfigcode,etc.nemailhostcode,etc.nsitecode,etc.nenableemail,"
					+ " etc.nemailtemplatecode,etc.nemailscreencode,etc.nenablesms,etc.nstatus,eh.shostname,et.stemplatename, "
					+ " coalesce (t.jsondata->'stransdisplaystatus'->>'" + userinfo.getSlanguagetypecode()
					+ "',t.jsondata->'stransdisplaystatus'->>'en-US') as senablestatus,qf.nformcode,"
					+ " coalesce (qf.jsondata->'sdisplayname'->>'" + userinfo.getSlanguagetypecode()
					+ "',qf.jsondata->'sdisplayname'->>'en-US')as sscreenname,cm.ncontrolcode,"
					+ " coalesce (qf.jsondata->'sdisplayname'->>'" + userinfo.getSlanguagetypecode()
					+ "',qf.jsondata->'sdisplayname'->>'en-US')as sformname, "
					+ " coalesce (t1.jsondata ->'stransdisplaystatus'->>'" + userinfo.getSlanguagetypecode()
					+ "',t1.jsondata ->'stransdisplaystatus'->>'en-US') as senablesmsstatus,"
					+ " coalesce (cm.jsondata->'scontrolids'->>'" + userinfo.getSlanguagetypecode()
					+ "',cm.jsondata->'scontrolids'->>'en-US') as scontrolids,euq.sdisplayname,etc.nemailtypecode,  "
					+ " coalesce (etp.jsondata->'semailtypename'->>'" + userinfo.getSlanguagetypecode()
					+ "',etp.jsondata->'semailtypename'->>'en-US')as semailtypename ,etc.nemailscreenschedulercode,ess.sscheduletypename from emailconfig etc "
					+ " join emailhost eh on eh.nemailhostcode = etc.nemailhostcode "
					+ " join emailtemplate et on et.nemailtemplatecode = etc.nemailtemplatecode "
					+ " join emailtype etp on etp.nemailtypecode = etc.nemailtypecode "
					+ " join emailscreen es on  es.nemailscreencode = etc.nemailscreencode "
					+ " join transactionstatus t on  t.ntranscode=etc.nenableemail "
					+ " join transactionstatus t1 on t1.ntranscode=etc.nenablesms "
					+ " join controlmaster cm on cm.ncontrolcode = etc.ncontrolcode  "
					+ " join qualisforms qf on qf.nformcode = etc.nformcode "
					+ " join emailscreenscheduler ess  on ess.nemailscreenschedulercode = etc.nemailscreenschedulercode "
					+ " join emailuserquery euq on euq.nemailuserquerycode = etc.nemailuserquerycode"
					+ " where etc.nemailtypecode="+nemailTypeCode+" and etc.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
					+ " and eh.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
					+ " and et.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
					+ " and es.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
					+ " and t.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
					+ " and t1.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
					+ " and cm.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
					+ " and ess.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
					+ " and etp.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
					+ " and qf.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
					+ " and euq.nstatus= " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
					+ " and etc.nsitecode=" + userinfo.getNmastersitecode() + " and etp.nsitecode="
					+ userinfo.getNmastersitecode() + " and eh.nsitecode=" + userinfo.getNmastersitecode()
					+ " and et.nsitecode=" + userinfo.getNmastersitecode() + " and es.nsitecode="
					+ userinfo.getNmastersitecode() + " and nemailconfigcode =" + nemailconfigcode
					+ " order by nemailconfigcode asc";
			final List<EmailConfig> lst1 = jdbcTemplate.query(strQuery1, new EmailConfig());
			if (!lst1.isEmpty())
				selectedEmailConfig = lst1.get(0);
		}
		if (selectedEmailConfig == null) {
			final String returnString = commonFunction.getMultilingualMessage("IDS_MAILCONFIG",
					userinfo.getSlanguagefilename()) + " " + commonFunction.getMultilingualMessage(
					Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(), userinfo.getSlanguagefilename());
			return new ResponseEntity<>(returnString, HttpStatus.EXPECTATION_FAILED);
		} else {
			outputMap.put("SelectedEmailConfig", selectedEmailConfig);
			outputMap.putAll(getEmailUserRoleAndUsers(nemailconfigcode, userinfo));
			return new ResponseEntity<>(outputMap, HttpStatus.OK);
		}
	}

	// Added by Gowtham on Oct 29 2025 for jira-id:BGSI-147
	private Map<String, Object> getEmailUserRoleAndUsers(final int nemailconfigcode, final UserInfo userInfo)
			throws Exception {
		final Map<String, Object> returnMap = new HashMap<String, Object>();

		final String query = "select u.nusercode,umr.nuserrolecode,u.semail,uc.nemailconfigcode,uc.nemailuserconfigcode,"
				+ " concat(u.sfirstname,' ',u.slastname,'(',u.sloginid,')') as susername"
				+ " from users u join userssite us on us.nusercode=u.nusercode"
				+ " join usermultirole umr on us.nusersitecode = umr.nusersitecode"
				+ " join userrole ur on umr.nuserrolecode = ur.nuserrolecode"
				+ " join emailuserconfig uc on u.nusercode = uc.nusercode"
				+ " join emailuserroleconfig urc on urc.nemailuserroleconfigcode = uc.nemailuserroleconfigcode"
				+ " and umr.nuserrolecode = urc.nuserrolecode and urc.nemailconfigcode=uc.nemailconfigcode where u.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and u.nsitecode = "
				+ userInfo.getNmastersitecode() + " and umr.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and umr.nsitecode = "
				+ userInfo.getNmastersitecode() + " and ur.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ur.nsitecode = "
				+ userInfo.getNmastersitecode() + " and uc.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and us.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and uc.nemailconfigcode=" + nemailconfigcode;
		final List<EmailUserConfig> lstEmailUserConfig = jdbcTemplate.query(query, new EmailUserConfig());

		final String userRoleQuery = "select ur.nuserrolecode, ur.suserrolename, eurc.nemailconfigcode"
				+ " from userrole ur join emailuserroleconfig eurc on eurc.nuserrolecode=ur.nuserrolecode"
				+ " and eurc.nemailconfigcode=" + nemailconfigcode + " where ur.nuserrolecode > 0 and ur.nstatus=" 
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and eurc.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ur.nsitecode="
				+ userInfo.getNmastersitecode();
		final List<EmailUserRoleConfig> lstUserRole = jdbcTemplate.query(userRoleQuery, new EmailUserRoleConfig());

		returnMap.put("users", lstEmailUserConfig);
		returnMap.put("emailUserRoles", lstUserRole);

		return returnMap;
	}

	// Modified by sonia on 3rd sept 2025 for jira id:SWSM-12
	@Override
	public Map<String,Object> getEmailConfigDetails(final int nformCode, final UserInfo userInfo) throws Exception {
		final Map<String, Object> returnMap = new HashMap<>();
		//#SECURITY-VULNERABILITY-MERGING
		//final String hostquery = "select nemailhostcode,shostname from emailhost where nstatus ="
		String hostquery = "select nemailhostcode,shostname,ndefaultstatus from emailhost where nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode = "
				+ userInfo.getNmastersitecode() + ";";
		final List<EmailHost> lstemailhost = jdbcTemplate.query(hostquery, new EmailHost());

		final String templatequery = "select nemailtemplatecode,stemplatename from emailtemplate where nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode = "
				+ userInfo.getNmastersitecode() + " ";
		final List<EmailTemplate> lstemailtemplate = jdbcTemplate.query(templatequery, new EmailTemplate());

		//Modified the select query by sonia on 18th Nov 2025 for jira id:BGSI-234
		final String screenquery = "select es.nemailscreencode ," + "coalesce (qf.jsondata->'sdisplayname'->>'"
				+ userInfo.getSlanguagetypecode() + "',qf.jsondata->'sdisplayname'->>'en-US')as sscreenname ,"
				+ "es.nformcode from emailscreen es"
				+ " join qualisforms qf on qf.nformcode = es.nformcode " + " where es.nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and es.nsitecode = "
				+ userInfo.getNmastersitecode() + " ";
		final List<EmailScreen> lstemailscreen = jdbcTemplate.query(screenquery, new EmailScreen());

		returnMap.putAll((Map<String, Object>) getEmailConfigControl(nformCode, userInfo));
		returnMap.putAll((Map<String, Object>) getEmailConfigScheduler(nformCode, userInfo));
		returnMap.put("emailHost", lstemailhost);
		returnMap.put("emailTemplate", lstemailtemplate);
		returnMap.put("emailScreen", lstemailscreen);

		return returnMap;
	}

	@Override
	public Map<String,Object> getEmailConfigControl(final int nformCode, final UserInfo userInfo) throws Exception {
		final Map<String, Object> returnMap = new HashMap<>();

		final String controlquery = " select coalesce (cm.jsondata->'scontrolids'->>'" + userInfo.getSlanguagetypecode()
				+ "',cm.jsondata->'scontrolids'->>'en-US')scontrolids,cm.ncontrolcode from controlmaster cm,"
				+ " sitecontrolmaster scm where cm.ncontrolcode = scm.ncontrolcode and cm.nformcode=" + nformCode
				+ " and scm.nisemailrequired = " + Enumeration.TransactionStatus.YES.gettransactionstatus()
				+ " and cm.nstatus in ( " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ","
				+ Enumeration.TransactionStatus.DEACTIVE.gettransactionstatus() + ") and scm.nstatus in ("
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ","
				+ Enumeration.TransactionStatus.DEACTIVE.gettransactionstatus() + ") and scm.nsitecode="
				+ userInfo.getNmastersitecode() + ";";
		final List<ControlMaster> lstcontrol = jdbcTemplate.query(controlquery, new ControlMaster());

		returnMap.put("formControls", lstcontrol);
		return returnMap;
	}
	
	@Override
	public Map<String,Object> getEmailConfigScheduler(final int nformCode, final UserInfo userInfo) throws Exception {
		final Map<String, Object> returnMap = new HashMap<>();
		
		final String schedulerquery = " select * from emailscreenscheduler where nformcode=" + nformCode
				+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		final List<EmailScreenScheduler> lstEmailScreenScheduler = jdbcTemplate.query(schedulerquery, new EmailScreenScheduler());	
		
		returnMap.put("emailScreenScheduler", lstEmailScreenScheduler);
		return returnMap;
	}

	@Override
	public ResponseEntity<Object> getEmailUserQuery(final int nformCode,final int ncontrolCode,final int nemailScreenSchedulerCode,
			final int nemailTypeCode, final UserInfo userInfo) throws Exception {
		final Map<String, Object> returnMap = new HashMap<>();		
		
		final String emailuserquery = "select nemailuserquerycode,squery,sdisplayname from emailuserquery where nemailtypecode= "
				+ nemailTypeCode + " and nformcode=" + nformCode + " and ncontrolcode=" + ncontrolCode + " and nemailscreenschedulercode="
				+ nemailScreenSchedulerCode + " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		final List<EmailUserQuery> lstEmailUserQuery = jdbcTemplate.query(emailuserquery, new EmailUserQuery());
		
		returnMap.put("emailUserQuery", lstEmailUserQuery);
		return new ResponseEntity<>(returnMap, HttpStatus.OK);
	}

	// Modified by sonia on 3rd sept 2025 for jira id:SWSM-12
	@Override
	public ResponseEntity<Object> createEmailConfig(final EmailConfig emailconfig,
			final List<EmailUserConfig> nusercode, final List<UserRole> nuserrolecode, final UserInfo userInfo)
			throws Exception {
		final List<Object> savedEmailConfigList = new ArrayList<>();

		final String sUserRoleCodes = nuserrolecode.stream().map(item -> item.getNuserrolecode()).map(String::valueOf)
				.collect(Collectors.joining(","));
		final String sUserCodes = nusercode.stream().map(item -> String.valueOf(item.getNusercode()))
				.collect(Collectors.joining(","));

		String statuscheck = "select nemailconfigcode, nemailtypecode from emailconfig where nemailtypecode=" 
				+ emailconfig.getNemailtypecode() + " and nsitecode=" + userInfo.getNmastersitecode() 
				+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		
		if (emailconfig.getNemailtypecode() == Enumeration.EmailType.SCHEDULEDBASEDMAIL.getEmailType()) {
			statuscheck += " and nemailscreenschedulercode=" + emailconfig.getNemailscreenschedulercode();
		} else {
			statuscheck += " and ncontrolcode=" + emailconfig.getNcontrolcode();
		}
		
		final EmailConfig emailconfig1 = (EmailConfig) jdbcUtilityFunction.queryForObject(statuscheck,
				EmailConfig.class, jdbcTemplate);

		if (emailconfig1 != null) {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage(
					emailconfig.getNemailtypecode() == Enumeration.EmailType.SCHEDULEDBASEDMAIL.getEmailType()
							? "IDS_ALREDYMAILCONFIGUREDFORTHISSCREENSCHEDULER" 
							: "IDS_ALREDYMAILCONFIGURED", userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
		} else {

			final String sequencequery = "select nsequenceno from SeqNoEmailManagement where stablename ='emailconfig' and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
			int nsequenceno = (int) jdbcUtilityFunction.queryForObject(sequencequery, Integer.class, jdbcTemplate);
			nsequenceno++;

			// Added nenablesms by Gowtham on 27th Sept 2025 for jira id:SWSM-64
			final String insertquery = "insert into emailconfig (nemailconfigcode,ncontrolcode,nemailhostcode,nemailscreencode,"
					+ " nemailtemplatecode,nemailuserquerycode,nenableemail,nenablesms,nformcode,nneedattachment,nemailtypecode,"
					+ " nemailscreenschedulercode,nsitecode,nstatus,dmodifieddate) values (" + nsequenceno + "," 
					+ emailconfig.getNcontrolcode() + "," + emailconfig.getNemailhostcode() + "," 
					+ emailconfig.getNemailscreencode() + "," + emailconfig.getNemailtemplatecode() + "," 
					+ emailconfig.getNemailuserquerycode() + "," + emailconfig.getNenableemail() + "," 
					+ emailconfig.getNenablesms() + "," + emailconfig.getNformcode() + "," 
					+ emailconfig.getNneedattachment() + "," + emailconfig.getNemailtypecode() + ","
					+ emailconfig.getNemailscreenschedulercode() + "," + userInfo.getNmastersitecode() + ","
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ",'"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' )";
			jdbcTemplate.execute(insertquery);

			final String updatequery = "update SeqNoEmailManagement set nsequenceno =" + nsequenceno
					+ " where stablename='emailconfig' and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
			jdbcTemplate.execute(updatequery);

			savedEmailConfigList.add(emailconfig);
			emailconfig.setNemailconfigcode(nsequenceno);
			auditUtilityFunction.fnInsertAuditAction(savedEmailConfigList, 1, null, Arrays.asList("IDS_ADDEMAILCONFIG"),
					userInfo);

			// Added by Gowtham on Oct 29 2025 for jira-id:BGSI-147 - start
			List<UserRole> lstEmailUserRoleConfig = new ArrayList<UserRole>();
			if (!nuserrolecode.isEmpty()) {
				final String userRoleQuery = "select nuserrolecode, nstatus from userrole where nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
						+ userInfo.getNmastersitecode() + " and nuserrolecode in (" + sUserRoleCodes + ")";
				lstEmailUserRoleConfig = jdbcTemplate.query(userRoleQuery, new UserRole());
			}

			final String userRoleSeqQuery = "select nsequenceno+1 from SeqNoEmailManagement where stablename ='emailuserroleconfig'"
					+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			int userRoleSequence = (int) jdbcUtilityFunction.queryForObject(userRoleSeqQuery, Integer.class,
					jdbcTemplate);

			List<EmailUserConfig> lstEmailUserConfig = new ArrayList<EmailUserConfig>();
			if (!nusercode.isEmpty()) {
				final String userQuery = "select s.nusercode,s.nstatus from users s where nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
						+ userInfo.getNmastersitecode() + " and nusercode in (" + sUserCodes + ")";
				lstEmailUserConfig = jdbcTemplate.query(userQuery, new EmailUserConfig());
			}

			final String userSeqQuery = "select nsequenceno+1 from SeqNoEmailManagement where stablename ='emailuserconfig'"
					+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			int userSequence = (int) jdbcUtilityFunction.queryForObject(userSeqQuery, Integer.class, jdbcTemplate);

			final StringBuilder roleInsertBuilder = new StringBuilder();
			final StringBuilder userInsertBuilder = new StringBuilder();

			roleInsertBuilder.append("INSERT INTO emailuserroleconfig"
					+ " (nemailuserroleconfigcode, nemailconfigcode, nuserrolecode, dmodifieddate, nstatus) VALUES ");

			userInsertBuilder.append("INSERT INTO emailuserconfig"
					+ " (nemailuserconfigcode, nemailuserroleconfigcode, nemailconfigcode, nusercode, dmodifieddate, nstatus) VALUES ");

			for (final UserRole userRole : lstEmailUserRoleConfig) {
				roleInsertBuilder.append("(" + userRoleSequence + "," + emailconfig.getNemailconfigcode() + ","
						+ userRole.getNuserrolecode() + ",'" + dateUtilityFunction.getCurrentDateTime(userInfo)
						+ "'," + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "),");
				List<EmailUserConfig> emailUserConfigLst = nusercode.stream()
						.filter(item -> item.getNuserrolecode() == userRole.getNuserrolecode())
						.collect(Collectors.toList());
				for (final EmailUserConfig user : emailUserConfigLst) {
					userInsertBuilder.append("(" + userSequence + "," + userRoleSequence + ","
							+ emailconfig.getNemailconfigcode() + "," + user.getNusercode()
							+ ",'" + dateUtilityFunction.getCurrentDateTime(userInfo) + "',"
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "),");
					userSequence++;
				}
				userRoleSequence++;
			}

			if (!lstEmailUserRoleConfig.isEmpty()) {
				jdbcTemplate.execute(roleInsertBuilder.toString().substring(0, roleInsertBuilder.length() - 1));
				final String userRoleUpdateQuery = "update SeqNoEmailManagement set nsequenceno =(" + userRoleSequence
						+ ") +( " + lstEmailUserRoleConfig.size()
						+ ")  where stablename='emailuserroleconfig' and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				jdbcTemplate.execute(userRoleUpdateQuery);
				final List<Object> savedList = new ArrayList<>();
				savedList.add(lstEmailUserRoleConfig);
				auditUtilityFunction.fnInsertListAuditAction(savedList, 1, null,
						Arrays.asList("IDS_ADDEMAILUSERROLECONFIG"), userInfo);
			}

			if (!lstEmailUserConfig.isEmpty()) {
				jdbcTemplate.execute(userInsertBuilder.toString().substring(0, userInsertBuilder.length() - 1));
				final String userUpdateQuery = "update SeqNoEmailManagement set nsequenceno =(" + userSequence + ") +( "
						+ lstEmailUserConfig.size() + ")  where stablename='emailuserconfig' and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
				jdbcTemplate.execute(userUpdateQuery);
				final List<Object> savedList = new ArrayList<>();
				savedList.add(lstEmailUserConfig);
				auditUtilityFunction.fnInsertListAuditAction(savedList, 1, null,
						Arrays.asList("IDS_ADDEMAILUSERCONFIG"), userInfo);
			}
			// end
			return getEmailConfigData(-1,emailconfig.getNemailtypecode(), userInfo);
		}
	}

	// Modified by sonia on 3rd sept 2025 for jira id:SWSM-12
	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> getActiveEmailConfigById(final int nemailconfigcode,final short nemailTypeCode, final UserInfo userInfo)
			throws Exception {
		final Map<String, Object> objMap = new HashMap<>();

		// Added nenablesms,senablesmsstatus by Gowtham on 27th Sept 2025 for jira-id:SWSM-64
		final String strQuery = "select eh.sprofilename,etc.nemailconfigcode,etc.nemailhostcode,etc.nsitecode,"
				+ " etc.nenableemail,etc.nemailtypecode,etc.nemailtemplatecode, etc.nenablesms,etc.nemailscreencode,"
				+ " etc.nstatus,eh.shostname,et.stemplatename,coalesce (t.jsondata->'stransdisplaystatus'->>'"
				+ userInfo.getSlanguagetypecode() + "',t.jsondata->'stransdisplaystatus'->>'en-US') as senablestatus, "
				+ " coalesce (ts1.jsondata ->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode()
				+ "',ts1.jsondata ->'stransdisplaystatus'->>'en-US') as senablesmsstatus,"
				+ "coalesce (qf.jsondata->'sdisplayname'->>'" + userInfo.getSlanguagetypecode()
				+ "',qf.jsondata->'sdisplayname'->>'en-US')as sscreenname,qf.nformcode,"
				+ "coalesce (cm.jsondata->'scontrolids'->>'" + userInfo.getSlanguagetypecode()
				+ "',cm.jsondata->'scontrolids'->>'en-US') as scontrolids,cm.ncontrolcode,"
				+ "emu.squery,etc.nemailuserquerycode, emu.sdisplayname, "
				+ " coalesce (etp.jsondata->'semailtypename'->>'" + userInfo.getSlanguagetypecode()
				+ "',etp.jsondata->'semailtypename'->>'en-US')as semailtypename,etc.nemailscreenschedulercode,ess.sscheduletypename "
				+ " from emailconfig etc,emailhost eh,emailtemplate et,emailscreen es,transactionstatus t,"
				+ " transactionstatus ts1,qualisforms qf,controlmaster cm ,emailuserquery emu,emailtype etp,emailscreenscheduler ess "
				+ "where t.ntranscode=etc.nenableemail and qf.nformcode = etc.nformcode and cm.ncontrolcode = etc.ncontrolcode"
				+ " and es.nemailscreencode = etc.nemailscreencode and etc.nemailhostcode = eh.nemailhostcode "
				+ " and etc.nemailtemplatecode = et.nemailtemplatecode and etp.nemailtypecode=etc.nemailtypecode "
				+ " and ts1.ntranscode=etc.nenablesms and emu.nemailuserquerycode=etc.nemailuserquerycode"
				+ " and etc.nemailscreenschedulercode = ess.nemailscreenschedulercode"
				+ " and etc.nemailtypecode= "+nemailTypeCode+" "
				+ " and ess.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and etc.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and emu.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and eh.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and et.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and es.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and etp.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and eh.nsitecode="
				+ userInfo.getNmastersitecode() + " and et.nsitecode=" + userInfo.getNmastersitecode()
				+ " and es.nsitecode=" + userInfo.getNmastersitecode() + " and etp.nsitecode="
				+ userInfo.getNmastersitecode() + " and etc.nemailconfigcode=" + nemailconfigcode;
		final List<EmailConfig> emailConfiglst = jdbcTemplate.query(strQuery, new EmailConfig());

		if (emailConfiglst.isEmpty()) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_MAILCONFIG",
							userInfo.getSlanguagefilename()) + " " +
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			objMap.put("EmailConfig", emailConfiglst.get(0));
			objMap.putAll((Map<String, Object>) getEmailConfigDetails(emailConfiglst.get(0).getNformcode(), userInfo));
			objMap.putAll((Map<String, Object>)getEmailUserQuery(emailConfiglst.get(0).getNformcode(),emailConfiglst.get(0).getNcontrolcode(),
					emailConfiglst.get(0).getNemailscreenschedulercode(),emailConfiglst.get(0).getNemailtypecode(),userInfo).getBody());

			return new ResponseEntity<>(objMap, HttpStatus.OK);
		}
	}

	@Override
	public ResponseEntity<Object> deleteEmailConfig(final EmailConfig emailconfig, final UserInfo userInfo)
			throws Exception {
		final EmailConfig emailConfig = (EmailConfig) jdbcUtilityFunction
				.queryForObject("select nemailconfigcode,nemailhostcode,nemailscreencode,nemailtemplatecode,"
								+ " nenableemail,nenablesms,ncontrolcode,nformcode from emailconfig "
								+ " where nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " and nemailconfigcode = " + emailconfig.getNemailconfigcode(),
						EmailConfig.class, jdbcTemplate);
		if (emailConfig == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_MAILCONFIG",
							userInfo.getSlanguagefilename()) + " " +
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			// Added by Gowtham on 31th Oct 2025 for BGSI-147 - start
			final String userRoleQuery = "select * from emailuserroleconfig where nemailconfigcode="
					+ emailconfig.getNemailconfigcode();
			final List<EmailUserRoleConfig> lstEmailUserRoleConfig = jdbcTemplate.query(userRoleQuery,
					new EmailUserRoleConfig());

			if (!lstEmailUserRoleConfig.isEmpty()) {
				jdbcTemplate.execute(
						"delete from emailuserroleconfig where nemailconfigcode=" + emailconfig.getNemailconfigcode());
				auditUtilityFunction.fnInsertListAuditAction(Arrays.asList(lstEmailUserRoleConfig), 1, null,
						Arrays.asList("IDS_DELETEEMAILUSERROLECONFIG"), userInfo);
			}
			// end
			final String userQuery = "select * from emailuserconfig where nemailconfigcode="
					+ emailconfig.getNemailconfigcode();
			final List<EmailUserConfig> lstEmailUserConfig = jdbcTemplate.query(userQuery, new EmailUserConfig());

			if (!lstEmailUserConfig.isEmpty()) {
				jdbcTemplate.execute(
						"delete from emailuserconfig where nemailconfigcode=" + emailconfig.getNemailconfigcode());
				auditUtilityFunction.fnInsertListAuditAction(Arrays.asList(lstEmailUserConfig), 1, null,
						Arrays.asList("IDS_DELETEEMAILUSERCONFIG"), userInfo);
			}
			final List<Object> deletedEmailHostList = new ArrayList<>();

			final String updateQueryString = "update emailconfig set nstatus = "
					+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ", dmodifieddate= '"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where nemailconfigcode="
					+ emailconfig.getNemailconfigcode();
			jdbcTemplate.execute(updateQueryString);

			emailconfig.setNstatus((short) Enumeration.TransactionStatus.DELETED.gettransactionstatus());
			deletedEmailHostList.add(emailconfig);
			auditUtilityFunction.fnInsertAuditAction(deletedEmailHostList, 1, null,
					Arrays.asList("IDS_DELETEEMAILCONFIG"), userInfo);
			return getEmailConfigData(-1,emailconfig.getNemailtypecode(), userInfo);
		}
	}

	// Modified by sonia on 3rd sept 2025 for jira id:SWSM-12
	@Override
	public ResponseEntity<Object> updateEmailConfig(final EmailConfig emailconfig, final UserInfo userInfo)
			throws Exception {
		final EmailConfig emailConfigById = (EmailConfig) jdbcUtilityFunction.queryForObject(
				" select nemailconfigcode,nemailhostcode,nemailscreencode, nemailtemplatecode,nenableemail,nenablesms,"
						+ " ncontrolcode, nformcode, nemailuserquerycode from emailconfig where nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nemailconfigcode = "
						+ emailconfig.getNemailconfigcode(),
				EmailConfig.class, jdbcTemplate);
		if (emailConfigById == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_MAILCONFIG",
							userInfo.getSlanguagefilename()) + " " +
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			final String statuscheck = "select nemailconfigcode from emailconfig where ncontrolcode = "
					+ emailconfig.getNcontrolcode() + " and nemailconfigcode <> " + emailconfig.getNemailconfigcode()
					+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and nsitecode=" + userInfo.getNmastersitecode();
			final EmailConfig emailConfigObj = (EmailConfig) jdbcUtilityFunction.queryForObject(statuscheck,
					EmailConfig.class, jdbcTemplate);
			if (emailConfigObj != null) {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_ALREDYMAILCONFIGURED",
						userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
			} else {
				// Added nenablesms by Gowtham on 27th Sept 2025 for jira id:SWSM-64
				final String updateQueryString = "update emailconfig set nemailtemplatecode="
						+ emailconfig.getNemailtemplatecode() + ",nemailuserquerycode="
						+ emailconfig.getNemailuserquerycode() + ",nemailhostcode=" + emailconfig.getNemailhostcode()
						+ ",ncontrolcode=" + emailconfig.getNcontrolcode() + ",nemailscreencode="
						+ emailconfig.getNemailscreencode() + ",nformcode= " + emailconfig.getNformcode() + ", "
						+ "nenableemail=" + emailconfig.getNenableemail() + ",nemailtypecode="
						+ emailconfig.getNemailtypecode() + ",nemailscreenschedulercode=" 
						+ emailconfig.getNemailscreenschedulercode() + ", nenablesms=" + emailconfig.getNenablesms() 
						+ ", dmodifieddate= '" + dateUtilityFunction.getCurrentDateTime(userInfo) 
						+ "' where nemailconfigcode=" + emailconfig.getNemailconfigcode() + " and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				jdbcTemplate.execute(updateQueryString);
				final List<Object> listAfterSave = new ArrayList<>();
				listAfterSave.add(emailconfig);
				final List<Object> listBeforeSave = new ArrayList<>();
				listBeforeSave.add(emailConfigById);
				auditUtilityFunction.fnInsertAuditAction(listAfterSave, 2, listBeforeSave,
						Arrays.asList("IDS_EDITEMAILCONFIG"), userInfo);
				return getEmailConfigData(emailconfig.getNemailconfigcode(),emailconfig.getNemailtypecode(), userInfo);
			}
		}
	}

	@Override
	public ResponseEntity<Object> getUserRoleEmail(final int nuserrolecode, final UserInfo userInfo) throws Exception {
		final String strQuery = "select u.semail,u.nusercode from  usermultirole um,userssite us,users u,userrole ur "
				+ " where ur.nuserrolecode=um.nuserrolecode and um.nusersitecode=us.nusersitecode and us.nusercode=u.nusercode"
				+ " and us.nsitecode=1 and u.nsitecode=" + userInfo.getNmastersitecode() + " and um.nuserrolecode="
				+ nuserrolecode + " and u.ntransactionstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and u.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and us.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ur.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and um.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		return new ResponseEntity<>(jdbcTemplate.query(strQuery, new Users()), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getUserEmailConfig(final int nemailconfigcode, final int nuserrolecode, final boolean isUserRole,
			final UserInfo userInfo) throws Exception {
		final Map<String, Object> outputMap = new HashMap<String, Object>();
		String query = "select nformcode from emailconfig where nemailconfigcode=" + nemailconfigcode + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		final EmailConfig emailConfig = (EmailConfig) jdbcUtilityFunction.queryForObject(query, EmailConfig.class,
				jdbcTemplate);

		if (emailConfig != null) {
			// Added emailuserroleconfig by Gowtham on 30 Oct 2025 for jira-id:BGSI-147
			query = "select uc.nusercode from emailuserconfig uc"
					+ " join emailuserroleconfig urc on urc.nemailconfigcode=" + nemailconfigcode
					+ " and urc.nuserrolecode = " + nuserrolecode + " and urc.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " where urc.nemailuserroleconfigcode = uc.nemailuserroleconfigcode and uc.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			final List<String> lstUserCodes = jdbcTemplate.queryForList(query, String.class);

			String susercode = lstUserCodes.stream().collect(Collectors.joining(","));

			if (susercode.isEmpty()) {
				susercode = "-1";
			} else {
				susercode = susercode + ",-1";
			}
			
			// Added by Gowtham on 5 Nov 2025 for jira-id:BGSI-147
			List<EmailUserConfig> lstEmailUserConfig = new ArrayList<EmailUserConfig>();
			List<EmailUserRoleConfig> lstUserRole = new ArrayList<EmailUserRoleConfig>();
			
			if (isUserRole) {
				
				query = "select nuserrolecode, suserrolename, " + nemailconfigcode + " as nemailconfigcode"
						+ " from userrole where nuserrolecode > 0 and nuserrolecode not in ("
						+ " select nuserrolecode from emailuserroleconfig where nemailconfigcode =" + nemailconfigcode
						+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ") and nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode = "
						+ userInfo.getNmastersitecode();
				lstUserRole = jdbcTemplate.query(query, new EmailUserRoleConfig());
				
				if (lstUserRole.isEmpty()) {
					return new ResponseEntity<>(
							commonFunction.getMultilingualMessage("IDS_NOAVAIALBLEUSERROLES", userInfo.getSlanguagefilename()),
							HttpStatus.EXPECTATION_FAILED);	
				}
				
			} else {
				query = "select u.nusercode, umr.nuserrolecode,"
						+ " concat(u.sfirstname, u.slastname, '(', u.sloginid , ')') as semail"
						+ " from users u join userssite us on us.nusercode=u.nusercode"
						+ " join usermultirole umr on us.nusersitecode=umr.nusersitecode"
						+ " join userrole ur on umr.nuserrolecode=ur.nuserrolecode"
						+ " where u.nusercode > 0 and u.nusercode not in (" + susercode + ") and u.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and u.nsitecode ="
						+ userInfo.getNmastersitecode() + " and umr.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and umr.nsitecode="
						+ userInfo.getNmastersitecode() + " and ur.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ur.nsitecode="
						+ userInfo.getNmastersitecode() + " and us.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				
				if (nuserrolecode != -1) {
					query += " and ur.nuserrolecode=" + nuserrolecode;
				}
				
				lstEmailUserConfig = jdbcTemplate.query(query, new EmailUserConfig());
				
				if (lstEmailUserConfig.isEmpty()) {
					return new ResponseEntity<>(
							commonFunction.getMultilingualMessage("IDS_NOAVAIALBLEUSERS", userInfo.getSlanguagefilename()),
							HttpStatus.EXPECTATION_FAILED);
				}
			}
			
			outputMap.put("userRole", lstUserRole);
			outputMap.put("users", lstEmailUserConfig);
			return new ResponseEntity<>(outputMap, HttpStatus.OK);
			// end

		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_MAILCONFIG",
							userInfo.getSlanguagefilename()) + " " +
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	@Override
	public ResponseEntity<Object> createUsers(final int nemailconfigcode, final String nusercode,
			final int nuserrolecode, final UserInfo userInfo) throws Exception {
		final String query1 = "select nemailtypecode from emailconfig where nemailconfigcode=" + nemailconfigcode 
				+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		final EmailConfig emailconfig = (EmailConfig) jdbcUtilityFunction.queryForObject(query1, EmailConfig.class,
				jdbcTemplate);

		if (emailconfig == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_MAILCONFIG",
							userInfo.getSlanguagefilename()) + " " +
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			// Added by Gowtham on 30 Oct 2025 for jira-id:BGSI-147
			String strQuery = "select eurc.nemailuserroleconfigcode, ur.suserrolename from emailuserroleconfig eurc"
					+ " join userrole ur on eurc.nuserrolecode=ur.nuserrolecode"
					+ " where eurc.nuserrolecode=" + nuserrolecode + " and eurc.nemailconfigcode=" 
					+ nemailconfigcode + " and ur.nsitecode=" + userInfo.getNmastersitecode() 
					+ " and eurc.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and ur.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			final EmailUserRoleConfig emailUserRoleConfig = (EmailUserRoleConfig) jdbcUtilityFunction
					.queryForObject(strQuery, EmailUserRoleConfig.class, jdbcTemplate);
			
			if (emailUserRoleConfig == null) {
				strQuery = "select suserrolename from userrole where nuserrolecode=" + nuserrolecode
						+ " and nsitecode=" + userInfo.getNmastersitecode();
				final UserRole objUserRole = (UserRole) jdbcUtilityFunction
						.queryForObject(strQuery, UserRole.class, jdbcTemplate);
				
				return new ResponseEntity<>(objUserRole.getSuserrolename() + " " +
						commonFunction.getMultilingualMessage("IDS_USERROLEALREADYDELETED",
								userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			}
			
			strQuery = "select euc.nusercode from emailuserconfig euc"
					+ " join emailuserroleconfig eurc on eurc.nemailuserroleconfigcode = euc.nemailuserroleconfigcode"
					+ " and eurc.nuserrolecode=" + nuserrolecode + " where euc.nusercode in(" + nusercode
					+ ") and euc.nemailconfigcode=" + nemailconfigcode;
			final List<EmailUserConfig> listUsercode = jdbcTemplate.query(strQuery, new EmailUserConfig());
			
			final String[] stringArray = nusercode.split(",");
			final String nonMatchingUserCodes = Arrays.stream(stringArray).filter(
					code -> listUsercode.stream().noneMatch(user -> String.valueOf(user.getNusercode()).equals(code)))
					.collect(Collectors.joining(","));
			if (!nonMatchingUserCodes.isEmpty()) {
				final String query = "select nusercode,nstatus," + nemailconfigcode	+ " as nemailconfigcode,"
						+ " sfirstname||' '||slastname as susername from users where nusercode in (" 
						+ nusercode + ") and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				final List<EmailUserConfig> lstEmailUserConfig = jdbcTemplate.query(query, new EmailUserConfig());

				final String sequencequery = "select nsequenceno from SeqNoEmailManagement where stablename ='emailuserconfig'"
						+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				final int nsequenceno = (int) jdbcUtilityFunction.queryForObject(sequencequery, Integer.class,
						jdbcTemplate);

				// Added nemailuserroleconfigcode by Gowtham on 30 Oct 2025 for jira-id:BGSI-147
				final String insertquery = "Insert into emailuserconfig (nemailuserconfigcode,nemailuserroleconfigcode,"
						+ " nemailconfigcode,nstatus,nusercode,dmodifieddate) select " + nsequenceno
						+ " + rank() over (order by nusercode)" + " as nemailuserconfigcode, "
						+ emailUserRoleConfig.getNemailuserroleconfigcode() + " as nemailuserroleconfigcode, "
						+ nemailconfigcode + " as nemailconfigcode, "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " as nstatus, nusercode,'"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' from users where nusercode in ("
						+ nonMatchingUserCodes + ")";
				jdbcTemplate.execute(insertquery);

				final String updatequery = "update SeqNoEmailManagement set nsequenceno =(" + nsequenceno + ")+ ("
						+ lstEmailUserConfig.size() + ") where stablename ='emailuserconfig' and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
				jdbcTemplate.execute(updatequery);

				final List<Object> savedList = new ArrayList<>();
				savedList.add(lstEmailUserConfig);
				auditUtilityFunction.fnInsertListAuditAction(savedList, 1, null,
						Arrays.asList("IDS_ADDEMAILUSERCONFIG"), userInfo);
			}
			return getEmailConfigData(nemailconfigcode,emailconfig.getNemailtypecode(), userInfo);
		}
	}

	@Override
	public ResponseEntity<Object> deleteUsers(final EmailUserConfig emailuserconfig, final UserInfo userInfo)
			throws Exception {
		final List<Object> listAfterSave = new ArrayList<>();

		String query = "select nemailtypecode from emailconfig where nemailconfigcode=" + emailuserconfig.getNemailconfigcode()
				+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
				+ userInfo.getNmastersitecode();
		final EmailConfig emailconfig = (EmailConfig) jdbcUtilityFunction.queryForObject(query, EmailConfig.class,
				jdbcTemplate);

		if (emailconfig == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_MAILCONFIG",
							userInfo.getSlanguagefilename()) + " " +
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			// Added by Gowtham on 30 Oct 2025 for jira-id:BGSI-147
			String strQuery = "select eurc.nemailuserroleconfigcode, ur.suserrolename from emailuserroleconfig eurc"
					+ " join userrole ur on eurc.nuserrolecode=ur.nuserrolecode"
					+ " where eurc.nuserrolecode=" + emailuserconfig.getNuserrolecode() + " and eurc.nemailconfigcode=" 
					+ emailuserconfig.getNemailconfigcode() + " and ur.nsitecode=" + userInfo.getNmastersitecode() 
					+ " and eurc.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and ur.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			final EmailUserRoleConfig emailUserRoleConfig = (EmailUserRoleConfig) jdbcUtilityFunction
					.queryForObject(strQuery, EmailUserRoleConfig.class, jdbcTemplate);
			
			if (emailUserRoleConfig == null) {
				strQuery = "select suserrolename from userrole where nuserrolecode=" + emailuserconfig.getNuserrolecode()
						+ " and nsitecode=" + userInfo.getNmastersitecode();
				final UserRole objUserRole = (UserRole) jdbcUtilityFunction
						.queryForObject(strQuery, UserRole.class, jdbcTemplate);
				
				return new ResponseEntity<>(objUserRole.getSuserrolename() + " " +
						commonFunction.getMultilingualMessage("IDS_USERROLEALREADYDELETED",
								userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			}
			
			query = "select nemailuserconfigcode from emailuserconfig where nemailuserconfigcode="
					+ emailuserconfig.getNemailuserconfigcode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			// ALPD-3636
			final EmailUserConfig emailuserconfigs = (EmailUserConfig) jdbcUtilityFunction.queryForObject(query,
					EmailUserConfig.class, jdbcTemplate);

			if (emailuserconfigs != null) {
				query = "delete from emailuserconfig where nemailuserconfigcode="
						+ emailuserconfig.getNemailuserconfigcode();
				jdbcTemplate.execute(query);

				listAfterSave.add(emailuserconfig);
				auditUtilityFunction.fnInsertAuditAction(listAfterSave, 1, null,
						Arrays.asList("IDS_DELETEEMAILCONFIGUSERS"), userInfo);
				return getEmailConfigData(emailuserconfig.getNemailconfigcode(),emailconfig.getNemailtypecode(), userInfo);
			} else {
				return new ResponseEntity<>(emailuserconfig.getSusername() + " " +
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			}
		}
	}
	
	// Added by Gowtham on 30 Oct 2025 for jira-id:BGSI-147
	@Override
	public ResponseEntity<Object> getUserRoles(UserInfo userInfo) throws Exception {
		Map<String, Object> outputMap = new HashMap<String,Object>();
		final String userRoleQuery = "select nuserrolecode, suserrolename from userrole"
				+ " where nuserrolecode > 0 and nstatus=" 
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
				+ userInfo.getNmastersitecode();
		final List<UserRole> lstUserRole = jdbcTemplate.query(userRoleQuery, new UserRole());
		outputMap.put("userRole", lstUserRole);
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}
	
	// Added by Gowtham on 30 Oct 2025 for jira-id:BGSI-147
	@Override
	public ResponseEntity<Object> getEmailUserOnUserRole(int nuserrolecode, int nemailconfigcode, UserInfo userInfo) throws Exception {
		Map<String, Object> outputMap = new HashMap<String,Object>();
		final String query = "select u.nusercode,umr.nuserrolecode,u.semail,uc.nemailconfigcode,uc.nemailuserconfigcode,"
				+ " concat(u.sfirstname,' ',u.slastname,'(',u.sloginid,')') as susername"
				+ " from users u join userssite us on us.nusercode=u.nusercode"
				+ " join usermultirole umr on us.nusersitecode = umr.nusersitecode"
				+ " join userrole ur on umr.nuserrolecode = ur.nuserrolecode and ur.nuserrolecode ="
				+ nuserrolecode + " join emailuserconfig uc on u.nusercode = uc.nusercode"
				+ " join emailuserroleconfig urc on urc.nemailuserroleconfigcode = uc.nemailuserroleconfigcode"
				+ " and umr.nuserrolecode = urc.nuserrolecode and urc.nemailconfigcode=uc.nemailconfigcode"
				+ " where u.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
				+ " and u.nsitecode = " + userInfo.getNmastersitecode() + " and umr.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
				+ " and umr.nsitecode = " + userInfo.getNmastersitecode() + " and ur.nstatus=" 
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
				+ " and ur.nsitecode = " + userInfo.getNmastersitecode() + " and uc.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and us.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and uc.nemailconfigcode=" + nemailconfigcode;
		final List<EmailUserConfig> lstEmailUserConfig = jdbcTemplate.query(query, new EmailUserConfig());
		outputMap.put("users", lstEmailUserConfig);
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}
	
	// Added by Gowtham on 30 Oct 2025 for jira-id:BGSI-147
	@Override
	public ResponseEntity<Object> getEmailUsers(int nuserrolecode, UserInfo userInfo) throws Exception {
		Map<String, Object> outputMap = new HashMap<String,Object>();
		final String query = "select distinct u.nusercode,umr.nuserrolecode,"
				+ " concat(u.sfirstname,' ',u.slastname,'(',u.sloginid,')') as semail"
				+ " from users u join userssite us on us.nusercode=u.nusercode"
				+ " join usermultirole umr on us.nusersitecode = umr.nusersitecode"
				+ " join userrole ur on umr.nuserrolecode=ur.nuserrolecode"
				+ " and ur.nuserrolecode= " + nuserrolecode + " where u.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and u.nsitecode="
				+ userInfo.getNmastersitecode() + " and umr.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and umr.nsitecode="
				+ userInfo.getNmastersitecode() + " and ur.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ur.nsitecode="
				+ userInfo.getNmastersitecode() + " and us.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		final List<EmailUserConfig> lstEmailUserConfig = jdbcTemplate.query(query, new EmailUserConfig());
		outputMap.put("users", lstEmailUserConfig);
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	// Added by Gowtham on 30 Oct 2025 for jira-id:BGSI-147
	@Override
	public ResponseEntity<Object> createUserRoles(final int nemailconfigcode, final String nuserrolecode,
			final UserInfo userInfo) throws Exception {
		final String query1 = "select nemailtypecode from emailconfig where nemailconfigcode=" + nemailconfigcode 
				+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		final EmailConfig emailconfig = (EmailConfig) jdbcUtilityFunction.queryForObject(query1, EmailConfig.class,
				jdbcTemplate);

		if (emailconfig == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_MAILCONFIG",
							userInfo.getSlanguagefilename()) + " " +
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			final String strQuery = "select nuserrolecode from emailuserroleconfig where nuserrolecode in(" + nuserrolecode
					+ ") and nemailconfigcode=" + nemailconfigcode;
			final List<EmailUserRoleConfig> listUserRolecode = jdbcTemplate.query(strQuery, new EmailUserRoleConfig());

			final String[] stringArray = nuserrolecode.split(",");
			final String nonMatchingUserRoleCodes = Arrays.stream(stringArray).filter(
					code -> listUserRolecode.stream().noneMatch(user -> String.valueOf(user.getNuserrolecode()).equals(code)))
					.collect(Collectors.joining(","));

			if (!nonMatchingUserRoleCodes.isEmpty()) {
				final String query = "select suserrolename, nuserrolecode from userrole where nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nuserrolecode in ("
						+ nuserrolecode + ")";
				final List<EmailUserRoleConfig> lstEmailUserConfig = jdbcTemplate.query(query, new EmailUserRoleConfig());

				final String sequencequery = "select nsequenceno from  SeqNoEmailManagement"
						+ " where stablename ='emailuserroleconfig' and nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				final int nsequenceno = (int) jdbcUtilityFunction.queryForObject(sequencequery, Integer.class,
						jdbcTemplate);

				final String insertquery = "Insert into emailuserroleconfig (nemailuserroleconfigcode,nemailconfigcode,"
						+ " nstatus,nuserrolecode,dmodifieddate) select " + nsequenceno
						+ " + rank() over (order by nuserrolecode) as nemailuserroleconfigcode," + nemailconfigcode
						+ " as nemailconfigcode ," + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " as nstatus,nuserrolecode,'" + dateUtilityFunction.getCurrentDateTime(userInfo)
						+ "' from userrole where nuserrolecode in (" + nonMatchingUserRoleCodes + ")";
				jdbcTemplate.execute(insertquery);

				final String updatequery = "update SeqNoEmailManagement set nsequenceno=" 
						+ nsequenceno + lstEmailUserConfig.size() + " where stablename ='emailuserroleconfig'"
						+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				jdbcTemplate.execute(updatequery);

				final List<Object> savedList = new ArrayList<>();
				savedList.add(lstEmailUserConfig);
				auditUtilityFunction.fnInsertListAuditAction(savedList, 1, null,
						Arrays.asList("IDS_ADDEMAILUSERROLESCONFIG"), userInfo);
			}
			return getEmailConfigData(nemailconfigcode,emailconfig.getNemailtypecode(), userInfo);
		}
	}

	// Added by Gowtham on 29 Oct 2025 for jira-id:BGSI-147
	@Override
	public ResponseEntity<Object> deleteUserRole(final EmailUserRoleConfig emailUserRoleConfig, final UserInfo userInfo)
			throws Exception {
		final List<Object> listAfterSave = new ArrayList<>();

		String query = "select nemailtypecode from emailconfig where nemailconfigcode="
				+ emailUserRoleConfig.getNemailconfigcode() + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
				+ userInfo.getNmastersitecode();
		final EmailConfig emailconfig = (EmailConfig) jdbcUtilityFunction.queryForObject(query, EmailConfig.class,
				jdbcTemplate);

		if (emailconfig == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_MAILCONFIG",
							userInfo.getSlanguagefilename()) + " " +
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			query = "select * from emailuserroleconfig where nemailconfigcode="
					+ emailUserRoleConfig.getNemailconfigcode() + " and nuserrolecode ="
					+ emailUserRoleConfig.getNuserrolecode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			// ALPD-3636
			final EmailUserRoleConfig emailuserroleconfig = (EmailUserRoleConfig) jdbcUtilityFunction
					.queryForObject(query, EmailUserRoleConfig.class, jdbcTemplate);

			if (emailuserroleconfig != null) {
				query = "delete from emailuserroleconfig where nemailconfigcode="
						+ emailUserRoleConfig.getNemailconfigcode() + " and nuserrolecode ="
						+ emailUserRoleConfig.getNuserrolecode() + " and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				jdbcTemplate.execute(query);

				query = "delete from emailuserconfig where nemailconfigcode="
						+ emailUserRoleConfig.getNemailconfigcode() + " and nemailuserroleconfigcode="
						+ emailuserroleconfig.getNemailuserroleconfigcode();
				jdbcTemplate.execute(query);

				listAfterSave.add(emailuserroleconfig);
				auditUtilityFunction.fnInsertAuditAction(listAfterSave, 1, null,
						Arrays.asList("IDS_DELETEEMAILCONFIGUSERROLES"), userInfo);
				return getEmailConfigData(emailUserRoleConfig.getNemailconfigcode(),emailconfig.getNemailtypecode(), userInfo);
			} else {
				query = "select suserrolename from userrole where nuserrolecode=" + emailUserRoleConfig.getNuserrolecode()
						+ " and nsitecode=" + userInfo.getNmastersitecode();
				final UserRole objUserRole = (UserRole) jdbcUtilityFunction
						.queryForObject(query, UserRole.class, jdbcTemplate);
				
				return new ResponseEntity<>(objUserRole.getSuserrolename() + " " +
						commonFunction.getMultilingualMessage("IDS_USERROLEALREADYDELETED",
								userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			}
		}
	}

}