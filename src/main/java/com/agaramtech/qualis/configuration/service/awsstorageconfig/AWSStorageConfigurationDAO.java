package com.agaramtech.qualis.configuration.service.awsstorageconfig;

import org.springframework.http.ResponseEntity;

import com.agaramtech.qualis.configuration.model.AWSStorageConfig;
import com.agaramtech.qualis.global.UserInfo;

public interface AWSStorageConfigurationDAO  {
	/**
	 * This is method is get the all AWSCredentials with respect to default status records in the table
	 * @param nmasterSiteCode argument passed to get AWSCredentials with respect to site
	 * @return AWSStorageConfig object
	 * @throws Exception
	 */
	public ResponseEntity<Object> getAWSStorageConfig(final UserInfo userInfo) throws Exception;
	
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
	public ResponseEntity<Object> createAWSStorageConfig(AWSStorageConfig awsStorageConfig,final UserInfo userInfo) throws Exception;
	
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
    public ResponseEntity<Object> updateAWSStorageConfig(AWSStorageConfig awsStorageConfig,final UserInfo userInfo) throws Exception;

    
    /**
	 * This method id used to delete an entry in AWSStorageConfig table
	 * Need to check the record is already deleted or not
	 * @param awsStorageConfig [AWSStorageConfig] an Object holds the record to be deleted
	 * @param userInfo [UserInfo] holding logged in user details based on 
	 * 							  which the list is to be fetched
	 * @return a response entity with list of available AWSStorageConfig objects
	 * @exception Exception that are thrown from this DAO layer
	 */
	public ResponseEntity<Object> deleteAWSStorageConfig(AWSStorageConfig awsStorageConfig,final UserInfo userInfo) throws Exception;
	
	/**
     * This method is used to retrieve active AWSCredentials object based on the specified nawsstorageconfigcode
     * @param nawsstorageconfigcode [int] primary key of AWSStorageConfig object
     * @param userInfo [UserInfo] holding logged in user details based on 
	 * 							  which the list is to be fetched
	 * @return response entity  object holding response status and data of AWSStorageConfig  object
	 * @throws Exception that are thrown from this DAO layer
     */
	public AWSStorageConfig getActiveAWSStorageConfigById(final int nawsstorageconfigcode,final UserInfo userInfo) throws Exception;


}
