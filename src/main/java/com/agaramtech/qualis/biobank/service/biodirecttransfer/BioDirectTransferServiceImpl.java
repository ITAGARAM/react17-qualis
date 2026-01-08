package com.agaramtech.qualis.biobank.service.biodirecttransfer;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.agaramtech.qualis.global.UserInfo;

@Transactional(readOnly = true, rollbackFor = Exception.class)
@Service
public class BioDirectTransferServiceImpl implements BioDirectTransferService {

	private final BioDirectTransferDAO bioDirectTransferDAO;

	public BioDirectTransferServiceImpl(BioDirectTransferDAO bioDirectTransferDAO) {
		this.bioDirectTransferDAO = bioDirectTransferDAO;
	}

	/**
	 * This method is used to retrieve list of all active transfer form for the specified
	 * site.
	 * 
	 * @param inputMap parameter holds the values that required to fetch the transfer form records
	 * @param nbioDirectTransferCode parameter holds the values of primary key of transfer form
	 * @return response entity object holding response status and list of all active
	 *         transfer forms
	 * @throws Exception that are thrown from this DAO layer
	 */
	
	@Override
	public ResponseEntity<Object> getBioDirectTransfer(final Map<String, Object> inputMap,
			final int nbioDirectTransferCode, final UserInfo userInfo) throws Exception {
		return bioDirectTransferDAO.getBioDirectTransfer(inputMap, nbioDirectTransferCode, userInfo);
	}

	/**
	 * This method is used to retrieve active transfer form object based on the specified
	 * nbioDirectTransferCode.
	 * 
	 * @param nbioDirectTransferCode [int] primary key of transfer form object
	 * @return transfer form object
	 * @throws Exception that are thrown from this DAO layer
	 */
	
	@Override
	public ResponseEntity<Object> getActiveBioDirectTransfer(final int nbioDirectTransferCode, final UserInfo userInfo)
			throws Exception {
		return bioDirectTransferDAO.getActiveBioDirectTransfer(nbioDirectTransferCode, userInfo);
	}

	/**
	 * This method is used to retrieve active transfer type object
	 * 
	 * @return transfer type object
	 * @throws Exception that are thrown from this DAO layer
	 */
	
	@Override
	public ResponseEntity<Object> getTransferType(final UserInfo userInfo) throws Exception {
		return bioDirectTransferDAO.getTransferType(userInfo);
	}

	/**
	 * This method is used to retrieve active site object based on transfer type
	 * 
	 * @return site object
	 * @throws Exception that are thrown from this DAO layer
	 */
	
	@Override
	public ResponseEntity<Object> getSiteBasedOnTransferType(final UserInfo userInfo) throws Exception {
		return bioDirectTransferDAO.getSiteBasedOnTransferType(userInfo);
	}

	/**
	 * This method is used to retrieve active project object based on site
	 * @param nbioBankSiteCode holds the primary value of the site
	 * @return project object
	 * @throws Exception that are thrown from this DAO layer
	 */
	
	@Override
	public ResponseEntity<Object> getProjectBasedOnSite(final int nbioBankSiteCode, final UserInfo userInfo)
			throws Exception {
		return bioDirectTransferDAO.getProjectBasedOnSite(nbioBankSiteCode, userInfo);
	}

	/**
	 * This method is used to retrieve active parent sample object based on project
	 * @param nbioProjectCode holds the primary value of the project
	 * @return parent sample object
	 * @throws Exception that are thrown from this DAO layer
	 */
	
	@Override
	public ResponseEntity<Object> getParentSampleBasedOnProject(final int nbioProjectCode, final UserInfo userInfo)
			throws Exception {
		return bioDirectTransferDAO.getParentSampleBasedOnProject(nbioProjectCode, userInfo);
	}

	/**
	 * This method is used to retrieve active storage type object
	 * 
	 * @return parent storage type object
	 * @throws Exception that are thrown from this DAO layer
	 */
	
	@Override
	public ResponseEntity<Object> getStorageType(final UserInfo userInfo) throws Exception {
		return bioDirectTransferDAO.getStorageType(userInfo);
	}

	/**
	 * This method is used to retrieve active sample type object
	 * @param inputMap holds the primary value of sample code in the key of sparentsamplecode
	 * @return parent sample type object
	 * @throws Exception that are thrown from this DAO layer
	 */
	
	@Override
	public ResponseEntity<Object> getSampleTypeBySampleCode(final Map<String,Object> inputMap,
			final UserInfo userInfo) throws Exception {
		return bioDirectTransferDAO.getSampleTypeBySampleCode(inputMap, userInfo);
	}

	/**
	 * This method is used to retrieve active sample receiving details object
	 * @param inputMap holds the keys that required to get sample receiving details
	 * @return sample receiving details object
	 * @throws Exception that are thrown from this DAO layer
	 */
	
