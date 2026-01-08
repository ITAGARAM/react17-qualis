package com.agaramtech.qualis.invoice.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;

import org.springframework.jdbc.core.RowMapper;

import com.agaramtech.qualis.global.CustomizedResultsetRowMapper;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * This class is used to map the fields of 'sitefieldmaster' table of the
 * Database.
 */
@Entity
@Table(name = "sitefieldmaster ")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class SiteFieldMaster extends CustomizedResultsetRowMapper<SiteFieldMaster>
		implements Serializable, RowMapper<SiteFieldMaster> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nsitefieldcode")
	private short nsitefieldcode;
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode;
	@Column(name = "nformcode", nullable = false)
	private short nformcode;
	@Column(name = "nfieldcode", nullable = false)
	private short nfieldcode;
	@Column(name = "nneedfield", nullable = false)
	private short nneedfield = 4;
	@Column(name = "nisbarcodecontrol", nullable = false)
	private short nisbarcodecontrol = 4;
	@Column(name = "nisreportcontrol", nullable = false)
	private short nisreportcontrol = 4;
	@Column(name = "nisemailrequired", nullable = false)
	private short nisemailrequired = 4;
	@Column(name = "nstatus", nullable = false)
	private short nstatus = 1;
	@Column(name = "dmodifieddate")
	private Instant dmodifieddate;

	private transient String sdisplayname;
	private transient String scontrolids;
	private transient String scontrolname;

	@Override
	public SiteFieldMaster mapRow(ResultSet arg0, int arg1) throws SQLException {
		SiteFieldMaster objSiteFieldMaster = new SiteFieldMaster();
		objSiteFieldMaster.setNsitecode(getShort(arg0, "nsitefieldcode", arg1));
		objSiteFieldMaster.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objSiteFieldMaster.setNformcode(getShort(arg0, "nformcode", arg1));
		objSiteFieldMaster.setNfieldcode(getShort(arg0, "nfieldcode", arg1));
		objSiteFieldMaster.setNneedfield(getShort(arg0, "nneedfield", arg1));
		objSiteFieldMaster.setNisbarcodecontrol(getShort(arg0, "nisbarcodecontrol", arg1));
		objSiteFieldMaster.setNisreportcontrol(getShort(arg0, "nisreportcontrol", arg1));
		objSiteFieldMaster.setNisemailrequired(getShort(arg0, "nisemailrequired", arg1));
		objSiteFieldMaster.setNstatus(getShort(arg0, "nstatus", arg1));
		objSiteFieldMaster.setSdisplayname(getString(arg0, "sdisplayname", arg1));
		objSiteFieldMaster.setScontrolids(getString(arg0, "scontrolids", arg1));
		objSiteFieldMaster.setScontrolname(getString(arg0, "scontrolname", arg1));
		objSiteFieldMaster.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		return objSiteFieldMaster;
	}

}
