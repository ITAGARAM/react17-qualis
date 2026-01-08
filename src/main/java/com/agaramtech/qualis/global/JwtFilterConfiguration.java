package com.agaramtech.qualis.global;

import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

//Class created by gowtham on 18 July, ALPDJ21-27 - JWT

public class JwtFilterConfiguration extends OncePerRequestFilter {

	private final JwtUtilityFunction jwtUtilityFunction;
	private final List<ApiEndPoint> dynamicUrls;
	private static final Logger LOGGER = LoggerFactory.getLogger(JwtFilterConfiguration.class);
	
	//#SECURITY-VULNERABILITY-MERGING-START
	private final Integer nrateLimitPerMinute;
	private final Integer nrateLimitPerSecond;
	private final Integer nrateLimitPerDay;
	//#SECURITY-VULNERABILITY-MERGING-END
	
	private final ConcurrentHashMap<String, Bucket> rateLimiterMap = new ConcurrentHashMap<>();
	private final Set<String> usedRequestTokens = ConcurrentHashMap.newKeySet();

	public JwtFilterConfiguration(JwtUtilityFunction jwtUtilityFunction, List<ApiEndPoint> dynamicUrls,
			Integer nrateLimitPerMinute, Integer nrateLimitPerSecond, Integer nrateLimitPerDay) {
		super();
		this.jwtUtilityFunction = jwtUtilityFunction;
		this.dynamicUrls = dynamicUrls;
		this.nrateLimitPerMinute = nrateLimitPerMinute;
		this.nrateLimitPerSecond = nrateLimitPerSecond;
		this.nrateLimitPerDay = nrateLimitPerDay;
	}

	private Bucket createBucket() {
		final Bandwidth perSecond = Bandwidth.classic(nrateLimitPerSecond, Refill.greedy(nrateLimitPerSecond, Duration.ofSeconds(1)));
		final Bandwidth perDay = Bandwidth.classic(nrateLimitPerDay, Refill.intervally(nrateLimitPerDay, Duration.ofMinutes(nrateLimitPerMinute)));

		return Bucket.builder().addLimit(perSecond).addLimit(perDay).build();
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		if (!request.getMethod().toString().equals(HttpMethod.OPTIONS.name())) {

			// RATE LIMITING (Before anything else)
		String token = request.getHeader("Authorization");
		boolean isValid = false;
		LOGGER.info("Token---> "+ token);
		//#SECURITY-VULNERABILITY-MERGING-START
		if (token != null && !token.equals("")) {
			final Bucket bucket = rateLimiterMap.computeIfAbsent(token, k -> createBucket());

			if (!bucket.tryConsume(1)) {
				response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
				sendErrorResponse(response, "Rate limit exceeded");
				return;
			}

			// UNIQUE REQUEST TOKEN VALIDATION
			final String reqToken = request.getHeader("X-Request-Token");
			if (reqToken == null || reqToken.isBlank()) {
				response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
				sendErrorResponse(response, "Missing X-Request-Token");
				return;
			}

			// reject reused tokens
			if (!usedRequestTokens.add(reqToken)) {
				response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
				sendErrorResponse(response, "Replay detected: request token already used");
				return;
			}

			if (usedRequestTokens.size() > 10000) {
				usedRequestTokens.clear();
			}
		}
		//#SECURITY-VULNERABILITY-MERGING-END
		if (token != null && token.startsWith("Bearer")) {
			token = token.substring(token.length() > 6 ? 7 : 6);
			final String sessionId = token.trim().length() > 0 ? jwtUtilityFunction.extractSessionId(token) : null;
			// Dynamic Token Validation
				if ((sessionId != null && SecurityContextHolder.getContext().getAuthentication() == null)
						&& jwtUtilityFunction.isTokenValid(token)) {
				isValid = true;
					final UsernamePasswordAuthenticationToken authtoken = new UsernamePasswordAuthenticationToken(
							sessionId, null, null);
				java.util.Enumeration<String> data = request.getParameterNames();
				LOGGER.info("Request ------->"+request.getRemoteAddr());
				while(data.hasMoreElements()) {
					LOGGER.info("Request ------->"+data.nextElement());
				}				
				authtoken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
				SecurityContextHolder.getContext().setAuthentication(authtoken);
			} else {
			// Static Token Validation
				final String requestUri = request.getServletPath();
				LOGGER.info("RequestURI---> "+requestUri);
				final Optional<ApiEndPoint> urlObj = dynamicUrls.stream().filter(item -> item.getSapiendpoint().equals(requestUri)).findFirst();
				LOGGER.info("URL OBj---> "+urlObj.toString());
				LOGGER.info("URL isPresent---> "+urlObj.isPresent());
				if (urlObj.isPresent()) {
					LOGGER.info("URL isPresent1---> "+urlObj.isPresent());
					LOGGER.info("URL OBj1---> "+urlObj);
					LOGGER.info("getSstatictoken---> "+token.equals(urlObj.get().getSstatictoken()));
					LOGGER.info("getNreadstatictoken---> "+urlObj.get().getNreadstatictoken());
					LOGGER.info("Token Check ->"+token.equals(urlObj.get().getSstatictoken()));
					if (urlObj.get().getNreadstatictoken() == Enumeration.TransactionStatus.NO.gettransactionstatus()
							|| (urlObj.get().getNreadstatictoken() == Enumeration.TransactionStatus.YES.gettransactionstatus() 
									&& token.equals(urlObj.get().getSstatictoken()))) {
						
						LOGGER.info("condition---> "+(urlObj.get().getNreadstatictoken() == Enumeration.TransactionStatus.YES.gettransactionstatus() 
								&& token.equals(urlObj.get().getSstatictoken())));
						isValid = true;
							final UsernamePasswordAuthenticationToken authtoken = new UsernamePasswordAuthenticationToken(
									"TEMPTOKEN", null, null);
						authtoken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
						SecurityContextHolder.getContext().setAuthentication(authtoken);
					}	
				}
			}
			if (sessionId == null && token != "" && isValid == false) {
				LOGGER.info("SessionID---> "+sessionId);
				
				//#SECURITY-VULNERABILITY-MERGING-START
				/*
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				*/
				response.setStatus(HttpStatus.UNAUTHORIZED.value());
				//#SECURITY-VULNERABILITY-MERGING-END
				
				sendErrorResponse(response, "Invalid JWT Token");
				return;
			}
		} else {
				final String requestUri = request.getServletPath();
				final Optional<ApiEndPoint> urlObj = dynamicUrls.stream()
						.filter(item -> item.getSapiendpoint().equals(requestUri)).findFirst();
			if (urlObj.isPresent()) {
				if (urlObj.get().getNreadstatictoken() == Enumeration.TransactionStatus.NO.gettransactionstatus()) {
						final UsernamePasswordAuthenticationToken authtoken = new UsernamePasswordAuthenticationToken(
								"TEMPTOKEN", null, null);
					authtoken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
					SecurityContextHolder.getContext().setAuthentication(authtoken);
				}	
			}
		}
		}
		filterChain.doFilter(request, response);
	}
	
	private void sendErrorResponse(HttpServletResponse response, String message) throws IOException {
        response.setContentType("application/json");
        final Map<String, Object> error = new HashMap<>();
        error.put("StatusCode", response.getStatus());
        error.put("Message", message);
        new ObjectMapper().writeValue(response.getOutputStream(), error);
    }
}
