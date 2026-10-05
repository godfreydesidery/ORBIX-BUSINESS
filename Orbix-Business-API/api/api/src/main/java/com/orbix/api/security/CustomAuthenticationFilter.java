/**
 * 
 */
package com.orbix.api.security;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.bind.annotation.CrossOrigin;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.orbix.api.exceptions.InvalidOperationException;
import com.orbix.api.modules.audit.AuditLogService;
import com.orbix.api.modules.audit.AuditLogServiceController;
import com.orbix.api.modules.audit.AuditRequests;
import com.orbix.api.modules.identityandaccess.UserRepository;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;

/**
 * @author GODFREY
 *
 */
//@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class CustomAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
	
	private final UserRepository userRepository;

	private final AuthenticationManager authenticationManager;

	private final AuditLogService auditLogService;
	
	public CustomAuthenticationFilter(AuthenticationManager authenticationManager, UserRepository userRepository, AuditLogService auditLogService) {
		this.authenticationManager = authenticationManager;
		this.userRepository = userRepository;
		this.auditLogService = auditLogService;
	}
	
	@Override
	public Authentication attemptAuthentication(
			HttpServletRequest request, 
			HttpServletResponse response)
			throws AuthenticationException {
		String username = request.getParameter("username");
		String password = request.getParameter("password");		
		UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(username, password);
		return authenticationManager.authenticate(authenticationToken);
	}

	@Override
	protected void successfulAuthentication(
			HttpServletRequest request, 
			HttpServletResponse response, 
			FilterChain chain,
			Authentication authentication) throws IOException, ServletException {	
		User user = (User)authentication.getPrincipal();
		
		Algorithm algorithm = JwtKey.ALGORITHM;
		String access_token = JWT.create()
				.withSubject(user.getUsername())
				.withExpiresAt(new Date(System.currentTimeMillis()+8*60*60*1000))
				.withIssuer(request.getRequestURI().toString())
				.withClaim("privileges", user.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()))
				.sign(algorithm);
		
		String refresh_token = JWT.create()
				.withSubject(user.getUsername())
				.withExpiresAt(new Date(System.currentTimeMillis()+24*60*60*1000))
				.withIssuer(request.getRequestURI().toString())
				.sign(algorithm);
		
		Map<String, String> tokens = new HashMap<>();
		tokens.put("access_token", access_token);
		tokens.put("refresh_token", refresh_token);
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		
		//com.orbix.api.domain.User _user = userRepository.findByUsername(request.getParameter("username")).get();
		//_user.setAuthorizationToken(access_token.substring(access_token.length() - 20));
		//userRepository.save(_user);
		
		// Sign-in recorded in the audit log, in the background
		auditLogService.recordAuth("LOGIN_SUCCESS", AuditLogServiceController.SUCCESS, user.getUsername(), null,
				AuditRequests.ipAddress(request), AuditRequests.forwardedFor(request), AuditRequests.userAgent(request));
		
		new ObjectMapper().writeValue(response.getOutputStream(), tokens);
	}
	
	@Override
	protected void unsuccessfulAuthentication(
			HttpServletRequest request, 
			HttpServletResponse response,
			AuthenticationException failed) throws IOException, ServletException {
		// Failed sign-in recorded in the audit log, in the background; the response is unchanged
		try {
			auditLogService.recordAuth("LOGIN_FAILED", AuditLogServiceController.FAILURE, request.getParameter("username"), failed.getMessage(),
					AuditRequests.ipAddress(request), AuditRequests.forwardedFor(request), AuditRequests.userAgent(request));
		}catch(Exception e) {
			// Recording must never change the response
		}
		super.unsuccessfulAuthentication(request, response, failed);
	}

	
}
