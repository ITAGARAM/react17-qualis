package com.agaramtech.qualis.barcodeprinting.service.RepositoryIdBarcode;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.agaramtech.qualis.global.UserInfo;


@Service
@Transactional(readOnly = true, rollbackFor = Exception.class)
public class RepositoryIdBarcodeServiceImpl implements RepositoryIdBarcodeService {
	
	private final RepositoryIdBarcodeDAO repositoryIdBarcodeDAO;

	public RepositoryIdBarcodeServiceImpl(RepositoryIdBarcodeDAO repositoryIdBarcodeDAO) {
		this.repositoryIdBarcodeDAO = repositoryIdBarcodeDAO;
	}

	@Override
	public ResponseEntity<Object> getRepositoryIdBarcode(UserInfo userInfo,int nbarcodefiltertypecode) throws Exception {
		return repositoryIdBarcodeDAO.getRepositoryIdBarcode(userInfo,nbarcodefiltertypecode);
	}


}
