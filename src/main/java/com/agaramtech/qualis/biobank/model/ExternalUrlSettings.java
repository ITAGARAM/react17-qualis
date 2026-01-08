
package com.agaramtech.qualis.biobank.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Map;

import org.hibernate.annotations.ColumnDefault;
import org.springframework.jdbc.core.RowMapper;

import com.agaramtech.qualis.global.CustomizedResultsetRowMapper;
import com.agaramtech.qualis.global.Enumeration;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "externalurlsettings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ExternalUrlSettings extends CustomizedResultsetRowMapper<ExternalUrlSettings>
        implements Serializable, RowMapper<ExternalUrlSettings> {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "nexternalurlsettingcode", nullable = false)
    private short nexternalurlsettingcode;

    @Column(name = "sexternalurl", length = 255)
    private String sexternalurl;

    @Lob
    @Column(name = "jsondata", columnDefinition = "jsonb")
    private Map<String, Object> jsondata;

    @Column(name = "sdescription", length = 100)
    private String sdescription;

    @Column(name = "dmodifieddate", nullable = false)
    private Instant dmodifieddate;

    @ColumnDefault("-1")
    @Column(name = "nsitecode", nullable = false)
    private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

    @ColumnDefault("1")
    @Column(name = "nstatus", nullable = false)
    private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();


    @Override
    public ExternalUrlSettings mapRow(ResultSet rs, int rowNum) throws SQLException {
        final ExternalUrlSettings obj = new ExternalUrlSettings();

        obj.setNexternalurlsettingcode(getShort(rs, "nexternalurlsettingcode", rowNum));
        obj.setSexternalurl(getString(rs, "sexternalurl", rowNum));
        obj.setJsondata(unescapeString(getJsonObject(rs, "jsondata", rowNum)));
        obj.setSdescription(getString(rs, "sdescription", rowNum));
        obj.setDmodifieddate(getInstant(rs, "dmodifieddate", rowNum));
        obj.setNsitecode(getShort(rs, "nsitecode", rowNum));
        obj.setNstatus(getShort(rs, "nstatus", rowNum));

        return obj;
    }
}
