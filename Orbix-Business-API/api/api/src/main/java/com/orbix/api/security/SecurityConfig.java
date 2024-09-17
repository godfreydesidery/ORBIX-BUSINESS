/**
 * 
 */
package com.orbix.api.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.orbix.api.filter.CustomAuthenticationFilter;
import com.orbix.api.filter.CustomAuthorizationFilter;
import com.orbix.api.repositories.UserRepository;

import lombok.RequiredArgsConstructor;

/**
 * @author GODFREY
 *
 */
@Configuration
@EnableWebSecurity
@EnableWebMvc
@EnableGlobalMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig extends WebSecurityConfigurerAdapter implements WebMvcConfigurer {
	
	private final UserDetailsService userDetailsService;
	private final BCryptPasswordEncoder bCryptPasswordEncoder;
	
	private final UserRepository userRepository;
	
	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {		
		auth.userDetailsService(userDetailsService).passwordEncoder(bCryptPasswordEncoder);
	}
	
	@Override
	public void configure(WebSecurity web) throws Exception {
	    web.ignoring().antMatchers(
	    		"/zana-hmis-api/auth/**",
	    		"/v2/api-docs",
	    		"/wms/**", 
	            "/configuration/ui", 
	            "/swagger-resources", 
	            "/configuration/security",
	            "/swagger-ui.html", 
	            "/webjars/**",
	            "/favicon.ico",
	            "/**/*.png",
	            "/**/*.gif",
	            "/**/*.svg",
	            "/**/*.jpg",
	            "/**/*.html",
	            "/**/*.css",
	            "/**/*.js");
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		CustomAuthenticationFilter customAuthenticationFilter = new CustomAuthenticationFilter(authenticationManagerBean(), userRepository);
		customAuthenticationFilter.setFilterProcessesUrl("/zana-hmis-api/login");
		http.cors();
		http.csrf().disable();
		http.anonymous().disable();
		http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
		http.authorizeRequests()
		//Public end points
		.antMatchers("/v2/api-docs").permitAll()
		.antMatchers("/swagger-ui.html").permitAll()
		.antMatchers("/swagger-ui").permitAll()
		.antMatchers("/zana-hmis-api/login/**").permitAll()
		.antMatchers("/zana-hmis-api/token/refresh/**").permitAll()	
		.antMatchers("/wms/**").permitAll();
		//Private endpoints
		//.anyRequest().authenticated();
		
		//http.authorizeRequests().antMatchers(HttpMethod.GET, "/api/users/users").hasAnyAuthority("dfgh");
		
		http.addFilter(customAuthenticationFilter);
		http.addFilterBefore(new CustomAuthorizationFilter(userRepository), UsernamePasswordAuthenticationFilter.class);
	}
	
	public WebMvcConfigurer corsConfigurer() {
		return new WebMvcConfigurer() {
			@Override
			public void addCorsMappings(CorsRegistry registry) {
				registry.addMapping("/**")
				.allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE")
				.allowedHeaders("*")
				.allowedOrigins("*");
			}
		};
	}
	
	@Bean
	@Override
	public AuthenticationManager authenticationManagerBean() throws Exception {
		return super.authenticationManagerBean();
	}
	
}
