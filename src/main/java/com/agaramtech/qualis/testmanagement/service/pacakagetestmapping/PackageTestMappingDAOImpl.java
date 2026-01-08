package com.agaramtech.qualis.testmanagement.service.pacakagetestmapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import com.agaramtech.qualis.global.AuditUtilityFunction;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.testmanagement.model.TestCategory;
import com.agaramtech.qualis.testmanagement.model.TestPackage;
import com.agaramtech.qualis.testmanagement.model.TestPackageTest;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Repository
public class PackageTestMappingDAOImpl implements PackageTestMappingDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(PackageTestMappingDAOImpl.class);

	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;

	/**
	 * This Method is used to get the over all Test  with respect to Package
	 * 
	 * @param inputMap [Map] contains key nmasterSiteCode which holds the value of
	 *                 respective site code
	 * @return a response entity which holds the list of Test Package with respect to
	 *         site and also have the HTTP response code
	 * @throws Exception that are thrown
	 */
	public ResponseEntity<Object> getPackageTestMapping(final Integer nTestpackageCode, final UserInfo userInfo)
			throws Exception {
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		TestPackage selectedTestPackage = null;
		String strQuery = "";
		if (nTestpackageCode == null) {
			strQuery = getTestPackage(nTestpackageCode, userInfo);
			LOGGER.info("getParameterTestMapping-->" + strQuery);
			final List<TestPackage> testPackageList = jdbcTemplate.query(strQuery, new TestPackage());
			if (testPackageList.isEmpty()) {
				outputMap.put("TestPackage", testPackageList);
				outputMap.put("SelectedTestPackage", null);
				outputMap.put("PackageTestMapping", null);

				return new ResponseEntity<>(outputMap, HttpStatus.OK);
			} else {
				outputMap.put("TestPackage", testPackageList);
				selectedTestPackage = testPackageList.get(testPackageList.size() - 1);
				outputMap.put("SelectedTestPackage", selectedTestPackage);

				outputMap.putAll(getTestPackageTestMapping(selectedTestPackage.getNtestpackagecode(), userInfo).getBody());

			}
		} else {
			strQuery = getTestPackage(nTestpackageCode, userInfo);
			TestPackage objTestpackage = (TestPackage) jdbcUtilityFunction.queryForObject(strQuery, TestPackage.class,
					jdbcTemplate);
			if (objTestpackage == null) {
				final String returnString = commonFunction.getMultilingualMessage(
						Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(), userInfo.getSlanguagefilename());
				return new ResponseEntity<>(returnString, HttpStatus.EXPECTATION_FAILED);
			} else {

				strQuery = getTestPackage(null, userInfo);

				final List<TestPackage> testPackageList = jdbcTemplate.query(strQuery, new TestPackage());
				if (testPackageList.isEmpty()) {
					outputMap.put("TestPackage", testPackageList);
					outputMap.put("SelectedTestPackage", null);
					outputMap.put("PackageTestMapping", null);
				} else {
					outputMap.put("TestPackage", testPackageList);
					outputMap.put("SelectedTestPackage", objTestpackage);
					outputMap.putAll(getTestPackageTestMapping(nTestpackageCode, userInfo).getBody());

				}
			}

		}
		return new ResponseEntity<>(outputMap, HttpStatus.OK);

	}
	
	/**
	 *This method retrieves the overall list of packages.
	 * 
	 * @param inputMap [Map] contains key nmasterSiteCode which holds the value of
	 *                 respective site code
	 * @return a response entity which holds the list of Package with respect to
	 *         site and also have the HTTP response code
	 * @throws Exception that are thrown
	 */
	private String getTestPackage(final Integer nTestpackageCode,final UserInfo userInfo){
		String sTestpackagecode="";
		if(nTestpackageCode!=null) {
			sTestpackagecode=" and ntestpackagecode="+nTestpackageCode+" ";
		}
		
		final String strQuery = "select ntestpackagecode, stestpackagename,sdescription, nsitecode, nstatus  from testpackage tp where nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ntestpackagecode>0  and nsitecode=" + userInfo.getNmastersitecode()
				+ " "+sTestpackagecode+" order by ntestpackagecode";

		
		return strQuery;


	}
	
	/**
	 *This method retrieves the overall list of tests organized package-wise.
	 * 
	 * @param inputMap [Map] contains key nmasterSiteCode which holds the value of
	 *                 respective site code
	 * @return a response entity which holds the list of Package Test with respect to
	 *         site and also have the HTTP response code
	 * @throws Exception that are thrown
	 */
	public ResponseEntity<Map<String, Object>> getTestPackageTestMapping(final Integer nTestpackageCode,final UserInfo userInfo) {
		Map<String,Object> retrunMap=new HashMap<>();
		final String strQuery = "select tpt.ntestpackagetestcode,tpt.ntestcode,tm.stestname,tm.stestsynonym,tpt.ntestpackagecode,tpt.nstatus,tc.ntestcategorycode,tc.stestcategoryname from testpackagetest tpt,testpackage tp,testmaster tm,testcategory tc "
				+ " where tc.ntestcategorycode=tm.ntestcategorycode and  tpt.ntestcode = tm.ntestcode  and tpt.ntestpackagecode = tp.ntestpackagecode and tpt.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tm.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tp.nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tc.nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  and tpt.ntestpackagecode = "
				+ nTestpackageCode + " and tm.nsitecode =" + userInfo.getNmastersitecode()
				+ " and tpt.nsitecode =" + userInfo.getNmastersitecode() + " and tp.nsitecode ="
				+ userInfo.getNmastersitecode();
		 final List<TestPackageTest> testPackageTestList = jdbcTemplate.query(strQuery, new TestPackageTest());
		 retrunMap.put("PackageTestMapping", testPackageTestList);

			return new ResponseEntity<>(retrunMap, HttpStatus.OK);
	}

	/**
	 * This Method is used to get the over all Test Category with respect to site
	 * 
	 * @param inputMap [Map] contains key nmasterSiteCode which holds the value of
	 *                 respective site code
	 * @return a response entity which holds the list of Test Category with respect to
	 *         site and also have the HTTP response code
	 * @throws Exception that are thrown
	 */
	
	public ResponseEntity<Object> getTestCategory(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {

		String strQuery = "";
		strQuery = getTestPackage((int) inputMap.get("ntestpackagecode"), userInfo);
		TestPackage objTestpackage = (TestPackage) jdbcUtilityFunction.queryForObject(strQuery, TestPackage.class,
				jdbcTemplate);
		if (objTestpackage == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			Map<String, Object> responseMap = new HashMap<>();

			strQuery = "SELECT t.ntestcategorycode, t.stestcategoryname, t.ndefaultstatus FROM testcategory t "
					+ "JOIN testmaster tm ON tm.ntestcategorycode = t.ntestcategorycode WHERE t.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " AND tm.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " AND t.nsitecode = "
					+ userInfo.getNmastersitecode() + " AND tm.nsitecode = " + userInfo.getNmastersitecode()
					+ " AND NOT EXISTS ( SELECT 1 FROM testpackagetest tpt "
					+ "     WHERE tpt.ntestcode = tm.ntestcode  AND tpt.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tpt.ntestpackagecode="+(int) inputMap.get("ntestpackagecode")+") "
					+ "GROUP BY t.ntestcategorycode, t.stestcategoryname, t.ndefaultstatus";


			final List<TestCategory> lstTestCategory = jdbcTemplate.query(strQuery, new TestCategory());

			final int ntestcategorycode = lstTestCategory.stream()
					.filter(category -> category.getNdefaultstatus() == Enumeration.TransactionStatus.YES
							.gettransactionstatus())
					.map(TestCategory::getNtestcategorycode).findFirst()
					.orElse(Enumeration.TransactionStatus.NA.gettransactionstatus());

			responseMap.put("TestCategory", lstTestCategory);

			if (!lstTestCategory.isEmpty()) {
				if (ntestcategorycode != Enumeration.TransactionStatus.NA.gettransactionstatus()) {

					inputMap.put("ntestcategorycode", ntestcategorycode);
					responseMap.putAll(getTestPackageTest(inputMap, userInfo).getBody());

				}

			}
			return new ResponseEntity<>(responseMap, HttpStatus.OK);
		}
	}


	/**
	 * This Method is used to get the over all Test with respect to Test Category
	 * 
	 * @param inputMap [Map] contains key nmasterSiteCode which holds the value of
	 *                 respective site code
	 * @return a response entity which holds the list of Package Test with respect to
	 *         site and also have the HTTP response code
	 * @throws Exception that are thrown
	 */
	
	public ResponseEntity<Map<String, Object>> getTestPackageTest(final Map<String, Object> inputMap, final UserInfo userInfo) {
		Map<String, Object> retrunMap = new HashMap<>();

		final int nTestCategoryCode = (int) inputMap.get("ntestcategorycode");
		final int nTestPackageCode = (int) inputMap.get("ntestpackagecode");

		final String strQuery = "SELECT tm.ntestcode, tm.stestname FROM testmaster tm "
				+ " WHERE  ntestcategorycode =" + nTestCategoryCode + " AND tm.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " AND tm.nsitecode = "
				+ userInfo.getNmastersitecode() + " AND NOT EXISTS ( "
				+ " SELECT 1 FROM testpackagetest tpt WHERE tpt.ntestcode = tm.ntestcode "
				+ " AND tpt.ntestpackagecode = " + nTestPackageCode + " AND tpt.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";

		final List<TestPackageTest> testPackageTestList = jdbcTemplate.query(strQuery, new TestPackageTest());

		retrunMap.put("TestPackageTest", testPackageTestList);
		return new ResponseEntity<>(retrunMap, HttpStatus.OK);
	}
	
	/**
	 * This method is used to create a new entry in the testpackagetest table. 
	 * 
	 * @param inputMap [Map] contains key nmasterSiteCode which holds the value of
	 *                 respective site code
	 * @return a response entity which holds the list of  Package Test with respect to
	 *         site and also have the HTTP response code
	 * @throws Exception that are thrown
	 */
	public ResponseEntity<Object> createPackageTestMapping(final Map<String,Object> inputMap, final UserInfo userInfo) throws Exception {

		String strQuery = "";
		final int nTestPackageCode=(int) inputMap.get("ntestpackagecode");
		strQuery = getTestPackage(nTestPackageCode, userInfo);
		final TestPackage objTestpackage = (TestPackage) jdbcUtilityFunction.queryForObject(strQuery, TestPackage.class,
				jdbcTemplate);

		if (objTestpackage == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {

			final String sTestCode= inputMap.get("ntestcode").toString();

			final String seqString = "select nsequenceno from seqnotestmanagement  where stablename ='testpackagetest'";
			int nsequenceNo = jdbcTemplate.queryForObject(seqString, Integer.class) + 1;
			

			String queryString = 
				    "INSERT INTO testpackagetest " +
				    " (ntestpackagetestcode, ntestcode, ntestpackagecode, ndefaultstatus, dmodifieddate, nsitecode, nstatus) " +
				    "SELECT " +
				    "  RANK() OVER (ORDER BY tm.ntestcode ASC) + " + nsequenceNo + " AS ntestpackagetestcode, " +
				    "  tm.ntestcode, " + nTestPackageCode + ", " +
				    "  CASE WHEN EXISTS ( " +
				    "        SELECT 1 FROM testpackagetest tpt " +
				    "        WHERE tpt.ntestcode = tm.ntestcode " +
				    "          AND tpt.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + 
				    "          AND tpt.ndefaultstatus = " + Enumeration.TransactionStatus.YES.gettransactionstatus() + 
				    "      ) THEN " + Enumeration.TransactionStatus.NO.gettransactionstatus() +
				    "      ELSE " + Enumeration.TransactionStatus.YES.gettransactionstatus() + 
				    "  END AS ndefaultstatus, " +
				    "  '" + dateUtilityFunction.getCurrentDateTime(userInfo) + "', " +
				    "  " + userInfo.getNmastersitecode() + ", " +
				    "  " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " +
				    "FROM testmaster tm " +
				    "WHERE tm.nsitecode = " + userInfo.getNmastersitecode() + " " +
				    "  AND tm.ntestcode IN (" + sTestCode + ") " +
				    "  AND NOT EXISTS ( " +
				    "       SELECT 1 FROM testpackagetest tpt2 " +
				    "       WHERE tpt2.ntestcode = tm.ntestcode and tpt2.ntestpackagecode = " + nTestPackageCode + " " +
				    "         AND tpt2.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + 
				    "  );";
			

			queryString = queryString+" update seqnotestmanagement set nsequenceno = (select max(ntestpackagetestcode) from testpackagetest)"
					+ " where stablename = 'testpackagetest'";
			jdbcTemplate.execute(queryString);

			final String sQry = "SELECT RANK() OVER (ORDER BY t.ntestcode ASC) + " + nsequenceNo + " AS ntestpackagetestcode, "
					+ "       " + nTestPackageCode + " AS ntestpackagecode,tc.ntestcategorycode, "
					+ "       t.ntestcode " + "FROM testmaster t "
					+ "JOIN testcategory tc ON tc.ntestcategorycode = t.ntestcategorycode " + "WHERE t.nsitecode = "
					+ userInfo.getNmastersitecode() + " " + "  AND t.ntestcode IN (" + sTestCode + ") "
					+ "  AND  EXISTS ( SELECT 1 FROM testpackagetest tpt "
					+ "    WHERE tpt.ntestcode = t.ntestcode and tpt.ntestpackagecode = " + nTestPackageCode + "  AND tpt.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ");";


			final List<TestPackageTest> testPackageTestList = (List<TestPackageTest>) jdbcTemplate.query(sQry, new TestPackageTest());

			final List<Object> savedList = new ArrayList<>();
			savedList.add(testPackageTestList);

			final List<String> multiLingualIDList = new ArrayList<String>();
			multiLingualIDList.add("IDS_ADDTEST");

			auditUtilityFunction.fnInsertListAuditAction(savedList, 1, null, multiLingualIDList, userInfo);

			return getPackageTestMapping(nTestPackageCode, userInfo);
		}
	}
	
	/**
	 * This method is used to delete a package test from the testpackagetest table based on the respective ntestpackagetestcode.”
	 * 
	 * @param inputMap [Map] contains key nmasterSiteCode which holds the value of
	 *                 respective site code
	 * @return a response entity which holds the list of Package Test with respect to
	 *         site and also have the HTTP response code
	 * @throws Exception that are thrown
	 */
	public ResponseEntity<Object> deletePackageTestMapping(final TestPackageTest objTestPackageTest,
			final UserInfo userInfo) throws Exception {
		
		

			
			String sQuery = "select * from testpackagetest where nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ntestpackagetestcode = "
					+ objTestPackageTest.getNtestpackagetestcode();
			
			final TestPackageTest objTestPackageTestById = (TestPackageTest) jdbcUtilityFunction.queryForObject(sQuery,
					TestPackageTest.class, jdbcTemplate);

			if (objTestPackageTestById == null) {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_TESTALREADYDELETED",
						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			} else {
				final List<String> multilingualIDList = new ArrayList<String>();
				final List<Object> savedList = new ArrayList<>();

				sQuery = "update testpackagetest set nstatus = "
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ", dmodifieddate ='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where ntestpackagetestcode = "
						+ objTestPackageTest.getNtestpackagetestcode();
				jdbcTemplate.execute(sQuery);

				objTestPackageTest.setNstatus((short) Enumeration.TransactionStatus.DELETED.gettransactionstatus());

				savedList.add(objTestPackageTest);
				multilingualIDList.add("IDS_DELETETEST");
				auditUtilityFunction.fnInsertAuditAction(savedList, 1, null, multilingualIDList, userInfo);

				return getPackageTestMapping(objTestPackageTest.getNtestpackagecode(), userInfo);
			}
		}
	}

