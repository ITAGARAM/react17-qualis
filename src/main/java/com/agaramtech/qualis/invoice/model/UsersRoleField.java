package com.agaramtech.qualis.invoice.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.jdbc.core.RowMapper;
import com.agaramtech.qualis.global.CustomizedResultsetRowMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * This class is used to map the fields of 'userrolefield' table of the
 * Database.
 */
@Entity
@Table(name = "userrolefield ")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class UsersRoleField extends CustomizedResultsetRowMapper<UsersRoleField>
		implements Serializable, RowMapper<UsersRoleField> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nuserrolefieldcode")
	private int nuserrolefieldcode;

	@Column(name = "nformcode", nullable = false)
	private short nformcode;

	@Column(name = "nuserrolecode", nullable = false)
	private int nuserrolecode;

	@Column(name = "nstatus", nullable = false)
	@ColumnDefault("1")
	private short nstatus = 1;

	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;

	private transient String sdisplayname;
	private transient String label;
	private transient String value;
	private transient Integer nsitecode;
	private transient int nusercode;

	@Override
	public UsersRoleField mapRow(ResultSet arg0, int arg1) throws SQLException {
		UsersRoleField objUsersRoleField = new UsersRoleField();
		objUsersRoleField.setNuserrolefieldcode(getInteger(arg0, "nuserrolefieldcode", arg1));
		objUsersRoleField.setNformcode(getShort(arg0, "nformcode", arg1));
		objUsersRoleField.setNuserrolecode(getInteger(arg0, "nuserrolecode", arg1));
		objUsersRoleField.setNstatus(getShort(arg0, "nstatus", arg1));
		objUsersRoleField.setSdisplayname(getString(arg0, "sdisplayname", arg1));
		objUsersRoleField.setLabel(getString(arg0, "label", arg1));
		objUsersRoleField.setValue(getString(arg0, "value", arg1));
		objUsersRoleField.setNsitecode(getInteger(arg0, "nsitecode", arg1));
		objUsersRoleField.setNusercode(getInteger(arg0, "nusercode", arg1));
		objUsersRoleField.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));

		return objUsersRoleField;
	}

}
