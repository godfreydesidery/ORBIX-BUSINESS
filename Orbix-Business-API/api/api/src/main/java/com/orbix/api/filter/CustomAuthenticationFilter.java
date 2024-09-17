/**
 * 
 */
package com.orbix.api.filter;

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
import com.orbix.api.repositories.UserRepository;
import com.orbix.api.service.UserService;
import com.orbix.api.service.UserServiceImpl;


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
	
	public CustomAuthenticationFilter(AuthenticationManager authenticationManager, UserRepository userRepository) {
		this.authenticationManager = authenticationManager;
		this.userRepository = userRepository;
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
		
		Algorithm algorithm = Algorithm.HMAC256("secret".getBytes());
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
		
		//can use this segment to register login activity
		
		new ObjectMapper().writeValue(response.getOutputStream(), tokens);
	}

	
}
