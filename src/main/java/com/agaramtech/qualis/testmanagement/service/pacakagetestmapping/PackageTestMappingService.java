package com.agaramtech.qualis.testmanagement.service.pacakagetestmapping;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.testmanagement.model.TestPackageTest;


public interface PackageTestMappingService {
	/**
	 * This Method is used to get the over all Test  with respect to Package
	 * 
	 * @param inputMap [Map] contains key nmasterSiteCode which holds the value of
	 *                 respective site code
	 * @return a response entity which holds the list of Test Package with respect to
	 *         site and also have the HTTP response code
	 * @throws Exception that are thrown
	 */
	
	public ResponseEntity<Object> getPackageTestMapping(final Integer nTestpackageCode,final UserInfo userInfo) throws Exception;
	
	/**
	 * This Method is used to get the over all Test Category with respect to site
	 * 
	 * @param inputMap [Map] contains key nmasterSiteCode which holds the value of
	 *                 respective site code
	 * @return a response entity which holds the list of Test Category with respect to
	 *         site and also have the HTTP response code
	 * @throws Exception that are thrown
	 */
	
	public ResponseEntity<Object> getTestCategory(final Map<String,Object> inputMap,final UserInfo userInfo) throws Exception;

	/**
	 * This Method is used to get the over all Test with respect to Test Category
	 * 
	 * @param inputMap [Map] contains key nmasterSiteCode which holds the value of
	 *                 respective site code
	 * @return a response entity which holds the list of Package Test with respect to
	 *         site and also have the HTTP response code
	 * @throws Exception that are thrown
	 */
	
	public ResponseEntity<Map<String, Object>> getTestPackageTest(final Map<String,Object> inputMap,final UserInfo userInfo) throws Exception;
	
	/**
	 * This method is used to create a new entry in the testpackagetest table. 
	 * 
	 * @param inputMap [Map] contains key nmasterSiteCode which holds the value of
	 *                 respective site code
	 * @return a response entity which holds the list of  Package Test with respect to
	 *         site and also have the HTTP response code
	 * @throws Exception that are thrown
	 */
	public ResponseEntity<Object> createPackageTestMapping(final Map<String,Object> inputMap,final UserInfo userInfo) throws Exception;
	
	/**
	 * This method is used to delete a package test from the testpackagetest table based on the respective ntestpackagetestcode.”
	 * 
	 * @param inputMap [Map] contains key nmasterSiteCode which holds the value of
	 *                 respective site code
	 * @return a response entity which holds the list of Package Test with respect to
	 *         site and also have the HTTP response code
	 * @throws Exception that are thrown
	 */
	
	public ResponseEntity<Object> deletePackageTestMapping(final TestPackageTest objTestPackageTest,final UserInfo userInfo) throws Exception;

	/**
	 *This method retrieves the overall list of tests organized package-wise.
	 * 
	 * @param inputMap [Map] contains key nmasterSiteCode which holds the value of
	 *                 respective site code
	 * @return a response entity which holds the list of Package Test with respect to
	 *         site and also have the HTTP response code
	 * @throws Exception that are thrown
	 */
	
	public ResponseEntity<Map<String, Object>> getTestPackageTestMapping(final Integer nTestpackageCode,UserInfo userInfo) throws Exception;

}
