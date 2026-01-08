package com.agaramtech.qualis.configuration.service.awsstorageconfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.agaramtech.qualis.configuration.model.AWSStorageConfig;
import com.agaramtech.qualis.configuration.model.FTPConfig;
import com.agaramtech.qualis.global.AuditUtilityFunction;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.PasswordUtilityFunction;
import com.agaramtech.qualis.global.StringUtilityFunction;
import com.agaramtech.qualis.global.UserInfo;

import lombok.AllArgsConstructor;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListBucketsResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Repository
@AllArgsConstructor
public class AWSStorageConfigurationDAOimpl implements AWSStorageConfigurationDAO {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(AWSStorageConfigurationDAOimpl.class);

	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;
	private final PasswordUtilityFunction passwordUtilityFunction;
	
	
	/**
	 * This is method is get the all AWSCredentials with respect to default status records in the table
	 * @param nmasterSiteCode argument passed to get AWSCredentials with respect to site
	 * @return AWSStorageConfig object
	 * @throws Exception
	 */

	private AWSStorageConfig getAWSStorageConfigByStatus(final int nmasterSiteCode) throws Exception {
		final String strQuery = "Select * from awsstorageconfig where nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode=" + nmasterSiteCode
				+ " and ndefaultstatus=" + Enumeration.TransactionStatus.YES.gettransactionstatus();
		return (AWSStorageConfig) jdbcUtilityFunction.queryForObject(strQuery, AWSStorageConfig.class, jdbcTemplate);
	}
	
	/**
	 * This is method is get the all AWSCredentials with active nstatus records in the table
	 *@param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
     *              which the list is to be fetched
     * @return response entity  object holding response status and list of all active AWSCredentials
     * @throws Exception that are thrown from this DAO layer
	 */

