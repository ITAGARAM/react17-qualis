package com.agaramtech.qualis.global;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * This class is used to map the fields of 'unit' table of the Database.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RateLimiter{
	
	private int nratelimitpersecond;
	private int nratelimitperday;
	
	public RateLimiter(RateLimiter rateLimiter) {
		super();
		this.nratelimitpersecond = rateLimiter.nratelimitpersecond;
		this.nratelimitperday = rateLimiter.nratelimitperday;
	}

}