	@Override
	public ResponseEntity<Object> getSampleReceivingDetails(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return bioDirectTransferDAO.getSampleReceivingDetails(inputMap, userInfo);
	}

	/**
	 * This method is used to add a new entry to biodirecttransfer table. Need to check for
	 * duplicate entry of samples for the specified transfer form and specified site before saving into
	 * database.
	 * 
	 * @param inputMap holding the data required to create a transfer form and also the sample in the form
	 * @return response entity object holding response status and data of added
	 *         transfer form object
	 * @throws Exception that are thrown from this DAO layer
	 */
	
	@Override
	@Transactional
	public ResponseEntity<Object> createBioDirectTransfer(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return bioDirectTransferDAO.createBioDirectTransfer(inputMap, userInfo);
	}

	/**
	 * This method is used to get the active transfer form object based on nbioDirectTransferCode
	 * @param nbioDirectTransferCode holding the primary value of the transfer form
	 * @return response entity object holding response list of transfer form
	 * @throws Exception that are thrown from this DAO layer
	 */
	
	@Override
	public ResponseEntity<Object> getActiveBioDirectTransferById(final int nbioDirectTransferCode,
			final UserInfo userInfo) throws Exception {
		return bioDirectTransferDAO.getActiveBioDirectTransferById(nbioDirectTransferCode, userInfo);
	}

	/**
	 * This method is used to update the transfer form object
	 * @param inputMap holding the data that are changed for the transfer form
	 * @return response entity object holding response list of transfer form
	 * @throws Exception that are thrown from this DAO layer
	 */
	
	@Override
	@Transactional
	public ResponseEntity<Object> updateBioDirectTransfer(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return bioDirectTransferDAO.updateBioDirectTransfer(inputMap, userInfo);
	}

	/**
	 * This method is used to delete the samples
	 * @param nbioDirectTransferCode holding the primary value of transfer form
	 * @param nbioDirectTransferDetailsCode holding the primary value of samples
	 * @return response entity object holding response status and data of deleted samples object
	 * @throws Exception that are thrown from this DAO layer
	 */
	
	@Override
	@Transactional
	public ResponseEntity<Object> deleteChildDirectTransfer(final int nbioDirectTransferCode,
			final String nbioDirectTransferDetailsCode, final UserInfo userInfo) throws Exception {
		return bioDirectTransferDAO.deleteChildDirectTransfer(nbioDirectTransferCode, nbioDirectTransferDetailsCode,
				userInfo);
	}

	/**
	 * This method is used to dispose the samples
	 * @param nbioDirectTransferCode holding the primary value of transfer form
	 * @param nbioDirectTransferDetailsCode holding the primary value of samples
	 * @return response entity object holding response status and data of disposed samples object
	 * @throws Exception that are thrown from this DAO layer
	 */
	
	@Override
	@Transactional
	public ResponseEntity<Object> disposeSamples(final int nbioDirectTransferCode,
			final String nbioDirectTransferDetailsCode, final UserInfo userInfo) throws Exception {
		return bioDirectTransferDAO.disposeSamples(nbioDirectTransferCode, nbioDirectTransferDetailsCode, userInfo);
	}

	/**
	 * This method is used to undo the disposes samples
	 * @param nbioDirectTransferCode holding the primary value of transfer form
	 * @param nbioDirectTransferDetailsCode holding the primary value of samples
	 * @return response entity object holding response status and data of undo disposed samples object
	 * @throws Exception that are thrown from this DAO layer
	 */
	
	@Override
	@Transactional
	public ResponseEntity<Object> undoDisposeSamples(final int nbioDirectTransferCode,
			final String nbioDirectTransferDetailsCode, final UserInfo userInfo) throws Exception {
		return bioDirectTransferDAO.undoDisposeSamples(nbioDirectTransferCode, nbioDirectTransferDetailsCode,
				userInfo);
	}
	
	/**
	 * This method is used to add the samples to the transfer form
	 * @param inputMap holding the data required to add samples to the particular transfer form
	 * @return response entity object holding response status and data of transfer form object
	 * @throws Exception that are thrown from this DAO layer
	 */
	
