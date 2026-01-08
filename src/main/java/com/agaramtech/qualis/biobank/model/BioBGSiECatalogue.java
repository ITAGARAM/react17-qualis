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
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Maps to 'biobgsiecatalogue' table.
 */
@Entity
@Table(name = "biobgsiecatalogue")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class BioBGSiECatalogue extends CustomizedResultsetRowMapper<BioBGSiECatalogue>
        implements Serializable, RowMapper<BioBGSiECatalogue> {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "nbgsiecatrequestcode", nullable = false)
    private int nbgsiecatrequestcode;

    @Id
    @Column(name = "nsitecode", nullable = false)
    @ColumnDefault("-1")
    private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

    @Column(name = "sformnumber", length = 50, nullable = false)
    private String sformnumber;
    
    @Column(name = "nreqformtypecode", nullable = false)
    private short nreqformtypecode;

    @Column(name = "nreceiversitecode", nullable = false)
    private short nreceiversitecode;

    @Column(name = "nbioprojectcode", nullable = false)
    private int nbioprojectcode;
    
    @Column(name = "drequesteddate", nullable = false)
    private Instant drequesteddate;

    @ColumnDefault("-1")
    @Column(name = "ntzrequesteddate", nullable = false)
    private short ntzrequesteddate = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

    @ColumnDefault("0")
    @Column(name = "noffsetdrequesteddate", nullable = false)
    private int noffsetdrequesteddate = Enumeration.TransactionStatus.NON_EMPTY.gettransactionstatus();

    @ColumnDefault("8")
    @Column(name = "ntransactionstatus", nullable = false)
    private short ntransactionstatus = (short) Enumeration.TransactionStatus.DRAFT.gettransactionstatus();

    @Column(name = "sremarks", length = 300)
    private String sremarks;
    
    @Column(name = "sapprovalremarks", length = 300)
    private String sapprovalremarks;

    @Column(name = "dtransactiondate", nullable = false)
    private Instant dtransactiondate;

    @ColumnDefault("-1")
    @Column(name = "ntztransactiondate", nullable = false)
    private int ntztransactiondate = Enumeration.TransactionStatus.NA.gettransactionstatus();

    @ColumnDefault("0")
    @Column(name = "noffsetdtransactiondate", nullable = false)
    private int noffsetdtransactiondate = Enumeration.TransactionStatus.NON_EMPTY.gettransactionstatus();

    @ColumnDefault("1")
    @Column(name = "nstatus", nullable = false)
    private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

    // Transient fields
    @Transient private transient String sreceivingsitename;
    @Transient private transient String stransactionstatus;
    @Transient private transient String srequesteddate;
    @Transient private transient String stransactiondate;
    @Transient private transient String sprojecttitles;
    @Transient private transient String scolorhexcode;
    @Transient private transient String sreqformtypename;


    @Override
    public BioBGSiECatalogue mapRow(ResultSet rs, int rowNum) throws SQLException {
        BioBGSiECatalogue obj = new BioBGSiECatalogue();
        obj.setNbgsiecatrequestcode(getInteger(rs, "nbgsiecatrequestcode", rowNum));
        obj.setNsitecode(getShort(rs, "nsitecode", rowNum));
        obj.setSformnumber(getString(rs, "sformnumber", rowNum));
        obj.setNreqformtypecode(getShort(rs, "nreqformtypecode", rowNum));
        obj.setNreceiversitecode(getShort(rs, "nreceiversitecode", rowNum));
        obj.setDrequesteddate(getInstant(rs, "drequesteddate", rowNum));
        obj.setNtzrequesteddate(getShort(rs, "ntzrequesteddate", rowNum));
        obj.setNoffsetdrequesteddate(getInteger(rs, "noffsetdrequesteddate", rowNum));
        obj.setNtransactionstatus(getShort(rs, "ntransactionstatus", rowNum));
        obj.setSremarks(getString(rs, "sremarks", rowNum));
        obj.setSapprovalremarks(getString(rs, "sapprovalremarks", rowNum));
        obj.setDtransactiondate(getInstant(rs, "dtransactiondate", rowNum));
        obj.setNtztransactiondate(getInteger(rs, "ntztransactiondate", rowNum));
        obj.setNoffsetdtransactiondate(getInteger(rs, "noffsetdtransactiondate", rowNum));
        obj.setNstatus(getShort(rs, "nstatus", rowNum));

        obj.setSreceivingsitename(getString(rs, "sreceivingsitename", rowNum));
        obj.setSreqformtypename(getString(rs, "sreqformtypename", rowNum));
        obj.setSprojecttitles(getString(rs, "sprojecttitles", rowNum));
        obj.setStransactionstatus(getString(rs, "stransactionstatus", rowNum));
        obj.setSrequesteddate(getString(rs, "srequesteddate", rowNum));
        obj.setStransactiondate(getString(rs, "stransactiondate", rowNum));
        obj.setScolorhexcode(getString(rs, "scolorhexcode", rowNum));
        obj.setNbioprojectcode(getInteger(rs, "nbioprojectcode", rowNum));


        return obj;
    }
}
