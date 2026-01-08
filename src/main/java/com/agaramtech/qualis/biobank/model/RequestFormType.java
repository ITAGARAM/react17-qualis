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
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "requestformtype")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class RequestFormType extends CustomizedResultsetRowMapper<RequestFormType> implements Serializable, RowMapper<RequestFormType> {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "nreqformtypecode", nullable = false)
    private short nreqformtypecode;

    @Column(name = "sreqformtypename", length = 50, nullable = false)
    private String sreqformtypename;

    @Lob
    @Column(name = "jsondata", columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> jsondata;

    @Column(name = "dmodifieddate")
    private Instant dmodifieddate;

    @ColumnDefault("-1")
    @Column(name = "nsitecode", nullable = false)
    private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

    @ColumnDefault("1")
    @Column(name = "nstatus", nullable = false)
    private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

    @Override
    public RequestFormType mapRow(ResultSet rs, int rowNum) throws SQLException {

        final RequestFormType obj = new RequestFormType();

        obj.setNreqformtypecode(getShort(rs, "nreqformtypecode", rowNum));
        obj.setSreqformtypename(getString(rs, "sreqformtypename", rowNum));
        obj.setJsondata(getJsonObject(rs, "jsondata", rowNum));
        obj.setDmodifieddate(getInstant(rs, "dmodifieddate", rowNum));
        obj.setNsitecode(getShort(rs, "nsitecode", rowNum));
        obj.setNstatus(getShort(rs, "nstatus", rowNum));

        return obj;
    }
}
