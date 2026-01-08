package com.agaramtech.qualis.invoice.service.invoicecustomermaster;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import com.agaramtech.qualis.global.AuditUtilityFunction;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.FTPUtilityFunction;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.invoice.model.CustomerFile;
import com.agaramtech.qualis.invoice.model.InvoiceCustomerMaster;
import com.agaramtech.qualis.invoice.model.UserRoleFieldControl;
import com.agaramtech.qualis.invoice.model.UsersRoleField;
import com.agaramtech.qualis.credential.model.ControlMaster;
import com.agaramtech.qualis.credential.model.UserRoleScreenControl;
import com.agaramtech.qualis.global.LinkMaster;
import com.agaramtech.qualis.global.ProjectDAOSupport;
import com.agaramtech.qualis.global.StringUtilityFunction;
import com.agaramtech.qualis.global.ValidatorDel;
import com.agaramtech.qualis.invoice.model.FieldMaster;
import com.agaramtech.qualis.invoice.model.InvoiceCustomerType;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.AllArgsConstructor;

/**
 * This class is used to perform CRUD Operation on "Invoice Customer Master" table by
 * implementing methods from its interface.
 */

@AllArgsConstructor
@Repository
public class InvoiceCustomerMasterDAOImpl implements InvoiceCustomerMasterDAO {
	
	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private final FTPUtilityFunction ftpUtilityFunction;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final ProjectDAOSupport projectDAOSupport;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;
	
	/**
	 * This method is used to fetch the active Invoicecustomermaster objects for the
	 * specified Invoicecustomermaster name and site.
	 * 
	 * @param ntypecode       [String] name of the Invoicecustomermaster
	 * @param nmasterSiteCode [int] site code of the Invoicecustomermaster
	 * @return list of active Invoicecustomermaster code(s) based on the specified
	 *         Invoicecustomermaster name and site
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> getInvoiceCustomermaster(final UserInfo userInfo) throws Exception {
		
		
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		final List<InvoiceCustomerType> CustomerTypeList =(List <InvoiceCustomerType>)getCustomerType(userInfo).getBody();
		outputMap.put("customerTypeName", CustomerTypeList);
		final List<InvoiceCustomerMaster> CusmtomerMasterlist = (List<InvoiceCustomerMaster>) getCustomermaster(userInfo).getBody();	
		
		outputMap.put("cusmtomermasterlist", CusmtomerMasterlist);
		if(CusmtomerMasterlist.isEmpty()) {
			outputMap.put("selectedCustomer", null);
			
		}
		else {	
			final int ncustomercode = CusmtomerMasterlist.get(0).getNcustomercode();
			outputMap.put("selectedCustomer", CusmtomerMasterlist.get(0));
			outputMap.putAll((Map<String, Object>) getCustomerFile(ncustomercode, userInfo).getBody());
			}
	
		if(CusmtomerMasterlist.isEmpty()) {
			outputMap.put("selectedCustomer", null);
			
		}
		else {	
			final int ncustomercode = CusmtomerMasterlist.get(0).getNcustomercode();
			outputMap.put("selectedCustomer", CusmtomerMasterlist.get(0));
			outputMap.putAll((Map<String, Object>) getCustomerFile(ncustomercode, userInfo).getBody());
			}
		
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}
	
	/**
	 * This method is used to fetch the active Invoicecustomermaster objects for the
	 * specified Invoicecustomermaster name and site.
	 * 
	 * @param ntypecode       [String] name of the Invoicecustomermaster
	 * @param nmasterSiteCode [int] site code of the Invoicecustomermaster
	 * @return list of active Invoicecustomermaster code(s) based on the specified
	 *         Invoicecustomermaster name and site
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	@Override

	public ResponseEntity<Object> getInvoiceCustomerMaster(final UserInfo userInfo) throws Exception {
		
		
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();		

			final List<InvoiceCustomerType> CustomerTypeList =(List <InvoiceCustomerType>)getCustomerType(userInfo).getBody();
			outputMap.put("customerTypeName", CustomerTypeList);
			final List<InvoiceCustomerMaster> CusmtomerMasterlist = (List<InvoiceCustomerMaster>) getCustomermaster(userInfo).getBody();
			
			final List<InvoiceCustomerMaster> CusmtomerMasterList = CusmtomerMasterlist.stream()
		                .sorted(Comparator.comparing(InvoiceCustomerMaster::getScustomername))
		                .collect(Collectors.toList());

			
			outputMap.put("cusmtomermasterlist", CusmtomerMasterlist);
			if(CusmtomerMasterList.isEmpty()) {
				outputMap.put("selectedCustomer", null);
				
			}
			else {	
				final int ncustomercode = CusmtomerMasterlist.get(0).getNcustomercode();
				outputMap.put("selectedCustomer", CusmtomerMasterlist);
				outputMap.put("selectedCustomer", CusmtomerMasterlist.get(0));
				outputMap.put("selectedCustomer", CusmtomerMasterlist.get(0));
				outputMap.putAll((Map<String, Object>) getCustomerFile(ncustomercode, userInfo).getBody());
				}
		
		 int Customerpoc=0;
		 int Customershipping=0;
		 int otherdetails=0;
		 int customerreference=0;
		 int customerreference2=0;
		 int projectreference=0;
		 int projectreference2=0;
		 int tinno=0;
		 int accountdetails=0;

		outputMap.put("cusmtomermasterlist", CusmtomerMasterlist);
		
		final String st = "select * from userrolefieldcontrol where nformcode="+userInfo.getNformcode()+" and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and nneedrights=3 ";
		final List<UserRoleFieldControl> listTest = (List<UserRoleFieldControl>) jdbcTemplate.query(st,new UserRoleFieldControl());
		final String sntestgrouptestcode = stringUtilityFunction.fnDynamicListToString(listTest, "getNfieldcode");
		
		if(listTest.size()==0) {
			final Map<String, Integer> customerDetails = new HashMap<>();
			final int defaultValue = +Enumeration.TransactionStatus.NO.gettransactionstatus();
			    customerDetails.put("Customerpoc", defaultValue);
			    customerDetails.put("Customershipping", defaultValue);
			    customerDetails.put("otherdetails", defaultValue);
			    customerDetails.put("customerreference1", defaultValue);
			    customerDetails.put("customerreference2", defaultValue);
			    customerDetails.put("projectreference1", defaultValue);
			    customerDetails.put("projectreference2", defaultValue);
			    customerDetails.put("tinno", defaultValue);
			    customerDetails.put("accountdetails", defaultValue);
		}
		else {
			final String std="select * from fieldmaster where nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and "
				 +"nfieldcode in (" + sntestgrouptestcode	+ ")";
	
			final List<FieldMaster> listTestfield = (List<FieldMaster>) jdbcTemplate.query(std,new FieldMaster());
		
			final  Set<String> fieldNames = listTestfield.stream()
		            .map(FieldMaster::getSfieldname)
		            .collect(Collectors.toSet());
			final int defaultValue = +Enumeration.TransactionStatus.YES.gettransactionstatus();
			final int value = +Enumeration.TransactionStatus.NO.gettransactionstatus();
		  Customerpoc = fieldNames.contains("scustomerpoc") ? defaultValue : value;
		  Customershipping = fieldNames.contains("scustomershipingaddress") ? defaultValue : value;
		  otherdetails = fieldNames.contains("sotherdetails") ? defaultValue : value;
		  customerreference = fieldNames.contains("scustomerreference1") ? defaultValue : value;
		  customerreference2 = fieldNames.contains("scustomerreference2") ? defaultValue : value;
		  projectreference = fieldNames.contains("sprojectreference1") ? defaultValue : value;
		  projectreference2 = fieldNames.contains("sprojectreference2") ? defaultValue : value;
		  tinno = fieldNames.contains("scusttin") ? defaultValue : value;
		  accountdetails = fieldNames.contains("saccountdetails") ? defaultValue : value;
	    }
         
					 outputMap.put("customerpoc", Customerpoc);
					 outputMap.put("customerpoc", Customerpoc);
				     outputMap.put("customershipping", Customershipping);
					 outputMap.put("Customerreference", customerreference);
					 outputMap.put("Customerreference2", customerreference2);
					 outputMap.put("Projectreference", projectreference);
					 outputMap.put("Projectreference2", projectreference2);
					 outputMap.put("Otherdetails", otherdetails);
					 outputMap.put("Tinno", tinno);
					 outputMap.put("Accountdetails", accountdetails);
			
		if(CusmtomerMasterList.isEmpty()) {
			outputMap.put("selectedCustomer", null);
			
		}
		else {	
			final int ncustomercode = CusmtomerMasterlist.get(0).getNcustomercode();
			outputMap.put("selectedCustomer", CusmtomerMasterlist.get(0));
			outputMap.putAll((Map<String, Object>) getCustomerFile(ncustomercode, userInfo).getBody());
			}
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}
	/**
	 * This method is used to add a new entry to Invoicecustomermaster table. Need to
	 * check for duplicate entry of Invoicecustomermaster name for the specified site
	 * before saving into database. Need to check that there should be only one
	 * default Invoicecustomermaster for a site
	 * 
	 * @param objInvoicecustomermaster [Invoicecustomermaster] object holding details
	 *                                to be added in Invoicecustomermaster table
	 * @return inserted Invoicecustomermaster object and HTTP Status on successive
	 *         insert otherwise corresponding HTTP Status
	 * @throws Exception that are thrown from this DAO layer
	 */
	@SuppressWarnings("unused")
	@Override
	public ResponseEntity<Object> createInvoiceCustomerMaster(final InvoiceCustomerMaster objInvoiceCustomerMaster,final UserInfo userInfo) throws Exception {

	    if (!objInvoiceCustomerMaster.getSemailid().isEmpty()) {
	        final InvoiceCustomerMaster customerMaster = getcustomerListByName(objInvoiceCustomerMaster.getSemailid(),
	                objInvoiceCustomerMaster.getNsitecode());
	        if (customerMaster != null) {
	            return new ResponseEntity<>(
	                    commonFunction.getMultilingualMessage("IDS_EMAILISALREADYEXISTS", userInfo.getSlanguagefilename()),
	                    HttpStatus.EXPECTATION_FAILED);
	        }
	    }

	    if (!objInvoiceCustomerMaster.getSphone().isEmpty()) {
	        final InvoiceCustomerMaster customermasterphn = getCustomerListByNamephone(objInvoiceCustomerMaster.getSphone(),
	                objInvoiceCustomerMaster.getNsitecode());
	    }

	    if (!objInvoiceCustomerMaster.getScusttin().isEmpty()) {
	        final InvoiceCustomerMaster CustomerMastertin = getcustomerListByNametin(objInvoiceCustomerMaster.getScusttin(),
	                objInvoiceCustomerMaster.getNsitecode());
	        if (CustomerMastertin != null) {
	            return new ResponseEntity<>(
	                    commonFunction.getMultilingualMessage("IDS_TINNOISALREADYEXISTS", userInfo.getSlanguagefilename()),
	                    HttpStatus.EXPECTATION_FAILED);
	        }
	    }

	    if (!objInvoiceCustomerMaster.getScustgst().isEmpty()) {
	        final InvoiceCustomerMaster customerMastergst = getcustomerListByNamegst(objInvoiceCustomerMaster.getScustgst(),
	                objInvoiceCustomerMaster.getNsitecode());
	        if (customerMastergst != null) {
	            return new ResponseEntity<>(
	                    commonFunction.getMultilingualMessage("IDS_GSTISALREADYEXISTS", userInfo.getSlanguagefilename()),
	                    HttpStatus.EXPECTATION_FAILED);
	        }
	    }

	    final String sequencequery = "SELECT nsequenceno FROM seqnoinvoice WHERE stablename ='invoicecustomermaster'";
	    int nsequenceno = jdbcTemplate.queryForObject(sequencequery, Integer.class);
	    nsequenceno++;

	    final String insertquery = "INSERT INTO invoicecustomermaster(ncustomercode, scustomerreference, scustomertypename, ntypecode, scustomername, scustomerpoc, scustomeraddress, scustomershipingaddress, semailid, sphone, saccountdetails, sotherdetails, scusttin, scustgst, scustomerreference1, scustomerreference2, ndiscountavailable, nsameasaddress, sprojectreference1, sprojectreference2, nusercode, dmodifieddate, nsitecode, nstatus)"
	            + "VALUES (" + nsequenceno + ",'" + stringUtilityFunction.replaceQuote(objInvoiceCustomerMaster.getScustomerreference()) + "','DIRECT','2', '"
	            + stringUtilityFunction.replaceQuote(objInvoiceCustomerMaster.getScustomername()) + "','" + stringUtilityFunction.replaceQuote(objInvoiceCustomerMaster.getScustomerpoc()) + "','"
	            + stringUtilityFunction.replaceQuote(objInvoiceCustomerMaster.getScustomeraddress()) + "','" + stringUtilityFunction.replaceQuote(objInvoiceCustomerMaster.getScustomershipingaddress()) + "','"
	            + stringUtilityFunction.replaceQuote(objInvoiceCustomerMaster.getSemailid()) + "','" + stringUtilityFunction.replaceQuote(objInvoiceCustomerMaster.getSphone()) + "','"
	            + stringUtilityFunction.replaceQuote(objInvoiceCustomerMaster.getSaccountdetails()) + "','" + stringUtilityFunction.replaceQuote(objInvoiceCustomerMaster.getSotherdetails()) + "','"
	            + stringUtilityFunction.replaceQuote(objInvoiceCustomerMaster.getScusttin()) + "','" + stringUtilityFunction.replaceQuote(objInvoiceCustomerMaster.getScustgst()) + "','"
	            + stringUtilityFunction.replaceQuote(objInvoiceCustomerMaster.getScustomerreference1()) + "','" + stringUtilityFunction.replaceQuote(objInvoiceCustomerMaster.getScustomerreference2()) + "',"
	            + objInvoiceCustomerMaster.getNdiscountavailable() + ",'" + objInvoiceCustomerMaster.getNsameasaddress() + "','"
	            + stringUtilityFunction.replaceQuote(objInvoiceCustomerMaster.getSprojectreference1()) + "','" + stringUtilityFunction.replaceQuote(objInvoiceCustomerMaster.getSprojectreference2()) + "',"
	            + userInfo.getNusercode() + ",'" + dateUtilityFunction.getCurrentDateTime(userInfo) + "','" + objInvoiceCustomerMaster.getNsitecode() + "','"
	            + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +"')";
	    jdbcTemplate.execute(insertquery);

	    final String updatequery = "UPDATE seqnoinvoice SET nsequenceno =" + nsequenceno + " WHERE stablename='invoicecustomermaster'";
	    jdbcTemplate.execute(updatequery);

	    final String stransdisplaystatus = (objInvoiceCustomerMaster.getNdiscountavailable() == 3) ? "yes" : "No";
	    final List<String> multilingualIDList = new ArrayList<>();
	    multilingualIDList.add("IDS_ADDCUSTOMER");
	    final List<Object> savedCustomerList = new ArrayList<>();
	    objInvoiceCustomerMaster.setNcustomercode(nsequenceno);
	    objInvoiceCustomerMaster.setStransdisplaystatus(stransdisplaystatus);
	    savedCustomerList.add(objInvoiceCustomerMaster);
	    auditUtilityFunction.fnInsertAuditAction(savedCustomerList, 1, null, multilingualIDList, userInfo);

	    return getInvoiceCustomerMaster(userInfo);
	}
	/**
	 * This method is used to fetch the active Invoicecustomermaster objects for the
	 * specified Invoicecustomermaster emailid field.
	 * 
	 * @param ntypecode       [String] name of the Invoicecustomermaster
	 * @param nmasterSiteCode [int] site code of the Invoicecustomermaster
	 * @return list of active Invoicecustomermaster code(s) based on the specified
	 *         Invoicecustomermaster name and site
	 * @throws Exception
	 */
	private InvoiceCustomerMaster getcustomerListByName(final String Semailid, final int nsiteCode) throws Exception {
	    final String strQuery = "select Semailid " +
	                            "from invoicecustomermaster " +
	                            "where Semailid = '" + stringUtilityFunction.replaceQuote(Semailid) + "' " +
	                            "and nsitecode = " + nsiteCode + " " +
	                            "and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and  scustomertypename='DIRECT'";

	    return (InvoiceCustomerMaster) jdbcUtilityFunction.queryForObject(strQuery, InvoiceCustomerMaster.class,
				jdbcTemplate);
	}
	