	@Override
	public ResponseEntity<Object> getAWSStorageConfig(final UserInfo userInfo) throws Exception {
		
		final String strQuery = " select fc.nawsstorageconfigcode, fc.saccesskeyid,  fc.sbucketname, fc.sregion,"
				+ "fc.ndefaultstatus, coalesce(ts.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode()
				+ "'," + " ts.jsondata->'stransdisplaystatus'->>'en-US') as sdefaultstatus "
				+ " from awsstorageconfig fc,transactionstatus ts" + " where fc.ndefaultstatus=ts.ntranscode"
				+ " and  fc.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ts.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and fc.nsitecode="
				+ userInfo.getNmastersitecode();

		return new ResponseEntity<>((List<AWSStorageConfig>) jdbcTemplate.query(strQuery, new AWSStorageConfig()),
				HttpStatus.OK);

	}
	
	
    /**
     * This method is used to retrieve active AWSCredentials object based on the specified nawsstorageconfigcode
     * @param nawsstorageconfigcode [int] primary key of AWSStorageConfig object
     * @param userInfo [UserInfo] holding logged in user details based on 
	 * 							  which the list is to be fetched
	 * @return response entity  object holding response status and data of AWSStorageConfig  object
	 * @throws Exception that are thrown from this DAO layer
     */
	@Override
	public AWSStorageConfig getActiveAWSStorageConfigById(final int nawsstorageconfigcode, final UserInfo userInfo)
			throws Exception {
		
		

		final String strQuerybyid = " select fc.nawsstorageconfigcode, fc.saccesskeyid,fc.ssecretpasskey, fc.sbucketname, fc.sregion,"
				+ "fc.ndefaultstatus, coalesce(ts.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode()
				+ "'," + " ts.jsondata->'stransdisplaystatus'->>'en-US') as sdefaultstatus "
				+ " from awsstorageconfig fc,transactionstatus ts" + " where fc.ndefaultstatus=ts.ntranscode"

				+ " and  fc.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ts.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and fc.nsitecode="
				+ userInfo.getNmastersitecode() + " and fc.nawsstorageconfigcode=" + nawsstorageconfigcode + ";";

		final AWSStorageConfig awsStorageConfig = (AWSStorageConfig) jdbcUtilityFunction.queryForObject(strQuerybyid,
				AWSStorageConfig.class, jdbcTemplate);
		

		return awsStorageConfig;
	}
    
/**
 * This method is used to fetch the AWSStorageConfig object for the specified Accesskeyid name 
 * @param saccesskeyid [String] is the  unique key id  of AWSStorageConfig
 * @param nmasterSiteCode [int] site code of the AWSStorageConfigAWSStorageConfig
 * @return AWSStorageConfig object based on the specified Accesskeyid and site
 * @throws Exception that are thrown from this DAO layer
 */
	private AWSStorageConfig getAWSStorageByAccessId(final String saccesskeyid, int nmasterSiteCode) throws Exception {
		final String strQuery = "select nawsstorageconfigcode from awsstorageconfig where saccesskeyid = N'"
				+ stringUtilityFunction.replaceQuote(saccesskeyid) + "' and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode =" + nmasterSiteCode;
		return (AWSStorageConfig) jdbcUtilityFunction.queryForObject(strQuery, AWSStorageConfig.class, jdbcTemplate);

	}
/**
 * This method is used to check the AWSCredentials validate through S3 Client
 * @param awsStorageConfig object argument passed
 * @return return the map object
 */
	private Map<String, String> getCorrectCredentials(AWSStorageConfig awsStorageConfig) {
		
		Map<String, String> map = new HashMap<>();
        map.put("rtn", Enumeration.ReturnStatus.SUCCESS.getreturnstatus());

		final StaticCredentialsProvider credentialsProvider = StaticCredentialsProvider.create(
				AwsBasicCredentials.create(awsStorageConfig.getSaccesskeyid(), awsStorageConfig.getSsecretpasskey()));

		try (S3Client s3 = S3Client.builder().region(Region.of(awsStorageConfig.getSregion()))
				.credentialsProvider(credentialsProvider).build()) {

			ListBucketsResponse response = s3.listBuckets();

			System.out.println("Successfully connected to S3!");
			System.out.println("Found " + response.buckets().size() + " buckets.");
			response.buckets().forEach(b -> System.out.println(" - " + b.name()));
			
			
			if (!response.buckets().isEmpty()) { 
			    boolean chk = response.buckets().stream()
			            .anyMatch(b -> b.name().equals(awsStorageConfig.getSbucketname()));

			    if (!chk) {
			        map.put("rtn", "IDS_BUCKETNOTFOUND");
			    }
			    
			}else {
				 map.put("rtn", "IDS_BUCKETNOTFOUND");
				
			}
			

		} catch (S3Exception e) {
			System.err.println("AWS S3 connection failed: " + e.awsErrorDetails().errorMessage());
			map.put("rtn", "IDS_WRONGCREDENTIALS");
			
			
		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
			map.put("rtn", "IDS_WRONGCREDENTIALS");
			
		}
		return map;

	}
	
	
/**
 * This method is used to add new record in the AWSStorageconfig table
 * Accesskeyid is unique accross the table
 * Need to check for duplicate entry of Accesskeyid for the specified site before saving into database.
 * @param awsStorageConfig [AWSStorageConfig] object holding details to be added in AWSStorageConfig table
 * @param userInfo [UserInfo] holding logged in user details based on 
 * 							  which the list is to be fetched
 * @return saved AWSStorageConfig object with status code 200 if saved successfully else if the unit already exists, 
 * 			response will be returned as 'Already Exists' with status code 409
 * @throws Exception that are thrown from this DAO layer
 */
	@Override
	public ResponseEntity<Object> createAWSStorageConfig(AWSStorageConfig awsStorageConfig, final UserInfo userInfo)
			throws Exception {

		final AWSStorageConfig awsstoragebyaccessid = getAWSStorageByAccessId(awsStorageConfig.getSaccesskeyid(),
				awsStorageConfig.getNsitecode());

		if (awsstoragebyaccessid == null) {
			final  Map<String, String> mapcrtcredentials = getCorrectCredentials(awsStorageConfig);

			if (Enumeration.ReturnStatus.SUCCESS.getreturnstatus().equals(mapcrtcredentials.get("rtn"))) {
				
						
				final String sQuery = " lock  table awsstorageconfig "
						+ Enumeration.ReturnStatus.EXCLUSIVEMODE.getreturnstatus();
				jdbcTemplate.execute(sQuery);

				final List<String> multilingualIDList = new ArrayList<>();
				final List<Object> savedAWSList = new ArrayList<>();

				if (awsStorageConfig.getNdefaultstatus() == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
					final AWSStorageConfig defaultAWSStorageConfig = getAWSStorageConfigByStatus(
							awsStorageConfig.getNsitecode());
					if (defaultAWSStorageConfig != null && defaultAWSStorageConfig
							.getNawsstorageconfigcode() != awsStorageConfig.getNawsstorageconfigcode()) {

						final List<Object> oldDefaultAWS = new ArrayList<>();
						final List<Object> modifiedDefaultAWS = new ArrayList<>();
						final AWSStorageConfig newAWS = SerializationUtils.clone(defaultAWSStorageConfig);

						defaultAWSStorageConfig
								.setNdefaultstatus((short) Enumeration.TransactionStatus.NO.gettransactionstatus());
						final String updateQueryString = " update awsstorageconfig set ndefaultstatus="
								+ Enumeration.TransactionStatus.NO.gettransactionstatus() + ", dmodifieddate='"
								+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where nawsstorageconfigcode="
								+ defaultAWSStorageConfig.getNawsstorageconfigcode();
						jdbcTemplate.execute(updateQueryString);
						modifiedDefaultAWS.add(defaultAWSStorageConfig);
						oldDefaultAWS.add(newAWS);
					}
				}else {
					final AWSStorageConfig defaultAWSStorageConfig = getAWSStorageConfigByStatus(
							awsStorageConfig.getNsitecode());
					if (defaultAWSStorageConfig == null) {
						awsStorageConfig.setNdefaultstatus((short) Enumeration.TransactionStatus.YES.gettransactionstatus());
					}
				}

				String sequencequery = "select nsequenceno from SeqNoConfigurationMaster where stablename ='awsstorageconfig'";
				int nsequenceno = jdbcTemplate.queryForObject(sequencequery, Integer.class);
				nsequenceno++;

				final String insertquery = "INSERT INTO awsstorageconfig ("
						+ "nawsstorageconfigcode, saccesskeyid, ssecretpasskey, sbucketname, sregion, "
						+ "ndefaultstatus, dmodifieddate, nsitecode, nstatus) " + "VALUES (" + nsequenceno + ", " + "N'"
						+ stringUtilityFunction.replaceQuote(awsStorageConfig.getSaccesskeyid()) + "', " + "N'"
						+ stringUtilityFunction.replaceQuote(awsStorageConfig.getSsecretpasskey()) + "', " + "N'"
						+ stringUtilityFunction.replaceQuote(awsStorageConfig.getSbucketname()) + "', " + "N'"
						+ stringUtilityFunction.replaceQuote(awsStorageConfig.getSregion()) + "', "
						+ awsStorageConfig.getNdefaultstatus() + ", " + "'"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNmastersitecode()
						+ ", " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";

				jdbcTemplate.execute(insertquery);

				final String updatequery = "update SeqNoConfigurationMaster set nsequenceno=" + nsequenceno
						+ " where stablename ='awsstorageconfig'";
				jdbcTemplate.execute(updatequery);

				awsStorageConfig.setNawsstorageconfigcode(nsequenceno);
				
				passwordUtilityFunction.encryptPassword("awsstorageconfig", "nawsstorageconfigcode",awsStorageConfig.getNawsstorageconfigcode() , awsStorageConfig.getSsecretpasskey(), "ssecretpasskey");
				savedAWSList.add(awsStorageConfig);
				multilingualIDList.add("IDS_ADDAWSSTORAGECONFIG");

				auditUtilityFunction.fnInsertAuditAction(savedAWSList, 1, null, multilingualIDList, userInfo);

				return getAWSStorageConfig(userInfo);
				}
			
				
				
			else {
				
				
					return new ResponseEntity<>(
				
						commonFunction.getMultilingualMessage(mapcrtcredentials.get("rtn"), userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);

			}
			
			
		}

		else {

			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}

	}
/**
 * This method is used to update entry in AWSStorageConfig table.
 * Need to validate that the AWSStorageConfig object to be updated is active before updating 
 * details in database.
 *  Need to check for duplicate entry of Accesskeyid  for the specified site before saving into database.
 *  Need to check that there should be only one default AWSCredentials  for a site
 *  @param awsStorageConfig [AWSStorageConfig] object holding details to be updated in AWSStorageConfig table
 * @param userInfo [UserInfo] holding logged in user details based on 
 * 							  which the list is to be fetched
 * @return saved AWSStorageConfig object with status code 200 if saved successfully 
 * 			else if the AWSStorageConfig already exists, response will be returned as 'Already Exists' with status code 409
 *          else if the AWSStorageConfig to be updated is not available, response will be returned as 'Already Deleted' with status code 417
 * @throws Exception that are thrown from this DAO layer
 */
	@Override
	public ResponseEntity<Object> updateAWSStorageConfig(AWSStorageConfig awsStorageConfig,final  UserInfo userInfo)
			throws Exception {

		final AWSStorageConfig objAWSStorageConfig = getActiveAWSStorageConfigById(
				awsStorageConfig.getNawsstorageconfigcode(), userInfo);

		if (objAWSStorageConfig == null) {
			// status code:417
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			
			
			
				final String plainPassword = passwordUtilityFunction.decryptPassword("awsstorageconfig", "nawsstorageconfigcode",awsStorageConfig.getNawsstorageconfigcode() , "ssecretpasskey");
				awsStorageConfig.setSsecretpasskey(plainPassword);
				
			final List<String> multilingualIDList = new ArrayList<>();
			final List<Object> savedAWSList = new ArrayList<>();
			final List<Object> editedAWSList = new ArrayList<>();

			final String queryString = "select nawsstorageconfigcode from awsstorageconfig where saccesskeyid = N'"
					+ stringUtilityFunction.replaceQuote(awsStorageConfig.getSaccesskeyid())
					+ "' and nawsstorageconfigcode <> " + awsStorageConfig.getNawsstorageconfigcode()
					+ " and nsitecode =" + awsStorageConfig.getNsitecode() + " and  nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

			final AWSStorageConfig availableAwsStorageConfig = (AWSStorageConfig) jdbcUtilityFunction
					.queryForObject(queryString, AWSStorageConfig.class, jdbcTemplate);

			if (availableAwsStorageConfig == null) {

				final  Map<String, String> mapcrtcredentials = getCorrectCredentials(awsStorageConfig);

				 if (mapcrtcredentials.get("rtn").equals(Enumeration.ReturnStatus.SUCCESS.getreturnstatus())) {

					if (awsStorageConfig.getNdefaultstatus() == Enumeration.TransactionStatus.YES
							.gettransactionstatus()) {
						final AWSStorageConfig defaultAWSStorageConfig = getAWSStorageConfigByStatus(
								awsStorageConfig.getNsitecode());
						if (defaultAWSStorageConfig != null && defaultAWSStorageConfig
								.getNawsstorageconfigcode() != awsStorageConfig.getNawsstorageconfigcode()) {

							
							final AWSStorageConfig newAWS = SerializationUtils.clone(defaultAWSStorageConfig);

							defaultAWSStorageConfig
									.setNdefaultstatus((short) Enumeration.TransactionStatus.NO.gettransactionstatus());
							final String updateQueryString = " update awsstorageconfig set ndefaultstatus="
									+ Enumeration.TransactionStatus.NO.gettransactionstatus() + ", dmodifieddate='"
									+ dateUtilityFunction.getCurrentDateTime(userInfo)
									+ "' where nawsstorageconfigcode="
									+ defaultAWSStorageConfig.getNawsstorageconfigcode();
							jdbcTemplate.execute(updateQueryString);

							savedAWSList.add(defaultAWSStorageConfig);
							editedAWSList.add(newAWS);
						}
					}

					final String updateQueryString = "UPDATE awsstorageconfig SET " + "ndefaultstatus=" + awsStorageConfig.getNdefaultstatus()
							+ " WHERE nawsstorageconfigcode = " + awsStorageConfig.getNawsstorageconfigcode();

					jdbcTemplate.execute(updateQueryString);
					

					savedAWSList.add(awsStorageConfig);
					editedAWSList.add(objAWSStorageConfig);
					multilingualIDList.add("IDS_EDITAWSSTORAGECONFIG");

					auditUtilityFunction.fnInsertAuditAction(savedAWSList, 2, editedAWSList, multilingualIDList,
							userInfo);

					return getAWSStorageConfig(userInfo);
				} else {

					return new ResponseEntity<>(commonFunction.getMultilingualMessage(mapcrtcredentials.get("rtn"),
							userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);

				}

			}

			else {

				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.CONFLICT);
			}

		}

	}
/**
 * This method id used to delete an entry in AWSStorageConfig table
 * Need to check the record is already deleted or not
 * @param awsStorageConfig [AWSStorageConfig] an Object holds the record to be deleted
 * @param userInfo [UserInfo] holding logged in user details based on 
 * 							  which the list is to be fetched
 * @return a response entity with list of available AWSStorageConfig objects
 * @exception Exception that are thrown from this DAO layer
 */
	@Override
	public ResponseEntity<Object> deleteAWSStorageConfig(AWSStorageConfig awsStorageConfig,final UserInfo userInfo)
			throws Exception {
		final AWSStorageConfig objAWSStorageConfig = getActiveAWSStorageConfigById(
				awsStorageConfig.getNawsstorageconfigcode(), userInfo);
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> deletedAWSConfig = new ArrayList<>();

		if (objAWSStorageConfig == null) {
			// status code:417
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}

		else {

			final String deleteQuery = "update awsstorageconfig set nstatus="
					+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ", dmodifieddate='"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where nawsstorageconfigcode="
					+ awsStorageConfig.getNawsstorageconfigcode();
			awsStorageConfig.setNstatus((short) Enumeration.TransactionStatus.DELETED.gettransactionstatus());
			jdbcTemplate.execute(deleteQuery);

			deletedAWSConfig.add(objAWSStorageConfig);
			multilingualIDList.add("IDS_DELETEAWSSTORAGECONFIG");

			auditUtilityFunction.fnInsertAuditAction(deletedAWSConfig, 1, null, multilingualIDList, userInfo);

			return getAWSStorageConfig(userInfo);
		}

	}

}
