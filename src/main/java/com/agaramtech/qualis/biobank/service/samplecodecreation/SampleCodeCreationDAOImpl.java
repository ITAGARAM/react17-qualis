package com.agaramtech.qualis.biobank.service.samplecodecreation;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.agaramtech.qualis.biobank.model.SampleCodeCreation;
import com.agaramtech.qualis.global.AuditUtilityFunction;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.ProjectDAOSupport;
import com.agaramtech.qualis.global.StringUtilityFunction;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.global.ValidatorDel;
import com.agaramtech.qualis.product.model.Product;
import com.agaramtech.qualis.product.service.product.ProductDAO;

import lombok.RequiredArgsConstructor;

/**
 * This class is used to perform CRUD Operation on "samplecodecreation" table
 * by implementing methods from its interface.
 */

@RequiredArgsConstructor
@Repository
public class SampleCodeCreationDAOImpl implements SampleCodeCreationDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(SampleCodeCreationDAOImpl.class);

	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private ValidatorDel valiDatorDel;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final ProjectDAOSupport projectDAOSupport;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;
	private final ProductDAO productDAO;

	/**
	 * This method is used to retrieve list of all active samplecodecreation for
	 * the specified site.
	 * 
	 * @param userInfo [UserInfo] nmasterSiteCode [int] primary key of site object
	 *                 for which the list is to be fetched
	 * @return response entity object holding response status and list of all active
	 *         samplecodecreation
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> getSampleCodeCreation(final UserInfo userInfo) throws Exception {
		final String strQuery = "select  pm.nproductcodemappingcode, pm.nproductcode, pm.sproductcode, p.sproductname "
				+ "from productcodemapping pm,product p where pm.nproductcode=p.nproductcode and pm.nproductcodemappingcode>0 "
				+ "and pm.nsitecode = " +userInfo.getNmastersitecode()+" "
				+ "and p.nsitecode = " + userInfo.getNmastersitecode()+" "
				+ "and pm.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
				+ "and p.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		LOGGER.info("getSampleCodeCreation -->" + strQuery);
		
		return new ResponseEntity<Object>(jdbcTemplate.query(strQuery, new SampleCodeCreation()), HttpStatus.OK);
	}

	/**
	 * This method is used to retrieve active samplecodecreation object based on
	 * the specified nsamplecodecreationcode.
	 * 
	 * @param nsamplecodecreationcode [int] primary key of samplecodecreation object
	 * @param userInfo [UserInfo] holding logged in user details based on 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of
	 *         samplecodecreation object
	 * @throws Exception that are thrown from this DAO layer
	 */

	@Override
	public SampleCodeCreation getActiveSampleCodeCreationById(final int nsamplecodecreationcode, final UserInfo userInfo)
			throws Exception {
		final String strQuery = "select  pm.nproductcodemappingcode, pm.nproductcode, pm.sproductcode, p.sproductname "
				+ "from productcodemapping pm,product p where pm.nproductcode=p.nproductcode and pm.nproductcodemappingcode>0 and nproductcodemappingcode="+nsamplecodecreationcode+" "
				+ "and pm.nsitecode = " +userInfo.getNmastersitecode()+" "
				+ "and p.nsitecode = " + userInfo.getNmastersitecode()+" "
				+ "and pm.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
				+ "and p.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

		return (SampleCodeCreation) jdbcUtilityFunction.queryForObject(strQuery, SampleCodeCreation.class,
				jdbcTemplate);
	}
	
	/**
	 * This method is used to add a new entry to samplecodecreation table. Need to
	 * check for duplicate entry of Sample Type Name and Code for the specified
	 * ProjectType before saving into database.
	 * 
	 * @param objSampleCodeCreation [SampleCodeCreation] object holding details to
	 *                              be added in samplecodecreation table
	 * @param userInfo [UserInfo] holding logged in user details based on 
	 * 							  which the list is to be fetched
	 * @return inserted sampleCodeCreation object with HTTP Status
	 * @throws Exception that are thrown from this DAO layer
	 */
	
	@Override
	public ResponseEntity<Object> getProduct(final UserInfo userInfo) {
		// TODO Auto-generated method stub
		final String strQuery = "SELECT nproductcode, sproductname "
				+ "FROM product where  nproductcode not in (select nproductcode from"
				+ " productcodemapping where nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
				+ "and  nsitecode="+userInfo.getNmastersitecode()+") "
				+ "and nsitecode = " + userInfo.getNmastersitecode()+" and nproductcode>0 "
				+ "and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

		LOGGER.info("getProduct-->" + strQuery);
		return new ResponseEntity<Object>(jdbcTemplate.query(strQuery, new SampleCodeCreation()), HttpStatus.OK);
	}

	/**
	 * This method is used to add a new entry to samplecodecreation table. Need to
	 * check for duplicate entry of Sample Type Name and Code for the specified
	 * ProjectType before saving into database.
	 * 
	 * @param objSampleCodeCreation [SampleCodeCreation] object holding details to
	 *                              be added in samplecodecreation table
	 * @param userInfo [UserInfo] holding logged in user details based on 
	 * 							  which the list is to be fetched
	 * @return inserted sampleCodeCreation object with HTTP Status
	 * @throws Exception that are thrown from this DAO layer
	 */

	@Override
	public ResponseEntity<Object> createSampleCodeCreation(final SampleCodeCreation objSampleCodeCreation,
			final UserInfo userInfo) throws Exception {

		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> savedStudyIdentity = new ArrayList<>();
			final Product productResponse = productDAO.getActiveProductById(objSampleCodeCreation.getNproductcode(),
					userInfo);
			if (productResponse == null) {
				final String projecttype = commonFunction.getMultilingualMessage("IDS_SAMPLETYPE",
						userInfo.getSlanguagefilename());

				return new ResponseEntity<>(projecttype + " "
						+ commonFunction
								.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
										userInfo.getSlanguagefilename())
								.toLowerCase(),
						HttpStatus.CONFLICT);
			} else {
				final SampleCodeCreation sampleCodeCreation = getSampleCodeCreation( objSampleCodeCreation.getNproductcode(),
						objSampleCodeCreation.getSproductcode(), userInfo.getNmastersitecode(),
						objSampleCodeCreation.getNproductcodemappingcode());
				if (sampleCodeCreation != null && sampleCodeCreation.isSampletypeExists() == false && sampleCodeCreation.isCodeExists() == false) {
					final String sQuery = " lock  table productcodemapping "
							+ Enumeration.ReturnStatus.EXCLUSIVEMODE.getreturnstatus();
					jdbcTemplate.execute(sQuery);
					final String sequencenoquery = "select nsequenceno from seqnoproductmanagement where stablename ='productcodemapping' and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
					int nsequenceno = jdbcTemplate.queryForObject(sequencenoquery, Integer.class);
					nsequenceno++;
					final String insertquery = "INSERT INTO productcodemapping("
							+ "	nproductcodemappingcode, nproductcode, sproductcode, dmodifieddate, nsitecode, nstatus)"
							+ "	VALUES ("+nsequenceno+", "+objSampleCodeCreation.getNproductcode()+",'"+stringUtilityFunction.replaceQuote(objSampleCodeCreation.getSproductcode())+"', '"+dateUtilityFunction.getCurrentDateTime(userInfo)+"', "+userInfo.getNmastersitecode()+", "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+");";
					jdbcTemplate.execute(insertquery);
					final String updatequery = "update seqnoproductmanagement set nsequenceno =" + nsequenceno
							+ " where stablename ='productcodemapping'";
					jdbcTemplate.execute(updatequery);
					savedStudyIdentity.add(objSampleCodeCreation);
					multilingualIDList.add("IDS_ADDSAMPLECODECREATION");
					auditUtilityFunction.fnInsertAuditAction(savedStudyIdentity, 1, null, multilingualIDList, userInfo);
					return getSampleCodeCreation(userInfo);

				} else {
					String alert = "";
					final boolean isSampleTypeExists = sampleCodeCreation.isSampletypeExists();
					final boolean isCodeExists = sampleCodeCreation.isCodeExists();
					if (isSampleTypeExists == true && isCodeExists== true ) {
						alert = commonFunction.getMultilingualMessage("IDS_SAMPLETYPE", userInfo.getSlanguagefilename())
								+ " and "
								+ commonFunction.getMultilingualMessage("IDS_CODE", userInfo.getSlanguagefilename());
					}else if(isSampleTypeExists== true) {
						alert = commonFunction.getMultilingualMessage("IDS_SAMPLETYPE", userInfo.getSlanguagefilename());
					}else if(isCodeExists== true) {
						alert = commonFunction.getMultilingualMessage("IDS_CODE", userInfo.getSlanguagefilename());
					}
					return new ResponseEntity<>(alert + " "
							+ commonFunction
									.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
											userInfo.getSlanguagefilename())
									.toLowerCase(),
							HttpStatus.CONFLICT);
				}
			}
		}
	

	/**
	 * This method is used to update entry in samplecodecreation table. Need to
	 * validate that the samplecodecreation object to be updated is active before
	 * updating details in database. Need to check for duplicate entry of sample
	 * type and code for the specified project type before saving into database.
	 * 
	 * @param objSampleCodeCreation [SampleCodeCreation] object holding details
	 *                                to be updated in samplecodecreation table
	 * @param userInfo [UserInfo] holding logged in user details based on 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of updated
	 *         samplecodecreation object
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> updateSampleCodeCreation(final SampleCodeCreation objSampleCodeCreation,
			final UserInfo userInfo) throws Exception {
		final SampleCodeCreation objsamplecodecreation = getActiveSampleCodeCreationById(
				objSampleCodeCreation.getNproductcodemappingcode(), userInfo);
		
		if (objsamplecodecreation == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			
				final Product productResponse = productDAO
						.getActiveProductById(objSampleCodeCreation.getNproductcode(), userInfo);
				if (productResponse == null) {
					final String projecttype = commonFunction.getMultilingualMessage("IDS_SAMPLETYPE",
							userInfo.getSlanguagefilename());
					return new ResponseEntity<>(projecttype + " "
							+ commonFunction
									.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
											userInfo.getSlanguagefilename())
									.toLowerCase(),
							HttpStatus.CONFLICT);
				} else {
					final SampleCodeCreation sampleCodeCreation = getSampleCodeCreation(
							 objSampleCodeCreation.getNproductcode(),
							objSampleCodeCreation.getSproductcode(), userInfo.getNmastersitecode(),
							objSampleCodeCreation.getNproductcodemappingcode());
					if (sampleCodeCreation != null && sampleCodeCreation.isSampletypeExists() == false && sampleCodeCreation.isCodeExists() == false) {
						final String updateQueryString = "UPDATE productcodemapping SET"
						+ " nproductcode="+objSampleCodeCreation.getNproductcode()+", "
						+ "sproductcode='"+stringUtilityFunction.replaceQuote(objSampleCodeCreation.getSproductcode())+"', dmodifieddate='"+dateUtilityFunction.getCurrentDateTime(userInfo)+"' "
						+ "WHERE nproductcodemappingcode="+objSampleCodeCreation.getNproductcodemappingcode()+" and  nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +";";

						jdbcTemplate.execute(updateQueryString);
						final List<String> multilingualIDList = new ArrayList<>();
						multilingualIDList.add("IDS_EDITSAMPLECODECREATION");
						final List<Object> listAfterSave = new ArrayList<>();
						listAfterSave.add(objSampleCodeCreation);
						final List<Object> listBeforeSave = new ArrayList<>();
						listBeforeSave.add(objsamplecodecreation);
						auditUtilityFunction.fnInsertAuditAction(listAfterSave, 2, listBeforeSave, multilingualIDList,
								userInfo);
						return getSampleCodeCreation(userInfo);

					} else {
						String alert = "";
						final boolean isSampleTypeExists = sampleCodeCreation.isSampletypeExists();
						final boolean isCodeExists = sampleCodeCreation.isCodeExists();
						if (isSampleTypeExists == true && isCodeExists== true ) {
							alert = commonFunction.getMultilingualMessage("IDS_SAMPLETYPE", userInfo.getSlanguagefilename())
									+ " and "
									+ commonFunction.getMultilingualMessage("IDS_CODE", userInfo.getSlanguagefilename());
						}else if(isSampleTypeExists== true) {
							alert = commonFunction.getMultilingualMessage("IDS_SAMPLETYPE", userInfo.getSlanguagefilename());
						}else if(isCodeExists== true) {
							alert = commonFunction.getMultilingualMessage("IDS_CODE", userInfo.getSlanguagefilename());
						}
						return new ResponseEntity<>(alert + " "
								+ commonFunction
										.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
												userInfo.getSlanguagefilename())
										.toLowerCase(),
								HttpStatus.CONFLICT);
					}
				}
			}
		}
	

	/**
	 * This method id used to delete an entry in samplecodecreation table.
	 * 
	 * @param objSampleCodeCreation [SampleCodeCreation] an Object holds the
	 *                              record to be deleted
	 * @param userInfo [UserInfo] holding logged in user details based on 
	 * 							  which the list is to be fetched
	 * @return a response entity with corresponding HTTP status and an
	 *         samplecodecreation object
	 * @exception Exception that are thrown from this DAO layer
	 */

	@Override
	public ResponseEntity<Object> deleteSampleCodeCreation(final SampleCodeCreation objSampleCodeCreation,
			final UserInfo userInfo) throws Exception {
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> savedSampleCodeCreation = new ArrayList<>();
		final SampleCodeCreation sampleCodeCreation = getActiveSampleCodeCreationById(
				objSampleCodeCreation.getNproductcodemappingcode(), userInfo);
		//final boolean validRecord = true;
		if (sampleCodeCreation == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
//			final String query = "";
//			valiDatorDel = projectDAOSupport.getTransactionInfo(query, userInfo);
//			validRecord = false;
//			if (valiDatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
//				validRecord = true;
//				valiDatorDel = projectDAOSupport.validateDeleteRecord(
//						Integer.toString(sampleCodeCreation.getNproductcodemappingcode()), userInfo);
//				if (valiDatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
//					validRecord = true;
//				} else {
//					validRecord = false;
//				}
//			}
//			if (validRecord) {
				final String updateQueryString = "update productcodemapping set nstatus = "
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ",dmodifieddate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' "
						+ "WHERE nproductcodemappingcode="+objSampleCodeCreation.getNproductcodemappingcode()+" and  nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +";";
				jdbcTemplate.execute(updateQueryString);
				objSampleCodeCreation.setNstatus((short) Enumeration.TransactionStatus.DELETED.gettransactionstatus());
				savedSampleCodeCreation.add(objSampleCodeCreation);
				multilingualIDList.add("IDS_DELETESAMPLECODECREATION");
				auditUtilityFunction.fnInsertAuditAction(savedSampleCodeCreation, 1, null, multilingualIDList,
						userInfo);
				return getSampleCodeCreation(userInfo);
//			} else {
//				return new ResponseEntity<>(valiDatorDel.getSreturnmessage(), HttpStatus.EXPECTATION_FAILED);
//			}
		}
	}

	/**
	 * This method is used to retrieve active samplecodecreation object based on
	 * the specified sampletype, code, nsamplecodecreationcode and site.
	 * @return response entity object holding response status and data of
	 *         SampleCodeCreation object
	 * @throws Exception that are thrown from this DAO layer
	 */
	private SampleCodeCreation getSampleCodeCreation(final int productcode, final String scode,
			final int nmastersitecode, final int nsamplecodecreationcode) throws Exception {
		String ssamplecodecreationQuery = "";

		if (nsamplecodecreationcode != 0) {
			ssamplecodecreationQuery = " and nproductcodemappingcode <> " + nsamplecodecreationcode + "";
		}
		final String strQuery = "SELECT"
				+ " CASE WHEN EXISTS (SELECT sproductcode  FROM productcodemapping WHERE LOWER(sproductcode)=LOWER('"+scode+"') and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and  nsitecode=" + nmastersitecode
				+ " " + ssamplecodecreationQuery + ")" + " THEN true  ELSE false"
				+ " END AS isCodeExists,"
				+ " CASE WHEN EXISTS (SELECT nproductcode  FROM productcodemapping WHERE nproductcode="+productcode+" and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and  nsitecode=" + nmastersitecode
				+ " " + ssamplecodecreationQuery + ")" + " THEN true  ELSE false"
				+ " END AS isSampletypeExists";
		return (SampleCodeCreation) jdbcUtilityFunction.queryForObject(strQuery, SampleCodeCreation.class,
				jdbcTemplate);
	}


}
