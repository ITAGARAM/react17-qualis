package com.agaramtech.qualis.biobank.service.samplecodecreation;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.agaramtech.qualis.biobank.model.SampleCodeCreation;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.UserInfo;

/**
 * This class holds methods to perform CRUD operation on 'samplecodecreation'
 * table through its DAO layer.
 */
@Transactional(readOnly = true, rollbackFor = Exception.class)
@Service
public class SampleCodeCreationServiceImpl implements SampleCodeCreationService {

	private final SampleCodeCreationDAO sampleCodeCreationDAO;

	private final CommonFunction commonFunction;
	
	/**
	 * This constructor injection method is used to pass the object dependencies to the class properties.
	 * @param sampleCodeCreationDAO sampleCodeCreationDAO Interface
	 * @param commonFunction CommonFunction holding common utility functions
	 */
	public SampleCodeCreationServiceImpl(final SampleCodeCreationDAO sampleCodeCreationDAO,
			final CommonFunction commonFunction) {
		super();
		this.sampleCodeCreationDAO = sampleCodeCreationDAO;
		this.commonFunction = commonFunction;
	}

	/**
	 * This method is used to retrieve list of all active samplecodecreation for
	 * the specified site.
	 * 
	 * @param userInfo [UserInfo] primary key of site object for which the list is
	 *                 to be fetched
	 * @return response entity object holding response status and list of all active
	 *         samplecodecreation
	 * @throws Exception that are thrown from this Service layer
	 */
	@Override
	public ResponseEntity<Object> getSampleCodeCreation(final UserInfo userInfo) throws Exception {
		return sampleCodeCreationDAO.getSampleCodeCreation(userInfo);
	}

	/**
	 * This method is used to retrieve active samplecodecreation object based on
	 * the specified nsamplecodecreationcode.
	 * 
	 * @param nsamplecodecreationcode [int] primary key of samplecodecreation
	 *                                  object
	 * @param userInfo [UserInfo] holding logged in user details based on 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of
	 *         samplecodecreation object
	 * @throws Exception that are thrown from this Service layer
	 */
	@Override
	public ResponseEntity<Object> getActiveSampleCodeCreationById(final int nsamplecodecreationcode,
			final UserInfo userInfo) throws Exception {
		final SampleCodeCreation objSampleCodeCreation = sampleCodeCreationDAO
				.getActiveSampleCodeCreationById(nsamplecodecreationcode, userInfo);
		if (objSampleCodeCreation == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			return new ResponseEntity<>(objSampleCodeCreation, HttpStatus.OK);
		}
	}

	/**
	 * This method is used to add a new entry to samplecodecreation table.
	 * 
	 * @param objSampleCodeCreation [SampleCodeCreation] object holding details
	 *                                to be added in samplecodecreation table
	 * @param userInfo [UserInfo]
	 * @return inserted samplecodecreation object with HTTP Status;
	 * @throws Exception that are thrown from this Service layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> createSampleCodeCreation(final SampleCodeCreation objSampleCodeCreation,
			final UserInfo userInfo) throws Exception {
		return sampleCodeCreationDAO.createSampleCodeCreation(objSampleCodeCreation, userInfo);
	}

	/**
	 * This method is used to update entry in samplecodecreation table.
	 * 
	 * @param objSampleCodeCreation [SampleCodeCreation] object holding details
	 *                                to be updated in samplecodecreation table
	 * @param userInfo [UserInfo]
	 * @return response entity object holding response status and data of updated
	 *         samplecodecreation object
	 * @throws Exception that are thrown from this Service layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> updateSampleCodeCreation(final SampleCodeCreation objSampleCodeCreation,
			final UserInfo userInfo) throws Exception {
		return sampleCodeCreationDAO.updateSampleCodeCreation(objSampleCodeCreation, userInfo);
	}

	/**
	 * This method id used to delete an entry in samplecodecreation table
	 * 
	 * @param objSampleCodeCreation [SampleCodeCreation] an Object holds the
	 *                                record to be deleted
	 * @param userInfo [UserInfo]
	 * @return a response entity with corresponding HTTP status and an
	 *         samplecodecreation object
	 * @exception Exception that are thrown from this Service layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> deleteSampleCodeCreation(final SampleCodeCreation objSampleCodeCreation,
			final UserInfo userInfo) throws Exception {
		return sampleCodeCreationDAO.deleteSampleCodeCreation(objSampleCodeCreation, userInfo);
	}

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
	@Override
	public ResponseEntity<Object> getProduct(final UserInfo userInfo) {
		// TODO Auto-generated method stub
		return sampleCodeCreationDAO.getProduct(userInfo);
	}

}
