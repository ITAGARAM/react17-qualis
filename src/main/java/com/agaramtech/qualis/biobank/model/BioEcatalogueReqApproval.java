package com.agaramtech.qualis.biobank.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.jdbc.core.RowMapper;
import com.agaramtech.qualis.global.CustomizedResultsetRowMapper;
import com.agaramtech.qualis.global.Enumeration;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "bioecataloguereqapproval")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class BioEcatalogueReqApproval extends CustomizedResultsetRowMapper<BioEcatalogueReqApproval>
        implements Serializable, RowMapper<BioEcatalogueReqApproval> {

    private static final long serialVersionUID = 1L;
    
    @Id
    @Column(name = "necatrequestreqapprovalcode", nullable = false)
    private int necatrequestreqapprovalcode;
    
    @Column(name = "ntransfertype")
    private short ntransfertype;
    
    @Column(name = "norginsitecode", nullable = false)
    private short norginsitecode;
    
    @Column(name = "nreqformtypecode", nullable = false)
    private short nreqformtypecode;
    
    @Column(name = "norginthirdpartycode")
    private int norginthirdpartycode;
    
    @Column(name = "sformnumber", length = 50)
    private String sformnumber;
    
    @Column(name = "drequesteddate")
    private Instant drequesteddate;
    
    @Column(name = "ntzrequesteddate", nullable = false)
    private short ntzrequesteddate;
    
    @Column(name = "noffsetdrequesteddate", nullable = false)
    private int noffsetdrequesteddate;
    
    @Column(name = "ntransactionstatus", nullable = false)
    private short ntransactionstatus;
    
    @Column(name = "sapprovalremarks", length = 255)
    private String sapprovalremarks;
    
    @Column(name = "dtransactiondate", nullable = false)
    private Instant dtransactiondate;
    
    @Column(name = "ntztransactiondate", nullable = false)
    private int ntztransactiondate;

    @Column(name = "noffsetdtransactiondate", nullable = false)
	private int noffsetdtransactiondate;
    
	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();
    
	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
    
	@Transient
	private transient String scolorhexcode;
	
	@Transient
	private transient String stransdisplaystatus;
	
	@Transient
	private transient String ssitename;	
	
	@Transient
	private transient String sthirdpartyname;
	
    @Transient 
    private transient String strrequesteddate;

    @Transient 
    private transient String sselectedsitename; 

    @Override
    public BioEcatalogueReqApproval mapRow(final ResultSet arg0, final int arg1) throws SQLException {

        final var objbioEcatalogueReqApproval = new BioEcatalogueReqApproval();

        objbioEcatalogueReqApproval.setNecatrequestreqapprovalcode(getInteger(arg0, "necatrequestreqapprovalcode", arg1));
        objbioEcatalogueReqApproval.setSformnumber(getString(arg0, "sformnumber", arg1));
        objbioEcatalogueReqApproval.setNtransfertype(getShort(arg0, "ntransfertype", arg1));
        objbioEcatalogueReqApproval.setNorginthirdpartycode(getInteger(arg0, "norginthirdpartycode", arg1));
        objbioEcatalogueReqApproval.setNorginsitecode(getShort(arg0, "norginsitecode", arg1));
        objbioEcatalogueReqApproval.setNreqformtypecode(getShort(arg0, "nreqformtypecode", arg1));
        objbioEcatalogueReqApproval.setDrequesteddate(getInstant(arg0, "drequesteddate", arg1));
        objbioEcatalogueReqApproval.setNtzrequesteddate(getShort(arg0, "ntzrequesteddate", arg1));
        objbioEcatalogueReqApproval.setNoffsetdrequesteddate(getInteger(arg0, "noffsetdrequesteddate", arg1));
        objbioEcatalogueReqApproval.setNtransactionstatus(getShort(arg0, "ntransactionstatus", arg1));
        objbioEcatalogueReqApproval.setSapprovalremarks(getString(arg0, "sapprovalremarks", arg1));
        objbioEcatalogueReqApproval.setDtransactiondate(getInstant(arg0, "dtransactiondate", arg1));
        objbioEcatalogueReqApproval.setNtztransactiondate(getInteger(arg0, "ntztransactiondate", arg1));
        objbioEcatalogueReqApproval.setNoffsetdtransactiondate(getInteger(arg0, "noffsetdtransactiondate", arg1));
        objbioEcatalogueReqApproval.setNsitecode(getShort(arg0, "nsitecode", arg1));
        objbioEcatalogueReqApproval.setNstatus(getShort(arg0, "nstatus", arg1));
        objbioEcatalogueReqApproval.setScolorhexcode(getString(arg0, "scolorhexcode", arg1));
        objbioEcatalogueReqApproval.setStransdisplaystatus(getString(arg0, "stransdisplaystatus", arg1));
        objbioEcatalogueReqApproval.setSsitename(getString(arg0, "ssitename", arg1));
        objbioEcatalogueReqApproval.setSthirdpartyname(getString(arg0, "sthirdpartyname", arg1));
        objbioEcatalogueReqApproval.setStrrequesteddate(getString(arg0, "strrequesteddate", arg1));
        objbioEcatalogueReqApproval.setSselectedsitename(getString(arg0, "sselectedsitename", arg1));

        return objbioEcatalogueReqApproval;

    }
}
