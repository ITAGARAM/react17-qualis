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
 * Maps to 'biobankecataloguerequestexternalhistory' table.
 */
@Entity
@Table(name = "biobankecataloguerequestexternalhistory")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class BiobankECatalogueRequestExternalHistory
        extends CustomizedResultsetRowMapper<BiobankECatalogueRequestExternalHistory>
        implements Serializable, RowMapper<BiobankECatalogueRequestExternalHistory> {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "nbiobankecatreqexternalhistorycode", nullable = false)
    private int nbiobankecatreqexternalhistorycode;

    @Column(name = "nbiobankecatreqexternalcode", nullable = false)
    private int nbiobankecatreqexternalcode;

    @Column(name = "nusercode", nullable = false)
    @ColumnDefault("-1")
    private int nusercode = Enumeration.TransactionStatus.NA.gettransactionstatus();

    @Column(name = "nuserrolecode", nullable = false)
    @ColumnDefault("-1")
    private int nuserrolecode = Enumeration.TransactionStatus.NA.gettransactionstatus();

    @Column(name = "ndeputyusercode", nullable = false)
    private int ndeputyusercode;

    @Column(name = "ndeputyuserrolecode", nullable = false)
    private int ndeputyuserrolecode;

    @Column(name = "ntransactionstatus", nullable = false)
    private int ntransactionstatus;

    @Column(name = "scomments", length = 255)
    private String scomments;

    @Column(name = "dtransactiondate", nullable = false)
    private Instant dtransactiondate;

    @Column(name = "ntztransactiondate", nullable = false)
    @ColumnDefault("-1")
    private short ntztransactiondate = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

    @Column(name = "noffsettransactiondate", nullable = false)
    @ColumnDefault("0")
    private int noffsettransactiondate = Enumeration.TransactionStatus.NON_EMPTY.gettransactionstatus();

    @Id
    @Column(name = "nsitecode", nullable = false)
    @ColumnDefault("-1")
    private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

    @Column(name = "nstatus", nullable = false)
    @ColumnDefault("1")
    private int nstatus = Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
    
 // Transient fields
    @Transient private transient String susername;
    @Transient private transient String suserrolename;
    @Transient private transient String sdeputyusername;
    @Transient private transient String sdeputyuserrolename;
    @Transient private transient String stransactionstatus;
    @Transient private transient String stransactiondate;

    @Override
    public BiobankECatalogueRequestExternalHistory mapRow(ResultSet rs, int rowNum) throws SQLException {
        BiobankECatalogueRequestExternalHistory objBiobankECatalogueRequestExternalHistory = new BiobankECatalogueRequestExternalHistory();
        objBiobankECatalogueRequestExternalHistory.setNbiobankecatreqexternalhistorycode(getInteger(rs, "nbiobankecatreqexternalhistorycode", rowNum));
        objBiobankECatalogueRequestExternalHistory.setNbiobankecatreqexternalcode(getInteger(rs, "nbiobankecatreqexternalcode", rowNum));
        objBiobankECatalogueRequestExternalHistory.setNusercode(getInteger(rs, "nusercode", rowNum));
        objBiobankECatalogueRequestExternalHistory.setNuserrolecode(getInteger(rs, "nuserrolecode", rowNum));
        objBiobankECatalogueRequestExternalHistory.setNdeputyusercode(getInteger(rs, "ndeputyusercode", rowNum));
        objBiobankECatalogueRequestExternalHistory.setNdeputyuserrolecode(getInteger(rs, "ndeputyuserrolecode", rowNum));
        objBiobankECatalogueRequestExternalHistory.setNtransactionstatus(getInteger(rs, "ntransactionstatus", rowNum));
        objBiobankECatalogueRequestExternalHistory.setScomments(getString(rs, "scomments", rowNum));
        objBiobankECatalogueRequestExternalHistory.setDtransactiondate(getInstant(rs, "dtransactiondate", rowNum));
        objBiobankECatalogueRequestExternalHistory.setNtztransactiondate(getShort(rs, "ntztransactiondate", rowNum));
        objBiobankECatalogueRequestExternalHistory.setNoffsettransactiondate(getInteger(rs, "noffsettransactiondate", rowNum));
        objBiobankECatalogueRequestExternalHistory.setNsitecode(getShort(rs, "nsitecode", rowNum));
        objBiobankECatalogueRequestExternalHistory.setNstatus(getInteger(rs, "nstatus", rowNum));
        
        objBiobankECatalogueRequestExternalHistory.setSusername(getString(rs, "susername", rowNum));
        objBiobankECatalogueRequestExternalHistory.setSuserrolename(getString(rs, "suserrolename", rowNum));
        objBiobankECatalogueRequestExternalHistory.setSdeputyusername(getString(rs, "sdeputyusername", rowNum));
        objBiobankECatalogueRequestExternalHistory.setSdeputyuserrolename(getString(rs, "sdeputyuserrolename", rowNum));
        objBiobankECatalogueRequestExternalHistory.setStransactionstatus(getString(rs, "stransactionstatus", rowNum));
        objBiobankECatalogueRequestExternalHistory.setStransactiondate(getString(rs, "stransactiondate", rowNum));
        
        return objBiobankECatalogueRequestExternalHistory;
    }
}