	/**
	 * This method is used to fetch the active Invoicecustomermaster objects for the
	 * specified Invoicecustomermaster phonenumber field.
	 * 
	 * @param ntypecode       [String] name of the Invoicecustomermaster
	 * @param nmasterSiteCode [int] site code of the Invoicecustomermaster
	 * @return list of active Invoicecustomermaster code(s) based on the specified
	 *         Invoicecustomermaster name and site
	 * @throws Exception
	 */
	private InvoiceCustomerMaster getCustomerListByNamephone(final String sphone, final int nsiteCode) throws Exception {
	    final String strQuery = "select sphone " +
	                            "from invoicecustomermaster " +
	                            "where sphone = '" + stringUtilityFunction.replaceQuote(sphone) + "' " +
	                            "and nsitecode = " + nsiteCode + " " +
	                            "and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and  scustomertypename='DIRECT'";

	    return (InvoiceCustomerMaster) jdbcUtilityFunction.queryForObject(strQuery, InvoiceCustomerMaster.class,
				jdbcTemplate);
	}
	
	/**
	 * This method is used to fetch the active Invoicecustomermaster objects for the
	 * specified Invoicecustomermaster tinno field.
	 * 
	 * @param ntypecode       [String] name of the Invoicecustomermaster
	 * @param nmasterSiteCode [int] site code of the Invoicecustomermaster
	 * @return list of active Invoicecustomermaster code(s) based on the specified
	 *         Invoicecustomermaster name and site
	 * @throws Exception
	 */
	private InvoiceCustomerMaster getcustomerListByNametin(final String scusttin, final int nsiteCode) throws Exception {
	    final String strQuery = "select scusttin " +
	                            "from invoicecustomermaster " +
	                            "where scusttin = '" + stringUtilityFunction.replaceQuote(scusttin) + "' " +
	                            "and nsitecode = " + nsiteCode + " " +
	                            "and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+ " and  scustomertypename='DIRECT'";
   return (InvoiceCustomerMaster) jdbcUtilityFunction.queryForObject(strQuery, InvoiceCustomerMaster.class,
				jdbcTemplate);
	}
	/**
	 * This method is used to fetch the active Invoicecustomermaster objects for the
	 * specified Invoicecustomermaster name and site.
	 * 
	 * @param ntypecode       [String] name of the Invoicecustomermaster
	 * @param nmasterSiteCode [int] site code of the Invoicecustomermaster
	 * @return list of active Invoicecustomermaster code(s) based on the specified
	 *         Invoicecustomermaster name and site
	 * @throws Exception
	 */
	private InvoiceCustomerMaster getcustomerListByNamegst(final String scustgst, final int nsiteCode) throws Exception {
	    final String strQuery = "select scustgst " +
	                            "from invoicecustomermaster " +
	                            "where scustgst = '" + stringUtilityFunction.replaceQuote(scustgst) + "' " +
	                            "and nsitecode = " + nsiteCode + " " +
	                            "and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+ " and  scustomertypename='DIRECT'";

	    return (InvoiceCustomerMaster) jdbcUtilityFunction.queryForObject(strQuery, InvoiceCustomerMaster.class,
				jdbcTemplate);
	}
	/**
	 * This method is used to update entry in Invoicecustomermaster table. Need to
	 * validate that the Invoicecustomermaster object to be updated is active before
	 * updating details in database. Need to check for duplicate entry of
	 * Invoicecustomermaster name for the specified site before saving into database.
	 * Need to check that there should be only one default Invoicecustomermaster for
	 * a site
	 * 
	 * @param objInvoicecustomermaster [Invoicecustomermaster] object holding details
	 *                                to be updated in Invoicecustomermaster table
	 * @return response entity object holding response status and data of updated
	 *         Invoicecustomermaster object
	 * @throws Exception that are thrown from this DAO layer
	 */
	
	
	@SuppressWarnings("unused")
	public ResponseEntity<Object> updateInvoiceCustomerMaster(final InvoiceCustomerMaster objInvoiceCustomerMaster,final UserInfo userInfo) throws Exception {
	    
	    final InvoiceCustomerMaster customerMaster = getActiveInvoiceCustomerMasterByIdforUpdate(objInvoiceCustomerMaster.getNcustomercode(),userInfo);
	    if (customerMaster == null) {
	        return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(), userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
	    }
	    
	    final String queryStringemail = "select semailid from invoicecustomermaster where nstatus = "
	            + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and nsitecode="+userInfo.getNmastersitecode()+""
	            + " and semailid is not null and semailid <>'' and ncustomercode!="
	            + objInvoiceCustomerMaster.getNcustomercode();
	    final List<InvoiceCustomerMaster> CMAllEmails = (List<InvoiceCustomerMaster>) jdbcTemplate.query(queryStringemail,
	            new InvoiceCustomerMaster());

	    final String getEmailIds = objInvoiceCustomerMaster.getSemailid();
	    boolean duplicateEmail = false;

	    if (getEmailIds != null && !getEmailIds.trim().isEmpty() && !"NA".equalsIgnoreCase(getEmailIds.trim())) {
	        duplicateEmail = CMAllEmails.stream()
	                .anyMatch(customer -> getEmailIds.equalsIgnoreCase(customer.getSemailid()));
	    }

	    final String queryStringphone = "select sphone from invoicecustomermaster where nstatus = "
	            + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and nsitecode="+userInfo.getNmastersitecode()+""
	            + " and sphone is not null and sphone <>'' and ncustomercode!="
	            + objInvoiceCustomerMaster.getNcustomercode();
	    final List<InvoiceCustomerMaster> CMAllphones = (List<InvoiceCustomerMaster>) jdbcTemplate.query(queryStringphone,
	            new InvoiceCustomerMaster());

	    final String getphone = objInvoiceCustomerMaster.getSphone();
	    boolean duplicatephone = false;

	    if (getphone != null && !getphone.trim().isEmpty() && !"NA".equalsIgnoreCase(getphone.trim())) {
	        String normalizedPhone = getphone.trim().replaceAll("[^\\d]", "");
	        duplicatephone = CMAllphones.stream()
	                .anyMatch(customer -> {
	                    if (customer.getSphone() != null) {
	                        String customerPhone = customer.getSphone().trim().replaceAll("[^\\d]", "");
	                        return customerPhone.equals(normalizedPhone);
	                    }
	                    return false;
	                });
	    }

	    final String queryStringtin = "select scusttin from invoicecustomermaster where nstatus = "
	            + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and nsitecode="+userInfo.getNmastersitecode()+""
	            + " and scusttin is not null and scusttin <>'' and ncustomercode!="
	            + objInvoiceCustomerMaster.getNcustomercode();
	    final List<InvoiceCustomerMaster> CMAllTin = (List<InvoiceCustomerMaster>) jdbcTemplate.query(queryStringtin,
	            new InvoiceCustomerMaster());

	    final String getTIN = objInvoiceCustomerMaster.getScusttin();
	    boolean duplicateTin = false;

	    if (getTIN != null && !getTIN.trim().isEmpty() && !"NA".equalsIgnoreCase(getTIN.trim())) {
	        duplicateTin = CMAllTin.stream()
	                .anyMatch(customer -> getTIN.equalsIgnoreCase(customer.getScusttin()));
	    }

	    final String queryStringgst = "select scustgst from invoicecustomermaster where nstatus = "
	            + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and nsitecode="+userInfo.getNmastersitecode()+""
	            + " and scustgst is not null and scustgst <>'' and ncustomercode!="
	            + objInvoiceCustomerMaster.getNcustomercode();
	    final List<InvoiceCustomerMaster> CMAllGST = (List<InvoiceCustomerMaster>) jdbcTemplate.query(queryStringgst,
	            new InvoiceCustomerMaster());

	    final String getGST = objInvoiceCustomerMaster.getScustgst();
	    boolean duplicateGST = false;

	    if (getGST != null && !getGST.trim().isEmpty() && !"NA".equalsIgnoreCase(getGST.trim())) {
	        duplicateGST = CMAllGST.stream()
	                .anyMatch(customer -> getGST.equalsIgnoreCase(customer.getScustgst()));
	    }

	    if (duplicateEmail) {
	        return new ResponseEntity<>(
	                commonFunction.getMultilingualMessage("IDS_EMAILISALREADYEXISTS", userInfo.getSlanguagefilename()),
	                HttpStatus.EXPECTATION_FAILED);
	    }
   
	    if (duplicateTin) {
	        return new ResponseEntity<>(
	                commonFunction.getMultilingualMessage("IDS_TINNOISALREADYEXISTS", userInfo.getSlanguagefilename()),
	                HttpStatus.EXPECTATION_FAILED);
	    }
	    
	    if (duplicateGST) {
	        return new ResponseEntity<>(
	                commonFunction.getMultilingualMessage("IDS_GSTISALREADYEXISTS", userInfo.getSlanguagefilename()),
	                HttpStatus.EXPECTATION_FAILED);
	    }

	    final String updateQuery = "update invoicecustomermaster set scustomerreference ='" + stringUtilityFunction.replaceQuote(objInvoiceCustomerMaster.getScustomerreference()) + "',"
	            + "scustomertypename='" + stringUtilityFunction.replaceQuote(objInvoiceCustomerMaster.getScustomertypename()) + "',"
	            + "ntypecode=" + objInvoiceCustomerMaster.getNtypecode() + ","
	            + "scustomername='" + stringUtilityFunction.replaceQuote(objInvoiceCustomerMaster.getScustomername()) + "',"
	            + "scustomerpoc='" + stringUtilityFunction.replaceQuote(objInvoiceCustomerMaster.getScustomerpoc()) + "',"
	            + "scustomeraddress='" + stringUtilityFunction.replaceQuote(objInvoiceCustomerMaster.getScustomeraddress()) + "',"
	            + "scustomershipingaddress='" + stringUtilityFunction.replaceQuote(objInvoiceCustomerMaster.getScustomershipingaddress()) + "',"
	            + "semailid='" + stringUtilityFunction.replaceQuote(objInvoiceCustomerMaster.getSemailid()) + "',"
	            + "sphone='" + stringUtilityFunction.replaceQuote(objInvoiceCustomerMaster.getSphone()) + "',"
	            + "saccountdetails='" + stringUtilityFunction.replaceQuote(objInvoiceCustomerMaster.getSaccountdetails()) + "',"
	            + "sotherdetails='" + stringUtilityFunction.replaceQuote(objInvoiceCustomerMaster.getSotherdetails()) + "',"
	            + "scusttin='" + stringUtilityFunction.replaceQuote(objInvoiceCustomerMaster.getScusttin()) + "',"
	            + "scustgst='" + stringUtilityFunction.replaceQuote(objInvoiceCustomerMaster.getScustgst()) + "',"
	            + "scustomerreference1='" + stringUtilityFunction.replaceQuote(objInvoiceCustomerMaster.getScustomerreference1()) + "',"
	            + "scustomerreference2='" + stringUtilityFunction.replaceQuote(objInvoiceCustomerMaster.getScustomerreference2()) + "',"
	            + "ndiscountavailable=" + objInvoiceCustomerMaster.getNdiscountavailable() + ","
	            + "nsameasaddress=" + objInvoiceCustomerMaster.getNsameasaddress() + ","
	            + "sprojectreference1='" + stringUtilityFunction.replaceQuote(objInvoiceCustomerMaster.getSprojectreference1()) + "',"
	            + "sprojectreference2='" + stringUtilityFunction.replaceQuote(objInvoiceCustomerMaster.getSprojectreference2()) + "',"
	            + "dmodifieddate='" + dateUtilityFunction.getCurrentDateTime(userInfo) + "' where ncustomercode=" + objInvoiceCustomerMaster.getNcustomercode();

	    jdbcTemplate.execute(updateQuery);
	    
	    final List<String> multilingualIDList = new ArrayList<>();
	    multilingualIDList.add("IDS_EDITCUSTOMER");
	    final List<Object> listAfterSave = new ArrayList<>();
	    listAfterSave.add(objInvoiceCustomerMaster);
	    final List<Object> listBeforeSave = new ArrayList<>();
	    listBeforeSave.add(customerMaster);
	    auditUtilityFunction.fnInsertAuditAction(listAfterSave, 2, listBeforeSave, multilingualIDList, userInfo);

	    return getInvoiceCustomerMaster(userInfo);
	}
	/**
	 * This method id used to delete an entry in Invoicecustomermaster table Need to
	 * check the record is already deleted or not Need to check whether the record
	 * is used in other tables such as
	 * 'testparameter','testgrouptestparameter','transactionsampleresults'
	 * 
	 * @param objInvoicecustomermaster [Invoicecustomermaster] an Object holds the
	 *                                record to be deleted
	 * @return a response entity with corresponding HTTP status and an
	 *         Invoicecustomermaster object
	 * @exception Exception that are thrown from this DAO layer
	 */
	public ResponseEntity<Object> deleteInvoiceCustomerMaster(final InvoiceCustomerMaster objInvoiceCustomerMaster,final UserInfo userInfo) throws Exception{
		final List<Object> savedCustomerList = new ArrayList<>();
		final List<String> multilingualIDList  = new ArrayList<>();

		final InvoiceCustomerMaster customerMaster = getActiveInvoiceCustomerMasterByIdforUpdate(objInvoiceCustomerMaster.getNcustomercode(),userInfo);
		if (customerMaster == null){
			return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}
		else {
			
			final String query = "select 'IDS_TRANSACTION' as Msg from invoicequotationheader where "
			        + "jsondata->>'CustomerName' = '" + customerMaster.getScustomername() + "' and "
			        + "jsondata->>'PhoneNo' = '" + customerMaster.getSphone() + "' and " // Ensure PhoneNo matches as well
			        + "nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+ " and nsitecode="+userInfo.getNmastersitecode()
			        + " union all"
			        + " select 'IDS_TRANSACTION' as Msg from invoiceheader where "
			        + "jsondata->>'CustomerName' = '" + customerMaster.getScustomername() + "' and "
			        + "jsondata->>'PhoneNo' = '" + customerMaster.getSphone() + "' and " // Ensure PhoneNo matches as well
			        + "nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and nsitecode="+userInfo.getNmastersitecode();
			ValidatorDel objDeleteValidation= projectDAOSupport.getTransactionInfo(query, userInfo);  
		
			boolean validRecord = false;
			if (objDeleteValidation.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) 
			{		
				validRecord = true;
				objDeleteValidation =  projectDAOSupport.validateDeleteRecord(Integer.toString(objInvoiceCustomerMaster.getNcustomercode()), userInfo);
				if (objDeleteValidation.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) 
				{					
					validRecord = true;
				}
				else {
					validRecord = false;
				}
			}
		
			if(validRecord) {
			final String deleteQuery="update invoicecustomermaster set nstatus = "+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ", dmodifieddate = '"+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'" + " where ncustomercode= "+objInvoiceCustomerMaster.getNcustomercode()+";";
			 jdbcTemplate.execute(deleteQuery);
			 objInvoiceCustomerMaster.setNstatus((short) Enumeration.TransactionStatus.DELETED.gettransactionstatus());
			 
			 	savedCustomerList.add(objInvoiceCustomerMaster);						
				multilingualIDList.add("IDS_DELETECUSTOMER");
				auditUtilityFunction.fnInsertAuditAction(savedCustomerList, 1, null, multilingualIDList, userInfo);
				return getInvoiceCustomerMaster(userInfo) ; 
		}
		
		else{
			
			return new ResponseEntity<>(objDeleteValidation.getSreturnmessage(), HttpStatus.EXPECTATION_FAILED);
		}	
				
		}
	}
	/**
	 * This method is used to fetch the active Invoicecustomermaster objects for the
	 * specified Invoicecustomermaster name and site.
	 * 
	 * @param ntypecode       [String] name of the Invoicecustomermaster
	 * @param nmasterSiteCode [int] site code of the Invoicecustomermaster
	 * @return list of active Invoicecustomermaster code(s) based on the specified
	 *         Invoicecustomermaster name and site
	 * @throws Exception
	 */
	@SuppressWarnings({ "unchecked", "unused" })
	@Override
	public ResponseEntity<Object> getSelectedCustomerDetail(final UserInfo userInfo, final int ncustomercode) throws Exception {

		final Map<String, Object> objMap = new LinkedHashMap<String, Object>();
		final int nuser=userInfo.getNuserrole();
		final String orderbyalphabetical="scustomername";
		
		final String strQuery = "select icm.*,coalesce(ts.jsondata->'stransdisplaystatus'->>'"+userInfo.getSlanguagetypecode()+"') as stransdisplaystatus "
				+ "from invoicecustomermaster icm,transactionstatus ts where ts.ntranscode=icm.ndiscountavailable "
				+ " and icm.ncustomercode = " +ncustomercode + " and icm.nstatus= "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +""
				+ " and icm.nsitecode="+userInfo.getNmastersitecode();
		final List<InvoiceCustomerMaster> lstSelectedCustomerDetail = jdbcTemplate.query(strQuery, new InvoiceCustomerMaster());

		final List<InvoiceCustomerType> CustomerTypeList =(List <InvoiceCustomerType>)getCustomerType(userInfo).getBody();
		objMap.put("customerTypeName", CustomerTypeList);
		objMap.put("selectedCustomer", lstSelectedCustomerDetail.get(0));
		objMap.putAll((Map<String, Object>) getCustomerFile(ncustomercode, userInfo).getBody());

			objMap.put("customerTypeName", CustomerTypeList);
			objMap.put("selectedCustomer", lstSelectedCustomerDetail.get(0));
		objMap.putAll((Map<String, Object>) getCustomerFile(ncustomercode, userInfo).getBody());
		
		 int Customerpoc=0;
		 int Customershipping=0;
		 int otherdetails=0; 
		 int customerreference=0;
		 int customerreference2=0;
		 int projectreference=0;
		 int projectreference2=0;
		 int tinno=0;
		 int accountdetails=0;
		 final String st = "select * from userrolefieldcontrol where nformcode="+userInfo.getNformcode()+" and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and nneedrights=3 ";
		 final List<UserRoleFieldControl> listTest = (List<UserRoleFieldControl>) jdbcTemplate.query(st,new UserRoleFieldControl());
		final String sntestgrouptestcode = stringUtilityFunction.fnDynamicListToString(listTest, "getNfieldcode");
		
		if(listTest.size()==0) {
			final Map<String, Integer> customerDetails = new HashMap<>();
			final int defaultValue = +Enumeration.TransactionStatus.NO.gettransactionstatus();
			    customerDetails.put("Customerpoc", defaultValue);
			    customerDetails.put("Customershipping", defaultValue);
			    customerDetails.put("otherdetails", defaultValue);
			    customerDetails.put("customerreference1", defaultValue);
			    customerDetails.put("customerreference2", defaultValue);
			    customerDetails.put("projectreference1", defaultValue);
			    customerDetails.put("projectreference2", defaultValue);
			    customerDetails.put("tinno", defaultValue);
			    customerDetails.put("accountdetails", defaultValue);
		}
		else {
			final String std="select * from fieldmaster where nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and "
				 +"nfieldcode in (" + sntestgrouptestcode	+ ")";
	
			final List<FieldMaster> listtest = (List<FieldMaster>) jdbcTemplate.query(std,new FieldMaster());
		
			final Set<String> fieldNames = listtest.stream()
		            .map(FieldMaster::getSfieldname)
		            .collect(Collectors.toSet());
			final int defaultValue = +Enumeration.TransactionStatus.YES.gettransactionstatus();
			final int value = +Enumeration.TransactionStatus.NO.gettransactionstatus();
		  Customerpoc = fieldNames.contains("scustomerpoc") ? defaultValue : value;
		  Customershipping = fieldNames.contains("scustomershipingaddress") ? defaultValue : value;
		  otherdetails = fieldNames.contains("sotherdetails") ? defaultValue : value;
		  customerreference = fieldNames.contains("scustomerreference1") ? defaultValue : value;
		  customerreference2 = fieldNames.contains("scustomerreference2") ? defaultValue : value;
		  projectreference = fieldNames.contains("sprojectreference1") ? defaultValue : value;
		  projectreference2 = fieldNames.contains("sprojectreference2") ? defaultValue : value;
		  tinno = fieldNames.contains("scusttin") ? defaultValue : value;
		  accountdetails = fieldNames.contains("saccountdetails") ? defaultValue : value;
	    }
         
			
		objMap.put("customerpoc", Customerpoc);
		objMap.put("customerpoc", Customerpoc);
		objMap.put("customershipping", Customershipping);
		objMap.put("Customerreference", customerreference);
		objMap.put("Customerreference2", customerreference2);
		objMap.put("Projectreference", projectreference);
		objMap.put("Projectreference2", projectreference2);
		objMap.put("Otherdetails", otherdetails);
		objMap.put("Tinno", tinno);
		objMap.put("Accountdetails", accountdetails);
			
		return new ResponseEntity<Object>(objMap, HttpStatus.OK);
}
	/**
	 * This method is used to fetch the active Invoicecustomermaster objects for the
	 * specified Invoicecustomermaster name and site.
	 * 
	 * @param ntypecode       [String] name of the Invoicecustomermaster
	 * @param nmasterSiteCode [int] site code of the Invoicecustomermaster
	 * @return list of active Invoicecustomermaster code(s) based on the specified
	 *         Invoicecustomermaster name and site
	 * @throws Exception
	 */
	@Override
	public ResponseEntity<Object> getCustomerType(final UserInfo userinfo) throws Exception {
		final String strQuery = "select ntypecode, scustomertypename from invoicecustomertype " 
				+ " where nstatus= "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
				+ " and scustomertypename="+"'DIRECT'";
	 	return new ResponseEntity<>(jdbcTemplate.query(strQuery, new InvoiceCustomerType()),HttpStatus.OK);	
}
	/**
	 * This method is used to get the Invoicecustomermaster objects for the
	 * specified Invoicecustomermaster name and site.
	*/
	
