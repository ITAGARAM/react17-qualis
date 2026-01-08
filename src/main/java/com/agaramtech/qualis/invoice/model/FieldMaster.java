package com.agaramtech.qualis.invoice.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Map;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import org.springframework.jdbc.core.RowMapper;
import com.agaramtech.qualis.global.CustomizedResultsetRowMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * This class is used to map the fields of 'fieldmaster' table of the Database.
 */
@Entity
@Table(name = "fieldmaster")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@SuppressWarnings("serial")
public class FieldMaster extends CustomizedResultsetRowMapper<FieldMaster>
		implements Serializable, RowMapper<FieldMaster> {

	@Id
	@Column(name = "nfieldcode")
	private short nfieldcode;
	@Column(name = "nformcode", nullable = false)
	private short nformcode;
	@Column(name = "sfieldname", length = 50, nullable = false)
	private String sfieldname;
	@Lob
	@Column(name = "jsondata", columnDefinition = "jsonb")
	private Map<String, Object> jsondata;
	@Column(name = "nisfieldcontrol", nullable = false)
	private short nisfieldcontrol = 4;
	@Column(name = "nisdistributedsite", nullable = false)
	private short nisdistributedsite = 4;
	@Column(name = "nisprimarysyncsite", nullable = false)
	private short nisprimarysyncsite = 3;
	@Column(name = "nstatus", nullable = false)
	private short nstatus = 1;
	@Column(name = "dmodifieddate")
	private Instant dmodifieddate;

	private transient String scontrolids;
	transient private int npropertiescode;
	transient private int npropertytypecode;
	transient private String spropertyname;
	transient private String spropertiesvalue;
	transient private String screenname;
	transient private int nauditpropertiescode;
	transient private String sauditpropertyname;
	transient private String sauditpropertyvalue;
	transient private String sformname;
	transient private String sdisplayname;
	transient private int nsitecontrolcode;
	transient private short nsitecode;
	transient private int nuserrolecode;
	transient private int nuserrolefieldcontrolcode;
	transient private short nisbarcodecontrol;
	transient private short nneedrights;
	transient private String ssubfoldername;
	transient private int needesignsparent;
	transient private String sdefaultname;
	transient private int nuserrolefieldcode;

	@Override
	public FieldMaster mapRow(ResultSet arg0, int arg1) throws SQLException {
		FieldMaster objControlMaster = new FieldMaster();
		objControlMaster.setNstatus(getShort(arg0, "nstatus", arg1));
		objControlMaster.setNfieldcode(getShort(arg0, "nfieldcode", arg1));
		objControlMaster.setSfieldname(getString(arg0, "sfieldname", arg1));
		objControlMaster.setNformcode(getShort(arg0, "nformcode", arg1));
		objControlMaster.setNuserrolefieldcontrolcode(getInteger(arg0, "nuserrolefieldcontrolcode", arg1));
		objControlMaster.setNpropertiescode(getInteger(arg0, "npropertiescode", arg1));
		objControlMaster.setNpropertytypecode(getInteger(arg0, "npropertytypecode", arg1));
		objControlMaster.setSpropertyname(getString(arg0, "spropertyname", arg1));
		objControlMaster.setSpropertiesvalue(getString(arg0, "spropertiesvalue", arg1));
		objControlMaster.setSauditpropertyname(getString(arg0, "sauditpropertyname", arg1));
		objControlMaster.setSauditpropertyvalue(getString(arg0, "sauditpropertyvalue", arg1));
		objControlMaster.setNauditpropertiescode(getInteger(arg0, "nauditpropertiescode", arg1));
		objControlMaster.setSformname(getString(arg0, "sformname", arg1));
		objControlMaster.setNisfieldcontrol(getShort(arg0, "nisfieldcontrol", arg1));
		objControlMaster.setNisbarcodecontrol(getShort(arg0, "nisbarcodecontrol", arg1));
		objControlMaster.setSdisplayname(getString(arg0, "sdisplayname", arg1));
		objControlMaster.setScontrolids(getString(arg0, "scontrolids", arg1));
		objControlMaster.setNsitecontrolcode(getInteger(arg0, "nsitecontrolcode", arg1));
		objControlMaster.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objControlMaster.setNneedrights(getShort(arg0, "nneedrights", arg1));
		objControlMaster.setNuserrolecode(getInteger(arg0, "nuserrolecode", arg1));
		objControlMaster.setNisdistributedsite(getShort(arg0, "nisdistributedsite", arg1));
		objControlMaster.setScreenname(getString(arg0, "screenname", arg1));
		objControlMaster.setSsubfoldername(getString(arg0, "ssubfoldername", arg1));
		objControlMaster.setNeedesignsparent(getInteger(arg0, "needesignsparent", arg1));
		objControlMaster.setJsondata(getJsonObject(arg0, "jsondata", arg1));
		objControlMaster.setSdefaultname(getString(arg0, "sdefaultname", arg1));
		objControlMaster.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		objControlMaster.setNisprimarysyncsite(getShort(arg0, "nisprimarysyncsite", arg1));
		objControlMaster.setNuserrolefieldcode(getInteger(arg0, "nuserrolefieldcode", arg1));

		return objControlMaster;
	}

}
