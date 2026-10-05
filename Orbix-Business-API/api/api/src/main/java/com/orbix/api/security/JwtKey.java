package com.orbix.api.security;

import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;

/**
 * The key that signs and verifies login tokens, defined in one place.
 *
 * It is read from the ORBIX_JWT_SECRET environment variable. Until that variable is set on the server,
 * the key used so far applies, so existing sessions and behaviour are unchanged. Setting the variable
 * rotates the key, and every user signs in again once.
 */
public final class JwtKey {

	public static final String ENVIRONMENT_VARIABLE = "ORBIX_JWT_SECRET";
	private static final String PREVIOUS_KEY = "secret";

	private static final Logger log = LoggerFactory.getLogger(JwtKey.class);

	// Built once and reused: the algorithm and verifier are immutable and thread-safe
	public static final Algorithm ALGORITHM = Algorithm.HMAC256(key().getBytes(StandardCharsets.UTF_8));
	public static final JWTVerifier VERIFIER = JWT.require(ALGORITHM).build();

	private JwtKey() {
	}

	private static String key() {
		String key = System.getenv(ENVIRONMENT_VARIABLE);
		if(key == null || key.isBlank()) {
			log.warn("{} is not set: login tokens are signed with the previous built-in key. Set it to a long random value.", ENVIRONMENT_VARIABLE);
			return PREVIOUS_KEY;
		}
		return key;
	}
}