	public ResponseEntity<Object> getCustomermaster(final UserInfo userinfo) throws Exception {

		final String strQuery = "select icm.*,coalesce(ts.jsondata->'stransdisplaystatus'->>'"+userinfo.getSlanguagetypecode()+"') as stransdisplaystatus "
				+ " from invoicecustomermaster icm,transactionstatus ts where ts.ntranscode=icm.ndiscountavailable and " 
				+ "icm.nstatus= "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +" and icm.nsitecode="+userinfo.getNmastersitecode()+""
				+ " order by ncustomercode desc" ;
		
		
		return new ResponseEntity<>(jdbcTemplate.query(strQuery, new InvoiceCustomerMaster()),HttpStatus.OK);	
}
	/**
	 * This method is used to fetch the active Invoicecustomermaster objects for the
	 * specified Invoicecustomermaster name and site.
	 * 
	 * @param ntypecode       [String] name of the Invoicecustomermaster
	 * @param nmasterSiteCode [int] site code of the Invoicecustomermaster
	 * @return list of active Invoicecustomermaster code(s) based on the specified
	 *         Invoicecustomermaster name and site
	 * @throws Exception
	 */
	@SuppressWarnings("unused")
	@Override
	public ResponseEntity<Object> getCustomerMaster(final UserInfo userinfo) throws Exception {
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		final int nuser=userinfo.getNuserrole();
			
			@SuppressWarnings("unchecked")
			final List<InvoiceCustomerType> CustomerTypeList =(List <InvoiceCustomerType>)getCustomerType(userinfo).getBody();
			outputMap.put("customerTypeName", CustomerTypeList);
			
			final String strQuery = "select icm.scustomerreference,icm.scustomertypename,icm.scustomername, ict.scustomertypename "
					+ " from invoicecustomermaster icm,invoicecustomertype ict where icm.ntypecode=ict.ntypecode "
					+ " and icm.nstatus= "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +" and icm.nsitecode="+userinfo.getNmastersitecode()+""
					+ " and ict.nstatus= "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			final List<InvoiceCustomerMaster> CusmtomerMasterList = jdbcTemplate.query(strQuery, new InvoiceCustomerMaster());
			outputMap.put("cusmtomermasterlist", CusmtomerMasterList);
			 int Customerpoc=0;
			 int Customershipping=0;
			 int otherdetails=0;
			 int customerreference=0;
			 int customerreference2=0;
			 int projectreference=0;
			 int projectreference2=0;
			 int tinno=0;
			 int accountdetails=0;
	
		
			 final String st = "select * from userrolefieldcontrol  where nformcode="+userinfo.getNformcode()+" and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and nneedrights=3 ";
			 final List<UserRoleFieldControl> listTest = (List<UserRoleFieldControl>) jdbcTemplate.query(st,new UserRoleFieldControl());
		final String sntestgrouptestcode = stringUtilityFunction.fnDynamicListToString(listTest, "getNfieldcode");
		
		if(listTest.size()==0) {
			final Map<String, Integer> customerDetails = new HashMap<>();
			final int defaultValue = +Enumeration.TransactionStatus.NO.gettransactionstatus();
			    customerDetails.put("Customerpoc", defaultValue);
			    customerDetails.put("Customershipping", defaultValue);
			    customerDetails.put("otherdetails", defaultValue);
			    customerDetails.put("customerreference1", defaultValue);
			    customerDetails.put("customerreference2", defaultValue);
			    customerDetails.put("projectreference1", defaultValue);
			    customerDetails.put("projectreference2", defaultValue);
			    customerDetails.put("tinno", defaultValue);
			    customerDetails.put("accountdetails", defaultValue);
		}
		else {
			final String std="select * from fieldmaster where nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and "
				 +"nfieldcode in (" + sntestgrouptestcode	+ ")";
	
			final List<FieldMaster> listtest = (List<FieldMaster>) jdbcTemplate.query(std,new FieldMaster());
		
			final Set<String> fieldNames = listtest.stream()
		            .map(FieldMaster::getSfieldname)
		            .collect(Collectors.toSet());
			final int defaultValue = +Enumeration.TransactionStatus.YES.gettransactionstatus();
			final int value = +Enumeration.TransactionStatus.NO.gettransactionstatus();
		  Customerpoc = fieldNames.contains("scustomerpoc") ? defaultValue : value;
		  Customershipping = fieldNames.contains("scustomershipingaddress") ? defaultValue : value;
		  otherdetails = fieldNames.contains("sotherdetails") ? defaultValue : value;
		  customerreference = fieldNames.contains("scustomerreference1") ? defaultValue : value;
		  customerreference2 = fieldNames.contains("scustomerreference2") ? defaultValue : value;
		  projectreference = fieldNames.contains("sprojectreference1") ? defaultValue : value;
		  projectreference2 = fieldNames.contains("sprojectreference2") ? defaultValue : value;
		  tinno = fieldNames.contains("scusttin") ? defaultValue : value;
		  accountdetails = fieldNames.contains("saccountdetails") ? defaultValue : value;
	    }
		outputMap.put("customerTypeName", CustomerTypeList);
		outputMap.put("cusmtomermasterlist", CusmtomerMasterList);
		outputMap.put("customerpoc", Customerpoc);
		outputMap.put("customershipping", Customershipping);
		outputMap.put("Customerreference", customerreference);
		outputMap.put("Customerreference2", customerreference2);
		outputMap.put("Projectreference", projectreference);
		outputMap.put("Projectreference2", projectreference2);
		outputMap.put("Otherdetails", otherdetails);
		outputMap.put("Tinno", tinno);
		outputMap.put("Accountdetails", accountdetails);
		 
		
		
		return new ResponseEntity<>(outputMap, HttpStatus.OK);	
}
	
