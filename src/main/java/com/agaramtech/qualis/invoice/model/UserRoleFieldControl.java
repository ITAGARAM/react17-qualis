package com.agaramtech.qualis.invoice.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Map;
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
 * This class is used to map the fields of 'userrolefieldcontrol' table of the
 * Database.
 */
@Entity
@Table(name = "userrolefieldcontrol")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class UserRoleFieldControl extends CustomizedResultsetRowMapper<UserRoleFieldControl>
		implements Serializable, RowMapper<UserRoleFieldControl> {

	private static final long serialVersionUID = 1L;
	@Id
	@Column(name = "nuserrolefieldcontrolcode")
	private int nuserrolefieldcontrolcode;
	@Column(name = "nformcode", nullable = false)
	private short nformcode;
	@Column(name = "nuserrolecode", nullable = false)
	private int nuserrolecode;
	@Column(name = "nfieldcode", nullable = false)
	private short nfieldcode;
	@Column(name = "nneedrights", nullable = false)
	private short nneedrights;
	@Column(name = "nneedfield", nullable = false)
	private short nneedfield = 4;
	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;
	@Column(name = "nstatus", nullable = false)
	@ColumnDefault("1")
	private short nstatus = 1;

	private transient String scontrolname;
	private transient String scontrolids;
	private transient int npropertiescode;
	private transient int npropertytypecode;
	private transient String spropertyname;
	private transient String spropertiesvalue;
	private transient int nisesigncontrol = 4;
	private transient int nauditpropertiescode;
	private transient String sauditpropertyname;
	private transient String sauditpropertyvalue;
	private transient String sformname;
	private transient String sdisplayname;

	private transient int nsitecontrolcode;
	private transient int nsitecode;
	private transient String screenname;
	private transient String ssubfoldername;
	private transient int nisbarcodecontrol;
	transient private int needesignsparent;
	private transient Map<String, Object> jsondata;
	private transient String sdefaultname;
	private transient Short nisdistributedsite;
	private transient Short nisprimarysyncsite;
	private transient String sfieldname;
	private transient int nisfieldcontrol;
	private transient int ncontrolcode;
	private transient int nuserrolecontrolcode;
	private transient int nuserrolescreencode;
	private transient int nneedesign;
	private transient int nuserrolefieldcode;

	@Override
	public UserRoleFieldControl mapRow(ResultSet arg0, int arg1) throws SQLException {
		UserRoleFieldControl objUserRoleFieldControl = new UserRoleFieldControl();
		objUserRoleFieldControl.setNstatus(getShort(arg0, "nstatus", arg1));
		objUserRoleFieldControl.setNfieldcode(getShort(arg0, "nfieldcode", arg1));
		objUserRoleFieldControl.setScontrolname(getString(arg0, "scontrolname", arg1));
		objUserRoleFieldControl.setNformcode(getShort(arg0, "nformcode", arg1));
		objUserRoleFieldControl.setSauditpropertyname(getString(arg0, "sauditpropertyname", arg1));
		objUserRoleFieldControl.setSauditpropertyvalue(getString(arg0, "sauditpropertyvalue", arg1));
		objUserRoleFieldControl.setNauditpropertiescode(getInteger(arg0, "nauditpropertiescode", arg1));
		objUserRoleFieldControl.setSformname(getString(arg0, "sformname", arg1));
		objUserRoleFieldControl.setNisesigncontrol(getInteger(arg0, "nisesigncontrol", arg1));
		objUserRoleFieldControl.setNisbarcodecontrol(getInteger(arg0, "nisbarcodecontrol", arg1));
		objUserRoleFieldControl.setSdisplayname(getString(arg0, "sdisplayname", arg1));
		objUserRoleFieldControl.setScontrolids(getString(arg0, "scontrolids", arg1));
		objUserRoleFieldControl.setNsitecontrolcode(getInteger(arg0, "nsitecontrolcode", arg1));
		objUserRoleFieldControl.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objUserRoleFieldControl.setNneedfield(getShort(arg0, "nneedfield", arg1));
		objUserRoleFieldControl.setNneedrights(getShort(arg0, "nneedrights", arg1));
		objUserRoleFieldControl.setNuserrolecode(getInteger(arg0, "nuserrolecode", arg1));
		objUserRoleFieldControl.setScreenname(getString(arg0, "screenname", arg1));
		objUserRoleFieldControl.setSsubfoldername(getString(arg0, "ssubfoldername", arg1));
		objUserRoleFieldControl.setNeedesignsparent(getInteger(arg0, "needesignsparent", arg1));
		objUserRoleFieldControl.setJsondata(getJsonObject(arg0, "jsondata", arg1));
		objUserRoleFieldControl.setSdefaultname(getString(arg0, "sdefaultname", arg1));
		objUserRoleFieldControl.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		objUserRoleFieldControl.setNisdistributedsite(getShort(arg0, "nisdistributedsite", arg1));
		objUserRoleFieldControl.setNisprimarysyncsite(getShort(arg0, "nisprimarysyncsite", arg1));
		objUserRoleFieldControl.setSfieldname(getString(arg0, "sfieldname", arg1));
		objUserRoleFieldControl.setNisfieldcontrol(getShort(arg0, "nisfieldcontrol", arg1));
		objUserRoleFieldControl.setNcontrolcode(getInteger(arg0, "ncontrolcode", arg1));
		objUserRoleFieldControl.setNuserrolecontrolcode(getInteger(arg0, "nuserrolecontrolcode", arg1));
		objUserRoleFieldControl.setNuserrolescreencode(getInteger(arg0, "nuserrolescreencode", arg1));
		objUserRoleFieldControl.setNneedesign(getInteger(arg0, "nneedesign", arg1));
		objUserRoleFieldControl.setNuserrolefieldcode(getInteger(arg0, "nuserrolefieldcode", arg1));

		return objUserRoleFieldControl;
	}

}
