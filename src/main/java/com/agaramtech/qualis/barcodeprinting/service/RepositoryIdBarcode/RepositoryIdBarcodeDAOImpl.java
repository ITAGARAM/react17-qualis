package com.agaramtech.qualis.barcodeprinting.service.RepositoryIdBarcode;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.agaramtech.qualis.barcodeprinting.model.BarcodeFilterType;
import com.agaramtech.qualis.barcodeprinting.service.ParentSampleBarcode.ParentSampleBarcodeDAO;
import com.agaramtech.qualis.biobank.model.BioParentSampleReceiving;
import com.agaramtech.qualis.exception.service.ExceptionLogDAOImpl;
import com.agaramtech.qualis.global.AuditUtilityFunction;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.FTPUtilityFunction;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.ProjectDAOSupport;
import com.agaramtech.qualis.global.StringUtilityFunction;
import com.agaramtech.qualis.global.UserInfo;

import lombok.AllArgsConstructor;

@SuppressWarnings("unused")
@AllArgsConstructor
@Repository
public class RepositoryIdBarcodeDAOImpl implements RepositoryIdBarcodeDAO {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(RepositoryIdBarcodeDAOImpl.class);

	private final StringUtilityFunction stringUtilityFunction;
	private final JdbcTemplate jdbcTemplate;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final CommonFunction commonFunction;
	private final ProjectDAOSupport projectDAOSupport;
	private final AuditUtilityFunction auditUtilityFunction;
	private final FTPUtilityFunction ftpUtilityFunction;

	@Override
	public ResponseEntity<Object> getRepositoryIdBarcode(UserInfo userInfo,int nbarcodefiltertypecode) throws Exception {

		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		String conditionValue="";
		if(nbarcodefiltertypecode!=-1) {
			conditionValue=" and nbarcodefiltertypecode="+nbarcodefiltertypecode;
		}
		final String strQuery = "select *,coalesce(jsondata->'sbarcodefiltertypename'->>'"
				+ userInfo.getSlanguagetypecode() + "',"
				+ " jsondata->'sbarcodefiltertypename'->>'en-US') as sdisplaystatus from barcodefiltertype where nformcode="+Enumeration.QualisForms.REPOSITORYIDBARCODE.getqualisforms()+" "+conditionValue+" order by  1";
		final List<BarcodeFilterType> lstBarcodeFilterType = (List<BarcodeFilterType>) jdbcTemplate.query(strQuery,
				new BarcodeFilterType());
		if (!lstBarcodeFilterType.isEmpty()) {

			String squery = (String) lstBarcodeFilterType.get(0).getSsqlquery();
			StringBuilder sbuilder1 = new StringBuilder();
			if (squery != null) {
				sbuilder1.append(squery);
				while (squery.contains("<@")) {
					int nStart = squery.indexOf("<@");
					int nEnd = squery.indexOf("@>");
					sbuilder1.replace(nStart, nEnd + 2, String.valueOf(userInfo.getNtranssitecode()));
					squery = sbuilder1.toString();
				}
			}
			final String lstQuery = squery;
			final List<Map<String, Object>> lstquery = jdbcTemplate.queryForList(lstQuery);;
			outputMap.put("RepositoryIdBarcode", lstquery);
			outputMap.put("selectedBarcodeFilterType", lstBarcodeFilterType.get(0));
		}
		if(nbarcodefiltertypecode==-1) { 
		outputMap.put("BarcodeFilterType", lstBarcodeFilterType);
		}
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

}