	/**
	 * This method is used to fetch the active Invoicecustomermaster objects for the
	 * specified Invoicecustomermaster name and site.
	 * 
	 * @param ntypecode       [String] name of the Invoicecustomermaster
	 * @param nmasterSiteCode [int] site code of the Invoicecustomermaster
	 * @return list of active Invoicecustomermaster code(s) based on the specified
	 *         Invoicecustomermaster name and site
	 * @throws Exception
	 */
	@Override
	public ResponseEntity<Object> getActiveInvoiceCustomerMasterById(final int ncustomercode,final UserInfo userInfo) throws Exception {
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();

		final String query;
		
		final String squery ="select scustomername,sphone from invoicecustomermaster where ncustomercode =" +ncustomercode+ ""
				+ " and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+ " and nsitecode="+userInfo.getNmastersitecode();

		final InvoiceCustomerMaster objInvoicecustomerMaster = (InvoiceCustomerMaster) jdbcUtilityFunction.queryForObject(squery, InvoiceCustomerMaster.class,
				jdbcTemplate);
		
		
	    query = "select 'IDS_TRANSACTION' as Msg from invoicequotationheader where "
		        + " jsondata->>'CustomerName' = '" + objInvoicecustomerMaster.getScustomername() + "' and "
		        + " jsondata->>'PhoneNo' = '" + objInvoicecustomerMaster.getSphone() + "' and " // Ensure PhoneNo matches as well
		        + " nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+ " and nsitecode="+userInfo.getNmastersitecode()+""
		        + " union all"
		        + " select 'IDS_TRANSACTION' as Msg from invoiceheader where "
		        + " jsondata->>'CustomerName' = '" + objInvoicecustomerMaster.getScustomername() + "' and "
		        + " jsondata->>'PhoneNo' = '" + objInvoicecustomerMaster.getSphone() + "' and " // Ensure PhoneNo matches as well
		        + " nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
		        + " and nsitecode="+userInfo.getNmastersitecode();
		ValidatorDel objDeleteValidation= projectDAOSupport.getTransactionInfo(query, userInfo);  
	
				
		boolean validRecord = false;
		if (objDeleteValidation.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) 
		{		
			validRecord = true;
			objDeleteValidation =  projectDAOSupport.validateDeleteRecord(objInvoicecustomerMaster.getScustomername(), userInfo);
			if (objDeleteValidation.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) 
			{					
				validRecord = true;
			}
			else {
				validRecord = false;
			}
		}
		
		if(validRecord) {
		
		final String strQuery = "select ncustomercode, scustomerreference,scustomertypename, ntypecode, scustomername, scustomerpoc, "
				+ " scustomeraddress, scustomershipingaddress, semailid, sphone, saccountdetails, sotherdetails, scusttin, scustgst, "
				+ " scustomerreference1, scustomerreference2, ndiscountavailable, sprojectreference1, sprojectreference2,nusercode,"
				+ " dmodifieddate,nsitecode,nstatus,nsameasaddress  from invoicecustomermaster "  
				+ " where ncustomercode = " +ncustomercode +" and nstatus= "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
				+ " and nsitecode="+userInfo.getNmastersitecode();
	
		final InvoiceCustomerMaster lstActiveInvoiceCustomer  = (InvoiceCustomerMaster) jdbcUtilityFunction.queryForObject(strQuery,
				InvoiceCustomerMaster.class,jdbcTemplate);
		
		outputMap.put("selectedCustomer", lstActiveInvoiceCustomer);
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
		}
		else{
			
			return new ResponseEntity<>(objDeleteValidation.getSreturnmessage(), HttpStatus.EXPECTATION_FAILED);
		}
		
	}
	/**
	 * This method is used to fetch the active Invoicecustomermaster objects for the
	 * specified Invoicecustomermaster name and site.
	 * 
	 * @param ntypecode       [String] name of the Invoicecustomermaster
	 * @param nmasterSiteCode [int] site code of the Invoicecustomermaster
	 * @return list of active Invoicecustomermaster code(s) based on the specified
	 *         Invoicecustomermaster name and site
	 * @throws Exception
	 */
	@Override
	public InvoiceCustomerMaster getActiveInvoiceCustomerMasterByIdforUpdate(final int ncustomercode,final UserInfo userInfo) throws Exception {
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		final String strQuery = "select ncustomercode, scustomerreference,scustomertypename, ntypecode, scustomername, scustomerpoc,"
				+ " scustomeraddress, scustomershipingaddress, semailid, sphone, saccountdetails, sotherdetails, scusttin, scustgst,"
				+ " scustomerreference1, scustomerreference2, ndiscountavailable, sprojectreference1, sprojectreference2,nusercode,"
				+ " dmodifieddate,nsitecode,nstatus,nsameasaddress  from invoicecustomermaster where ncustomercode = " +ncustomercode 
				+ " and nstatus= "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and nsitecode="+userInfo.getNmastersitecode();
		final List<InvoiceCustomerMaster> lstActiveInvoiceCustomer = jdbcTemplate.query(strQuery, new InvoiceCustomerMaster());
		outputMap.put("selectedCustomer", lstActiveInvoiceCustomer);
//		return new ResponseEntity<>(outputMap, HttpStatus.OK);	
		return (InvoiceCustomerMaster) jdbcUtilityFunction.queryForObject(strQuery, InvoiceCustomerMaster.class,jdbcTemplate);
	}
	/**
	 * This method is used to add a new entry to Invoicecustomermaster table. Need to
	 * check for duplicate entry of Invoicecustomermaster name for the specified site
	 * before saving into database. Need to check that there should be only one
	 * default Invoicecustomermaster for a site
	 * 
	 * @param objInvoicecustomermaster [Invoicecustomermaster] object holding details
	 *                                to be added in Invoicecustomermaster table
	 * @return inserted Invoicecustomermaster object and HTTP Status on successive
	 *         insert otherwise corresponding HTTP Status
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> createCustomerFile(final MultipartHttpServletRequest request, final UserInfo objUserInfo)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();

		final List<CustomerFile> lstReqCustomerFile = objMapper.readValue(request.getParameter("customerfile"),
				new TypeReference<List<CustomerFile>>() {
				});

		if (lstReqCustomerFile != null && lstReqCustomerFile.size() > 0) {
			final InvoiceCustomerMaster objInvoiceCustomerMaster = checKCustomerIsPresent(lstReqCustomerFile.get(0).getNcustomercode(),objUserInfo);

			if (objInvoiceCustomerMaster != null) {
				String sReturnString = Enumeration.ReturnStatus.SUCCESS.getreturnstatus();
				if (lstReqCustomerFile.get(0).getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()) {

					sReturnString = ftpUtilityFunction.getFileFTPUpload(request, -1, objUserInfo); // Folder Name - master
				}

				if (sReturnString.equals(Enumeration.ReturnStatus.SUCCESS.getreturnstatus())) {

					final Instant instantDate = dateUtilityFunction.getCurrentDateTime(objUserInfo).truncatedTo(ChronoUnit.SECONDS);
					final String sattachmentDate = dateUtilityFunction.instantDateToString(instantDate);
					final int noffset = dateUtilityFunction.getCurrentDateTimeOffset(objUserInfo.getStimezoneid());

					lstReqCustomerFile.forEach(objtf -> {
						objtf.setDcreateddate(instantDate);
						if (objtf.getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()) {
							objtf.setDcreateddate(instantDate);
							objtf.setNoffsetdcreateddate(noffset);
							objtf.setScreateddate(sattachmentDate.replace("T", " "));
						}

					});

					final String sequencequery = "select nsequenceno from seqnoinvoice where stablename ='customerfile'";
					int nsequenceno = jdbcTemplate.queryForObject(sequencequery, Integer.class);
					nsequenceno++;
					final String insertquery = "Insert into customerfile(ncustomerfilecode,ncustomercode,nlinkcode,nattachmenttypecode,sfilename,sdescription,nfilesize,dcreateddate,noffsetdcreateddate,ntzcreateddate,ssystemfilename,dmodifieddate,nsitecode,nstatus)"
							+ "values (" + nsequenceno + "," + lstReqCustomerFile.get(0).getNcustomercode() + ","
							+ lstReqCustomerFile.get(0).getNlinkcode() + ","
							+ lstReqCustomerFile.get(0).getNattachmenttypecode() + "," + " N'"
							+ stringUtilityFunction.replaceQuote(lstReqCustomerFile.get(0).getSfilename()) + "',N'"
							+ stringUtilityFunction.replaceQuote(lstReqCustomerFile.get(0).getSdescription()) + "',"
							+ lstReqCustomerFile.get(0).getNfilesize() + "," + " '"
							+ lstReqCustomerFile.get(0).getDcreateddate() + "',"
							+ lstReqCustomerFile.get(0).getNoffsetdcreateddate() + "," + objUserInfo.getNtimezonecode()
							+ ",N'" + lstReqCustomerFile.get(0).getSsystemfilename() + "','"
							+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "'," + objUserInfo.getNmastersitecode() + ","
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";

					jdbcTemplate.execute(insertquery);

					final String updatequery = "update seqnoinvoice set nsequenceno =" + nsequenceno
							+ " where stablename ='customerfile'";
					jdbcTemplate.execute(updatequery);

					final List<String> multilingualIDList = new ArrayList<>();

					multilingualIDList.add(
							lstReqCustomerFile.get(0).getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()
									? "IDS_ADDCUSTOMERFILE"
									: "IDS_ADDCUSTOMERLINK");
					final List<Object> listObject = new ArrayList<Object>();
					final String auditqry = "select * from customerfile where ncustomercode = "
							+  lstReqCustomerFile.get(0).getNcustomercode() + " and ncustomerfilecode = " + nsequenceno
							+ " and nstatus =" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
					final List<CustomerFile> lstvalidate = (List<CustomerFile>) jdbcTemplate.query(auditqry,
							new CustomerFile());

					listObject.add(lstvalidate);

					auditUtilityFunction.fnInsertListAuditAction(listObject, 1, null, multilingualIDList, objUserInfo);
				} else {
					// status code:417
					return new ResponseEntity<>(
							commonFunction.getMultilingualMessage(sReturnString, objUserInfo.getSlanguagefilename()),
							HttpStatus.EXPECTATION_FAILED);
				}
			} else {
				// status code:417
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_CUSTOMERALREADYDELETED",
						objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}
			return (getCustomerFile(lstReqCustomerFile.get(0).getNcustomercode(), objUserInfo));
		} else {
			// status code:417
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_FILENOTFOUND", objUserInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}
	/**
	 * This method id used to delete an entry in Invoicecustomermaster table Need to
	 * check the record is already deleted or not Need to check whether the record
	 * is used in other tables such as
	 * 'testparameter','testgrouptestparameter','transactionsampleresults'
	 * 
	 * @param objInvoicecustomermaster [Invoicecustomermaster] an Object holds the
	 *                                record to be deleted
	 * @return a response entity with corresponding HTTP status and an
	 *         Invoicecustomermaster object
	 * @exception Exception that are thrown from this DAO layer
	 */

