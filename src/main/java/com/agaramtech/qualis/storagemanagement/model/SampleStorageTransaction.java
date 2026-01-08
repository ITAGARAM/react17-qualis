package com.agaramtech.qualis.storagemanagement.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Map;

import org.apache.commons.text.StringEscapeUtils;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.jdbc.core.RowMapper;

import com.agaramtech.qualis.global.CustomizedResultsetRowMapper;
import com.agaramtech.qualis.global.Enumeration;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * SampleStorageTransaction entity / row mapper
 */
@Table(name = "samplestoragetransaction")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class SampleStorageTransaction extends CustomizedResultsetRowMapper<SampleStorageTransaction>
        implements Serializable, RowMapper<SampleStorageTransaction> {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "nsamplestoragetransactioncode", nullable = false)
    private int nsamplestoragetransactioncode;

    @Column(name = "nsamplestoragelocationcode", nullable = false)
    private short nsamplestoragelocationcode;

    @Column(name = "nsamplestoragemappingcode", nullable = false)
    private int nsamplestoragemappingcode;

    @Column(name = "ninstrumentcode", nullable = false)
    @ColumnDefault("-1")
    private int ninstrumentcode = Enumeration.TransactionStatus.NA.gettransactionstatus();

    @Column(name = "nprojecttypecode", nullable = false)
    @ColumnDefault("-1")
    private int nprojecttypecode = Enumeration.TransactionStatus.NA.gettransactionstatus();

    @Column(name = "sposition", length = 100)
    private String sposition;

    @Column(name = "spositionvalue", length = 100)
    private String spositionvalue;

    @Lob
    @Column(name = "jsondata", columnDefinition = "jsonb")
    private Map<String, Object> jsondata;

    @Column(name = "npositionfilled", nullable = false)
    private short npositionfilled;

    @Column(name = "nbiosamplereceivingcode", nullable = false)
    @ColumnDefault("-1")
    private int nbiosamplereceivingcode = Enumeration.TransactionStatus.NA.gettransactionstatus();
    
    @Column(name = "nbioparentsamplecode", nullable = false)
    @ColumnDefault("-1")
    private int nbioparentsamplecode = Enumeration.TransactionStatus.NA.gettransactionstatus();

    @Column(name = "sparentsamplecode", length = 30)
    private String sparentsamplecode;

    @Column(name = "ncohortno", nullable = false)
    @ColumnDefault("-1")
    private short ncohortno = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

    @Column(name = "nproductcatcode", nullable = false)
    @ColumnDefault("-1")
    private int nproductcatcode = Enumeration.TransactionStatus.NA.gettransactionstatus();

    @Column(name = "nproductcode", nullable = false)
    @ColumnDefault("-1")
    private int nproductcode = Enumeration.TransactionStatus.NA.gettransactionstatus();

    @Column(name = "sqty", length = 10)
    private String sqty;

    @Column(name = "slocationcode", length = 100)
    private String slocationcode;

    @Column(name = "ssubjectid", length = 50)
    private String ssubjectid;

    @Column(name = "scasetype", length = 100)
    private String scasetype;

    @Column(name = "ndiagnostictypecode", nullable = false)
    @ColumnDefault("-1")
    private int ndiagnostictypecode = Enumeration.TransactionStatus.NA.gettransactionstatus();

    @Column(name = "ncontainertypecode", nullable = false)
    @ColumnDefault("-1")
    private short ncontainertypecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

    @Column(name = "nstoragetypecode", nullable = false)
    @ColumnDefault("-1")
    private int nstoragetypecode = Enumeration.TransactionStatus.NA.gettransactionstatus();

    @Column(name = "dmodifieddate")
    private Instant dmodifieddate;

    @Column(name = "nsitecode", nullable = false)
    @ColumnDefault("-1")
    private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

    @Column(name = "nstatus", nullable = false)
    @ColumnDefault("1")
    private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

    /* Transient/display fields kept from original class */
    @Transient
    private transient String ssamplestoragelocationname;

    @Transient
    private transient String ssamplestoragepathname;

    @Transient
    private transient String stosamplestoragelocationname;

    @Transient
    private transient String stosamplestoragepathname;

    @Transient
    private transient String sboxid;

    @Transient
    private transient String stoboxid;

    @Transient
    private transient String sproductcatname;

    @Transient
    private transient String sinstrumentid;

    @Transient
    private transient String sdiagnostictypename;

    @Transient
    private transient String scontainertype;

    @Transient
    private transient String sstoragetypename;

    @Transient
    private transient String sproductname;

    @Transient
    private transient String sprojecttitle;

    @Transient
    private transient String sdmodifieddate; 

    @Override
    public SampleStorageTransaction mapRow(ResultSet arg0, int arg1) throws SQLException {
        final SampleStorageTransaction t = new SampleStorageTransaction();

        t.setNsamplestoragetransactioncode(getInteger(arg0, "nsamplestoragetransactioncode", arg1));
        t.setNsamplestoragelocationcode(getShort(arg0, "nsamplestoragelocationcode", arg1));
        t.setNsamplestoragemappingcode(getInteger(arg0, "nsamplestoragemappingcode", arg1));
        t.setNinstrumentcode(getInteger(arg0, "ninstrumentcode", arg1));
        t.setNprojecttypecode(getInteger(arg0, "nprojecttypecode", arg1));

        t.setSposition(StringEscapeUtils.unescapeJava(getString(arg0, "sposition", arg1)));
        t.setSpositionvalue(StringEscapeUtils.unescapeJava(getString(arg0, "spositionvalue", arg1)));

        t.setNpositionfilled(getShort(arg0, "npositionfilled", arg1));
        t.setJsondata(unescapeString(getJsonObject(arg0, "jsondata", arg1)));

        t.setNbiosamplereceivingcode(getInteger(arg0, "nbiosamplereceivingcode", arg1));
        t.setSparentsamplecode(getString(arg0, "sparentsamplecode", arg1));
        t.setNcohortno(getShort(arg0, "ncohortno", arg1));
        t.setNproductcatcode(getInteger(arg0, "nproductcatcode", arg1));
        t.setNproductcode(getInteger(arg0, "nproductcode", arg1));
        t.setSqty(getString(arg0, "sqty", arg1));
        t.setSlocationcode(getString(arg0, "slocationcode", arg1));
        t.setSsubjectid(getString(arg0, "ssubjectid", arg1));
        t.setScasetype(getString(arg0, "scasetype", arg1));
        t.setNdiagnostictypecode(getInteger(arg0, "ndiagnostictypecode", arg1));
        t.setNcontainertypecode(getShort(arg0, "ncontainertypecode", arg1));
        t.setNstoragetypecode(getInteger(arg0, "nstoragetypecode", arg1));

        t.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
        t.setNsitecode(getShort(arg0, "nsitecode", arg1));
        t.setNstatus(getShort(arg0, "nstatus", arg1));

        t.setSsamplestoragelocationname(getString(arg0, "ssamplestoragelocationname", arg1));
        t.setSsamplestoragepathname(getString(arg0, "ssamplestoragepathname", arg1));
        t.setStosamplestoragelocationname(getString(arg0, "stosamplestoragelocationname", arg1));
        t.setStosamplestoragepathname(getString(arg0, "stosamplestoragepathname", arg1));
        t.setSboxid(getString(arg0, "sboxid", arg1));
        t.setStoboxid(getString(arg0, "stoboxid", arg1));

        t.setSproductcatname(getString(arg0, "sproductcatname", arg1));
        t.setSinstrumentid(getString(arg0, "sinstrumentid", arg1));
        t.setSdiagnostictypename(getString(arg0, "sdiagnostictypename", arg1));
        t.setScontainertype(getString(arg0, "scontainertype", arg1));
        t.setSstoragetypename(getString(arg0, "sstoragetypename", arg1));
        t.setSproductname(getString(arg0, "sproductname", arg1));
        t.setSprojecttitle(getString(arg0, "sprojecttitle", arg1));
        t.setSdmodifieddate(getString(arg0, "sdmodifieddate", arg1));
        t.setNbioparentsamplecode(getInteger(arg0, "nbioparentsamplecode", arg1));


        return t;
    }

}
