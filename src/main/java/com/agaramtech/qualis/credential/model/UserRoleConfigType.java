package com.agaramtech.qualis.credential.model;

/**
 * This class is used to map fields of 'UserRoleConfigType' table of database
*/
import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Map;

import org.hibernate.annotations.ColumnDefault;
import org.springframework.jdbc.core.RowMapper;

import com.agaramtech.qualis.global.CustomizedResultsetRowMapper;
import com.agaramtech.qualis.global.Enumeration;

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
@Table(name = "userroleconfigtype")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class UserRoleConfigType extends CustomizedResultsetRowMapper<UserRoleConfigType>
		implements Serializable, RowMapper<UserRoleConfigType> {

	private static final long serialVersionUID = 1L;
	@Id
	@Column(name = "nuserroleconfigtypecode")
	private int nuserroleconfigtypecode;

	@Column(name = "suserroleconfigtype", length = 100, nullable = false)
	private String suserroleconfigtype;

	@Column(name = "sdescription", length = 200, nullable = false)
	private String sdescription;

	@Lob
	@Column(name = "jsondata", columnDefinition = "jsonb")
	private Map<String, Object> jsondata;

	@ColumnDefault("1")
	@Column(name = "nsorter", nullable = false)
	private short nsorter;

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode;

	@Column(name = "dmodifieddate")
	private Instant dmodifieddate;

	@Transient
	private transient String sdisplayname;

	@Override
	public UserRoleConfigType mapRow(final ResultSet arg0, final int arg1) throws SQLException {

		final UserRoleConfigType objUserRoleConfigType = new UserRoleConfigType();

		objUserRoleConfigType.setNuserroleconfigtypecode(getInteger(arg0, "nuserroleconfigtypecode", arg1));
		objUserRoleConfigType.setSuserroleconfigtype(getString(arg0, "suserroleconfigtype", arg1));
		objUserRoleConfigType.setNsorter(getShort(arg0, "nsorter", arg1));
		objUserRoleConfigType.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objUserRoleConfigType.setSdisplayname(getString(arg0, "sdisplayname", arg1));
		objUserRoleConfigType.setNstatus(getShort(arg0, "nstatus", arg1));
		objUserRoleConfigType.setSdescription(getString(arg0, "sdescription", arg1));
		objUserRoleConfigType.setJsondata(getJsonObject(arg0, "jsondata", arg1));
		objUserRoleConfigType.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));

		return objUserRoleConfigType;
	}

}
