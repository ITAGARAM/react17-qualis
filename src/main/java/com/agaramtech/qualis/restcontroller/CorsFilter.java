package com.agaramtech.qualis.restcontroller;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
import com.agaramtech.qualis.global.Enumeration;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;

@Configuration
public class CorsFilter implements Filter {

	private static final Logger log = Logger.getAnonymousLogger();

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {

	}

	// Added by gowtham on 23 june, ALPDJ21-27 - JWT
	@Bean
	public FilterRegistrationBean<CorsFilter> corsFilterRegistration(CorsFilter corsFilter) {
		FilterRegistrationBean<CorsFilter> registration = new FilterRegistrationBean<>(corsFilter);
		registration.setOrder(Ordered.HIGHEST_PRECEDENCE); // ⬅️ Very important access public urls (before Login urls)
		return registration;
	}

	@Override
	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
			throws IOException, ServletException {
		String SECRET_KEY = Enumeration.NetworkEncryption.SECRET_KEY.getNetworkEncryption();
		log.info("Adding Access Control Response Headers");
		HttpServletResponse response = (HttpServletResponse) servletResponse;
		HttpServletRequest request = (HttpServletRequest) servletRequest;
		response.setHeader("Access-Control-Allow-Origin", "*");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Methods", "POST, GET, HEAD, OPTIONS");
		response.setHeader("Access-Control-Allow-Headers",
				"Authorization, Origin, Accept, X-Requested-With, X-Request-Token, Content-Type, Access-Control-Request-Method, Access-Control-Request-Headers");
//		boolean isMultipart = ServletFileUpload.isMultipartContent(request);	

//		if(!isMultipart){
//			ResettableStreamHttpServlet.ResettableStreamHttpServletRequest wrappedRequest = new ResettableStreamHttpServlet.ResettableStreamHttpServletRequest(
//					(HttpServletRequest) request);
//			String body = IOUtils.toString(wrappedRequest.getReader());
//			System.out.println(body);
//			wrappedRequest.resetInputStream();
//						
//			RequestDispatcher rd = request.getRequestDispatcher("/callService");
//			rd.include(wrappedRequest,response);
//			wrappedRequest.resetInputStream();		
//			filterChain.doFilter(wrappedRequest, response);
//		
//		}else{
		if (HttpMethod.OPTIONS.matches(request.getMethod())) {
		    response.setStatus(HttpServletResponse.SC_OK);
		    return;
		}

		String contentType = servletRequest.getContentType();
		if (contentType != null && contentType.toLowerCase().contains("multipart")) {
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			HttpServletResponseWrapper capturingResponse = new HttpServletResponseWrapper(response) {
				@Override
				public ServletOutputStream getOutputStream() {
					return new ServletOutputStream() {
						@Override
						public boolean isReady() {
							return true;
						}
						
						@Override
						public void setWriteListener(WriteListener writeListener) {
						}
						
						@Override
						public void write(int b) {
							buffer.write(b);
						}
					};
				}
				
				@Override
				public PrintWriter getWriter() {
					return new PrintWriter(new OutputStreamWriter(buffer, StandardCharsets.UTF_8));
				}
			};
			
			// Continue filters and controller using the decrypted request
			filterChain.doFilter(servletRequest, capturingResponse);
			
			String plainResponse = buffer.toString(StandardCharsets.UTF_8.name());
			
			if (plainResponse.trim().startsWith("{") || plainResponse.trim().startsWith("[")) {
				try {
					String encrypted = encryptCryptoJS(plainResponse, SECRET_KEY);
					String wrapped = "{\"data\":\"" + encrypted + "\"}";
					
					byte[] out = wrapped.getBytes(StandardCharsets.UTF_8);
					response.setContentLength(out.length);
					response.getOutputStream().write(out);
					return;
					
				} catch (Exception e) {
					throw new RuntimeException("Response encrypt failed", e);
				}
			}
			
			// Fallback: send original plaintext if not JSON
			byte[] out = plainResponse.getBytes(StandardCharsets.UTF_8);
			response.setContentLength(out.length);
			response.getOutputStream().write(out);
		
		} else {
			String body = request.getReader().lines().collect(Collectors.joining());
			HttpServletRequest decryptedRequest = request;
			
			if (body != null && body.contains("\"data\"")) {
				try {
					ObjectMapper mapper = new ObjectMapper();
					JsonNode node = mapper.readTree(body);
					String encrypted = node.get("data").asText();
					
					String decryptedJson = decryptCryptoJS(encrypted, SECRET_KEY);
					
					decryptedRequest = new HttpServletRequestWrapper(request) {
						private final byte[] raw = decryptedJson.getBytes(StandardCharsets.UTF_8);
						
						@Override
						public ServletInputStream getInputStream() {
							ByteArrayInputStream bais = new ByteArrayInputStream(raw);
							return new ServletInputStream() {
								@Override
								public int read() {
									return bais.read();
								}
								
								@Override
								public boolean isFinished() {
									return bais.available() == 0;
								}
								
								@Override
								public boolean isReady() {
									return true;
								}
								
								@Override
								public void setReadListener(ReadListener l) {
								}
							};
						}
						
						@Override
						public BufferedReader getReader() {
							return new BufferedReader(new InputStreamReader(getInputStream()));
						}
					};
					
				} catch (Exception e) {
					throw new RuntimeException("Request decrypt failed", e);
				}
			}
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			HttpServletResponseWrapper capturingResponse = new HttpServletResponseWrapper(response) {
				@Override
				public ServletOutputStream getOutputStream() {
					return new ServletOutputStream() {
						@Override
						public boolean isReady() {
							return true;
						}
						
						@Override
						public void setWriteListener(WriteListener writeListener) {
						}
						
						@Override
						public void write(int b) {
							buffer.write(b);
						}
					};
				}
				
				@Override
				public PrintWriter getWriter() {
					return new PrintWriter(new OutputStreamWriter(buffer, StandardCharsets.UTF_8));
				}
			};
			
			// Continue filters and controller using the decrypted request
			filterChain.doFilter(decryptedRequest, capturingResponse);
			
			String plainResponse = buffer.toString(StandardCharsets.UTF_8.name());
			
			if (!request.getServletPath().equals("/reststimulsoft/urljson")  // Added this condition by Gowtham on dec 2 2025 for SWSM-125 to not encrypt this data.
					&& (plainResponse.trim().startsWith("{") || plainResponse.trim().startsWith("["))) {
				try {
					String encrypted = encryptCryptoJS(plainResponse, SECRET_KEY);
					String wrapped = "{\"data\":\"" + encrypted + "\"}";
					
					byte[] out = wrapped.getBytes(StandardCharsets.UTF_8);
					response.setContentLength(out.length);
					response.getOutputStream().write(out);
					return;
					
				} catch (Exception e) {
					throw new RuntimeException("Response encrypt failed", e);
				}
			}
			
			// Fallback: send original plaintext if not JSON
			byte[] out = plainResponse.getBytes(StandardCharsets.UTF_8);
			response.setContentLength(out.length);
			response.getOutputStream().write(out);
//			filterChain.doFilter(servletRequest, servletResponse);
		}
	}

