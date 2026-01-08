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
 * This class maps the fields of 'biosamplereceivinghistory' table in the database.
 * It is used to track user and deputy actions performed on a biosample receiving record.
 */
@Entity
@Table(name = "biosamplereceivinghistory")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class BioSampleReceivingHistory extends CustomizedResultsetRowMapper<BioSampleReceivingHistory>
        implements Serializable, RowMapper<BioSampleReceivingHistory> {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "nbiosamplereceivinghistorycode", nullable = false)
    private int nbiosamplereceivinghistorycode;

    @Column(name = "nbiosamplereceivingcode", nullable = false)
    private int nbiosamplereceivingcode;

    @ColumnDefault("-1")
    @Column(name = "nusercode", nullable = false)
    private int nusercode = Enumeration.TransactionStatus.NA.gettransactionstatus();

    @ColumnDefault("-1")
    @Column(name = "nuserrolecode", nullable = false)
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

    @ColumnDefault("-1")
    @Column(name = "ntztransactiondate", nullable = false)
    private short ntztransactiondate = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

    @ColumnDefault("0")
    @Column(name = "noffsettransactiondate", nullable = false)
    private int noffsettransactiondate = Enumeration.TransactionStatus.NON_EMPTY.gettransactionstatus();

    @ColumnDefault("-1")
    @Column(name = "nsitecode", nullable = false)
    private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

    @ColumnDefault("1")
    @Column(name = "nstatus", nullable = false)
    private int nstatus = Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

    // Transient fields for display purposes
    @Transient
    private transient String susername;

    @Transient
    private transient String suserrolename;

    @Transient
    private transient String sdeputyusername;

    @Transient
    private transient String sdeputyuserrolename;

    @Transient
    private transient String stransactiondate;

    @Transient
    private transient String stztransactiondate;

    @Override
    public BioSampleReceivingHistory mapRow(ResultSet arg0, int arg1) throws SQLException {
        BioSampleReceivingHistory obj = new BioSampleReceivingHistory();

        obj.setNbiosamplereceivinghistorycode(getInteger(arg0, "nbiosamplereceivinghistorycode", arg1));
        obj.setNbiosamplereceivingcode(getInteger(arg0, "nbiosamplereceivingcode", arg1));
        obj.setNusercode(getInteger(arg0, "nusercode", arg1));
        obj.setNuserrolecode(getInteger(arg0, "nuserrolecode", arg1));
        obj.setNdeputyusercode(getInteger(arg0, "ndeputyusercode", arg1));
        obj.setNdeputyuserrolecode(getInteger(arg0, "ndeputyuserrolecode", arg1));
        obj.setNtransactionstatus(getInteger(arg0, "ntransactionstatus", arg1));
        obj.setScomments(getString(arg0, "scomments", arg1));
        obj.setDtransactiondate(getInstant(arg0, "dtransactiondate", arg1));
        obj.setNtztransactiondate(getShort(arg0, "ntztransactiondate", arg1));
        obj.setNoffsettransactiondate(getInteger(arg0, "noffsettransactiondate", arg1));
        obj.setNsitecode(getShort(arg0, "nsitecode", arg1));
        obj.setNstatus(getInteger(arg0, "nstatus", arg1));

        obj.setSusername(getString(arg0, "susername", arg1));
        obj.setSuserrolename(getString(arg0, "suserrolename", arg1));
        obj.setSdeputyusername(getString(arg0, "sdeputyusername", arg1));
        obj.setSdeputyuserrolename(getString(arg0, "sdeputyuserrolename", arg1));
        obj.setStransactiondate(getString(arg0, "stransactiondate", arg1));
        obj.setStztransactiondate(getString(arg0, "stztransactiondate", arg1));

        return obj;
    }
}
