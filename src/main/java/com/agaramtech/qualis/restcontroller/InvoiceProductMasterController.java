package com.agaramtech.qualis.restcontroller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.invoice.model.InvoiceProductFile;
import com.agaramtech.qualis.invoice.model.InvoiceProductMaster;
import com.agaramtech.qualis.invoice.model.TaxProductDetails;
import com.agaramtech.qualis.invoice.service.invoiceproductmaster.InvoiceProductMasterService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletResponse;

/**
 * This controller is used to dispatch the input request to its relevant service
 * methods to access the invoiceproductmaster Service methods. IN-448 Product
 * Master
 */
@RestController
@RequestMapping("/invoiceproductmaster")
public class InvoiceProductMasterController {

	private RequestContext requestContext;

	private InvoiceProductMasterService invoiceProductMasterService;

	/**
	 * This constructor injection method is used to pass the object dependencies to
	 * the class.
	 * 
	 * @param requestContext              RequestContext to hold the request
	 * @param invoiceproductmasterService invoiceproductmasterService
	 */

	public InvoiceProductMasterController(RequestContext requestContext,
			InvoiceProductMasterService invoiceProductMasterService) {
		super();
		this.requestContext = requestContext;
		this.invoiceProductMasterService = invoiceProductMasterService;
	}

	/**
	 * This method is used to fetch the active InvoiceProductmaster objects for the
	 * specified InvoiceProductmaster name and site.
	 * 
	 * @param ntypecode       [String] name of the InvoiceProductmaster
	 * @param nmasterSiteCode [int] site code of the InvoiceProductmaster
	 * @return list of active InvoiceProductmaster code(s) based on the specified
	 *         InvoiceProductmaster name and site
	 * @throws Exception
	 */
	@PostMapping("/getInvoiceProductMaster")
	public ResponseEntity<Object> getProductMaster(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userinfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userinfo);

