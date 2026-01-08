package com.agaramtech.qualis.barcodeprinting.service.ParentSampleBarcode;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.agaramtech.qualis.global.UserInfo;

@Service
@Transactional(readOnly = true, rollbackFor = Exception.class)
public class ParentSampleBarcodeServiceImpl implements ParentSampleBarcodeService {

	private final ParentSampleBarcodeDAO parentSampleBarcodeDAO;

	public ParentSampleBarcodeServiceImpl(ParentSampleBarcodeDAO parentSampleBarcodeDAO) {
		this.parentSampleBarcodeDAO = parentSampleBarcodeDAO;
	}

	@Override
	public ResponseEntity<Object> getParentSampleBarcode(UserInfo userInfo) throws Exception {
		return parentSampleBarcodeDAO.getParentSampleBarcode(userInfo);
	}

	@Override
	public ResponseEntity<Object> getPrinter(UserInfo userInfo) throws Exception {
		return parentSampleBarcodeDAO.getPrinter(userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> PrintBarcode(Map<String, Object> inputMap) throws Exception {
		return parentSampleBarcodeDAO.PrintBarcode(inputMap);
	}
	
	// modified by sujatha ATE_274 by adding userInfo for throwing alert if sql query not there in table
	@Override
	public ResponseEntity<Object> getControlBasedBarcode(final int ncontrolcode, final UserInfo userInfo) throws Exception {
		return parentSampleBarcodeDAO.getControlBasedBarcode(ncontrolcode, userInfo);
	}

	@Override
	public ResponseEntity<Object> getParentSampleBarcodedata(Map<String, Object> inputMap) throws Exception {
		return parentSampleBarcodeDAO.getParentSampleBarcodedata(inputMap);
	}

}
