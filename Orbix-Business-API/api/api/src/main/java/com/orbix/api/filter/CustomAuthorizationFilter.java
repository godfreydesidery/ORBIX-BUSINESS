/**
 * 
 */
package com.orbix.api.filter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Arrays;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.http.HttpStatus;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.orbix.api.domain.Role;
import com.orbix.api.domain.User;
import com.orbix.api.exceptions.InvalidOperationException;
import com.orbix.api.exceptions.NotFoundException;
import com.orbix.api.repositories.DayRepository;
import com.orbix.api.repositories.UserRepository;
import com.orbix.api.service.DayService;
import com.orbix.api.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author GODFREY
 *
 */
@Slf4j
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class CustomAuthorizationFilter extends OncePerRequestFilter {
	
	private final UserRepository userRepository;

	@Override
	protected void doFilterInternal(
			HttpServletRequest request, 
			HttpServletResponse response, 
			FilterChain filterChain)
			throws ServletException, IOException {
		if(request.getServletPath().equals("**/api/login") || request.getServletPath().equals("**/api/token/refresh")) {
			filterChain.doFilter(request, response);
		} else {
			
			String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
			if(authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
				try {
					String token = authorizationHeader.substring("Bearer ".length());
					Algorithm algorithm = Algorithm.HMAC256("secret".getBytes());
					JWTVerifier verifier = JWT.require(algorithm).build();
					DecodedJWT decodedJWT = verifier.verify(token);
					String username = decodedJWT.getSubject();
					String[] roles = decodedJWT.getClaim("privileges").asArray(String.class);
					Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
					Arrays.stream(roles).forEach(role -> {
						authorities.add(new SimpleGrantedAuthority(role));
					});
					/*Optional<User> _user = userRepository.findByUsername(username);
					if(_user.isEmpty()) {
						throw new NotFoundException("User not found in database");
					}
					if(token.length() == 0) {
						throw new InvalidOperationException("Invalid token");
					}else if(!_user.get().getAuthorizationToken().equals(token.substring(token.length() - 20))) {
						throw new InvalidOperationException("Invalid token");
					}*/
					UsernamePasswordAuthenticationToken authenticationToken = 
							new UsernamePasswordAuthenticationToken(username, null, authorities);
					SecurityContextHolder.getContext().setAuthentication(authenticationToken);
					filterChain.doFilter(request, response);
				}catch(Exception exception) {
					exception.printStackTrace();
					log.info("Error logging in: {}", exception.getMessage());
					response.setHeader("error", exception.getMessage());
					response.setStatus(HttpStatus.FORBIDDEN.value());
					Map<String, String> error = new HashMap<>();
					error.put("error_message", exception.getMessage());
					response.setContentType(MediaType.APPLICATION_JSON_VALUE);
					new ObjectMapper().writeValue(response.getOutputStream(), error);					
				}									
			}else {
				 filterChain.doFilter(request, response);
			}
		}		
	}	
}
