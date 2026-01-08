package com.agaramtech.qualis.barcodeprinting.service.LocationBoxBarcode;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.agaramtech.qualis.global.UserInfo;

@Service
@Transactional(readOnly = true, rollbackFor = Exception.class)
public class LocationBoxBarcodeServiceImpl implements LocationBoxBarcodeService{
	
	private final LocationBoxBarcodeDAO locationBoxBarcodeDAO;

	public LocationBoxBarcodeServiceImpl(LocationBoxBarcodeDAO locationBoxBarcodeDAO) {
		this.locationBoxBarcodeDAO = locationBoxBarcodeDAO;
	}

	@Override
	public ResponseEntity<Object> getLocationBoxBarcode(UserInfo userInfo,int nbarcodefiltertypecode) throws Exception {
		return locationBoxBarcodeDAO.getLocationBoxBarcode(userInfo,nbarcodefiltertypecode);
	}

}
