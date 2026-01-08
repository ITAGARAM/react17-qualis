package com.agaramtech.qualis.global;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import com.agaramtech.qualis.emailmanagement.model.EmailAlertTransHistory;
import com.agaramtech.qualis.emailmanagement.model.EmailAlertTransaction;
import com.agaramtech.qualis.emailmanagement.model.EmailConfig;
import com.agaramtech.qualis.emailmanagement.model.EmailDynamicFile;
import com.agaramtech.qualis.emailmanagement.model.EmailHost;
import com.agaramtech.qualis.emailmanagement.model.EmailScreen;
import com.agaramtech.qualis.emailmanagement.model.EmailUserQuery;
import com.agaramtech.qualis.emailmanagement.model.SeqNoEmailManagement;
import com.agaramtech.qualis.emailmanagement.model.SmsSettings;
import com.twilio.Twilio;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.internet.AddressException;
import jakarta.mail.BodyPart;
import jakarta.mail.Multipart;
import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class EmailDAOSupport {

	private static final Log LOGGER = LogFactory.getLog(EmailDAOSupport.class);

	private final StringUtilityFunction stringUtilityFunction;
	private final DateTimeUtilityFunction dateUtilityFunction;
	// private final ClassUtilityFunction classUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	// private ValidatorDel valiDatorDel;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	// private final AuditUtilityFunction auditUtilityFunction;
	private final ProjectDAOSupport projectDAOSupport;
	private final PasswordUtilityFunction passwordUtilityFunction;
	private final FTPUtilityFunction ftpUtilityFunction;

	//Modified by sonia on 18th Nov 2025 for jira id:BGSI-234
	@SuppressWarnings("unchecked")
	public ResponseEntity<Object> createEmailAlertTransaction(Map<String, Object> inputMap, UserInfo userinfo)throws Exception {

		final Map<String, Object> objMap = inputMap;
		String ssystemid = null;
		int ncontrolcode =-1;
			
		if(inputMap.containsKey("ncontrolcode")) {
			ncontrolcode =(int) inputMap.get("ncontrolcode");
		}
			
		if (inputMap.containsKey("ssystemid")) {
			ssystemid = inputMap.get("ssystemid").toString();
		}
				
		//#SECURITY-VULNERABILITY-MERGING-START
		//Added by sonia on 29th Nov 2025 for jira id:SWSM-125
		int nregtypecode =-1;
		int nregsubtypecode =-1;
		
		if(inputMap.containsKey("nregtypecode")) {
			nregtypecode=(int) inputMap.get("nregtypecode");
		}
		
		if(inputMap.containsKey("nregsubtypecode")) {
			nregsubtypecode=(int) inputMap.get("nregsubtypecode");
		}
		//#SECURITY-VULNERABILITY-MERGING-END
		
		//Added by sonia on 15th sept 2025 for jira id:SWSM-12
		String columnName ="";
		//#SECURITY-VULNERABILITY-MERGING-END
		// Added nenablesms by Gowtham on 27th Sept 2025 for jira id:SWSM-64
		final String checkEmailEnabled = " select ec.nemailconfigcode, ec.nemailuserquerycode, ec.nenableemail, ec.nenablesms,"
									   + " euq.scolumnname,euq.stablename,ec.nemailtypecode,euq.nqueryneed,euq.squery "
									   + " from emailconfig ec "
									   + " join emailscreen es on es.nemailscreencode=ec.nemailscreencode "
									   + " join emailtype et on et.nemailtypecode=ec.nemailtypecode "
									   + " join emailuserquery euq on euq.nemailuserquerycode=ec.nemailuserquerycode "
									   + " where ec.nformcode = "+ userinfo.getNformcode() + " "
									   + " and ec.ncontrolcode = " + ncontrolcode
									   + " and ec.nregtypecode ="+nregtypecode+" and ec.nregsubtypecode ="+nregsubtypecode+" " 
									   + " and ec.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
									   + " and ec.nsitecode = " + userinfo.getNmastersitecode()
									   + " and es.nstatus= " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									   + " and es.nsitecode = " + userinfo.getNmastersitecode()
									   + " and et.nstatus= " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									   + " and et.nsitecode = " + userinfo.getNmastersitecode()
									   + " and euq.nstatus= "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

		final EmailConfig emailConfig = (EmailConfig) jdbcUtilityFunction.queryForObject(checkEmailEnabled,EmailConfig.class, jdbcTemplate);

		// Added nenablesms by Gowtham on 27th Sept 2025 for jira id:SWSM-64
		if (emailConfig != null	&& (emailConfig.getNenableemail() == Enumeration.TransactionStatus.YES.gettransactionstatus() || emailConfig.getNenablesms() == Enumeration.TransactionStatus.YES.gettransactionstatus())) {
			
			//#SECURITY-VULNERABILITY-MERGING-START-syed-nasrin
			//String columnName =emailConfig.getScolumnname();
			columnName =emailConfig.getScolumnname();
			//#SECURITY-VULNERABILITY-MERGING-END
			
			final String sQuery = " lock  table lockmail " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
			jdbcTemplate.execute(sQuery);

			String emailQuery = "";
			boolean isEmailQueryUser = false;	
			List<String> lstInteger = new ArrayList<>();
			List<Map<String, String>> mailDetailList = new ArrayList<>();	
			Map<String, String> strFailedStatus = new HashMap<String, String>();
			//Added by sonia on 11th sept 2025 for jira id:SWSM-12
			List<Map<String, Object>> ccEmailList = null;
			String allCCEmails = "";	
			String allCCPhone ="";
			
			//Modified by sonia on 15th sept 2025 for jira id:SWSM-12
			if ((inputMap.get(columnName)) instanceof List) {
				lstInteger = (List<String>) inputMap.get(columnName);
			} else if (inputMap.get(columnName) instanceof Integer) {
				lstInteger.add(String.valueOf( inputMap.get(columnName)));
			}	

			lstInteger.stream().forEach(primarykey -> {
				try {
					// ALPD-5651 Modified code by Vishakh for temperory fix for release when configured mail (06-04-2025)
					Map<String, String> mailDetails = new HashMap<String, String>();
					mailDetails = getEmailDetails(emailConfig.getNemailconfigcode(), primarykey,userinfo.getNtranssitecode(), null, userinfo, objMap);

					if (mailDetails.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus()) == Enumeration.ReturnStatus.FAILED.getreturnstatus()) {
						strFailedStatus.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),Enumeration.ReturnStatus.FAILED.getreturnstatus());
					}
					mailDetailList.add(mailDetails);
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
			if (strFailedStatus.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus()) == Enumeration.ReturnStatus.FAILED.getreturnstatus()) {
				return null;
			}
			final String sSeqnoQuery = " select stablename,nsequenceno from  seqnoemailmanagement where stablename in('emailalerttransaction','emailalerttranshistory') and " 
									 + " nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

			final List<SeqNoEmailManagement> lstSeqNo = jdbcTemplate.query(sSeqnoQuery, new SeqNoEmailManagement());
			inputMap = lstSeqNo.stream().collect(Collectors.toMap(SeqNoEmailManagement::getStablename,SeqNoEmailManagement -> SeqNoEmailManagement.getNsequenceno()));

			int nseqnoalerttrans = (int) inputMap.get("emailalerttransaction");
			int nseqnoalerttranshistory = (int) inputMap.get("emailalerttranshistory");

			final String checkEmailUserQuery = "select u.semail, u.sphoneno from emailuserconfig euc "
											 + "join users u on euc.nusercode=u.nusercode "
											 + " where euc.nemailconfigcode=" + emailConfig.getNemailconfigcode() 
											 + " and euc.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
											 + " and u.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
											 + " and u.nsitecode = " + userinfo.getNmastersitecode() + " ";
			
			List<Map<String, Object>> listOfMap = jdbcTemplate.queryForList(checkEmailUserQuery);			
			
			if (!listOfMap.isEmpty()) {
				emailQuery = " select u.semail, u.sphoneno from emailuserconfig euc"
						   + " join users u on euc.nusercode = u.nusercode "
						   + " join userssite us on u.nusercode=us.nusercode "
						   + " where euc.nemailconfigcode = " + emailConfig.getNemailconfigcode() + " "
						   + " and euc.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
						   + " and u.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
						   + " and us.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
						   + " and u.nsitecode = " + userinfo.getNmastersitecode() + " "
						   + " and us.nsitecode = " + userinfo.getNtranssitecode() + " ";
				
				listOfMap = jdbcTemplate.queryForList(emailQuery);
				
				if (listOfMap.isEmpty()) {
					return null;
				}
				
			} else {
				emailQuery = " select u.semail, u.sphoneno from emailuserroleconfig eurc "
						   + " join usermultirole um on eurc.nuserrolecode = um.nuserrolecode "
						   + " join userssite us on um.nusersitecode = us.nusersitecode "
						   + " join users u on us.nusercode = u.nusercode"
						   + " where eurc.nemailconfigcode = " + emailConfig.getNemailconfigcode() + ""
						   + " and eurc.nstatus = "	+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
						   + " and um.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
						   + " and us.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
						   + " and u.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
						   + " and um.nsitecode = " + userinfo.getNmastersitecode() + " "
						   + " and us.nsitecode = " + userinfo.getNtranssitecode() 
						   + " and u.nsitecode = " + userinfo.getNmastersitecode() + " ";
				
				listOfMap = jdbcTemplate.queryForList(emailQuery);
			}
					
			if (listOfMap.isEmpty()) {				
				//Added by sonia on 11th sept 2025 for jira id:SWSM-12
				//start
				final String getUsersForCCEmail = "select u.semail as sccemail, u.sphoneno as sccphoneno "
											  + "from ccemailuserrole eur "
											  + "join usermultirole umr on umr.nuserrolecode =eur.nuserrolecode "
											  + "join userssite us on us.nusersitecode =umr.nusersitecode "
											  + "join users u on u.nusercode =us.nusercode "
											  + "where eur.nformcode="+userinfo.getNformcode()+" and eur.ncontrolcode="+ncontrolcode+" and us.nsitecode="+userinfo.getNtranssitecode()+" "
											  + "and eur.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
											  + "and umr.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
											  + "and us.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
											  + "and u.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" ";
				ccEmailList = jdbcTemplate.queryForList(getUsersForCCEmail);	
				if(ccEmailList !=null && ccEmailList.size()>0) { 
					allCCEmails = ccEmailList.stream().map(map -> (String) map.get("sccemail")).collect(Collectors.joining(","));	
					allCCPhone = ccEmailList.stream().map(map -> (String) map.get("sccphoneno")).collect(Collectors.joining(","));
				}
				//end
				
				final String lstPrimaryKey = String.join(",", lstInteger);
				objMap.put("userinfo", userinfo);
				objMap.put(emailConfig.getScolumnname(), lstPrimaryKey);
				final String ReplacedFinalQuery = projectDAOSupport.fnReplaceParameter(emailConfig.getSquery(),objMap);
				listOfMap = jdbcTemplate.queryForList(ReplacedFinalQuery);
				isEmailQueryUser = true;
			
				final ResourceBundle resourcebundle = commonFunction.getResourceBundle(userinfo.getSlanguagefilename(),false);

				listOfMap.stream().map(item -> {
					item.replace("sreason",
							resourcebundle.containsKey((String) item.get("sreason"))
							? resourcebundle.getString((String) item.get("sreason"))
									: (String) item.get("sreason"));
					return item;
				}).collect(Collectors.toList());				
			}
			if (listOfMap.size() > 0) {
				List<String> alertTransactionarray = new ArrayList<>();
				int j = 0;			
				
				StringBuilder alertTransaction = new StringBuilder("insert into emailalerttransaction(nrunningno,"
						+ " ntransactionno,nemailconfigcode,nisemail,dcreateddate,dmodifieddate,semail,sreason,nstatus,nsitecode,ssubject,stemplatebody,ssystemid,nusercode,ncontrolcode,sccemail,nemailscreenschedulercode) values");
				StringBuilder alertTransactionHistory = new StringBuilder(
						"insert into emailalerttranshistory (nemailhistorycode,nrunningno,ntransstatus,dtransdate,dmodifieddate,noffsetdtransdate,ntransdatetimezonecode,smailsentby,nmailitemid,nstatus,nsitecode) values");

				for (int k = 0; k < lstInteger.size(); k++) {
					for (int i = 0; i < listOfMap.size(); i++) {
							String reason = null;
							if (listOfMap.get(i).get("sreason") != null) {
								reason = "'"+ stringUtilityFunction.replaceQuote((String) listOfMap.get(i).get("sreason"))+ "'";
							}
							// Added nenableemail condition by Gowtham on 27th Sept 2025 for jira id:SWSM-64
							// start
							if(emailConfig.getNenableemail() == Enumeration.TransactionStatus.YES.gettransactionstatus() && listOfMap.get(i).get("semail") != null) {
							++nseqnoalerttrans;
							++nseqnoalerttranshistory;
							alertTransaction.append("( " + nseqnoalerttrans + "," + lstInteger.get(k) + ","
										+ emailConfig.getNemailconfigcode() + "," 
										+ Enumeration.TransactionStatus.YES.gettransactionstatus() + ",'"
										+ dateUtilityFunction.getCurrentDateTime(userinfo) + "','"
										+ dateUtilityFunction.getCurrentDateTime(userinfo) + "','"
										+ listOfMap.get(i).get("semail") + "'," + reason + ","
										+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ","
										+ userinfo.getNsitecode() + ", N'"
										+ stringUtilityFunction.replaceQuote(mailDetailList.get(k).get("subject")) + "', N'"
										+ stringUtilityFunction.replaceQuote(mailDetailList.get(k).get("body")) + "', " 
										+ "N'" + stringUtilityFunction.replaceQuote(ssystemid) + "', "
										+ userinfo.getNusercode() + ", " + ncontrolcode + ",'"+allCCEmails+"',"+Enumeration.TransactionStatus.NA.gettransactionstatus()+" ),");

							alertTransactionHistory.append(
									"( " + nseqnoalerttranshistory + "," + nseqnoalerttrans + ","
									+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + ",'"
									+ dateUtilityFunction.getCurrentDateTime(userinfo) + "','"
									+ dateUtilityFunction.getCurrentDateTime(userinfo) + "',"
									+ dateUtilityFunction.getCurrentDateTimeOffset(userinfo.getStimezoneid())
									+ "," + userinfo.getNtimezonecode() + ",'AUTO'," + " "
									+ Enumeration.TransactionStatus.NA.gettransactionstatus() + ","
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ", "
									+ userinfo.getNsitecode() + " ),");
							}
							if(emailConfig.getNenablesms() == Enumeration.TransactionStatus.YES.gettransactionstatus() && listOfMap.get(i).get("sphoneno") != null) {							
								++nseqnoalerttrans;
								++nseqnoalerttranshistory;
								alertTransaction.append("( " + nseqnoalerttrans + "," + lstInteger.get(k) + ","
										+ emailConfig.getNemailconfigcode() + "," 
										+ Enumeration.TransactionStatus.NO.gettransactionstatus() + ",'"
										+ dateUtilityFunction.getCurrentDateTime(userinfo) + "','"
										+ dateUtilityFunction.getCurrentDateTime(userinfo) + "','"
										+ listOfMap.get(i).get("sphoneno") + "'," + reason + ","
										+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ","
										+ userinfo.getNsitecode() + ", N'"
										+ stringUtilityFunction.replaceQuote(mailDetailList.get(k).get("subject")) + "', N'"
										+ stringUtilityFunction.replaceQuote(mailDetailList.get(k).get("body")) + "', "
										+ "N'" + stringUtilityFunction.replaceQuote(ssystemid) + "', "
										+ userinfo.getNusercode() + ", " + ncontrolcode + ",'"+allCCPhone+"',"+Enumeration.TransactionStatus.NA.gettransactionstatus()+" ),");

								alertTransactionHistory.append(
										"( " + nseqnoalerttranshistory + "," + nseqnoalerttrans + ","
										+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + ",'"
										+ dateUtilityFunction.getCurrentDateTime(userinfo) + "','"
										+ dateUtilityFunction.getCurrentDateTime(userinfo) + "',"
										+ dateUtilityFunction.getCurrentDateTimeOffset(userinfo.getStimezoneid())
										+ "," + userinfo.getNtimezonecode() + ",'AUTO'," + " "
										+ Enumeration.TransactionStatus.NA.gettransactionstatus() + ","
										+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ", "
										+ userinfo.getNsitecode() + " ),");
							}
							// end
							if (j == 899) {
								alertTransactionarray.add(alertTransaction.toString().substring(0, alertTransaction.length() - 1) + ";");
								alertTransactionarray.add(alertTransactionHistory.toString().substring(0,alertTransactionHistory.length() - 1) + ";");
								alertTransaction.delete(0, alertTransaction.length());
								alertTransactionHistory.delete(0, alertTransactionHistory.length());
								alertTransaction = new StringBuilder("insert into emailalerttransaction(nrunningno,"
										+ " ntransactionno,nemailconfigcode,dcreateddate,dmodifieddate,semail,sreason,nstatus,nsitecode,ssubject,stemplatebody,ssystemid,nusercode,ncontrolcode,sccemail,nemailscreenschedulercode) values");
								alertTransactionHistory = new StringBuilder("insert into emailalerttranshistory (nemailhistorycode,nrunningno,"
												+ " ntransstatus,dtransdate,dmodifieddate,noffsetdtransdate,ntransdatetimezonecode,"
												+ " smailsentby,nmailitemid,nstatus,nsitecode) values");
								j = 0;
							} else {
								j++;
							}
						}
					if (isEmailQueryUser == true)
						break;
				}

				if (j > 0 && j < 899) {
					alertTransactionarray.add(alertTransaction.toString().substring(0, alertTransaction.length() - 1) + ";");
					alertTransactionarray.add(alertTransactionHistory.toString().substring(0, alertTransactionHistory.length() - 1)
							+ ";");
				}

				projectDAOSupport.executeBulkDatainSingleInsert(alertTransactionarray.toArray(new String[0]));
				final String sUpdateQuery = "update seqnoemailmanagement set nsequenceno = " + (nseqnoalerttrans)	+ " where stablename = 'emailalerttransaction';"
						+ "update seqnoemailmanagement set nsequenceno = " + (nseqnoalerttranshistory)	+ " where stablename = 'emailalerttranshistory';";
				jdbcTemplate.execute(sUpdateQuery);

				return new ResponseEntity<>(HttpStatus.OK);
			} else {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_EMAILUSERUNAVAILABLE",userinfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}
		}
		return null;
	}

	public Map<String, String> getEmailDetails(int nemailconfigcode, String nprimarykey, Short nsitecode,
			String srecievermail, UserInfo userInfo, Map<String, Object> inputMap) throws Exception {


		final String configQry = " select ec.ncontrolcode,ec.nformcode, ec.nsitecode, euq.scolumnname, euq.stablename, eh.semail, eh.nemailhostcode, eh.shostname, "
							   + " eh.nportno, etm.stemplatebody, etm.ssubject, et.squery as stagquery "
							   + " from emailconfig ec "
							   + " join emailscreen es on es.nemailscreencode=ec.nemailscreencode "
							   + " join emailhost eh on eh.nemailhostcode=ec.nemailhostcode "
							   + " join emailtemplate etm on etm.nemailtemplatecode=ec.nemailtemplatecode "
							   + " join emailtag et on et.nemailtagcode=etm.nemailtagcode "
							   + " join emailuserquery euq on euq.nemailuserquerycode = ec.nemailuserquerycode"
							   + " where  ec.nemailconfigcode = " + nemailconfigcode + " "
							   + " and ec.nstatus = "  + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							   + " and es.nstatus="   + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							   + " and eh.nstatus="   + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							   + " and etm.nstatus="  + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							   + " and et.nstatus="   + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
							   + " and euq.nstatus="   + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
							   + " and ec.nsitecode = " + userInfo.getNmastersitecode() + " and es.nsitecode = " + userInfo.getNmastersitecode()
							   + " and eh.nsitecode = " + userInfo.getNmastersitecode() + " and etm.nsitecode = " + userInfo.getNmastersitecode();
		final EmailConfig objEmailConfig = (EmailConfig) jdbcUtilityFunction.queryForObject(configQry,
				EmailConfig.class, jdbcTemplate);

		if (objEmailConfig != null) {
			
			final HashMap<String, String> map = new HashMap<>();

			objEmailConfig.setStemplatebody(objEmailConfig.getStemplatebody().replaceAll("&lt;", "<").replaceAll("&gt;", ">"));
			// objEmailConfig.setStemplatebody(objEmailConfig.getStemplatebody().replaceAll("&gt;",">"));

			inputMap.put(objEmailConfig.getScolumnname(), nprimarykey);
			inputMap.put("nsitecode", (int) nsitecode);
			inputMap.put("userinfo", userInfo);

			final String ReplacedFinalQuery = projectDAOSupport.fnReplaceParameter(objEmailConfig.getStagquery(),
					inputMap);
			// ALPD-5651 Modified code by Vishakh for temperory fix for release when configured mail (06-04-2025)
			if (ReplacedFinalQuery == Enumeration.ReturnStatus.FAILED.getreturnstatus()) {
				Map<String, String> rtnMap = new HashMap<String, String>();
				rtnMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),	Enumeration.ReturnStatus.FAILED.getreturnstatus());
				return rtnMap;
			}
			Map<String, Object> tagargs = new HashMap<>();
			try {
				tagargs = jdbcTemplate.queryForMap(ReplacedFinalQuery);
			} catch (Exception e) {
				LOGGER.error(e.getMessage());
				tagargs = null;
			}
			if (tagargs != null) {
				final List<String> tagkeys = tagargs.keySet().stream().collect(Collectors.toList());
				for (int i = 0; i < tagkeys.size(); i++) {
					if (objEmailConfig.getSsubject().contains(tagkeys.get(i))) {
						objEmailConfig.setSsubject(objEmailConfig.getSsubject().replaceAll(tagkeys.get(i),
								tagargs.get(tagkeys.get(i)).toString()));
					}
					if (objEmailConfig.getStemplatebody().contains(tagkeys.get(i))) {
						//Added the pattern.quote & Matcher.quoteReplacement by sonia on 24th Oct 2025 for jira id:SWSM-89
						objEmailConfig.setStemplatebody(objEmailConfig.getStemplatebody().replaceAll(Pattern.quote(tagkeys.get(i)),
								Matcher.quoteReplacement(tagargs.get(tagkeys.get(i)).toString())));
					}
				}
			}

			map.put("receiverMailID", srecievermail);
			map.put("senderMailID", objEmailConfig.getSemail());

			final String password = projectDAOSupport.decryptPassword("emailhost", "nemailhostcode",objEmailConfig.getNemailhostcode(), "spassword");
			map.put("password", password);
			map.put("host", objEmailConfig.getShostname());
			map.put("port", String.valueOf(objEmailConfig.getNportno()));
			map.put("subject", objEmailConfig.getSsubject());
			map.put("body", objEmailConfig.getStemplatebody());
			map.put("nformcode", String.valueOf(objEmailConfig.getNformcode()));
			map.put("ncontrolcode", String.valueOf(objEmailConfig.getNcontrolcode()));

			return map;
		}

		return null;
	}

	public Map<String, String> sendMailCommonFunction() throws Exception {
		
		//Added by sonia on 6th sept 2025 for jira id:SWSM-12
		Date currentDateObj  = new Date();
		final SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy-MM-dd");
		String formattedCurrentDate = sdFormat.format(currentDateObj);
		
		// Added by Gowtham on 27th Sept 2025 for jira id:SWSM-64
		final String smsgetquery = "select ssid, sauthtoken, svirtualno from smssettings where ndefaultstatus="
				+ Enumeration.TransactionStatus.YES.gettransactionstatus() + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		
		final SmsSettings smsSettingsObj = (SmsSettings) jdbcUtilityFunction.queryForObject(smsgetquery, SmsSettings.class, jdbcTemplate);
		
		final String runningnoqry = "select eath.nrunningno, eat.nisemail from emailalerttranshistory eath,emailalerttransaction eat where eath.ntransstatus="
				+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + " and dtransdate::date  ='"+formattedCurrentDate+"' and eath.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and eath.nemailhistorycode in (select max(nemailhistorycode) from emailalerttranshistory eath,emailalerttransaction eat where eat.nrunningno = eath.nrunningno group by eat.nrunningno)"
				+ " and eat.nrunningno=eath.nrunningno";
		
		List<Integer> runningnolst = new ArrayList<>();
		List<Integer> nisemailLst = new ArrayList<>();	// Added by Gowtham on 27th Sept 2025 for jira id:SWSM-64
		
		try {
//			Added by Gowtham on 27th Sept 2025 for jira id:SWSM-64
//			runningnolst = jdbcTemplate.queryForList(runningnoqry, Integer.class);
			List<Map<String, Object>> results = jdbcTemplate.queryForList(runningnoqry);
			runningnolst = results.stream()
			        .map(row -> (Integer) row.get("nrunningno"))
			        .collect(Collectors.toList());
			nisemailLst = results.stream()
			        .map(row -> (Integer) row.get("nisemail"))
			        .collect(Collectors.toList());
		} catch (Exception e) {
			runningnolst = null;
			LOGGER.error(e.getMessage());
			LOGGER.error(e.getCause());
		}
		if (!runningnolst.isEmpty() && runningnolst != null) {
			for (int i = 0; i < runningnolst.size(); i++) {
				final String emailAlertQry = "select eat.nemailconfigcode, eat.ntransactionno, eat.semail ,eat.sccemail, eat.ssubject, eat.stemplatebody, eat.ssystemid, eat.ncontrolcode, eat.nsitecode from emailalerttransaction eat where nrunningno="
						+ runningnolst.get(i) + " and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				final EmailAlertTransaction emlaltobj = (EmailAlertTransaction) jdbcUtilityFunction
						.queryForObject(emailAlertQry, EmailAlertTransaction.class, jdbcTemplate);
				final String emailAlertHisQry = "select noffsetdtransdate, ntransdatetimezonecode from emailalerttranshistory where nrunningno="
						+ runningnolst.get(i) + " and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				final EmailAlertTransHistory emlalthisobj = (EmailAlertTransHistory) jdbcUtilityFunction
						.queryForObject(emailAlertHisQry, EmailAlertTransHistory.class, jdbcTemplate);

				if (emlaltobj != null) {
					Map<String, String> map = new HashMap<>();

					try {
						map = getEmailHostDetails(emlaltobj.getNemailconfigcode(), null);
						// map=getEmailDetails(emlaltobj.getNemailconfigcode(),emlaltobj.getNtransactionno(),emlaltobj.getNsitecode(),emlaltobj.getSemail(),
						// null);
					} catch (Exception e) {
						LOGGER.error(e.getMessage());
						LOGGER.error(e.getCause());
						map = null;
					}
					if (map != null) {
						// Added by Gowtham on 27nd sept 2025 for jira id:SWSM-64
						map.put("nisemail", nisemailLst.get(i).toString());
						map.put("ssid", smsSettingsObj.getSsid());
						map.put("sauthtoken", smsSettingsObj.getSauthtoken());
						map.put("svirtualno", smsSettingsObj.getSvirtualno());
						
						map.put("nrunningno", runningnolst.get(i).toString());
						map.put("receiverMailID", emlaltobj.getSemail());
						map.put("ccMailID", emlaltobj.getSccemail());

						map.put("subject", emlaltobj.getSsubject());
						map.put("body", emlaltobj.getStemplatebody());
						map.put("ncontrolcode", String.valueOf(emlaltobj.getNcontrolcode()));
						final String dynamicFileQuery = "select edf.stablename, edf.stableprimarycolumnname, edf.sprimarykeycolumnname, edf.sfilecolumnname, edf.isqueryneed, edf.squery from emaildynamicfile edf "
								+ " where ncontrolcode= " + map.get("ncontrolcode") + " and nstatus= "
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
						EmailDynamicFile emlDynFileObj = new EmailDynamicFile();
						try {
							emlDynFileObj = (EmailDynamicFile) jdbcUtilityFunction.queryForObject(dynamicFileQuery,
									EmailDynamicFile.class, jdbcTemplate);
						} catch (Exception e) {
							LOGGER.error(e.getMessage());
							LOGGER.error(e.getCause());
							emlDynFileObj = null;
						}
						if (emlDynFileObj != null) {
							String ssystemfilename = null;
							if (emlDynFileObj.getIsqueryneed() == Enumeration.TransactionStatus.YES
									.gettransactionstatus()) {
								try {
									Map<String, Object> inputMap = new HashMap<>();
									inputMap.put(emlDynFileObj.getSprimarykeycolumnname(),
											emlaltobj.getNtransactionno());
									inputMap.put("userinfo", null);
									inputMap.put("nsitecode", (int) emlaltobj.getNsitecode());
									String ReplacedFinalQuery = projectDAOSupport
											.fnReplaceParameter(emlDynFileObj.getSquery(), inputMap);
									ssystemfilename = jdbcTemplate.queryForObject(ReplacedFinalQuery, String.class);
								} catch (Exception e) {
									LOGGER.error(e.getMessage());
									LOGGER.error(e.getCause());
								}
							} else {
								final String reportpresentQuery = "select " + emlDynFileObj.getSfilecolumnname()
								+ " from " + emlDynFileObj.getStablename() + " where "
								+ emlDynFileObj.getStableprimarycolumnname() + " = ( select max( "
								+ emlDynFileObj.getStableprimarycolumnname() + " ) from "
								+ emlDynFileObj.getStablename() + " where "
								+ emlDynFileObj.getSprimarykeycolumnname() + " = "
								+ emlaltobj.getNtransactionno() + " and nsitecode=" + emlaltobj.getNsitecode()
								+ " ) and nsitecode=" + emlaltobj.getNsitecode() + ";";

								// select ssystemfilename from coareporthistory where
								// ncoareporthistorycode=(select max(ncoareporthistorycode) from
								// coareporthistory where ncoaparentcode=3 )
								try {
									ssystemfilename = jdbcTemplate.queryForObject(reportpresentQuery, String.class);
								} catch (Exception e) {
									LOGGER.error(e.getMessage());
									LOGGER.error(e.getCause());
									// map.put("attachmentFilePath", null);
									// map.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
									// Enumeration.ReturnStatus.FAILED.getreturnstatus()) ;
									// return map;
								}
							}
							if (ssystemfilename != null) {
								final String getReportPath = "select ssettingvalue from reportsettings where nreportsettingcode="
										+ Enumeration.ReportSettings.REPORT_PDF_PATH.getNreportsettingcode()
										+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
								String filePath = null;
								try {
									filePath = jdbcTemplate.queryForObject(getReportPath, String.class);
								} catch (Exception e) {
									LOGGER.error(e.getMessage());
									LOGGER.error(e.getCause());

								}

								final String filePath1 = filePath + ssystemfilename;
								File file = new File(filePath1);
								if (file.exists() && file.length() != 0) {
									filePath = filePath1;
								} else {
									UserInfo mailUserInfo = new UserInfo();
									mailUserInfo.setNformcode((short) 77);
									mailUserInfo.setNmastersitecode((short) -1);
									mailUserInfo.setSloginid("");
									final Map<String, Object> ftpObj = ftpUtilityFunction.multiFileDownloadFromFTP(
											Arrays.asList(ssystemfilename), -1, mailUserInfo, filePath,
											Arrays.asList(ssystemfilename));
									if (Enumeration.ReturnStatus.SUCCESS.getreturnstatus() == ftpObj
											.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus())) {
										file = new File(filePath1);
										if (file.exists() && file.length() != 0) {
											filePath = filePath1;
										} else {
											filePath = null;
										}
									} else {
										filePath = null;
									}

								}

								map.put("attachmentFilePath", filePath);
							} else {
								map.put("attachmentFilePath", null);
							}
						}
						sendMail(map);

					} else {

						final String isUTCqry = "select ssettingvalue from settings where ssettingname='Need UTC Conversation in Site screen = 3' and nstatus= "
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
						int isUTCenabled = 0;
						try {
							isUTCenabled = Integer.parseInt(jdbcTemplate.queryForObject(isUTCqry, String.class));
						} catch (Exception e) {
							LOGGER.error(e.getMessage());
							LOGGER.error(e.getCause());
						}
						Instant date = null;
						if (isUTCenabled == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
							date = Instant.now().truncatedTo(ChronoUnit.SECONDS);
						} else {
							date = LocalDateTime.now().toInstant(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS);
						}
						final String sQuery = " lock  table lockmail "
								+ Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
						jdbcTemplate.execute(sQuery);
						final String sSeqnoQuery = " select nsequenceno from  seqnoemailmanagement  "
								// + Enumeration.ReturnStatus.TABLOCK.getreturnstatus()
								+ " where stablename='emailalerttranshistory'";
						final Integer nseqnoalerttranshistory = (jdbcTemplate.queryForObject(sSeqnoQuery, Integer.class)
								+ 1);

						String query = " insert into emailalerttranshistory (nemailhistorycode,nrunningno,ntransstatus,"
								+ "dtransdate,noffsetdtransdate,ntransdatetimezonecode,smailsentby,nmailitemid,nstatus,dmodifieddate,nsitecode) values"
								+ "( " + nseqnoalerttranshistory + "," + runningnolst.get(i) + ","
								+ Enumeration.TransactionStatus.FAIL.gettransactionstatus() + ",'" + date + "',"
								+ emlalthisobj.getNoffsetdtransdate() + "," + emlalthisobj.getNtransdatetimezonecode()
								+ ",'AUTO'," + " " + Enumeration.TransactionStatus.NA.gettransactionstatus() + ","
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ",'" + date + "', "
								+ emlaltobj.getNsitecode() + " );";

						jdbcTemplate.execute(query);
						query = "update seqnoemailmanagement set nsequenceno = " + nseqnoalerttranshistory
								+ " where stablename = 'emailalerttranshistory';";

						jdbcTemplate.execute(query);

					}
				}
			}
		}
		return null;

	}

	public void sendMail(Map<String, String> inputMap) throws Exception {
		System.out.println("Preparing mail from java....");

		final String senderMail = inputMap.get("senderMailID");
		final String receiverMail = inputMap.get("receiverMailID");
		final String ccMail = inputMap.get("ccMailID");


		final String password = inputMap.get("password");
		/*Primary key duplication issue in the Email Alert Transaction History table, so uncommented the code.
		Uncommented by sonia on 27th sept 2025 for jira id:SWSM-12*/
		final String lockQuery = " lock  table lockmail " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
		jdbcTemplate.execute(lockQuery);
		final String sSeqnoQuery = " select nsequenceno from  seqnoemailmanagement  "
				+ " where stablename='emailalerttranshistory'";
		final SeqNoEmailManagement seqnoobj = ((SeqNoEmailManagement) jdbcUtilityFunction.queryForObject(sSeqnoQuery,
				SeqNoEmailManagement.class, jdbcTemplate));
		final int nseqnoalerttranshistory = seqnoobj.getNsequenceno() + 1;
		final String isUTCqry = "select ssettingvalue from settings where ssettingname='Need UTC Conversation in Site screen = 3' and nstatus= "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		int isUTCenabled = 0;
		try {
			isUTCenabled = Integer.parseInt(jdbcTemplate.queryForObject(isUTCqry, String.class));
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
			LOGGER.error(e.getCause());
		}

		Instant date = null;
		if (isUTCenabled == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
			date = Instant.now().truncatedTo(ChronoUnit.SECONDS);
		} else {
			date = LocalDateTime.now().toInstant(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS);
		}
		final String emailalthisqry = "select noffsetdtransdate, ntransdatetimezonecode, nsitecode from emailalerttranshistory where nrunningno="
				+ Integer.parseInt(inputMap.get("nrunningno")) + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		final EmailAlertTransHistory emlalthisobj = (EmailAlertTransHistory) jdbcUtilityFunction
				.queryForObject(emailalthisqry, EmailAlertTransHistory.class, jdbcTemplate);

		try {
			// Added by Gowtham on 27th Sept 2025 for jira id:SWSM-64
			// start
			if(Integer.parseInt(inputMap.get("nisemail")) == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
				Properties properties = new Properties();
				properties.put("mail.smtp.auth", "true");
				properties.put("mail.smtp.starttls.enable", "true");
				properties.put("mail.smtp.host", inputMap.get("host"));
				properties.put("mail.smtp.port", inputMap.get("port"));
				
				// ALPD-5580 Added by Abdul on 18-03-2025 for 2 step verification as it needs
				// TLS1.2 for SMTP Authentication
				properties.put("mail.smtp.starttls.required", "true");
				properties.put("mail.smtp.ssl.protocols", "TLSv1.2");
				
				Session session = Session.getInstance(properties, new Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(senderMail, password);
			}
		});
		// session.setDebug(true);
		Message message = prepareMessage(session, senderMail, receiverMail,ccMail, inputMap.get("subject"),
				inputMap.get("body"), inputMap.get("attachmentFilePath"));
			// InternetAddress internetAddress = new
			// InternetAddress(inputMap.get("receiverMailID"));
			// internetAddress.validate();
			Transport.send(message);
			System.out.println("Mail sent Successfully....");
			} else {
				final String ACCOUNT_SID = inputMap.get("ssid");
				final String AUTH_TOKEN = inputMap.get("sauthtoken");
				Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
				List<String> recipients = new ArrayList<>();
				recipients.add(receiverMail);
				recipients.addAll(Arrays.asList(ccMail.split(",")));
				recipients = recipients.stream().distinct().collect(Collectors.toList());
				String extractedContent = inputMap.get("body").replaceAll("<[^>]+>", "").trim();
				for(String userNo : recipients) {
					try {
						System.out.println("Preparing SMS from java....");
						Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
//						com.twilio.rest.api.v2010.account.Message sms =
							com.twilio.rest.api.v2010.account.Message.creator(
								new com.twilio.type.PhoneNumber("+91" + userNo),
								new com.twilio.type.PhoneNumber(inputMap.get("svirtualno")),
								extractedContent
							).create();
						System.out.println("SMS sent Successfully....");		        		
					} catch (Exception e) {
						LOGGER.error(e.getMessage());
						LOGGER.error(e.getCause());
					}
				}
			}
			// end
			
			String query = " insert into emailalerttranshistory (nemailhistorycode,nrunningno,ntransstatus,"
					+ "dtransdate,noffsetdtransdate,ntransdatetimezonecode,smailsentby,nmailitemid,nstatus,dmodifieddate,nsitecode) values"
					+ "( " + nseqnoalerttranshistory + "," + Integer.parseInt(inputMap.get("nrunningno")) + ","
					+ Enumeration.TransactionStatus.SENT.gettransactionstatus() + ",'" + date + "',"
					+ emlalthisobj.getNoffsetdtransdate() + "," + emlalthisobj.getNtransdatetimezonecode() + ",'AUTO',"
					+ " " + Enumeration.TransactionStatus.NA.gettransactionstatus() + ","
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ",'" + date + "', "
					+ emlalthisobj.getNsitecode() + " );";

			jdbcTemplate.execute(query);
			query = "update seqnoemailmanagement set nsequenceno = " + nseqnoalerttranshistory
					+ " where stablename = 'emailalerttranshistory';";

			jdbcTemplate.execute(query);

		} catch (Exception e) {
			LOGGER.error(e.getMessage());
			LOGGER.error(e.getCause());
			String query = " insert into emailalerttranshistory (nemailhistorycode,nrunningno,ntransstatus,"
					+ "dtransdate,noffsetdtransdate,ntransdatetimezonecode,smailsentby,nmailitemid,nstatus,dmodifieddate,nsitecode) values"
					+ "( " + nseqnoalerttranshistory + "," + Integer.parseInt(inputMap.get("nrunningno")) + ","
					+ Enumeration.TransactionStatus.FAIL.gettransactionstatus() + ",'" + date + "',"
					+ emlalthisobj.getNoffsetdtransdate() + "," + emlalthisobj.getNtransdatetimezonecode() + ",'AUTO',"
					+ " " + Enumeration.TransactionStatus.NA.gettransactionstatus() + ","
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ",'" + date + "', "
					+ emlalthisobj.getNsitecode() + " );";

			jdbcTemplate.execute(query);
			query = "update seqnoemailmanagement set nsequenceno = " + nseqnoalerttranshistory
					+ " where stablename = 'emailalerttranshistory';";

			jdbcTemplate.execute(query);

		}

	}

	public Map<String, String> getEmailHostDetails(int nemailconfigcode, UserInfo userinfo) throws Exception {
		final HashMap<String, String> map = new HashMap<>();
		final String hostQry = "select eh.semail, eh.nemailhostcode, eh.shostname, eh.nportno from  emailhost eh, emailconfig ec where "
				+ "eh.nemailhostcode = ec.nemailhostcode and ec.nemailconfigcode =  " + nemailconfigcode
				+ " and ec.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and eh.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

		final EmailHost objEmailHost = (EmailHost) jdbcUtilityFunction.queryForObject(hostQry, EmailHost.class,jdbcTemplate);
		map.put("senderMailID", objEmailHost.getSemail());
		final String password = passwordUtilityFunction.decryptPassword("emailhost", "nemailhostcode",objEmailHost.getNemailhostcode(), "spassword");
		map.put("password", password);
		map.put("host", objEmailHost.getShostname());
		map.put("port", String.valueOf(objEmailHost.getNportno()));

		return map;

	}

	private static Message prepareMessage(Session session, String senderMail, String sreceipientname,String ccMail,
			String subject, String body, String filePath) throws AddressException, MessagingException, IOException {
		MimeMessage mimemessage = new MimeMessage(session);
		mimemessage.setFrom(new InternetAddress(senderMail));
		mimemessage.setRecipients(Message.RecipientType.TO, InternetAddress.parse(sreceipientname));
		//Added by sonia on 11th sept 2025 for jira id:SWSM-12
		//start
		 if (ccMail != null && !ccMail.trim().isEmpty()) {
				mimemessage.setRecipients(Message.RecipientType.CC, InternetAddress.parse(ccMail));
         }
		 //end
		mimemessage.setSubject(subject);

		BodyPart messageBodyPart1 = new MimeBodyPart();
		messageBodyPart1.setContent(body, "text/html");

		Multipart multipart = new MimeMultipart();
		multipart.addBodyPart(messageBodyPart1);

		if (filePath != null && !filePath.isEmpty()) {
			MimeBodyPart messageBodyPart2 = new MimeBodyPart();
			messageBodyPart2.attachFile(filePath);
			multipart.addBodyPart(messageBodyPart2);
		}
		mimemessage.setContent(multipart);
		return mimemessage;
	}
	
	//Added by sonia on 18th Nov 2025 for jira id:BGSI-234
	@SuppressWarnings("unchecked")
	public ResponseEntity<Object> createScheduleEmailAlertTransaction(Map<String, Object> inputMap, UserInfo userinfo)throws Exception {

		final Map<String, Object> objMap = inputMap;
		String ssystemid = null;
		int formCode =-1;
		int ncontrolCode =-1;
		int nemailScreenSchedulerCode =-1;		
		
		
		
		if (inputMap.containsKey("nemailscreenschedulercode")) {	
			nemailScreenSchedulerCode = (int) inputMap.get("nemailscreenschedulercode");
		}
		
		if (inputMap.containsKey("nformcode")) {	
			formCode = (int) inputMap.get("nformcode");
		}
		
		if (inputMap.containsKey("ssystemid")) {
			ssystemid = inputMap.get("ssystemid").toString();
		}
		
		final String checkEmailEnabled = " select ec.nemailconfigcode, ec.nemailuserquerycode, ec.nenableemail, ec.nenablesms,euq.scolumnname,euq.stablename,"
									   + " ec.nemailtypecode,euq.nqueryneed,euq.squery "
									   + " from emailconfig ec "
									   + " join emailscreen es on es.nemailscreencode=ec.nemailscreencode "
									   + " join emailuserquery euq on euq.nemailuserquerycode=ec.nemailuserquerycode "
									   + " where ec.nformcode = "+ formCode + " and ec.nemailscreenschedulercode= "+nemailScreenSchedulerCode+" "
									   + " and ec.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  "
									   + " and es.nstatus= "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
									   + " and euq.nstatus= "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" ";

		final EmailConfig emailConfig = (EmailConfig) jdbcUtilityFunction.queryForObject(checkEmailEnabled,EmailConfig.class, jdbcTemplate);
		
		if (emailConfig != null && (emailConfig.getNenableemail() == Enumeration.TransactionStatus.YES.gettransactionstatus() || emailConfig.getNenablesms() == Enumeration.TransactionStatus.YES.gettransactionstatus())) {
			String columnName =emailConfig.getScolumnname();			
			final String sQuery = " lock  table lockmail " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
			jdbcTemplate.execute(sQuery);
			
			String emailQuery = "";
			boolean isEmailQueryUser = false;
			Instant finalReminderTime;	
			String allCCEmails = "";	
			String allCCPhone ="";
			List<String> lstInteger = new ArrayList<>();
			List<Map<String, String>> mailDetailList = new ArrayList<>();	
			Map<String, String> strFailedStatus = new HashMap<String, String>();
			List<Map<String, Object>> ccEmailList = null;
			
			if ((inputMap.get(columnName)) instanceof List) {
				lstInteger = (List<String>) inputMap.get(columnName);
			} else if (inputMap.get(columnName) instanceof Integer) {
				lstInteger.add(String.valueOf( inputMap.get(columnName)));
			}

			final String emailAlertQuery ="select * from emailalerttransaction where ntransactionno ="+inputMap.get(columnName)+" and nemailconfigcode= "+emailConfig.getNemailconfigcode()+" and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" ";
			final EmailAlertTransaction emailAlert = (EmailAlertTransaction) jdbcUtilityFunction.queryForObject(emailAlertQuery,EmailAlertTransaction.class, jdbcTemplate);
			if(emailAlert == null) {
				lstInteger.stream().forEach(primarykey -> {
					try {
						Map<String, String> mailDetails = new HashMap<String, String>();
						mailDetails = getEmailDetails(emailConfig.getNemailconfigcode(), primarykey,(short) -1, null, userinfo, objMap);

						if (mailDetails.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus()) == Enumeration.ReturnStatus.FAILED.getreturnstatus()) {
							strFailedStatus.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),Enumeration.ReturnStatus.FAILED.getreturnstatus());
						}
						
						mailDetailList.add(mailDetails);
					} catch (Exception e) {
						e.printStackTrace();
					}
				});
				if (strFailedStatus.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus()) == Enumeration.ReturnStatus.FAILED.getreturnstatus()) {
					return null;
				}
				final String sSeqnoQuery = " select stablename,nsequenceno from  seqnoemailmanagement "	+ " where stablename in('emailalerttransaction','emailalerttranshistory') and " 
										 + " nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

				final List<SeqNoEmailManagement> lstSeqNo = jdbcTemplate.query(sSeqnoQuery, new SeqNoEmailManagement());
				inputMap = lstSeqNo.stream().collect(Collectors.toMap(SeqNoEmailManagement::getStablename,SeqNoEmailManagement -> SeqNoEmailManagement.getNsequenceno()));

				int nseqnoalerttrans = (int) inputMap.get("emailalerttransaction");
				int nseqnoalerttranshistory = (int) inputMap.get("emailalerttranshistory");

				final String checkEmailUserQuery = "select u.semail, u.sphoneno from emailuserconfig euc "
						 						 + "join users u on euc.nusercode=u.nusercode "
						 						 + " where euc.nemailconfigcode=" + emailConfig.getNemailconfigcode() 
						 						 + " and euc.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
						 						 + " and u.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
						 						 + " and u.nsitecode = " + userinfo.getNmastersitecode() + " ";

				List<Map<String, Object>> listOfMap = jdbcTemplate.queryForList(checkEmailUserQuery);	
				
				if (!listOfMap.isEmpty()) {
					emailQuery = " select u.semail, u.sphoneno from emailuserconfig euc"
							   + " join users u on euc.nusercode = u.nusercode "
							   + " join userssite us on u.nusercode=us.nusercode "
							   + " where euc.nemailconfigcode = " + emailConfig.getNemailconfigcode() + " "
							   + " and euc.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
							   + " and u.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
							   + " and us.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
							   + " and u.nsitecode = " + userinfo.getNmastersitecode() + " "
							   + " and us.nsitecode = " + userinfo.getNtranssitecode() + " ";
					
					listOfMap = jdbcTemplate.queryForList(emailQuery);
					
					if (listOfMap.isEmpty()) {
						return null;
					}
				} else {
					emailQuery = " select u.semail, u.sphoneno from emailuserroleconfig eurc "
							   + " join usermultirole um on eurc.nuserrolecode = um.nuserrolecode "
							   + " join userssite us on um.nusersitecode = us.nusersitecode "
							   + " join users u on us.nusercode = u.nusercode"
							   + " where eurc.nemailconfigcode = " + emailConfig.getNemailconfigcode() + ""
							   + " and eurc.nstatus = "	+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
							   + " and um.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
							   + " and us.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
							   + " and u.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
							   + " and um.nsitecode = " + userinfo.getNmastersitecode() + " "
							   + " and us.nsitecode = " + userinfo.getNtranssitecode() 
							   + " and u.nsitecode = " + userinfo.getNmastersitecode() + " ";					
					listOfMap = jdbcTemplate.queryForList(emailQuery);
				}
				if (listOfMap.isEmpty()) {	
					
					final String getUsersForCCEmail = "select u.semail as sccemail, u.sphoneno as sccphoneno "
						  							+ "from ccemailuserrole eur "
						  							+ "join usermultirole umr on umr.nuserrolecode =eur.nuserrolecode "
						  							+ "join userssite us on us.nusersitecode =umr.nusersitecode "
						  							+ "join users u on u.nusercode =us.nusercode "
						  							+ "where eur.nformcode="+formCode+" and eur.ncontrolcode="+ncontrolCode+" "
						  							+ "and eur.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
						  							+ "and umr.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
						  							+ "and us.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
						  							+ "and u.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" ";
					ccEmailList = jdbcTemplate.queryForList(getUsersForCCEmail);	
					
					if(ccEmailList !=null && ccEmailList.size()>0) { 							
						allCCEmails = ccEmailList.stream().map(map -> (String) map.get("sccemail")).collect(Collectors.joining(","));	
						allCCPhone = ccEmailList.stream().map(map -> (String) map.get("sccphoneno")).collect(Collectors.joining(","));							
					}	
					
					
					final String lstPrimaryKey = String.join(",", lstInteger);
					objMap.put("userinfo", userinfo);
					objMap.put(emailConfig.getScolumnname(), lstPrimaryKey);

					final String ReplacedFinalQuery = projectDAOSupport.fnReplaceParameter(emailConfig.getSquery(),objMap);
					listOfMap = jdbcTemplate.queryForList(ReplacedFinalQuery);
					isEmailQueryUser = true;				

					final ResourceBundle resourcebundle = commonFunction.getResourceBundle(userinfo.getSlanguagefilename(),	false);

					listOfMap.stream().map(item -> {
						item.replace("sreason",
								resourcebundle.containsKey((String) item.get("sreason"))
								? resourcebundle.getString((String) item.get("sreason"))
										: (String) item.get("sreason"));
						return item;
					}).collect(Collectors.toList());						
				}				
				if(objMap.containsKey("remainderDate")) {
					String reminderDateStr = (String) objMap.get("remainderDate");
					DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
					LocalDateTime reminderDateTime = LocalDateTime.parse(reminderDateStr, formatter);
					// Convert LocalDateTime -> Instant (using system default zone)
					Instant reminderTime = reminderDateTime.atZone(ZoneId.systemDefault()).toInstant();
					Instant now = Instant.now();
					Instant nowTruncatedToSeconds = now.truncatedTo(ChronoUnit.SECONDS);					
					if (reminderTime.compareTo(nowTruncatedToSeconds) <= 0) {
					    finalReminderTime = nowTruncatedToSeconds;
					} else {
					    finalReminderTime = reminderTime;
					}
				}else {
					finalReminderTime= dateUtilityFunction.getCurrentDateTime(userinfo);
				}					

				if (listOfMap.size() > 0) {
					List<String> alertTransactionarray = new ArrayList<>();					
					int j = 0;
					StringBuilder alertTransaction = new StringBuilder("insert into emailalerttransaction(nrunningno,"
							+ " ntransactionno,nemailconfigcode,nisemail,dcreateddate,dmodifieddate,semail,sreason,nstatus,nsitecode,ssubject,stemplatebody,ssystemid,nusercode,ncontrolcode,sccemail,nemailscreenschedulercode) values");
					StringBuilder alertTransactionHistory = new StringBuilder(
							"insert into emailalerttranshistory (nemailhistorycode,nrunningno,ntransstatus,dtransdate,dmodifieddate,noffsetdtransdate,ntransdatetimezonecode,smailsentby,nmailitemid,nstatus,nsitecode) values");

					for (int k = 0; k < lstInteger.size(); k++) {
						for (int i = 0; i < listOfMap.size(); i++) {
								String reason = null;
								if (listOfMap.get(i).get("sreason") != null) {
									reason = "'"+ stringUtilityFunction.replaceQuote((String) listOfMap.get(i).get("sreason"))+ "'";
								}							
								if(emailConfig.getNenableemail() == Enumeration.TransactionStatus.YES.gettransactionstatus() && listOfMap.get(i).get("semail") != null) {
									/*String alertTransQuery="select * from emailalerttransaction where ntransactionno= "+lstInteger.get(k)+" "
														 + "and nemailscreenschedulercode="+nemailScreenSchedulerCode+" and  nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" ";
									List<EmailAlertTransaction> objEmailAlertTransaction= (List<EmailAlertTransaction>) jdbcTemplate.query(alertTransQuery, new EmailAlertTransaction());;
									if(objEmailAlertTransaction.size()>0) {
										
										String deleteQuery="update emailalerttranshistory set nstatus="+Enumeration.TransactionStatus.DELETED.gettransactionstatus()+" where nrunningno =any( "
														+ "	select nrunningno from emailalerttransaction where ntransactionno="+lstInteger.get(k)+" "
														+ " and nemailscreenschedulercode="+nemailScreenSchedulerCode+" and  nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" );";
										
										 deleteQuery=deleteQuery+"update emailalerttransaction set nstatus="+Enumeration.TransactionStatus.DELETED.gettransactionstatus()+" where ntransactionno= "+lstInteger.get(k)+" and nemailscreenschedulercode="+nemailScreenSchedulerCode+"; " ;
										
										jdbcTemplate.execute(deleteQuery);
									}*/														
									++nseqnoalerttrans;
									++nseqnoalerttranshistory;
									alertTransaction.append("( " + nseqnoalerttrans + "," + lstInteger.get(k) + ","
											+ emailConfig.getNemailconfigcode() + "," 
											+ Enumeration.TransactionStatus.YES.gettransactionstatus() + ",'"
											+ dateUtilityFunction.getCurrentDateTime(userinfo) + "','"
											+ dateUtilityFunction.getCurrentDateTime(userinfo) + "','"
											+ listOfMap.get(i).get("semail") + "'," + reason + ","
											+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ","
											+ userinfo.getNsitecode() + ", N'"
											+ stringUtilityFunction.replaceQuote(mailDetailList.get(k).get("subject")) + "', N'"
											+ stringUtilityFunction.replaceQuote(mailDetailList.get(k).get("body")) + "', "
											+ "N'" + stringUtilityFunction.replaceQuote(ssystemid) + "', "
											+ userinfo.getNusercode() + ", " + ncontrolCode + ",'"+allCCEmails+"',"+nemailScreenSchedulerCode+" ),");

									alertTransactionHistory.append("( " + nseqnoalerttranshistory + "," + nseqnoalerttrans + ","
											+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + ",'"
											+ finalReminderTime + "','"
											+ dateUtilityFunction.getCurrentDateTime(userinfo) + "',"
											+ dateUtilityFunction.getCurrentDateTimeOffset(userinfo.getStimezoneid())
											+ "," + userinfo.getNtimezonecode() + ",'AUTO'," + " "
											+ Enumeration.TransactionStatus.NA.gettransactionstatus() + ","
											+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ", "
											+ userinfo.getNsitecode() + " ),");
								}
								if(emailConfig.getNenablesms() == Enumeration.TransactionStatus.YES.gettransactionstatus() && listOfMap.get(i).get("sphoneno") != null) {
									++nseqnoalerttrans;
									++nseqnoalerttranshistory;
									alertTransaction.append("( " + nseqnoalerttrans + "," + lstInteger.get(k) + ","
											+ emailConfig.getNemailconfigcode() + "," 
											+ Enumeration.TransactionStatus.NO.gettransactionstatus() + ",'"
											+ dateUtilityFunction.getCurrentDateTime(userinfo) + "','"
											+ dateUtilityFunction.getCurrentDateTime(userinfo) + "','"
											+ listOfMap.get(i).get("sphoneno") + "'," + reason + ","
											+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ","
											+ userinfo.getNsitecode() + ", N'"
											+ stringUtilityFunction.replaceQuote(mailDetailList.get(k).get("subject")) + "', N'"
											+ stringUtilityFunction.replaceQuote(mailDetailList.get(k).get("body")) + "', "
											+ "N'" + stringUtilityFunction.replaceQuote(ssystemid) + "', "
											+ userinfo.getNusercode() + ", " + ncontrolCode + ",'"+allCCPhone+"',"+nemailScreenSchedulerCode+" ),");

									alertTransactionHistory.append("( " + nseqnoalerttranshistory + "," + nseqnoalerttrans + ","
											+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + ",'"
											+ finalReminderTime + "','"
											+ dateUtilityFunction.getCurrentDateTime(userinfo) + "',"
											+ dateUtilityFunction.getCurrentDateTimeOffset(userinfo.getStimezoneid())
											+ "," + userinfo.getNtimezonecode() + ",'AUTO'," + " "
											+ Enumeration.TransactionStatus.NA.gettransactionstatus() + ","
											+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ", "
											+ userinfo.getNsitecode() + " ),");
								}
								if (j == 899) {
									alertTransactionarray.add(alertTransaction.toString().substring(0, alertTransaction.length() - 1) + ";");
									alertTransactionarray.add(alertTransactionHistory.toString().substring(0,alertTransactionHistory.length() - 1) + ";");

									alertTransaction.delete(0, alertTransaction.length());
									alertTransactionHistory.delete(0, alertTransactionHistory.length());
									alertTransaction = new StringBuilder("insert into emailalerttransaction(nrunningno,"
											+ " ntransactionno,nemailconfigcode,dcreateddate,dmodifieddate,semail,sreason,nstatus,nsitecode,ssubject,stemplatebody,ssystemid,nusercode,ncontrolcode,sccemail,nemailscreenschedulercode) values");
									alertTransactionHistory = new StringBuilder("insert into emailalerttranshistory (nemailhistorycode,nrunningno,"
													+ " ntransstatus,dtransdate,dmodifieddate,noffsetdtransdate,ntransdatetimezonecode,"
													+ " smailsentby,nmailitemid,nstatus,nsitecode) values");
									j = 0;
								} else {
									j++;
								}
							}
						if (isEmailQueryUser == true)
							break;
					}
					if (j > 0 && j < 899) {
						alertTransactionarray.add(alertTransaction.toString().substring(0, alertTransaction.length() - 1) + ";");
						alertTransactionarray.add(alertTransactionHistory.toString().substring(0, alertTransactionHistory.length() - 1)+ ";");
					}
					projectDAOSupport.executeBulkDatainSingleInsert(alertTransactionarray.toArray(new String[0]));
					final String sUpdateQuery = "update seqnoemailmanagement set nsequenceno = " + (nseqnoalerttrans)	+ " where stablename = 'emailalerttransaction';"
										      + "update seqnoemailmanagement set nsequenceno = " + (nseqnoalerttranshistory)+ " where stablename = 'emailalerttranshistory';";
					jdbcTemplate.execute(sUpdateQuery);
					return new ResponseEntity<>(HttpStatus.OK);
				} else {
					return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_EMAILUSERUNAVAILABLE",userinfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
				}
			}
		}
		return null;
	}

}
