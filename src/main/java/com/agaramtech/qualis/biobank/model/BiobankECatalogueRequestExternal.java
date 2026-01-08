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

/**
 * Maps to 'biobankecataloguerequestexternal' table.
 */
@Entity
@Table(name = "biobankecataloguerequestexternal")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class BiobankECatalogueRequestExternal
        extends CustomizedResultsetRowMapper<BiobankECatalogueRequestExternal>
        implements Serializable, RowMapper<BiobankECatalogueRequestExternal> {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "nbiobankecatreqexternalcode", nullable = false)
    private int nbiobankecatreqexternalcode;

    @Id
    @Column(name = "nsitecode", nullable = false)
    @ColumnDefault("-1")
    private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

    @Column(name = "sformnumber", length = 50, nullable = false)
    private String sformnumber;

    @Column(name = "nreqformtypecode", nullable = false)
    private short nreqformtypecode;
    
    @Column(name = "nthirdpartycode", nullable = false)
    private int nthirdpartycode;

    @Column(name = "nbioprojectcode", nullable = false)
    private int nbioprojectcode;

    @Column(name = "nreceiversitecode", nullable = false)
    private short nreceiversitecode;

    @Column(name = "drequesteddate", nullable = false)
    private Instant drequesteddate;

    @Column(name = "ntzrequesteddate", nullable = false)
    @ColumnDefault("-1")
    private short ntzrequesteddate = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

    @Column(name = "noffsetdrequesteddate", nullable = false)
    @ColumnDefault("0")
    private int noffsetdrequesteddate = Enumeration.TransactionStatus.NON_EMPTY.gettransactionstatus();

    @Column(name = "ntransactionstatus", nullable = false)
    @ColumnDefault("8")
    private short ntransactionstatus = (short) Enumeration.TransactionStatus.DRAFT.gettransactionstatus();

    @Column(name = "sremarks", length = 300)
    private String sremarks;

    @Column(name = "sapprovalremarks", length = 300)
    private String sapprovalremarks;

    @Column(name = "dtransactiondate", nullable = false)
    private Instant dtransactiondate;

    @Column(name = "ntztransactiondate", nullable = false)
    @ColumnDefault("-1")
    private int ntztransactiondate = Enumeration.TransactionStatus.NA.gettransactionstatus();

    @Column(name = "noffsetdtransactiondate", nullable = false)
    @ColumnDefault("0")
    private int noffsetdtransactiondate = Enumeration.TransactionStatus.NON_EMPTY.gettransactionstatus();

    @Column(name = "nstatus", nullable = false)
    @ColumnDefault("1")
    private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
    
 // Transient fields
    @Transient private transient String sreceivingsitename;
    @Transient private transient String stransactionstatus;
    @Transient private transient String srequesteddate;
    @Transient private transient String stransactiondate;
    @Transient private transient String sprojecttitles;
    @Transient private transient String scolorhexcode;
    @Transient private transient String sreqformtypename;
    @Transient private transient String sthirdpartyname;


    @Override
    public BiobankECatalogueRequestExternal mapRow(ResultSet rs, int rowNum) throws SQLException {
        BiobankECatalogueRequestExternal objBiobankECatalogueRequestExternal = new BiobankECatalogueRequestExternal();
        objBiobankECatalogueRequestExternal.setNbiobankecatreqexternalcode(getInteger(rs, "nbiobankecatreqexternalcode", rowNum));
        objBiobankECatalogueRequestExternal.setNsitecode(getShort(rs, "nsitecode", rowNum));
        objBiobankECatalogueRequestExternal.setSformnumber(getString(rs, "sformnumber", rowNum));
        objBiobankECatalogueRequestExternal.setNreqformtypecode(getShort(rs, "nreqformtypecode", rowNum));
        objBiobankECatalogueRequestExternal.setNthirdpartycode(getInteger(rs, "nthirdpartycode", rowNum));
        objBiobankECatalogueRequestExternal.setNbioprojectcode(getInteger(rs, "nbioprojectcode", rowNum));
        objBiobankECatalogueRequestExternal.setNreceiversitecode(getShort(rs, "nreceiversitecode", rowNum));
        objBiobankECatalogueRequestExternal.setDrequesteddate(getInstant(rs, "drequesteddate", rowNum));
        objBiobankECatalogueRequestExternal.setNtzrequesteddate(getShort(rs, "ntzrequesteddate", rowNum));
        objBiobankECatalogueRequestExternal.setNoffsetdrequesteddate(getInteger(rs, "noffsetdrequesteddate", rowNum));
        objBiobankECatalogueRequestExternal.setNtransactionstatus(getShort(rs, "ntransactionstatus", rowNum));
        objBiobankECatalogueRequestExternal.setSremarks(getString(rs, "sremarks", rowNum));
        objBiobankECatalogueRequestExternal.setSapprovalremarks(getString(rs, "sapprovalremarks", rowNum));
        objBiobankECatalogueRequestExternal.setDtransactiondate(getInstant(rs, "dtransactiondate", rowNum));
        objBiobankECatalogueRequestExternal.setNtztransactiondate(getInteger(rs, "ntztransactiondate", rowNum));
        objBiobankECatalogueRequestExternal.setNoffsetdtransactiondate(getInteger(rs, "noffsetdtransactiondate", rowNum));
        objBiobankECatalogueRequestExternal.setNstatus(getShort(rs, "nstatus", rowNum));
        
        objBiobankECatalogueRequestExternal.setSreceivingsitename(getString(rs, "sreceivingsitename", rowNum));
        objBiobankECatalogueRequestExternal.setSreqformtypename(getString(rs, "sreqformtypename", rowNum));
        objBiobankECatalogueRequestExternal.setSprojecttitles(getString(rs, "sprojecttitles", rowNum));
        objBiobankECatalogueRequestExternal.setStransactionstatus(getString(rs, "stransactionstatus", rowNum));
        objBiobankECatalogueRequestExternal.setSrequesteddate(getString(rs, "srequesteddate", rowNum));
        objBiobankECatalogueRequestExternal.setStransactiondate(getString(rs, "stransactiondate", rowNum));
        objBiobankECatalogueRequestExternal.setScolorhexcode(getString(rs, "scolorhexcode", rowNum));
        objBiobankECatalogueRequestExternal.setSthirdpartyname(getString(rs, "sthirdpartyname", rowNum));

        
        return objBiobankECatalogueRequestExternal;
    }
}