		return invoiceProductMasterService.getProductMaster(userinfo);

	}

	/**
	 * This method is used to fetch the active InvoiceProductmaster objects for the
	 * specified InvoiceProductmaster name and site.
	 * 
	 * @param ntypecode       [String] name of the InvoiceProductmaster
	 * @param nmasterSiteCode [int] site code of the InvoiceProductmaster
	 * @return list of active InvoiceProductmaster code(s) based on the specified
	 *         InvoiceProductmaster name and site
	 * @throws Exception
	 */
	@PostMapping("/getProductType")
	public ResponseEntity<Object> getProductType(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userinfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userinfo);

		return invoiceProductMasterService.getProductType(userinfo);

	}

	/**
	 * This method is used to fetch the active InvoiceProductmaster objects for the
	 * specified InvoiceProductmaster name and site.
	 * 
	 * @param ntypecode       [String] name of the InvoiceProductmaster
	 * @param nmasterSiteCode [int] site code of the InvoiceProductmaster
	 * @return list of active InvoiceProductmaster code(s) based on the specified
	 *         InvoiceProductmaster name and site
	 * @throws Exception
	 */
	@PostMapping("/getSampleTypeData")
	public ResponseEntity<Object> getSampleTypeData(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userinfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userinfo);

		return invoiceProductMasterService.getSampleTypeData(userinfo);

	}

	/**
	 * This method is used to fetch the active InvoiceProductmaster objects for the
	 * specified InvoiceProductmaster name and site.
	 * 
	 * @param ntypecode       [String] name of the InvoiceProductmaster
	 * @param nmasterSiteCode [int] site code of the InvoiceProductmaster
	 * @return list of active InvoiceProductmaster code(s) based on the specified
	 *         InvoiceProductmaster name and site
	 * @throws Exception
	 */
	@PostMapping("/getSampleTypeByProduct")
	public ResponseEntity<Object> getSampleTypeByProduct(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userinfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userinfo);

		int nproductCatcode = (int) inputMap.get("nproductcatcode");

		return invoiceProductMasterService.getSampleTypeByProduct(userinfo, nproductCatcode);

	}

	/**
	 * This method is used to fetch the active InvoiceProductmaster objects for the
	 * specified InvoiceProductmaster name and site.
	 * 
	 * @param ntypecode       [String] name of the InvoiceProductmaster
	 * @param nmasterSiteCode [int] site code of the InvoiceProductmaster
	 * @return list of active InvoiceProductmaster code(s) based on the specified
	 *         InvoiceProductmaster name and site
	 * @throws Exception
	 */
	@PostMapping("/getTreeTemplateByProduct")
	public ResponseEntity<Object> getTreeTemplateByProduct(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userinfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userinfo);

		int nproductCatcode = (int) inputMap.get("nproductcatcode");
		int nproductcode = (int) inputMap.get("nproductcode");

		return invoiceProductMasterService.getTreeTemplateByProduct(userinfo, nproductCatcode, nproductcode);

	}

	/**
	 * This method is used to fetch the active InvoiceProductmaster objects for the
	 * specified InvoiceProductmaster name and site.
	 * 
	 * @param ntypecode       [String] name of the InvoiceProductmaster
	 * @param nmasterSiteCode [int] site code of the InvoiceProductmaster
	 * @return list of active InvoiceProductmaster code(s) based on the specified
	 *         InvoiceProductmaster name and site
	 * @throws Exception
	 */
	@PostMapping("/getSpecificationByTreetemplate")
	public ResponseEntity<Object> getSpecificationByTreetemplate(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userinfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userinfo);

		int nproductCatcode = (int) inputMap.get("nproductcatcode");
		int nproductcode = (int) inputMap.get("nproductcode");
		int ntreetemplatemanipulationcode = (int) inputMap.get("ntemplatemanipulationcode");
		int nformcode = (int) inputMap.get("nformcode");
		String lastLabel = "";
		if (inputMap.containsKey("label")) {
			Object labelObject = inputMap.get("label");

			if (labelObject instanceof Map) {
				@SuppressWarnings("unchecked")
				Map<String, Object> labelMap = (Map<String, Object>) labelObject;

				if (labelMap.containsKey("label")) {
					Object labelValue = labelMap.get("label");
					if (labelValue instanceof String) {

						String[] labels = labelValue.toString().split(", ");

						lastLabel = labels[labels.length - 1];

					}
				}
			}
		}

		return invoiceProductMasterService.getSpecificationByTreetemplate(userinfo, nproductCatcode, nproductcode,
				ntreetemplatemanipulationcode, nformcode, lastLabel);

	}

	/**
	 * This method is used to fetch the active InvoiceProductmaster objects for the
	 * specified InvoiceProductmaster name and site.
	 * 
	 * @param ntypecode       [String] name of the InvoiceProductmaster
	 * @param nmasterSiteCode [int] site code of the InvoiceProductmaster
	 * @return list of active InvoiceProductmaster code(s) based on the specified
	 *         InvoiceProductmaster name and site
	 * @throws Exception
	 */
	@PostMapping(value = "/getProductType1")
	public ResponseEntity<Object> getProductTypes(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		// final int ntypecode = (Integer) inputMap.get("ntypecode");
		requestContext.setUserInfo(userInfo);
		return invoiceProductMasterService.getProductTypes(inputMap, userInfo);

	}

	/**
	 * This method is used to fetch the active InvoiceProductmaster objects for the
	 * specified InvoiceProductmaster name and site.
	 * 
	 * @param ntypecode       [String] name of the InvoiceProductmaster
	 * @param nmasterSiteCode [int] site code of the InvoiceProductmaster
	 * @return list of active InvoiceProductmaster code(s) based on the specified
	 *         InvoiceProductmaster name and site
	 * @throws Exception
	 */
	@RequestMapping(value = "/getinvoiceProductType", method = RequestMethod.POST)
	public ResponseEntity<Object> getinvoiceProductType(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		// final int ntypecode = (Integer) inputMap.get("ntypecode");
		requestContext.setUserInfo(userInfo);
		return invoiceProductMasterService.getinvoiceProductType(userInfo);

	}

	/**
	 * This method will is used to make a new entry to invoiceproductmaster table.
	 * 
	 * @param inputMap map object holding params ( invoiceproductmaster
	 *                 [invoiceproductmaster] object holding details to be added in
	 *                 invoiceproductmaster table, userinfo [UserInfo] holding
	 *                 logged in user details ) Input:{ "invoiceproductmaster ": {
	 *                 "sproductname"}, "userinfo":{ "activelanguagelist":
	 *                 ["en-US"], "isutcenabled": 4,"ndeptcode": -
	 *                 1,"ndeputyusercode": -1,"ndeputyuserrole": -1, "nformcode":
	 *                 33,"nmastersitecode": -1,"nmodulecode": 1, "nreasoncode":
	 *                 0,"nsitecode": 1,"ntimezonecode": -1,"ntranssitecode":
	 *                 1,"nusercode": -1, "nuserrole": -1,"nusersitecode":
	 *                 -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss", "sgmtoffset":
	 *                 "UTC +00:00","slanguagefilename":
	 *                 "Msg_en_US","slanguagename": "English", "slanguagetypecode":
	 *                 "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss","sreason":
	 *                 "","ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy
	 *                 HH:mm:ss"} }
	 * @return ResponseEntity with string message as 'Already Exists' if the
	 *         invoiceproductmaster already exists/ list of invoiceproductmaster
	 *         along with the newly added invoiceproductmaster .
	 * @throws Exception exception
	 */
	@PostMapping("/createInvoiceProductMaster")
	public ResponseEntity<Object> createInvoiceProductMaster(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);

		return invoiceProductMasterService.createInvoiceProductMaster(inputMap, userInfo);

	}

	/**
	 * This method is used to fetch the active InvoiceProductmaster objects for the
	 * specified InvoiceProductmaster name and site.
	 * 
	 * @param ntypecode       [String] name of the InvoiceProductmaster
	 * @param nmasterSiteCode [int] site code of the InvoiceProductmaster
	 * @return list of active InvoiceProductmaster code(s) based on the specified
	 *         InvoiceProductmaster name and site
	 * @throws Exception
	 */
	@PostMapping("/getActiveInvoiceProductMasterById")
	public ResponseEntity<Object> getActiveInvoiceProductMasterById(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		// try {
		final ObjectMapper objMapper = new ObjectMapper();
		int nproductcode = (int) inputMap.get("nproductcode");
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return invoiceProductMasterService.getActiveInvoiceProductMasterById(nproductcode, userInfo);

	}

	/**
	 * This method is used to update selected invoiceproductmaster details.
	 * 
	 * @param inputMap [map object holding params( invoiceproductmaster
	 *                 [invoiceproductmaster] object holding details to be updated
	 *                 in invoiceproductmaster table, userinfo [UserInfo] holding
	 *                 logged in user details) Input:{ "invoiceproductmaster ": {
	 *                 "sproductname"}, "userinfo":{ "activelanguagelist":
	 *                 ["en-US"],"isutcenabled": 4,"ndeptcode":
	 *                 -1,"ndeputyusercode": -1,"ndeputyuserrole": -1, "nformcode":
	 *                 33,"nmastersitecode": -1, "nmodulecode": 1, "nreasoncode":
	 *                 0,"nsitecode": 1,"ntimezonecode": -1,"ntranssitecode":
	 *                 1,"nusercode": -1, "nuserrole": -1,"nusersitecode":
	 *                 -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss", "sgmtoffset":
	 *                 "UTC +00:00", "slanguagefilename":
	 *                 "Msg_en_US","slanguagename": "English", "slanguagetypecode":
	 *                 "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss", "sreason": "",
	 *                 "ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy
	 *                 HH:mm:ss"}}
	 * 
	 * 
	 * @return ResponseEntity with string message as 'Already Deleted' if the
	 *         invoiceproductmaster record is not available/ list of all
	 *         invoiceproductmaster and along with the updated invoiceproductmaster.
	 * @throws Exception exception
	 */
	@PostMapping("/updateInvoiceProductMaster")
	public ResponseEntity<Object> updateInvoiceProductMaster(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		InvoiceProductMaster objInvoiceProductMaster = objMapper.convertValue(inputMap.get("invoiceproduct"),
				new TypeReference<InvoiceProductMaster>() {
				});
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return invoiceProductMasterService.updateInvoiceProductMaster(objInvoiceProductMaster, userInfo);

	}

	/**
	 * This method is used to delete an entry in invoiceproductmaster table
	 * 
	 * @param inputMap [Map] object with keys of invoiceproductmaster entity and
	 *                 UserInfo object. Input:{ "invoiceproductmaster":
	 *                 {"ninvoiceproductmaster":1}, "userinfo":{
	 *                 "activelanguagelist": ["en-US"],"isutcenabled":
	 *                 4,"ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole":
	 *                 -1, "nformcode": 33,"nmastersitecode": -1, "nmodulecode": 1,
	 *                 "nreasoncode": 0,"nsitecode": 1,"ntimezonecode":
	 *                 -1,"ntranssitecode": 1,"nusercode": -1, "nuserrole":
	 *                 -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy
	 *                 HH:mm:ss", "sgmtoffset": "UTC +00:00", "slanguagefilename":
	 *                 "Msg_en_US","slanguagename": "English", "slanguagetypecode":
	 *                 "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss", "sreason": "",
	 *                 "ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy
	 *                 HH:mm:ss"}}
	 * @return ResponseEntity with string message as 'Already deleted' if the
	 *         invoiceproductmaster record is not available/ string message as
	 *         'Record is used in....' when the invoiceproductmaster is associated
	 *         in transaction / list of all invoiceproductmaster excluding the
	 *         deleted record
	 * @throws Exception exception
	 */
	@PostMapping("/deleteInvoiceProductMaster")
	public ResponseEntity<Object> deleteInvoiceProductMaster(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return invoiceProductMasterService.deleteInvoiceProductMaster(inputMap, userInfo);
	}

	/**
	 * This method is used to retrieve a specific invoiceproductmaster record.
	 * 
	 * @param inputMap [Map] map object with "nproductcode" and "userinfo" as keys
	 *                 for which the data is to be fetched Input:{ "nproductcode":
	 *                 1, "userinfo":{ "activelanguagelist": ["en-US"],
	 *                 "isutcenabled": 4,"ndeptcode": -1,"ndeputyusercode":
	 *                 -1,"ndeputyuserrole": -1, "nformcode": 33,"nmastersitecode":
	 *                 -1,"nmodulecode": 1, "nreasoncode": 0,"nsitecode":
	 *                 1,"ntimezonecode": -1,"ntranssitecode": 1,"nusercode": -1,
	 *                 "nuserrole": -1,"nusersitecode": -1,"sdatetimeformat":
	 *                 "dd/MM/yyyy HH:mm:ss", "sgmtoffset": "UTC
	 *                 +00:00","slanguagefilename": "Msg_en_US","slanguagename":
	 *                 "English", "slanguagetypecode": "en-US", "spgdatetimeformat":
	 *                 "dd/MM/yyyy HH24:mi:ss", "spgsitedatetime": "dd/MM/yyyy
	 *                 HH24:mi:ss","sreason": "","ssitedate": "dd/MM/yyyy",
	 *                 "ssitedatetime": "dd/MM/yyyy HH:mm:ss"}}
	 * @return ResponseEntity with invoiceproductmaster object for the specified
	 *         primary key / with string message as 'Deleted' if the
	 *         invoiceproductmaster record is not available
	 * @throws Exception exception
	 */
	// Edit popup
	@PostMapping("/getActiveProductMasterById")
	public ResponseEntity<Object> getActiveProductMasterById(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		final ObjectMapper objmapper = new ObjectMapper();
		final int nproductcode = (Integer) inputMap.get("nproductcode");
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return invoiceProductMasterService.getActiveProductMasterById(nproductcode, userInfo);

	}

	/**
	 * This method is used to fetch the active InvoiceProductmaster objects for the
	 * specified InvoiceProductmaster name and site.
	 * 
	 * @param ntypecode       [String] name of the InvoiceProductmaster
	 * @param nmasterSiteCode [int] site code of the InvoiceProductmaster
	 * @return list of active InvoiceProductmaster code(s) based on the specified
	 *         InvoiceProductmaster name and site
	 * @throws Exception
	 */
	@PostMapping("/getSelectedProductMasterDetail")
	public ResponseEntity<Object> getSelectedClientDetail(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final int nproductcode = (Integer) inputMap.get("nproductcode");
		requestContext.setUserInfo(userInfo);
		return invoiceProductMasterService.getSelectedProductMasterDetail(userInfo, nproductcode);
	}

	/**
	 * This method is used to fetch the active InvoiceProductmaster objects for the
	 * specified InvoiceProductmaster name and site.
	 * 
	 * @param ntypecode       [String] name of the InvoiceProductmaster
	 * @param nmasterSiteCode [int] site code of the InvoiceProductmaster
	 * @return list of active InvoiceProductmaster code(s) based on the specified
	 *         InvoiceProductmaster name and site
	 * @throws Exception
	 */
	@PostMapping("/getProductByType")
	public ResponseEntity<Object> getProductByType(@RequestBody Map<String, Object> inputMap) throws Exception {
		try {
			final ObjectMapper objMapper = new ObjectMapper();
			final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
			int nallottedspeccode = 0;
			final int ntypecode = (int) inputMap.get("ntypecode");
			if (inputMap.containsKey("nallottedspeccode")) {
				nallottedspeccode = (int) inputMap.get("nallottedspeccode");
			} else {
				nallottedspeccode = -1;
			}
			requestContext.setUserInfo(userInfo);
			return invoiceProductMasterService.getproductByType(ntypecode, userInfo, nallottedspeccode);
		} catch (Exception e) {
			return new ResponseEntity<Object>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	/**
	 * This method is used to retrieve list of active invoiceproductmaster for the
	 * specified site.
	 * 
	 * @param inputMap [Map] map object with "nsitecode" as key for which the list
	 *                 is to be fetched
	 * @return response object with list of active countries that are to be listed
	 *         for the specified site
	 */
	@PostMapping("/getInvoiceByProduct")
	public ResponseEntity<Object> getInvoiceByProduct(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final int nproductcode = (Integer) inputMap.get("nproductcode");
		requestContext.setUserInfo(userInfo);
		return invoiceProductMasterService.getInvoiceByProduct(userInfo, nproductcode);
	}

	/**
	 * This method will is used to make a new entry to invoiceproductmaster table.
	 * 
	 * @param inputMap map object holding params ( invoiceproductmaster
	 *                 [invoiceproductmaster] object holding details to be added in
	 *                 invoiceproductmaster table, userinfo [UserInfo] holding
	 *                 logged in user details ) Input:{ "invoiceproductmaster ": {
	 *                 "sproductname"}, "userinfo":{ "activelanguagelist":
	 *                 ["en-US"], "isutcenabled": 4,"ndeptcode": -
	 *                 1,"ndeputyusercode": -1,"ndeputyuserrole": -1, "nformcode":
	 *                 33,"nmastersitecode": -1,"nmodulecode": 1, "nreasoncode":
	 *                 0,"nsitecode": 1,"ntimezonecode": -1,"ntranssitecode":
	 *                 1,"nusercode": -1, "nuserrole": -1,"nusersitecode":
	 *                 -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss", "sgmtoffset":
	 *                 "UTC +00:00","slanguagefilename":
	 *                 "Msg_en_US","slanguagename": "English", "slanguagetypecode":
	 *                 "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss","sreason":
	 *                 "","ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy
	 *                 HH:mm:ss"} }
	 * @return ResponseEntity with string message as 'Already Exists' if the
	 *         invoiceproductmaster already exists/ list of invoiceproductmaster
	 *         along with the newly added invoiceproductmaster .
	 * @throws Exception exception
	 */
	// Product tax add
	@PostMapping("/createTaxProduct")
	public ResponseEntity<Object> createTaxProduct(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objmapper = new ObjectMapper();
		final TaxProductDetails taxproduct = objmapper.convertValue(inputMap.get("taxproductdetails"),
				TaxProductDetails.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return invoiceProductMasterService.createTaxProduct(taxproduct, userInfo);

	}

	/**
	 * This method is used to fetch the active InvoiceProductmaster objects for the
	 * specified InvoiceProductmaster name and site.
	 * 
	 * @param ntypecode       [String] name of the InvoiceProductmaster
	 * @param nmasterSiteCode [int] site code of the InvoiceProductmaster
	 * @return list of active InvoiceProductmaster code(s) based on the specified
	 *         InvoiceProductmaster name and site
	 * @throws Exception
	 */
	// Tax Edit Get
	@PostMapping("/getTaxProductById")
	public ResponseEntity<Object> getTaxProductById(@RequestBody Map<String, Object> inputMap) throws Exception {

		final int nproductcode = (Integer) inputMap.get("nproductcode");
		final int ntaxproductcode = (Integer) inputMap.get("ntaxproductcode");
		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return invoiceProductMasterService.getTaxProductById(nproductcode, ntaxproductcode, userInfo);

	}

	/**
	 * This method is used to update selected invoiceproductmaster details.
	 * 
	 * @param inputMap [map object holding params( invoiceproductmaster
	 *                 [invoiceproductmaster] object holding details to be updated
	 *                 in invoiceproductmaster table, userinfo [UserInfo] holding
	 *                 logged in user details) Input:{ "invoiceproductmaster ": {
	 *                 "sproductname"}, "userinfo":{ "activelanguagelist":
	 *                 ["en-US"],"isutcenabled": 4,"ndeptcode":
	 *                 -1,"ndeputyusercode": -1,"ndeputyuserrole": -1, "nformcode":
	 *                 33,"nmastersitecode": -1, "nmodulecode": 1, "nreasoncode":
	 *                 0,"nsitecode": 1,"ntimezonecode": -1,"ntranssitecode":
	 *                 1,"nusercode": -1, "nuserrole": -1,"nusersitecode":
	 *                 -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss", "sgmtoffset":
	 *                 "UTC +00:00", "slanguagefilename":
	 *                 "Msg_en_US","slanguagename": "English", "slanguagetypecode":
	 *                 "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss", "sreason": "",
	 *                 "ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy
	 *                 HH:mm:ss"}}
	 * 
	 * 
	 * @return ResponseEntity with string message as 'Already Deleted' if the
	 *         invoiceproductmaster record is not available/ list of all
	 *         invoiceproductmaster and along with the updated invoiceproductmaster.
	 * @throws Exception exception
	 */
	// Edit Save
	@PostMapping("/updateTaxProduct")
	public ResponseEntity<Object> updateTaxProduct(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objmapper = new ObjectMapper();
		final TaxProductDetails TaxProduct = objmapper.convertValue(inputMap.get("taxproductdetails"),
				TaxProductDetails.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return invoiceProductMasterService.updateTaxProduct(TaxProduct, userInfo);
	}

	/**
	 * This method is used to delete an entry in invoiceproductmaster table
	 * 
	 * @param inputMap [Map] object with keys of invoiceproductmaster entity and
	 *                 UserInfo object. Input:{ "invoiceproductmaster":
	 *                 {"ninvoiceproductmaster":1}, "userinfo":{
	 *                 "activelanguagelist": ["en-US"],"isutcenabled":
	 *                 4,"ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole":
	 *                 -1, "nformcode": 33,"nmastersitecode": -1, "nmodulecode": 1,
	 *                 "nreasoncode": 0,"nsitecode": 1,"ntimezonecode":
	 *                 -1,"ntranssitecode": 1,"nusercode": -1, "nuserrole":
	 *                 -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy
	 *                 HH:mm:ss", "sgmtoffset": "UTC +00:00", "slanguagefilename":
	 *                 "Msg_en_US","slanguagename": "English", "slanguagetypecode":
	 *                 "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss", "sreason": "",
	 *                 "ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy
	 *                 HH:mm:ss"}}
	 * @return ResponseEntity with string message as 'Already deleted' if the
	 *         invoiceproductmaster record is not available/ string message as
	 *         'Record is used in....' when the invoiceproductmaster is associated
	 *         in transaction / list of all invoiceproductmaster excluding the
	 *         deleted record
	 * @throws Exception exception
	 */
	// Delete
	@PostMapping("/deleteTaxProduct")
	public ResponseEntity<Object> deleteClientSiteAddress(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objmapper = new ObjectMapper();
		final TaxProductDetails taxProductDetails = objmapper.convertValue(inputMap.get("taxproductdetails"),
				TaxProductDetails.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return invoiceProductMasterService.deleteTaxProduct(taxProductDetails, userInfo);

	}

	/**
	 * This method will is used to make a new entry to invoiceproductmaster table.
	 * 
	 * @param inputMap map object holding params ( invoiceproductmaster
	 *                 [invoiceproductmaster] object holding details to be added in
	 *                 invoiceproductmaster table, userinfo [UserInfo] holding
	 *                 logged in user details ) Input:{ "invoiceproductmaster ": {
	 *                 "sproductname"}, "userinfo":{ "activelanguagelist":
	 *                 ["en-US"], "isutcenabled": 4,"ndeptcode": -
	 *                 1,"ndeputyusercode": -1,"ndeputyuserrole": -1, "nformcode":
	 *                 33,"nmastersitecode": -1,"nmodulecode": 1, "nreasoncode":
	 *                 0,"nsitecode": 1,"ntimezonecode": -1,"ntranssitecode":
	 *                 1,"nusercode": -1, "nuserrole": -1,"nusersitecode":
	 *                 -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss", "sgmtoffset":
	 *                 "UTC +00:00","slanguagefilename":
	 *                 "Msg_en_US","slanguagename": "English", "slanguagetypecode":
	 *                 "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss","sreason":
	 *                 "","ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy
	 *                 HH:mm:ss"} }
	 * @return ResponseEntity with string message as 'Already Exists' if the
	 *         invoiceproductmaster already exists/ list of invoiceproductmaster
	 *         along with the newly added invoiceproductmaster .
	 * @throws Exception exception
	 */
	@PostMapping("/createInvoiceProductFile")
	public ResponseEntity<Object> createInvoiceProductFile(MultipartHttpServletRequest request) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.readValue(request.getParameter("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return invoiceProductMasterService.createInvoiceProductFile(request, userInfo);

	}

	/**
	 * This method is used to update selected invoiceproductmaster details.
	 * 
	 * @param inputMap [map object holding params( invoiceproductmaster
	 *                 [invoiceproductmaster] object holding details to be updated
	 *                 in invoiceproductmaster table, userinfo [UserInfo] holding
	 *                 logged in user details) Input:{ "invoiceproductmaster ": {
	 *                 "sproductname"}, "userinfo":{ "activelanguagelist":
	 *                 ["en-US"],"isutcenabled": 4,"ndeptcode":
	 *                 -1,"ndeputyusercode": -1,"ndeputyuserrole": -1, "nformcode":
	 *                 33,"nmastersitecode": -1, "nmodulecode": 1, "nreasoncode":
	 *                 0,"nsitecode": 1,"ntimezonecode": -1,"ntranssitecode":
	 *                 1,"nusercode": -1, "nuserrole": -1,"nusersitecode":
	 *                 -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss", "sgmtoffset":
	 *                 "UTC +00:00", "slanguagefilename":
	 *                 "Msg_en_US","slanguagename": "English", "slanguagetypecode":
	 *                 "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss", "sreason": "",
	 *                 "ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy
	 *                 HH:mm:ss"}}
	 * 
	 * 
	 * @return ResponseEntity with string message as 'Already Deleted' if the
	 *         invoiceproductmaster record is not available/ list of all
	 *         invoiceproductmaster and along with the updated invoiceproductmaster.
	 * @throws Exception exception
	 */
	@PostMapping("/updateInvoiceProductFile")
	public ResponseEntity<Object> updateInvoiceProductFile(MultipartHttpServletRequest request) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.readValue(request.getParameter("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return invoiceProductMasterService.updateInvoiceProductFile(request, userInfo);

	}

	/**
	 * This method is used to delete an entry in invoiceproductmaster table
	 * 
	 * @param inputMap [Map] object with keys of invoiceproductmaster entity and
	 *                 UserInfo object. Input:{ "invoiceproductmaster":
	 *                 {"ninvoiceproductmaster":1}, "userinfo":{
	 *                 "activelanguagelist": ["en-US"],"isutcenabled":
	 *                 4,"ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole":
	 *                 -1, "nformcode": 33,"nmastersitecode": -1, "nmodulecode": 1,
	 *                 "nreasoncode": 0,"nsitecode": 1,"ntimezonecode":
	 *                 -1,"ntranssitecode": 1,"nusercode": -1, "nuserrole":
	 *                 -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy
	 *                 HH:mm:ss", "sgmtoffset": "UTC +00:00", "slanguagefilename":
	 *                 "Msg_en_US","slanguagename": "English", "slanguagetypecode":
	 *                 "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss", "sreason": "",
	 *                 "ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy
	 *                 HH:mm:ss"}}
	 * @return ResponseEntity with string message as 'Already deleted' if the
	 *         invoiceproductmaster record is not available/ string message as
	 *         'Record is used in....' when the invoiceproductmaster is associated
	 *         in transaction / list of all invoiceproductmaster excluding the
	 *         deleted record
	 * @throws Exception exception
	 */
	@PostMapping("/deleteInvoiceProductFile")
	public ResponseEntity<Object> deleteInvoiceProductFile(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final InvoiceProductFile objProductFile = objMapper.convertValue(inputMap.get("invoiceproductfile"),
				InvoiceProductFile.class);
		requestContext.setUserInfo(userInfo);
		return invoiceProductMasterService.deleteInvoiceProductFile(objProductFile, userInfo);
	}

	/**
	 * This method is used to update selected invoiceproductmaster details.
	 * 
	 * @param inputMap [map object holding params( invoiceproductmaster
	 *                 [invoiceproductmaster] object holding details to be updated
	 *                 in invoiceproductmaster table, userinfo [UserInfo] holding
	 *                 logged in user details) Input:{ "invoiceproductmaster ": {
	 *                 "sproductname"}, "userinfo":{ "activelanguagelist":
	 *                 ["en-US"],"isutcenabled": 4,"ndeptcode":
	 *                 -1,"ndeputyusercode": -1,"ndeputyuserrole": -1, "nformcode":
	 *                 33,"nmastersitecode": -1, "nmodulecode": 1, "nreasoncode":
	 *                 0,"nsitecode": 1,"ntimezonecode": -1,"ntranssitecode":
	 *                 1,"nusercode": -1, "nuserrole": -1,"nusersitecode":
	 *                 -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss", "sgmtoffset":
	 *                 "UTC +00:00", "slanguagefilename":
	 *                 "Msg_en_US","slanguagename": "English", "slanguagetypecode":
	 *                 "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss", "sreason": "",
	 *                 "ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy
	 *                 HH:mm:ss"}}
	 * 
	 * 
	 * @return ResponseEntity with string message as 'Already Deleted' if the
	 *         invoiceproductmaster record is not available/ list of all
	 *         invoiceproductmaster and along with the updated invoiceproductmaster.
	 * @throws Exception exception
	 */
	@PostMapping("/editInvoiceProductFile")
	public ResponseEntity<Object> editInvoiceProductFile(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final InvoiceProductFile objProductFile = objMapper.convertValue(inputMap.get("productfile"),
				InvoiceProductFile.class);
		requestContext.setUserInfo(userInfo);
		return invoiceProductMasterService.editInvoiceProductFile(objProductFile, userInfo);

	}

	/**
	 * This method is used to fetch the active InvoiceProductmaster objects for the
	 * specified InvoiceProductmaster name and site.
	 * 
	 * @param ntypecode       [String] name of the InvoiceProductmaster
	 * @param nmasterSiteCode [int] site code of the InvoiceProductmaster
	 * @return list of active InvoiceProductmaster code(s) based on the specified
	 *         InvoiceProductmaster name and site
	 * @throws Exception
	 */
	@PostMapping("/viewAttachedInvoiceProductFile")
	public ResponseEntity<Object> viewAttachedInvoiceProductFile(@RequestBody Map<String, Object> inputMap,
			HttpServletResponse response) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final InvoiceProductFile objProductFile = objMapper.convertValue(inputMap.get("productfile"),
				InvoiceProductFile.class);
		Map<String, Object> outputMap = invoiceProductMasterService.viewAttachedInvoiceProductFile(objProductFile,
				userInfo);
		requestContext.setUserInfo(userInfo);
		return new ResponseEntity<Object>(outputMap, HttpStatus.OK);
	}

	/**
	 * This method is used to fetch the active InvoiceProductmaster objects for the
	 * specified InvoiceProductmaster name and site.
	 * 
	 * @param ntypecode       [String] name of the InvoiceProductmaster
	 * @param nmasterSiteCode [int] site code of the InvoiceProductmaster
	 * @return list of active InvoiceProductmaster code(s) based on the specified
	 *         InvoiceProductmaster name and site
	 * @throws Exception
	 */
	@PostMapping("/getSearchFieldData")
	public ResponseEntity<Object> getSearchFieldData(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		// final Map<String, Object> quotation = (Map<String, Object>)
		// inputMap.get("quotation");
		requestContext.setUserInfo(userInfo);
		return invoiceProductMasterService.getSearchFieldData(inputMap, userInfo);

	}

	/**
	 * This method is used to fetch the active InvoiceProductmaster objects for the
	 * specified InvoiceProductmaster name and site.
	 * 
	 * @param ntypecode       [String] name of the InvoiceProductmaster
	 * @param nmasterSiteCode [int] site code of the InvoiceProductmaster
	 * @return list of active InvoiceProductmaster code(s) based on the specified
	 *         InvoiceProductmaster name and site
	 * @throws Exception
	 */
	@PostMapping("/getProductTests")
	public ResponseEntity<Object> getProductTests(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userinfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userinfo);

		return invoiceProductMasterService.getProductMaster(userinfo);

	}

	/**
	 * This method is used to fetch the active InvoiceProductmaster objects for the
	 * specified InvoiceProductmaster name and site.
	 * 
	 * @param ntypecode       [String] name of the InvoiceProductmaster
	 * @param nmasterSiteCode [int] site code of the InvoiceProductmaster
	 * @return list of active InvoiceProductmaster code(s) based on the specified
	 *         InvoiceProductmaster name and site
	 * @throws Exception
	 */
	@PostMapping("/getInvoiceTaxtype")
	public ResponseEntity<Object> getTaxtype(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();

		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		int Productcode = (int) inputMap.get("nproductcode");

		return invoiceProductMasterService.getTaxtype(userInfo, Productcode, inputMap);

	}
}
