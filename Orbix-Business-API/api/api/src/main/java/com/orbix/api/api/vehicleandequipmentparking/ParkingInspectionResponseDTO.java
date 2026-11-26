package com.orbix.api.api.vehicleandequipmentparking;

import java.util.List;

import lombok.Data;

/** The devices and signatures of one parking, for its View; kept out of the lists, which never need them */
@Data
public class ParkingInspectionResponseDTO {

	String parkingId;
	List<ParkingDeviceDTO> devices;
	List<Signature> signatures;

	@Data
	public static class Signature {
		String stage;
		String role;
		String signerName;
		String signedAt;
		/** data:image/png;base64,... */
		String image;
		/** MODIFICATION only: one "Field: old → new" per line */
		String changes;
	}
}