	private String decryptCryptoJS(String ciphertext, String password) throws Exception {
		byte[] ctBytes = Base64.getDecoder().decode(ciphertext);

		byte[] salt = Arrays.copyOfRange(ctBytes, 8, 16);
		byte[] encrypted = Arrays.copyOfRange(ctBytes, 16, ctBytes.length);

		byte[] keyIv = evpBytesToKey(password.getBytes("UTF-8"), salt, 32, 16);
		byte[] key = Arrays.copyOfRange(keyIv, 0, 32);
		byte[] iv = Arrays.copyOfRange(keyIv, 32, 48);

		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"), new IvParameterSpec(iv));

		return new String(cipher.doFinal(encrypted), "UTF-8");
	}

	private String encryptCryptoJS(String data, String password) throws Exception {
		SecureRandom rng = new SecureRandom();
		byte[] salt = new byte[8];
		rng.nextBytes(salt);

		byte[] keyIv = evpBytesToKey(password.getBytes("UTF-8"), salt, 32, 16);
		byte[] key = Arrays.copyOfRange(keyIv, 0, 32);
		byte[] iv = Arrays.copyOfRange(keyIv, 32, 48);

		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"), new IvParameterSpec(iv));

		byte[] encrypted = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		out.write("Salted__".getBytes());
		out.write(salt);
		out.write(encrypted);

		return Base64.getEncoder().encodeToString(out.toByteArray());
	}

	private byte[] evpBytesToKey(byte[] password, byte[] salt, int keyLen, int ivLen) throws Exception {
		MessageDigest md = MessageDigest.getInstance("MD5");
		byte[] keyIv = new byte[keyLen + ivLen];
		byte[] buffer = null;
		int offset = 0;

		while (offset < keyIv.length) {
			md.reset();
			if (buffer != null)
				md.update(buffer);
			md.update(password);
			md.update(salt);

			buffer = md.digest();

			int length = Math.min(buffer.length, keyIv.length - offset);
			System.arraycopy(buffer, 0, keyIv, offset, length);
			offset += length;
		}
		return keyIv;
	}

	@Override
	public void destroy() {

	}

//	private static class ResettableStreamHttpServletRequest extends HttpServletRequestWrapper {
//
//		private byte[] rawData;
//		private HttpServletRequest request;
//		private ResettableServletInputStream servletStream;
//
//		public ResettableStreamHttpServletRequest(HttpServletRequest request) {
//			super(request);
//			this.request = request;
//			this.servletStream = new ResettableServletInputStream();
//		}
//
//		public void resetInputStream() {
//			servletStream.stream = new ByteArrayInputStream(rawData);
//		}
//
//		@Override
//		public ServletInputStream getInputStream() throws IOException {
//			if (rawData == null) {
//				rawData = IOUtils.toByteArray(this.request.getReader());
//				servletStream.stream = new ByteArrayInputStream(rawData);
//			}
//			return servletStream;
//		}
//
//		@Override
//		public BufferedReader getReader() throws IOException {
//			if (rawData == null) {
//				rawData = IOUtils.toByteArray(this.request.getReader());
//				servletStream.stream = new ByteArrayInputStream(rawData);
//			}
//			return new BufferedReader(new InputStreamReader(servletStream));
//		}
//
//		private class ResettableServletInputStream extends ServletInputStream {
//
//			private InputStream stream;
//
//			@Override
//			public int read() throws IOException {
//				return stream.read();
//			}
//
//			@Override
//			public boolean isFinished() {
//				// TODO Auto-generated method stub
//				return false;
//			}
//
//			@Override
//			public boolean isReady() {
//				// TODO Auto-generated method stub
//				return false;
//			}
//
//			@Override
//			public void setReadListener(ReadListener arg0) {
//				// TODO Auto-generated method stub
//
//			}
//		}
//	}
}