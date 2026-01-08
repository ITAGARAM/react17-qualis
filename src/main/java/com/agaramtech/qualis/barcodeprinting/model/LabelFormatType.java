package com.agaramtech.qualis.barcodeprinting.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Map;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.jdbc.core.RowMapper;
import com.agaramtech.qualis.global.CustomizedResultsetRowMapper;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;


@Entity
@Table(name = "labelformattype")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class LabelFormatType extends CustomizedResultsetRowMapper<LabelFormatType> implements Serializable,RowMapper<LabelFormatType> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nlabelformattypecode ")
	private short nlabelformattypecode;
	
	@Column(name = "nlabelcolumncount ")
	private short nlabelcolumncount;
	
	@Lob
	@Column(name="jsondata",columnDefinition = "jsonb")
	private Map<String,Object> jsondata;
	
	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable=false)
	private short nsitecode;
	
	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus;
	
	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;
	
	@Transient
	private transient String slabelformattypename;
	
	public LabelFormatType mapRow(ResultSet arg0, int arg1) throws SQLException {
		final LabelFormatType objLabelFormatType = new LabelFormatType();
		objLabelFormatType.setNlabelformattypecode(getShort(arg0,"nlabelformattypecode",arg1));
		objLabelFormatType.setNlabelcolumncount(getShort(arg0,"nlabelcolumncount",arg1));
		objLabelFormatType.setNsitecode(getShort(arg0,"nsitecode",arg1));
		objLabelFormatType.setNstatus(getShort(arg0,"nstatus",arg1));
		objLabelFormatType.setJsondata(unescapeString(getJsonObject(arg0,"jsondata",arg1)));
		objLabelFormatType.setSlabelformattypename(getString(arg0,"slabelformattypename",arg1));
		objLabelFormatType.setDmodifieddate(getInstant(arg0,"dmodifieddate",arg1));
		return objLabelFormatType;
	}
}