	@Override
	@Transactional
	public ResponseEntity<Object> createChildBioDirectTransfer(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {
		return bioDirectTransferDAO.createChildBioDirectTransfer(inputMap, userInfo);
	}

	/**
	 * This method is used to retrieve active sample condition object
	 * @return sample condition object
	 * @throws Exception that are thrown from this DAO layer
	 */
	
	@Override
	public ResponseEntity<Object> getSampleConditionStatus(final UserInfo userInfo) throws Exception {
		return bioDirectTransferDAO.getSampleConditionStatus(userInfo);
	}

	/**
	 * This method is used to retrieve active reason object
	 * @return reason object
	 * @throws Exception that are thrown from this DAO layer
	 */
	
	@Override
	public ResponseEntity<Object> getReason(final UserInfo userInfo) throws Exception {
		return bioDirectTransferDAO.getReason(userInfo);
	}

	/**
	 * This method is used to update the sample condition object
	 * @param inputMap holding the data that are changed for the sample condition
	 * @return response entity object holding response list of samples
	 * @throws Exception that are thrown from this DAO layer
	 */
	
	@Override
	@Transactional
	public ResponseEntity<Object> updateSampleCondition(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return bioDirectTransferDAO.updateSampleCondition(inputMap, userInfo);
	}

	/**
	 * This method is used to retrieve active storage condition object
	 * @return storage condition object
	 * @throws Exception that are thrown from this DAO layer
	 */
	
	@Override
	public ResponseEntity<Object> getStorageCondition(final UserInfo userInfo) throws Exception {
		return bioDirectTransferDAO.getStorageCondition(userInfo);
	}

	/**
	 * This method is used to retrieve active users object based on site
	 * @return users object
	 * @throws Exception that are thrown from this DAO layer
	 */
	
	@Override
	public ResponseEntity<Object> getUsersBasedOnSite(final UserInfo userInfo) throws Exception {
		return bioDirectTransferDAO.getUsersBasedOnSite(userInfo);
	}

	/**
	 * This method is used to retrieve active courier object
	 * @return courier object
	 * @throws Exception that are thrown from this DAO layer
	 */
	
	@Override
	public ResponseEntity<Object> getCourier(final UserInfo userInfo) throws Exception {
		return bioDirectTransferDAO.getCourier(userInfo);
	}

	/**
	 * This method is used to validate the transfer form
	 * @param inputMap holding the data that are validated for the transfer form
	 * @return response entity object holding response list of transfer forms
	 * @throws Exception that are thrown from this DAO layer
	 */
	
	@Override
	@Transactional
	public ResponseEntity<Object> createValidationBioDirectTransfer(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {
		return bioDirectTransferDAO.createValidationBioDirectTransfer(inputMap, userInfo);
	}

	/**
	 * This method is used to transfer the transfer form
	 * @param inputMap holding the data that are transfered for the transfer form
	 * @return response entity object holding response list of transfer forms
	 * @throws Exception that are thrown from this DAO layer
	 */
	
	@Override
	@Transactional
	public ResponseEntity<Object> transferDirectTransfer(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return bioDirectTransferDAO.transferDirectTransfer(inputMap, userInfo);
	}

	/**
	 * This method is used to find the status of transfer form
	 * @param nbioDirectTransferCode holding primary value of transfer form
	 * @return response entity object holding response of primary key
	 * @throws Exception that are thrown from this DAO layer
	 */
	
	@Override
	public int findStatusDirectTransfer(final int nbioDirectTransferCode, final UserInfo userInfo) throws Exception {
		return bioDirectTransferDAO.findStatusDirectTransfer(nbioDirectTransferCode, userInfo);
	}

	/**
	 * This method is used to get the samples based on transfer form
	 * @param nbioDirectTransferCode holding the primary value of the transfer form
	 * @return response entity object holding response list of samples based on nbioDirectTransferCode
	 * @throws Exception that are thrown from this DAO layer
	 */
	
	@Override
	public List<Map<String, Object>> getChildInitialGet(final int nbioDirectTransferCode, final UserInfo userInfo)
			throws Exception {
		return bioDirectTransferDAO.getChildInitialGet(nbioDirectTransferCode, userInfo);
	}

	/**
	 * This method is used to cancel the transfer form
	 * @param nbioDirectTransferCode holding primary value of transfer form
	 * @return response entity object holding response of list of transfer form
	 * @throws Exception that are thrown from this DAO layer
	 */
	
	@Override
	@Transactional
	public ResponseEntity<Object> cancelDirectTransfer(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return bioDirectTransferDAO.cancelDirectTransfer(inputMap, userInfo);
	}

	/**
	 * This method is used to accept or reject the samples
	 * @param inputMap holding data to accept or reject the samples
	 * @return response entity object holding response of list of samples
	 * @throws Exception that are thrown from this DAO layer
	 */
	
	@Override
	public ResponseEntity<Object> acceptRejectDirectTransferSlide(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return bioDirectTransferDAO.acceptRejectDirectTransferSlide(inputMap, userInfo);
	}
	
	/**
	 * This method is used to check the sample that are accessible or not
	 * @param nbioDirectTransferCode holding primary value of the transfer form
	 * @return response entity object holding response of list of sample details
	 * @throws Exception that are thrown from this DAO layer
	 */
	
	@Override
	public ResponseEntity<Object> checkAccessibleSamples(final int nbioDirectTransferCode, final UserInfo userInfo)
			throws Exception {
		return bioDirectTransferDAO.checkAccessibleSamples(nbioDirectTransferCode, userInfo);
	}
	
}
