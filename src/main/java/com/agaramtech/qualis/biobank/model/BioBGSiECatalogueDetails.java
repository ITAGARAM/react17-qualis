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
 * Maps to 'biobgsiecataloguedetails' table.
 */
@Entity
@Table(name = "biobgsiecataloguedetails")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class BioBGSiECatalogueDetails extends CustomizedResultsetRowMapper<BioBGSiECatalogueDetails>
        implements Serializable, RowMapper<BioBGSiECatalogueDetails> {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "nbgsiecatdetailcode", nullable = false)
    private int nbgsiecatdetailcode;

    @Column(name = "nbgsiecatrequestcode", nullable = false)
    private int nbgsiecatrequestcode;

    @Column(name = "nbioprojectcode", nullable = false)
    private int nbioprojectcode;

    @Column(name = "nproductcode", nullable = false)
    private int nproductcode;
    
    @Column(name ="sparentsamplecode", length = 30)
    private String sparentsamplecode;
    
    @Column(name = "nreqnoofsamples", nullable = false)
    private short nreqnoofsamples;
    
    @Column(name = "sreqminvolume", length = 10, nullable = false)
    private String sreqminvolume;
    
    @Column(name = "naccnoofsamples")
    private short naccnoofsamples;

    @Column(name = "saccminvolume", length = 10)
    private String saccminvolume;
    
    @Column(name = "dtransactiondate", nullable = false)
    private Instant dtransactiondate;

    @ColumnDefault("-1")
    @Column(name = "ntztransactiondate", nullable = false)
    private int ntztransactiondate = Enumeration.TransactionStatus.NA.gettransactionstatus();

    @ColumnDefault("0")
    @Column(name = "noffsetdtransactiondate", nullable = false)
    private int noffsetdtransactiondate = Enumeration.TransactionStatus.NON_EMPTY.gettransactionstatus();

    @Id
    @Column(name = "nsitecode", nullable = false)
    @ColumnDefault("-1")
    private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

    @ColumnDefault("1")
    @Column(name = "nstatus", nullable = false)
    private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

    // Transient fields
    @Transient private transient String sformnumber;
    @Transient private transient String sprojecttitle;
    @Transient private transient String sreceivingsitename;
    @Transient private transient String sproductname;
    @Transient private transient String stransactiondate;
    @Transient private transient short nreceiversitecode;

    @Override
    public BioBGSiECatalogueDetails mapRow(ResultSet rs, int rowNum) throws SQLException {
        BioBGSiECatalogueDetails obj = new BioBGSiECatalogueDetails();

        // PKs / FKs
        obj.setNbgsiecatdetailcode(getInteger(rs, "nbgsiecatdetailcode", rowNum));
        obj.setNbgsiecatrequestcode(getInteger(rs, "nbgsiecatrequestcode", rowNum));
        obj.setNbioprojectcode(getInteger(rs, "nbioprojectcode", rowNum));
        obj.setNproductcode(getInteger(rs, "nproductcode", rowNum));
        obj.setNsitecode(getShort(rs, "nsitecode", rowNum));

        // Domain fields
        obj.setSparentsamplecode(getString(rs, "sparentsamplecode", rowNum));
        obj.setNreqnoofsamples(getShort(rs, "nreqnoofsamples", rowNum));
        obj.setSreqminvolume(getString(rs, "sreqminvolume", rowNum));
        obj.setNaccnoofsamples(getShort(rs, "naccnoofsamples", rowNum));
        obj.setSaccminvolume(getString(rs, "saccminvolume", rowNum));
        obj.setDtransactiondate(getInstant(rs, "dtransactiondate", rowNum));
        obj.setNtztransactiondate(getInteger(rs, "ntztransactiondate", rowNum));
        obj.setNoffsetdtransactiondate(getInteger(rs, "noffsetdtransactiondate", rowNum));
        obj.setNstatus(getShort(rs, "nstatus", rowNum));

        // Transient / computed display fields
        obj.setSformnumber(getString(rs, "sformnumber", rowNum));
        obj.setSprojecttitle(getString(rs, "sprojecttitle", rowNum));
        obj.setSreceivingsitename(getString(rs, "sreceivingsitename", rowNum));
        obj.setSproductname(getString(rs, "sproductname", rowNum));
        obj.setStransactiondate(getString(rs, "stransactiondate", rowNum));
        obj.setNreceiversitecode(getShort(rs, "nreceiversitecode", rowNum));


        return obj;
    }

}
