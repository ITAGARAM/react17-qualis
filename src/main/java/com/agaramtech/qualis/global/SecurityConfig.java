package com.agaramtech.qualis.global;

import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import com.agaramtech.qualis.configuration.model.Settings;

// Class created by gowtham on 18 July, ALPDJ21-27 - JWT
@Configuration
@EnableWebSecurity
public class SecurityConfig {

	private final JwtUtilityFunction jwtUtilityFunction;
	private final JdbcTemplate jdbcTemplate;

	public SecurityConfig(JwtUtilityFunction jwtUtilityFunction, JdbcTemplate jdbcTemplate) {
		this.jwtUtilityFunction = jwtUtilityFunction;
		this.jdbcTemplate = jdbcTemplate;
	}

	private List<ApiEndPoint> getApiEndPoints() {
		return jdbcTemplate.query("select sapiendpoint, sstatictoken, nreadstatictoken from apiendpoint where nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(), new ApiEndPoint());
	}

	private List<Settings> getRateLimitParams() throws Exception {
		return jdbcTemplate.query("select nsettingcode, ssettingvalue from settings where nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsettingcode in ("
				+ Enumeration.Settings.JWT_EXPIRYTIME.getNsettingcode() + ","
				+ Enumeration.Settings.RATELIMITPERSECOND.getNsettingcode() + ","
				+ Enumeration.Settings.RATELIMITPERDAY.getNsettingcode() + ")", new Settings());
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		final List<ApiEndPoint> dynamicUrls = getApiEndPoints();
		final List<Settings> settingsList = getRateLimitParams();
		
		final Settings perMinuteSetting = settingsList.stream()
				.filter(s -> s.getNsettingcode() == Enumeration.Settings.JWT_EXPIRYTIME.getNsettingcode())
				.findFirst().orElse(null);
		final Integer nrateLimitPerMinute = Integer.parseInt(perMinuteSetting.getSsettingvalue());
		
		final Settings perSecondSetting = settingsList.stream()
				.filter(s -> s.getNsettingcode() == Enumeration.Settings.RATELIMITPERSECOND.getNsettingcode())
				.findFirst().orElse(null);
		final Integer nrateLimitPerSecond = Integer.parseInt(perSecondSetting.getSsettingvalue());
		
		final Settings perDaySetting = settingsList.stream()
				.filter(s -> s.getNsettingcode() == Enumeration.Settings.RATELIMITPERDAY.getNsettingcode())
				.findFirst().orElse(null);
		final Integer nrateLimitPerDay = Integer.parseInt(perDaySetting.getSsettingvalue());
		
		final JwtFilterConfiguration jwtFilterObj = new JwtFilterConfiguration(jwtUtilityFunction, dynamicUrls,
				nrateLimitPerMinute, nrateLimitPerSecond, nrateLimitPerDay);
		return http.csrf(csrf -> csrf.disable())
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(auth -> {
					auth.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll();
					auth.anyRequest().authenticated();
				}).addFilterBefore(jwtFilterObj, UsernamePasswordAuthenticationFilter.class).build();
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
		return config.getAuthenticationManager();
	}

}