	@Override
	public ResponseEntity<Object> deleteCustomerFile(final CustomerFile objCustomerFile, final UserInfo objUserInfo) throws Exception {
		final InvoiceCustomerMaster invoicecustomermaster  = checKCustomerIsPresent(objCustomerFile.getNcustomercode(),objUserInfo);
		if (invoicecustomermaster != null) {
			if (objCustomerFile != null) {
				final String sQuery = "select * from customerfile where ncustomerfilecode = "
						+  objCustomerFile.getNcustomerfilecode() + " and nstatus = "
						+  Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and nsitecode="+objUserInfo.getNmastersitecode();
				final CustomerFile objTF = (CustomerFile) jdbcUtilityFunction.queryForObject(sQuery, CustomerFile.class,jdbcTemplate);
				if (objTF != null) {
					final String sUpdateQuery = "update CustomerFile set" + "  dmodifieddate ='"
							+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "'" + ", nstatus = "
							+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " where ncustomerfilecode = "
							+ objCustomerFile.getNcustomerfilecode();
					jdbcTemplate.execute(sUpdateQuery);
					final List<String> multilingualIDList = new ArrayList<>();
					final List<Object> lstObject = new ArrayList<>();
					multilingualIDList
							.add(objCustomerFile.getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()
									? "IDS_DELETECUSTOMERFILE"
									: "IDS_DELETECUSTOMERLINK");
					lstObject.add(objCustomerFile);
					auditUtilityFunction.fnInsertAuditAction(lstObject, 1, null, multilingualIDList, objUserInfo);
				} else {
					// status code:417
					return new ResponseEntity<>(commonFunction.getMultilingualMessage(
							Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
				}
			}
			return getCustomerFile(objCustomerFile.getNcustomercode(), objUserInfo);
		}else {
			// status code:417
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SUPPLIERALREADYDELETED",
					objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		}
	}
	
	/**
	 * This method is used to update entry in Invoicecustomermaster table. Need to
	 * validate that the Invoicecustomermaster object to be updated is active before
	 * updating details in database. Need to check for duplicate entry of
	 * Invoicecustomermaster name for the specified site before saving into database.
	 * Need to check that there should be only one default Invoicecustomermaster for
	 * a site
	 * 
	 * @param objInvoicecustomermaster [Invoicecustomermaster] object holding details
	 *                                to be updated in Invoicecustomermaster table
	 * @return response entity object holding response status and data of updated
	 *         Invoicecustomermaster object
	 * @throws Exception that are thrown from this DAO layer
	 */
	@SuppressWarnings("unused")
	@Override
	public ResponseEntity<Object> updateCustomerFile(final MultipartHttpServletRequest request, final UserInfo objUserInfo)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();

		final List<CustomerFile> lstCustomerFile = objMapper.readValue(request.getParameter("customerfile"),
				new TypeReference<List<CustomerFile>>() {
				});
		if (lstCustomerFile != null && lstCustomerFile.size() > 0) {
			final CustomerFile objCustomerFile = lstCustomerFile.get(0);
			final InvoiceCustomerMaster objInvoiceCustomerMaster = checKCustomerIsPresent(objCustomerFile.getNcustomercode(),objUserInfo);

			if (objInvoiceCustomerMaster != null) {
				final int isFileEdited = Integer.valueOf(request.getParameter("isFileEdited"));
				String sReturnString = Enumeration.ReturnStatus.SUCCESS.getreturnstatus();

				if (isFileEdited == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
					if (objCustomerFile.getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()) {
						sReturnString = ftpUtilityFunction.getFileFTPUpload(request, -1, objUserInfo);
					}
				}

				if (sReturnString.equals(Enumeration.ReturnStatus.SUCCESS.getreturnstatus())) {
					final String sQuery = "select * from customerfile where ncustomerfilecode = "
							+  objCustomerFile.getNcustomerfilecode() + " and nstatus = "
							+  Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and nsitecode="+objUserInfo.getNmastersitecode();
					final CustomerFile objTF = (CustomerFile) jdbcUtilityFunction.queryForObject(sQuery, CustomerFile.class,jdbcTemplate);

					final String sCheckDefaultQuery = "select * from Customerfile where nCustomercode = "
							+ objCustomerFile.getNcustomercode() + " and nCustomerfilecode!="
							+ objCustomerFile.getNcustomerfilecode() + " and nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and nsitecode="+objUserInfo.getNmastersitecode();
					final List<CustomerFile> lstDefCustomerFiles = (List<CustomerFile>) jdbcTemplate.query(sCheckDefaultQuery,
							new CustomerFile());

					if (objTF != null) {
						String ssystemfilename = "";
						if (objCustomerFile.getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()) {
							ssystemfilename = objCustomerFile.getSsystemfilename();
						}

						final String sUpdateQuery = "update customerfile set sfilename=N'"
								+ stringUtilityFunction.replaceQuote(objCustomerFile.getSfilename()) + "'," + " sdescription=N'"
								+ stringUtilityFunction.replaceQuote(objCustomerFile.getSdescription()) + "', ssystemfilename= N'"
								+ ssystemfilename + "'," + " nattachmenttypecode = "
								+ objCustomerFile.getNattachmenttypecode() + ", nlinkcode=" + objCustomerFile.getNlinkcode()
								+ "," + " nfilesize = " + objCustomerFile.getNfilesize() + ",dmodifieddate='"
								+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "' where nCustomerfilecode = "
								+ objCustomerFile.getNcustomerfilecode();
						objCustomerFile.setDcreateddate(objTF.getDcreateddate());
						jdbcTemplate.execute(sUpdateQuery);

						final List<String> multilingualIDList = new ArrayList<>();
						final List<Object> lstOldObject = new ArrayList<Object>();
						multilingualIDList
								.add(objCustomerFile.getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()
										? "IDS_EDITCUSTOMERFILE"
										: "IDS_EDITCUSTOMERLINK");
						lstOldObject.add(objTF);

						auditUtilityFunction.fnInsertAuditAction(lstCustomerFile, 2, lstOldObject, multilingualIDList, objUserInfo);
						return (getCustomerFile(objCustomerFile.getNcustomercode(), objUserInfo));
					} else {
						// status code:417
						return new ResponseEntity<>(commonFunction.getMultilingualMessage(
								Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
								objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
					}
				} else {
					// status code:417
					return new ResponseEntity<>(
							commonFunction.getMultilingualMessage(sReturnString, objUserInfo.getSlanguagefilename()),
							HttpStatus.EXPECTATION_FAILED);
				}
			} else {
				// status code:417
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_CUSTOMERALREADYDELETED",
						objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}
		} else {
			// status code:417
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_FILENOTFOUND", objUserInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}
	/**
	 * This method is used to update entry in Invoicecustomermaster table. Need to
	 * validate that the Invoicecustomermaster object to be updated is active before
	 * updating details in database. Need to check for duplicate entry of
	 * Invoicecustomermaster name for the specified site before saving into database.
	 * Need to check that there should be only one default Invoicecustomermaster for
	 * a site
	 * 
	 * @param objInvoicecustomermaster [Invoicecustomermaster] object holding details
	 *                                to be updated in Invoicecustomermaster table
	 * @return response entity object holding response status and data of updated
	 *         Invoicecustomermaster object
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> editCustomerFile(final CustomerFile objCustomerFile, final UserInfo objUserInfo)
			throws Exception {
		final String sEditQuery = "select tf.ncustomerfilecode, tf.ncustomercode, tf.nlinkcode, tf.nattachmenttypecode, tf.sfilename, tf.sdescription, tf.nfilesize,"
				+ " tf.ssystemfilename,  lm.jsondata->>'slinkname' as slinkname"
				+ " from customerfile tf, linkmaster lm where lm.nlinkcode = tf.nlinkcode" 
				+ " and tf.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +" and tf.nsitecode="+objUserInfo.getNmastersitecode()+""
				+ " and lm.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +" and lm.nsitecode="+objUserInfo.getNmastersitecode()+""
				+ " and tf.ncustomerfilecode = "+ objCustomerFile.getNcustomerfilecode();
		final CustomerFile objTF = (CustomerFile) jdbcUtilityFunction.queryForObject(sEditQuery, CustomerFile.class,jdbcTemplate);
		if (objTF != null) {
			return new ResponseEntity<Object>(objTF, HttpStatus.OK);
		} else {
					return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							objUserInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}
	/**
	 * This method is used to fetch the active Invoicecustomermaster objects for the
	 * specified Invoicecustomermaster name and site.
	 * 
	 * @param ntypecode       [String] name of the Invoicecustomermaster
	 * @param nmasterSiteCode [int] site code of the Invoicecustomermaster
	 * @return list of active Invoicecustomermaster code(s) based on the specified
	 *         Invoicecustomermaster name and site
	 * @throws Exception
	 */
	public ResponseEntity<Object> getCustomerFile(final int ncustomercode, final UserInfo objUserInfo) throws Exception {
		final Map<String, Object> outputMap = new HashMap<String, Object>();
		final String query = "select tf.noffsetdcreateddate,tf.ncustomerfilecode,(select  count(ncustomerfilecode) from customerfile where ncustomerfilecode>0 and ncustomercode = "
				+ ncustomercode + " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ ") as ncount,tf.sdescription,"
				+ " tf.ncustomerfilecode as nprimarycode,tf.sfilename,tf.ncustomercode,tf.ssystemfilename,"
				+ " tf.nattachmenttypecode,coalesce(at.jsondata->'sattachmenttype'->>'"
				+ objUserInfo.getSlanguagetypecode() + "',"
				+ "	at.jsondata->'sattachmenttype'->>'en-US') as sattachmenttype, case when tf.nlinkcode=-1 then '-' else lm.jsondata->>'slinkname'"
				+ " end slinkname, tf.nfilesize," + " case when tf.nattachmenttypecode= "
				+ Enumeration.AttachmentType.LINK.gettype() + " then '-' else" + " COALESCE(TO_CHAR(tf.dcreateddate,'"
				+ objUserInfo.getSpgdatetimeformat() + "'),'-') end  as screateddate, "
				+ " tf.nlinkcode, case when tf.nlinkcode = -1 then tf.nfilesize::varchar(1000) else '-' end sfilesize"
				+ " from customerfile tf,attachmenttype at, linkmaster lm  "
				+ " where at.nattachmenttypecode = tf.nattachmenttypecode "
				+ " and at.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and at.nsitecode="+objUserInfo.getNmastersitecode()+""
				+ " and lm.nlinkcode = tf.nlinkcode and lm.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and lm.nsitecode="+objUserInfo.getNmastersitecode()+""
				+ " and tf.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and tf.nsitecode="+objUserInfo.getNmastersitecode()+"" 
				+ " and tf.ncustomercode=" + ncustomercode
				+ " order by tf.ncustomerfilecode;";
		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());

		final List<CustomerFile> customerFile = jdbcTemplate.query(query, new CustomerFile());


		outputMap.put("customerFile", dateUtilityFunction.getSiteLocalTimeFromUTC(customerFile, Arrays.asList("screateddate"), null,
				objUserInfo, false, null, false));


		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}
	
	/**
	 * This method is used to checKCustomerIsPresent the active Invoicecustomermaster objects for the
	 * specified Invoicecustomermaster name and site.
	 * 
	 * @param ntypecode       [String] name of the Invoicecustomermaster
	 * @param nmasterSiteCode [int] site code of the Invoicecustomermaster
	 * @return list of active Invoicecustomermaster code(s) based on the specified
	 *         Invoicecustomermaster name and site
	 * @throws Exception
	 */
	public InvoiceCustomerMaster checKCustomerIsPresent(final int ncustomercode,final UserInfo userInfo) throws Exception {
		final String strQuery = "select ncustomercode from invoicecustomermaster where ncustomercode = " + ncustomercode + " and  nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and nsitecode="+userInfo.getNmastersitecode();
		final InvoiceCustomerMaster objInvoiceCustomerMaster = (InvoiceCustomerMaster) jdbcUtilityFunction.queryForObject(strQuery, InvoiceCustomerMaster.class,jdbcTemplate);
		return objInvoiceCustomerMaster;
	}
	/**
	 * This method is used to view file from invoiceproductfile table. * @return
	 * response entity object holding response status and data of updated
	 * Invoicecustomermaster object
	 * 
	 * @param ntypecode       [String] name of the Invoicecustomermaster
	 * @param nmasterSiteCode [int] site code of the Invoicecustomermaster
	 * @return list of active Invoicecustomermaster code(s) based on the specified
	 *         Invoicecustomermaster name and site
	 * @throws Exception
	 */
	@Override
	@SuppressWarnings("unused")
	public Map<String, Object> viewAttachedCustomerFile(final CustomerFile objCustomerFile, final UserInfo objUserInfo) throws Exception {
		Map<String, Object> map = new HashMap<>();
		
		final InvoiceCustomerMaster objInvoiceCustomerMaster = checKCustomerIsPresent(objCustomerFile.getNcustomercode(),objUserInfo);
		if (objCustomerFile != null) {

			String sQuery = "select * from customerfile where ncustomerfilecode = " + objCustomerFile.getNcustomerfilecode()
					+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and nsitecode="+objUserInfo.getNmastersitecode();
			final CustomerFile objTF = (CustomerFile) jdbcUtilityFunction.queryForObject(sQuery, CustomerFile.class,jdbcTemplate);
			if (objTF != null) {
				if (objTF.getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()) {
					map = ftpUtilityFunction.FileViewUsingFtp(objTF.getSsystemfilename(), -1, objUserInfo, "", "");// Folder Name - master
				} else {
					sQuery = "select jsondata->>'slinkname' as slinkname from linkmaster where nlinkcode="
							+ objTF.getNlinkcode() + " and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
							+ " and nsitecode="+objUserInfo.getNmastersitecode();
					LinkMaster objlinkmaster = (LinkMaster) jdbcUtilityFunction.queryForObject(sQuery, LinkMaster.class,jdbcTemplate);
					map.put("AttachLink", objlinkmaster.getSlinkname() + objTF.getSfilename());
					objCustomerFile.setScreateddate(null);
				}
				final List<String> multilingualIDList = new ArrayList<>();
				final List<Object> lstObject = new ArrayList<>();
				multilingualIDList
						.add(objCustomerFile.getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()
								? "IDS_VIEWCUSTOMERFILE"
								: "IDS_VIEWCUSTOMERLINK");
				lstObject.add(objCustomerFile);
				auditUtilityFunction.fnInsertAuditAction(lstObject, 1, null, multilingualIDList, objUserInfo);
			} else {
				map.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(), commonFunction.getMultilingualMessage(
						Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(), objUserInfo.getSlanguagefilename()));
				return map;
			}
		}
		return map;
	}


	/**
	 * This method is used to add a new entry to Invoicecustomermaster table. Need to
	 * check for duplicate entry of Invoicecustomermaster name for the specified site
	 * before saving into database. Need to check that there should be only one
	 * default Invoicecustomermaster for a site
	 * 
	 * @param objInvoicecustomermaster [Invoicecustomermaster] object holding details
	 *                                to be added in Invoicecustomermaster table
	 * @return inserted Invoicecustomermaster object and HTTP Status on successive
	 *         insert otherwise corresponding HTTP Status
	 * @throws Exception that are thrown from this DAO layer
	 */
	@SuppressWarnings({ "unchecked", "unused" })
	@Override
	public ResponseEntity<Object> createControlRights(final UserInfo userInfo,final UserRoleFieldControl userroleController, final List<UsersRoleField> lstusersrolescreen,final int nflag, final int nneedrights) throws Exception {
		final List<Object> savedControlRightsList = new ArrayList<>();
		final List<Object> beforeControlRightsList = new ArrayList<>();
		 List<UserRoleScreenControl> lstBeforeSave=new ArrayList<>();
		 final Map<String,Object> objMap=new HashMap<>();
		 final List<String> columnids=new ArrayList<>();
		 
		 final int field= lstusersrolescreen.get(0).getNuserrolefieldcode();
		if(nflag==1) {
		   final String querys= " Select * from  UserRoleFieldControl  where nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				    		 
			// status code:200
		   final UserRoleFieldControl	lstBeforeSaveUserRoleScreenControl=  (UserRoleFieldControl) jdbcTemplate.queryForObject(querys, UserRoleFieldControl.class);
			if(lstBeforeSaveUserRoleScreenControl != null) {
			final String query="update userrolefieldcontrol set dmodifieddate='"+dateUtilityFunction.getCurrentDateTime(userInfo)+"', nneedrights="+nneedrights+" where "
					           + " nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and "
					           + " nuserrolefieldcontrolcode="+userroleController.getNuserrolefieldcontrolcode();
			jdbcTemplate.execute(query);
			savedControlRightsList.add(userroleController);
			beforeControlRightsList.add(lstBeforeSaveUserRoleScreenControl);
			final String std="select * from fieldmaster where nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and "
					 +"nfieldcode="+lstBeforeSaveUserRoleScreenControl.getNfieldcode();
			final FieldMaster	lstBeforeSaveUserRoleScreenControls=  (FieldMaster)  jdbcTemplate.queryForObject(std, FieldMaster.class,jdbcTemplate);
			

			final String Fieldname=lstBeforeSaveUserRoleScreenControls.getSfieldname();
			if(nneedrights==3) {
				columnids.add("IDS_ENABLECONTROL");
			}else {
				columnids.add("IDS_DISABLECONTROL");
			}
			auditUtilityFunction.fnInsertAuditAction(savedControlRightsList,2,beforeControlRightsList,columnids,userInfo);
			final String suserrolescreencode=stringUtilityFunction.fnDynamicListToString(lstusersrolescreen,"getNuserrolefieldcode");
			objMap.putAll((Map<String, Object>) getControlMaster(userInfo, suserrolescreencode).getBody());
			final Connection c = jdbcTemplate.getDataSource().getConnection();
			final PreparedStatement ps = c.prepareStatement("SELECT * FROM invoicecustomermaster");
			final ResultSet rs = ps.executeQuery();
			final ResultSetMetaData rsmd = rs.getMetaData();
			final String name = rsmd.getColumnName(1);
				
			final String sotherdetails= rsmd.getColumnName(12);
				  
				  final String strquery= " Select * from  UserRoleFieldControl  where nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and "
			    		  + " nuserrolefieldcontrolcode="+lstBeforeSaveUserRoleScreenControl.getNuserrolefieldcontrolcode();
		// status code:200
			    final UserRoleFieldControl	lstAfterSaveUserRoleScreenControl=  (UserRoleFieldControl) jdbcTemplate.queryForObject(strquery, UserRoleFieldControl.class);
			    final int nneedright=  lstAfterSaveUserRoleScreenControl  .getNneedrights() ;
				  if(Fieldname.equals(sotherdetails)) {
					  objMap.put("needrights", lstAfterSaveUserRoleScreenControl.getNneedrights());
					  objMap.put("formcode", lstAfterSaveUserRoleScreenControl.getNformcode());		} 
				 
				 
			return new ResponseEntity<>(objMap, HttpStatus.OK);
				}else {
					return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
				}
			}else {
				int needright=0;
				final String suserrolescreencode;
				suserrolescreencode=stringUtilityFunction.fnDynamicListToString(lstusersrolescreen,"getNuserrolefieldcode");
				lstBeforeSave= getControlRightsActiveID(userInfo,suserrolescreencode);
				
				if(lstBeforeSave.size()>0) {
					if(nneedrights==3) {
						columnids.add("IDS_ENABLEALLCONTROL");
						needright=4;
					}else {
						columnids.add("IDS_DISABLEALLCONTROL");
						needright=3;
					}
					
					final String query= " update  userrolefieldcontrol set dmodifieddate='"+dateUtilityFunction.getCurrentDateTime(userInfo)+"',nneedrights="+nneedrights+" where "
								+ " nuserrolefieldcontrolcode in (select us.nuserrolecontrolcode from userrolefield u,sitefieldmaster s,"
								+ " userrolefieldcontrol us where us.nneedrights="+needright+" and"
								+ " u.nuserrolecode=us.nuserrolecode and u.nuserrolesfieldcode in ("+suserrolescreencode+") and "
								+ " us.nformcode=u.nformcode and us.ncontrolcode=s.ncontrolcode and "
								+ " us.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
								+ " and  s.nformcode=u.nformcode and s.nsitecode="+userInfo.getNmastersitecode()+""
								+ " and s.nstatus= "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and "
								+ " u.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+")"
								+ " and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

					jdbcTemplate.execute(query);
					
					final List<UserRoleFieldControl> lstUserRoleScreenControl=(List<UserRoleFieldControl>) jdbcTemplate.queryForObject("select us.* from userrolefield u,"
							+ "sitefieldmaster s,userrolefieldcontrol us,controlmaster c where"
							+ " c.ncontrolcode=s.ncontrolcode and c.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
							+ " and u.nuserrolecode=us.nuserrolecode and u.nuserrolesfieldcode in ("+suserrolescreencode+") "
							+ " and us.nformcode=u.nformcode and us.ncontrolcode=s.ncontrolcode "
							+ " and us.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and  "
		                    + " s.nformcode=u.nformcode and s.nsitecode="+userInfo.getNmastersitecode()+" and  "
							+ " s.nstatus= "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and "
							+ " u.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(), UserRoleFieldControl.class);
					savedControlRightsList.add(lstUserRoleScreenControl);
					beforeControlRightsList.add(lstBeforeSave);
					
					auditUtilityFunction.fnInsertListAuditAction(savedControlRightsList, 2, beforeControlRightsList,	columnids, userInfo);
					objMap.putAll((Map<String, Object>) getControlMaster(userInfo, suserrolescreencode).getBody());
					return new ResponseEntity<>(objMap, HttpStatus.OK);
				}else {
					return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
				}
			}
	}
	/**
	 * This method is used to fetch the active Invoicecustomermaster objects for the
	 * specified Invoicecustomermaster name and site.
	 * 
	 * @param ntypecode       [String] name of the Invoicecustomermaster
	 * @param nmasterSiteCode [int] site code of the Invoicecustomermaster
	 * @return list of active Invoicecustomermaster code(s) based on the specified
	 *         Invoicecustomermaster name and site
	 * @throws Exception
	 */
	private ResponseEntity<Object> getControlMaster(final UserInfo userInfo, final String userRoleScreenCode) throws Exception {
		final Map<String,Object> objMap =new HashMap<String, Object>();
		  final String query = "select "
				+ " coalesce(qs.jsondata->'sdisplayname'->>'"+userInfo.getSlanguagetypecode()+"',"
				+ " qs.jsondata->'sdisplayname'->>'en-US') as screenname, "
				+ " coalesce(c.jsondata->'scontrolids'->>'"+userInfo.getSlanguagetypecode()+"',"
				+ " c.jsondata->'scontrolids'->>'en-US') as scontrolids, "
		   		+ " u.nuserrolefieldcode,us.nuserrolefieldcontrolcode ,s.nsitefieldcode,us.nneedrights,c.*,"
				+ " u.nuserrolecode from "
				+ " sitefieldmaster s,userrolefield u,fieldmaster c,qualisforms qs, userrolefieldcontrol us"
				+ "  where qs.nformcode=s.nformcode and s.nsitecode="
				+   userInfo.getNmastersitecode() + " and s.nformcode=u.nformcode "
				+ " and u.nuserrolefieldcode in("+ userRoleScreenCode + ") and "
				+ " s.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and u.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and qs.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and  c.nfieldcode=s.nfieldcode and c.nstatus="
				+   Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and c.nformcode=s.nformcode"
				+ " and s.nformcode=u.nformcode  and us.nformcode=u.nformcode and us.nfieldcode=s.nfieldcode "
				+ "  and u.nuserrolecode=us.nuserrolecode and us.nstatus= "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(); 
		  
		  final List<ControlMaster> controlmaster= jdbcTemplate.query(query, new ControlMaster());
		objMap.put("ControlRights", controlmaster);
		return new ResponseEntity<>(objMap, HttpStatus.OK);
	}
	/**
	 * This method is used to fetch the active Invoicecustomermaster objects for the
	 * specified Invoicecustomermaster name and site.
	 * 
	 * @param ntypecode       [String] name of the Invoicecustomermaster
	 * @param nmasterSiteCode [int] site code of the Invoicecustomermaster
	 * @return list of active Invoicecustomermaster code(s) based on the specified
	 *         Invoicecustomermaster name and site
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public List<UserRoleScreenControl> getControlRightsActiveID(final UserInfo userInfo, final String suserrolescreencode) throws Exception {
	final String query="select us.* from usersrolescreen u,sitecontrolmaster s,userrolescreencontrol us,controlmaster c"
			+ " where c.ncontrolcode=s.ncontrolcode and c.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
			+ " and u.nuserrolecode=us.nuserrolecode and u.nuserrolescreencode in ("+suserrolescreencode+")"
			+ " and us.nformcode=u.nformcode and us.ncontrolcode=s.ncontrolcode and"
			+ " us.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and"
			+ " s.nformcode=u.nformcode and s.nsitecode="+userInfo.getNmastersitecode()+""
			+ " and s.nstatus= "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
			+ " and u.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	final List<UserRoleScreenControl> lstUserRoleScreenControl= (List<UserRoleScreenControl>) jdbcTemplate.queryForObject(query, UserRoleScreenControl.class);
	 return   lstUserRoleScreenControl;
	}


}
