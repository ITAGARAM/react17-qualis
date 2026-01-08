package com.agaramtech.qualis.biobank.service.samplecodecreation;

import org.springframework.http.ResponseEntity;

import com.agaramtech.qualis.biobank.model.SampleCodeCreation;
import com.agaramtech.qualis.global.UserInfo;

/**
 * This interface declaration holds methods to perform CRUD operation on
 * 'samplecodecreation' table
 * 
 */
public interface SampleCodeCreationService {
	/**
	 * This interface declaration is used to get the over all samplecodecreation
	 * with respect to site
	 * 
	 * @param userInfo [UserInfo]
	 * @return a response entity which holds the list of samplecodecreation with
	 *         respect to site and also have the HTTP response code
	 * @throws Exception
	 */
	public ResponseEntity<Object> getSampleCodeCreation(final UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to retrieve active samplecodecreation
	 * object based on the specified ncollectiontubetypecode.
	 * 
	 * @param nsamplecodecreationcode [int] primary key of CollectionTubeType
	 *                                  object
	 * @param userInfo [UserInfo]
	 * @return response entity object holding response status and data of
	 *         CollectionTubeType object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getActiveSampleCodeCreationById(final int nsamplecodecreationcode,
			final UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to add a new entry to samplecodecreation
	 * table.
	 * 
	 * @param objSampleCodeCreation [SampleCodeCreation] object holding details
	 *                                to be added in samplecodecreation table
	 * @param userInfo [UserInfo]
	 * @return response entity object holding response status and data of added
	 *         samplecodecreation object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> createSampleCodeCreation(final SampleCodeCreation objSampleCodeCreation,
			final UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to update entry in samplecodecreation
	 * table.
	 * 
	 * @param objSampleCodeCreation [SampleCodeCreation] object holding details
	 *                                to be updated in samplecodecreation table
	 * @param userInfo [UserInfo]
	 * @return response entity object holding response status and data of updated
	 *         samplecodecreation object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> updateSampleCodeCreation(final SampleCodeCreation objSampleCodeCreation,
			final UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to delete entry in samplecodecreation
	 * table.
	 * 
	 * @param objSampleCodeCreation [SampleCodeCreation] object holding detail
	 *                                to be deleted in samplecodecreation table
	 * @param userInfo [UserInfo]
	 * @return response entity object holding response status and data of
	 *         samplecodecreation object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> deleteSampleCodeCreation(final SampleCodeCreation objSampleCodeCreation,
			final UserInfo userInfo) throws Exception;
	
	/**
	 * This interface declaration is used to retrieve active samplecodecreation
	 * object based on the specified ncollectiontubetypecode.
	 * 
	 * @param nsamplecodecreationcode [int] primary key of CollectionTubeType
	 *                                  object
	 * @param userInfo [UserInfo]
	 * @return response entity object holding response status and data of
	 *         CollectionTubeType object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getProduct(UserInfo userInfo);
}
