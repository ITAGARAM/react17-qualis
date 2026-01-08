package com.agaramtech.qualis.subcontracttestdetail.pojo;

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

@Data
@Entity
@Table(name = "suppliertype")
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class SupplierType extends CustomizedResultsetRowMapper<SupplierType>
		implements Serializable, RowMapper<SupplierType> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nsuppliertypecode")
	private short nsuppliertypecode;
	@Lob
	@Column(name = "jsondata", columnDefinition = "jsonb")
	private Map<String, Object> jsondata;
	@ColumnDefault("3")
	@Column(name = "ndefaultstatus", nullable = false)
	private short ndefaultstatus = 3;
	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = -1;
	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus = 1;
	@Column(name = "dmodifieddate")
	private Instant dmodifieddate;

	@Transient
	private transient String ssuppliertypename;
	@Transient
	private transient String sdisplayname;
	@Transient
	private transient short nflag;
	@Transient
	private transient String sdefaultname;

	@Override
	public SupplierType mapRow(ResultSet arg0, int arg1) throws SQLException {

		final SupplierType objSupplierType = new SupplierType();
		objSupplierType.setNsuppliertypecode(getShort(arg0, "nsuppliertypecode", arg1));
		objSupplierType.setJsondata(getJsonObject(arg0, "jsondata", arg1));
		objSupplierType.setSsuppliertypename(getString(arg0, "ssuppliertypename", arg1));
		objSupplierType.setNdefaultstatus(getShort(arg0, "ndefaultstatus", arg1));
		objSupplierType.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objSupplierType.setNstatus(getShort(arg0, "nstatus", arg1));
		objSupplierType.setSdisplayname(getString(arg0, "sdisplayname", arg1));
		objSupplierType.setNflag(getShort(arg0, "nflag", arg1));
		objSupplierType.setSdefaultname(getString(arg0, "sdefaultname", arg1));
		objSupplierType.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));

		return objSupplierType;

	}

}
