package com.orbix.api.api.vehicleandequipmentparking;

import java.util.Base64;

import com.orbix.api.exceptions.InvalidEntryException;

/**
 * Turns a signature sent by the browser (a base64 PNG, with or without its "data:image/png;base64," prefix) into
 * bytes, refusing anything that is not a small PNG. The browser sends a tiny black and white image, a few hundred
 * bytes, so the cap leaves plenty of room while keeping large pictures out of the database.
 */
final class SignatureImages {

	static final int MAX_BYTES = 5 * 1024;

	private static final byte[] PNG_SIGNATURE = {(byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1A, '\n'};

	private SignatureImages() {}

	static boolean isPresent(String encoded) {
		return encoded != null && !encoded.trim().isEmpty();
	}

	/** The PNG bytes of the given signature, which must be present */
	static byte[] decode(String encoded, String whose) {
		if(!isPresent(encoded)) {
			throw new InvalidEntryException(whose + " signature is required");
		}
		String data = encoded.trim();
		int comma = data.indexOf(',');
		if(data.startsWith("data:") && comma > 0) {
			data = data.substring(comma + 1);
		}
		byte[] bytes;
		try {
			bytes = Base64.getDecoder().decode(data);
		}catch(IllegalArgumentException e) {
			throw new InvalidEntryException(whose + " signature could not be read");
		}
		if(bytes.length > MAX_BYTES) {
			throw new InvalidEntryException(whose + " signature is too large");
		}
		if(bytes.length < PNG_SIGNATURE.length) {
			throw new InvalidEntryException(whose + " signature is not an image");
		}
		for(int i = 0; i < PNG_SIGNATURE.length; i++) {
			if(bytes[i] != PNG_SIGNATURE[i]) {
				throw new InvalidEntryException(whose + " signature is not a PNG image");
			}
		}
		return bytes;
	}

	static String encode(byte[] bytes) {
		return bytes == null ? null : "data:image/png;base64," + Base64.getEncoder().encodeToString(bytes);
	}
}
