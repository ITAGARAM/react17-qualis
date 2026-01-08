package com.agaramtech.qualis.basemaster.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.text.StringEscapeUtils;
import org.springframework.jdbc.core.RowMapper;

import com.agaramtech.qualis.global.CustomizedResultsetRowMapper;

import lombok.Data;

@Data
public class Printer extends CustomizedResultsetRowMapper<Printer> implements Serializable, RowMapper<Printer> {

	
	private static final long serialVersionUID = 1L;
	public int nprintercode;
	public String spath;
	public String sprintermodel;
	public String sprintername;
	
	
	@Override
	public Printer mapRow(ResultSet arg0, int arg1) throws SQLException {
		final Printer printer = new Printer();
		printer.setNprintercode(getInteger(arg0, "nprintercode", arg1));
		printer.setSpath(StringEscapeUtils.unescapeJava(getString(arg0, "spath", arg1)));
		printer.setSprintermodel(StringEscapeUtils.unescapeJava(getString(arg0, "sprintermodel", arg1)));
		printer.setSprintername(StringEscapeUtils.unescapeJava(getString(arg0, "sprintername", arg1)));

		return printer;
	}

}
